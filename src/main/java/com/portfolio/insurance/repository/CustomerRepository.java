package com.portfolio.insurance.repository;

import com.portfolio.insurance.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {
    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, UUID id);
}
