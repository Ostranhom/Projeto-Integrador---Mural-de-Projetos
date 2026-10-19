package br.edu.uepb.editais.config;

import br.edu.uepb.editais.forum.Categoria;
import br.edu.uepb.editais.forum.Comentario;
import br.edu.uepb.editais.forum.ComentarioRepository;
import br.edu.uepb.editais.forum.Postagem;
import br.edu.uepb.editais.forum.PostagemRepository;
import br.edu.uepb.editais.usuario.Perfil;
import br.edu.uepb.editais.usuario.Usuario;
import br.edu.uepb.editais.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria usuários e postagens de teste ao subir a aplicação no perfil "dev".
 * NUNCA roda em produção (perfil "mysql").
 */
@Component
@Profile("dev")
public class DadosIniciaisDev implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciaisDev.class);
    private static final String SENHA_PADRAO = "senha123";

    private final UsuarioRepository repository;
    private final PostagemRepository postagemRepository;
    private final ComentarioRepository comentarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DadosIniciaisDev(UsuarioRepository repository,
                            PostagemRepository postagemRepository,
                            ComentarioRepository comentarioRepository,
                            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.postagemRepository = postagemRepository;
        this.comentarioRepository = comentarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Usuario aluno = criar("Aluno Teste", "aluno@aluno.uepb.edu.br", Perfil.ALUNO);
        Usuario professor = criar("Professor Teste", "professor@servidor.uepb.edu.br", Perfil.PROFESSOR);
        criar("Admin Teste", "admin@servidor.uepb.edu.br", Perfil.ADMIN);
        log.info("Usuários de teste disponíveis (senha: {}): aluno@aluno.uepb.edu.br, "
                + "professor@servidor.uepb.edu.br, admin@servidor.uepb.edu.br", SENHA_PADRAO);

        if (postagemRepository.count() == 0) {
            criarPostagensDeExemplo(aluno, professor);
        }
    }

    private Usuario criar(String nome, String email, Perfil perfil) {
        if (repository.existsByEmail(email)) {
            return repository.findByEmail(email).orElseThrow();
        }
        return repository.save(new Usuario(nome, email, passwordEncoder.encode(SENHA_PADRAO), perfil));
    }

    private void criarPostagensDeExemplo(Usuario aluno, Usuario professor) {
        postagemRepository.save(new Postagem(
                "Vaga de estágio em desenvolvimento web",
                "O laboratório de computação está com 2 vagas de estágio para alunos a partir do 4º período.\n\n"
                        + "Requisitos: conhecimento básico de Java e HTML.\n"
                        + "Interessados, comentem aqui ou falem comigo na coordenação.",
                Categoria.VAGA_ESTAGIO, professor));

        postagemRepository.save(new Postagem(
                "Bolsa PIBIC - projeto de análise de dados",
                "Procuro um(a) aluno(a) para bolsa de iniciação científica no projeto de análise de dados educacionais.",
                Categoria.BOLSA_PESQUISA, professor));

        Postagem duvida = postagemRepository.save(new Postagem(
                "Alguém sabe quando sai o resultado do edital de monitoria?",
                "Me inscrevi semana passada e ainda não vi nada no site. Alguém tem notícia?",
                Categoria.DUVIDA, aluno));

        comentarioRepository.save(new Comentario(
                "O resultado deve sair até o fim do mês, fique de olho no site da PRPGP.",
                duvida, professor));
    }
}
