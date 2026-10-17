package com.mercatus.customer.infrastructure.persistence.customer;

import com.mercatus.customer.application.customer.port.out.CustomerRepository;
import com.mercatus.customer.domain.customer.Customer;
import com.mercatus.customer.domain.customer.CustomerFilter;
import com.mercatus.customer.domain.customer.Email;
import com.mercatus.customer.domain.exception.DuplicateEmailException;
import com.mercatus.customer.domain.shared.PageQuery;
import com.mercatus.customer.domain.shared.PageResult;
import com.mercatus.customer.infrastructure.persistence.support.SortParser;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;

    CustomerRepositoryAdapter(CustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        try {
            CustomerEntity saved = jpaRepository.saveAndFlush(CustomerMapper.toEntity(customer));
            return CustomerMapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException("Customer email already exists: " + customer.email().value());
        }
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpaRepository.findById(id).map(CustomerMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmailIgnoreCase(email.value());
    }

    @Override
    public PageResult<Customer> findAll(CustomerFilter filter, PageQuery pageQuery) {
        PageRequest pageRequest = PageRequest.of(
                pageQuery.page(), pageQuery.size(), SortParser.parse(pageQuery.sort(), "name"));
        Page<CustomerEntity> page = jpaRepository.findAll(toSpecification(filter), pageRequest);
        List<Customer> content = page.getContent().stream().map(CustomerMapper::toDomain).toList();
        return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private Specification<CustomerEntity> toSpecification(CustomerFilter filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null && filter.status() != null) {
                predicates.add(builder.equal(root.get("status"), filter.status()));
            }
            if (filter != null && filter.search() != null && !filter.search().isBlank()) {
                String pattern = "%" + filter.search().trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("email")), pattern)));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
