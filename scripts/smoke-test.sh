#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Smoke test executado pelo pipeline logo apos cada deploy (staging e producao).
# Confere se a API subiu, se esta no ambiente certo e se os fluxos principais
# (login -> listar -> cadastrar -> indicadores) funcionam de ponta a ponta.
#
# Uso: scripts/smoke-test.sh <url-base> <ambiente-esperado>
#   ex: scripts/smoke-test.sh http://localhost:8081 staging
# ---------------------------------------------------------------------------
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
AMBIENTE_ESPERADO="${2:-}"
TENTATIVAS=60

passo() { echo -e "\n==> $*"; }
falha() { echo "ERRO: $*" >&2; exit 1; }

passo "Aguardando a aplicacao responder em $BASE_URL/actuator/health"
for i in $(seq 1 "$TENTATIVAS"); do
  status=$(curl -s "$BASE_URL/actuator/health" | jq -r '.status' 2>/dev/null || true)
  if [ "$status" = "UP" ]; then
    echo "Health: UP (tentativa $i)"
    break
  fi
  [ "$i" -eq "$TENTATIVAS" ] && falha "aplicacao nao ficou UP a tempo"
  sleep 5
done

passo "GET /api/info (ambiente)"
info=$(curl -sf "$BASE_URL/api/info")
echo "$info" | jq .
ambiente=$(echo "$info" | jq -r '.ambiente')
if [ -n "$AMBIENTE_ESPERADO" ] && [ "$ambiente" != "$AMBIENTE_ESPERADO" ]; then
  falha "esperava ambiente '$AMBIENTE_ESPERADO' mas a API respondeu '$ambiente'"
fi

passo "POST /api/auth/login"
token=$(curl -sf -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@esg.com","senha":"admin123"}' | jq -r '.token')
[ -n "$token" ] && [ "$token" != "null" ] || falha "login nao retornou token"
echo "Token JWT recebido (${#token} caracteres)"

passo "GET /api/pontos-coleta sem token deve dar 401"
codigo=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/pontos-coleta")
[ "$codigo" = "401" ] || falha "esperava 401 e recebeu $codigo"
echo "OK: $codigo"

passo "GET /api/pontos-coleta"
curl -sf "$BASE_URL/api/pontos-coleta" -H "Authorization: Bearer $token" | jq -c '.[] | {id, nome, tipoResiduo}'

passo "POST /api/pontos-coleta"
novo=$(curl -sf -X POST "$BASE_URL/api/pontos-coleta" \
  -H "Authorization: Bearer $token" -H "Content-Type: application/json" \
  -d "{\"nome\":\"Ecoponto Smoke Test $ambiente\",\"endereco\":\"Rua do Pipeline, 1\",\"bairro\":\"Centro\",\"tipoResiduo\":\"PAPEL\",\"capacidadeKg\":150}")
echo "$novo" | jq -c .
id=$(echo "$novo" | jq -r '.id')

passo "POST /api/coletas + PATCH concluir"
coleta=$(curl -sf -X POST "$BASE_URL/api/coletas" \
  -H "Authorization: Bearer $token" -H "Content-Type: application/json" \
  -d "{\"pontoColetaId\":$id,\"dataColeta\":\"$(date +%F)\",\"quantidadeKg\":42.5,\"observacao\":\"Gerada pelo smoke test\"}")
coleta_id=$(echo "$coleta" | jq -r '.id')
curl -sf -X PATCH "$BASE_URL/api/coletas/$coleta_id/concluir" -H "Authorization: Bearer $token" | jq -c '{id, status, quantidadeKg}'

passo "GET /api/indicadores"
curl -sf "$BASE_URL/api/indicadores" -H "Authorization: Bearer $token" | jq .

echo -e "\nSMOKE TEST OK - ambiente '$ambiente' funcionando em $BASE_URL"
