package br.com.urnacidada.urna_backend;

import br.com.urnacidada.urna_swing.PreviaCandidatoCache;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

/** Testa a resposta visual imediata sem chamar Firebase nem servidor. */
class PreviaCandidatoCacheTests {
    private PreviaCandidatoCache cachePronto() {
        PreviaCandidatoCache cache = new PreviaCandidatoCache();
        Map<String,Object> partido = Map.of("nome", "Partido Marvel", "sigla", "PM", "numero", "71");
        cache.atualizar(List.of(
            Map.of("nome", "Homem-Aranha", "numero", "7101", "cargo", "Deputado Federal", "partido", partido),
            Map.of("nome", "Homem de Ferro", "numero", "71", "cargo", "Presidente", "partido", partido,
                "vice", Map.of("nome", "Thor"))
        ));
        return cache;
    }

    @Test void semCatalogoNaoInventaCandidato() {
        assertNull(new PreviaCandidatoCache().preparar("Presidente", "71", false));
    }

    @Test void deputadoApareceInstantaneamente() {
        var voto = cachePronto().preparar("Deputado Federal", "7101", false);
        assertEquals("VALIDO", voto.get("tipoVoto"));
        assertEquals("Homem-Aranha", ((Map<?,?>) voto.get("candidato")).get("nome"));
    }

    @Test void presidenteMostraVice() {
        var voto = cachePronto().preparar("Presidente", "71", false);
        Map<?,?> candidato = (Map<?,?>) voto.get("candidato");
        assertEquals("Thor", ((Map<?,?>) candidato.get("vice")).get("nome"));
    }

    @Test void brancoENuloNaoRecebemCandidato() {
        assertEquals("BRANCO", cachePronto().preparar("Presidente", "", true).get("tipoVoto"));
        var nulo = cachePronto().preparar("Presidente", "99", false);
        assertEquals("NULO", nulo.get("tipoVoto"));
        assertNull(nulo.get("candidato"));
    }

    @Test void numeroIncorretoERejeitado() {
        assertThrows(IllegalArgumentException.class,
            () -> cachePronto().preparar("Deputado Federal", "71", false));
    }
}
