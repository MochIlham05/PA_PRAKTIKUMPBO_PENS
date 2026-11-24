@echo off
title Package JAR - Rantai Pasok Tembakau
echo =======================================================================
echo   Packaging Standalone Executable JAR - Java PBO
echo =======================================================================
echo.

set JAR_CMD=jar
where jar >nul 2>nul
if %errorlevel% neq 0 (
    set JAR_CMD="C:\Program Files\Java\jdk-25.0.2\bin\jar.exe"
)

echo Membuat Manifest dan berkas JAR...
%JAR_CMD% --create --file RantaiPasokTembakau.jar --main-class id.ac.pens.rantaipasok.ui.MainDesktopApp -C bin .

if %errorlevel% equ 0 (
    echo [SUKSES] Berkas RantaiPasokTembakau.jar berhasil dibuat!
    echo Anda dapat langsung klik dua kali (double-click) RantaiPasokTembakau.jar untuk menjalankan aplikasi.
) else (
    echo [ERROR] Gagal membuat JAR.
)
pause
