# Compilation PowerShell - Java 17 pur
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host " COMPILATION DU LABORATOIRE CRYPTOGRAPHIQUE (JAVA 17 PURE)" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$javaFiles = Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d bin -sourcepath src $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCES] Compilation terminee avec succes dans /bin." -ForegroundColor Green
    Write-Host "Lancement : .\run.ps1" -ForegroundColor Yellow
    Write-Host "Tests     : .\test.ps1" -ForegroundColor Yellow
} else {
    Write-Host "[ERREUR] Echec lors de la compilation javac." -ForegroundColor Red
}
