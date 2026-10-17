package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.StatusPedido;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

// Antes, esta classe era @RestController, o que era inadequado, pois ela não retornava JSON.
// Agora é @Controller, pois retorna o nome de um template HTML para o navegador renderizar via Thymeleaf.
// Toda controller que trabalha com telas HTML deve usar @Controller.
@Controller

// Antes, era @RequestMapping("/api/pedidos"), mas estava errado, pois o prefixo /api significa que retorna JSON.
// Como esta classe não retorna JSON, a URL ficou limpa @RequestMapping("/pedidos").
// @RequestMapping no início da classe define o prefixo comum (/pedido) de todas as rotas abaixo.
// Exemplo: /pedidos + /novo = /pedidos/novo | /pedidos + /{id} = /pedidos/{id}
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // CORREÇÃO: injetados para popular os dropdowns do formulário de novo pedido
    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    // Quando se tem páginas html, só existem 2 métodos http: GET para abrir formulário vazio e POST para salvar os dados.
    // O metodo exibirFormulario cria um Pedido vazio e manda para o html,
    // só para saber quais campos existem e onde guardar o que o usuário digitar.
    // Sem este metodo, o Thymeleaf não consegue montar o formulário e lança erro.

    // CORREÇÃO: agora passa clientes e produtos para o HTML montar os <select> de cliente e produto
    @GetMapping("/novo")
    public String exibirFormulario(Model model) {
        model.addAttribute("pedido", new Pedido());
        model.addAttribute("clientes", clienteService.listarClientes());
        model.addAttribute("produtos", produtoService.listarProdutos());
        return "pedidos/form";
    }

    // @RequestBody é usado com @RestController para ler dados que chegam em formato JSON no corpo da requisição.
    // @ModelAttribute é usado com @Controller para ler dados que chegam de um <form> HTML e preenche o objeto Pedido automaticamente.
    // redirect:/pedidos serve para, após salvar, redirecionar o navegador para GET /pedidos (lista).
    // isso evita que, ao atualizar a página, o pedido seja criado duas vezes.
    @PostMapping("/salvar")
    public String salvarPedido(@ModelAttribute Pedido pedido) {
        pedidoService.criarPedido(pedido);
        return "redirect:/pedidos";
    }

    // Só listarPedidos e detalharPedido usam Model pois são os únicos que precisam exibir dados na tela.
    // salvarPedido e atualizarStatus só executam uma ação e redirecionam, não exibem nada.
    @GetMapping
    public String listarPedidos(Model model) {
        List<Pedido> pedidos = pedidoService.listarPedidos();
        model.addAttribute("pedidos", pedidos);
        return "pedidos/lista";
    }

    // @PathVariable captura o {id} que está dentro da URL. Exemplo: /pedidos/abc123 → id = "abc123"
    // @RequestParam captura valores que chegam depois do ?: Exemplo: /pedidos?status=PRONTO → status = PRONTO
    // buscarPorId busca o pedido no banco pelo id para exibir na tela de detalhe.
    // CORREÇÃO: em vez de mandar StatusPedido.values() (todos os status), agora monta apenas os próximos
    // status válidos para o pedido atual, evitando que o usuário selecione transições inválidas e cause 500.
    @GetMapping("/{id}")
    public String detalharPedido(@PathVariable String id, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);
        model.addAttribute("pedido", pedido);

        // CORREÇÃO: statusOpcoes agora contém apenas as transições permitidas pelo fluxo (RF03)
        // Antes era StatusPedido.values(), que mostrava todos e permitia transições inválidas
        List<StatusPedido> proximosStatus = new ArrayList<>();
        switch (pedido.getStatus()) {
            case RECEBIDO          -> proximosStatus.add(StatusPedido.EM_PREPARACAO);
            case EM_PREPARACAO     -> proximosStatus.add(StatusPedido.PRONTO);
            case PRONTO            -> {
                proximosStatus.add(StatusPedido.SAIU_PARA_ENTREGA);
                proximosStatus.add(StatusPedido.RETIRADO);
            }
            case SAIU_PARA_ENTREGA -> proximosStatus.add(StatusPedido.FINALIZADO);
            case RETIRADO          -> proximosStatus.add(StatusPedido.FINALIZADO);
            default                -> {} // FINALIZADO e CANCELADO: lista vazia (card some pelo th:if do HTML)
        }
        model.addAttribute("statusOpcoes", proximosStatus);

        return "pedidos/detalhe";
    }

    // Antes era @PutMapping("/{id}/status"), mas HTML não suporta PUT,
    // o navegador só conhece GET e POST. Um <form method="put"> simplesmente não funciona.
    // Agora é @PostMapping("/{id}/status"), pois o formulário HTML usa POST para enviar a alteração.
    // @PathVariable captura o {id} da URL para saber qual pedido alterar.
    // @RequestParam captura o valor do status enviado pelo <select> do formulário: ?status=PRONTO
    // redirect:/pedidos serve para, após atualizar, redirecionar para a lista, assim como em salvarPedido.
    // isso evita que, ao atualizar a página, o pedido seja criado duas vezes.
    @PostMapping("/{id}/status")
    public String atualizarStatus(@PathVariable String id, @RequestParam StatusPedido status) {
        pedidoService.atualizarStatus(id, status);
        return "redirect:/pedidos";
    }

    // Rota exclusiva do GERENTE para cancelar um pedido (RF04)
    // @RequestParam justificativa captura o texto digitado no formulário de cancelamento
    // A restrição de acesso ao GERENTE está no SecurityConfig: /pedidos/*/cancelar hasRole("GERENTE")
    // A PedidoService valida se o status atual ainda permite cancelamento antes de executar
    @PostMapping("/{id}/cancelar")
    public String cancelarPedido(@PathVariable String id, @RequestParam String justificativa) {
        pedidoService.cancelarPedido(id, justificativa);
        return "redirect:/pedidos";
    }

    // Rota exclusiva do GERENTE para aplicar desconto (RF04)
    // @RequestParam percentualDesconto captura o número digitado no formulário (ex: 15 para 15%)
    // A restrição de acesso ao GERENTE está no SecurityConfig: /pedidos/*/desconto hasRole("GERENTE")
    // A PedidoService valida se o percentual está entre 1% e 20% antes de aplicar
    @PostMapping("/{id}/desconto")
    public String aplicarDesconto(@PathVariable String id, @RequestParam Double percentualDesconto) {
        pedidoService.aplicarDesconto(id, percentualDesconto);
        return "redirect:/pedidos";
    }

}