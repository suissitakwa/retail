# Stage 1: Builder - Compile and Package the JAR
FROM maven:3.8.7-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy dependencies first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -o

# Stage 2: Runtime - Create a lean final image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port and define startup command
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]