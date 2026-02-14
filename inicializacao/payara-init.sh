#!/bin/bash
# Script de inicialização do Payara Server com configuração de senha admin

set -e

ADMIN_PASSWORD="${PAYARA_ADMIN_PASSWORD:-root}"
PASSWORD_FILE="/opt/payara/passwordFile"
DOMAIN_NAME="${PAYARA_DOMAIN_NAME:-domain1}"

echo "🔧 Configurando Payara Server..."

# Iniciar o domínio
echo "🚀 Iniciando domínio ${DOMAIN_NAME}..."
/opt/payara/appserver/bin/asadmin start-domain ${DOMAIN_NAME}

# Aguardar o domínio estar pronto
echo "⏳ Aguardando domínio estar pronto..."
sleep 10

# Verificar se o domínio está rodando
if ! /opt/payara/appserver/bin/asadmin list-domains | grep -q "${DOMAIN_NAME} running"; then
    echo "❌ Erro: Domínio não iniciou corretamente"
    exit 1
fi

# Criar arquivo de senha com senha vazia (padrão do Payara)
cat > "$PASSWORD_FILE" << EOF
AS_ADMIN_PASSWORD=
AS_ADMIN_NEWPASSWORD=${ADMIN_PASSWORD}
EOF

# Alterar senha do admin
echo "🔐 Configurando senha do admin..."
/opt/payara/appserver/bin/asadmin --user admin --passwordfile="$PASSWORD_FILE" change-admin-password

# Atualizar o arquivo de senha com a nova senha
cat > "$PASSWORD_FILE" << EOF
AS_ADMIN_PASSWORD=${ADMIN_PASSWORD}
EOF

# Habilitar secure admin para acesso remoto
echo "🌐 Habilitando secure admin para permitir acesso remoto..."
/opt/payara/appserver/bin/asadmin --passwordfile="$PASSWORD_FILE" enable-secure-admin

# Configurar timezone da JVM
echo "🕐 Configurando timezone para ${TZ:-America/Sao_Paulo}..."
/opt/payara/appserver/bin/asadmin --passwordfile="$PASSWORD_FILE" create-jvm-options "-Duser.timezone=${TZ:-America/Sao_Paulo}"

# Reiniciar o domínio para aplicar mudanças do secure-admin e timezone
echo "🔄 Reiniciando domínio para aplicar configurações..."
/opt/payara/appserver/bin/asadmin --passwordfile="$PASSWORD_FILE" stop-domain ${DOMAIN_NAME}
sleep 5
/opt/payara/appserver/bin/asadmin start-domain ${DOMAIN_NAME}

# Aguardar o domínio reiniciar completamente
echo "⏳ Aguardando domínio reiniciar..."
sleep 15

echo ""
echo "✅ Payara Server configurado com sucesso!"
echo ""
echo "┌────────────────────────────────────────────────────┐"
echo "│  🌐 ACESSO AO PAINEL ADMINISTRATIVO                │"
echo "├────────────────────────────────────────────────────┤"
echo "│  URL:     https://localhost:4848                   │"
echo "│  Usuário: admin                                    │"
echo "│  Senha:   ${ADMIN_PASSWORD}                        │"
echo "├────────────────────────────────────────────────────┤"
echo "│  📝 Aplicação: http://localhost:8080               │"
echo "└────────────────────────────────────────────────────┘"
echo ""
echo "💡 Dica: Para deploy manual via painel admin:"
echo "   1. Acesse https://localhost:4848"
echo "   2. Vá em Applications → Deploy"
echo "   3. Faça upload do arquivo WAR"
echo "   4. Configure o context root desejado"
echo ""

# Manter o container rodando e exibir logs
echo "📋 Iniciando monitoramento de logs..."
tail -f /opt/payara/appserver/glassfish/domains/${DOMAIN_NAME}/logs/server.log
