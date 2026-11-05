package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.ListAddressesUseCase;
import com.mercatus.customer.application.customer.usecase.support.CustomerAccess;
import com.mercatus.customer.domain.customer.Address;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ListAddressesService implements ListAddressesUseCase {

    private final CustomerAccess gateway;

    ListAddressesService(CustomerAccess gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> list(UUID customerId) {
        return gateway.require(customerId).addresses();
    }
}
