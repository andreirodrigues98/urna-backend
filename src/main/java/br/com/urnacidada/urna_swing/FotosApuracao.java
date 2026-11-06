package br.com.urnacidada.urna_swing;

import java.util.List;
import java.util.Map;

/**
 * Seleciona exclusivamente os retratos dos vencedores da apuração.
 * Empates e resultados sem votos válidos não exibem personagens.
 * O placar é calculado no backend; aqui só escolhemos as imagens da interface.
 */
final class FotosApuracao {
    private FotosApuracao() {}

    record Retrato(String identificador, String nome, String cargo) {}

    static List<Retrato> deputados(Map<String, Object> apuracao) {
        return switch (partidoVencedor(apuracao)) {
            case "PM" -> List.of(new Retrato("7101", "Homem-Aranha", "DEPUTADO"));
            case "PDC" -> List.of(new Retrato("7201", "Mulher-Maravilha", "DEPUTADA"));
            default -> List.of();
        };
    }

    static List<Retrato> presidencia(Map<String, Object> apuracao) {
        return switch (partidoVencedor(apuracao)) {
            case "PM" -> List.of(
                new Retrato("71", "Homem de Ferro", "PRESIDENTE"),
                new Retrato("71-VICE", "Thor", "VICE-PRESIDENTE")
            );
            case "PDC" -> List.of(
                new Retrato("72", "Superman", "PRESIDENTE"),
                new Retrato("72-VICE", "Batman", "VICE-PRESIDENTE")
            );
            default -> List.of();
        };
    }

    private static String partidoVencedor(Map<String, Object> apuracao) {
        if (apuracao == null) return "";
        Object valorPm = apuracao.get("pm");
        Object valorPdc = apuracao.get("pdc");
        if (!(valorPm instanceof Number pm) || !(valorPdc instanceof Number pdc)) return "";

        long votosPm = pm.longValue();
        long votosPdc = pdc.longValue();
        // Nada de foto em empate, sem votos válidos, ou relatório inconsistente.
        String vencedor = String.valueOf(apuracao.getOrDefault("vencedor", ""));
        if (votosPm < 0 || votosPdc < 0 || votosPm == votosPdc
                || "EMPATE".equals(vencedor)
                || "SEM VOTOS VÁLIDOS".equals(vencedor)) return "";
        return votosPm > votosPdc ? "PM" : "PDC";
    }
}
