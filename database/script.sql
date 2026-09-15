-- =============================================================
-- Sistema B2B para Distribuidora de Bebidas
-- Sprint 1 - RF01: Cadastro e gestao de usuarios e perfis de acesso
-- Sprint 2 - RF02: Autenticacao e controle de acesso por perfil
-- Banco: MySQL
-- =============================================================
-- Este script e opcional para quem quiser criar o banco manualmente.
-- Se preferir, basta rodar a aplicacao Spring Boot: a propriedade
-- spring.jpa.hibernate.ddl-auto=update ja cria o banco e as tabelas
-- automaticamente na primeira execucao (e o DataSeeder ja cadastra
-- os perfis padrao e o usuario administrador de teste).
--
-- O RF02 (login/token de sessao) nao criou nenhuma tabela nova: os
-- tokens de sessao ficam guardados em memoria no backend (ver
-- TokenService), entao nao ha nada a persistir no banco para isso.
-- =============================================================

CREATE DATABASE IF NOT EXISTS sistema_b2b
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE sistema_b2b;

-- -------------------------------------------------------------
-- Tabela: roles (perfis de acesso)
-- Antes era um ENUM fixo dentro de "usuarios"; agora e uma tabela
-- propria, permitindo criar/gerenciar perfis sem alterar codigo.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(50) NOT NULL,

    CONSTRAINT uk_roles_nome UNIQUE (nome)
);

-- -------------------------------------------------------------
-- Tabela: usuarios (RF01)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(150) NOT NULL,
    email    VARCHAR(150) NOT NULL,
    senha    VARCHAR(255) NOT NULL,          -- senha armazenada com hash (BCrypt)
    ativo    BOOLEAN NOT NULL DEFAULT FALSE, -- novo usuario nasce inativo (precisa ser ativado por um admin)
    role_id  BIGINT NOT NULL,                -- FK para roles(id)

    -- Regra 2 do RF01: nao pode haver dois usuarios com o mesmo e-mail/login
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT fk_usuarios_role FOREIGN KEY (role_id) REFERENCES roles (id)
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
-- Usuario administrador de teste
-- -------------------------------------------------------------
-- Nao ha um INSERT manual aqui porque a senha precisa ser gravada
-- com hash BCrypt (o mesmo algoritmo usado pelo backend para validar
-- o login). Ao rodar a aplicacao Spring Boot pela primeira vez, a
-- classe DataSeeder cria automaticamente este usuario de teste,
-- ja ativo e associado ao perfil ROLE_ADMIN:
--   E-mail: admin@b2b.com.br
--   Senha : admin123
