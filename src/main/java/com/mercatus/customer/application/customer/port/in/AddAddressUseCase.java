package com.mercatus.customer.application.customer.port.in;

import com.mercatus.customer.domain.customer.Customer;

import java.util.UUID;

public interface AddAddressUseCase {

    Customer add(AddAddressCommand command);

    record AddAddressCommand(
            UUID customerId,
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state,
            boolean asDefault) {
    }
}
