package br.edu.uepb.editais.forum;

import br.edu.uepb.editais.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Uma postagem do fórum. O @Entity faz esta classe virar a tabela "postagens" no banco.
 */
@Entity
@Table(name = "postagens")
public class Postagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // o banco gera o id sozinho (1, 2, 3...)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 5000)
    private String conteudo;

    @Enumerated(EnumType.STRING) // salva o nome da categoria ("DUVIDA") em vez de um número
    @Column(nullable = false, length = 30)
    private Categoria categoria;

    // Muitas postagens podem ter o mesmo autor -> @ManyToOne
    @ManyToOne(optional = false)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime editadoEm;

    protected Postagem() {
        // construtor vazio exigido pelo JPA
    }

    public Postagem(String titulo, String conteudo, Categoria categoria, Usuario autor) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.categoria = categoria;
        this.autor = autor;
    }

    /** Chamado automaticamente antes de salvar pela primeira vez. */
    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    /** Chamado automaticamente antes de salvar uma alteração. */
    @PreUpdate
    void aoEditar() {
        this.editadoEm = LocalDateTime.now();
    }

    public void atualizar(String titulo, String conteudo, Categoria categoria) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Usuario getAutor() {
        return autor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getEditadoEm() {
        return editadoEm;
    }
}
