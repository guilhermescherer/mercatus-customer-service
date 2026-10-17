package com.mercatus.customer.application.customer.usecase;

import com.mercatus.customer.application.customer.port.in.ListCustomersUseCase;
import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.customer.CustomerFilter;
import com.mercatus.customer.domain.shared.PageQuery;
import com.mercatus.customer.domain.shared.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ListCustomersService implements ListCustomersUseCase {

    private final CustomerRepository customerRepository;

    ListCustomersService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Customer> list(CustomerFilter filter, PageQuery pageQuery) {
        return customerRepository.findAll(filter, pageQuery);
    }
}
