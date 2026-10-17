package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Auditoria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends MongoRepository<Auditoria, String> {

    // Busca todos os registros de auditoria de um usuário específico
    // Exemplo: ver tudo que o usuário "Paulo Silva" fez no sistema
    List<Auditoria> findByUsuarioResponsavel(String usuarioResponsavel);

    // Busca todos os registros de um tipo de ação específica
    // Exemplo: ver todos os "PEDIDO_CANCELADO" do sistema
    List<Auditoria> findByAcao(String acao);
}

//AuditoriaRepository herda os métodos prontos de MongoRepository (save, findAll, findById, delete).
//MongoRepository não é criado no projeto, vem da biblioteca spring-data-mongodb do pom.xml.
//findByUsuarioResponsavel e findByAcao são métodos extras que não são
//herdados pelo MongoRepository, por isso devem ser criados nesta interface.

//Os métodos desta interface não tem corpo, pois o Spring os implementa
//automaticamente em tempo de execução, lendo o nome do metodo, formado
//por findBy + nomeAtributo, e gerando a query correspondente no banco de dados.
//Exemplo: metodo findByUsuarioResponsavel → procura o atributo usuarioResponsavel na
//classe Auditoria → gera automaticamente a query WHERE usuarioResponsavel = ?.
//Se o nome não bater com o atributo, dá erro na inicialização.