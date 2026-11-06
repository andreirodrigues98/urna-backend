package br.com.urnacidada.urna_swing;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Map;

class FotosApuracaoTests {
    @Test void marvelVenceNosDoisCargos() {
        var placar = Map.<String, Object>of("pm", 2L, "pdc", 1L, "vencedor", "Homem de Ferro");
        assertEquals("7101", FotosApuracao.deputados(placar).get(0).identificador());
        assertEquals("71", FotosApuracao.presidencia(placar).get(0).identificador());
        assertEquals("71-VICE", FotosApuracao.presidencia(placar).get(1).identificador());
    }
    @Test void dcVenceNosDoisCargos() {
        var placar = Map.<String, Object>of("pm", 1, "pdc", 4, "vencedor", "Superman");
        assertEquals("7201", FotosApuracao.deputados(placar).get(0).identificador());
        assertEquals("72", FotosApuracao.presidencia(placar).get(0).identificador());
        assertEquals("72-VICE", FotosApuracao.presidencia(placar).get(1).identificador());
    }
    @Test void empateNaoMostraFotos() {
        var placar = Map.<String, Object>of("pm", 1, "pdc", 1, "vencedor", "EMPATE");
        assertTrue(FotosApuracao.deputados(placar).isEmpty());
        assertTrue(FotosApuracao.presidencia(placar).isEmpty());
    }
    @Test void semVotosValidosNaoMostraFotos() {
        var placar = Map.<String, Object>of("pm", 0, "pdc", 0, "vencedor", "SEM VOTOS VÁLIDOS");
        assertTrue(FotosApuracao.deputados(placar).isEmpty());
        assertTrue(FotosApuracao.presidencia(placar).isEmpty());
    }
    @Test void empateSomenteParaDeputado() {
        var empate = Map.<String, Object>of("pm", 5, "pdc", 5, "vencedor", "EMPATE");
        var vencedor = Map.<String, Object>of("pm", 3, "pdc", 2, "vencedor", "Homem de Ferro");
        assertTrue(FotosApuracao.deputados(empate).isEmpty());
        assertEquals(2, FotosApuracao.presidencia(vencedor).size());
    }
}
