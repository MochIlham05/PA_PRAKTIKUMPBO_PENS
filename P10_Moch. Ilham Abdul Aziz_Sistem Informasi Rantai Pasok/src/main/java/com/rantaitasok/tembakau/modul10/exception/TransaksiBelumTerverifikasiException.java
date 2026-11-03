package com.rantaitasok.tembakau.modul10.exception;

/**
 * Custom Checked Exception yang dilempar saat transaksi yang belum diverifikasi
 * atau berstatus ditolak dipaksa masuk ke dalam inventaris gudang penyimpanan.
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class TransaksiBelumTerverifikasiException extends Exception {
    private final String idTransaksi;
    private final String statusVerifikasi;

    public TransaksiBelumTerverifikasiException(String idTransaksi, String statusVerifikasi) {
        super(String.format("Transaksi '%s' tidak dapat diproses ke gudang karena belum lolos verifikasi mutu (Status: %s)!",
                idTransaksi, statusVerifikasi));
        this.idTransaksi = idTransaksi;
        this.statusVerifikasi = statusVerifikasi;
    }

    public String getIdTransaksi() { return idTransaksi; }
    public String getStatusVerifikasi() { return statusVerifikasi; }
}

