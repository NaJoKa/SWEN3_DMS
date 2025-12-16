FROM amazoncorretto:21-alpine-jdk AS builder

#RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN apk update && apk add maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY --from=builder /app/target/worker-0.0.1-SNAPSHOT.jar /app/worker.jar

ENTRYPOINT ["java", "-jar", "/app/worker.jar"]