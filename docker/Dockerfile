FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /src
COPY app/pom.xml /src/pom.xml
COPY app/src /src/src
RUN mvn -q -DskipTests package

FROM tomcat:9.0-jdk11-temurin

RUN rm -rf /usr/local/tomcat/webapps/*
# Deploy as ROOT so the app is served at "/"
COPY --from=build /src/target/java-war-demo.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

