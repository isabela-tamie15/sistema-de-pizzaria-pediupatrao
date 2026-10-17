package com.umc.pediupatrao.entity;

public class ItemPedido {

    private String nomeProduto;
    private int quantidade;
    private double precoUnitario;

    // Getters e Setters

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
}

// ItemPedido representa uma linha de produto dentro do pedido
// Pode ter mais de um produto nessa linha.
// Exemplo: 2x Pizza Calabresa por R$35,00 cada.
// ItemPedido não tem @Document pois não tem coleção própria no banco,
// fica embutido dentro do documento Pedido.
