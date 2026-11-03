package com.rantaitasok.tembakau.entity;

import com.rantaitasok.tembakau.model.enums.GradeTembakau;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * OntoUML Stereotype: «Quality» / «Mode»
 * HasilGrading merepresentasikan kualitas intrinsik dari tembakau hasil panen setelah proses pemeraman & perajangan/pengeringan.
 * Eksistensinya bersifat Inherent (komposisi penuh) terhadap entitas Panen.
 */
@Entity
@Table(name = "hasil_grading")
public class HasilGrading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false, length = 10)
    private GradeTembakau grade;

    @Column(name = "berat_kering", nullable = false, precision = 10, scale = 2)
    private BigDecimal beratKering;

    @Column(name = "harga_per_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal hargaPerKg;

    @Column(name = "catatan_kualitas", columnDefinition = "TEXT")
    private String catatanKualitas;

    /**
     * Relasi Komposisi dengan Panen.
     * HasilGrading tidak dapat berdiri sendiri tanpa adanya Panen penampungnya.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "panen_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_grading_panen"))
    private Panen panen;

    protected HasilGrading() {
        // Diperlukan oleh JPA runtime
    }

    /**
     * Package-private constructor: Memastikan pembentukan HasilGrading dilakukan secara sah melalui factory method pada Panen.
     */
    HasilGrading(Panen panen, GradeTembakau grade, BigDecimal beratKering, BigDecimal hargaPerKg, String catatanKualitas) {
        if (panen == null) {
            throw new IllegalArgumentException("HasilGrading tidak dapat dibuat tanpa instansiasi Panen (Composition constraint).");
        }
        this.panen = panen;
        this.grade = Objects.requireNonNull(grade, "Grade tembakau tidak boleh null.");
        setBeratKering(beratKering);
        
        if (hargaPerKg == null || hargaPerKg.compareTo(BigDecimal.ZERO) <= 0) {
            this.hargaPerKg = tentukanHargaBerdasarkanGrade();
        } else {
            this.hargaPerKg = hargaPerKg;
        }
        
        this.catatanKualitas = catatanKualitas;
    }

    // --- Domain Business Responsibilities ---

    /**
     * Menetapkan harga patokan per kg secara otomatis berdasarkan standar mutu pasar tembakau Desa Prancak.
     */
    public BigDecimal tentukanHargaBerdasarkanGrade() {
        if (this.grade == null) {
            throw new IllegalStateException("Grade tembakau belum ditentukan.");
        }
        return this.grade.getHargaReferensiPerKg();
    }

    /**
     * Menghitung nilai valuasi total hasil panen kering (beratKering * hargaPerKg).
     */
    public BigDecimal hitungTotalValuasiKering() {
        if (this.beratKering == null || this.hargaPerKg == null) {
            return BigDecimal.ZERO;
        }
        return this.beratKering.multiply(this.hargaPerKg).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Validasi invariant: Berat kering tembakau hasil rajangan tidak boleh melebihi berat basahnya.
     */
    public void validasiKesesuaianBobot() {
        if (this.panen != null && this.panen.getBeratBasah() != null) {
            if (this.beratKering.compareTo(this.panen.getBeratBasah()) > 0) {
                throw new IllegalStateException(
                    String.format("Pelanggaran Invariant: Berat kering (%s kg) tidak boleh lebih besar dari berat basah (%s kg).",
                            this.beratKering, this.panen.getBeratBasah())
                );
            }
        }
    }

    // --- Getter & Setter Enkapsulasi ---

    public Long getId() {
        return id;
    }

    public GradeTembakau getGrade() {
        return grade;
    }

    public void setGrade(GradeTembakau grade) {
        this.grade = Objects.requireNonNull(grade, "Grade tembakau tidak boleh null.");
    }

    public BigDecimal getBeratKering() {
        return beratKering;
    }

    public void setBeratKering(BigDecimal beratKering) {
        if (beratKering == null || beratKering.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Berat kering harus lebih besar dari 0 kg.");
        }
        this.beratKering = beratKering;
        validasiKesesuaianBobot();
    }

    public BigDecimal getHargaPerKg() {
        return hargaPerKg;
    }

    public void setHargaPerKg(BigDecimal hargaPerKg) {
        if (hargaPerKg == null || hargaPerKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Harga per kg harus bernilai positif.");
        }
        this.hargaPerKg = hargaPerKg;
    }

    public String getCatatanKualitas() {
        return catatanKualitas;
    }

    public void setCatatanKualitas(String catatanKualitas) {
        this.catatanKualitas = catatanKualitas;
    }

    public Panen getPanen() {
        return panen;
    }

    void internalSetPanen(Panen panen) {
        this.panen = panen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HasilGrading other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "HasilGrading{" +
                "id=" + id +
                ", grade=" + grade +
                ", beratKering=" + beratKering +
                ", hargaPerKg=" + hargaPerKg +
                '}';
    }
}
