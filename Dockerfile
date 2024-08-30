# Build stage
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -Dhttps.protocols=TLSv1.2

# Run stage
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/restapi_draft.war app.war
ENTRYPOINT ["java", "-jar", "app.war"]
