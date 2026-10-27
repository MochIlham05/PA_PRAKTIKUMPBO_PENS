@echo off
setlocal
cd /d "%~dp0"
set "MAVEN_HOME=%~dp0.maven\apache-maven-3.9.9"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

echo =====================================================================
echo  MENJALANKAN TEST SKENARIO SIDANG (UNIT & INTEGRATION TEST)
echo =====================================================================
echo.

call mvn test

pause
