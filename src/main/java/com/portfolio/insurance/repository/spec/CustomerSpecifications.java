package com.portfolio.insurance.repository.spec;

import com.portfolio.insurance.domain.Customer;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecifications {

    public static Specification<Customer> nameOrCpfLike(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("fullName")), pattern),
                    builder.like(builder.lower(root.get("cpf")), pattern),
                    builder.like(builder.lower(root.get("cnpj")), pattern)
            );
        };
    }
}
