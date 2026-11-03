package br.com.urnacidada.urna_backend.model;

import java.util.UUID;

public class SessaoVotacao {

    private final UUID id;
    private EtapaVotacao etapa;
    private Voto votoDeputado;
    private Voto votoPresidente;

    // CONSTRUTOR
    public SessaoVotacao() {
        this.id = UUID.randomUUID();
        this.etapa = EtapaVotacao.DEPUTADO;
        this.votoDeputado = null;
        this.votoPresidente = null;
    }

    // GETTERS
    public UUID getId() {
        return id;
    }

    public EtapaVotacao getEtapa() {
        return etapa;
    }

    public Voto getVotoDeputado() {
        return votoDeputado;
    }

    public Voto getVotoPresidente() {
        return votoPresidente;
    }

    // CONFIRMAR VOTO
    public synchronized void confirmarVoto(Voto voto) {

        if (voto == null) {
            throw new IllegalArgumentException(
                "O voto não pode ser nulo."
            );
        }

        // IMPEDIR VOTOS APÓS O FIM DA SESSÃO
        if (etapa == EtapaVotacao.FINALIZADA) {
            throw new IllegalStateException(
                "Esta sessão de votação já foi finalizada."
            );
        }

        // VOTAÇÃO PARA DEPUTADO
        if (etapa == EtapaVotacao.DEPUTADO) {

            if (!"Deputado Federal".equals(voto.getCargo())) {
                throw new IllegalArgumentException(
                    "A etapa atual exige um voto para Deputado Federal."
                );
            }

            this.votoDeputado = voto;
            this.etapa = EtapaVotacao.PRESIDENTE;

        // VOTAÇÃO PARA PRESIDENTE
        } else if (etapa == EtapaVotacao.PRESIDENTE) {

            if (!"Presidente".equals(voto.getCargo())) {
                throw new IllegalArgumentException(
                    "A etapa atual exige um voto para Presidente."
                );
            }

            this.votoPresidente = voto;
            this.etapa = EtapaVotacao.FINALIZADA;
        }
    }

    // VERIFICAR SE A VOTAÇÃO TERMINOU
    public boolean isFinalizada() {
        return etapa == EtapaVotacao.FINALIZADA;
    }

    // EXIBIR ESTADO DA SESSÃO
    public void exibirEstado() {

        System.out.println("Sessão: " + id);
        System.out.println("Etapa atual: " + etapa);
        System.out.println("Finalizada: " + isFinalizada());
    }
}