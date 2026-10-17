package com.mercatus.customer.application.customer.port.in;

import com.mercatus.customer.domain.customer.Customer;

import java.util.UUID;

public interface RemoveAddressUseCase {

    Customer remove(UUID customerId, UUID addressId);
}
