# --------------------------
# Stage 1: Dependencies (Cache Layer)
# --------------------------
FROM openjdk:21-slim AS dependencies

# Maven manuell hinzufügen, weil openjdk-Images kein Maven enthalten
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/app

# Nur die POMs kopieren, um Dependencies zu cachen
COPY pom.xml .

# Dependencies vorbereiten (offline-Cache)
RUN mvn -B -e org.apache.maven.plugins:maven-dependency-plugin:3.7.1:go-offline \
    -f pom.xml \
    -DexcludeArtifactIds=document-service

# --------------------------
# Stage 2: Build
# --------------------------
FROM openjdk:21-slim AS builder

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/app

# Cache aus vorherigem Stage übernehmen
COPY --from=dependencies /root/.m2 /root/.m2

# Jetzt den kompletten Source Code kopieren
COPY src /opt/app/src
COPY pom.xml .

# Projekt bauen (Tests überspringen für schnellere Builds)
RUN mvn -B -f pom.xml clean package -DskipTests

# --------------------------
# Stage 3: Runtime
# --------------------------
FROM openjdk:21-jdk-slim AS runtime

WORKDIR /app

# Das fette Spring Boot Jar vom api-Modul kopieren
COPY --from=builder /opt/app/target/document-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
