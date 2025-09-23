# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent POM and eureka-service module
COPY ../pom.xml ./
COPY ../eureka-service ./eureka-service

# Build only eureka-service module with its dependencies
RUN mvn -pl eureka-service -am clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/eureka-service/target/eureka-service-*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java","-jar","app.jar"]
