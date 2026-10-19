package br.edu.uepb.editais.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados que vêm do formulário de nova postagem / edição.
 * As anotações (@NotBlank, @Size...) validam os campos automaticamente.
 */
public class PostagemForm {

    @NotBlank(message = "Informe um título.")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres.")
    private String titulo;

    @NotBlank(message = "Escreva o conteúdo da postagem.")
    @Size(max = 5000, message = "O conteúdo deve ter no máximo 5000 caracteres.")
    private String conteudo;

    @NotNull(message = "Escolha uma categoria.")
    private Categoria categoria;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
