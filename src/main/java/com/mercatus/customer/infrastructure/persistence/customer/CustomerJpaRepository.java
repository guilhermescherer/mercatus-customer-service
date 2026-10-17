package com.mercatus.customer.infrastructure.persistence.customer;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface CustomerJpaRepository
        extends JpaRepository<CustomerEntity, UUID>, JpaSpecificationExecutor<CustomerEntity> {

    boolean existsByEmailIgnoreCase(String email);
}
