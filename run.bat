@echo off
title FOODFLOW // Online Food Order Processing Simulator
echo Starting FOODFLOW Simulator...

if exist "FoodFlow.jar" (
    java -jar FoodFlow.jar
) else (
    java -cp bin Main
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo If the simulator failed to launch, please run build.bat first!
    pause
)
