package com.portfolio.insurance.mapper;

import com.portfolio.insurance.domain.Customer;
import com.portfolio.insurance.domain.CustomerType;
import com.portfolio.insurance.dto.CustomerRequest;
import com.portfolio.insurance.dto.CustomerResponse;

public class CustomerMapper {

    public static Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        CustomerType customerType = resolveCustomerType(request.customerType());
        customer.setFullName(trim(request.fullName()));
        customer.setCustomerType(customerType);
        customer.setCpf(customerType == CustomerType.PESSOA_FISICA ? trim(request.cpf()) : null);
        customer.setCnpj(customerType == CustomerType.PESSOA_JURIDICA ? trim(request.cnpj()) : null);
        customer.setEmail(trimToNull(request.email()));
        customer.setPhone(trimToNull(request.phone()));
        customer.setBirthDate(request.birthDate());
        return customer;
    }

    public static void updateEntity(Customer customer, CustomerRequest request) {
        CustomerType customerType = resolveCustomerType(request.customerType());
        customer.setFullName(trim(request.fullName()));
        customer.setCustomerType(customerType);
        customer.setCpf(customerType == CustomerType.PESSOA_FISICA ? trim(request.cpf()) : null);
        customer.setCnpj(customerType == CustomerType.PESSOA_JURIDICA ? trim(request.cnpj()) : null);
        customer.setEmail(trimToNull(request.email()));
        customer.setPhone(trimToNull(request.phone()));
        customer.setBirthDate(request.birthDate());
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getCustomerType(),
                customer.getCpf(),
                customer.getCnpj(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getBirthDate(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static CustomerType resolveCustomerType(CustomerType customerType) {
        return customerType == null ? CustomerType.PESSOA_FISICA : customerType;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }
}
