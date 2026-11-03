package br.com.urnacidada.urna_backend;

import br.com.urnacidada.urna_backend.model.Voto;
import br.com.urnacidada.urna_backend.service.VotacaoService;

public class TesteVotacaoService {

    public static void main(String[] args) {

        VotacaoService service = new VotacaoService();

        // Voto válido
        Voto voto1 = service.prepararVoto(
            "Deputado Federal",
            "7101",
            false
        );

        voto1.exibirDados();
        service.confirmarVoto(voto1);

        // Voto nulo
        Voto voto2 = service.prepararVoto(
            "Presidente",
            "99",
            false
        );

        voto2.exibirDados();
        service.confirmarVoto(voto2);

        // Voto branco
        Voto voto3 = service.prepararVoto(
            "Presidente",
            "",
            true
        );

        voto3.exibirDados();
        service.confirmarVoto(voto3);

        System.out.println(
            "Total de votos confirmados: "
            + service.contarVotosConfirmados()
        );
        
    }
}