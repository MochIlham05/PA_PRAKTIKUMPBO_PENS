package com.rantaitasok.tembakau.config;

import com.rantaitasok.tembakau.entity.*;
import com.rantaitasok.tembakau.model.enums.GradeTembakau;
import com.rantaitasok.tembakau.repository.*;
import com.rantaitasok.tembakau.service.PanenService;
import com.rantaitasok.tembakau.service.TransaksiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * DataSeeder mengimplementasikan CommandLineRunner untuk menginisialisasi
 * data demonstrasi rantai pasok tembakau Desa Prancak saat aplikasi pertama kali dijalankan.
 * Sangat krusial untuk live demo di hadapan dosen penguji/sidang.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private final PetaniRepository petaniRepository;
    private final LahanRepository lahanRepository;
    private final PengepulRepository pengepulRepository;
    private final PanenService panenService;
    private final TransaksiService transaksiService;

    public DataSeeder(
            PetaniRepository petaniRepository,
            LahanRepository lahanRepository,
            PengepulRepository pengepulRepository,
            PanenService panenService,
            TransaksiService transaksiService
    ) {
        this.petaniRepository = petaniRepository;
        this.lahanRepository = lahanRepository;
        this.pengepulRepository = pengepulRepository;
        this.panenService = panenService;
        this.transaksiService = transaksiService;
    }

    @Override
    public void run(String... args) {
        log.info("==================================================================================");
        log.info(">>> MEMULAI DATA SEEDER: RANTAI PASOK TEMBAKAU DESA PRANCAK, SUMENEP <<<");
        log.info("==================================================================================");

        // 1. Simpan 1 Petani bernama "Bapak Fulan" dari "Desa Prancak"
        Petani bapakFulan = new Petani(
                "Bapak Fulan",
                "3529012345670001",
                "Dusun Bates, Desa Prancak, Kec. Pasongsongan, Kab. Sumenep"
        );
        bapakFulan = petaniRepository.save(bapakFulan);
        log.info("[SEEDED 1/5] Petani Terdaftar: ID={}, Nama='{}', NIK={}, Alamat='{}'",
                bapakFulan.getId(), bapakFulan.getNama(), bapakFulan.getNik(), bapakFulan.getAlamat());

        // 2. Simpan 2 Lahan milik Petani tersebut
        Lahan lahan1 = new Lahan(
                new BigDecimal("1.25"),
                "-6.915234, 113.725678 (Blok Somber Barat, Desa Prancak)",
                bapakFulan
        );
        Lahan lahan2 = new Lahan(
                new BigDecimal("0.85"),
                "-6.918912, 113.729101 (Blok Somber Timur, Desa Prancak)",
                bapakFulan
        );

        bapakFulan.tambahLahan(lahan1);
        bapakFulan.tambahLahan(lahan2);

        lahan1 = lahanRepository.save(lahan1);
        lahan2 = lahanRepository.save(lahan2);

        log.info("[SEEDED 2/5] Lahan 1 Terdaftar: ID={}, Luas={} Ha, Lokasi GPS='{}', Pemilik='{}'",
                lahan1.getId(), lahan1.getLuasHektar(), lahan1.getLokasiGps(), lahan1.getPetani().getNama());
        log.info("[SEEDED 2/5] Lahan 2 Terdaftar: ID={}, Luas={} Ha, Lokasi GPS='{}', Pemilik='{}'",
                lahan2.getId(), lahan2.getLuasHektar(), lahan2.getLokasiGps(), lahan2.getPetani().getNama());
        log.info("             -> Total Akumulasi Luas Lahan Bapak Fulan: {} Ha", bapakFulan.hitungTotalLuasLahan());

        // Siapkan 1 Pengepul Pembeli sebagai counterparty transaksi
        Pengepul pengepul = pengepulRepository.save(new Pengepul(
                "UD Tembakau Madura Makmur",
                "081987654321",
                "Jl. Raya Pasongsongan No. 45, Sumenep",
                "NIB-PRANCAK-889"
        ));
        log.info("[SEEDED INFO] Pengepul Terdaftar: ID={}, Nama='{}', Kontak={}",
                pengepul.getId(), pengepul.getNama(), pengepul.getNomorTelepon());

        // 3. Buat 1 data Panen dari lahan pertama (berat basah: 500 kg)
        double beratBasah = 500.0;
        Panen panen = panenService.catatPanenBaru(bapakFulan.getId(), lahan1.getId(), beratBasah);
        log.info("[SEEDED 3/5] Panen Dicatat: ID={}, Tanggal={}, Berat Basah={} kg, Petani='{}', Lahan ID={}",
                panen.getId(), panen.getTanggalPanen(), panen.getBeratBasah(), panen.getPetani().getNama(), panen.getLahan().getId());
        log.info("             -> Status Kesiapan Transaksi: {} (Menunggu Grading)", panen.isSiapDitransaksikan());

        // 4. Lakukan proses grading pada Panen tersebut (Grade A, harga 75000/kg, berat kering 70 kg)
        GradeTembakau gradeDipilih = GradeTembakau.A;
        double beratKering = 70.0;
        double hargaPerKg = 75000.0;

        HasilGrading hasilGrading = panenService.prosesGrading(
                panen.getId(),
                gradeDipilih,
                beratKering,
                hargaPerKg
        );
        log.info("[SEEDED 4/5] Hasil Grading (Komposisi): Mutu={}, Deskripsi='{}', Berat Kering={} kg, Harga/Kg={}",
                hasilGrading.getGrade(), hasilGrading.getGrade().getDeskripsi(),
                hasilGrading.getBeratKering(), rupiahFormat.format(hasilGrading.getHargaPerKg()));

        Panen panenSetelahGrading = panenService.getPanenById(panen.getId());
        log.info("             -> Rendemen Penyusutan Daun Kering: {}%", panenSetelahGrading.hitungPersentaseRendemen());
        log.info("             -> Status Kesiapan Transaksi: {} (Siap Ditransaksikan)", panenSetelahGrading.isSiapDitransaksikan());

        // 5. Buat Transaksi untuk panen tersebut menjadi LUNAS
        // Checkout transaksi (Otomatis memanggil kalkulasiTotalHarga(): 70 kg * Rp 75.000 = Rp 5.250.000)
        Transaksi transaksi = transaksiService.buatTransaksi(panenSetelahGrading.getId(), pengepul.getId());
        log.info("[SEEDED 5/5] Transaksi Dibuat: ID={}, Tanggal={}, StatusAwal={}, TotalHarga={}",
                transaksi.getId(), transaksi.getTanggalTransaksi(), transaksi.getStatus(), rupiahFormat.format(transaksi.getTotalHarga()));

        // Konfirmasi pelunasan pembayaran
        transaksi = transaksiService.konfirmasiPelunasan(transaksi.getId());
        log.info("[SEEDED 5/5] Transaksi Dilunasi: ID={}, StatusAkhir={}, TotalDibayar={}",
                transaksi.getId(), transaksi.getStatus(), rupiahFormat.format(transaksi.getTotalHarga()));

        log.info("==================================================================================");
        log.info(">>> DATA SEEDER SUKSES: DATABASE SIAP DIGUNAKAN UNTUK DEMO SIDANG! <<<");
        log.info(">>> Endpoint Demo: http://localhost:8080/api/transaksi/{} (Struk Digital) <<<", transaksi.getId());
        log.info(">>> H2 Console   : http://localhost:8080/h2-console (JDBC: jdbc:h2:mem:tembakaudb) <<<");
        log.info("==================================================================================");
    }
}
