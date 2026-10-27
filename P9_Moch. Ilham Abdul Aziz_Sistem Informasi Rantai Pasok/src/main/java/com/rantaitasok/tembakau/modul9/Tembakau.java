package com.rantaitasok.tembakau.modul9;

/**
 * Entitas yang merepresentasikan Batch Daun Tembakau hasil panen.
 * Memuat aturan bisnis standardisasi mutu dan penentuan grade daun tembakau Madura.
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class Tembakau {
    private String kodeBatch;
    private String varietas;
    private double beratKg;
    private double kadarAirPersen;
    private String grade;
    private boolean layakProduksi;

    public Tembakau(String kodeBatch, String varietas, double beratKg, double kadarAirPersen) {
        if (beratKg <= 0) {
            throw new IllegalArgumentException("Berat tembakau tidak valid (harus > 0 kg).");
        }
        if (kadarAirPersen < 0 || kadarAirPersen > 100) {
            throw new IllegalArgumentException("Persentase kadar air harus berada pada rentang 0 - 100%.");
        }
        this.kodeBatch = kodeBatch;
        this.varietas = varietas;
        this.beratKg = beratKg;
        this.kadarAirPersen = kadarAirPersen;
        
        // Evaluasi mutu otomatis saat instansiasi dilakukan
        evaluasiMutuDanGrade();
    }

    /**
     * Business Logic: Validasi mutu dan penetapan Grade Tembakau secara deterministik.
     * Standar Mutu Industri Rokok/Pabrikan Tembakau Madura:
     * - Kadar air > 18%: Ditolak (Tidak Layak, risiko pembusukan/jamur).
     * - Kadar air <= 12%: Grade Super (Aroma tajam, warna kuning keemasan).
     * - Kadar air 13% - 15%: Grade Medium (Kualitas standar pabrikan).
     * - Kadar air 16% - 18%: Grade Rendah (Kualitas bawah, potongan harga).
     */
    public void evaluasiMutuDanGrade() {
        if (this.kadarAirPersen > 18.0) {
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
        System.out.println("Status Kelayakan   : " + (layakProduksi ? "DITERIMA / LOLOS UJI" : "DITOLAK (Terlalu Basah)"));
        System.out.println("--------------------------------------------------");
    }
}
