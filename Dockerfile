FROM eclipse-temurin:25-jdk-jammy
ARG JAR_FILE=target/auth-service-1.0.0.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app.jar"]
