package br.edu.uepb.editais.forum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso ao banco para comentários. */
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    /** Comentários de uma postagem, do mais antigo para o mais novo. */
    List<Comentario> findByPostagemOrderByCriadoEmAsc(Postagem postagem);

    /** Apaga todos os comentários de uma postagem (usado antes de apagar a postagem). */
    void deleteByPostagem(Postagem postagem);
}
