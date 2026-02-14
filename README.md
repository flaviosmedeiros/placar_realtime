# 🏆 Placar Realtime

> Sistema completo de gerenciamento e atualizações em tempo real de placares de jogos de futebol com arquitetura de microserviços event-driven.

## 🎯 Sobre o Projeto

Plataforma moderna e escalável para gerenciamento de placares esportivos em tempo real, desenvolvida com tecnologias enterprise Java. O sistema permite gestão administrativa de jogos, publicação de eventos via mensageria e atualizações em tempo real através de Server-Sent Events (SSE).

### ✨ Principais Funcionalidades

- **🎮 Gestão Administrativa** - CRUD completo de jogos e eventos esportivos
- **⚡ Tempo Real** - Atualizações instantâneas via Server-Sent Events (SSE)
- **📨 Mensageria** - Comunicação assíncrona com RabbitMQ
- **🗄️ Cache Distribuído** - Alta performance com Redis
- **🔄 API REST** - Endpoints completos para integração
- **📊 Monitoramento** - Health checks e métricas integradas

## 🏗️ Arquitetura

### Diagrama de Sistema
```
                    🌐 Angular SPA (Frontend)
                              ↑
                            [SSE]
                              ↑
┌─────────────────────────────────────────────────────────────────┐
│                    🐳 Docker Infrastructure                      │
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
│  └─────────────────┘                         └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘

### Fluxo de Dados
```
1. 👤 Admin ──────── Wicket UI ──────▶ wicket-publisher
                                           │
2. 💾 CRUD Jogos ── JPA/Hibernate ────▶ PostgreSQL
                                           │
3. 📨 Eventos ───── RabbitMQ ─────────▶ rest-consumer
                                           │
4. 🗄️ Cache ────── Redis ────────────▶ Fast Access
                                           │
5. 📡 SSE ─────── Server-Sent Events ──▶ 🌐 Angular SPA
                                           │
```

### Componentes

#### **📊 wicket-publisher** 
Aplicação administrativa Jakarta EE + Apache Wicket
- Interface web para gestão de jogos
- API REST completa
- Publicação automática de eventos
- Persistência transacional (JPA/Hibernate)

#### **📈 rest-consumer**
Aplicação Spring Boot para consumo e distribuição
- Consumo de eventos RabbitMQ
- Cache distribuído Redis
- Server-Sent Events (SSE)
- Circuit Breaker e Retry
- Health checks e métricas

#### **📨 shared-domain**
Módulo compartilhado
- Entidades JPA comuns
- Events e DTOs
- Enums e validações

## 🛠️ Stack Tecnológica

### Backend
| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| **Java** | 17 | Runtime principal |
| **Apache Wicket** | 10.0.0 | Framework web component-based |
| **Spring Boot** | 3.2.2 | Microserviços e APIs |
| **Jakarta EE** | 10.0.0 | Plataforma enterprise |
| **JPA/Hibernate** | 6.2.13 | ORM - Mapeamento objeto-relacional |

### Infraestrutura
| Serviço | Versão | Porta | Propósito |
|---------|--------|-------|-----------|
| **PostgreSQL** | 15-alpine | 5432 | Banco de dados principal |
| **Redis** | 7-alpine | 6379 | Cache distribuído |
| **RabbitMQ** | 3.12-management | 5672, 15672 | Message broker |
| **Payara Server** | 6.2023.5 | 8080, 4848 | Application server |

### Ferramentas
- **Maven** - Build e dependências
- **Docker & Docker Compose** - Containerização e orquestração
- **Resilience4j** - Circuit breaker e retry
- **OpenAPI/Swagger** - Documentação automática

## 🚀 Início Rápido

### Pré-requisitos

- ✅ **Docker & Docker Compose**
- ✅ **Java 17**  
- ✅ **Maven 3.9+**
- ✅ **Portas livres**: 5432, 6379, 5672, 15672, 8080, 4848, 8585


## 🧪 Testes

O projeto possui suíte completa de testes automatizados:

- **21+ Classes de teste**
- **Cobertura 100% dos módulos**
- **JUnit 5 + Mockito + AssertJ**
- **TestContainers** para testes de integração
- **Testes unitários, integração e componente**




## 🔗 Ferramentas de Administração

Após o deploy, as seguintes ferramentas ficam disponíveis:

- **RabbitMQ Management**: http://localhost:15672 (root/root)
- **Payara Admin Console**: http://localhost:4848 (admin/root)  



## 📝 Documentação Adicional

- **Swagger/OpenAPI**: Disponível nas URLs das aplicações
- **Arquitetura Detalhada**: Ver `VISAO_GERAL.md`
- **Guia de Deploy**: Ver `DEPLOY.md`


---

**Desenvolvido com tecnologias enterprise Java e padrões de arquitetura modernos.**
