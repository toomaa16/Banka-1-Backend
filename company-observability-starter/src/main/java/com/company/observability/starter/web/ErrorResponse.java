package com.company.observability.starter.web;

import java.time.OffsetDateTime;

/**
 * Genericki odgovor koji klijent dobija kada se desi neocekivana greska.
 *
 * @param timestamp vreme nastanka greske
 * @param status HTTP status kod odgovora
 * @param error naziv HTTP greske
 * @param message korisnicka poruka o gresci
 * @param correlationId correlation ID povezan sa zahtevom
 * @param path putanja zahteva na kojoj je greska nastala
 */
public record ErrorResponse(OffsetDateTime timestamp, int status, String error, String message, String correlationId, String path) {

}
