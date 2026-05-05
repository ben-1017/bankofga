ARG SERVICE_NAME

FROM maven:3.9-eclipse-temurin-17 AS build
ARG SERVICE_NAME
WORKDIR /workspace
COPY . .
RUN mvn -pl ${SERVICE_NAME} -am package -DskipTests -B

FROM eclipse-temurin:17-jre
ARG SERVICE_NAME
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /workspace/${SERVICE_NAME}/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
