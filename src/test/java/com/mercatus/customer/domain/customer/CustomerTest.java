package com.mercatus.customer.domain.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mercatus.customer.domain.exception.AddressNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CustomerTest {

    private Customer sampleCustomer() {
        return Customer.create("Joao", "joao@x.com", "11999999999");
    }

    private Address addAddress(Customer customer, boolean asDefault) {
        return customer.addAddress("01001-000", "Praca da Se", "100", null, "Se", "Sao Paulo", "SP", asDefault);
    }

    @Test
    void createsActiveCustomer() {
        assertThat(sampleCustomer().status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void firstAddressBecomesDefaultEvenWhenNotRequested() {
        Customer customer = sampleCustomer();
        Address address = addAddress(customer, false);
        assertThat(address.isDefault()).isTrue();
        assertThat(customer.defaultAddress()).contains(address);
    }

    @Test
    void settingNewDefaultUnmarksThePrevious() {
        Customer customer = sampleCustomer();
        Address a = addAddress(customer, true);
        Address b = addAddress(customer, true);

        assertThat(b.isDefault()).isTrue();
        assertThat(a.isDefault()).isFalse();
        assertThat(customer.addresses().stream().filter(Address::isDefault).count()).isEqualTo(1);
    }

    @Test
    void setDefaultAddressKeepsAtMostOneDefault() {
        Customer customer = sampleCustomer();
        Address a = addAddress(customer, true);
        Address b = addAddress(customer, false);

        customer.setDefaultAddress(b.id());

        assertThat(customer.defaultAddress()).contains(b);
        assertThat(a.isDefault()).isFalse();
        assertThat(customer.addresses().stream().filter(Address::isDefault).count()).isEqualTo(1);
    }

    @Test
    void removingDefaultPromotesAnotherAddress() {
        Customer customer = sampleCustomer();
        Address a = addAddress(customer, true);
        Address b = addAddress(customer, false);

        customer.removeAddress(a.id());

        assertThat(customer.addresses()).hasSize(1);
        assertThat(customer.defaultAddress()).contains(b);
    }

    @Test
    void operatingOnUnknownAddressThrowsNotFound() {
        Customer customer = sampleCustomer();
        assertThatThrownBy(() -> customer.setDefaultAddress(UUID.randomUUID()))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void deactivateAndReactivate() {
        Customer customer = sampleCustomer();
        customer.deactivate();
        assertThat(customer.status()).isEqualTo(Status.INACTIVE);
        customer.activate();
        assertThat(customer.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void updateChangesEmailNormalized() {
        Customer customer = sampleCustomer();
        customer.update("Joao Silva", "NOVO@X.COM", "11988887777");
        assertThat(customer.email().value()).isEqualTo("novo@x.com");
        assertThat(customer.name()).isEqualTo("Joao Silva");
    }
}
