package com.portfolio.insurance.controller;

import com.portfolio.insurance.dto.InsurerRequest;
import com.portfolio.insurance.dto.InsurerResponse;
import com.portfolio.insurance.service.InsurerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

import java.util.UUID;

@Tag(name = "Insurers", description = "Operações de seguradoras")
@RestController
@RequestMapping("/insurers")
public class InsurerController {

    private final InsurerService insurerService;

    public InsurerController(InsurerService insurerService) {
        this.insurerService = insurerService;
    }

    @Operation(summary = "Criar seguradora")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InsurerResponse create(@Valid @RequestBody InsurerRequest request) {
        return insurerService.create(request);
    }

    @Operation(summary = "Listar seguradoras")
    @GetMapping
    public Page<InsurerResponse> list(@RequestParam(required = false) String name,
                                      @ParameterObject Pageable pageable) {
        return insurerService.list(name, pageable);
    }

    @Operation(summary = "Buscar seguradora por ID")
    @GetMapping("/{id}")
    public InsurerResponse get(@PathVariable UUID id) {
        return insurerService.get(id);
    }

    @Operation(summary = "Atualizar seguradora")
    @PutMapping("/{id}")
    public InsurerResponse update(@PathVariable UUID id, @Valid @RequestBody InsurerRequest request) {
        return insurerService.update(id, request);
    }

    @Operation(summary = "Remover seguradora (soft delete)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        insurerService.deactivate(id);
    }
}
