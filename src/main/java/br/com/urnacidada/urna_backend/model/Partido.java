package br.com.urnacidada.urna_backend.model;

public class Partido {

    private String nome;
    private String sigla;
    private String numero;

    public Partido(String nome, String sigla, String numero) {
        this.nome = nome;
        this.sigla = sigla;
        this.numero = numero;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla(){
        return sigla;
    }

    public String getNumero(){
        return numero;
    }

    public void setNome(String novo_nome){
        this.nome = novo_nome;
    }

    public void setSigla(String nova_sigla){
        this.sigla = nova_sigla;
    }

    public void setNumero(String novo_numero){
        this.numero = novo_numero;
    }
}