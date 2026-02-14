#!/bin/sh
# Script para parar toda a infraestrutura

echo "🛑 Parando infraestrutura do Placar Realtime..."
echo ""

cd inicializacao
docker compose down

echo ""
echo "🛑 Removendo volumes (APAGA DADOS)..."
docker compose down -v
echo ""

echo "✅ Infraestrutura parada com sucesso!"
echo ""
echo "📝 Para iniciar novamente: ./start.sh"