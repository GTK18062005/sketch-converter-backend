# Build stage
FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /app

# Install OpenCV from apt (faster but may have older version)
RUN apt-get update && apt-get install -y \
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
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install OpenCV runtime
RUN apt-get update && apt-get install -y \
    libopencv-core-dev \
    libopencv-imgproc-dev \
    libopencv-imgcodecs-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy OpenCV Java library
RUN find /usr -name "libopencv_java*.so" -exec cp {} /usr/lib/ \; 2>/dev/null || true

# Copy the built jar
COPY --from=build /app/target/sketchimage-1.0.0.jar app.jar

# Create directories
RUN mkdir -p /tmp/uploads /tmp/outputs

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-Djava.library.path=/usr/lib:/usr/lib/jni", "-jar", "app.jar"]
