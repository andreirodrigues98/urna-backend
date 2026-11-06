# Inicia a interface Swing sem precisar do VS Code.
# Requer Java 21 e acesso ao Maven na primeira compilacao.
param([string]$ApiUrl = 'http://localhost:8080')
$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path (Join-Path $PSScriptRoot '..'))

& .\mvnw.cmd -q -DskipTests compile dependency:build-classpath '-Dmdep.outputFile=target/votoverso-classpath.txt'
if ($LASTEXITCODE -ne 0) { throw 'Falha ao compilar ou preparar dependencias do Swing.' }

$dependencias = (Get-Content -Raw -LiteralPath 'target/votoverso-classpath.txt').Trim()
$classes = Join-Path (Get-Location).Path 'target/classes'
$classpath = "$classes;$dependencias"
& java "-Dvotoverso.api.url=$ApiUrl" -cp $classpath br.com.urnacidada.urna_swing.Main
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
