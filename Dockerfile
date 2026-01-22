# -------- BUILD STAGE --------
FROM gradle:8.9-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

# -------- RUN STAGE --------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Render asigna el puerto en la variable PORT
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
