package com.rantaitasok.tembakau.controller;

import com.rantaitasok.tembakau.dto.ApiResponse;
import com.rantaitasok.tembakau.dto.CheckoutRequest;
import com.rantaitasok.tembakau.dto.TransaksiResponse;
import com.rantaitasok.tembakau.entity.Transaksi;
import com.rantaitasok.tembakau.service.TransaksiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller untuk mengelola transaksi perdagangan tembakau.
 * Menyediakan endpoint checkout jual-beli dan struk digital bagi aplikasi mobile/frontend.
 */
@RestController
@RequestMapping("/api/transaksi")
public class TransaksiController {

    private final TransaksiService transaksiService;

    public TransaksiController(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    /**
     * Endpoint 1: Membuat transaksi baru dari panen yang sudah di-grading.
     * POST /api/transaksi/checkout/{panenId}
     *
     * @param panenId ID Panen yang sudah selesai grading
     * @param request Payload opsional untuk menentukan pengepulId tertentu
     */
    @PostMapping("/checkout/{panenId}")
    public ResponseEntity<ApiResponse<TransaksiResponse>> checkoutTransaksi(
            @PathVariable("panenId") Long panenId,
            @RequestBody(required = false) CheckoutRequest request
    ) {
        Transaksi transaksi;
        if (request != null && request.pengepulId() != null) {
            transaksi = transaksiService.buatTransaksi(panenId, request.pengepulId());
        } else {
            transaksi = transaksiService.buatTransaksi(panenId);
        }

        TransaksiResponse responseData = TransaksiResponse.fromEntity(transaksi);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transaksi checkout tembakau berhasil dibuat dengan total harga otomatis.", responseData));
    }

    /**
     * Endpoint 2: Mendapatkan struk / detail transaksi berdasarkan ID.
     * GET /api/transaksi/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransaksiResponse>> getDetailTransaksi(@PathVariable("id") Long id) {
        Transaksi transaksi = transaksiService.getTransaksiById(id);
        TransaksiResponse responseData = TransaksiResponse.fromEntity(transaksi);

        return ResponseEntity.ok(ApiResponse.ok("Detail struk transaksi berhasil dimuat.", responseData));
    }

    /**
     * Endpoint Tambahan: Konfirmasi pelunasan transaksi.
     * POST /api/transaksi/{id}/lunas
     */
    @PostMapping("/{id}/lunas")
    public ResponseEntity<ApiResponse<TransaksiResponse>> konfirmasiPelunasan(@PathVariable("id") Long id) {
        Transaksi transaksi = transaksiService.konfirmasiPelunasan(id);
        TransaksiResponse responseData = TransaksiResponse.fromEntity(transaksi);

        return ResponseEntity.ok(ApiResponse.ok("Status transaksi berhasil diubah menjadi LUNAS.", responseData));
    }

    /**
     * Mengambil seluruh riwayat transaksi perdagangan tembakau.
     * GET /api/transaksi
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransaksiResponse>>> getAllTransaksi() {
        List<TransaksiResponse> daftar = transaksiService.getSemuaTransaksi()
                .stream()
                .map(TransaksiResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok("Seluruh riwayat transaksi berhasil dimuat.", daftar));
    }
}
