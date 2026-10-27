package com.rantaitasok.tembakau.model.enums;

/**
 * Status siklus hidup Transaksi perdagangan rantai pasok tembakau.
 */
public enum StatusTransaksi {
    PENDING("Menunggu pembayaran dan verifikasi serah terima barang"),
    LUNAS("Pembayaran selesai dan tembakau telah diserahterimakan"),
    BATAL("Transaksi dibatalkan karena ketidaksesuaian kontrak/kondisi");

    private final String keterangan;

    StatusTransaksi(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getKeterangan() {
        return keterangan;
    }
}
