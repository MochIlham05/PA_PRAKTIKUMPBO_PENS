package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Kelas Utama Lapisan Desktop Java Swing (MainDesktopApp).
 * Mengintegrasikan seluruh arsitektur sistem informasi rantai pasok tembakau
 * menjadi satu sistem terintegrasi yang utuh dan profesional.
 *
 * Modul yang Terintegrasi:
 * 1. Manajemen Pasokan (Alur 2 Tahap Verifikasi & Invarian Protection)
 * 2. Registrasi Petani Mitra dengan Validasi e-KTP 16-Digit & Anti-Duplikasi
 * 3. Logistik Alur Keluar (Outbound Pengiriman ke Pabrikan Rokok Mitra & Surat Jalan)
 * 4. Modul Keuangan & Kasir (Pelunasan Hak Petani & Bukti Kas Keluar)
 * 5. Dashboard Eksekutif & Analitik Mutu Tembakau (KPI)
 * 6. Pemisahan Hak Akses Multi-Peran (Role-Based Access Control)
 * 7. Persistensi JSON Terstruktur (Auto-Save/Load)
 * 8. Log Audit State Empiris (Pola 3 Subbab 4.6.4)
 */
public class MainDesktopApp extends JFrame {

    // 1. Polimorfisme pada atribut utama sesuai ketentuan wajib
    private final PenerimaPasokan gudang = new GudangTembakau(
        "GDG-PST-01", "Gudang Penyangga Pusat", 1000.0
    );

    // Multi-Gudang, Multi-Daerah & Referensi Gudang Aktif
    private final List<String> daftarDaerah = new ArrayList<>(Arrays.asList("Sumenep", "Pamekasan", "Sampang", "Bangkalan"));
    private String daerahAktif = "Sumenep";
    private final JComboBox<String> cmbDaerah = new JComboBox<>();
    private final Map<String, GudangTembakau> daftarGudang = new LinkedHashMap<>();
    private final Map<String, String> wilayahGudangMap = new LinkedHashMap<>();
    private final Map<String, String> gudangDaerahMap = new LinkedHashMap<>();
    private final JComboBox<GudangTembakau> cmbGudangAktif = new JComboBox<>();
    private PenerimaPasokan gudangAktif = gudang;

    // Koleksi State Aplikasi
    private final List<Petani> daftarPetani = new ArrayList<>();
    private final List<TransaksiPasok> riwayatSemuaTransaksi = new ArrayList<>();

    // Model Tabel
    private final PetaniTableModel petaniTableModel = new PetaniTableModel(daftarPetani);
    private final TransaksiTableModel transaksiGudangTableModel = new TransaksiTableModel();
    private final TransaksiTableModel semuaTransaksiTableModel = new TransaksiTableModel(riwayatSemuaTransaksi);

    // Tabel Swing
    private JTable tblTransaksiGudang;
    private JTable tblSemuaTransaksi;
    private JTable tblPetani;

    // Panel-Panel Modul Sistem
    private PanelStatusGudang panelStatusGudang;
    private PanelRegistrasiPetani panelRegistrasiPetani;
    private PanelOperasiPasokan panelOperasiPasokan;
    private PanelDistribusiPabrik panelDistribusiPabrik;
    private PanelKasirPembayaran panelKasirPembayaran;
    private PanelDashboardKPI panelDashboardKPI;

    // Komponen TabbedPane dan Log
    private final JTabbedPane tabKiri = new JTabbedPane();
    private final JTabbedPane tabKanan = new JTabbedPane();
    private final JTextArea logArea = new JTextArea();

    // Role-Based Access Control Switcher
    private final JComboBox<String> cmbRole = new JComboBox<>(new String[] {
        "Administrator / Kepala Gudang",
        "Petugas QC & Grader Mutu",
        "Petugas Timbang & Logistik",
        "Petugas Kasir & Keuangan"
    });

    private final DecimalFormat formatAngka;
    private final DecimalFormat formatRupiah;
    private final DateTimeFormatter formatWaktu = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public MainDesktopApp() {
        super("Sistem Informasi Manajemen Rantai Pasok Tembakau");

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);
        this.formatRupiah = new DecimalFormat("Rp #,##0.00", symbols);

        inisialisasiLookAndFeel();
        setJMenuBar(buatMenuBar());
        inisialisasiKomponen();
        muatDataAwalAtauFixture();
        aturUkuranDanPosisi();
    }

    private void inisialisasiLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void inisialisasiKomponen() {
        setLayout(new BorderLayout(8, 8));

        // 0. Inisialisasi Jaringan Multi-Gudang Bertingkat (Daerah & Zonasi Wilayah)
        GudangTembakau gPusat = (GudangTembakau) gudang;
        GudangTembakau gSmpBarat = new GudangTembakau("GDG-SMP-02", "Gudang Pengumpul Barat", 800.0);
        GudangTembakau gSmpTimur = new GudangTembakau("GDG-SMP-03", "Gudang Penyangga Timur", 1500.0);
        GudangTembakau gSmpSelatan = new GudangTembakau("GDG-SMP-04", "Gudang Pembelian Selatan", 500.0);

        // Gudang-gudang di Daerah Sumenep
        daftarGudang.put(gPusat.getKodeGudang(), gPusat);
        daftarGudang.put(gSmpBarat.getKodeGudang(), gSmpBarat);
        daftarGudang.put(gSmpTimur.getKodeGudang(), gSmpTimur);
        daftarGudang.put(gSmpSelatan.getKodeGudang(), gSmpSelatan);

        wilayahGudangMap.put(gPusat.getKodeGudang(), "Wilayah Pusat");
        wilayahGudangMap.put(gSmpBarat.getKodeGudang(), "Wilayah Barat");
        wilayahGudangMap.put(gSmpTimur.getKodeGudang(), "Wilayah Timur");
        wilayahGudangMap.put(gSmpSelatan.getKodeGudang(), "Wilayah Selatan");

        gudangDaerahMap.put(gPusat.getKodeGudang(), "Sumenep");
        gudangDaerahMap.put(gSmpBarat.getKodeGudang(), "Sumenep");
        gudangDaerahMap.put(gSmpTimur.getKodeGudang(), "Sumenep");
        gudangDaerahMap.put(gSmpSelatan.getKodeGudang(), "Sumenep");

        // Inisialisasi Gudang di Daerah Pamekasan (Fresh / 0 kg)
        inisialisasiGudangDaerahStandar("Pamekasan", "PMK", 1200.0, 800.0, 1000.0, 600.0);

        // Inisialisasi Gudang di Daerah Sampang (Fresh / 0 kg)
        inisialisasiGudangDaerahStandar("Sampang", "SPG", 1000.0, 700.0, 900.0, 500.0);

        // Inisialisasi Gudang di Daerah Bangkalan (Fresh / 0 kg)
        inisialisasiGudangDaerahStandar("Bangkalan", "BKL", 1000.0, 600.0, 800.0, 500.0);

        // Isi ComboBox Daerah
        cmbDaerah.removeAllItems();
        for (String d : daftarDaerah) {
            cmbDaerah.addItem(d);
        }
        cmbDaerah.setSelectedItem("Sumenep");
        this.daerahAktif = "Sumenep";

        cmbGudangAktif.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof GudangTembakau) {
                    GudangTembakau g = (GudangTembakau) value;
                    String zona = wilayahGudangMap.getOrDefault(g.getKodeGudang(), "Wilayah");
                    setText("[" + zona + "] " + g.getNamaGudang() + " (" + formatAngka.format(g.getKapasitasMaksKg()) + " kg)");
                }
                return this;
            }
        });

        // Event listener saat gudang aktif berubah
        cmbGudangAktif.addActionListener(e -> {
            GudangTembakau g = (GudangTembakau) cmbGudangAktif.getSelectedItem();
            if (g != null && g != gudangAktif) {
                terapkanGudangAktif(g);
                String wilayah = wilayahGudangMap.getOrDefault(g.getKodeGudang(), "-");
                String daerah = gudangDaerahMap.getOrDefault(g.getKodeGudang(), daerahAktif);
                catatLogInformasi("[GANTI GUDANG] Entitas penerima aktif: "
                    + g.getNamaGudang() + " [" + g.getKodeGudang() + "] di Daerah " + daerah
                    + " (" + wilayah + ", Kapasitas: " + formatAngka.format(g.getKapasitasMaksKg()) + " kg)");
            }
        });

        // Isi combobox gudang sesuai daerah aktif
        perbaruiPilihanGudangPerDaerah("Sumenep");
        cmbGudangAktif.setSelectedItem(gPusat);
        this.gudangAktif = gPusat;

        // Event listener saat Daerah berubah
        cmbDaerah.addActionListener(e -> {
            String daerahPilihan = (String) cmbDaerah.getSelectedItem();
            if (daerahPilihan != null && !daerahPilihan.equals(daerahAktif)) {
                this.daerahAktif = daerahPilihan;
                perbaruiPilihanGudangPerDaerah(daerahPilihan);
                catatLogInformasi("[GANTI DAERAH] Berpindah ke Daerah: " + daerahPilihan
                    + ". Seluruh gudang & metrik operasional disinkronisasikan ke daerah ini.");
            }
        });

        // 1. Inisialisasi Seluruh Panel Modul
        petaniTableModel.setGudangAktif(gudangAktif);
        panelStatusGudang = new PanelStatusGudang(gudangAktif);
        panelStatusGudang.setDaerah("Sumenep");
        panelStatusGudang.setWilayah(wilayahGudangMap.getOrDefault(gPusat.getKodeGudang(), "Wilayah Pusat"));
        panelRegistrasiPetani = new PanelRegistrasiPetani(petaniTableModel);
        panelOperasiPasokan = new PanelOperasiPasokan(
            gudangAktif, panelStatusGudang, petaniTableModel, transaksiGudangTableModel
        );
        panelDistribusiPabrik = new PanelDistribusiPabrik(gudangAktif);
        panelDistribusiPabrik.setOnPengosonganGudangListener(gt -> eksekusiKosongkanGudang(gt, false));

        List<TransaksiPasok> trxGudangAwal = (gPusat != null) ? gPusat.getDaftarTransaksi() : riwayatSemuaTransaksi;
        panelKasirPembayaran = new PanelKasirPembayaran(daftarPetani, trxGudangAwal);
        panelKasirPembayaran.setIdentitasGudang(gPusat.getNamaGudang(), "Sumenep - Wilayah Pusat");

        panelDashboardKPI = new PanelDashboardKPI(daftarPetani, trxGudangAwal);
        panelDashboardKPI.setIdentitasGudang(gPusat.getNamaGudang(), "Sumenep - Wilayah Pusat");

        // Sinkronisasi pendaftaran petani baru
        panelRegistrasiPetani.addOnPetaniAddedListener(p -> {
            if (!daftarPetani.contains(p)) {
                daftarPetani.add(p);
            }
            panelOperasiPasokan.tambahPetaniKeComboBox(p);
            List<TransaksiPasok> trxAktifGudang = (gudangAktif instanceof GudangTembakau)
                ? ((GudangTembakau) gudangAktif).getDaftarTransaksi()
                : riwayatSemuaTransaksi;
            panelKasirPembayaran.perbaruiDaftarPetani(daftarPetani, trxAktifGudang);
            panelDashboardKPI.perbaruiData(daftarPetani, trxAktifGudang);
            catatLogInformasi("Petani baru berhasil didaftarkan: " + p.getNama() + " (" + p.getAsalDesa() + ")");
            simpanDatabaseKeFile(false);
        });

        // Sinkronisasi status transaksi
        panelOperasiPasokan.addOnTransaksiListener(this::sinkronkanRiwayatTransaksi);

        // Pasang Audit Wrapper untuk State Snapshot (Pola 3 Subbab 4.6.4)
        pasangAuditWrapper();

        // 2. Bagian Atas: Header Multi-Gudang & Multi-Peran + Toolbar Simulasi + Panel Status Gudang
        JPanel panelAtas = new JPanel(new BorderLayout(4, 4));

        JPanel panelHeaderDanRole = new JPanel(new BorderLayout(8, 4));
        panelHeaderDanRole.setOpaque(false);
        panelHeaderDanRole.add(buatToolbarPresentasi(), BorderLayout.CENTER);

        // Selector Daerah, Gudang, Tambah Gudang, Edit Kapasitas & Role di kanan atas
        JPanel panelControlsAtas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 4));
        panelControlsAtas.setOpaque(false);

        JLabel lblDaerah = new JLabel("Daerah:");
        lblDaerah.setFont(new Font("SansSerif", Font.BOLD, 11));
        cmbDaerah.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JButton btnTambahDaerah = new JButton("+ Daerah");
        btnTambahDaerah.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnTambahDaerah.setBackground(new Color(46, 125, 50));
        btnTambahDaerah.setForeground(Color.WHITE);
        btnTambahDaerah.setFocusPainted(false);
        btnTambahDaerah.setToolTipText("Tambah daerah / kabupaten baru operasional rantai pasok");
        btnTambahDaerah.addActionListener(e -> bukaDialogTambahDaerah());

        JLabel lblGudang = new JLabel("Gudang:");
        lblGudang.setFont(new Font("SansSerif", Font.BOLD, 11));
        cmbGudangAktif.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JButton btnTambahGudang = new JButton("+ Gudang");
        btnTambahGudang.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnTambahGudang.setBackground(new Color(25, 118, 210));
        btnTambahGudang.setForeground(Color.WHITE);
        btnTambahGudang.setFocusPainted(false);
        btnTambahGudang.setToolTipText("Tambah entitas gudang baru secara manual dan tetapkan wilayah zonasi");
        btnTambahGudang.addActionListener(e -> bukaDialogTambahGudang());

        JButton btnEditKapasitas = new JButton("Edit Kapasitas");
        btnEditKapasitas.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnEditKapasitas.setBackground(new Color(230, 149, 0));
        btnEditKapasitas.setForeground(Color.WHITE);
        btnEditKapasitas.setFocusPainted(false);
        btnEditKapasitas.setToolTipText("Ubah kapasitas penampungan maksimum gudang aktif (dengan validasi Invarian I2)");
        btnEditKapasitas.addActionListener(e -> bukaDialogEditKapasitas());

        JButton btnKosongkanGudang = new JButton("Kosongkan Gudang");
        btnKosongkanGudang.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnKosongkanGudang.setBackground(new Color(198, 40, 40));
        btnKosongkanGudang.setForeground(Color.WHITE);
        btnKosongkanGudang.setFocusPainted(false);
        btnKosongkanGudang.setToolTipText("Kosongkan / Reset seluruh muatan fisik gudang aktif (misal setelah selesai pengiriman ke pabrik)");
        btnKosongkanGudang.addActionListener(e -> kosongkanMuatanGudangAktif());

        JLabel lblRole = new JLabel("Peran:");
        lblRole.setFont(new Font("SansSerif", Font.BOLD, 11));
        cmbRole.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cmbRole.addActionListener(e -> aturHakAksesPeran(cmbRole.getSelectedIndex()));

        panelControlsAtas.add(lblDaerah);
        panelControlsAtas.add(cmbDaerah);
        panelControlsAtas.add(btnTambahDaerah);
        panelControlsAtas.add(Box.createHorizontalStrut(4));
        panelControlsAtas.add(lblGudang);
        panelControlsAtas.add(cmbGudangAktif);
        panelControlsAtas.add(btnTambahGudang);
        panelControlsAtas.add(btnEditKapasitas);
        panelControlsAtas.add(btnKosongkanGudang);
        panelControlsAtas.add(Box.createHorizontalStrut(6));
        panelControlsAtas.add(lblRole);
        panelControlsAtas.add(cmbRole);
        panelHeaderDanRole.add(panelControlsAtas, BorderLayout.EAST);

        panelAtas.add(panelHeaderDanRole, BorderLayout.NORTH);
        panelAtas.add(panelStatusGudang, BorderLayout.SOUTH);
        add(panelAtas, BorderLayout.NORTH);

        // 3. Bagian Kiri: TabbedPane Operasi (Inbound, Pendaftaran, Outbound, Kasir)
        tabKiri.setFont(new Font("SansSerif", Font.BOLD, 12));
        tabKiri.addTab("Penerimaan Pasokan", panelOperasiPasokan);
        tabKiri.addTab("Registrasi Petani", panelRegistrasiPetani);
        tabKiri.addTab("Distribusi Pabrik", panelDistribusiPabrik);
        tabKiri.addTab("Kasir & Pembayaran", panelKasirPembayaran);

        // 4. Bagian Kanan: TabbedPane Monitoring, Tabel, & Dashboard KPI
        tabKanan.setFont(new Font("SansSerif", Font.BOLD, 12));

        tblTransaksiGudang = buatTabelKustom(transaksiGudangTableModel);
        tblSemuaTransaksi = buatTabelKustom(semuaTransaksiTableModel);
        tblPetani = buatTabelKustom(petaniTableModel);

        // Double-click untuk cetak kuitansi cepat
        tblTransaksiGudang.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) bukaKuitansiTerpilih(tblTransaksiGudang, transaksiGudangTableModel);
            }
        });
        tblSemuaTransaksi.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) bukaKuitansiTerpilih(tblSemuaTransaksi, semuaTransaksiTableModel);
            }
        });

        tabKanan.addTab("Transaksi Masuk Gudang", new JScrollPane(tblTransaksiGudang));
        tabKanan.addTab("Riwayat Semua Transaksi", new JScrollPane(tblSemuaTransaksi));
        tabKanan.addTab("Saldo Petani Mitra", new JScrollPane(tblPetani));
        tabKanan.addTab("Dashboard Mutu & Eksekutif", panelDashboardKPI);

        // Panel aksi tabel di kanan bawah
        JPanel panelAksiTabel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panelAksiTabel.setOpaque(false);

        JButton btnLihatKuitansiTabel = new JButton("Cetak Kuitansi Transaksi");
        btnLihatKuitansiTabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnLihatKuitansiTabel.setBackground(new Color(25, 118, 210));
        btnLihatKuitansiTabel.setForeground(Color.WHITE);
        btnLihatKuitansiTabel.setFocusPainted(false);
        btnLihatKuitansiTabel.addActionListener(e -> {
            int tabIdx = tabKanan.getSelectedIndex();
            if (tabIdx == 0) {
                bukaKuitansiTerpilih(tblTransaksiGudang, transaksiGudangTableModel);
            } else if (tabIdx == 1) {
                bukaKuitansiTerpilih(tblSemuaTransaksi, semuaTransaksiTableModel);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Silakan pilih baris transaksi pada Tab 'Transaksi Masuk Gudang' atau 'Riwayat Semua Transaksi'.",
                    "Pilih Transaksi", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnHapusData = new JButton("Hapus Data Terpilih");
        btnHapusData.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnHapusData.setBackground(new Color(198, 40, 40));
        btnHapusData.setForeground(Color.WHITE);
        btnHapusData.setFocusPainted(false);
        btnHapusData.addActionListener(e -> prosesHapusDataTerpilih());

        JButton btnEksporCsv = new JButton("Ekspor Rekapitulasi CSV");
        btnEksporCsv.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnEksporCsv.setBackground(new Color(46, 125, 50));
        btnEksporCsv.setForeground(Color.WHITE);
        btnEksporCsv.setFocusPainted(false);
        btnEksporCsv.addActionListener(e -> eksporLaporanCSV());

        panelAksiTabel.add(btnLihatKuitansiTabel);
        panelAksiTabel.add(btnHapusData);
        panelAksiTabel.add(btnEksporCsv);

        JPanel panelKananWrapper = new JPanel(new BorderLayout(4, 4));
        panelKananWrapper.add(tabKanan, BorderLayout.CENTER);
        panelKananWrapper.add(panelAksiTabel, BorderLayout.SOUTH);

        JSplitPane splitTengah = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabKiri, panelKananWrapper);
        splitTengah.setResizeWeight(0.48);
        splitTengah.setContinuousLayout(true);
        splitTengah.setDividerSize(6);
        add(splitTengah, BorderLayout.CENTER);

        // 5. Bagian Bawah: Panel Log Audit State & Invarian
        JPanel panelBawah = new JPanel(new BorderLayout(4, 4));
        panelBawah.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Log Audit Sistem & Validasi State Invarian",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(33, 37, 41)
            ),
            new EmptyBorder(4, 8, 6, 8)
        ));

        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(24, 26, 27));
        logArea.setForeground(new Color(224, 224, 224));
        logArea.setCaretColor(Color.WHITE);

        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setPreferredSize(new Dimension(100, 150));
        panelBawah.add(scrollLog, BorderLayout.CENTER);

        add(panelBawah, BorderLayout.SOUTH);
    }

    private void aturHakAksesPeran(int roleIndex) {
        switch (roleIndex) {
            case 0: // Admin
                panelOperasiPasokan.getBtnTahap1().setEnabled(true);
                panelOperasiPasokan.getBtnTahap2().setEnabled(true);
                tabKiri.setEnabledAt(0, true);
                tabKiri.setEnabledAt(1, true);
                tabKiri.setEnabledAt(2, true);
                tabKiri.setEnabledAt(3, true);
                break;
            case 1: // Grader Mutu
                panelOperasiPasokan.getBtnTahap1().setEnabled(true);
                panelOperasiPasokan.getBtnTahap2().setEnabled(false);
                tabKiri.setSelectedIndex(0);
                tabKiri.setEnabledAt(0, true);
                tabKiri.setEnabledAt(1, false);
                tabKiri.setEnabledAt(2, false);
                tabKiri.setEnabledAt(3, false);
                break;
            case 2: // Gudang & Logistik
                panelOperasiPasokan.getBtnTahap1().setEnabled(false);
                panelOperasiPasokan.getBtnTahap2().setEnabled(true);
                tabKiri.setEnabledAt(0, true);
                tabKiri.setEnabledAt(1, true);
                tabKiri.setEnabledAt(2, true);
                tabKiri.setEnabledAt(3, false);
                break;
            case 3: // Kasir & Keuangan
                panelOperasiPasokan.getBtnTahap1().setEnabled(false);
                panelOperasiPasokan.getBtnTahap2().setEnabled(false);
                tabKiri.setSelectedIndex(3);
                tabKiri.setEnabledAt(0, false);
                tabKiri.setEnabledAt(1, false);
                tabKiri.setEnabledAt(2, false);
                tabKiri.setEnabledAt(3, true);
                break;
        }
        catatLogInformasi("Hak akses peran diubah ke: " + cmbRole.getSelectedItem());
    }

    private JTable buatTabelKustom(javax.swing.table.TableModel model) {
        JTable tbl = new JTable(model);
        tbl.setRowHeight(24);
        tbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setGridColor(new Color(222, 226, 230));
        return tbl;
    }

    private JMenuBar buatMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 1. Menu Berkas
        JMenu menuBerkas = new JMenu("Berkas");
        menuBerkas.setMnemonic('B');

        JMenuItem miSimpanDB = new JMenuItem("Simpan Database");
        miSimpanDB.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        miSimpanDB.addActionListener(e -> simpanDatabaseKeFile(true));
        menuBerkas.add(miSimpanDB);

        JMenuItem miMuatDB = new JMenuItem("Muat Ulang Database");
        miMuatDB.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        miMuatDB.addActionListener(e -> muatDatabaseDariFile());
        menuBerkas.add(miMuatDB);

        menuBerkas.addSeparator();

        JMenuItem miHapusData = new JMenuItem("Hapus Data Terpilih");
        miHapusData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        miHapusData.addActionListener(e -> prosesHapusDataTerpilih());
        menuBerkas.add(miHapusData);

        menuBerkas.addSeparator();

        JMenuItem miEksporCsv = new JMenuItem("Ekspor Rekapitulasi CSV");
        miEksporCsv.addActionListener(e -> eksporLaporanCSV());
        menuBerkas.add(miEksporCsv);

        menuBerkas.addSeparator();

        JMenuItem miKeluar = new JMenuItem("Keluar");
        miKeluar.addActionListener(e -> {
            simpanDatabaseKeFile(false);
            System.exit(0);
        });
        menuBerkas.add(miKeluar);

        // 2. Menu Navigasi Modul
        JMenu menuNavigasi = new JMenu("Modul Operasi");
        menuNavigasi.setMnemonic('M');

        JMenu menuPilihDaerah = new JMenu("Pilih Daerah Operasional");
        for (String d : daftarDaerah) {
            JMenuItem miD = new JMenuItem("Daerah " + d);
            miD.addActionListener(e -> cmbDaerah.setSelectedItem(d));
            menuPilihDaerah.add(miD);
        }
        menuPilihDaerah.addSeparator();
        JMenuItem miTambahDaerah = new JMenuItem("Tambah Daerah Baru...");
        miTambahDaerah.addActionListener(e -> bukaDialogTambahDaerah());
        menuPilihDaerah.add(miTambahDaerah);
        menuNavigasi.add(menuPilihDaerah);

        JMenu menuPilihGudang = new JMenu("Pilih Gudang Aktif");
        for (GudangTembakau g : daftarGudang.values()) {
            String dae = gudangDaerahMap.getOrDefault(g.getKodeGudang(), "Sumenep");
            String zona = wilayahGudangMap.getOrDefault(g.getKodeGudang(), "Wilayah");
            JMenuItem miG = new JMenuItem("[" + dae + " - " + zona + "] " + g.getNamaGudang() + " (" + formatAngka.format(g.getKapasitasMaksKg()) + " kg)");
            miG.addActionListener(e -> {
                cmbDaerah.setSelectedItem(dae);
                cmbGudangAktif.setSelectedItem(g);
            });
            menuPilihGudang.add(miG);
        }
        menuNavigasi.add(menuPilihGudang);

        JMenuItem miTambahGudang = new JMenuItem("Tambah Gudang Baru...");
        miTambahGudang.addActionListener(e -> bukaDialogTambahGudang());
        menuNavigasi.add(miTambahGudang);

        JMenuItem miEditKapasitas = new JMenuItem("Ubah Kapasitas Gudang Aktif...");
        miEditKapasitas.addActionListener(e -> bukaDialogEditKapasitas());
        menuNavigasi.add(miEditKapasitas);

        JMenuItem miKosongkanGudang = new JMenuItem("Kosongkan / Reset Muatan Gudang Aktif...");
        miKosongkanGudang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        miKosongkanGudang.addActionListener(e -> kosongkanMuatanGudangAktif());
        menuNavigasi.add(miKosongkanGudang);
        menuNavigasi.addSeparator();

        JMenuItem miPasokan = new JMenuItem("Penerimaan Pasokan");
        miPasokan.addActionListener(e -> tabKiri.setSelectedIndex(0));
        menuNavigasi.add(miPasokan);

        JMenuItem miPetani = new JMenuItem("Registrasi Petani Mitra");
        miPetani.addActionListener(e -> tabKiri.setSelectedIndex(1));
        menuNavigasi.add(miPetani);

        JMenuItem miDistribusi = new JMenuItem("Distribusi Pabrik");
        miDistribusi.addActionListener(e -> tabKiri.setSelectedIndex(2));
        menuNavigasi.add(miDistribusi);

        JMenuItem miKasir = new JMenuItem("Kasir & Pelunasan Hak");
        miKasir.addActionListener(e -> tabKiri.setSelectedIndex(3));
        menuNavigasi.add(miKasir);

        // 3. Menu Pengujian Sistem
        JMenu menuUji = new JMenu("Pengujian Sistem");
        menuUji.setMnemonic('P');

        JMenuItem miResetFixture = new JMenuItem("Reset Data Awal (650 kg)");
        miResetFixture.addActionListener(e -> resetFixtureBab4());
        menuUji.add(miResetFixture);

        menuUji.addSeparator();

        JMenuItem miUjiS1 = new JMenuItem("Skenario S1: Penerimaan Normal (200 kg)");
        miUjiS1.addActionListener(e -> presetSkenarioS1());
        menuUji.add(miUjiS1);

        JMenuItem miUjiS2 = new JMenuItem("Skenario S2: Mutu Ditolak (21.5% Kadar Air)");
        miUjiS2.addActionListener(e -> presetSkenarioS2());
        menuUji.add(miUjiS2);

        JMenuItem miUjiS3 = new JMenuItem("Skenario S3: Melebihi Kapasitas (500 kg)");
        miUjiS3.addActionListener(e -> presetSkenarioS3());
        menuUji.add(miUjiS3);

        JMenuItem miUjiS4 = new JMenuItem("Skenario S4: Pelanggaran Urutan Verifikasi");
        miUjiS4.addActionListener(e -> presetSkenarioS4());
        menuUji.add(miUjiS4);

        JMenuItem miUjiS5 = new JMenuItem("Skenario S5: Pencegahan Simpan Ganda");
        miUjiS5.addActionListener(e -> presetSkenarioS5());
        menuUji.add(miUjiS5);

        // 4. Menu Bantuan
        JMenu menuBantuan = new JMenu("Bantuan");
        menuBantuan.setMnemonic('T');

        JMenuItem miTentang = new JMenuItem("Tentang Sistem");
        miTentang.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Sistem Informasi Manajemen Rantai Pasok Tembakau\n"
                + "Arsitektur Domain-Driven Object-Oriented dengan State Invariant Protection\n\n"
                + "Pengembang: Moch. Ilham Abdul Aziz (NRP: 3125522014)\n"
                + "Program Studi D4 Teknik Informatika - PENS 2026",
                "Tentang Sistem", JOptionPane.INFORMATION_MESSAGE);
        });
        menuBantuan.add(miTentang);

        menuBar.add(menuBerkas);
        menuBar.add(menuNavigasi);
        menuBar.add(menuUji);
        menuBar.add(menuBantuan);

        return menuBar;
    }

    private JToolBar buatToolbarPresentasi() {
        JToolBar toolBar = new JToolBar("Toolbar Skenario Pengujian");
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(240, 243, 246));
        toolBar.setBorder(new EmptyBorder(4, 6, 4, 6));

        JLabel lblJudul = new JLabel("Skenario Uji: ");
        lblJudul.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblJudul.setForeground(new Color(40, 53, 64));
        toolBar.add(lblJudul);

        JButton btnFixture = buatTombolToolbar("Reset Data (650 kg)", new Color(33, 150, 243));
        btnFixture.addActionListener(e -> resetFixtureBab4());
        toolBar.add(btnFixture);

        toolBar.addSeparator(new Dimension(6, 20));

        JButton btnS1 = buatTombolToolbar("Uji S1: Normal", new Color(46, 125, 50));
        btnS1.addActionListener(e -> presetSkenarioS1());
        toolBar.add(btnS1);

        JButton btnS2 = buatTombolToolbar("Uji S2: Mutu Ditolak", new Color(198, 40, 40));
        btnS2.addActionListener(e -> presetSkenarioS2());
        toolBar.add(btnS2);

        JButton btnS3 = buatTombolToolbar("Uji S3: Kapasitas Penuh", new Color(230, 149, 0));
        btnS3.addActionListener(e -> presetSkenarioS3());
        toolBar.add(btnS3);

        JButton btnS4 = buatTombolToolbar("Uji S4: Belum Verifikasi", new Color(123, 31, 162));
        btnS4.addActionListener(e -> presetSkenarioS4());
        toolBar.add(btnS4);

        JButton btnS5 = buatTombolToolbar("Uji S5: Simpan Ganda", new Color(69, 90, 100));
        btnS5.addActionListener(e -> presetSkenarioS5());
        toolBar.add(btnS5);

        toolBar.addSeparator(new Dimension(8, 20));

        JButton btnSimpanDB = buatTombolToolbar("Simpan DB", new Color(0, 137, 123));
        btnSimpanDB.addActionListener(e -> simpanDatabaseKeFile(true));
        toolBar.add(btnSimpanDB);

        JButton btnMuatDB = buatTombolToolbar("Muat DB", new Color(92, 107, 115));
        btnMuatDB.addActionListener(e -> muatDatabaseDariFile());
        toolBar.add(btnMuatDB);

        toolBar.add(Box.createHorizontalGlue());

        JButton btnClearLog = buatTombolToolbar("Bersihkan Log", new Color(117, 117, 117));
        btnClearLog.addActionListener(e -> logArea.setText(""));
        toolBar.add(btnClearLog);

        toolBar.addSeparator(new Dimension(6, 20));

        JButton btnToolbarKosongkan = buatTombolToolbar("Kosongkan Gudang", new Color(198, 40, 40));
        btnToolbarKosongkan.setToolTipText("Kosongkan / Reset seluruh muatan fisik gudang aktif saat ini");
        btnToolbarKosongkan.addActionListener(e -> kosongkanMuatanGudangAktif());
        toolBar.add(btnToolbarKosongkan);

        return toolBar;
    }

    private JButton buatTombolToolbar(String teks, Color warnaBg) {
        JButton btn = new JButton(teks);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBackground(warnaBg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(3, 6, 3, 6));
        return btn;
    }

    private void bukaKuitansiTerpilih(JTable tabel, TransaksiTableModel model) {
        int selectedRow = tabel.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Silakan klik salah satu baris transaksi pada tabel terlebih dahulu.",
                "Pilih Baris Transaksi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = tabel.convertRowIndexToModel(selectedRow);
        TransaksiPasok trx = model.getTransaksiAt(modelRow);
        if (trx != null) {
            new DialogKuitansiPasokan(this, trx).setVisible(true);
        }
    }

    private void inisialisasiGudangDaerahStandar(String daerah, String prefix,
                                                 double kapPst, double kapBrt, double kapTmr, double kapSlt) {
        GudangTembakau gPst = new GudangTembakau("GDG-" + prefix + "-01", "Gudang Penyangga Pusat", kapPst);
        GudangTembakau gBrt = new GudangTembakau("GDG-" + prefix + "-02", "Gudang Pengumpul Barat", kapBrt);
        GudangTembakau gTmr = new GudangTembakau("GDG-" + prefix + "-03", "Gudang Penyangga Timur", kapTmr);
        GudangTembakau gSlt = new GudangTembakau("GDG-" + prefix + "-04", "Gudang Pembelian Selatan", kapSlt);

        daftarGudang.put(gPst.getKodeGudang(), gPst);
        daftarGudang.put(gBrt.getKodeGudang(), gBrt);
        daftarGudang.put(gTmr.getKodeGudang(), gTmr);
        daftarGudang.put(gSlt.getKodeGudang(), gSlt);

        wilayahGudangMap.put(gPst.getKodeGudang(), "Wilayah Pusat");
        wilayahGudangMap.put(gBrt.getKodeGudang(), "Wilayah Barat");
        wilayahGudangMap.put(gTmr.getKodeGudang(), "Wilayah Timur");
        wilayahGudangMap.put(gSlt.getKodeGudang(), "Wilayah Selatan");

        gudangDaerahMap.put(gPst.getKodeGudang(), daerah);
        gudangDaerahMap.put(gBrt.getKodeGudang(), daerah);
        gudangDaerahMap.put(gTmr.getKodeGudang(), daerah);
        gudangDaerahMap.put(gSlt.getKodeGudang(), daerah);
    }

    private void perbaruiPilihanGudangPerDaerah(String namaDaerah) {
        this.daerahAktif = namaDaerah;
        cmbGudangAktif.removeAllItems();
        for (GudangTembakau g : daftarGudang.values()) {
            String d = gudangDaerahMap.getOrDefault(g.getKodeGudang(), "Sumenep");
            if (d.equalsIgnoreCase(namaDaerah)) {
                cmbGudangAktif.addItem(g);
            }
        }
        if (cmbGudangAktif.getItemCount() > 0) {
            cmbGudangAktif.setSelectedIndex(0);
            terapkanGudangAktif((GudangTembakau) cmbGudangAktif.getSelectedItem());
        }
    }

    private void terapkanGudangAktif(GudangTembakau g) {
        if (g == null) return;
        this.gudangAktif = g;
        String wilayah = wilayahGudangMap.getOrDefault(g.getKodeGudang(), "-");
        String daerah = gudangDaerahMap.getOrDefault(g.getKodeGudang(), daerahAktif);

        if (panelStatusGudang != null) {
            panelStatusGudang.setGudang(g);
            panelStatusGudang.setDaerah(daerah);
            panelStatusGudang.setWilayah(wilayah);
            panelStatusGudang.perbaruiStatus();
        }

        if (panelOperasiPasokan != null) {
            panelOperasiPasokan.setPenerimaPasokan(g);
            panelOperasiPasokan.resetForm();
        }

        if (panelDistribusiPabrik != null) {
            panelDistribusiPabrik.setGudang(g);
        }

        if (transaksiGudangTableModel != null) {
            transaksiGudangTableModel.setDaftarTransaksi(g.getDaftarTransaksi());
        }

        if (petaniTableModel != null) {
            petaniTableModel.setGudangAktif(g);
            petaniTableModel.refreshData();
        }

        String labelIdGudang = daerah + " - " + wilayah;
        if (panelKasirPembayaran != null) {
            panelKasirPembayaran.setIdentitasGudang(g.getNamaGudang(), labelIdGudang);
            panelKasirPembayaran.perbaruiDaftarPetani(daftarPetani, g.getDaftarTransaksi());
        }

        if (panelDashboardKPI != null) {
            panelDashboardKPI.setIdentitasGudang(g.getNamaGudang(), labelIdGudang);
            panelDashboardKPI.perbaruiData(daftarPetani, g.getDaftarTransaksi());
        }
    }

    private void kurangiSaldoPetaniInternal(Petani p, double beratKg) {
        if (p == null || beratKg <= 0.0) return;
        try {
            java.lang.reflect.Field fTotal = Petani.class.getDeclaredField("totalSetoranKg");
            fTotal.setAccessible(true);
            double lama = fTotal.getDouble(p);
            double baru = Math.max(0.0, lama - beratKg);
            fTotal.setDouble(p, baru);
        } catch (Exception ignored) {}
    }

    public void kosongkanMuatanGudangAktif() {
        if (!(gudangAktif instanceof GudangTembakau)) {
            JOptionPane.showMessageDialog(this, "Pilih gudang aktif terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        GudangTembakau gt = (GudangTembakau) gudangAktif;
        String wil = wilayahGudangMap.getOrDefault(gt.getKodeGudang(), "-");
        String dae = gudangDaerahMap.getOrDefault(gt.getKodeGudang(), daerahAktif);

        int konfirmasi = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin mengosongkan / mereset seluruh muatan fisik gudang ini?\n\n"
            + "• Gudang         : " + gt.getNamaGudang() + " [" + gt.getKodeGudang() + "]\n"
            + "• Daerah         : " + dae + " (" + wil + ")\n"
            + "• Muatan Fisik   : " + formatAngka.format(gt.getStokTersimpanKg()) + " kg ("
            + gt.getDaftarTransaksi().size() + " transaksi)\n"
            + "• Kapasitas Maks : " + formatAngka.format(gt.getKapasitasMaksKg()) + " kg\n\n"
            + "Tindakan ini akan mengosongkan persediaan fisik menjadi 0,00 kg (misal setelah\n"
            + "seluruh tembakau selesai dikirim ke pabrik rokok mitra).\n"
            + "Gudang wilayah lain di daerah ini atau daerah lain tidak akan terpengaruh.",
            "Konfirmasi Kosongkan Muatan Gudang",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (konfirmasi != JOptionPane.YES_OPTION) return;

        eksekusiKosongkanGudang(gt, true);
    }

    public void eksekusiKosongkanGudang(GudangTembakau gt, boolean tampilkanPesan) {
        if (gt == null) return;
        String wil = wilayahGudangMap.getOrDefault(gt.getKodeGudang(), "-");
        String dae = gudangDaerahMap.getOrDefault(gt.getKodeGudang(), daerahAktif);
        double beratLama = gt.getStokTersimpanKg();
        int jmlTrx = gt.getDaftarTransaksi().size();

        // 1. Ambil salinan daftar transaksi gudang
        List<TransaksiPasok> trxList = new ArrayList<>(gt.getDaftarTransaksi());

        // 2. Bersihkan transaksi dari riwayatSemuaTransaksi dan kurangi saldo petani terkait
        for (TransaksiPasok t : trxList) {
            riwayatSemuaTransaksi.remove(t);
            if (t.getPetani() != null && t.getTembakau() != null) {
                kurangiSaldoPetaniInternal(t.getPetani(), t.getTembakau().getBeratKg());
            }
        }

        // 3. Kosongkan stok fisik dan daftar transaksi di gudang via refleksi
        kosongkanGudangInternal(gt);

        // 4. Perbarui seluruh UI terkait
        terapkanGudangAktif(gt);
        semuaTransaksiTableModel.refreshData();

        catatLogInformasi("[RESET GUDANG] Seluruh muatan fisik gudang '" + gt.getNamaGudang() + "' [" + gt.getKodeGudang()
            + "] di Daerah " + dae + " (" + wil + ") sebesar " + formatAngka.format(beratLama)
            + " kg (" + jmlTrx + " transaksi) telah berhasil dikosongkan (Stok: 0,00 kg).");

        simpanDatabaseKeFile(false);

        if (tampilkanPesan) {
            JOptionPane.showMessageDialog(this,
                "Gudang '" + gt.getNamaGudang() + "' telah berhasil dikosongkan!\n\n"
                + "• Stok Fisik Tersimpan: 0,00 kg (0,0%)\n"
                + "• Total Transaksi Aktif: 0 transaksi\n\n"
                + "Gudang siap menerima siklus pasokan tembakau berikutnya.",
                "Gudang Berhasil Dikosongkan", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void bukaDialogTambahDaerah() {
        String input = JOptionPane.showInputDialog(this,
            "Masukkan Nama Daerah / Kabupaten Baru (misal: Tuban, Jember, Bondowoso):",
            "Tambah Daerah Baru", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isBlank()) {
            String namaDaerah = input.trim();
            for (String d : daftarDaerah) {
                if (d.equalsIgnoreCase(namaDaerah)) {
                    JOptionPane.showMessageDialog(this, "Daerah '" + namaDaerah + "' sudah ada di sistem.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                    cmbDaerah.setSelectedItem(d);
                    return;
                }
            }
            daftarDaerah.add(namaDaerah);
            cmbDaerah.addItem(namaDaerah);

            String prefix = (namaDaerah.length() >= 3 ? namaDaerah.substring(0, 3).toUpperCase() : "REG");
            inisialisasiGudangDaerahStandar(namaDaerah, prefix, 1000.0, 800.0, 1000.0, 500.0);

            cmbDaerah.setSelectedItem(namaDaerah);
            catatLogInformasi("[DAERAH BARU] Daerah operasional '" + namaDaerah + "' berhasil didaftarkan beserta 4 gudang zonasi.");
            simpanDatabaseKeFile(false);
        }
    }

    private void bukaDialogEditKapasitas() {
        if (gudangAktif instanceof GudangTembakau) {
            GudangTembakau g = (GudangTembakau) gudangAktif;
            String wil = wilayahGudangMap.getOrDefault(g.getKodeGudang(), "-");
            String dae = gudangDaerahMap.getOrDefault(g.getKodeGudang(), daerahAktif);
            DialogEditKapasitasGudang dlg = new DialogEditKapasitasGudang(this, g, dae, wil, kapasitasBaru -> {
                panelStatusGudang.perbaruiStatus();
                cmbGudangAktif.repaint();
                catatLogInformasi("[UBAH KAPASITAS] Kapasitas maksimum gudang '" + g.getNamaGudang()
                    + "' [" + g.getKodeGudang() + "] diubah menjadi " + formatAngka.format(kapasitasBaru) + " kg.");
                simpanDatabaseKeFile(false);
            });
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Pilih gudang aktif terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void bukaDialogTambahGudang() {
        DialogTambahGudang dlg = new DialogTambahGudang(this, daftarDaerah, daerahAktif, daftarGudang, wilayahGudangMap, gudangDaerahMap, gBaru -> {
            String dae = gudangDaerahMap.getOrDefault(gBaru.getKodeGudang(), daerahAktif);
            if (dae.equalsIgnoreCase(daerahAktif)) {
                cmbGudangAktif.addItem(gBaru);
                cmbGudangAktif.setSelectedItem(gBaru);
            } else {
                cmbDaerah.setSelectedItem(dae);
                cmbGudangAktif.setSelectedItem(gBaru);
            }
            catatLogInformasi("[GUDANG BARU] Entitas '" + gBaru.getNamaGudang() + "' [" + gBaru.getKodeGudang()
                + "] di Daerah " + dae + " (" + wilayahGudangMap.getOrDefault(gBaru.getKodeGudang(), "-")
                + ") siap beroperasi.");
            simpanDatabaseKeFile(false);
        });
        dlg.setVisible(true);
    }

    private void pasangAuditWrapper() {
        panelOperasiPasokan.setAuditWrapper((namaAksi, aksiRunnable) -> {
            PenerimaPasokan gTarget = (gudangAktif != null) ? gudangAktif : gudang;
            StateSnapshot awal = new StateSnapshot(gTarget, daftarPetani, panelOperasiPasokan.getTransaksiAktif());
            catatLogAwal(namaAksi, awal);

            try {
                aksiRunnable.run();
            } finally {
                // Pola 3: Blok finally khusus untuk pembersihan UI & sinkronisasi
                StateSnapshot akhir = new StateSnapshot(gTarget, daftarPetani, panelOperasiPasokan.getTransaksiAktif());
                catatLogAkhir(namaAksi, awal, akhir);

                panelStatusGudang.perbaruiStatus();
                petaniTableModel.refreshData();
                if (gTarget instanceof GudangTembakau) {
                    GudangTembakau gt = (GudangTembakau) gTarget;
                    transaksiGudangTableModel.setDaftarTransaksi(gt.getDaftarTransaksi());
                    panelKasirPembayaran.perbaruiDaftarPetani(daftarPetani, gt.getDaftarTransaksi());
                    panelDashboardKPI.perbaruiData(daftarPetani, gt.getDaftarTransaksi());
                } else {
                    panelKasirPembayaran.perbaruiDaftarPetani(daftarPetani, riwayatSemuaTransaksi);
                    panelDashboardKPI.perbaruiData(daftarPetani, riwayatSemuaTransaksi);
                }
                semuaTransaksiTableModel.refreshData();

                logArea.setCaretPosition(logArea.getDocument().getLength());
                simpanDatabaseKeFile(false);
            }
        });
    }

    private void prosesHapusDataTerpilih() {
        int tabIdx = tabKanan.getSelectedIndex();
        if (tabIdx == 0) {
            // Tab 0: Transaksi Masuk Gudang (Gudang Aktif)
            int selectedRow = tblTransaksiGudang.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                    "Silakan pilih baris transaksi pada tabel 'Transaksi Masuk Gudang' yang ingin dihapus.",
                    "Pilih Transaksi Gudang", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int modelRow = tblTransaksiGudang.convertRowIndexToModel(selectedRow);
            TransaksiPasok trx = transaksiGudangTableModel.getTransaksiAt(modelRow);
            if (trx == null) return;

            int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin membatalkan/menghapus transaksi masuk ini dari gudang aktif?\n\n"
                + "• ID Transaksi : " + trx.getIdTransaksi() + "\n"
                + "• Petani Mitra : " + (trx.getPetani() != null ? trx.getPetani().getNama() : "-") + "\n"
                + "• Berat Fisik  : " + formatAngka.format(trx.getTembakau().getBeratKg()) + " kg\n"
                + "• Nilai Total  : " + formatRupiah.format(trx.getTotalPembayaran()) + "\n\n"
                + "Muatan fisik gudang aktif akan dipotong sebesar " + formatAngka.format(trx.getTembakau().getBeratKg()) + " kg,\n"
                + "saldo setoran petani akan dikurangi secara sinkron, dan transaksi dihapus.",
                "Konfirmasi Hapus Transaksi Gudang", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (konfirmasi == JOptionPane.YES_OPTION) {
                if (gudangAktif instanceof GudangTembakau) {
                    GudangTembakau gt = (GudangTembakau) gudangAktif;
                    try {
                        java.lang.reflect.Field fDaftar = GudangTembakau.class.getDeclaredField("daftarTransaksi");
                        fDaftar.setAccessible(true);
                        ((List<?>) fDaftar.get(gt)).remove(trx);

                        java.lang.reflect.Field fStok = GudangTembakau.class.getDeclaredField("stokTersimpanKg");
                        fStok.setAccessible(true);
                        double beratTrx = trx.getTembakau().getBeratKg();
                        double stokBaru = Math.max(0.0, gt.getStokTersimpanKg() - beratTrx);
                        fStok.setDouble(gt, stokBaru);
                    } catch (Exception ignored) {}
                }
                if (trx.getPetani() != null) {
                    kurangiSaldoPetaniInternal(trx.getPetani(), trx.getTembakau().getBeratKg());
                }
                riwayatSemuaTransaksi.remove(trx);
                if (gudangAktif instanceof GudangTembakau) {
                    terapkanGudangAktif((GudangTembakau) gudangAktif);
                }
                semuaTransaksiTableModel.refreshData();
                catatLogInformasi("[HAPUS TRANSAKSI GUDANG] Transaksi " + trx.getIdTransaksi()
                    + " (" + formatAngka.format(trx.getTembakau().getBeratKg()) + " kg) dibatalkan dari gudang.");
                simpanDatabaseKeFile(false);
            }
        } else if (tabIdx == 1) {
            // Tab 1: Riwayat Semua Transaksi
            int selectedRow = tblSemuaTransaksi.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                    "Silakan pilih baris transaksi pada tabel Riwayat Semua Transaksi yang ingin dihapus.",
                    "Pilih Transaksi", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int modelRow = tblSemuaTransaksi.convertRowIndexToModel(selectedRow);
            TransaksiPasok trx = semuaTransaksiTableModel.getTransaksiAt(modelRow);
            if (trx == null) return;

            String infoGudang = "";
            GudangTembakau gudangPemilik = null;
            for (GudangTembakau g : daftarGudang.values()) {
                if (g.getDaftarTransaksi().contains(trx)) {
                    gudangPemilik = g;
                    infoGudang = "\n\nTransaksi ini tersimpan di: " + g.getNamaGudang()
                        + " [" + g.getKodeGudang() + "].\nMenghapus akan memotong muatan fisik gudang tersebut.";
                    break;
                }
            }

            int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus transaksi ini dari riwayat sistem?\n\n"
                + "• ID Transaksi : " + trx.getIdTransaksi() + "\n"
                + "• Petani       : " + (trx.getPetani() != null ? trx.getPetani().getNama() : "-") + "\n"
                + "• Status       : " + trx.getStatus().getLabel() + "\n"
                + "• Berat        : " + formatAngka.format(trx.getTembakau().getBeratKg()) + " kg"
                + infoGudang + "\n\n"
                + "Lanjutkan penghapusan?",
                "Konfirmasi Hapus Transaksi", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (konfirmasi == JOptionPane.YES_OPTION) {
                if (gudangPemilik != null) {
                    try {
                        java.lang.reflect.Field fDaftar = GudangTembakau.class.getDeclaredField("daftarTransaksi");
                        fDaftar.setAccessible(true);
                        ((List<?>) fDaftar.get(gudangPemilik)).remove(trx);

                        java.lang.reflect.Field fStok = GudangTembakau.class.getDeclaredField("stokTersimpanKg");
                        fStok.setAccessible(true);
                        double stokBaru = Math.max(0.0, gudangPemilik.getStokTersimpanKg() - trx.getTembakau().getBeratKg());
                        fStok.setDouble(gudangPemilik, stokBaru);
                    } catch (Exception ignored) {}
                }
                if (trx.getPetani() != null) {
                    kurangiSaldoPetaniInternal(trx.getPetani(), trx.getTembakau().getBeratKg());
                }
                riwayatSemuaTransaksi.remove(trx);
                semuaTransaksiTableModel.refreshData();
                if (gudangAktif instanceof GudangTembakau) {
                    terapkanGudangAktif((GudangTembakau) gudangAktif);
                }
                catatLogInformasi("[HAPUS TRANSAKSI] Transaksi ID: " + trx.getIdTransaksi() + " berhasil dihapus.");
                simpanDatabaseKeFile(false);
            }
        } else if (tabIdx == 2) {
            // Tab 2: Saldo Petani Mitra (Diizinkan hapus meski sudah pernah setoran & diterima)
            int selectedRow = tblPetani.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                    "Silakan pilih baris petani pada tabel Saldo Petani Mitra yang ingin dihapus.",
                    "Pilih Petani", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int modelRow = tblPetani.convertRowIndexToModel(selectedRow);
            Petani p = petaniTableModel.getPetaniAt(modelRow);
            if (p == null) return;

            // Kumpulkan seluruh transaksi terkait petani ini
            List<TransaksiPasok> trxPetani = new ArrayList<>();
            for (TransaksiPasok t : riwayatSemuaTransaksi) {
                if (t.getPetani() != null && t.getPetani().getNik().equals(p.getNik())) {
                    trxPetani.add(t);
                }
            }
            for (GudangTembakau g : daftarGudang.values()) {
                for (TransaksiPasok t : g.getDaftarTransaksi()) {
                    if (t.getPetani() != null && t.getPetani().getNik().equals(p.getNik()) && !trxPetani.contains(t)) {
                        trxPetani.add(t);
                    }
                }
            }

            String infoSetoran = "";
            if (!trxPetani.isEmpty()) {
                double totalBerat = 0.0;
                for (TransaksiPasok t : trxPetani) {
                    if (t.getTembakau() != null) totalBerat += t.getTembakau().getBeratKg();
                }
                infoSetoran = "\n\nINFORMASI TRANSAKSI AKTIF:\n"
                    + "Petani ini memiliki " + trxPetani.size() + " transaksi setoran (" + formatAngka.format(totalBerat) + " kg).\n"
                    + "Menghapus petani ini akan MEMBATALKAN seluruh transaksinya,\n"
                    + "MEMOTONG muatan fisik gudang yang bersangkutan, dan membersihkan kasir.";
            }

            int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus data petani mitra ini?\n\n"
                + "• Nama Lengkap : " + p.getNama() + "\n"
                + "• NIK          : " + p.getNik() + "\n"
                + "• Asal Desa    : " + p.getAsalDesa() + "\n"
                + "• Luas Lahan   : " + p.getLuasLahanHa() + " Ha"
                + infoSetoran + "\n\n"
                + "Lanjutkan penghapusan petani dan seluruh data terkait?",
                "Konfirmasi Hapus Petani Mitra", JOptionPane.YES_NO_OPTION,
                !trxPetani.isEmpty() ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);

            if (konfirmasi == JOptionPane.YES_OPTION) {
                // Keluarkan transaksi petani dari seluruh gudang dan kurangi stok fisik gudang
                for (TransaksiPasok t : trxPetani) {
                    riwayatSemuaTransaksi.remove(t);
                    for (GudangTembakau g : daftarGudang.values()) {
                        if (g.getDaftarTransaksi().contains(t)) {
                            try {
                                java.lang.reflect.Field fDaftar = GudangTembakau.class.getDeclaredField("daftarTransaksi");
                                fDaftar.setAccessible(true);
                                ((List<?>) fDaftar.get(g)).remove(t);

                                java.lang.reflect.Field fStok = GudangTembakau.class.getDeclaredField("stokTersimpanKg");
                                fStok.setAccessible(true);
                                double beratTrx = (t.getTembakau() != null) ? t.getTembakau().getBeratKg() : 0.0;
                                double stokBaru = Math.max(0.0, g.getStokTersimpanKg() - beratTrx);
                                fStok.setDouble(g, stokBaru);
                            } catch (Exception ignored) {}
                        }
                    }
                }

                daftarPetani.remove(p);
                petaniTableModel.refreshData();
                panelOperasiPasokan.perbaruiDaftarPetani(daftarPetani);
                if (gudangAktif instanceof GudangTembakau) {
                    terapkanGudangAktif((GudangTembakau) gudangAktif);
                }
                semuaTransaksiTableModel.refreshData();
                catatLogInformasi("[HAPUS PETANI] Data petani mitra '" + p.getNama() + "' (NIK: " + p.getNik()
                    + ") beserta " + trxPetani.size() + " transaksi setorannya berhasil dihapus.");
                simpanDatabaseKeFile(false);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Silakan pilih tab 'Transaksi Masuk Gudang', 'Riwayat Semua Transaksi', atau 'Saldo Petani Mitra' terlebih dahulu.",
                "Pilih Tab Data", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void sinkronkanRiwayatTransaksi(TransaksiPasok trx) {
        if (trx == null) return;
        boolean ditemukan = false;
        for (int i = 0; i < riwayatSemuaTransaksi.size(); i++) {
            if (riwayatSemuaTransaksi.get(i).getIdTransaksi().equals(trx.getIdTransaksi())) {
                riwayatSemuaTransaksi.set(i, trx);
                ditemukan = true;
                break;
            }
        }
        if (!ditemukan) {
            riwayatSemuaTransaksi.add(trx);
        }
        semuaTransaksiTableModel.refreshData();
        PenerimaPasokan gTarget = (gudangAktif != null) ? gudangAktif : gudang;
        if (gTarget instanceof GudangTembakau) {
            GudangTembakau gt = (GudangTembakau) gTarget;
            transaksiGudangTableModel.setDaftarTransaksi(gt.getDaftarTransaksi());
            panelKasirPembayaran.perbaruiDaftarPetani(daftarPetani, gt.getDaftarTransaksi());
            panelDashboardKPI.perbaruiData(daftarPetani, gt.getDaftarTransaksi());
        } else {
            panelKasirPembayaran.perbaruiDaftarPetani(daftarPetani, riwayatSemuaTransaksi);
            panelDashboardKPI.perbaruiData(daftarPetani, riwayatSemuaTransaksi);
        }
    }

    private void kosongkanGudangInternal(PenerimaPasokan g) {
        if (g instanceof GudangTembakau) {
            try {
                java.lang.reflect.Field fDaftar = GudangTembakau.class.getDeclaredField("daftarTransaksi");
                fDaftar.setAccessible(true);
                ((List<?>) fDaftar.get(g)).clear();

                java.lang.reflect.Field fStok = GudangTembakau.class.getDeclaredField("stokTersimpanKg");
                fStok.setAccessible(true);
                fStok.setDouble(g, 0.0);
            } catch (Exception ignored) {
            }
        }
    }

    private void muatDataAwalAtauFixture() {
        Path dbPath = Paths.get(DataStorageService.DEFAULT_FILE_PATH);
        if (Files.exists(dbPath)) {
            try {
                PenerimaPasokan gTarget = (gudangAktif != null) ? gudangAktif : gudang;
                for (GudangTembakau g : daftarGudang.values()) {
                    kosongkanGudangInternal(g);
                }
                DataStorageService.StorageData data = DataStorageService.muatDariFile(null, daftarGudang, gTarget);
                daftarPetani.clear();
                daftarPetani.addAll(data.daftarPetani);
                riwayatSemuaTransaksi.clear();
                riwayatSemuaTransaksi.addAll(data.riwayatSemuaTransaksi);

                petaniTableModel.setDaftarPetani(daftarPetani);
                panelOperasiPasokan.perbaruiDaftarPetani(daftarPetani);
                semuaTransaksiTableModel.setDaftarTransaksi(riwayatSemuaTransaksi);

                if (!data.daftarGudang.isEmpty()) {
                    for (DataStorageService.GudangRecord gr : data.daftarGudang) {
                        String dae = (gr.daerah != null && !gr.daerah.isBlank()) ? gr.daerah : "Sumenep";
                        if (!daftarDaerah.contains(dae)) {
                            daftarDaerah.add(dae);
                            cmbDaerah.addItem(dae);
                        }
                        if (!daftarGudang.containsKey(gr.kode)) {
                            GudangTembakau gBaru = new GudangTembakau(gr.kode, gr.nama, gr.kapasitasMaksKg);
                            daftarGudang.put(gr.kode, gBaru);
                            wilayahGudangMap.put(gr.kode, gr.wilayah);
                            gudangDaerahMap.put(gr.kode, dae);
                        } else {
                            GudangTembakau gAda = daftarGudang.get(gr.kode);
                            try {
                                java.lang.reflect.Field f = GudangTembakau.class.getDeclaredField("kapasitasMaksKg");
                                f.setAccessible(true);
                                f.setDouble(gAda, gr.kapasitasMaksKg);
                            } catch (Exception ignored) {}
                            wilayahGudangMap.put(gr.kode, gr.wilayah);
                            gudangDaerahMap.put(gr.kode, dae);
                        }
                    }
                    perbaruiPilihanGudangPerDaerah(daerahAktif);
                }

                if (gTarget instanceof GudangTembakau) {
                    terapkanGudangAktif((GudangTembakau) gTarget);
                }

                catatLogInformasi("[INFO] Database lokal berhasil dimuat dari: " + dbPath.toAbsolutePath());
                return;
            } catch (Exception ex) {
                catatLogInformasi("Peringatan muat database: " + ex.getMessage() + ". Memuat fixture default...");
            }
        }

        inisialisasiFixtureAwal();
        simpanDatabaseKeFile(false);
    }

    private void simpanDatabaseKeFile(boolean bersuara) {
        try {
            DataStorageService.simpanKeFile(null, daftarPetani, riwayatSemuaTransaksi, daftarGudang, wilayahGudangMap, gudangDaerahMap);
            if (bersuara) {
                JOptionPane.showMessageDialog(this,
                    "Seluruh data berhasil disimpan permanen ke:\n"
                    + Paths.get(DataStorageService.DEFAULT_FILE_PATH).toAbsolutePath(),
                    "Penyimpanan Sukses", JOptionPane.INFORMATION_MESSAGE);
                catatLogInformasi("[INFO] Database berhasil disimpan permanen.");
            }
        } catch (IOException ex) {
            if (bersuara) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan database: " + ex.getMessage(),
                    "Error Penyimpanan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void muatDatabaseDariFile() {
        try {
            PenerimaPasokan gTarget = (gudangAktif != null) ? gudangAktif : gudang;
            for (GudangTembakau g : daftarGudang.values()) {
                kosongkanGudangInternal(g);
            }
            DataStorageService.StorageData data = DataStorageService.muatDariFile(null, daftarGudang, gTarget);
            daftarPetani.clear();
            daftarPetani.addAll(data.daftarPetani);
            riwayatSemuaTransaksi.clear();
            riwayatSemuaTransaksi.addAll(data.riwayatSemuaTransaksi);

            if (!data.daftarGudang.isEmpty()) {
                for (DataStorageService.GudangRecord gr : data.daftarGudang) {
                    String dae = (gr.daerah != null && !gr.daerah.isBlank()) ? gr.daerah : "Sumenep";
                    if (!daftarDaerah.contains(dae)) {
                        daftarDaerah.add(dae);
                        cmbDaerah.addItem(dae);
                    }
                    if (!daftarGudang.containsKey(gr.kode)) {
                        GudangTembakau gBaru = new GudangTembakau(gr.kode, gr.nama, gr.kapasitasMaksKg);
                        daftarGudang.put(gr.kode, gBaru);
                        wilayahGudangMap.put(gr.kode, gr.wilayah);
                        gudangDaerahMap.put(gr.kode, dae);
                    } else {
                        GudangTembakau gAda = daftarGudang.get(gr.kode);
                        try {
                            java.lang.reflect.Field f = GudangTembakau.class.getDeclaredField("kapasitasMaksKg");
                            f.setAccessible(true);
                            f.setDouble(gAda, gr.kapasitasMaksKg);
                        } catch (Exception ignored) {}
                        wilayahGudangMap.put(gr.kode, gr.wilayah);
                        gudangDaerahMap.put(gr.kode, dae);
                    }
                }
                perbaruiPilihanGudangPerDaerah(daerahAktif);
            }

            petaniTableModel.setDaftarPetani(daftarPetani);
            panelOperasiPasokan.perbaruiDaftarPetani(daftarPetani);
            semuaTransaksiTableModel.setDaftarTransaksi(riwayatSemuaTransaksi);

            if (gTarget instanceof GudangTembakau) {
                terapkanGudangAktif((GudangTembakau) gTarget);
            }

            JOptionPane.showMessageDialog(this, "Database berhasil dimuat ulang.", "Muat Sukses", JOptionPane.INFORMATION_MESSAGE);
            catatLogInformasi("[INFO] Database berhasil dimuat ulang.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat database: " + ex.getMessage(),
                "Error Muat File", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eksporLaporanCSV() {
        Path path = Paths.get("data/laporan_pasokan_tembakau.csv");
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("ID Transaksi,Nama Petani,NIK Petani,Asal Desa,Kode Batch,Varietas,Berat (kg),Kadar Air (%),Grade Mutu,Harga Satuan (Rp),Total Pembayaran (Rp),Status Transaksi\n");

            for (TransaksiPasok t : riwayatSemuaTransaksi) {
                Petani p = t.getPetani();
                Tembakau tb = t.getTembakau();
                sb.append(String.format(Locale.ROOT, "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,\"%s\",%.2f,%.2f,\"%s\"\n",
                    t.getIdTransaksi(),
                    p != null ? p.getNama() : "-",
                    p != null ? p.getNik() : "-",
                    p != null ? p.getAsalDesa() : "-",
                    tb != null ? tb.getKodeBatch() : "-",
                    tb != null ? tb.getVarietas() : "-",
                    tb != null ? tb.getBeratKg() : 0.0,
                    tb != null ? tb.getKadarAirPersen() : 0.0,
                    tb != null && tb.getGrade() != null ? tb.getGrade().name() : "-",
                    t.getHargaPerKg(),
                    t.getTotalPembayaran(),
                    t.getStatus() != null ? t.getStatus().getLabel() : "-"
                ));
            }

            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this,
                "Laporan rekapitulasi transaksi berhasil diekspor ke format CSV:\n" + path.toAbsolutePath()
                + "\n\n(Dapat langsung dibuka di Microsoft Excel)",
                "Ekspor CSV Sukses", JOptionPane.INFORMATION_MESSAGE);
            catatLogInformasi("[INFO] Laporan CSV diekspor: " + path.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal ekspor CSV: " + ex.getMessage(), "Error Ekspor", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inisialisasiFixtureAwal() {
        kosongkanGudangInternal(gudang);
        daftarPetani.clear();
        riwayatSemuaTransaksi.clear();

        if (cmbDaerah != null && cmbDaerah.getItemCount() > 0) {
            cmbDaerah.setSelectedItem("Sumenep");
        }
        if (cmbGudangAktif != null && cmbGudangAktif.getItemCount() > 0) {
            cmbGudangAktif.setSelectedIndex(0);
        }
        this.gudangAktif = gudang;
        if (panelStatusGudang != null) {
            panelStatusGudang.setDaerah("Sumenep");
            panelStatusGudang.setWilayah(wilayahGudangMap.getOrDefault(((GudangTembakau) gudang).getKodeGudang(), "Wilayah Pusat"));
            panelStatusGudang.setGudang(gudang);
        }
        if (panelOperasiPasokan != null) panelOperasiPasokan.setPenerimaPasokan(gudang);
        if (panelDistribusiPabrik != null) panelDistribusiPabrik.setGudang(gudang);

        Petani p1 = new Petani("3529011111110001", "Pak Slamet", "Kec. Guluk-Guluk", 1.5);
        Petani p2 = new Petani("3529012222220002", "Pak Mat", "Kec. Pasongsongan", 2.0);
        daftarPetani.add(p1);
        daftarPetani.add(p2);

        try {
            Tembakau t1 = new Tembakau("BATCH-001", "Prancak-95", 250.0, 11.5);
            TransaksiPasok trx1 = new TransaksiPasok("T-001", p1, t1);
            trx1.verifikasiTransaksi();
            gudang.terimaPasokan(trx1);
            riwayatSemuaTransaksi.add(trx1);

            Tembakau t2 = new Tembakau("BATCH-002", "Prancak-95", 400.0, 13.5);
            TransaksiPasok trx2 = new TransaksiPasok("T-002", p2, t2);
            trx2.verifikasiTransaksi();
            gudang.terimaPasokan(trx2);
            riwayatSemuaTransaksi.add(trx2);
        } catch (Exception ex) {
            catatLogInformasi("Peringatan inisialisasi fixture: " + ex.getMessage());
        }

        petaniTableModel.setDaftarPetani(daftarPetani);
        petaniTableModel.setGudangAktif(gudang);
        panelOperasiPasokan.perbaruiDaftarPetani(daftarPetani);
        List<TransaksiPasok> trxGudangPst = (gudang instanceof GudangTembakau)
            ? ((GudangTembakau) gudang).getDaftarTransaksi()
            : riwayatSemuaTransaksi;
        if (gudang instanceof GudangTembakau) {
            transaksiGudangTableModel.setDaftarTransaksi(trxGudangPst);
        }
        semuaTransaksiTableModel.setDaftarTransaksi(riwayatSemuaTransaksi);
        panelStatusGudang.perbaruiStatus();
        panelDistribusiPabrik.setGudang(gudang);
        if (gudang instanceof GudangTembakau) {
            GudangTembakau gt = (GudangTembakau) gudang;
            String wil = wilayahGudangMap.getOrDefault(gt.getKodeGudang(), "Wilayah Pusat");
            panelKasirPembayaran.setIdentitasGudang(gt.getNamaGudang(), "Sumenep - " + wil);
            panelDashboardKPI.setIdentitasGudang(gt.getNamaGudang(), "Sumenep - " + wil);
        }
        panelKasirPembayaran.perbaruiDaftarPetani(daftarPetani, trxGudangPst);
        panelDashboardKPI.perbaruiData(daftarPetani, trxGudangPst);

        catatLogInformasi(
            "=== FIXTURE BAB 4 TELAH DIMUAT ===\n"
            + "• Gudang: " + ((GudangTembakau) gudang).getNamaGudang() + " (Kapasitas: 1.000 kg)\n"
            + "• Stok Awal: 650,00 kg (65,0%) | Sisa Kapasitas: 350,00 kg\n"
            + "• Saldo Awal P1 (Pak Slamet): 250,00 kg (dari T-001)\n"
            + "• Saldo Awal P2 (Pak Mat): 400,00 kg (dari T-002)\n"
            + "• Status Audit Invarian I2-I4: VALID"
        );
    }

    private void resetFixtureBab4() {
        inisialisasiFixtureAwal();
        simpanDatabaseKeFile(false);
        JOptionPane.showMessageDialog(
            this,
            "State Fixture Bab 4 berhasil dimuat ulang (Gudang 650/1000 kg).\n"
            + "Seluruh skenario S1 s.d. S5 siap didemonstrasikan.",
            "Fixture Dimuat", JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void presetSkenarioS1() {
        tabKiri.setSelectedIndex(0);
        Petani p1 = daftarPetani.get(0);
        panelOperasiPasokan.getCmbPetani().setSelectedItem(p1);
        panelOperasiPasokan.getTxtIdTransaksi().setText("T-003");
        panelOperasiPasokan.getTxtKodeBatch().setText("BATCH-003");
        panelOperasiPasokan.getTxtVarietas().setText("Prancak-95");
        panelOperasiPasokan.getTxtBeratKg().setText("200.0");
        panelOperasiPasokan.getTxtKadarAir().setText("14.0");

        catatLogInformasi(
            "[PRESET S1 DIMUAT: PENERIMAAN NORMAL]\n"
            + "Parameter: T-003 (200 kg, 14,0% -> Grade B) oleh " + p1.getNama() + "\n"
            + "Petunjuk Dosen: Tekan 'Tahap 1' (Verifikasi Mutu), lalu tekan 'Tahap 2' (Simpan ke Gudang).\n"
            + "Ekspektasi: Stok gudang bertambah dari 650 kg menjadi 850 kg, saldo P1 naik menjadi 450 kg."
        );
    }

    private void presetSkenarioS2() {
        tabKiri.setSelectedIndex(0);
        Petani p1 = daftarPetani.get(0);
        panelOperasiPasokan.getCmbPetani().setSelectedItem(p1);
        panelOperasiPasokan.getTxtIdTransaksi().setText("T-003");
        panelOperasiPasokan.getTxtKodeBatch().setText("BATCH-003");
        panelOperasiPasokan.getTxtVarietas().setText("Prancak-95");
        panelOperasiPasokan.getTxtBeratKg().setText("500.0");
        panelOperasiPasokan.getTxtKadarAir().setText("21.5");

        catatLogInformasi(
            "[PRESET S2 DIMUAT: MUTU DITOLAK]\n"
            + "Parameter: T-003 (500 kg, kadar air 21,5% > 18,0%)\n"
            + "Petunjuk Dosen: Tekan tombol 'Tahap 1: Buat & Verifikasi Mutu'.\n"
            + "Ekspektasi: MutuTembakauDitolakException (MUTU-001) tertangkap. Status tetap DIBUAT dan saldo P1 TIDAK bertambah (tetap 250 kg)."
        );
    }

    private void presetSkenarioS3() {
        tabKiri.setSelectedIndex(0);
        Petani p1 = daftarPetani.get(0);
        panelOperasiPasokan.getCmbPetani().setSelectedItem(p1);
        panelOperasiPasokan.getTxtIdTransaksi().setText("T-003");
        panelOperasiPasokan.getTxtKodeBatch().setText("BATCH-003");
        panelOperasiPasokan.getTxtVarietas().setText("Prancak-95");
        panelOperasiPasokan.getTxtBeratKg().setText("500.0");
        panelOperasiPasokan.getTxtKadarAir().setText("14.0");

        catatLogInformasi(
            "[PRESET S3 DIMUAT: MELEBIHI KAPASITAS FISIK GUDANG]\n"
            + "Parameter: T-003 (500 kg, 14,0%) | Sisa gudang hanya 350 kg (lebih 150 kg).\n"
            + "Petunjuk Dosen: Tekan 'Tahap 1' (sukses lolos verifikasi mutu), lalu tekan 'Tahap 2: Simpan ke Gudang'.\n"
            + "Ekspektasi: KapasitasGudangPenuhException (KAPASITAS-001) tertangkap. Failure atomicity terbukti: stok gudang tetap 650 kg dan saldo P1 tetap 250 kg (tidak ada penambahan parsial)."
        );
    }

    private void presetSkenarioS4() {
        tabKiri.setSelectedIndex(0);
        Petani p1 = daftarPetani.get(0);
        panelOperasiPasokan.getCmbPetani().setSelectedItem(p1);
        panelOperasiPasokan.getTxtIdTransaksi().setText("T-003");
        panelOperasiPasokan.getTxtKodeBatch().setText("BATCH-003");
        panelOperasiPasokan.getTxtVarietas().setText("Prancak-95");
        panelOperasiPasokan.getTxtBeratKg().setText("200.0");
        panelOperasiPasokan.getTxtKadarAir().setText("14.0");

        Tembakau tembakau = new Tembakau("BATCH-003", "Prancak-95", 200.0, 14.0);
        TransaksiPasok trxBelumVerif = new TransaksiPasok("T-003", p1, tembakau);
        panelOperasiPasokan.setTransaksiAktif(trxBelumVerif);

        catatLogInformasi(
            "[PRESET S4 DIMUAT: TRANSAKSI BELUM TERVERIFIKASI]\n"
            + "Parameter: T-003 aktif dengan status DIBUAT (belum melalui Tahap 1).\n"
            + "Petunjuk Dosen: Langsung tekan tombol 'Tahap 2: Simpan ke Gudang' tanpa Tahap 1.\n"
            + "Ekspektasi: TransaksiBelumTerverifikasiException (STATUS-001) tertangkap. Transaksi dilarang masuk gudang."
        );
    }

    private void presetSkenarioS5() {
        tabKiri.setSelectedIndex(0);
        if (gudang instanceof GudangTembakau) {
            List<TransaksiPasok> trxGudang = ((GudangTembakau) gudang).getDaftarTransaksi();
            if (!trxGudang.isEmpty()) {
                TransaksiPasok trxTersimpan = trxGudang.get(0);
                panelOperasiPasokan.setTransaksiAktif(trxTersimpan);

                catatLogInformasi(
                    "[PRESET S5 DIMUAT: PENYIMPANAN GANDA]\n"
                    + "Parameter: Transaksi aktif disetel ke " + trxTersimpan.getIdTransaksi()
                    + " yang sudah berstatus '" + trxTersimpan.getStatus().getLabel() + "'.\n"
                    + "Petunjuk Dosen: Tekan tombol 'Tahap 2: Simpan ke Gudang'.\n"
                    + "Ekspektasi: IllegalStateException tertangkap. Gudang menolak penyimpanan ulang (Invarian I4 terjaga)."
                );
            }
        }
    }

    private void catatLogAwal(String namaAksi, StateSnapshot s) {
        String waktu = LocalTime.now().format(formatWaktu);
        StringBuilder sb = new StringBuilder();
        sb.append("\n====================================================================================================\n");
        sb.append(String.format("[%s] AUDIT SEBELUM AKSI: %s\n", waktu, namaAksi));
        sb.append(String.format("• Stok Gudang     : %s kg / %s kg | Trx di Gudang: %d | Audit Invarian (I2-I4): %s\n",
            formatAngka.format(s.stokGudang), formatAngka.format(1000.0), s.jumlahTrxGudang,
            s.invarianValid ? "VALID" : "RUSAK"));
        sb.append("• Saldo Petani    : ");
        for (Map.Entry<String, Double> e : s.saldoPetani.entrySet()) {
            sb.append(String.format("[%s: %s kg] ", e.getKey(), formatAngka.format(e.getValue())));
        }
        sb.append(String.format("\n• Transaksi Aktif : ID=%s | Status=%s\n", s.idTrx, s.statusTrx));
        sb.append("----------------------------------------------------------------------------------------------------\n");
        logArea.append(sb.toString());
    }

    private void catatLogAkhir(String namaAksi, StateSnapshot awal, StateSnapshot akhir) {
        String waktu = LocalTime.now().format(formatWaktu);
        StringBuilder sb = new StringBuilder();
        double selisihStok = akhir.stokGudang - awal.stokGudang;
        String ketStok = (Math.abs(selisihStok) < 1e-9) ? "TETAP" : String.format("+%s kg", formatAngka.format(selisihStok));

        sb.append(String.format("[%s] AUDIT SESUDAH AKSI: %s\n", waktu, namaAksi));
        sb.append(String.format("• Stok Gudang     : %s kg (%s) | Trx di Gudang: %d\n",
            formatAngka.format(akhir.stokGudang), ketStok, akhir.jumlahTrxGudang));
        sb.append("• Saldo Petani    : ");
        for (Map.Entry<String, Double> e : akhir.saldoPetani.entrySet()) {
            double saldoAwal = awal.saldoPetani.getOrDefault(e.getKey(), 0.0);
            double selisih = e.getValue() - saldoAwal;
            String ketSaldo = (Math.abs(selisih) < 1e-9) ? "TETAP" : String.format("+%s kg", formatAngka.format(selisih));
            sb.append(String.format("[%s: %s kg (%s)] ", e.getKey(), formatAngka.format(e.getValue()), ketSaldo));
        }
        sb.append(String.format("\n• Transaksi Aktif : ID=%s | Status=%s\n", akhir.idTrx, akhir.statusTrx));
        sb.append(String.format("• Audit Invarian  : %s (I2-I4 Terjaga)\n", akhir.invarianValid ? "VALID" : "RUSAK"));

        boolean stateTetap = Math.abs(selisihStok) < 1e-9;
        if (stateTetap) {
            sb.append("• Vonis Audit     : [FAILURE ATOMICITY TERJAGA - TIDAK ADA MUTASI PARSIAL]\n");
        } else {
            sb.append("• Vonis Audit     : [MUTASI STATE BERHASIL DAN KONSISTEN]\n");
        }
        sb.append("====================================================================================================\n");
        logArea.append(sb.toString());
    }

    private void catatLogInformasi(String pesan) {
        String waktu = LocalTime.now().format(formatWaktu);
        logArea.append(String.format("[%s] %s\n", waktu, pesan));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void aturUkuranDanPosisi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 880);
        setMinimumSize(new Dimension(1024, 720));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                simpanDatabaseKeFile(false);
            }
        });
    }

    private static class StateSnapshot {
        final double stokGudang;
        final int jumlahTrxGudang;
        final boolean invarianValid;
        final Map<String, Double> saldoPetani = new LinkedHashMap<>();
        final String statusTrx;
        final String idTrx;

        StateSnapshot(PenerimaPasokan gudang, List<Petani> daftarPetani, TransaksiPasok trxAktif) {
            if (gudang instanceof GudangTembakau) {
                GudangTembakau gt = (GudangTembakau) gudang;
                this.stokGudang = gt.getStokTersimpanKg();
                this.jumlahTrxGudang = gt.getDaftarTransaksi().size();
                this.invarianValid = gt.periksaInvarian();
            } else {
                this.stokGudang = 0.0;
                this.jumlahTrxGudang = 0;
                this.invarianValid = false;
            }

            for (Petani p : daftarPetani) {
                saldoPetani.put(p.getNama(), p.getTotalSetoranKg());
            }

            if (trxAktif != null) {
                this.idTrx = trxAktif.getIdTransaksi();
                this.statusTrx = trxAktif.getStatus().getLabel();
            } else {
                this.idTrx = "-";
                this.statusTrx = "Belum Ada";
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainDesktopApp app = new MainDesktopApp();
            app.setVisible(true);
        });
    }
}
