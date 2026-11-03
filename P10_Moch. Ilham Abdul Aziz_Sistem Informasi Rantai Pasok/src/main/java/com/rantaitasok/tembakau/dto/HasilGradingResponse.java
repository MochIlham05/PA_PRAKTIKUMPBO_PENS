package com.rantaitasok.tembakau.dto;

import com.rantaitasok.tembakau.entity.HasilGrading;
import com.rantaitasok.tembakau.model.enums.GradeTembakau;

import java.math.BigDecimal;

/**
 * DTO Response untuk menyajikan data HasilGrading ke antarmuka klien/mobile.
 */
public record HasilGradingResponse(
    Long id,
    GradeTembakau grade,
    String deskripsiGrade,
    BigDecimal beratKering,
    BigDecimal hargaPerKg,
    BigDecimal totalValuasi,
    String catatanKualitas
) {
    public static HasilGradingResponse fromEntity(HasilGrading grading) {
        if (grading == null) return null;
        return new HasilGradingResponse(
            grading.getId(),
            grading.getGrade(),
            grading.getGrade().getDeskripsi(),
            grading.getBeratKering(),
            grading.getHargaPerKg(),
            grading.hitungTotalValuasiKering(),
            grading.getCatatanKualitas()
        );
    }
}
