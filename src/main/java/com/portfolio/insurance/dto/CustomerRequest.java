package com.portfolio.insurance.dto;

import com.portfolio.insurance.validation.ValidCpf;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criação/atualização de cliente")
public record CustomerRequest(
        @Schema(example = "João da Silva")
        @NotBlank(message = "Nome completo é obrigatório")
        @Size(max = 255, message = "Nome completo deve ter no máximo 255 caracteres")
        String fullName,
        @Schema(example = "52998224725", description = "CPF com 11 dígitos, sem pontuação")
        @NotBlank(message = "CPF é obrigatório")
        @ValidCpf
        String cpf,
        @Schema(example = "joao@email.com")
        @Email(message = "Email inválido")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,
        @Schema(example = "+55 11 99999-0000")
        @Size(max = 50, message = "Telefone deve ter no máximo 50 caracteres")
        String phone,
        @Schema(example = "1990-05-20")
        @Past(message = "Data de nascimento deve estar no passado")
        LocalDate birthDate
) {
}
