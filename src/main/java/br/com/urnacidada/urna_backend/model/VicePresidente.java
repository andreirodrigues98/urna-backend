package br.com.urnacidada.urna_backend.model;

/** Integrante fictício da chapa presidencial. */
public class VicePresidente {
    private final String nome;

    public VicePresidente(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O vice-presidente precisa de um nome.");
        }
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
