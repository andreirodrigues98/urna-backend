package br.com.urnacidada.urna_backend.service;

import br.com.urnacidada.urna_backend.model.*;
import br.com.urnacidada.urna_backend.repository.FirestoreEleicaoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Regras de voto separadas do banco. Nenhuma confirmação fica apenas em memória. */
@Service
public class VotacaoService {
    private final List<Candidato> candidatos = new ArrayList<>();
    private final FirestoreEleicaoRepository repositorio;

    public VotacaoService(FirestoreEleicaoRepository repositorio) {
        this.repositorio = repositorio;
        Partido marvel = new Partido("Partido Marvel", "PM", "71");
        Partido dc = new Partido("Partido DC", "PDC", "72");
        cadastrarCandidato(new Deputado("Homem-Aranha", "7101", marvel));
        cadastrarCandidato(new Presidente("Homem de Ferro", "71", marvel, new VicePresidente("Thor")));
        cadastrarCandidato(new Deputado("Mulher-Maravilha", "7201", dc));
        cadastrarCandidato(new Presidente("Superman", "72", dc, new VicePresidente("Batman")));
    }

    public void cadastrarCandidato(Candidato c) {
        if (c == null) throw new IllegalArgumentException("Candidato obrigatório.");
        if (buscarCandidato(c.getCargo(), c.getNumero()) != null) {
            throw new IllegalArgumentException("Candidato/numero duplicado.");
        }
        candidatos.add(c);
    }
    public Candidato buscarCandidato(String cargo, String numero) {
        for (Candidato c : candidatos) {
            if (c.getCargo().equals(cargo) && c.getNumero().equals(numero)) return c;
        }
        return null;
    }
    public List<Candidato> listarCandidatos() { return List.copyOf(candidatos); }
    public SessaoVotacao criarSessao() { return repositorio.criarSessao(); }
    public SessaoVotacao buscarSessao(UUID id) { return repositorio.buscarSessao(id); }

    public Voto prepararVoto(String cargo, String numero, boolean branco) {
        int n;
        if ("Deputado Federal".equals(cargo)) n = 4;
        else if ("Presidente".equals(cargo)) n = 2;
        else throw new IllegalArgumentException("Cargo inválido.");
        if (branco) return new Voto(cargo, TipoVoto.BRANCO, null);
        if (numero == null || !numero.matches("\\d{" + n + "}")) {
            throw new IllegalArgumentException("Número deve ter " + n + " dígitos.");
        }
        Candidato c = buscarCandidato(cargo, numero);
        return c == null ? new Voto(cargo, TipoVoto.NULO, null)
            : new Voto(cargo, TipoVoto.VALIDO, c);
    }

    public Voto prepararVotoNaSessao(UUID id, String cargo, String numero, boolean branco) {
        SessaoVotacao sessao = buscarSessao(id);
        String esperado = switch (sessao.getEtapa()) {
            case DEPUTADO -> "Deputado Federal";
            case PRESIDENTE -> "Presidente";
            case FINALIZADA -> throw new IllegalStateException("Esta sessão já terminou.");
        };
        if (!esperado.equals(cargo)) throw new IllegalStateException("Etapa atual exige: " + esperado);
        return prepararVoto(cargo, numero, branco);
    }

    public record ConfirmacaoRealizada(Voto voto, EtapaVotacao proximaEtapa, int totalConfirmados) { }

    public ConfirmacaoRealizada confirmarVoto(UUID id, String cargo, String numero, boolean branco) {
        // Validação local rápida. A transação no Firestore é a fonte de verdade:
        // ela valida a sessão, a rodada, o estado da eleição e o cargo esperado.
        Voto voto = prepararVoto(cargo, numero, branco);
        var persistida = repositorio.confirmar(id, voto);
        return new ConfirmacaoRealizada(voto, persistida.proximaEtapa(), persistida.totalConfirmados());
    }

    @Deprecated
    public void confirmarVoto(Voto voto) {
        throw new IllegalStateException("Confirmação sem sessão desativada.");
    }
    public int contarVotosConfirmados() { return repositorio.totalConfirmados(); }
}
