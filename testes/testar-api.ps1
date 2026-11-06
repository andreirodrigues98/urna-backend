# RODE APENAS COM UMA URNA_ELEICAO_ID NOVA, RESERVADA PARA TESTES.
# Exige Spring Boot rodando e a variável URNA_ADMIN_KEY configurada no terminal.
param([string]$Base = 'http://localhost:8080')
$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($env:URNA_ADMIN_KEY)) { throw 'Defina URNA_ADMIN_KEY neste terminal.' }
if ($env:URNA_ELEICAO_ID -notmatch '^votoverso-teste-') {
    throw 'Por segurança, use URNA_ELEICAO_ID começando com votoverso-teste- (e uma ID nova).'
}
$headers = @{'X-Admin-Key'=$env:URNA_ADMIN_KEY}
function Get-Api([string]$caminho) { Invoke-RestMethod -Uri "$Base$caminho" -Method Get }
function Post-Api([string]$caminho, $dados, [bool]$admin=$false) {
    $argsHttp = @{Uri="$Base$caminho"; Method='Post';ContentType='application/json; charset=utf-8'}
    if ($admin) { $argsHttp['Headers']=$headers }
    if ($null -ne $dados) { $argsHttp['Body']=($dados | ConvertTo-Json -Depth 8) }
    Invoke-RestMethod @argsHttp
}
function Assegurar($condicao, [string]$mensagem) { if (-not $condicao) { throw "FALHOU: $mensagem" } }
function EsperarConflito([scriptblock]$acao) {
    try { & $acao | Out-Null; throw 'FALHOU: era esperado HTTP 409.' }
    catch {
        if ($_.Exception.Message -like '*FALHOU:*') { throw }
        $status = [int]$_.Exception.Response.StatusCode
        if ($status -ne 409) { throw "HTTP $status, mas esperava 409: $_" }
    }
}
$e = Get-Api '/api/eleicao/estado'
Assegurar ($e.estado -eq 'PREPARACAO') 'use uma URNA_ELEICAO_ID inédita; este teste não reutiliza dados.'
$z = Post-Api '/api/eleicao/abrir' $null $true
Assegurar ($z.totalConfirmados -eq 0) 'zerésima precisa ser zero'
Assegurar ($z.rodada -eq 1) 'primeira rodada'
$s = Post-Api '/api/votacao/sessoes' $null
$id = $s.id
$d = @{cargo='Deputado Federal';numero='7101';branco=$false}
$p = @{cargo='Presidente';numero='72';branco=$false}
Post-Api "/api/votacao/sessoes/$id/confirmar" $d | Out-Null
EsperarConflito { Post-Api "/api/votacao/sessoes/$id/confirmar" $d }
Assegurar ((Get-Api '/api/votacao/total').totalConfirmados -eq 1) 'duplicidade não pode somar voto'
Post-Api "/api/votacao/sessoes/$id/confirmar" $p | Out-Null
Assegurar ((Get-Api '/api/votacao/total').totalConfirmados -eq 2) '2 votos por sessão completa'
$e = Get-Api '/api/eleicao/estado'
Assegurar ($e.podeZerar -eq $true) 'botão ZERAR TESTES deve aparecer quando há votos'
$z = Post-Api '/api/eleicao/zerar-testes' $null $true
Assegurar ($z.totalConfirmados -eq 0 -and $z.rodada -eq 2) 'nova rodada zerada'
EsperarConflito { Get-Api "/api/votacao/sessoes/$id" }
$s2 = Post-Api '/api/votacao/sessoes' $null
Post-Api "/api/votacao/sessoes/$($s2.id)/confirmar" @{cargo='Deputado Federal';numero='7201';branco=$false} | Out-Null
Post-Api "/api/votacao/sessoes/$($s2.id)/confirmar" @{cargo='Presidente';numero='72';branco=$false} | Out-Null
$s3 = Post-Api '/api/votacao/sessoes' $null
Post-Api "/api/votacao/sessoes/$($s3.id)/confirmar" @{cargo='Deputado Federal';numero='';branco=$true} | Out-Null
Post-Api "/api/votacao/sessoes/$($s3.id)/confirmar" @{cargo='Presidente';numero='99';branco=$false} | Out-Null
$r = Post-Api '/api/eleicao/encerrar' $null $true
Assegurar ($r.deputado.vencedor -eq 'Mulher-Maravilha') 'deputada vencedora DC'
Assegurar ($r.presidente.vencedor -eq 'Superman') 'presidente vencedor DC'
Assegurar ($r.deputado.brancos -eq 1 -and $r.presidente.nulos -eq 1) 'brancos e nulos'
EsperarConflito { Post-Api '/api/votacao/sessoes' $null }
Write-Host 'PASSOU: zerésima, sessões, idempotência, reinício, branco, nulo, vencedores e fechamento.' -ForegroundColor Green
Write-Host "Rodada final: $($r.rodada); total por cargo: 2. Reinicie o Spring Boot e consulte /api/eleicao/resultado para testar persistência." -ForegroundColor Yellow
