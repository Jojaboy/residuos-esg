#!/usr/bin/env bash
# Roda os testes contra uma API que ja esta no ar. Requer Java 17 e Maven.
# Uso: bash scripts/executar-testes.sh [url-base]
set -euo pipefail
cd "$(dirname "$0")/.."
BASE_URL="${1:-${BASE_URL:-http://localhost:8080}}"
echo "Executando os testes BDD contra $BASE_URL"
mvn -B test "-Dbase.url=$BASE_URL"
echo "Relatorio: tests-bdd/target/cucumber-reports/relatorio.html"
