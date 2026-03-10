package com.company.observability.starter.service;

import com.company.observability.starter.domain.CorrelationContext;
import com.company.observability.starter.domain.UuidCorrelationIdGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorrelationIdServiceTest {

    @Test
    void shouldReturnExistingCorrelationIdWhenProvided() {
        UuidCorrelationIdGenerator generator = mock(UuidCorrelationIdGenerator.class);
        CorrelationIdService service = new CorrelationIdService(generator);

        CorrelationContext result = service.resolve("existing-id");

        assertEquals("existing-id", result.correlationId());
        assertFalse(result.generated());
        verifyNoInteractions(generator);
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() {
        UuidCorrelationIdGenerator generator = mock(UuidCorrelationIdGenerator.class);
        when(generator.generate()).thenReturn("generated-id");

        CorrelationIdService service = new CorrelationIdService(generator);

        CorrelationContext result = service.resolve(null);

        assertEquals("generated-id", result.correlationId());
        assertTrue(result.generated());
        verify(generator).generate();
    }

    @Test
    void shouldGenerateCorrelationIdWhenBlank() {
        UuidCorrelationIdGenerator generator = mock(UuidCorrelationIdGenerator.class);
        when(generator.generate()).thenReturn("generated-id");

        CorrelationIdService service = new CorrelationIdService(generator);

        CorrelationContext result = service.resolve("   ");

        assertEquals("generated-id", result.correlationId());
        assertTrue(result.generated());
        verify(generator).generate();
    }

    @Test
    void shouldTrimIncomingCorrelationId() {
        UuidCorrelationIdGenerator generator = mock(UuidCorrelationIdGenerator.class);
        CorrelationIdService service = new CorrelationIdService(generator);

        CorrelationContext result = service.resolve("  test-id  ");

        assertEquals("test-id", result.correlationId());
        assertFalse(result.generated());
        verifyNoInteractions(generator);
    }
}