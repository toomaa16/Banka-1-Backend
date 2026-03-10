package com.company.observability.starter.service;

import com.company.observability.starter.domain.CorrelationContext;
import com.company.observability.starter.domain.UuidCorrelationIdGenerator;
import org.springframework.util.StringUtils;

/**
 * Servis zaduzen za razresavanje correlation ID vrednosti.
 * <p>
 * Ako zahtev vec sadrzi correlation ID, koristi se postojeca vrednost.
 * U suprotnom, generise se nova vrednost.
 */
public class CorrelationIdService {
    private final UuidCorrelationIdGenerator uuidGenerator;

    /**
     * Kreira servis za razresavanje correlation ID vrednosti.
     *
     * @param uuidGenerator generator koji se koristi za kreiranje novog correlation ID-a
     */
    public CorrelationIdService(UuidCorrelationIdGenerator uuidGenerator) {
        this.uuidGenerator = uuidGenerator;
    }

    /**
     * Razresava correlation ID na osnovu dolazne vrednosti iz zahteva.
     *
     * @param incomingCID correlation ID procitan iz dolaznog zahteva
     * @return rezultat razresavanja koji sadrzi finalni correlation ID i informaciju
     *         da li je ID generisan ili preuzet iz zahteva
     */
    public CorrelationContext resolve(String incomingCID){
        if(StringUtils.hasText(incomingCID)){
            return new CorrelationContext(incomingCID.trim(),false);
        }
        String generatedCID = uuidGenerator.generate();
        return new CorrelationContext(generatedCID,true);
    }


}
