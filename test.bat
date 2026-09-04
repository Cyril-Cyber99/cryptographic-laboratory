@echo off
chcp 65001 > nul
echo ====================================================================
echo  EXECUTION DE LA SUITE DE VALIDATION CRYPTOGRAPHIQUE INTERNE
echo ====================================================================

if not exist bin\test\CryptoValidationTest.class (
    echo Compilation prealable requise...
    call compile.bat
)

java -Dfile.encoding=UTF-8 -cp bin test.CryptoValidationTest
pause
