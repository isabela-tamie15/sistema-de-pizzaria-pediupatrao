package com.umc.pediupatrao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "auditoria")
public class Auditoria {

    @Id
    private String id;

    private String acao;

    private String usuarioResponsavel;

    // Perfil do usuário que executou a ação (ADMIN, GERENTE, ATENDENTE)
    private String perfilUsuario;

    // Identificador da entidade afetada (id do pedido ou do cliente)
    private String entidadeId;

    private String descricao;

    // Valor do campo antes da alteração para rastrear mudanças
    private String valorAnterior;

    // Valor do campo após a alteração para rastrear mudanças
    private String valorNovo;

    // Justificativa obrigatória para cancelamentos
    private String justificativa;

    private LocalDateTime dataHora;

    // Getters e Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getUsuarioResponsavel() {
        return usuarioResponsavel;
    }

    public void setUsuarioResponsavel(String usuarioResponsavel) {
        this.usuarioResponsavel = usuarioResponsavel;
    }

    public String getPerfilUsuario() {
        return perfilUsuario;
    }

    public void setPerfilUsuario(String perfilUsuario) {
        this.perfilUsuario = perfilUsuario;
    }

    public String getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(String entidadeId) {
        this.entidadeId = entidadeId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNovo() {
        return valorNovo;
    }

    public void setValorNovo(String valorNovo) {
        this.valorNovo = valorNovo;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}

// Atributos de Auditoria:
// acao: mostra o que aconteceu ("PEDIDO_CRIADO", "PEDIDO_CANCELADO", etc)
// usuarioResponsavel: mostra o login de quem executou a ação
// perfilUsuario: mostra o perfil de quem executou (ADMIN, GERENTE, ATENDENTE)
// entidadeId: id do pedido ou cliente afetado pela ação
// descricao: mostra detalhes do que aconteceu
// valorAnterior: valor do campo antes da alteração (ex: status anterior do pedido)
// valorNovo: valor do campo após a alteração (ex: novo status do pedido)
// justificativa: obrigatória em cancelamentos, opcional nas demais ações
// dataHora: mostra quando o evento aconteceu