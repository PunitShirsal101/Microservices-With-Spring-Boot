# Backend Dockerfile - Simplified version using pre-built JAR
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the pre-built JAR file from local build
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]