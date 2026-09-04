# Lancement PowerShell
if (-not (Test-Path "bin\gui\UiUtils.class")) {
    .\compile.ps1
}
java "-Dfile.encoding=UTF-8" -cp bin app.Main
