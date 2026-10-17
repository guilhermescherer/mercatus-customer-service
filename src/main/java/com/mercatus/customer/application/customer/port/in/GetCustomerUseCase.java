package com.mercatus.customer.application.customer.port.in;

import com.mercatus.customer.domain.customer.Customer;

import java.util.UUID;

public interface GetCustomerUseCase {

    Customer get(UUID id);
}
