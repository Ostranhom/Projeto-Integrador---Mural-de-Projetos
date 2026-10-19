# FIP — Fórum Integrado de Projetos (UEPB)

Plataforma acadêmica da UEPB que reúne todos os editais (iniciação científica, extensão, monitoria, etc.)
e oferece um fórum integrado para alunos e professores interagirem e divulgarem vagas de estágio e bolsas de pesquisa.

## Stack

- **Java 21+** e **Spring Boot 4** (Spring MVC, Spring Security, Spring Data JPA, Bean Validation)
- **Thymeleaf** para as páginas HTML (renderizadas no servidor)
- **MySQL** em produção / **H2** (banco em memória) em desenvolvimento
- **Maven** via Maven Wrapper (`mvnw`) — não precisa instalar o Maven

## Equipe

- Eugênio Genuíno Mota — @ostranhom-github

- Davi Marinho Donato — @Kenichi01x

- Ian — @iannespinola0-ops


## Como rodar

Pré-requisito: JDK 21 ou mais novo instalado (`java -version`).

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Acesse <http://localhost:8080>. No perfil `dev` (padrão), o banco é H2 em memória e são criados
usuários de teste (senha `senha123`):

| E-mail                           | Perfil    |
|----------------------------------|-----------|
| `aluno@aluno.uepb.edu.br`        | ALUNO     |
| `professor@servidor.uepb.edu.br` | PROFESSOR |
| `admin@servidor.uepb.edu.br`     | ADMIN     |

Console do banco H2: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:editais`, usuário `sa`, sem senha).

### Usando MySQL

```bash
docker compose up -d                                   # sobe um MySQL local (ou use um MySQL já instalado)
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

As credenciais podem ser trocadas pelas variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`.

### Testes

```bash
.\mvnw.cmd test
```

## Estrutura do código

O código é organizado **por funcionalidade** — cada pasta é um módulo que alguém do grupo pode assumir.

```
src/main/java/br/edu/uepb/editais/
├── config/       SecurityConfig (regras de acesso), dados de teste do perfil dev
├── auth/         login/cadastro: AuthController, UsuarioLogado, UsuarioDetailsService
├── usuario/      entidade Usuario, Perfil, UsuarioService, regras de domínio de e-mail
├── web/          HomeController (página inicial e painel)
├── forum/        fórum: Postagem, Comentario, Categoria, PostagemService, PostagemController
└── edital/       (próximo) editais
src/main/resources/
├── templates/    páginas Thymeleaf (fragments/layout.html tem navbar e rodapé; forum/ tem as páginas do fórum)
├── static/css/   estilos
└── application*.properties
```

## Sistema de login

- Cadastro somente com e-mail institucional. O **perfil é definido pelo domínio** (configurável em
  `application.properties`, chaves `app.cadastro.dominios-*`):
  - `@aluno.uepb.edu.br` → `ALUNO`
  - `@servidor.uepb.edu.br` → `PROFESSOR`
  - `ADMIN` é atribuído manualmente no banco.
- Senhas guardadas com hash **BCrypt**; sessão com proteção **CSRF** (o Thymeleaf injeta o token nos formulários).
- Para restringir algo por perfil:
  - na URL: `SecurityConfig` → `.requestMatchers("/admin/**").hasRole("ADMIN")`
  - no método: `@PreAuthorize("hasRole('PROFESSOR')")`
  - na página: `<div sec:authorize="hasRole('PROFESSOR')">…</div>`
- Para pegar o usuário logado num controller: `@AuthenticationPrincipal UsuarioLogado usuario`
  (tem `getId()`, `getNome()`, `getPerfil()`).

## Fórum (postagens)

Acesse em <http://localhost:8080/forum> (precisa estar logado).

| Categoria          | Quem pode publicar        |
|--------------------|---------------------------|
| Discussão, Dúvida  | qualquer usuário logado   |
| Vaga de estágio, Bolsa / Pesquisa | só PROFESSOR e ADMIN |

- Qualquer usuário logado pode ler e comentar.
- Só o **autor** ou um **ADMIN** pode editar/excluir uma postagem ou comentário.
- As regras ficam todas em `PostagemService`; as categorias (e quem pode usá-las) em `Categoria`.
- Para criar uma nova categoria basta adicionar uma linha no enum `Categoria` (e uma cor `.badge-NOME` no CSS).

## Próximos passos

- [ ] **Confirmação de e-mail** (link enviado por e-mail antes de ativar a conta) — sem isso, qualquer
      pessoa pode se cadastrar com um e-mail institucional que não é dela e ganhar o perfil de professor.
- [ ] Recuperação de senha ("esqueci minha senha")
- [x] Módulo de postagens/fórum (`forum/`)
- [ ] Paginação e busca no fórum
- [ ] Módulo de editais (`edital/`)
- [ ] Migrações de banco com Flyway (substituir `ddl-auto=update`)
- [ ] Limitar tentativas de login (proteção contra força bruta)
