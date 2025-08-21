#도커라이징(Dockerizing), 컨테이너화(Containerizing), 도커 이미지 빌드(Docker Image Build)
#Build stage
# ✅ 올바른 이미지 이름으로 수정
FROM bellsoft/liberica-openjdk-alpine:17 AS builder


#컨테이너 안에서 앞으로의 모든 명령어가 실행될 기본 폴더
WORKDIR /app


# <폴더,파일> -> 컨테이너 안으로 복사
COPY . .


#   RUN : 컨테이너 안에서 특정 명령어 실행
#   ./gradlew : Gradle Wrapper 실행
#   clean : 이전에 빌드했던 결과물이 있다면 삭제
#   build : 소스 코드를 컴파일하고 실행 가능한 파일로 만듬

#   -x test : build를 진행할때 테스트 단계는 제외하는 옵션
RUN ./gradlew clean build -x test

# Spring Boot DevTools 사용 위해 포트 오픈
EXPOSE 8080

# 개발용 실행 명령어 (파일 변경 자동 반영은 IDE나 Devtools 설정 필요)
CMD ["./gradlew", "bootRun", "--no-daemon"]