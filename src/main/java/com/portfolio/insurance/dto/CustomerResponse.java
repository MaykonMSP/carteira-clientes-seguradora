package com.portfolio.insurance.dto;

import com.portfolio.insurance.domain.CustomerType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de cliente")
public record CustomerResponse(
        UUID id,
        String fullName,
        CustomerType customerType,
        String cpf,
        String cnpj,
        String email,
        String phone,
        LocalDate birthDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
