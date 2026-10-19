package br.edu.uepb.editais.forum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acesso ao banco para postagens.
 *
 * Não precisamos escrever SQL: o Spring lê o NOME do método e monta a consulta.
 * Ex.: findByCategoriaOrderByCriadoEmDesc = "busque pela categoria, ordenando da mais nova para a mais antiga".
 * Métodos como save(), findById() e delete() já vêm prontos do JpaRepository.
 */
public interface PostagemRepository extends JpaRepository<Postagem, Long> {

    List<Postagem> findAllByOrderByCriadoEmDesc();

    List<Postagem> findByCategoriaOrderByCriadoEmDesc(Categoria categoria);
}
