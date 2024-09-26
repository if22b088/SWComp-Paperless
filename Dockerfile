FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the packaged jar file into the container
COPY target/your-app-name.jar app.jar

EXPOSE 8081

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]

