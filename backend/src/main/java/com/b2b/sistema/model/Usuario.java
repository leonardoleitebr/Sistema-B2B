package com.b2b.sistema.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;
    
    @Column(nullable = false)
    private boolean ativo;
    

    public boolean isAtivo() {
		return ativo;
	}


	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Preenchido somente para usuarios com perfil Cliente (RF06/RF07): tabela de precos vinculada.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tabela_preco_id")
    private TabelaPreco tabelaPreco;

    // Preenchido somente para usuarios com perfil Cliente (RF10): vendedor responsavel pela base do cliente.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedorResponsavel;

    // Preenchido somente para usuarios com perfil Vendedor (RF10/RN06): limite de desconto percentual autorizado.
    @Column(name = "limite_desconto_percentual", precision = 5, scale = 2)
    private BigDecimal limiteDescontoPercentual = BigDecimal.ZERO;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void aoPersistir() {
        this.dataCriacao = LocalDateTime.now();
    }

    public Usuario() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public TabelaPreco getTabelaPreco() {
        return tabelaPreco;
    }

    public void setTabelaPreco(TabelaPreco tabelaPreco) {
        this.tabelaPreco = tabelaPreco;
    }

    public Usuario getVendedorResponsavel() {
        return vendedorResponsavel;
    }

    public void setVendedorResponsavel(Usuario vendedorResponsavel) {
        this.vendedorResponsavel = vendedorResponsavel;
    }

    public BigDecimal getLimiteDescontoPercentual() {
        return limiteDescontoPercentual;
    }

    public void setLimiteDescontoPercentual(BigDecimal limiteDescontoPercentual) {
        this.limiteDescontoPercentual = limiteDescontoPercentual;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}