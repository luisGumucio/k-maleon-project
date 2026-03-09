package com.kmaleon.exception;

import com.kmaleon.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void whenResourceNotFound_thenReturns404WithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Operation not found: 123");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Operation not found: 123");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void whenValidationFails_thenReturns400WithFieldMessages() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "amount", "must be greater than 0"));
        bindingResult.addError(new FieldError("request", "type", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("must be greater than 0");
        assertThat(response.getBody().getMessage()).contains("must not be blank");
    }

    @Test
    void whenUnreadableBody_thenReturns400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response = handler.handleUnreadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Malformed or missing request body");
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void whenGenericException_thenReturns500WithoutDetails() {
        Exception ex = new RuntimeException("DB connection failed - sensitive info");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getMessage()).doesNotContain("DB connection failed");
        assertThat(response.getBody().getMessage()).doesNotContain("sensitive info");
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void whenResourceNotFound_thenErrorFieldIsSet() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Supplier not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertThat(response.getBody().getError()).isEqualTo("Not Found");
    }

    @Test
    void whenValidationFails_withNoErrors_thenMessageIsEmpty() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEmpty();
    }
}
