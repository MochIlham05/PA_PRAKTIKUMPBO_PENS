package com.rantaitasok.tembakau.entity;

import com.rantaitasok.tembakau.model.enums.StatusTransaksi;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * OntoUML Stereotype: «Relator»
 * Transaksi mengikat (mediates) hubungan kontraktual perdagangan tembakau antara
 * pemilik Panen (Petani) dan pembeli (Pengepul).
 * 
 * Aturan Bisnis Esensial:
 * 1. Transaksi hanya sah jika Panen telah melalui proses grading mutu (HasilGrading != null).
 * 2. Total harga dihitung secara deterministik dari beratKering * hargaPerKg pada HasilGrading.
 */
@Entity
@Table(name = "transaksi")
public class Transaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tanggal_transaksi", nullable = false)
    private LocalDateTime tanggalTransaksi;

    @Column(name = "total_harga", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHarga;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private StatusTransaksi status;

    /**
     * Panen yang ditransaksikan.
     * Bersifat One-to-One dengan unique constraint (1 batch hasil panen hanya dapat ditransaksikan pada satu kesepakatan jual-beli).
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "panen_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_transaksi_panen"))
    private Panen panen;

    /**
     * Pengepul pembeli komoditas.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pengepul_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaksi_pengepul"))
    private Pengepul pengepul;

    @Column(name = "catatan_transaksi", columnDefinition = "TEXT")
    private String catatanTransaksi;

    protected Transaksi() {
        // Diperlukan oleh JPA runtime
    }

    public Transaksi(Panen panen, Pengepul pengepul, LocalDateTime tanggalTransaksi) {
        validasiKelayakanPanen(panen);
        this.panen = panen;
        assignPengepul(pengepul);
        setTanggalTransaksi(tanggalTransaksi);
        this.status = StatusTransaksi.PENDING;
        kalkulasiTotalHarga();
    }

    // --- Domain Business Responsibilities ---

    /**
     * Memverifikasi aturan bisnis ontologis: Objek Panen wajib memiliki HasilGrading sebelum masuk ke ranah perdagangan.
     */
    public final void validasiKelayakanPanen(Panen panen) {
        if (panen == null) {
            throw new IllegalArgumentException("Transaksi tidak dapat dilakukan tanpa objek Panen.");
        }
        if (!panen.isSiapDitransaksikan()) {
            throw new IllegalStateException(
                "Aturan Bisnis: Tembakau hasil panen belum melalui proses Grading mutu. Transaksi ditolak."
            );
        }
        HasilGrading grading = panen.getHasilGrading();
        if (grading.getBeratKering() == null || grading.getHargaPerKg() == null) {
            throw new IllegalStateException("Aturan Bisnis: Parameter HasilGrading (berat kering & harga) belum lengkap.");
        }
    }

    /**
     * Menghitung total nilai transaksi berdasarkan kuantitas tembakau kering dan harga kesepakatan mutu grading.
     * Total = Panen.HasilGrading.beratKering * Panen.HasilGrading.hargaPerKg
     */
    public BigDecimal kalkulasiTotalHarga() {
        if (this.panen == null || this.panen.getHasilGrading() == null) {
            throw new IllegalStateException("Tidak dapat menghitung total harga: Data panen atau grading belum tersedia.");
        }
        HasilGrading grading = this.panen.getHasilGrading();
        this.totalHarga = grading.getBeratKering()
                .multiply(grading.getHargaPerKg())
                .setScale(2, RoundingMode.HALF_UP);
        return this.totalHarga;
    }

    /**
     * State transition: Menyelesaikan transaksi dan mengubah status menjadi LUNAS.
     */
    public void selesaikanPembayaran() {
        if (this.status == StatusTransaksi.LUNAS) {
            throw new IllegalStateException("Transaksi sudah berstatus LUNAS.");
        }
        if (this.status == StatusTransaksi.BATAL) {
            throw new IllegalStateException("Tidak dapat melunasi transaksi yang telah dibatalkan.");
        }
        this.status = StatusTransaksi.LUNAS;
    }

    /**
     * State transition: Membatalkan transaksi.
     */
    public void batalkanTransaksi(String alasan) {
        if (this.status == StatusTransaksi.LUNAS) {
            throw new IllegalStateException("Transaksi yang sudah LUNAS tidak dapat dibatalkan secara sepihak.");
        }
        this.status = StatusTransaksi.BATAL;
        this.catatanTransaksi = (this.catatanTransaksi != null ? this.catatanTransaksi + " | " : "") + 
                                "Dibatalkan: " + alasan;
    }

    public void assignPengepul(Pengepul pengepul) {
        if (pengepul == null) {
            throw new IllegalArgumentException("Transaksi wajib memiliki pihak Pengepul pembeli.");
        }
        this.pengepul = pengepul;
        pengepul.internalAddTransaksi(this);
    }

    // --- Getter & Setter Enkapsulasi ---

    public Long getId() {
        return id;
    }

    public LocalDateTime getTanggalTransaksi() {
        return tanggalTransaksi;
    }

    public void setTanggalTransaksi(LocalDateTime tanggalTransaksi) {
        if (tanggalTransaksi == null) {
            throw new IllegalArgumentException("Tanggal transaksi wajib ditentukan.");
        }
        this.tanggalTransaksi = tanggalTransaksi;
    }

    public BigDecimal getTotalHarga() {
        return totalHarga;
    }

    public StatusTransaksi getStatus() {
        return status;
    }

    public Panen getPanen() {
        return panen;
    }

    public Pengepul getPengepul() {
        return pengepul;
    }

    public String getCatatanTransaksi() {
        return catatanTransaksi;
    }

    public void setCatatanTransaksi(String catatanTransaksi) {
        this.catatanTransaksi = catatanTransaksi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaksi other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Transaksi{" +
                "id=" + id +
                ", tanggalTransaksi=" + tanggalTransaksi +
                ", totalHarga=" + totalHarga +
                ", status=" + status +
                ", panenId=" + (panen != null ? panen.getId() : null) +
                '}';
    }
}
