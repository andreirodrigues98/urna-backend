package br.com.urnacidada.urna_backend.model;

public class Presidente extends Candidato {

    public Presidente(String nome, String numero, Partido partido) {
        super(nome, numero, partido);

        if (numero == null || !numero.matches("\\d{2}")) {
            throw new IllegalArgumentException(
                "O número do presidente deve possuir exatamente 2 dígitos."
            );
        }
    }

    @Override
    public String getCargo() {
        return "Presidente";
    }

    @Override
    public void setNumero(String novoNumero) {

        if (novoNumero == null || !novoNumero.matches("\\d{2}")) {
            throw new IllegalArgumentException(
                "O número do presidente deve possuir exatamente 2 dígitos."
            );
        }

        super.setNumero(novoNumero);
    }
}