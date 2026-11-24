package id.ac.pens.rantaipasok.ui;

import id.ac.pens.rantaipasok.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Layanan Persistensi Data (JSON Storage Service).
 * Bertanggung jawab menyimpan dan memuat state sistem (Petani, Transaksi, dan Gudang)
 * ke/dari berkas JSON lokal secara permanen tanpa dependensi pustaka pihak ketiga.
 *
 * Mengembalikan state secara murni melalui alur domain (terimaPasokan) sehingga seluruh
 * invarian I1 s.d. I5 tetap terverifikasi secara formal saat data dimuat ulang.
 */
public class DataStorageService {

    public static final String DEFAULT_FILE_PATH = "data/rantaipasok_db.json";

    public static class GudangRecord {
        public final String kode;
        public final String nama;
        public final double kapasitasMaksKg;
        public final String wilayah;
        public final String daerah;

        public GudangRecord(String kode, String nama, double kapasitasMaksKg, String wilayah) {
            this(kode, nama, kapasitasMaksKg, wilayah, "Sumenep");
        }

        public GudangRecord(String kode, String nama, double kapasitasMaksKg, String wilayah, String daerah) {
            this.kode = kode;
            this.nama = nama;
            this.kapasitasMaksKg = kapasitasMaksKg;
            this.wilayah = wilayah;
            this.daerah = (daerah != null && !daerah.isBlank()) ? daerah.trim() : "Sumenep";
        }
    }

    public static class StorageData {
        public final List<Petani> daftarPetani = new ArrayList<>();
        public final List<TransaksiPasok> riwayatSemuaTransaksi = new ArrayList<>();
        public final List<GudangRecord> daftarGudang = new ArrayList<>();
    }

    /**
     * Menyimpan seluruh petani dan transaksi ke dalam berkas JSON.
     */
    public static void simpanKeFile(String filePath,
                                    List<Petani> daftarPetani,
                                    List<TransaksiPasok> riwayatTransaksi) throws IOException {
        simpanKeFile(filePath, daftarPetani, riwayatTransaksi, null, null);
    }

    public static void simpanKeFile(String filePath,
                                    List<Petani> daftarPetani,
                                    List<TransaksiPasok> riwayatTransaksi,
                                    Map<String, id.ac.pens.rantaipasok.GudangTembakau> daftarGudang,
                                    Map<String, String> wilayahGudangMap) throws IOException {
        simpanKeFile(filePath, daftarPetani, riwayatTransaksi, daftarGudang, wilayahGudangMap, null);
    }

    public static void simpanKeFile(String filePath,
                                    List<Petani> daftarPetani,
                                    List<TransaksiPasok> riwayatTransaksi,
                                    Map<String, id.ac.pens.rantaipasok.GudangTembakau> daftarGudang,
                                    Map<String, String> wilayahGudangMap,
                                    Map<String, String> gudangDaerahMap) throws IOException {
        Path path = Paths.get(filePath != null ? filePath : DEFAULT_FILE_PATH);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // 1. Simpan Petani
        sb.append("  \"petani\": [\n");
        if (daftarPetani != null) {
            for (int i = 0; i < daftarPetani.size(); i++) {
                Petani p = daftarPetani.get(i);
                sb.append("    {\n");
                sb.append("      \"nik\": \"").append(escapeJson(p.getNik())).append("\",\n");
                sb.append("      \"nama\": \"").append(escapeJson(p.getNama())).append("\",\n");
                sb.append("      \"asalDesa\": \"").append(escapeJson(p.getAsalDesa())).append("\",\n");
                sb.append("      \"luasLahanHa\": ").append(p.getLuasLahanHa()).append("\n");
                sb.append("    }").append(i < daftarPetani.size() - 1 ? "," : "").append("\n");
            }
        }
        sb.append("  ],\n");

        // 2. Simpan Transaksi
        sb.append("  \"transaksi\": [\n");
        if (riwayatTransaksi != null) {
            for (int i = 0; i < riwayatTransaksi.size(); i++) {
                TransaksiPasok t = riwayatTransaksi.get(i);
                String kodeGdg = "";
                if (daftarGudang != null) {
                    for (id.ac.pens.rantaipasok.GudangTembakau g : daftarGudang.values()) {
                        if (g.getDaftarTransaksi().contains(t)) {
                            kodeGdg = g.getKodeGudang();
                            break;
                        }
                    }
                }
                sb.append("    {\n");
                sb.append("      \"idTransaksi\": \"").append(escapeJson(t.getIdTransaksi())).append("\",\n");
                sb.append("      \"nikPetani\": \"").append(escapeJson(t.getPetani().getNik())).append("\",\n");
                sb.append("      \"kodeBatch\": \"").append(escapeJson(t.getTembakau().getKodeBatch())).append("\",\n");
                sb.append("      \"varietas\": \"").append(escapeJson(t.getTembakau().getVarietas())).append("\",\n");
                sb.append("      \"beratKg\": ").append(t.getTembakau().getBeratKg()).append(",\n");
                sb.append("      \"kadarAirPersen\": ").append(t.getTembakau().getKadarAirPersen()).append(",\n");
                sb.append("      \"kodeGudang\": \"").append(escapeJson(kodeGdg)).append("\",\n");
                sb.append("      \"status\": \"").append(t.getStatus().name()).append("\"\n");
                sb.append("    }").append(i < riwayatTransaksi.size() - 1 ? "," : "").append("\n");
            }
        }
        sb.append("  ]");

        // 3. Simpan Entitas Gudang
        if (daftarGudang != null && !daftarGudang.isEmpty()) {
            sb.append(",\n  \"gudang\": [\n");
            int idx = 0;
            int total = daftarGudang.size();
            for (Map.Entry<String, id.ac.pens.rantaipasok.GudangTembakau> entry : daftarGudang.entrySet()) {
                id.ac.pens.rantaipasok.GudangTembakau g = entry.getValue();
                String wilayah = (wilayahGudangMap != null) ? wilayahGudangMap.getOrDefault(g.getKodeGudang(), "Wilayah Umum") : "Wilayah Umum";
                String daerah = (gudangDaerahMap != null) ? gudangDaerahMap.getOrDefault(g.getKodeGudang(), "Sumenep") : "Sumenep";
                sb.append("    {\n");
                sb.append("      \"kode\": \"").append(escapeJson(g.getKodeGudang())).append("\",\n");
                sb.append("      \"nama\": \"").append(escapeJson(g.getNamaGudang())).append("\",\n");
                sb.append("      \"kapasitasMaksKg\": ").append(g.getKapasitasMaksKg()).append(",\n");
                sb.append("      \"wilayah\": \"").append(escapeJson(wilayah)).append("\",\n");
                sb.append("      \"daerah\": \"").append(escapeJson(daerah)).append("\"\n");
                sb.append("    }").append(idx < total - 1 ? "," : "").append("\n");
                idx++;
            }
            sb.append("  ]\n");
        } else {
            sb.append("\n");
        }
        sb.append("}\n");

        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Memuat kembali seluruh data dari berkas JSON ke memori dan merekonstruksi
     * objek domain beserta integritas status gudang dan saldo petani.
     */
    public static StorageData muatDariFile(String filePath, PenerimaPasokan gudang)
            throws IOException, DomainRantaiPasokException {
        return muatDariFile(filePath, null, gudang);
    }

    public static StorageData muatDariFile(String filePath,
                                           Map<String, id.ac.pens.rantaipasok.GudangTembakau> daftarGudangMap,
                                           PenerimaPasokan gudangDefault)
            throws IOException, DomainRantaiPasokException {
        Path path = Paths.get(filePath != null ? filePath : DEFAULT_FILE_PATH);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Berkas database lokal tidak ditemukan: " + path.toAbsolutePath());
        }

        String json = Files.readString(path, StandardCharsets.UTF_8);
        StorageData data = new StorageData();
        Map<String, Petani> petaniMap = new LinkedHashMap<>();

        // 1. Parsing Blok Petani
        Matcher mPetaniBlok = Pattern.compile("\"petani\"\\s*:\\s*\\[([\\s\\S]*?)\\]\\s*,").matcher(json);
        if (mPetaniBlok.find()) {
            String petaniArrStr = mPetaniBlok.group(1);
            Matcher mObj = Pattern.compile("\\{([^{}]+)\\}").matcher(petaniArrStr);
            while (mObj.find()) {
                String objStr = mObj.group(1);
                String nik = ekstrakField(objStr, "nik");
                String nama = ekstrakField(objStr, "nama");
                String asalDesa = ekstrakField(objStr, "asalDesa");
                String luasStr = ekstrakField(objStr, "luasLahanHa");

                if (nik != null && nama != null && asalDesa != null && luasStr != null) {
                    double luas = Double.parseDouble(luasStr);
                    Petani p = new Petani(nik, nama, asalDesa, luas);
                    data.daftarPetani.add(p);
                    petaniMap.put(nik, p);
                }
            }
        }

        // 2. Parsing Blok Gudang (jika ada)
        Matcher mGudangBlok = Pattern.compile("\"gudang\"\\s*:\\s*\\[([\\s\\S]*?)\\]").matcher(json);
        if (mGudangBlok.find()) {
            String arrStr = mGudangBlok.group(1);
            Matcher mObj = Pattern.compile("\\{([^{}]+)\\}").matcher(arrStr);
            while (mObj.find()) {
                String objStr = mObj.group(1);
                String kode = ekstrakField(objStr, "kode");
                String nama = ekstrakField(objStr, "nama");
                String kapStr = ekstrakField(objStr, "kapasitasMaksKg");
                String wilayah = ekstrakField(objStr, "wilayah");
                String daerah = ekstrakField(objStr, "daerah");
                if (kode != null && nama != null && kapStr != null) {
                    try {
                        double kap = Double.parseDouble(kapStr);
                        data.daftarGudang.add(new GudangRecord(
                            kode, nama, kap,
                            wilayah != null ? wilayah : "Wilayah Umum",
                            daerah != null ? daerah : "Sumenep"
                        ));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // 3. Parsing Blok Transaksi
        Matcher mTrxBlok = Pattern.compile("\"transaksi\"\\s*:\\s*\\[([\\s\\S]*?)\\]").matcher(json);
        if (mTrxBlok.find()) {
            String trxArrStr = mTrxBlok.group(1);
            Matcher mObj = Pattern.compile("\\{([^{}]+)\\}").matcher(trxArrStr);
            while (mObj.find()) {
                String objStr = mObj.group(1);
                String idTransaksi = ekstrakField(objStr, "idTransaksi");
                String nikPetani = ekstrakField(objStr, "nikPetani");
                String kodeBatch = ekstrakField(objStr, "kodeBatch");
                String varietas = ekstrakField(objStr, "varietas");
                String beratStr = ekstrakField(objStr, "beratKg");
                String kadarAirStr = ekstrakField(objStr, "kadarAirPersen");
                String kodeGudang = ekstrakField(objStr, "kodeGudang");
                String statusStr = ekstrakField(objStr, "status");

                if (idTransaksi != null && nikPetani != null && kodeBatch != null
                        && varietas != null && beratStr != null && kadarAirStr != null) {
                    Petani p = petaniMap.get(nikPetani);
                    if (p != null) {
                        double berat = Double.parseDouble(beratStr);
                        double kadarAir = Double.parseDouble(kadarAirStr);
                        Tembakau tembakau = new Tembakau(kodeBatch, varietas, berat, kadarAir);
                        TransaksiPasok trx = new TransaksiPasok(idTransaksi, p, tembakau);

                        if ("TERVERIFIKASI".equals(statusStr)) {
                            trx.verifikasiTransaksi();
                        } else if ("DISIMPAN_DI_GUDANG".equals(statusStr)) {
                            trx.verifikasiTransaksi();
                            PenerimaPasokan target = null;
                            if (daftarGudangMap != null && kodeGudang != null && daftarGudangMap.containsKey(kodeGudang)) {
                                target = daftarGudangMap.get(kodeGudang);
                            } else if (gudangDefault != null) {
                                target = gudangDefault;
                            }
                            if (target != null) {
                                target.terimaPasokan(trx);
                            }
                        }
                        data.riwayatSemuaTransaksi.add(trx);
                    }
                }
            }
        }

        return data;
    }

    private static String ekstrakField(String jsonObjectStr, String fieldName) {
        Pattern p = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(?:\"([^\"]*)\"|([0-9.]+))");
        Matcher m = p.matcher(jsonObjectStr);
        if (m.find()) {
            return m.group(1) != null ? m.group(1) : m.group(2);
        }
        return null;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
