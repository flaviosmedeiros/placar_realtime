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
echo "✅ Docker está funcionando"


# Iniciar containers
echo "📦 Iniciando containers..."
docker compose up -d

echo ""
echo "⏳ Aguardando serviços ficarem prontos..."
sleep 10

# Verificar status dos containers
echo ""
echo "📊 Status dos containers:"
docker compose ps

echo ""
echo "✅ Infraestrutura iniciada com sucesso!"
echo ""
echo "📝 Para parar os serviços: ./stop.sh"
echo "📝 Para ver logs: docker compose logs -f [service]"



