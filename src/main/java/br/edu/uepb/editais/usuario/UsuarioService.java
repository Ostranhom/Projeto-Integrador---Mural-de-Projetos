package br.edu.uepb.editais.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final DominiosInstitucionaisProperties dominios;

    public UsuarioService(UsuarioRepository repository,
                          PasswordEncoder passwordEncoder,
                          DominiosInstitucionaisProperties dominios) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.dominios = dominios;
    }

    /**
     * Cadastra um novo usuário. O perfil (ALUNO/PROFESSOR) é definido pelo
     * domínio do e-mail institucional, nunca escolhido pelo próprio usuário.
     */
    @Transactional
    public Usuario cadastrar(CadastroForm form) {
        String email = normalizarEmail(form.getEmail());

        if (!form.getSenha().equals(form.getConfirmacaoSenha())) {
            throw new CadastroException("confirmacaoSenha", "As senhas não conferem.");
        }

        Perfil perfil = dominios.perfilPara(email)
                .orElseThrow(() -> new CadastroException("email",
                        "Use seu e-mail institucional da UEPB."));

        if (repository.existsByEmail(email)) {
            throw new CadastroException("email", "Já existe uma conta com este e-mail.");
        }

        Usuario usuario = new Usuario(
                form.getNome().trim(),
                email,
                passwordEncoder.encode(form.getSenha()),
                perfil);
        return repository.save(usuario);
    }

    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
