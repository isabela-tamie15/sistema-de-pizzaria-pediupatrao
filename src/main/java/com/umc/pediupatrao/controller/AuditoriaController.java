package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Controller foi utilizado pois cada metodo retorna uma string com
//o nome da tela (template). Ex: "auditoria/lista"
//@RestController é inadequado pois retornaria dados JSON para a API
//que fez a requisição (um app mobile, um Postman, etc.)
@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    // Exibe a lista completa ou filtrada, dependendo dos parâmetros recebidos
    // @RequestParam(required = false) aceita a URL com ou sem parâmetros
    @GetMapping
    public String listar(
            @RequestParam(required = false) String usuarioResponsavel,
            @RequestParam(required = false) String acao,
            Model model) {

        List<Auditoria> registros;

        if (usuarioResponsavel != null && !usuarioResponsavel.isBlank()) {
            registros = auditoriaService.listarPorUsuario(usuarioResponsavel);
            model.addAttribute("filtroUsuario", usuarioResponsavel);
        } else if (acao != null && !acao.isBlank()) {
            registros = auditoriaService.listarPorAcao(acao);
            model.addAttribute("filtroAcao", acao);
        } else {
            registros = auditoriaService.listarTodos();
        }

        model.addAttribute("registros", registros);
        return "auditoria/lista";
    }
}

// AuditoriaController só tem rotas GET, não tem POST, pois auditoria nunca é criada pelo usuário via navegador.
// Os registros de auditoria são criados automaticamente pelo PedidoService e ClienteService, e exibidos aqui somente para consulta.
// Model é a única forma de enviar dados do Java para o template Thymeleaf renderizar no navegador.

// O metodo listar() aceita filtros opcionais via @RequestParam(required = false):
// - sem parâmetros → retorna todos os registros
// - com usuarioResponsavel → filtra pelo usuário que gerou o evento
// - com acao → filtra pelo tipo de ação (ex: PEDIDO_CRIADO)
// Os parâmetros são enviados pelo formulário do template auditoria/lista.html via GET para /auditoria?param=valor.