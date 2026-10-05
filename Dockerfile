# ============================================================
# ETAPA 1: BUILD
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /almacen-app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# ETAPA 2: RUNTIME
# ============================================================
FROM eclipse-temurin:17-jre

WORKDIR /almacen-app

COPY --from=build /almacen-app/target/*.jar almacen-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "almacen-app.jar"]