package com.elevate.sparkle.adapter.in.web.service;

import com.elevate.sparkle.adapter.in.web.dto.ui.*;
import com.elevate.sparkle.domain.valueobject.JourneyDSL;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Translation Service: Bidirectional conversion between UI Model and Engine DSL
 * 
 * UI Model (Graph-based):
 * - Nodes with positions
 * - Edges connecting nodes
 * - Optimized for visual editing
 * 
 * Engine DSL (List-based):
 * - Steps with IDs
 * - Next step references
 * - Conditional transitions
 * - Optimized for execution
 */
@Service
public class JourneyTranslationService {
    
    /**
     * Translate UI Model -> Engine DSL
     * This is called when saving a journey from the visual editor
     */
    public JourneyDSL translateToEngineDSL(JourneyUIModel uiModel) {
        // Find START node
        UINode startNode = uiModel.nodes().stream()
                .filter(n -> n.type() == NodeType.START)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Journey must have a START node"));
        
        // Build adjacency map from edges
        Map<String, List<UIEdge>> outgoingEdges = buildAdjacencyMap(uiModel.edges());
        
        // Convert nodes to steps
        List<JourneyDSL.StepDefinition> steps = new ArrayList<>();
        for (UINode node : uiModel.nodes()) {
            if (node.type() == NodeType.START) {
                continue; // START is not a step, just the entry point
            }
            
            JourneyDSL.StepDefinition step = convertNodeToStep(node, outgoingEdges);
            steps.add(step);
        }
        
        // Find first real step after START
        List<UIEdge> startEdges = outgoingEdges.get(startNode.id());
        String startStepId = (startEdges != null && !startEdges.isEmpty()) 
                ? startEdges.get(0).target() 
                : null;
        
        if (startStepId == null) {
            throw new IllegalArgumentException("START node must have an outgoing edge");
        }
        
        return new JourneyDSL(startStepId, steps, Map.of());
    }
    
    /**
     * Translate Engine DSL -> UI Model
     * This is called when loading a journey into the visual editor
     */
    public JourneyUIModel translateToUIModel(
            UUID journeyId,
            String journeyName,
            String description,
            String version,
            JourneyDSL dsl,
            JourneyUIModel.JourneyMetadata metadata
    ) {
        List<UINode> nodes = new ArrayList<>();
        List<UIEdge> edges = new ArrayList<>();
        
        // Create START node (positioned at top-left)
        UINode startNode = new UINode(
                "start-node",
                NodeType.START,
                new Position(100, 100),
                new UINode.UINodeData("Start", "Journey entry point", null, null, null)
        );
        nodes.add(startNode);
        
        // Edge from START to first step
        edges.add(new UIEdge(
                "edge-start-" + dsl.startStepId(),
                "start-node",
                dsl.startStepId(),
                null,
                null,
                new UIEdge.UIEdgeData(null, null)
        ));
        
        // Convert steps to nodes (auto-layout in vertical flow)
        int yOffset = 250;
        int xOffset = 100;
        Map<String, Position> positions = calculateLayout(dsl, xOffset, yOffset);
        
        for (JourneyDSL.StepDefinition step : dsl.steps()) {
            UINode node = convertStepToNode(step, positions.get(step.id()));
            nodes.add(node);
            
            // Create edges for nextStepId
            if (step.nextStepId() != null) {
                edges.add(new UIEdge(
                        "edge-" + step.id() + "-" + step.nextStepId(),
                        step.id(),
                        step.nextStepId(),
                        null,
                        null,
                        new UIEdge.UIEdgeData("next", null)
                ));
            }
            
            // Create edges for conditional transitions
            if (step.conditionalTransitions() != null) {
                for (int i = 0; i < step.conditionalTransitions().size(); i++) {
                    JourneyDSL.ConditionalTransition transition = step.conditionalTransitions().get(i);
                    edges.add(new UIEdge(
                            "edge-" + step.id() + "-" + transition.targetStepId() + "-" + i,
                            step.id(),
                            transition.targetStepId(),
                            "branch-" + (i + 1),
                            null,
                            new UIEdge.UIEdgeData("branch " + (i + 1), transition.condition())
                    ));
                }
            }
        }
        
        return new JourneyUIModel(
                journeyId,
                journeyName,
                description,
                version,
                nodes,
                edges,
                metadata
        );
    }
    
    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================
    
    private Map<String, List<UIEdge>> buildAdjacencyMap(List<UIEdge> edges) {
        Map<String, List<UIEdge>> map = new HashMap<>();
        for (UIEdge edge : edges) {
            map.computeIfAbsent(edge.source(), k -> new ArrayList<>()).add(edge);
        }
        return map;
    }
    
    private JourneyDSL.StepDefinition convertNodeToStep(
            UINode node, 
            Map<String, List<UIEdge>> outgoingEdges
    ) {
        List<UIEdge> edges = outgoingEdges.getOrDefault(node.id(), List.of());
        
        // Separate conditional edges from default edge
        List<UIEdge> conditionalEdges = edges.stream()
                .filter(e -> e.sourceHandle() != null && e.sourceHandle().startsWith("branch-"))
                .collect(Collectors.toList());
        
        UIEdge defaultEdge = edges.stream()
                .filter(e -> e.sourceHandle() == null || e.sourceHandle().isEmpty())
                .findFirst()
                .orElse(null);
        
        String nextStepId = (defaultEdge != null) ? defaultEdge.target() : null;
        
        // Build conditional transitions
        List<JourneyDSL.ConditionalTransition> conditionalTransitions = conditionalEdges.stream()
                .map(e -> new JourneyDSL.ConditionalTransition(
                        e.data().condition() != null ? e.data().condition() : "true",
                        e.target()
                ))
                .collect(Collectors.toList());
        
        // Convert config
        Map<String, Object> config = convertNodeConfigToMap(node.data().config(), node.type());
        
        // Convert retry policy
        JourneyDSL.RetryPolicy retryPolicy = null;
        if (node.data().retryPolicy() != null) {
            RetryPolicy uiRetry = node.data().retryPolicy();
            retryPolicy = new JourneyDSL.RetryPolicy(
                    uiRetry.maxAttempts(),
                    uiRetry.delayMs(),
                    uiRetry.delayMs() * 10L,
                    2.0
            );
        }
        
        return new JourneyDSL.StepDefinition(
                node.id(),
                node.data().label(),
                node.type().name(),
                config,
                nextStepId,
                conditionalTransitions.isEmpty() ? null : conditionalTransitions,
                retryPolicy,
                null
        );
    }
    
    private UINode convertStepToNode(JourneyDSL.StepDefinition step, Position position) {
        NodeType type = NodeType.valueOf(step.type());
        NodeConfig config = convertMapToNodeConfig(step.config(), type);
        
        RetryPolicy retryPolicy = null;
        if (step.retryPolicy() != null) {
            JourneyDSL.RetryPolicy dslRetry = step.retryPolicy();
            retryPolicy = new RetryPolicy(
                    dslRetry.maxAttempts(),
                    (int) dslRetry.initialDelayMs(),
                    "exponential"
            );
        }
        
        UINode.UINodeData data = new UINode.UINodeData(
                step.name(),
                null,
                config,
                retryPolicy,
                null
        );
        
        return new UINode(step.id(), type, position, data);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertNodeConfigToMap(NodeConfig config, NodeType type) {
        if (config == null) return Map.of();
        
        Map<String, Object> map = new HashMap<>();
        
        switch (type) {
            case HTTP -> {
                HttpNodeConfig http = (HttpNodeConfig) config;
                map.put("url", http.url());
                map.put("method", http.method());
                if (http.headers() != null) map.put("headers", http.headers());
                if (http.body() != null) map.put("body", http.body());
                if (http.timeoutMs() != null) map.put("timeoutMs", http.timeoutMs());
            }
            case MESSAGE -> {
                MessageNodeConfig msg = (MessageNodeConfig) config;
                map.put("queue", msg.queue());
                map.put("message", msg.message());
            }
            case WAIT -> {
                WaitNodeConfig wait = (WaitNodeConfig) config;
                map.put("duration", wait.duration());
                map.put("unit", wait.unit());
            }
            case TRANSFORM -> {
                TransformNodeConfig transform = (TransformNodeConfig) config;
                map.put("script", transform.script());
                map.put("language", transform.language());
            }
            case DECISION -> {
                DecisionNodeConfig decision = (DecisionNodeConfig) config;
                if (decision.branches() != null) {
                    List<Map<String, String>> branches = decision.branches().stream()
                            .map(b -> Map.of("id", b.id(), "label", b.label(), "condition", b.condition()))
                            .collect(Collectors.toList());
                    map.put("branches", branches);
                }
                if (decision.defaultBranch() != null) map.put("defaultBranch", decision.defaultBranch());
            }
            case PARALLEL -> {
                ParallelNodeConfig parallel = (ParallelNodeConfig) config;
                if (parallel.branchIds() != null) map.put("branchIds", parallel.branchIds());
                if (parallel.maxConcurrency() != null) map.put("maxConcurrency", parallel.maxConcurrency());
            }
            case AGGREGATE -> {
                AggregateNodeConfig aggregate = (AggregateNodeConfig) config;
                map.put("strategy", aggregate.strategy());
            }
        }
        
        return map;
    }
    
    @SuppressWarnings("unchecked")
    private NodeConfig convertMapToNodeConfig(Map<String, Object> config, NodeType type) {
        if (config == null || config.isEmpty()) return null;
        
        return switch (type) {
            case HTTP -> new HttpNodeConfig(
                    (String) config.get("url"),
                    (String) config.get("method"),
                    (Map<String, String>) config.get("headers"),
                    (String) config.get("body"),
                    (Integer) config.get("timeoutMs"),
                    null
            );
            case MESSAGE -> new MessageNodeConfig(
                    (String) config.get("queue"),
                    (String) config.get("message")
            );
            case WAIT -> new WaitNodeConfig(
                    (Integer) config.getOrDefault("duration", 5),
                    (String) config.getOrDefault("unit", "s")
            );
            case TRANSFORM -> new TransformNodeConfig(
                    (String) config.get("script"),
                    (String) config.getOrDefault("language", "javascript")
            );
            case DECISION -> {
                List<Map<String, String>> branchesList = (List<Map<String, String>>) config.get("branches");
                List<DecisionNodeConfig.DecisionBranch> branches = null;
                if (branchesList != null) {
                    branches = branchesList.stream()
                            .map(b -> new DecisionNodeConfig.DecisionBranch(
                                    b.get("id"),
                                    b.get("label"),
                                    b.get("condition")
                            ))
                            .collect(Collectors.toList());
                }
                yield new DecisionNodeConfig(branches, (String) config.get("defaultBranch"));
            }
            case PARALLEL -> new ParallelNodeConfig(
                    (List<String>) config.get("branchIds"),
                    (Integer) config.get("maxConcurrency")
            );
            case AGGREGATE -> new AggregateNodeConfig(
                    (String) config.getOrDefault("strategy", "merge")
            );
            default -> null;
        };
    }
    
    /**
     * Simple auto-layout algorithm (vertical flow)
     */
    private Map<String, Position> calculateLayout(JourneyDSL dsl, int startX, int startY) {
        Map<String, Position> positions = new HashMap<>();
        int currentY = startY;
        int xSpacing = 300;
        int ySpacing = 150;
        
        // Simple vertical layout for now
        // TODO: Implement proper graph layout algorithm (Dagre, etc.)
        for (int i = 0; i < dsl.steps().size(); i++) {
            JourneyDSL.StepDefinition step = dsl.steps().get(i);
            positions.put(step.id(), new Position(startX, currentY));
            currentY += ySpacing;
        }
        
        return positions;
    }
}
