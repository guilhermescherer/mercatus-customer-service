package com.mercatus.customer.application.customer.port.in;

import com.mercatus.customer.domain.customer.Address;

import java.util.List;
import java.util.UUID;

public interface ListAddressesUseCase {

    List<Address> list(UUID customerId);
}
