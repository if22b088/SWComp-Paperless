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

# Install Tesseract and required dependencies
RUN apt-get update && apt-get install -y tesseract-ocr tesseract-ocr-eng ghostscript
#RUN apt-get install -y tesseract-ocr tesseract-ocr-eng

# Copy the packaged jar file from the builder stage into the final image
COPY --from=buildstage /app/target/*.jar app.jar

# Copy the tessdata (language data files) into the container
#COPY --from=buildstage /app/tessdata /usr/share/tesseract-ocr/4.00/tessdata/
#COPY --from=buildstage /app/tessdata /usr/local/share/tesseract-ocr/4.00/tessdata/

# Expose the necessary port (e.g., 8082)
EXPOSE 8082

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
