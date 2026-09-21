// URLs base da API do backend (Spring Boot).
// API_BASE e usada pelos demais arquivos (produtos.js, catalogo.js, pedidos.js, dashboard.js).
const API_BASE = "http://localhost:8080/api";
const API_URL = `${API_BASE}/usuarios`;
const ROLES_URL = `${API_BASE}/roles`;
const AUTH_URL = `${API_BASE}/auth`;

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
const btnEnviarCodigo = document.getElementById("btnEnviarCodigo");
const btnRedefinirSenha = document.getElementById("btnRedefinirSenha");
const mensagemRedefinirSenha = document.getElementById("mensagemRedefinirSenha");

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

// RF06/RF10 - campos condicionais conforme o perfil escolhido
const camposCliente = document.getElementById("camposCliente");
const camposVendedor = document.getElementById("camposVendedor");
const campoTabelaPreco = document.getElementById("campoTabelaPreco");
const campoVendedorResponsavel = document.getElementById("campoVendedorResponsavel");
const campoLimiteDesconto = document.getElementById("campoLimiteDesconto");

let perfisCarregados = [];
let usuariosCarregados = [];

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

// ==================== NAVEGACAO ENTRE SECOES ====================
// Cada perfil so enxerga os botoes de navegacao relevantes para ele.
const NAV_POR_PERFIL = {
    ROLE_ADMIN: ["navUsuarios", "navCategorias", "navProdutos", "navTabelasPrecos", "navPedidos", "navDashboard"],
    ROLE_VENDEDOR: ["navCatalogo", "navPedidos", "navDashboard"],
    ROLE_CLIENTE: ["navCatalogo", "navPedidos"],
    ROLE_EXPEDICAO: ["navPedidos"]
};

const TODAS_SECOES = [
    "secaoUsuarios", "secaoCategorias", "secaoProdutos",
    "secaoTabelasPrecos", "secaoCatalogo", "secaoPedidos", "secaoDashboard"
];

// Ao abrir uma secao, dispara um callback opcional registrado pelo arquivo JS
// responsavel por ela (ex.: window.aoAbrir_secaoCatalogo definido em catalogo.js).
// Isso evita que este arquivo precise conhecer os detalhes dos outros modulos.
function mostrarSecao(nomeSecao) {
    TODAS_SECOES.forEach((id) => {
        document.getElementById(id).style.display = id === nomeSecao ? "block" : "none";
    });
    document.querySelectorAll(".nav-btn").forEach((botao) => {
        botao.classList.toggle("ativo", botao.dataset.secao === nomeSecao);
    });

    const callback = window[`aoAbrir_${nomeSecao}`];
    if (typeof callback === "function") {
        callback();
    }
}

document.querySelectorAll(".nav-btn").forEach((botao) => {
    botao.addEventListener("click", () => mostrarSecao(botao.dataset.secao));
});

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

    const perfil = usuario.perfil ? usuario.perfil.nome : null;
    const navsPermitidos = NAV_POR_PERFIL[perfil] || [];

    document.querySelectorAll(".nav-btn").forEach((botao) => {
        botao.style.display = navsPermitidos.includes(botao.id) ? "inline-block" : "none";
    });

    if (perfil === "ROLE_ADMIN") {
        carregarPerfis();
        carregarUsuarios();
        carregarTabelasPrecoParaFormulario();
    }

    const primeiroNav = navsPermitidos.length > 0 ? document.getElementById(navsPermitidos[0]) : null;
    if (primeiroNav) {
        mostrarSecao(primeiroNav.dataset.secao);
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

    // Desabilita o botao assim que o clique acontece: o envio do e-mail
    // (SMTP) pode demorar alguns segundos, e sem isso um clique duplo do
    // usuario dispara duas (ou mais) requisicoes, gerando varios e-mails.
    const textoOriginal = btnEnviarCodigo.textContent;
    btnEnviarCodigo.disabled = true;
    btnEnviarCodigo.textContent = "Enviando...";

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
    } finally {
        // Reabilita o botao tanto no sucesso quanto no erro - se der
        // erro, o usuario pode querer tentar de novo.
        btnEnviarCodigo.disabled = false;
        btnEnviarCodigo.textContent = textoOriginal;
    }
});

// Passo 2: usuario informa o codigo recebido e a nova senha (a sua escolha)
formDefinirNovaSenha.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const novaSenha = document.getElementById("novaSenhaRedefinicao").value;
    const confirmarNovaSenha = document.getElementById("confirmarNovaSenhaRedefinicao").value;

    // Validacao no proprio navegador: evita uma chamada desnecessaria ao
    // backend quando as duas senhas nao coincidem.
    if (novaSenha !== confirmarNovaSenha) {
        exibirMensagem(mensagemRedefinirSenha, "As senhas informadas nao coincidem.", "erro");
        return;
    }

    const corpo = {
        token: document.getElementById("codigoRedefinicao").value.trim(),
        novaSenha: novaSenha
    };

    const textoOriginal = btnRedefinirSenha.textContent;
    btnRedefinirSenha.disabled = true;
    btnRedefinirSenha.textContent = "Redefinindo...";

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
    } finally {
        btnRedefinirSenha.disabled = false;
        btnRedefinirSenha.textContent = textoOriginal;
    }
});

// ==================== PERFIS (ROLES) ====================

async function carregarPerfis() {
    try {
        const resposta = await fetch(ROLES_URL, { headers: cabecalhosAutenticados() });
        perfisCarregados = await resposta.json();

        perfilInput.innerHTML = '<option value="">Selecione...</option>';
        perfisCarregados.forEach((perfil) => {
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

// RF06/RF10 - mostra "Tabela de precos"/"Vendedor responsavel" so para perfil
// Cliente, e "Limite de desconto" so para perfil Vendedor.
function atualizarCamposCondicionais() {
    const perfilSelecionado = perfisCarregados.find((p) => String(p.id) === perfilInput.value);
    const nomePerfil = perfilSelecionado ? perfilSelecionado.nome : null;

    camposCliente.style.display = nomePerfil === "ROLE_CLIENTE" ? "block" : "none";
    camposVendedor.style.display = nomePerfil === "ROLE_VENDEDOR" ? "block" : "none";
}

perfilInput.addEventListener("change", atualizarCamposCondicionais);

async function carregarTabelasPrecoParaFormulario() {
    try {
        const resposta = await fetch(`${API_BASE}/tabelas-precos`, { headers: cabecalhosAutenticados() });
        const tabelas = await resposta.json();

        campoTabelaPreco.innerHTML = '<option value="">Nenhuma (usa o preco padrao dos produtos)</option>';
        tabelas.forEach((tabela) => {
            const opcao = document.createElement("option");
            opcao.value = tabela.id;
            opcao.textContent = tabela.nome;
            campoTabelaPreco.appendChild(opcao);
        });
    } catch (erro) {
        console.error("Erro ao carregar tabelas de precos:", erro);
    }
}

function preencherDropdownVendedores() {
    campoVendedorResponsavel.innerHTML = '<option value="">Nenhum</option>';
    usuariosCarregados
        .filter((usuario) => usuario.perfil && usuario.perfil.nome === "ROLE_VENDEDOR")
        .forEach((vendedor) => {
            const opcao = document.createElement("option");
            opcao.value = vendedor.id;
            opcao.textContent = vendedor.nome;
            campoVendedorResponsavel.appendChild(opcao);
        });
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

        usuariosCarregados = await resposta.json();
        renderizarTabela(usuariosCarregados);
        preencherDropdownVendedores();
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
        perfil: { id: Number(perfilInput.value) },
        tabelaPrecoId: campoTabelaPreco.value ? Number(campoTabelaPreco.value) : null,
        vendedorResponsavelId: campoVendedorResponsavel.value ? Number(campoVendedorResponsavel.value) : null,
        limiteDescontoPercentual: campoLimiteDesconto.value ? Number(campoLimiteDesconto.value) : 0
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
        campoTabelaPreco.value = usuario.tabelaPrecoId || "";
        campoVendedorResponsavel.value = usuario.vendedorResponsavel ? usuario.vendedorResponsavel.id : "";
        campoLimiteDesconto.value = usuario.limiteDescontoPercentual || 0;
        atualizarCamposCondicionais();

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
    camposCliente.style.display = "none";
    camposVendedor.style.display = "none";
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

// ==================== UTIL (compartilhado com os outros arquivos JS) ====================

function exibirMensagem(elemento, texto, tipo) {
    elemento.textContent = texto;
    elemento.className = `mensagem ${tipo}`;
}

function formatarMoeda(valor) {
    const numero = Number(valor || 0);
    return numero.toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Escapa um texto para ser usado com seguranca dentro de um atributo
// onclick="..." que, por sua vez, usa aspas simples para os argumentos de
// string (ex.: nomes de produtos/tabelas digitados pelo usuario).
function escAtributoOnclick(texto) {
    return String(texto)
        .replace(/\\/g, "\\\\")
        .replace(/'/g, "\\'")
        .replace(/"/g, "&quot;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}

function formatarDataHora(isoString) {
    if (!isoString) return "-";
    const data = new Date(isoString);
    return data.toLocaleString("pt-BR");
}
