FROM openjdk:17-jdk-alpine

ARG JAR_FILE=build/libs/*.jar

WORKDIR /app

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "-Duser.timezone=Asia/Seoul", "app.jar"]