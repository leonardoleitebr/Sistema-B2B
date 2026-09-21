-- =============================================================
-- Sistema B2B para Distribuidora de Bebidas
-- Script de referencia com todas as tabelas do sistema, agrupadas
-- por sprint/RF conforme o Documento de Requisitos (Lab Inovacao IV).
-- Banco: MySQL
-- =============================================================
-- Este script e opcional para quem quiser criar o banco manualmente.
-- Se preferir, basta rodar a aplicacao Spring Boot: a propriedade
-- spring.jpa.hibernate.ddl-auto=update ja cria o banco e as tabelas
-- automaticamente na primeira execucao, e o DataSeeder ja cadastra os
-- perfis, os usuarios de teste e alguns dados de demonstracao
-- (categorias, produtos, tabela de precos). Ver README.md.
-- =============================================================

CREATE DATABASE IF NOT EXISTS sistema_b2b
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE sistema_b2b;

-- -------------------------------------------------------------
-- RF01 - roles (perfis de acesso)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(50) NOT NULL,

    CONSTRAINT uk_roles_nome UNIQUE (nome)
);

-- -------------------------------------------------------------
-- RF03 - categorias de produtos
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(100) NOT NULL,

    CONSTRAINT uk_categorias_nome UNIQUE (nome)
);

-- -------------------------------------------------------------
-- RF06 - tabelas de precos personalizadas
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tabelas_precos (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(100) NOT NULL,

    CONSTRAINT uk_tabelas_precos_nome UNIQUE (nome)
);

-- -------------------------------------------------------------
-- RF01 - usuarios
-- RF06 - tabela_preco_id (so usado quando role = ROLE_CLIENTE)
-- RF10 - vendedor_id: vendedor responsavel pelo cliente (so usado
--        quando role = ROLE_CLIENTE); limite_desconto_percentual:
--        desconto maximo que o vendedor pode aplicar (so usado
--        quando role = ROLE_VENDEDOR)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome                        VARCHAR(150) NOT NULL,
    email                       VARCHAR(150) NOT NULL,
    senha                       VARCHAR(255) NOT NULL,          -- hash BCrypt
    ativo                       BOOLEAN NOT NULL DEFAULT FALSE, -- novo usuario nasce inativo
    role_id                     BIGINT NOT NULL,
    tabela_preco_id             BIGINT NULL,
    vendedor_id                 BIGINT NULL,
    limite_desconto_percentual  DECIMAL(5,2) NOT NULL DEFAULT 0,
    data_criacao                DATETIME NULL,

    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT fk_usuarios_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_usuarios_tabela_preco FOREIGN KEY (tabela_preco_id) REFERENCES tabelas_precos (id),
    CONSTRAINT fk_usuarios_vendedor FOREIGN KEY (vendedor_id) REFERENCES usuarios (id)
);

-- -------------------------------------------------------------
-- RF02 - codigo de redefinicao de senha por e-mail
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    token           VARCHAR(20) NOT NULL,
    usuario_id      BIGINT NOT NULL,
    data_expiracao  DATETIME NOT NULL,
    usado           BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_password_reset_token UNIQUE (token),
    CONSTRAINT fk_password_reset_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

-- -------------------------------------------------------------
-- RF04/RF05 - produtos e controle de estoque
-- quantidade_estoque    = total fisico em estoque
-- quantidade_reservada  = comprometido com pedidos ja aprovados (mas
--                         ainda nao enviados); a baixa real so ocorre
--                         no envio (RF14)
-- estoque_minimo        = limite para o alerta de estoque baixo (RF15)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produtos (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome                  VARCHAR(150) NOT NULL,
    categoria_id          BIGINT NOT NULL,
    unidade_venda         VARCHAR(50) NOT NULL,
    preco_padrao          DECIMAL(12,2) NOT NULL,
    quantidade_estoque    INT NOT NULL DEFAULT 0,
    quantidade_reservada  INT NOT NULL DEFAULT 0,
    estoque_minimo        INT NOT NULL DEFAULT 0,
    destaque              BOOLEAN NOT NULL DEFAULT FALSE,
    ativo                 BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_produtos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id)
);

-- -------------------------------------------------------------
-- RF06 - preco de um produto especifico dentro de uma tabela de precos
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS itens_tabela_preco (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    tabela_preco_id   BIGINT NOT NULL,
    produto_id        BIGINT NOT NULL,
    preco             DECIMAL(12,2) NOT NULL,

    CONSTRAINT uk_item_tabela_produto UNIQUE (tabela_preco_id, produto_id),
    CONSTRAINT fk_item_tabela_preco FOREIGN KEY (tabela_preco_id) REFERENCES tabelas_precos (id),
    CONSTRAINT fk_item_tabela_produto FOREIGN KEY (produto_id) REFERENCES produtos (id)
);

-- -------------------------------------------------------------
-- RF08/RF09/RF10/RF11 - pedidos
-- vendedor_id preenchido somente quando o pedido foi lancado por um
-- vendedor em nome do cliente (RF10); status segue o fluxo oficial:
-- RASCUNHO, AGUARDANDO_APROVACAO, APROVADO, EM_SEPARACAO, DIVERGENCIA,
-- ENVIADO, CONCLUIDO, CANCELADO.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedidos (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id            BIGINT NOT NULL,
    vendedor_id           BIGINT NULL,
    status                VARCHAR(30) NOT NULL,
    valor_total           DECIMAL(12,2) NOT NULL,
    desconto_percentual   DECIMAL(5,2) NOT NULL DEFAULT 0,
    data_criacao          DATETIME NOT NULL,
    data_atualizacao      DATETIME NOT NULL,

    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id),
    CONSTRAINT fk_pedidos_vendedor FOREIGN KEY (vendedor_id) REFERENCES usuarios (id)
);

-- -------------------------------------------------------------
-- RF08 - itens de um pedido (preco unitario aplicado fica congelado
-- no momento da criacao do pedido)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS itens_pedido (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id                 BIGINT NOT NULL,
    produto_id                BIGINT NOT NULL,
    quantidade                INT NOT NULL,
    preco_unitario_aplicado   DECIMAL(12,2) NOT NULL,
    subtotal                  DECIMAL(12,2) NOT NULL,

    CONSTRAINT fk_itens_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id),
    CONSTRAINT fk_itens_pedido_produto FOREIGN KEY (produto_id) REFERENCES produtos (id)
);

-- -------------------------------------------------------------
-- RF11 R3 - toda mudanca de status do pedido fica registrada
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedido_status_historico (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id   BIGINT NOT NULL,
    status      VARCHAR(30) NOT NULL,
    data_hora   DATETIME NOT NULL,
    observacao  VARCHAR(255) NULL,

    CONSTRAINT fk_historico_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
);

-- -------------------------------------------------------------
-- RF14 - divergencias registradas pela Expedicao durante a separacao
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS divergencias (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id      BIGINT NOT NULL,
    descricao      VARCHAR(255) NOT NULL,
    data_registro  DATETIME NOT NULL,

    CONSTRAINT fk_divergencias_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
);

-- -------------------------------------------------------------
-- RF09/RF15 - constantes de negocio configuraveis pelo Administrador
-- (linha unica, id fixo = 1): valor de pedido minimo e o numero de
-- dias sem comprar usado para sinalizar clientes inativos no dashboard.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS configuracoes (
    id                         BIGINT PRIMARY KEY,
    valor_pedido_minimo        DECIMAL(12,2) NOT NULL,
    dias_inatividade_cliente   INT NOT NULL
);

-- -------------------------------------------------------------
-- Perfis de acesso do documento de requisitos
-- -------------------------------------------------------------
INSERT IGNORE INTO roles (nome) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_VENDEDOR'),
    ('ROLE_CLIENTE'),
    ('ROLE_EXPEDICAO');

-- -------------------------------------------------------------
-- Usuarios e dados de demonstracao
-- -------------------------------------------------------------
-- Nao ha INSERTs manuais aqui porque as senhas precisam ser gravadas
-- com hash BCrypt (o mesmo algoritmo usado pelo backend para validar
-- o login). Ao rodar a aplicacao Spring Boot pela primeira vez, a
-- classe DataSeeder cria automaticamente os usuarios de teste (ja
-- ativos) e alguns dados de exemplo de catalogo. Ver README.md para
-- a lista completa de logins.
