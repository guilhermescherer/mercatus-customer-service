package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.GetCustomerUseCase;
import com.mercatus.customer.application.customer.usecase.support.CustomerGateway;
import com.mercatus.customer.domain.customer.Customer;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class GetCustomerService implements GetCustomerUseCase {

    private final CustomerGateway gateway;

    GetCustomerService(CustomerGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer get(UUID id) {
        return gateway.require(id);
    }
}
