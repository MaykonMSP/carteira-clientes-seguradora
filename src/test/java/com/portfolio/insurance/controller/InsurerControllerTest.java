package com.portfolio.insurance.controller;

import com.portfolio.insurance.domain.Insurer;
import com.portfolio.insurance.repository.InsurerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InsurerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InsurerRepository insurerRepository;

    @Test
    @WithMockUser(roles = "USER")
    void shouldListInsurers() throws Exception {
        Insurer insurer = new Insurer();
        insurer.setName("Seguradora XP");
        insurer.setCnpj("11222333000181");
        insurerRepository.save(insurer);

        mockMvc.perform(get("/insurers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Seguradora XP"));
    }
}
