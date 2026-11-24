package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.GudangTembakau;
import id.ac.pens.rantaipasok.PenerimaPasokan;
import id.ac.pens.rantaipasok.Petani;
import id.ac.pens.rantaipasok.StatusTransaksi;
import id.ac.pens.rantaipasok.TransaksiPasok;

import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Model tabel untuk menampilkan daftar Petani pada lapisan UI Swing.
 * Terisolasi strictly per wilayah gudang aktif yang sedang dipilih.
 */
public class PetaniTableModel extends AbstractTableModel {

    private static final String[] NAMA_KOLOM = {
        "NIK",
        "Nama Petani",
        "Asal Desa",
        "Luas Lahan (Ha)",
        "Saldo Setoran di Gudang Ini (kg)",
        "Status di Gudang Ini"
    };

    private List<Petani> daftarPetani;
    private PenerimaPasokan gudangAktif;
    private final DecimalFormat formatAngka;

    public PetaniTableModel() {
        this(new ArrayList<>());
    }

    public PetaniTableModel(List<Petani> daftarPetani) {
        this.daftarPetani = (daftarPetani != null) ? daftarPetani : new ArrayList<>();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        this.formatAngka = new DecimalFormat("#,##0.00", symbols);
    }

    public void setGudangAktif(PenerimaPasokan gudangAktif) {
        this.gudangAktif = gudangAktif;
        fireTableDataChanged();
    }

    public PenerimaPasokan getGudangAktif() {
        return gudangAktif;
    }

    public void setDaftarPetani(List<Petani> daftarPetani) {
        this.daftarPetani = (daftarPetani != null) ? daftarPetani : new ArrayList<>();
        fireTableDataChanged();
    }

    public List<Petani> getDaftarPetani() {
        return Collections.unmodifiableList(daftarPetani);
    }

    public Petani getPetaniAt(int rowIndex) {
        if (daftarPetani != null && rowIndex >= 0 && rowIndex < daftarPetani.size()) {
            return daftarPetani.get(rowIndex);
        }
        return null;
    }

    /**
     * Memperbarui tampilan tabel saat ada mutasi data.
     */
    public void refreshData() {
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return (daftarPetani != null) ? daftarPetani.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return NAMA_KOLOM.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < NAMA_KOLOM.length) {
            return NAMA_KOLOM[columnIndex];
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
            case 2:
                return String.class;
            case 3:
            case 4:
                return Double.class;
            case 5:
                return String.class;
            default:
                return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (daftarPetani == null || rowIndex < 0 || rowIndex >= daftarPetani.size()) {
            return null;
        }

        Petani p = daftarPetani.get(rowIndex);
        if (p == null) {
            return null;
        }

        double setoranGudang = hitungSetoranDiGudangAktif(p);

        switch (columnIndex) {
            case 0:
                return p.getNik();
            case 1:
                return p.getNama();
            case 2:
                return p.getAsalDesa();
            case 3:
                return p.getLuasLahanHa();
            case 4:
                return setoranGudang;
            case 5:
                return setoranGudang > 0.0
                    ? "Tersimpan (" + formatAngka.format(setoranGudang) + " kg)"
                    : "Belum Ada Pasokan";
            default:
                return null;
        }
    }

    private double hitungSetoranDiGudangAktif(Petani p) {
        if (gudangAktif == null || p == null) {
            return 0.0;
        }
        if (gudangAktif instanceof GudangTembakau) {
            GudangTembakau g = (GudangTembakau) gudangAktif;
            List<TransaksiPasok> list = g.getDaftarTransaksi();
            if (list == null || list.isEmpty()) {
                return 0.0;
            }
            double total = 0.0;
            for (TransaksiPasok t : list) {
                if (t.getPetani() != null && t.getPetani().getNik().equals(p.getNik())) {
                    if (t.getStatus() == StatusTransaksi.DISIMPAN_DI_GUDANG && t.getTembakau() != null) {
                        total += t.getTembakau().getBeratKg();
                    }
                }
            }
            return total;
        }
        return 0.0;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
