# Stage 1: Build the Spring Boot application using Java 25
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application using Java 25
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/ /app/target/

# Select the executable Spring Boot JAR
RUN find /app/target -maxdepth 1 -type f -name "*.jar" \
    ! -name "original-*.jar" \
    -exec cp {} /app/app.jar \;

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
