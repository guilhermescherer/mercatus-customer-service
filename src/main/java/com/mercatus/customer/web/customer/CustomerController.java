package com.mercatus.customer.web.customer;

import com.mercatus.customer.application.customer.port.in.ChangeCustomerStatusUseCase;
import com.mercatus.customer.application.customer.port.in.CreateCustomerUseCase;
import com.mercatus.customer.application.customer.port.in.CreateCustomerUseCase.CreateCustomerCommand;
import com.mercatus.customer.application.customer.port.in.GetCustomerUseCase;
import com.mercatus.customer.application.customer.port.in.ListCustomersUseCase;
import com.mercatus.customer.application.customer.port.in.UpdateCustomerUseCase;
import com.mercatus.customer.application.customer.port.in.UpdateCustomerUseCase.UpdateCustomerCommand;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.customer.CustomerFilter;
import com.mercatus.customer.domain.customer.Status;
import com.mercatus.customer.domain.shared.PageQuery;
import com.mercatus.customer.domain.shared.PageResult;
import com.mercatus.customer.web.common.EmailMasker;
import com.mercatus.customer.web.common.PageResponse;
import com.mercatus.customer.web.customer.dto.CreateCustomerRequest;
import com.mercatus.customer.web.customer.dto.CustomerResponse;
import com.mercatus.customer.web.customer.dto.UpdateCustomerRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CreateCustomerUseCase createCustomer;
    private final GetCustomerUseCase getCustomer;
    private final ListCustomersUseCase listCustomers;
    private final UpdateCustomerUseCase updateCustomer;
    private final ChangeCustomerStatusUseCase changeStatus;

    CustomerController(CreateCustomerUseCase createCustomer, GetCustomerUseCase getCustomer,
                       ListCustomersUseCase listCustomers, UpdateCustomerUseCase updateCustomer,
                       ChangeCustomerStatusUseCase changeStatus) {
        this.createCustomer = createCustomer;
        this.getCustomer = getCustomer;
        this.listCustomers = listCustomers;
        this.updateCustomer = updateCustomer;
        this.changeStatus = changeStatus;
    }

    @PostMapping
    ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        log.info("Creating customer with email {}", EmailMasker.mask(request.email()));
        CreateCustomerCommand command = new CreateCustomerCommand(request.name(), request.email(), request.phone());
        Customer customer = createCustomer.create(command);

        return ResponseEntity.created(URI.create("/customers/" + customer.id()))
                .body(CustomerResponse.from(customer));
    }

    @GetMapping("/{id}")
    CustomerResponse get(@PathVariable UUID id) {
        return CustomerResponse.from(getCustomer.get(id));
    }

    @GetMapping
    PageResponse<CustomerResponse> list(@RequestParam(required = false) Status status,
                                        @RequestParam(required = false) String search,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) String sort) {
        PageResult<Customer> customers = listCustomers.list(new CustomerFilter(status, search), new PageQuery(page, size, sort));
        return PageResponse.from(customers, CustomerResponse::from);
    }

    @PutMapping("/{id}")
    CustomerResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
        log.info("Updating customer {} with email {}", id, EmailMasker.mask(request.email()));
        UpdateCustomerCommand command = new UpdateCustomerCommand(id, request.name(), request.email(), request.phone(), request.version());
        Customer customer = updateCustomer.update(command);

        return CustomerResponse.from(customer);
    }

    @PostMapping("/{id}:activate")
    CustomerResponse activate(@PathVariable UUID id) {
        return CustomerResponse.from(changeStatus.activate(id));
    }

    @PostMapping("/{id}:deactivate")
    CustomerResponse deactivate(@PathVariable UUID id) {
        return CustomerResponse.from(changeStatus.deactivate(id));
    }
}
