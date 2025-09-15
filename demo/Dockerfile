FROM --platform=linux/amd64 eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test


# ==================================
# 2. 실행(Runtime) 단계: 서버 환경(linux/amd64)용 JRE 이미지 사용
# ==================================
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]