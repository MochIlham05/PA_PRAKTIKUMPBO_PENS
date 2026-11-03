package com.rantaitasok.tembakau.modul10;

/**
 * Entitas yang merepresentasikan Petani Tembakau.
 * Menerapkan prinsip enkapsulasi ketat, validasi state invariant, dan fail-fast validation.
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class Petani {
    private String nik;
    private String nama;
    private String asalDesa;
    private double luasLahanHa;
    private double totalSetoranKg;

    /**
     * Fail-Fast Constructor dengan validasi parameter input.
     * Mencegah instansiasi objek dengan state cacat logika.
     */
    public Petani(String nik, String nama, String asalDesa, double luasLahanHa) {
        if (nik == null || nik.trim().isEmpty()) {
            throw new IllegalArgumentException("Validasi Gagal: NIK Petani tidak boleh kosong atau null.");
        }
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Validasi Gagal: Nama Petani tidak boleh kosong atau null.");
        }
        if (asalDesa == null || asalDesa.trim().isEmpty()) {
            throw new IllegalArgumentException("Validasi Gagal: Asal Desa tidak boleh kosong atau null.");
        }
        if (luasLahanHa <= 0) {
            throw new IllegalArgumentException(
                String.format("Validasi Gagal: Luas lahan harus bernilai positif (> 0 Ha). Diberikan: %.2f Ha.", luasLahanHa)
            );
        }

        this.nik = nik.trim();
        this.nama = nama.trim();
        this.asalDesa = asalDesa.trim();
        this.luasLahanHa = luasLahanHa;
        this.totalSetoranKg = 0.0; // Invariant awal setoran petani adalah 0.0 kg
    }

    /**
     * Business Method: Menambahkan riwayat setoran tembakau secara kumulatif.
     * Menerapkan prinsip Pemeriksaan Sebelum Mutasi State (Check Before Mutate).
     * 
     * @param beratKg Bobot tembakau yang berhasil disetorkan (harus > 0 kg).
     * @throws IllegalArgumentException jika bobot bernilai <= 0.
     */
    public void tambahRiwayatSetoran(double beratKg) {
        // Guard condition: Validasi sebelum mengubah state
        if (beratKg <= 0) {
            throw new IllegalArgumentException(
                String.format("Validasi Gagal: Bobot tembakau setoran harus lebih besar dari 0 kg. Diberikan: %.2f kg.", beratKg)
            );
        }

        // Mutasi state hanya dieksekusi setelah validasi berhasil
        this.totalSetoranKg += beratKg;
        System.out.printf("[INFO PETANI] Riwayat setoran Petani '%s' bertambah %.2f kg. (Akumulasi Total: %.2f kg)%n", 
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

