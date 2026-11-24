package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.TransaksiPasok;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entitas Alur Keluar (Outbound / Distribusi).
 * Mewakili pengiriman batch tembakau dari Gudang Penyangga ke Pabrikan Rokok Mitra.
 */
public class PengirimanPabrik {

    public enum MitraPabrik {
        GUDANG_GARAM("PT Gudang Garam Tbk", "Kediri, Jawa Timur", "Gudang Pembelian Unit 3"),
        DJARUM("PT Djarum", "Kudus, Jawa Tengah", "Sentra Pengolahan Tembakau Krapyak"),
        SAMPOERNA("PT HM Sampoerna Tbk", "Surabaya, Jawa Timur", "Plant Rungkut Industri"),
        WISMILAK("PT Wismilak Inti Makmur", "Surabaya, Jawa Timur", "Fasilitas Pengeringan Tembakau");

        private final String nama;
        private final String kota;
        private final String alamat;

        MitraPabrik(String nama, String kota, String alamat) {
            this.nama = nama;
            this.kota = kota;
            this.alamat = alamat;
        }

        public String getNama() { return nama; }
        public String getKota() { return kota; }
        public String getAlamat() { return alamat; }

        @Override
        public String toString() {
            return nama + " (" + kota + ")";
        }
    }

    private final String noSuratJalan;
    private final MitraPabrik pabrikTujuan;
    private final List<TransaksiPasok> daftarBatch;
    private final double totalBeratKg;
    private final double totalNilaiRp;
    private final String noPolisiTruk;
    private final String namaSopir;
    private final LocalDateTime waktuKirim;
    private String statusPengiriman;

    public PengirimanPabrik(String noSuratJalan,
                            MitraPabrik pabrikTujuan,
                            List<TransaksiPasok> daftarBatch,
                            String noPolisiTruk,
                            String namaSopir) {
        if (noSuratJalan == null || noSuratJalan.isBlank()) {
            throw new IllegalArgumentException("Nomor Surat Jalan wajib diisi");
        }
        if (pabrikTujuan == null) {
            throw new IllegalArgumentException("Pabrik tujuan wajib dipilih");
        }
        if (daftarBatch == null || daftarBatch.isEmpty()) {
            throw new IllegalArgumentException("Daftar batch tembakau yang dikirim tidak boleh kosong");
        }
        this.noSuratJalan = noSuratJalan.trim();
        this.pabrikTujuan = pabrikTujuan;
        this.daftarBatch = new ArrayList<>(daftarBatch);
        this.noPolisiTruk = (noPolisiTruk != null && !noPolisiTruk.isBlank()) ? noPolisiTruk.trim() : "M 8472 UA";
        this.namaSopir = (namaSopir != null && !namaSopir.isBlank()) ? namaSopir.trim() : "Bpk. Rahmat Hidayat";
        this.waktuKirim = LocalDateTime.now();
        this.statusPengiriman = "DALAM_PERJALANAN";

        double berat = 0.0;
        double nilai = 0.0;
        for (TransaksiPasok t : this.daftarBatch) {
            berat += t.getTembakau().getBeratKg();
            nilai += t.getTotalPembayaran();
        }
        this.totalBeratKg = berat;
        this.totalNilaiRp = nilai;
    }

    public String getNoSuratJalan() { return noSuratJalan; }
    public MitraPabrik getPabrikTujuan() { return pabrikTujuan; }
    public List<TransaksiPasok> getDaftarBatch() { return Collections.unmodifiableList(daftarBatch); }
    public double getTotalBeratKg() { return totalBeratKg; }
    public double getTotalNilaiRp() { return totalNilaiRp; }
    public String getNoPolisiTruk() { return noPolisiTruk; }
    public String getNamaSopir() { return namaSopir; }
    public LocalDateTime getWaktuKirim() { return waktuKirim; }
    public String getStatusPengiriman() { return statusPengiriman; }
    public void setStatusPengiriman(String status) { this.statusPengiriman = status; }
}
