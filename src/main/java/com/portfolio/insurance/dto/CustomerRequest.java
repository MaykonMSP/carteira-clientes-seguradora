package com.portfolio.insurance.dto;

import com.portfolio.insurance.domain.CustomerType;
import com.portfolio.insurance.validation.ValidCnpj;
import com.portfolio.insurance.validation.ValidCpf;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criacao/atualizacao de cliente")
public record CustomerRequest(
        @Schema(example = "Joao da Silva")
        @NotBlank(message = "Nome completo e obrigatorio")
        @Size(max = 255, message = "Nome completo deve ter no maximo 255 caracteres")
        String fullName,
        @Schema(example = "PESSOA_FISICA", description = "Tipo de cliente. Se omitido, assume PESSOA_FISICA.")
        CustomerType customerType,
        @Schema(example = "52998224725", description = "CPF com 11 digitos, sem pontuacao. Obrigatorio para pessoa fisica.")
        @ValidCpf
        String cpf,
        @Schema(example = "11222333000181", description = "CNPJ com 14 digitos, sem pontuacao. Obrigatorio para pessoa juridica.")
        @ValidCnpj
        String cnpj,
        @Schema(example = "joao@email.com")
        @Email(message = "Email invalido")
        @Size(max = 255, message = "Email deve ter no maximo 255 caracteres")
        String email,
        @Schema(example = "+55 11 99999-0000")
        @Size(max = 50, message = "Telefone deve ter no maximo 50 caracteres")
        String phone,
        @Schema(example = "1990-05-20")
        @Past(message = "Data de nascimento deve estar no passado")
        LocalDate birthDate
) {
}
