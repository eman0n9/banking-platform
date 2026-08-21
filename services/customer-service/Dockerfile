FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY services/customer-service/pom.xml services/customer-service/pom.xml

RUN chmod +x mvnw && \
    ./mvnw -pl services/customer-service -am dependency:go-offline

COPY services/customer-service/src services/customer-service/src

RUN ./mvnw \
    -pl services/customer-service \
    -am \
    clean package \
    -DskipTests


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S banking && \
    adduser -S banking -G banking

COPY --from=build \
    /workspace/services/customer-service/target/customer-service-*.jar \
    app.jar

USER banking

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]