package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.GudangTembakau;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Panel Swing untuk menampilkan status real-time Gudang Tembakau.
 * Menampilkan:
 * 1. Informasi gudang (Kode, Nama, Kapasitas Maksimal, Stok Tersimpan, Sisa Kapasitas).
 * 2. JProgressBar visual dengan pewarnaan dinamis:
 *    - Hijau  (&lt; 70%)
 *    - Kuning (70% - 90%)
 *    - Merah  (&gt; 90%)
 * 3. Badge indikator status audit invarian (gudang.periksaInvarian()) yang
 *    menampilkan "INVARIANT VALID (I2-I4)" secara real-time.
 */
public class PanelStatusGudang extends JPanel {

    // Palet warna status progress bar
    public static final Color WARNA_HIJAU = new Color(46, 125, 50);   // < 70%
    public static final Color WARNA_KUNING = new Color(230, 149, 0);  // 70% - 90%
    public static final Color WARNA_MERAH = new Color(198, 40, 40);   // > 90%

    // Palet warna badge invarian
    public static final Color BADGE_VALID_BG = new Color(232, 245, 233);
    public static final Color BADGE_VALID_FG = new Color(27, 94, 32);
    public static final Color BADGE_VALID_BORDER = new Color(76, 175, 80);

    public static final Color BADGE_INVALID_BG = new Color(255, 235, 238);
    public static final Color BADGE_INVALID_FG = new Color(183, 28, 28);
    public static final Color BADGE_INVALID_BORDER = new Color(239, 83, 80);

    private GudangTembakau gudang;

    // Komponen visual
    private final JLabel lblDaerahVal = new JLabel("-");
    private final JLabel lblKodeVal = new JLabel("-");
    private final JLabel lblNamaVal = new JLabel("-");
    private final JLabel lblWilayahVal = new JLabel("-");
    private final JLabel lblKapasitasMaksVal = new JLabel("-");
    private final JLabel lblStokTersimpanVal = new JLabel("-");
    private final JLabel lblSisaKapasitasVal = new JLabel("-");

    private String daerah = "-";
    private String wilayah = "-";

    private final JProgressBar progressBarKapasitas = new JProgressBar(0, 100);
    private final JLabel lblBadgeInvarian = new JLabel("INVARIANT VALID (I2-I4)", SwingConstants.CENTER);

    private final DecimalFormat formatAngka;

    public PanelStatusGudang() {
        this((GudangTembakau) null);
    }

    public PanelStatusGudang(id.ac.pens.rantaipasok.PenerimaPasokan penerima) {
        this(penerima instanceof GudangTembakau ? (GudangTembakau) penerima : null);
    }

    public PanelStatusGudang(GudangTembakau gudang) {
        this.gudang = gudang;

        // Inisialisasi formatter angka (Locale Indonesia: ribuan titik, desimal koma)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);

        inisialisasiUI();
        perbaruiStatus();
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Status & Audit Invarian Gudang",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(33, 37, 41)
            ),
            new EmptyBorder(8, 12, 10, 12)
        ));

        // Panel Grid Informasi Gudang (7 Field)
        JPanel panelInfo = new JPanel(new GridLayout(7, 2, 8, 4));
        panelInfo.setOpaque(false);

        Font fontLabel = new Font("SansSerif", Font.PLAIN, 12);
        Font fontValue = new Font("SansSerif", Font.BOLD, 12);

        lblDaerahVal.setFont(fontValue);
        lblKodeVal.setFont(fontValue);
        lblNamaVal.setFont(fontValue);
        lblWilayahVal.setFont(fontValue);
        lblKapasitasMaksVal.setFont(fontValue);
        lblStokTersimpanVal.setFont(fontValue);
        lblSisaKapasitasVal.setFont(fontValue);

        panelInfo.add(buatLabel("Daerah / Wilayah:", fontLabel));
        panelInfo.add(lblDaerahVal);

        panelInfo.add(buatLabel("Kode Gudang:", fontLabel));
        panelInfo.add(lblKodeVal);

        panelInfo.add(buatLabel("Nama Gudang:", fontLabel));
        panelInfo.add(lblNamaVal);

        panelInfo.add(buatLabel("Kategori Zonasi:", fontLabel));
        panelInfo.add(lblWilayahVal);

        panelInfo.add(buatLabel("Kapasitas Maksimal:", fontLabel));
        panelInfo.add(lblKapasitasMaksVal);

        panelInfo.add(buatLabel("Stok Tersimpan:", fontLabel));
        panelInfo.add(lblStokTersimpanVal);

        panelInfo.add(buatLabel("Sisa Kapasitas:", fontLabel));
        panelInfo.add(lblSisaKapasitasVal);

        // Konfigurasi ProgressBar
        progressBarKapasitas.setStringPainted(true);
        progressBarKapasitas.setFont(new Font("SansSerif", Font.BOLD, 11));
        progressBarKapasitas.setPreferredSize(new Dimension(180, 24));
        // Gunakan UI kustom agar perubahan warna tetap konsisten di seluruh Look and Feel
        progressBarKapasitas.setUI(new BasicProgressBarUI() {
            @Override
            protected Color getSelectionBackground() {
                return Color.DARK_GRAY;
            }

            @Override
            protected Color getSelectionForeground() {
                return Color.WHITE;
            }
        });

        // Panel Progress Bar
        JPanel panelProgress = new JPanel(new BorderLayout(5, 4));
        panelProgress.setOpaque(false);
        JLabel lblHeaderProgress = new JLabel("Utilisasi Kapasitas Fisik:");
        lblHeaderProgress.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelProgress.add(lblHeaderProgress, BorderLayout.NORTH);
        panelProgress.add(progressBarKapasitas, BorderLayout.CENTER);

        // Konfigurasi Badge Invarian
        lblBadgeInvarian.setOpaque(true);
        lblBadgeInvarian.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblBadgeInvarian.setPreferredSize(new Dimension(240, 32));
        lblBadgeInvarian.setBorder(new CompoundBorder(
            new LineBorder(BADGE_VALID_BORDER, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));

        JPanel panelBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        panelBadge.setOpaque(false);
        panelBadge.add(lblBadgeInvarian);

        // Gabungkan bagian bawah (Progress Bar + Badge)
        JPanel panelBawah = new JPanel(new BorderLayout(0, 8));
        panelBawah.setOpaque(false);
        panelBawah.add(panelProgress, BorderLayout.NORTH);
        panelBawah.add(panelBadge, BorderLayout.SOUTH);

        add(panelInfo, BorderLayout.CENTER);
        add(panelBawah, BorderLayout.SOUTH);
    }

    private JLabel buatLabel(String teks, Font font) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(font);
        lbl.setForeground(new Color(73, 80, 87));
        return lbl;
    }

    /**
     * Memperbarui seluruh tampilan informasi, visualisasi persentase kapasitas,
     * dan status audit invarian secara real-time.
     */
    public void perbaruiStatus() {
        if (gudang == null) {
            lblKodeVal.setText("-");
            lblNamaVal.setText("-");
            lblKapasitasMaksVal.setText("-");
            lblStokTersimpanVal.setText("-");
            lblSisaKapasitasVal.setText("-");
            progressBarKapasitas.setValue(0);
            progressBarKapasitas.setString("0.0% (0 / 0 kg)");
            progressBarKapasitas.setForeground(WARNA_HIJAU);
            setTampilanBadge(true, "INVARIANT VALID (I2-I4)");
            return;
        }

        // 1. Perbarui teks informasi gudang
        lblKodeVal.setText(gudang.getKodeGudang());
        lblNamaVal.setText(gudang.getNamaGudang());
        lblKapasitasMaksVal.setText(formatAngka.format(gudang.getKapasitasMaksKg()) + " kg");
        lblStokTersimpanVal.setText(formatAngka.format(gudang.getStokTersimpanKg()) + " kg");
        lblSisaKapasitasVal.setText(formatAngka.format(gudang.cekKapasitasTersedia()) + " kg");

        // 2. Perbarui JProgressBar dengan pewarnaan dinamis
        double kapasitas = gudang.getKapasitasMaksKg();
        double stok = gudang.getStokTersimpanKg();
        double persentase = (kapasitas > 0.0) ? (stok / kapasitas) * 100.0 : 0.0;
        int persentaseBulat = (int) Math.round(persentase);

        progressBarKapasitas.setValue(Math.min(100, Math.max(0, persentaseBulat)));
        progressBarKapasitas.setString(String.format(Locale.ROOT, "%.1f%% (%s / %s kg)",
                persentase, formatAngka.format(stok), formatAngka.format(kapasitas)));

        // Aturan perubahan warna dinamis:
        // - Hijau:  < 70%
        // - Kuning: 70% - 90%
        // - Merah:  > 90%
        if (persentase < 70.0) {
            progressBarKapasitas.setForeground(WARNA_HIJAU);
        } else if (persentase <= 90.0) {
            progressBarKapasitas.setForeground(WARNA_KUNING);
        } else {
            progressBarKapasitas.setForeground(WARNA_MERAH);
        }

        // 3. Perbarui indikator audit invarian secara real-time
        boolean invarianValid = gudang.periksaInvarian();
        if (invarianValid) {
            setTampilanBadge(true, "INVARIANT VALID (I2-I4)");
        } else {
            setTampilanBadge(false, "INVARIANT VIOLATED (I2-I4)");
        }

        repaint();
    }

    /**
     * Alias untuk perbaruiStatus() agar mudah dipanggil sesuai konvensi.
     */
    public void updateStatus() {
        perbaruiStatus();
    }

    public void setDaerah(String daerah) {
        this.daerah = (daerah != null && !daerah.isBlank()) ? daerah.trim() : "-";
        lblDaerahVal.setText(this.daerah);
    }

    public String getDaerah() {
        return daerah;
    }

    public void setWilayah(String wilayah) {
        this.wilayah = (wilayah != null && !wilayah.isBlank()) ? wilayah.trim() : "-";
        lblWilayahVal.setText(this.wilayah);
    }

    public String getWilayah() {
        return wilayah;
    }

    public void setGudang(GudangTembakau gudang) {
        this.gudang = gudang;
        perbaruiStatus();
    }

    public void setGudang(id.ac.pens.rantaipasok.PenerimaPasokan penerima) {
        if (penerima instanceof GudangTembakau) {
            this.gudang = (GudangTembakau) penerima;
        } else {
            this.gudang = null;
        }
        perbaruiStatus();
    }

    public GudangTembakau getGudang() {
        return gudang;
    }

    public JProgressBar getProgressBarKapasitas() {
        return progressBarKapasitas;
    }

    public JLabel getBadgeInvarian() {
        return lblBadgeInvarian;
    }

    private void setTampilanBadge(boolean valid, String pesan) {
        lblBadgeInvarian.setText(pesan);
        if (valid) {
            lblBadgeInvarian.setBackground(BADGE_VALID_BG);
            lblBadgeInvarian.setForeground(BADGE_VALID_FG);
            lblBadgeInvarian.setBorder(new CompoundBorder(
                new LineBorder(BADGE_VALID_BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)
            ));
        } else {
            lblBadgeInvarian.setBackground(BADGE_INVALID_BG);
            lblBadgeInvarian.setForeground(BADGE_INVALID_FG);
            lblBadgeInvarian.setBorder(new CompoundBorder(
                new LineBorder(BADGE_INVALID_BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)
            ));
        }
    }
}
