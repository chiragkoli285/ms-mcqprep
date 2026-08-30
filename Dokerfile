# --- Build stage ---
FROM gradle:8.10-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test --no-daemon

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]