package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.TransaksiPasok;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model tabel untuk menampilkan daftar TransaksiPasok pada antarmuka Java Swing.
 * Mampu menerima List&lt;TransaksiPasok&gt; baik daftar transaksi baru maupun daftar
 * transaksi yang tersimpan di gudang (gudang.getDaftarTransaksi()).
 */
public class TransaksiTableModel extends AbstractTableModel {

    private static final String[] NAMA_KOLOM = {
        "ID Transaksi",
        "Nama Petani",
        "Kode Batch",
        "Varietas",
        "Berat (kg)",
        "Kadar Air (%)",
        "Grade Mutu",
        "Total Pembayaran (Rp)",
        "Status Transaksi"
    };

    private List<TransaksiPasok> daftarTransaksi;

    public TransaksiTableModel() {
        this(new ArrayList<>());
    }

    public TransaksiTableModel(List<TransaksiPasok> daftarTransaksi) {
        this.daftarTransaksi = (daftarTransaksi != null) ? daftarTransaksi : new ArrayList<>();
    }

    public void setDaftarTransaksi(List<TransaksiPasok> daftarTransaksi) {
        this.daftarTransaksi = (daftarTransaksi != null) ? daftarTransaksi : new ArrayList<>();
        fireTableDataChanged();
    }

    public List<TransaksiPasok> getDaftarTransaksi() {
        return Collections.unmodifiableList(daftarTransaksi);
    }

    public TransaksiPasok getTransaksiAt(int rowIndex) {
        if (daftarTransaksi != null && rowIndex >= 0 && rowIndex < daftarTransaksi.size()) {
            return daftarTransaksi.get(rowIndex);
        }
        return null;
    }

    /**
     * Memperbarui tampilan tabel saat terjadi perubahan status atau penambahan transaksi baru.
     */
    public void refreshData() {
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return (daftarTransaksi != null) ? daftarTransaksi.size() : 0;
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
            case 3:
                return String.class;
            case 4:
            case 5:
                return Double.class;
            case 6:
                return String.class;
            case 7:
                return Double.class;
            case 8:
                return String.class;
            default:
                return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (daftarTransaksi == null || rowIndex < 0 || rowIndex >= daftarTransaksi.size()) {
            return null;
        }

        TransaksiPasok trx = daftarTransaksi.get(rowIndex);
        if (trx == null) {
            return null;
        }

        switch (columnIndex) {
            case 0:
                return trx.getIdTransaksi();
            case 1:
                return (trx.getPetani() != null) ? trx.getPetani().getNama() : "-";
            case 2:
                return (trx.getTembakau() != null) ? trx.getTembakau().getKodeBatch() : "-";
            case 3:
                return (trx.getTembakau() != null) ? trx.getTembakau().getVarietas() : "-";
            case 4:
                return (trx.getTembakau() != null) ? trx.getTembakau().getBeratKg() : 0.0;
            case 5:
                return (trx.getTembakau() != null) ? trx.getTembakau().getKadarAirPersen() : 0.0;
            case 6:
                return (trx.getTembakau() != null && trx.getTembakau().getGrade() != null)
                        ? trx.getTembakau().getGrade().name() : "-";
            case 7:
                return trx.getTotalPembayaran();
            case 8:
                return (trx.getStatus() != null) ? trx.getStatus().getLabel() : "-";
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
