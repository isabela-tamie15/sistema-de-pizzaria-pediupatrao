# 🍕 Sistema de Pizzaria - PediuPatrão

> Sistema web de gerenciamento de pedidos para pizzaria, desenvolvido com **Spring Boot + MongoDB Atlas**, evoluído a partir do projeto base da disciplina. Implementa controle de acesso por perfil, fluxo controlado de status de pedidos, cancelamentos e descontos exclusivos do gerente, e auditoria persistente de operações.

---

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6-brightgreen?logo=springsecurity&logoColor=white)
![MongoDB Atlas](https://img.shields.io/badge/MongoDB-Atlas-green?logo=mongodb&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3-005F0F?logo=thymeleaf&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven&logoColor=white)

</div>

---
Projeto de sistema desenvolvido para a disciplina de **Análise e Projeto de Software** da Universidade de Mogi das Cruzes (UMC).

| |                                   |
|---|-----------------------------------|
| 👩‍🎓 **Autora** | Isabela Tamie Shihara             |
| 🪪 **RGM** | 11231203887                       |
| 🏫 **Turma** | 8ºB - Engenharia de Software      |

---

## 🎬 Demonstração em vídeo

> 🔗 **[Assistir no YouTube](https://youtu.be/dnC8DVx6CdE)** 

O vídeo demonstra: login com os três perfis, restrições de acesso por perfil, criação e acompanhamento de pedidos, aplicação de desconto e cancelamento pelo GERENTE, consulta à auditoria por ADMIN e GERENTE.

---

## 📋 Sumário

- [Demonstração em vídeo](#-demonstração-em-vídeo)
- [Como executar](#️-como-executar)
- [Usuários de demonstração](#-usuários-de-demonstração)
- [Requisitos implementados](#-requisitos-implementados)
- [Matriz de permissões](#-matriz-de-permissões)
- [Rotas da aplicação](#️-rotas-da-aplicação)
- [Testes manuais](#-testes-manuais)
- [Principais alterações em relação ao projeto base](#-principais-alterações-em-relação-ao-projeto-base)
- [Estrutura do projeto](#️-estrutura-do-projeto)

---

## ⚙️ Como executar

### Pré-requisitos

- **Java 21** (JDK)
- **Maven 3.9+** (ou use o wrapper `./mvnw` incluído no projeto)
- **Conta no MongoDB Atlas** — o banco de dados é na nuvem; não é necessário instalar MongoDB localmente

> 💡 **Por que MongoDB Atlas em vez de Docker?**
> Esta versão utiliza MongoDB Atlas (nuvem gratuita) no lugar de Docker Compose. A conexão já está configurada no `application.properties`. Quem for clonar e rodar localmente precisa criar sua própria conta no Atlas e substituir a connection string — veja o passo 3.

---

### Passo a passo

**1. Clone o repositório:**
```bash
git clone https://github.com/isabela-tamie15/sistema-de-pizzaria-pediupatrao.git
cd sistema-de-pizzaria-pediupatrao
```

**2. Configure a conexão com o MongoDB Atlas:**

Abra `src/main/resources/application.properties` e substitua a linha abaixo pela sua própria connection string do Atlas:

```properties
# Substitua pela sua connection string do MongoDB Atlas
spring.data.mongodb.uri=mongodb+srv://SEU_USUARIO:SUA_SENHA@seucluster.mongodb.net/pizzaria?retryWrites=true&w=majority
spring.data.mongodb.database=pizzaria
```

> 📌 Para criar uma conta gratuita: [mongodb.com/cloud/atlas](https://www.mongodb.com/cloud/atlas)
> Crie um cluster, um usuário de banco e libere seu IP no Network Access.

**3. Execute o projeto:**

Com Maven instalado:
```bash
mvn spring-boot:run
```

Ou com o wrapper do projeto (sem Maven instalado):
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

**4. Acesse no navegador:**

```
http://localhost:8080/login
```

> ✅ Na primeira inicialização, o `DataInitializer` cria automaticamente os três usuários de demonstração. Não é necessário cadastrar nada manualmente.

---

## 👥 Usuários de demonstração

Criados automaticamente pelo `DataInitializer` ao subir a aplicação, caso não existam:

| 👤 Usuário | 🔑 Senha | 🎭 Perfil |
|:---:|:---:|:---:|
| `admin` | `teste123` | ADMIN |
| `gerente` | `teste123` | GERENTE |
| `atendente` | `teste123` | ATENDENTE |

> As credenciais acima são apenas para demonstração acadêmica — nunca use senhas simples em sistemas reais.

---

## 📌 Requisitos implementados

### RF01 — Controle de usuários e permissões

- Perfis definidos como `String` na entidade `Usuario` (`ADMIN`, `GERENTE`, `ATENDENTE`), carregados do MongoDB pelo `UsuarioDetailsService`
- Autorização configurada no `SecurityConfig` com regras por URL (`.requestMatchers(...)`)
- `@EnableMethodSecurity` ativado para permitir `@PreAuthorize` nos controllers
- Menus ocultados por perfil via `sec:authorize` no Thymeleaf — acessar a URL sem permissão resulta em redirecionamento para `/acesso-negado`
- Cancelamento e desconto restritos à URL `/pedidos/*/cancelar` e `/pedidos/*/desconto` apenas para `ROLE_GERENTE`
- Gestão de usuários restrita à URL `/usuarios/**` e `/api/usuarios/**` apenas para `ROLE_ADMIN`
- CSRF ativo — formulários Thymeleaf recebem o token automaticamente

### RF02 — Evolução do cadastro de pedidos

- Entidade `Pedido` com campos: `nomeCliente`, `itens` (lista de `ItemPedido`), `status`, `dataPedido`, `valorTotal`, `observacoes`, `desconto`, `responsavelPeloRegistro`
- Valor total calculado no servidor pelo `PedidoService`, não pelo formulário
- Status controlado pelo enum `StatusPedido` com os únicos valores válidos do fluxo

### RF03 — Controle de entrada, saída e transições

Fluxo implementado no enum `StatusPedido` e validado pelo `PedidoService`:

```
RECEBIDO → EM_PREPARACAO → PRONTO → SAIU_PARA_ENTREGA
                                  ↘ RETIRADO           → FINALIZADO
```

```
(qualquer status antes da saída) → CANCELADO  ← exclusivo do GERENTE
```

- Transições fora do fluxo são bloqueadas pelo método `validarTransicaoDeStatus()` com exceção de negócio
- `CANCELADO` é estado terminal — nenhuma alteração posterior é aceita
- Responsável pelo registro capturado automaticamente via `SecurityContextHolder`

### RF04 — Cancelamentos e descontos

- **Cancelamento:** exclusivo do GERENTE, exige justificativa, bloqueado após saída para entrega/retirada
- **Desconto:** exclusivo do GERENTE, percentual máximo de 20%, recalculado no servidor
- Ambas as operações geram registro de auditoria automaticamente

### RF05 — Auditoria

- Entidade `Auditoria` com coleção `auditorias` no MongoDB
- Registra: usuário responsável, ação realizada, data e hora, e detalhes do evento
- Operações auditadas automaticamente pelo `AuditoriaService`:

| 📋 Ação | ⏱️ Quando |
|---|---|
| `PEDIDO_CRIADO` | Criação de pedido |
| `STATUS_ATUALIZADO` | Cada mudança de status |
| `DESCONTO_APLICADO` | Aplicação de desconto pelo GERENTE |
| `PEDIDO_CANCELADO` | Cancelamento com justificativa |

- Consulta exclusiva para `ADMIN` e `GERENTE` na tela **Auditoria**
- Filtros por `usuarioResponsavel` e `acao` via `@RequestParam(required = false)` — URL com ou sem parâmetros funciona

---

## 🔐 Matriz de permissões

| Funcionalidade | ADMIN | GERENTE | ATENDENTE |
|---|:---:|:---:|:---:|
| Administrar usuários | ✅ | ❌ | ❌ |
| Consultar pedidos | ✅ | ✅ | ✅ |
| Criar pedido e avançar status | ❌ | ✅ | ✅ |
| Aplicar desconto | ❌ | ✅ | ❌ |
| Cancelar pedido | ❌ | ✅ | ❌ |
| Consultar clientes | ✅ | ✅ | ✅ |
| Cadastrar e editar clientes | ❌ | ❌ | ✅ |
| Gerenciar produtos (cardápio) | ❌ | ✅ | ❌ |
| Consultar auditoria | ✅ | ✅ | ❌ |

---

## 🗺️ Rotas da aplicação

### Páginas (Thymeleaf MVC)

Todas as rotas abaixo exigem autenticação. O acesso sem login redireciona para `/login`.

| Método | Rota | Perfis permitidos | Descrição |
|---|---|---|---|
| GET | `/login` | Público | Tela de login |
| GET | `/` | Todos | Dashboard principal |
| GET | `/pedidos` | Todos | Lista de pedidos |
| GET | `/pedidos/novo` | GERENTE, ATENDENTE | Formulário de novo pedido |
| POST | `/pedidos/salvar` | GERENTE, ATENDENTE | Salva novo pedido |
| GET | `/pedidos/{id}` | Todos | Detalhe do pedido |
| POST | `/pedidos/{id}/status` | GERENTE, ATENDENTE | Atualiza status do pedido |
| POST | `/pedidos/{id}/cancelar` | GERENTE | Cancela pedido com justificativa |
| POST | `/pedidos/{id}/desconto` | GERENTE | Aplica desconto (máx. 20%) |
| GET | `/clientes` | GERENTE, ATENDENTE | Lista de clientes |
| GET | `/clientes/novo` | ATENDENTE | Formulário de novo cliente |
| POST | `/clientes/salvar` | ATENDENTE | Salva novo cliente |
| GET | `/clientes/editar/{id}` | ATENDENTE | Formulário de edição |
| POST | `/clientes/editar/{id}` | ATENDENTE | Salva edição do cliente |
| GET | `/produtos` | GERENTE | Lista do cardápio |
| GET | `/usuarios` | ADMIN | Lista de usuários |
| GET | `/usuarios/novo` | ADMIN | Formulário de novo usuário |
| POST | `/usuarios/salvar` | ADMIN | Salva novo usuário |
| GET | `/usuarios/editar/{id}` | ADMIN | Formulário de edição |
| POST | `/usuarios/editar/{id}` | ADMIN | Salva edição do usuário |
| POST | `/usuarios/deletar/{id}` | ADMIN | Remove usuário |
| GET | `/auditoria` | ADMIN, GERENTE | Lista de registros de auditoria |
| GET | `/auditoria?usuarioResponsavel=X` | ADMIN, GERENTE | Filtro por usuário |
| GET | `/auditoria?acao=X` | ADMIN, GERENTE | Filtro por tipo de ação |
| GET | `/acesso-negado` | Todos | Tela de acesso negado (403) |

### API REST

| Método | Rota | Perfis | Descrição |
|---|---|---|---|
| GET | `/api/clientes` | GERENTE, ATENDENTE | Lista clientes (JSON) |
| POST | `/api/clientes` | ATENDENTE | Cria cliente |
| PUT | `/api/clientes/{id}` | ATENDENTE | Atualiza cliente |
| DELETE | `/api/clientes/{id}` | GERENTE | Remove cliente |
| GET | `/api/produtos` | Todos | Lista produtos/cardápio (JSON) |
| POST | `/api/produtos` | GERENTE | Cria produto |
| PUT | `/api/produtos/{id}` | GERENTE | Atualiza produto |
| DELETE | `/api/produtos/{id}` | GERENTE | Remove produto |
| GET | `/api/usuarios` | ADMIN | Lista usuários (JSON) |
| GET | `/api/usuarios/{id}` | ADMIN | Detalha usuário |
| POST | `/api/usuarios` | ADMIN | Cria usuário |
| PUT | `/api/usuarios/{id}` | ADMIN | Atualiza usuário |
| DELETE | `/api/usuarios/{id}` | ADMIN | Remove usuário |

---

## 🧪 Testes manuais

Roteiro de testes para verificar os requisitos implementados:

### 1. Autenticação e controle de acesso

| # | Ação | Resultado esperado |
|---|---|---|
| 1 | Acessar `localhost:8080` sem login | Redirecionado para `/login` |
| 2 | Login com `admin` / `teste123` | Acessa dashboard, menu sem opções de pedido |
| 3 | Tentar acessar `/pedidos/novo` como `admin` | Redirecionado para `/acesso-negado` |
| 4 | Login com `atendente` / `teste123` | Acessa pedidos e clientes, sem ver auditoria |
| 5 | Tentar acessar `/auditoria` como `atendente` | Redirecionado para `/acesso-negado` |
| 6 | Login com `gerente` / `teste123` | Acessa pedidos, clientes, auditoria e cardápio |

### 2. Fluxo de pedidos

| # | Ação | Resultado esperado |
|---|---|---|
| 7 | Criar pedido como `atendente` | Pedido salvo com status `RECEBIDO` e responsável preenchido |
| 8 | Avançar status para `EM_PREPARACAO` | Transição aceita |
| 9 | Tentar avançar direto para `FINALIZADO` | Erro — transição inválida |
| 10 | Avançar até `PRONTO` → `SAIU_PARA_ENTREGA` → `FINALIZADO` | Todas as transições aceitas em ordem |

### 3. Cancelamento e desconto (apenas GERENTE)

| # | Ação | Resultado esperado |
|---|---|---|
| 11 | Criar pedido como `atendente`, logar como `gerente` e cancelar | Pedido com status `CANCELADO` |
| 12 | Tentar cancelar sem justificativa | Erro de validação |
| 13 | Aplicar desconto de 10% como `gerente` | Valor total recalculado |
| 14 | Tentar aplicar desconto de 25% | Erro — limite é 20% |
| 15 | Tentar cancelar pedido já `FINALIZADO` | Operação bloqueada |

### 4. Auditoria

| # | Ação | Resultado esperado |
|---|---|---|
| 16 | Logar como `admin` e acessar `/auditoria` | Lista todos os eventos registrados |
| 17 | Filtrar por `usuarioResponsavel=gerente` | Apenas eventos gerados pelo gerente |
| 18 | Logar como `atendente` e acessar `/auditoria` | Redirecionado para `/acesso-negado` |

### 5. Gestão de usuários (apenas ADMIN)

| # | Ação | Resultado esperado |
|---|---|---|
| 19 | Logar como `admin` e criar novo usuário | Usuário salvo no banco com senha criptografada |
| 20 | Logar como `gerente` e tentar acessar `/usuarios` | Redirecionado para `/acesso-negado` |

---

## 📝 Principais alterações em relação ao projeto base

| 🔧 Área | Projeto base | ✅ Esta versão |
|---|---|---|
| Perfis de acesso | Todos autenticados recebiam `USER` | `ADMIN`, `GERENTE` e `ATENDENTE` distintos, lidos do MongoDB |
| CSRF | Desabilitado | Reativado — formulários Thymeleaf protegidos automaticamente |
| Status do pedido | Campo de texto livre, qualquer valor aceito | Enum `StatusPedido` com fluxo validado pelo `PedidoService` |
| Transições de status | Sem validação | `validarTransicaoDeStatus()` bloqueia transições inválidas |
| Cancelamento | Inexistente | Exclusivo do GERENTE, exige justificativa, bloqueado após saída |
| Desconto | Inexistente | Exclusivo do GERENTE, máximo 20%, recalculado no servidor |
| Auditoria | Inexistente | `AuditoriaService` registra automaticamente cada operação relevante |
| Filtro de auditoria | — | `@RequestParam(required = false)` permite filtrar por usuário ou ação |
| Banco de dados | MongoDB local | MongoDB Atlas (nuvem), sem necessidade de instalar localmente |
| Usuários iniciais | Criados manualmente | `DataInitializer` cria os 3 perfis automaticamente no startup |
| Segurança por URL | Sem regras detalhadas | `SecurityConfig` com regras específicas por rota e perfil |

---

## 🗂️ Estrutura do projeto

```
sistema-de-pizzaria-pediupatrao/
├── src/
│   ├── main/
│   │   ├── java/com/umc/pediupatrao/
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java      ← cria usuários admin/gerente/atendente no startup
│   │   │   │   ├── MongoInitConfig.java
│   │   │   │   └── SecurityConfig.java       ← regras de acesso por URL e perfil
│   │   │   ├── controller/
│   │   │   │   ├── AuditoriaController.java  ← consulta de registros com filtros opcionais
│   │   │   │   ├── ClienteController.java    ← API REST de clientes
│   │   │   │   ├── HomeController.java       ← páginas Thymeleaf (usuários, clientes, produtos)
│   │   │   │   ├── PedidoController.java     ← fluxo de pedidos (MVC)
│   │   │   │   ├── ProdutoController.java    ← API REST de produtos
│   │   │   │   └── UsuarioController.java    ← API REST de usuários
│   │   │   ├── entity/
│   │   │   │   ├── Auditoria.java
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── ItemPedido.java
│   │   │   │   ├── Pedido.java               ← @Document MongoDB, inclui enum StatusPedido
│   │   │   │   ├── Produto.java
│   │   │   │   ├── StatusPedido.java         ← enum com os status válidos do fluxo
│   │   │   │   └── Usuario.java
│   │   │   ├── repository/
│   │   │   │   └── *.java                    ← repositórios Spring Data MongoDB
│   │   │   └── service/
│   │   │       ├── AuditoriaService.java     ← persiste registros de auditoria
│   │   │       ├── PedidoService.java        ← regras de negócio, validação de transições
│   │   │       └── UsuarioDetailsService.java← integração com Spring Security
│   │   └── resources/
│   │       ├── application.properties        ← configuração do MongoDB Atlas
│   │       └── templates/                    ← páginas HTML com Thymeleaf
├── pom.xml
└── README.md
```

