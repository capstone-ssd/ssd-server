# syntax=docker/dockerfile:1.7

# 런타임 이미지를 가볍게 유지하기 위해 별도 stage에서 boot jar를 빌드합니다.
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Gradle 설정 파일을 먼저 복사해 의존성 캐시 재사용률을 높입니다.
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY ssd-api/build.gradle ./ssd-api/build.gradle
COPY ssd-domain/build.gradle ./ssd-domain/build.gradle
COPY ssd-common/build.gradle ./ssd-common/build.gradle
COPY ssd-auth/build.gradle ./ssd-auth/build.gradle
COPY ssd-external/build.gradle ./ssd-external/build.gradle
COPY ssd-infra/build.gradle ./ssd-infra/build.gradle

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon :ssd-api:dependencies >/dev/null 2>&1 || true

# 소스 변경이 의존성 레이어까지 깨지지 않도록 소스 복사는 뒤에서 수행합니다.
COPY ssd-api/src ./ssd-api/src
COPY ssd-domain/src ./ssd-domain/src
COPY ssd-common/src ./ssd-common/src
COPY ssd-auth/src ./ssd-auth/src
COPY ssd-external/src ./ssd-external/src
COPY ssd-infra/src ./ssd-infra/src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon :ssd-api:bootJar -x test && \
    cp /workspace/ssd-api/build/libs/*.jar /workspace/app.jar

# boot jar를 풀어서 라이브러리 레이어와 클래스 레이어를 분리합니다.
FROM eclipse-temurin:21-jdk-alpine AS extractor

WORKDIR /layers

COPY --from=builder /workspace/app.jar app.jar

RUN mkdir extracted && cd extracted && jar -xf ../app.jar

# 최종 런타임 이미지는 boot loader, 라이브러리, 컴파일된 클래스만 포함합니다.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=extractor /layers/extracted/org ./org
COPY --from=extractor /layers/extracted/META-INF ./META-INF
COPY --from=extractor /layers/extracted/BOOT-INF/lib ./BOOT-INF/lib
COPY --from=extractor /layers/extracted/BOOT-INF/classes ./BOOT-INF/classes

EXPOSE 8080

CMD ["java", "-Dspring.profiles.active=dev", "-Duser.timezone=Asia/Seoul", "org.springframework.boot.loader.launch.JarLauncher"]
