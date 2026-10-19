# ============================================================================
# Dockerfile — Mercado UMSS (Spring Boot + Vaadin + SQLite)
# Construye y ejecuta la app en Java 21.
# ============================================================================

# --- STAGE 1: Compilación ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar pom.xml raíz (tiene ambos módulos: nucleo y servidor)
COPY pom.xml .

# Copiar código de ambos módulos
COPY nucleo ./nucleo
COPY servidor ./servidor

# Compilar y empaquetar solo el módulo servidor (y sus dependencias del monorepo)
RUN mvn package -pl servidor -am -DskipTests -B

# --- STAGE 2: Ejecución (imagen ligera con solo JRE) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el jar generado en la etapa de build
COPY --from=build /app/servidor/target/*.jar app.jar

# Puerto que Render inyecta (variable PORT)
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
