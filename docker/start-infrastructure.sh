#!/bin/sh
# Script para iniciar toda a infraestrutura com Docker Compose

echo "🚀 Iniciando infraestrutura do Placar Realtime..."
echo ""


if ! command -v docker >/dev/null 2>&1; then
    log_error "Docker não encontrado. Instale o Docker primeiro."
    exit 1
fi

if ! docker info > /dev/null 2>&1; then
    log_error "Docker não está rodando. Inicie o Docker primeiro."
    exit 1
fi

log_success "Docker está funcionando"


# Iniciar containers
echo "📦 Iniciando containers..."
docker compose up -d

echo ""
echo "⏳ Aguardando serviços ficarem prontos..."
sleep 5

# Verificar status dos containers
echo ""
echo "📊 Status dos containers:"
docker compose ps

echo ""
echo "✅ Infraestrutura iniciada com sucesso!"
echo ""
echo "🔗 Serviços disponíveis:"
echo "   PostgreSQL:     localhost:5432"
echo "   Redis:          localhost:6379"
echo "   RabbitMQ AMQP:  localhost:5672"
echo "   RabbitMQ Admin: http://localhost:15672 (user: root, pass: root)"
echo "   pgAdmin:        http://localhost:5050 (email: admin@placar.com, pass: admin)"
echo "   Payara Home:    http://localhost:8080 (página inicial)"
echo "   Payara Admin:   http://localhost:4848 (user: admin, pass: root)"
echo "   REST Consumer:  http://localhost:8585 (SSE endpoints e health check)"
echo ""
echo "📝 Para parar os serviços: ./stop-infrastructure.sh"
echo "📝 Para ver logs: docker compose logs -f [service]"
echo "📝 Para ver logs: docker compose logs -f [service]"


