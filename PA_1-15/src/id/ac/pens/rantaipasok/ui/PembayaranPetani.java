package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.Petani;
import id.ac.pens.rantaipasok.TransaksiPasok;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entitas Modul Keuangan / Kasir Pembayaran Petani.
 * Mewakili bukti pembayaran atau pencairan dana setoran tembakau kepada Petani Mitra.
 */
public class PembayaranPetani {

    public enum MetodeBayar {
        TUNAI("Tunai / Kas Langsung"),
        TRANSFER_BRI("Transfer Bank BRI"),
        TRANSFER_BSI("Transfer Bank Syariah Indonesia (BSI)"),
        TRANSFER_MANDIRI("Transfer Bank Mandiri"),
        TRANSFER_JATIM("Transfer Bank Jatim");

        private final String label;
        MetodeBayar(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    private final String noBuktiBayar;
    private final Petani petani;
    private final List<TransaksiPasok> daftarTransaksi;
    private final double totalNominalRp;
    private final double totalBeratKg;
    private final MetodeBayar metodeBayar;
    private final String noRekeningTujuan;
    private final LocalDateTime waktuBayar;
    private final String status;

    public PembayaranPetani(String noBuktiBayar,
                            Petani petani,
                            List<TransaksiPasok> daftarTransaksi,
                            MetodeBayar metodeBayar,
                            String noRekeningTujuan) {
        if (noBuktiBayar == null || noBuktiBayar.isBlank()) {
            throw new IllegalArgumentException("Nomor Bukti Bayar wajib diisi");
        }
        if (petani == null) {
            throw new IllegalArgumentException("Petani mitra wajib dipilih");
        }
        if (daftarTransaksi == null || daftarTransaksi.isEmpty()) {
            throw new IllegalArgumentException("Daftar transaksi yang dibayarkan tidak boleh kosong");
        }
        this.noBuktiBayar = noBuktiBayar.trim();
        this.petani = petani;
        this.daftarTransaksi = new ArrayList<>(daftarTransaksi);
        this.metodeBayar = (metodeBayar != null) ? metodeBayar : MetodeBayar.TUNAI;
        this.noRekeningTujuan = (noRekeningTujuan != null && !noRekeningTujuan.isBlank()) ? noRekeningTujuan.trim() : "-";
        this.waktuBayar = LocalDateTime.now();
        this.status = "LUNAS";

        double nominal = 0.0;
        double berat = 0.0;
        for (TransaksiPasok t : this.daftarTransaksi) {
            nominal += t.getTotalPembayaran();
            berat += t.getTembakau().getBeratKg();
        }
        this.totalNominalRp = nominal;
        this.totalBeratKg = berat;
    }

    public String getNoBuktiBayar() { return noBuktiBayar; }
    public Petani getPetani() { return petani; }
    public List<TransaksiPasok> getDaftarTransaksi() { return Collections.unmodifiableList(daftarTransaksi); }
    public double getTotalNominalRp() { return totalNominalRp; }
    public double getTotalBeratKg() { return totalBeratKg; }
    public MetodeBayar getMetodeBayar() { return metodeBayar; }
    public String getNoRekeningTujuan() { return noRekeningTujuan; }
    public LocalDateTime getWaktuBayar() { return waktuBayar; }
    public String getStatus() { return status; }
}
