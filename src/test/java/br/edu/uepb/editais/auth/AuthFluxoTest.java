package br.edu.uepb.editais.auth;

import br.edu.uepb.editais.usuario.Perfil;
import br.edu.uepb.editais.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFluxoTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository repository;

    @Test
    void paginasPublicasAbremSemLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/cadastro")).andExpect(status().isOk());
    }

    @Test
    void painelExigeLogin() throws Exception {
        mvc.perform(get("/painel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void cadastroDeAlunoComEmailInstitucional() throws Exception {
        mvc.perform(post("/cadastro").with(csrf())
                        .param("nome", "Joana Silva")
                        .param("email", "  Joana@Aluno.UEPB.edu.br ")
                        .param("senha", "minhasenha")
                        .param("confirmacaoSenha", "minhasenha"))
                .andExpect(redirectedUrl("/login?cadastrado"));

        var usuario = repository.findByEmail("joana@aluno.uepb.edu.br").orElseThrow();
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.ALUNO);
        assertThat(usuario.getSenha()).startsWith("{bcrypt}").doesNotContain("minhasenha");

        mvc.perform(formLogin("/login").user("email", "joana@aluno.uepb.edu.br")
                        .password("senha", "minhasenha"))
                .andExpect(authenticated().withRoles("ALUNO"));
    }

    @Test
    void cadastroRecusaEmailNaoInstitucional() throws Exception {
        mvc.perform(post("/cadastro").with(csrf())
                        .param("nome", "Fulano")
                        .param("email", "fulano@gmail.com")
                        .param("senha", "minhasenha")
                        .param("confirmacaoSenha", "minhasenha"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "email"));

        assertThat(repository.existsByEmail("fulano@gmail.com")).isFalse();
    }

    @Test
    void cadastroRecusaSenhasDiferentes() throws Exception {
        mvc.perform(post("/cadastro").with(csrf())
                        .param("nome", "Fulano")
                        .param("email", "fulano@aluno.uepb.edu.br")
                        .param("senha", "minhasenha")
                        .param("confirmacaoSenha", "outrasenha"))
                .andExpect(model().attributeHasFieldErrors("form", "confirmacaoSenha"));
    }

    @Test
    void loginComSenhaErradaFalha() throws Exception {
        mvc.perform(formLogin("/login").user("email", "professor@servidor.uepb.edu.br")
                        .password("senha", "errada"))
                .andExpect(redirectedUrl("/login?erro"))
                .andExpect(unauthenticated());
    }

    @Test
    void professorDeTesteFazLogin() throws Exception {
        mvc.perform(formLogin("/login").user("email", "professor@servidor.uepb.edu.br")
                        .password("senha", "senha123"))
                .andExpect(authenticated().withRoles("PROFESSOR"));
    }

    @Test
    void painelMostraConteudoConformePerfil() throws Exception {
        var sessaoProfessor = (MockHttpSession) mvc.perform(formLogin("/login")
                        .user("email", "professor@servidor.uepb.edu.br").password("senha", "senha123"))
                .andReturn().getRequest().getSession();
        mvc.perform(get("/painel").session(sessaoProfessor))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Professor Teste")))
                .andExpect(content().string(containsString("Divulgar vaga")));

        var sessaoAluno = (MockHttpSession) mvc.perform(formLogin("/login")
                        .user("email", "aluno@aluno.uepb.edu.br").password("senha", "senha123"))
                .andReturn().getRequest().getSession();
        mvc.perform(get("/painel").session(sessaoAluno))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Divulgar vaga"))));
    }
}
