# Stage 1: Build the application using a Gradle image
FROM gradle:8.8.0-jdk21 AS build

# Set the working directory inside the container
WORKDIR /home/gradle/src

# Copy build files and download dependencies
COPY build.gradle .
COPY settings.gradle .
RUN gradle build --no-daemon --stacktrace -x test

# Copy the rest of the source code
COPY src ./src

# Build the application, creating the executable JAR
RUN gradle bootJar --no-daemon --stacktrace

# Stage 2: Create the final, smaller runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the executable JAR from the build stage
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]