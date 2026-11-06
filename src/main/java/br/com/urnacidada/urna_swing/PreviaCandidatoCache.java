package br.com.urnacidada.urna_swing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mostra nomes, partidos e vices sem esperar uma consulta ao Firestore.
 * O catálogo é obtido do backend; só o backend registra/confirma votos.
 */
public final class PreviaCandidatoCache {
    private final Map<String, Map<String, Object>> candidatos = new HashMap<>();
    private boolean carregado;

    public void atualizar(List<Map<String, Object>> resposta) {
        if (resposta == null) throw new IllegalArgumentException("Catálogo ausente.");
        Map<String, Map<String, Object>> novo = new HashMap<>();
        for (Map<String, Object> candidato : resposta) {
            String cargo = String.valueOf(candidato.get("cargo"));
            String numero = String.valueOf(candidato.get("numero"));
            if (candidato.get("nome") == null || candidato.get("partido") == null) {
                throw new IllegalArgumentException("Catálogo de candidatos incompleto.");
            }
            novo.put(cargo + "|" + numero, Map.copyOf(candidato));
        }
        candidatos.clear();
        candidatos.putAll(novo);
        carregado = true;
    }

    public boolean carregado() { return carregado; }

    /** Retorna null quando ainda não há cache; nesse caso, consultar o backend. */
    public Map<String, Object> preparar(String cargo, String numero, boolean branco) {
        if (!carregado) return null;
        int tamanho = switch (cargo) {
            case "Deputado Federal" -> 4;
            case "Presidente" -> 2;
            default -> throw new IllegalArgumentException("Cargo inválido.");
        };
        if (!branco && (numero == null || !numero.matches("\\d{" + tamanho + "}"))) {
            throw new IllegalArgumentException("Número deve ter " + tamanho + " dígitos.");
        }
        Map<String, Object> candidato = branco ? null : candidatos.get(cargo + "|" + numero);
        Map<String, Object> previa = new HashMap<>();
        previa.put("cargo", cargo);
        previa.put("tipoVoto", branco ? "BRANCO" : candidato == null ? "NULO" : "VALIDO");
        previa.put("candidato", candidato);
        return previa;
    }
}
