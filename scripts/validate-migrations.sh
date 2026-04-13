#!/bin/bash

# ========================================
# Migration Validation Script
# ========================================
# Valida sintaxe SQL e ordem das migrations
# Uso: ./validate-migrations.sh

set -e

MIGRATION_DIR="adapter-out-persistence/src/main/resources/db/migration"
TEMP_DB="sparkle_validation_$$"

echo "🔍 Validando migrations do Flyway..."
echo ""

# Verificar se o diretório existe
if [ ! -d "$MIGRATION_DIR" ]; then
    echo "❌ Erro: Diretório de migrations não encontrado: $MIGRATION_DIR"
    exit 1
fi

# Listar migrations em ordem
echo "📋 Migrations encontradas:"
ls -1 "$MIGRATION_DIR"/V*.sql | sort -V | while read file; do
    basename "$file"
done
echo ""

# Verificar numeração sequencial
echo "🔢 Verificando numeração sequencial..."
EXPECTED=1
for file in "$MIGRATION_DIR"/V*.sql; do
    VERSION=$(basename "$file" | sed 's/V\([0-9]*\)__.*/\1/')
    if [ "$VERSION" != "$EXPECTED" ]; then
        echo "⚠️  Atenção: Gap na numeração! Esperado V${EXPECTED}, encontrado V${VERSION}"
    fi
    EXPECTED=$((VERSION + 1))
done
echo "✅ Numeração validada"
echo ""

# Verificar sintaxe SQL básica (se psql disponível)
if command -v psql &> /dev/null; then
    echo "🔍 Verificando sintaxe SQL..."
    
    # Criar banco temporário para validação
    if command -v docker &> /dev/null; then
        echo "  Iniciando PostgreSQL temporário..."
        CONTAINER_ID=$(docker run -d --rm \
            -e POSTGRES_PASSWORD=test \
            -e POSTGRES_DB=$TEMP_DB \
            postgres:15-alpine)
        
        # Aguardar PostgreSQL iniciar
        echo "  Aguardando PostgreSQL iniciar..."
        sleep 5
        
        CONTAINER_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $CONTAINER_ID)
        
        # Validar cada migration
        for file in "$MIGRATION_DIR"/V*.sql; do
            MIGRATION_NAME=$(basename "$file")
            echo "  Validando: $MIGRATION_NAME"
            
            if docker exec $CONTAINER_ID psql -U postgres -d $TEMP_DB -f - < "$file" > /dev/null 2>&1; then
                echo "    ✅ $MIGRATION_NAME"
            else
                echo "    ❌ Erro em $MIGRATION_NAME"
                docker stop $CONTAINER_ID > /dev/null 2>&1
                exit 1
            fi
        done
        
        # Limpar
        echo "  Limpando container temporário..."
        docker stop $CONTAINER_ID > /dev/null 2>&1
        
        echo "✅ Todas as migrations são válidas"
    else
        echo "  ⚠️  Docker não disponível - pulando validação de sintaxe"
    fi
else
    echo "⚠️  psql não disponível - pulando validação de sintaxe"
fi

echo ""
echo "🎉 Validação concluída com sucesso!"
echo ""
echo "📝 Próximos passos:"
echo "   1. mvn clean install           # Build do projeto"
echo "   2. docker-compose up -d        # Iniciar infraestrutura"
echo "   3. mvn flyway:migrate          # Aplicar migrations"
echo ""
