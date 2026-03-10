package com.company.observability.starter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SensitiveDataMaskingServiceTest {

    private SensitiveDataMaskingService maskingService;

    @BeforeEach
    void setUp() {
        maskingService = new SensitiveDataMaskingService();
    }

    @Test
    void shouldReturnDashForNullQuery() {
        String result = maskingService.maskQuery(null);

        assertEquals("-", result);
    }

    @Test
    void shouldReturnDashForBlankQuery() {
        String result = maskingService.maskQuery("   ");

        assertEquals("-", result);
    }

    @Test
    void shouldMaskSingleSensitiveQueryParameter() {
        String result = maskingService.maskQuery("token=abc123");

        assertEquals("token=***", result);
    }

    @Test
    void shouldMaskMultipleSensitiveQueryParameters() {
        String result = maskingService.maskQuery(
                "userId=15&token=abc123&password=myPass123&page=2"
        );

        assertEquals("userId=15&token=***&password=***&page=2", result);
    }

    @Test
    void shouldMaskFirstQueryParameter() {
        String result = maskingService.maskQuery("api_key=abcdef&sort=desc");

        assertEquals("api_key=***&sort=desc", result);
    }

    @Test
    void shouldMaskCaseInsensitiveQueryParameters() {
        String result = maskingService.maskQuery(
                "ACCESS_TOKEN=abc&CLIENT_SECRET=xyz&PASSWORD=123"
        );

        assertEquals("ACCESS_TOKEN=***&CLIENT_SECRET=***&PASSWORD=***", result);
    }

    @Test
    void shouldNotChangeQueryWithoutSensitiveParameters() {
        String query = "page=1&size=20&sort=desc";

        String result = maskingService.maskQuery(query);

        assertEquals(query, result);
    }

    @Test
    void shouldMaskEmptySensitiveParameterValues() {
        String result = maskingService.maskQuery("token=&password=&page=1");

        assertEquals("token=***&password=***&page=1", result);
    }

    @Test
    void shouldReturnDashForNullAuthorizationHeader() {
        String result = maskingService.maskAuthorizationHeader(null);

        assertEquals("-", result);
    }

    @Test
    void shouldReturnDashForBlankAuthorizationHeader() {
        String result = maskingService.maskAuthorizationHeader("   ");

        assertEquals("-", result);
    }

    @Test
    void shouldMaskBearerAuthorizationHeader() {
        String result = maskingService.maskAuthorizationHeader("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");

        assertEquals("Bearer ***", result);
    }

    @Test
    void shouldMaskBearerAuthorizationHeaderCaseInsensitive() {
        String result = maskingService.maskAuthorizationHeader("bearer some-token-value");

        assertEquals("Bearer ***", result);
    }

    @Test
    void shouldMaskNonBearerAuthorizationHeader() {
        String result = maskingService.maskAuthorizationHeader("Basic dXNlcjpwYXNz");

        assertEquals("***", result);
    }
}