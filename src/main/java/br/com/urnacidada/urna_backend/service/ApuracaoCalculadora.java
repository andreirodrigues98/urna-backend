package br.com.urnacidada.urna_backend.service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Somente a lógica de apuração; sem acesso ao banco e sem preferência por partido. */
public final class ApuracaoCalculadora {
    private ApuracaoCalculadora() {}

    public static Map<String, Object> apurar(long pm, long pdc, long brancos, long nulos,
            String nomePm, String nomePdc) {
        if (pm < 0 || pdc < 0 || brancos < 0 || nulos < 0) {
            throw new IllegalArgumentException("Contagens não podem ser negativas.");
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("pm", pm);
        resultado.put("pdc", pdc);
        resultado.put("brancos", brancos);
        resultado.put("nulos", nulos);
        resultado.put("votosValidos", pm + pdc);
        resultado.put("nomePM", nomePm);
        resultado.put("nomePDC", nomePdc);
        resultado.put("vencedor", pm + pdc == 0 ? "SEM VOTOS VÁLIDOS"
            : pm == pdc ? "EMPATE" : pm > pdc ? nomePm : nomePdc);
        return resultado;
    }
}
