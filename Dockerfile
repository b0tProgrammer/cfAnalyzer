FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /cfAnalyzer

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /cfAnalyzer


COPY --from=build /cfAnalyzer/target/CF-Analyzer.jar CF-Analyzer.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "CF-Analyzer.jar"]