# Quick Start Guide

## 🚀 Setup em 5 minutos

### 1. Pré-requisitos
```bash
# Verificar versões
java -version  # Java 17+
mvn -version   # Maven 3.8+
docker --version
docker-compose --version
```

### 2. Build e Run
```bash
# Clone e entre no diretório
cd elevate-sparkle-core-backend

# Build do projeto
mvn clean install -DskipTests

# Inicie os serviços com Docker
docker-compose up -d

# Aguarde ~30 segundos para todos os serviços iniciarem
sleep 30

# Inicialize o LocalStack (SQS)
./scripts/init-localstack.sh
```

### 3. Teste a API

#### Registrar um usuário
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "fullName": "John Doe"
  }'
```

#### Fazer login e obter JWT
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Copie o token JWT retornado!**

#### Criar um pedido
```bash
export JWT_TOKEN="<seu-token-aqui>"

curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "shippingAddress": {
      "street": "123 Main Street",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    },
    "items": [
      {
        "productId": "550e8400-e29b-41d4-a716-446655440000",
        "productName": "MacBook Pro 16",
        "quantity": 1,
        "unitPrice": {
          "amount": 2499.99,
          "currency": "USD"
        }
      }
    ]
  }'
```

#### Listar pedidos
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## 📊 Acessar ferramentas

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **API** | http://localhost:8080 | JWT token |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **Health Check** | http://localhost:8080/actuator/health | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin/admin |
| **PostgreSQL** | localhost:5432 | sparkle/sparkle123 |

## 🧪 Executar testes

```bash
# Testes unitários
mvn test

# Testes de integração
mvn verify

# Testes com cobertura
mvn test jacoco:report
# Relatório em: target/site/jacoco/index.html
```

## 🔍 Verificar logs

```bash
# Logs da aplicação
docker-compose logs -f app

# Logs do PostgreSQL
docker-compose logs -f postgres

# Logs do LocalStack (SQS)
docker-compose logs -f localstack
```

## 🛑 Parar os serviços

```bash
# Parar containers
docker-compose down

# Parar e remover volumes (limpa dados)
docker-compose down -v
```

## 🐛 Troubleshooting

### Porta 8080 já em uso
```bash
# Verificar processo usando a porta
lsof -i :8080

# Matar processo (Linux/Mac)
kill -9 <PID>
```

### Rebuild completo
```bash
# Limpar tudo e reconstruir
docker-compose down -v
mvn clean
mvn install
docker-compose build --no-cache
docker-compose up -d
```

### Ver mensagens no SQS (LocalStack)
```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

aws --endpoint-url=http://localhost:4566 sqs receive-message \
  --queue-url http://localhost:4566/000000000000/order-events \
  --max-number-of-messages 10
```

## 📖 Próximos passos

1. Explorar a API no Swagger: http://localhost:8080/swagger-ui.html
2. Verificar métricas no Grafana: http://localhost:3000
3. Consultar o [README.md](README.md) completo
4. Revisar a arquitetura hexagonal nos módulos `core-*`

## 💡 Dicas

- Use **httpie** ou **Postman** para testar a API de forma mais amigável
- Configure sua IDE (IntelliJ/Eclipse) para usar o projeto Maven
- Importe a coleção Postman em `postman_collection.json`
- Ative o profile `dev` para desenvolvimento local
- Consulte logs estruturados em JSON no stdout

---

**Pronto para começar! 🎉**
