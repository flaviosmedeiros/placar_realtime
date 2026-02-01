# Docker Infrastructure - Placar Realtime

Esta pasta contém toda a infraestrutura necessária para executar o sistema de Placar Realtime.

## 🐳 Serviços Disponíveis

### PostgreSQL 15
- **Porta**: 5432
- **Database**: placar_db
- **Usuário**: placar_user
- **Senha**: placar_pass
- **Schema**: placar

### Redis 7
- **Porta**: 6379
- **Senha**: redis_pass
- **Persistência**: Habilitada (AOF)

### RabbitMQ 3.12
- **Porta AMQP**: 5672
- **Porta Management UI**: 15672
- **Usuário**: rabbitmq_user
- **Senha**: rabbitmq_pass
- **VHost**: placar_vhost
- **Management UI**: http://localhost:15672

### Payara Server 6 (Jakarta EE)
- **Imagem**: `payara/server-full:6.2023.5-jdk17`
- **Porta HTTP**: 8080
- **Porta Admin**: 4848
- **Usuário / Senha**: `admin` / `admin123`
- **Autodeploy**: copiar o WAR para `docker/payara/deployments/`
- **Comandos úteis**: `docker compose exec payara /opt/payara/bin/asadmin <cmd>`

### pgAdmin 4 (Opcional)
- **Porta**: 5050
- **URL**: http://localhost:5050
- **Email**: admin@placar.com
- **Senha**: admin

## 🚀 Como Usar

### Iniciar Infraestrutura
```bash
./start-infrastructure.sh
```

Ou diretamente com Docker Compose:

```bash
docker compose up -d
```

### Configurar o Payara (JDBC + System Properties)
```bash
cd docker
./configure-payara.sh
```

O script cria o pool `placar-pool`, o datasource `jdbc/placarDS` e aponta `rabbitmq.host` / `redis.host` para os containers da stack.

### Publicar o WAR no Payara
Após gerar o WAR (`mvn clean install`), copie-o para a pasta de autodeploy e reinicie o serviço:

```bash
cp ../wicket-publisher/target/wicket-publisher.war payara/deployments/
docker compose restart payara
```

O Payara fará o hot deploy automaticamente na inicialização.


### Parar Infraestrutura
```bash
./stop-infrastructure.sh
```

Ou diretamente:

```bash
docker compose down
```

### Ver Logs
```bash
# Todos os serviços
docker compose logs -f

# Serviço específico
docker compose logs -f postgres
docker compose logs -f redis
docker compose logs -f rabbitmq
docker compose logs -f payara
```

### Verificar Status
```bash
docker compose ps
```

### Remover Volumes (⚠️ APAGA DADOS)
```bash
docker compose down -v
```

## 🔧 Configurações

### Conectar ao PostgreSQL via pgAdmin
1. Acesse http://localhost:5050
2. Faça login com: admin@placar.com / admin
3. Adicione um novo servidor:
   - **Name**: Placar DB
   - **Host**: postgres (ou host.docker.internal se pgAdmin não estiver no Docker)
   - **Port**: 5432
   - **Database**: placar_db
   - **Username**: placar_user
   - **Password**: placar_pass

### Conectar ao PostgreSQL via CLI
```bash
docker exec -it placar-postgres psql -U placar_user -d placar_db
```

### Conectar ao Redis via CLI
```bash
docker exec -it placar-redis redis-cli
AUTH redis_pass
```

### Acessar RabbitMQ Management
1. Acesse http://localhost:15672
2. Login: rabbitmq_user / rabbitmq_pass
3. VHost: placar_vhost

## 📦 Volumes

Os dados são persistidos nos seguintes volumes Docker:
- `placar-postgres-data` - Dados do PostgreSQL
- `placar-redis-data` - Dados do Redis
- `placar-rabbitmq-data` - Dados do RabbitMQ
- `placar-rabbitmq-log` - Logs do RabbitMQ
- `placar-pgadmin-data` - Configurações do pgAdmin

## 🌐 Rede

Todos os serviços estão na rede `placar-network` (bridge), permitindo comunicação entre containers.

## ✅ Health Checks

Todos os serviços possuem health checks configurados:
- PostgreSQL: Verifica conexão com pg_isready
- Redis: Incrementa contador de ping
- RabbitMQ: Diagnóstico de ping

## 🔍 Troubleshooting

### Portas já em uso
Se alguma porta estiver em uso, edite o `docker-compose.yml` e altere o mapeamento de portas.

### Container não inicia
Verifique os logs:
```bash
docker compose logs [service]
```

### Resetar tudo
```bash
docker compose down -v
docker compose up -d
./configure-payara.sh
```

## 📝 Notas

- As senhas são para desenvolvimento. **NÃO USE EM PRODUÇÃO!**
- O arquivo `init-db.sql` é executado apenas na primeira criação do container PostgreSQL
- Para recriar o banco, remova o volume: `docker volume rm placar-postgres-data`
- Para forçar um redeploy limpo, apague o WAR de `payara/deployments/` e reinicie o serviço