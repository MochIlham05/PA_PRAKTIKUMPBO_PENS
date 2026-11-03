package com.rantaitasok.tembakau.service;

import com.rantaitasok.tembakau.entity.Panen;
import com.rantaitasok.tembakau.entity.Pengepul;
import com.rantaitasok.tembakau.entity.Transaksi;
import com.rantaitasok.tembakau.model.enums.StatusTransaksi;
import com.rantaitasok.tembakau.repository.PanenRepository;
import com.rantaitasok.tembakau.repository.PengepulRepository;
import com.rantaitasok.tembakau.repository.TransaksiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service untuk mengelola transaksi perdagangan rantai pasok tembakau Desa Prancak.
 * Bertanggung jawab memvalidasi kesiapan komoditas (adanya HasilGrading)
 * dan kalkulasi nilai ekonomi secara otomatis.
 */
@Service
@Transactional
public class TransaksiService {

    private final TransaksiRepository transaksiRepository;
    private final PanenRepository panenRepository;
    private final PengepulRepository pengepulRepository;

    public TransaksiService(
            TransaksiRepository transaksiRepository,
            PanenRepository panenRepository,
            PengepulRepository pengepulRepository
    ) {
        this.transaksiRepository = transaksiRepository;
        this.panenRepository = panenRepository;
        this.pengepulRepository = pengepulRepository;
    }

    /**
     * Membuat kesepakatan transaksi perdagangan tembakau baru untuk batch Panen tertentu.
     * Menggunakan pengepul default/terdaftar pertama di sistem rantai pasok Prancak.
     *
     * Validasi Bisnis:
     * 1. Data Panen wajib terdaftar.
     * 2. Panen wajib telah memiliki HasilGrading (tidak dapat diperdagangkan jika belum di-grading).
     * 3. Batch Panen belum pernah ditransaksikan sebelumnya (mencegah double-selling).
     * 4. Kalkulasi total harga dieksekusi secara otomatis dari mutu grading dan bobot kering.
     *
     * @param panenId ID Panen yang akan ditransaksikan
     * @return Objek Transaksi yang telah dipersist
     */
    public Transaksi buatTransaksi(Long panenId) {
        Pengepul defaultPengepul = pengepulRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> pengepulRepository.save(
                        new Pengepul("Koperasi Tembakau Desa Prancak", "081234567890", "Kec. Pasongsongan, Kab. Sumenep", "NIB-PRANCAK-001")
                ));

        return buatTransaksi(panenId, defaultPengepul.getId());
    }

    /**
     * Membuat transaksi dengan menentukan pihak Pengepul pembeli secara eksplisit.
     *
     * @param panenId    ID Panen yang diperdagangkan
     * @param pengepulId ID Pengepul pembeli
     * @return Objek Transaksi yang valid
     */
    public Transaksi buatTransaksi(Long panenId, Long pengepulId) {
        if (panenId == null) {
            throw new IllegalArgumentException("ID Panen tidak boleh bernilai null.");
        }
        if (pengepulId == null) {
            throw new IllegalArgumentException("ID Pengepul tidak boleh bernilai null.");
        }

        Panen panen = panenRepository.findById(panenId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Transaksi Gagal: Data Panen dengan ID " + panenId + " tidak ditemukan."
                ));

        // Validasi Aturan Domain: Transaksi hanya bisa dibuat jika Panen sudah memiliki HasilGrading
        if (!panen.isSiapDitransaksikan() || panen.getHasilGrading() == null) {
            throw new IllegalStateException(
                String.format("Transaksi Ditolak: Panen ID %d belum memiliki HasilGrading. " +
                              "Komoditas wajib diuji mutu dan ditimbang kering sebelum kesepakatan jual beli dibuat.", panenId)
            );
        }

        // Validasi Relator One-to-One: Panen tidak boleh diperjualbelikan lebih dari satu kali
        if (transaksiRepository.existsByPanenId(panenId)) {
            throw new IllegalStateException(
                String.format("Transaksi Ditolak: Panen ID %d sudah terikat pada kontrak transaksi lain sebelumnya.", panenId)
            );
        }

        Pengepul pengepul = pengepulRepository.findById(pengepulId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Transaksi Gagal: Pengepul dengan ID " + pengepulId + " tidak ditemukan."
                ));

        // Instansiasi Transaksi (Relator)
        // Konstruktor Transaksi otomatis memanggil kalkulasiTotalHarga() dari HasilGrading milik Panen
        Transaksi transaksiBaru = new Transaksi(panen, pengepul, LocalDateTime.now());

        // Memastikan eksekusi kalkulasi terpanggil secara eksplisit sesuai instruksi bisnis
        transaksiBaru.kalkulasiTotalHarga();

        return transaksiRepository.save(transaksiBaru);
    }

    /**
     * Menyelesaikan pembayaran transaksi tembakau.
     */
    public Transaksi konfirmasiPelunasan(Long transaksiId) {
        Transaksi transaksi = transaksiRepository.findById(transaksiId)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi dengan ID " + transaksiId + " tidak ditemukan."));

        transaksi.selesaikanPembayaran();
        return transaksiRepository.save(transaksi);
    }

    /**
     * Membatalkan transaksi dengan pencatatan alasan pembatalan.
     */
    public Transaksi batalkanTransaksi(Long transaksiId, String alasan) {
        Transaksi transaksi = transaksiRepository.findById(transaksiId)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi dengan ID " + transaksiId + " tidak ditemukan."));

        transaksi.batalkanTransaksi(alasan);
        return transaksiRepository.save(transaksi);
    }

    @Transactional(readOnly = true)
    public Transaksi getTransaksiById(Long transaksiId) {
        return transaksiRepository.findById(transaksiId)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi dengan ID " + transaksiId + " tidak ditemukan."));
    }

    @Transactional(readOnly = true)
    public List<Transaksi> getSemuaTransaksi() {
        return transaksiRepository.findAll();
    }
}
