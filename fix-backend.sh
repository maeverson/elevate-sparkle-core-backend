#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

choose_runtime_and_compose() {
    if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
        if docker compose version >/dev/null 2>&1; then
            echo "docker|docker compose"
            return
        fi
        if command -v docker-compose >/dev/null 2>&1; then
            echo "docker|docker-compose"
            return
        fi
    fi

    if command -v podman >/dev/null 2>&1 && command -v podman-compose >/dev/null 2>&1; then
        echo "podman|podman-compose"
        return
    fi

    echo "|"
}

RUNTIME_AND_COMPOSE="$(choose_runtime_and_compose)"
RUNTIME="${RUNTIME_AND_COMPOSE%%|*}"
COMPOSE_CMD="${RUNTIME_AND_COMPOSE#*|}"

if [ -z "$RUNTIME" ] || [ -z "$COMPOSE_CMD" ]; then
    echo "❌ Não foi possível encontrar Docker Compose ou Podman Compose." >&2
    exit 1
fi

echo "========================================"
echo "  CORRIGINDO AMBIENTE BACKEND"
echo "========================================"
echo ""

echo "1. Parando todos os containers..."
$COMPOSE_CMD down -v 2>/dev/null || true

echo ""
echo "2. Removendo containers órfãos..."
$RUNTIME rm -f sparkle-backend sparkle-postgres sparkle-localstack sparkle-otel-collector sparkle-prometheus sparkle-grafana 2>/dev/null || true

echo ""
echo "3. Removendo imagem antiga do backend..."
$RUNTIME rmi localhost/elevate-sparkle-core-backend_app 2>/dev/null || true

echo ""
echo "4. Limpando redes..."
$RUNTIME network rm elevate-sparkle-core-backend_sparkle-network 2>/dev/null || true
$RUNTIME network prune -f 2>/dev/null || true

echo ""
echo "5. Aguardando 3 segundos..."
sleep 3

echo ""
echo "6. Reconstruindo e iniciando serviços (isso vai demorar ~2 minutos)..."
$COMPOSE_CMD up -d --build

echo ""
echo "7. Aguardando inicialização dos serviços..."
echo "   - PostgreSQL: 15 segundos"
sleep 15

echo "   - LocalStack e outros: 10 segundos"
sleep 10

echo "   - Backend: 20 segundos"
sleep 20

echo ""
echo "========================================"
echo "  STATUS DOS CONTAINERS"
echo "========================================"
$RUNTIME ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "========================================"
echo "  VERIFICANDO REDE"
echo "========================================"
$RUNTIME network inspect elevate-sparkle-core-backend_sparkle-network --format '{{range .Containers}}Container: {{.Name}} - IP: {{.IPv4Address}}{{"\n"}}{{end}}' 2>/dev/null || echo "Rede não encontrada"

echo ""
echo "========================================"
echo "  LOGS DO BACKEND (últimas 20 linhas)"
echo "========================================"
$RUNTIME logs sparkle-backend 2>&1 | tail -n 20 || echo "Backend ainda não iniciou"

echo ""
echo "========================================"
echo "  TESTE DE CONECTIVIDADE"
echo "========================================"
echo -n "Backend health check: "
if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "✓ FUNCIONANDO!"
    echo ""
    curl -s http://localhost:8080/actuator/health | head -5
else
    echo "✗ Ainda não disponível (pode levar mais alguns segundos)"
    echo ""
    echo "Execute para monitorar:"
    echo "  watch -n 2 'curl -s http://localhost:8080/actuator/health'"
fi

echo ""
echo "========================================"
echo " FINALIZADO!"
echo "========================================"
