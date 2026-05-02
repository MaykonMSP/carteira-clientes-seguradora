package com.portfolio.insurance.service;

import com.portfolio.insurance.domain.Customer;
import com.portfolio.insurance.domain.Insurer;
import com.portfolio.insurance.domain.Policy;
import com.portfolio.insurance.domain.PolicyStatus;
import com.portfolio.insurance.domain.PolicyType;
import com.portfolio.insurance.dto.PolicyRequest;
import com.portfolio.insurance.dto.PolicyResponse;
import com.portfolio.insurance.exception.BusinessException;
import com.portfolio.insurance.exception.ConflictException;
import com.portfolio.insurance.repository.CustomerRepository;
import com.portfolio.insurance.repository.InsurerRepository;
import com.portfolio.insurance.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PolicyServiceTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InsurerRepository insurerRepository;

    @Test
    void shouldRecalculateStatusRespectingCancelled() {
        Customer customer = new Customer();
        customer.setFullName("Maria Souza");
        customer.setCpf("52998224725");
        customerRepository.save(customer);

        Insurer insurer = new Insurer();
        insurer.setName("Seguradora Teste");
        insurerRepository.save(insurer);

        Policy expiredPolicy = new Policy();
        expiredPolicy.setPolicyNumber("POL-EXPIRED");
        expiredPolicy.setType(PolicyType.VIDA);
        expiredPolicy.setStatus(PolicyStatus.VIGENTE);
        expiredPolicy.setStartDate(LocalDate.now().minusDays(10));
        expiredPolicy.setEndDate(LocalDate.now().minusDays(1));
        expiredPolicy.setCustomer(customer);
        expiredPolicy.setInsurer(insurer);
        policyRepository.save(expiredPolicy);

        Policy cancelledPolicy = new Policy();
        cancelledPolicy.setPolicyNumber("POL-CANCELLED");
        cancelledPolicy.setType(PolicyType.AUTO);
        cancelledPolicy.setStatus(PolicyStatus.CANCELADA);
        cancelledPolicy.setStartDate(LocalDate.now().minusDays(5));
        cancelledPolicy.setEndDate(LocalDate.now().minusDays(1));
        cancelledPolicy.setCustomer(customer);
        cancelledPolicy.setInsurer(insurer);
        policyRepository.save(cancelledPolicy);

        int updated = policyService.recalculateStatuses();

        Policy updatedExpired = policyRepository.findById(expiredPolicy.getId()).orElseThrow();
        Policy updatedCancelled = policyRepository.findById(cancelledPolicy.getId()).orElseThrow();

        assertThat(updated).isEqualTo(1);
        assertThat(updatedExpired.getStatus()).isEqualTo(PolicyStatus.VENCIDA);
        assertThat(updatedCancelled.getStatus()).isEqualTo(PolicyStatus.CANCELADA);
    }

    @Test
    void shouldCreatePolicyWithCalculatedStatusWhenStatusIsOmitted() {
        Customer customer = saveCustomer("João Validado", "39053344705");
        Insurer insurer = saveInsurer("Seguradora Criacao", "11222333000181", true);

        PolicyRequest request = new PolicyRequest(
                "POL-CALCULATED",
                PolicyType.AUTO,
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(30),
                null,
                null,
                customer.getId(),
                insurer.getId()
        );

        PolicyResponse response = policyService.create(request);

        assertThat(response.status()).isEqualTo(PolicyStatus.VIGENTE);
    }

    @Test
    void shouldCreateExpiredPolicyWithCalculatedStatusWhenEndDateIsPast() {
        Customer customer = saveCustomer("Cliente Vencido", "32696423857");
        Insurer insurer = saveInsurer("Seguradora Vencimento", null, true);

        PolicyRequest request = new PolicyRequest(
                "POL-CALCULATED-EXPIRED",
                PolicyType.AUTO,
                null,
                LocalDate.now().minusDays(40),
                LocalDate.now().minusDays(1),
                null,
                null,
                customer.getId(),
                insurer.getId()
        );

        PolicyResponse response = policyService.create(request);

        assertThat(response.status()).isEqualTo(PolicyStatus.VENCIDA);
    }

    @Test
    void shouldSetPolicyAsActiveWhenExpiredPolicyIsRenewedWithFutureEndDate() {
        Customer customer = saveCustomer("Cliente Renovado", "40140404840");
        Insurer insurer = saveInsurer("Seguradora Renovada", null, true);
        Policy policy = savePolicy("POL-RENEWED", customer, insurer, PolicyStatus.VENCIDA, LocalDate.now().minusDays(1));

        PolicyRequest request = new PolicyRequest(
                policy.getPolicyNumber(),
                PolicyType.AUTO,
                PolicyStatus.VENCIDA,
                LocalDate.now(),
                LocalDate.now().plusDays(365),
                null,
                "Renovacao anual",
                customer.getId(),
                insurer.getId()
        );

        PolicyResponse response = policyService.update(policy.getId(), request);

        assertThat(response.status()).isEqualTo(PolicyStatus.VIGENTE);
        assertThat(response.endDate()).isEqualTo(LocalDate.now().plusDays(365));
    }

    @Test
    void shouldRejectPolicyWhenEndDateIsBeforeStartDate() {
        Customer customer = saveCustomer("Cliente Data Invalida", "57116847477");
        Insurer insurer = saveInsurer("Seguradora Data Invalida", null, true);

        PolicyRequest request = new PolicyRequest(
                "POL-INVALID-DATES",
                PolicyType.AUTO,
                null,
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                null,
                null,
                customer.getId(),
                insurer.getId()
        );

        assertThatThrownBy(() -> policyService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Data fim");
    }

    @Test
    void shouldRejectInactiveInsurerForPolicy() {
        Customer customer = saveCustomer("Cliente Inativo", "11144477735");
        Insurer insurer = saveInsurer("Seguradora Inativa", "04252011000110", false);

        PolicyRequest request = new PolicyRequest(
                "POL-INACTIVE",
                PolicyType.VIDA,
                PolicyStatus.VIGENTE,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                null,
                null,
                customer.getId(),
                insurer.getId()
        );

        assertThatThrownBy(() -> policyService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Seguradora inativa");
    }

    @Test
    void shouldRejectDuplicatedPolicyNumber() {
        Customer customer = saveCustomer("Cliente Duplicado", "29537958806");
        Insurer insurer = saveInsurer("Seguradora Duplicada", null, true);
        savePolicy("POL-DUPLICATED", customer, insurer, PolicyStatus.VIGENTE, LocalDate.now().plusDays(10));

        PolicyRequest request = new PolicyRequest(
                "POL-DUPLICATED",
                PolicyType.AUTO,
                PolicyStatus.VIGENTE,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                null,
                null,
                customer.getId(),
                insurer.getId()
        );

        assertThatThrownBy(() -> policyService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("número");
    }

    @Test
    void shouldRejectInvalidExpiringDays() {
        assertThatThrownBy(() -> policyService.expiringPolicies(-1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dias");
    }

    @Test
    void shouldNotListCancelledPoliciesAsExpiring() {
        Customer customer = saveCustomer("Cliente Renovacao", "15481420701");
        Insurer insurer = saveInsurer("Seguradora Renovacao", null, true);
        savePolicy("POL-EXPIRING-ACTIVE", customer, insurer, PolicyStatus.VIGENTE, LocalDate.now().plusDays(5));
        savePolicy("POL-EXPIRING-CANCELLED", customer, insurer, PolicyStatus.CANCELADA, LocalDate.now().plusDays(5));

        List<PolicyResponse> policies = policyService.expiringPolicies(10);

        assertThat(policies)
                .extracting(PolicyResponse::policyNumber)
                .contains("POL-EXPIRING-ACTIVE")
                .doesNotContain("POL-EXPIRING-CANCELLED");
    }

    @Test
    void shouldListOnlyPoliciesInsideExpiringWindowIncludingToday() {
        Customer customer = saveCustomer("Cliente Janela", "98700539008");
        Insurer insurer = saveInsurer("Seguradora Janela", null, true);
        savePolicy("POL-EXPIRING-TODAY", customer, insurer, PolicyStatus.VIGENTE, LocalDate.now());
        savePolicy("POL-EXPIRING-OUTSIDE", customer, insurer, PolicyStatus.VIGENTE, LocalDate.now().plusDays(31));

        List<PolicyResponse> policies = policyService.expiringPolicies(30);

        assertThat(policies)
                .extracting(PolicyResponse::policyNumber)
                .contains("POL-EXPIRING-TODAY")
                .doesNotContain("POL-EXPIRING-OUTSIDE");
    }

    @Test
    void shouldSearchPoliciesByCustomerNameAndNotes() {
        Customer customer = saveCustomer("Cliente Horizonte", "06230790402");
        Customer otherCustomer = saveCustomer("Cliente Avulso", "85387712033");
        Insurer insurer = saveInsurer("Seguradora Busca", null, true);
        Policy matchedByCustomer = savePolicy("POL-CUSTOMER-SEARCH", customer, insurer, PolicyStatus.VIGENTE, LocalDate.now().plusDays(20));
        Policy matchedByNotes = savePolicy("POL-NOTES-SEARCH", otherCustomer, insurer, PolicyStatus.VIGENTE, LocalDate.now().plusDays(25));
        matchedByNotes.setNotes("Renovacao prioritaria");
        policyRepository.save(matchedByNotes);

        Page<PolicyResponse> byCustomer = policyService.list(null, null, null, null, null, null, null, null, "Horizonte", PageRequest.of(0, 10));
        Page<PolicyResponse> byNotes = policyService.list(null, null, null, null, null, null, null, null, "prioritaria", PageRequest.of(0, 10));

        assertThat(byCustomer.getContent())
                .extracting(PolicyResponse::policyNumber)
                .containsExactly(matchedByCustomer.getPolicyNumber());
        assertThat(byNotes.getContent())
                .extracting(PolicyResponse::policyNumber)
                .containsExactly(matchedByNotes.getPolicyNumber());
    }

    private Customer saveCustomer(String name, String cpf) {
        Customer customer = new Customer();
        customer.setFullName(name);
        customer.setCpf(cpf);
        return customerRepository.save(customer);
    }

    private Insurer saveInsurer(String name, String cnpj, boolean active) {
        Insurer insurer = new Insurer();
        insurer.setName(name);
        insurer.setCnpj(cnpj);
        insurer.setActive(active);
        return insurerRepository.save(insurer);
    }

    private Policy savePolicy(String policyNumber, Customer customer, Insurer insurer, PolicyStatus status, LocalDate endDate) {
        Policy policy = new Policy();
        policy.setPolicyNumber(policyNumber);
        policy.setType(PolicyType.AUTO);
        policy.setStatus(status);
        policy.setStartDate(endDate.minusDays(30));
        policy.setEndDate(endDate);
        policy.setCustomer(customer);
        policy.setInsurer(insurer);
        return policyRepository.save(policy);
    }
}
