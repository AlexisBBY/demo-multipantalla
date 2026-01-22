# -------- BUILD STAGE --------
FROM gradle:8.9-jdk17 AS build
WORKDIR /app

# Copiamos primero archivos de gradle para aprovechar cache
COPY build.gradle.kts settings.gradle.kts gradle.properties* gradlew* ./
COPY gradle ./gradle

# Bajamos dependencias (cache)
RUN gradle --no-daemon dependencies || true

# Ahora copiamos el resto del proyecto
COPY . .

# Construimos el jar
RUN gradle clean bootJar --no-daemon

# -------- RUN STAGE --------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

# Render necesita que tu app escuche en $PORT
# (Spring ya lo hace si tienes server.port: ${PORT:8080})
EXPOSE 8080

# Recomendado para contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
