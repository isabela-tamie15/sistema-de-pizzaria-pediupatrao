package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // AuditoriaService para registrar operações em dados de clientes (RF05)
    @Autowired
    private AuditoriaService auditoriaService;

    public Cliente novoCliente(Cliente cliente) {
        Cliente clienteSalvo = clienteRepository.save(cliente);
        // Registra auditoria de criação de cliente (RF05)
        auditoriaService.registrarAuditoria("CLIENTE_CRIADO", clienteSalvo.getId(),
                "Novo cliente cadastrado: " + clienteSalvo.getNome(), null, clienteSalvo.getNome(), null);
        return clienteSalvo;
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    // Metodo para excluir cliente
    public void excluir(String id) {
        // Busca o nome antes de excluir para registrar na auditoria
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        String nomeCliente = clienteOpt.map(Cliente::getNome).orElse("desconhecido");
        clienteRepository.deleteById(id);
        // Registra auditoria de exclusão de cliente (RF05)
        auditoriaService.registrarAuditoria("CLIENTE_EXCLUIDO", id,
                "Cliente excluído: " + nomeCliente, nomeCliente, null, null);
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    // Método para salvar um novo cliente ou atualizar um cliente existente
    public Cliente salvar(Cliente cliente) {
        // Se o cliente não tem ID (novo cliente), salva como novo
        if (cliente.getId() == null) {
            Cliente clienteSalvo = clienteRepository.save(cliente);
            // Registra auditoria de criação de cliente (RF05)
            auditoriaService.registrarAuditoria("CLIENTE_CRIADO", clienteSalvo.getId(),
                    "Novo cliente cadastrado: " + clienteSalvo.getNome(), null, clienteSalvo.getNome(), null);
            return clienteSalvo;
        } // Se já tem ID (cliente existente), atualiza
        else {
            // Verifica se o cliente existe antes de atualizar
            if (clienteRepository.existsById(cliente.getId())) {
                Cliente clienteSalvo = clienteRepository.save(cliente);
                // Registra auditoria de atualização de cliente (RF05)
                auditoriaService.registrarAuditoria("CLIENTE_ATUALIZADO", clienteSalvo.getId(),
                        "Dados do cliente atualizados: " + clienteSalvo.getNome(), null, clienteSalvo.getNome(), null);
                return clienteSalvo;
            } else {
                throw new IllegalArgumentException("Cliente não encontrado para atualização.");
            }
        }
    }

}

// RF05: Toda alteração em dados de clientes é registrada na auditoria automaticamente.
// O AuditoriaService captura internamente o usuário autenticado via SecurityContextHolder.
// Os métodos de escrita (novoCliente, salvar, excluir) registram auditoria;
// Os métodos listarClientes e buscarPorId não registram pois são consultas.