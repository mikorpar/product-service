package com.mkorpar.productservice.controllers.handlers;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mkorpar.productservice.data.rest.ErrorData;
import com.mkorpar.productservice.data.rest.ValidationErrorData;
import com.mkorpar.productservice.data.rest.ValidationErrorDataList;
import com.mkorpar.productservice.exceptions.DuplicateProductCodeException;
import com.mkorpar.productservice.exceptions.ProductNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.mkorpar.productservice.utils.PropertyNamingStrategyUtils;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final PropertyNamingStrategies.NamingBase namingStrategy;

    public GlobalExceptionHandler(JacksonProperties jacksonProperties) {
        this.namingStrategy = PropertyNamingStrategyUtils.getStrategy(jacksonProperties.getPropertyNamingStrategy());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDataList> handleValidationException(MethodArgumentNotValidException e) {
        return createResponse(getValidationErrors(e.getBindingResult()));
    }

    private List<ValidationErrorData> getValidationErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> new ValidationErrorData(
                        namingStrategy.translate(error.getField()),
                        Objects.requireNonNull(error.getDefaultMessage(), "")
                )).toList();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorDataList> handleViolationException(ConstraintViolationException e) {
        return createResponse(getValidationErrors(e.getConstraintViolations()));
    }

    private List<ValidationErrorData> getValidationErrors(Set<ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> new ValidationErrorData(
                        namingStrategy.translate(getFieldName(violation.getPropertyPath())),
                        Objects.requireNonNull(violation.getMessage(), "")
                )).toList();
    }

    private String getFieldName(Path propertyPath) {
        String fieldName = "";
        for (Path.Node node : propertyPath) {
            fieldName = node.getName();
        }
        return fieldName;
    }

    private ResponseEntity<ValidationErrorDataList> createResponse(List<ValidationErrorData> validationErrors) {
        return ResponseEntity.badRequest().body(new ValidationErrorDataList(validationErrors));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    private ResponseEntity<ErrorData> handleProductNotFoundException(ProductNotFoundException e) {
        return handleException(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateProductCodeException.class)
    private ResponseEntity<ErrorData> handleDuplicateProductCodeException(DuplicateProductCodeException e) {
        return handleException(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<ErrorData> handleException(Exception e) {
        return handleException(e, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<ErrorData> handleException(Exception e, HttpStatusCode status) {
        ErrorData errorData = new ErrorData(e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity.status(status).body(errorData);
    }

}
