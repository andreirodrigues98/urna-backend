package br.com.urnacidada.urna_backend.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.urnacidada.urna_backend.model.Candidato;
import br.com.urnacidada.urna_backend.model.EtapaVotacao;
import br.com.urnacidada.urna_backend.model.SessaoVotacao;
import br.com.urnacidada.urna_backend.model.TipoVoto;
import br.com.urnacidada.urna_backend.model.Voto;
import br.com.urnacidada.urna_backend.service.VotacaoService;
import br.com.urnacidada.urna_backend.repository.RepositorioIndisponivelException;

@RestController
@RequestMapping("/api/votacao")
public class VotacaoController {

    private final VotacaoService service;

    // INJEÇÃO DE DEPENDÊNCIA
    public VotacaoController(VotacaoService service) {
        this.service = service;
    }

    // LISTAR CANDIDATOS
    @GetMapping("/candidatos")
    public List<Candidato> listarCandidatos() {
        return service.listarCandidatos();
    }

    // BUSCAR CANDIDATO
    @GetMapping("/candidatos/buscar")
    public ResponseEntity<Candidato> buscarCandidato(
        @RequestParam("cargo") String cargo,
        @RequestParam("numero") String numero
    ) {

        Candidato candidato = service.buscarCandidato(
            cargo,
            numero
        );

        if (candidato == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(candidato);
    }

    // CRIAR SESSÃO
    @PostMapping("/sessoes")
    public ResponseEntity<ResumoSessao> criarSessao() {

        SessaoVotacao sessao = service.criarSessao();

        ResumoSessao resposta = criarResumo(sessao);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(resposta);
    }

    // CONSULTAR SESSÃO
    @GetMapping("/sessoes/{id}")
    public ResumoSessao consultarSessao(
        @PathVariable("id") UUID id
    ) {

        SessaoVotacao sessao = service.buscarSessao(id);

        return criarResumo(sessao);
    }

    // PREPARAR VOTO SEM CONFIRMAR
    @PostMapping("/sessoes/{id}/preparar")
    public Voto prepararVoto(
        @PathVariable("id") UUID id,
        @RequestBody DadosVoto dados
    ) {

        validarDados(dados);

        return service.prepararVotoNaSessao(
            id,
            dados.cargo(),
            dados.numero(),
            dados.branco()
        );
    }

    // CONFIRMAR VOTO EM UMA SESSÃO
    @PostMapping("/sessoes/{id}/confirmar")
    public ResultadoConfirmacao confirmarVoto(
        @PathVariable("id") UUID id,
        @RequestBody DadosVoto dados
    ) {

        validarDados(dados);

        // O Firestore já devolve o estado pós-confirmação na mesma transação.
        var confirmacao = service.confirmarVoto(id, dados.cargo(), dados.numero(), dados.branco());
        return new ResultadoConfirmacao(
            id,
            confirmacao.voto().getCargo(),
            confirmacao.voto().getTipoVoto(),
            confirmacao.proximaEtapa(),
            confirmacao.proximaEtapa() == EtapaVotacao.FINALIZADA,
            confirmacao.totalConfirmados()
        );
    }

    // CONTAR VOTOS
    @GetMapping("/total")
    public Map<String, Integer> contarVotos() {

        return Map.of(
            "totalConfirmados",
            service.contarVotosConfirmados()
        );
    }

    // CRIAR RESPOSTA RESUMIDA DA SESSÃO
    private ResumoSessao criarResumo(
        SessaoVotacao sessao
    ) {

        return new ResumoSessao(
            sessao.getId(),
            sessao.getEtapa(),
            sessao.isFinalizada()
        );
    }

    // VALIDAR DADOS RECEBIDOS
    private void validarDados(DadosVoto dados) {

        if (dados == null || dados.cargo() == null) {

            throw new IllegalArgumentException(
                "O cargo deve ser informado."
            );
        }
    }

    // TRATAR ERROS DE DADOS INVÁLIDOS
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarErro(
        IllegalArgumentException erro
    ) {

        return ResponseEntity.badRequest().body(
            Map.of("erro", erro.getMessage())
        );
    }

    // TRATAR ERROS DE ESTADO DA VOTAÇÃO
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> tratarEstado(
        IllegalStateException erro
    ) {

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("erro", erro.getMessage()));
    }

    @ExceptionHandler(RepositorioIndisponivelException.class)
    public ResponseEntity<Map<String, String>> tratarFirestore(RepositorioIndisponivelException erro) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("erro", erro.getMessage()));
    }

    // DADOS ENVIADOS PELA URNA
    public record DadosVoto(
        String cargo,
        String numero,
        boolean branco
    ) {}

    // RESPOSTA DA SESSÃO
    public record ResumoSessao(
        UUID id,
        EtapaVotacao etapa,
        boolean finalizada
    ) {}

    // RESULTADO DA CONFIRMAÇÃO
    public record ResultadoConfirmacao(
        UUID sessaoId,
        String cargo,
        TipoVoto tipoVoto,
        EtapaVotacao proximaEtapa,
        boolean finalizada,
        int totalConfirmados
    ) {}
}