@echo off
title Build and Run - Rantai Pasok Tembakau
echo =======================================================================
echo   Kompilasi & Eksekusi Otomatis - Java PBO
echo =======================================================================
echo.

set JAVAC_CMD=javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    set JAVAC_CMD="C:\Program Files\Java\jdk-25.0.2\bin\javac.exe"
)

set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    set JAVA_CMD="C:\Program Files\Java\jdk-25.0.2\bin\java.exe"
)

if not exist bin mkdir bin

echo [1/2] Melakukan kompilasi seluruh berkas Java...
powershell -Command "$files = (Get-ChildItem -Recurse -Filter *.java src).FullName; & %JAVAC_CMD% -d bin $files"

if %errorlevel% neq 0 (
    echo [ERROR] Kompilasi gagal! Periksa sintaks kode sumber.
    pause
    exit /b %errorlevel%
)

echo [2/2] Kompilasi SUKSES! Membuka GUI Aplikasi...
echo.
%JAVA_CMD% -cp bin id.ac.pens.rantaipasok.ui.MainDesktopApp
