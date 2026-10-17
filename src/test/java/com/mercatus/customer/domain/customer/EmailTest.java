package com.mercatus.customer.domain.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mercatus.customer.domain.exception.DomainRuleException;
import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void normalizesToLowerCase() {
        assertThat(Email.of("JOAO@X.COM").value()).isEqualTo("joao@x.com");
    }

    @Test
    void trimsSurroundingSpaces() {
        assertThat(Email.of("  joao@x.com  ").value()).isEqualTo("joao@x.com");
    }

    @Test
    void equalIgnoringCase() {
        assertThat(Email.of("Joao@X.com")).isEqualTo(Email.of("joao@x.com"));
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> Email.of("  ")).isInstanceOf(DomainRuleException.class);
    }

    @Test
    void rejectsInvalidFormat() {
        assertThatThrownBy(() -> Email.of("not-an-email")).isInstanceOf(DomainRuleException.class);
    }
}
