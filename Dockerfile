# ===================================================================
# Multi-Stage Dockerfile for Client Portal & Project Management
# ===================================================================

# Stage 1: Build Stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy Maven wrapper / POM first for dependency caching
COPY pom.xml .
RUN apk add --no-cache maven && mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for enterprise container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy executable fat JAR from build stage
COPY --from=builder /build/target/client-portal-1.0.0.jar app.jar

# Configuration defaults
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=default
EXPOSE 8080

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
