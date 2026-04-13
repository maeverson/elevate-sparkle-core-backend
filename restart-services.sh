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

echo "=== Parando containers ==="
$COMPOSE_CMD down -v 2>/dev/null || true

echo "=== Removendo containers órfãos ==="
$RUNTIME rm -f sparkle-backend sparkle-postgres sparkle-localstack sparkle-otel-collector sparkle-prometheus sparkle-grafana 2>/dev/null || true

echo "=== Limpando redes ==="
$RUNTIME network prune -f 2>/dev/null || true

echo "=== Aguardando 2 segundos ==="
sleep 2

echo "=== Iniciando serviços ==="
$COMPOSE_CMD up -d

echo "=== Aguardando inicialização (30s) ==="
sleep 30

echo "=== Status dos containers ==="
$RUNTIME ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "=== Logs do backend ==="
$RUNTIME logs --tail 20 sparkle-backend

echo ""
echo "=== Verificando saúde do backend ==="
curl -s http://localhost:8080/actuator/health || echo "Backend ainda não está pronto"
