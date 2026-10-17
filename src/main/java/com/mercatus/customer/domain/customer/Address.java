package com.mercatus.customer.domain.customer;

import com.mercatus.customer.domain.exception.DomainRuleException;

import java.util.UUID;

public class Address {

    private static final String DEFAULT_COUNTRY = "BR";

    private final UUID id;
    private String zipCode;
    private String street;
    private String number;
    private String complement;
    private String district;
    private String city;
    private String state;
    private final String country;
    private boolean isDefault;

    private Address(
            UUID id,
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state,
            String country,
            boolean isDefault) {
        this.id = id;
        this.zipCode = zipCode;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.district = district;
        this.city = city;
        this.state = state;
        this.country = country;
        this.isDefault = isDefault;
    }

    static Address create(
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state,
            boolean isDefault) {
        return new Address(
                UUID.randomUUID(),
                requireField(zipCode, "zipCode"),
                requireField(street, "street"),
                requireField(number, "number"),
                normalizeOptional(complement),
                requireField(district, "district"),
                requireField(city, "city"),
                requireState(state),
                DEFAULT_COUNTRY,
                isDefault);
    }

    public static Address reconstitute(
            UUID id,
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state,
            String country,
            boolean isDefault) {
        return new Address(id, zipCode, street, number, complement, district, city, state, country, isDefault);
    }

    void update(
            String zipCode,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state) {
        this.zipCode = requireField(zipCode, "zipCode");
        this.street = requireField(street, "street");
        this.number = requireField(number, "number");
        this.complement = normalizeOptional(complement);
        this.district = requireField(district, "district");
        this.city = requireField(city, "city");
        this.state = requireState(state);
    }

    void markDefault() {
        this.isDefault = true;
    }

    void unmarkDefault() {
        this.isDefault = false;
    }

    private static String requireField(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainRuleException("Address " + field + " must not be blank");
        }
        return value.trim();
    }

    private static String requireState(String state) {
        String normalized = requireField(state, "state").toUpperCase();
        if (normalized.length() != 2) {
            throw new DomainRuleException("Address state must be a two-letter UF");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public UUID id() {
        return id;
    }

    public String zipCode() {
        return zipCode;
    }

    public String street() {
        return street;
    }

    public String number() {
        return number;
    }

    public String complement() {
        return complement;
    }

    public String district() {
        return district;
    }

    public String city() {
        return city;
    }

    public String state() {
        return state;
    }

    public String country() {
        return country;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
