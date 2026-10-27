package com.rantaitasok.tembakau.service;

import com.rantaitasok.tembakau.entity.HasilGrading;
import com.rantaitasok.tembakau.entity.Lahan;
import com.rantaitasok.tembakau.entity.Panen;
import com.rantaitasok.tembakau.entity.Petani;
import com.rantaitasok.tembakau.model.enums.GradeTembakau;
import com.rantaitasok.tembakau.repository.LahanRepository;
import com.rantaitasok.tembakau.repository.PanenRepository;
import com.rantaitasok.tembakau.repository.PetaniRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Service untuk mengelola siklus hidup agrikultur dan pemrosesan panen tembakau Desa Prancak.
 * Menerapkan validasi aturan domain OOAD & OntoUML (Relator & Composition).
 */
@Service
@Transactional
public class PanenService {

    private final PanenRepository panenRepository;
    private final PetaniRepository petaniRepository;
    private final LahanRepository lahanRepository;

    public PanenService(
            PanenRepository panenRepository,
            PetaniRepository petaniRepository,
            LahanRepository lahanRepository
    ) {
        this.panenRepository = panenRepository;
        this.petaniRepository = petaniRepository;
        this.lahanRepository = lahanRepository;
    }

    /**
     * Mencatat peristiwa panen tembakau baru.
     * 
     * Validasi Bisnis:
     * 1. Petani dan Lahan wajib terdaftar di sistem.
     * 2. Lahan yang dipanen terbukti sah sebagai milik Petani yang bersangkutan.
     * 3. Berat basah harus bernilai positif (> 0 kg).
     *
     * @param petaniId   ID Petani yang memanen
     * @param lahanId    ID Lahan lokasi panen
     * @param beratBasah Bobot daun tembakau basah (kg)
     * @return Objek Panen yang berhasil dipersist
     */
    public Panen catatPanenBaru(Long petaniId, Long lahanId, double beratBasah) {
        if (petaniId == null) {
            throw new IllegalArgumentException("ID Petani tidak boleh bernilai null.");
        }
        if (lahanId == null) {
            throw new IllegalArgumentException("ID Lahan tidak boleh bernilai null.");
        }
        if (beratBasah <= 0.0) {
            throw new IllegalArgumentException(
                String.format("Validasi Gagal: Berat basah harus lebih besar dari 0 kg. Nilai input: %.2f kg.", beratBasah)
            );
        }

        Petani petani = petaniRepository.findById(petaniId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Petani dengan ID " + petaniId + " tidak ditemukan dalam sistem."
                ));

        Lahan lahan = lahanRepository.findById(lahanId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Lahan dengan ID " + lahanId + " tidak ditemukan dalam sistem."
                ));

        // Validasi Relasional Domain: Memastikan Lahan tersebut benar milik Petani yang bersangkutan
        if (lahan.getPetani() == null || !Objects.equals(lahan.getPetani().getId(), petani.getId())) {
            throw new IllegalArgumentException(
                String.format("Pelanggaran Integritas Bisnis: Lahan ID %d bukan merupakan aset milik Petani '%s' (ID: %d).",
                        lahanId, petani.getNama(), petaniId)
            );
        }

        // Instansiasi Panen (Relator antara Petani dan Lahan)
        Panen panenBaru = new Panen(
                LocalDate.now(),
                BigDecimal.valueOf(beratBasah),
                petani,
                lahan
        );

        // Pertahankan sinkronisasi objek in-memory
        petani.catatPanen(panenBaru);

        return panenRepository.save(panenBaru);
    }

    /**
     * Memproses grading kualitas dan perajangan/pengeringan tembakau.
     * Mengeksekusi relasi Composition antara Panen dan HasilGrading.
     * 
     * Validasi State & Invariant Fisik:
     * 1. Data Panen wajib ditemukan.
     * 2. Berat kering tembakau tidak boleh melampaui berat basah daun (beratKering <= beratBasah).
     * 3. Berat kering dan harga per kg harus bernilai positif.
     *
     * @param panenId     ID Panen target
     * @param grade       Klasifikasi mutu (Grade A - E)
     * @param beratKering Bobot tembakau kering hasil rajangan (kg)
     * @param hargaPerKg  Harga kesepakatan pasar per kg (bisa 0 jika ingin memakai referensi otomatis grade)
     * @return Objek HasilGrading yang terpasang secara komposit pada Panen
     */
    public HasilGrading prosesGrading(Long panenId, GradeTembakau grade, double beratKering, double hargaPerKg) {
        if (panenId == null) {
            throw new IllegalArgumentException("ID Panen tidak boleh null.");
        }
        if (grade == null) {
            throw new IllegalArgumentException("Grade kualitas tembakau wajib ditentukan.");
        }
        if (beratKering <= 0.0) {
            throw new IllegalArgumentException("Berat kering harus lebih besar dari 0 kg.");
        }

        Panen panen = panenRepository.findById(panenId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Grading Gagal: Data Panen dengan ID " + panenId + " tidak ditemukan."
                ));

        double beratBasah = panen.getBeratBasah().doubleValue();

        // Validasi State / Invariant Fisik: Berat kering tidak boleh lebih besar dari berat basah
        if (beratKering > beratBasah) {
            throw new IllegalArgumentException(
                String.format("Grading Gagal: Berat kering (%.2f kg) tidak boleh lebih besar dari berat basah daun tembakau (%.2f kg).",
                        beratKering, beratBasah)
            );
        }

        BigDecimal hargaFinal = hargaPerKg > 0.0 ? BigDecimal.valueOf(hargaPerKg) : grade.getHargaReferensiPerKg();

        // Mengeksekusi relasi Komposisi via Factory Method pada Panen
        HasilGrading hasilGrading = panen.lakukanGrading(
                grade,
                BigDecimal.valueOf(beratKering),
                hargaFinal,
                "Grading mutu standar tembakau Desa Prancak"
        );

        // Menyimpan Panen akan meng-cascade persist/update pada HasilGrading secara otomatis
        panenRepository.save(panen);

        return hasilGrading;
    }

    @Transactional(readOnly = true)
    public Panen getPanenById(Long panenId) {
        return panenRepository.findById(panenId)
                .orElseThrow(() -> new IllegalArgumentException("Panen dengan ID " + panenId + " tidak ditemukan."));
    }

    @Transactional(readOnly = true)
    public List<Panen> getDaftarPanenSiapTransaksi() {
        return panenRepository.findAllSudahGrading();
    }

    @Transactional(readOnly = true)
    public List<Panen> getDaftarPanenMenungguGrading() {
        return panenRepository.findAllBelumGrading();
    }
}
