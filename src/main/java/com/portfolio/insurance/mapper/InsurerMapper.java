package com.portfolio.insurance.mapper;

import com.portfolio.insurance.domain.Insurer;
import com.portfolio.insurance.dto.InsurerRequest;
import com.portfolio.insurance.dto.InsurerResponse;

public class InsurerMapper {

    public static Insurer toEntity(InsurerRequest request) {
        Insurer insurer = new Insurer();
        insurer.setName(trim(request.name()));
        insurer.setCnpj(trimToNull(request.cnpj()));
        return insurer;
    }

    public static void updateEntity(Insurer insurer, InsurerRequest request) {
        insurer.setName(trim(request.name()));
        insurer.setCnpj(trimToNull(request.cnpj()));
    }

    public static InsurerResponse toResponse(Insurer insurer) {
        return new InsurerResponse(
                insurer.getId(),
                insurer.getName(),
                insurer.getCnpj(),
                insurer.isActive()
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
