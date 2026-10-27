package com.rantaitasok.tembakau.modul9;

/**
 * Driver class untuk menguji alur orkestrasi objek dari panen tembakau,
 * penilaian mutu, kalkulasi nota, hingga pencatatan di gudang tembakau.
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("    SIMULASI SISTEM INFORMASI RANTAI PASOK TEMBAKAU SUMENEP    ");
        System.out.println("        PRAKTIKUM PBO - MODUL 9 - PENS PSDKU SUMENEP           ");
        System.out.println("===============================================================\n");

        // 1. Instansiasi Data Master Petani Tembakau
        System.out.println("[LANGKAH 1: REGISTRASI DATA PETANI MITRA]");
        Petani pakHasan = new Petani("3529012304780001", "Pak Hasan Basri", "Desa Guluk-Guluk", 1.8);
        Petani pakJumai = new Petani("3529021508820002", "Pak Jumai", "Desa Pasongsongan", 2.5);
        pakHasan.tampilkanProfil();

        // 2. Instansiasi Objek Batch Panen Tembakau dengan Berbagai Variasi Kadar Air
        System.out.println("\n[LANGKAH 2: UJI LABORATORIUM & PENILAIAN MUTU TEMBAKAU]");
        // Kasus 1: Kadar air prima (10.5%) -> Grade Super
        Tembakau batchA = new Tembakau("BATCH-GLK-001", "Campalok Super", 250.0, 10.5);
        batchA.tampilkanDetailMutu();

        // Kasus 2: Kadar air sedang (14.0%) -> Grade Medium
        Tembakau batchB = new Tembakau("BATCH-PSS-002", "Prancak 95", 400.0, 14.0);
        batchB.tampilkanDetailMutu();

        // Kasus 3: Kadar air terlalu tinggi (21.5%) -> Ditolak / Reject
        Tembakau batchC = new Tembakau("BATCH-PSS-003", "Cangkring", 180.0, 21.5);
        batchC.tampilkanDetailMutu();

        // 3. Pembuatan Transaksi Pasok (Menghubungkan Petani dan Tembakau)
        System.out.println("\n[LANGKAH 3: PEMBUATAN TRANSAKSI & VERIFIKASI AKAD PASOK]");
        TransaksiPasok tx1 = new TransaksiPasok("TRX-2026-001", pakHasan, batchA);
        TransaksiPasok tx2 = new TransaksiPasok("TRX-2026-002", pakJumai, batchB);
        TransaksiPasok tx3 = new TransaksiPasok("TRX-2026-003", pakJumai, batchC);

        // Eksekusi Verifikasi
        System.out.println("\n--- Eksekusi Verifikasi Transaksi 1 ---");
        tx1.verifikasiTransaksi();
        tx1.cetakFakturNota();

        System.out.println("\n--- Eksekusi Verifikasi Transaksi 2 ---");
        tx2.verifikasiTransaksi();
        tx2.cetakFakturNota();

        System.out.println("\n--- Eksekusi Verifikasi Transaksi 3 (Kasus Mutu Rusak) ---");
        tx3.verifikasiTransaksi();
        tx3.cetakFakturNota();

        // 4. Instansiasi Gudang & Agregasi Stok Pasokan
        System.out.println("\n[LANGKAH 4: PENGELOLAAN LOGISTIK & PENERIMAAN GUDANG]");
        GudangTembakau gudangPusat = new GudangTembakau("GDG-SMP-01", "Gudang Penyangga Sumenep", 1000.0);

        // Uji penerimaan pasokan ke gudang
        System.out.println("\n--> Mengirim Transaksi 1 ke Gudang:");
        gudangPusat.terimaPasokan(tx1);

        System.out.println("\n--> Mengirim Transaksi 2 ke Gudang:");
        gudangPusat.terimaPasokan(tx2);

        System.out.println("\n--> Mengirim Transaksi 3 ke Gudang (Harus Gagal karena Belum Terverifikasi):");
        gudangPusat.terimaPasokan(tx3);

        // Uji Edge Case: Simulasi Pasokan Melebihi Kapasitas (Overcapacity)
        System.out.println("\n--> Menguji Simulasi Pasokan Melebihi Kapasitas (Overcapacity):");
        Tembakau batchJumbo = new Tembakau("BATCH-EXTRA-999", "Prancak Super", 500.0, 11.0);
        TransaksiPasok txJumbo = new TransaksiPasok("TRX-2026-004", pakHasan, batchJumbo);
        txJumbo.verifikasiTransaksi();
        gudangPusat.terimaPasokan(txJumbo); // Sisa gudang hanya 350 kg, muatan 500 kg -> Ditolak

        // 5. Cetak Laporan Akhir Inventaris Gudang
        gudangPusat.tampilkanLaporanInventaris();

        // 6. Tampilkan Pembaruan Profil Petani Setelah Transaksi Sukses
        System.out.println("[LANGKAH 5: EVALUASI AKHIR RIWAYAT PETANI]");
        pakHasan.tampilkanProfil();
        pakJumai.tampilkanProfil();
    }
}
