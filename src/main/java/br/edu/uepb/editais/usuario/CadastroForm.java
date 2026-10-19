package br.edu.uepb.editais.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados enviados pelo formulário de cadastro. */
public class CadastroForm {

    @NotBlank(message = "Informe seu nome.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    private String nome;

    @NotBlank(message = "Informe seu e-mail institucional.")
    @Email(message = "E-mail inválido.")
    @Size(max = 160, message = "E-mail muito longo.")
    private String email;

    // BCrypt só considera os primeiros 72 bytes da senha, por isso o limite.
    @NotBlank(message = "Informe uma senha.")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
    private String senha;

    @NotBlank(message = "Confirme a senha.")
    private String confirmacaoSenha;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome == null ? null : nome.trim();
    }

    public String getEmail() {
        return email;
    }

    // Remove espaços antes da validação (comum ao copiar/colar o e-mail).
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmacaoSenha() {
        return confirmacaoSenha;
    }

    public void setConfirmacaoSenha(String confirmacaoSenha) {
        this.confirmacaoSenha = confirmacaoSenha;
    }
}
