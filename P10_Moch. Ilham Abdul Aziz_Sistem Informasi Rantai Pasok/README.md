# Sistem Informasi Manajemen Rantai Pasok Tembakau Desa Prancak, Sumenep
**Ontology-Driven Architecture & Object-Oriented Analysis and Design (OOAD)**

Backend berbasis **Java 17**, **Spring Boot 3**, dan **Spring Data JPA** yang mengimplementasikan model domain **OntoUML** untuk rantai pasok agrikultur tembakau rajangan Desa Prancak, Pasongsongan, Kabupaten Sumenep.

---

## 🏛️ Pemetaan Konseptual OntoUML

| Konsep Domain | Stereotipe OntoUML | Karakteristik & Relasi JPA | Tanggung Jawab Domain / Aturan Bisnis |
| :--- | :--- | :--- | :--- |
| **`Petani`** | `«Kind»` | Entity rigid universal, pembawa identitas independen. `@OneToMany` ke `Lahan` dan `Panen`. | Enkapsulasi list aset (`unmodifiableList`), perhitungan total luas lahan garapan dan total panen basah. |
| **`Lahan`** | `«Subkind» / «Role»` | Bidang tanah pertanian. `@ManyToOne` ke `Petani` (`optional = false`). | Estimasi kapasitas produksi daun basah (~7.500 kg/Ha). Lahan tidak boleh eksis tanpa pemilik. |
| **`Panen`** | `«Relator»` | Peristiwa panen mengikat `Petani` dan `Lahan`. Aggregate Root bagi `HasilGrading`. | Factory method `lakukanGrading(...)`, validasi kesiapan panen (`isSiapDitransaksikan`), persentase rendemen. |
| **`HasilGrading`** | `«Quality» / «Mode»` | Kualitas intrinsik (*inherent*) hasil rajangan. **Composition** dengan `Panen` (`orphanRemoval = true`). | Penetapan harga patokan mutu, validasi *physical invariant* (`beratKering <= beratBasah`). |
| **`Transaksi`** | `«Relator»` | Relator kontraktual jual-beli antara `Panen` dan `Pengepul`. | Memvalidasi bahwa panen wajib telah di-grading, kalkulasi otomatis nilai ekonomi (`beratKering * hargaPerKg`). |
| **`Pengepul`** | `«Kind» / «Role»` | Agen pembeli/pedagang pengumpul tembakau. `@OneToMany` ke `Transaksi`. | Counterparty kontraktual dalam transaksi perdagangan. |

---

## 🚀 Menjalankan Aplikasi

Pastikan Java 17+ telah terpasang. Jalankan perintah berikut di root direktori proyek:

```bash
mvn spring-boot:run
```

Saat aplikasi pertama kali dijalankan, class `DataSeeder` akan otomatis mengeksekusi 5 skenario awal:
1. Pendaftaran **Bapak Fulan** (Petani Desa Prancak).
2. Pendaftaran **2 Lahan** milik Bapak Fulan.
3. Pencatatan **Panen** daun basah 500 kg di lahan pertama.
4. Pemrosesan **Grading Mutu** (Grade A, 70 kg, Rp 75.000/kg).
5. Checkout **Transaksi** dan pelunasan pembayaran (**Status: LUNAS**, Total: Rp 5.250.000).

---

## 📡 REST API Endpoints (Integrasi Frontend / SmartLeaf App)

| Method | Endpoint | Deskripsi | Status HTTP |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/panen` | Mencatat data panen baru | `201 CREATED` |
| `POST` | `/api/panen/{id}/grading` | Input hasil grading mutu tembakau | `200 OK` |
| `GET` | `/api/panen/{id}` | Detail batch panen | `200 OK` |
| `GET` | `/api/panen/siap-transaksi` | Daftar panen yang sudah selesai grading | `200 OK` |
| `POST` | `/api/transaksi/checkout/{panenId}` | Checkout transaksi jual beli tembakau | `201 CREATED` |
| `GET` | `/api/transaksi/{id}` | Struk digital rincian transaksi | `200 OK` |
| `POST` | `/api/transaksi/{id}/lunas` | Konfirmasi pelunasan pembayaran | `200 OK` |

---

## 🗄️ Database Console (H2 In-Memory)
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:tembakaudb`
- **User**: `sa`
- **Password**: *(kosongkan)*
