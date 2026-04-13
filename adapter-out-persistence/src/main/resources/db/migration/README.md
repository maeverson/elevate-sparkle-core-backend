# Database Migrations

Este diretório contém as migrations do Flyway para o banco de dados PostgreSQL do projeto Sparkle Core Backend.

## Estrutura de Migrations

### V1: Tabelas de Usuários
**Arquivo:** `V1__create_users_table.sql`
- Cria tabela `users` com autenticação e informações do usuário
- Cria tabela `user_roles` para gestão de permissões (RBAC)
- Índices em `username` e `email` para performance

### V2: Tabelas de Pedidos
**Arquivo:** `V2__create_orders_table.sql`
- Cria tabela `orders` para gerenciamento de pedidos
- Cria tabela `order_items` para itens do pedido
- Relacionamentos: `orders.user_id -> users.id`
- Índices em `user_id`, `status`, `created_at` para queries de dashboard

### V3: Event Store (Event Sourcing)
**Arquivo:** `V3__create_event_store.sql`
- Cria tabela append-only `event_store` para event sourcing
- Cria tabela `event_snapshot` para otimização de performance
- **IMPORTANTE:** Nunca fazer UPDATE ou DELETE no event_store
- Índices otimizados para reconstrução de agregados
- Suporte a JSONB para payloads de eventos

### V4: Tabelas de Jornadas (Journey Orchestration)
**Arquivo:** `V4__create_journey_tables.sql`
- Cria tabela `journey_definitions` para definições de workflows
- Cria tabela `journey_versions` para versionamento imutável
- Relacionamento bidirecional com `current_published_version_id`
- DSL em formato JSONB para flexibilidade
- Status: DRAFT, PUBLISHED, ARCHIVED

### V5: Projeções CQRS
**Arquivo:** `V5__create_execution_projections.sql`
- Cria tabela `execution_summary` (read model)
- Otimizada para queries de dashboard e monitoramento
- Atualizada via domain events (pattern CQRS)
- Índices compostos para queries complexas

### V6: Dados Seed - Sistema
**Arquivo:** `V6__seed_system_user.sql`
- Cria usuário sistema padrão (ID: `00000000-0000-0000-0000-000000000001`)
- Username: `system`
- Password padrão: `admin123` (BCrypt hash)
- **⚠️ PRODUÇÃO:** Alterar senha imediatamente!

### V7: Dados Seed - Teste
**Arquivo:** `V7__seed_test_data.sql`
- Cria 3 journey definitions de exemplo
- Cria versões publicadas para cada journey
- Popula histórico de execuções (últimos 30 dias)
- Dados para desenvolvimento e demonstração

## Comandos Úteis

### Verificar status das migrations
```bash
mvn flyway:info
```

### Aplicar migrations pendentes
```bash
mvn flyway:migrate
```

### Limpar banco (⚠️ CUIDADO - apenas dev!)
```bash
mvn flyway:clean
```

### Validar migrations
```bash
mvn flyway:validate
```

## Convenções

### Nomenclatura
- Formato: `V{VERSION}__{DESCRIPTION}.sql`
- Versão: número sequencial (V1, V2, V3...)
- Descrição: snake_case descritivo

### Boas Práticas
- ✅ Migrations são imutáveis - nunca editar após deploy
- ✅ Sempre use transações implícitas (padrão do Flyway)
- ✅ Adicione índices para queries frequentes
- ✅ Use `ON DELETE CASCADE` com cuidado
- ✅ Documente constraints complexas com COMMENTs
- ❌ Nunca fazer DROP em produção sem backup
- ❌ Evitar migrations lentas (> 5 segundos)

## Troubleshooting

### Migration falhou - e agora?

1. **Verificar logs:**
   ```bash
   mvn flyway:info
   ```

2. **Reparar schema após falha:**
   ```bash
   mvn flyway:repair
   ```

3. **Em desenvolvimento - limpar e recriar:**
   ```bash
   mvn flyway:clean && mvn flyway:migrate
   ```

### Conflito de versão

Se múltiplas branches criaram a mesma versão:
```bash
# Renumerar migration conflitante para próxima versão disponível
mv V7__my_feature.sql V8__my_feature.sql
```

## Configuração

### application.yml
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public
```

### Variáveis de Ambiente
```bash
DB_URL=jdbc:postgresql://localhost:5432/sparkledb
DB_USERNAME=sparkle
DB_PASSWORD=sparkle123
```

## Schema Atual

### Diagrama ER Simplificado
```
users (1) ----< (N) orders
                    |
                    +----< order_items

journey_definitions (1) ----< (N) journey_versions
        |                              ^
        +------------------------------+ (current_published_version_id)

journey_definitions (1) ----< (N) execution_summary

event_store (aggregate_id) --> journey execution events
```

### Tabelas por Contexto

**Autenticação & Autorização:**
- users
- user_roles

**Domínio de Negócio:**
- orders
- order_items

**Journey Orchestration:**
- journey_definitions
- journey_versions
- execution_summary

**Event Sourcing:**
- event_store
- event_snapshot

## Próximos Passos

Features planejadas para futuras migrations:

- [ ] **V8:** Tabela de connectors (integrações externas)
- [ ] **V9:** Tabela de audit_log (auditoria de ações)
- [ ] **V10:** Tabela de notifications (notificações de sistema)
- [ ] **V11:** Índices adicionais baseados em métricas de produção

## Referências

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL Best Practices](https://wiki.postgresql.org/wiki/Don%27t_Do_This)
- [Flyway Naming Patterns](https://flywaydb.org/documentation/concepts/migrations#naming)
