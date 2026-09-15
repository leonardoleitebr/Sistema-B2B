// URLs base da API do backend (Spring Boot)
const API_URL = "http://localhost:8080/api/usuarios";
const ROLES_URL = "http://localhost:8080/api/roles";
const AUTH_URL = "http://localhost:8080/api/auth";

// ==================== ELEMENTOS - LOGIN / SESSAO ====================
const secaoLogin = document.getElementById("secaoLogin");
const areaLogada = document.getElementById("areaLogada");
const formLogin = document.getElementById("formLogin");
const mensagemLogin = document.getElementById("mensagemLogin");
const nomeUsuarioLogado = document.getElementById("nomeUsuarioLogado");
const perfilUsuarioLogado = document.getElementById("perfilUsuarioLogado");
const btnSair = document.getElementById("btnSair");

// ==================== ELEMENTOS - REDEFINIR SENHA ====================
const linkEsqueciSenha = document.getElementById("linkEsqueciSenha");
const linkVoltarLogin = document.getElementById("linkVoltarLogin");
const secaoRedefinirSenha = document.getElementById("secaoRedefinirSenha");
const passoSolicitarCodigo = document.getElementById("passoSolicitarCodigo");
const passoDefinirNovaSenha = document.getElementById("passoDefinirNovaSenha");
const formSolicitarCodigo = document.getElementById("formSolicitarCodigo");
const formDefinirNovaSenha = document.getElementById("formDefinirNovaSenha");
const mensagemRedefinirSenha = document.getElementById("mensagemRedefinirSenha");

const painelAdmin = document.getElementById("painelAdmin");
const painelNaoAdmin = document.getElementById("painelNaoAdmin");
const btnTestarAcessoRestrito = document.getElementById("btnTestarAcessoRestrito");
const mensagemTesteAcesso = document.getElementById("mensagemTesteAcesso");

// ==================== ELEMENTOS - GESTAO DE USUARIOS (admin) ====================
const formUsuario = document.getElementById("formUsuario");
const tituloFormulario = document.getElementById("tituloFormulario");
const usuarioIdInput = document.getElementById("usuarioId");
const nomeInput = document.getElementById("nome");
const emailInput = document.getElementById("email");
const senhaInput = document.getElementById("senha");
const perfilInput = document.getElementById("perfil");
const btnCancelar = document.getElementById("btnCancelar");
const mensagemFormulario = document.getElementById("mensagemFormulario");
const tabelaUsuariosBody = document.querySelector("#tabelaUsuarios tbody");

// Nomes amigaveis para os perfis padrao (ROLE_ADMIN, ROLE_VENDEDOR, ...).
const NOME_AMIGAVEL_PERFIL = {
    ROLE_ADMIN: "Administrador",
    ROLE_VENDEDOR: "Vendedor",
    ROLE_CLIENTE: "Cliente empresarial",
    ROLE_EXPEDICAO: "Expedicao"
};

function nomeAmigavelPerfil(perfil) {
    if (!perfil) return "-";
    return NOME_AMIGAVEL_PERFIL[perfil.nome] || perfil.nome;
}

// ==================== SESSAO (token guardado no navegador) ====================
// Guardamos o token no localStorage so para o usuario nao precisar logar
// de novo a cada F5. Isso e normal para uma aplicacao web real (diferente
// de um artifact isolado) - o token continua sendo validado no backend
// a cada requisicao.
function salvarSessao(token, usuario) {
    localStorage.setItem("b2b_token", token);
    localStorage.setItem("b2b_usuario", JSON.stringify(usuario));
}

function limparSessao() {
    localStorage.removeItem("b2b_token");
    localStorage.removeItem("b2b_usuario");
}

function obterToken() {
    return localStorage.getItem("b2b_token");
}

function obterUsuarioSalvo() {
    const dado = localStorage.getItem("b2b_usuario");
    return dado ? JSON.parse(dado) : null;
}

function cabecalhosAutenticados() {
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${obterToken()}`
    };
}

// ==================== INICIALIZACAO ====================

document.addEventListener("DOMContentLoaded", async () => {
    const token = obterToken();
    if (!token) {
        mostrarTelaLogin();
        return;
    }

    // Confirma no backend se o token ainda e valido (GET /api/auth/me)
    try {
        const resposta = await fetch(`${AUTH_URL}/me`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!resposta.ok) {
            limparSessao();
            mostrarTelaLogin();
            return;
        }

        const usuario = await resposta.json();
        salvarSessao(token, usuario);
        entrarNaAreaLogada(usuario);
    } catch (erro) {
        console.error("Erro ao validar sessao:", erro);
        mostrarTelaLogin();
    }
});

function mostrarTelaLogin() {
    secaoLogin.style.display = "block";
    secaoRedefinirSenha.style.display = "none";
    areaLogada.style.display = "none";
}

function entrarNaAreaLogada(usuario) {
    secaoLogin.style.display = "none";
    areaLogada.style.display = "block";

    nomeUsuarioLogado.textContent = usuario.nome;
    perfilUsuarioLogado.textContent = nomeAmigavelPerfil(usuario.perfil);

    const ehAdmin = usuario.perfil && usuario.perfil.nome === "ROLE_ADMIN";
    painelAdmin.style.display = ehAdmin ? "block" : "none";
    painelNaoAdmin.style.display = ehAdmin ? "none" : "block";

    if (ehAdmin) {
        carregarPerfis();
        carregarUsuarios();
    }
}

// ==================== LOGIN / LOGOUT ====================

formLogin.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const corpo = {
        email: document.getElementById("loginEmail").value,
        senha: document.getElementById("loginSenha").value
    };

    try {
        const resposta = await fetch(`${AUTH_URL}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(corpo)
        });

        const dados = await resposta.json();

        if (!resposta.ok) {
            exibirMensagem(mensagemLogin, dados.mensagem || "Nao foi possivel entrar.", "erro");
            return;
        }

        salvarSessao(dados.token, dados.usuario);
        formLogin.reset();
        mensagemLogin.textContent = "";
        entrarNaAreaLogada(dados.usuario);
    } catch (erro) {
        console.error("Erro ao fazer login:", erro);
        exibirMensagem(mensagemLogin, "Nao foi possivel conectar ao backend.", "erro");
    }
});

btnSair.addEventListener("click", async () => {
    try {
        await fetch(`${AUTH_URL}/logout`, {
            method: "POST",
            headers: { Authorization: `Bearer ${obterToken()}` }
        });
    } catch (erro) {
        console.error("Erro ao sair:", erro);
    } finally {
        limparSessao();
        mostrarTelaLogin();
    }
});

// ==================== ESQUECI MINHA SENHA ====================
// Fluxo: o Administrador cadastra o usuario e escolhe o perfil (RF01/tela
// "Cadastrar usuario"); depois, o proprio usuario usa esta tela para
// definir a senha do jeito que preferir, sem depender do Administrador.

linkEsqueciSenha.addEventListener("click", (evento) => {
    evento.preventDefault();
    secaoLogin.style.display = "none";
    secaoRedefinirSenha.style.display = "block";

    // Sempre comeca pelo passo 1 (informar o e-mail)
    passoSolicitarCodigo.style.display = "block";
    passoDefinirNovaSenha.style.display = "none";
    mensagemRedefinirSenha.textContent = "";
    formSolicitarCodigo.reset();
    formDefinirNovaSenha.reset();
});

linkVoltarLogin.addEventListener("click", (evento) => {
    evento.preventDefault();
    secaoRedefinirSenha.style.display = "none";
    secaoLogin.style.display = "block";
});

// Passo 1: usuario informa o e-mail e recebe um codigo por e-mail
formSolicitarCodigo.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const email = document.getElementById("emailRedefinicao").value;

    try {
        const resposta = await fetch(`${AUTH_URL}/esqueci-senha`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });

        const dados = await resposta.json();

        if (!resposta.ok) {
            exibirMensagem(mensagemRedefinirSenha, dados.mensagem || "Nao foi possivel enviar o codigo.", "erro");
            return;
        }

        exibirMensagem(mensagemRedefinirSenha, dados.mensagem, "sucesso");

        // Avanca para o passo 2. A mensagem generica do backend ja evita
        // revelar se o e-mail existe ou nao, entao sempre avancamos.
        passoSolicitarCodigo.style.display = "none";
        passoDefinirNovaSenha.style.display = "block";
    } catch (erro) {
        console.error("Erro ao solicitar codigo de redefinicao:", erro);
        exibirMensagem(mensagemRedefinirSenha, "Nao foi possivel conectar ao backend.", "erro");
    }
});

// Passo 2: usuario informa o codigo recebido e a nova senha (a sua escolha)
formDefinirNovaSenha.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const corpo = {
        token: document.getElementById("codigoRedefinicao").value.trim(),
        novaSenha: document.getElementById("novaSenhaRedefinicao").value
    };

    try {
        const resposta = await fetch(`${AUTH_URL}/redefinir-senha`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(corpo)
        });

        const dados = await resposta.json();

        if (!resposta.ok) {
            exibirMensagem(mensagemRedefinirSenha, dados.mensagem || "Nao foi possivel redefinir a senha.", "erro");
            return;
        }

        exibirMensagem(mensagemRedefinirSenha, dados.mensagem, "sucesso");
        formSolicitarCodigo.reset();
        formDefinirNovaSenha.reset();

        // Depois de alguns segundos, volta sozinho para a tela de login
        setTimeout(() => {
            secaoRedefinirSenha.style.display = "none";
            secaoLogin.style.display = "block";
            passoSolicitarCodigo.style.display = "block";
            passoDefinirNovaSenha.style.display = "none";
        }, 2500);
    } catch (erro) {
        console.error("Erro ao redefinir senha:", erro);
        exibirMensagem(mensagemRedefinirSenha, "Nao foi possivel conectar ao backend.", "erro");
    }
});

// ==================== TESTE DE ACESSO RESTRITO (perfis nao-admin) ====================

btnTestarAcessoRestrito.addEventListener("click", async () => {
    try {
        const resposta = await fetch(API_URL, { headers: cabecalhosAutenticados() });
        const dados = await resposta.json();

        if (!resposta.ok) {
            exibirMensagem(
                mensagemTesteAcesso,
                `Bloqueado como esperado (HTTP ${resposta.status}): ${dados.mensagem}`,
                "erro"
            );
        } else {
            exibirMensagem(mensagemTesteAcesso, "Acesso permitido (nao deveria acontecer).", "sucesso");
        }
    } catch (erro) {
        console.error("Erro ao testar acesso restrito:", erro);
    }
});

// ==================== PERFIS (ROLES) ====================

async function carregarPerfis() {
    try {
        const resposta = await fetch(ROLES_URL, { headers: cabecalhosAutenticados() });
        const perfis = await resposta.json();

        perfilInput.innerHTML = '<option value="">Selecione...</option>';
        perfis.forEach((perfil) => {
            const opcao = document.createElement("option");
            opcao.value = perfil.id;
            opcao.textContent = nomeAmigavelPerfil(perfil);
            perfilInput.appendChild(opcao);
        });
    } catch (erro) {
        console.error("Erro ao carregar perfis:", erro);
        exibirMensagem(mensagemFormulario, "Nao foi possivel carregar os perfis de acesso.", "erro");
    }
}

// ==================== LISTAGEM DE USUARIOS ====================

async function carregarUsuarios() {
    try {
        const resposta = await fetch(API_URL, { headers: cabecalhosAutenticados() });

        if (resposta.status === 401) {
            limparSessao();
            mostrarTelaLogin();
            return;
        }

        const usuarios = await resposta.json();
        renderizarTabela(usuarios);
    } catch (erro) {
        console.error("Erro ao carregar usuarios:", erro);
        exibirMensagem(mensagemFormulario, "Nao foi possivel conectar ao backend.", "erro");
    }
}

function renderizarTabela(usuarios) {
    tabelaUsuariosBody.innerHTML = "";

    usuarios.forEach((usuario) => {
        const linha = document.createElement("tr");

        const statusClasse = usuario.ativo ? "status-ativo" : "status-inativo";
        const statusTexto = usuario.ativo ? "Ativo" : "Inativo";

        linha.innerHTML = `
            <td>${usuario.id}</td>
            <td>${usuario.nome}</td>
            <td>${usuario.email}</td>
            <td>${nomeAmigavelPerfil(usuario.perfil)}</td>
            <td class="${statusClasse}">${statusTexto}</td>
            <td class="acoes">
                <button onclick="editarUsuario(${usuario.id})">Editar</button>
                ${usuario.ativo
                    ? `<button onclick="inativarUsuario(${usuario.id})">Inativar</button>`
                    : `<button onclick="ativarUsuario(${usuario.id})">Ativar</button>`}
            </td>
        `;

        tabelaUsuariosBody.appendChild(linha);
    });
}

// ==================== CADASTRO / EDICAO ====================

formUsuario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const id = usuarioIdInput.value;
    const corpo = {
        nome: nomeInput.value,
        email: emailInput.value,
        senha: senhaInput.value,
        perfil: { id: Number(perfilInput.value) }
    };

    const editando = Boolean(id);
    const url = editando ? `${API_URL}/${id}` : API_URL;
    const metodo = editando ? "PUT" : "POST";

    try {
        const resposta = await fetch(url, {
            method: metodo,
            headers: cabecalhosAutenticados(),
            body: JSON.stringify(corpo)
        });

        const dados = await resposta.json();

        if (!resposta.ok) {
            exibirMensagem(mensagemFormulario, dados.mensagem || "Erro ao salvar usuario.", "erro");
            return;
        }

        exibirMensagem(
            mensagemFormulario,
            editando
                ? "Usuario atualizado com sucesso!"
                : "Usuario cadastrado com sucesso! Ele fica inativo ate ser ativado na lista abaixo.",
            "sucesso"
        );
        limparFormulario();
        carregarUsuarios();
    } catch (erro) {
        console.error("Erro ao salvar usuario:", erro);
        exibirMensagem(mensagemFormulario, "Nao foi possivel conectar ao backend.", "erro");
    }
});

async function editarUsuario(id) {
    try {
        const resposta = await fetch(`${API_URL}/${id}`, { headers: cabecalhosAutenticados() });
        const usuario = await resposta.json();

        usuarioIdInput.value = usuario.id;
        nomeInput.value = usuario.nome;
        emailInput.value = usuario.email;
        senhaInput.value = "";
        senhaInput.placeholder = "Deixe em branco para manter a senha atual";
        perfilInput.value = usuario.perfil ? usuario.perfil.id : "";

        tituloFormulario.textContent = `Editando usuario #${usuario.id}`;
        btnCancelar.style.display = "inline-block";
        window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (erro) {
        console.error("Erro ao buscar usuario:", erro);
    }
}

btnCancelar.addEventListener("click", limparFormulario);

function limparFormulario() {
    formUsuario.reset();
    usuarioIdInput.value = "";
    senhaInput.placeholder = "Obrigatorio no cadastro";
    tituloFormulario.textContent = "Cadastrar usuario";
    btnCancelar.style.display = "none";
}

// ==================== ATIVAR / INATIVAR ====================

async function inativarUsuario(id) {
    if (!confirm("Deseja realmente inativar este usuario?")) return;
    await alterarStatus(id, "inativar");
}

async function ativarUsuario(id) {
    await alterarStatus(id, "ativar");
}

async function alterarStatus(id, acao) {
    try {
        await fetch(`${API_URL}/${id}/${acao}`, {
            method: "PATCH",
            headers: cabecalhosAutenticados()
        });
        carregarUsuarios();
    } catch (erro) {
        console.error(`Erro ao ${acao} usuario:`, erro);
    }
}

// ==================== UTIL ====================

function exibirMensagem(elemento, texto, tipo) {
    elemento.textContent = texto;
    elemento.className = `mensagem ${tipo}`;
}
