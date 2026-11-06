package br.com.urnacidada.urna_backend;

import br.com.urnacidada.urna_swing.HorarioLocal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class HorarioLocalTests {
    @Test void converteUTCParaSalvador() {
        assertEquals("05/11/2026 às 20:47:51",
            HorarioLocal.formatar("2026-11-05T23:47:51.129515400Z"));
    }

    @Test void preservaDataDesconhecida() {
        assertEquals("", HorarioLocal.formatar(""));
    }
}
