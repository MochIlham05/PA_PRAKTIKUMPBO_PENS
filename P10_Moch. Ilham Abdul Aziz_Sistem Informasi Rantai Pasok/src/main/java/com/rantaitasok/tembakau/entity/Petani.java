package com.rantaitasok.tembakau.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * OntoUML Stereotype: «Kind»
 * Mewakili entitas Petani sebagai autonomous agent fundamental dalam rantai pasok tembakau Desa Prancak.
 * Bertindak sebagai pemilik identitas esensial (rigid concept).
 */
@Entity
@Table(
    name = "petani",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_petani_nik", columnNames = "nik")
    }
)
public class Petani {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama", nullable = false, length = 100)
    private String nama;

    @Column(name = "nik", nullable = false, length = 16)
    private String nik;

    @Column(name = "alamat", nullable = false, columnDefinition = "TEXT")
    private String alamat;

    /**
     * Association One-to-Many dengan Lahan.
     * Secara OntoUML / domain rule, seorang petani terdaftar memiliki relasi 1..* ke Lahan garapan/milik.
     */
    @OneToMany(
        mappedBy = "petani",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.LAZY
    )
    private List<Lahan> daftarLahan = new ArrayList<>();

    /**
     * Association One-to-Many dengan Panen.
     */
    @OneToMany(
        mappedBy = "petani",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    private List<Panen> daftarPanen = new ArrayList<>();

    protected Petani() {
        // Diperlukan oleh JPA runtime
    }

    public Petani(String nama, String nik, String alamat) {
        setNama(nama);
        setNik(nik);
        setAlamat(alamat);
    }

    // --- Domain Business Responsibilities ---

    /**
     * Mendaftarkan kepemilikan lahan baru untuk petani ini.
     * Menjamin konsistensi referensi bidirectional kedua belah pihak.
     */
    public void tambahLahan(Lahan lahan) {
        Objects.requireNonNull(lahan, "Objek Lahan tidak boleh bernilai null.");
        if (!this.daftarLahan.contains(lahan)) {
            this.daftarLahan.add(lahan);
            lahan.assignPetani(this);
        }
    }

    /**
     * Menghapus lahan dari kepemilikan petani dengan validasi invariant.
     */
    public void hapusLahan(Lahan lahan) {
        if (lahan == null) return;
        if (this.daftarLahan.contains(lahan)) {
            this.daftarLahan.remove(lahan);
            lahan.detachPetani();
        }
    }

    /**
     * Menambahkan catatan panen baru yang dihasilkan oleh petani ini.
     */
    public void catatPanen(Panen panen) {
        Objects.requireNonNull(panen, "Objek Panen tidak boleh null.");
        if (!this.daftarPanen.contains(panen)) {
            this.daftarPanen.add(panen);
            panen.assignPetani(this);
        }
    }

    /**
     * Business calculation: Menghitung akumulasi total luas lahan garapan petani (dalam hektar).
     */
    public BigDecimal hitungTotalLuasLahan() {
        return daftarLahan.stream()
                .map(Lahan::getLuasHektar)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Business calculation: Menghitung total berat basah tembakau yang pernah dipanen oleh petani.
     */
    public BigDecimal hitungTotalProduksiBasahKg() {
        return daftarPanen.stream()
                .map(Panen::getBeratBasah)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- Getter dan Setter dengan Validasi & Enkapsulasi Ketat ---

    public Long getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama petani tidak boleh kosong.");
        }
        this.nama = nama.trim();
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        if (nik == null || !nik.matches("^\\d{16}$")) {
            throw new IllegalArgumentException("NIK harus berupa 16 digit angka valid.");
        }
        this.nik = nik;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        if (alamat == null || alamat.trim().isEmpty()) {
            throw new IllegalArgumentException("Alamat petani tidak boleh kosong.");
        }
        this.alamat = alamat.trim();
    }

    /**
     * Mengembalikan list unmodifiable untuk mencegah modifikasi state internal secara langsung dari luar.
     */
    public List<Lahan> getDaftarLahan() {
        return Collections.unmodifiableList(daftarLahan);
    }

    public List<Panen> getDaftarPanen() {
        return Collections.unmodifiableList(daftarPanen);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Petani other)) return false;
        return nik != null && nik.equals(other.nik);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nik);
    }

    @Override
    public String toString() {
        return "Petani{" +
                "id=" + id +
                ", nama='" + nama + '\'' +
                ", nik='" + nik + '\'' +
                ", alamat='" + alamat + '\'' +
                '}';
    }
}
