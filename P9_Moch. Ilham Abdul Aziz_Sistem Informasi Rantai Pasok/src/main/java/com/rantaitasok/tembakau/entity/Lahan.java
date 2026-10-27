package com.rantaitasok.tembakau.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * OntoUML Stereotype: «Subkind» / «Role» (Land Parcel in role of Agricultural Tobacco Land)
 * Lahan merepresentasikan bidang tanah produktif di Desa Prancak yang digarap untuk tembakau.
 * Memiliki ketergantungan eksistensial relasional: Tepat dimiliki oleh satu Petani (multiplicity 1).
 */
@Entity
@Table(name = "lahan")
public class Lahan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "luas_hektar", nullable = false, precision = 8, scale = 4)
    private BigDecimal luasHektar;

    @Column(name = "lokasi_gps", nullable = false, length = 100)
    private String lokasiGps;

    /**
     * Relasi Many-to-One ke Petani.
     * Constraint OntoUML: Multiplicity 1..1 pada sisi Petani (setiap lahan wajib terasosiasi dengan tepat satu Petani).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "petani_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lahan_petani"))
    private Petani petani;

    protected Lahan() {
        // Diperlukan oleh JPA runtime
    }

    public Lahan(BigDecimal luasHektar, String lokasiGps, Petani petani) {
        setLuasHektar(luasHektar);
        setLokasiGps(lokasiGps);
        assignPetani(petani);
    }

    // --- Domain Business Responsibilities ---

    /**
     * Memperbarui relasi kepemilikan lahan ke Petani baru secara sinkron.
     */
    public void assignPetani(Petani petani) {
        if (petani == null) {
            throw new IllegalArgumentException("Lahan wajib memiliki pemilik (Petani tidak boleh null).");
        }
        this.petani = petani;
    }

    void detachPetani() {
        this.petani = null;
    }

    /**
     * Menghitung estimasi rata-rata kapasitas produksi daun tembakau basah (kg).
     * Benchmark agronomis Desa Prancak: ~7.500 kg per hektar panen basah.
     */
    public BigDecimal estimasiKapasitasProduksiBasahKg() {
        if (this.luasHektar == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal produktivitasRataRataPerHektar = new BigDecimal("7500");
        return this.luasHektar.multiply(produktivitasRataRataPerHektar).setScale(2, RoundingMode.HALF_UP);
    }

    // --- Getter & Setter dengan Validasi Ketat ---

    public Long getId() {
        return id;
    }

    public BigDecimal getLuasHektar() {
        return luasHektar;
    }

    public void setLuasHektar(BigDecimal luasHektar) {
        if (luasHektar == null || luasHektar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Luas lahan harus bernilai lebih dari 0 hektar.");
        }
        this.luasHektar = luasHektar;
    }

    public String getLokasiGps() {
        return lokasiGps;
    }

    public void setLokasiGps(String lokasiGps) {
        if (lokasiGps == null || lokasiGps.trim().isEmpty()) {
            throw new IllegalArgumentException("Lokasi GPS / koordinat lahan wajib diisi.");
        }
        this.lokasiGps = lokasiGps.trim();
    }

    public Petani getPetani() {
        return petani;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lahan other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Lahan{" +
                "id=" + id +
                ", luasHektar=" + luasHektar +
                ", lokasiGps='" + lokasiGps + '\'' +
                ", petaniId=" + (petani != null ? petani.getId() : null) +
                '}';
    }
}
