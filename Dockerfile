# Stage 1: Build
FROM openjdk:17-jdk-slim as build
WORKDIR /workspace/app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven

COPY pom.xml .
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]