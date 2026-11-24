package id.ac.pens.rantaipasok;
 
/** Transaksi yang belum terverifikasi dipaksa masuk ke gudang. */
public final class TransaksiBelumTerverifikasiException
        extends DomainRantaiPasokException {
    private static final long serialVersionUID = 1L;
 
    private final String idTransaksi;
    private final String statusTransaksi;
 
    public TransaksiBelumTerverifikasiException(String idTransaksi,
            String statusTransaksi) {
        super("STATUS-001", "Transaksi " + idTransaksi
            + " belum terverifikasi (status: " + statusTransaksi + ")");
        this.idTransaksi = idTransaksi;
        this.statusTransaksi = statusTransaksi;
    }
 
    public String getIdTransaksi() { return idTransaksi; }
 
    public String getStatusTransaksi() { return statusTransaksi; }
}
