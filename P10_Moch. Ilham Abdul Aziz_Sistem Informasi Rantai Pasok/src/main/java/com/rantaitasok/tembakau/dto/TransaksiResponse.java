package com.rantaitasok.tembakau.dto;

import com.rantaitasok.tembakau.entity.Transaksi;
import com.rantaitasok.tembakau.model.enums.GradeTembakau;
import com.rantaitasok.tembakau.model.enums.StatusTransaksi;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response untuk struk dan detail informasi transaksi perdagangan tembakau.
 */
public record TransaksiResponse(
    Long id,
    LocalDateTime tanggalTransaksi,
    BigDecimal totalHarga,
    StatusTransaksi status,
    String catatanTransaksi,
    Long panenId,
    String namaPetani,
    String lokasiLahan,
    GradeTembakau grade,
    String deskripsiGrade,
    BigDecimal beratKeringKg,
    BigDecimal hargaPerKg,
    Long pengepulId,
    String namaPengepul,
    String nomorTeleponPengepul
) {
    public static TransaksiResponse fromEntity(Transaksi transaksi) {
        if (transaksi == null) return null;

        var panen = transaksi.getPanen();
        var grading = panen != null ? panen.getHasilGrading() : null;
        var pengepul = transaksi.getPengepul();

        return new TransaksiResponse(
            transaksi.getId(),
            transaksi.getTanggalTransaksi(),
            transaksi.getTotalHarga(),
            transaksi.getStatus(),
            transaksi.getCatatanTransaksi(),
            panen != null ? panen.getId() : null,
            panen != null && panen.getPetani() != null ? panen.getPetani().getNama() : null,
            panen != null && panen.getLahan() != null ? panen.getLahan().getLokasiGps() : null,
            grading != null ? grading.getGrade() : null,
            grading != null ? grading.getGrade().getDeskripsi() : null,
            grading != null ? grading.getBeratKering() : null,
            grading != null ? grading.getHargaPerKg() : null,
            pengepul != null ? pengepul.getId() : null,
            pengepul != null ? pengepul.getNama() : null,
            pengepul != null ? pengepul.getNomorTelepon() : null
        );
    }
}
