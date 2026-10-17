# ---- Build stage: full JDK and Maven wrapper ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Dependencies first, so they stay cached while only the code changes
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B package -DskipTests

# ---- Runtime stage: JRE only ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --uid 1001 spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]