package com.cloud_ide.executor_service.runtime;

import lombok.Data;

@Data
public class RuntimeConfig {
    private String baseImage;
    private String runCommand;
    private String buildCommand;
    private String workDir;
    private String runImage;


}

