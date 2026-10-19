package br.edu.uepb.editais.forum;

/**
 * Categorias de postagem do fórum.
 *
 * O segundo valor (true/false) diz se a categoria é EXCLUSIVA de professores.
 * Exemplo: só professores (e admins) podem divulgar vagas de estágio.
 */
public enum Categoria {

    DISCUSSAO("Discussão", false),
    DUVIDA("Dúvida", false),
    VAGA_ESTAGIO("Vaga de estágio", true),
    BOLSA_PESQUISA("Bolsa / Pesquisa", true);

    private final String descricao;
    private final boolean exclusivaProfessor;

    Categoria(String descricao, boolean exclusivaProfessor) {
        this.descricao = descricao;
        this.exclusivaProfessor = exclusivaProfessor;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isExclusivaProfessor() {
        return exclusivaProfessor;
    }
}
