package com.rantaitasok.tembakau.modul9;

import java.util.ArrayList;
import java.util.List;

/**
 * Entitas yang mengelola inventaris fisik penyimpanan tembakau di gudang mitra.
 * Menerapkan relasi Agregasi (Aggregation) terhadap kumpulan TransaksiPasok.
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class GudangTembakau {
    private String kodeGudang;
    private String namaGudang;
    private double kapasitasMaksKg;
    private double stokTersimpanKg;
    private List<TransaksiPasok> daftarTransaksi; // Relasi Aggregation

    public GudangTembakau(String kodeGudang, String namaGudang, double kapasitasMaksKg) {
        if (kapasitasMaksKg <= 0) {
            throw new IllegalArgumentException("Kapasitas gudang harus bernilai positif (> 0 kg).");
        }
        this.kodeGudang = kodeGudang;
        this.namaGudang = namaGudang;
        this.kapasitasMaksKg = kapasitasMaksKg;
        this.stokTersimpanKg = 0.0;
        this.daftarTransaksi = new ArrayList<>();
    }

    /**
     * Business Logic: Menerima transaksi pasokan masuk ke gudang.
     * Menjaga invariant kapasitas gudang agar tidak pernah overcapacity.
     */
    public boolean terimaPasokan(TransaksiPasok transaksi) {
        if (transaksi == null) {
            System.out.println("[PERINGATAN GUDANG] Objek transaksi pasokan kosong (null).");
            return false;
        }

        if (!transaksi.isTerverifikasi()) {
            System.out.printf("[PENOLAKAN GUDANG] Transaksi %s belum diverifikasi atau ditolak mutu.%n", 
                              transaksi.getIdTransaksi());
            return false;
        }

        double beratMasuk = transaksi.getTembakau().getBeratKg();
        if (this.stokTersimpanKg + beratMasuk > this.kapasitasMaksKg) {
            System.out.printf("[OVERCAPACITY] Gudang %s penuh! Sisa kapasitas: %.2f kg, Muatan masuk: %.2f kg.%n",
                              this.namaGudang, cekKapasitasTersedia(), beratMasuk);
            return false;
        }

        // Simpan transaksi dan perbarui akumulasi stok gudang
        daftarTransaksi.add(transaksi);
        this.stokTersimpanKg += beratMasuk;
        System.out.printf("[SUKSES GUDANG] Pasokan batch %s berhasil disimpan di %s. Stok sekarang: %.2f / %.2f kg.%n",
                          transaksi.getTembakau().getKodeBatch(), this.namaGudang, this.stokTersimpanKg, this.kapasitasMaksKg);
        return true;
    }

    public double cekKapasitasTersedia() {
        return this.kapasitasMaksKg - this.stokTersimpanKg;
    }

    public void tampilkanLaporanInventaris() {
        System.out.println("\n##################################################");
        System.out.println("          LAPORAN REKAPITULASI INVENTARIS GUDANG   ");
        System.out.println("##################################################");
        System.out.println("Kode Gudang        : " + kodeGudang);
        System.out.println("Nama Fasilitas     : " + namaGudang);
        System.out.printf("Kapasitas Maksimal : %,.2f kg%n", kapasitasMaksKg);
        System.out.printf("Stok Tersimpan     : %,.2f kg (Tersedia: %,.2f kg)%n", stokTersimpanKg, cekKapasitasTersedia());
        System.out.println("Jumlah Transaksi   : " + daftarTransaksi.size() + " Transaksi Masuk");
        System.out.println("--------------------------------------------------");
        System.out.println("RINCIAN BATCH TEMBAKAU TERSIMPAN:");
        if (daftarTransaksi.isEmpty()) {
            System.out.println("   [Gudang masih kosong belum ada pasokan]");
        } else {
            int no = 1;
            for (TransaksiPasok tx : daftarTransaksi) {
                System.out.printf("   %d. Batch: %s | Petani: %-15s | Grade: %-16s | Berat: %6.2f kg%n",
                                  no++,
                                  tx.getTembakau().getKodeBatch(),
                                  tx.getPetani().getNama(),
                                  tx.getTembakau().getGrade(),
                                  tx.getTembakau().getBeratKg());
            }
        }
        System.out.println("##################################################\n");
    }
}
