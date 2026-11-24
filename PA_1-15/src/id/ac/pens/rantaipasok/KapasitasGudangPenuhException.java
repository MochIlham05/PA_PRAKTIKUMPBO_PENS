package id.ac.pens.rantaipasok;
 
import java.util.Locale;
 
/** Muatan melebihi sisa kapasitas fisik gudang. */
public final class KapasitasGudangPenuhException
        extends DomainRantaiPasokException {
    private static final long serialVersionUID = 1L;
 
    private final String kodeGudang;
    private final double kapasitasMaksKg;
    private final double stokTersimpanKg;
    private final double beratMasukKg;
 
    public KapasitasGudangPenuhException(String kodeGudang,
            double kapasitasMaksKg, double stokTersimpanKg,
            double beratMasukKg) {
        super("KAPASITAS-001", String.format(Locale.ROOT,
            "Gudang %s penuh: sisa %.1f kg, muatan %.1f kg (lebih %.1f kg)",
            kodeGudang, kapasitasMaksKg - stokTersimpanKg, beratMasukKg,
            beratMasukKg - (kapasitasMaksKg - stokTersimpanKg)));
        this.kodeGudang = kodeGudang;
        this.kapasitasMaksKg = kapasitasMaksKg;
        this.stokTersimpanKg = stokTersimpanKg;
        this.beratMasukKg = beratMasukKg;
    }
 
    public double getSisaKapasitasKg() {
        return kapasitasMaksKg - stokTersimpanKg;
    }
 
    public double getKelebihanKg() {
        return beratMasukKg - getSisaKapasitasKg();
    }
 
    public String getKodeGudang() { return kodeGudang; }
}
