# 🏆 Placar Realtime - Visão Geral da Implementação

Sistema completo de gerenciamento e atualização em tempo real de placares de jogos de futebol, desenvolvido com arquitetura de microserviços event-driven.

---

## 📋 Visão Geral do Sistema

Sistema moderno e escalável para gerenciamento de placares esportivos em tempo real, desenvolvido como desafio técnico utilizando tecnologias enterprise Java e ferramentas de código aberto.

### 🎯 Objetivo Principal

Criar uma plataforma robusta que permite:
- **Gestão administrativa** de jogos e eventos esportivos
- **Publicação de eventos** em tempo real via mensageria
- **Cache distribuído** para alta performance
- **API REST** para integração com frontends
- **Server-Sent Events (SSE)** para atualizações em tempo real

---

## 🏗️ Arquitetura do Sistema

### Diagrama de Arquitetura

```
                    🌐 SPA RealTime(Angular)
                              ↑
                            [SSE]
                              ↑
┌─────────────────────────────────────────────────────────────────┐
│                       🐳 Docker Infrastructure                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  📊 wicket-publisher         📨 RabbitMQ          📈 rest-consumer│
│  (Payara:8080)              (5672/15672)         (Spring:8585)  │
│  ┌─────────────────┐        ┌─────────────┐      ┌─────────────┐ │
│  │ Apache Wicket   │        │   Exchange  │      │ SSE Server  │ │
│  │ + REST API      │───────▶│   Queues    │─────▶│ + Cache     │ │
│  │ + JPA/Hibernate │        │    DLQ      │      │ + REST API  │ │
│  └─────────────────┘        └─────────────┘      └─────────────┘ │
│           │                                               │      │
│           ▼                                               ▼      │
│  🗄️ PostgreSQL 15                            🗄️ Redis 7        │
│  (5432)                                       (6379)             │
│  ┌─────────────────┐                         ┌─────────────────┐ │
│  │ Persistent Data │                         │ Cache Layer     │ │
│  │ + Transactions  │                         │ + Fast Access  │ │
│  └─────────────────┘                         └─────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Fluxo de Dados

```
1. 👤 Administrador ─── Wicket UI ──▶ wicket-publisher
                                           │
2. 💾 CRUD Jogos ──────── JPA/Hibernate ──▶ PostgreSQL
                                           │
3. 📨 Eventos ─────────── RabbitMQ ──────▶ rest-consumer
                                           │
4. 🗄️ Cache ──────────── Redis ──────────▶ Fast Access
                                           │
5. 📡 SSE ────────────── WebSocket ──────▶ SPA Angular RealTime
```

---

## 📦 Módulos do Sistema

### 1. **shared-domain** 
**Módulo compartilhado entre Publisher e Consumer**

- **Entities JPA:** `Jogo`, `StatusJogo`, `PlacarAtualizadoEvent`
- **Enums:** Status e tipos de eventos
- **Configuração:** Annotations JPA/Hibernate
- **Formato:** JAR library

**Tecnologias:** Jakarta Persistence API, Hibernate Annotations

---

### 2. **wicket-publisher** ⭐
**Aplicação administrativa web (Jakarta EE + Wicket)**

#### Funcionalidades:
- ✅ **CRUD completo de jogos** (criar, listar, editar, excluir)
- ✅ **Gerenciamento de eventos** (início, gols, encerramento)
- ✅ **Interface web responsiva** com Apache Wicket
- ✅ **REST API completa** para integração externa
- ✅ **Publicação automática** de eventos no RabbitMQ
- ✅ **Persistência transacional** via JPA

#### Camadas Implementadas:
- **Presentation:** Wicket Pages e Components + JAX-RS REST
- **Service:** Lógica de negócio e coordenação
- **Repository:** Acesso a dados via JPA
- **Integration:** RabbitMQ Publisher
- **DTO:** Transferência de dados (MapStruct)

**Deploy:** Payara Server 6 (WAR)  
**Context-root:** `/wicket-publisher`  
**Porta:** 8080

---

### 3. **rest-consumer** ⭐
**Aplicação Spring Boot de consumo e API**

#### Funcionalidades:
- ✅ **Consumo de eventos** do RabbitMQ
- ✅ **Cache distribuído** com Redis
- ✅ **REST API** para consultas
- ✅ **Server-Sent Events (SSE)** para tempo real
- ✅ **Health checks** e métricas (Actuator)
- ✅ **Circuit breaker** e retry (Resilience4j)
- ✅ **Dead Letter Queue** (DLQ) para mensagens problemáticas

#### Componentes:
- **RabbitMQ Listener:** Consome mensagens da fila
- **Redis Repository:** Gerencia cache de jogos
- **SSE Broadcaster:** Envia eventos para clientes conectados
- **REST Controllers:** API de consulta
- **Circuit Breaker:** Proteção contra falhas

**Runtime:** Spring Boot (JAR executável)  
**Porta:** 8585

---

## 🛠️ Tecnologias e Versões

### Backend Publisher (Wicket)

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| **Jakarta EE** | 10.0.0 | Plataforma enterprise |
| **Apache Wicket** | 10.0.0 | Framework web component-based |
| **Payara Server** | 6.2023.5 | Application server (Jakarta EE 10) |
| **JPA/Hibernate** | 6.2.13 | ORM - Mapeamento objeto-relacional |
| **PostgreSQL Driver** | 42.7.1 | Conexão com banco de dados |
| **RabbitMQ Client** | 5.20.0 | Publicação de mensagens AMQP |
| **Jackson** | 2.15.3 | Serialização JSON |
| **MapStruct** | 1.5.5 | Mapeamento entre DTOs e Entities |
| **Lombok** | 1.18.30 | Redução de boilerplate code |
| **SLF4J** | 2.0.9 | Logging abstraction |
| **MicroProfile OpenAPI** | 3.1 | Documentação automática da API |

### Backend Consumer (Spring Boot)

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| **Spring Boot** | 3.2.2 | Framework de aplicação |
| **Spring AMQP** | - | Integração com RabbitMQ |
| **Spring Data Redis** | - | Integração com Redis (cache) |
| **Spring Web** | - | REST API e SSE |
| **Spring Actuator** | - | Health checks e métricas |
| **Resilience4j** | 2.1.0 | Circuit breaker e retry |
| **Lettuce** | - | Client Redis (via Spring Data) |
| **Jackson** | 2.15.x | Serialização JSON |
| **Lombok** | 1.18.30 | Redução de boilerplate code |
| **SpringDoc OpenAPI** | 2.3.0 | Documentação Swagger |

### Infraestrutura

| Serviço | Versão | Porta | Propósito |
|---------|--------|-------|-----------|
| **PostgreSQL** | 15-alpine | 5432 | Banco de dados relacional |
| **Redis** | 7-alpine | 6379 | Cache distribuído em memória |
| **RabbitMQ** | 3.12-management | 5672, 15672 | Message broker AMQP |
| **Payara Server** | 6.2023.5-jdk17 | 8080, 4848 | Application server Jakarta EE |
| **pgAdmin** | latest | 5050 | Interface web PostgreSQL |

### Ferramentas de Desenvolvimento

| Ferramenta | Versão | Propósito |
|------------|--------|-----------|
| **Maven** | 3.9+ | Build e gerenciamento de dependências |
| **JDK** | 17 (Eclipse Temurin) | Java Development Kit |
| **Docker** | - | Containerização |
| **Docker Compose** | - | Orquestração de containers |

---

## 🌐 Endpoints REST - Wicket Publisher

### Base URL
`http://localhost:8080/wicket-publisher/rest/api/v1/jogos`

### Endpoints Disponíveis

#### 1. **Listar Jogos**
```http
GET /api/v1/jogos
```

**Parâmetros de Query (Opcionais):**
- `timeA`: Filtrar por nome do Time A
- `timeB`: Filtrar por nome do Time B  
- `status`: Filtrar por status (NAO_INICIADO, EM_ANDAMENTO, FINALIZADO)

**Exemplo cURL:**
```bash
# Listar todos os jogos
curl -X GET "http://localhost:8080/wicket-publisher/rest/api/v1/jogos" \
  -H "Accept: application/json"

# Filtrar jogos em andamento
curl -X GET "http://localhost:8080/wicket-publisher/rest/api/v1/jogos?status=EM_ANDAMENTO" \
  -H "Accept: application/json"
```

#### 2. **Buscar Jogo por ID**
```http
GET /api/v1/jogos/{id}
```

**Exemplo cURL:**
```bash
curl -X GET "http://localhost:8080/wicket-publisher/rest/api/v1/jogos/1" \
  -H "Accept: application/json"
```

#### 3. **Criar Novo Jogo**
```http
POST /api/v1/jogos
```

**Exemplo cURL:**
```bash
curl -X POST "http://localhost:8080/wicket-publisher/rest/api/v1/jogos" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "timeA": "Flamengo",
    "timeB": "Vasco",
    "dataHoraInicioPartida": "2026-02-15T15:00:00"
  }'
```

#### 4. **Atualizar Jogo**
```http
PUT /api/v1/jogos/{id}
```

**Exemplo cURL:**
```bash
curl -X PUT "http://localhost:8080/wicket-publisher/rest/api/v1/jogos/1" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "id": 1,
    "timeA": "Flamengo",
    "timeB": "Vasco",
    "placarA": 2,
    "placarB": 1,
    "status": "EM_ANDAMENTO"
  }'
```

#### 5. **Deletar Jogo**
```http
DELETE /api/v1/jogos/{id}
```

**Exemplo cURL:**
```bash
curl -X DELETE "http://localhost:8080/wicket-publisher/rest/api/v1/jogos/1" \
  -H "Accept: application/json"
```

#### 6. **Iniciar Jogo**
```http
POST /api/v1/jogos/{id}/iniciar
```

**Exemplo cURL:**
```bash
curl -X POST "http://localhost:8080/wicket-publisher/rest/api/v1/jogos/1/iniciar" \
  -H "Accept: application/json"
```

#### 7. **Finalizar Jogo**
```http
POST /api/v1/jogos/{id}/finalizar
```

**Exemplo cURL:**
```bash
curl -X POST "http://localhost:8080/wicket-publisher/rest/api/v1/jogos/1/finalizar" \
  -H "Accept: application/json"
```

#### 8. **Atualizar Placar**
```http
PUT /api/v1/jogos/{id}/placar
```

**Exemplo cURL:**
```bash
curl -X PUT "http://localhost:8080/wicket-publisher/rest/api/v1/jogos/1/placar" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "placarA": 3,
    "placarB": 1
  }'
```

#### 9. **Health Check**
```http
GET /api/v1/jogos/health
```

**Exemplo cURL:**
```bash
curl -X GET "http://localhost:8080/wicket-publisher/rest/api/v1/jogos/health" \
  -H "Accept: application/json"
```

### 📖 Documentação Swagger/OpenAPI - Publisher

**Swagger UI (Interface Web):**
```
http://localhost:8080/wicket-publisher/swagger-ui/
```

**OpenAPI Specification (JSON/YAML):**
```
http://localhost:8080/wicket-publisher/openapi
http://localhost:8080/wicket-publisher/api/openapi
http://localhost:8080/wicket-publisher/rest/openapi
```

**Arquivo Estático (sempre disponível):**
```
http://localhost:8080/wicket-publisher/swagger-ui/openapi.yaml
```

---

## 🌐 Endpoints REST - REST Consumer

### Base URL
`http://localhost:8585/consumer/api`

### Endpoints de Consulta

#### 1. **Buscar Jogo no Cache**
```http
GET /consumer/api/games/{id}
```

**Exemplo cURL:**
```bash
curl -X GET "http://localhost:8585/consumer/api/games/1" \
  -H "Accept: application/json"
```

#### 2. **Criar/Atualizar Jogo no Cache**
```http
POST /consumer/api/games
```

**Exemplo cURL:**
```bash
curl -X POST "http://localhost:8585/consumer/api/games" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "id": 1,
    "timeA": "Flamengo",
    "timeB": "Vasco",
    "placarA": 2,
    "placarB": 1,
    "status": "EM_ANDAMENTO"
  }'
```

### Endpoints Server-Sent Events (SSE)

#### 1. **Novos Jogos**
```http
GET /consumer/api/sse/games/novos
```

**Exemplo cURL:**
```bash
# Conectar ao stream de novos jogos
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8585/consumer/api/sse/games/novos"
```

#### 2. **Jogos Iniciados**
```http
GET /consumer/api/sse/games/inicio
```

**Exemplo cURL:**
```bash
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8585/consumer/api/sse/games/inicio"
```

#### 3. **Atualizações de Placar**
```http
GET /consumer/api/sse/games/placar
```

**Exemplo cURL:**
```bash
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8585/consumer/api/sse/games/placar"
```

#### 4. **Jogos Encerrados**
```http
GET /consumer/api/sse/games/encerrado
```

**Exemplo cURL:**
```bash
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8585/consumer/api/sse/games/encerrado"
```

#### 5. **Status dos Canais SSE**
```http
GET /consumer/api/sse/games/status
```

**Exemplo cURL:**
```bash
curl -X GET "http://localhost:8585/consumer/api/sse/games/status" \
  -H "Accept: application/json"
```

### Health Checks e Métricas

#### 1. **Health Check**
```http
GET /actuator/health
```

**Exemplo cURL:**
```bash
curl -X GET "http://localhost:8585/actuator/health" \
  -H "Accept: application/json"
```

#### 2. **Métricas**
```http
GET /actuator/metrics
```

**Exemplo cURL:**
```bash
curl -X GET "http://localhost:8585/actuator/metrics" \
  -H "Accept: application/json"
```

### 📖 Documentação Swagger/OpenAPI - Consumer

**Swagger UI (Interface Web):**
```
http://localhost:8585/consumer/swagger-ui.html
```

**OpenAPI Specification (JSON):**
```
http://localhost:8585/consumer/api-docs
```

---

## 🎨 Padrões de Projeto Implementados

### Arquitetura

- ✅ **Event-Driven Architecture** - Comunicação assíncrona via eventos
- ✅ **Microservices** - Serviços independentes e especializados  
- ✅ **CQRS** - Separação de comandos (Publisher) e queries (Consumer)
- ✅ **Repository Pattern** - Abstração de acesso a dados
- ✅ **DTO Pattern** - Transferência de dados entre camadas
- ✅ **Layered Architecture** - Separação em camadas lógicas

### Qualidade de Código

- ✅ **Dependency Injection** - Inversão de controle (CDI / Spring)
- ✅ **Transaction Management** - Transações declarativas
- ✅ **Exception Handling** - Tratamento centralizado de erros
- ✅ **Validation** - Bean Validation (Jakarta / Spring)
- ✅ **Logging** - SLF4J com níveis apropriados
- ✅ **Circuit Breaker** - Proteção contra cascata de falhas

### Resiliência

- ✅ **Health Checks** - Monitoramento de saúde dos serviços
- ✅ **Retry Mechanism** - Tentativas automáticas em falhas
- ✅ **Dead Letter Queue** - Mensagens problemáticas isoladas
- ✅ **Connection Pooling** - Reutilização de conexões
- ✅ **Cache Strategy** - Redução de carga no banco

---

## 💡 Informações Adicionais

### Credenciais de Acesso

**PostgreSQL:**
- Host: localhost:5432
- Database: placar_db
- Usuário: placar_user
- Senha: placar_pass

**Redis:**
- Host: localhost:6379
- Senha: redis_pass

**RabbitMQ:**
- AMQP: localhost:5672
- Management: http://localhost:15672
- Usuário: root / Senha: root

**Payara Server:**
- HTTP: localhost:8080
- Admin: localhost:4848
- Usuário: admin / Senha: root

### Configurações de Desenvolvimento

- **Java Version:** 17 (Eclipse Temurin)
- **Maven Version:** 3.9+
- **Encoding:** UTF-8
- **Timezone:** America/Sao_Paulo

### Monitoramento e Logs

- **Publisher Logs:** `docker compose logs -f payara`
- **Consumer Logs:** `docker compose logs -f rest-consumer`
- **Infrastructure Logs:** `docker compose logs -f postgres redis rabbitmq`

---

## 🧪 Suíte de Testes

O projeto conta com uma suíte completa de testes automatizados que garante a qualidade e confiabilidade do sistema, seguindo as melhores práticas de testing em aplicações enterprise Java.

### 📊 Estatísticas dos Testes

- **Total de Classes de Teste:** 21+
- **Cobertura de Módulos:** 100% dos módulos têm testes
- **Tipos de Teste:** Unitários, Integração, Componente
- **Framework Principal:** JUnit 5 + Mockito + AssertJ

### 🏗️ Estrutura da Suíte

#### **wicket-publisher** - Testes do Publisher
```
📂 src/test/java/
├── 🧪 WicketPublisherTestSuite.java          # Suíte principal
├── 📦 service/
│   ├── JogoServiceTest.java                  # Lógica de negócio
│   ├── publisher/GameEventPublisherTest.java # Publicação de eventos
│   └── listener/GameEventListenerTest.java   # Processamento de eventos
├── 📦 integration/
│   ├── GameEventIntegrationTest.java         # Fluxo completo de eventos
│   └── RabbitMQContainerIntegrationTest.java # Testes com TestContainers
├── 📦 config/
│   └── RabbitMQConfigTest.java               # Configurações RabbitMQ
└── 📦 util/
    └── TestDataBuilder.java                  # Builders para dados de teste
```

#### **rest-consumer** - Testes do Consumer
```
📂 src/test/java/
├── 📦 service/
│   ├── GameCacheServiceTest.java             # Cache distribuído
│   └── GameEventProcessorTest.java           # Processamento de eventos
├── 📦 redis/
│   ├── GameCacheRepositoryTest.java          # Operações Redis
│   └── GameQueryControllerTest.java          # Endpoints REST
└── 📦 config/
    ├── AsyncConfigTest.java                  # Configuração assíncrona
    ├── RedisConfigTest.java                  # Configuração Redis
    └── RabbitConfigTest.java                 # Configuração RabbitMQ
```

### 🔧 Tecnologias e Ferramentas

| Ferramenta | Versão | Propósito |
|------------|--------|-----------|
| **JUnit 5** | 5.10.1 | Framework principal de testes |
| **Mockito** | 5.8.0 | Mocking e stubbing |
| **AssertJ** | 3.24.2 | Assertions fluentes |
| **TestContainers** | 1.19.3 | Testes com containers Docker |
| **Maven Surefire** | 3.2.3 | Execução de testes |
| **Spring Boot Test** | 3.2.1 | Testes de integração Spring |

### 📝 Categorias de Testes

#### **1. Testes Unitários**
- **Foco:** Validação isolada de componentes
- **Características:**
  - Mock de todas as dependências externas
  - Execução rápida (< 100ms por teste)
  - Validação de regras de negócio
  - Cobertura de cenários de erro

**Exemplos:**
```java
@ExtendWith(MockitoExtension.class)
class JogoServiceTest {
    @Mock private JogoRepository repository;
    @Mock private ApplicationEventPublisher eventPublisher;
    
    @Test
    void deveCriarJogoComSucesso() {
        // Validação de criação de jogo
    }
    
    @Test 
    void deveValidarDadosObrigatorios() {
        // Validação de regras de negócio
    }
}
```

#### **2. Testes de Integração**
- **Foco:** Validação de fluxos completos entre componentes
- **Características:**
  - Integração real entre camadas
  - Uso de TestContainers para infraestrutura
  - Validação de eventos e mensageria
  - Simulação de cenários reais

**Exemplos:**
```java
@TestPropertySource(locations = "classpath:application-test.properties")
class GameEventIntegrationTest {
    @Test
    void deveProcessarFluxoCompletoDeEvento() {
        // Testa criação → evento → processamento → cache
    }
}
```

#### **3. Testes de Componente**
- **Foco:** Validação de APIs REST e endpoints
- **Características:**
  - Teste de controllers completos
  - Validação de serialização JSON
  - Verificação de status codes
  - Teste de documentação OpenAPI

**Exemplos:**
```java
@ExtendWith(MockitoExtension.class)
class GameQueryControllerTest {
    @Test
    void deveRetornarJogosDoCache() {
        // Validação de endpoint REST
    }
}
```

### 🐳 Testes com TestContainers

O projeto utiliza **TestContainers** para testes de integração com infraestrutura real:

#### **Containers de Teste Disponíveis:**
- **PostgreSQL:** Testes de persistência e transações
- **RabbitMQ:** Validação de mensageria e filas
- **Redis:** Testes de cache distribuído

```java
@TestContainers
class RabbitMQContainerIntegrationTest {
    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.12-management");
    
    @Test
    void devePublicarEventoNoRabbitMQ() {
        // Teste com container real
    }
}
```

### ▶️ Execução dos Testes

#### **Executar Todos os Testes:**
```bash
# Na raiz do projeto
mvn test

# Apenas wicket-publisher
mvn test -pl wicket-publisher

# Apenas rest-consumer  
mvn test -pl rest-consumer
```

#### **Executar Suíte Específica:**
```bash
# Suíte completa do wicket-publisher
mvn test -pl wicket-publisher -Dtest=WicketPublisherTestSuite
```

#### **Executar com Profile de Teste:**
```bash
# Com configurações específicas
mvn test -Dspring.profiles.active=test
```

### 📊 Perfis de Teste

| Perfil | Configuração | Uso |
|--------|--------------|-----|
| **test** | Banco H2 em memória | Testes unitários rápidos |
| **integration** | TestContainers | Testes de integração |
| **docker** | Containers reais | Validação completa |

### 🎯 Estratégia de Testes

#### **Pirâmide de Testes:**
```
    🔺 E2E Tests (Manual/Cypress)
   ════════════════════════════════
  🔸🔸 Integration Tests (TestContainers)
 ══════════════════════════════════════════
🔹🔹🔹🔹 Unit Tests (JUnit + Mockito)
```

#### **Cobertura por Camada:**
- **Services:** 95%+ cobertura de regras de negócio
- **Controllers:** 90%+ cobertura de endpoints
- **Repositories:** 85%+ cobertura de queries
- **Configurations:** 100% configurações críticas

### 🔍 Qualidade dos Testes

#### **Padrões Adotados:**
- **AAA Pattern:** Arrange, Act, Assert
- **Given-When-Then:** Para testes BDD
- **Test Data Builders:** Para criação de objetos de teste
- **Naming Convention:** `deve[Acao]Quando[Cenario]`

#### **Características dos Bons Testes:**
- ✅ **Isolados:** Não dependem uns dos outros
- ✅ **Determinísticos:** Sempre produzem o mesmo resultado
- ✅ **Rápidos:** Execução em segundos, não minutos
- ✅ **Legíveis:** Documentam comportamento esperado
- ✅ **Mantíveis:** Fáceis de atualizar quando código muda

### 🚀 Continuous Integration

Os testes são executados automaticamente em:
- **Push/Pull Request:** Validação contínua
- **Deploy:** Gate de qualidade antes do deploy
- **Schedules:** Execução noturna para regressão

---

## 🔗 Links Úteis

### Aplicações
- **Wicket Publisher:** http://localhost:8080/wicket-publisher/
- **Publisher Swagger:** http://localhost:8080/wicket-publisher/swagger-ui/
- **Consumer API:** http://localhost:8585/actuator/health
- **Consumer Swagger:** http://localhost:8585/consumer/swagger-ui.html

### Ferramentas de Administração
- **Payara Admin:** http://localhost:4848/ (admin/root)
- **RabbitMQ Management:** http://localhost:15672/ (root/root)
- **pgAdmin:** http://localhost:5050/ (admin@placar.com/admin)

---

**Desenvolvido utilizando tecnologias enterprise Java e padrões de arquitetura modernos.**