package com.mercatus.customer.application.customer.port.in;

import com.mercatus.customer.domain.customer.Customer;

import java.util.UUID;

public interface UpdateAddressUseCase {

    Customer update(UpdateAddressCommand command);

    record UpdateAddressCommand(
            UUID customerId,
            UUID addressId,
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state) {
    }
}
