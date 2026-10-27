package com.rantaitasok.tembakau.modul9;

/**
 * Entitas yang merepresentasikan Petani Tembakau.
 * Menerapkan prinsip enkapsulasi ketat untuk menjaga integritas data petani.
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class Petani {
    private String nik;
    private String nama;
    private String asalDesa;
    private double luasLahanHa;
    private double totalSetoranKg;

    // Constructor dengan validasi input
    public Petani(String nik, String nama, String asalDesa, double luasLahanHa) {
        if (nik == null || nik.trim().isEmpty()) {
            throw new IllegalArgumentException("NIK Petani tidak boleh kosong.");
        }
        if (luasLahanHa <= 0) {
            throw new IllegalArgumentException("Luas lahan harus bernilai positif (> 0 Ha).");
        }
        this.nik = nik;
        this.nama = nama;
        this.asalDesa = asalDesa;
        this.luasLahanHa = luasLahanHa;
        this.totalSetoranKg = 0.0;
    }

    /**
     * Business Method: Menambahkan riwayat setoran petani secara kumulatif.
     * Mencegah pembaruan langsung nilai akumulasi tanpa melalui validasi kuantitas.
     */
    public void tambahRiwayatSetoran(double beratKg) {
        if (beratKg <= 0) {
            System.out.println("[PERINGATAN] Penambahan setoran gagal: Berat harus lebih besar dari 0 kg.");
            return;
        }
        this.totalSetoranKg += beratKg;
        System.out.printf("[INFO] Riwayat setoran Petani %s bertambah %.2f kg. (Total: %.2f kg)%n", 
                          this.nama, beratKg, this.totalSetoranKg);
    }

    public String getNik() { return nik; }
    public String getNama() { return nama; }
    public String getAsalDesa() { return asalDesa; }
    public double getLuasLahanHa() { return luasLahanHa; }
    public double getTotalSetoranKg() { return totalSetoranKg; }

    public void tampilkanProfil() {
        System.out.println("--------------------------------------------------");
        System.out.println("             DATA IDENTITAS PETANI               ");
        System.out.println("--------------------------------------------------");
        System.out.println("NIK Petani         : " + nik);
        System.out.println("Nama Lengkap       : " + nama);
        System.out.println("Asal Desa / Daerah : " + asalDesa);
        System.out.printf("Luas Lahan         : %.2f Ha%n", luasLahanHa);
        System.out.printf("Total Setoran      : %.2f kg%n", totalSetoranKg);
        System.out.println("--------------------------------------------------");
    }
}
