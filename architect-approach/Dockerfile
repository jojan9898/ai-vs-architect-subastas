# Etapa de construccion: compila y empaqueta con Maven y JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cachea las dependencias primero (solo pom.xml) para acelerar rebuilds
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copia el codigo y empaqueta (sin tests; se validan localmente)
COPY src ./src
RUN mvn -B clean package -DskipTests

# Etapa de runtime: solo JRE 21 + el jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Memoria limitada y zona horaria UTC para consistencia de timestamps
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=UTC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
