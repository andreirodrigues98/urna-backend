package br.com.urnacidada.urna_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import br.com.urnacidada.urna_backend.service.ApuracaoCalculadora;

class ApuracaoCalculadoraTests {
    @Test void marvelVence() {
        assertEquals("Homem de Ferro", ApuracaoCalculadora.apurar(3, 2, 0, 0,
            "Homem de Ferro", "Superman").get("vencedor"));
    }
    @Test void dcVence() {
        assertEquals("Superman", ApuracaoCalculadora.apurar(0, 4, 2, 1,
            "Homem de Ferro", "Superman").get("vencedor"));
    }
    @Test void empate() {
        assertEquals("EMPATE", ApuracaoCalculadora.apurar(2, 2, 0, 0,
            "Homem de Ferro", "Superman").get("vencedor"));
    }
    @Test void nenhumVotoValido() {
        assertEquals("SEM VOTOS VÁLIDOS", ApuracaoCalculadora.apurar(0, 0, 3, 2,
            "Homem de Ferro", "Superman").get("vencedor"));
    }
}
