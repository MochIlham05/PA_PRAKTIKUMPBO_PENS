package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.GudangTembakau;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Dialog Form Penambahan Gudang Penyangga Baru secara Manual.
 * Mendukung kategorisasi bertingkat: Daerah (Kabupaten) & Zonasi Wilayah operasional.
 */
public class DialogTambahGudang extends JDialog {

    private final JComboBox<String> cmbDaerah = new JComboBox<>();
    private final JTextField txtKode = new JTextField("GDG-UTR-01", 14);
    private final JTextField txtNama = new JTextField("Gudang Penyangga Utara", 18);
    private final JTextField txtKapasitas = new JTextField("1000.0", 10);
    private final JComboBox<String> cmbWilayah = new JComboBox<>(new String[]{
        "Wilayah Pusat",
        "Wilayah Barat",
        "Wilayah Timur",
        "Wilayah Utara",
        "Wilayah Selatan",
        "Wilayah Khusus / Eksternal"
    });

    private final Map<String, GudangTembakau> daftarGudang;
    private final Map<String, String> wilayahGudangMap;
    private final Map<String, String> gudangDaerahMap;
    private final Consumer<GudangTembakau> onGudangDitambahkan;

    public DialogTambahGudang(Window owner,
                              Map<String, GudangTembakau> daftarGudang,
                              Map<String, String> wilayahGudangMap,
                              Consumer<GudangTembakau> onGudangDitambahkan) {
        this(owner, List.of("Sumenep", "Pamekasan", "Sampang", "Bangkalan"), "Sumenep",
             daftarGudang, wilayahGudangMap, null, onGudangDitambahkan);
    }

    public DialogTambahGudang(Window owner,
                              List<String> daftarDaerah,
                              String daerahTerpilih,
                              Map<String, GudangTembakau> daftarGudang,
                              Map<String, String> wilayahGudangMap,
                              Map<String, String> gudangDaerahMap,
                              Consumer<GudangTembakau> onGudangDitambahkan) {
        super(owner, "Pendaftaran Entitas Gudang Baru", ModalityType.APPLICATION_MODAL);
        this.daftarGudang = daftarGudang;
        this.wilayahGudangMap = wilayahGudangMap;
        this.gudangDaerahMap = gudangDaerahMap;
        this.onGudangDitambahkan = onGudangDitambahkan;

        if (daftarDaerah != null) {
            for (String d : daftarDaerah) {
                cmbDaerah.addItem(d);
            }
        }
        if (daerahTerpilih != null) {
            cmbDaerah.setSelectedItem(daerahTerpilih);
        }

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
                "Spesifikasi Gudang, Daerah & Zonasi Wilayah",
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
        cmbWilayah.setEditable(true);

        // Baris 0: Daerah / Kabupaten
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Daerah / Kabupaten:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panelKonten.add(cmbDaerah, gbc);

        // Baris 1: Kode Gudang
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Kode Gudang:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        panelKonten.add(txtKode, gbc);

        // Baris 2: Nama Gudang
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Nama Gudang:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panelKonten.add(txtNama, gbc);

        // Baris 3: Kategori Zonasi Wilayah
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Zonasi Wilayah:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        panelKonten.add(cmbWilayah, gbc);

        // Baris 4: Kapasitas Maksimal (kg)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        panelKonten.add(buatLabel("Kapasitas Maks (kg):", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        panelKonten.add(txtKapasitas, gbc);

        // Panel Tombol
        JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnBatal = new JButton("Batal");
        btnBatal.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnBatal.addActionListener(e -> dispose());

        JButton btnSimpan = new JButton("Simpan Gudang Baru");
        btnSimpan.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSimpan.setBackground(new Color(25, 118, 210));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFocusPainted(false);
        btnSimpan.addActionListener(e -> prosesSimpanGudang());

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

    private void prosesSimpanGudang() {
        String kode = txtKode.getText().trim();
        String nama = txtNama.getText().trim();
        String kapasitasStr = txtKapasitas.getText().trim();
        Object wilayahObj = cmbWilayah.getSelectedItem();
        String wilayah = wilayahObj != null ? wilayahObj.toString().trim() : "Wilayah Umum";
        Object daerahObj = cmbDaerah.getSelectedItem();
        String daerah = daerahObj != null ? daerahObj.toString().trim() : "Sumenep";

        try {
            if (kode.isBlank()) throw new IllegalArgumentException("Kode gudang wajib diisi.");
            if (nama.isBlank()) throw new IllegalArgumentException("Nama gudang wajib diisi.");
            if (wilayah.isBlank()) wilayah = "Wilayah Umum";

            if (daftarGudang.containsKey(kode)) {
                throw new IllegalArgumentException("Gudang dengan kode '" + kode + "' sudah terdaftar di sistem!");
            }

            double kapasitas;
            try {
                kapasitas = Double.parseDouble(kapasitasStr);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Kapasitas gudang harus berupa bilangan valid: " + kapasitasStr);
            }

            // Instansiasi objek domain GudangTembakau (validasi fail-fast konstruktor domain)
            GudangTembakau gudangBaru = new GudangTembakau(kode, nama, kapasitas);

            daftarGudang.put(kode, gudangBaru);
            wilayahGudangMap.put(kode, wilayah);
            if (gudangDaerahMap != null) {
                gudangDaerahMap.put(kode, daerah);
            }

            if (onGudangDitambahkan != null) {
                onGudangDitambahkan.accept(gudangBaru);
            }

            JOptionPane.showMessageDialog(this,
                "Gudang baru berhasil didaftarkan dan diaktifkan:\n"
                + "• Daerah        : " + daerah + "\n"
                + "• Kode Gudang   : " + gudangBaru.getKodeGudang() + "\n"
                + "• Nama Gudang   : " + gudangBaru.getNamaGudang() + "\n"
                + "• Wilayah/Zona  : " + wilayah + "\n"
                + "• Kapasitas Maks: " + gudangBaru.getKapasitasMaksKg() + " kg",
                "Pendaftaran Gudang Sukses", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                "Validasi Gagal:\n" + ex.getMessage(),
                "Kesalahan Input", JOptionPane.ERROR_MESSAGE);
        }
    }
}
