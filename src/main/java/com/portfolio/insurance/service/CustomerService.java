package com.portfolio.insurance.service;

import com.portfolio.insurance.domain.Customer;
import com.portfolio.insurance.domain.CustomerType;
import com.portfolio.insurance.dto.CustomerRequest;
import com.portfolio.insurance.dto.CustomerResponse;
import com.portfolio.insurance.exception.BusinessException;
import com.portfolio.insurance.exception.ConflictException;
import com.portfolio.insurance.exception.NotFoundException;
import com.portfolio.insurance.mapper.CustomerMapper;
import com.portfolio.insurance.repository.CustomerRepository;
import com.portfolio.insurance.repository.PolicyRepository;
import com.portfolio.insurance.repository.spec.CustomerSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final PolicyRepository policyRepository;

    public CustomerService(CustomerRepository customerRepository, PolicyRepository policyRepository) {
        this.customerRepository = customerRepository;
        this.policyRepository = policyRepository;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        log.info("Criando cliente: {}", request.fullName());
        validateCustomerDocuments(request, null);
        Customer customer = CustomerMapper.toEntity(request);
        return CustomerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(String search, Pageable pageable) {
        Specification<Customer> spec = CustomerSpecifications.nameOrCpfLike(search);
        return customerRepository.findAll(spec, pageable).map(CustomerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado"));
        return CustomerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado"));
        validateCustomerDocuments(request, id);
        CustomerMapper.updateEntity(customer, request);
        return CustomerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado"));
        if (policyRepository.existsByCustomerId(id)) {
            throw new BusinessException("Cliente possui apolices vinculadas e nao pode ser removido");
        }
        customerRepository.delete(customer);
        log.info("Cliente removido: {}", id);
    }

    private void validateCustomerDocuments(CustomerRequest request, UUID currentCustomerId) {
        CustomerType customerType = CustomerMapper.resolveCustomerType(request.customerType());
        String cpf = normalize(request.cpf());
        String cnpj = normalize(request.cnpj());

        if (customerType == CustomerType.PESSOA_FISICA && cpf == null) {
            throw new BusinessException("CPF e obrigatorio para pessoa fisica");
        }
        if (customerType == CustomerType.PESSOA_JURIDICA && cnpj == null) {
            throw new BusinessException("CNPJ e obrigatorio para pessoa juridica");
        }

        if (cpf != null) {
            validateCpfAvailable(cpf, currentCustomerId);
        }
        if (cnpj != null) {
            validateCnpjAvailable(cnpj, currentCustomerId);
        }
    }

    private void validateCpfAvailable(String cpf, UUID currentCustomerId) {
        String normalizedCpf = cpf == null ? null : cpf.trim();
        boolean exists = currentCustomerId == null
                ? customerRepository.existsByCpf(normalizedCpf)
                : customerRepository.existsByCpfAndIdNot(normalizedCpf, currentCustomerId);
        if (exists) {
            throw new ConflictException("Ja existe um cliente cadastrado com este CPF");
        }
    }

    private void validateCnpjAvailable(String cnpj, UUID currentCustomerId) {
        String normalizedCnpj = cnpj == null ? null : cnpj.trim();
        boolean exists = currentCustomerId == null
                ? customerRepository.existsByCnpj(normalizedCnpj)
                : customerRepository.existsByCnpjAndIdNot(normalizedCnpj, currentCustomerId);
        if (exists) {
            throw new ConflictException("Ja existe um cliente cadastrado com este CNPJ");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
