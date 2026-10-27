package com.mercatus.customer.domain.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mercatus.customer.domain.exception.DomainRuleException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AddressTest {

    private static final Instant NOW = Instant.parse("2026-10-27T12:00:00Z");

    private Customer customerWithAddress(String state) {
        Customer customer = Customer.create(UUID.randomUUID(), "Joao", "joao@x.com", null, NOW);
        customer.addAddress(UUID.randomUUID(), "01001-000", "Praca da Se", "100", null, "Se", "Sao Paulo", state, true, NOW);
        return customer;
    }

    @Test
    void countryIsFixedToBr() {
        Customer customer = customerWithAddress("SP");
        assertThat(customer.addresses().get(0).country()).isEqualTo("BR");
    }

    @Test
    void stateIsUpperCased() {
        Customer customer = customerWithAddress("sp");
        assertThat(customer.addresses().get(0).state()).isEqualTo("SP");
    }

    @Test
    void blankRequiredFieldIsRejected() {
        Customer customer = Customer.create(UUID.randomUUID(), "Joao", "joao@x.com", null, NOW);
        assertThatThrownBy(() ->
                customer.addAddress(UUID.randomUUID(), "01001-000", "  ", "100", null, "Se", "Sao Paulo", "SP", true, NOW))
                .isInstanceOf(DomainRuleException.class);
    }

    @Test
    void invalidStateLengthIsRejected() {
        Customer customer = Customer.create(UUID.randomUUID(), "Joao", "joao@x.com", null, NOW);
        assertThatThrownBy(() ->
                customer.addAddress(UUID.randomUUID(), "01001-000", "Praca da Se", "100", null, "Se", "Sao Paulo", "SPX", true, NOW))
                .isInstanceOf(DomainRuleException.class);
    }
}
