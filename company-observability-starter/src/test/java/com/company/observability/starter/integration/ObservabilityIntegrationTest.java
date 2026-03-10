package com.company.observability.starter.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "company.observability.starter.enabled=true",
                "company.observability.starter.correlation-header-name=X-Correlation-Id",
                "company.observability.starter.user-id-mdc-enabled=false"
        }
)
@AutoConfigureMockMvc
class ObservabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateCorrelationIdAndReturnItInResponseHeader() throws Exception {
        mockMvc.perform(get("/demo/ok"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void shouldPropagateExistingCorrelationId() throws Exception {
        mockMvc.perform(get("/demo/ok")
                        .header("X-Correlation-Id", "my-correlation-id"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "my-correlation-id"));
    }

    @Test
    void shouldReturnStandardized500ErrorResponse() throws Exception {
        mockMvc.perform(get("/demo/boom")
                        .header("X-Correlation-Id", "corr-500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Unexpected server error"))
                .andExpect(jsonPath("$.correlationId").value("corr-500"))
                .andExpect(jsonPath("$.path").value("/demo/boom"));
    }
}