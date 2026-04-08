package com.elevate.sparkle.adapter.out.messaging.adapter;

import com.elevate.sparkle.adapter.out.messaging.event.OrderCancelledEvent;
import com.elevate.sparkle.adapter.out.messaging.event.OrderCreatedEvent;
import com.elevate.sparkle.adapter.out.messaging.event.OrderStatusChangedEvent;
import com.elevate.sparkle.adapter.out.messaging.mapper.OrderEventMapper;
import com.elevate.sparkle.application.port.out.OrderEventPublisherPort;
import com.elevate.sparkle.domain.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing OrderEventPublisherPort
 * Publishes events to AWS SQS with resilience patterns
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqsOrderEventPublisher implements OrderEventPublisherPort {

    private final SqsTemplate sqsTemplate;
    private final OrderEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.order-events-queue:order-events}")
    private String orderEventsQueue;

    @Override
    @Retry(name = "sqsPublish")
    @CircuitBreaker(name = "sqsPublish", fallbackMethod = "publishOrderCreatedFallback")
    public void publishOrderCreated(Order order) {
        log.info("Publishing order created event for order: {}", order.getId());
        
        try {
            OrderCreatedEvent event = eventMapper.toOrderCreatedEvent(order);
            String payload = objectMapper.writeValueAsString(event);
            
            sqsTemplate.send(to -> to
                    .queue(orderEventsQueue)
                    .payload(payload)
                    .header("eventType", "ORDER_CREATED")
                    .header("orderId", order.getId().getValue())
            );
            
            log.info("Successfully published order created event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish order created event", e);
            throw new RuntimeException("Failed to publish order created event", e);
        }
    }

    @Override
    @Retry(name = "sqsPublish")
    @CircuitBreaker(name = "sqsPublish", fallbackMethod = "publishOrderStatusChangedFallback")
    public void publishOrderStatusChanged(Order order) {
        log.info("Publishing order status changed event for order: {}", order.getId());
        
        try {
            // In a real scenario, you'd want to track the old status
            // For simplicity, passing null here
            OrderStatusChangedEvent event = eventMapper.toOrderStatusChangedEvent(order, null);
            String payload = objectMapper.writeValueAsString(event);
            
            sqsTemplate.send(to -> to
                    .queue(orderEventsQueue)
                    .payload(payload)
                    .header("eventType", "ORDER_STATUS_CHANGED")
                    .header("orderId", order.getId().getValue())
            );
            
            log.info("Successfully published order status changed event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish order status changed event", e);
            throw new RuntimeException("Failed to publish order status changed event", e);
        }
    }

    @Override
    @Retry(name = "sqsPublish")
    @CircuitBreaker(name = "sqsPublish", fallbackMethod = "publishOrderCancelledFallback")
    public void publishOrderCancelled(Order order) {
        log.info("Publishing order cancelled event for order: {}", order.getId());
        
        try {
            OrderCancelledEvent event = eventMapper.toOrderCancelledEvent(order);
            String payload = objectMapper.writeValueAsString(event);
            
            sqsTemplate.send(to -> to
                    .queue(orderEventsQueue)
                    .payload(payload)
                    .header("eventType", "ORDER_CANCELLED")
                    .header("orderId", order.getId().getValue())
            );
            
            log.info("Successfully published order cancelled event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish order cancelled event", e);
            throw new RuntimeException("Failed to publish order cancelled event", e);
        }
    }

    // Fallback methods for circuit breaker
    private void publishOrderCreatedFallback(Order order, Exception e) {
        log.error("Circuit breaker triggered for order created event: {}", order.getId(), e);
        // In production, you might want to:
        // 1. Store in outbox table for later retry
        // 2. Send alert to monitoring system
        // 3. Return gracefully without blocking the main flow
    }

    private void publishOrderStatusChangedFallback(Order order, Exception e) {
        log.error("Circuit breaker triggered for order status changed event: {}", order.getId(), e);
    }

    private void publishOrderCancelledFallback(Order order, Exception e) {
        log.error("Circuit breaker triggered for order cancelled event: {}", order.getId(), e);
    }
}
