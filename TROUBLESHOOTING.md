# 🔧 Troubleshooting - Build & Run

## ❌ Erro: "Unable to find a suitable main class"

### Causa
Você está executando `mvn spring-boot:run` no módulo **root** (parent POM) que não tem uma classe main.

### ✅ Soluções

#### Solução 1: Usar o script de inicialização (Recomendado)
```bash
./start-app.sh
```

#### Solução 2: Executar no módulo bootstrap
```bash
cd bootstrap
mvn spring-boot:run
```

#### Solução 3: Executar do root especificando o módulo
```bash
mvn spring-boot:run -pl bootstrap
```

#### Solução 4: Executar o JAR compilado
```bash
# 1. Build
mvn clean package -DskipTests

# 2. Executar
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar
```

---

## 📋 Comandos Importantes

### Build
```bash
# Build completo (sem testes)
mvn clean install -DskipTests

# Build com testes
mvn clean install

# Build apenas do módulo bootstrap
mvn clean install -pl bootstrap -am
```

### Executar Aplicação
```bash
# Com script (recomendado)
./start-app.sh

# Com Maven
cd bootstrap && mvn spring-boot:run

# Com JAR
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar

# Com profile específico
cd bootstrap && mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Infraestrutura (Docker)
```bash
# Iniciar serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar serviços
docker-compose down

# Recriar (limpa volumes)
docker-compose down -v && docker-compose up -d
```

### Migrations (Flyway)
```bash
# Ver status
mvn flyway:info

# Aplicar migrations
mvn flyway:migrate

# Validar
mvn flyway:validate

# Limpar (⚠️ CUIDADO - apenas dev!)
mvn flyway:clean
```

---

## 🐳 Setup Completo (Passo a Passo)

### 1. Iniciar Docker (se necessário)
```bash
sudo systemctl start docker
# ou
sudo service docker start
```

### 2. Iniciar infraestrutura
```bash
cd elevate-sparkle-core-backend
docker-compose up -d
```

### 3. Aguardar PostgreSQL ficar pronto (~10 segundos)
```bash
docker-compose logs -f postgres
# Aguarde: "database system is ready to accept connections"
# Ctrl+C para sair dos logs
```

### 4. Build do projeto
```bash
mvn clean install -DskipTests
```

### 5. Executar aplicação
```bash
./start-app.sh
# ou
cd bootstrap && mvn spring-boot:run
```

### 6. Verificar se está funcionando
```bash
# Em outro terminal:
curl http://localhost:8080/actuator/health

# Deve retornar:
# {"status":"UP"}
```

---

## 🔍 Verificar Problemas

### PostgreSQL não está pronto
```bash
# Ver logs do PostgreSQL
docker-compose logs postgres

# Reiniciar PostgreSQL
docker-compose restart postgres
```

### Porta 8080 já em uso
```bash
# Ver o que está usando a porta
sudo lsof -i :8080
# ou
sudo netstat -tulpn | grep 8080

# Matar processo
kill -9 <PID>
```

### Erro de conexão com banco
```bash
# Verificar se PostgreSQL está acessível
docker exec -it sparkle-postgres psql -U sparkle -d sparkledb -c "SELECT 1;"

# Deve retornar: "1"
```

### Migrations falharam
```bash
# Ver status
mvn flyway:info

# Reparar (se migration falhou no meio)
mvn flyway:repair

# Limpar e recriar (⚠️ apenas dev!)
mvn flyway:clean && mvn flyway:migrate
```

---

## 📚 Estrutura de Módulos

```
elevate-sparkle-core-backend/
├── pom.xml                    # Parent POM (sem main class)
├── core-domain/               # Módulo de domínio
├── core-application/          # Módulo de aplicação
├── adapter-in-web/            # Controllers REST
├── adapter-out-persistence/   # JPA/Database
├── adapter-out-messaging/     # SQS
├── infrastructure/            # Security, Config
└── bootstrap/                 # ⭐ MÓDULO EXECUTÁVEL (tem main class)
    └── src/main/java/
        └── SparkleBackendApplication.java  # ← Classe principal
```

**⚠️ IMPORTANTE:** Apenas o módulo `bootstrap` tem a classe main e pode ser executado!

---

## 🎯 URLs de Acesso

Após iniciar a aplicação:

- **API Base**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

---

## 🔐 Credenciais Padrão

### Banco de Dados
- **Host**: localhost:5432
- **Database**: sparkledb
- **Username**: sparkle
- **Password**: sparkle123

### Usuário Sistema (App)
- **Username**: system
- **Email**: system@sparkle.local
- **Password**: admin123

### Grafana
- **Username**: admin
- **Password**: admin

---

## ⚡ Quick Reference

```bash
# Start tudo de uma vez
./start-app.sh

# Build rápido
mvn clean install -DskipTests

# Run rápido
cd bootstrap && mvn spring-boot:run

# Ver logs
docker-compose logs -f

# Health check
curl http://localhost:8080/actuator/health
```

---

## 🆘 Precisa de Ajuda?

1. Verifique os logs: `docker-compose logs -f`
2. Verifique o status dos containers: `docker-compose ps`
3. Verifique as migrations: `mvn flyway:info`
4. Reinicie a infraestrutura: `docker-compose restart`
5. Limpe e recrie (último recurso): `docker-compose down -v && docker-compose up -d`
