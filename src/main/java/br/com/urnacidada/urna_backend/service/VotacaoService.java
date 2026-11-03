
package br.com.urnacidada.urna_backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.urnacidada.urna_backend.model.Candidato;
import br.com.urnacidada.urna_backend.model.Deputado;
import br.com.urnacidada.urna_backend.model.EtapaVotacao;
import br.com.urnacidada.urna_backend.model.Partido;
import br.com.urnacidada.urna_backend.model.Presidente;
import br.com.urnacidada.urna_backend.model.SessaoVotacao;
import br.com.urnacidada.urna_backend.model.TipoVoto;
import br.com.urnacidada.urna_backend.model.Voto;

@Service
public class VotacaoService {

    private final List<Candidato> candidatos;
    private final List<Voto> votosConfirmados;
    private final Map<UUID, SessaoVotacao> sessoes;

    // CONSTRUTOR
    public VotacaoService() {

        candidatos = new ArrayList<>();
        votosConfirmados = new ArrayList<>();
        sessoes = new HashMap<>();

        // PARTIDOS FICTÍCIOS
        Partido partido1 = new Partido(
            "Partido dos Inventores",
            "PIN",
            "71"
        );

        Partido partido2 = new Partido(
            "Partido das Nuvens",
            "PNV",
            "72"
        );

        // CANDIDATOS FICTÍCIOS
        cadastrarCandidato(new Deputado(
            "Nico Circuito",
            "7101",
            partido1
        ));

        cadastrarCandidato(new Presidente(
            "Lia Parafuso",
            "71",
            partido1
        ));

        cadastrarCandidato(new Deputado(
            "Bia Arco-Íris",
            "7201",
            partido2
        ));

        cadastrarCandidato(new Presidente(
            "Luna Neblina",
            "72",
            partido2
        ));
    }

    // CADASTRAR CANDIDATO
    public synchronized void cadastrarCandidato(Candidato candidato) {

        if (candidato == null) {
            throw new IllegalArgumentException(
                "O candidato não pode ser nulo."
            );
        }

        Candidato existente = buscarCandidato(
            candidato.getCargo(),
            candidato.getNumero()
        );

        if (existente != null) {
            throw new IllegalArgumentException(
                "Já existe um candidato com esse número e cargo."
            );
        }

        candidatos.add(candidato);
    }

    // BUSCAR CANDIDATO
    public synchronized Candidato buscarCandidato(
        String cargo,
        String numero
    ) {

        for (Candidato candidato : candidatos) {

            if (
                candidato.getCargo().equals(cargo)
                && candidato.getNumero().equals(numero)
            ) {
                return candidato;
            }
        }

        return null;
    }

    // LISTAR CANDIDATOS
    public synchronized List<Candidato> listarCandidatos() {
        return List.copyOf(candidatos);
    }

    // CRIAR NOVA SESSÃO
    public synchronized SessaoVotacao criarSessao() {

        SessaoVotacao sessao = new SessaoVotacao();

        sessoes.put(sessao.getId(), sessao);

        return sessao;
    }

    // BUSCAR SESSÃO
    public synchronized SessaoVotacao buscarSessao(UUID id) {

        if (id == null) {
            throw new IllegalArgumentException(
                "O ID da sessão não pode ser nulo."
            );
        }

        SessaoVotacao sessao = sessoes.get(id);

        if (sessao == null) {
            throw new IllegalArgumentException(
                "Sessão de votação não encontrada."
            );
        }

        return sessao;
    }

    // PREPARAR VOTO SEM CONFIRMAR
    public synchronized Voto prepararVoto(
        String cargo,
        String numero,
        boolean branco
    ) {

        int quantidadeDigitos;

        if ("Deputado Federal".equals(cargo)) {
            quantidadeDigitos = 4;

        } else if ("Presidente".equals(cargo)) {
            quantidadeDigitos = 2;

        } else {
            throw new IllegalArgumentException(
                "Cargo inválido."
            );
        }

        // VOTO BRANCO
        if (branco) {
            return new Voto(
                cargo,
                TipoVoto.BRANCO,
                null
            );
        }

        // VALIDAR O NÚMERO
        if (
            numero == null
            || !numero.matches("\\d{" + quantidadeDigitos + "}")
        ) {
            throw new IllegalArgumentException(
                "Número inválido para o cargo informado."
            );
        }

        // BUSCAR CANDIDATO
        Candidato candidato = buscarCandidato(
            cargo,
            numero
        );

        // VOTO NULO
        if (candidato == null) {
            return new Voto(
                cargo,
                TipoVoto.NULO,
                null
            );
        }

        // VOTO VÁLIDO
        return new Voto(
            cargo,
            TipoVoto.VALIDO,
            candidato
        );
    }

    // PREPARAR VOTO VINCULADO À SESSÃO
    public synchronized Voto prepararVotoNaSessao(
        UUID idSessao,
        String cargo,
        String numero,
        boolean branco
    ) {

        SessaoVotacao sessao = buscarSessao(idSessao);

        EtapaVotacao etapa = sessao.getEtapa();

        // IMPEDIR PREPARAÇÃO APÓS O FIM
        if (etapa == EtapaVotacao.FINALIZADA) {
            throw new IllegalStateException(
                "Esta sessão já foi finalizada."
            );
        }

        String cargoEsperado;

        // IDENTIFICAR CARGO DA ETAPA ATUAL
        if (etapa == EtapaVotacao.DEPUTADO) {

            cargoEsperado = "Deputado Federal";

        } else if (etapa == EtapaVotacao.PRESIDENTE) {

            cargoEsperado = "Presidente";

        } else {
            throw new IllegalStateException(
                "Etapa de votação inválida."
            );
        }

        // CONFERIR SE O CARGO CORRESPONDE À ETAPA
        if (!cargoEsperado.equals(cargo)) {
            throw new IllegalStateException(
                "A etapa atual exige um voto para "
                + cargoEsperado
            );
        }

        // REUTILIZAR O MÉTODO ORIGINAL
        return prepararVoto(
            cargo,
            numero,
            branco
        );
    }

    // CONFIRMAR VOTO DENTRO DA SESSÃO
    public synchronized Voto confirmarVoto(
        UUID idSessao,
        String cargo,
        String numero,
        boolean branco
    ) {

        SessaoVotacao sessao = buscarSessao(idSessao);

        // PREPARAR RESPEITANDO A ETAPA ATUAL
        Voto voto = prepararVotoNaSessao(
            idSessao,
            cargo,
            numero,
            branco
        );

        // CONFIRMAR E AVANÇAR A ETAPA
        sessao.confirmarVoto(voto);

        // REGISTRAR O VOTO NA MEMÓRIA
        votosConfirmados.add(voto);

        return voto;
    }

    // BLOQUEAR A CONFIRMAÇÃO ANTIGA SEM SESSÃO
    @Deprecated
    public void confirmarVoto(Voto voto) {

        throw new IllegalStateException(
            "Confirmação sem sessão desativada. "
            + "Informe o UUID da sessão."
        );
    }

    // CONTAR VOTOS CONFIRMADOS
    public synchronized int contarVotosConfirmados() {
        return votosConfirmados.size();
    }
}
