package com.mercatus.customer.application.customer.port.in;

import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.customer.CustomerFilter;
import com.mercatus.customer.domain.shared.PageQuery;
import com.mercatus.customer.domain.shared.PageResult;

public interface ListCustomersUseCase {

    PageResult<Customer> list(CustomerFilter filter, PageQuery pageQuery);
}
