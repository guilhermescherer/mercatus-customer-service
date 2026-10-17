package com.mercatus.customer.web.customer.dto;

import com.mercatus.customer.domain.customer.Address;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        String zipCode,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state,
        String country,
        boolean isDefault) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.id(),
                address.zipCode(),
                address.street(),
                address.number(),
                address.complement(),
                address.district(),
                address.city(),
                address.state(),
                address.country(),
                address.isDefault());
    }
}
