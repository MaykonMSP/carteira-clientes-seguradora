package com.portfolio.insurance.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentValidatorTest {

    @Test
    void shouldValidateCpfDigits() {
        assertThat(DocumentValidator.isCpf("52998224725")).isTrue();
        assertThat(DocumentValidator.isCpf("11111111111")).isFalse();
        assertThat(DocumentValidator.isCpf("12345678901")).isFalse();
    }

    @Test
    void shouldValidateCnpjDigits() {
        assertThat(DocumentValidator.isCnpj("11222333000181")).isTrue();
        assertThat(DocumentValidator.isCnpj("11111111111111")).isFalse();
        assertThat(DocumentValidator.isCnpj("12345678000190")).isFalse();
    }
}
