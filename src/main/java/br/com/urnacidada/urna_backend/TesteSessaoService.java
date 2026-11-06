package br.com.urnacidada.urna_backend;

import br.com.urnacidada.urna_backend.model.*;

/** Teste isolado da máquina de estados, sem criar dados reais no Firestore. */
public class TesteSessaoService {
    public static void main(String[] args) {
        Partido p = new Partido("Partido Marvel", "PM", "71");
        SessaoVotacao s = new SessaoVotacao();
        System.out.println("INÍCIO: " + s.getEtapa());
        s.confirmarVoto(new Voto("Deputado Federal", TipoVoto.VALIDO,
            new Deputado("Homem-Aranha", "7101", p)));
        System.out.println("APÓS DEPUTADO: " + s.getEtapa());
        s.confirmarVoto(new Voto("Presidente", TipoVoto.VALIDO,
            new Presidente("Homem de Ferro", "71", p, new VicePresidente("Thor"))));
        System.out.println("APÓS PRESIDENTE: " + s.getEtapa());
    }
}
