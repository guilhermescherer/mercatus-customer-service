package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.AddAddressUseCase;
import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.application.customer.usecase.support.CustomerGateway;
import com.mercatus.customer.application.exception.BusinessRuleException;
import com.mercatus.customer.application.exception.StateConflictException;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.exception.DomainRuleException;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AddAddressService implements AddAddressUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerGateway gateway;
    private final Clock clock;
    private final Supplier<UUID> identifiers;

    AddAddressService(CustomerRepository customerRepository, CustomerGateway gateway,
                      Clock clock, Supplier<UUID> identifiers) {
        this.customerRepository = customerRepository;
        this.gateway = gateway;
        this.clock = clock;
        this.identifiers = identifiers;
    }

    @Override
    @Transactional
    public Customer add(AddAddressCommand command) {
        Customer customer = gateway.require(command.customerId());
        try {
            customer.addAddress(
                    identifiers.get(),
                    command.zipCode(),
                    command.street(),
                    command.number(),
                    command.complement(),
                    command.district(),
                    command.city(),
                    command.state(),
                    command.asDefault(),
                    clock.instant());
            return customerRepository.save(customer);
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        } catch (OptimisticLockingFailureException ex) {
            throw new StateConflictException("Customer was modified concurrently");
        }
    }
}
