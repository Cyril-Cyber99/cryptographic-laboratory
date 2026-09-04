@echo off
chcp 65001 > nul
echo ====================================================================
echo  LANCEMENT DU LABORATOIRE CRYPTOGRAPHIQUE AES-CBC VS AES-GCM
echo ====================================================================

if not exist bin\gui\UiUtils.class (
    echo Compilation des sources requise...
    if not exist bin mkdir bin
    javac -encoding UTF-8 -d bin -sourcepath src src\app\Main.java src\gui\*.java src\crypto\*.java src\experiment\*.java src\attack\*.java src\visualization\*.java src\util\*.java src\test\*.java
)

if %ERRORLEVEL% equ 0 (
    java -Dfile.encoding=UTF-8 -cp bin app.Main
) else (
    echo [ERREUR] Echec lors de la compilation.
    pause
)
