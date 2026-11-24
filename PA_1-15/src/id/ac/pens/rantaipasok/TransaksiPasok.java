package id.ac.pens.rantaipasok;
 
/**
 * Transaksi setoran satu batch tembakau dari satu petani.
 * Memiliki mesin status satu arah. Saldo petani TIDAK dikredit saat
 * verifikasi, melainkan hanya ketika gudang resmi menyimpan muatan.
 */
public class TransaksiPasok {
    private final String idTransaksi;
    private final Petani petani;
    private final Tembakau tembakau;
    private StatusTransaksi status;
 
    public TransaksiPasok(String id, Petani petani, Tembakau tembakau) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("idTransaksi wajib diisi");
        }
        if (petani == null || tembakau == null) {
            throw new IllegalArgumentException("petani/tembakau null");
        }
        this.idTransaksi = id.trim();
        this.petani = petani;
        this.tembakau = tembakau;
        this.status = StatusTransaksi.DIBUAT;
    }
 
    /**
     * Verifikasi mutu. Hanya mengubah status; tidak menyentuh saldo.
     * @throws MutuTembakauDitolakException kadar air > 18,0% (status
     *         tetap DIBUAT, tidak ada state yang berubah)
     * @throws IllegalStateException bila diverifikasi lebih dari sekali
     */
    public void verifikasiTransaksi() throws MutuTembakauDitolakException {
        if (status != StatusTransaksi.DIBUAT) {
            throw new IllegalStateException(
                "Transaksi " + idTransaksi + " sudah berstatus "
                + status.getLabel());
        }
        if (!tembakau.isLayakProduksi()) {
            throw new MutuTembakauDitolakException(
                tembakau.getKodeBatch(), tembakau.getVarietas(),
                tembakau.getKadarAirPersen(),
                GradeMutu.C_RENDAH.getBatasAtasPersen());
        }
        status = StatusTransaksi.TERVERIFIKASI;
    }
 
    /**
     * Dipanggil hanya oleh GudangTembakau pada fase mutasi.
     * Langkah yang berpotensi gagal (kredit saldo) didahulukan
     * sebelum perubahan status.
     */
    void tandaiDisimpanDiGudang() {
        if (status != StatusTransaksi.TERVERIFIKASI) {
            throw new IllegalStateException(
                "Hanya transaksi TERVERIFIKASI yang dapat disimpan");
        }
        petani.kreditSetoran(tembakau.getBeratKg());
        status = StatusTransaksi.DISIMPAN_DI_GUDANG;
    }
 
    public double getHargaPerKg() {
        return tembakau.getGrade().getHargaPerKg();
    }
 
    public double getTotalPembayaran() {
        return getHargaPerKg() * tembakau.getBeratKg();
    }
 
    public String getIdTransaksi() { return idTransaksi; }
 
    public Petani getPetani() { return petani; }
 
    public Tembakau getTembakau() { return tembakau; }
 
    public StatusTransaksi getStatus() { return status; }
}
