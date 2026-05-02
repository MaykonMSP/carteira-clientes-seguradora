package com.portfolio.insurance.repository;

import com.portfolio.insurance.domain.Insurer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InsurerRepository extends JpaRepository<Insurer, UUID>, JpaSpecificationExecutor<Insurer> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, UUID id);
}
