FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 aplicacao
WORKDIR /app
COPY --from=build /workspace/target/clientes-api-*.jar app.jar
USER aplicacao
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
