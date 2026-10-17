package com.mercatus.customer.application.customer.usecase.support;

import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.application.exception.ResourceNotFoundException;
import com.mercatus.customer.application.exception.StateConflictException;
import com.mercatus.customer.domain.customer.Customer;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerGateway {

    private final CustomerRepository customerRepository;

    public CustomerGateway(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer require(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    public void checkVersion(Long current, long expected) {
        if (current == null || current != expected) {
            throw new StateConflictException("Version conflict for customer");
        }
    }
}
