package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.ChangeCustomerStatusUseCase;
import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.application.customer.usecase.support.CustomerGateway;
import com.mercatus.customer.application.exception.StateConflictException;
import com.mercatus.customer.domain.customer.Customer;
import java.time.Clock;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ChangeCustomerStatusService implements ChangeCustomerStatusUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerGateway gateway;
    private final Clock clock;

    ChangeCustomerStatusService(CustomerRepository customerRepository, CustomerGateway gateway, Clock clock) {
        this.customerRepository = customerRepository;
        this.gateway = gateway;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Customer activate(UUID id) {
        Customer customer = gateway.require(id);
        customer.activate(clock.instant());
        return persist(customer);
    }

    @Override
    @Transactional
    public Customer deactivate(UUID id) {
        Customer customer = gateway.require(id);
        customer.deactivate(clock.instant());
        return persist(customer);
    }

    private Customer persist(Customer customer) {
        try {
            return customerRepository.save(customer);
        } catch (OptimisticLockingFailureException ex) {
            throw new StateConflictException("Customer was modified concurrently");
        }
    }
}
