package br.edu.uepb.editais.usuario;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Domínios de e-mail aceitos no cadastro e o perfil que cada um recebe.
 * Configurado em application.properties (prefixo "app.cadastro").
 */
@ConfigurationProperties(prefix = "app.cadastro")
public record DominiosInstitucionaisProperties(
        List<String> dominiosAluno,
        List<String> dominiosProfessor) {

    public DominiosInstitucionaisProperties {
        dominiosAluno = dominiosAluno == null ? List.of() : List.copyOf(dominiosAluno);
        dominiosProfessor = dominiosProfessor == null ? List.of() : List.copyOf(dominiosProfessor);
    }

    /**
     * Descobre o perfil a partir do domínio do e-mail (parte depois do "@").
     * A comparação é exata, então "x@falso-aluno.uepb.edu.br" NÃO é aceito.
     */
    public Optional<Perfil> perfilPara(String email) {
        int arroba = email.lastIndexOf('@');
        if (arroba < 0) {
            return Optional.empty();
        }
        String dominio = email.substring(arroba + 1).toLowerCase(Locale.ROOT);
        if (contem(dominiosAluno, dominio)) {
            return Optional.of(Perfil.ALUNO);
        }
        if (contem(dominiosProfessor, dominio)) {
            return Optional.of(Perfil.PROFESSOR);
        }
        return Optional.empty();
    }

    private static boolean contem(List<String> dominios, String dominio) {
        return dominios.stream().anyMatch(d -> d.trim().equalsIgnoreCase(dominio));
    }
}
