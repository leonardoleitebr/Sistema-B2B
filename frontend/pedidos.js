// ==================== RF08 a RF14 - PEDIDOS ====================

const tabelaPedidosBody = document.querySelector("#tabelaPedidos tbody");
const filtrosStatusPedido = document.getElementById("filtrosStatusPedido");
const cardDetalhePedido = document.getElementById("cardDetalhePedido");
const detalhePedidoIdSpan = document.getElementById("detalhePedidoId");
const detalhePedidoConteudo = document.getElementById("detalhePedidoConteudo");

let pedidosCarregados = [];
let filtroStatusAtual = "TODOS";

const NOME_AMIGAVEL_STATUS = {
    RASCUNHO: "Rascunho",
    AGUARDANDO_APROVACAO: "Aguardando aprovacao",
    APROVADO: "Aprovado",
    EM_SEPARACAO: "Em separacao",
    DIVERGENCIA: "Divergencia",
    ENVIADO: "Enviado",
    CONCLUIDO: "Concluido",
    CANCELADO: "Cancelado"
};

window.aoAbrir_secaoPedidos = async function () {
    cardDetalhePedido.style.display = "none";
    await carregarPedidos();
};

async function carregarPedidos() {
    try {
        const resposta = await fetch(`${API_BASE}/pedidos`, { headers: cabecalhosAutenticados() });
        pedidosCarregados = await resposta.json();
        renderizarFiltrosStatus();
        renderizarTabelaPedidos();
    } catch (erro) {
        console.error("Erro ao carregar pedidos:", erro);
    }
}

// RF11 R2 - visualizar pedidos agrupados por status
function renderizarFiltrosStatus() {
    const statusPresentes = [...new Set(pedidosCarregados.map((p) => p.status))];
    filtrosStatusPedido.innerHTML = "";

    const botaoTodos = document.createElement("button");
    botaoTodos.type = "button";
    botaoTodos.className = "filtro-btn" + (filtroStatusAtual === "TODOS" ? " ativo" : "");
    botaoTodos.textContent = `Todos (${pedidosCarregados.length})`;
    botaoTodos.onclick = () => {
        filtroStatusAtual = "TODOS";
        renderizarFiltrosStatus();
        renderizarTabelaPedidos();
    };
    filtrosStatusPedido.appendChild(botaoTodos);

    statusPresentes.forEach((status) => {
        const quantidade = pedidosCarregados.filter((p) => p.status === status).length;
        const botao = document.createElement("button");
        botao.type = "button";
        botao.className = "filtro-btn" + (filtroStatusAtual === status ? " ativo" : "");
        botao.textContent = `${NOME_AMIGAVEL_STATUS[status] || status} (${quantidade})`;
        botao.onclick = () => {
            filtroStatusAtual = status;
            renderizarFiltrosStatus();
            renderizarTabelaPedidos();
        };
        filtrosStatusPedido.appendChild(botao);
    });
}

function renderizarTabelaPedidos() {
    const lista = filtroStatusAtual === "TODOS"
        ? pedidosCarregados
        : pedidosCarregados.filter((p) => p.status === filtroStatusAtual);

    tabelaPedidosBody.innerHTML = "";
    lista.forEach((pedido) => {
        const linha = document.createElement("tr");
        linha.innerHTML = `
            <td>${pedido.id}</td>
            <td>${pedido.cliente ? pedido.cliente.nome : "-"}</td>
            <td>${pedido.vendedor ? pedido.vendedor.nome : "-"}</td>
            <td><span class="etiqueta-status">${NOME_AMIGAVEL_STATUS[pedido.status] || pedido.status}</span></td>
            <td>R$ ${formatarMoeda(pedido.valorTotal)}</td>
            <td>${formatarDataHora(pedido.dataCriacao)}</td>
            <td class="acoes"><button onclick="abrirDetalhePedido(${pedido.id})">Detalhes</button></td>
        `;
        tabelaPedidosBody.appendChild(linha);
    });
}

async function abrirDetalhePedido(id) {
    try {
        const resposta = await fetch(`${API_BASE}/pedidos/${id}`, { headers: cabecalhosAutenticados() });
        const pedido = await resposta.json();
        renderizarDetalhePedido(pedido);
        cardDetalhePedido.style.display = "block";
        cardDetalhePedido.scrollIntoView({ behavior: "smooth" });
    } catch (erro) {
        console.error("Erro ao carregar pedido:", erro);
    }
}

function renderizarDetalhePedido(pedido) {
    detalhePedidoIdSpan.textContent = pedido.id;
    const usuario = obterUsuarioSalvo();
    const perfil = usuario.perfil.nome;

    const itensHtml = pedido.itens.map((item) => `
        <tr>
            <td>${item.produtoNome}</td>
            <td>${item.quantidade} ${item.unidadeVenda}</td>
            <td>R$ ${formatarMoeda(item.precoUnitarioAplicado)}</td>
            <td>R$ ${formatarMoeda(item.subtotal)}</td>
        </tr>
    `).join("");

    const historicoHtml = pedido.historico.map((h) => `
        <li>${formatarDataHora(h.dataHora)} &mdash; ${NOME_AMIGAVEL_STATUS[h.status] || h.status}${h.observacao ? ": " + h.observacao : ""}</li>
    `).join("");

    const divergenciasHtml = pedido.divergencias.length > 0
        ? `<h4>Divergencias registradas</h4><ul>${pedido.divergencias.map((d) => `<li>${formatarDataHora(d.dataRegistro)} &mdash; ${d.descricao}</li>`).join("")}</ul>`
        : "";

    detalhePedidoConteudo.innerHTML = `
        <p><strong>Cliente:</strong> ${pedido.cliente.nome} (${pedido.cliente.email})</p>
        ${pedido.vendedor ? `<p><strong>Lancado pelo vendedor:</strong> ${pedido.vendedor.nome}${Number(pedido.descontoPercentual) > 0 ? ` &mdash; desconto aplicado: ${pedido.descontoPercentual}%` : ""}</p>` : ""}
        <p><strong>Status atual:</strong> ${NOME_AMIGAVEL_STATUS[pedido.status] || pedido.status}</p>
        <div class="tabela-scroll">
        <table>
            <thead><tr><th>Produto</th><th>Qtd.</th><th>Preco unit.</th><th>Subtotal</th></tr></thead>
            <tbody>${itensHtml}</tbody>
        </table>
        </div>
        <p><strong>Total: R$ ${formatarMoeda(pedido.valorTotal)}</strong></p>
        <h4>Historico do pedido</h4>
        <ul>${historicoHtml}</ul>
        ${divergenciasHtml}
        <div class="botoes">${montarAcoesPedido(pedido, perfil)}</div>
        <p id="mensagemDetalhePedido" class="mensagem"></p>
    `;
}

// Monta os botoes de acao disponiveis para o pedido, de acordo com o perfil logado e o status atual.
function montarAcoesPedido(pedido, perfil) {
    const botoes = [];

    if (perfil === "ROLE_ADMIN") {
        if (pedido.status === "AGUARDANDO_APROVACAO") {
            botoes.push(`<button onclick="executarAcaoPedido(${pedido.id}, 'aprovar')">Aprovar</button>`);
        }
        if (pedido.status !== "CANCELADO" && pedido.status !== "CONCLUIDO") {
            botoes.push(`<button onclick="executarAcaoPedido(${pedido.id}, 'cancelar')">Cancelar</button>`);
        }
        if (pedido.status === "ENVIADO") {
            botoes.push(`<button onclick="executarAcaoPedido(${pedido.id}, 'concluir')">Marcar como concluido</button>`);
        }
    }

    if (perfil === "ROLE_CLIENTE" && pedido.status === "AGUARDANDO_APROVACAO") {
        botoes.push(`<button onclick="executarAcaoPedido(${pedido.id}, 'cancelar')">Cancelar pedido</button>`);
    }

    if (perfil === "ROLE_CLIENTE" || perfil === "ROLE_VENDEDOR") {
        botoes.push(`<button onclick="repetirPedido(${pedido.id})">Repetir pedido</button>`);
    }

    if (perfil === "ROLE_EXPEDICAO") {
        if (pedido.status === "APROVADO") {
            botoes.push(`<button onclick="executarAcaoPedido(${pedido.id}, 'iniciar-separacao')">Iniciar separacao</button>`);
        }
        if (pedido.status === "EM_SEPARACAO" || pedido.status === "DIVERGENCIA") {
            botoes.push(`<button onclick="abrirFormDivergencia(${pedido.id})">Registrar divergencia</button>`);
            botoes.push(`<button onclick="executarAcaoPedido(${pedido.id}, 'enviar')">Marcar como enviado</button>`);
        }
    }

    return botoes.join(" ");
}

async function executarAcaoPedido(id, acao) {
    if (acao === "cancelar" && !(await confirmarPersonalizado("Confirma o cancelamento deste pedido?"))) return;

    try {
        const resposta = await fetch(`${API_BASE}/pedidos/${id}/${acao}`, {
            method: "PATCH",
            headers: cabecalhosAutenticados()
        });
        const dados = await resposta.json();

        if (!resposta.ok) {
            await alertaPersonalizado(dados.mensagem || "Nao foi possivel executar a acao.");
            return;
        }

        await carregarPedidos();
        abrirDetalhePedido(id);
    } catch (erro) {
        console.error(`Erro ao executar a acao "${acao}" no pedido:`, erro);
        await alertaPersonalizado("Nao foi possivel concluir a acao. Tente novamente.");
    }
}

// RF14 - Expedicao registra uma divergencia encontrada na separacao
async function abrirFormDivergencia(id) {
    const descricao = await promptPersonalizado("Descreva a divergencia encontrada (ex.: falta de produto, quantidade incorreta, avaria):");
    if (!descricao) return;
    registrarDivergencia(id, descricao);
}

async function registrarDivergencia(id, descricao) {
    try {
        const resposta = await fetch(`${API_BASE}/pedidos/${id}/divergencias`, {
            method: "POST",
            headers: cabecalhosAutenticados(),
            body: JSON.stringify({ descricao })
        });
        const dados = await resposta.json();

        if (!resposta.ok) {
            await alertaPersonalizado(dados.mensagem || "Nao foi possivel registrar a divergencia.");
            return;
        }

        await carregarPedidos();
        abrirDetalhePedido(id);
    } catch (erro) {
        console.error("Erro ao registrar divergencia:", erro);
        await alertaPersonalizado("Nao foi possivel concluir a acao. Tente novamente.");
    }
}

// RF12 - repetir pedido: reaproveita os itens/quantidades, mas sempre com os precos vigentes (RN03)
async function repetirPedido(id) {
    try {
        const resposta = await fetch(`${API_BASE}/pedidos/${id}`, { headers: cabecalhosAutenticados() });
        const pedido = await resposta.json();
        await window.repetirPedidoNoCarrinho(pedido.itens, pedido.cliente.id);
    } catch (erro) {
        console.error("Erro ao repetir pedido:", erro);
    }
}