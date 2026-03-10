package com.company.observability.starter.web.filter;

import com.company.observability.starter.domain.RequestLogContext;
import com.company.observability.starter.service.RequestLoggingService;
import com.company.observability.starter.service.SensitiveDataMaskingService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpRequestLoggingFilterTest {

    @Test
    void shouldLogRequestDurationApplyMaskingAndExecuteFilterChain() throws Exception {
        RequestLoggingService requestLoggingService = mock(RequestLoggingService.class);
        SensitiveDataMaskingService maskingService = mock(SensitiveDataMaskingService.class);

        when(maskingService.maskQuery("password=123&token=abc"))
                .thenReturn("password=***&token=***");
        when(maskingService.maskAuthorizationHeader("Bearer secret"))
                .thenReturn("***");

        HttpRequestLoggingFilter filter = new HttpRequestLoggingFilter(
                requestLoggingService,
                maskingService
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/demo/ping");
        request.setQueryString("password=123&token=abc");
        request.addHeader("Authorization", "Bearer secret");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((MockHttpServletResponse) res).setStatus(201);

        filter.doFilter(request, response, chain);

        ArgumentCaptor<RequestLogContext> captor = ArgumentCaptor.forClass(RequestLogContext.class);
        verify(requestLoggingService).logRequest(captor.capture());

        RequestLogContext context = captor.getValue();

        assertEquals("GET", context.httpMethod());
        assertEquals("/demo/ping", context.uri());
        assertEquals(201, context.status());
        assertTrue(context.durationMs() >= 0);
        assertEquals("password=***&token=***", context.query());
        assertEquals("***", context.authorization());
    }
}