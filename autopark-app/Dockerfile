# ===== Stage 1: build =====
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Сначала зависимости (кеш быстрее)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Затем исходники
COPY src ./src
RUN mvn -q -DskipTests package

# ===== Stage 2: runtime =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /opt/app

# Копируем собранный jar (имя не хардкодим)
COPY --from=builder /build/target/*.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /opt/app/app.jar"]
