param(
    [int]$TimeoutSeconds = 90,
    [switch]$Logs
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Rebuild and restart backend service" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$backendDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $backendDir

Write-Host "[1/3] Building and starting backend container..." -ForegroundColor Yellow
docker compose up -d --build backend

if ($LASTEXITCODE -ne 0) {
    Write-Host "  Failed to rebuild backend. Check Docker Desktop and build logs." -ForegroundColor Red
    exit 1
}

Write-Host "  Backend container rebuild command completed." -ForegroundColor Green
Write-Host ""

Write-Host "[2/3] Waiting for health check..." -ForegroundColor Yellow
$healthUrl = "http://localhost:8080/actuator/health"
$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
$ready = $false

while ((Get-Date) -lt $deadline) {
    try {
        $response = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 3 -ErrorAction Stop
        if ($response.status -eq "UP") {
            $ready = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 3
    }
}

if (-not $ready) {
    Write-Host "  Backend did not become healthy within $TimeoutSeconds seconds." -ForegroundColor Red
    Write-Host "  Recent backend logs:" -ForegroundColor Yellow
    docker compose logs --tail=80 backend
    exit 1
}

Write-Host "  Backend is healthy: $healthUrl" -ForegroundColor Green
Write-Host ""

Write-Host "[3/3] Current container status:" -ForegroundColor Yellow
docker compose ps backend

if ($Logs) {
    Write-Host ""
    Write-Host "Following backend logs. Press Ctrl+C to stop." -ForegroundColor Yellow
    docker compose logs -f backend
}

Write-Host ""
Write-Host "Done. Backend is available at http://localhost:8080" -ForegroundColor Cyan
