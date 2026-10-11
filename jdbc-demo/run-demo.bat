@echo off
echo ===================================================
echo Starting Campus Marketplace - JDBC Demonstration
echo ===================================================

set JAVA_HOME=C:\Program Files\Java\jdk-26.0.1
set PATH=%JAVA_HOME%\bin;%PATH%

javac -cp "..\lib\mysql-connector-j-8.3.0.jar" -d bin src\main\java\com\campus\jdbc\JdbcMarketplaceDemo.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

java -cp "bin;..\lib\mysql-connector-j-8.3.0.jar" com.campus.jdbc.JdbcMarketplaceDemo
echo.
pause
