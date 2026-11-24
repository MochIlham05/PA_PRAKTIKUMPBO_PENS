package id.ac.pens.rantaipasok;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/**
 * Gudang penyangga. Agregasi atas TransaksiPasok: transaksi sudah ada
 * sebelum disimpan dan tidak ikut hilang bersama gudang.
 */
public class GudangTembakau implements PenerimaPasokan {
    private static final double EPS = 1e-9;
 
    private final String kodeGudang;
    private final String namaGudang;
    private final double kapasitasMaksKg;
    private double stokTersimpanKg;
    private final List<TransaksiPasok> daftarTransaksi = new ArrayList<>();
 
    public GudangTembakau(String kode, String nama, double kapasitasMaksKg) {
        if (kode == null || kode.isBlank() || nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("kode/nama gudang wajib");
        }
        if (Double.isNaN(kapasitasMaksKg) || Double.isInfinite(kapasitasMaksKg)
                || kapasitasMaksKg <= 0.0) {
            throw new IllegalArgumentException(
                "kapasitas harus bilangan terhingga > 0: " + kapasitasMaksKg);
        }
        this.kodeGudang = kode.trim();
        this.namaGudang = nama.trim();
        this.kapasitasMaksKg = kapasitasMaksKg;
        this.stokTersimpanKg = 0.0;
    }
 
    /**
     * Pola check before mutate dalam dua fase.
     * FASE 1 (guard): seluruh syarat diperiksa, tidak ada yang diubah.
     * FASE 2 (mutasi): hanya dicapai bila semua guard lolos.
     */
    @Override
    public void terimaPasokan(TransaksiPasok trx)
            throws KapasitasGudangPenuhException,
                   TransaksiBelumTerverifikasiException {
        // ---- FASE 1: GUARD ----
        if (trx == null) {                                   // guard 1
            throw new IllegalArgumentException("trx tidak boleh null");
        }
        StatusTransaksi st = trx.getStatus();
        if (st == StatusTransaksi.DISIMPAN_DI_GUDANG) {      // guard 2
            throw new IllegalStateException(
                "Transaksi " + trx.getIdTransaksi() + " sudah disimpan");
        }
        if (st != StatusTransaksi.TERVERIFIKASI) {           // guard 3
            throw new TransaksiBelumTerverifikasiException(
                trx.getIdTransaksi(), st.getLabel());
        }
        double berat = trx.getTembakau().getBeratKg();
        if (berat - cekKapasitasTersedia() > EPS) {          // guard 4
            throw new KapasitasGudangPenuhException(
                kodeGudang, kapasitasMaksKg, stokTersimpanKg, berat);
        }
        // ---- FASE 2: MUTASI (tidak ada lagi throw bisnis) ----
        trx.tandaiDisimpanDiGudang();   // kredit saldo petani + status
        stokTersimpanKg += berat;
        daftarTransaksi.add(trx);
        assert periksaInvarian() : "invarian gudang rusak";
    }
 
    @Override
    public double cekKapasitasTersedia() {
        return kapasitasMaksKg - stokTersimpanKg;
    }
 
    /** Memeriksa invarian I2, I3, dan I4 milik gudang. */
    public boolean periksaInvarian() {
        if (stokTersimpanKg < -EPS
                || stokTersimpanKg - kapasitasMaksKg > EPS) {      // I2
            return false;
        }
        double jumlah = 0.0;
        for (TransaksiPasok t : daftarTransaksi) {
            if (t.getStatus() != StatusTransaksi.DISIMPAN_DI_GUDANG) {
                return false;                                      // I4
            }
            jumlah += t.getTembakau().getBeratKg();
        }
        return Math.abs(jumlah - stokTersimpanKg) <= EPS;          // I3
    }
 
    /** Tampilan baca-saja: pemanggil tidak dapat mengubah koleksi. */
    public List<TransaksiPasok> getDaftarTransaksi() {
        return Collections.unmodifiableList(daftarTransaksi);
    }
 
    public String getKodeGudang() { return kodeGudang; }
 
    public String getNamaGudang() { return namaGudang; }
 
    public double getKapasitasMaksKg() { return kapasitasMaksKg; }
 
    public double getStokTersimpanKg() { return stokTersimpanKg; }
}
