package com.company.observability.starter.web;

import com.company.observability.starter.web.ErrorResponse;
import com.company.observability.starter.service.ExceptionLoggingService;
import com.company.observability.starter.web.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldReturnInternalServerErrorResponseWithCorrelationIdAndLogException() {
        ExceptionLoggingService exceptionLoggingService = mock(ExceptionLoggingService.class);
        GlobalExceptionHandler handler = new GlobalExceptionHandler(exceptionLoggingService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/demo/ping");

        Exception exception = new RuntimeException("Database connection failed");
        MDC.put(CorrelationIdFilter.MDC_KEY, "corr-123");

        ResponseEntity<Object> responseEntity = handler.handleException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertInstanceOf(ErrorResponse.class, responseEntity.getBody());

        ErrorResponse body = (ErrorResponse) responseEntity.getBody();

        assertAll(
                () -> assertNotNull(body.timestamp()),
                () -> assertEquals(500, body.status()),
                () -> assertEquals("Internal Server Error", body.error()),
                () -> assertEquals("Unexpected server error", body.message()),
                () -> assertEquals("corr-123", body.correlationId()),
                () -> assertEquals("/demo/ping", body.path())
        );

        verify(exceptionLoggingService).logUnhandledException(exception, request);
    }

    @Test
    void shouldNotLeakOriginalExceptionMessageToResponseBody() {
        ExceptionLoggingService exceptionLoggingService = mock(ExceptionLoggingService.class);
        GlobalExceptionHandler handler = new GlobalExceptionHandler(exceptionLoggingService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users");

        Exception exception = new RuntimeException("Sensitive database error details");
        MDC.put(CorrelationIdFilter.MDC_KEY, "corr-999");

        ResponseEntity<Object> responseEntity = handler.handleException(exception, request);

        ErrorResponse body = (ErrorResponse) responseEntity.getBody();

        assertNotNull(body);
        assertEquals("Unexpected server error", body.message());
        assertNotEquals(exception.getMessage(), body.message());
        assertFalse(body.message().contains("Sensitive"));
    }

    @Test
    void shouldReturnNullCorrelationIdWhenMdcDoesNotContainIt() {
        ExceptionLoggingService exceptionLoggingService = mock(ExceptionLoggingService.class);
        GlobalExceptionHandler handler = new GlobalExceptionHandler(exceptionLoggingService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/health");

        Exception exception = new RuntimeException("boom");

        ResponseEntity<Object> responseEntity = handler.handleException(exception, request);

        ErrorResponse body = (ErrorResponse) responseEntity.getBody();

        assertNotNull(body);
        assertNull(body.correlationId());
        assertEquals("/health", body.path());

        verify(exceptionLoggingService).logUnhandledException(exception, request);
    }
}