// ==================== RF15 - DASHBOARD ====================

const dashboardDataInicio = document.getElementById("dashboardDataInicio");
const dashboardDataFim = document.getElementById("dashboardDataFim");
const btnPeriodoPersonalizado = document.getElementById("btnPeriodoPersonalizado");

const indTotalPedidos = document.getElementById("indTotalPedidos");
const indFaturamento = document.getElementById("indFaturamento");
const indTicketMedio = document.getElementById("indTicketMedio");
const tabelaStatusDashboardBody = document.querySelector("#tabelaStatusDashboard tbody");
const tabelaMaisVendidosBody = document.querySelector("#tabelaMaisVendidos tbody");
const cardEstoqueBaixoDashboard = document.getElementById("cardEstoqueBaixoDashboard");
const tabelaEstoqueBaixoDashboardBody = document.querySelector("#tabelaEstoqueBaixoDashboard tbody");
const tabelaClientesInativosBody = document.querySelector("#tabelaClientesInativos tbody");

window.aoAbrir_secaoDashboard = function () {
    aplicarPeriodoDashboard("7dias");
};

document.querySelectorAll("#filtrosPeriodoDashboard .filtro-btn").forEach((botao) => {
    botao.addEventListener("click", () => aplicarPeriodoDashboard(botao.dataset.periodo));
});

btnPeriodoPersonalizado.addEventListener("click", () => {
    if (!dashboardDataInicio.value || !dashboardDataFim.value) return;
    marcarFiltroPeriodoAtivo(null);
    const inicio = new Date(`${dashboardDataInicio.value}T00:00:00`);
    const fim = new Date(`${dashboardDataFim.value}T23:59:59`);
    carregarDashboard(inicio, fim);
});

function marcarFiltroPeriodoAtivo(periodo) {
    document.querySelectorAll("#filtrosPeriodoDashboard .filtro-btn").forEach((botao) => {
        botao.classList.toggle("ativo", botao.dataset.periodo === periodo);
    });
}

// RF15 R2 - filtros de periodo: Hoje, Ultimos 7 dias, Este mes, Personalizado
function aplicarPeriodoDashboard(periodo) {
    marcarFiltroPeriodoAtivo(periodo);
    const agora = new Date();
    const fim = agora;
    let inicio;

    if (periodo === "hoje") {
        inicio = new Date(agora.getFullYear(), agora.getMonth(), agora.getDate(), 0, 0, 0);
    } else if (periodo === "mes") {
        inicio = new Date(agora.getFullYear(), agora.getMonth(), 1, 0, 0, 0);
    } else {
        inicio = new Date(agora.getFullYear(), agora.getMonth(), agora.getDate() - 6, 0, 0, 0);
    }
    carregarDashboard(inicio, fim);
}

function isoLocal(data) {
    const dois = (n) => String(n).padStart(2, "0");
    return `${data.getFullYear()}-${dois(data.getMonth() + 1)}-${dois(data.getDate())}T${dois(data.getHours())}:${dois(data.getMinutes())}:${dois(data.getSeconds())}`;
}

async function carregarDashboard(inicio, fim) {
    try {
        const url = `${API_BASE}/dashboard?inicio=${isoLocal(inicio)}&fim=${isoLocal(fim)}`;
        const resposta = await fetch(url, { headers: cabecalhosAutenticados() });
        const dados = await resposta.json();
        renderizarDashboard(dados);
    } catch (erro) {
        console.error("Erro ao carregar dashboard:", erro);
    }
}

function renderizarDashboard(dados) {
    indTotalPedidos.textContent = dados.totalPedidos;
    indFaturamento.textContent = `R$ ${formatarMoeda(dados.faturamentoTotal)}`;
    indTicketMedio.textContent = `R$ ${formatarMoeda(dados.ticketMedio)}`;

    tabelaStatusDashboardBody.innerHTML = "";
    dados.pedidosPorStatus
        .filter((item) => item.quantidade > 0)
        .forEach((item) => {
            const linha = document.createElement("tr");
            linha.innerHTML = `<td>${NOME_AMIGAVEL_STATUS[item.status] || item.status}</td><td>${item.quantidade}</td>`;
            tabelaStatusDashboardBody.appendChild(linha);
        });

    tabelaMaisVendidosBody.innerHTML = "";
    dados.produtosMaisVendidos.forEach((item) => {
        const linha = document.createElement("tr");
        linha.innerHTML = `<td>${item.nome}</td><td>${item.quantidadeVendida}</td>`;
        tabelaMaisVendidosBody.appendChild(linha);
    });
    if (dados.produtosMaisVendidos.length === 0) {
        tabelaMaisVendidosBody.innerHTML = "<tr><td colspan='2'>Nenhuma venda no periodo.</td></tr>";
    }

    // RF15 - alerta de estoque baixo so faz sentido para o Administrador
    if (dados.produtosEstoqueBaixo && dados.produtosEstoqueBaixo.length > 0) {
        cardEstoqueBaixoDashboard.style.display = "block";
        tabelaEstoqueBaixoDashboardBody.innerHTML = "";
        dados.produtosEstoqueBaixo.forEach((produto) => {
            const linha = document.createElement("tr");
            linha.innerHTML = `<td>${produto.nome}</td><td>${produto.quantidadeDisponivel}</td><td>${produto.estoqueMinimo}</td>`;
            tabelaEstoqueBaixoDashboardBody.appendChild(linha);
        });
    } else {
        cardEstoqueBaixoDashboard.style.display = "none";
    }

    tabelaClientesInativosBody.innerHTML = "";
    dados.clientesInativos.forEach((cliente) => {
        const linha = document.createElement("tr");
        const ultimaCompraTexto = cliente.ultimaCompra ? formatarDataHora(cliente.ultimaCompra) : "Nunca comprou";
        linha.innerHTML = `<td>${cliente.nome}</td><td>${ultimaCompraTexto}</td>`;
        tabelaClientesInativosBody.appendChild(linha);
    });
    if (dados.clientesInativos.length === 0) {
        tabelaClientesInativosBody.innerHTML = "<tr><td colspan='2'>Nenhum cliente inativo no momento.</td></tr>";
    }
}
