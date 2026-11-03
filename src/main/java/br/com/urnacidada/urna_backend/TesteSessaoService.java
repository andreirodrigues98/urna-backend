package br.com.urnacidada.urna_backend;

import java.util.UUID;

import br.com.urnacidada.urna_backend.model.SessaoVotacao;
import br.com.urnacidada.urna_backend.service.VotacaoService;

public class TesteSessaoService {

    public static void main(String[] args) {

        VotacaoService service = new VotacaoService();

        // CRIAR SESSÃO
        SessaoVotacao sessao = service.criarSessao();

        UUID id = sessao.getId();

        System.out.println("Sessão criada: " + id);
        System.out.println("Etapa inicial: " + sessao.getEtapa());

        // CONFIRMAR DEPUTADO
        service.confirmarVoto(
            id,
            "Deputado Federal",
            "7101",
            false
        );

        System.out.println(
            "Após votar para deputado: " + sessao.getEtapa()
        );

        // TENTAR VOTAR PARA DEPUTADO NOVAMENTE
        try {

            service.confirmarVoto(
                id,
                "Deputado Federal",
                "7201",
                false
            );

        } catch (IllegalStateException e) {

            System.out.println(
                "Erro esperado: " + e.getMessage()
            );
        }

        // CONFIRMAR VOTO BRANCO PARA PRESIDENTE
        service.confirmarVoto(
            id,
            "Presidente",
            "",
            true
        );

        System.out.println(
            "Após votar para presidente: " + sessao.getEtapa()
        );

        System.out.println(
            "Sessão finalizada: " + sessao.isFinalizada()
        );

        // TENTAR VOTAR DEPOIS DO FIM
        try {

            service.confirmarVoto(
                id,
                "Presidente",
                "71",
                false
            );

        } catch (IllegalStateException e) {

            System.out.println(
                "Erro esperado: " + e.getMessage()
            );
        }

        // VERIFICAR O TOTAL DE VOTOS
        System.out.println(
            "Total de votos confirmados: "
            + service.contarVotosConfirmados()
        );

        // CRIAR OUTRA SESSÃO
        SessaoVotacao novaSessao = service.criarSessao();

        System.out.println(
            "Nova sessão: " + novaSessao.getId()
        );

        System.out.println(
            "Etapa da nova sessão: " + novaSessao.getEtapa()
        );
    }
}