# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=builder /app/target/visa-slot-bot-0.0.1-SNAPSHOT.jar /app/visa-bot.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/visa-bot.jar"]
