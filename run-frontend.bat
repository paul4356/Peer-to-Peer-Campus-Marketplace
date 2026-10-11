@echo off
title Campus Marketplace - Desktop Client
set "ROOT=%~dp0"
set "JAVA_BIN=C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
set "FAT_JAR=%ROOT%frontend-java\target\campus-marketplace-gui-1.0.0-jar-with-dependencies.jar"

if exist "%FAT_JAR%" (
    "%JAVA_BIN%" -jar "%FAT_JAR%"
) else (
    "%ROOT%mvn.cmd" -f "%ROOT%frontend-java\pom.xml" compile
    "%JAVA_BIN%" -cp "%ROOT%frontend-java\target\classes" com.campus.marketplace.Main
)
