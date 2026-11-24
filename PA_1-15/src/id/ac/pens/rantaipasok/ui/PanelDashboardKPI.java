package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.GradeMutu;
import id.ac.pens.rantaipasok.Petani;
import id.ac.pens.rantaipasok.TransaksiPasok;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Panel Dashboard Eksekutif & Key Performance Indicators (KPI).
 * Menyajikan analitik visual agregat dari seluruh rantai pasok tembakau.
 */
public class PanelDashboardKPI extends JPanel {

    private final JLabel lblValTotalKg = new JLabel("0,00 kg");
    private final JLabel lblValTotalRp = new JLabel("Rp 0,00");
    private final JLabel lblValAvgKadarAir = new JLabel("0,00 %");
    private final JLabel lblValProduktivitas = new JLabel("0,00 kg/Ha");

    private final JLabel lblCountGradeA = new JLabel("0 batch (0.0%)");
    private final JLabel lblCountGradeB = new JLabel("0 batch (0.0%)");
    private final JLabel lblCountGradeC = new JLabel("0 batch (0.0%)");
    private final JLabel lblCountRejected = new JLabel("0 batch (0.0%)");

    private final JProgressBar barGradeA = new JProgressBar(0, 100);
    private final JProgressBar barGradeB = new JProgressBar(0, 100);
    private final JProgressBar barGradeC = new JProgressBar(0, 100);
    private final JProgressBar barRejected = new JProgressBar(0, 100);

    private List<Petani> daftarPetani = new ArrayList<>();
    private List<TransaksiPasok> riwayatSemuaTransaksi = new ArrayList<>();
    private String identitasGudang = "Semua Gudang";

    private final DecimalFormat formatAngka;
    private final DecimalFormat formatRupiah;

    public PanelDashboardKPI() {
        this(null, null);
    }

    public PanelDashboardKPI(List<Petani> daftarPetani, List<TransaksiPasok> riwayatSemuaTransaksi) {
        if (daftarPetani != null) this.daftarPetani = daftarPetani;
        if (riwayatSemuaTransaksi != null) this.riwayatSemuaTransaksi = riwayatSemuaTransaksi;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);
        this.formatRupiah = new DecimalFormat("Rp #,##0.00", symbols);

        inisialisasiUI();
        perbaruiMetrik();
    }

    public void setIdentitasGudang(String namaGudang, String wilayah) {
        if (namaGudang == null || namaGudang.trim().isEmpty()) {
            this.identitasGudang = "Semua Gudang";
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
                "Ringkasan Statistik & Komposisi Mutu Tembakau - " + identitasGudang,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(33, 37, 41)
            ),
            new EmptyBorder(10, 12, 12, 12)
        ));
        revalidate();
        repaint();
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(12, 12));
        perbaruiJudulBorder();

        // 1. Grid 4 Kartu Metrik Utama di Atas
        JPanel panelKartuAtas = new JPanel(new GridLayout(1, 4, 10, 0));
        panelKartuAtas.setOpaque(false);

        panelKartuAtas.add(buatKartuStatistik("Total Penyerapan Tembakau", lblValTotalKg, new Color(33, 150, 243)));
        panelKartuAtas.add(buatKartuStatistik("Total Dana Tersalurkan", lblValTotalRp, new Color(76, 175, 80)));
        panelKartuAtas.add(buatKartuStatistik("Rata-Rata Kadar Air", lblValAvgKadarAir, new Color(255, 152, 0)));
        panelKartuAtas.add(buatKartuStatistik("Efisiensi Produktivitas Lahan", lblValProduktivitas, new Color(156, 39, 176)));

        // 2. Panel Analitik Komposisi Mutu Tembakau
        JPanel panelKomposisi = new JPanel(new GridLayout(4, 3, 10, 8));
        panelKomposisi.setOpaque(false);
        panelKomposisi.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                "Komposisi Distribusi Grade Mutu Pasokan",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(73, 80, 87)
            ),
            new EmptyBorder(12, 14, 12, 14)
        ));

        Font fontGrade = new Font("SansSerif", Font.BOLD, 12);
        Font fontHitung = new Font("SansSerif", Font.PLAIN, 12);

        barGradeA.setForeground(new Color(46, 125, 50));
        barGradeA.setStringPainted(true);
        barGradeB.setForeground(new Color(2, 136, 209));
        barGradeB.setStringPainted(true);
        barGradeC.setForeground(new Color(245, 124, 0));
        barGradeC.setStringPainted(true);
        barRejected.setForeground(new Color(211, 47, 47));
        barRejected.setStringPainted(true);

        // Baris Grade A
        panelKomposisi.add(buatLabelGrade("Grade A (Super - Kadar Air <= 12.0%)", fontGrade, new Color(46, 125, 50)));
        panelKomposisi.add(barGradeA);
        panelKomposisi.add(lblCountGradeA);

        // Baris Grade B
        panelKomposisi.add(buatLabelGrade("Grade B (Medium - Kadar Air <= 15.0%)", fontGrade, new Color(2, 136, 209)));
        panelKomposisi.add(barGradeB);
        panelKomposisi.add(lblCountGradeB);

        // Baris Grade C
        panelKomposisi.add(buatLabelGrade("Grade C (Rendah - Kadar Air <= 18.0%)", fontGrade, new Color(245, 124, 0)));
        panelKomposisi.add(barGradeC);
        panelKomposisi.add(lblCountGradeC);

        // Baris Rejected
        panelKomposisi.add(buatLabelGrade("Ditolak / Rusak (Kadar Air > 18.0%)", fontGrade, new Color(211, 47, 47)));
        panelKomposisi.add(barRejected);
        panelKomposisi.add(lblCountRejected);

        add(panelKartuAtas, BorderLayout.NORTH);
        add(panelKomposisi, BorderLayout.CENTER);
    }

    private JPanel buatKartuStatistik(String judul, JLabel lblNilai, Color warnaAksen) {
        JPanel kartu = new JPanel(new BorderLayout(0, 6));
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(new CompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblJudul.setForeground(new Color(108, 117, 125));

        lblNilai.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNilai.setForeground(warnaAksen);

        kartu.add(lblJudul, BorderLayout.NORTH);
        kartu.add(lblNilai, BorderLayout.CENTER);
        return kartu;
    }

    private JLabel buatLabelGrade(String teks, Font font, Color warna) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(font);
        lbl.setForeground(warna);
        return lbl;
    }

    public void perbaruiData(List<Petani> petaniList, List<TransaksiPasok> trxList) {
        this.daftarPetani = (petaniList != null) ? petaniList : new ArrayList<>();
        this.riwayatSemuaTransaksi = (trxList != null) ? trxList : new ArrayList<>();
        perbaruiMetrik();
    }

    public void perbaruiMetrik() {
        double totalKg = 0.0;
        double totalRp = 0.0;
        double sumKadarAir = 0.0;
        int countValidTrx = 0;

        int cA = 0, cB = 0, cC = 0, cRej = 0;

        for (TransaksiPasok t : riwayatSemuaTransaksi) {
            if (t.getTembakau() != null) {
                double berat = t.getTembakau().getBeratKg();
                double air = t.getTembakau().getKadarAirPersen();
                GradeMutu grade = t.getTembakau().getGrade();

                totalKg += berat;
                totalRp += t.getTotalPembayaran();
                sumKadarAir += air;
                countValidTrx++;

                if (grade == GradeMutu.A_SUPER) cA++;
                else if (grade == GradeMutu.B_MEDIUM) cB++;
                else if (grade == GradeMutu.C_RENDAH) cC++;
                else cRej++;
            }
        }

        double totalLahanHa = 0.0;
        for (Petani p : daftarPetani) {
            totalLahanHa += p.getLuasLahanHa();
        }

        double avgAir = (countValidTrx > 0) ? sumKadarAir / countValidTrx : 0.0;
        double produktivitas = (totalLahanHa > 0) ? totalKg / totalLahanHa : 0.0;

        lblValTotalKg.setText(formatAngka.format(totalKg) + " kg");
        lblValTotalRp.setText(formatRupiah.format(totalRp));
        lblValAvgKadarAir.setText(String.format(Locale.ROOT, "%.2f %%", avgAir));
        lblValProduktivitas.setText(formatAngka.format(produktivitas) + " kg/Ha");

        int totalBatch = Math.max(1, countValidTrx);
        int pA = (int) Math.round((cA * 100.0) / totalBatch);
        int pB = (int) Math.round((cB * 100.0) / totalBatch);
        int pC = (int) Math.round((cC * 100.0) / totalBatch);
        int pRej = (int) Math.round((cRej * 100.0) / totalBatch);

        barGradeA.setValue(pA);
        barGradeA.setString(pA + "%");
        lblCountGradeA.setText(cA + " batch (" + pA + "%)");

        barGradeB.setValue(pB);
        barGradeB.setString(pB + "%");
        lblCountGradeB.setText(cB + " batch (" + pB + "%)");

        barGradeC.setValue(pC);
        barGradeC.setString(pC + "%");
        lblCountGradeC.setText(cC + " batch (" + pC + "%)");

        barRejected.setValue(pRej);
        barRejected.setString(pRej + "%");
        lblCountRejected.setText(cRej + " batch (" + pRej + "%)");
    }
}
