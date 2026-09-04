@echo off
chcp 65001 > nul
echo ====================================================================
echo  COMPILATION DU LABORATOIRE CRYPTOGRAPHIQUE (JAVA 17 PURE)
echo ====================================================================

if not exist bin mkdir bin

echo Compilation de toutes les sources src/...
javac -encoding UTF-8 -d bin -sourcepath src src\app\Main.java src\gui\*.java src\crypto\*.java src\experiment\*.java src\attack\*.java src\visualization\*.java src\util\*.java src\test\*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCES] Compilation terminee sans erreur dans le dossier /bin.
    echo Vous pouvez lancer l'application avec : run.bat
    echo Ou lancer les tests de validation avec : test.bat
) else (
    echo [ERREUR] Echec lors de la compilation. Verifiez que le JDK 17 est present dans votre PATH.
)
echo ====================================================================
pause
