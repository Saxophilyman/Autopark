FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

# Кеш зависимостей: POM'ы ВСЕХ модулей из <modules>
COPY pom.xml .
COPY autopark-events/pom.xml   autopark-events/pom.xml
COPY autopark-app/pom.xml      autopark-app/pom.xml
COPY autopark-loadtest/pom.xml autopark-loadtest/pom.xml
COPY notify-service/pom.xml    notify-service/pom.xml
RUN mvn -B -U -DskipTests dependency:go-offline

# Теперь исходники и сборка
COPY . .
RUN mvn -B -U -DskipTests -pl autopark-app -am clean package \
 && cp autopark-app/target/autopark-app-*.jar /tmp/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /opt/app
COPY --from=build /tmp/app.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
