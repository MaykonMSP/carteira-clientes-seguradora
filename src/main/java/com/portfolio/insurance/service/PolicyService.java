package com.portfolio.insurance.service;

import com.portfolio.insurance.domain.Customer;
import com.portfolio.insurance.domain.Insurer;
import com.portfolio.insurance.domain.Policy;
import com.portfolio.insurance.domain.PolicyStatus;
import com.portfolio.insurance.domain.PolicyType;
import com.portfolio.insurance.dto.PolicyRequest;
import com.portfolio.insurance.dto.PolicyResponse;
import com.portfolio.insurance.exception.BusinessException;
import com.portfolio.insurance.exception.ConflictException;
import com.portfolio.insurance.exception.NotFoundException;
import com.portfolio.insurance.mapper.PolicyMapper;
import com.portfolio.insurance.repository.CustomerRepository;
import com.portfolio.insurance.repository.InsurerRepository;
import com.portfolio.insurance.repository.PolicyRepository;
import com.portfolio.insurance.repository.spec.PolicySpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PolicyService {

    private static final int MAX_EXPIRING_DAYS = 366;
    private static final int STATUS_RECALCULATION_BATCH_SIZE = 500;

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final InsurerRepository insurerRepository;

    public PolicyService(PolicyRepository policyRepository,
                         CustomerRepository customerRepository,
                         InsurerRepository insurerRepository) {
        this.policyRepository = policyRepository;
        this.customerRepository = customerRepository;
        this.insurerRepository = insurerRepository;
    }

    @Transactional
    public PolicyResponse create(PolicyRequest request) {
        validateDates(request.startDate(), request.endDate());
        validatePolicyNumberAvailable(request.policyNumber(), null);
        Customer customer = getCustomer(request.customerId());
        Insurer insurer = getInsurer(request.insurerId());
        Policy policy = PolicyMapper.toEntity(request, customer, insurer);
        recalculateStatus(policy);
        log.info("Criando apólice: {}", request.policyNumber());
        return PolicyMapper.toResponse(policyRepository.save(policy));
    }

    @Transactional
    public Page<PolicyResponse> list(PolicyStatus status, PolicyType type, UUID insurerId, UUID customerId,
                                    LocalDate startDateFrom, LocalDate startDateTo,
                                    LocalDate endDateFrom, LocalDate endDateTo,
                                    String search, Pageable pageable) {
        validateDateRange(startDateFrom, startDateTo, "Data inicial");
        validateDateRange(endDateFrom, endDateTo, "Data final");
        Specification<Policy> spec = PolicySpecifications.hasStatus(status)
                .and(PolicySpecifications.hasType(type))
                .and(PolicySpecifications.hasInsurer(insurerId))
                .and(PolicySpecifications.hasCustomer(customerId))
                .and(PolicySpecifications.startDateFrom(startDateFrom))
                .and(PolicySpecifications.startDateTo(startDateTo))
                .and(PolicySpecifications.endDateFrom(endDateFrom))
                .and(PolicySpecifications.endDateTo(endDateTo))
                .and(PolicySpecifications.searchContains(search));
        Page<Policy> page = policyRepository.findAll(spec, pageable);
        updateStatusesIfNeeded(page.getContent());
        return page.map(PolicyMapper::toResponse);
    }

    @Transactional
    public PolicyResponse get(UUID id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Apólice não encontrada"));
        boolean updated = recalculateStatus(policy);
        if (updated) {
            policyRepository.save(policy);
        }
        return PolicyMapper.toResponse(policy);
    }

    @Transactional
    public PolicyResponse update(UUID id, PolicyRequest request) {
        validateDates(request.startDate(), request.endDate());
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Apólice não encontrada"));
        validatePolicyNumberAvailable(request.policyNumber(), id);
        Customer customer = getCustomer(request.customerId());
        Insurer insurer = getInsurer(request.insurerId());
        PolicyMapper.updateEntity(policy, request, customer, insurer);
        recalculateStatus(policy);
        return PolicyMapper.toResponse(policyRepository.save(policy));
    }

    @Transactional
    public void delete(UUID id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Apólice não encontrada"));
        policyRepository.delete(policy);
        log.info("Apólice removida: {}", id);
    }

    @Transactional
    public List<PolicyResponse> expiringPolicies(int days) {
        validateExpiringDays(days);
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(days);
        Specification<Policy> spec = PolicySpecifications.endDateFrom(today)
                .and(PolicySpecifications.endDateTo(limit))
                .and(PolicySpecifications.isNotCancelled());
        List<Policy> policies = policyRepository.findAll(spec, org.springframework.data.domain.Sort.by("endDate").ascending());
        updateStatusesIfNeeded(policies);
        return policies.stream().map(PolicyMapper::toResponse).toList();
    }

    @Transactional
    public int recalculateStatuses() {
        int updated = 0;
        int pageNumber = 0;
        Page<Policy> page;
        do {
            PageRequest pageRequest = PageRequest.of(
                    pageNumber,
                    STATUS_RECALCULATION_BATCH_SIZE,
                    Sort.by("endDate").ascending().and(Sort.by("id"))
            );
            page = policyRepository.findAll(pageRequest);
            updated += updateStatusesIfNeeded(page.getContent());
            pageNumber++;
        } while (page.hasNext());
        log.info("Recalculo de status finalizado. Atualizadas: {}", updated);
        return updated;
    }

    private int updateStatusesIfNeeded(List<Policy> policies) {
        List<Policy> updatedPolicies = new ArrayList<>();
        for (Policy policy : policies) {
            if (recalculateStatus(policy)) {
                updatedPolicies.add(policy);
            }
        }
        if (!updatedPolicies.isEmpty()) {
            policyRepository.saveAll(updatedPolicies);
        }
        return updatedPolicies.size();
    }

    private boolean recalculateStatus(Policy policy) {
        if (policy.getStatus() == PolicyStatus.CANCELADA) {
            return false;
        }
        PolicyStatus newStatus = LocalDate.now().isAfter(policy.getEndDate())
                ? PolicyStatus.VENCIDA
                : PolicyStatus.VIGENTE;
        if (policy.getStatus() != newStatus) {
            policy.setStatus(newStatus);
            return true;
        }
        return false;
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new BusinessException("Datas de início e fim são obrigatórias");
        }
        if (end.isBefore(start)) {
            throw new BusinessException("Data fim deve ser maior ou igual à data início");
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to, String fieldName) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new BusinessException(fieldName + " final deve ser maior ou igual à " + fieldName.toLowerCase() + " inicial");
        }
    }

    private void validateExpiringDays(int days) {
        if (days < 0) {
            throw new BusinessException("Quantidade de dias deve ser maior ou igual a zero");
        }
        if (days > MAX_EXPIRING_DAYS) {
            throw new BusinessException("Quantidade de dias deve ser menor ou igual a " + MAX_EXPIRING_DAYS);
        }
    }

    private void validatePolicyNumberAvailable(String policyNumber, UUID currentPolicyId) {
        String normalizedPolicyNumber = policyNumber == null ? null : policyNumber.trim();
        boolean exists = currentPolicyId == null
                ? policyRepository.existsByPolicyNumber(normalizedPolicyNumber)
                : policyRepository.existsByPolicyNumberAndIdNot(normalizedPolicyNumber, currentPolicyId);
        if (exists) {
            throw new ConflictException("Já existe uma apólice cadastrada com este número");
        }
    }

    private Customer getCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
    }

    private Insurer getInsurer(UUID id) {
        Insurer insurer = insurerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Seguradora não encontrada"));
        if (!insurer.isActive()) {
            throw new BusinessException("Seguradora inativa não pode ser vinculada a apólices");
        }
        return insurer;
    }
}
