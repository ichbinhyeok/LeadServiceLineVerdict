# Build stage
FROM bellsoft/liberica-openjdk-alpine:21 AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src
COPY data data

RUN ./gradlew buildData bootJar -x test --no-daemon

# Runtime stage
FROM bellsoft/liberica-openjre-alpine:21
WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar
COPY --from=build /app/data data

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-Xms256m", "-Xmx384m", "-Xss512k", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
