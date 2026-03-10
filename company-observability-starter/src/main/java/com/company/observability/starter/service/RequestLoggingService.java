package com.company.observability.starter.service;

import com.company.observability.starter.domain.RequestLogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servis za logovanje osnovnih informacija o HTTP zahtevima.
 */

public class RequestLoggingService {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingService.class);

    /**
     * Loguje osnovne informacije o obradjenom HTTP zahtevu.
     *
     * @param context kontekst sa podacima o HTTP zahtevu i odgovoru
     */
    public void logRequest(RequestLogContext context) {
        log.info(
                "HTTP request completed method={} uri={} status={} durationMs={} query={} authorization={}",
                context.httpMethod(),
                context.uri(),
                context.status(),
                context.durationMs(),
                context.query(),
                context.authorization()
        );
    }
}
