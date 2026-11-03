package br.com.urnacidada.urna_backend.model;

public class Deputado extends Candidato {

    public Deputado(String nome, String numero, Partido partido) {
        super(nome, numero, partido);

        if (numero == null || !numero.matches("\\d{4}")) {
            throw new IllegalArgumentException(
                "O número do deputado deve possuir exatamente 4 dígitos."
            );
        }
    }

    @Override
    public String getCargo() {
        return "Deputado Federal";
    }

    @Override
    public void setNumero(String novoNumero) {

        if (novoNumero == null || !novoNumero.matches("\\d{4}")) {
            throw new IllegalArgumentException(
                "O número do deputado deve possuir exatamente 4 dígitos."
            );
        }

        super.setNumero(novoNumero);
    }
}