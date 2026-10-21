# FOODFLOW PowerShell Launch Script
Write-Host "==================================================" -ForegroundColor Yellow
Write-Host "  FOODFLOW // MULTI-THREADED SIMULATOR" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Yellow

$jarPath = Join-Path $PSScriptRoot "FoodFlow.jar"

if (Test-Path $jarPath) {
    Write-Host "Launching $jarPath..." -ForegroundColor Green
    java -jar $jarPath
} else {
    Write-Host "Running from compiled bin/..." -ForegroundColor Green
    java -cp (Join-Path $PSScriptRoot "bin") Main
}
