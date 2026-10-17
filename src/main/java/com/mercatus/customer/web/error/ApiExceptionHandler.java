package com.mercatus.customer.web.error;

import com.mercatus.customer.application.exception.BusinessRuleException;
import com.mercatus.customer.application.exception.ResourceNotFoundException;
import com.mercatus.customer.application.exception.StateConflictException;
import com.mercatus.customer.domain.exception.DomainRuleException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({BusinessRuleException.class, DomainRuleException.class})
    public ProblemDetail handleBusinessRule(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler({
        StateConflictException.class,
        OptimisticLockingFailureException.class,
        DataIntegrityViolationException.class
    })
    public ProblemDetail handleConflict(RuntimeException ex) {
        String detail = ex instanceof StateConflictException ? ex.getMessage() : "Conflict with current state";
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    }
}
