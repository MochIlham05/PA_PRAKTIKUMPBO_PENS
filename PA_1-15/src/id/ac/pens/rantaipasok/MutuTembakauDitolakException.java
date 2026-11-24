package id.ac.pens.rantaipasok;
 
import java.util.Locale;
 
/** Kadar air melebihi batas maksimal sehingga batch ditolak. */
public final class MutuTembakauDitolakException
        extends DomainRantaiPasokException {
    private static final long serialVersionUID = 1L;
 
    private final String kodeBatch;
    private final String varietas;
    private final double kadarAirPersen;
    private final double batasMaksimalPersen;
 
    public MutuTembakauDitolakException(String kodeBatch, String varietas,
            double kadarAirPersen, double batasMaksimalPersen) {
        super("MUTU-001", String.format(Locale.ROOT,
            "Batch %s (%s) ditolak: kadar air %.1f%% melebihi batas %.1f%%",
            kodeBatch, varietas, kadarAirPersen, batasMaksimalPersen));
        this.kodeBatch = kodeBatch;
        this.varietas = varietas;
        this.kadarAirPersen = kadarAirPersen;
        this.batasMaksimalPersen = batasMaksimalPersen;
    }
 
    public String getKodeBatch() { return kodeBatch; }
 
    public String getVarietas() { return varietas; }
 
    public double getKadarAirPersen() { return kadarAirPersen; }
 
    public double getBatasMaksimalPersen() { return batasMaksimalPersen; }
}
