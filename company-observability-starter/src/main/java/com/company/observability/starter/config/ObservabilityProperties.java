package com.company.observability.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfiguraciona svojstva za observability starter biblioteku.
 * <p>
 * Svojstva se ucitavaju iz konfiguracije aplikacije preko prefiksa
 * {@code company.observability.starter}.
 */
@ConfigurationProperties(prefix = "company.observability.starter")
public class ObservabilityProperties {
    private boolean enabled = true;
    private String correlationHeaderName = "X-Correlation-Id";
    private boolean userIdMdcEnabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCorrelationHeaderName() {
        return correlationHeaderName;
    }

    public void setCorrelationHeaderName(String correlationHeaderName) {
        this.correlationHeaderName = correlationHeaderName;
    }

    public boolean isUserIdMdcEnabled() {
        return userIdMdcEnabled;
    }

    public void setUserIdMdcEnabled(boolean userIdMdcEnabled) {
        this.userIdMdcEnabled = userIdMdcEnabled;
    }

}
