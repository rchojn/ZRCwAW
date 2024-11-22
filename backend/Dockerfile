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

# List target directory contents for debugging
RUN ls -la target/

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Add shell for debugging and maintenance
RUN apk add --no-cache bash

EXPOSE 8080

# Use shell form of ENTRYPOINT for better variable expansion and shell features
ENTRYPOINT exec java -jar app.jar
