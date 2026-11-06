package br.com.urnacidada.urna_backend.repository;

import br.com.urnacidada.urna_backend.model.EtapaVotacao;
import br.com.urnacidada.urna_backend.model.SessaoVotacao;
import br.com.urnacidada.urna_backend.model.TipoVoto;
import br.com.urnacidada.urna_backend.model.Voto;
import br.com.urnacidada.urna_backend.service.ApuracaoCalculadora;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Firestore é a fonte única de verdade. Sessões e totais sobrevivem ao reinício do servidor.
 * As duas operações críticas (confirmação e fechamento) usam transações.
 */
@Repository
public class FirestoreEleicaoRepository {
    private static final String[] CHAVES = {
        "D_7101", "D_7201", "D_BRANCO", "D_NULO",
        "P_71", "P_72", "P_BRANCO", "P_NULO"
    };

    private final Firestore db;
    private final DocumentReference eleicao;
    private final String eleicaoId;

    public FirestoreEleicaoRepository(Firestore db,
            @Value("${urna.eleicao.id}") String eleicaoId) {
        if (eleicaoId == null || !eleicaoId.matches("[a-zA-Z0-9_-]{3,80}")) {
            throw new IllegalArgumentException("URNA_ELEICAO_ID inválido (use letras, números, - ou _).");
        }
        this.db = db;
        this.eleicaoId = eleicaoId;
        this.eleicao = db.collection("eleicoes").document(eleicaoId);
    }

    public Map<String, Object> estado() {
        DocumentSnapshot d = aguardar(eleicao.get());
        long quantidade = total(d, "totalConfirmados");
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("eleicaoId", eleicaoId);
        resposta.put("estado", d.exists() ? String.valueOf(d.getString("estado")) : "PREPARACAO");
        resposta.put("zeresimaEmitida", d.exists());
        resposta.put("zeresimaEm", d.exists() ? String.valueOf(d.getString("zeresimaEm")) : "");
        resposta.put("rodada", numeroRodada(d));
        resposta.put("totalConfirmados", quantidade);
        resposta.put("podeZerar", d.exists() && quantidade > 0L);
        return resposta;
    }

    /** Única abertura de eleição: os oito contadores começam em zero no banco. */
    public Map<String, Object> abrirComZeresima() {
        Map<String, Object> dados = aguardar(db.runTransaction(tx -> {
            DocumentSnapshot anterior = tx.get(eleicao).get();
            if (anterior.exists()) {
                throw new IllegalStateException("Esta eleição já foi aberta. A zerésima só pode ser emitida antes do primeiro voto. Para uma nova eleição, configure outro URNA_ELEICAO_ID.");
            }
            Map<String, Object> inicial = new HashMap<>();
            inicial.put("estado", "ABERTA");
            inicial.put("zeresimaEm", Instant.now().toString());
            inicial.put("totalConfirmados", 0L);
            inicial.put("rodada", 1L);
            for (String chave : CHAVES) inicial.put(chave, 0L);
            tx.create(eleicao, inicial);
            return inicial;
        }));
        return gerarZeresima(dados);
    }

    /** A zerésima é um retrato histórico dos zeros iniciais, não a contagem atual. */
    public Map<String, Object> zeresima() {
        DocumentSnapshot d = aguardar(eleicao.get());
        if (!d.exists()) throw new IllegalStateException("A zerésima ainda não foi emitida.");
        return gerarZeresima(d.getData());
    }

    private Map<String, Object> gerarZeresima(Map<String, Object> dados) {
        Map<String, Object> zeros = new LinkedHashMap<>();
        for (String chave : CHAVES) zeros.put(chave, 0);
        return Map.of("eleicaoId", eleicaoId,
            "rodada", dados.getOrDefault("rodada", 1L),
            "emitidaEm", String.valueOf(dados.get("zeresimaEm")),
            "totalConfirmados", 0,
            "contagens", zeros,
            "observacao", "Registro inicial: nenhum voto havia sido confirmado no momento da abertura.");
    }

    /**
     * Reinicia a votação SOMENTE quando existem votos. Os documentos das rodadas
     * anteriores não são apagados: seus votos e placares ficam arquivados,
     * sem influenciar a apuração atual.
     * A troca de rodada, estado, zerésima e contadores ocorre atomicamente.
     * Sessões antigas são recusadas por conferência da rodada na transação.
     */
    public Map<String, Object> zerarTestes() {
        Map<String, Object> reiniciada = aguardar(db.runTransaction(tx -> {
            DocumentSnapshot d = tx.get(eleicao).get();
            if (!d.exists() || total(d, "totalConfirmados") == 0) {
                throw new IllegalStateException("Não existem votos para zerar. O botão só é permitido se a eleição já recebeu votos.");
            }
            long rodadaAnterior = numeroRodada(d);
            long novaRodada = rodadaAnterior + 1L;
            // Arquiva a apuração anterior sem perder seus contadores.
            Map<String, Object> arquivo = new HashMap<>();
            arquivo.put("rodada", rodadaAnterior);
            arquivo.put("estadoAnterior", String.valueOf(d.getString("estado")));
            arquivo.put("zeresimaEm", String.valueOf(d.getString("zeresimaEm")));
            arquivo.put("totalConfirmados", total(d, "totalConfirmados"));
            arquivo.put("arquivadoEm", Instant.now().toString());
            for (String chave : CHAVES) arquivo.put(chave, total(d, chave));
            tx.create(eleicao.collection("rodadas").document(Long.toString(rodadaAnterior)), arquivo);
            Map<String, Object> atualizacao = new HashMap<>();
            atualizacao.put("rodada", novaRodada);
            atualizacao.put("estado", "ABERTA");
            atualizacao.put("zeresimaEm", Instant.now().toString());
            atualizacao.put("encerradaEm", "");
            atualizacao.put("totalConfirmados", 0L);
            for (String chave : CHAVES) atualizacao.put(chave, 0L);
            tx.update(eleicao, atualizacao);
            return atualizacao;
        }));
        return gerarZeresima(reiniciada);
    }

    public SessaoVotacao criarSessao() {
        UUID id = UUID.randomUUID();
        DocumentReference ref = eleicao.collection("sessoes").document(id.toString());
        aguardar(db.runTransaction(tx -> {
            DocumentSnapshot estado = tx.get(eleicao).get();
            exigirAberta(estado);
            tx.create(ref, Map.of("etapa", "DEPUTADO", "rodada", numeroRodada(estado),
                "criadaEm", Instant.now().toString()));
            return null;
        }));
        return SessaoVotacao.restaurar(id, EtapaVotacao.DEPUTADO);
    }

    public SessaoVotacao buscarSessao(UUID id) {
        if (id == null) throw new IllegalArgumentException("O ID da sessão não pode ser nulo.");
        DocumentSnapshot d = aguardar(eleicao.collection("sessoes").document(id.toString()).get());
        if (!d.exists()) throw new IllegalArgumentException("Sessão não encontrada nesta eleição.");
        DocumentSnapshot eleicaoAtual = aguardar(eleicao.get());
        validarRodada(d, eleicaoAtual);
        if (!"ABERTA".equals(eleicaoAtual.getString("estado"))) {
            throw new IllegalStateException("Eleição encerrada. Não é possível continuar a sessão.");
        }
        return SessaoVotacao.restaurar(id, EtapaVotacao.valueOf(d.getString("etapa")));
    }

    /**
     * Confirma um único voto e incrementa o placar na mesma transação.
     * Se houver requisição repetida para o mesmo cargo/sessão, a etapa impede duplicidade.
     */
    public record ConfirmacaoPersistida(EtapaVotacao proximaEtapa, int totalConfirmados) { }

    public ConfirmacaoPersistida confirmar(UUID id, Voto voto) {
        if (id == null || voto == null) throw new IllegalArgumentException("Sessão e voto são obrigatórios.");
        DocumentReference sessaoRef = eleicao.collection("sessoes").document(id.toString());
        // Não vincular o registro anônimo do voto ao UUID da sessão.
        DocumentReference votoRef = eleicao.collection("votos").document(UUID.randomUUID().toString());
        String chave = chave(voto);
        return aguardar(db.runTransaction(tx -> {
            // Todas as leituras ocorrem antes de qualquer escrita.
            DocumentSnapshot ed = tx.get(eleicao).get();
            DocumentSnapshot sd = tx.get(sessaoRef).get();
            exigirAberta(ed);
            if (!sd.exists()) throw new IllegalArgumentException("Sessão não encontrada.");
            validarRodada(sd, ed);
            String etapa = sd.getString("etapa");
            String cargoEsperado = switch (etapa) {
                case "DEPUTADO" -> "Deputado Federal";
                case "PRESIDENTE" -> "Presidente";
                default -> throw new IllegalStateException("Esta sessão já está finalizada.");
            };
            if (!cargoEsperado.equals(voto.getCargo())) {
                throw new IllegalStateException("A etapa atual exige voto para " + cargoEsperado + ".");
            }
            String proxima = "DEPUTADO".equals(etapa) ? "PRESIDENTE" : "FINALIZADA";
            tx.update(sessaoRef, "etapa", proxima);
            tx.update(eleicao, Map.of(
                chave, FieldValue.increment(1),
                "totalConfirmados", FieldValue.increment(1)
            ));
            Map<String, Object> registro = new HashMap<>();
            registro.put("cargo", voto.getCargo());
            registro.put("tipoVoto", voto.getTipoVoto().name());
            registro.put("numero", voto.getCandidato() == null ? "" : voto.getCandidato().getNumero());
            registro.put("rodada", numeroRodada(ed));
            tx.create(votoRef, registro);
            // O mesmo documento lido na transação fornece a contagem atualizada,
            // evitando 3 consultas extras após a gravação no Firestore.
            long novoTotal = total(ed, "totalConfirmados") + 1L;
            return new ConfirmacaoPersistida(EtapaVotacao.valueOf(proxima), Math.toIntExact(novoTotal));
        }));
    }

    private static String chave(Voto voto) {
        String prefixo = switch (voto.getCargo()) {
            case "Deputado Federal" -> "D_";
            case "Presidente" -> "P_";
            default -> throw new IllegalArgumentException("Cargo inválido.");
        };
        String sufixo = voto.getTipoVoto() == TipoVoto.VALIDO
            ? voto.getCandidato().getNumero() : voto.getTipoVoto().name();
        String chave = prefixo + sufixo;
        for (String permitida : CHAVES) if (permitida.equals(chave)) return chave;
        throw new IllegalArgumentException("Candidato não está no cadastro oficial desta simulação.");
    }

    public int totalConfirmados() {
        DocumentSnapshot d = aguardar(eleicao.get());
        return (int) total(d, "totalConfirmados");
    }

    /** Não expõe a contagem parcial antes do encerramento. */
    public Map<String, Object> encerrar() {
        aguardar(db.runTransaction(tx -> {
            DocumentSnapshot d = tx.get(eleicao).get();
            exigirAberta(d);
            long dep = total(d, "D_7101") + total(d, "D_7201") + total(d, "D_BRANCO") + total(d, "D_NULO");
            long pres = total(d, "P_71") + total(d, "P_72") + total(d, "P_BRANCO") + total(d, "P_NULO");
            if (dep == 0 || dep != pres) {
                throw new IllegalStateException("Para encerrar, todas as sessões precisam concluir os dois cargos (deputado e presidente).");
            }
            tx.update(eleicao, "estado", "ENCERRADA", "encerradaEm", Instant.now().toString());
            return null;
        }));
        return resultado();
    }

    public Map<String, Object> resultado() {
        DocumentSnapshot d = aguardar(eleicao.get());
        if (!d.exists() || !"ENCERRADA".equals(d.getString("estado"))) {
            throw new IllegalStateException("A apuração só é divulgada após o encerramento da eleição.");
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("eleicaoId", eleicaoId);
        r.put("estado", "ENCERRADA");
        r.put("rodada", numeroRodada(d));
        r.put("encerradaEm", d.getString("encerradaEm"));
        r.put("totalConfirmados", total(d, "totalConfirmados"));
        r.put("deputado", apurarCargo(d, "D_", "Homem-Aranha", "Mulher-Maravilha", "7101", "7201"));
        r.put("presidente", apurarCargo(d, "P_", "Homem de Ferro", "Superman", "71", "72"));
        return r;
    }

    /** Empates e ausência de válidos são explicitados: não há vencedor artificial. */
    private static Map<String, Object> apurarCargo(DocumentSnapshot d, String prefixo,
            String nomePm, String nomePdc, String numeroPm, String numeroPdc) {
        return ApuracaoCalculadora.apurar(
            total(d, prefixo + numeroPm), total(d, prefixo + numeroPdc),
            total(d, prefixo + "BRANCO"), total(d, prefixo + "NULO"),
            nomePm, nomePdc);
    }

    private static long numeroRodada(DocumentSnapshot d) {
        if (!d.exists()) return 0L;
        Long n = d.getLong("rodada");
        return n == null ? 1L : n;
    }

    private static void validarRodada(DocumentSnapshot sessao, DocumentSnapshot eleicaoAtual) {
        if (!eleicaoAtual.exists() || numeroRodada(sessao) != numeroRodada(eleicaoAtual)) {
            throw new IllegalStateException("Sessão de uma rodada anterior. Inicie uma nova simulação.");
        }
    }

    private static long total(DocumentSnapshot d, String chave) {
        if (!d.exists()) return 0;
        Long n = d.getLong(chave);
        return n == null ? 0L : n;
    }

    private static void exigirAberta(DocumentSnapshot d) {
        if (!d.exists() || !"ABERTA".equals(d.getString("estado"))) {
            throw new IllegalStateException("Eleição não está aberta. Emita a zerésima ou verifique se a eleição já foi encerrada.");
        }
    }

    private static <T> T aguardar(java.util.concurrent.Future<T> futuro) {
        try {
            return futuro.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RepositorioIndisponivelException("Operação com o Firebase foi interrompida.", e);
        } catch (ExecutionException e) {
            Throwable causa = e.getCause();
            for (int i = 0; i < 5 && causa != null; i++, causa = causa.getCause()) {
                if (causa instanceof IllegalStateException st) throw st;
                if (causa instanceof IllegalArgumentException ia) throw ia;
            }
            throw new RepositorioIndisponivelException("Não foi possível acessar/gravar no Firestore. Confira credenciais e conexão.", e);
        }
    }
}
