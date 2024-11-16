# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine as builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /app/target/*.jar project-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "project-0.0.1-SNAPSHOT.jar"]
