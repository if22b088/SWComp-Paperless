# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17-slim AS buildstage

#set working directory
WORKDIR /app

# Copy the pom.xml and any other necessary files
COPY pom.xml ./
COPY src ./src

# Run Maven to build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:17-slim

# set working direcotry
WORKDIR /app

#install tesseract and dependencies
RUN apt-get update && apt-get install -y tesseract-ocr tesseract-ocr-eng ghostscript

# copy jar from build stage to final image
COPY --from=buildstage /app/target/*.jar app.jar

# download tessdata
RUN mkdir -p /usr/share/tesseract-ocr/4.00/tessdata/ && \
    apt-get install -y wget && \
    wget -P /usr/share/tesseract-ocr/4.00/tessdata/ https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata

EXPOSE 8082

#run jar
ENTRYPOINT ["java", "-jar", "app.jar"]