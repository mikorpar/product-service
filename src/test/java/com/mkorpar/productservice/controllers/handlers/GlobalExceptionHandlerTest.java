package com.mkorpar.productservice.controllers.handlers;

import com.mkorpar.productservice.data.rest.ErrorData;
import com.mkorpar.productservice.data.rest.ValidationErrorData;
import com.mkorpar.productservice.data.rest.ValidationErrorDataList;
import com.mkorpar.productservice.exceptions.DuplicateProductCodeException;
import com.mkorpar.productservice.exceptions.ProductNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private JacksonProperties jacksonProperties;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(jacksonProperties.getPropertyNamingStrategy()).thenReturn("SNAKE_CASE");
        exceptionHandler = new GlobalExceptionHandler(jacksonProperties);
    }

    @Test
    void shouldReturnBadRequestWithValidationErrors() {
        // Arrange
        String fieldName = "code";
        String violationMessage = "must not be blank";

        FieldError fieldError = new FieldError("product", fieldName, violationMessage);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<ValidationErrorDataList> response = exceptionHandler.handleValidationException(exception);

        // Assert
        assertViolationErrorResponse(response, fieldName, violationMessage);
    }

    @Test
    void shouldReturnBadRequestWithViolationErrors() {
        // Arrange
        String fieldName = "code";
        String violationMessage = "must not be blank";

        Path.Node node = mock(Path.Node.class);
        when(node.getName()).thenReturn(fieldName);

        Path path = mock(Path.class);
        when(path.iterator()).thenReturn(List.of(node).iterator());

        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(violationMessage);

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        // Act
        ResponseEntity<ValidationErrorDataList> response = exceptionHandler.handleViolationException(exception);

        // Assert
        assertViolationErrorResponse(response, fieldName, violationMessage);
    }

    @Test
    void shouldReturnNotFoundStatus() {
        // Arrange
        ProductNotFoundException exception = new ProductNotFoundException("Product not found");

        // Act
        ResponseEntity<ErrorData> response = exceptionHandler.handleProductNotFoundException(exception);

        // Assert
        assertCustomExcepctionHandlerResponse(response, exception, HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnConflictStatus() {
        // Arrange
        DuplicateProductCodeException exception = new DuplicateProductCodeException("Product code already exists");

        // Act
        ResponseEntity<ErrorData> response = exceptionHandler.handleDuplicateProductCodeException(exception);

        // Assert
        assertCustomExcepctionHandlerResponse(response, exception, HttpStatus.CONFLICT);
    }

    private void assertViolationErrorResponse(ResponseEntity<ValidationErrorDataList> response, String fieldName, String message) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> assertThat(body.errors())
                        .hasSize(1)
                        .first()
                        .extracting(ValidationErrorData::field, ValidationErrorData::message)
                        .containsExactly(fieldName, message)
                );

    }

    void assertCustomExcepctionHandlerResponse(ResponseEntity<ErrorData> response, RuntimeException e, HttpStatusCode status) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull()
                .satisfies(body -> {
                    assertThat(body.error()).isEqualTo(e.getClass().getSimpleName());
                    assertThat(body.message()).isEqualTo(e.getMessage());
                });
    }

}