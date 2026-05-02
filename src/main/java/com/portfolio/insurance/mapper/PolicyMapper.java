package com.portfolio.insurance.mapper;

import com.portfolio.insurance.domain.Customer;
import com.portfolio.insurance.domain.Insurer;
import com.portfolio.insurance.domain.Policy;
import com.portfolio.insurance.dto.PolicyRequest;
import com.portfolio.insurance.dto.PolicyResponse;

public class PolicyMapper {

    public static Policy toEntity(PolicyRequest request, Customer customer, Insurer insurer) {
        Policy policy = new Policy();
        policy.setPolicyNumber(trim(request.policyNumber()));
        policy.setType(request.type());
        policy.setStatus(request.status());
        policy.setStartDate(request.startDate());
        policy.setEndDate(request.endDate());
        policy.setMonthlyPremium(request.monthlyPremium());
        policy.setNotes(trimToNull(request.notes()));
        policy.setCustomer(customer);
        policy.setInsurer(insurer);
        return policy;
    }

    public static void updateEntity(Policy policy, PolicyRequest request, Customer customer, Insurer insurer) {
        policy.setPolicyNumber(trim(request.policyNumber()));
        policy.setType(request.type());
        policy.setStatus(request.status());
        policy.setStartDate(request.startDate());
        policy.setEndDate(request.endDate());
        policy.setMonthlyPremium(request.monthlyPremium());
        policy.setNotes(trimToNull(request.notes()));
        policy.setCustomer(customer);
        policy.setInsurer(insurer);
    }

    public static PolicyResponse toResponse(Policy policy) {
        return new PolicyResponse(
                policy.getId(),
                policy.getPolicyNumber(),
                policy.getType(),
                policy.getStatus(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getMonthlyPremium(),
                policy.getNotes(),
                policy.getCustomer().getId(),
                policy.getCustomer().getFullName(),
                policy.getInsurer().getId(),
                policy.getInsurer().getName(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
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
