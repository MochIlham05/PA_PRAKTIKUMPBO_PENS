package id.ac.pens.rantaipasok;
 
/**
 * Mesin status transaksi. Urutan yang sah hanya maju:
 * DIBUAT -> TERVERIFIKASI -> DISIMPAN_DI_GUDANG.
 */
public enum StatusTransaksi {
    DIBUAT("Dibuat"),
    TERVERIFIKASI("Terverifikasi"),
    DISIMPAN_DI_GUDANG("Disimpan di gudang");
 
    private final String label;
 
    StatusTransaksi(String label) { this.label = label; }
 
    public String getLabel() { return label; }
}
