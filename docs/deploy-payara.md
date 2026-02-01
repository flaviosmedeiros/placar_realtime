# 📦 Deploy do Placar Realtime no Payara 6 (Docker)

Este guia descreve como usar a imagem oficial **Payara Server Community 6.2023.5 (JDK 17)** dentro do `docker-compose` do projeto para publicar o `wicket-publisher.war` sem instalações manuais no host.

## ✅ Compatibilidade e referências

| Componente | Versão | Observação |
|------------|--------|------------|
| Payara Server Community | `payara/server-full:6.2023.5-jdk17` | Imagem oficial no Docker Hub ([link](https://hub.docker.com/r/payara/server-full)) |
| Jakarta EE / JDK | 10 / 17 | Mesmo target do projeto |
| WAR gerado | `wicket-publisher/target/wicket-publisher.war` | Produzido por `mvn clean install` |

## 🧰 Pré-requisitos

1. Docker e Docker Compose instalados e ativos.
2. Projeto compilado (`mvn clean install`).
3. Diretório `docker/payara/deployments/` acessível (já versionado com `.gitkeep`).
4. Portas 8080 (HTTP) e 4848 (Console) livres no host.

## 🗂️ Visão geral da stack Docker

O `docker/docker-compose.yml` agora sobe, além de PostgreSQL, Redis, RabbitMQ e pgAdmin, um container `payara` com:
- Usuário/senha admin: `admin` / `admin123` (dev only).
- Volume persistente para o domínio: `payara_domain_data`.
- Pasta de autodeploy montada em `docker/payara/deployments`.
- Acesso HTTP em `http://localhost:8080` e console em `http://localhost:4848`.

## 🚀 Passo a passo roteirizado

### 1. Subir toda a infraestrutura
```bash
cd docker
./start-infrastructure.sh
```
Isso fará o pull da imagem `payara/server-full:6.2023.5-jdk17` automaticamente e iniciará todos os serviços.

### 2. Configurar recursos internos do Payara
Com o container em execução, rode o script que automatiza pool JDBC, datasource JNDI e system properties:
```bash
cd docker
./configure-payara.sh
```
O script executa internamente:
- `create-jdbc-connection-pool placar-pool` apontando para o serviço `postgres`.
- `create-jdbc-resource jdbc/placarDS` (JNDI solicitado pela app).
- `create-system-properties rabbitmq.host=rabbitmq redis.host=redis ...` para que o aplicativo enxergue os containers vizinhos.

> Se preferir rodar comandos manualmente, utilize `docker compose exec payara /opt/payara/bin/asadmin <comando>`.

### 3. Copiar o WAR para a pasta de autodeploy
Após compilar:
```bash
cp wicket-publisher/target/wicket-publisher.war docker/payara/deployments/
```
A pasta está mapeada diretamente para `/opt/payara/deployments` dentro do container.

### 4. Reiniciar o serviço Payara para disparar o deploy
```bash
cd docker
docker compose restart payara
```
Durante o restart, o Payara detecta o WAR na pasta de autodeploy e realiza a publicação automaticamente. Acompanhe com:
```bash
docker compose logs -f payara
```
Busque pelas mensagens `Deploying application wicket-publisher` e `wicket-publisher was successfully deployed in ...`.

### 5. Validar o ambiente
- Interface Wicket: http://localhost:8080/wicket-publisher/app
- API REST: http://localhost:8080/wicket-publisher/api
- OpenAPI: http://localhost:8080/wicket-publisher/api/openapi
- Console Admin: http://localhost:4848 (admin / admin123)

Para checar o datasource no server, execute:
```bash
cd docker
docker compose exec payara /opt/payara/bin/asadmin ping-connection-pool placar-pool
```

## 🔁 Redeploys futuros
1. Gere um novo WAR (`mvn clean install`).
2. Substitua o arquivo em `docker/payara/deployments/`.
3. Execute `docker compose restart payara` (ou `docker compose exec payara touch /opt/payara/deployments/wicket-publisher.war`).
4. Monitore os logs até ver o redeploy concluído.

## 🛠️ Troubleshooting rápido
| Sintoma | Ação |
|---------|------|
| `configure-payara.sh` falha | Verifique se o container `payara` está rodando (`docker compose ps payara`). |
| `ping-connection-pool` falha | Confirme que o container `postgres` está saudável e que o script foi executado após o start. |
| App não alcança RabbitMQ/Redis | Reaplique o script para recriar as system properties ou ajuste valores via `asadmin create-system-properties`. |
| Deploy não acontece após copiar WAR | Certifique-se de que o arquivo está em `docker/payara/deployments/` e reinicie o serviço `payara`. |

## 📚 Referências úteis
- [Docker Hub – payara/server-full](https://hub.docker.com/r/payara/server-full)
- [Documentação oficial Payara Docker](https://docs.payara.fish/community/docs/documentation/ecosystem/docker.html)

Com isso, todo o ambiente (infra + servidor de aplicação) roda em Docker, deixando o host responsável apenas pelo build do WAR.