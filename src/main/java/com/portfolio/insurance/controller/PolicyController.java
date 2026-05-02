package com.portfolio.insurance.controller;

import com.portfolio.insurance.domain.PolicyStatus;
import com.portfolio.insurance.domain.PolicyType;
import com.portfolio.insurance.dto.PolicyRequest;
import com.portfolio.insurance.dto.PolicyResponse;
import com.portfolio.insurance.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Policies", description = "Operações de apólices")
@RestController
@RequestMapping("/policies")
@Validated
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @Operation(summary = "Criar apólice")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse create(@Valid @RequestBody PolicyRequest request) {
        return policyService.create(request);
    }

    @Operation(summary = "Listar apólices")
    @GetMapping
    public Page<PolicyResponse> list(
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) PolicyType type,
            @RequestParam(required = false) UUID insurerId,
            @RequestParam(required = false) UUID customerId,
            @Parameter(description = "Filtra apólices com início a partir desta data")
            @RequestParam(required = false) LocalDate startDateFrom,
            @Parameter(description = "Filtra apólices com início até esta data")
            @RequestParam(required = false) LocalDate startDateTo,
            @Parameter(description = "Filtra apólices com vencimento a partir desta data")
            @RequestParam(required = false) LocalDate endDateFrom,
            @Parameter(description = "Filtra apólices com vencimento até esta data")
            @RequestParam(required = false) LocalDate endDateTo,
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        return policyService.list(status, type, insurerId, customerId, startDateFrom, startDateTo, endDateFrom, endDateTo, search, pageable);
    }

    @Operation(summary = "Buscar apólice por ID")
    @GetMapping("/{id}")
    public PolicyResponse get(@PathVariable UUID id) {
        return policyService.get(id);
    }

    @Operation(summary = "Atualizar apólice")
    @PutMapping("/{id}")
    public PolicyResponse update(@PathVariable UUID id, @Valid @RequestBody PolicyRequest request) {
        return policyService.update(id, request);
    }

    @Operation(summary = "Remover apólice")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        policyService.delete(id);
    }

    @Operation(summary = "Listar apólices a vencer")
    @GetMapping("/expiring")
    public List<PolicyResponse> expiring(@RequestParam(defaultValue = "30") @Min(0) @Max(366) int days) {
        return policyService.expiringPolicies(days);
    }

    @Operation(summary = "Recalcular status das apólices")
    @PostMapping("/recalculate-status")
    public String recalculateStatus() {
        int updated = policyService.recalculateStatuses();
        return "Apólices atualizadas: " + updated;
    }
}
