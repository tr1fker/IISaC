FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B package -DskipTests
FROM eclipse-temurin:17-jre
WORKDIR /app
ARG SERVICE=gateway
COPY --from=build /app/${SERVICE}/target/${SERVICE}-1.0.0.jar app.jar
EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
