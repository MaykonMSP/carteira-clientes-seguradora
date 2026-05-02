package com.portfolio.insurance.service;

import com.portfolio.insurance.domain.CustomerType;
import com.portfolio.insurance.dto.CustomerRequest;
import com.portfolio.insurance.dto.CustomerResponse;
import com.portfolio.insurance.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Test
    void shouldCreateLegalEntityCustomerWithCnpj() {
        CustomerRequest request = new CustomerRequest(
                "Empresa Ficticia Ltda",
                CustomerType.PESSOA_JURIDICA,
                null,
                "12345678000195",
                "contato@empresa.example.test",
                "+55 11 91000-0000",
                null
        );

        CustomerResponse response = customerService.create(request);

        assertThat(response.customerType()).isEqualTo(CustomerType.PESSOA_JURIDICA);
        assertThat(response.cnpj()).isEqualTo("12345678000195");
        assertThat(response.cpf()).isNull();
    }

    @Test
    void shouldRejectLegalEntityWithoutCnpj() {
        CustomerRequest request = new CustomerRequest(
                "Empresa Sem Documento Ltda",
                CustomerType.PESSOA_JURIDICA,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNPJ");
    }
}
