package com.cloud_ide.executor_service.runtime;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component  // <- makes Spring manage this bean
public class RuntimeRegistry {

    private final Map<String, RuntimeConfig> runtimes = new HashMap<>();

    public RuntimeRegistry() {
        // Hardcode the runtime
        RuntimeConfig javaSpring = new RuntimeConfig();
        javaSpring.setBaseImage("maven:3.9.9-eclipse-temurin-17");
        javaSpring.setRunCommand("java -jar target/helloworld-0.0.1-SNAPSHOT.jar");
        javaSpring.setBuildCommand("mvn clean install");
        javaSpring.setRunImage("eclipse-temurin:17-jdk");
        javaSpring.setWorkDir("/app");
        runtimes.put("java-springboot", javaSpring);
    }

    public RuntimeConfig getRuntime(String runtimeName) {
        return runtimes.get(runtimeName);
    }
}
