package br.com.urnacidada.urna_backend;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

import br.com.urnacidada.urna_backend.model.Candidato;
import br.com.urnacidada.urna_backend.model.Presidente;
import br.com.urnacidada.urna_backend.service.VotacaoService;

class CadastroPersonagensTests {
    @Test
    void haExatamenteDoisDeputadosEDoisPresidentes() {
        VotacaoService servico = new VotacaoService(null);
        List<Candidato> candidatos = servico.listarCandidatos();
        assertEquals(4, candidatos.size());
        assertEquals(2, candidatos.stream()
            .filter(c -> c.getCargo().equals("Deputado Federal")).count());
        assertEquals(2, candidatos.stream()
            .filter(c -> c.getCargo().equals("Presidente")).count());
    }

    @Test
    void nomesEPartidosEstaoCorretos() {
        VotacaoService servico = new VotacaoService(null);
        assertEquals("Homem-Aranha", servico.buscarCandidato("Deputado Federal", "7101").getNome());
        assertEquals("PM", servico.buscarCandidato("Deputado Federal", "7101").getPartido().getSigla());
        assertEquals("Mulher-Maravilha", servico.buscarCandidato("Deputado Federal", "7201").getNome());
        assertEquals("PDC", servico.buscarCandidato("Deputado Federal", "7201").getPartido().getSigla());
        Presidente ferro = (Presidente) servico.buscarCandidato("Presidente", "71");
        Presidente superman = (Presidente) servico.buscarCandidato("Presidente", "72");
        assertEquals("Homem de Ferro", ferro.getNome());
        assertEquals("Thor", ferro.getVice().getNome());
        assertEquals("Superman", superman.getNome());
        assertEquals("Batman", superman.getVice().getNome());
    }
}
