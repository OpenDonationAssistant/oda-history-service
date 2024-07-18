FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/oda-history-service-0.3.jar /app

CMD ["java","--add-opens","java.base/java.time=ALL-UNNAMED","-jar","oda-history-service-0.3.jar"]
