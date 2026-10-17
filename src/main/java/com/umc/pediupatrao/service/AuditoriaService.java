package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    // Registra uma nova ação no histórico de auditoria com todos os campos exigidos
    // valorAnterior e valorNovo podem ser null quando não há campo específico alterado (ex: criação)
    // justificativa pode ser null quando não é exigida pela regra de negócio
    public void registrarAuditoria(String acao, String entidadeId, String descricao,
                                   String valorAnterior, String valorNovo, String justificativa) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioResponsavel = auth.getName();
        // Pega o primeiro perfil do usuário logado (ex: ROLE_GERENTE e remove o prefixo ROLE_)
        String perfilUsuario = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        Auditoria auditoria = new Auditoria();
        auditoria.setAcao(acao);
        auditoria.setUsuarioResponsavel(usuarioResponsavel);
        auditoria.setPerfilUsuario(perfilUsuario);
        auditoria.setEntidadeId(entidadeId);
        auditoria.setDescricao(descricao);
        auditoria.setValorAnterior(valorAnterior);
        auditoria.setValorNovo(valorNovo);
        auditoria.setJustificativa(justificativa);
        auditoria.setDataHora(LocalDateTime.now());
        auditoriaRepository.save(auditoria);
    }

    // Retorna todo o historico de auditoria do sistema
    public List<Auditoria> listarTodos() {
        return auditoriaRepository.findAll();
    }

    // Retorna apenas as ações de um usuário específico
    public List<Auditoria> listarPorUsuario(String usuarioResponsavel) {
        return auditoriaRepository.findByUsuarioResponsavel(usuarioResponsavel);
    }

    // Retorna apenas as ações de um tipo específico (ex: "PEDIDO_CRIADO")
    public List<Auditoria> listarPorAcao(String acao) {
        return auditoriaRepository.findByAcao(acao);
    }
}
// AuditoriaService não faz validação, só registra o que aconteceu.
// O metodo registrarAuditoria() é chamado pelo PedidoService e ClienteService sempre que uma ação importante ocorre.
// usuarioResponsavel e perfilUsuario são capturados automaticamente do Spring Security, não precisam ser passados como parâmetro.
// dataHora é preenchida automaticamente pelo sistema com LocalDateTime.now().