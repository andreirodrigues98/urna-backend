package br.com.urnacidada.urna_backend.repository;

/** Falha real de acesso ao banco, distinta de erro de validação/estado. */
public class RepositorioIndisponivelException extends RuntimeException {
    public RepositorioIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
