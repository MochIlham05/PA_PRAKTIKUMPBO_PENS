package com.rantaitasok.tembakau.dto;

/**
 * Request payload opsional untuk checkout transaksi.
 * Jika pengepulId null, sistem akan mengarahkan ke pengepul default/koperasi.
 */
public record CheckoutRequest(
    Long pengepulId
) {}
