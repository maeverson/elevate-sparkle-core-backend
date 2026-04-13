#!/bin/bash

# ========================================
# Sparkle Backend Startup Script
# ========================================
# Inicia a aplicação Spring Boot
# Uso: ./start-app.sh [profile]

set -e

PROFILE=${1:-dev}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

choose_compose_cmd() {
    if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
        if docker compose version >/dev/null 2>&1; then
            echo "docker compose"
            return
        fi
        if command -v docker-compose >/dev/null 2>&1; then
            echo "docker-compose"
            return
        fi
    fi

    if command -v podman-compose >/dev/null 2>&1; then
        echo "podman-compose"
        return
    fi

    echo ""
}

COMPOSE_CMD="$(choose_compose_cmd)"

if [ -z "$COMPOSE_CMD" ]; then
    echo "❌ Nenhum runtime de compose disponível foi encontrado."
    echo "   Instale/ative Docker (daemon) ou Podman + podman-compose."
    exit 1
fi

echo "🚀 Starting Sparkle Backend Application..."
echo "📋 Profile: $PROFILE"
echo "🐳 Compose: $COMPOSE_CMD"
echo ""

# Verificar se a infraestrutura está rodando
echo "🔍 Checking infrastructure..."
if ! $COMPOSE_CMD ps 2>/dev/null | grep -q sparkle-postgres; then
    echo "⚠️  PostgreSQL não está rodando!"
    echo "   Execute: $COMPOSE_CMD up -d"
    echo ""
    read -p "Deseja iniciar a infraestrutura agora? [y/N] " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🐳 Starting infrastructure..."
        $COMPOSE_CMD up -d
        echo "⏳ Waiting for PostgreSQL to be ready..."
        sleep 10
    else
        echo "❌ Abortado. Inicie a infraestrutura primeiro."
        exit 1
    fi
fi

echo "✅ Infrastructure is ready"
echo ""

# Build se necessário
if [ ! -d "bootstrap/target" ]; then
    echo "📦 Building project (first run)..."
    mvn clean install -DskipTests
    echo ""
fi

# Executar aplicação
echo "🚀 Starting application..."
echo "   Access: http://localhost:8080"
echo "   Health: http://localhost:8080/actuator/health"
echo "   API Docs: http://localhost:8080/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop"
echo ""

cd bootstrap && mvn spring-boot:run -Dspring-boot.run.profiles=$PROFILE
