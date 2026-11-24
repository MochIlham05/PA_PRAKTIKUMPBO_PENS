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
 * Dialog Cetak & Pratinjau Bukti Kas Keluar / Slip Pencairan Dana Petani.
 */
public class DialogBuktiPembayaran extends JDialog {

    private final PembayaranPetani pembayaran;
    private final JTextArea txtBukti = new JTextArea();
    private final DecimalFormat formatRupiah;
    private final DecimalFormat formatAngka;

    public DialogBuktiPembayaran(Window parent, PembayaranPetani pembayaran) {
        super(parent, "Bukti Pencairan Dana & Pembayaran Petani", ModalityType.APPLICATION_MODAL);
        this.pembayaran = pembayaran;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatRupiah = new DecimalFormat("Rp #,##0.00", symbols);
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);

        inisialisasiUI();
        susunTeksBukti();
        setSize(600, 680);
        setLocationRelativeTo(parent);
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(8, 8));

        txtBukti.setEditable(false);
        txtBukti.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtBukti.setBackground(Color.WHITE);
        txtBukti.setForeground(new Color(33, 37, 41));
        txtBukti.setMargin(new Insets(12, 16, 12, 16));

        JScrollPane scrollPane = new JScrollPane(txtBukti);
        scrollPane.setBorder(new CompoundBorder(
            new EmptyBorder(8, 12, 0, 12),
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1)
        ));
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCetak = new JButton("Cetak Bukti Pembayaran");
        btnCetak.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCetak.setBackground(new Color(46, 125, 50));
        btnCetak.setForeground(Color.WHITE);
        btnCetak.addActionListener(e -> {
            try {
                boolean selesai = txtBukti.print();
                if (selesai) {
                    JOptionPane.showMessageDialog(this, "Bukti Pembayaran berhasil dikirim ke printer.",
                            "Cetak Sukses", JOptionPane.INFORMATION_MESSAGE);
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

    private void susunTeksBukti() {
        if (pembayaran == null) {
            txtBukti.setText("Data pembayaran tidak ditemukan.");
            return;
        }

        String waktuBayar = pembayaran.getWaktuBayar().format(
            DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID"))
        );

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================\n");
        sb.append("               GUDANG PENYANGGA TEMBAKAU RAJANGAN               \n");
        sb.append("                 DIVISI KEUANGAN & KASIR                        \n");
        sb.append("        Jl. Raya Trunojoyo No. 45, Jawa Timur                   \n");
        sb.append("================================================================\n");
        sb.append("                 BUKTI PENGELUARAN KAS / PEMBAYARAN             \n");
        sb.append(String.format(" No. Kuitansi  : %-22s Tanggal: %s\n", pembayaran.getNoBuktiBayar(), waktuBayar));
        sb.append("----------------------------------------------------------------\n");
        sb.append(" PENERIMA PEMBAYARAN:                                           \n");
        sb.append(String.format(" • Nama Petani : %s\n", pembayaran.getPetani().getNama()));
        sb.append(String.format(" • NIK Petani  : %s\n", pembayaran.getPetani().getNik()));
        sb.append(String.format(" • Asal Desa   : %s\n", pembayaran.getPetani().getAsalDesa()));
        sb.append("----------------------------------------------------------------\n");
        sb.append(" METODE TRANSAKSI KEUANGAN:                                     \n");
        sb.append(String.format(" • Cara Bayar  : %s\n", pembayaran.getMetodeBayar().getLabel()));
        sb.append(String.format(" • No. Rekening: %s\n", pembayaran.getNoRekeningTujuan()));
        sb.append(String.format(" • Status Dana : %s (Telah Ditransfer / Dibayarkan Tunai)\n", pembayaran.getStatus()));
        sb.append("----------------------------------------------------------------\n");
        sb.append(" DAFTAR TRANSAKSI PASOKAN YANG DILUNASI:                        \n");
        sb.append(String.format(" %-4s | %-12s | %-10s | %-12s | %-16s\n",
            "No", "ID Transaksi", "Berat (kg)", "Grade Mutu", "Nilai (Rp)"));
        sb.append("----------------------------------------------------------------\n");

        int no = 1;
        for (TransaksiPasok t : pembayaran.getDaftarTransaksi()) {
            sb.append(String.format(" %-4d | %-12s | %8s kg | %-12s | %16s\n",
                no++,
                t.getIdTransaksi(),
                formatAngka.format(t.getTembakau().getBeratKg()),
                t.getTembakau().getGrade().name(),
                formatRupiah.format(t.getTotalPembayaran())));
        }

        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format(" TOTAL BERAT PASOKAN : %s kg\n", formatAngka.format(pembayaran.getTotalBeratKg())));
        sb.append(String.format(" TOTAL DANA DICAIRKAN: %s\n", formatRupiah.format(pembayaran.getTotalNominalRp())));
        sb.append(String.format(" Terbilang           : %s\n", sebutRupiah((long) pembayaran.getTotalNominalRp())));
        sb.append("================================================================\n");
        sb.append("     Penerima Dana / Petani,              Kasir / Bendahara,    \n\n\n\n");
        sb.append(String.format("    ( %-22s )       ( Moch. Ilham Abdul Aziz )  \n", pembayaran.getPetani().getNama()));
        sb.append("================================================================\n");
        sb.append("  *Dokumen ini merupakan bukti pelunasan sah dan mengikat*      \n");

        txtBukti.setText(sb.toString());
        txtBukti.setCaretPosition(0);
    }

    private String sebutRupiah(long angka) {
        if (angka <= 0) return "Nol Rupiah";
        String[] satuan = {"", "Satu", "Dua", "Tiga", "Empat", "Lima", "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas"};
        return terbilang(angka, satuan).trim() + " Rupiah";
    }

    private String terbilang(long n, String[] satuan) {
        if (n < 12) return " " + satuan[(int) n];
        if (n < 20) return terbilang(n - 10, satuan) + " Belas";
        if (n < 100) return terbilang(n / 10, satuan) + " Puluh" + terbilang(n % 10, satuan);
        if (n < 200) return " Seratus" + terbilang(n - 100, satuan);
        if (n < 1000) return terbilang(n / 100, satuan) + " Ratus" + terbilang(n % 100, satuan);
        if (n < 2000) return " Seribu" + terbilang(n - 1000, satuan);
        if (n < 1000000) return terbilang(n / 1000, satuan) + " Ribu" + terbilang(n % 1000, satuan);
        if (n < 1000000000) return terbilang(n / 1000000, satuan) + " Juta" + terbilang(n % 1000000, satuan);
        return terbilang(n / 1000000000, satuan) + " Miliar" + terbilang(n % 1000000000, satuan);
    }
}
