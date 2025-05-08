# Используем базовый образ с Java 17
FROM eclipse-temurin:17-jdk

# Указываем рабочую директорию в контейнере
WORKDIR /app

# Копируем JAR-файл внутрь контейнера
COPY target/Autopark-0.0.1-SNAPSHOT.jar app.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
