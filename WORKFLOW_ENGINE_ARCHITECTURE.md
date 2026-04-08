# Event-Sourced Workflow Engine - Architecture Documentation

## Overview

This is a **production-grade, event-sourced workflow engine** built with:

- **Event Sourcing**: All state is derived from events (append-only)
- **Hexagonal Architecture**: Clear separation between domain, application, and infrastructure
- **CQRS**: Command-Query Responsibility Segregation
- **Deterministic Execution**: No side effects in the core
- **Distributed-Ready**: Built for horizontal scaling

## Architecture

### Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                        ADAPTERS-IN                           │
│                  (adapter-in-web)                            │
│                                                              │
│  ┌────────────────────────────────────────────────┐        │
│  │  ExecutionController (REST API)                │        │
│  │  - POST /api/executions/start                  │        │
│  │  - GET /api/executions/{id}                    │        │
│  │  - GET /api/executions/{id}/events             │        │
│  └────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                         │
│                  (core-application)                          │
│                                                              │
│  ┌────────────────────────────────────────────────┐        │
│  │  WorkflowEngine                                │        │
│  │  - Pure business logic                         │        │
│  │  - Framework-agnostic                          │        │
│  │  - Orchestrates commands & events              │        │
│  └────────────────────────────────────────────────┘        │
│                                                              │
│  ┌────────────────────────────────────────────────┐        │
│  │  StepExecutors (Strategy Pattern)              │        │
│  │  - HttpStepExecutor                            │        │
│  │  - InternalStepExecutor                        │        │
│  │  - MessageStepExecutor                         │        │
│  └────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│                    (core-domain)                             │
│                                                              │
│  ┌────────────────────────────────────────────────┐        │
│  │  JourneyExecutionAggregate (Aggregate Root)    │        │
│  │  - Event application (apply)                   │        │
│  │  - Business rules enforcement                  │        │
│  │  - State derived from events                   │        │
│  └────────────────────────────────────────────────┘        │
│                                                              │
│  ┌────────────────────────────────────────────────┐        │
│  │  Domain Events (Immutable)                     │        │
│  │  - JourneyStartedEvent                         │        │
│  │  - StepScheduledEvent                          │        │
│  │  - StepStartedEvent                            │        │
│  │  - StepCompletedEvent                          │        │
│  │  - StepFailedEvent                             │        │
│  │  - JourneyCompletedEvent                       │        │
│  │  - JourneyFailedEvent                          │        │
│  └────────────────────────────────────────────────┘        │
│                                                              │
│  ┌────────────────────────────────────────────────┐        │
│  │  Commands                                      │        │
│  │  - StartJourneyCommand                         │        │
│  │  - StartStepCommand                            │        │
│  │  - CompleteStepCommand                         │        │
│  │  - FailStepCommand                             │        │
│  └────────────────────────────────────────────────┘        │
│                                                              │
│  ┌────────────────────────────────────────────────┐        │
│  │  Ports (Interfaces)                            │        │
│  │  - ExecutionEventRepository                    │        │
│  │  - StepExecutor                                │        │
│  └────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     ADAPTERS-OUT                             │
│                (adapter-out-persistence)                     │
│                                                              │
│  ┌────────────────────────────────────────────────┐        │
│  │  EventStoreAdapter                             │        │
│  │  - Implements ExecutionEventRepository         │        │
│  │  - JPA/Hibernate integration                   │        │
│  │  - PostgreSQL with JSONB                       │        │
│  └────────────────────────────────────────────────┘        │
│                                                              │
│  ┌────────────────────────────────────────────────┐        │
│  │  Event Serializer                              │        │
│  │  - Event <-> JSON conversion                   │        │
│  │  - Schema versioning support                   │        │
│  └────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
                    ┌─────────────┐
                    │  PostgreSQL │
                    │  event_store│
                    │  (JSONB)    │
                    └─────────────┘
```

## Event Sourcing Architecture

### Event Store Schema

```sql
CREATE TABLE event_store (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,              -- Execution ID
    aggregate_type VARCHAR(100) NOT NULL,    -- JOURNEY_EXECUTION
    event_type VARCHAR(100) NOT NULL,        -- e.g., "JourneyStarted"
    event_version INTEGER NOT NULL,          -- Schema version
    sequence_number BIGINT NOT NULL,         -- Ordering within aggregate
    payload JSONB NOT NULL,                  -- Event data
    metadata JSONB NOT NULL,                 -- Metadata
    created_at TIMESTAMP NOT NULL,
    
    UNIQUE (aggregate_id, sequence_number)   -- Optimistic locking
);
```

### Event Flow

```
1. Command arrives → WorkflowEngine
2. Load events from Event Store
3. Rebuild Aggregate from events (rehydration)
4. Execute command on Aggregate
5. Aggregate emits new events
6. Persist events to Event Store
7. Update projections (if any)
```

### Key Principles

1. **Append-Only**: Events are NEVER updated or deleted
2. **Immutable**: Events are immutable records of facts
3. **Ordered**: Events have sequence numbers within aggregates
4. **Deterministic**: Same events = same state
5. **Auditability**: Complete history of all changes

## Module Structure

### core-domain (Pure Domain)

**NO FRAMEWORK DEPENDENCIES**

- Domain Events (`DomainEvent`, `JourneyStartedEvent`, etc.)
- Aggregate Root (`JourneyExecutionAggregate`)
- Value Objects (`ExecutionStatus`, `StepStatus`)
- Commands (`StartJourneyCommand`, etc.)
- Ports (interfaces): `ExecutionEventRepository`, `StepExecutor`
- Domain Exceptions

**Key Classes:**

- `JourneyExecutionAggregate`: Rebuilds state from events, enforces business rules
- `DomainEvent`: Base interface for all events
- Events: Immutable records representing facts

### core-application (Application Services)

**Framework-agnostic business logic**

- `WorkflowEngine`: Orchestrates command execution
- Step Executors: `HttpStepExecutor`, `InternalStepExecutor`, `MessageStepExecutor`
- `CompositeStepExecutor`: Delegates to specific executors

**WorkflowEngine Flow:**

```java
1. loadAggregate(executionId)
   └─> eventRepository.findByAggregateId()
   └─> JourneyExecutionAggregate.fromEvents()
   
2. aggregate.executeCommand()
   └─> Business logic
   └─> Emit events
   
3. eventRepository.saveEvents()
   └─> Optimistic locking
   └─> Append to event store
```

### adapter-out-persistence (Infrastructure)

- `EventStoreAdapter`: JPA implementation of `ExecutionEventRepository`
- `EventStoreEntity`: JPA entity for `event_store` table
- `EventStoreJpaRepository`: Spring Data repository
- `EventSerializer`: JSON serialization/deserialization
- Database migration: `V3__create_event_store.sql`

### adapter-in-web (REST API)

- `ExecutionController`: REST endpoints
- DTOs: `StartJourneyRequest`, `ExecutionResponse`, etc.

### bootstrap (Configuration)

- `WorkflowEngineConfig`: Spring beans configuration
- Wires together hexagonal architecture components

## API Endpoints

### Start Journey

```http
POST /api/executions/start
Content-Type: application/json

{
  "journeyDefinitionId": "uuid",
  "journeyVersion": "1.0.0",
  "initialContext": {
    "userId": "user123",
    "orderTotal": 100.50
  },
  "startedBy": "admin"
}

Response:
{
  "status": "success",
  "data": {
    "executionId": "uuid"
  }
}
```

### Get Execution Details

```http
GET /api/executions/{executionId}

Response:
{
  "status": "success",
  "data": {
    "executionId": "uuid",
    "journeyDefinitionId": "uuid",
    "journeyVersion": "1.0.0",
    "status": "RUNNING",
    "context": { ... },
    "currentStepId": "step-1",
    "startedAt": "2026-04-07T10:00:00Z",
    "version": 5,
    "steps": {
      "step-1": {
        "stepId": "step-1",
        "stepType": "HTTP",
        "status": "COMPLETED",
        ...
      }
    }
  }
}
```

### Get Event Stream

```http
GET /api/executions/{executionId}/events

Response:
{
  "status": "success",
  "data": [
    {
      "eventType": "JourneyStarted",
      "sequenceNumber": 0,
      "occurredAt": "2026-04-07T10:00:00Z",
      "payload": { ... }
    },
    ...
  ]
}
```

### Complete Step

```http
POST /api/executions/{executionId}/steps/complete
Content-Type: application/json

{
  "stepId": "step-1",
  "outputData": {
    "result": "success",
    "value": 42
  },
  "durationMs": 1000
}
```

## Usage Examples

### Starting a Journey

```java
// The engine handles everything:
// 1. Creates new aggregate
// 2. Executes command
// 3. Persists events

UUID executionId = workflowEngine.startJourney(
    new StartJourneyCommand(
        null,                      // Auto-generated
        journeyDefinitionId,
        "1.0.0",
        Map.of("userId", "123"),
        "admin",
        Instant.now()
    )
);
```

### Completing a Step

```java
// The engine handles everything:
// 1. Loads events
// 2. Rebuilds aggregate
// 3. Executes command
// 4. Persists new events

workflowEngine.completeStep(
    new CompleteStepCommand(
        executionId,
        "step-1",
        Map.of("result", "ok"),
        1000L,
        Instant.now()
    )
);
```

### Querying Execution State

```java
// Rebuild aggregate from events
JourneyExecutionAggregate aggregate = 
    workflowEngine.getExecution(executionId);

// All state is derived from events
ExecutionStatus status = aggregate.getStatus();
Map<String, Object> context = aggregate.getContext();
```

## Determinism Guarantees

### ✅ What Makes It Deterministic

1. **No System.currentTimeMillis()**: Timestamps come from commands
2. **No Random**: All IDs passed via commands
3. **Event-Driven State**: State = f(events)
4. **Pure Functions**: Aggregate methods are pure

### Example

```java
// BAD (non-deterministic)
event.occurredAt = Instant.now();  // ❌

// GOOD (deterministic)
event.occurredAt = command.timestamp;  // ✅
```

## Concurrency Control

### Optimistic Locking

Uses sequence numbers for optimistic locking:

```java
// Database constraint
UNIQUE (aggregate_id, sequence_number)

// On conflict
throw new ConcurrencyException(aggregateId, expectedVersion, actualVersion);
```

### Handling Conflicts

```java
try {
    eventRepository.saveEvents(events);
} catch (ConcurrencyException e) {
    // Retry with fresh state
    aggregate = loadAggregate(executionId);
    // Re-execute command
}
```

## Testing

### Aggregate Tests

```java
@Test
void shouldRebuildFromEvents() {
    List<DomainEvent> events = List.of(
        new JourneyStartedEvent(...),
        new StepScheduledEvent(...),
        new StepCompletedEvent(...)
    );
    
    JourneyExecutionAggregate aggregate = 
        JourneyExecutionAggregate.fromEvents(events);
    
    assertEquals(ExecutionStatus.RUNNING, aggregate.getStatus());
    assertEquals(2L, aggregate.getVersion());
}
```

### Engine Tests

```java
@Test
void shouldPersistEventsWhenCommandExecuted() {
    UUID executionId = engine.startJourney(command);
    
    verify(eventRepository).saveEvents(eventsCaptor.capture());
    
    List<DomainEvent> events = eventsCaptor.getValue();
    assertTrue(events.get(0) instanceof JourneyStartedEvent);
}
```

## Extending the Engine

### Adding New Event Types

1. Create event class implementing `DomainEvent`
2. Add `apply*` method to aggregate
3. Update `EventSerializer`
4. Add to aggregate's event switch

### Adding New Step Executors

1. Implement `StepExecutor` interface
2. Register in `WorkflowEngineConfig`
3. Add to `CompositeStepExecutor`

### Adding Snapshots (Future)

For performance with long event streams:

```java
// Save snapshot every N events
if (aggregate.getVersion() % 100 == 0) {
    snapshotRepository.save(aggregate);
}

// Load from snapshot + events
Snapshot snapshot = snapshotRepository.load(executionId);
List<Event> events = eventRepository.findFromSequence(
    executionId, 
    snapshot.version + 1
);
aggregate = JourneyExecutionAggregate.fromSnapshot(snapshot, events);
```

## Performance Considerations

### Event Store Indexes

```sql
-- Critical for fast aggregate loading
CREATE INDEX idx_event_store_aggregate_id 
ON event_store(aggregate_id, sequence_number);

-- For event type queries
CREATE INDEX idx_event_store_event_type 
ON event_store(event_type, created_at);
```

### Projections (Future)

For read-heavy workloads, create separate read models:

```sql
CREATE TABLE execution_projection (
    execution_id UUID PRIMARY KEY,
    status VARCHAR(50),
    current_step_id VARCHAR(255),
    context JSONB,
    updated_at TIMESTAMP
);
```

## Deployment & Scaling

### Horizontal Scaling

- Multiple engine instances
- Event Store handles concurrency
- Workers can be distributed

### Event Replay

Rebuild all state from events:

```bash
# Replay events for aggregate
SELECT * FROM event_store 
WHERE aggregate_id = 'xxx' 
ORDER BY sequence_number;
```

## Security Considerations

1. **Audit Trail**: All changes are in event store
2. **Tamper-Proof**: Append-only = hard to modify history
3. **Point-in-Time Recovery**: Replay to any point
4. **Compliance**: Complete audit log

## Migration from State-Based Systems

1. Dual-write events + state (transition period)
2. Validate consistency
3. Switch to event-sourced reads
4. Remove state tables

## References

- Event Sourcing: Martin Fowler, Greg Young
- Hexagonal Architecture: Alistair Cockburn
- CQRS: Greg Young, Udi Dahan
- Domain-Driven Design: Eric Evans

---

**Built with:**
- Java 17+
- Spring Boot 3.x
- PostgreSQL 14+ (JSONB support)
- Hibernate 6.x
- JUnit 5 & Mockito
