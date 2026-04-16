package com.elevate.sparkle.config;

import com.elevate.sparkle.adapter.out.persistence.adapter.OrderPersistenceAdapter;
import com.elevate.sparkle.adapter.out.persistence.adapter.UserPersistenceAdapter;
import com.elevate.sparkle.adapter.out.messaging.adapter.SqsOrderEventPublisher;
import com.elevate.sparkle.application.port.out.*;
import com.elevate.sparkle.application.usecase.*;
import com.elevate.sparkle.application.service.*;
import com.elevate.sparkle.application.engine.WorkflowEngine;
import com.elevate.sparkle.application.scheduler.StepOrchestrator;
import com.elevate.sparkle.domain.service.JourneyDSLValidator;
import com.elevate.sparkle.domain.port.out.StepExecutor;
import com.elevate.sparkle.infrastructure.security.JwtTokenProvider;
import com.elevate.sparkle.infrastructure.security.PasswordEncoderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application Configuration
 * Wires use cases with their dependencies (ports)
 * This is where hexagonal architecture gets wired together
 */
@Configuration
public class ApplicationConfig {

    // Order Use Cases
    
    @Bean
    public CreateOrderService createOrderService(
            OrderRepositoryPort orderRepository,
            OrderEventPublisherPort eventPublisher) {
        return new CreateOrderService(orderRepository, eventPublisher);
    }

    @Bean
    public GetOrderService getOrderService(OrderRepositoryPort orderRepository) {
        return new GetOrderService(orderRepository);
    }

    @Bean
    public ListOrdersService listOrdersService(OrderRepositoryPort orderRepository) {
        return new ListOrdersService(orderRepository);
    }

    @Bean
    public UpdateOrderStatusService updateOrderStatusService(
            OrderRepositoryPort orderRepository,
            OrderEventPublisherPort eventPublisher) {
        return new UpdateOrderStatusService(orderRepository, eventPublisher);
    }

    // User Use Cases

    @Bean
    public RegisterUserService registerUserService(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder) {
        return new RegisterUserService(userRepository, passwordEncoder);
    }

    @Bean
    public AuthenticateUserService authenticateUserService(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider) {
        return new AuthenticateUserService(userRepository, passwordEncoder, tokenProvider);
    }

    @Bean
    public AdminUserService adminUserService(
            UserRepositoryPort userRepository,
            PasswordEncoderPort passwordEncoder) {
        return new AdminUserService(userRepository, passwordEncoder);
    }

    // Connector Use Cases

    @Bean
    public ConnectorService connectorService(
            ConnectorRepositoryPort connectorRepository) {
        return new ConnectorService(connectorRepository);
    }
    
    // Journey Use Cases
    
    @Bean
    public CreateJourneyService createJourneyService(
            JourneyDefinitionRepositoryPort journeyRepository) {
        return new CreateJourneyService(journeyRepository);
    }
    
    @Bean
    public CreateJourneyVersionService createJourneyVersionService(
            JourneyVersionRepositoryPort versionRepository,
            JourneyDefinitionRepositoryPort definitionRepository,
            JourneyDSLValidator dslValidator) {
        return new CreateJourneyVersionService(versionRepository, definitionRepository, dslValidator);
    }
    
    @Bean
    public PublishJourneyVersionService publishJourneyVersionService(
            JourneyVersionRepositoryPort versionRepository,
            JourneyDefinitionRepositoryPort definitionRepository) {
        return new PublishJourneyVersionService(versionRepository, definitionRepository);
    }
    
    @Bean
    public GetJourneyService getJourneyService(
            JourneyDefinitionRepositoryPort repository) {
        return new GetJourneyService(repository);
    }
    
    @Bean
    public GetJourneyVersionService getJourneyVersionService(
            JourneyVersionRepositoryPort repository) {
        return new GetJourneyVersionService(repository);
    }
    
    @Bean
    public ListExecutionsService listExecutionsService(
            ExecutionQueryPort executionQueryPort,
            WorkflowEngine workflowEngine) {
        return new ListExecutionsService(executionQueryPort, workflowEngine);
    }
    
    @Bean
    public RetryExecutionService retryExecutionService(
            WorkflowEngine workflowEngine) {
        return new RetryExecutionService(workflowEngine);
    }
    
    @Bean
    public DashboardMetricsService dashboardMetricsService(
            ExecutionQueryPort executionQueryPort,
            WorkflowEngine workflowEngine,
            JourneyDefinitionRepositoryPort journeyRepository) {
        return new DashboardMetricsService(executionQueryPort, workflowEngine, journeyRepository);
    }
    
    // Domain Services
    
    @Bean
    public JourneyDSLValidator journeyDSLValidator() {
        return new JourneyDSLValidator();
    }
    
    @Bean
    public StepOrchestrator stepOrchestrator(
            WorkflowEngine workflowEngine,
            StepExecutor stepExecutor,
            StepDispatcherPort stepDispatcher) {
        return new StepOrchestrator(workflowEngine, stepExecutor, stepDispatcher);
    }
}
