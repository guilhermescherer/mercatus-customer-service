package com.mercatus.customer.application.customer.port.in;

import com.mercatus.customer.domain.customer.Customer;

import java.util.UUID;

public interface UpdateCustomerUseCase {

    Customer update(UpdateCustomerCommand command);

    record UpdateCustomerCommand(UUID id, String name, String email, String phone, long expectedVersion) {
    }
}
