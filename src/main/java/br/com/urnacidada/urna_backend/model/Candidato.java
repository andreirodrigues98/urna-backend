package br.com.urnacidada.urna_backend.model;

public abstract class Candidato {

    private String nome;
    private String numero;
    private Partido partido;

    // CONSTRUTOR
    public Candidato(String nome, String numero, Partido partido) {
        this.nome = nome;
        this.numero = numero;
        this.partido = partido;
    }

    // GETTERS
    public String getNome() {
        return nome;
    }

    public String getNumero() {
        return numero;
    }

    public Partido getPartido() {
        return partido;
    }

    // SETTERS
    public void setNome(String novo_nome) {
        this.nome = novo_nome;
    }

    public void setNumero(String novo_numero) {
        this.numero = novo_numero;
    }

    public void setPartido(Partido novo_partido) {
        this.partido = novo_partido;
    }

    // MÉTODO ABSTRATO
    public abstract String getCargo();
}