# ============================================================================
# Dockerfile — Mercado UMSS (Spring Boot + Vaadin + SQLite)
# Construye y ejecuta la app en Java 21.
# ============================================================================

# --- STAGE 1: Compilación ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar solo el pom.xml primero para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el resto del código
COPY src ./src

# Compilar y empaquetar (sin tests para ahorrar tiempo)
RUN mvn package -DskipTests -B

# --- STAGE 2: Ejecución (imagen ligera con solo JRE) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el jar generado en la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Puerto que Render inyecta (variable PORT)
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
