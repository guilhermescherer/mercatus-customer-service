package com.mercatus.customer.web.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class EndpointAuthorization {

    private static final String CUSTOMER = "CUSTOMER";
    private static final String OPERATOR = "OPERATOR";
    private static final String ADMIN = "ADMIN";
    private static final String SERVICE = "SERVICE";

    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(POST, "/customers").hasRole(CUSTOMER)
                .requestMatchers(GET, "/customers").hasRole(OPERATOR)
                .requestMatchers(POST, "/customers/*:activate", "/customers/*:deactivate").hasRole(ADMIN)
                .requestMatchers(GET, "/customers/*").hasAnyRole(CUSTOMER, OPERATOR, SERVICE)
                .requestMatchers(PUT, "/customers/*").hasRole(CUSTOMER)
                .requestMatchers(GET, "/customers/*/addresses").hasAnyRole(CUSTOMER, OPERATOR)
                .requestMatchers(POST, "/customers/*/addresses").hasRole(CUSTOMER)
                .requestMatchers(PUT, "/customers/*/addresses/*", "/customers/*/addresses/*/default").hasRole(CUSTOMER)
                .requestMatchers(DELETE, "/customers/*/addresses/*").hasRole(CUSTOMER);
    }
}
