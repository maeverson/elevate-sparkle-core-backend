package com.elevate.sparkle.adapter.out.persistence.adapter;

import com.elevate.sparkle.adapter.out.persistence.repository.EventStoreJpaRepository;
import com.elevate.sparkle.application.port.out.ExecutionQueryPort;
import com.elevate.sparkle.domain.valueobject.ExecutionStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter for querying execution projections
 * This queries the event store to find executions
 */
@Component
public class ExecutionQueryAdapter implements ExecutionQueryPort {
    
    private final EventStoreJpaRepository eventStoreRepository;
    private final JdbcTemplate jdbcTemplate;
    
    public ExecutionQueryAdapter(EventStoreJpaRepository eventStoreRepository, JdbcTemplate jdbcTemplate) {
        this.eventStoreRepository = eventStoreRepository;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public List<UUID> findExecutionIds(
            UUID journeyDefinitionId,
            ExecutionStatus status,
            Instant startedAfter,
            Instant startedBefore,
            int limit,
            int offset
    ) {
        // For now, get all unique aggregate IDs from event store
        // In production, this should use a proper projection/read model
        return eventStoreRepository.findAll().stream()
                .map(e -> e.getAggregateId())
                .distinct()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countExecutions(
            UUID journeyDefinitionId,
            ExecutionStatus status,
            Instant startedAfter,
            Instant startedBefore
    ) {
        return eventStoreRepository.findAll().stream()
                .map(e -> e.getAggregateId())
                .distinct()
                .count();
    }
    
    @Override
    public List<UUID> findByJourneyId(UUID journeyDefinitionId) {
        // This requires a proper projection table
        // For now, return empty list
        return List.of();
    }
    
    @Override
    public List<UUID> findByStatus(ExecutionStatus status) {
        // This requires a proper projection table
        // For now, return empty list
        return List.of();
    }
    
    @Override
    public List<ExecutionSummary> findByJourneyId(
            UUID journeyId,
            ExecutionStatus status,
            Instant startedAfter,
            Instant startedBefore,
            Integer limit
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT execution_id, journey_id, journey_name, status,
                   started_at, completed_at, duration_ms,
                   total_steps, completed_steps, failed_steps
            FROM execution_summary
            WHERE journey_id = ?
            """);
        
        List<Object> params = new ArrayList<>();
        params.add(journeyId);
        
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        
        if (startedAfter != null) {
            sql.append(" AND started_at >= ?");
            params.add(Timestamp.from(startedAfter));
        }
        
        if (startedBefore != null) {
            sql.append(" AND started_at <= ?");
            params.add(Timestamp.from(startedBefore));
        }
        
        sql.append(" ORDER BY started_at DESC");
        
        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
            params.add(limit);
        }
        
        return jdbcTemplate.query(sql.toString(), new ExecutionSummaryRowMapper(), params.toArray());
    }
    
    @Override
    public List<ExecutionSummary> findByDateRange(Instant from, Instant to) {
        String sql = """
            SELECT execution_id, journey_id, journey_name, status,
                   started_at, completed_at, duration_ms,
                   total_steps, completed_steps, failed_steps
            FROM execution_summary
            WHERE started_at BETWEEN ? AND ?
            ORDER BY started_at DESC
            """;
        
        return jdbcTemplate.query(sql, new ExecutionSummaryRowMapper(), 
            Timestamp.from(from), Timestamp.from(to));
    }
    
    @Override
    public List<ExecutionSummary> findByStatus(ExecutionStatus status, int limit) {
        String sql = """
            SELECT execution_id, journey_id, journey_name, status,
                   started_at, completed_at, duration_ms,
                   total_steps, completed_steps, failed_steps
            FROM execution_summary
            WHERE status = ?
            ORDER BY started_at DESC
            LIMIT ?
            """;
        
        return jdbcTemplate.query(sql, new ExecutionSummaryRowMapper(), status.name(), limit);
    }
    
    /**
     * RowMapper for ExecutionSummary records from execution_summary table
     */
    private static class ExecutionSummaryRowMapper implements RowMapper<ExecutionSummary> {
        @Override
        public ExecutionSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            Instant completedAt = rs.getTimestamp("completed_at") != null
                ? rs.getTimestamp("completed_at").toInstant()
                : null;
            
            Long durationMs = rs.getObject("duration_ms", Long.class);
            Integer failedSteps = rs.getObject("failed_steps", Integer.class);
            
            return new ExecutionSummary(
                (UUID) rs.getObject("execution_id"),
                (UUID) rs.getObject("journey_id"),
                ExecutionStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("started_at").toInstant(),
                completedAt,
                durationMs,
                rs.getInt("total_steps"),
                rs.getInt("completed_steps"),
                failedSteps != null ? failedSteps : 0
            );
        }
    }
}
