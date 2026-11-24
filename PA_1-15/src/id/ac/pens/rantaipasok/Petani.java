package id.ac.pens.rantaipasok;
 
/**
 * Petani mitra. Saldo setoran hanya dapat bertambah melalui
 * kreditSetoran() yang berakses paket, sehingga hanya kelas
 * sepaket (TransaksiPasok) yang dapat memicunya (invarian I5).
 */
public class Petani {
    private final String nik;
    private final String nama;
    private final String asalDesa;
    private final double luasLahanHa;
    private double totalSetoranKg;
 
    public Petani(String nik, String nama, String desa, double lahan) {
        this.nik = wajibTeks(nik, "nik");
        this.nama = wajibTeks(nama, "nama");
        this.asalDesa = wajibTeks(desa, "asalDesa");
        if (Double.isNaN(lahan) || Double.isInfinite(lahan) || lahan <= 0.0) {
            throw new IllegalArgumentException(
                "luasLahanHa harus bilangan terhingga > 0: " + lahan);
        }
        this.luasLahanHa = lahan;
        this.totalSetoranKg = 0.0;
    }
 
    private static String wajibTeks(String s, String nama) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(nama + " wajib diisi");
        }
        return s.trim();
    }
 
    /** Mutator berakses paket: tidak terlihat oleh kode di luar paket. */
    void kreditSetoran(double beratKg) {
        if (Double.isNaN(beratKg) || Double.isInfinite(beratKg)
                || beratKg <= 0.0) {
            throw new IllegalArgumentException(
                "beratKg harus bilangan terhingga > 0: " + beratKg);
        }
        this.totalSetoranKg += beratKg;
    }
 
    public double getTotalSetoranKg() { return totalSetoranKg; }
 
    public String getNik() { return nik; }
 
    public String getNama() { return nama; }
 
    public String getAsalDesa() { return asalDesa; }
 
    public double getLuasLahanHa() { return luasLahanHa; }
}
