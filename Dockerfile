FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /cfAnalyzer

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-focal

WORKDIR /cfAnalyzer


COPY --from=build /cfAnalyzer/target/CF-Analyzer.jar CF-Analyzer.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "CF-Analyzer.jar"]