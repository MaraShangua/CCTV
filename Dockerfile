# 1단계: Maven을 포함한 이미지로 빌드
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY src/main/java/com/example/cctv .
RUN mvn clean package -DskipTests

# 2단계: 경량화된 OpenJDK 이미지로 실행
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
