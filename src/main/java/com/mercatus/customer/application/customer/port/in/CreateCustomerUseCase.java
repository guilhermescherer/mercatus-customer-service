package com.mercatus.customer.application.customer.port.in;

import com.mercatus.customer.domain.customer.Customer;

public interface CreateCustomerUseCase {

    Customer create(CreateCustomerCommand command);

    record CreateCustomerCommand(String name, String email, String phone) {
    }
}
