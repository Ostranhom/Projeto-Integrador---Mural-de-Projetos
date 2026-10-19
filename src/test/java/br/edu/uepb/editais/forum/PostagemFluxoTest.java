package br.edu.uepb.editais.forum;

import br.edu.uepb.editais.auth.UsuarioLogado;
import br.edu.uepb.editais.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostagemFluxoTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PostagemRepository postagemRepository;

    @Autowired
    ComentarioRepository comentarioRepository;

    /** Usuários de teste criados pelo DadosIniciaisDev. */
    private UsuarioLogado logado(String email) {
        return new UsuarioLogado(usuarioRepository.findByEmail(email).orElseThrow());
    }

    private UsuarioLogado aluno() {
        return logado("aluno@aluno.uepb.edu.br");
    }

    private UsuarioLogado professor() {
        return logado("professor@servidor.uepb.edu.br");
    }

    private UsuarioLogado admin() {
        return logado("admin@servidor.uepb.edu.br");
    }

    private Long ultimoIdCriado() {
        return postagemRepository.findAllByOrderByCriadoEmDesc().getFirst().getId();
    }

    @Test
    void forumExigeLogin() throws Exception {
        mvc.perform(get("/forum")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void listaMostraPostagensDeExemplo() throws Exception {
        mvc.perform(get("/forum").with(user(aluno())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Vaga de estágio em desenvolvimento web")));
    }

    @Test
    void filtroPorCategoria() throws Exception {
        mvc.perform(get("/forum").param("categoria", "DUVIDA").with(user(aluno())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("edital de monitoria")))
                .andExpect(content().string(not(containsString("Bolsa PIBIC"))));
    }

    @Test
    void alunoCriaDuvidaEComenta() throws Exception {
        mvc.perform(post("/forum/nova").with(csrf()).with(user(aluno()))
                        .param("titulo", "Como funciona o TCC?")
                        .param("conteudo", "Alguém pode explicar?")
                        .param("categoria", "DUVIDA"))
                .andExpect(redirectedUrlPattern("/forum/*"));

        Long id = ultimoIdCriado();
        Postagem criada = postagemRepository.findById(id).orElseThrow();
        assertThat(criada.getTitulo()).isEqualTo("Como funciona o TCC?");
        assertThat(criada.getAutor().getEmail()).isEqualTo("aluno@aluno.uepb.edu.br");

        mvc.perform(post("/forum/" + id + "/comentarios").with(csrf()).with(user(professor()))
                        .param("texto", "Procure a coordenação do curso."))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/forum/" + id).with(user(aluno())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Procure a coordenação do curso.")));
    }

    @Test
    void alunoNaoPodePublicarVaga() throws Exception {
        long antes = postagemRepository.count();
        mvc.perform(post("/forum/nova").with(csrf()).with(user(aluno()))
                        .param("titulo", "Vaga falsa")
                        .param("conteudo", "...")
                        .param("categoria", "VAGA_ESTAGIO"))
                .andExpect(status().isForbidden());
        assertThat(postagemRepository.count()).isEqualTo(antes);
    }

    @Test
    void formularioDoAlunoNaoMostraCategoriaDeVaga() throws Exception {
        mvc.perform(get("/forum/nova").with(user(aluno())))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Vaga de estágio"))));
        mvc.perform(get("/forum/nova").with(user(professor())))
                .andExpect(content().string(containsString("Vaga de estágio")));
    }

    @Test
    void professorPublicaVaga() throws Exception {
        mvc.perform(post("/forum/nova").with(csrf()).with(user(professor()))
                        .param("titulo", "Estágio em redes")
                        .param("conteudo", "Vaga para o NTI.")
                        .param("categoria", "VAGA_ESTAGIO"))
                .andExpect(redirectedUrlPattern("/forum/*"));
        assertThat(postagemRepository.findById(ultimoIdCriado()).orElseThrow().getCategoria())
                .isEqualTo(Categoria.VAGA_ESTAGIO);
    }

    @Test
    void camposVaziosMostramErros() throws Exception {
        mvc.perform(post("/forum/nova").with(csrf()).with(user(aluno()))
                        .param("titulo", "")
                        .param("conteudo", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "titulo", "conteudo", "categoria"));
    }

    @Test
    void somenteAutorOuAdminPodemEditarEExcluir() throws Exception {
        mvc.perform(post("/forum/nova").with(csrf()).with(user(professor()))
                        .param("titulo", "Postagem do professor")
                        .param("conteudo", "Texto original")
                        .param("categoria", "DISCUSSAO"))
                .andExpect(status().is3xxRedirection());
        Long id = ultimoIdCriado();

        // outro usuário (aluno) não pode editar nem excluir
        mvc.perform(get("/forum/" + id + "/editar").with(user(aluno())))
                .andExpect(status().isForbidden());
        mvc.perform(post("/forum/" + id + "/excluir").with(csrf()).with(user(aluno())))
                .andExpect(status().isForbidden());

        // o autor pode editar
        mvc.perform(post("/forum/" + id + "/editar").with(csrf()).with(user(professor()))
                        .param("titulo", "Postagem do professor (editada)")
                        .param("conteudo", "Texto novo")
                        .param("categoria", "DISCUSSAO"))
                .andExpect(redirectedUrl("/forum/" + id));
        assertThat(postagemRepository.findById(id).orElseThrow().getConteudo()).isEqualTo("Texto novo");

        // o admin pode excluir (junto com os comentários)
        mvc.perform(post("/forum/" + id + "/comentarios").with(csrf()).with(user(aluno()))
                .param("texto", "comentário que some junto"));
        mvc.perform(post("/forum/" + id + "/excluir").with(csrf()).with(user(admin())))
                .andExpect(redirectedUrl("/forum?excluida"));
        assertThat(postagemRepository.existsById(id)).isFalse();
    }

    @Test
    void postagemInexistenteDa404() throws Exception {
        mvc.perform(get("/forum/999999").with(user(aluno())))
                .andExpect(status().isNotFound());
    }

    @Test
    void comentarioVazioEhRecusado() throws Exception {
        Long id = postagemRepository.findAllByOrderByCriadoEmDesc().getLast().getId();
        long antes = comentarioRepository.count();
        mvc.perform(post("/forum/" + id + "/comentarios").with(csrf()).with(user(aluno()))
                        .param("texto", "   "))
                .andExpect(redirectedUrl("/forum/" + id + "?erroComentario"));
        assertThat(comentarioRepository.count()).isEqualTo(antes);
    }
}
