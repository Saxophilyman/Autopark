# ===== build =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

# 1) Кеш зависимостей: копируем только POM'ы
COPY pom.xml .
COPY autopark-events/pom.xml autopark-events/pom.xml
COPY autopark-app/pom.xml     autopark-app/pom.xml
RUN mvn -B -U -DskipTests dependency:go-offline

# 2) Теперь исходники — и сборка ТОЛЬКО нужного модуля + его проектных зависимостей
COPY . .
RUN mvn -B -U -DskipTests -pl autopark-app -am clean package

# 3) Забираем fat-jar приложения
RUN cp /build/autopark-app/target/autopark-app-*.jar /tmp/app.jar

# ===== run =====
FROM eclipse-temurin:17-jre
WORKDIR /opt/app
COPY --from=build /tmp/app.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
