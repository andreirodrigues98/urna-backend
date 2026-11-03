package br.com.urnacidada.urna_backend;

import br.com.urnacidada.urna_backend.model.*;

public class TesteCandidatos {

    public static void main(String[] args) {

        Partido partido1 = new Partido(
            "Partido dos Inventores",
            "PIN",
            "71"
        );

        Candidato deputado1 = new Deputado(
            "Nico Circuito",
            "7101",
            partido1
        );

        Candidato presidente1 = new Presidente(
            "Lia Parafuso",
            "71",
            partido1
        );

        Candidato[] candidatos = {
            deputado1,
            presidente1
        };

        for (Candidato candidato : candidatos) {

            System.out.println("Nome: " + candidato.getNome());
            System.out.println("Número: " + candidato.getNumero());
            System.out.println("Cargo: " + candidato.getCargo());
            System.out.println("Partido: " + candidato.getPartido().getSigla());

            System.out.println();
        }


        try {

    Deputado deputadoInvalido = new Deputado(
                "Nico Circuito",
                "71",
                partido1
            );

        } catch (IllegalArgumentException e) {

            System.out.println("Erro: " + e.getMessage());
        }


        try {

            deputado1.setNumero("123");

        } catch (IllegalArgumentException e) {

            System.out.println("Erro: " + e.getMessage());
        }


        // VOTO VÁLIDO
        Voto voto1 = new Voto(
            "Deputado Federal",
            TipoVoto.VALIDO,
            deputado1
        );

        voto1.exibirDados();

        // VOTO BRANCO
        Voto voto2 = new Voto(
            "Presidente",
            TipoVoto.BRANCO,
            null
        );

        voto2.exibirDados();

        // VOTO NULO
        Voto voto3 = new Voto(
            "Presidente",
            TipoVoto.NULO,
            null
        );

        voto3.exibirDados();


        SessaoVotacao sessao = new SessaoVotacao();

        sessao.exibirEstado();

        // Voto para deputado
        Voto votoDeputado = new Voto(
            "Deputado Federal",
            TipoVoto.VALIDO,
            deputado1
        );

        sessao.confirmarVoto(votoDeputado);
        sessao.exibirEstado();

        // Voto branco para presidente
        Voto votoPresidente = new Voto(
            "Presidente",
            TipoVoto.BRANCO,
            null
        );

        sessao.confirmarVoto(votoPresidente);
        sessao.exibirEstado();
    }
}