ARG SERVICE_NAME

FROM maven:3.9-eclipse-temurin-17 AS build
ARG SERVICE_NAME
WORKDIR /workspace
COPY pom.xml .
COPY ${SERVICE_NAME}/pom.xml ${SERVICE_NAME}/pom.xml
COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src
RUN mvn -pl ${SERVICE_NAME} -am package -DskipTests -B

FROM eclipse-temurin:17-jre
ARG SERVICE_NAME
WORKDIR /app
COPY --from=build /workspace/${SERVICE_NAME}/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
