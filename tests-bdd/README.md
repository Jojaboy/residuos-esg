# Testes Automatizados BDD - API ESG de Gestão de Resíduos

Testes da API `residuos-esg` (Java 17 + Spring Boot) escritos em **Gherkin** e executados com
**Cucumber**, **RestAssured** e **JSON Schema**.

**Aluno:** João Victor Mattos, RM99412

## O que é testado

| Requisito | Onde está |
|---|---|
| Cenários BDD em Gherkin, positivos e negativos | `src/test/resources/features/` (44 cenários; 53 execuções com os exemplos) |
| Testes de todas as APIs | 16 operações cobertas (tabela abaixo) |
| Validação de status code | passo `o status da resposta deve ser ...` |
| Validação do corpo JSON | passos `o campo ... deve ser ...` |
| Testes de contrato | `src/test/resources/schemas/` (10 JSON Schemas) |
| Execução documentada | este README, `scripts/` e `.github/workflows/testes-bdd.yml` |

| Método | Rota | Status validados | Contrato |
|---|---|---|---|
| POST | `/api/auth/login` | 200, 400, 401 | `token.json`, `erro.json` |
| GET | `/api/info` | 200 | `info.json` |
| GET | `/actuator/health` | 200 | `health.json` |
| GET | `/v3/api-docs` | 200 | `openapi.json` |
| GET | `/api/pontos-coleta` | 200, 400, 401 | `pontos-coleta-lista.json`, `erro.json` |
| POST | `/api/pontos-coleta` | 201, 400 | `ponto-coleta.json`, `erro.json` |
| GET | `/api/pontos-coleta/{id}` | 200, 404 | `ponto-coleta.json`, `erro.json` |
| PUT | `/api/pontos-coleta/{id}` | 200, 404 | `ponto-coleta.json`, `erro.json` |
| DELETE | `/api/pontos-coleta/{id}` | 204, 403, 404, 409 | `erro.json` |
| GET | `/api/coletas` | 200 | `coletas-lista.json` |
| POST | `/api/coletas` | 201, 400, 401, 404 | `coleta.json`, `erro.json` |
| GET | `/api/coletas/{id}` | 200, 404 | `coleta.json`, `erro.json` |
| PUT | `/api/coletas/{id}` | 200, 409 | `coleta.json`, `erro.json` |
| PATCH | `/api/coletas/{id}/concluir` | 200, 404, 409 | `coleta.json`, `erro.json` |
| DELETE | `/api/coletas/{id}` | 204, 403, 404 | `erro.json` |
| GET | `/api/indicadores` | 200, 401 | `indicadores.json` |

As respostas 401 de rota protegida não têm corpo, por isso validam somente o status.

## Relação com os pilares ESG

- **Ambiental:** filtro por tipo de resíduo, agendamento e conclusão de coletas.
- **Social:** acesso controlado por perfil, com mensagens de erro claras para cada falha.
- **Governança:** coletas concluídas não podem ser alteradas, pontos com histórico não podem ser
  excluídos, e somente o que foi coletado entra no total de quilos reciclados.

Filtre por tag: `mvn test "-Dcucumber.filter.tags=@governanca"`. As tags são `@positivo`,
`@negativo`, `@esg`, `@governanca` e `@seguranca`.

## Estrutura

```text
tests-bdd/
├── pom.xml                         dependências (Cucumber, RestAssured, JUnit 5)
├── Dockerfile                      imagem que executa os testes
├── docker-compose.testes.yml       adiciona os testes ao compose da API
├── scripts/                        execução local, com Docker e resumo do CI
└── src/test/
    ├── java/.../ApiSteps.java      implementação dos passos do Gherkin
    └── resources/
        ├── features/               cenários em Gherkin
        └── schemas/                contratos em JSON Schema
```

Na raiz do projeto, `.github/workflows/testes-bdd.yml` executa tudo no GitHub Actions.

## Como executar

Os usuários `admin@esg.com` e `operador@esg.com` são criados pelas migrations da API. Para rodar
de novo no mesmo banco, cada cenário apaga os dados que criou.

### Opção 1: GitHub Actions (não precisa instalar nada)

1. Envie o projeto inteiro para um repositório no GitHub, incluindo a pasta `.github`.
2. O push dispara o workflow **Testes BDD - API ESG**. Para repetir, use a aba **Actions** →
   **Run workflow**.
3. O resumo aparece na página da execução. O relatório HTML fica em **Artifacts** →
   `relatorio-testes-bdd`.

### Opção 2: somente com Docker

Na raiz do projeto:

```bash
bash tests-bdd/scripts/executar-com-docker.sh
```

O comando sobe a API e o Oracle, roda os testes e remove os containers. O relatório fica em
`tests-bdd/reports/cucumber-reports/relatorio.html`.

### Opção 3: com a API já no ar (Java 17 e Maven)

```bash
cp .env.example .env
docker compose up -d --build --wait
bash tests-bdd/scripts/executar-testes.sh http://localhost:8080
```

No Windows: `powershell -File tests-bdd\scripts\executar-testes.ps1 -BaseUrl http://localhost:8080`

## Configuração

Use `-Dpropriedade=valor` ou a variável de ambiente correspondente:

| Propriedade | Variável | Padrão |
|---|---|---|
| `base.url` | `BASE_URL` | `http://localhost:8080` |
| `admin.email` | `ADMIN_EMAIL` | `admin@esg.com` |
| `admin.senha` | `ADMIN_SENHA` | `admin123` |
| `operador.email` | `OPERADOR_EMAIL` | `operador@esg.com` |
| `operador.senha` | `OPERADOR_SENHA` | `operador123` |

## Evidências

Depois de uma execução verde, capture os prints listados na documentação da entrega:
workflow verde, resumo dos cenários e relatório HTML do Cucumber.
