package com.rantaitasok.tembakau.modul10.exception;

/**
 * Custom Checked Exception yang dilempar saat muatan tembakau masuk
 * melebihi sisa daya tampung fisik gudang penyimpanan.
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class KapasitasGudangPenuhException extends Exception {
    private final String kodeGudang;
    private final String namaGudang;
    private final double kapasitasMaksKg;
    private final double stokTersimpanKg;
    private final double beratMasukKg;
    private final double sisaKapasitasKg;

    public KapasitasGudangPenuhException(String kodeGudang, String namaGudang, 
                                        double kapasitasMaksKg, double stokTersimpanKg, 
                                        double beratMasukKg) {
        super(String.format("Daya tampung gudang '%s' (%s) tidak mencukupi! Sisa kapasitas: %.2f kg, Muatan masuk: %.2f kg (Kelebihan: %.2f kg).",
                namaGudang, kodeGudang, (kapasitasMaksKg - stokTersimpanKg), beratMasukKg, 
                (stokTersimpanKg + beratMasukKg - kapasitasMaksKg)));
        this.kodeGudang = kodeGudang;
        this.namaGudang = namaGudang;
        this.kapasitasMaksKg = kapasitasMaksKg;
        this.stokTersimpanKg = stokTersimpanKg;
        this.beratMasukKg = beratMasukKg;
        this.sisaKapasitasKg = kapasitasMaksKg - stokTersimpanKg;
    }

    public String getKodeGudang() { return kodeGudang; }
    public String getNamaGudang() { return namaGudang; }
    public double getKapasitasMaksKg() { return kapasitasMaksKg; }
    public double getStokTersimpanKg() { return stokTersimpanKg; }
    public double getBeratMasukKg() { return beratMasukKg; }
    public double getSisaKapasitasKg() { return sisaKapasitasKg; }
}

