package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.Petani;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel formulir pendaftaran Petani mitra baru.
 * Menerapkan validasi fail-fast konstruktor Petani domain layer dengan
 * penanganan NumberFormatException dan IllegalArgumentException.
 */
public class PanelRegistrasiPetani extends JPanel {

    private final JTextField txtNik = new JTextField(16);
    private final JTextField txtNama = new JTextField(20);
    private final JTextField txtAsalDesa = new JTextField(20);
    private final JTextField txtLuasLahan = new JTextField(10);
    private final JButton btnTambahPetani = new JButton("Tambah Petani");

    private PetaniTableModel petaniTableModel;
    private final List<Consumer<Petani>> onPetaniAddedListeners = new ArrayList<>();

    public PanelRegistrasiPetani() {
        this(null);
    }

    public PanelRegistrasiPetani(PetaniTableModel petaniTableModel) {
        this.petaniTableModel = petaniTableModel;
        inisialisasiUI();
    }

    private void inisialisasiUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Registrasi Petani Mitra Baru",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(33, 37, 41)
            ),
            new EmptyBorder(10, 14, 12, 14)
        ));

        // Panel Grid Form Input
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fontLabel = new Font("SansSerif", Font.PLAIN, 12);

        // Baris 1: NIK
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panelForm.add(buatLabel("NIK Petani:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        panelForm.add(txtNik, gbc);

        // Baris 2: Nama
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Nama Lengkap:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        panelForm.add(txtNama, gbc);

        // Baris 3: Asal Desa
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Asal Desa / Kecamatan:", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panelForm.add(txtAsalDesa, gbc);

        // Baris 4: Luas Lahan (Ha)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panelForm.add(buatLabel("Luas Lahan (Ha):", fontLabel), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        panelForm.add(txtLuasLahan, gbc);

        // Baris 5: Tombol Aksi
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        btnTambahPetani.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnTambahPetani.setBackground(new Color(25, 118, 210));
        btnTambahPetani.setForeground(Color.WHITE);
        btnTambahPetani.setFocusPainted(false);
        panelForm.add(btnTambahPetani, gbc);

        btnTambahPetani.addActionListener(e -> prosesTambahPetani());

        add(panelForm, BorderLayout.CENTER);
    }

    private JLabel buatLabel(String teks, Font font) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(font);
        lbl.setForeground(new Color(73, 80, 87));
        return lbl;
    }

    /**
     * Memproses penambahan petani dengan parsing dan validasi fail-fast.
     */
    private void prosesTambahPetani() {
        String nik = txtNik.getText();
        String nama = txtNama.getText();
        String desa = txtAsalDesa.getText();
        String luasStr = txtLuasLahan.getText();

        try {
            if (nik == null || nik.isBlank()) {
                throw new IllegalArgumentException("NIK wajib diisi");
            }
            nik = nik.trim();
            if (nik.length() != 16 || !nik.matches("\\d{16}")) {
                throw new IllegalArgumentException("NIK harus tepat 16 digit angka (standar e-KTP): " + nik);
            }
            if (petaniTableModel != null) {
                for (Petani existing : petaniTableModel.getDaftarPetani()) {
                    if (existing.getNik().equals(nik)) {
                        throw new IllegalArgumentException("Petani dengan NIK '" + nik + "' sudah terdaftar atas nama: " + existing.getNama());
                    }
                }
            }

            if (luasStr == null || luasStr.isBlank()) {
                throw new IllegalArgumentException("Luas lahan (Ha) wajib diisi");
            }

            // Parsing angka
            double luasLahan = Double.parseDouble(luasStr.trim());

            // Instansiasi objek domain Petani (memicu validasi fail-fast pada konstruktor)
            Petani petaniBaru = new Petani(nik, nama, desa, luasLahan);

            // Tambahkan ke model tabel jika terpasang
            if (petaniTableModel != null) {
                List<Petani> currentList = new ArrayList<>(petaniTableModel.getDaftarPetani());
                currentList.add(petaniBaru);
                petaniTableModel.setDaftarPetani(currentList);
            }

            // Beritahukan listener (misal untuk memperbarui combo box di panel operasi pasokan)
            for (Consumer<Petani> listener : onPetaniAddedListeners) {
                listener.accept(petaniBaru);
            }

            // Bersihkan form input
            txtNik.setText("");
            txtNama.setText("");
            txtAsalDesa.setText("");
            txtLuasLahan.setText("");

            JOptionPane.showMessageDialog(
                this,
                "Petani berhasil didaftarkan:\n"
                    + "Nama: " + petaniBaru.getNama() + "\n"
                    + "NIK: " + petaniBaru.getNik() + "\n"
                    + "Desa: " + petaniBaru.getAsalDesa() + "\n"
                    + "Luas Lahan: " + petaniBaru.getLuasLahanHa() + " Ha",
                "Registrasi Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Format luas lahan tidak valid: '" + luasStr + "' bukan angka desimal yang valid.",
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
        }
    }

    public void addOnPetaniAddedListener(Consumer<Petani> listener) {
        if (listener != null) {
            onPetaniAddedListeners.add(listener);
        }
    }

    public void setPetaniTableModel(PetaniTableModel petaniTableModel) {
        this.petaniTableModel = petaniTableModel;
    }

    public JTextField getTxtNik() { return txtNik; }
    public JTextField getTxtNama() { return txtNama; }
    public JTextField getTxtAsalDesa() { return txtAsalDesa; }
    public JTextField getTxtLuasLahan() { return txtLuasLahan; }
    public JButton getBtnTambahPetani() { return btnTambahPetani; }
}
