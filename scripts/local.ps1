#Requires -Version 5.1
param(
    [ValidateSet('help', 'up', 'deps', 'run', 'test', 'status', 'logs', 'db', 'stop', 'down')]
    [string]$Action = 'help'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Invoke-Compose {
    param([string[]]$ComposeArguments)
    & docker compose -f docker/docker-compose.yml @ComposeArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose falhou (codigo $LASTEXITCODE)."
    }
}

if ($Action -eq 'help') {
    Write-Output 'Uso: .\scripts\local.ps1 {up|deps|run|test|status|logs|db|stop|down}'
    Write-Output 'up: tudo em containers; deps: somente PostgreSQL e RabbitMQ'
    Write-Output 'run: dependencias + Java no terminal; test: suite completa via Maven Wrapper'
    Write-Output 'stop: para somente a API; down: para containers e preserva os dados'
    exit 0
}

Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw 'Docker nao encontrado. Instale o Docker Desktop e reabra o terminal.'
    }
    $dockerOs = & docker info --format '{{.OSType}}'
    if ($LASTEXITCODE -ne 0) {
        throw 'Inicie o Docker Desktop e aguarde o engine ficar pronto.'
    }
    if ($dockerOs -ne 'linux') {
        throw 'Selecione containers Linux no Docker Desktop.'
    }
    & docker compose version | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker Compose nao encontrado. Atualize o Docker Desktop.'
    }

    switch ($Action) {
        'up' {
            Invoke-Compose -ComposeArguments @('up', '-d', '--build', '--wait')
            $ready = $false
            for ($attempt = 0; $attempt -lt 60; $attempt++) {
                try {
                    $response = Invoke-WebRequest -Uri 'http://localhost:8080/hello' -UseBasicParsing -TimeoutSec 2
                    if ($response.StatusCode -eq 200) {
                        $ready = $true
                        break
                    }
                } catch {
                    # A API ainda pode estar inicializando depois dos servicos.
                }
                Start-Sleep -Seconds 1
            }
            if (-not $ready) {
                Invoke-Compose -ComposeArguments @('logs', '--tail', '100', 'endpoint')
                throw 'A API nao ficou pronta. Confira os logs acima.'
            }
            Write-Output 'API pronta: http://localhost:8080/hello'
            Write-Output 'RabbitMQ: http://localhost:15672 (app / app)'
        }
        'deps' { Invoke-Compose -ComposeArguments @('up', '-d', '--wait', 'postgres', 'rabbitmq') }
        { $_ -in 'run', 'test' } {
            & .\mvnw.cmd -version
            if ($LASTEXITCODE -ne 0) {
                throw 'Maven Wrapper falhou. Configure JAVA_HOME para o JDK 25.'
            }
            if ($Action -eq 'run') {
                $running = @(Invoke-Compose -ComposeArguments @('ps', '--status', 'running', '-q', 'endpoint'))
                if ($running.Count -gt 0) {
                    throw 'A API ja esta em container. Execute .\scripts\local.ps1 stop antes de usar run.'
                }
                Invoke-Compose -ComposeArguments @('up', '-d', '--wait', 'postgres', 'rabbitmq')
                & .\mvnw.cmd spring-boot:run
            } else {
                & .\mvnw.cmd test
            }
            exit $LASTEXITCODE
        }
        'status' { Invoke-Compose -ComposeArguments @('ps') }
        'logs' { Invoke-Compose -ComposeArguments @('logs', '-f', 'endpoint') }
        'db' { Invoke-Compose -ComposeArguments @('exec', 'postgres', 'psql', '-U', 'app', '-d', 'estudojava') }
        'stop' { Invoke-Compose -ComposeArguments @('stop', 'endpoint') }
        'down' { Invoke-Compose -ComposeArguments @('down') }
    }
} catch {
    [Console]::Error.WriteLine($_.Exception.Message)
    exit 1
} finally {
    Pop-Location
}
