package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.TransaksiPasok;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dialog Cetak & Pratinjau Surat Jalan Resmi Pengiriman Tembakau ke Pabrik Rokok.
 */
public class DialogSuratJalan extends JDialog {

    private final PengirimanPabrik pengiriman;
    private final JTextArea txtSuratJalan = new JTextArea();
    private final DecimalFormat formatRupiah;
    private final DecimalFormat formatAngka;

    public DialogSuratJalan(Window parent, PengirimanPabrik pengiriman) {
        super(parent, "Surat Jalan Pengiriman Tembakau ke Pabrik", ModalityType.APPLICATION_MODAL);
        this.pengiriman = pengiriman;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatRupiah = new DecimalFormat("Rp #,##0.00", symbols);
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);

        inisialisasiUI();
        susunTeksSuratJalan();
        setSize(650, 720);
        setLocationRelativeTo(parent);
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(8, 8));

        txtSuratJalan.setEditable(false);
        txtSuratJalan.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtSuratJalan.setBackground(Color.WHITE);
        txtSuratJalan.setForeground(new Color(33, 37, 41));
        txtSuratJalan.setMargin(new Insets(12, 16, 12, 16));

        JScrollPane scrollPane = new JScrollPane(txtSuratJalan);
        scrollPane.setBorder(new CompoundBorder(
            new EmptyBorder(8, 12, 0, 12),
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1)
        ));
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCetak = new JButton("Cetak Surat Jalan");
        btnCetak.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCetak.setBackground(new Color(25, 118, 210));
        btnCetak.setForeground(Color.WHITE);
        btnCetak.addActionListener(e -> {
            try {
                boolean selesai = txtSuratJalan.print();
                if (selesai) {
                    JOptionPane.showMessageDialog(this, "Surat Jalan berhasil dikirim ke antrean cetak printer.",
                            "Cetak Berhasil", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal mencetak: " + ex.getMessage(),
                        "Error Printer", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnTutup = new JButton("Tutup");
        btnTutup.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnTutup.addActionListener(e -> dispose());

        panelTombol.add(btnCetak);
        panelTombol.add(btnTutup);
        add(panelTombol, BorderLayout.SOUTH);
    }

    private void susunTeksSuratJalan() {
        if (pengiriman == null) {
            txtSuratJalan.setText("Data pengiriman tidak ditemukan.");
            return;
        }

        String waktuKirim = pengiriman.getWaktuKirim().format(
            DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID"))
        );

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                 GUDANG PENYANGGA TEMBAKAU RAJANGAN                     \n");
        sb.append("         DIVISI LOGISTIK & DISTRIBUSI PABRIKAN ROKOK NASIONAL           \n");
        sb.append("                Jl. Raya Trunojoyo No. 45, Jawa Timur                   \n");
        sb.append("========================================================================\n");
        sb.append("                  SURAT JALAN / DELIVERY ORDER (DO)                     \n");
        sb.append(String.format(" No. Surat Jalan : %-25s Tanggal: %s\n", pengiriman.getNoSuratJalan(), waktuKirim));
        sb.append("------------------------------------------------------------------------\n");
        sb.append(" TUJUAN PENGIRIMAN:                                                     \n");
        sb.append(String.format(" • Pabrik Penerima : %s\n", pengiriman.getPabrikTujuan().getNama()));
        sb.append(String.format(" • Kota / Alamat   : %s (%s)\n", pengiriman.getPabrikTujuan().getAlamat(), pengiriman.getPabrikTujuan().getKota()));
        sb.append("------------------------------------------------------------------------\n");
        sb.append(" EKSPEDISI & ARMADA TRANSPORTASI:                                       \n");
        sb.append(String.format(" • No. Polisi Truk : %s\n", pengiriman.getNoPolisiTruk()));
        sb.append(String.format(" • Nama Pengemudi  : %s\n", pengiriman.getNamaSopir()));
        sb.append(String.format(" • Status Ekspedisi: %s (Dalam Pengawalan Logistik)\n", pengiriman.getStatusPengiriman()));
        sb.append("------------------------------------------------------------------------\n");
        sb.append(" DAFTAR BATCH TEMBAKAU YANG DIKIRIM:                                    \n");
        sb.append(String.format(" %-4s | %-12s | %-12s | %-10s | %-8s | %-12s\n",
            "No", "ID Transaksi", "Kode Batch", "Varietas", "Berat", "Grade"));
        sb.append("------------------------------------------------------------------------\n");

        int no = 1;
        for (TransaksiPasok t : pengiriman.getDaftarBatch()) {
            sb.append(String.format(" %-4d | %-12s | %-12s | %-10s | %7s kg | %-12s\n",
                no++,
                t.getIdTransaksi(),
                t.getTembakau().getKodeBatch(),
                t.getTembakau().getVarietas(),
                formatAngka.format(t.getTembakau().getBeratKg()),
                t.getTembakau().getGrade().name()));
        }

        sb.append("------------------------------------------------------------------------\n");
        sb.append(String.format(" TOTAL MUATAN FISIK : %s kg\n", formatAngka.format(pengiriman.getTotalBeratKg())));
        sb.append(String.format(" TOTAL ESTIMASI NILAI: %s\n", formatRupiah.format(pengiriman.getTotalNilaiRp())));
        sb.append("========================================================================\n");
        sb.append("   Dikeluarkan Oleh,             Pengemudi Truk,       Penerima Pabrik, \n");
        sb.append("    Kepala Gudang,                                                      \n\n\n\n");
        sb.append(String.format(" ( Moch. Ilham Abdul Aziz )     ( %-16s )   ( ................. ) \n", pengiriman.getNamaSopir()));
        sb.append("========================================================================\n");
        sb.append("   *Barang diterima dalam kondisi segel terpasang utuh dan kering*      \n");

        txtSuratJalan.setText(sb.toString());
        txtSuratJalan.setCaretPosition(0);
    }
}
