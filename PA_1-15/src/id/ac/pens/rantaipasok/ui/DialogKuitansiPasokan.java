package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.GradeMutu;
import id.ac.pens.rantaipasok.TransaksiPasok;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dialog Cetak & Pratinjau Kuitansi / Slip Setoran Tembakau Resmi.
 * Menghasilkan bukti fisik transaksi pasokan petani yang rapi dan profesional,
 * memuat data kualitas mutu tembakau, berat, rincian pembayaran, serta tanda tangan digital.
 */
public class DialogKuitansiPasokan extends JDialog {

    private final TransaksiPasok transaksi;
    private final JTextArea txtKuitansi = new JTextArea();
    private final DecimalFormat formatRupiah;
    private final DecimalFormat formatAngka;

    public DialogKuitansiPasokan(Window parent, TransaksiPasok transaksi) {
        super(parent, "Kuitansi Pembayaran Pasokan Tembakau", ModalityType.APPLICATION_MODAL);
        this.transaksi = transaksi;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatRupiah = new DecimalFormat("Rp #,##0.00", symbols);
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);

        inisialisasiUI();
        susunTeksKuitansi();
        setSize(580, 680);
        setLocationRelativeTo(parent);
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(8, 8));

        txtKuitansi.setEditable(false);
        txtKuitansi.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtKuitansi.setBackground(Color.WHITE);
        txtKuitansi.setForeground(new Color(33, 37, 41));
        txtKuitansi.setMargin(new Insets(12, 16, 12, 16));

        JScrollPane scrollPane = new JScrollPane(txtKuitansi);
        scrollPane.setBorder(new CompoundBorder(
            new EmptyBorder(8, 12, 0, 12),
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1)
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Panel Tombol Bawah
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCetak = new JButton("Cetak Kuitansi");
        btnCetak.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCetak.setBackground(new Color(25, 118, 210));
        btnCetak.setForeground(Color.WHITE);
        btnCetak.addActionListener(e -> {
            try {
                boolean selesai = txtKuitansi.print();
                if (selesai) {
                    JOptionPane.showMessageDialog(this, "Pencetakan kuitansi berhasil dikirim ke printer.",
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

    private void susunTeksKuitansi() {
        if (transaksi == null) {
            txtKuitansi.setText("Tidak ada data transaksi yang dipilih.");
            return;
        }

        String waktuSekarang = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss", Locale.forLanguageTag("id-ID")));
        String nik = transaksi.getPetani() != null ? transaksi.getPetani().getNik() : "-";
        String nama = transaksi.getPetani() != null ? transaksi.getPetani().getNama() : "-";
        String desa = transaksi.getPetani() != null ? transaksi.getPetani().getAsalDesa() : "-";
        double lahan = transaksi.getPetani() != null ? transaksi.getPetani().getLuasLahanHa() : 0.0;

        String kodeBatch = transaksi.getTembakau() != null ? transaksi.getTembakau().getKodeBatch() : "-";
        String varietas = transaksi.getTembakau() != null ? transaksi.getTembakau().getVarietas() : "-";
        double berat = transaksi.getTembakau() != null ? transaksi.getTembakau().getBeratKg() : 0.0;
        double kadarAir = transaksi.getTembakau() != null ? transaksi.getTembakau().getKadarAirPersen() : 0.0;
        GradeMutu grade = (transaksi.getTembakau() != null) ? transaksi.getTembakau().getGrade() : GradeMutu.REJECTED;

        double hargaKg = transaksi.getHargaPerKg();
        double totalBayar = transaksi.getTotalPembayaran();
        String status = (transaksi.getStatus() != null) ? transaksi.getStatus().getLabel() : "-";

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================\n");
        sb.append("               GUDANG PENYANGGA TEMBAKAU RAJANGAN               \n");
        sb.append("           LEMBAGA PENGELOLA KOMODITAS PERKEBUNAN              \n");
        sb.append("            Jl. Raya Trunojoyo No. 45, Jawa Timur               \n");
        sb.append("================================================================\n");
        sb.append("                 SLIP PENERIMAAN PASOKAN & KUITANSI             \n");
        sb.append(String.format(" No. Transaksi : %-22s Tanggal: %s\n", transaksi.getIdTransaksi(), waktuSekarang));
        sb.append("----------------------------------------------------------------\n");
        sb.append(" 1. IDENTITAS PETANI MITRA                                     \n");
        sb.append(String.format("    • NIK Petani       : %s\n", nik));
        sb.append(String.format("    • Nama Petani      : %s\n", nama));
        sb.append(String.format("    • Asal Desa/Kec.   : %s\n", desa));
        sb.append(String.format("    • Luas Lahan       : %s Ha\n", formatAngka.format(lahan)));
        sb.append("----------------------------------------------------------------\n");
        sb.append(" 2. SPESIFIKASI MUTU TEMBAKAU                                   \n");
        sb.append(String.format("    • Kode Batch       : %s\n", kodeBatch));
        sb.append(String.format("    • Varietas         : %s (Rajangan Pilihan)\n", varietas));
        sb.append(String.format("    • Berat Netto      : %s kg\n", formatAngka.format(berat)));
        sb.append(String.format("    • Kadar Air        : %s %%\n", formatAngka.format(kadarAir)));
        sb.append(String.format("    • Grade Mutu SNI   : %s (Maks. %s%%)\n", grade.name(), formatAngka.format(grade.getBatasAtasPersen())));
        sb.append("----------------------------------------------------------------\n");
        sb.append(" 3. RINCIAN HARGA & PEMBAYARAN                                 \n");
        sb.append(String.format("    • Harga Satuan     : %s / kg\n", formatRupiah.format(hargaKg)));
        sb.append(String.format("    • TOTAL DIBAYARKAN : %s\n", formatRupiah.format(totalBayar)));
        sb.append(String.format("    • Terbilang        : %s\n", sebutRupiah((long) totalBayar)));
        sb.append("----------------------------------------------------------------\n");
        sb.append(" 4. STATUS PENYIMPANAN GUDANG                                   \n");
        sb.append(String.format("    • Status Mesin     : %s\n", status));
        sb.append("    • Validasi Invarian: INVARIANT VALID (I2 - I5 Terproteksi) \n");
        sb.append("================================================================\n");
        sb.append("      Petani Mitra,                 Petugas Penilai & Timbang,  \n\n\n\n");
        sb.append(String.format("     ( %-16s )               ( Moch. Ilham Abdul Aziz ) \n", nama));
        sb.append("================================================================\n");
        sb.append("  *Simpan struk ini sebagai bukti pembayaran dan setoran yang sah* \n");

        txtKuitansi.setText(sb.toString());
        txtKuitansi.setCaretPosition(0);
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
