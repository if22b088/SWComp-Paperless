# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17-slim AS buildstage

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and any other necessary files
COPY pom.xml ./
COPY src ./src

# Run Maven to build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:17-slim

# Set the working directory in the container
WORKDIR /app

# Copy the packaged jar file from the builder stage into the final image
COPY --from=buildstage /app/target/*.jar app.jar

# Make port available outside container
EXPOSE 8081

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]


# OLD VERSION NEED TO RUN MVN CLEAN PACKAGE manually before running docker compose up
#FROM openjdk:17-slim

# Set the working directory in the container
#WORKDIR /app

# Copy the packaged jar file into the container
#COPY target/*.jar app.jar

# Make port available outside container
#EXPOSE 8081

# Run the jar file
#ENTRYPOINT ["java", "-jar", "app.jar"]