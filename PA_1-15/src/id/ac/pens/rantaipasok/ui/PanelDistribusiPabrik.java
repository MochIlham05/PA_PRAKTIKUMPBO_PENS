package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.GudangTembakau;
import id.ac.pens.rantaipasok.PenerimaPasokan;
import id.ac.pens.rantaipasok.TransaksiPasok;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Panel Distribusi & Alur Keluar (Outbound Logistik).
 * Memungkinkan pengelolaan pengiriman tembakau dari Gudang Penyangga ke Pabrikan Rokok.
 */
public class PanelDistribusiPabrik extends JPanel {

    private final JComboBox<PengirimanPabrik.MitraPabrik> cmbPabrik = new JComboBox<>(PengirimanPabrik.MitraPabrik.values());
    private final JTextField txtNoSuratJalan = new JTextField("SJ-2026-001", 12);
    private final JTextField txtNoPolisi = new JTextField("M 8472 UA", 10);
    private final JTextField txtNamaSopir = new JTextField("Bpk. Rahmat Hidayat", 14);

    private final JButton btnKirimPabrik = new JButton("Kirim ke Pabrik (Terbitkan Surat Jalan)");
    private final JButton btnCetakSuratJalan = new JButton("Cetak Surat Jalan");
    private final JButton btnKosongkanGudangManual = new JButton("Kosongkan Muatan Gudang Sekarang");
    private final JCheckBox chkKosongkanGudang = new JCheckBox("Kosongkan muatan fisik gudang setelah surat jalan diterbitkan", true);

    private java.util.function.Consumer<GudangTembakau> onPengosonganGudangListener;

    private final List<PengirimanPabrik> riwayatPengiriman = new ArrayList<>();
    private final PengirimanTableModel pengirimanTableModel = new PengirimanTableModel();
    private final JTable tblPengiriman = new JTable(pengirimanTableModel);

    private PenerimaPasokan gudang;
    private final DecimalFormat formatAngka;
    private final DecimalFormat formatRupiah;

    public PanelDistribusiPabrik() {
        this(null);
    }

    public PanelDistribusiPabrik(PenerimaPasokan gudang) {
        this.gudang = gudang;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);
        this.formatRupiah = new DecimalFormat("Rp #,##0.00", symbols);

        inisialisasiUI();
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Manajemen Distribusi & Pengiriman Pabrik Rokok",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(33, 37, 41)
            ),
            new EmptyBorder(8, 12, 10, 12)
        ));

        // Form Input Ekspedisi (Kiri/Atas)
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font fontLabel = new Font("SansSerif", Font.PLAIN, 12);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Pabrik Rokok Tujuan:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panelForm.add(cmbPabrik, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelForm.add(buatLabel("No. Surat Jalan:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        panelForm.add(txtNoSuratJalan, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panelForm.add(buatLabel("No. Polisi Armada Truk:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panelForm.add(txtNoPolisi, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Nama Pengemudi / Sopir:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        panelForm.add(txtNamaSopir, gbc);

        // Opsi Pengosongan Gudang Pasca-Kirim
        chkKosongkanGudang.setFont(new Font("SansSerif", Font.PLAIN, 11));
        chkKosongkanGudang.setForeground(new Color(55, 71, 79));
        chkKosongkanGudang.setToolTipText("Bila dicentang, seluruh transaksi gudang ini akan diselesaikan dan stok fisik kembali 0 kg.");
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL;
        panelForm.add(chkKosongkanGudang, gbc);

        // Tombol Aksi Kirim & Reset Gudang
        JPanel panelBtnAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBtnAksi.setOpaque(false);

        btnKosongkanGudangManual.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnKosongkanGudangManual.setBackground(new Color(198, 40, 40));
        btnKosongkanGudangManual.setForeground(Color.WHITE);
        btnKosongkanGudangManual.setFocusPainted(false);
        btnKosongkanGudangManual.setToolTipText("Kosongkan muatan fisik gudang aktif saat ini tanpa menerbitkan surat jalan");
        btnKosongkanGudangManual.addActionListener(e -> {
            if (gudang instanceof GudangTembakau && onPengosonganGudangListener != null) {
                onPengosonganGudangListener.accept((GudangTembakau) gudang);
            }
        });
        panelBtnAksi.add(btnKosongkanGudangManual);

        btnKirimPabrik.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnKirimPabrik.setBackground(new Color(25, 118, 210));
        btnKirimPabrik.setForeground(Color.WHITE);
        btnKirimPabrik.setFocusPainted(false);
        btnKirimPabrik.addActionListener(e -> prosesKirimKePabrik());
        panelBtnAksi.add(btnKirimPabrik);

        gbc.gridx = 1; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panelForm.add(panelBtnAksi, gbc);

        // Tabel Riwayat Surat Jalan (Tengah)
        tblPengiriman.setRowHeight(24);
        tblPengiriman.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tblPengiriman.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tblPengiriman.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel panelTabel = new JPanel(new BorderLayout(4, 4));
        panelTabel.setBorder(BorderFactory.createTitledBorder("Riwayat Surat Jalan & Pengiriman Pabrik"));
        panelTabel.add(new JScrollPane(tblPengiriman), BorderLayout.CENTER);

        JPanel panelAksiBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnCetakSuratJalan.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnCetakSuratJalan.setBackground(new Color(67, 160, 71));
        btnCetakSuratJalan.setForeground(Color.WHITE);
        btnCetakSuratJalan.setFocusPainted(false);
        btnCetakSuratJalan.addActionListener(e -> cetakSuratJalanTerpilih());
        panelAksiBawah.add(btnCetakSuratJalan);
        panelTabel.add(panelAksiBawah, BorderLayout.SOUTH);

        add(panelForm, BorderLayout.NORTH);
        add(panelTabel, BorderLayout.CENTER);
    }

    private JLabel buatLabel(String teks, Font font) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(font);
        lbl.setForeground(new Color(73, 80, 87));
        return lbl;
    }

    private void prosesKirimKePabrik() {
        if (!(gudang instanceof GudangTembakau)) {
            JOptionPane.showMessageDialog(this, "Objek gudang belum terhubung.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GudangTembakau gt = (GudangTembakau) gudang;
        List<TransaksiPasok> trxGudang = gt.getDaftarTransaksi();

        if (trxGudang.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Stok gudang masih kosong! Belum ada transaksi yang tersimpan di gudang untuk dikirim ke pabrik.",
                "Gudang Kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil batch yang tersedia di gudang untuk dikirim
        List<TransaksiPasok> batchKirim = new ArrayList<>(trxGudang);

        PengirimanPabrik.MitraPabrik pabrik = (PengirimanPabrik.MitraPabrik) cmbPabrik.getSelectedItem();
        String noSj = txtNoSuratJalan.getText().trim();
        String nopol = txtNoPolisi.getText().trim();
        String sopir = txtNamaSopir.getText().trim();

        try {
            PengirimanPabrik pengirimanBaru = new PengirimanPabrik(noSj, pabrik, batchKirim, nopol, sopir);
            riwayatPengiriman.add(pengirimanBaru);
            pengirimanTableModel.fireTableDataChanged();

            // Auto-increment nomor surat jalan untuk batch berikutnya
            int nextNo = riwayatPengiriman.size() + 1;
            txtNoSuratJalan.setText(String.format("SJ-2026-%03d", nextNo));

            // Tampilkan Surat Jalan Resmi
            new DialogSuratJalan(SwingUtilities.getWindowAncestor(this), pengirimanBaru).setVisible(true);

            // Bila opsi kosongkan dicentang, lakukan reset muatan gudang
            if (chkKosongkanGudang.isSelected() && onPengosonganGudangListener != null) {
                onPengosonganGudangListener.accept(gt);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memproses pengiriman: " + ex.getMessage(),
                "Error Pengiriman", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cetakSuratJalanTerpilih() {
        int row = tblPengiriman.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu baris pengiriman pada tabel terlebih dahulu.",
                "Pilih Baris", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PengirimanPabrik p = riwayatPengiriman.get(row);
        new DialogSuratJalan(SwingUtilities.getWindowAncestor(this), p).setVisible(true);
    }

    public void setGudang(PenerimaPasokan gudang) {
        this.gudang = gudang;
    }

    public void setOnPengosonganGudangListener(java.util.function.Consumer<GudangTembakau> listener) {
        this.onPengosonganGudangListener = listener;
    }

    public List<PengirimanPabrik> getRiwayatPengiriman() {
        return riwayatPengiriman;
    }

    /**
     * Model Tabel untuk Riwayat Pengiriman Pabrik.
     */
    private class PengirimanTableModel extends AbstractTableModel {
        private final String[] kolom = {
            "No. Surat Jalan", "Pabrik Tujuan", "Kota", "Armada Truk", "Pengemudi", "Total Muatan (kg)", "Estimasi Nilai (Rp)", "Waktu Kirim"
        };

        @Override public int getRowCount() { return riwayatPengiriman.size(); }
        @Override public int getColumnCount() { return kolom.length; }
        @Override public String getColumnName(int c) { return kolom[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            PengirimanPabrik p = riwayatPengiriman.get(r);
            switch (c) {
                case 0: return p.getNoSuratJalan();
                case 1: return p.getPabrikTujuan().getNama();
                case 2: return p.getPabrikTujuan().getKota();
                case 3: return p.getNoPolisiTruk();
                case 4: return p.getNamaSopir();
                case 5: return formatAngka.format(p.getTotalBeratKg()) + " kg";
                case 6: return formatRupiah.format(p.getTotalNilaiRp());
                case 7: return p.getWaktuKirim().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                default: return null;
            }
        }
    }
}
