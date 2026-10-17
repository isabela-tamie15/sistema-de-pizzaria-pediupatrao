package com.umc.pediupatrao.exception;

public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}

//super se refere à super classe RuntimeException que a classe atual está herdando.
//a mensagem de erro da superclasse é exibida nesta classe, apenas para personalizar
//a exceção e mostrar que se trata de exceção nas regras de negócio da pizzaria.

//Essa exceção personalizada é usada quando ocorre erro de regra de negócio, não erro técnico.
//Erro de regra de negócio: o usuário tentou fazer algo que não é permitido pelas regras da pizzaria.
//Exemplo: cancelar pedido já entregue, criar produto com preço negativo, fazer pedido sem nenhum item.

