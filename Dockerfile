FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/urlshortener-0.0.1-SNAPSHOT.jar /app/urlshortener.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/urlshortener.jar"]
