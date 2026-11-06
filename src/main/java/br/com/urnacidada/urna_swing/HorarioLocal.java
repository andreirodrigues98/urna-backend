package br.com.urnacidada.urna_swing;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Apresentação no fuso da Bahia; no Firestore os instantes continuam em UTC. */
public final class HorarioLocal {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter
        .ofPattern("dd/MM/yyyy 'às' HH:mm:ss")
        .withZone(ZoneId.of("America/Bahia"));

    private HorarioLocal() { }

    public static String formatar(Object instanteUtc) {
        String texto = String.valueOf(instanteUtc);
        try {
            return FORMATO.format(Instant.parse(texto));
        } catch (RuntimeException e) {
            return texto; // Preserva o valor recebido em caso de dado legado inesperado.
        }
    }
}
