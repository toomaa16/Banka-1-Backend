package com.company.observability.starter.web.filter;

import com.company.observability.starter.config.ObservabilityProperties;
import com.company.observability.starter.domain.UuidCorrelationIdGenerator;
import com.company.observability.starter.service.CorrelationIdService;
import com.company.observability.starter.service.UserIdMdcService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldGenerateCorrelationIdSetResponseHeaderAndClearMdc() throws Exception {
        UuidCorrelationIdGenerator generator = mock(UuidCorrelationIdGenerator.class);
        when(generator.generate()).thenReturn("generated-id");

        CorrelationIdService correlationIdService = new CorrelationIdService(generator);
        ObservabilityProperties properties = new ObservabilityProperties();
        properties.setCorrelationHeaderName("X-Correlation-Id");

        UserIdMdcService userIdMdcService = mock(UserIdMdcService.class);

        CorrelationIdFilter filter = new CorrelationIdFilter(
                correlationIdService,
                properties,
                userIdMdcService
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/demo/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            assertEquals("generated-id", MDC.get("correlationId"));
        };

        filter.doFilter(request, response, chain);

        assertEquals("generated-id", response.getHeader("X-Correlation-Id"));
        assertNull(MDC.get("correlationId"));
        verify(userIdMdcService).putUserIdIfPresent();
        verify(userIdMdcService).clear();
    }

    @Test
    void shouldPropagateExistingCorrelationId() throws Exception {
        UuidCorrelationIdGenerator generator = mock(UuidCorrelationIdGenerator.class);
        CorrelationIdService correlationIdService = new CorrelationIdService(generator);

        ObservabilityProperties properties = new ObservabilityProperties();
        properties.setCorrelationHeaderName("X-Correlation-Id");

        UserIdMdcService userIdMdcService = mock(UserIdMdcService.class);

        CorrelationIdFilter filter = new CorrelationIdFilter(
                correlationIdService,
                properties,
                userIdMdcService
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/demo/ping");
        request.addHeader("X-Correlation-Id", "existing-id");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> assertEquals("existing-id", MDC.get("correlationId"));

        filter.doFilter(request, response, chain);

        assertEquals("existing-id", response.getHeader("X-Correlation-Id"));
        verifyNoInteractions(generator);
    }
}