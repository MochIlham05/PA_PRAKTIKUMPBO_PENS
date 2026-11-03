package com.rantaitasok.tembakau.controller;

import com.rantaitasok.tembakau.dto.*;
import com.rantaitasok.tembakau.entity.HasilGrading;
import com.rantaitasok.tembakau.entity.Panen;
import com.rantaitasok.tembakau.service.PanenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller untuk mengelola endpoint siklus panen dan grading tembakau Desa Prancak.
 * Terbuka untuk konsumsi mobile apps (SmartLeaf App) maupun web client.
 */
@RestController
@RequestMapping("/api/panen")
public class PanenController {

    private final PanenService panenService;

    public PanenController(PanenService panenService) {
        this.panenService = panenService;
    }

    /**
     * Endpoint 1: Menyimpan data panen baru.
     * POST /api/panen
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PanenResponse>> catatPanen(@Valid @RequestBody PanenRequest request) {
        Panen panen = panenService.catatPanenBaru(
                request.petaniId(),
                request.lahanId(),
                request.beratBasah()
        );

        PanenResponse responseData = PanenResponse.fromEntity(panen);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Data panen tembakau baru berhasil dicatat ke sistem.", responseData));
    }

    /**
     * Endpoint 2: Menginput hasil grading tembakau (Relasi Composition).
     * POST /api/panen/{id}/grading
     */
    @PostMapping("/{id}/grading")
    public ResponseEntity<ApiResponse<HasilGradingResponse>> prosesGrading(
            @PathVariable("id") Long id,
            @Valid @RequestBody GradingRequest request
    ) {
        HasilGrading hasilGrading = panenService.prosesGrading(
                id,
                request.grade(),
                request.beratKering(),
                request.hargaPerKg()
        );

        HasilGradingResponse responseData = HasilGradingResponse.fromEntity(hasilGrading);
        return ResponseEntity.ok(
                ApiResponse.ok("Hasil grading mutu tembakau berhasil diproses dan terikat ke data Panen.", responseData)
        );
    }

    /**
     * Mendapatkan detail data panen berdasarkan ID.
     * GET /api/panen/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PanenResponse>> getDetailPanen(@PathVariable("id") Long id) {
        Panen panen = panenService.getPanenById(id);
        return ResponseEntity.ok(ApiResponse.ok("Data panen berhasil ditemukan.", PanenResponse.fromEntity(panen)));
    }

    /**
     * Mengambil daftar seluruh panen yang sudah siap ditransaksikan (sudah di-grading).
     * GET /api/panen/siap-transaksi
     */
    @GetMapping("/siap-transaksi")
    public ResponseEntity<ApiResponse<List<PanenResponse>>> getPanenSiapTransaksi() {
        List<PanenResponse> daftar = panenService.getDaftarPanenSiapTransaksi()
                .stream()
                .map(PanenResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok("Daftar batch panen siap transaksi berhasil dimuat.", daftar));
    }
}
