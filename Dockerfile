FROM amazoncorretto:17-al2023-headless
WORKDIR /app
COPY build/libs/team-3trim-commerce-payment-project-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]