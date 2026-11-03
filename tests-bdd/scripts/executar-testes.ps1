# Roda os testes no Windows contra uma API que ja esta no ar. Requer Java 17 e Maven.
# Uso: powershell -File scripts\executar-testes.ps1 [-BaseUrl http://localhost:8080]
param([string]$BaseUrl = "http://localhost:8080")
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
Write-Host "Executando os testes BDD contra $BaseUrl"
mvn -B test "-Dbase.url=$BaseUrl"
exit $LASTEXITCODE
