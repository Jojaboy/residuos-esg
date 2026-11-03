#!/usr/bin/env bash
# Sobe API + Oracle, roda os testes em um container e derruba tudo no final.
# Requer somente Docker com Compose v2.
set -uo pipefail
cd "$(dirname "$0")/../.."
[ -f .env ] || cp .env.example .env
ARQUIVOS=(-f docker-compose.yml -f tests-bdd/docker-compose.testes.yml)
docker compose "${ARQUIVOS[@]}" up --build --abort-on-container-exit --exit-code-from testes-bdd
codigo=$?
docker compose "${ARQUIVOS[@]}" down -v
echo "Relatorio: tests-bdd/reports/cucumber-reports/relatorio.html"
exit "$codigo"
