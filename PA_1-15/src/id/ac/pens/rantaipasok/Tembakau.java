package id.ac.pens.rantaipasok;
 
/**
 * Batch tembakau rajangan. Kelas immutable: seluruh field final dan
 * grade diturunkan dari kadar air di konstruktor, sehingga grade
 * tidak mungkin berbeda dari kadar airnya (invarian I1).
 */
public final class Tembakau {
    private final String kodeBatch;
    private final String varietas;
    private final double beratKg;
    private final double kadarAirPersen;
    private final GradeMutu grade;
 
    /** @throws IllegalArgumentException bila ada argumen tidak valid */
    public Tembakau(String kode, String var, double kg, double air) {
        this.kodeBatch = wajibTeks(kode, "kodeBatch");
        this.varietas = wajibTeks(var, "varietas");
        // fail-fast: NaN dan Infinity lolos "kg <= 0", cek eksplisit
        if (Double.isNaN(kg) || Double.isInfinite(kg) || kg <= 0.0) {
            throw new IllegalArgumentException(
                "beratKg harus bilangan terhingga > 0: " + kg);
        }
        this.beratKg = kg;
        this.grade = GradeMutu.dariKadarAir(air); // validasi 0..100
        this.kadarAirPersen = air;
    }
 
    private static String wajibTeks(String s, String nama) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(nama + " wajib diisi");
        }
        return s.trim();
    }
 
    /** Layak diproduksi bila tidak ditolak (kadar air <= 18,0%). */
    public boolean isLayakProduksi() { return grade != GradeMutu.REJECTED; }
 
    public String getKodeBatch() { return kodeBatch; }
 
    public String getVarietas() { return varietas; }
 
    public double getBeratKg() { return beratKg; }
 
    public double getKadarAirPersen() { return kadarAirPersen; }
 
    public GradeMutu getGrade() { return grade; }
}
