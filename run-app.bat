@echo off
title Peer-to-Peer Campus Marketplace
echo =====================================================================
echo    PEER-TO-PEER CAMPUS MARKETPLACE - FULL APPLICATION LAUNCHER      
echo =====================================================================
echo.

set "ROOT=%~dp0"
set "PHP_BIN=C:\xampp\php\php.exe"
set "JAVA_BIN=C:\Program Files\Java\jdk-26.0.1\bin\java.exe"

REM Step 1: Ensure MySQL daemon is active on port 3306
echo [1/3] Checking MySQL database status on port 3306...
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
if %ERRORLEVEL% NEQ 0 (
    echo Starting MySQL service / daemon...
    net start MySQL80 >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        if exist "C:\xampp\mysql\bin\mysqld.exe" (
            start /B "" "C:\xampp\mysql\bin\mysqld.exe" --console >nul 2>&1
        )
    )
    timeout /t 3 /nobreak >nul
) else (
    echo MySQL is active and listening on port 3306.
)

REM Step 2: Ensure PHP REST API Server is active on port 8000
echo [2/3] Checking PHP REST API server on http://127.0.0.1:8000...
netstat -ano | findstr ":8000" | findstr "LISTENING" >nul
if %ERRORLEVEL% NEQ 0 (
    echo Starting PHP REST API server...
    start /B "" "%PHP_BIN%" -S 127.0.0.1:8000 -t "%ROOT%backend-php\public" "%ROOT%backend-php\public\router.php" >nul 2>&1
    timeout /t 2 /nobreak >nul
) else (
    echo PHP REST API is active on port 8000.
)

REM Step 3: Launch Java Swing Desktop Client
echo [3/3] Launching Java Swing Desktop Client...
set "FAT_JAR=%ROOT%frontend-java\target\campus-marketplace-gui-1.0.0-jar-with-dependencies.jar"

if exist "%FAT_JAR%" (
    start "" "%JAVA_BIN%" -jar "%FAT_JAR%"
) else (
    start "" "%JAVA_BIN%" -cp "%ROOT%frontend-java\target\classes" com.campus.marketplace.Main
)

echo.
echo =====================================================================
echo Application launched! You can now use the desktop interface.
echo =====================================================================
echo.
pause
