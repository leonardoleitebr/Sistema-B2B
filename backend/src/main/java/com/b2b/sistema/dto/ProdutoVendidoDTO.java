package com.b2b.sistema.dto;

public class ProdutoVendidoDTO {

    private Long produtoId;
    private String nome;
    private long quantidadeVendida;

    public ProdutoVendidoDTO(Long produtoId, String nome, long quantidadeVendida) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.quantidadeVendida = quantidadeVendida;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getNome() {
        return nome;
    }

    public long getQuantidadeVendida() {
        return quantidadeVendida;
    }
}
