package com.rantaitasok.tembakau.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload untuk mencatat peristiwa panen tembakau baru.
 */
public record PanenRequest(
    @NotNull(message = "ID Petani wajib diisi.")
    Long petaniId,

    @NotNull(message = "ID Lahan wajib diisi.")
    Long lahanId,

    @Positive(message = "Berat basah harus lebih besar dari 0 kg.")
    double beratBasah
) {}
