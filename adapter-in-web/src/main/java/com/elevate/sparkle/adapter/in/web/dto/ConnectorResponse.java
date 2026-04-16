package com.elevate.sparkle.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for connector response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorResponse {

    private String id;
    private String name;
    private String type;
    private String config;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
