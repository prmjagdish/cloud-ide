package com.cloud_ide.executor_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService jobExecutorService() {
        // pool of 4 workers, adjust per VM resources
        return Executors.newFixedThreadPool(2);
    }
}
