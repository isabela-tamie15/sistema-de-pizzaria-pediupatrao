package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository) {
        return args -> {

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // Admin
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("teste123"));
                admin.setRole("ADMIN"); // <<< CORREÇÃO: linha que faltava
                usuarioRepository.save(admin);
                System.out.println("Usuário criado: admin [ADMIN]");
            }

            // Gerente
            if (usuarioRepository.findByUsername("gerente").isEmpty()) {
                Usuario gerente = new Usuario();
                gerente.setUsername("gerente");
                gerente.setPassword(encoder.encode("teste123"));
                gerente.setRole("GERENTE");
                usuarioRepository.save(gerente);
                System.out.println("Usuário criado: gerente [GERENTE]");
            }

            // Atendente
            if (usuarioRepository.findByUsername("atendente").isEmpty()) {
                Usuario atendente = new Usuario();
                atendente.setUsername("atendente");
                atendente.setPassword(encoder.encode("teste123"));
                atendente.setRole("ATENDENTE");
                usuarioRepository.save(atendente);
                System.out.println("Usuário criado: atendente [ATENDENTE]");
            }
        };
    }
}