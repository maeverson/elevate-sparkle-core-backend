# Stage 1: Build
FROM docker.io/library/maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom files for dependency resolution
COPY pom.xml .
COPY core-domain/pom.xml core-domain/
COPY core-application/pom.xml core-application/
COPY adapter-in-web/pom.xml adapter-in-web/
COPY adapter-out-persistence/pom.xml adapter-out-persistence/
COPY adapter-out-messaging/pom.xml adapter-out-messaging/
COPY infrastructure/pom.xml infrastructure/
COPY bootstrap/pom.xml bootstrap/

# Download dependencies (cached if pom files don't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY core-domain/src core-domain/src
COPY core-application/src core-application/src
COPY adapter-in-web/src adapter-in-web/src
COPY adapter-out-persistence/src adapter-out-persistence/src
COPY adapter-out-messaging/src adapter-out-messaging/src
COPY infrastructure/src infrastructure/src
COPY bootstrap/src bootstrap/src

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM docker.io/library/eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S sparkle && adduser -S sparkle -G sparkle

# Copy JAR from builder
COPY --from=builder /app/bootstrap/target/*.jar app.jar

# Set ownership
RUN chown -R sparkle:sparkle /app

# Switch to non-root user
USER sparkle

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
