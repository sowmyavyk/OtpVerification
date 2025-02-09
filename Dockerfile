# Stage 1: Build
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:17-jdk-slim
ENV JAVA_OPTS="-Djava.awt.headless=true"
WORKDIR /app
# Copy the JAR file from the build stage to the runtime container
COPY --from=build /app/target/target/OTP-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]