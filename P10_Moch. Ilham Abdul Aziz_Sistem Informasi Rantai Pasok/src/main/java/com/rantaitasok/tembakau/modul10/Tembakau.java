package com.rantaitasok.tembakau.modul10;

/**
 * Entitas yang merepresentasikan Batch Daun Tembakau hasil panen.
 * Memuat aturan bisnis standardisasi mutu dan penentuan grade daun tembakau Madura.
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class Tembakau {
    public static final double BATAS_KADAR_AIR_MAKS = 18.0; // Standar industri: > 18% ditolak

    private String kodeBatch;
    private String varietas;
    private double beratKg;
    private double kadarAirPersen;
    private String grade;
    private boolean layakProduksi;

    /**
     * Fail-Fast Constructor dengan validasi rentang fisik dan format komoditas.
     */
    public Tembakau(String kodeBatch, String varietas, double beratKg, double kadarAirPersen) {
        if (kodeBatch == null || kodeBatch.trim().isEmpty()) {
            throw new IllegalArgumentException("Validasi Gagal: Kode batch tembakau tidak boleh kosong atau null.");
        }
        if (varietas == null || varietas.trim().isEmpty()) {
            throw new IllegalArgumentException("Validasi Gagal: Varietas tembakau tidak boleh kosong atau null.");
        }
        if (beratKg <= 0) {
            throw new IllegalArgumentException(
                String.format("Validasi Gagal: Berat tembakau harus bernilai positif (> 0 kg). Diberikan: %.2f kg.", beratKg)
            );
        }
        if (kadarAirPersen < 0.0 || kadarAirPersen > 100.0) {
            throw new IllegalArgumentException(
                String.format("Validasi Gagal: Persentase kadar air harus berada pada rentang 0.0%% - 100.0%%. Diberikan: %.2f%%.", kadarAirPersen)
            );
        }

        this.kodeBatch = kodeBatch.trim();
        this.varietas = varietas.trim();
        this.beratKg = beratKg;
        this.kadarAirPersen = kadarAirPersen;
        
        // Evaluasi mutu otomatis saat instansiasi dilakukan
        evaluasiMutuDanGrade();
    }

    /**
     * Business Logic: Evaluasi mutu dan penetapan Grade Tembakau secara deterministik.
     * Standar Mutu Industri Rokok/Pabrikan Tembakau Madura:
     * - Kadar air > 18.0%: Ditolak / REJECTED (Risiko busuk & jamur kapang).
     * - Kadar air <= 12.0%: Grade Super (Aroma tajam, warna kuning keemasan).
     * - Kadar air 12.1% - 15.0%: Grade Medium (Kualitas standar pabrikan).
     * - Kadar air 15.1% - 18.0%: Grade Rendah (Kualitas bawah, potongan harga).
     */
    public void evaluasiMutuDanGrade() {
        if (this.kadarAirPersen > BATAS_KADAR_AIR_MAKS) {
            this.grade = "REJECTED (TIDAK LAYAK)";
            this.layakProduksi = false;
        } else if (this.kadarAirPersen <= 12.0) {
            this.grade = "GRADE A (SUPER)";
            this.layakProduksi = true;
        } else if (this.kadarAirPersen <= 15.0) {
            this.grade = "GRADE B (MEDIUM)";
            this.layakProduksi = true;
        } else {
            this.grade = "GRADE C (RENDAH)";
            this.layakProduksi = true;
        }
    }

    public String getKodeBatch() { return kodeBatch; }
    public String getVarietas() { return varietas; }
    public double getBeratKg() { return beratKg; }
    public double getKadarAirPersen() { return kadarAirPersen; }
    public String getGrade() { return grade; }
    public boolean isLayakProduksi() { return layakProduksi; }

    public void tampilkanDetailMutu() {
        System.out.println("--------------------------------------------------");
        System.out.println("            UJI LABORATORIUM MUTU TEMBAKAU        ");
        System.out.println("--------------------------------------------------");
        System.out.println("Kode Batch         : " + kodeBatch);
        System.out.println("Varietas Daun      : " + varietas);
        System.out.printf("Berat Bersih       : %.2f kg%n", beratKg);
        System.out.printf("Kadar Air          : %.2f %%%n", kadarAirPersen);
        System.out.println("Hasil Grade        : " + grade);
        System.out.println("Status Kelayakan   : " + (layakProduksi ? "DITERIMA / LOLOS UJI" : "DITOLAK (Terlalu Basah / Rusak)"));
        System.out.println("--------------------------------------------------");
    }
}

