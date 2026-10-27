package com.rantaitasok.tembakau.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * OntoUML Stereotype: «Kind» / «Role» (Agent).
 * Pengepul berperan sebagai pembeli/pedagang pengumpul dalam rantai pasok tembakau.
 */
@Entity
@Table(name = "pengepul")
public class Pengepul {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama", nullable = false, length = 100)
    private String nama;

    @Column(name = "nomor_izin_usaha", length = 50)
    private String nomorIzinUsaha;

    @Column(name = "nomor_telepon", nullable = false, length = 20)
    private String nomorTelepon;

    @Column(name = "alamat", nullable = false, columnDefinition = "TEXT")
    private String alamat;

    @OneToMany(mappedBy = "pengepul", fetch = FetchType.LAZY)
    private List<Transaksi> riwayatTransaksi = new ArrayList<>();

    protected Pengepul() {
        // Required by JPA specification
    }

    public Pengepul(String nama, String nomorTelepon, String alamat, String nomorIzinUsaha) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama pengepul tidak boleh kosong.");
        }
        if (nomorTelepon == null || nomorTelepon.trim().isEmpty()) {
            throw new IllegalArgumentException("Nomor telepon pengepul wajib diisi.");
        }
        this.nama = nama.trim();
        this.nomorTelepon = nomorTelepon.trim();
        this.alamat = alamat != null ? alamat.trim() : "";
        this.nomorIzinUsaha = nomorIzinUsaha;
    }

    public Long getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama pengepul tidak boleh kosong.");
        }
        this.nama = nama.trim();
    }

    public String getNomorIzinUsaha() {
        return nomorIzinUsaha;
    }

    public void setNomorIzinUsaha(String nomorIzinUsaha) {
        this.nomorIzinUsaha = nomorIzinUsaha;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        if (nomorTelepon == null || nomorTelepon.trim().isEmpty()) {
            throw new IllegalArgumentException("Nomor telepon tidak boleh kosong.");
        }
        this.nomorTelepon = nomorTelepon.trim();
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public List<Transaksi> getRiwayatTransaksi() {
        return Collections.unmodifiableList(riwayatTransaksi);
    }

    void internalAddTransaksi(Transaksi transaksi) {
        this.riwayatTransaksi.add(transaksi);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pengepul other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
