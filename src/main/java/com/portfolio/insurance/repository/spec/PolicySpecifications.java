package com.portfolio.insurance.repository.spec;

import com.portfolio.insurance.domain.Policy;
import com.portfolio.insurance.domain.PolicyStatus;
import com.portfolio.insurance.domain.PolicyType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class PolicySpecifications {

    public static Specification<Policy> hasStatus(PolicyStatus status) {
        return (root, query, builder) -> status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
    }

    public static Specification<Policy> isNotCancelled() {
        return (root, query, builder) -> builder.notEqual(root.get("status"), PolicyStatus.CANCELADA);
    }

    public static Specification<Policy> hasType(PolicyType type) {
        return (root, query, builder) -> type == null ? builder.conjunction() : builder.equal(root.get("type"), type);
    }

    public static Specification<Policy> hasInsurer(UUID insurerId) {
        return (root, query, builder) -> insurerId == null ? builder.conjunction() : builder.equal(root.get("insurer").get("id"), insurerId);
    }

    public static Specification<Policy> hasCustomer(UUID customerId) {
        return (root, query, builder) -> customerId == null ? builder.conjunction() : builder.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Policy> startDateFrom(LocalDate from) {
        return (root, query, builder) -> from == null ? builder.conjunction() : builder.greaterThanOrEqualTo(root.get("startDate"), from);
    }

    public static Specification<Policy> startDateTo(LocalDate to) {
        return (root, query, builder) -> to == null ? builder.conjunction() : builder.lessThanOrEqualTo(root.get("startDate"), to);
    }

    public static Specification<Policy> endDateFrom(LocalDate from) {
        return (root, query, builder) -> from == null ? builder.conjunction() : builder.greaterThanOrEqualTo(root.get("endDate"), from);
    }

    public static Specification<Policy> endDateTo(LocalDate to) {
        return (root, query, builder) -> to == null ? builder.conjunction() : builder.lessThanOrEqualTo(root.get("endDate"), to);
    }

    public static Specification<Policy> searchContains(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("policyNumber")), pattern),
                    builder.like(builder.lower(root.get("notes")), pattern),
                    builder.like(builder.lower(root.join("customer", JoinType.LEFT).get("fullName")), pattern),
                    builder.like(builder.lower(root.join("insurer", JoinType.LEFT).get("name")), pattern)
            );
        };
    }
}
