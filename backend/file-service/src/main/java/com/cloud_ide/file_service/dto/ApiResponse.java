package com.cloud_ide.file_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ApiResponse {
    private String status;
    private String message;
    private Instant timestamp;
}
