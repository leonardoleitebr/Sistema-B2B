// ==================== RF03 - CATEGORIAS ====================

const formCategoria = document.getElementById("formCategoria");
const categoriaIdInput = document.getElementById("categoriaId");
const categoriaNomeInput = document.getElementById("categoriaNome");
const btnCancelarCategoria = document.getElementById("btnCancelarCategoria");
const mensagemCategoria = document.getElementById("mensagemCategoria");
const tabelaCategoriasBody = document.querySelector("#tabelaCategorias tbody");
const tituloFormCategoria = document.getElementById("tituloFormCategoria");

let categoriasCarregadas = [];

async function carregarCategorias() {
    try {
        const resposta = await fetch(`${API_BASE}/categorias`, { headers: cabecalhosAutenticados() });
        categoriasCarregadas = await resposta.json();
        renderizarTabelaCategorias();
        preencherDropdownCategorias();
    } catch (erro) {
        console.error("Erro ao carregar categorias:", erro);
    }
}

function renderizarTabelaCategorias() {
    tabelaCategoriasBody.innerHTML = "";
    categoriasCarregadas.forEach((categoria) => {
        const linha = document.createElement("tr");
        linha.innerHTML = `
            <td>${categoria.id}</td>
            <td>${categoria.nome}</td>
            <td class="acoes">
                <button onclick="editarCategoria(${categoria.id})">Editar</button>
                <button onclick="removerCategoria(${categoria.id})">Remover</button>
            </td>
        `;
        tabelaCategoriasBody.appendChild(linha);
    });
}

function preencherDropdownCategorias() {
    const select = document.getElementById("produtoCategoria");
    const valorAtual = select.value;
    select.innerHTML = '<option value="">Selecione...</option>';
    categoriasCarregadas.forEach((categoria) => {
        const opcao = document.createElement("option");
        opcao.value = categoria.id;
        opcao.textContent = categoria.nome;
        select.appendChild(opcao);
    });
    if (valorAtual) select.value = valorAtual;
}

formCategoria.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const id = categoriaIdInput.value;
    const corpo = { nome: categoriaNomeInput.value };
    const editando = Boolean(id);

    try {
        const resposta = await fetch(`${API_BASE}/categorias${editando ? "/" + id : ""}`, {
            method: editando ? "PUT" : "POST",
            headers: cabecalhosAutenticados(),
            body: JSON.stringify(corpo)
        });
        const dados = await resposta.json();
        if (!resposta.ok) {
            exibirMensagem(mensagemCategoria, dados.mensagem || "Erro ao salvar categoria.", "erro");
            return;
        }
        exibirMensagem(mensagemCategoria, "Categoria salva com sucesso!", "sucesso");
        limparFormularioCategoria();
        carregarCategorias();
    } catch (erro) {
        console.error("Erro ao salvar categoria:", erro);
        exibirMensagem(mensagemCategoria, "Nao foi possivel conectar ao backend.", "erro");
    }
});

function editarCategoria(id) {
    const categoria = categoriasCarregadas.find((c) => c.id === id);
    if (!categoria) return;
    categoriaIdInput.value = categoria.id;
    categoriaNomeInput.value = categoria.nome;
    tituloFormCategoria.textContent = `Editando categoria #${categoria.id}`;
    btnCancelarCategoria.style.display = "inline-block";
}

btnCancelarCategoria.addEventListener("click", limparFormularioCategoria);

function limparFormularioCategoria() {
    formCategoria.reset();
    categoriaIdInput.value = "";
    tituloFormCategoria.textContent = "Nova categoria";
    btnCancelarCategoria.style.display = "none";
}

async function removerCategoria(id) {
    if (!(await confirmarPersonalizado("Remover esta categoria? So e possivel se nao houver produtos vinculados."))) return;
    try {
        const resposta = await fetch(`${API_BASE}/categorias/${id}`, {
            method: "DELETE",
            headers: cabecalhosAutenticados()
        });
        if (!resposta.ok) {
            const dados = await resposta.json();
            exibirMensagem(mensagemCategoria, dados.mensagem || "Nao foi possivel remover a categoria.", "erro");
            return;
        }
        carregarCategorias();
    } catch (erro) {
        console.error("Erro ao remover categoria:", erro);
    }
}

window.aoAbrir_secaoCategorias = carregarCategorias;

// ==================== RF04/RF05 - PRODUTOS E ESTOQUE ====================

const formProduto = document.getElementById("formProduto");
const produtoIdInput = document.getElementById("produtoId");
const produtoNomeInput = document.getElementById("produtoNome");
const produtoCategoriaInput = document.getElementById("produtoCategoria");
const produtoUnidadeInput = document.getElementById("produtoUnidade");
const produtoPrecoInput = document.getElementById("produtoPreco");
const produtoEstoqueInput = document.getElementById("produtoEstoque");
const produtoEstoqueMinimoInput = document.getElementById("produtoEstoqueMinimo");
const produtoDestaqueInput = document.getElementById("produtoDestaque");
const btnCancelarProduto = document.getElementById("btnCancelarProduto");
const mensagemProduto = document.getElementById("mensagemProduto");
const tabelaProdutosBody = document.querySelector("#tabelaProdutos tbody");
const tituloFormProduto = document.getElementById("tituloFormProduto");

let produtosCarregados = [];

async function carregarProdutos() {
    try {
        const resposta = await fetch(`${API_BASE}/produtos`, { headers: cabecalhosAutenticados() });
        produtosCarregados = await resposta.json();
        renderizarTabelaProdutos();
    } catch (erro) {
        console.error("Erro ao carregar produtos:", erro);
    }
}

function renderizarTabelaProdutos() {
    tabelaProdutosBody.innerHTML = "";
    produtosCarregados.forEach((produto) => {
        const linha = document.createElement("tr");
        const statusClasse = produto.ativo ? "status-ativo" : "status-inativo";
        const statusTexto = produto.ativo ? "Ativo" : "Inativo";
        const alertaEstoque = produto.estoqueBaixo ? ' <span class="etiqueta-alerta">estoque baixo</span>' : "";

        linha.innerHTML = `
            <td>${produto.id}</td>
            <td>${produto.nome}${produto.destaque ? " ⭐" : ""}</td>
            <td>${produto.categoria ? produto.categoria.nome : "-"}</td>
            <td>R$ ${formatarMoeda(produto.precoPadrao)}</td>
            <td>${produto.quantidadeEstoque}</td>
            <td>${produto.quantidadeReservada}</td>
            <td>${produto.quantidadeDisponivel}${alertaEstoque}</td>
            <td class="${statusClasse}">${statusTexto}</td>
            <td class="acoes">
                <button onclick="editarProduto(${produto.id})">Editar</button>
                ${produto.ativo
                    ? `<button onclick="inativarProduto(${produto.id})">Inativar</button>`
                    : `<button onclick="ativarProduto(${produto.id})">Ativar</button>`}
            </td>
        `;
        tabelaProdutosBody.appendChild(linha);
    });
}

formProduto.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const id = produtoIdInput.value;
    const corpo = {
        nome: produtoNomeInput.value,
        categoriaId: Number(produtoCategoriaInput.value),
        unidadeVenda: produtoUnidadeInput.value,
        precoPadrao: Number(produtoPrecoInput.value),
        quantidadeEstoque: Number(produtoEstoqueInput.value),
        estoqueMinimo: Number(produtoEstoqueMinimoInput.value || 0),
        destaque: produtoDestaqueInput.checked
    };
    const editando = Boolean(id);

    try {
        const resposta = await fetch(`${API_BASE}/produtos${editando ? "/" + id : ""}`, {
            method: editando ? "PUT" : "POST",
            headers: cabecalhosAutenticados(),
            body: JSON.stringify(corpo)
        });
        const dados = await resposta.json();
        if (!resposta.ok) {
            exibirMensagem(mensagemProduto, dados.mensagem || "Erro ao salvar produto.", "erro");
            return;
        }
        exibirMensagem(mensagemProduto, "Produto salvo com sucesso!", "sucesso");
        limparFormularioProduto();
        carregarProdutos();
    } catch (erro) {
        console.error("Erro ao salvar produto:", erro);
        exibirMensagem(mensagemProduto, "Nao foi possivel conectar ao backend.", "erro");
    }
});

function editarProduto(id) {
    const produto = produtosCarregados.find((p) => p.id === id);
    if (!produto) return;
    produtoIdInput.value = produto.id;
    produtoNomeInput.value = produto.nome;
    produtoCategoriaInput.value = produto.categoria ? produto.categoria.id : "";
    produtoUnidadeInput.value = produto.unidadeVenda;
    produtoPrecoInput.value = produto.precoPadrao;
    produtoEstoqueInput.value = produto.quantidadeEstoque;
    produtoEstoqueMinimoInput.value = produto.estoqueMinimo;
    produtoDestaqueInput.checked = produto.destaque;
    tituloFormProduto.textContent = `Editando produto #${produto.id}`;
    btnCancelarProduto.style.display = "inline-block";
    window.scrollTo({ top: 0, behavior: "smooth" });
}

btnCancelarProduto.addEventListener("click", limparFormularioProduto);

function limparFormularioProduto() {
    formProduto.reset();
    produtoIdInput.value = "";
    tituloFormProduto.textContent = "Novo produto";
    btnCancelarProduto.style.display = "none";
}

async function ativarProduto(id) {
    await alterarStatusProduto(id, "ativar");
}

async function inativarProduto(id) {
    if (!(await confirmarPersonalizado("Inativar este produto? Ele deixara de aparecer no catalogo."))) return;
    await alterarStatusProduto(id, "inativar");
}

async function alterarStatusProduto(id, acao) {
    try {
        await fetch(`${API_BASE}/produtos/${id}/${acao}`, {
            method: "PATCH",
            headers: cabecalhosAutenticados()
        });
        carregarProdutos();
    } catch (erro) {
        console.error(`Erro ao ${acao} produto:`, erro);
    }
}

window.aoAbrir_secaoProdutos = async function () {
    if (categoriasCarregadas.length === 0) {
        await carregarCategorias();
    }
    carregarProdutos();
};

// ==================== RF06 - TABELAS DE PRECOS ====================

const formTabelaPreco = document.getElementById("formTabelaPreco");
const tabelaPrecoNomeInput = document.getElementById("tabelaPrecoNome");
const mensagemTabelaPreco = document.getElementById("mensagemTabelaPreco");
const tabelaTabelasPrecosBody = document.querySelector("#tabelaTabelasPrecos tbody");

const cardItensTabelaPreco = document.getElementById("cardItensTabelaPreco");
const nomeTabelaPrecoSelecionada = document.getElementById("nomeTabelaPrecoSelecionada");
const formItemTabelaPreco = document.getElementById("formItemTabelaPreco");
const itemTabelaProdutoInput = document.getElementById("itemTabelaProduto");
const itemTabelaPrecoInput = document.getElementById("itemTabelaPreco");
const mensagemItemTabelaPreco = document.getElementById("mensagemItemTabelaPreco");
const tabelaItensTabelaPrecoBody = document.querySelector("#tabelaItensTabelaPreco tbody");

let tabelaPrecoSelecionadaId = null;

async function carregarTabelasPrecos() {
    try {
        const resposta = await fetch(`${API_BASE}/tabelas-precos`, { headers: cabecalhosAutenticados() });
        const tabelas = await resposta.json();
        tabelaTabelasPrecosBody.innerHTML = "";
        tabelas.forEach((tabela) => {
            const linha = document.createElement("tr");
            linha.innerHTML = `
                <td>${tabela.id}</td>
                <td>${tabela.nome}</td>
                <td>${tabela.itens.length}</td>
                <td class="acoes">
                    <button onclick="abrirItensTabelaPreco(${tabela.id}, '${escAtributoOnclick(tabela.nome)}')">Gerenciar precos</button>
                    <button onclick="removerTabelaPreco(${tabela.id})">Remover</button>
                </td>
            `;
            tabelaTabelasPrecosBody.appendChild(linha);
        });
    } catch (erro) {
        console.error("Erro ao carregar tabelas de precos:", erro);
    }
}

formTabelaPreco.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    try {
        const resposta = await fetch(`${API_BASE}/tabelas-precos`, {
            method: "POST",
            headers: cabecalhosAutenticados(),
            body: JSON.stringify({ nome: tabelaPrecoNomeInput.value })
        });
        const dados = await resposta.json();
        if (!resposta.ok) {
            exibirMensagem(mensagemTabelaPreco, dados.mensagem || "Erro ao criar tabela.", "erro");
            return;
        }
        exibirMensagem(mensagemTabelaPreco, "Tabela criada com sucesso!", "sucesso");
        formTabelaPreco.reset();
        carregarTabelasPrecos();
    } catch (erro) {
        console.error("Erro ao criar tabela de precos:", erro);
        exibirMensagem(mensagemTabelaPreco, "Nao foi possivel conectar ao backend.", "erro");
    }
});

async function removerTabelaPreco(id) {
    if (!(await confirmarPersonalizado("Remover esta tabela de precos? So e possivel se nao houver clientes vinculados."))) return;
    try {
        const resposta = await fetch(`${API_BASE}/tabelas-precos/${id}`, {
            method: "DELETE",
            headers: cabecalhosAutenticados()
        });
        if (!resposta.ok) {
            const dados = await resposta.json();
            exibirMensagem(mensagemTabelaPreco, dados.mensagem || "Nao foi possivel remover.", "erro");
            return;
        }
        if (tabelaPrecoSelecionadaId === id) {
            cardItensTabelaPreco.style.display = "none";
        }
        carregarTabelasPrecos();
    } catch (erro) {
        console.error("Erro ao remover tabela de precos:", erro);
    }
}

async function abrirItensTabelaPreco(id, nome) {
    tabelaPrecoSelecionadaId = id;
    nomeTabelaPrecoSelecionada.textContent = nome;
    cardItensTabelaPreco.style.display = "block";

    if (produtosCarregados.length === 0) {
        await carregarProdutos();
    }
    itemTabelaProdutoInput.innerHTML = '<option value="">Selecione...</option>';
    produtosCarregados.forEach((produto) => {
        const opcao = document.createElement("option");
        opcao.value = produto.id;
        opcao.textContent = `${produto.nome} (padrao: R$ ${formatarMoeda(produto.precoPadrao)})`;
        itemTabelaProdutoInput.appendChild(opcao);
    });

    await carregarItensTabelaPreco();
    cardItensTabelaPreco.scrollIntoView({ behavior: "smooth" });
}

async function carregarItensTabelaPreco() {
    try {
        const resposta = await fetch(`${API_BASE}/tabelas-precos/${tabelaPrecoSelecionadaId}`, {
            headers: cabecalhosAutenticados()
        });
        const tabela = await resposta.json();
        tabelaItensTabelaPrecoBody.innerHTML = "";
        tabela.itens.forEach((item) => {
            const linha = document.createElement("tr");
            linha.innerHTML = `
                <td>${item.produtoNome}</td>
                <td>R$ ${formatarMoeda(item.preco)}</td>
                <td class="acoes">
                    <button onclick="removerItemTabelaPreco(${item.produtoId})">Remover</button>
                </td>
            `;
            tabelaItensTabelaPrecoBody.appendChild(linha);
        });
    } catch (erro) {
        console.error("Erro ao carregar itens da tabela de precos:", erro);
    }
}

formItemTabelaPreco.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    try {
        const resposta = await fetch(`${API_BASE}/tabelas-precos/${tabelaPrecoSelecionadaId}/itens`, {
            method: "PUT",
            headers: cabecalhosAutenticados(),
            body: JSON.stringify({
                produtoId: Number(itemTabelaProdutoInput.value),
                preco: Number(itemTabelaPrecoInput.value)
            })
        });
        const dados = await resposta.json();
        if (!resposta.ok) {
            exibirMensagem(mensagemItemTabelaPreco, dados.mensagem || "Erro ao definir preco.", "erro");
            return;
        }
        exibirMensagem(mensagemItemTabelaPreco, "Preco definido com sucesso!", "sucesso");
        formItemTabelaPreco.reset();
        carregarItensTabelaPreco();
        carregarTabelasPrecos();
    } catch (erro) {
        console.error("Erro ao definir preco na tabela:", erro);
        exibirMensagem(mensagemItemTabelaPreco, "Nao foi possivel conectar ao backend.", "erro");
    }
});

async function removerItemTabelaPreco(produtoId) {
    try {
        await fetch(`${API_BASE}/tabelas-precos/${tabelaPrecoSelecionadaId}/itens/${produtoId}`, {
            method: "DELETE",
            headers: cabecalhosAutenticados()
        });
        carregarItensTabelaPreco();
        carregarTabelasPrecos();
    } catch (erro) {
        console.error("Erro ao remover preco da tabela:", erro);
    }
}

window.aoAbrir_secaoTabelasPrecos = function () {
    cardItensTabelaPreco.style.display = "none";
    carregarTabelasPrecos();
};