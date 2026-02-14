#!/bin/bash

set -e

# Função para log colorido
log_info() { echo "ℹ️  $1${NC}"; }
log_success() { echo "✅ $1${NC}"; }
log_warning() { echo "⚠️  $1${NC}"; }
log_error() { echo "❌ $1${NC}"; }
log_step() { echo "🔄 $1${NC}"; }

echo "════════════════════════════════════════════════════════════════${NC}"
echo "  🚀 EXECUTAR DEPLOY COMPLETO DO PLACAR REALTIME${NC}"
echo "════════════════════════════════════════════════════════════════${NC}"
echo ""

# Ir para o diretório do script
cd "$(dirname "$0")"
log_info "Diretório de trabalho: $(pwd)"

log_step "PASSO INICIAL:Executar suite de testes e Gerar os executáveis"
sleep 5
echo ""
mvn clean install
log_success "✅ Executáveis gerados com sucesso!"
echo ""


log_step "PASSO 1/5: Dando permissão de execução aos scripts..."
sleep 5
cd inicializacao
chmod +x start-infrastructure.sh
chmod +x configure-payara.sh  
chmod +x deploy-wicket.sh
log_success "Permissões concedidas"
echo ""



log_step "PASSO 2/5: Inicializando os containers..."
sleep 5
./start-infrastructure.sh
echo ""


# Aguardar tempo base para inicialização
echo ""
log_info "⏳ Aguardando inicialização completa do Payara (40 segundos)..."
sleep 40
echo ""



# Verificar se containers estão rodando
log_info "🔍 Verificando status dos containers (após 40 segundos)"

if ! docker compose ps --status running postgres >/dev/null 2>&1; then
    log_warning "PostgreSQL ainda não está pronto, aguardando mais 10 segundos..."
    sleep 10
fi

if ! docker compose ps --status running redis >/dev/null 2>&1; then
    log_warning "Redis ainda não está pronto, aguardando mais 5 segundos..."
    sleep 5
fi

if ! docker compose ps --status running rest-consumer >/dev/null 2>&1; then
    log_warning "rest-consumer ainda não está pronto, aguardando mais 5 segundos..."
    sleep 5
fi

if ! docker compose ps --status running rabbitmq >/dev/null 2>&1; then
    log_warning "RabbitMQ ainda não está pronto, aguardando mais 10 segundos..."
    sleep 10
fi

if ! docker compose ps --status running payara >/dev/null 2>&1; then
    log_warning "Payara ainda não está pronto, aguardando mais 30 segundos..."
    sleep 30
fi


# Verificação final com retry
MAX_RETRIES=6
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if docker compose ps --status running payara >/dev/null 2>&1; then
        log_success "Payara está em execução!"
        break
    else
        RETRY_COUNT=$((RETRY_COUNT + 1))
        log_warning "Tentativa $RETRY_COUNT/$MAX_RETRIES - Aguardando Payara inicializar (20 segundos)..."
        sleep 20
    fi
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    log_error "Payara não iniciou após $((MAX_RETRIES * 15)) segundos"
    log_error "Verifique os logs: docker compose logs payara"
    exit 1
fi

# Status final dos containers
echo ""
log_success "✅ Infraestrutura iniciada com sucesso!"

echo ""
echo ""
echo ""
log_step "PASSO 3/5: Preparando o payara server com configurações iniciais..."
echo "🔍 Aguardando payara...30 segundos"
sleep 30
./configure-payara.sh


echo ""
log_step "PASSO 4/5: Executando o deploy do Publisher-Wicket no Payara..."
echo "🔍 Aguardando 10 segundos.."
sleep 10
./deploy-wicket.sh


# =============================
# FINALIZAÇÃO
# =============================
echo ""
echo ""
echo ""
echo "════════════════════════════════════════════════════════════════${NC}"
echo "  🎉 DEPLOY COMPLETO FINALIZADO COM SUCESSO!${NC}"
echo "════════════════════════════════════════════════════════════════${NC}"
cd ..
echo ""
echo ""
echo "📝 Serviços disponíveis:"
echo "🔗 wicket-publisher disponivel em: http://localhost:8080/wicket-publisher"
echo "🔗 Painel RealTime disponível em: http://localhost:8585"
echo ""
echo ""
echo "🔗 PostgreSQL:     localhost:5432"
echo "🔗 Redis:          localhost:6379"
echo "🔗 RabbitMQ AMQP:  localhost:5672"
echo "🔗 RabbitMQ Admin: http://localhost:15672 (user: root, pass: root)"
echo "🔗 Payara Home:    http://localhost:8080 (página inicial)"
echo "🔗 Payara Admin:   http://localhost:4848 (user: admin, pass: root)"
echo "🔗 REST Consumer:  http://localhost:8585/actuator/health (health check)"
echo ""