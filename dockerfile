# Use a base image with OpenCV pre-installed
FROM ubuntu:22.04

# Install Java and OpenCV
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    maven \
    libopencv-dev \
    libopencv-java \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy OpenCV Java library to known location
RUN find /usr -name "libopencv_java*.so" -exec cp {} /usr/lib/ \; 2>/dev/null || true

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create directories
RUN mkdir -p /tmp/uploads /tmp/outputs

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-Djava.library.path=/usr/lib:/usr/lib/jni", "-jar", "target/sketchimage-1.0.0.jar"]
