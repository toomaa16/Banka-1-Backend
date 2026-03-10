package com.company.observability.starter.domain;

import java.util.UUID;

// Generator correlation Id vrednosti u uuid formatu
public class UuidCorrelationIdGenerator {
    public String generate(){
        return UUID.randomUUID().toString();
    }
}
