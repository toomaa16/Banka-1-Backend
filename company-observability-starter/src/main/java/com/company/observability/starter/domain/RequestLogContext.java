package com.company.observability.starter.domain;

/**
 * Predstavlja podatke potrebne za logovanje jednog HTTP zahteva.
 *
 * @param httpMethod HTTP metoda zahteva
 * @param uri URI putanja zahteva
 * @param status HTTP status kod odgovora
 * @param durationMs trajanje obrade zahteva u milisekundama
 * @param query maskirani query string zahteva
 * @param authorization maskirana vrednost Authorization zaglavlja
 */
public record RequestLogContext(String httpMethod, String uri, int status, long durationMs, String query, String authorization) {
    public RequestLogContext{
        if(httpMethod == null || httpMethod.isBlank()){
            throw new IllegalArgumentException("HTTP metoda ne sme biti null ili prazna");
        }
        if(uri == null || uri.isBlank()){
            throw new IllegalArgumentException("URI ne sme biti null ili prazan");
        }
        if(durationMs < 0){
            throw new IllegalArgumentException("Trajanje zahteva ne sme biti negativno");
        }
    }
}
