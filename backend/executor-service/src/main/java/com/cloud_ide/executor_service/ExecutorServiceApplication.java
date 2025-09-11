package com.cloud_ide.executor_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExecutorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExecutorServiceApplication.class, args);
	}

}
