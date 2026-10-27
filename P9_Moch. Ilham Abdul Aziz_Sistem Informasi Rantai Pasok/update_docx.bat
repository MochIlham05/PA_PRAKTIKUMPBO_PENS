@echo off
echo ========================================================
echo   Sinkronisasi File Laporan Praktikum Modul 9 ke Dokumen Asli
echo ========================================================
echo.
python -c "
import shutil, os, sys
src = 'Moch. Ilham Abdul Aziz_PBO_3125522014_Pertemuan 9_FINAL.docx'
dst = 'Moch. Ilham Abdul Aziz_PBO_3125522014_Pertemuan 9.docx'
try:
    shutil.copyfile(src, dst)
    print('[BERHASIL] File asli ' + dst + ' berhasil diperbarui!')
except Exception as e:
    print('[GAGAL] ' + str(e))
    print('Mohon pastikan Microsoft Word sudah ditutup terlebih dahulu.')
    sys.exit(1)
"
pause
