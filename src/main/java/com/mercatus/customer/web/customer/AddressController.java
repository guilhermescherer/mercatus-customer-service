package com.mercatus.customer.web.customer;

import com.mercatus.customer.application.customer.port.in.AddAddressUseCase;
import com.mercatus.customer.application.customer.port.in.AddAddressUseCase.AddAddressCommand;
import com.mercatus.customer.application.customer.port.in.ListAddressesUseCase;
import com.mercatus.customer.application.customer.port.in.RemoveAddressUseCase;
import com.mercatus.customer.application.customer.port.in.SetDefaultAddressUseCase;
import com.mercatus.customer.application.customer.port.in.UpdateAddressUseCase;
import com.mercatus.customer.application.customer.port.in.UpdateAddressUseCase.UpdateAddressCommand;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.web.customer.dto.AddressResponse;
import com.mercatus.customer.web.customer.dto.CreateAddressRequest;
import com.mercatus.customer.web.customer.dto.CustomerResponse;
import com.mercatus.customer.web.customer.dto.UpdateAddressRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers/{customerId}/addresses")
class AddressController {

    private final AddAddressUseCase addAddress;
    private final UpdateAddressUseCase updateAddress;
    private final RemoveAddressUseCase removeAddress;
    private final SetDefaultAddressUseCase setDefaultAddress;
    private final ListAddressesUseCase listAddresses;

    AddressController(AddAddressUseCase addAddress, UpdateAddressUseCase updateAddress,
                      RemoveAddressUseCase removeAddress, SetDefaultAddressUseCase setDefaultAddress,
                      ListAddressesUseCase listAddresses) {
        this.addAddress = addAddress;
        this.updateAddress = updateAddress;
        this.removeAddress = removeAddress;
        this.setDefaultAddress = setDefaultAddress;
        this.listAddresses = listAddresses;
    }

    @PostMapping
    ResponseEntity<CustomerResponse> add(@PathVariable UUID customerId,
                                         @Valid @RequestBody CreateAddressRequest request) {
        AddAddressCommand command = new AddAddressCommand(
                customerId,
                request.zipCode(),
                request.street(),
                request.number(),
                request.complement(),
                request.district(),
                request.city(),
                request.state(),
                request.asDefault());
        Customer customer = addAddress.add(command);

        return ResponseEntity.status(201).body(CustomerResponse.from(customer));
    }

    @GetMapping
    List<AddressResponse> list(@PathVariable UUID customerId) {
        return listAddresses.list(customerId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    @PutMapping("/{addressId}")
    CustomerResponse update(@PathVariable UUID customerId, @PathVariable UUID addressId,
                            @Valid @RequestBody UpdateAddressRequest request) {
        UpdateAddressCommand command = new UpdateAddressCommand(
                customerId,
                addressId,
                request.zipCode(),
                request.street(),
                request.number(),
                request.complement(),
                request.district(),
                request.city(),
                request.state());
        Customer customer = updateAddress.update(command);

        return CustomerResponse.from(customer);
    }

    @DeleteMapping("/{addressId}")
    CustomerResponse remove(@PathVariable UUID customerId, @PathVariable UUID addressId) {
        return CustomerResponse.from(removeAddress.remove(customerId, addressId));
    }

    @PutMapping("/{addressId}/default")
    CustomerResponse setDefault(@PathVariable UUID customerId, @PathVariable UUID addressId) {
        return CustomerResponse.from(setDefaultAddress.setDefault(customerId, addressId));
    }
}
