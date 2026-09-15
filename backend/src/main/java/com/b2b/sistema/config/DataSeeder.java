package com.b2b.sistema.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.b2b.sistema.model.Role;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.RoleRepository;
import com.b2b.sistema.repository.UsuarioRepository;

/**
 * Cria os perfis de acesso (Role) e um usuario Administrador padrao na
 * primeira execucao, para que a aplicacao ja possa ser testada/demonstrada
 * sem depender de inserts manuais no banco.
 *
 * Login de teste: admin@b2b.com.br / admin123
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataSeeder(UsuarioRepository usuarioRepository, RoleRepository roleRepository) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {

        // 1. Garante que os 4 perfis do documento de requisitos existam
        Role roleAdmin = criarRoleSeNaoExistir("ROLE_ADMIN");
        criarRoleSeNaoExistir("ROLE_VENDEDOR");
        criarRoleSeNaoExistir("ROLE_CLIENTE");
        criarRoleSeNaoExistir("ROLE_EXPEDICAO");

        // 2. Cria o usuario administrador padrao, se ainda nao existir nenhum usuario
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail("admin@b2b.com.br");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setRole(roleAdmin);
            admin.setAtivo(true); // sem isso o admin de teste tambem nasceria inativo

            usuarioRepository.save(admin);

            System.out.println("=====================================================");
            System.out.println("Usuario administrador padrao criado para testes:");
            System.out.println("E-mail: admin@b2b.com.br");
            System.out.println("Senha : admin123");
            System.out.println("=====================================================");
        }
    }

    private Role criarRoleSeNaoExistir(String nome) {
        return roleRepository.findByNome(nome)
                .orElseGet(() -> roleRepository.save(new Role(nome)));
    }
}
