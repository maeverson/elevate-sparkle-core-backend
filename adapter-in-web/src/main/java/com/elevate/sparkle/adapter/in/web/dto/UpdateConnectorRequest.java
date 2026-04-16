package com.elevate.sparkle.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a connector
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConnectorRequest {

    private String name;
    private String type;
    private String config;
    private String status;
}
