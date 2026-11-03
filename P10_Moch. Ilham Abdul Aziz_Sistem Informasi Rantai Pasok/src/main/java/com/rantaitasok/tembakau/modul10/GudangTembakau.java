package com.rantaitasok.tembakau.modul10;

import com.rantaitasok.tembakau.modul10.exception.KapasitasGudangPenuhException;
import com.rantaitasok.tembakau.modul10.exception.TransaksiBelumTerverifikasiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Entitas yang mengelola inventaris fisik penyimpanan tembakau di gudang mitra.
 * Menerapkan relasi Agregasi (Aggregation) terhadap kumpulan TransaksiPasok.
 * Menjamin prinsip "Pemeriksaan Sebelum Mutasi State (Check Before Mutate)"
 * dengan melempar checked exception ketika daya tampung penuh atau data tidak valid.
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
        if (kodeGudang == null || kodeGudang.trim().isEmpty()) {
            throw new IllegalArgumentException("Validasi Gagal: Kode gudang tidak boleh kosong atau null.");
        }
        if (namaGudang == null || namaGudang.trim().isEmpty()) {
            throw new IllegalArgumentException("Validasi Gagal: Nama gudang tidak boleh kosong atau null.");
        }
        if (kapasitasMaksKg <= 0) {
            throw new IllegalArgumentException(
                String.format("Validasi Gagal: Kapasitas gudang harus bernilai positif (> 0 kg). Diberikan: %.2f kg.", kapasitasMaksKg)
            );
        }

        this.kodeGudang = kodeGudang.trim();
        this.namaGudang = namaGudang.trim();
        this.kapasitasMaksKg = kapasitasMaksKg;
        this.stokTersimpanKg = 0.0;
        this.daftarTransaksi = new ArrayList<>();
    }

    /**
     * Business Logic: Menerima transaksi pasokan masuk ke gudang penyimpanan.
     * Menerapkan prinsip "Check Before Mutate":
     * 1. Validasi objek transaksi tidak null (IllegalArgumentException).
     * 2. Validasi status verifikasi transaksi (TransaksiBelumTerverifikasiException).
     * 3. Validasi batas kapasitas fisik gudang (KapasitasGudangPenuhException).
     * 
     * JIKA SALAH SATU VALIDASI GAGAL, stokTersimpanKg dan daftarTransaksi
     * DIJAMIN TIDAK MENGALAMI PERUBAHAN APAPUN (State Tetap Konsisten).
     * 
     * @param transaksi Objek transaksi pasok yang telah disahkan.
     * @throws IllegalArgumentException jika objek transaksi bernilai null.
     * @throws TransaksiBelumTerverifikasiException jika transaksi belum diverifikasi.
     * @throws KapasitasGudangPenuhException jika muatan melebihi sisa kapasitas gudang.
     */
    public void terimaPasokan(TransaksiPasok transaksi) 
            throws KapasitasGudangPenuhException, TransaksiBelumTerverifikasiException, IllegalArgumentException {
        
        // Guard 1: Cek nullability transaksi
        if (transaksi == null) {
            throw new IllegalArgumentException("Validasi Gagal: Objek transaksi pasokan tidak boleh bernilai null.");
        }

        // Guard 2: Cek status verifikasi mutu transaksi
        if (!transaksi.isTerverifikasi()) {
            throw new TransaksiBelumTerverifikasiException(
                transaksi.getIdTransaksi(), 
                "BELUM TERVERIFIKASI / DITOLAK MUTU"
            );
        }

        // Guard 3: Cek sisa kapasitas fisik gudang
        double beratMasuk = transaksi.getTembakau().getBeratKg();
        if (this.stokTersimpanKg + beratMasuk > this.kapasitasMaksKg) {
            throw new KapasitasGudangPenuhException(
                this.kodeGudang,
                this.namaGudang,
                this.kapasitasMaksKg,
                this.stokTersimpanKg,
                beratMasuk
            );
        }

        // State Mutation: Hanya dieksekusi jika ketiga guard condition di atas lolos!
        this.daftarTransaksi.add(transaksi);
        this.stokTersimpanKg += beratMasuk;

        System.out.printf("[SUKSES GUDANG] Pasokan batch '%s' (%.2f kg) berhasil disimpan di '%s'. Stok terkini: %.2f / %.2f kg (Tersedia: %.2f kg).%n",
                          transaksi.getTembakau().getKodeBatch(), beratMasuk, this.namaGudang, 
                          this.stokTersimpanKg, this.kapasitasMaksKg, cekKapasitasTersedia());
    }

    public double cekKapasitasTersedia() {
        return this.kapasitasMaksKg - this.stokTersimpanKg;
    }

    public String getKodeGudang() { return kodeGudang; }
    public String getNamaGudang() { return namaGudang; }
    public double getKapasitasMaksKg() { return kapasitasMaksKg; }
    public double getStokTersimpanKg() { return stokTersimpanKg; }
    public List<TransaksiPasok> getDaftarTransaksi() { return daftarTransaksi; }

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

