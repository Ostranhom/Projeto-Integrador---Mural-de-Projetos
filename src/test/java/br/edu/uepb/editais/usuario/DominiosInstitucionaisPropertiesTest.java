package br.edu.uepb.editais.usuario;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DominiosInstitucionaisPropertiesTest {

    private final DominiosInstitucionaisProperties dominios = new DominiosInstitucionaisProperties(
            List.of("aluno.uepb.edu.br"), List.of("servidor.uepb.edu.br"));

    @Test
    void emailDeAlunoRecebePerfilAluno() {
        assertThat(dominios.perfilPara("joao@aluno.uepb.edu.br")).contains(Perfil.ALUNO);
    }

    @Test
    void emailDeServidorRecebePerfilProfessor() {
        assertThat(dominios.perfilPara("maria@SERVIDOR.uepb.edu.br")).contains(Perfil.PROFESSOR);
    }

    @Test
    void emailExternoNaoEhAceito() {
        assertThat(dominios.perfilPara("joao@gmail.com")).isEmpty();
    }

    @Test
    void subdominioParecidoNaoEhAceito() {
        assertThat(dominios.perfilPara("joao@falso-aluno.uepb.edu.br")).isEmpty();
        assertThat(dominios.perfilPara("joao@aluno.uepb.edu.br.hacker.com")).isEmpty();
    }
}
