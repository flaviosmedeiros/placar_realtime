# 🚀 Placar Realtime - Guia de Deploy

Guia completo para implantação, execução e gerenciamento do sistema Placar Realtime.

---

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter:

- ✅ **Docker** instalado e rodando
- ✅ **Docker Compose** instalado
- ✅ **Portas disponíveis**: 5432, 6379, 5672, 15672, 8080, 4848, 8585, 5050
- ✅ **Java 17** (para compilação)
- ✅ **Maven 3.9+** (para build)

### Verificar Pré-requisitos

```bash
# Verificar Docker
docker --version
docker compose version
docker info

# Verificar Java e Maven
java --version
mvn --version

# Verificar portas disponíveis
netstat -tuln | grep -E ':(5432|6379|5672|15672|8080|4848|8585|5050)'
```

Se algum comando falhar, instale as ferramentas necessárias antes de continuar.

---

## 🚀 Deploy Automático - Método Recomendado

### Passo 1: Preparar Ambiente

```bash
# Navegar para o diretório raiz do projeto
cd /caminho/para/placar_realtime

# Dar permissões de execução aos scripts
chmod +x start.sh
chmod +x stop.sh
```

### Passo 2: Deploy Completo

```bash
# Executar deploy automático (5-7 minutos)
./start.sh
```

**⏱️ Tempo total estimado:** ~5-7 minutos

#### O que o script `start.sh` faz automaticamente:

1. ✅ **Compilação Maven** (2-3 min)
   - Compila todos os módulos: shared-domain, wicket-publisher, rest-consumer
   - Executa testes unitários
   - Gera artefatos WAR e JAR

2. ✅ **Infraestrutura Docker** (30-45 seg)
   - Inicia PostgreSQL, Redis, RabbitMQ
   - Aguarda serviços ficarem healthy
   - Inicializa banco de dados

3. ✅ **Payara Server** (15-20 seg)
   - Inicia container Payara
   - Configura datasource JDBC
   - Aguarda servidor ficar pronto

4. ✅ **Deploy Wicket Publisher** (10-15 seg)
   - Faz undeploy da versão anterior (se existir)
   - Copia WAR para Payara deployments
   - Executa deploy automático

5. ✅ **REST Consumer** (5-10 seg)
   - Inicia aplicação Spring Boot
   - Conecta com RabbitMQ e Redis
   - Ativa endpoints SSE

### Passo 3: Validar Deploy

Após a conclusão do script, você deve ver uma mensagem similar a:

```
✅ Deploy automático concluído com sucesso!

🔗 Aplicações disponíveis:
   - Wicket Publisher: http://localhost:8080/wicket-publisher
   - REST Consumer: http://localhost:8585/actuator/health
   - Payara Admin: http://localhost:4848 (admin/root)

🛠️ Ferramentas:
   - RabbitMQ Management: http://localhost:15672 (root/root)
```

---

## 🔧 Deploy Manual - Passo a Passo

Se preferir fazer o deploy manual ou em caso de problemas com o script automático:

### Passo 1: Compilação

```bash
# Limpar e compilar todos os módulos
mvn clean install

# Verificar se os artefatos foram criados
ls -la wicket-publisher/target/*.war
ls -la rest-consumer/target/*.jar
```

### Passo 2: Infraestrutura

```bash
# Navegar para pasta Docker
cd inicializacao

# Iniciar serviços de infraestrutura
./start-infrastructure.sh

# Aguardar inicialização (45 segundos)
sleep 45

# Verificar se todos os containers estão rodando
docker compose ps
```

### Passo 3: Configurar Payara

```bash
# Configurar datasource JDBC
./configure-payara.sh

# Verificar se o datasource foi criado
docker compose exec payara /opt/payara/bin/asadmin list-jdbc-resources
```

### Passo 4: Deploy Wicket Publisher

```bash
# Deploy automático do WAR
./deploy-wicket.sh

# Verificar deploy
docker compose exec payara /opt/payara/bin/asadmin list-applications
```

### Passo 5: Verificar Rest Consumer

```bash
# O REST Consumer inicia automaticamente via docker-compose
docker compose logs -f rest-consumer
```

---

## ✅ Verificação do Deploy

### Checklist de Validação

Execute os comandos abaixo para verificar se tudo está funcionando:

#### 1. **Containers Rodando**
```bash
docker compose ps
```
**Esperado:** Todos os serviços devem mostrar status `UP` ou `healthy`.

#### 2. **Wicket Publisher**
```bash
# Testar página inicial
curl -I http://localhost:8080/wicket-publisher/

# Testar API REST
curl http://localhost:8080/wicket-publisher/rest/api/v1/jogos/health
```
**Esperado:** HTTP 200 para ambas as requisições.

#### 3. **REST Consumer**
```bash
# Health check
curl http://localhost:8585/actuator/health

# Testar SSE endpoint
curl -N -H "Accept: text/event-stream" http://localhost:8585/consumer/api/sse/games/novos
```
**Esperado:** `{"status":"UP"}` e stream SSE conectado.

#### 4. **Banco de Dados**
```bash
# Conectar via psql (se instalado)
psql -h localhost -p 5432 -U placar_user -d placar_db

```

#### 5. **RabbitMQ**
```bash
# Verificar filas
curl -u root:root http://localhost:15672/api/queues

# Ou acessar via web: http://localhost:15672
```


---

## 🌐 Serviços Disponíveis Após Deploy

Após o deploy completo, os seguintes serviços estarão disponíveis:

### 🎯 Aplicações Principais

| Serviço | URL | Credenciais | Descrição |
|---------|-----|-------------|-----------|
| **Wicket Publisher** | http://localhost:8080/wicket-publisher/ | - | Interface web administrativa |
| **Wicket Publisher API** | http://localhost:8080/wicket-publisher/rest/api/v1/jogos | - | REST API para jogos |
| **Swagger Publisher** | http://localhost:8080/wicket-publisher/swagger-ui/ | - | Documentação da API |
| **REST Consumer** | http://localhost:8585/actuator/health | - | Health check do consumer |
| **Consumer API** | http://localhost:8585/consumer/api/games | - | API de consulta cache |
| **Consumer SSE** | http://localhost:8585/consumer/api/sse/games | - | Server-Sent Events |
| **Swagger Consumer** | http://localhost:8585/consumer/swagger-ui.html | - | Documentação da API |

### 🛠️ Ferramentas de Administração

| Serviço | URL | Credenciais | Descrição |
|---------|-----|-------------|-----------|
| **Payara Admin** | http://localhost:4848/ | admin / root | Console administrativo |
| **RabbitMQ Management** | http://localhost:15672/ | root / root | Gerenciamento de filas |

### 🗄️ Bancos de Dados

| Serviço | Host:Porta | Credenciais | Descrição |
|---------|------------|-------------|-----------|
| **PostgreSQL** | localhost:5432 | placar_user / placar_pass | Banco principal |
| **Redis** | localhost:6379 | - / redis_pass | Cache em memória |

### 📨 Mensageria

| Serviço | Host:Porta | Credenciais | Descrição |
|---------|------------|-------------|-----------|
| **RabbitMQ AMQP** | localhost:5672 | root / root | Protocolo AMQP |
| **RabbitMQ Management** | localhost:15672 | root / root | Interface web |

---

## 🔗 Links de Acesso Rápido

### 📊 Dashboards e Interfaces
- **🏠 Página Inicial Payara:** http://localhost:8080/
- **⚡ Wicket Publisher:** http://localhost:8080/wicket-publisher/
- **📈 REST Consumer Health:** http://localhost:8585/actuator/health
- **📊 Consumer Métricas:** http://localhost:8585/actuator/metrics

### 📖 Documentação da API
- **📚 Swagger Publisher:** http://localhost:8080/wicket-publisher/swagger-ui/
- **📚 Swagger Consumer:** http://localhost:8585/consumer/swagger-ui.html

### 🛠️ Ferramentas Administrativas
- **⚙️ Payara Console:** http://localhost:4848/ (admin/root)
- **🐰 RabbitMQ Admin:** http://localhost:15672/ (root/root)

---

## 🛑 Desligar o Sistema

### Método 1: Script Automático (Recomendado)

```bash
# Para todo o sistema
./stop.sh
```

### Método 2: Docker Compose

```bash
# Para todos os serviços
docker compose down

# Para e remove volumes (⚠️ APAGA DADOS)
docker compose down -v

# Para, remove containers e imagens
docker compose down --rmi all
```

### Método 3: Controle Individual

```bash
# Parar serviços específicos
docker compose stop payara
docker compose stop rest-consumer
docker compose stop postgres
docker compose stop redis
docker compose stop rabbitmq

# Remover containers específicos
docker compose rm -f payara rest-consumer
```

---

## 🔍 Logs e Monitoramento

### Visualizar Logs

```bash
# Todos os serviços
docker compose logs -f

# Serviço específico
docker compose logs -f payara
docker compose logs -f rest-consumer
docker compose logs -f postgres
docker compose logs -f redis
docker compose logs -f rabbitmq

# Filtrar por timestamp
docker compose logs -f --since="2026-02-14T10:00:00"

# Últimas 100 linhas
docker compose logs --tail=100 payara
```

### Monitorar Recursos

```bash
# Status dos containers
docker compose ps

# Uso de recursos
docker compose top

# Estatísticas em tempo real
docker stats $(docker compose ps -q)
```

---


### Backup de Dados

```bash
# Backup PostgreSQL
docker compose exec postgres pg_dump -U placar_user placar_db > backup_$(date +%Y%m%d).sql

# Backup Redis
docker compose exec redis redis-cli -a redis_pass --rdb /tmp/backup.rdb
docker compose cp redis:/tmp/backup.rdb ./redis_backup_$(date +%Y%m%d).rdb
```



---

**✅ Sistema Placar Realtime implantado com sucesso!**

*Desenvolvido utilizando Docker, Jakarta EE, Spring Boot e ferramentas modernas de containerização.*