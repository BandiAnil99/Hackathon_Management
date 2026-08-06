FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /workspace

# 1. Copy wrapper and pom.xml first
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml ./

# Make wrapper executable
RUN chmod +x mvnw

# 2. Download dependencies (this layer gets cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# 3. Copy source code
COPY src ./src

# 4. Package the application
RUN ./mvnw -B -DskipTests package

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /workspace/target/ai-recruitment-event-platform-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]