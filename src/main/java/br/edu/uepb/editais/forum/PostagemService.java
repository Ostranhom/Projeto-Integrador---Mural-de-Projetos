package br.edu.uepb.editais.forum;

import br.edu.uepb.editais.auth.UsuarioLogado;
import br.edu.uepb.editais.usuario.Perfil;
import br.edu.uepb.editais.usuario.Usuario;
import br.edu.uepb.editais.usuario.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio do fórum (o "cozinheiro").
 * O controller só recebe o pedido; quem decide o que pode ou não pode é esta classe.
 */
@Service
public class PostagemService {

    private final PostagemRepository postagemRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;

    // O Spring entrega os repositórios prontos por este construtor (injeção de dependência).
    public PostagemService(PostagemRepository postagemRepository,
                           ComentarioRepository comentarioRepository,
                           UsuarioRepository usuarioRepository) {
        this.postagemRepository = postagemRepository;
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** Lista as postagens. Se a categoria for null, lista todas. */
    public List<Postagem> listar(Categoria categoria) {
        if (categoria == null) {
            return postagemRepository.findAllByOrderByCriadoEmDesc();
        }
        return postagemRepository.findByCategoriaOrderByCriadoEmDesc(categoria);
    }

    /** Busca uma postagem pelo id. Se não existir, o site mostra a página de erro 404. */
    public Postagem buscar(Long id) {
        return postagemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Comentario> listarComentarios(Postagem postagem) {
        return comentarioRepository.findByPostagemOrderByCriadoEmAsc(postagem);
    }

    // ------------------------------------------------------------------
    // Permissões
    // ------------------------------------------------------------------

    /** Alunos não podem usar categorias exclusivas de professor (vagas, bolsas). */
    public boolean podeUsarCategoria(UsuarioLogado usuario, Categoria categoria) {
        if (!categoria.isExclusivaProfessor()) {
            return true;
        }
        return usuario.getPerfil() == Perfil.PROFESSOR || usuario.getPerfil() == Perfil.ADMIN;
    }

    /** Categorias que aparecem no formulário para este usuário. */
    public List<Categoria> categoriasPermitidas(UsuarioLogado usuario) {
        List<Categoria> permitidas = new ArrayList<>();
        for (Categoria categoria : Categoria.values()) {
            if (podeUsarCategoria(usuario, categoria)) {
                permitidas.add(categoria);
            }
        }
        return permitidas;
    }

    /** Só o autor ou um admin pode editar/apagar uma postagem ou comentário. */
    public boolean podeAlterar(UsuarioLogado usuario, Usuario autor) {
        boolean ehAutor = usuario.getId().equals(autor.getId());
        boolean ehAdmin = usuario.getPerfil() == Perfil.ADMIN;
        return ehAutor || ehAdmin;
    }

    // ------------------------------------------------------------------
    // Postagens
    // ------------------------------------------------------------------

    @Transactional
    public Postagem criar(PostagemForm form, UsuarioLogado usuario) {
        if (!podeUsarCategoria(usuario, form.getCategoria())) {
            throw new AccessDeniedException("Apenas professores podem publicar nesta categoria.");
        }
        Usuario autor = usuarioRepository.getReferenceById(usuario.getId());
        Postagem postagem = new Postagem(
                form.getTitulo().trim(),
                form.getConteudo().trim(),
                form.getCategoria(),
                autor);
        return postagemRepository.save(postagem);
    }

    @Transactional
    public void editar(Long id, PostagemForm form, UsuarioLogado usuario) {
        Postagem postagem = buscar(id);
        if (!podeAlterar(usuario, postagem.getAutor())) {
            throw new AccessDeniedException("Você não pode editar esta postagem.");
        }
        if (!podeUsarCategoria(usuario, form.getCategoria())) {
            throw new AccessDeniedException("Apenas professores podem publicar nesta categoria.");
        }
        postagem.atualizar(form.getTitulo().trim(), form.getConteudo().trim(), form.getCategoria());
        postagemRepository.save(postagem);
    }

    @Transactional
    public void excluir(Long id, UsuarioLogado usuario) {
        Postagem postagem = buscar(id);
        if (!podeAlterar(usuario, postagem.getAutor())) {
            throw new AccessDeniedException("Você não pode excluir esta postagem.");
        }
        comentarioRepository.deleteByPostagem(postagem); // primeiro os comentários...
        postagemRepository.delete(postagem);             // ...depois a postagem
    }

    // ------------------------------------------------------------------
    // Comentários
    // ------------------------------------------------------------------

    @Transactional
    public void comentar(Long postagemId, String texto, UsuarioLogado usuario) {
        Postagem postagem = buscar(postagemId);
        Usuario autor = usuarioRepository.getReferenceById(usuario.getId());
        comentarioRepository.save(new Comentario(texto.trim(), postagem, autor));
    }

    /** Apaga um comentário e devolve o id da postagem (para voltar para ela). */
    @Transactional
    public Long excluirComentario(Long comentarioId, UsuarioLogado usuario) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!podeAlterar(usuario, comentario.getAutor())) {
            throw new AccessDeniedException("Você não pode excluir este comentário.");
        }
        Long postagemId = comentario.getPostagem().getId();
        comentarioRepository.delete(comentario);
        return postagemId;
    }
}
