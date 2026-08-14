FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY engine engine
COPY backend backend
WORKDIR /app/backend
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]