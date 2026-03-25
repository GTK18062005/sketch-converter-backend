# Use Eclipse Temurin JDK 17 as base
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Install OpenCV dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    libopencv-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install OpenCV runtime
RUN apt-get update && apt-get install -y \
    libopencv-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy the built jar
COPY --from=build /app/target/sketchimage-1.0.0.jar app.jar

# Create directories
RUN mkdir -p /tmp/uploads /tmp/outputs

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-Djava.library.path=/usr/lib", "-jar", "app.jar"]
