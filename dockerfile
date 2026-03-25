# Build stage
FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /app

# Install OpenCV and dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    cmake \
    build-essential \
    libgtk2.0-dev \
    pkg-config \
    libavcodec-dev \
    libavformat-dev \
    libswscale-dev \
    libtbb2 \
    libtbb-dev \
    libjpeg-dev \
    libpng-dev \
    libtiff-dev \
    libdc1394-22-dev \
    && rm -rf /var/lib/apt/lists/*

# Download and build OpenCV (more reliable)
RUN wget -q https://github.com/opencv/opencv/archive/4.12.0.tar.gz && \
    tar -xzf 4.12.0.tar.gz && \
    cd opencv-4.12.0 && \
    mkdir build && cd build && \
    cmake -D CMAKE_BUILD_TYPE=RELEASE \
          -D CMAKE_INSTALL_PREFIX=/usr/local \
          -D WITH_TBB=ON \
          -D WITH_V4L=ON \
          -D WITH_QT=OFF \
          -D WITH_OPENGL=ON \
          -D BUILD_SHARED_LIBS=ON \
          -D BUILD_opencv_java=ON \
          .. && \
    make -j$(nproc) && \
    make install && \
    ldconfig

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

# Install OpenCV runtime dependencies
RUN apt-get update && apt-get install -y \
    libgtk2.0-0 \
    libtbb2 \
    libjpeg62-turbo \
    libpng16-16 \
    libtiff5 \
    libdc1394-25 \
    && rm -rf /var/lib/apt/lists/*

# Copy OpenCV libraries from build stage
COPY --from=build /usr/local/lib/libopencv_* /usr/lib/
COPY --from=build /usr/local/share/java/opencv4/libopencv_java4120.so /usr/lib/

# Copy the built jar
COPY --from=build /app/target/sketchimage-1.0.0.jar app.jar

# Create directories for uploads
RUN mkdir -p /tmp/uploads /tmp/outputs

# Expose port
EXPOSE 8080

# Run the application with OpenCV library path
CMD ["java", "-Djava.library.path=/usr/lib", "-jar", "app.jar"]
