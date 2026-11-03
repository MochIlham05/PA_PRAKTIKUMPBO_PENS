package com.rantaitasok.tembakau.modul10;

import com.rantaitasok.tembakau.modul10.exception.MutuTembakauDitolakException;

/**
 * Entitas yang merepresentasikan Transaksi Penerimaan dan Pembayaran Pasokan Tembakau.
 * Menghubungkan Petani dan Tembakau melalui hubungan Asosiasi Objek.
 * Menerapkan prinsip "Pemeriksaan Sebelum Mutasi State" dengan custom exception handling.
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

    /**
     * Constructor TransaksiPasok dengan validasi asosiasi objek.
     */
    public TransaksiPasok(String idTransaksi, Petani petani, Tembakau tembakau) {
        if (idTransaksi == null || idTransaksi.trim().isEmpty()) {
            throw new IllegalArgumentException("Validasi Gagal: ID Transaksi tidak boleh kosong atau null.");
        }
        if (petani == null) {
            throw new IllegalArgumentException("Validasi Gagal: Objek Petani transaksi tidak boleh bernilai null.");
        }
        if (tembakau == null) {
            throw new IllegalArgumentException("Validasi Gagal: Objek Tembakau transaksi tidak boleh bernilai null.");
        }

        this.idTransaksi = idTransaksi.trim();
        this.petani = petani;
        this.tembakau = tembakau;
        this.terverifikasi = false; // Status awal transaksi: belum diverifikasi

        // Hitung estimasi harga awal berdasarkan mutu tembakau
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
     * Business Method: Memverifikasi keabsahan transaksi pasokan tembakau.
     * Menerapkan prinsip "Pemeriksaan Sebelum Mutasi State (Check Before Mutate)":
     * 1. Validasi idempotensi: Memastikan transaksi belum pernah diverifikasi sebelumnya.
     * 2. Validasi kelayakan mutu: Melempar MutuTembakauDitolakException jika kadar air > 18%.
     * 3. Jika validasi gagal, state petani (riwayat setoran) sama sekali TIDAK dipanggil/berubah.
     * 
     * @throws MutuTembakauDitolakException jika mutu tembakau berstatus REJECTED (> 18.0%).
     * @throws IllegalStateException jika transaksi sudah diverifikasi sebelumnya.
     */
    public void verifikasiTransaksi() throws MutuTembakauDitolakException, IllegalStateException {
        // Guard 1: Cek apakah transaksi sudah pernah diverifikasi
        if (this.terverifikasi) {
            throw new IllegalStateException(
                String.format("Operasi Ditolak: Transaksi '%s' sudah dalam status terverifikasi sebelumnya.", idTransaksi)
            );
        }

        // Guard 2: Cek kelayakan mutu fisik daun tembakau
        if (!tembakau.isLayakProduksi() || tembakau.getKadarAirPersen() > Tembakau.BATAS_KADAR_AIR_MAKS) {
            // Operasi dibatalkan dengan checked exception sebelum mutasi state
            throw new MutuTembakauDitolakException(
                tembakau.getKodeBatch(),
                tembakau.getVarietas(),
                tembakau.getKadarAirPersen(),
                Tembakau.BATAS_KADAR_AIR_MAKS,
                tembakau.getGrade()
            );
        }

        // State mutasi: Hanya dieksekusi setelah seluruh pemeriksaan mutu berhasil lolos
        this.terverifikasi = true;

        // Memperbarui riwayat setoran pada entitas Petani terkait
        petani.tambahRiwayatSetoran(tembakau.getBeratKg());
        System.out.printf("[SUKSES TRANSAKSI] Transaksi '%s' lolos verifikasi mutu dan disahkan secara resmi.%n", idTransaksi);
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

