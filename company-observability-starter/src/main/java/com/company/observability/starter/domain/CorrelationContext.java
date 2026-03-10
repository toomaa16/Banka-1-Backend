package com.company.observability.starter.domain;


/**
 * Predstavlja rezultat razresavanja correlation ID vrednosti.
 *
 * @param correlationId finalna vrednost correlation ID-a
 * @param generated {@code true} ako je correlation ID generisan,
 *                  {@code false} ako je preuzet iz dolaznog zahteva
 */

public record CorrelationContext(String correlationId, boolean generated) {
    public CorrelationContext{
        if(correlationId == null || correlationId.isBlank()){
            throw new IllegalArgumentException("Correlation ID ne sme biti null ili prazan");
        }
    }
}
