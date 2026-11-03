package com.rantaitasok.tembakau.modul10.exception;

/**
 * Custom Checked Exception yang dilempar saat verifikasi transaksi gagal
 * akibat kadar air tembakau di luar batas standar industri (> 18.0% / berstatus REJECTED).
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class MutuTembakauDitolakException extends Exception {
    private final String kodeBatch;
    private final String varietas;
    private final double kadarAirPersen;
    private final double batasMaksimalAir;
    private final String grade;

    public MutuTembakauDitolakException(String kodeBatch, String varietas, 
                                       double kadarAirPersen, double batasMaksimalAir, 
                                       String grade) {
        super(String.format("Mutu tembakau batch '%s' [%s] DITOLAK! Kadar air %.2f%% melampaui ambang batas maksimum pabrikan (%.2f%%). Grade: %s.",
                kodeBatch, varietas, kadarAirPersen, batasMaksimalAir, grade));
        this.kodeBatch = kodeBatch;
        this.varietas = varietas;
        this.kadarAirPersen = kadarAirPersen;
        this.batasMaksimalAir = batasMaksimalAir;
        this.grade = grade;
    }

    public String getKodeBatch() { return kodeBatch; }
    public String getVarietas() { return varietas; }
    public double getKadarAirPersen() { return kadarAirPersen; }
    public double getBatasMaksimalAir() { return batasMaksimalAir; }
    public String getGrade() { return grade; }
}

