FROM eclipse-temurin:17-jre-alpine

COPY build/libs/ssd-0.0.1-SNAPSHOT.jar /app.jar

CMD ["java", "-Dspring.profiles.active=dev", "-jar", "/app.jar"]