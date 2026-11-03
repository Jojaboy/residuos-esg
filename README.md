# Projeto - Cidades ESGInteligentes

**API de Gestão de Resíduos e Reciclagem** — Java 17 + Spring Boot 3, com pipeline completo de CI/CD no GitHub Actions, containerização com Docker e orquestração com Docker Compose (aplicação + banco Oracle) em dois ambientes: **staging** e **produção**.

| | |
|---|---|
| **Integrante** | João Victor Mattos — RM99412 |
| **Tema ESG** | Gestão de Resíduos (pontos de coleta, coletas e indicadores de reciclagem) |
| **Ferramenta de CI/CD** | GitHub Actions |
| **Orquestração** | Docker Compose |

### O que a API faz

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/login` | público | Login, retorna token JWT |
| GET | `/api/info` | público | Nome, versão e **ambiente** em que a API está rodando |
| GET | `/actuator/health` | público | Health check (usado pelo Docker e pelo pipeline) |
| GET | `/api/pontos-coleta?tipo=VIDRO` | autenticado | Lista pontos de coleta (filtro opcional por tipo) |
| GET / PUT | `/api/pontos-coleta/{id}` | autenticado | Busca / atualiza um ponto |
| POST | `/api/pontos-coleta` | autenticado | Cadastra ponto de coleta |
| DELETE | `/api/pontos-coleta/{id}` | ADMIN | Exclui ponto (bloqueado se já tiver coletas) |
| GET / POST | `/api/coletas` | autenticado | Lista / agenda coletas |
| GET / PUT | `/api/coletas/{id}` | autenticado | Busca / altera coleta agendada |
| PATCH | `/api/coletas/{id}/concluir` | autenticado | Conclui uma coleta |
| DELETE | `/api/coletas/{id}` | ADMIN | Exclui coleta |
| GET | `/api/indicadores` | autenticado | Indicadores ESG: total de pontos, coletas e kg reciclados |

Documentação interativa (Swagger): `http://localhost:8080/swagger-ui.html`

Usuários criados automaticamente pela migration Flyway:

| E-mail | Senha | Perfil |
|---|---|---|
| admin@esg.com | admin123 | ADMIN |
| operador@esg.com | operador123 | OPERADOR |

---

## Como executar localmente com Docker

Pré-requisito: Docker com Docker Compose v2 (ou um **GitHub Codespace**, que já vem com Docker — útil quando não há espaço para instalar o Docker na máquina).

```bash
# 1. Clonar o repositório
git clone https://github.com/<seu-usuario>/residuos-esg.git
cd residuos-esg

# 2. Criar o arquivo de variáveis de ambiente a partir do exemplo
cp .env.example .env

# 3. Construir a imagem e subir app + Oracle
docker compose up -d --build

# 4. Acompanhar a inicialização (o Oracle leva ~1 min na primeira vez)
docker compose ps
docker compose logs -f app

# 5. Testar
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/info
bash scripts/smoke-test.sh http://localhost:8080 local    # teste completo de ponta a ponta

# 6. Derrubar (use -v para apagar também os dados do banco)
docker compose down
```

Exemplo de uso da API com token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@esg.com","senha":"admin123"}' | jq -r .token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/pontos-coleta
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/indicadores
```

Para simular os ambientes do pipeline na sua máquina:

```bash
docker compose --env-file envs/staging.env up -d --build      # staging na porta 8081
docker compose --env-file envs/production.env up -d --build   # produção na porta 8080
```

Rodar só os testes, sem Docker: `mvn test` (usa banco H2 em memória).

---

## Pipeline CI/CD

**Ferramenta:** GitHub Actions — arquivo [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml).
**Registry de imagens:** GitHub Container Registry (`ghcr.io`).

```
 push/PR ──► 1. Build ──► 2. Testes ──► 3. Imagem Docker ──► 4. Deploy STAGING ──► 5. Deploy PRODUÇÃO
             mvn package   mvn test       build + push ghcr     compose up + smoke     compose up + smoke
                                                                 test + tag :staging    test + tag :latest
```

| Etapa | O que faz | Quando roda |
|---|---|---|
| **1. Build** | Instala o JDK 17, compila com Maven e publica o `.jar` como artefato | sempre |
| **2. Testes automatizados** | Roda 21 testes (JUnit 5 + MockMvc + H2): autenticação JWT, CRUD, regras de negócio, permissões e indicadores. Gera tabela-resumo e publica os relatórios | sempre |
| **3. Imagem Docker** | Constrói a imagem com o `Dockerfile` (multi-stage) e publica no `ghcr.io` com as tags `sha-<commit>` e nome do branch | push (não roda em PR) |
| **4. Deploy Staging** | Baixa a imagem exata do commit, sobe app + Oracle com `docker compose --env-file envs/staging.env`, espera os health checks, roda o **smoke test** (login, cadastro, coleta, indicadores e conferência de que `/api/info` responde `staging`) e promove a imagem para a tag `:staging` | push na `main` ou `develop` |
| **5. Deploy Produção** | Só roda se o staging passou. Mesmo processo com `envs/production.env`, valida `/api/info` = `production` e promove a imagem para `:production` e `:latest` | push na `main` |

Lógica e boas práticas adotadas:

- **Build once, deploy many:** a imagem é construída uma única vez (etapa 3) e a *mesma* imagem, identificada pelo SHA do commit, é implantada em staging e depois em produção. O que muda entre os ambientes são só as variáveis de ambiente.
- **Promoção por gate:** produção depende do sucesso do staging (`needs: deploy-staging`). Se o smoke test de staging falhar, nada vai para produção.
- **Ambientes do GitHub (`environment: staging` / `production`):** cada deploy fica registrado na aba *Deployments* do repositório. Em *Settings → Environments → production* é possível exigir aprovação manual antes do deploy de produção.
- **Segredos:** os arquivos `envs/*.env` têm valores de demonstração. Se os secrets `STAGING_JWT_SECRET` e `PROD_JWT_SECRET` forem cadastrados em *Settings → Secrets and variables → Actions*, eles substituem os valores do arquivo automaticamente.
- **Evidências automáticas:** cada deploy escreve no resumo da execução (*Summary*) o `docker compose ps`, a resposta de `/api/info` e de `/actuator/health`, e publica os logs dos containers como artefato.

> **Sobre os ambientes:** o deploy é feito com Docker Compose dentro do runner do GitHub Actions, que faz o papel de servidor de cada ambiente (staging na porta 8081, produção na 8080). Os containers existem durante a execução do job, tempo em que são validados de ponta a ponta; o que permanece publicado são as imagens versionadas no ghcr.io (`:staging`, `:production`, `:latest`). Para manter os ambientes no ar 24h, basta trocar `runs-on: ubuntu-latest` por um *self-hosted runner* instalado em uma VM — o restante do pipeline não muda.

---

## Containerização

### Dockerfile

```dockerfile
# Estagio 1 - BUILD: compila e empacota o .jar com Maven + JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Estagio 2 - RUNTIME: imagem final enxuta, somente com o JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S esg && adduser -S esg -G esg \
    && mkdir -p /app/logs && chown -R esg:esg /app
COPY --from=build --chown=esg:esg /build/target/residuos-esg.jar app.jar
USER esg
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport" \
    SERVER_PORT=8080
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
```

Estratégias adotadas:

- **Multi-stage build:** Maven e JDK ficam só no estágio de build; a imagem final leva apenas o JRE Alpine e o `.jar`, ficando bem menor.
- **Cache de dependências:** o `pom.xml` é copiado antes do código, então as dependências só são baixadas de novo quando o `pom.xml` muda. No pipeline ainda há cache de camadas do Buildx (`cache-from: type=gha`).
- **Usuário não-root** (`esg`) para rodar a aplicação.
- **HEALTHCHECK** no `/actuator/health`, que também checa a conexão com o banco; o `docker compose --wait` do pipeline depende dele.
- **Configuração 100% por variáveis de ambiente** (12-factor): a mesma imagem roda em local, staging e produção.
- **`.dockerignore`** para não enviar `target/`, `.git/`, documentação etc. para o contexto de build.

### Orquestração — `docker-compose.yml`

| Recurso | Como foi usado |
|---|---|
| **Serviços** | `app` (API Spring Boot) e `oracle-db` (Oracle XE 21, imagem `gvenzl/oracle-xe`) |
| **Volumes** | `oracle-data` (dados do banco persistem entre reinícios) e `app-logs` (log da aplicação em arquivo) |
| **Redes** | `backend` (interna, sem saída para a internet: só a app enxerga o banco) e `frontend` (expõe a API). O banco **não** publica porta para fora |
| **Variáveis de ambiente** | Lidas do `.env` (local) ou de `envs/staging.env` / `envs/production.env`. Variáveis obrigatórias usam `${VAR:?mensagem}` e falham cedo se faltarem |
| **Ordem de subida** | `depends_on` com `condition: service_healthy`: a API só sobe depois que o Oracle passa no health check |
| **Banco por ambiente** | Cada ambiente tem seu próprio projeto Compose (`COMPOSE_PROJECT_NAME`), usuário e senha de banco, perfil Spring e porta |
| **Migrations** | Flyway cria tabelas e dados iniciais ao subir (`db/migration/oracle`); os testes usam scripts equivalentes para H2 (`db/migration/h2`) |

---

## Prints do funcionamento

> As imagens abaixo ficam na pasta `docs/prints/`. Elas são geradas pela execução real do pipeline no GitHub Actions.

**Pipeline completo (build → testes → imagem → staging → produção)**
![Pipeline completo](docs/prints/01-pipeline.png)

**Etapa de build**
![Build](docs/prints/02-build.png)

**Testes automatizados (resumo)**
![Testes](docs/prints/03-testes.png)

**Imagem publicada no GitHub Container Registry**
![Imagem no ghcr.io](docs/prints/04-imagem-ghcr.png)

**Deploy em staging funcionando (`/api/info` respondendo `"ambiente": "staging"`)**
![Staging](docs/prints/05-staging.png)

**Deploy em produção funcionando (`/api/info` respondendo `"ambiente": "production"`)**
![Produção](docs/prints/06-producao.png)

**Histórico de deploys por ambiente**
![Deployments](docs/prints/07-deployments.png)

Link da execução do pipeline: `https://github.com/<seu-usuario>/residuos-esg/actions`

---

## Tecnologias utilizadas

| Camada | Tecnologias |
|---|---|
| Linguagem / framework | Java 17, Spring Boot 3.2 (Web, Data JPA, Validation, Actuator) |
| Segurança | Spring Security, JWT (jjwt 0.12), BCrypt |
| Banco de dados | Oracle XE 21 (staging/produção), H2 em memória (testes), Flyway (migrations) |
| Documentação da API | springdoc-openapi / Swagger UI |
| Testes | JUnit 5, Spring Boot Test, MockMvc, AssertJ, Spring Security Test |
| Build | Maven 3.9 |
| Containerização | Docker (multi-stage, Eclipse Temurin 17 JRE Alpine) |
| Orquestração | Docker Compose v2 (volumes, redes, variáveis de ambiente, health checks) |
| CI/CD | GitHub Actions, GitHub Environments, GitHub Container Registry |
| Scripts | Bash, curl, jq (smoke test pós-deploy) |

---

## Estrutura do projeto

```
residuos-esg/
├── .github/workflows/ci-cd.yml     # pipeline CI/CD
├── Dockerfile
├── docker-compose.yml
├── .env.example                    # variáveis para rodar localmente
├── envs/
│   ├── staging.env                 # variáveis do ambiente de staging
│   └── production.env              # variáveis do ambiente de produção
├── scripts/smoke-test.sh           # validação pós-deploy
├── docs/prints/                    # evidências
├── pom.xml
└── src/
    ├── main/java/br/com/esg/residuos/   # config, controller, dto, entity, exception,
    │                                    # repository, security, service
    ├── main/resources/
    │   ├── application.yml / application-staging.yml / application-prod.yml
    │   └── db/migration/{oracle,h2}/    # migrations Flyway
    └── test/java/...                    # testes automatizados
```

---

## Checklist de entrega

| Item | OK |
|---|:---:|
| Projeto compactado em .ZIP com estrutura organizada | ☑ |
| Dockerfile funcional | ☑ |
| docker-compose.yml ou arquivos Kubernetes | ☑ |
| Pipeline com etapas de build, teste e deploy | ☑ |
| README.md com instruções e prints | ☑ |
| Documentação técnica com evidências (PDF ou PPT) | ☑ |
| Deploy realizado nos ambientes staging e produção | ☑ |

---

## Testes automatizados BDD

Os testes de API em Gherkin (Cucumber + RestAssured + JSON Schema) estão em [`tests-bdd/`](tests-bdd/README.md) e rodam no workflow [`testes-bdd.yml`](.github/workflows/testes-bdd.yml).
