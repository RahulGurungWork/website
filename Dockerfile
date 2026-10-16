# Build stage
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre
RUN useradd --system --uid 1001 app
WORKDIR /app
COPY --from=build /workspace/target/portfolio-*.jar app.jar
USER app
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
