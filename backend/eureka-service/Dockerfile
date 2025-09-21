# Stage 1: Build jar
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
<<<<<<< HEAD
COPY pom.xml .
COPY src ./src
=======
>>>>>>> 334d97b (fix(structure): parent pom move in backend fld)

# Copy entire cloud-ide project (so parent POM is included)
COPY ../../pom.xml ./  # parent POM relative path
COPY ./ ./             # eureka-service source

# Build module and dependencies
RUN mvn clean package -pl backend/eureka-service -am -DskipTests

# Stage 2: Run jar
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/backend/eureka-service/target/*.jar app.jar

EXPOSE 8761
CMD ["java","-jar","app.jar"]

