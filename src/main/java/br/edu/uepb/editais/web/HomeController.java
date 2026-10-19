package br.edu.uepb.editais.web;

import br.edu.uepb.editais.auth.UsuarioLogado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** Página inicial pública (futuramente: listagem de editais). */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /** Área logada: ponto de partida para alunos e professores. */
    @GetMapping("/painel")
    public String painel(@AuthenticationPrincipal UsuarioLogado usuario, Model model) {
        model.addAttribute("usuario", usuario);
        return "painel";
    }
}
