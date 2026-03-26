# Use Maven with OpenJDK
FROM maven:3.8.4-openjdk-17-slim

# Install OpenCV
RUN apt-get update && apt-get install -y \
    libopencv-dev \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy pom.xml first
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build with debug info
RUN mvn clean package -DskipTests -X

# Check if jar was created
RUN ls -la target/

# Create directories
RUN mkdir -p /tmp/uploads /tmp/outputs

# Expose port
EXPOSE 8080

# Run with debug logging
CMD ["sh", "-c", "java -Djava.library.path=/usr/lib -jar target/sketchimage-1.0.0.jar"]
