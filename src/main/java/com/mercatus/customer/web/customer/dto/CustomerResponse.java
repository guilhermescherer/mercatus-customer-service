package com.mercatus.customer.web.customer.dto;

import com.mercatus.customer.domain.customer.Customer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String status,
        List<AddressResponse> addresses,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static CustomerResponse from(Customer customer) {
        List<AddressResponse> addresses = customer.addresses().stream()
                .map(AddressResponse::from)
                .toList();
        return new CustomerResponse(
                customer.id(),
                customer.name(),
                customer.email().value(),
                customer.phone(),
                customer.status().name(),
                addresses,
                customer.createdAt(),
                customer.updatedAt(),
                customer.version());
    }
}
