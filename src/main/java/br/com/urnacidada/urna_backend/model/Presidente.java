package br.com.urnacidada.urna_backend.model;

/** Presidente fictício associado a seu vice-presidente. */
public class Presidente extends Candidato {

    private final VicePresidente vice;

    // Mantém compatibilidade com os testes antigos de três argumentos.
    public Presidente(String nome, String numero, Partido partido) {
        this(nome, numero, partido, null);
    }

    // Sobrecarga: agora a chapa também pode receber o vice.
    public Presidente(String nome, String numero, Partido partido, VicePresidente vice) {
        super(nome, numero, partido);
        validarNumero(numero);
        this.vice = vice;
    }

    private static void validarNumero(String numero) {
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
        validarNumero(novoNumero);
        super.setNumero(novoNumero);
    }

    public VicePresidente getVice() {
        return vice;
    }
}
