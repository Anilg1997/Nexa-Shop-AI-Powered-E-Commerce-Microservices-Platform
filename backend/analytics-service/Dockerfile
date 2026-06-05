FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/analytics-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
