# Imagem que executa os testes BDD contra a API (usada com docker-compose.testes.yml)
FROM maven:3.9-eclipse-temurin-17
WORKDIR /testes

# Baixa as dependencias antes de copiar o codigo, para aproveitar o cache do Docker
COPY pom.xml .
RUN mvn -B -q dependency:resolve dependency:resolve-plugins || true

COPY src ./src

ENV BASE_URL=http://app:8080
ENTRYPOINT ["mvn", "-B", "test"]
