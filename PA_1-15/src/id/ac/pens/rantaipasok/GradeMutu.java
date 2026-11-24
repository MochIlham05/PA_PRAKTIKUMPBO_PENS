package id.ac.pens.rantaipasok;
 
/**
 * Golongan mutu tembakau rajangan berdasarkan kadar air (persen).
 * Batas atas bersifat inklusif, sehingga 12,0% masih Grade A.
 * Harga per kg adalah parameter simulasi, bukan harga pasar resmi.
 */
public enum GradeMutu {
    A_SUPER(12.0, 45_000.0),
    B_MEDIUM(15.0, 38_000.0),
    C_RENDAH(18.0, 30_000.0),
    REJECTED(Double.POSITIVE_INFINITY, 0.0);
 
    private final double batasAtasPersen;
    private final double hargaPerKg;
 
    GradeMutu(double batasAtasPersen, double hargaPerKg) {
        this.batasAtasPersen = batasAtasPersen;
        this.hargaPerKg = hargaPerKg;
    }
 
    /** Fungsi murni tanpa efek samping: kadar air -> grade. */
    public static GradeMutu dariKadarAir(double air) {
        // NaN lolos operator pembanding, sehingga harus dicek eksplisit
        if (Double.isNaN(air) || air < 0.0 || air > 100.0) {
            throw new IllegalArgumentException(
                "Kadar air harus pada rentang 0-100 persen: " + air);
        }
        for (GradeMutu g : values()) {
            if (air <= g.batasAtasPersen) {
                return g;
            }
        }
        throw new AssertionError("tidak tercapai: REJECTED menampung sisa");
    }
 
    public double getBatasAtasPersen() { return batasAtasPersen; }
 
    public double getHargaPerKg() { return hargaPerKg; }
}
