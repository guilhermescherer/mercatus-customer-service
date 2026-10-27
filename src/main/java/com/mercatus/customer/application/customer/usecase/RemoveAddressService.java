package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.RemoveAddressUseCase;
import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.application.customer.usecase.support.CustomerGateway;
import com.mercatus.customer.application.exception.ResourceNotFoundException;
import com.mercatus.customer.application.exception.StateConflictException;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.exception.AddressNotFoundException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RemoveAddressService implements RemoveAddressUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerGateway gateway;
    private final Clock clock;

    RemoveAddressService(CustomerRepository customerRepository, CustomerGateway gateway, Clock clock) {
        this.customerRepository = customerRepository;
        this.gateway = gateway;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Customer remove(UUID customerId, UUID addressId) {
        Customer customer = gateway.require(customerId);
        try {
            customer.removeAddress(addressId, clock.instant());
            return customerRepository.save(customer);
        } catch (AddressNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (OptimisticLockingFailureException ex) {
            throw new StateConflictException("Customer was modified concurrently");
        }
    }
}
