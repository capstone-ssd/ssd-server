# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY ssd-api/build.gradle ./ssd-api/build.gradle
COPY ssd-domain/build.gradle ./ssd-domain/build.gradle
COPY ssd-core/build.gradle ./ssd-core/build.gradle
COPY ssd-infra/build.gradle ./ssd-infra/build.gradle

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon :ssd-api:dependencies >/dev/null 2>&1 || true

COPY ssd-api/src ./ssd-api/src
COPY ssd-domain/src ./ssd-domain/src
COPY ssd-core/src ./ssd-core/src
COPY ssd-infra/src ./ssd-infra/src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon :ssd-api:bootJar -x test && \
    cp /workspace/ssd-api/build/libs/*.jar /workspace/app.jar

FROM eclipse-temurin:21-jdk-alpine AS extractor

WORKDIR /layers

COPY --from=builder /workspace/app.jar app.jar

RUN mkdir extracted && cd extracted && jar -xf ../app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=extractor /layers/extracted/org ./org
COPY --from=extractor /layers/extracted/META-INF ./META-INF
COPY --from=extractor /layers/extracted/BOOT-INF/lib ./BOOT-INF/lib
COPY --from=extractor /layers/extracted/BOOT-INF/classes ./BOOT-INF/classes

EXPOSE 8080

CMD ["java", "-Dspring.profiles.active=dev", "-Duser.timezone=Asia/Seoul", "org.springframework.boot.loader.launch.JarLauncher"]
