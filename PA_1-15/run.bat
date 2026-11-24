@echo off
title Sistem Informasi Rantai Pasok Tembakau - PENS
echo =======================================================================
echo   SISTEM INFORMASI MANAJEMEN RANTAI PASOK TEMBAKAU
echo   Domain-Driven Architecture dengan State Invariant Protection
echo   Politeknik Elektronika Negeri Surabaya (PENS)
echo =======================================================================
echo.

set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    set JAVA_CMD="C:\Program Files\Java\jdk-25.0.2\bin\java.exe"
)

echo Menjalankan aplikasi Java Swing Desktop...
%JAVA_CMD% -cp bin id.ac.pens.rantaipasok.ui.MainDesktopApp

if %errorlevel% neq 0 (
    echo.
    echo Gagal menjalankan aplikasi secara langsung. Mencoba kompilasi ulang...
    call "%~dp0build_and_run.bat"
)
