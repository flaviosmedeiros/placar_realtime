#!/bin/sh
# Script para fazer deploy do wicket-publisher no Payara (via Docker)
# Remove a aplicação anterior se existir e faz um deploy limpo
#
# IMPORTANTE: Este script faz deploy AUTOMÁTICO via linha de comando.
# Você também pode fazer deploy MANUAL via painel admin (https://localhost:4848)
# sem usar este script. Ambas as opções funcionam e não interferem entre si.

set -e

# Verificar se o container está rodando
if ! docker compose ps --status running payara >/dev/null 2>&1; then
  echo "❌ O container 'payara' não está em execução."
  echo "   Execute: ./start-infrastructure.sh"
  exit 1
fi

ASADMIN="docker compose exec -T payara /opt/payara/appserver/bin/asadmin --passwordfile=/opt/payara/passwordFile"
APP_NAME="wicket-publisher"
WAR_FILE="../wicket-publisher/target/${APP_NAME}.war"
DEPLOY_DIR="./payara/deployments"

# Verificar se o WAR existe
if [ ! -f "$WAR_FILE" ]; then
  echo "❌ WAR não encontrado: $WAR_FILE"
  echo "   Execute: mvn clean install no diretório wicket-publisher"
  exit 1
fi

echo "🔍 Verificando se a aplicação já está deployada..."

# Verificar se a aplicação existe no Payara
if $ASADMIN list-applications 2>/dev/null | grep -q "^${APP_NAME} "; then
  echo "🗑️  Removendo aplicação anterior..."
  $ASADMIN undeploy "$APP_NAME" || true
  sleep 2
fi

# Limpar arquivos de controle do autodeploy para evitar conflito
if [ -f "${DEPLOY_DIR}/${APP_NAME}.war_deployed" ]; then
  echo "🧹 Limpando marcadores de autodeploy..."
  rm -f "${DEPLOY_DIR}/${APP_NAME}.war_deployed"
  rm -f "${DEPLOY_DIR}/${APP_NAME}.war_undeployed"
fi

# Copiar WAR para a pasta de deployments (necessário para o comando asadmin)
echo "📋 Copiando WAR para pasta de deployments..."
cp "$WAR_FILE" "${DEPLOY_DIR}/"

echo "📦 Fazendo deploy automático de ${APP_NAME}..."
$ASADMIN deploy --force=true --name="$APP_NAME" --contextroot="/wicket-publisher" "/opt/payara/deployments/${APP_NAME}.war"

echo ""
echo "✅ Deploy automático concluído com sucesso!"
echo ""
echo "🔗 Aplicação disponível em: http://localhost:8080/wicket-publisher"
echo ""
