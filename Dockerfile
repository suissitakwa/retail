# Stage 1: Builder - Compile and Package the JAR
FROM maven:3.8.7-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime - Create a lean final image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port and define startup command
EXPOSE 8080
ENTRYPOINT ["java", "-Xms256m", "-Xmx384m", "-XX:MaxMetaspaceSize=128m", "-XX:ReservedCodeCacheSize=48m", "-XX:MaxDirectMemorySize=48m", "-jar", "app.jar"]
