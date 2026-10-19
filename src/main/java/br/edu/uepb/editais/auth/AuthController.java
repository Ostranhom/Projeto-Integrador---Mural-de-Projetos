package br.edu.uepb.editais.auth;

import br.edu.uepb.editais.usuario.CadastroException;
import br.edu.uepb.editais.usuario.CadastroForm;
import br.edu.uepb.editais.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** O POST de /login é tratado automaticamente pelo Spring Security. */
    @GetMapping("/login")
    public String login(Authentication authentication) {
        return estaLogado(authentication) ? "redirect:/painel" : "auth/login";
    }

    @GetMapping("/cadastro")
    public String formularioCadastro(Authentication authentication, Model model) {
        if (estaLogado(authentication)) {
            return "redirect:/painel";
        }
        model.addAttribute("form", new CadastroForm());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroForm form,
                            BindingResult resultado) {
        if (resultado.hasErrors()) {
            return "auth/cadastro";
        }
        try {
            usuarioService.cadastrar(form);
        } catch (CadastroException e) {
            resultado.rejectValue(e.getCampo(), "cadastro", e.getMessage());
            return "auth/cadastro";
        }
        return "redirect:/login?cadastrado";
    }

    private static boolean estaLogado(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
