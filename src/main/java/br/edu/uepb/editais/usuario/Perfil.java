package br.edu.uepb.editais.usuario;

/**
 * Perfis de acesso do sistema. Cada perfil vira uma "role" do Spring Security
 * (ex.: PROFESSOR -> ROLE_PROFESSOR), usada para liberar ou bloquear funcionalidades.
 */
public enum Perfil {

    ALUNO("Aluno"),
    PROFESSOR("Professor"),
    ADMIN("Administrador");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
