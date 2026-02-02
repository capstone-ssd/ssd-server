FROM eclipse-temurin:21-jre-alpine

COPY ssd-api/build/libs/ssd-api-0.0.1-SNAPSHOT.jar /app.jar

CMD ["java", "-Dspring.profiles.active=dev", "-jar", "/app.jar"]
