# ===== Этап 1: Сборка проекта с помощью Maven =====
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf
RUN mvn clean package -DskipTests

# ===== Этап 2: Запуск финального JAR =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/Autopark-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
