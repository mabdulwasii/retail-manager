# Multi-stage build for Shop Manager Backend
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Copy source code (docs will be copied from CI/CD artifact)
COPY src ./src

# Build the application (skip tests - they run in CI/CD pipeline)
# Documentation is pre-generated in CI/CD and placed in src/main/resources/static/docs/
RUN mvn clean package -DskipTests -B && \
    ls -lh target/*.jar && \
    echo "Checking if docs were included:" && \
    ls -lh target/classes/static/docs/ 2>/dev/null || echo "No docs found (will be added by CI/CD)"

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install additional packages for monitoring and debugging
RUN apk add --no-cache curl dumb-init

# Create non-root user for security
RUN addgroup -g 1001 -S shopmanager && \
    adduser -u 1001 -S shopmanager -G shopmanager

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/shop-manager-1.0.0-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown -R shopmanager:shopmanager /app

# Switch to non-root user
USER shopmanager

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Use dumb-init to properly handle signals
ENTRYPOINT ["dumb-init", "--"]

# Run the application with optimized JVM settings
CMD ["java", \
     "-server", \
     "-XX:+UseG1GC", \
     "-XX:MaxGCPauseMillis=200", \
     "-XX:ParallelGCThreads=20", \
     "-XX:ConcGCThreads=5", \
     "-XX:InitiatingHeapOccupancyPercent=70", \
     "-XX:+UseStringDeduplication", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", \
     "-jar", "app.jar"]