package br.edu.uepb.editais.forum;

import br.edu.uepb.editais.auth.UsuarioLogado;
import br.edu.uepb.editais.usuario.Perfil;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Páginas do fórum (o "garçom"). Todas as URLs começam com /forum.
 *
 *   GET  /forum                      -> lista de postagens (pode filtrar por ?categoria=)
 *   GET  /forum/nova                 -> formulário de nova postagem
 *   POST /forum/nova                 -> salva a nova postagem
 *   GET  /forum/{id}                 -> mostra uma postagem e seus comentários
 *   GET  /forum/{id}/editar          -> formulário de edição
 *   POST /forum/{id}/editar          -> salva a edição
 *   POST /forum/{id}/excluir         -> apaga a postagem
 *   POST /forum/{id}/comentarios     -> adiciona um comentário
 *   POST /forum/comentarios/{id}/excluir -> apaga um comentário
 *
 * @AuthenticationPrincipal entrega o usuário que está logado.
 */
@Controller
@RequestMapping("/forum")
public class PostagemController {

    private static final int TAMANHO_MAXIMO_COMENTARIO = 2000;

    private final PostagemService postagemService;

    public PostagemController(PostagemService postagemService) {
        this.postagemService = postagemService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Categoria categoria, Model model) {
        model.addAttribute("postagens", postagemService.listar(categoria));
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("categoriaSelecionada", categoria);
        return "forum/lista";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id,
                      @AuthenticationPrincipal UsuarioLogado usuario,
                      Model model) {
        Postagem postagem = postagemService.buscar(id);
        model.addAttribute("postagem", postagem);
        model.addAttribute("comentarios", postagemService.listarComentarios(postagem));
        model.addAttribute("podeAlterar", postagemService.podeAlterar(usuario, postagem.getAutor()));
        // usados na página para decidir se mostra o botão "excluir" de cada comentário
        model.addAttribute("usuarioId", usuario.getId());
        model.addAttribute("ehAdmin", usuario.getPerfil() == Perfil.ADMIN);
        return "forum/detalhe";
    }

    // ------------------------------------------------------------------
    // Nova postagem
    // ------------------------------------------------------------------

    @GetMapping("/nova")
    public String formularioNova(@RequestParam(required = false) Categoria categoria,
                                 @AuthenticationPrincipal UsuarioLogado usuario,
                                 Model model) {
        PostagemForm form = new PostagemForm();
        form.setCategoria(categoria); // permite abrir já com a categoria escolhida
        model.addAttribute("form", form);
        prepararFormulario(model, usuario, null);
        return "forum/form";
    }

    @PostMapping("/nova")
    public String criar(@Valid @ModelAttribute("form") PostagemForm form,
                        BindingResult resultado,
                        @AuthenticationPrincipal UsuarioLogado usuario,
                        Model model) {
        if (resultado.hasErrors()) {
            // algum campo inválido: mostra o formulário de novo com as mensagens de erro
            prepararFormulario(model, usuario, null);
            return "forum/form";
        }
        Postagem postagem = postagemService.criar(form, usuario);
        return "redirect:/forum/" + postagem.getId();
    }

    // ------------------------------------------------------------------
    // Editar / excluir
    // ------------------------------------------------------------------

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id,
                                   @AuthenticationPrincipal UsuarioLogado usuario,
                                   Model model) {
        Postagem postagem = postagemService.buscar(id);
        if (!postagemService.podeAlterar(usuario, postagem.getAutor())) {
            throw new AccessDeniedException("Você não pode editar esta postagem.");
        }
        // preenche o formulário com os dados atuais da postagem
        PostagemForm form = new PostagemForm();
        form.setTitulo(postagem.getTitulo());
        form.setConteudo(postagem.getConteudo());
        form.setCategoria(postagem.getCategoria());

        model.addAttribute("form", form);
        prepararFormulario(model, usuario, id);
        return "forum/form";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @Valid @ModelAttribute("form") PostagemForm form,
                         BindingResult resultado,
                         @AuthenticationPrincipal UsuarioLogado usuario,
                         Model model) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, usuario, id);
            return "forum/form";
        }
        postagemService.editar(id, form, usuario);
        return "redirect:/forum/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado usuario) {
        postagemService.excluir(id, usuario);
        return "redirect:/forum?excluida";
    }

    // ------------------------------------------------------------------
    // Comentários
    // ------------------------------------------------------------------

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id,
                           @RequestParam String texto,
                           @AuthenticationPrincipal UsuarioLogado usuario) {
        if (texto.isBlank() || texto.length() > TAMANHO_MAXIMO_COMENTARIO) {
            return "redirect:/forum/" + id + "?erroComentario";
        }
        postagemService.comentar(id, texto, usuario);
        return "redirect:/forum/" + id + "#comentarios";
    }

    @PostMapping("/comentarios/{comentarioId}/excluir")
    public String excluirComentario(@PathVariable Long comentarioId,
                                    @AuthenticationPrincipal UsuarioLogado usuario) {
        Long postagemId = postagemService.excluirComentario(comentarioId, usuario);
        return "redirect:/forum/" + postagemId + "#comentarios";
    }

    // ------------------------------------------------------------------

    /**
     * Coloca na página o que o formulário precisa.
     * postagemId == null significa "nova postagem"; caso contrário, "editando".
     */
    private void prepararFormulario(Model model, UsuarioLogado usuario, Long postagemId) {
        model.addAttribute("categorias", postagemService.categoriasPermitidas(usuario));
        model.addAttribute("postagemId", postagemId);
    }
}
