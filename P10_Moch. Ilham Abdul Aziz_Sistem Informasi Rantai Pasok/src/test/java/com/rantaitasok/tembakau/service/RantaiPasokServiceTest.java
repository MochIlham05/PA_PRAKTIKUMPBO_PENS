package com.rantaitasok.tembakau.service;

import com.rantaitasok.tembakau.entity.*;
import com.rantaitasok.tembakau.model.enums.GradeTembakau;
import com.rantaitasok.tembakau.model.enums.StatusTransaksi;
import com.rantaitasok.tembakau.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RantaiPasokServiceTest {

    @Autowired
    private PanenService panenService;

    @Autowired
    private TransaksiService transaksiService;

    @Autowired
    private PetaniRepository petaniRepository;

    @Autowired
    private LahanRepository lahanRepository;

    @Autowired
    private PengepulRepository pengepulRepository;

    private Petani petaniAhmad;
    private Petani petaniBahrul;
    private Lahan lahanAhmad;
    private Lahan lahanBahrul;
    private Pengepul pengepulSumenep;

    @BeforeEach
    void setUp() {
        petaniAhmad = petaniRepository.save(new Petani("Pak Ahmad", "3529012345670001", "Dusun Somber, Desa Prancak"));
        petaniBahrul = petaniRepository.save(new Petani("Pak Bahrul", "3529012345670002", "Dusun Bates, Desa Prancak"));

        lahanAhmad = lahanRepository.save(new Lahan(new BigDecimal("1.50"), "-6.912345, 113.712345", petaniAhmad));
        lahanBahrul = lahanRepository.save(new Lahan(new BigDecimal("2.00"), "-6.923456, 113.723456", petaniBahrul));

        pengepulSumenep = pengepulRepository.save(new Pengepul("UD Tembakau Madura Makmur", "081987654321", "Pasongsongan, Sumenep", "NIB-12345"));
    }

    @Test
    @DisplayName("1. Skenario Sukses: Pencatatan Panen Baru pada Lahan yang Sah")
    void testCatatPanenBaru_Sukses() {
        Panen panen = panenService.catatPanenBaru(petaniAhmad.getId(), lahanAhmad.getId(), 500.0);

        assertNotNull(panen.getId());
        assertEquals(500.0, panen.getBeratBasah().doubleValue());
        assertEquals(petaniAhmad.getId(), panen.getPetani().getId());
        assertFalse(panen.isSiapDitransaksikan());
    }

    @Test
    @DisplayName("2. Skenario Validasi Gagal: Petani memanen lahan milik orang lain")
    void testCatatPanenBaru_Gagal_LahanBukanMilikPetani() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                panenService.catatPanenBaru(petaniAhmad.getId(), lahanBahrul.getId(), 300.0)
        );
        assertTrue(exception.getMessage().contains("bukan merupakan aset milik Petani"));
    }

    @Test
    @DisplayName("3. Skenario Sukses: Proses Grading Mutu (Komposisi)")
    void testProsesGrading_Sukses() {
        Panen panen = panenService.catatPanenBaru(petaniAhmad.getId(), lahanAhmad.getId(), 500.0);
        
        // Proses grading 75 kg tembakau kering Grade A
        HasilGrading grading = panenService.prosesGrading(panen.getId(), GradeTembakau.A, 75.0, 95000.0);

        assertNotNull(grading);
        assertEquals(GradeTembakau.A, grading.getGrade());
        assertEquals(75.0, grading.getBeratKering().doubleValue());
        assertEquals(0, grading.getHargaPerKg().compareTo(new BigDecimal("95000")));

        Panen updatedPanen = panenService.getPanenById(panen.getId());
        assertTrue(updatedPanen.isSiapDitransaksikan());
    }

    @Test
    @DisplayName("4. Skenario Validasi Invariant Fisik: Berat kering > berat basah")
    void testProsesGrading_Gagal_BeratKeringMelebihiBeratBasah() {
        Panen panen = panenService.catatPanenBaru(petaniAhmad.getId(), lahanAhmad.getId(), 200.0);

        // Mencoba input berat kering 250 kg padahal berat basah hanya 200 kg
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                panenService.prosesGrading(panen.getId(), GradeTembakau.B, 250.0, 80000.0)
        );
        assertTrue(exception.getMessage().contains("tidak boleh lebih besar dari berat basah"));
    }

    @Test
    @DisplayName("5. Skenario Validasi Transaksi: Menolak panen yang belum di-grading")
    void testBuatTransaksi_Gagal_BelumGrading() {
        Panen panenBelumGrading = panenService.catatPanenBaru(petaniAhmad.getId(), lahanAhmad.getId(), 400.0);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                transaksiService.buatTransaksi(panenBelumGrading.getId(), pengepulSumenep.getId())
        );
        assertTrue(exception.getMessage().contains("belum memiliki HasilGrading"));
    }

    @Test
    @DisplayName("6. Skenario Sukses: Transaksi dengan kalkulasi total harga otomatis")
    void testBuatTransaksi_Sukses_KalkulasiOtomatis() {
        Panen panen = panenService.catatPanenBaru(petaniAhmad.getId(), lahanAhmad.getId(), 500.0);
        panenService.prosesGrading(panen.getId(), GradeTembakau.A, 60.0, 100000.0); // 60 kg * Rp 100.000 = Rp 6.000.000

        Transaksi transaksi = transaksiService.buatTransaksi(panen.getId(), pengepulSumenep.getId());

        assertNotNull(transaksi.getId());
        assertEquals(StatusTransaksi.PENDING, transaksi.getStatus());
        // Verifikasi kalkulasi otomatis: 60 * 100000 = 6.000.000
        assertEquals(0, transaksi.getTotalHarga().compareTo(new BigDecimal("6000000")));

        // Verifikasi pelunasan
        transaksiService.konfirmasiPelunasan(transaksi.getId());
        Transaksi transaksiLunas = transaksiService.getTransaksiById(transaksi.getId());
        assertEquals(StatusTransaksi.LUNAS, transaksiLunas.getStatus());
    }
}
