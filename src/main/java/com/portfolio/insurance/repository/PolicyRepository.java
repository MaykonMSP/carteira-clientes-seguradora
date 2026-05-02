package com.portfolio.insurance.repository;

import com.portfolio.insurance.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID>, JpaSpecificationExecutor<Policy> {
    boolean existsByPolicyNumber(String policyNumber);

    boolean existsByPolicyNumberAndIdNot(String policyNumber, UUID id);

    boolean existsByCustomerId(UUID customerId);

    boolean existsByInsurerId(UUID insurerId);
}
