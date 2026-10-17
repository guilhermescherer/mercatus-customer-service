package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.UpdateCustomerUseCase;
import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.application.customer.usecase.support.CustomerGateway;
import com.mercatus.customer.application.exception.BusinessRuleException;
import com.mercatus.customer.application.exception.StateConflictException;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.customer.Email;
import com.mercatus.customer.domain.exception.DomainRuleException;
import com.mercatus.customer.domain.exception.DuplicateEmailException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateCustomerService implements UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerGateway gateway;

    UpdateCustomerService(CustomerRepository customerRepository, CustomerGateway gateway) {
        this.customerRepository = customerRepository;
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Customer update(UpdateCustomerCommand command) {
        Customer customer = gateway.require(command.id());
        gateway.checkVersion(customer.version(), command.expectedVersion());
        Email previousEmail = customer.email();
        try {
            customer.update(command.name(), command.email(), command.phone());
            if (!customer.email().equals(previousEmail) && customerRepository.existsByEmail(customer.email())) {
                throw new StateConflictException("Customer email already exists: " + customer.email().value());
            }
            return customerRepository.save(customer);
        } catch (DomainRuleException ex) {
            throw new BusinessRuleException(ex.getMessage());
        } catch (DuplicateEmailException ex) {
            throw new StateConflictException(ex.getMessage());
        } catch (OptimisticLockingFailureException ex) {
            throw new StateConflictException("Customer was modified concurrently");
        }
    }
}
