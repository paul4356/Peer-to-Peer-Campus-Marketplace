@echo off
title Campus Marketplace - Backend Server
echo =====================================================================
echo Starting MySQL and PHP REST API on http://127.0.0.1:8000 ...
echo =====================================================================

set "ROOT=%~dp0"
set "PHP_BIN=C:\xampp\php\php.exe"

netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
if %ERRORLEVEL% NEQ 0 (
    net start MySQL80 >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        if exist "C:\xampp\mysql\bin\mysqld.exe" (
            start /B "" "C:\xampp\mysql\bin\mysqld.exe" --console
        )
    )
    timeout /t 3 /nobreak >nul
)

"%PHP_BIN%" -S 127.0.0.1:8000 -t "%ROOT%backend-php\public" "%ROOT%backend-php\public\router.php"
