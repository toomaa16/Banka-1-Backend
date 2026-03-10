package com.company.observability.starter.service;

import com.company.observability.starter.config.ObservabilityProperties;
import com.company.observability.starter.domain.UserIdExtractor;
import org.slf4j.MDC;

/**
 * Servis za upisivanje i uklanjanje user ID vrednosti iz MDC konteksta.
 * <p>
 * Koristi {@link UserIdExtractor} za dobijanje user ID vrednosti i
 * upisuje je u MDC samo ako je ta funkcionalnost ukljucena kroz konfiguraciju.
 */
public class UserIdMdcService {
    public static final String MDC_KEY = "userId";
    private final UserIdExtractor userIdExtractor;
    private final ObservabilityProperties observabilityProperties;

    /**
     * Kreira servis za rad sa user ID vrednoscu u MDC kontekstu.
     *
     * @param userIdExtractor komponenta za izdvajanje user ID vrednosti
     * @param observabilityProperties konfiguraciona svojstva biblioteke
     */
    public UserIdMdcService(UserIdExtractor userIdExtractor, ObservabilityProperties observabilityProperties) {
        this.userIdExtractor = userIdExtractor;
        this.observabilityProperties = observabilityProperties;
    }

    /**
     * Upisuje user ID u MDC ako je funkcionalnost ukljucena i ako je user ID dostupan.
     */
    public void putUserIdIfPresent(){
        if(!observabilityProperties.isUserIdMdcEnabled()){
            return;
        }
        userIdExtractor.extractUserId().ifPresent(userId -> MDC.put(MDC_KEY,userId));
    }

    /**
     * Uklanja user ID iz MDC konteksta ako je funkcionalnost ukljucena.
     */
    public void clear(){
        if(observabilityProperties.isUserIdMdcEnabled()){
            MDC.remove(MDC_KEY);
        }
    }
}
