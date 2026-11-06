package br.com.urnacidada.urna_swing;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.List;

import tools.jackson.databind.json.JsonMapper;

/** Cliente HTTP da urna Swing. A lógica e os votos ficam no Spring Boot. */
public class UrnaApiClient {

    // Configure -Dvotoverso.api.url=http://servidor:8080 para testes em outra máquina.
    private final String baseUrl = System.getProperty("votoverso.api.url", "http://localhost:8080");

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(4))
        .build();

    // Spring Boot 4 utiliza Jackson 3 (pacote tools.jackson).
    private final JsonMapper json = JsonMapper.builder().build();

    /** Catálogo público, carregado uma vez ao abrir a interface (sem Firestore). */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarCandidatos() throws IOException, InterruptedException {
        HttpRequest pedido = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/votacao/candidatos"))
            .timeout(Duration.ofSeconds(8))
            .header("Accept", "application/json")
            .GET().build();
        HttpResponse<String> resposta = http.send(pedido, HttpResponse.BodyHandlers.ofString());
        if (resposta.statusCode() != 200) {
            throw new IOException("Não foi possível obter os candidatos (HTTP "
                + resposta.statusCode() + ").");
        }
        return json.readValue(resposta.body(), List.class);
    }

    public Map<String, Object> estadoEleicao() throws IOException, InterruptedException {
        return enviar("GET", "/api/eleicao/estado", null, null);
    }

    public Map<String, Object> emitirZeresima(String chave) throws IOException, InterruptedException {
        return enviar("POST", "/api/eleicao/abrir", null, chave);
    }

    public Map<String, Object> zerarTestes(String chave) throws IOException, InterruptedException {
        return enviar("POST", "/api/eleicao/zerar-testes", null, chave);
    }

    public Map<String, Object> verZeresima() throws IOException, InterruptedException {
        return enviar("GET", "/api/eleicao/zeresima", null, null);
    }

    public Map<String, Object> encerrarEleicao(String chave) throws IOException, InterruptedException {
        return enviar("POST", "/api/eleicao/encerrar", null, chave);
    }

    public Map<String, Object> resultadoEleicao() throws IOException, InterruptedException {
        return enviar("GET", "/api/eleicao/resultado", null, null);
    }

    public Map<String, Object> criarSessao() throws IOException, InterruptedException {
        return enviar("POST", "/sessoes", null);
    }

    public Map<String, Object> preparar(
        String id, String cargo, String numero, boolean branco
    ) throws IOException, InterruptedException {
        return enviar("POST", "/sessoes/" + id + "/preparar", dados(cargo, numero, branco));
    }

    public Map<String, Object> confirmar(
        String id, String cargo, String numero, boolean branco
    ) throws IOException, InterruptedException {
        return enviar("POST", "/sessoes/" + id + "/confirmar", dados(cargo, numero, branco));
    }

    private Map<String, Object> dados(String cargo, String numero, boolean branco) {
        return Map.of("cargo", cargo, "numero", numero, "branco", branco);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> enviar(
        String metodo, String caminho, Map<String, Object> corpo
    ) throws IOException, InterruptedException {
        return enviar(metodo, "/api/votacao" + caminho, corpo, null);
    }

    private Map<String, Object> enviar(
        String metodo, String caminho, Map<String, Object> corpo, String chaveAdmin
    ) throws IOException, InterruptedException {

        HttpRequest.Builder pedido = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + caminho))
            .timeout(Duration.ofSeconds(8))
            .header("Accept", "application/json");
        if (chaveAdmin != null) pedido.header("X-Admin-Key", chaveAdmin);

        if ("POST".equals(metodo)) {
            pedido.header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(
                    corpo == null ? "" : json.writeValueAsString(corpo)
                ));
        } else {
            pedido.GET();
        }

        HttpResponse<String> resposta = http.send(
            pedido.build(), HttpResponse.BodyHandlers.ofString()
        );

        Map<String, Object> dados = json.readValue(resposta.body(), Map.class);

        if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
            throw new IOException(
                "HTTP " + resposta.statusCode() + ": "
                + dados.getOrDefault("erro", "Falha na requisição")
            );
        }
        return dados;
    }
}
