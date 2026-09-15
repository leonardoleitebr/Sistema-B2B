# Sistema B2B - Distribuidora de Bebidas

## Sprint 1 (RF01) + Sprint 2 (RF02 + redefinicao de senha)

Este pacote contem:
- **RF01** - Cadastro e gestao de usuarios e perfis de acesso.
- **RF02** - Autenticacao e controle de acesso por perfil.
- **Redefinicao de senha por e-mail** ("Esqueci minha senha"), complementando
  o RF02: o Administrador cadastra o usuario e escolhe o perfil, e depois o
  proprio usuario define a senha que quiser, sem depender do Administrador
  para isso.

### Estrutura de pastas

```
sistema-b2b/
├── backend/        -> API REST em Java (Spring Boot)
├── database/       -> script.sql (criacao do banco MySQL)
├── frontend/        -> HTML + CSS + JavaScript puro (sem framework)
└── README.md
```

---

## Sprint 2 - RF02: Autenticacao e controle de acesso por perfil

### O que foi implementado

- **Login real**: `POST /api/auth/login` recebe e-mail/senha, valida no
  banco (senha comparada com o hash BCrypt) e devolve um **token de
  sessao**.
- **Token de sessao em memoria**: para manter a sprint simples de
  explicar, os tokens sao guardados em um `Map` dentro do backend
  (`TokenService`), em vez de usar JWT ou uma tabela de sessoes no
  banco. Se o backend reiniciar, todo mundo precisa logar de novo -
  o que e aceitavel para fins didaticos.
- **Interceptor de autenticacao** (`AutenticacaoInterceptor`): roda
  antes de qualquer controller em `/api/**` (exceto `/api/auth/**`).
  Ele exige o header `Authorization: Bearer <token>`, confere se o
  token e valido e se o usuario continua ativo.
- **Controle de acesso por perfil** (regras 1 e 2 do RF02): as rotas
  `/api/usuarios/**` e `/api/roles/**` (gestao de usuarios e perfis)
  agora exigem perfil `ROLE_ADMIN`. Qualquer outro perfil recebe
  `403 Forbidden` com uma mensagem clara.
- **Logout**: `POST /api/auth/logout` invalida o token.
- **Sessao persistida no navegador**: o frontend guarda o token no
  `localStorage` e usa `GET /api/auth/me` para restaurar a sessao se
  a pagina for recarregada (o token continua sendo validado no
  backend a cada chamada).
- **Menus diferentes por perfil** (criterio de aceite 1 do RF02): ao
  logar como Administrador, aparece a tela de "Gestao de usuarios".
  Ao logar com qualquer outro perfil, aparece um painel simples,
  com um botao para **demonstrar** que a area de administracao fica
  bloqueada mesmo tentando acessar via API diretamente (nao so
  escondida no menu).

### O que mudou em relacao a Sprint 1

- O endpoint provisorio `POST /api/usuarios/login` (criado so para
  demonstrar o criterio 3 do RF01) foi **removido** e substituido
  pelo login de verdade em `POST /api/auth/login`.
- Todas as chamadas do frontend para `/api/usuarios` e `/api/roles`
  agora enviam o header `Authorization: Bearer <token>`.

### Novos arquivos do backend

| Arquivo | Papel |
|---|---|
| `security/TokenService.java` | Gera, valida e invalida os tokens de sessao (em memoria) |
| `security/AutenticacaoInterceptor.java` | Bloqueia requisicoes sem token valido ou com perfil sem permissao |
| `security/WebConfig.java` | Registra o interceptor nas rotas `/api/**` |
| `controller/AuthController.java` | Endpoints `/api/auth/login`, `/api/auth/logout`, `/api/auth/me` |
| `dto/LoginResponseDTO.java` | Resposta do login: token + dados do usuario |
| `model/PasswordResetToken.java` | Codigo de redefinicao de senha, com validade e vinculo ao usuario |
| `repository/PasswordResetTokenRepository.java` | Busca do codigo de redefinicao pelo valor recebido |
| `service/EmailService.java` | Envio de e-mail via SMTP (Gmail), usado na redefinicao de senha |
| `service/PasswordResetService.java` | Regras da redefinicao: gerar codigo, enviar e-mail, validar e trocar a senha |
| `controller/PasswordResetController.java` | Endpoints `/api/auth/esqueci-senha`, `/api/auth/redefinir-senha` |
| `dto/EsqueciSenhaRequestDTO.java` / `dto/RedefinirSenhaRequestDTO.java` | Corpo esperado pelos dois endpoints acima |

### Endpoints da API

| Metodo | Rota | Protegido? | Descricao |
|--------|------|------------|-----------|
| POST   | `/api/auth/login`  | Nao | Login (e-mail + senha) -> devolve token |
| POST   | `/api/auth/logout` | Nao* | Invalida o token enviado |
| GET    | `/api/auth/me`     | Nao* | Devolve os dados do usuario dono do token |
| GET    | `/api/usuarios`               | Sim (ROLE_ADMIN) | Lista todos os usuarios |
| GET    | `/api/usuarios/{id}`           | Sim (ROLE_ADMIN) | Busca um usuario por id |
| POST   | `/api/usuarios`                | Sim (ROLE_ADMIN) | Cadastra um novo usuario (nasce inativo) |
| PUT    | `/api/usuarios/{id}`           | Sim (ROLE_ADMIN) | Edita um usuario existente |
| PATCH  | `/api/usuarios/{id}/inativar`  | Sim (ROLE_ADMIN) | Inativa um usuario |
| PATCH  | `/api/usuarios/{id}/ativar`    | Sim (ROLE_ADMIN) | Reativa um usuario |
| GET    | `/api/roles`                   | Sim (ROLE_ADMIN) | Lista os perfis de acesso cadastrados |
| POST   | `/api/auth/esqueci-senha`      | Nao | Recebe um e-mail e, se existir e estiver ativo, envia um codigo de redefinicao |
| POST   | `/api/auth/redefinir-senha`    | Nao | Recebe o codigo recebido por e-mail + a nova senha, e efetiva a troca |

\* `/api/auth/**` fica fora do interceptor (precisa ficar acessivel
sem token para o login acontecer), mas `logout`/`me` exigem o token
no header mesmo assim - so nao passam pela checagem de perfil.

Exemplo de login:

```
POST /api/auth/login
{
  "email": "admin@b2b.com.br",
  "senha": "admin123"
}
```

Resposta:

```json
{
  "token": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
  "usuario": {
    "id": 1,
    "nome": "Administrador",
    "email": "admin@b2b.com.br",
    "perfil": { "id": 1, "nome": "ROLE_ADMIN" },
    "ativo": true
  }
}
```

Esse token deve ir no header de todas as chamadas seguintes:

```
Authorization: Bearer 3f2504e0-4f89-11d3-9a0c-0305e82c3301
```

---

## Redefinicao de senha ("Esqueci minha senha")

Fluxo completo, do ponto de vista de quem usa o sistema:

1. O Administrador cadastra o usuario e escolhe o perfil de acesso (RF01)
   e depois ativa esse usuario.
2. Na tela de login, o usuario clica em "Esqueci minha senha" e informa
   o proprio e-mail.
3. O backend gera um codigo (8 caracteres, valido por 30 minutos) e manda
   por e-mail via `EmailService`.
4. O usuario volta pra mesma tela, informa esse codigo e a nova senha (do
   jeito que ele preferir) e confirma. A senha ja sai criptografada com
   BCrypt, igual ao cadastro feito pelo Administrador.

A resposta do `POST /api/auth/esqueci-senha` e sempre a mesma mensagem de
sucesso, exista ou nao aquele e-mail no banco — isso evita que alguem
descubra quais e-mails estao cadastrados so tentando varios.

**Atencao com a senha de app do Gmail**: o `application.properties`
guarda uma senha de aplicativo real do Gmail (`spring.mail.password`).
Antes de subir esse arquivo pro Git, vale considerar mover esse valor pra
uma variavel de ambiente (ou usar um `application-local.properties` fora
do controle de versao) - senha de verdade nao deveria ficar exposta num
repositorio, mesmo sendo um projeto academico.

---

## Como rodar

**1. Banco de dados**

Nao e obrigatorio rodar o `database/script.sql` manualmente: a
propriedade `spring.jpa.hibernate.ddl-auto=update` (em
`application.properties`) ja cria o banco `sistema_b2b` e as tabelas
`roles` e `usuarios` automaticamente na primeira execucao. O RF02 nao
adicionou nenhuma tabela nova.

Ajuste usuario/senha do MySQL em:
`backend/src/main/resources/application.properties`

**2. Backend**

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Na primeira execucao, os 4
perfis e um usuario administrador de teste sao criados
automaticamente:
- E-mail: `admin@b2b.com.br`
- Senha: `admin123`

**Atencao (erro comum)**: se aparecer "Port 8080 was already in use",
tem outra instancia do backend rodando. Pare todas antes de subir de
novo (no Eclipse, confira se so tem um processo ativo).

**3. Frontend**

Basta abrir o arquivo `frontend/index.html` diretamente no navegador
(duplo clique) com o backend rodando.

---

## Proximos passos (sprints futuras)

RF03 em diante (categorias de produtos, produtos, estoque, tabelas de
preco, etc.) serao implementados nas proximas sprints, conforme a
demanda for definida. Cada novo perfil (Vendedor, Cliente, Expedicao)
ganhara suas proprias telas dentro do "painel nao-admin" a medida que
essas funcionalidades forem entrando.
