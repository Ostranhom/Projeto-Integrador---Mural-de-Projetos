package br.edu.uepb.editais.auth;

import br.edu.uepb.editais.usuario.Perfil;
import br.edu.uepb.editais.usuario.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

/**
 * Usuário autenticado guardado na sessão. Use com
 * {@code @AuthenticationPrincipal UsuarioLogado usuario} nos controllers
 * (útil, por exemplo, para saber o autor de uma postagem).
 */
public class UsuarioLogado implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nome;
    private final String email;
    private final String senha;
    private final Perfil perfil;
    private final boolean ativo;

    public UsuarioLogado(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.senha = usuario.getSenha();
        this.perfil = usuario.getPerfil();
        this.ativo = usuario.isAtivo();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
