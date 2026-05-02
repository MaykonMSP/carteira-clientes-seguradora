package com.portfolio.insurance.dto;

import com.portfolio.insurance.validation.ValidCnpj;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação/atualização de seguradora")
public record InsurerRequest(
        @Schema(example = "Seguradora Atlas")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String name,
        @Schema(example = "11222333000181", description = "CNPJ com 14 dígitos, sem pontuação")
        @ValidCnpj
        String cnpj
) {
}
