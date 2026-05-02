package com.portfolio.insurance.mapper;

import com.portfolio.insurance.domain.Customer;
import com.portfolio.insurance.dto.CustomerRequest;
import com.portfolio.insurance.dto.CustomerResponse;

public class CustomerMapper {

    public static Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFullName(trim(request.fullName()));
        customer.setCpf(trim(request.cpf()));
        customer.setEmail(trimToNull(request.email()));
        customer.setPhone(trimToNull(request.phone()));
        customer.setBirthDate(request.birthDate());
        return customer;
    }

    public static void updateEntity(Customer customer, CustomerRequest request) {
        customer.setFullName(trim(request.fullName()));
        customer.setCpf(trim(request.cpf()));
        customer.setEmail(trimToNull(request.email()));
        customer.setPhone(trimToNull(request.phone()));
        customer.setBirthDate(request.birthDate());
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getCpf(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getBirthDate(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }
}
