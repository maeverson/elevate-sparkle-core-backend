package com.elevate.sparkle.adapter.in.web.dto.ui;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base interface for node-specific configuration
 * Uses Jackson polymorphic deserialization
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = HttpNodeConfig.class, name = "HTTP"),
    @JsonSubTypes.Type(value = DecisionNodeConfig.class, name = "DECISION"),
    @JsonSubTypes.Type(value = MessageNodeConfig.class, name = "MESSAGE"),
    @JsonSubTypes.Type(value = WaitNodeConfig.class, name = "WAIT"),
    @JsonSubTypes.Type(value = TransformNodeConfig.class, name = "TRANSFORM"),
    @JsonSubTypes.Type(value = ParallelNodeConfig.class, name = "PARALLEL"),
    @JsonSubTypes.Type(value = AggregateNodeConfig.class, name = "AGGREGATE")
})
public interface NodeConfig {
}
