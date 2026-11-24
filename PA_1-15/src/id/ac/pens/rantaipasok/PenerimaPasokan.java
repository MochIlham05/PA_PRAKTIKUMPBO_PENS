package id.ac.pens.rantaipasok;
 
/**
 * Kontrak penerima pasokan. Pemanggil bergantung pada interface ini
 * sehingga jenis gudang lain dapat ditambahkan tanpa mengubahnya.
 */
public interface PenerimaPasokan {
 
    /**
     * Menerima transaksi terverifikasi. Bila gagal, state penerima
     * dan state petani tidak boleh berubah (failure atomicity).
     */
    void terimaPasokan(TransaksiPasok trx)
            throws KapasitasGudangPenuhException,
                   TransaksiBelumTerverifikasiException;
 
    /** Sisa kapasitas dalam kilogram. */
    double cekKapasitasTersedia();
}
