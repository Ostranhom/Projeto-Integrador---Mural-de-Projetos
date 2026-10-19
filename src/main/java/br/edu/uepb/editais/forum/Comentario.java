package br.edu.uepb.editais.forum;

import br.edu.uepb.editais.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Um comentário feito em uma postagem. Vira a tabela "comentarios". */
@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String texto;

    // Muitos comentários pertencem a uma postagem
    @ManyToOne(optional = false)
    @JoinColumn(name = "postagem_id")
    private Postagem postagem;

    // Muitos comentários podem ter o mesmo autor
    @ManyToOne(optional = false)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Comentario() {
        // construtor vazio exigido pelo JPA
    }

    public Comentario(String texto, Postagem postagem, Usuario autor) {
        this.texto = texto;
        this.postagem = postagem;
        this.autor = autor;
    }

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public Postagem getPostagem() {
        return postagem;
    }

    public Usuario getAutor() {
        return autor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
