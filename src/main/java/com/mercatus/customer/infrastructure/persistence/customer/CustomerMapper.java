package com.mercatus.customer.infrastructure.persistence.customer;

import com.mercatus.customer.domain.customer.Address;
import com.mercatus.customer.domain.customer.Customer;

import java.util.List;

final class CustomerMapper {

    private CustomerMapper() {
    }

    static CustomerEntity toEntity(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.setId(customer.id());
        entity.setName(customer.name());
        entity.setEmail(customer.email().value());
        entity.setPhone(customer.phone());
        entity.setStatus(customer.status());
        entity.setCreatedAt(customer.createdAt());
        entity.setUpdatedAt(customer.updatedAt());
        entity.setVersion(customer.version());
        List<AddressEntity> addresses = customer.addresses().stream()
                .map(CustomerMapper::toEntity)
                .toList();
        entity.getAddresses().clear();
        entity.getAddresses().addAll(addresses);
        return entity;
    }

    static Customer toDomain(CustomerEntity entity) {
        List<Address> addresses = entity.getAddresses().stream()
                .map(CustomerMapper::toDomain)
                .toList();
        return Customer.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getStatus(),
                addresses,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }

    private static AddressEntity toEntity(Address address) {
        AddressEntity entity = new AddressEntity();
        entity.setId(address.id());
        entity.setZipCode(address.zipCode());
        entity.setStreet(address.street());
        entity.setNumber(address.number());
        entity.setComplement(address.complement());
        entity.setDistrict(address.district());
        entity.setCity(address.city());
        entity.setState(address.state());
        entity.setCountry(address.country());
        entity.setDefault(address.isDefault());
        return entity;
    }

    private static Address toDomain(AddressEntity entity) {
        return Address.reconstitute(
                entity.getId(),
                entity.getZipCode(),
                entity.getStreet(),
                entity.getNumber(),
                entity.getComplement(),
                entity.getDistrict(),
                entity.getCity(),
                entity.getState(),
                entity.getCountry(),
                entity.isDefault());
    }
}
