package com.mercatus.customer.domain.customer;

import com.mercatus.customer.domain.exception.AddressNotFoundException;
import com.mercatus.customer.domain.exception.DomainRuleException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Customer {

    private final UUID id;
    private String name;
    private Email email;
    private String phone;
    private Status status;
    private final List<Address> addresses;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private Customer(
            UUID id,
            String name,
            Email email,
            String phone,
            Status status,
            List<Address> addresses,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.addresses = new ArrayList<>(addresses);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Customer create(String name, String email, String phone) {
        Instant now = Instant.now();
        return new Customer(
                UUID.randomUUID(),
                requireName(name),
                Email.of(email),
                normalizePhone(phone),
                Status.ACTIVE,
                new ArrayList<>(),
                now,
                now,
                null);
    }

    public static Customer reconstitute(
            UUID id,
            String name,
            String email,
            String phone,
            Status status,
            List<Address> addresses,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        return new Customer(id, name, Email.of(email), phone, status, addresses, createdAt, updatedAt, version);
    }

    public void update(String name, String email, String phone) {
        this.name = requireName(name);
        this.email = Email.of(email);
        this.phone = normalizePhone(phone);
        touch();
    }

    public void activate() {
        this.status = Status.ACTIVE;
        touch();
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
        touch();
    }

    public Address addAddress(
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state,
            boolean asDefault) {
        boolean makeDefault = asDefault || addresses.isEmpty();
        Address address = Address.create(zipCode, street, number, complement, district, city, state, makeDefault);
        if (makeDefault) {
            clearDefaults();
        }
        addresses.add(address);
        touch();
        return address;
    }

    public Address updateAddress(
            UUID addressId,
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state) {
        Address address = requireAddress(addressId);
        address.update(zipCode, street, number, complement, district, city, state);
        touch();
        return address;
    }

    public void removeAddress(UUID addressId) {
        Address address = requireAddress(addressId);
        addresses.remove(address);
        if (address.isDefault()) {
            addresses.stream().findFirst().ifPresent(Address::markDefault);
        }
        touch();
    }

    public void setDefaultAddress(UUID addressId) {
        Address address = requireAddress(addressId);
        clearDefaults();
        address.markDefault();
        touch();
    }

    public Optional<Address> defaultAddress() {
        return addresses.stream().filter(Address::isDefault).findFirst();
    }

    private Address requireAddress(UUID addressId) {
        return addresses.stream()
                .filter(address -> address.id().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new AddressNotFoundException("Address not found: " + addressId));
    }

    private void clearDefaults() {
        addresses.forEach(Address::unmarkDefault);
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainRuleException("Customer name must not be blank");
        }
        return name.trim();
    }

    private static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Email email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public Status status() {
        return status;
    }

    public List<Address> addresses() {
        return List.copyOf(addresses);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Long version() {
        return version;
    }
}
