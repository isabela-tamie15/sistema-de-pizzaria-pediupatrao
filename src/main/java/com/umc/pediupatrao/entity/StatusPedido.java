package com.umc.pediupatrao.entity;

public enum StatusPedido {

    RECEBIDO,
    EM_PREPARACAO,
    PRONTO,
    SAIU_PARA_ENTREGA,
    RETIRADO,
    FINALIZADO,
    CANCELADO
}

// Enum define os únicos status válidos para um pedido, seguindo o fluxo obrigatório do PDF.
// RECEBIDO → EM_PREPARACAO → PRONTO → SAIU_PARA_ENTREGA ou RETIRADO → FINALIZADO
// CANCELADO é estado excepcional, permitido apenas ao GERENTE antes da saída.
// O PedidoService valida as transições e bloqueia as inválidas com RegraDeNegocioException.
// StatusPedido não gera coleção no banco — fica embutido dentro do Pedido.