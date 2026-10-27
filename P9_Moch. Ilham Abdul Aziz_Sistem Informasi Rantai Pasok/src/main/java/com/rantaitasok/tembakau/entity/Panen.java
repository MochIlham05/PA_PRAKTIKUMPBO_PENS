package com.rantaitasok.tembakau.entity;

import com.rantaitasok.tembakau.model.enums.GradeTembakau;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * OntoUML Stereotype: «Relator»
 * Panen merepresentasikan peristiwa agrikultur dan relasi pengikatan antara Petani, Lahan garapan,
 * serta hasil fisik panen tembakau daun basah.
 * Panen bertindak sebagai aggregate root bagi entitas HasilGrading (Composition).
 */
@Entity
@Table(name = "panen")
public class Panen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tanggal_panen", nullable = false)
    private LocalDate tanggalPanen;

    @Column(name = "berat_basah", nullable = false, precision = 10, scale = 2)
    private BigDecimal beratBasah;

    /**
     * Relasi Many-to-One dengan Petani (pemilik hasil panen).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "petani_id", nullable = false, foreignKey = @ForeignKey(name = "fk_panen_petani"))
    private Petani petani;

    /**
     * Relasi Many-to-One dengan Lahan tempat panen berlangsung.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lahan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_panen_lahan"))
    private Lahan lahan;

    /**
     * Relasi Composition (One-to-One) dengan HasilGrading.
     * Menggunakan CascadeType.ALL dan orphanRemoval = true untuk mencerminkan semantik komposisi OntoUML:
     * Siklus hidup HasilGrading terikat penuh pada eksistensi Panen.
     */
    @OneToOne(
        mappedBy = "panen",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private HasilGrading hasilGrading;

    protected Panen() {
        // Diperlukan oleh JPA runtime
    }

    public Panen(LocalDate tanggalPanen, BigDecimal beratBasah, Petani petani, Lahan lahan) {
        setTanggalPanen(tanggalPanen);
        setBeratBasah(beratBasah);
        assignPetani(petani);
        assignLahan(lahan);
    }

    // --- Domain Business Responsibilities ---

    /**
     * Factory & Encapsulation Method untuk membuat HasilGrading.
     * Menggaransi bahwa HasilGrading tidak pernah diciptakan secara terisolasi tanpa keberadaan Panen.
     */
    public HasilGrading lakukanGrading(GradeTembakau grade, BigDecimal beratKering, BigDecimal hargaPerKg, String catatanKualitas) {
        if (this.hasilGrading != null) {
            throw new IllegalStateException("Panen ini sudah pernah di-grading. Gunakan update grading jika ingin merevisi.");
        }
        HasilGrading gradingBaru = new HasilGrading(this, grade, beratKering, hargaPerKg, catatanKualitas);
        gradingBaru.validasiKesesuaianBobot();
        this.hasilGrading = gradingBaru;
        return this.hasilGrading;
    }

    /**
     * Menghapus hasil grading (orphan removal otomatis via JPA).
     */
    public void batalkanGrading() {
        if (this.hasilGrading != null) {
            this.hasilGrading.internalSetPanen(null);
            this.hasilGrading = null;
        }
    }

    /**
     * Cek kesiapan panen: Panen hanya dapat diperdagangkan jika sudah memiliki kualitas terverifikasi (HasilGrading).
     */
    public boolean isSiapDitransaksikan() {
        return this.hasilGrading != null;
    }

    /**
     * Menghitung persentase rendemen tembakau kering terhadap berat basah (drying yield ratio).
     * Formula: (beratKering / beratBasah) * 100%
     */
    public BigDecimal hitungPersentaseRendemen() {
        if (this.hasilGrading == null || this.hasilGrading.getBeratKering() == null || this.beratBasah.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return this.hasilGrading.getBeratKering()
                .divide(this.beratBasah, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // --- Helper Relasi Bidirectional & Invariant ---

    public void assignPetani(Petani petani) {
        if (petani == null) {
            throw new IllegalArgumentException("Panen wajib diasosiasikan dengan Petani penanggung jawab.");
        }
        this.petani = petani;
    }

    public void assignLahan(Lahan lahan) {
        if (lahan == null) {
            throw new IllegalArgumentException("Panen wajib diasosiasikan dengan objek Lahan lokasi panen.");
        }
        // Invariant check: Memastikan lahan yang dipanen memang milik petani yang bersangkutan
        if (this.petani != null && lahan.getPetani() != null && !this.petani.equals(lahan.getPetani())) {
            throw new IllegalStateException("Pelanggaran Integritas: Petani tidak terdaftar sebagai pemilik dari lahan ini.");
        }
        this.lahan = lahan;
    }

    // --- Getter & Setter Enkapsulasi ---

    public Long getId() {
        return id;
    }

    public LocalDate getTanggalPanen() {
        return tanggalPanen;
    }

    public void setTanggalPanen(LocalDate tanggalPanen) {
        if (tanggalPanen == null) {
            throw new IllegalArgumentException("Tanggal panen wajib diisi.");
        }
        if (tanggalPanen.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal panen tidak boleh mendahului tanggal hari ini.");
        }
        this.tanggalPanen = tanggalPanen;
    }

    public BigDecimal getBeratBasah() {
        return beratBasah;
    }

    public void setBeratBasah(BigDecimal beratBasah) {
        if (beratBasah == null || beratBasah.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Berat basah harus lebih besar dari 0 kg.");
        }
        this.beratBasah = beratBasah;
        if (this.hasilGrading != null) {
            this.hasilGrading.validasiKesesuaianBobot();
        }
    }

    public Petani getPetani() {
        return petani;
    }

    public Lahan getLahan() {
        return lahan;
    }

    public HasilGrading getHasilGrading() {
        return hasilGrading;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Panen other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Panen{" +
                "id=" + id +
                ", tanggalPanen=" + tanggalPanen +
                ", beratBasah=" + beratBasah +
                ", siapDitransaksikan=" + isSiapDitransaksikan() +
                '}';
    }
}
