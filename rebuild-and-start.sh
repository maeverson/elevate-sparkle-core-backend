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
$COMPOSE_CMD down -v

echo "=== Removendo imagem antiga do backend ==="
$RUNTIME rmi localhost/elevate-sparkle-core-backend_app 2>/dev/null || echo "Imagem não existe"

echo "=== Aguardando 3 segundos ==="
sleep 3

echo "=== Reconstruindo e iniciando serviços ==="
$COMPOSE_CMD up -d --build

echo "=== Aguardando inicialização (45s para build + start) ==="
for i in {1..45}; do
  echo -n "."
  sleep 1
done
echo ""

echo "=== Status dos containers ==="
$RUNTIME ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "=== Verificando logs do backend (últimas 30 linhas) ==="
$RUNTIME logs --tail 30 sparkle-backend 2>&1 || echo "Backend ainda não iniciou"

echo ""
echo "=== Testando conectividade ==="
curl -sf http://localhost:8080/actuator/health && echo "✓ Backend está saudável!" || echo "✗ Backend ainda não está disponível"
