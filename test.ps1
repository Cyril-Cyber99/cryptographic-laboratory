# Lancement des tests de validation cryptographique sous PowerShell
if (-not (Test-Path "bin\test\CryptoValidationTest.class")) {
    .\compile.ps1
}
java "-Dfile.encoding=UTF-8" -cp bin test.CryptoValidationTest
