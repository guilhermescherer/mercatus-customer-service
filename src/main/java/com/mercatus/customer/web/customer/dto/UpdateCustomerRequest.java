package com.mercatus.customer.web.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        long version) {
}
