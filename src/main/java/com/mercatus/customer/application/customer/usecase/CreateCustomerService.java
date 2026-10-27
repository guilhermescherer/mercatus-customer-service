package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.CreateCustomerUseCase;
import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.application.exception.BusinessRuleException;
import com.mercatus.customer.application.exception.StateConflictException;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.exception.DomainRuleException;
import com.mercatus.customer.domain.exception.DuplicateEmailException;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateCustomerService implements CreateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final Clock clock;
    private final Supplier<UUID> identifiers;

    CreateCustomerService(CustomerRepository customerRepository, Clock clock, Supplier<UUID> identifiers) {
        this.customerRepository = customerRepository;
        this.clock = clock;
        this.identifiers = identifiers;
    }

    @Override
    @Transactional
    public Customer create(CreateCustomerCommand command) {
        try {
            Customer customer = Customer.create(
                    identifiers.get(), command.name(), command.email(), command.phone(), clock.instant());
            if (customerRepository.existsByEmail(customer.email())) {
                throw new StateConflictException("Customer email already exists: " + customer.email().value());
            }
            return customerRepository.save(customer);
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        } catch (DuplicateEmailException ex) {
            throw new StateConflictException(ex.getMessage());
        }
    }
}
