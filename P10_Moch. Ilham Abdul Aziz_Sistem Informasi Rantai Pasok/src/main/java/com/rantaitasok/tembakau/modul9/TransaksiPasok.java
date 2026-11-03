package com.rantaitasok.tembakau.modul9;

/**
 * Entitas yang merepresentasikan Transaksi Penerimaan dan Pembayaran Pasokan Tembakau.
 * Menghubungkan Petani dan Tembakau melalui hubungan Asosiasi Objek.
 * 
 * @author Moch. Ilham Abdul Aziz (3125522014)
 */
public class TransaksiPasok {
    private String idTransaksi;
    private Petani petani;       // Association ke Petani
    private Tembakau tembakau;   // Association ke Tembakau
    private double hargaPerKg;
    private double totalPembayaran;
    private boolean terverifikasi;

    public TransaksiPasok(String idTransaksi, Petani petani, Tembakau tembakau) {
        if (petani == null || tembakau == null) {
            throw new IllegalArgumentException("Transaksi membutuhkan objek Petani dan Tembakau yang valid.");
        }
        this.idTransaksi = idTransaksi;
        this.petani = petani;
        this.tembakau = tembakau;
        this.terverifikasi = false;

        // Hitung estimasi harga awal
        kalkulasiHargaOtomatis();
    }

    /**
     * Business Logic: Menghitung nominal harga per kg berdasarkan grade mutu tembakau.
     */
    public void kalkulasiHargaOtomatis() {
        if (!tembakau.isLayakProduksi()) {
            this.hargaPerKg = 0.0;
            this.totalPembayaran = 0.0;
            return;
        }

        switch (tembakau.getGrade()) {
            case "GRADE A (SUPER)":
                this.hargaPerKg = 85_000.0; // Rp 85.000 / kg
                break;
            case "GRADE B (MEDIUM)":
                this.hargaPerKg = 60_000.0; // Rp 60.000 / kg
                break;
            case "GRADE C (RENDAH)":
                this.hargaPerKg = 35_000.0; // Rp 35.000 / kg
                break;
            default:
                this.hargaPerKg = 0.0;
                break;
        }
        this.totalPembayaran = this.hargaPerKg * tembakau.getBeratKg();
    }

    /**
     * Business Method: Memverifikasi keabsahan transaksi.
     * Jika tembakau layak mutu, transaksi diverifikasi dan riwayat setoran petani otomatis diperbarui.
     */
    public boolean verifikasiTransaksi() {
        if (!tembakau.isLayakProduksi()) {
            System.out.printf("[GAGAL VERIFIKASI] Transaksi %s ditolak karena mutu tembakau tidak memenuhi standar.%n", idTransaksi);
            this.terverifikasi = false;
            return false;
        }

        this.terverifikasi = true;
        // Memperbarui riwayat akumulasi setoran pada entitas Petani terkait
        petani.tambahRiwayatSetoran(tembakau.getBeratKg());
        System.out.printf("[SUKSES] Transaksi %s berhasil diverifikasi dan disahkan.%n", idTransaksi);
        return true;
    }

    public String getIdTransaksi() { return idTransaksi; }
    public Petani getPetani() { return petani; }
    public Tembakau getTembakau() { return tembakau; }
    public double getHargaPerKg() { return hargaPerKg; }
    public double getTotalPembayaran() { return totalPembayaran; }
    public boolean isTerverifikasi() { return terverifikasi; }

    public void cetakFakturNota() {
        System.out.println("==================================================");
        System.out.println("              FAKTUR PEMBAYARAN PASOK             ");
        System.out.println("==================================================");
        System.out.println("No. Transaksi      : " + idTransaksi);
        System.out.println("Nama Petani        : " + petani.getNama() + " (" + petani.getAsalDesa() + ")");
        System.out.println("Batch Tembakau     : " + tembakau.getKodeBatch() + " [" + tembakau.getVarietas() + "]");
        System.out.println("Kualitas Grade     : " + tembakau.getGrade());
        System.out.printf("Berat Disetor      : %.2f kg%n", tembakau.getBeratKg());
        System.out.printf("Harga Satuan       : Rp %,.2f / kg%n", hargaPerKg);
        System.out.println("--------------------------------------------------");
        System.out.printf("TOTAL PEMBAYARAN   : Rp %,.2f%n", totalPembayaran);
        System.out.println("Status Verifikasi  : " + (terverifikasi ? "LUNAS & TERVERIFIKASI" : "MENUNGGU VERIFIKASI / DITOLAK"));
        System.out.println("==================================================");
    }
}
