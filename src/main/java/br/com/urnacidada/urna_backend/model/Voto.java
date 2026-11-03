package br.com.urnacidada.urna_backend.model;

public class Voto {

    private String cargo;
    private TipoVoto tipoVoto;
    private Candidato candidato;

    // CONSTRUTOR
    public Voto(String cargo, TipoVoto tipoVoto, Candidato candidato) {
        this.cargo = cargo;
        this.tipoVoto = tipoVoto;
        this.candidato = candidato;

        if (tipoVoto == null) {
            throw new IllegalArgumentException(
                "O tipo de voto não pode ser nulo."
            );
        }

        if (tipoVoto == TipoVoto.VALIDO && candidato == null) {
            throw new IllegalArgumentException(
                "Um voto válido precisa de um candidato."
            );
        }

        if (tipoVoto != TipoVoto.VALIDO && candidato != null) {
            throw new IllegalArgumentException(
                "Votos brancos ou nulos não devem possuir candidato."
            );
        }

        if (candidato != null && !candidato.getCargo().equals(cargo)) {
            throw new IllegalArgumentException(
                "O cargo do voto não corresponde ao cargo do candidato."
            );
        }
    }

    // GETTERS
    public String getCargo() {
        return cargo;
    }

    public TipoVoto getTipoVoto() {
        return tipoVoto;
    }

    public Candidato getCandidato() {
        return candidato;
    }

    // EXIBIR INFORMAÇÕES
    public void exibirDados() {

        System.out.println("Cargo: " + cargo);
        System.out.println("Tipo de voto: " + tipoVoto);

        if (tipoVoto == TipoVoto.VALIDO) {
            System.out.println("Candidato: " + candidato.getNome());
            System.out.println("Número: " + candidato.getNumero());
        } else {
            System.out.println("Candidato: Nenhum");
        }
    }
}