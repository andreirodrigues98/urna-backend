# Execute no PowerShell APOS configurar as variaveis de ambiente nesta sessao.
# Nao armazene senhas ou o conteudo da chave Firebase neste arquivo.
$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path (Join-Path $PSScriptRoot '..'))
if (-not $env:GOOGLE_APPLICATION_CREDENTIALS -or
    -not (Test-Path -LiteralPath $env:GOOGLE_APPLICATION_CREDENTIALS -PathType Leaf)) {
    throw 'GOOGLE_APPLICATION_CREDENTIALS deve apontar para seu arquivo JSON privado, fora do projeto.'
}
foreach ($variavel in @('FIREBASE_PROJECT_ID','URNA_ADMIN_KEY','URNA_ELEICAO_ID')) {
    if (-not [Environment]::GetEnvironmentVariable($variavel)) {
        throw "Configure a variavel de ambiente $variavel antes de iniciar o backend."
    }
}
Write-Host 'Iniciando backend VotoVerso na porta 8080...'
& .\mvnw.cmd spring-boot:run
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
