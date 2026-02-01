#!/bin/sh
# Script para iniciar toda a infraestrutura com Docker Compose

echo "🚀 Iniciando infraestrutura do Placar Realtime..."
echo ""

cd "$(dirname "$0")"

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Por favor, inicie o Docker primeiro."
    exit 1
fi

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
echo "   RabbitMQ Admin: http://localhost:15672 (user: rabbitmq_user, pass: rabbitmq_pass)"
echo "   pgAdmin:        http://localhost:5050 (email: admin@placar.com, pass: admin)"
echo "   Payara Admin:   http://localhost:4848 (user: admin, pass: admin123)"
echo "   Payara HTTP:    http://localhost:8080"
echo ""
echo "📝 Para parar os serviços: ./stop-infrastructure.sh"
echo "📝 Para ver logs: docker-compose logs -f [service]"