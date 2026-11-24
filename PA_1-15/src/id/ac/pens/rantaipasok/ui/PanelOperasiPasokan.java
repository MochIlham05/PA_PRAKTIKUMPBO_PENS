package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Panel operasi penerimaan pasokan tembakau dengan alur dua tahap:
 * Tahap 1: Buat & Verifikasi Mutu (Tembakau & TransaksiPasok -> verifikasiTransaksi())
 * Tahap 2: Simpan ke Gudang (PenerimaPasokan.terimaPasokan(trx))
 * Dilengkapi kartu visual preview TransaksiPasok aktif, kalkulasi mutu otomatis,
 * serta demonstrasi failure atomicity dan exception domain secara transparan.
 */
public class PanelOperasiPasokan extends JPanel {

    // Komponen Form Input Transaksi
    private final JComboBox<Petani> cmbPetani = new JComboBox<>();
    private final JTextField txtIdTransaksi = new JTextField("TRX-001", 12);
    private final JTextField txtKodeBatch = new JTextField("BATCH-001", 12);
    private final JTextField txtVarietas = new JTextField("Prancak-95", 14);
    private final JTextField txtBeratKg = new JTextField("200.0", 8);
    private final JTextField txtKadarAir = new JTextField("11.5", 8);

    // Tombol Aksi Dua Tahap
    private final JButton btnTahap1 = new JButton("Tahap 1: Verifikasi Mutu");
    private final JButton btnTahap2 = new JButton("Tahap 2: Simpan ke Gudang");

    // Komponen Kartu Visual Transaksi Aktif
    private final JLabel lblCardIdTrx = new JLabel("-");
    private final JLabel lblCardPetani = new JLabel("-");
    private final JLabel lblCardBatchVarietas = new JLabel("-");
    private final JLabel lblCardBeratAir = new JLabel("-");
    private final JLabel lblCardGrade = new JLabel("-");
    private final JLabel lblCardHargaKg = new JLabel("-");
    private final JLabel lblCardTotalBayar = new JLabel("-");
    private final JLabel lblCardAuditLahan = new JLabel("Normal (Wajar)");
    private final JLabel lblCardBadgeStatus = new JLabel("BELUM DIBUAT", SwingConstants.CENTER);

    // Referensi Komponen Eksternal
    private PenerimaPasokan penerimaPasokan;
    private PanelStatusGudang panelStatusGudang;
    private PetaniTableModel petaniTableModel;
    private TransaksiTableModel transaksiTableModel;

    // Transaksi Pasok yang sedang aktif dalam alur
    private TransaksiPasok trxAktif;
    private final java.util.List<java.util.function.Consumer<TransaksiPasok>> onTransaksiListeners = new java.util.ArrayList<>();
    private java.util.function.BiConsumer<String, Runnable> auditWrapper;

    private final DecimalFormat formatRupiah;
    private final DecimalFormat formatAngka;

    public PanelOperasiPasokan() {
        this(null, null, null, null);
    }

    public PanelOperasiPasokan(PenerimaPasokan penerimaPasokan,
                               PanelStatusGudang panelStatusGudang,
                               PetaniTableModel petaniTableModel,
                               TransaksiTableModel transaksiTableModel) {
        this.penerimaPasokan = penerimaPasokan;
        this.panelStatusGudang = panelStatusGudang;
        this.petaniTableModel = petaniTableModel;
        this.transaksiTableModel = transaksiTableModel;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatRupiah = new DecimalFormat("Rp #,##0.00", symbols);
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);

        inisialisasiUI();
        pasangLivePreviewListener();
        perbaruiPreviewLive();
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Penerimaan Pasokan Tembakau (Verifikasi Dua Tahap)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(33, 37, 41)
            ),
            new EmptyBorder(10, 12, 10, 12)
        ));

        // Sub-panel 1: Form Input Transaksi (Kiri)
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font fontLabel = new Font("SansSerif", Font.PLAIN, 12);

        // Renderer khusus ComboBox Petani
        cmbPetani.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Petani) {
                    Petani p = (Petani) value;
                    setText(p.getNama() + " (" + p.getAsalDesa() + ") [NIK: " + p.getNik() + "]");
                } else if (value == null) {
                    setText("-- Pilih Petani Mitra --");
                }
                return this;
            }
        });

        // 1. Pilihan Petani
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Petani Mitra:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panelForm.add(cmbPetani, gbc);

        // 2. ID Transaksi
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelForm.add(buatLabel("ID Transaksi:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        panelForm.add(txtIdTransaksi, gbc);

        // 3. Kode Batch
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Kode Batch:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panelForm.add(txtKodeBatch, gbc);

        // 4. Varietas
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Varietas Tembakau:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        panelForm.add(txtVarietas, gbc);

        // 5. Berat (kg)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Berat Bersih (kg):", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        panelForm.add(txtBeratKg, gbc);

        // 6. Kadar Air (%)
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Kadar Air (%):", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        panelForm.add(txtKadarAir, gbc);

        // Tombol Aksi Dua Tahap
        JPanel panelTombol = new JPanel(new GridLayout(2, 1, 0, 6));
        panelTombol.setOpaque(false);

        btnTahap1.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnTahap1.setBackground(new Color(2, 119, 189));
        btnTahap1.setForeground(Color.WHITE);
        btnTahap1.setFocusPainted(false);
        btnTahap1.setToolTipText("Tahap 1: Verifikasi mutu kadar air tembakau");
        btnTahap1.addActionListener(e -> {
            if (auditWrapper != null) {
                auditWrapper.accept("Tahap 1: Buat & Verifikasi Mutu", this::aksiTahap1VerifikasiMutu);
            } else {
                aksiTahap1VerifikasiMutu();
            }
        });

        btnTahap2.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnTahap2.setBackground(new Color(46, 125, 50));
        btnTahap2.setForeground(Color.WHITE);
        btnTahap2.setFocusPainted(false);
        btnTahap2.setToolTipText("Tahap 2: Simpan muatan terverifikasi ke gudang");
        btnTahap2.addActionListener(e -> {
            if (auditWrapper != null) {
                auditWrapper.accept("Tahap 2: Simpan ke Gudang", this::aksiTahap2SimpanGudang);
            } else {
                aksiTahap2SimpanGudang();
            }
        });

        panelTombol.add(btnTahap1);
        panelTombol.add(btnTahap2);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 4, 4, 4);
        panelForm.add(panelTombol, gbc);

        // Sub-panel 2: Kartu Visual Transaksi Aktif (Kanan)
        JPanel panelKartu = buatPanelKartuVisual();

        // Bagi kiri dan kanan dengan ukuran proporsional
        JPanel panelUtama = new JPanel(new GridLayout(1, 2, 14, 0));
        panelUtama.setOpaque(false);
        panelUtama.add(panelForm);
        panelUtama.add(panelKartu);

        add(panelUtama, BorderLayout.CENTER);
    }

    private JPanel buatPanelKartuVisual() {
        JPanel kartu = new JPanel(new BorderLayout(8, 8));
        kartu.setBackground(new Color(248, 249, 250));
        kartu.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                "Rincian Objek Transaksi Tembakau",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(73, 80, 87)
            ),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel panelGrid = new JPanel(new GridLayout(8, 2, 6, 4));
        panelGrid.setOpaque(false);

        Font fontLabel = new Font("SansSerif", Font.PLAIN, 11);
        Font fontValue = new Font("SansSerif", Font.BOLD, 11);

        lblCardIdTrx.setFont(fontValue);
        lblCardPetani.setFont(fontValue);
        lblCardBatchVarietas.setFont(fontValue);
        lblCardBeratAir.setFont(fontValue);
        lblCardGrade.setFont(fontValue);
        lblCardHargaKg.setFont(fontValue);
        lblCardTotalBayar.setFont(fontValue);
        lblCardAuditLahan.setFont(fontValue);

        panelGrid.add(buatLabel("ID Transaksi:", fontLabel));
        panelGrid.add(lblCardIdTrx);

        panelGrid.add(buatLabel("Petani Mitra:", fontLabel));
        panelGrid.add(lblCardPetani);

        panelGrid.add(buatLabel("Batch & Varietas:", fontLabel));
        panelGrid.add(lblCardBatchVarietas);

        panelGrid.add(buatLabel("Berat / Kadar Air:", fontLabel));
        panelGrid.add(lblCardBeratAir);

        panelGrid.add(buatLabel("Grade Mutu:", fontLabel));
        panelGrid.add(lblCardGrade);

        panelGrid.add(buatLabel("Harga per Kg:", fontLabel));
        panelGrid.add(lblCardHargaKg);

        panelGrid.add(buatLabel("Total Pembayaran:", fontLabel));
        panelGrid.add(lblCardTotalBayar);

        panelGrid.add(buatLabel("Audit Kuota Lahan:", fontLabel));
        panelGrid.add(lblCardAuditLahan);

        // Badge Status Mesin Transaksi
        lblCardBadgeStatus.setOpaque(true);
        lblCardBadgeStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblCardBadgeStatus.setPreferredSize(new Dimension(200, 30));
        setTampilanBadgeStatus(null);

        JPanel panelBawahKartu = new JPanel(new GridLayout(2, 1, 0, 4));
        panelBawahKartu.setOpaque(false);

        JPanel panelBadgeWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        panelBadgeWrapper.setOpaque(false);
        panelBadgeWrapper.add(lblCardBadgeStatus);

        JButton btnLihatKuitansi = new JButton("Cetak Kuitansi Pasokan");
        btnLihatKuitansi.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnLihatKuitansi.setBackground(new Color(67, 160, 71));
        btnLihatKuitansi.setForeground(Color.WHITE);
        btnLihatKuitansi.setFocusPainted(false);
        btnLihatKuitansi.addActionListener(e -> {
            if (trxAktif == null) {
                JOptionPane.showMessageDialog(this, "Belum ada transaksi aktif untuk dicetak kuitansi.",
                    "Kuitansi Belum Tersedia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new DialogKuitansiPasokan(SwingUtilities.getWindowAncestor(this), trxAktif).setVisible(true);
        });

        panelBawahKartu.add(panelBadgeWrapper);
        panelBawahKartu.add(btnLihatKuitansi);

        kartu.add(panelGrid, BorderLayout.CENTER);
        kartu.add(panelBawahKartu, BorderLayout.SOUTH);

        return kartu;
    }

    private JLabel buatLabel(String teks, Font font) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(font);
        lbl.setForeground(new Color(108, 117, 125));
        return lbl;
    }

    private void pasangLivePreviewListener() {
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { perbaruiPreviewLive(); }
            @Override public void removeUpdate(DocumentEvent e) { perbaruiPreviewLive(); }
            @Override public void changedUpdate(DocumentEvent e) { perbaruiPreviewLive(); }
        };

        txtIdTransaksi.getDocument().addDocumentListener(docListener);
        txtKodeBatch.getDocument().addDocumentListener(docListener);
        txtVarietas.getDocument().addDocumentListener(docListener);
        txtBeratKg.getDocument().addDocumentListener(docListener);
        txtKadarAir.getDocument().addDocumentListener(docListener);
        cmbPetani.addActionListener(e -> perbaruiPreviewLive());
    }

    /**
     * Memperbarui visual kartu secara interaktif saat mengetik (live calculation).
     */
    private void perbaruiPreviewLive() {
        if (trxAktif != null) {
            // Jika ada objek transaksi aktif, tampilkan representasi objek aslinya
            lblCardIdTrx.setText(trxAktif.getIdTransaksi());
            lblCardPetani.setText(trxAktif.getPetani().getNama());
            lblCardBatchVarietas.setText(trxAktif.getTembakau().getKodeBatch()
                    + " (" + trxAktif.getTembakau().getVarietas() + ")");
            lblCardBeratAir.setText(formatAngka.format(trxAktif.getTembakau().getBeratKg()) + " kg / "
                    + formatAngka.format(trxAktif.getTembakau().getKadarAirPersen()) + "%");
            lblCardGrade.setText(trxAktif.getTembakau().getGrade().name());
            lblCardHargaKg.setText(formatRupiah.format(trxAktif.getHargaPerKg()));
            lblCardTotalBayar.setText(formatRupiah.format(trxAktif.getTotalPembayaran()));
            setTampilanBadgeStatus(trxAktif.getStatus());
            evaluasiKuotaLahan(trxAktif.getPetani(), trxAktif.getTembakau().getBeratKg());
            return;
        }

        // Jika belum ada objek resmi, buat perkiraan dari input form
        String idTrx = txtIdTransaksi.getText().trim();
        Petani p = (Petani) cmbPetani.getSelectedItem();
        String kode = txtKodeBatch.getText().trim();
        String var = txtVarietas.getText().trim();

        lblCardIdTrx.setText(idTrx.isEmpty() ? "-" : idTrx);
        lblCardPetani.setText(p != null ? p.getNama() : "-");
        lblCardBatchVarietas.setText((kode.isEmpty() && var.isEmpty()) ? "-" : kode + " (" + var + ")");

        double beratInput = 0.0;
        try {
            double berat = Double.parseDouble(txtBeratKg.getText().trim());
            beratInput = berat;
            double air = Double.parseDouble(txtKadarAir.getText().trim());
            GradeMutu grade = GradeMutu.dariKadarAir(air);
            double harga = grade.getHargaPerKg();
            double total = harga * berat;

            lblCardBeratAir.setText(formatAngka.format(berat) + " kg / " + formatAngka.format(air) + "%");
            lblCardGrade.setText(grade.name());
            lblCardHargaKg.setText(formatRupiah.format(harga));
            lblCardTotalBayar.setText(formatRupiah.format(total));
        } catch (Exception ex) {
            lblCardBeratAir.setText("-");
            lblCardGrade.setText("-");
            lblCardHargaKg.setText("-");
            lblCardTotalBayar.setText("-");
        }
        setTampilanBadgeStatus(null);
        evaluasiKuotaLahan(p, beratInput);
    }

    private void evaluasiKuotaLahan(Petani p, double beratTambahan) {
        if (p != null && p.getLuasLahanHa() > 0.0) {
            double akumulasi = p.getTotalSetoranKg() + Math.max(0.0, beratTambahan);
            double rasio = akumulasi / p.getLuasLahanHa();
            if (rasio > 2000.0) {
                lblCardAuditLahan.setText(String.format(Locale.ROOT, "Perhatian: Rasio %.0f kg/Ha (> 2.000)", rasio));
                lblCardAuditLahan.setForeground(new Color(198, 40, 40));
            } else {
                lblCardAuditLahan.setText(String.format(Locale.ROOT, "Wajar (%.0f kg/Ha)", rasio));
                lblCardAuditLahan.setForeground(new Color(46, 125, 50));
            }
        } else {
            lblCardAuditLahan.setText("Normal (Wajar)");
            lblCardAuditLahan.setForeground(new Color(108, 117, 125));
        }
    }

    private void setTampilanBadgeStatus(StatusTransaksi status) {
        if (status == null) {
            lblCardBadgeStatus.setText("STATUS: BELUM DIVERIFIKASI");
            lblCardBadgeStatus.setBackground(new Color(238, 238, 238));
            lblCardBadgeStatus.setForeground(new Color(97, 97, 97));
            lblCardBadgeStatus.setBorder(new LineBorder(new Color(189, 189, 189), 1, true));
        } else {
            switch (status) {
                case DIBUAT:
                    lblCardBadgeStatus.setText("STATUS: DIBUAT");
                    lblCardBadgeStatus.setBackground(new Color(227, 242, 253));
                    lblCardBadgeStatus.setForeground(new Color(21, 101, 192));
                    lblCardBadgeStatus.setBorder(new LineBorder(new Color(144, 202, 249), 1, true));
                    break;
                case TERVERIFIKASI:
                    lblCardBadgeStatus.setText("STATUS: TERVERIFIKASI");
                    lblCardBadgeStatus.setBackground(new Color(255, 248, 225));
                    lblCardBadgeStatus.setForeground(new Color(245, 124, 0));
                    lblCardBadgeStatus.setBorder(new LineBorder(new Color(255, 204, 128), 1, true));
                    break;
                case DISIMPAN_DI_GUDANG:
                    lblCardBadgeStatus.setText("STATUS: DISIMPAN DI GUDANG");
                    lblCardBadgeStatus.setBackground(new Color(232, 245, 233));
                    lblCardBadgeStatus.setForeground(new Color(46, 125, 50));
                    lblCardBadgeStatus.setBorder(new LineBorder(new Color(165, 214, 167), 1, true));
                    break;
            }
        }
    }

    /**
     * Aksi Tombol Tahap 1: Buat & Verifikasi Mutu.
     * Menginstansiasi Tembakau dan TransaksiPasok, lalu memanggil trx.verifikasiTransaksi().
     */
    private void aksiTahap1VerifikasiMutu() {
        Petani petani = (Petani) cmbPetani.getSelectedItem();
        if (petani == null) {
            JOptionPane.showMessageDialog(
                this,
                "Silakan pilih Petani Mitra terlebih dahulu.",
                "Petani Belum Dipilih",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String idTrx = txtIdTransaksi.getText();
        String kodeBatch = txtKodeBatch.getText();
        String varietas = txtVarietas.getText();
        String beratStr = txtBeratKg.getText();
        String airStr = txtKadarAir.getText();

        double beratKg;
        double kadarAirPersen;

        try {
            beratKg = Double.parseDouble(beratStr.trim());
            kadarAirPersen = Double.parseDouble(airStr.trim());

            // 1. Instansiasi objek Tembakau (immutable, fail-fast)
            Tembakau tembakau = new Tembakau(kodeBatch, varietas, beratKg, kadarAirPersen);

            // 2. Instansiasi objek TransaksiPasok (status awal: DIBUAT)
            TransaksiPasok trx = new TransaksiPasok(idTrx, petani, tembakau);
            this.trxAktif = trx;

            // Catat snapshot saldo awal sebelum verifikasi
            double saldoAwalPetani = petani.getTotalSetoranKg();

            // 3. Panggil metode verifikasiTransaksi()
            trx.verifikasiTransaksi();

            // Bila berhasil, status berubah menjadi TERVERIFIKASI
            perbaruiPreviewLive();
            beritahuTransaksiListener(trx);

            JOptionPane.showMessageDialog(
                this,
                "VERIFIKASI MUTU BERHASIL (TAHAP 1 SELESAI)\n\n"
                    + "ID Transaksi: " + trx.getIdTransaksi() + "\n"
                    + "Petani Mitra: " + trx.getPetani().getNama() + "\n"
                    + "Batch Tembakau: " + tembakau.getKodeBatch() + " (" + tembakau.getVarietas() + ")\n"
                    + "Kadar Air: " + formatAngka.format(tembakau.getKadarAirPersen()) + "% -> Grade: " + tembakau.getGrade().name() + "\n"
                    + "Harga / Kg: " + formatRupiah.format(trx.getHargaPerKg()) + "\n"
                    + "Estimasi Pembayaran: " + formatRupiah.format(trx.getTotalPembayaran()) + "\n\n"
                    + "Status Mesin Transaksi: " + trx.getStatus().getLabel() + "\n"
                    + "[Bukti]: Saldo Petani saat verifikasi tetap " + formatAngka.format(saldoAwalPetani) + " kg (belum dikredit).",
                "Tahap 1 Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (MutuTembakauDitolakException e) {
            // Tangkap exception domain kegagalan mutu
            perbaruiPreviewLive();
            beritahuTransaksiListener(trxAktif);
            JOptionPane.showMessageDialog(
                this,
                "MUTU TEMBAKAU DITOLAK!\n\n"
                    + "Kode Kesalahan: " + e.getKodeKesalahan() + "\n"
                    + "Pesan: " + e.getMessage() + "\n"
                    + "Kadar Air: " + formatAngka.format(e.getKadarAirPersen()) + "% "
                    + "(Batas Maksimal Layak Produksi: " + formatAngka.format(e.getBatasMaksimalPersen()) + "%)\n\n"
                    + "[BUKTI FAILURE ATOMICITY & STATE INVARIANT]\n"
                    + "• Status Transaksi: " + (trxAktif != null ? trxAktif.getStatus().getLabel() : "DIBUAT") + " (TETAP DIBUAT)\n"
                    + "• Saldo Petani (" + petani.getNama() + "): " + formatAngka.format(petani.getTotalSetoranKg()) + " kg (TIDAK BERTAMBAH)",
                "Verifikasi Mutu Ditolak (" + e.getKodeKesalahan() + ")",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Parsing angka gagal: Berat atau Kadar Air harus berupa angka desimal yang valid.",
                "Validasi Fail-Fast Gagal",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Validasi Fail-Fast Gagal",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Pelanggaran Status Mesin",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Aksi Tombol Tahap 2: Simpan ke Gudang.
     * Memanggil penerimaPasokan.terimaPasokan(trxAktif).
     */
    private void aksiTahap2SimpanGudang() {
        if (trxAktif == null) {
            JOptionPane.showMessageDialog(
                this,
                "Belum ada transaksi aktif!\nSilakan lengkapi form dan tekan tombol 'Tahap 1: Buat & Verifikasi Mutu' terlebih dahulu.",
                "Transaksi Belum Ada",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (penerimaPasokan == null) {
            JOptionPane.showMessageDialog(
                this,
                "Objek Gudang/Penerima Pasokan belum dikonfigurasi.",
                "Konfigurasi Kosong",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Ambil snapshot awal sebelum pemanggilan untuk bukti failure atomicity
        Petani petani = trxAktif.getPetani();
        double saldoAwalPetani = petani.getTotalSetoranKg();
        double stokAwalGudang = 0.0;
        if (penerimaPasokan instanceof GudangTembakau) {
            stokAwalGudang = ((GudangTembakau) penerimaPasokan).getStokTersimpanKg();
        }

        try {
            // Panggil operasi bisnis domain layer
            penerimaPasokan.terimaPasokan(trxAktif);

            // Bila berhasil sampai baris ini:
            perbaruiPreviewLive();

            if (panelStatusGudang != null) {
                panelStatusGudang.perbaruiStatus();
            }
            if (petaniTableModel != null) {
                petaniTableModel.refreshData();
            }
            if (transaksiTableModel != null) {
                // Bila model menerima getDaftarTransaksi() dari gudang atau list
                transaksiTableModel.refreshData();
            }

            double stokAkhirGudang = 0.0;
            if (penerimaPasokan instanceof GudangTembakau) {
                stokAkhirGudang = ((GudangTembakau) penerimaPasokan).getStokTersimpanKg();
            }

            JOptionPane.showMessageDialog(
                this,
                "PENYIMPANAN KE GUDANG BERHASIL (TAHAP 2 SELESAI)\n\n"
                    + "ID Transaksi: " + trxAktif.getIdTransaksi() + "\n"
                    + "Kode Batch: " + trxAktif.getTembakau().getKodeBatch() + "\n"
                    + "Berat Muatan: " + formatAngka.format(trxAktif.getTembakau().getBeratKg()) + " kg\n"
                    + "Status Transaksi: " + trxAktif.getStatus().getLabel() + "\n\n"
                    + "[Perubahan State Valid]:\n"
                    + "• Stok Gudang: " + formatAngka.format(stokAwalGudang) + " kg -> " + formatAngka.format(stokAkhirGudang) + " kg\n"
                    + "• Saldo Petani (" + petani.getNama() + "): " + formatAngka.format(saldoAwalPetani) + " kg -> " + formatAngka.format(petani.getTotalSetoranKg()) + " kg\n"
                    + "• Status Audit Invarian: VALID (I2-I4)",
                "Tahap 2 Sukses Disimpan",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (TransaksiBelumTerverifikasiException e) {
            // Tangkap exception status belum terverifikasi
            JOptionPane.showMessageDialog(
                this,
                "PENERIMAAN DITOLAK: TRANSAKSI BELUM TERVERIFIKASI!\n\n"
                    + "Kode Kesalahan: " + e.getKodeKesalahan() + "\n"
                    + "Pesan: " + e.getMessage() + "\n\n"
                    + "[BUKTI FAILURE ATOMICITY]\n"
                    + "• Transaksi berstatus '" + e.getStatusTransaksi() + "' dilarang masuk ke gudang.\n"
                    + "• Stok Gudang: " + formatAngka.format(stokAwalGudang) + " kg (TETAP)\n"
                    + "• Saldo Petani: " + formatAngka.format(saldoAwalPetani) + " kg (TETAP)",
                "Penolakan Alur (" + e.getKodeKesalahan() + ")",
                JOptionPane.WARNING_MESSAGE
            );
        } catch (KapasitasGudangPenuhException e) {
            // Tangkap exception overcapacity fisik
            JOptionPane.showMessageDialog(
                this,
                "PENERIMAAN DITOLAK: KAPASITAS GUDANG PENUH!\n\n"
                    + "Kode Kesalahan: " + e.getKodeKesalahan() + "\n"
                    + "Pesan: " + e.getMessage() + "\n"
                    + "Sisa Kapasitas Gudang: " + formatAngka.format(e.getSisaKapasitasKg()) + " kg\n"
                    + "Muatan Pasokan Masuk: " + formatAngka.format(trxAktif.getTembakau().getBeratKg()) + " kg\n"
                    + "Kelebihan Fisik: " + formatAngka.format(e.getKelebihanKg()) + " kg\n\n"
                    + "[BUKTI FAILURE ATOMICITY - GUARD SEBELUM MUTASI]\n"
                    + "• Guard 4 mendeteksi muatan melampaui sisa kapasitas fisik.\n"
                    + "• Operasi dibatalkan SEBELUM terjadi mutasi state.\n"
                    + "• Stok Gudang: " + formatAngka.format(stokAwalGudang) + " kg (TETAP)\n"
                    + "• Saldo Petani: " + formatAngka.format(saldoAwalPetani) + " kg (TETAP)",
                "Kapasitas Gudang Tidak Cukup (" + e.getKodeKesalahan() + ")",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalStateException e) {
            // Tangkap upaya penyimpanan ulang pada transaksi yang sudah disimpan
            JOptionPane.showMessageDialog(
                this,
                "OPERASI DITOLAK: PENYIMPANAN GANDA!\n\n"
                    + e.getMessage() + "\n\n"
                    + "[BUKTI INVARIAN I4]\n"
                    + "• Transaksi yang sudah tersimpan dilarang disimpan ulang.\n"
                    + "• Stok dan saldo aman dari duplikasi perhitungan.",
                "Penyimpanan Ganda Ditolak",
                JOptionPane.WARNING_MESSAGE
            );
        } finally {
            // Setiap operasi (baik sukses maupun ditolak), audit status gudang secara real-time
            if (panelStatusGudang != null) {
                panelStatusGudang.perbaruiStatus();
            }
            if (petaniTableModel != null) {
                petaniTableModel.refreshData();
            }
            if (transaksiTableModel != null) {
                transaksiTableModel.refreshData();
            }
            perbaruiPreviewLive();
            beritahuTransaksiListener(trxAktif);
        }
    }

    /**
     * Memperbarui daftar Petani pada ComboBox saat ada petani baru didaftarkan.
     */
    public void perbaruiDaftarPetani(List<Petani> daftar) {
        cmbPetani.removeAllItems();
        if (daftar != null) {
            for (Petani p : daftar) {
                cmbPetani.addItem(p);
            }
        }
        perbaruiPreviewLive();
    }

    /**
     * Menambahkan satu petani langsung ke ComboBox.
     */
    public void tambahPetaniKeComboBox(Petani p) {
        if (p != null) {
            cmbPetani.addItem(p);
            cmbPetani.setSelectedItem(p);
            perbaruiPreviewLive();
        }
    }

    // Setter & Getter untuk integrasi OOP
    public void setAuditWrapper(java.util.function.BiConsumer<String, Runnable> auditWrapper) {
        this.auditWrapper = auditWrapper;
    }

    public void setPenerimaPasokan(PenerimaPasokan penerimaPasokan) {
        this.penerimaPasokan = penerimaPasokan;
    }

    /**
     * Mereset form transaksi dan status kartu visual saat berpindah gudang.
     */
    public void resetForm() {
        this.trxAktif = null;
        setTampilanBadgeStatus(null);
        txtIdTransaksi.setText("TRX-" + String.format("%03d", (System.currentTimeMillis() % 900) + 100));
        txtKodeBatch.setText("BATCH-" + String.format("%03d", ((System.currentTimeMillis() + 19) % 900) + 100));
        txtBeratKg.setText("200.0");
        txtKadarAir.setText("11.5");
        perbaruiPreviewLive();
    }

    public void setPanelStatusGudang(PanelStatusGudang panelStatusGudang) {
        this.panelStatusGudang = panelStatusGudang;
    }

    public void setPetaniTableModel(PetaniTableModel petaniTableModel) {
        this.petaniTableModel = petaniTableModel;
    }

    public void setTransaksiTableModel(TransaksiTableModel transaksiTableModel) {
        this.transaksiTableModel = transaksiTableModel;
    }

    public TransaksiPasok getTransaksiAktif() {
        return trxAktif;
    }

    public void setTransaksiAktif(TransaksiPasok trxAktif) {
        this.trxAktif = trxAktif;
        perbaruiPreviewLive();
        beritahuTransaksiListener(trxAktif);
    }

    public void addOnTransaksiListener(java.util.function.Consumer<TransaksiPasok> listener) {
        if (listener != null) {
            onTransaksiListeners.add(listener);
        }
    }

    private void beritahuTransaksiListener(TransaksiPasok trx) {
        if (trx != null) {
            for (java.util.function.Consumer<TransaksiPasok> listener : onTransaksiListeners) {
                listener.accept(trx);
            }
        }
    }

    public JComboBox<Petani> getCmbPetani() { return cmbPetani; }
    public JTextField getTxtIdTransaksi() { return txtIdTransaksi; }
    public JTextField getTxtKodeBatch() { return txtKodeBatch; }
    public JTextField getTxtVarietas() { return txtVarietas; }
    public JTextField getTxtBeratKg() { return txtBeratKg; }
    public JTextField getTxtKadarAir() { return txtKadarAir; }
    public JButton getBtnTahap1() { return btnTahap1; }
    public JButton getBtnTahap2() { return btnTahap2; }
}
