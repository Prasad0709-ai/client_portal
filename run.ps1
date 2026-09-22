param(
    [string]$Profile = "test"
)

# ===================================================================
# Launch Script for Client Portal & Project Management (JV-CRM-005)
# ===================================================================

$ErrorActionPreference = "Stop"

$JAVA_HOME = "C:\Users\pujar\AppData\Local\Programs\IntelliJ IDEA 2025.3\jbr"
$env:JAVA_HOME = $JAVA_HOME
$env:PATH = "$JAVA_HOME\bin;" + $env:PATH

$MVN = "C:\Users\pujar\AppData\Local\Programs\IntelliJ IDEA 2025.3\plugins\maven\lib\maven3\bin\mvn.cmd"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Starting Client Portal & Project Management (JV-CRM-005)" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Java Home: $JAVA_HOME" -ForegroundColor Gray
Write-Host "Maven Path: $MVN" -ForegroundColor Gray

if ($Profile -eq "test" -or $Profile -eq "h2") {
    Write-Host "Starting with embedded H2 profile (zero-config)..." -ForegroundColor Yellow
    & $MVN spring-boot:run -Dspring-boot.run.profiles=test
} else {
    Write-Host "Starting with MySQL profile (default localhost:3306)..." -ForegroundColor Yellow
    & $MVN spring-boot:run
}
