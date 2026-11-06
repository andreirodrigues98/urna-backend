package br.com.urnacidada.urna_backend;

import br.com.urnacidada.urna_backend.service.VotacaoService;

/** Teste apenas de validação dos candidatos: não conecta no Firebase. */
public class TesteVotacaoService {
    public static void main(String[] args) {
        VotacaoService service = new VotacaoService(null);
        service.listarCandidatos().forEach(c -> System.out.println(c.getCargo()
            + " " + c.getNumero() + " " + c.getNome()));
        System.out.println(service.prepararVoto("Presidente", "72", false).getCandidato().getNome());
        System.out.println(service.prepararVoto("Presidente", "99", false).getTipoVoto());
    }
}
