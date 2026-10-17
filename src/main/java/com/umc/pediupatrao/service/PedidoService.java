package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.StatusPedido;
import com.umc.pediupatrao.exception.RegraDeNegocioException;
import com.umc.pediupatrao.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    // AuditoriaService injetado com @Autowired para registrar cada ação importante no histórico
    // PedidoService não acessa AuditoriaRepository diretamente, delega para AuditoriaService
    @Autowired
    private AuditoriaService auditoriaService;

    // Cria o pedido com status inicial RECEBIDO e data/hora automáticos
    // RF02: valorTotal calculado no servidor a partir dos itens (não aceita valor do formulário)
    // RF02: responsavelPeloRegistro capturado do usuário autenticado via SecurityContextHolder
    public Pedido criarPedido(Pedido pedido) {
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.RECEBIDO);

        // Captura o usuário autenticado para registrar como responsável pelo pedido (RF02)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        pedido.setResponsavelPeloRegistro(auth.getName());

        // Calcula o valorTotal a partir dos itens no servidor (RF02)
        // O formulário não deve definir o valor arbitrariamente, a regra de negócio é do backend
        if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            double total = pedido.getItens().stream()
                    .mapToDouble(item -> item.getPrecoUnitario() * item.getQuantidade())
                    .sum();
            pedido.setValorTotal(total);
        }

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // 1º parâmetro: nome da ação
        // 2º parâmetro: id do pedido afetado (entidadeId)
        // 3º parâmetro: descrição legível da ação
        // 4º parâmetro: valorAnterior = null (pedido não existia antes)
        // 5º parâmetro: valorNovo = status inicial do pedido
        // 6º parâmetro: justificativa = null (não exigida na criação)
        auditoriaService.registrarAuditoria(
                "PEDIDO_CRIADO",
                pedidoSalvo.getId(),
                "Pedido criado para: " + pedido.getNomeCliente()
                        + " | Responsável: " + pedido.getResponsavelPeloRegistro(),
                null,
                StatusPedido.RECEBIDO.name(),
                null
        );
        return pedidoSalvo;
    }

    // Busca um pedido pelo id ou lança exceção personalizada se não encontrar
    // Usado pelo PedidoController nos métodos de detalhe, status, cancelamento e desconto
    public Pedido buscarPorId(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Pedido não encontrado com o id: " + id));
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    // Atualiza o status seguindo o fluxo obrigatório definido no PDF (RF03)
    // Antes de salvar, chama validarTransicaoDeStatus para garantir a sequência correta
    // valorAnterior e valorNovo são gravados na auditoria para rastreabilidade completa
    public Pedido atualizarStatus(String id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Pedido não encontrado com o id: " + id));

        StatusPedido statusAtual = pedido.getStatus();

        // Lança RegraDeNegocioException se a transição não for permitida pelo fluxo do PDF
        validarTransicaoDeStatus(statusAtual, novoStatus);

        // Guarda o status anterior antes de sobrescrever, para registrar na auditoria
        String valorAnterior = statusAtual.name();

        pedido.setStatus(novoStatus);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // RF03: registra data/hora e responsável quando pedido sai para entrega ou é retirado
        String descricao = "Status do pedido alterado";
        if (novoStatus == StatusPedido.SAIU_PARA_ENTREGA || novoStatus == StatusPedido.RETIRADO) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            descricao = "Saída registrada por: " + auth.getName()
                    + " em " + LocalDateTime.now();
        }

        auditoriaService.registrarAuditoria(
                "STATUS_ATUALIZADO",
                id,
                descricao,
                valorAnterior,
                novoStatus.name(),
                null
        );
        return pedidoSalvo;
    }

    // Valida se a transição de status segue o fluxo obrigatório do PDF (RF03):
    // RECEBIDO → EM_PREPARACAO → PRONTO → SAIU_PARA_ENTREGA ou RETIRADO → FINALIZADO
    // CANCELADO e FINALIZADO são estados terminais: nenhuma transição é permitida a partir deles
    private void validarTransicaoDeStatus(StatusPedido statusAtual, StatusPedido novoStatus) {
        boolean transicaoValida = false;

        // Cada case representa os status que são destino válido a partir do status atual
        switch (statusAtual) {
            case RECEBIDO:
                transicaoValida = novoStatus == StatusPedido.EM_PREPARACAO;
                break;
            case EM_PREPARACAO:
                transicaoValida = novoStatus == StatusPedido.PRONTO;
                break;
            case PRONTO:
                // Do PRONTO pode ir para entrega (motoboy) ou retirada (balcão)
                transicaoValida = novoStatus == StatusPedido.SAIU_PARA_ENTREGA
                        || novoStatus == StatusPedido.RETIRADO;
                break;
            case SAIU_PARA_ENTREGA:
                transicaoValida = novoStatus == StatusPedido.FINALIZADO;
                break;
            case RETIRADO:
                transicaoValida = novoStatus == StatusPedido.FINALIZADO;
                break;
            case FINALIZADO:
            case CANCELADO:
                // Estados terminais: não permitem nenhuma transição
                transicaoValida = false;
                break;
        }

        if (!transicaoValida) {
            throw new RegraDeNegocioException(
                    "Transição de status inválida: " + statusAtual + " → " + novoStatus
                            + ". Verifique o fluxo obrigatório."
            );
        }
    }

    // Cancela um pedido, tarefa exclusiva do GERENTE (RF04)
    // Só é permitido antes do pedido sair para entrega ou ser retirado
    // justificativa é obrigatória e gravada na auditoria (exigência do PDF)
    public Pedido cancelarPedido(String id, String justificativa) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Pedido não encontrado com o id: " + id));

        StatusPedido statusAtual = pedido.getStatus();

        // Pedidos em trânsito, retirados, finalizados ou já cancelados não podem ser cancelados
        if (statusAtual == StatusPedido.SAIU_PARA_ENTREGA
                || statusAtual == StatusPedido.RETIRADO
                || statusAtual == StatusPedido.FINALIZADO
                || statusAtual == StatusPedido.CANCELADO) {
            throw new RegraDeNegocioException(
                    "Não é possível cancelar um pedido com status: " + statusAtual
            );
        }

        // justificativa é campo obrigatório para cancelamento conforme PDF RF04
        if (justificativa == null || justificativa.trim().isEmpty()) {
            throw new RegraDeNegocioException("A justificativa é obrigatória para cancelar um pedido.");
        }

        String valorAnterior = statusAtual.name();
        pedido.setStatus(StatusPedido.CANCELADO);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // justificativa é passada como 6º parâmetro, único método que usa este campo
        auditoriaService.registrarAuditoria(
                "PEDIDO_CANCELADO",
                id,
                "Pedido cancelado pelo GERENTE",
                valorAnterior,
                StatusPedido.CANCELADO.name(),
                justificativa
        );
        return pedidoSalvo;
    }

    // Aplica desconto ao pedido, tarefa exclusiva do GERENTE, máximo de 20% (RF04)
    // valorAnterior = valor original antes do desconto
    // valorNovo = valor final após o desconto aplicado
    public Pedido aplicarDesconto(String id, Double percentualDesconto) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Pedido não encontrado com o id: " + id));

        // Limite máximo de 20% conforme regra de negócio do PDF RF04
        if (percentualDesconto == null || percentualDesconto <= 0 || percentualDesconto > 20) {
            throw new RegraDeNegocioException(
                    "Desconto inválido. O valor deve ser entre 1% e 20%."
            );
        }

        Double valorOriginal = pedido.getValorTotal();
        Double valorDesconto = valorOriginal * (percentualDesconto / 100);
        Double valorComDesconto = valorOriginal - valorDesconto;

        // Atualiza os campos no pedido antes de salvar
        pedido.setDesconto(percentualDesconto);
        pedido.setValorTotal(valorComDesconto);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        auditoriaService.registrarAuditoria(
                "DESCONTO_APLICADO",
                id,
                "Desconto de " + percentualDesconto + "% aplicado pelo GERENTE",
                "R$ " + valorOriginal,
                "R$ " + valorComDesconto,
                null
        );
        return pedidoSalvo;
    }

}