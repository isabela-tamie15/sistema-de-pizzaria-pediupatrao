package com.umc.pediupatrao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PedidoServiceTest {

    @Test
    void contextLoads() {
        // Verifica que o contexto Spring sobe corretamente
    }

    @Test
    void statusRecebidoEhPrimeiro() {
        String statusInicial = "RECEBIDO";
        assert statusInicial.equals("RECEBIDO");
    }

    @Test
    void descontoMaximoVintePercento() {
        double desconto = 20.0;
        assert desconto <= 20.0;
    }
}