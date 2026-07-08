# --- Build stage ---
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn -B -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S opsdesk && adduser -S opsdesk -G opsdesk

COPY --from=build /workspace/target/*.jar app.jar

USER opsdesk

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]