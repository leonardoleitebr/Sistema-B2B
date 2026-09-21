// ==================== RF07 a RF10, RF13, RF16 - CATALOGO / CARRINHO ====================

const cardSelecionarCliente = document.getElementById("cardSelecionarCliente");
const selecionarClienteInput = document.getElementById("selecionarCliente");
const descontoVendedorInput = document.getElementById("descontoVendedor");
const ajudaLimiteDesconto = document.getElementById("ajudaLimiteDesconto");
const cardComprarNovamente = document.getElementById("cardComprarNovamente");
const listaComprarNovamente = document.getElementById("listaComprarNovamente");
const listaCatalogo = document.getElementById("listaCatalogo");
const mensagemPedidoMinimo = document.getElementById("mensagemPedidoMinimo");
const cardSugestoes = document.getElementById("cardSugestoes");
const listaSugestoes = document.getElementById("listaSugestoes");
const tabelaCarrinhoBody = document.querySelector("#tabelaCarrinho tbody");
const totalCarrinhoSpan = document.getElementById("totalCarrinho");
const btnFinalizarPedido = document.getElementById("btnFinalizarPedido");
const mensagemCarrinho = document.getElementById("mensagemCarrinho");

let carrinho = [];
let clienteAlvoId = null; // cliente para quem o catalogo/pedido esta sendo montado
let catalogoAtual = []; // ultima resposta de GET /api/catalogo (precos vigentes)
let configuracaoAtual = null; // { valorPedidoMinimo, diasInatividadeCliente }

window.aoAbrir_secaoCatalogo = async function () {
    const usuario = obterUsuarioSalvo();
    const perfil = usuario.perfil ? usuario.perfil.nome : null;

    carrinho = [];
    atualizarCarrinhoNaTela();
    mensagemCarrinho.textContent = "";
    await carregarConfiguracao();

    if (perfil === "ROLE_VENDEDOR") {
        cardSelecionarCliente.style.display = "block";
        cardComprarNovamente.style.display = "none";
        listaCatalogo.innerHTML = "";
        cardSugestoes.style.display = "none";
        ajudaLimiteDesconto.textContent = `Seu limite de desconto autorizado: ${usuario.limiteDescontoPercentual || 0}%`;
        await carregarClientesDoVendedor();
    } else if (perfil === "ROLE_CLIENTE") {
        cardSelecionarCliente.style.display = "none";
        clienteAlvoId = usuario.id;
        await carregarCatalogoParaCliente();
    }
};

async function carregarConfiguracao() {
    try {
        const resposta = await fetch(`${API_BASE}/configuracoes`, { headers: cabecalhosAutenticados() });
        configuracaoAtual = await resposta.json();
    } catch (erro) {
        console.error("Erro ao carregar configuracoes:", erro);
    }
}

async function carregarClientesDoVendedor() {
    try {
        const resposta = await fetch(`${API_BASE}/vendedor/clientes`, { headers: cabecalhosAutenticados() });
        const clientes = await resposta.json();
        selecionarClienteInput.innerHTML = '<option value="">Selecione um cliente da sua base...</option>';
        clientes.forEach((cliente) => {
            const opcao = document.createElement("option");
            opcao.value = cliente.id;
            opcao.textContent = `${cliente.nome} (${cliente.email})`;
            selecionarClienteInput.appendChild(opcao);
        });
    } catch (erro) {
        console.error("Erro ao carregar clientes do vendedor:", erro);
    }
}

selecionarClienteInput.addEventListener("change", async () => {
    clienteAlvoId = selecionarClienteInput.value ? Number(selecionarClienteInput.value) : null;
    carrinho = [];
    atualizarCarrinhoNaTela();
    if (clienteAlvoId) {
        await carregarCatalogoParaCliente();
    } else {
        listaCatalogo.innerHTML = "";
        cardComprarNovamente.style.display = "none";
        cardSugestoes.style.display = "none";
    }
});

descontoVendedorInput.addEventListener("input", atualizarCarrinhoNaTela);

async function carregarCatalogoParaCliente() {
    try {
        const resposta = await fetch(`${API_BASE}/catalogo?clienteId=${clienteAlvoId}`, { headers: cabecalhosAutenticados() });
        catalogoAtual = await resposta.json();
        renderizarCatalogo(catalogoAtual);

        const respostaFrequentes = await fetch(`${API_BASE}/catalogo/frequentes?clienteId=${clienteAlvoId}`, {
            headers: cabecalhosAutenticados()
        });
        renderizarComprarNovamente(await respostaFrequentes.json());
    } catch (erro) {
        console.error("Erro ao carregar catalogo:", erro);
    }
}

function renderizarCatalogo(itens) {
    listaCatalogo.innerHTML = "";
    itens.forEach((item) => {
        const cartao = document.createElement("div");
        cartao.className = "cartao-produto";
        const disponibilidadeTexto = item.disponivel ? `${item.quantidadeDisponivel} disponivel` : "Indisponivel";

        cartao.innerHTML = `
            <h3>${item.nome}${item.destaque ? " ⭐" : ""}</h3>
            <p class="cartao-categoria">${item.categoria} &mdash; ${item.unidadeVenda}</p>
            <p class="cartao-preco">R$ ${formatarMoeda(item.preco)}${item.precoPersonalizado ? ' <span class="etiqueta-preco">preco especial</span>' : ""}</p>
            <p class="cartao-disponibilidade">${disponibilidadeTexto}</p>
            <div class="cartao-acoes">
                <input type="number" min="1" value="1" class="input-quantidade" id="qtd-${item.produtoId}" ${item.disponivel ? "" : "disabled"}>
                <button ${item.disponivel ? "" : "disabled"} onclick="adicionarAoCarrinhoPeloInput(${item.produtoId})">Adicionar</button>
            </div>
        `;
        listaCatalogo.appendChild(cartao);
    });
}

function adicionarAoCarrinhoPeloInput(produtoId) {
    const input = document.getElementById(`qtd-${produtoId}`);
    const quantidade = Number(input && input.value ? input.value : 1);
    const produto = catalogoAtual.find((p) => p.produtoId === produtoId);
    if (!produto || quantidade <= 0) return;
    adicionarAoCarrinho(produtoId, produto.nome, produto.preco, produto.unidadeVenda, quantidade);
}

function adicionarAoCarrinho(produtoId, nome, preco, unidadeVenda, quantidade) {
    const existente = carrinho.find((item) => item.produtoId === produtoId);
    if (existente) {
        existente.quantidade += quantidade;
    } else {
        carrinho.push({ produtoId, nome, preco, unidadeVenda, quantidade });
    }
    atualizarCarrinhoNaTela();
}

function removerDoCarrinho(indice) {
    carrinho.splice(indice, 1);
    atualizarCarrinhoNaTela();
}

function atualizarCarrinhoNaTela() {
    tabelaCarrinhoBody.innerHTML = "";
    let total = 0;

    carrinho.forEach((item, indice) => {
        const subtotal = item.preco * item.quantidade;
        total += subtotal;
        const linha = document.createElement("tr");
        linha.innerHTML = `
            <td>${item.nome}</td>
            <td>R$ ${formatarMoeda(item.preco)}</td>
            <td>${item.quantidade}</td>
            <td>R$ ${formatarMoeda(subtotal)}</td>
            <td><button onclick="removerDoCarrinho(${indice})">Remover</button></td>
        `;
        tabelaCarrinhoBody.appendChild(linha);
    });

    const usuario = obterUsuarioSalvo();
    if (usuario && usuario.perfil && usuario.perfil.nome === "ROLE_VENDEDOR") {
        const desconto = Number(descontoVendedorInput.value || 0);
        if (desconto > 0) {
            total = total * (1 - desconto / 100);
        }
    }

    totalCarrinhoSpan.textContent = formatarMoeda(total);
    atualizarMensagemPedidoMinimo(total);
}

// RF09 - avisa quanto falta para o pedido minimo; RF16 - sugere produtos nesse caso
function atualizarMensagemPedidoMinimo(total) {
    if (!configuracaoAtual) return;

    if (total < configuracaoAtual.valorPedidoMinimo) {
        const faltante = configuracaoAtual.valorPedidoMinimo - total;
        mensagemPedidoMinimo.textContent =
            `Pedido minimo: R$ ${formatarMoeda(configuracaoAtual.valorPedidoMinimo)} — faltam R$ ${formatarMoeda(faltante)} para atingir o minimo.`;
        if (clienteAlvoId) {
            cardSugestoes.style.display = "block";
            carregarSugestoes();
        }
    } else {
        mensagemPedidoMinimo.textContent = `Pedido minimo: R$ ${formatarMoeda(configuracaoAtual.valorPedidoMinimo)} (ja atingido).`;
        cardSugestoes.style.display = "none";
    }
}

async function carregarSugestoes() {
    if (!clienteAlvoId) return;
    try {
        const resposta = await fetch(`${API_BASE}/catalogo/sugestoes?clienteId=${clienteAlvoId}`, {
            headers: cabecalhosAutenticados()
        });
        const sugestoes = await resposta.json();
        listaSugestoes.innerHTML = "";
        sugestoes.forEach((item) => {
            const cartao = document.createElement("div");
            cartao.className = "cartao-produto";
            cartao.innerHTML = `
                <h3>${item.nome}</h3>
                <p class="cartao-preco">R$ ${formatarMoeda(item.preco)}</p>
                <button ${item.disponivel ? "" : "disabled"}
                    onclick="adicionarAoCarrinho(${item.produtoId}, '${escAtributoOnclick(item.nome)}', ${item.preco}, '${item.unidadeVenda}', 1)">
                    Adicionar
                </button>
            `;
            listaSugestoes.appendChild(cartao);
        });
    } catch (erro) {
        console.error("Erro ao carregar sugestoes:", erro);
    }
}

// RF13 - "comprar novamente": produtos ja comprados, com a quantidade da ultima compra
function renderizarComprarNovamente(frequentes) {
    if (!frequentes || frequentes.length === 0) {
        cardComprarNovamente.style.display = "none";
        return;
    }
    cardComprarNovamente.style.display = "block";
    listaComprarNovamente.innerHTML = "";
    frequentes.forEach((item) => {
        const cartao = document.createElement("div");
        cartao.className = "cartao-produto";
        cartao.innerHTML = `
            <h3>${item.nome}</h3>
            <p class="cartao-preco">R$ ${formatarMoeda(item.preco)}</p>
            <p class="cartao-disponibilidade">Ultima compra: ${item.quantidadeUltimaCompra} ${item.unidadeVenda}</p>
            <button ${item.disponivel ? "" : "disabled"}
                onclick="adicionarAoCarrinho(${item.produtoId}, '${escAtributoOnclick(item.nome)}', ${item.preco}, '${item.unidadeVenda}', ${item.quantidadeUltimaCompra})">
                Adicionar ${item.quantidadeUltimaCompra}
            </button>
        `;
        listaComprarNovamente.appendChild(cartao);
    });
}

btnFinalizarPedido.addEventListener("click", async () => {
    if (carrinho.length === 0) {
        exibirMensagem(mensagemCarrinho, "Adicione ao menos um produto ao carrinho.", "erro");
        return;
    }

    const usuario = obterUsuarioSalvo();
    const perfil = usuario.perfil.nome;
    const itens = carrinho.map((item) => ({ produtoId: item.produtoId, quantidade: item.quantidade }));

    let url = `${API_BASE}/pedidos`;
    let corpo = { itens };

    if (perfil === "ROLE_VENDEDOR") {
        if (!clienteAlvoId) {
            exibirMensagem(mensagemCarrinho, "Selecione um cliente antes de finalizar o pedido.", "erro");
            return;
        }
        url = `${API_BASE}/pedidos/vendedor`;
        corpo = { clienteId: clienteAlvoId, itens, descontoPercentual: Number(descontoVendedorInput.value || 0) };
    }

    try {
        const resposta = await fetch(url, {
            method: "POST",
            headers: cabecalhosAutenticados(),
            body: JSON.stringify(corpo)
        });
        const dados = await resposta.json();

        if (!resposta.ok) {
            exibirMensagem(mensagemCarrinho, dados.mensagem || "Nao foi possivel finalizar o pedido.", "erro");
            return;
        }

        exibirMensagem(mensagemCarrinho, `Pedido #${dados.id} criado com sucesso! Acompanhe em "Pedidos".`, "sucesso");
        carrinho = [];
        atualizarCarrinhoNaTela();
        if (clienteAlvoId) {
            carregarCatalogoParaCliente();
        }
    } catch (erro) {
        console.error("Erro ao finalizar pedido:", erro);
        exibirMensagem(mensagemCarrinho, "Nao foi possivel conectar ao backend.", "erro");
    }
});

// RF12 - repetir pedido: chamado por pedidos.js, usa sempre os precos vigentes (RN03)
window.repetirPedidoNoCarrinho = async function (itensAntigos, clienteIdDoPedido) {
    const usuario = obterUsuarioSalvo();
    mostrarSecao("secaoCatalogo");

    if (usuario.perfil.nome === "ROLE_VENDEDOR") {
        clienteAlvoId = clienteIdDoPedido;
        cardSelecionarCliente.style.display = "block";
        await carregarClientesDoVendedor();
        selecionarClienteInput.value = clienteIdDoPedido;
    } else {
        clienteAlvoId = usuario.id;
    }

    carrinho = [];
    await carregarCatalogoParaCliente();

    itensAntigos.forEach((itemAntigo) => {
        const produtoAtual = catalogoAtual.find((p) => p.produtoId === itemAntigo.produtoId);
        if (!produtoAtual) return; // produto pode ter sido removido/inativado desde entao
        adicionarAoCarrinho(itemAntigo.produtoId, produtoAtual.nome, produtoAtual.preco, produtoAtual.unidadeVenda, itemAntigo.quantidade);
    });

    exibirMensagem(mensagemCarrinho, "Itens do pedido anterior adicionados ao carrinho com os precos atuais.", "sucesso");
};
