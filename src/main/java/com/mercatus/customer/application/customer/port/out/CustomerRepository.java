package com.mercatus.customer.application.customer.port.out;

import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.customer.CustomerFilter;
import com.mercatus.customer.domain.customer.Email;
import com.mercatus.customer.domain.shared.PageQuery;
import com.mercatus.customer.domain.shared.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    boolean existsByEmail(Email email);

    PageResult<Customer> findAll(CustomerFilter filter, PageQuery pageQuery);
}
