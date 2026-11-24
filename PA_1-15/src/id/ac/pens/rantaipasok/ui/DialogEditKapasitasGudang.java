package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.GudangTembakau;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Dialog untuk mengubah (mengedit) kapasitas penampungan maksimum gudang tembakau.
 * Memvalidasi Invarian I2: Kapasitas baru tidak boleh lebih kecil dari stok fisik yang sedang tersimpan.
 */
public class DialogEditKapasitasGudang extends JDialog {

    private final GudangTembakau gudang;
    private final String daerah;
    private final String zonasiWilayah;
    private final Consumer<Double> onKapasitasDiperbarui;

    private final JTextField txtKapasitasBaru = new JTextField(12);
    private final DecimalFormat formatAngka;

    public DialogEditKapasitasGudang(Window owner,
                                     GudangTembakau gudang,
                                     String daerah,
                                     String zonasiWilayah,
                                     Consumer<Double> onKapasitasDiperbarui) {
        super(owner, "Ubah Kapasitas Maksimum Gudang", ModalityType.APPLICATION_MODAL);
        this.gudang = gudang;
        this.daerah = (daerah != null) ? daerah : "-";
        this.zonasiWilayah = (zonasiWilayah != null) ? zonasiWilayah : "-";
        this.onKapasitasDiperbarui = onKapasitasDiperbarui;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);

        inisialisasiUI();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelKonten = new JPanel(new GridBagLayout());
        panelKonten.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Identitas Entitas Gudang & Konfigurasi Daya Tampung",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(33, 37, 41)
            ),
            new EmptyBorder(12, 14, 12, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font fontLabel = new Font("SansSerif", Font.PLAIN, 12);
        Font fontValue = new Font("SansSerif", Font.BOLD, 12);

        // Baris 0: Daerah / Wilayah Zonasi
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Daerah / Zonasi:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        JLabel lblDaerahVal = new JLabel(daerah + " / " + zonasiWilayah);
        lblDaerahVal.setFont(fontValue);
        panelKonten.add(lblDaerahVal, gbc);

        // Baris 1: Kode Gudang
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Kode Gudang:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        JLabel lblKodeVal = new JLabel(gudang != null ? gudang.getKodeGudang() : "-");
        lblKodeVal.setFont(fontValue);
        panelKonten.add(lblKodeVal, gbc);

        // Baris 2: Nama Gudang
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Nama Gudang:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        JLabel lblNamaVal = new JLabel(gudang != null ? gudang.getNamaGudang() : "-");
        lblNamaVal.setFont(fontValue);
        panelKonten.add(lblNamaVal, gbc);

        // Baris 3: Stok Fisik Tersimpan Saat Ini
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Stok Fisik Tersimpan:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        double stokSaatIni = gudang != null ? gudang.getStokTersimpanKg() : 0.0;
        JLabel lblStokVal = new JLabel(formatAngka.format(stokSaatIni) + " kg");
        lblStokVal.setFont(fontValue);
        lblStokVal.setForeground(new Color(25, 118, 210));
        panelKonten.add(lblStokVal, gbc);

        // Baris 4: Kapasitas Saat Ini
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Kapasitas Saat Ini:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        double kapSaatIni = gudang != null ? gudang.getKapasitasMaksKg() : 0.0;
        JLabel lblKapVal = new JLabel(formatAngka.format(kapSaatIni) + " kg");
        lblKapVal.setFont(fontValue);
        panelKonten.add(lblKapVal, gbc);

        // Baris 5: Kapasitas Baru (Input)
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Kapasitas Baru (kg):", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        txtKapasitasBaru.setText(String.valueOf(kapSaatIni));
        panelKonten.add(txtKapasitasBaru, gbc);

        // Panel Tombol
        JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnBatal = new JButton("Batal");
        btnBatal.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnBatal.addActionListener(e -> dispose());

        JButton btnSimpan = new JButton("Simpan Perubahan");
        btnSimpan.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSimpan.setBackground(new Color(25, 118, 210));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFocusPainted(false);
        btnSimpan.addActionListener(e -> prosesSimpanKapasitas());

        panelAksi.add(btnBatal);
        panelAksi.add(btnSimpan);

        add(panelKonten, BorderLayout.CENTER);
        add(panelAksi, BorderLayout.SOUTH);
    }

    private JLabel buatLabel(String teks, Font font) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(font);
        lbl.setForeground(new Color(73, 80, 87));
        return lbl;
    }

    private void prosesSimpanKapasitas() {
        if (gudang == null) {
            JOptionPane.showMessageDialog(this, "Objek gudang tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String inputStr = txtKapasitasBaru.getText().trim();
        double kapasitasBaru;
        try {
            kapasitasBaru = Double.parseDouble(inputStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Format nilai kapasitas tidak valid: '" + inputStr + "'. Masukkan angka positif.",
                "Kesalahan Format Angka", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (Double.isNaN(kapasitasBaru) || Double.isInfinite(kapasitasBaru) || kapasitasBaru <= 0.0) {
            JOptionPane.showMessageDialog(this,
                "Kapasitas baru harus berupa bilangan terhingga > 0 kg.",
                "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validasi Invarian I2: Kapasitas baru tidak boleh lebih kecil dari muatan stok fisik yang sudah tersimpan
        double stokTersimpan = gudang.getStokTersimpanKg();
        if (kapasitasBaru < stokTersimpan) {
            JOptionPane.showMessageDialog(this,
                "Penolakan Validasi Invarian Fisik (Invarian I2 Protection):\n\n"
                + "Kapasitas baru (" + formatAngka.format(kapasitasBaru) + " kg) tidak boleh lebih kecil dari stok fisik tersimpan ("
                + formatAngka.format(stokTersimpan) + " kg)!\n\n"
                + "Penyusutan kapasitas di bawah muatan aktif dilarang untuk mencegah pelanggaran integritas invarian gudang.",
                "Pelanggaran Invarian I2", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Field fKapasitas = GudangTembakau.class.getDeclaredField("kapasitasMaksKg");
            fKapasitas.setAccessible(true);
            fKapasitas.setDouble(gudang, kapasitasBaru);

            if (onKapasitasDiperbarui != null) {
                onKapasitasDiperbarui.accept(kapasitasBaru);
            }

            JOptionPane.showMessageDialog(this,
                "Kapasitas gudang '" + gudang.getNamaGudang() + "' [" + gudang.getKodeGudang() + "]\n"
                + "berhasil diperbarui menjadi " + formatAngka.format(kapasitasBaru) + " kg.",
                "Kapasitas Berhasil Diubah", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Gagal memperbarui kapasitas via refleksi: " + ex.getMessage(),
                "Kesalahan Sistem", JOptionPane.ERROR_MESSAGE);
        }
    }
}
