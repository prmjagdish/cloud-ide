# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Work at backend root
WORKDIR /app

# Copy entire backend (parent + all modules)
COPY . .

# Build only eureka-service + its dependencies
RUN mvn -f /app/pom.xml clean install -DskipTests

# Runtime image
FROM eclipse-temurin:21
WORKDIR /app
COPY --from=build /app/eureka-service/target/eureka-service-*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
