package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.Petani;
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
 * Panel Kasir & Pelunasan Saldo Petani (Fintech & Payout Module).
 * Memfasilitasi pembayaran hasil panen tembakau secara transparan dan terdokumentasi.
 */
public class PanelKasirPembayaran extends JPanel {

    private final JComboBox<Petani> cmbPetani = new JComboBox<>();
    private final JComboBox<PembayaranPetani.MetodeBayar> cmbMetode = new JComboBox<>(PembayaranPetani.MetodeBayar.values());
    private final JTextField txtNoBukti = new JTextField("BKK-2026-001", 12);
    private final JTextField txtNoRekening = new JTextField("0018-01-084729-50-3 (BRI)", 18);

    private final JLabel lblTotalSaldoKg = new JLabel("0,00 kg");
    private final JLabel lblTotalEstimasiRp = new JLabel("Rp 0,00");
    private final JButton btnBayarPetani = new JButton("Proses Pembayaran (Kas Keluar)");
    private final JButton btnCetakBukti = new JButton("Cetak Bukti Pembayaran");

    private final List<PembayaranPetani> riwayatPembayaran = new ArrayList<>();
    private final PembayaranTableModel pembayaranTableModel = new PembayaranTableModel();
    private final JTable tblPembayaran = new JTable(pembayaranTableModel);

    private List<Petani> daftarPetani = new ArrayList<>();
    private List<TransaksiPasok> riwayatSemuaTransaksi = new ArrayList<>();
    private final java.util.Set<String> idTrxTerbayar = new java.util.HashSet<>();
    private String identitasGudang = "Gudang Penyangga Pusat";

    private final DecimalFormat formatAngka;
    private final DecimalFormat formatRupiah;

    public PanelKasirPembayaran() {
        this(null, null);
    }

    public PanelKasirPembayaran(List<Petani> daftarPetani, List<TransaksiPasok> riwayatTransaksi) {
        if (daftarPetani != null) this.daftarPetani = daftarPetani;
        if (riwayatTransaksi != null) this.riwayatSemuaTransaksi = riwayatTransaksi;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);
        this.formatRupiah = new DecimalFormat("Rp #,##0.00", symbols);

        inisialisasiUI();
        pasangListener();
    }

    public void setIdentitasGudang(String namaGudang, String wilayah) {
        if (namaGudang == null || namaGudang.trim().isEmpty()) {
            this.identitasGudang = "Seluruh Gudang";
        } else {
            String zona = (wilayah != null && !wilayah.trim().isEmpty()) ? "[" + wilayah + "] " : "";
            this.identitasGudang = zona + namaGudang;
        }
        perbaruiJudulBorder();
    }

    private void perbaruiJudulBorder() {
        setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Kasir & Pelunasan Hasil Panen Petani Mitra - " + identitasGudang,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(33, 37, 41)
            ),
            new EmptyBorder(8, 12, 10, 12)
        ));
        revalidate();
        repaint();
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(10, 10));
        perbaruiJudulBorder();

        // Form Pembayaran
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font fontLabel = new Font("SansSerif", Font.PLAIN, 12);
        Font fontValueBold = new Font("SansSerif", Font.BOLD, 13);

        cmbPetani.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                   int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Petani) {
                    Petani p = (Petani) value;
                    setText(p.getNama() + " (" + p.getAsalDesa() + ") [NIK: " + p.getNik() + "]");
                } else if (value == null) {
                    setText("-- Pilih Petani Penerima --");
                }
                return this;
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Petani Penerima Dana:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panelForm.add(cmbPetani, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelForm.add(buatLabel("No. Bukti Kas Keluar:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        panelForm.add(txtNoBukti, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Metode Pembayaran:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panelForm.add(cmbMetode, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panelForm.add(buatLabel("No. Rekening / Bank Tujuan:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        panelForm.add(txtNoRekening, gbc);

        // Ringkasan Tagihan Petani
        lblTotalSaldoKg.setFont(fontValueBold);
        lblTotalSaldoKg.setForeground(new Color(25, 118, 210));
        lblTotalEstimasiRp.setFont(fontValueBold);
        lblTotalEstimasiRp.setForeground(new Color(46, 125, 50));

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Total Saldo Setoran:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        panelForm.add(lblTotalSaldoKg, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Total Hak Pembayaran:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        panelForm.add(lblTotalEstimasiRp, gbc);

        // Tombol Proses Bayar
        btnBayarPetani.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnBayarPetani.setBackground(new Color(46, 125, 50));
        btnBayarPetani.setForeground(Color.WHITE);
        btnBayarPetani.setFocusPainted(false);
        btnBayarPetani.addActionListener(e -> prosesPelunasanPetani());

        gbc.gridx = 1; gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panelForm.add(btnBayarPetani, gbc);

        // Tabel Riwayat Pembayaran Kasir
        tblPembayaran.setRowHeight(24);
        tblPembayaran.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tblPembayaran.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tblPembayaran.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel panelTabel = new JPanel(new BorderLayout(4, 4));
        panelTabel.setBorder(BorderFactory.createTitledBorder("Buku Kas Keluar & Riwayat Pelunasan Petani"));
        panelTabel.add(new JScrollPane(tblPembayaran), BorderLayout.CENTER);

        JPanel panelAksiBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnCetakBukti.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnCetakBukti.setBackground(new Color(25, 118, 210));
        btnCetakBukti.setForeground(Color.WHITE);
        btnCetakBukti.setFocusPainted(false);
        btnCetakBukti.addActionListener(e -> cetakBuktiTerpilih());
        panelAksiBawah.add(btnCetakBukti);
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

    private void pasangListener() {
        cmbPetani.addActionListener(e -> hitungTagihanPetaniTerpilih());
    }

    public void perbaruiDaftarPetani(List<Petani> petaniList, List<TransaksiPasok> trxList) {
        this.daftarPetani = (petaniList != null) ? petaniList : new ArrayList<>();
        this.riwayatSemuaTransaksi = (trxList != null) ? trxList : new ArrayList<>();

        cmbPetani.removeAllItems();
        for (Petani p : this.daftarPetani) {
            cmbPetani.addItem(p);
        }
        hitungTagihanPetaniTerpilih();
    }

    private void hitungTagihanPetaniTerpilih() {
        Petani p = (Petani) cmbPetani.getSelectedItem();
        if (p == null) {
            lblTotalSaldoKg.setText("0,00 kg");
            lblTotalEstimasiRp.setText("Rp 0,00");
            return;
        }

        double totalKgGudang = 0.0;
        double tagihanRp = 0.0;
        int countBelumLunas = 0;

        for (TransaksiPasok t : riwayatSemuaTransaksi) {
            if (t.getPetani() != null && t.getPetani().getNik().equals(p.getNik())) {
                // Hanya transaksi yang resmi tersimpan di gudang ini
                if (t.getStatus() == id.ac.pens.rantaipasok.StatusTransaksi.DISIMPAN_DI_GUDANG) {
                    if (t.getTembakau() != null) {
                        totalKgGudang += t.getTembakau().getBeratKg();
                    }
                    if (!idTrxTerbayar.contains(t.getIdTransaksi())) {
                        tagihanRp += t.getTotalPembayaran();
                        countBelumLunas++;
                    }
                }
            }
        }

        if (totalKgGudang == 0.0) {
            lblTotalSaldoKg.setText("0,00 kg (Belum ada setoran di gudang ini)");
            lblTotalEstimasiRp.setText("Rp 0,00");
            lblTotalEstimasiRp.setForeground(new Color(108, 117, 125));
        } else if (countBelumLunas == 0) {
            lblTotalSaldoKg.setText(formatAngka.format(totalKgGudang) + " kg (Tersimpan di gudang ini)");
            lblTotalEstimasiRp.setText("LUNAS (Rp 0,00)");
            lblTotalEstimasiRp.setForeground(new Color(46, 125, 50));
        } else {
            lblTotalSaldoKg.setText(formatAngka.format(totalKgGudang) + " kg (Tersimpan di gudang ini)");
            lblTotalEstimasiRp.setText(formatRupiah.format(tagihanRp));
            lblTotalEstimasiRp.setForeground(new Color(198, 40, 40));
        }
    }

    private void prosesPelunasanPetani() {
        Petani p = (Petani) cmbPetani.getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Silakan pilih petani terlebih dahulu.", "Pilih Petani", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<TransaksiPasok> trxBelumLunas = new ArrayList<>();
        for (TransaksiPasok t : riwayatSemuaTransaksi) {
            if (t.getPetani() != null && t.getPetani().getNik().equals(p.getNik())) {
                if (t.getStatus() == id.ac.pens.rantaipasok.StatusTransaksi.DISIMPAN_DI_GUDANG
                        && !idTrxTerbayar.contains(t.getIdTransaksi())) {
                    trxBelumLunas.add(t);
                }
            }
        }

        if (trxBelumLunas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Petani ini tidak memiliki tagihan tertunda.\nSemua transaksi yang masuk gudang sudah lunas atau belum ada pasokan yang diterima.",
                "Tidak Ada Tagihan", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String noBkk = txtNoBukti.getText().trim();
        PembayaranPetani.MetodeBayar metode = (PembayaranPetani.MetodeBayar) cmbMetode.getSelectedItem();
        String norek = txtNoRekening.getText().trim();

        try {
            PembayaranPetani bayarBaru = new PembayaranPetani(noBkk, p, trxBelumLunas, metode, norek);
            for (TransaksiPasok t : trxBelumLunas) {
                idTrxTerbayar.add(t.getIdTransaksi());
            }

            riwayatPembayaran.add(bayarBaru);
            pembayaranTableModel.fireTableDataChanged();
            hitungTagihanPetaniTerpilih();

            int nextNo = riwayatPembayaran.size() + 1;
            txtNoBukti.setText(String.format("BKK-2026-%03d", nextNo));

            // Buka Slip Bukti Kas Keluar
            new DialogBuktiPembayaran(SwingUtilities.getWindowAncestor(this), bayarBaru).setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memproses pembayaran: " + ex.getMessage(),
                "Error Kasir", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cetakBuktiTerpilih() {
        int row = tblPembayaran.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu baris pembayaran pada tabel.",
                "Pilih Baris", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PembayaranPetani bayar = riwayatPembayaran.get(row);
        new DialogBuktiPembayaran(SwingUtilities.getWindowAncestor(this), bayar).setVisible(true);
    }

    public List<PembayaranPetani> getRiwayatPembayaran() {
        return riwayatPembayaran;
    }

    private class PembayaranTableModel extends AbstractTableModel {
        private final String[] kolom = {
            "No. Bukti Kas", "Nama Petani", "NIK", "Total Muatan (kg)", "Total Dibayar (Rp)", "Metode Bayar", "Status", "Waktu Pelunasan"
        };

        @Override public int getRowCount() { return riwayatPembayaran.size(); }
        @Override public int getColumnCount() { return kolom.length; }
        @Override public String getColumnName(int c) { return kolom[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            PembayaranPetani p = riwayatPembayaran.get(r);
            switch (c) {
                case 0: return p.getNoBuktiBayar();
                case 1: return p.getPetani().getNama();
                case 2: return p.getPetani().getNik();
                case 3: return formatAngka.format(p.getTotalBeratKg()) + " kg";
                case 4: return formatRupiah.format(p.getTotalNominalRp());
                case 5: return p.getMetodeBayar().getLabel();
                case 6: return p.getStatus();
                case 7: return p.getWaktuBayar().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                default: return null;
            }
        }
    }
}
