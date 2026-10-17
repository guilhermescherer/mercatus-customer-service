package com.mercatus.customer.application.customer.port.in;

import com.mercatus.customer.domain.customer.Customer;

import java.util.UUID;

public interface ChangeCustomerStatusUseCase {

    Customer activate(UUID id);

    Customer deactivate(UUID id);
}
