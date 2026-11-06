package br.com.urnacidada.urna_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import br.com.urnacidada.urna_backend.model.*;

/** Testes unitários que não exigem credenciais externas do Firebase. */
class UrnaBackendApplicationTests {
    @Test
    void sessaoAvancaNaOrdemCerta() {
        SessaoVotacao sessao = new SessaoVotacao();
        Partido partido = new Partido("Partido Marvel", "PM", "71");
        sessao.confirmarVoto(new Voto("Deputado Federal", TipoVoto.VALIDO,
            new Deputado("Homem-Aranha", "7101", partido)));
        assertEquals(EtapaVotacao.PRESIDENTE, sessao.getEtapa());
        sessao.confirmarVoto(new Voto("Presidente", TipoVoto.VALIDO,
            new Presidente("Homem de Ferro", "71", partido, new VicePresidente("Thor"))));
        assertEquals(EtapaVotacao.FINALIZADA, sessao.getEtapa());
    }
}
