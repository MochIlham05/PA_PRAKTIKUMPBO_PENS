package com.rantaitasok.tembakau.dto;

import com.rantaitasok.tembakau.entity.Panen;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO Response untuk data Panen beserta status kelayakan transaksinya.
 */
public record PanenResponse(
    Long id,
    LocalDate tanggalPanen,
    BigDecimal beratBasah,
    Long petaniId,
    String namaPetani,
    Long lahanId,
    String lokasiGps,
    boolean siapDitransaksikan,
    BigDecimal persentaseRendemen,
    HasilGradingResponse hasilGrading
) {
    public static PanenResponse fromEntity(Panen panen) {
        if (panen == null) return null;
        return new PanenResponse(
            panen.getId(),
            panen.getTanggalPanen(),
            panen.getBeratBasah(),
            panen.getPetani() != null ? panen.getPetani().getId() : null,
            panen.getPetani() != null ? panen.getPetani().getNama() : null,
            panen.getLahan() != null ? panen.getLahan().getId() : null,
            panen.getLahan() != null ? panen.getLahan().getLokasiGps() : null,
            panen.isSiapDitransaksikan(),
            panen.hitungPersentaseRendemen(),
            panen.getHasilGrading() != null ? HasilGradingResponse.fromEntity(panen.getHasilGrading()) : null
        );
    }
}
