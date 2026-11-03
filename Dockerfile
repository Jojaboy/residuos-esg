# syntax=docker/dockerfile:1
# =====================================================================
# Estagio 1 - BUILD: compila e empacota o .jar com Maven + JDK 17
# =====================================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copia primeiro so o pom.xml para aproveitar o cache de camadas:
# as dependencias so sao baixadas de novo quando o pom.xml mudar.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Os testes rodam na etapa "Testes" do pipeline; aqui so empacotamos.
RUN mvn -B -q package -DskipTests

# =====================================================================
# Estagio 2 - RUNTIME: imagem final enxuta, somente com o JRE
# =====================================================================
FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.title="residuos-esg" \
      org.opencontainers.image.description="Cidades ESG Inteligentes - API de Gestao de Residuos" \
      org.opencontainers.image.authors="Joao Victor Mattos - RM99412"

WORKDIR /app

# Usuario sem privilegios de root (boa pratica de seguranca)
RUN addgroup -S esg && adduser -S esg -G esg \
    && mkdir -p /app/logs && chown -R esg:esg /app

COPY --from=build --chown=esg:esg /build/target/residuos-esg.jar app.jar
USER esg

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport" \
    SERVER_PORT=8080

EXPOSE 8080

# O Docker (e o docker compose --wait do pipeline) usa este health check
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
