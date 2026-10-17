package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.SetDefaultAddressUseCase;
import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.application.customer.usecase.support.CustomerGateway;
import com.mercatus.customer.application.exception.ResourceNotFoundException;
import com.mercatus.customer.application.exception.StateConflictException;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.exception.AddressNotFoundException;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SetDefaultAddressService implements SetDefaultAddressUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerGateway gateway;

    SetDefaultAddressService(CustomerRepository customerRepository, CustomerGateway gateway) {
        this.customerRepository = customerRepository;
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Customer setDefault(UUID customerId, UUID addressId) {
        Customer customer = gateway.require(customerId);
        try {
            customer.setDefaultAddress(addressId);
            return customerRepository.save(customer);
        } catch (AddressNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (OptimisticLockingFailureException ex) {
            throw new StateConflictException("Customer was modified concurrently");
        }
    }
}
