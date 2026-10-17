package com.mercatus.customer.domain.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mercatus.customer.domain.exception.DomainRuleException;
import org.junit.jupiter.api.Test;

class AddressTest {

    private Customer customerWithAddress(String state) {
        Customer customer = Customer.create("Joao", "joao@x.com", null);
        customer.addAddress("01001-000", "Praca da Se", "100", null, "Se", "Sao Paulo", state, true);
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
        Customer customer = Customer.create("Joao", "joao@x.com", null);
        assertThatThrownBy(() ->
                customer.addAddress("01001-000", "  ", "100", null, "Se", "Sao Paulo", "SP", true))
                .isInstanceOf(DomainRuleException.class);
    }

    @Test
    void invalidStateLengthIsRejected() {
        Customer customer = Customer.create("Joao", "joao@x.com", null);
        assertThatThrownBy(() ->
                customer.addAddress("01001-000", "Praca da Se", "100", null, "Se", "Sao Paulo", "SPX", true))
                .isInstanceOf(DomainRuleException.class);
    }
}
