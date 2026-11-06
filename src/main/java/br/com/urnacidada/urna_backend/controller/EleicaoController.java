package br.com.urnacidada.urna_backend.controller;

import br.com.urnacidada.urna_backend.repository.FirestoreEleicaoRepository;
import br.com.urnacidada.urna_backend.repository.RepositorioIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/** Abertura/encerramento são tarefas da equipe, não do eleitor. */
@RestController
@RequestMapping("/api/eleicao")
public class EleicaoController {
    private final FirestoreEleicaoRepository repo;
    private final String chaveAdmin;

    public EleicaoController(FirestoreEleicaoRepository repo,
            @Value("${urna.admin.key:}") String chaveAdmin) {
        this.repo = repo;
        this.chaveAdmin = chaveAdmin;
    }

    @GetMapping("/estado")
    public Map<String, Object> estado() { return repo.estado(); }

    @PostMapping("/abrir")
    public Map<String, Object> abrir(@RequestHeader(value="X-Admin-Key", required=false) String chave) {
        autenticar(chave);
        return repo.abrirComZeresima();
    }

    /** Acesso administrativo: não apaga documentos, inicia uma nova rodada zerada. */
    @PostMapping("/zerar-testes")
    public Map<String, Object> zerarTestes(@RequestHeader(value="X-Admin-Key", required=false) String chave) {
        autenticar(chave);
        return repo.zerarTestes();
    }

    @GetMapping("/zeresima")
    public Map<String, Object> zeresima() { return repo.zeresima(); }

    @PostMapping("/encerrar")
    public Map<String, Object> encerrar(@RequestHeader(value="X-Admin-Key", required=false) String chave) {
        autenticar(chave);
        return repo.encerrar();
    }

    @GetMapping("/resultado")
    public Map<String, Object> resultado() { return repo.resultado(); }

    private void autenticar(String recebida) {
        if (chaveAdmin == null || chaveAdmin.isBlank()) {
            throw new RepositorioIndisponivelException("Configure URNA_ADMIN_KEY no servidor para ações administrativas.", null);
        }
        if (recebida == null || !MessageDigest.isEqual(
                recebida.getBytes(StandardCharsets.UTF_8),
                chaveAdmin.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Chave de administrador incorreta.");
        }
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> acessoNegado(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflito(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(RepositorioIndisponivelException.class)
    public ResponseEntity<Map<String, String>> firebaseFora(RepositorioIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("erro", ex.getMessage()));
    }
}
