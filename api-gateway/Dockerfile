FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

ARG JAR_FILE=target/api-gateway-*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"] 