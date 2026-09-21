package com.b2b.sistema.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.b2b.sistema.model.Categoria;
import com.b2b.sistema.model.ItemTabelaPreco;
import com.b2b.sistema.model.Produto;
import com.b2b.sistema.model.Role;
import com.b2b.sistema.model.TabelaPreco;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.CategoriaRepository;
import com.b2b.sistema.repository.ItemTabelaPrecoRepository;
import com.b2b.sistema.repository.ProdutoRepository;
import com.b2b.sistema.repository.RoleRepository;
import com.b2b.sistema.repository.TabelaPrecoRepository;
import com.b2b.sistema.repository.UsuarioRepository;

import java.math.BigDecimal;

/**
 * Cria os perfis de acesso (Role) e dados minimos de demonstracao na primeira
 * execucao, para que a aplicacao ja possa ser testada/demonstrada sem depender
 * de inserts manuais no banco.
 *
 * Logins de teste (todos com o padrao "senha123", exceto o admin):
 *   admin@b2b.com.br      / admin123      (Administrador)
 *   vendedor@b2b.com.br   / vendedor123   (Vendedor, limite de desconto: 5%)
 *   cliente@b2b.com.br    / cliente123    (Cliente, atendido pelo vendedor acima,
 *                                          com a "Tabela Padrao Atacado" vinculada)
 *   expedicao@b2b.com.br  / expedicao123  (Expedicao)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final TabelaPrecoRepository tabelaPrecoRepository;
    private final ItemTabelaPrecoRepository itemTabelaPrecoRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataSeeder(UsuarioRepository usuarioRepository, RoleRepository roleRepository,
                       CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository,
                       TabelaPrecoRepository tabelaPrecoRepository, ItemTabelaPrecoRepository itemTabelaPrecoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
        this.tabelaPrecoRepository = tabelaPrecoRepository;
        this.itemTabelaPrecoRepository = itemTabelaPrecoRepository;
    }

    @Override
    public void run(String... args) {

        // 1. Garante que os 4 perfis do documento de requisitos existam
        Role roleAdmin = criarRoleSeNaoExistir("ROLE_ADMIN");
        Role roleVendedor = criarRoleSeNaoExistir("ROLE_VENDEDOR");
        Role roleCliente = criarRoleSeNaoExistir("ROLE_CLIENTE");
        Role roleExpedicao = criarRoleSeNaoExistir("ROLE_EXPEDICAO");

        boolean primeiraExecucao = usuarioRepository.count() == 0;

        // 2. Usuario administrador padrao
        Usuario admin = primeiraExecucao ? criarUsuario("Administrador", "admin@b2b.com.br", "admin123", roleAdmin) : null;

        // 3. Categorias e produtos de demonstracao (RF03/RF04/RF05)
        Categoria refrigerantes = criarCategoriaSeNaoExistir("Refrigerantes");
        Categoria aguas = criarCategoriaSeNaoExistir("Aguas");
        Categoria cervejas = criarCategoriaSeNaoExistir("Cervejas");
        Categoria energeticos = criarCategoriaSeNaoExistir("Energeticos");

        Produto refrigerante2L = criarProdutoSeNaoExistir("Refrigerante Cola 2L (caixa c/ 6)", refrigerantes, "Caixa",
                new BigDecimal("42.90"), 120, 20, true);
        criarProdutoSeNaoExistir("Refrigerante Guarana 2L (caixa c/ 6)", refrigerantes, "Caixa",
                new BigDecimal("39.90"), 100, 20, false);
        criarProdutoSeNaoExistir("Agua Mineral 500ml (fardo c/ 12)", aguas, "Fardo",
                new BigDecimal("18.50"), 200, 30, false);
        criarProdutoSeNaoExistir("Agua com Gas 500ml (fardo c/ 12)", aguas, "Fardo",
                new BigDecimal("21.00"), 15, 20, false);
        criarProdutoSeNaoExistir("Cerveja Pilsen 350ml (fardo c/ 12)", cervejas, "Fardo",
                new BigDecimal("54.00"), 80, 15, true);
        criarProdutoSeNaoExistir("Energetico 250ml (caixa c/ 24)", energeticos, "Caixa",
                new BigDecimal("96.00"), 40, 10, false);

        // 4. Tabela de precos de demonstracao (RF06)
        TabelaPreco tabelaAtacado = criarTabelaPrecoSeNaoExistir("Tabela Padrao Atacado");
        garantirItemTabelaPreco(tabelaAtacado, refrigerante2L, new BigDecimal("39.90"));

        // 5. Usuarios de demonstracao dos demais perfis, ja vinculados entre si
        if (primeiraExecucao) {
            Usuario vendedor = criarUsuario("Vendedor Demo", "vendedor@b2b.com.br", "vendedor123", roleVendedor);
            vendedor.setLimiteDescontoPercentual(new BigDecimal("5"));
            usuarioRepository.save(vendedor);

            Usuario cliente = criarUsuario("Cliente Demo", "cliente@b2b.com.br", "cliente123", roleCliente);
            cliente.setVendedorResponsavel(vendedor);
            cliente.setTabelaPreco(tabelaAtacado);
            usuarioRepository.save(cliente);

            criarUsuario("Expedicao Demo", "expedicao@b2b.com.br", "expedicao123", roleExpedicao);

            System.out.println("=====================================================");
            System.out.println("Usuarios padrao criados para testes:");
            System.out.println("Administrador : admin@b2b.com.br      / admin123");
            System.out.println("Vendedor      : vendedor@b2b.com.br   / vendedor123");
            System.out.println("Cliente       : cliente@b2b.com.br    / cliente123");
            System.out.println("Expedicao     : expedicao@b2b.com.br  / expedicao123");
            System.out.println("=====================================================");
        }
    }

    private Role criarRoleSeNaoExistir(String nome) {
        return roleRepository.findByNome(nome)
                .orElseGet(() -> roleRepository.save(new Role(nome)));
    }

    private Usuario criarUsuario(String nome, String email, String senha, Role role) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setRole(role);
        usuario.setAtivo(true); // usuarios de demonstracao ja nascem ativos
        return usuarioRepository.save(usuario);
    }

    private Categoria criarCategoriaSeNaoExistir(String nome) {
        return categoriaRepository.findByNome(nome).orElseGet(() -> categoriaRepository.save(new Categoria(nome)));
    }

    private Produto criarProdutoSeNaoExistir(String nome, Categoria categoria, String unidadeVenda,
                                              BigDecimal precoPadrao, int quantidadeEstoque, int estoqueMinimo,
                                              boolean destaque) {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getNome().equals(nome))
                .findFirst()
                .orElseGet(() -> {
                    Produto produto = new Produto();
                    produto.setNome(nome);
                    produto.setCategoria(categoria);
                    produto.setUnidadeVenda(unidadeVenda);
                    produto.setPrecoPadrao(precoPadrao);
                    produto.setQuantidadeEstoque(quantidadeEstoque);
                    produto.setQuantidadeReservada(0);
                    produto.setEstoqueMinimo(estoqueMinimo);
                    produto.setDestaque(destaque);
                    produto.setAtivo(true);
                    return produtoRepository.save(produto);
                });
    }

    private TabelaPreco criarTabelaPrecoSeNaoExistir(String nome) {
        return tabelaPrecoRepository.findAll().stream()
                .filter(t -> t.getNome().equals(nome))
                .findFirst()
                .orElseGet(() -> tabelaPrecoRepository.save(new TabelaPreco(nome)));
    }

    private void garantirItemTabelaPreco(TabelaPreco tabelaPreco, Produto produto, BigDecimal preco) {
        itemTabelaPrecoRepository.findByTabelaPrecoIdAndProdutoId(tabelaPreco.getId(), produto.getId())
                .orElseGet(() -> itemTabelaPrecoRepository.save(new ItemTabelaPreco(tabelaPreco, produto, preco)));
    }
}
