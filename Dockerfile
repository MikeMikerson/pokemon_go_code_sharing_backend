# Use OpenJDK 21 as the base image
FROM amazoncorretto:21-alpine-jdk AS build

# Set working directory
WORKDIR /app


# Copy Gradle wrapper and build files first (to leverage cache for dependencies)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Only resolve dependencies (this step is cached unless build files change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code last (this invalidates cache only for code changes)
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Create the runtime image
FROM amazoncorretto:21-alpine-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
