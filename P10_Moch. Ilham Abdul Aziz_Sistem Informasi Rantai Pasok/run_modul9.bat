@echo off
echo =====================================================================
echo  MENJALANKAN PRAKTIKUM PBO MODUL 9 - RANTAI PASOK TEMBAKAU
echo  Nama : Moch. Ilham Abdul Aziz (3125522014)
echo =====================================================================
echo.

if not exist bin mkdir bin

echo [1/2] Mengompilasi source code Java...
javac -d bin src/main/java/com/rantaitasok/tembakau/modul9/*.java
if %errorlevel% neq 0 (
    echo [GAGAL] Kompilasi mengalami error.
    pause
    exit /b %errorlevel%
)

echo [2/2] Menjalankan program Main...
echo.
java -cp bin com.rantaitasok.tembakau.modul9.Main

echo.
echo =====================================================================
echo  Program selesai dieksekusi.
echo =====================================================================
pause
