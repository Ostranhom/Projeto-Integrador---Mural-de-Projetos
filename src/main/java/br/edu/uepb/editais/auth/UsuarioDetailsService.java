package br.edu.uepb.editais.auth;

import br.edu.uepb.editais.usuario.UsuarioRepository;
import br.edu.uepb.editais.usuario.UsuarioService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Ensina o Spring Security a buscar usuários no nosso banco pelo e-mail. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(UsuarioService.normalizarEmail(email))
                .map(UsuarioLogado::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
