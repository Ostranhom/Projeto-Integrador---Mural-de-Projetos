package br.edu.uepb.editais.usuario;

/**
 * Erro de regra de negócio no cadastro, associado a um campo do formulário
 * para que a mensagem apareça no lugar certo da tela.
 */
public class CadastroException extends RuntimeException {

    private final String campo;

    public CadastroException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
