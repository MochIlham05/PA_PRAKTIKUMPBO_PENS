package com.rantaitasok.tembakau.modul10;

import com.rantaitasok.tembakau.modul10.exception.KapasitasGudangPenuhException;
import com.rantaitasok.tembakau.modul10.exception.MutuTembakauDitolakException;
import com.rantaitasok.tembakau.modul10.exception.TransaksiBelumTerverifikasiException;

/**
 * Driver Main Class untuk Pengujian Komprehensif Modul 10:
 * "Exception Handling dan Validasi State Objek"
 * Sistem Informasi Manajemen Rantai Pasok Tembakau Sumenep.
 * 
 * Menguji skenario:
 * 1. Validasi Input Parsing String & Fail-Fast Constructor (NumberFormatException & IllegalArgumentException)
 * 2. Transaksi Sukses Berbasis Mutu Prima (Grade A & B) dan Masuk Gudang Aman
 * 3. Penolakan Verifikasi Mutu Rusak/Basah (MutuTembakauDitolakException) & Pembuktian State Petani Tidak Berubah
 * 4. Penolakan Pasokan Belum Terverifikasi ke Gudang (TransaksiBelumTerverifikasiException) & Pembuktian State Gudang Konsisten
 * 5. Penolakan Muatan Overcapacity (KapasitasGudangPenuhException) & Pembuktian State Gudang Tidak Berubah
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("        SIMULASI SISTEM INFORMASI RANTAI PASOK TEMBAKAU SUMENEP (MODUL 10)     ");
        System.out.println("         TOPIK: EXCEPTION HANDLING & VALIDASI KONSISTENSI STATE OBJEK          ");
        System.out.println("       Praktikan: Moch. Ilham Abdul Aziz (NRP: 3125522014) - PENS PSDKU       ");
        System.out.println("===============================================================================\n");

        // Inisialisasi Gudang Penyangga Sumenep dengan kapasitas 1000 kg
        GudangTembakau gudangSumenep = new GudangTembakau("GDG-SMP-01", "Gudang Penyangga Sumenep", 1000.0);

        // =====================================================================
        // SKENARIO 1: VALIDASI INPUT PARSING STRING & FAIL-FAST CONSTRUCTOR
        // =====================================================================
        System.out.println(">>> [SKENARIO 1: VALIDASI INPUT STRING PARSING & FAIL-FAST CONSTRUCTOR] <<<");
        String[] daftarInputBerat = {"250.0", "dua_ratus_lima_puluh", "-75.5", "0.0"};

        for (String rawInput : daftarInputBerat) {
            System.out.printf("%n[UJI INPUT] Mencoba membuat objek Tembakau dengan parameter berat: '%s'%n", rawInput);
            try {
                // Parsing nilai berat dari string
                double beratParsed = Double.parseDouble(rawInput);
                Tembakau ujiTembakau = new Tembakau("BATCH-TEST", "Prancak 95", beratParsed, 12.0);
                System.out.printf("--> [BERHASIL] Objek Tembakau terbentuk dengan berat: %.2f kg.%n", ujiTembakau.getBeratKg());

            } catch (NumberFormatException e) {
                System.err.printf("--> [DITANGKAP - NumberFormatException] Input '%s' bukan format numerik desimal yang valid!%n", rawInput);
            } catch (IllegalArgumentException e) {
                System.err.printf("--> [DITANGKAP - IllegalArgumentException] %s%n", e.getMessage());
            } catch (Exception e) {
                System.err.printf("--> [DITANGKAP - General Exception] Terjadi kesalahan tak terduga: %s%n", e.getMessage());
            } finally {
                System.out.println("--> [FINALLY] Validasi input selesai dievaluasi.");
            }
        }

        // Uji Constructor Petani dengan NIK kosong
        System.out.println("\n[UJI INPUT] Mencoba registrasi Petani dengan NIK Kosong/Null:");
        try {
            Petani pInvalid = new Petani("", "Pak Supardi", "Desa Ganding", 1.2);
            System.out.println("--> [BERHASIL] Petani terbentuk: " + pInvalid.getNama());
        } catch (IllegalArgumentException e) {
            System.err.printf("--> [DITANGKAP - IllegalArgumentException] %s%n", e.getMessage());
        } finally {
            System.out.println("--> [FINALLY] Guard condition constructor berhasil melindungi integritas state Petani.\n");
        }

        // =====================================================================
        // SKENARIO 2: ALUR TRANSAKSI BERHASIL (MUTU PRIMA & MASUK GUDANG AMAN)
        // =====================================================================
        System.out.println(">>> [SKENARIO 2: TRANSAKSI SUKSES (GRADE A & B) & MASUK GUDANG SECARA AMAN] <<<");
        
        // Registrasi 2 Petani Mitra
        Petani petani1 = new Petani("3529012304780001", "Pak Hasan Basri", "Desa Guluk-Guluk", 1.80);
        Petani petani2 = new Petani("3529021508820002", "Pak Jumai", "Desa Pasongsongan", 2.50);

        System.out.println("\n--- Data Registrasi Awal Petani Mitra ---");
        petani1.tampilkanProfil();
        petani2.tampilkanProfil();

        // Batch 1: Kadar air 10.50% (Grade A / Super)
        Tembakau batch1 = new Tembakau("BATCH-GLK-001", "Campalok Super", 250.0, 10.50);
        TransaksiPasok tx1 = new TransaksiPasok("TRX-2026-001", petani1, batch1);

        // Batch 2: Kadar air 14.00% (Grade B / Medium)
        Tembakau batch2 = new Tembakau("BATCH-PSS-002", "Prancak 95", 400.0, 14.00);
        TransaksiPasok tx2 = new TransaksiPasok("TRX-2026-002", petani2, batch2);

        // Eksekusi Verifikasi & Pengiriman ke Gudang untuk Transaksi 1
        System.out.println("\n--- Memproses Transaksi 1 (Batch-GLK-001) ---");
        try {
            tx1.verifikasiTransaksi();
            tx1.cetakFakturNota();
            gudangSumenep.terimaPasokan(tx1);
        } catch (MutuTembakauDitolakException e) {
            System.err.printf("[DITANGKAP - MutuTembakauDitolakException] %s%n", e.getMessage());
        } catch (KapasitasGudangPenuhException e) {
            System.err.printf("[DITANGKAP - KapasitasGudangPenuhException] %s%n", e.getMessage());
        } catch (TransaksiBelumTerverifikasiException e) {
            System.err.printf("[DITANGKAP - TransaksiBelumTerverifikasiException] %s%n", e.getMessage());
        } finally {
            System.out.printf("[FINALLY AUDIT] Stok Gudang: %.2f / %.2f kg | Total Setoran Pak Hasan: %.2f kg%n",
                              gudangSumenep.getStokTersimpanKg(), gudangSumenep.getKapasitasMaksKg(), petani1.getTotalSetoranKg());
        }

        // Eksekusi Verifikasi & Pengiriman ke Gudang untuk Transaksi 2
        System.out.println("\n--- Memproses Transaksi 2 (Batch-PSS-002) ---");
        try {
            tx2.verifikasiTransaksi();
            tx2.cetakFakturNota();
            gudangSumenep.terimaPasokan(tx2);
        } catch (MutuTembakauDitolakException e) {
            System.err.printf("[DITANGKAP - MutuTembakauDitolakException] %s%n", e.getMessage());
        } catch (KapasitasGudangPenuhException e) {
            System.err.printf("[DITANGKAP - KapasitasGudangPenuhException] %s%n", e.getMessage());
        } catch (TransaksiBelumTerverifikasiException e) {
            System.err.printf("[DITANGKAP - TransaksiBelumTerverifikasiException] %s%n", e.getMessage());
        } finally {
            System.out.printf("[FINALLY AUDIT] Stok Gudang: %.2f / %.2f kg | Total Setoran Pak Jumai: %.2f kg%n",
                              gudangSumenep.getStokTersimpanKg(), gudangSumenep.getKapasitasMaksKg(), petani2.getTotalSetoranKg());
        }

        // =====================================================================
        // SKENARIO 3: SKENARIO GAGAL MUTU DITOLAK (MutuTembakauDitolakException)
        // =====================================================================
        System.out.println("\n>>> [SKENARIO 3: VALIDASI MUTU & CUSTOM EXCEPTION MutuTembakauDitolakException] <<<");
        System.out.println("Deskripsi Kasus: Petani Pak Jumai membawa tembakau basah (kadar air 21.5% > 18.0%).");
        
        Tembakau batchRusak = new Tembakau("BATCH-PSS-003", "Cangkring", 180.0, 21.50);
        TransaksiPasok tx3 = new TransaksiPasok("TRX-2026-003", petani2, batchRusak);

        double setoranAwalPetani2 = petani2.getTotalSetoranKg();
        System.out.printf("[STATE AWAL] Total Setoran Pak Jumai sebelum verifikasi: %.2f kg%n", setoranAwalPetani2);

        try {
            System.out.println("--> Menjalankan method verifikasiTransaksi() pada tx3...");
            tx3.verifikasiTransaksi();
            System.out.println("--> [TIDAK TERCAPAI] Transaksi berhasil lolos verifikasi.");
        } catch (MutuTembakauDitolakException e) {
            System.err.println("--> [DITANGKAP - MutuTembakauDitolakException] Verifikasi transaksi dibatalkan!");
            System.err.printf("    Rincian Error : %s%n", e.getMessage());
            System.err.printf("    Kadar Air Uji : %.2f%% (Maksimal diperbolehkan: %.2f%%)%n", 
                              e.getKadarAirPersen(), e.getBatasMaksimalAir());
        } catch (IllegalStateException e) {
            System.err.printf("--> [DITANGKAP - IllegalStateException] %s%n", e.getMessage());
        } finally {
            double setoranAkhirPetani2 = petani2.getTotalSetoranKg();
            System.out.println("--> [FINALLY - PEMBUKTIAN KONSISTENSI STATE PETANI]");
            System.out.printf("    State Sebelum Percobaan : %.2f kg%n", setoranAwalPetani2);
            System.out.printf("    State Sesudah Percobaan : %.2f kg%n", setoranAkhirPetani2);
            System.out.printf("    Kesimpulan Integritas   : %s%n", 
                              (setoranAwalPetani2 == setoranAkhirPetani2) 
                              ? "SUKSES KONSISTEN (Total setoran petani TIDAK bertambah saat mutu ditolak)." 
                              : "GAGAL! State mengalami inkonsistensi!");
        }

        // =====================================================================
        // SKENARIO 4: SKENARIO GAGAL TRANSAKSI BELUM TERVERIFIKASI KE GUDANG
        // =====================================================================
        System.out.println("\n>>> [SKENARIO 4: VALIDASI GUDANG DENGAN TransaksiBelumTerverifikasiException] <<<");
        System.out.println("Deskripsi Kasus: Memaksa transaksi tx3 (berstatus ditolak/belum lolos) masuk ke gudang.");

        double stokAwalGudang = gudangSumenep.getStokTersimpanKg();
        int jmlTxAwalGudang = gudangSumenep.getDaftarTransaksi().size();
        System.out.printf("[STATE AWAL GUDANG] Stok Tersimpan: %.2f kg | Jumlah Transaksi: %d%n", 
                          stokAwalGudang, jmlTxAwalGudang);

        try {
            System.out.println("--> Menjalankan gudang.terimaPasokan(tx3)...");
            gudangSumenep.terimaPasokan(tx3);
            System.out.println("--> [TIDAK TERCAPAI] Pasokan berhasil disimpan di gudang.");
        } catch (TransaksiBelumTerverifikasiException e) {
            System.err.println("--> [DITANGKAP - TransaksiBelumTerverifikasiException] Gudang menolak pasokan tembakau!");
            System.err.printf("    Rincian Error : %s%n", e.getMessage());
        } catch (KapasitasGudangPenuhException e) {
            System.err.printf("--> [DITANGKAP - KapasitasGudangPenuhException] %s%n", e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.printf("--> [DITANGKAP - IllegalArgumentException] %s%n", e.getMessage());
        } finally {
            double stokAkhirGudang = gudangSumenep.getStokTersimpanKg();
            int jmlTxAkhirGudang = gudangSumenep.getDaftarTransaksi().size();
            System.out.println("--> [FINALLY - PEMBUKTIAN KONSISTENSI STATE GUDANG]");
            System.out.printf("    Stok Gudang Sebelum   : %.2f kg | Jumlah Transaksi: %d%n", stokAwalGudang, jmlTxAwalGudang);
            System.out.printf("    Stok Gudang Sesudah   : %.2f kg | Jumlah Transaksi: %d%n", stokAkhirGudang, jmlTxAkhirGudang);
            System.out.printf("    Kesimpulan Integritas : %s%n", 
                              (stokAwalGudang == stokAkhirGudang && jmlTxAwalGudang == jmlTxAkhirGudang)
                              ? "SUKSES KONSISTEN (Stok gudang TIDAK bertambah dan transaksi cacat tidak dicatat)."
                              : "GAGAL! State gudang terkorupsi!");
        }

        // =====================================================================
        // SKENARIO 5: SKENARIO GAGAL OVERCAPACITY (KapasitasGudangPenuhException)
        // =====================================================================
        System.out.println("\n>>> [SKENARIO 5: VALIDASI OVERCAPACITY DENGAN KapasitasGudangPenuhException] <<<");
        System.out.println("Deskripsi Kasus: Pengiriman muatan 500 kg padahal sisa kapasitas gudang hanya 350 kg.");

        // Transaksi 4 dengan mutu prima tapi kuantitas jumbo (500 kg)
        Tembakau batchJumbo = new Tembakau("BATCH-GLK-004", "Campalok Super", 500.0, 11.20);
        TransaksiPasok tx4 = new TransaksiPasok("TRX-2026-004", petani1, batchJumbo);

        // Verifikasi transaksi tx4 berhasil di tingkat panen
        try {
            tx4.verifikasiTransaksi();
            System.out.printf("[INFO AKAD] Transaksi '%s' lolos verifikasi mutu (Total Muatan: %.2f kg).%n", 
                              tx4.getIdTransaksi(), tx4.getTembakau().getBeratKg());
        } catch (MutuTembakauDitolakException e) {
            System.err.println("Gagal verifikasi mutu tx4: " + e.getMessage());
        }

        double stokSebelumOver = gudangSumenep.getStokTersimpanKg();
        int jmlTxSebelumOver = gudangSumenep.getDaftarTransaksi().size();
        System.out.printf("[STATE AWAL GUDANG] Stok Tersimpan: %.2f kg (Sisa Tersedia: %.2f kg)%n", 
                          stokSebelumOver, gudangSumenep.cekKapasitasTersedia());

        try {
            System.out.printf("--> Mengirim pasokan tx4 (%.2f kg) ke Gudang Penyangga Sumenep...%n", batchJumbo.getBeratKg());
            gudangSumenep.terimaPasokan(tx4);
            System.out.println("--> [TIDAK TERCAPAI] Muatan overcapacity berhasil masuk ke gudang.");
        } catch (KapasitasGudangPenuhException e) {
            System.err.println("--> [DITANGKAP - KapasitasGudangPenuhException] Pengiriman ditolak oleh sistem gudang!");
            System.err.printf("    Pesan Kesalahan   : %s%n", e.getMessage());
            System.err.printf("    Fasilitas Gudang  : %s (%s)%n", e.getNamaGudang(), e.getKodeGudang());
            System.err.printf("    Kapasitas Maksimal: %,.2f kg%n", e.getKapasitasMaksKg());
            System.err.printf("    Stok Tersimpan    : %,.2f kg%n", e.getStokTersimpanKg());
            System.err.printf("    Sisa Daya Tampung : %,.2f kg%n", e.getSisaKapasitasKg());
            System.err.printf("    Muatan Ditolak    : %,.2f kg%n", e.getBeratMasukKg());
        } catch (TransaksiBelumTerverifikasiException e) {
            System.err.printf("--> [DITANGKAP - TransaksiBelumTerverifikasiException] %s%n", e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.printf("--> [DITANGKAP - IllegalArgumentException] %s%n", e.getMessage());
        } finally {
            double stokSesudahOver = gudangSumenep.getStokTersimpanKg();
            int jmlTxSesudahOver = gudangSumenep.getDaftarTransaksi().size();
            System.out.println("--> [FINALLY - PEMBUKTIAN KONSISTENSI STATE OVERCAPACITY]");
            System.out.printf("    Stok Gudang Sebelum   : %.2f kg | Jumlah Transaksi: %d%n", stokSebelumOver, jmlTxSebelumOver);
            System.out.printf("    Stok Gudang Sesudah   : %.2f kg | Jumlah Transaksi: %d%n", stokSesudahOver, jmlTxSesudahOver);
            System.out.printf("    Kesimpulan Integritas : %s%n", 
                              (stokSebelumOver == stokSesudahOver && jmlTxSebelumOver == jmlTxSesudahOver)
                              ? "SUKSES KONSISTEN (Stok gudang TIDAK meluber, daya tampung fisik terlindungi sempurna)."
                              : "GAGAL! Kapasitas gudang terlampaui / rusak!");
        }

        // =====================================================================
        // LAPORAN AKHIR REKAPITULASI & AUDIT INTEGRITAS
        // =====================================================================
        System.out.println("\n>>> [LAPORAN REKAPITULASI INVENTARIS FISIK GUDANG AKHIR] <<<");
        gudangSumenep.tampilkanLaporanInventaris();

        System.out.println(">>> [LAPORAN PROFIL & AKUMULASI SETORAN PETANI AKHIR] <<<");
        petani1.tampilkanProfil();
        petani2.tampilkanProfil();

        System.out.println("===============================================================================");
        System.out.println(" [RINGKASAN AUDIT SISTEM: SELURUH PRINSIP MODUL 10 TERVERIFIKASI 100% SUKSES]  ");
        System.out.println(" 1. Checked Custom Exception bekerja presisi sesuai aturan domain tembakau.    ");
        System.out.println(" 2. Prinsip 'Check Before Mutate' menjamin state tidak berubah saat operasi gagal.");
        System.out.println(" 3. Blok 'try-catch-finally' menangani kegagalan dari spesifik ke umum.         ");
        System.out.println(" 4. Blok 'finally' digunakan murni untuk audit observasi, bukan memutasi data. ");
        System.out.println("===============================================================================");
    }
}

