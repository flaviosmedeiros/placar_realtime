#!/bin/sh
# Script para configurar o Payara rodando no Docker Compose (JDBC + system properties)

set -e

cd "$(dirname "$0")"

if ! docker compose ps payara >/dev/null 2>&1; then
  echo "❌ O serviço 'payara' não está configurado no docker-compose ou o Docker não está acessível."
  exit 1
fi

if ! docker compose ps --status running payara >/dev/null 2>&1; then
  echo "⚠️ O container 'payara' não está em execução. Inicie a infraestrutura antes de configurar."
  echo "   Sugestão: ./start-infrastructure.sh"
  exit 1
fi

ASADMIN="docker compose exec -T payara /opt/payara/bin/asadmin"

echo "🧹 Limpando recursos anteriores (se existirem)..."
$ASADMIN delete-jdbc-resource jdbc/placarDS >/dev/null 2>&1 || true
$ASADMIN delete-jdbc-connection-pool placar-pool >/dev/null 2>&1 || true

for PROP in rabbitmq.host rabbitmq.port redis.host redis.port; do
  $ASADMIN delete-system-property "$PROP" >/dev/null 2>&1 || true
done

echo "🛠️ Criando pool JDBC e datasource..."
$ASADMIN create-jdbc-connection-pool \
  --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
  --restype javax.sql.DataSource \
  --isconnectvalidatereq=true \
  --validationclassname org.glassfish.api.jdbc.validation.PostgresConnectionValidation \
  --property user=placar_user:password=placar_pass:serverName=postgres:portNumber=5432:databaseName=placar_db \
  placar-pool

$ASADMIN create-jdbc-resource --connectionpoolid placar-pool jdbc/placarDS

echo "🔧 Definindo system properties para apontar para os serviços Docker..."
$ASADMIN create-system-properties \
  rabbitmq.host=rabbitmq \
  rabbitmq.port=5672 \
  redis.host=redis \
  redis.port=6379

echo "✅ Payara configurado!"
