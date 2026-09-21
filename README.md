# Sistema B2B - Distribuidora de Bebidas

Sistema de vendas B2B para uma distribuidora de bebidas: cadastro de usuarios
e perfis de acesso, catalogo com precos personalizados por cliente, controle
de estoque, pedidos com fluxo de aprovacao e expedicao, e um dashboard de
indicadores. Projeto academico (Lab Inovacao IV), evoluido de acordo com o
Documento de Requisitos.

**Perfis de acesso**: Administrador, Vendedor, Cliente empresarial e Expedicao
— cada um com seu proprio conjunto de telas e permissoes.

## Estrutura de pastas

```
sistema-b2b/
├── backend/        -> API REST em Java 17 + Spring Boot 3.2.5
├── database/       -> script.sql (referencia do schema MySQL)
├── frontend/       -> HTML + CSS + JavaScript puro (sem framework, sem build)
└── README.md
```

---

## Pre-requisitos

- **Java 17** (JDK)
- **Maven** 3.8+ (ou use o `mvnw`/`mvnw.cmd` incluido no projeto, se houver)
- **MySQL** 8.x rodando localmente (ou acessivel pela rede)
- Um navegador atual (Chrome, Edge, Firefox) para o frontend
- Nao ha dependencias de frontend para instalar (JS puro, sem `npm`)

## Instalacao

O backend usa Maven, que resolve as dependencias automaticamente ao compilar
ou rodar — nao ha um passo manual de "instalar dependencias" separado.

```bash
cd backend
mvn install
```

## Configuracao de ambiente

Ajuste o acesso ao banco e ao e-mail em
`backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sistema_b2b?createDatabaseIfNotExist=true
spring.datasource.username=SEU_USUARIO_MYSQL
spring.datasource.password=SUA_SENHA_MYSQL

spring.mail.username=SEU_EMAIL@gmail.com
spring.mail.password=SUA_SENHA_DE_APP_DO_GMAIL
```

- `spring.datasource.*`: usuario/senha do seu MySQL local.
- `spring.mail.*`: conta usada para enviar o e-mail de redefinicao de senha
  (RF02). Precisa ser uma **senha de app** do Gmail (Conta Google > Seguranca
  > Verificacao em duas etapas > Senhas de app), nao a senha normal da conta.

**Atencao**: nao suba credenciais reais para o Git. O ideal e mover esses
valores para variaveis de ambiente (`${DB_PASSWORD}`, `${MAIL_PASSWORD}`, por
exemplo, lidos via `application.properties`) ou usar um
`application-local.properties` fora do controle de versao. Se uma senha de
app do Gmail real ja foi commitada em algum momento, revogue-a e gere uma
nova em https://myaccount.google.com/apppasswords.

## Banco de dados

Nao e obrigatorio rodar `database/script.sql` manualmente:
`spring.jpa.hibernate.ddl-auto=update` (ja configurado) cria o banco
`sistema_b2b` e todas as tabelas automaticamente na primeira execucao do
backend. O script em `database/` existe apenas como referencia documentada
do schema, para quem preferir criar/inspecionar o banco manualmente.

Na primeira execucao, a classe `DataSeeder` tambem cadastra automaticamente:
- os 4 perfis de acesso (`ROLE_ADMIN`, `ROLE_VENDEDOR`, `ROLE_CLIENTE`, `ROLE_EXPEDICAO`);
- um usuario de teste para cada perfil (ja ativos — ver "Como acessar");
- algumas categorias, produtos e uma tabela de precos de demonstracao, para
  o catalogo nao comecar vazio.

## Como executar

**Backend**

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

**Frontend**

Basta abrir `frontend/index.html` diretamente no navegador (duplo clique),
com o backend rodando. Nao precisa de servidor web nem de build — e
HTML/CSS/JS puro.

## Como acessar

- **API**: `http://localhost:8080/api`
- **Frontend**: arquivo `frontend/index.html`, aberto direto no navegador

Usuarios de teste (criados automaticamente pelo `DataSeeder`, ja ativos):

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | `admin@b2b.com.br` | `admin123` |
| Vendedor (limite de desconto: 5%) | `vendedor@b2b.com.br` | `vendedor123` |
| Cliente (atendido pelo vendedor acima, com a "Tabela Padrao Atacado" vinculada) | `cliente@b2b.com.br` | `cliente123` |
| Expedicao | `expedicao@b2b.com.br` | `expedicao123` |

## Solucao de problemas

| Problema | Causa provavel | Solucao |
|---|---|---|
| `Port 8080 was already in use` | Outra instancia do backend ja esta rodando | Pare o processo anterior antes de subir de novo |
| Erro de conexao com o MySQL ao iniciar | Banco nao esta rodando, ou usuario/senha errados | Confira `spring.datasource.*` em `application.properties` e se o MySQL esta ativo |
| Cadastro de usuario funciona mas o e-mail de redefinicao de senha nao chega | Credenciais de `spring.mail.*` invalidas/nao configuradas | Gere uma nova senha de app do Gmail e atualize `application.properties` |
| Tela em branco / erro de conexao no frontend | Backend nao esta rodando, ou esta em outra porta | Confirme que o backend subiu em `http://localhost:8080` (os arquivos `script.js`/`catalogo.js`/etc. apontam para essa URL fixa) |
| Login funciona mas as telas ficam vazias | Usuario ainda esta inativo | Um Administrador precisa ativa-lo em "Usuarios" antes do primeiro acesso |

---

## Perfis e principais telas

- **Administrador**: Usuarios (RF01), Categorias (RF03), Produtos e estoque
  (RF04/RF05), Tabelas de precos (RF06), Pedidos — aprovar/cancelar/concluir
  (RF11), Dashboard completo (RF15).
- **Vendedor**: Catalogo com preco do cliente selecionado, monta pedido em
  nome de um cliente da sua base com desconto ate o seu limite (RF10), Pedidos
  da sua base, Dashboard restrito aos seus clientes (RF15).
- **Cliente empresarial**: Catalogo com seus precos (RF07), Comprar novamente
  (RF13), Sugestoes para atingir o pedido minimo (RF16), Carrinho e checape
  finalizacao do pedido (RF08/RF09), Meus pedidos — cancelar enquanto aguarda
  aprovacao, repetir pedido (RF11/RF12).
- **Expedicao**: Pedidos aprovados, iniciar separacao, registrar divergencia
  e marcar como enviado (RF14).

## Requisitos atendidos

| Requisito | Descricao | Onde |
|---|---|---|
| RF01 | Cadastro/gestao de usuarios e perfis | `UsuarioController`/`UsuarioService`, tela "Usuarios" |
| RF02 | Autenticacao, controle de acesso por perfil, redefinicao de senha | `AuthController`, `AutenticacaoInterceptor`, `PasswordResetController` |
| RF03 | Categorias de produtos | `CategoriaController`/`CategoriaService`, tela "Categorias" |
| RF04 | Cadastro/gestao de produtos | `ProdutoController`/`ProdutoService`, tela "Produtos" |
| RF05 | Controle de estoque (reserva na aprovacao, baixa no envio, alerta de minimo) | `Produto` (`quantidadeReservada`/`quantidadeEstoque`), `PedidoService` |
| RF06 | Tabelas de precos personalizadas por cliente | `TabelaPrecoController`/`TabelaPrecoService`, tela "Tabelas de Precos" |
| RF07 | Catalogo com preco personalizado | `CatalogoController`/`CatalogoService` |
| RF08 | Cliente monta e finaliza pedido (carrinho) | tela "Catalogo" (carrinho), `POST /api/pedidos` |
| RF09 | Pedido minimo configuravel, com aviso de quanto falta | `ConfiguracaoController`, `PedidoService`, sugestoes (RF16) |
| RF10 | Vendedor lanca pedido em nome do cliente, com limite de desconto | `PedidoController#criarComoVendedor`, `Usuario.limiteDescontoPercentual` |
| RF11 | Aprovacao/cancelamento pelo Administrador, pedidos por status, historico | `PedidoService#aprovar/#cancelar`, `PedidoStatusHistorico` |
| RF12 | Repetir pedido, sempre com precos vigentes | `repetirPedido()` em `pedidos.js` + `repetirPedidoNoCarrinho()` em `catalogo.js` |
| RF13 | Comprar novamente (produtos frequentes) | `CatalogoService#produtosFrequentes`, `GET /api/catalogo/frequentes` |
| RF14 | Expedicao: separacao, divergencia, envio (baixa de estoque) | `PedidoService#iniciarSeparacao/#registrarDivergencia/#enviar` |
| RF15 | Dashboard com indicadores e filtro de periodo | `DashboardController`/`DashboardService`, tela "Dashboard" |
| RF16 | Sugestao de produtos para atingir o pedido minimo | `CatalogoService#sugestoes`, `GET /api/catalogo/sugestoes` |

### Regras de negocio aplicadas

- **RN01** — o preco de um produto para um cliente vem da tabela de precos
  dele (se houver um item definido para aquele produto); caso contrario, usa
  o preco padrao do produto (`CatalogoService#resolverPreco`).
- **RN02** — pedido abaixo do valor minimo nao e aceito; a resposta de erro
  ja informa quanto falta (`PedidoService#montarEPersistirPedido`).
- **RN03** — ao repetir um pedido (RF12), os itens sao adicionados ao
  carrinho com os **precos vigentes**, nunca os precos congelados do pedido
  antigo.
- **RN06** — o desconto que o Vendedor aplica no pedido nunca pode superar o
  `limiteDescontoPercentual` cadastrado para ele (validado no backend, nao so
  no frontend).
- **RN07** — toda divergencia fica sempre vinculada a um pedido (`Divergencia.pedido`).

### Fluxo de status do pedido

```
RASCUNHO -> AGUARDANDO_APROVACAO -> APROVADO -> EM_SEPARACAO -> (DIVERGENCIA) -> ENVIADO -> CONCLUIDO
                    |                    |             |
                    +-------------------------> CANCELADO
```

- O estoque e **reservado** quando o Administrador aprova o pedido, e
  **baixado de verdade** somente quando a Expedicao marca o pedido como
  enviado — assim o estoque disponivel no catalogo sempre reflete o que
  realmente pode ser vendido.
- Cancelamento: o Administrador pode cancelar a qualquer momento (exceto
  pedido ja concluido); o Cliente so pode cancelar enquanto o pedido estiver
  "Aguardando aprovacao".

### Decisoes tomadas onde o documento nao detalhava a implementacao

Pontos em que o requisito descreve o comportamento esperado mas nao a
estrutura de dados/tela exata — decisoes tomadas para manter a solucao
simples e consistente com o resto do sistema, documentadas aqui para o grupo
revisar:

- **"Rascunho" (carrinho ainda nao finalizado)**: o carrinho vive no
  navegador (estado em JavaScript) ate o clique em "Finalizar pedido"; o
  pedido so e criado no backend, ja como `AGUARDANDO_APROVACAO`, nesse
  momento. O status `RASCUNHO` existe no modelo de dados (enum `StatusPedido`)
  para cobrir o fluxo oficial descrito pelo professor, mas nao ha hoje uma
  tela que liste "rascunhos salvos" no backend.
- **Vinculo Cliente-Vendedor (RF10)**: como o cadastro de usuarios continua
  sendo uma funcionalidade exclusiva do Administrador (RF01/Matriz de
  Permissoes), quem define o "vendedor responsavel" de um cliente e o
  Administrador, no proprio formulario de cadastro/edicao de usuario (campo
  visivel so quando o perfil escolhido e Cliente) — o Vendedor nao tem
  permissao para criar usuarios.
- **Pedido minimo e dias de inatividade do cliente (RF09/RF15)**: sao
  constantes configuraveis pelo Administrador via `GET/PUT /api/configuracoes`
  (nao ha tela dedicada a isso no frontend ainda; pode ser feito via API/Postman
  ou adicionando um pequeno formulario depois).
- **Estoque minimo por produto (RF15)**: campo `estoqueMinimo`, definido por
  produto na tela "Produtos", usado para o alerta de estoque baixo no
  dashboard.
- **Produtos em destaque / sugestoes (RF16)**: as sugestoes combinam os
  produtos que o cliente ja compra com frequencia com os produtos marcados
  como "destaque" pelo Administrador (checkbox na tela "Produtos").

---

## Endpoints da API

Todas as rotas (exceto `/api/auth/**`) exigem o header
`Authorization: Bearer <token>`, obtido em `POST /api/auth/login`.

### Usuarios, perfis e autenticacao

| Metodo | Rota | Quem pode | Descricao |
|---|---|---|---|
| POST | `/api/auth/login` | Qualquer um | Login (e-mail + senha) -> token |
| POST | `/api/auth/logout` | Logado | Invalida o token |
| GET | `/api/auth/me` | Logado | Dados do usuario dono do token |
| POST | `/api/auth/esqueci-senha` | Qualquer um | Envia codigo de redefinicao por e-mail |
| POST | `/api/auth/redefinir-senha` | Qualquer um | Efetiva a troca de senha com o codigo |
| GET/POST/PUT | `/api/usuarios`, `/api/usuarios/{id}` | Admin | Gestao de usuarios (RF01) |
| PATCH | `/api/usuarios/{id}/ativar` \| `/inativar` | Admin | Ativa/inativa usuario |
| GET | `/api/roles` | Admin | Lista os perfis de acesso |

### Catalogo (RF03 a RF07)

| Metodo | Rota | Quem pode | Descricao |
|---|---|---|---|
| GET/POST/PUT/DELETE | `/api/categorias`, `/api/categorias/{id}` | Admin | Categorias |
| GET/POST/PUT | `/api/produtos`, `/api/produtos/{id}` | Admin | Produtos e estoque |
| PATCH | `/api/produtos/{id}/ativar` \| `/inativar` | Admin | Ativa/inativa produto |
| GET | `/api/produtos/estoque-baixo` | Admin | Produtos no limite ou abaixo do estoque minimo |
| GET/POST/PUT/DELETE | `/api/tabelas-precos`, `/api/tabelas-precos/{id}` | Admin | Tabelas de precos |
| PUT/DELETE | `/api/tabelas-precos/{id}/itens/{produtoId}` | Admin | Define/remove o preco de um produto na tabela |
| GET | `/api/configuracoes` | Logado | Le o pedido minimo vigente e os dias de inatividade |
| PUT | `/api/configuracoes` | Admin | Altera essas constantes |
| GET | `/api/catalogo?clienteId=` | Cliente/Vendedor/Admin | Catalogo com o preco resolvido para o cliente |
| GET | `/api/catalogo/frequentes?clienteId=` | Cliente/Vendedor/Admin | "Comprar novamente" |
| GET | `/api/catalogo/sugestoes?clienteId=` | Cliente/Vendedor/Admin | Sugestoes para atingir o pedido minimo |
| GET | `/api/vendedor/clientes` | Vendedor | Clientes da base do vendedor logado |

### Pedidos (RF08 a RF14)

| Metodo | Rota | Quem pode | Descricao |
|---|---|---|---|
| GET | `/api/pedidos` | Logado | Lista pedidos (escopo varia por perfil) |
| GET | `/api/pedidos/{id}` | Logado (dono/relacionado) | Detalhe do pedido |
| POST | `/api/pedidos` | Cliente | Finaliza o proprio pedido |
| POST | `/api/pedidos/vendedor` | Vendedor | Lanca pedido em nome de um cliente da base |
| PATCH | `/api/pedidos/{id}/aprovar` | Admin | Aprova (reserva estoque) |
| PATCH | `/api/pedidos/{id}/cancelar` | Admin (sempre) / Cliente (so aguardando aprovacao) | Cancela |
| PATCH | `/api/pedidos/{id}/iniciar-separacao` | Expedicao | Inicia a separacao |
| POST | `/api/pedidos/{id}/divergencias` | Expedicao | Registra divergencia |
| PATCH | `/api/pedidos/{id}/enviar` | Expedicao | Marca como enviado (baixa o estoque) |
| PATCH | `/api/pedidos/{id}/concluir` | Admin/Expedicao | Fecha o pedido apos a entrega |

### Dashboard (RF15)

| Metodo | Rota | Quem pode | Descricao |
|---|---|---|---|
| GET | `/api/dashboard?inicio=&fim=` | Admin/Vendedor | Indicadores do periodo (datas no formato `AAAA-MM-DDTHH:mm:ss`) |

---

## Limitacoes conhecidas / possiveis proximos passos

- Nao ha testes automatizados neste pacote (nem unitarios nem de
  integracao) — as validacoes foram feitas por revisao manual do codigo, ja
  que o ambiente usado para gerar esta entrega nao tem acesso ao Maven
  Central para baixar as dependencias e rodar um build completo. **Antes de
  apresentar, rode `mvn clean verify` (ou pelo menos `mvn spring-boot:run` e
  um teste manual do fluxo completo) no seu ambiente.**
- O "Rascunho" do pedido nao e persistido no backend enquanto o cliente monta
  o carrinho (ver "Decisoes tomadas" acima).
- Pedido minimo e dias de inatividade sao configurados via API
  (`/api/configuracoes`); ainda nao existe uma tela dedicada para isso.
- E-mail de redefinicao de senha depende de uma senha de app do Gmail válida
  configurada em `application.properties` (ver "Configuracao de ambiente").
