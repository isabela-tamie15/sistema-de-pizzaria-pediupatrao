package com.umc.pediupatrao.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConfiguracoesController {

    @GetMapping("/funcionarios")
    public String funcionarios() {
        return "configuracoes/funcionarios";
    }

    @GetMapping("/entregadores")
    public String entregadores() {
        return "configuracoes/entregadores";
    }

    @GetMapping("/horarios")
    public String horarios() {
        return "configuracoes/horarios";
    }
}