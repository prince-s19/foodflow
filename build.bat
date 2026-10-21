@echo off
echo ===================================================
echo   FOODFLOW // BUILD SCRIPT
echo ===================================================
if not exist "bin" mkdir bin

echo [1/2] Compiling Java source files...
javac -encoding UTF-8 -d bin src\models\*.java src\threading\*.java src\utils\*.java src\core\*.java src\ui\*.java src\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Packaging executable JAR (FoodFlow.jar)...
jar cfe FoodFlow.jar Main -C bin .

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] JAR packaging failed!
    pause
    exit /b %ERRORLEVEL%
)

echo ===================================================
echo [SUCCESS] Build complete! Executable: FoodFlow.jar
echo Run using: run.bat OR java -jar FoodFlow.jar
echo ===================================================
pause
