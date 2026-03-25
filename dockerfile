# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Install OpenCV dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    libopencv-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Install OpenCV runtime
RUN apt-get update && apt-get install -y \
    libopencv-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy the built jar
COPY --from=build /app/target/sketchimage-1.0.0.jar app.jar

# Create directories for uploads
RUN mkdir -p /tmp/uploads /tmp/outputs

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-Djava.library.path=/usr/lib", "-jar", "app.jar"]