package com.umc.pediupatrao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//Removi o @Entity e seu import, pois serve para usa JPA em banco sql, e aqui é banco nosql MongoDB
//@Entity é uma anotação do JPA (Java Persistence API) para bancos relacionais
//@Document é o equivalente MongoDB de @Entity

@Document(collection = "produtos")

public class Produto {
    
    @Id
    private String id;
    private String nome;
    private String tipo; // Pizza, Esfiha, Bebida, Lanche
    private Double preco;
    private Boolean ativo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    
}
