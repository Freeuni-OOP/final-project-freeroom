FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Bake the Google Calendar OAuth token so it survives without volume mounts
COPY tokens/ ./tokens/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
