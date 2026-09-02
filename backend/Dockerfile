# Standard lightweight pick for Java 21
FROM eclipse-temurin:21-jre-alpine

LABEL authors="Max"
WORKDIR /app
COPY target/argent-0.1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]