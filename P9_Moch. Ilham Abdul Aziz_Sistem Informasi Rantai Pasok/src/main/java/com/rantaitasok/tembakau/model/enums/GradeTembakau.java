package com.rantaitasok.tembakau.model.enums;

import java.math.BigDecimal;

/**
 * Representasi Kualitas / Grade Tembakau Prancak (Sumenep).
 * Memiliki baseline harga pasar referensi per grade.
 */
public enum GradeTembakau {
    A("Grade A - Daun Puncak (Top Quality / Rajangan Halus Aromatik)", new BigDecimal("95000")),
    B("Grade B - Daun Tengah Atas (Warna Kuning Emas, Elastis)", new BigDecimal("80000")),
    C("Grade C - Daun Tengah (Standar Pabrikan)", new BigDecimal("65000")),
    D("Grade D - Daun Bawah (Kualitas Menengah ke Bawah)", new BigDecimal("45000")),
    E("Grade E - Daun Pasir / Sortiran Rendah", new BigDecimal("30000"));

    private final String deskripsi;
    private final BigDecimal hargaReferensiPerKg;

    GradeTembakau(String deskripsi, BigDecimal hargaReferensiPerKg) {
        this.deskripsi = deskripsi;
        this.hargaReferensiPerKg = hargaReferensiPerKg;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public BigDecimal getHargaReferensiPerKg() {
        return hargaReferensiPerKg;
    }
}
