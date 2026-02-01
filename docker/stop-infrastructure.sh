#!/bin/sh
# Script para parar toda a infraestrutura

echo "🛑 Parando infraestrutura do Placar Realtime..."
echo ""

cd "$(dirname "$0")"

docker compose down

echo ""
echo "✅ Infraestrutura parada com sucesso!"
echo ""
echo "📝 Para iniciar novamente: ./start-infrastructure.sh"
echo "📝 Para remover volumes (APAGA DADOS): docker compose down -v"
