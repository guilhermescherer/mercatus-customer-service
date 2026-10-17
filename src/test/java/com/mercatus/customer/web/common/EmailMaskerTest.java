package com.mercatus.customer.web.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailMaskerTest {

    @Test
    void masksLocalPartKeepingFirstLetterAndDomain() {
        assertThat(EmailMasker.mask("joao@x.com")).isEqualTo("j***@x.com");
    }

    @Test
    void masksNullOrBlank() {
        assertThat(EmailMasker.mask(null)).isEqualTo("***");
        assertThat(EmailMasker.mask("  ")).isEqualTo("***");
    }

    @Test
    void masksValueWithoutAt() {
        assertThat(EmailMasker.mask("invalid")).isEqualTo("***");
    }
}
