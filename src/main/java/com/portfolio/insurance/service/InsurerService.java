package com.portfolio.insurance.service;

import com.portfolio.insurance.domain.Insurer;
import com.portfolio.insurance.dto.InsurerRequest;
import com.portfolio.insurance.dto.InsurerResponse;
import com.portfolio.insurance.exception.ConflictException;
import com.portfolio.insurance.exception.NotFoundException;
import com.portfolio.insurance.mapper.InsurerMapper;
import com.portfolio.insurance.repository.InsurerRepository;
import com.portfolio.insurance.repository.spec.InsurerSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InsurerService {

    private static final Logger log = LoggerFactory.getLogger(InsurerService.class);

    private final InsurerRepository insurerRepository;

    public InsurerService(InsurerRepository insurerRepository) {
        this.insurerRepository = insurerRepository;
    }

    @Transactional
    public InsurerResponse create(InsurerRequest request) {
        log.info("Criando seguradora: {}", request.name());
        validateUniqueData(request, null);
        Insurer insurer = InsurerMapper.toEntity(request);
        return InsurerMapper.toResponse(insurerRepository.save(insurer));
    }

    @Transactional(readOnly = true)
    public Page<InsurerResponse> list(String name, Pageable pageable) {
        Specification<Insurer> spec = InsurerSpecifications.nameContains(name);
        return insurerRepository.findAll(spec, pageable).map(InsurerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InsurerResponse get(UUID id) {
        Insurer insurer = insurerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Seguradora não encontrada"));
        return InsurerMapper.toResponse(insurer);
    }

    @Transactional
    public InsurerResponse update(UUID id, InsurerRequest request) {
        Insurer insurer = insurerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Seguradora não encontrada"));
        validateUniqueData(request, id);
        InsurerMapper.updateEntity(insurer, request);
        return InsurerMapper.toResponse(insurerRepository.save(insurer));
    }

    @Transactional
    public void deactivate(UUID id) {
        Insurer insurer = insurerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Seguradora não encontrada"));
        insurer.setActive(false);
        insurerRepository.save(insurer);
        log.info("Seguradora desativada: {}", id);
    }

    private void validateUniqueData(InsurerRequest request, UUID currentInsurerId) {
        String name = request.name() == null ? null : request.name().trim();
        String cnpj = normalizeOptional(request.cnpj());

        boolean nameExists = currentInsurerId == null
                ? insurerRepository.existsByNameIgnoreCase(name)
                : insurerRepository.existsByNameIgnoreCaseAndIdNot(name, currentInsurerId);
        if (nameExists) {
            throw new ConflictException("Já existe uma seguradora cadastrada com este nome");
        }

        if (cnpj != null) {
            boolean cnpjExists = currentInsurerId == null
                    ? insurerRepository.existsByCnpj(cnpj)
                    : insurerRepository.existsByCnpjAndIdNot(cnpj, currentInsurerId);
            if (cnpjExists) {
                throw new ConflictException("Já existe uma seguradora cadastrada com este CNPJ");
            }
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
