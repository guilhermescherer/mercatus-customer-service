package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.UpdateAddressUseCase;
import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.application.customer.usecase.support.CustomerGateway;
import com.mercatus.customer.application.exception.BusinessRuleException;
import com.mercatus.customer.application.exception.ResourceNotFoundException;
import com.mercatus.customer.application.exception.StateConflictException;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.exception.AddressNotFoundException;
import com.mercatus.customer.domain.exception.DomainRuleException;
import java.time.Clock;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateAddressService implements UpdateAddressUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerGateway gateway;
    private final Clock clock;

    UpdateAddressService(CustomerRepository customerRepository, CustomerGateway gateway, Clock clock) {
        this.customerRepository = customerRepository;
        this.gateway = gateway;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Customer update(UpdateAddressCommand command) {
        Customer customer = gateway.require(command.customerId());
        try {
            customer.updateAddress(
                    command.addressId(),
                    command.zipCode(),
                    command.street(),
                    command.number(),
                    command.complement(),
                    command.district(),
                    command.city(),
                    command.state(),
                    clock.instant());
            return customerRepository.save(customer);
        } catch (AddressNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        } catch (OptimisticLockingFailureException ex) {
            throw new StateConflictException("Customer was modified concurrently");
        }
    }
}
