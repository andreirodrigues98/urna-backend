package br.com.urnacidada.urna_backend;

import br.com.urnacidada.urna_backend.service.ApuracaoCalculadora;
import java.util.Map;

/** Execute com javac/java ou use como referência para testes automatizados. */
public class VerificarApuracao {
    public static void main(String[] args) {
        testar(3, 1, 0, 0, "Homem de Ferro");
        testar(2, 5, 0, 0, "Superman");
        testar(2, 2, 1, 1, "EMPATE");
        testar(0, 0, 4, 2, "SEM VOTOS VÁLIDOS");
        System.out.println("PASSOU: PM, PDC, empate e nenhum voto válido.");
    }
    private static void testar(long pm, long pdc, long brancos, long nulos, String esperado) {
        Map<String, Object> resultado = ApuracaoCalculadora.apurar(
            pm, pdc, brancos, nulos, "Homem de Ferro", "Superman");
        if (!esperado.equals(resultado.get("vencedor"))) {
            throw new AssertionError("Esperado " + esperado + " mas recebido " + resultado.get("vencedor"));
        }
        if (!Long.valueOf(pm + pdc).equals(resultado.get("votosValidos"))) {
            throw new AssertionError("Votos válidos incorretos.");
        }
    }
}
