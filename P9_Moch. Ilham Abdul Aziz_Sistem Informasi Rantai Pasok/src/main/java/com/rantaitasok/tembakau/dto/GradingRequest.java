package com.rantaitasok.tembakau.dto;

import com.rantaitasok.tembakau.model.enums.GradeTembakau;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload untuk memasukkan hasil grading kualitas tembakau.
 */
public record GradingRequest(
    @NotNull(message = "Grade kualitas tembakau wajib dipilih (A, B, C, D, atau E).")
    GradeTembakau grade,

    @Positive(message = "Berat kering hasil rajangan harus lebih besar dari 0 kg.")
    double beratKering,

    double hargaPerKg
) {}
