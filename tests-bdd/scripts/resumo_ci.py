#!/usr/bin/env python3
"""Transforma o cucumber.json em uma tabela de resumo para a pagina da execucao no GitHub Actions."""
import json
import sys
from pathlib import Path

CAMINHO = Path(sys.argv[1] if len(sys.argv) > 1 else "tests-bdd/target/cucumber-reports/cucumber.json")


def main():
    if not CAMINHO.exists():
        print("## Testes BDD\n\nO arquivo cucumber.json nao foi gerado: os testes nao chegaram a executar.")
        return
    total = aprovados = 0
    linhas = []
    falhas = []
    for funcionalidade in json.loads(CAMINHO.read_text(encoding="utf-8")):
        cenarios = [e for e in funcionalidade.get("elements", []) if e.get("type") == "scenario"]
        ok = 0
        for cenario in cenarios:
            passos = cenario.get("before", []) + cenario.get("steps", []) + cenario.get("after", [])
            if all(p.get("result", {}).get("status") == "passed" for p in passos):
                ok += 1
            else:
                falhas.append(f"{funcionalidade['name']}: {cenario['name']}")
        total += len(cenarios)
        aprovados += ok
        linhas.append(f"| {funcionalidade['name']} | {len(cenarios)} | {ok} | {len(cenarios) - ok} |")
    print("## Testes BDD (Cucumber + RestAssured + JSON Schema)\n")
    print(f"**{aprovados} de {total} cenarios aprovados**\n")
    print("| Funcionalidade | Cenarios | Aprovados | Falhas |\n|---|---:|---:|---:|")
    print("\n".join(linhas))
    if falhas:
        print("\n### Cenarios com falha\n")
        print("\n".join(f"- {falha}" for falha in falhas))


if __name__ == "__main__":
    main()
