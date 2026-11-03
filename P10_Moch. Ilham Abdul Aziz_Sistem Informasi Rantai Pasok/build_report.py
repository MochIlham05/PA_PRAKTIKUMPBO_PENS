import os
import shutil
import docx
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

DOC_NAME = "Moch. Ilham Abdul Aziz_PBO_3125522014_Pertemuan 9.docx"
BACKUP_NAME = "Moch. Ilham Abdul Aziz_PBO_3125522014_Pertemuan 9_backup.docx"

if not os.path.exists(BACKUP_NAME):
    shutil.copyfile(DOC_NAME, BACKUP_NAME)
    print(f"[INFO] Backup dokumen asli dibuat di: {BACKUP_NAME}")
else:
    print(f"[INFO] Menggunakan backup yang sudah ada: {BACKUP_NAME}")

doc = docx.Document(BACKUP_NAME)

# Set base styles
normal_style = doc.styles['Normal']
normal_style.font.name = 'Times New Roman'
normal_style.font.size = Pt(12)
normal_style.font.color.rgb = RGBColor(0x22, 0x22, 0x22)
normal_style.paragraph_format.line_spacing = 1.15
normal_style.paragraph_format.space_after = Pt(4)

# Color Constants
C_NAVY = RGBColor(0x1B, 0x36, 0x5D)
C_SLATE = RGBColor(0x2C, 0x3E, 0x50)
HEX_HEADER_BG = "1B365D"
HEX_ZEBRA_BG = "F4F6F9"
HEX_CODE_BG = "F8F9FA"

def set_cell_background(cell, hex_color):
    tcPr = cell._tc.get_or_add_tcPr()
    for child in list(tcPr):
        if child.tag.endswith('shd'):
            tcPr.remove(child)
    tcPr.append(parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>'))

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    for child in list(tcPr):
        if child.tag.endswith('tcMar'):
            tcPr.remove(child)
    tcMar = parse_xml(
        f'<w:tcMar {nsdecls("w")}>'
        f'  <w:top w:w="{top}" w:type="dxa"/>'
        f'  <w:bottom w:w="{bottom}" w:type="dxa"/>'
        f'  <w:left w:w="{left}" w:type="dxa"/>'
        f'  <w:right w:w="{right}" w:type="dxa"/>'
        f'</w:tcMar>'
    )
    tcPr.append(tcMar)

def set_table_borders(table, color="D3D3D3", sz="4", val="single"):
    tblPr = table._tbl.tblPr
    borders = parse_xml(
        f'<w:tblBorders {nsdecls("w")}>'
        f'  <w:top w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'  <w:bottom w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'  <w:insideH w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'  <w:insideV w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'  <w:left w:val="none"/>'
        f'  <w:right w:val="none"/>'
        f'</w:tblBorders>'
    )
    tblPr.append(borders)

def add_heading_1(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(16)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(14)
    run.font.bold = True
    run.font.color.rgb = C_NAVY
    return p

def add_heading_2(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(13)
    run.font.bold = True
    run.font.color.rgb = C_SLATE
    return p

def add_heading_3(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(9)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(12)
    run.font.bold = True
    run.font.color.rgb = C_SLATE
    return p

def add_body_paragraph(doc, text, bold_prefix=None, italic=False):
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing = 1.15
    p.paragraph_format.space_after = Pt(5)
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    if bold_prefix:
        r_pre = p.add_run(bold_prefix)
        r_pre.font.name = 'Times New Roman'
        r_pre.font.size = Pt(12)
        r_pre.font.bold = True
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.italic = italic
    return p

def add_bullet_item(doc, text, bold_prefix=None):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.line_spacing = 1.15
    p.paragraph_format.space_after = Pt(3)
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    if bold_prefix:
        r_pre = p.add_run(bold_prefix)
        r_pre.font.name = 'Times New Roman'
        r_pre.font.size = Pt(12)
        r_pre.font.bold = True
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    return p

def add_code_block(doc, code_text):
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    
    cell = table.cell(0, 0)
    cell.width = Inches(6.5)
    set_cell_background(cell, HEX_CODE_BG)
    set_cell_margins(cell, top=140, bottom=140, left=180, right=180)
    
    tcPr = cell._tc.get_or_add_tcPr()
    borders = parse_xml(
        f'<w:tcBorders {nsdecls("w")}>'
        f'  <w:left w:val="single" w:sz="18" w:space="0" w:color="1B365D"/>'
        f'  <w:top w:val="single" w:sz="4" w:space="0" w:color="DCDCDC"/>'
        f'  <w:right w:val="single" w:sz="4" w:space="0" w:color="DCDCDC"/>'
        f'  <w:bottom w:val="single" w:sz="4" w:space="0" w:color="DCDCDC"/>'
        f'</w:tcBorders>'
    )
    tcPr.append(borders)
    
    p = cell.paragraphs[0]
    p.paragraph_format.line_spacing = 1.0
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(0)
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    
    run = p.add_run(code_text)
    run.font.name = 'Consolas'
    run.font.size = Pt(9.5)
    run.font.color.rgb = RGBColor(0x1F, 0x24, 0x28)
    
    sp = doc.add_paragraph()
    sp.paragraph_format.space_after = Pt(4)
    sp.paragraph_format.line_spacing = 1.0

def add_callout_box(doc, text_content):
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    
    cell = table.cell(0, 0)
    cell.width = Inches(6.5)
    set_cell_background(cell, "F2F6FA")
    set_cell_margins(cell, top=260, bottom=260, left=200, right=200)
    
    tcPr = cell._tc.get_or_add_tcPr()
    borders = parse_xml(
        f'<w:tcBorders {nsdecls("w")}>'
        f'  <w:left w:val="dashed" w:sz="14" w:space="0" w:color="1B365D"/>'
        f'  <w:top w:val="dashed" w:sz="14" w:space="0" w:color="1B365D"/>'
        f'  <w:right w:val="dashed" w:sz="14" w:space="0" w:color="1B365D"/>'
        f'  <w:bottom w:val="dashed" w:sz="14" w:space="0" w:color="1B365D"/>'
        f'</w:tcBorders>'
    )
    tcPr.append(borders)
    
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(text_content)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(11)
    run.font.bold = True
    run.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D)
    
    sp = doc.add_paragraph()
    sp.paragraph_format.space_after = Pt(4)

# Read code files
def read_file(path):
    with open(path, "r", encoding="utf-8") as f:
        return f.read().strip()

CODE_DIR = "src/main/java/com/rantaitasok/tembakau/modul9"
code_petani = read_file(os.path.join(CODE_DIR, "Petani.java"))
code_tembakau = read_file(os.path.join(CODE_DIR, "Tembakau.java"))
code_transaksi = read_file(os.path.join(CODE_DIR, "TransaksiPasok.java"))
code_gudang = read_file(os.path.join(CODE_DIR, "GudangTembakau.java"))
code_main = read_file(os.path.join(CODE_DIR, "Main.java"))

print("[INFO] Berhasil membaca seluruh berkas source code Java.")

# Add Page Break after Cover Page
doc.add_page_break()

# ==========================================
# HEADER & BAGIAN 1: IDENTITAS & DESKRIPSI SISTEM
# ==========================================
p_title = doc.add_paragraph()
p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
p_title.paragraph_format.space_after = Pt(2)
r_t1 = p_title.add_run("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK\n")
r_t1.font.name = 'Times New Roman'
r_t1.font.size = Pt(14)
r_t1.font.bold = True
r_t1.font.color.rgb = C_NAVY

r_t2 = p_title.add_run("MODUL 9: IMPLEMENTASI UML CLASS DIAGRAM KE PROGRAM JAVA\n")
r_t2.font.name = 'Times New Roman'
r_t2.font.size = Pt(13)
r_t2.font.bold = True
r_t2.font.color.rgb = C_SLATE

r_t3 = p_title.add_run("Proyek: Sistem Informasi Manajemen Rantai Pasok Tembakau")
r_t3.font.name = 'Times New Roman'
r_t3.font.size = Pt(12)
r_t3.font.italic = True

add_heading_1(doc, "BAGIAN 1: IDENTITAS & DESKRIPSI SISTEM")

add_heading_2(doc, "1.1 Lembar Identitas Praktikan")

t_id = doc.add_table(rows=7, cols=2)
t_id.alignment = WD_TABLE_ALIGNMENT.CENTER
t_id.autofit = False
set_table_borders(t_id, color="B0C4DE", sz="6")

id_data = [
    ("Institusi Perguruan Tinggi", "Politeknik Elektronika Negeri Surabaya (PENS) - PSDKU Sumenep"),
    ("Program Studi / Jurusan", "D3 Teknik Informatika / Departemen Teknik Informatika dan Komputer"),
    ("Mata Kuliah / Semester", "Praktikum Pemrograman Berorientasi Obyek (PBO) / Semester 3"),
    ("Pertemuan / Topik", "Modul 9 – Implementasi UML Class Diagram ke Program Java"),
    ("Dosen Pengampu", "Nirwana Haidar Hari, S.Pd., M.Kom."),
    ("Nama Mahasiswa / NRP", "Moch. Ilham Abdul Aziz / 3125522014"),
    ("Judul Proyek / Aplikasi", "Sistem Informasi Manajemen Rantai Pasok Tembakau")
]

for idx, (param, val) in enumerate(id_data):
    row = t_id.rows[idx]
    c0 = row.cells[0]
    c1 = row.cells[1]
    c0.width = Inches(2.3)
    c1.width = Inches(4.2)
    
    bg = HEX_ZEBRA_BG if idx % 2 == 1 else "FFFFFF"
    set_cell_background(c0, bg)
    set_cell_background(c1, bg)
    set_cell_margins(c0, top=80, bottom=80, left=120, right=120)
    set_cell_margins(c1, top=80, bottom=80, left=120, right=120)
    
    p0 = c0.paragraphs[0]
    p0.paragraph_format.line_spacing = 1.0
    r0 = p0.add_run(param)
    r0.font.name = 'Times New Roman'
    r0.font.size = Pt(11)
    r0.font.bold = True
    
    p1 = c1.paragraphs[0]
    p1.paragraph_format.line_spacing = 1.0
    r1 = p1.add_run(val)
    r1.font.name = 'Times New Roman'
    r1.font.size = Pt(11)

p_sp1 = doc.add_paragraph()
p_sp1.paragraph_format.space_after = Pt(6)

add_heading_2(doc, "1.2 Latar Belakang & Deskripsi Sistem")
add_body_paragraph(
    doc,
    "Kabupaten Sumenep dan wilayah Madura secara historis dan ekonomis merupakan salah satu sentra perkebunan serta "
    "produsen daun tembakau rajangan terbesar di Indonesia. Namun dalam tata kelola rantai pasok tradisional, aktivitas "
    "penerimaan komoditas dari petani sering kali mengalami kendala berupa ketidakpastian mutu fisik daun, subjektivitas dalam grading mutu, "
    "pencatatan setoran yang tercecer, hingga risiko kelebihan beban tampung (overcapacity) pada fasilitas gudang mitra.",
    bold_prefix="Latar Belakang Studi Kasus: "
)

add_body_paragraph(
    doc,
    "Untuk mengatasi permasalahan tersebut, dirancang Sistem Informasi Manajemen Rantai Pasok Tembakau berbasis "
    "paradigma Object-Oriented Programming (OOP). Sistem ini memodelkan seluruh siklus rantai pasok ke dalam entitas perangkat lunak "
    "yang modular, terenkapsulasi, dan saling berkolaborasi secara presisi, meliputi:",
    bold_prefix="Solusi Berbasis OOP: "
)

add_bullet_item(doc, "Menyimpan data identitas petani, legalitas, luas lahan garapan, serta mengelola akumulasi riwayat kuantitas tembakau yang telah disetorkan secara tervalidasi.", bold_prefix="1. Entitas Petani: ")
add_bullet_item(doc, "Menyimpan data batch panen (kode batch, varietas, kadar air, bobot), serta mengotomatiskan validasi mutu dan penentuan kelas grade secara objektif berdasarkan standar industri rokok.", bold_prefix="2. Entitas Tembakau: ")
add_bullet_item(doc, "Menghubungkan entitas Petani dan Tembakau dalam satu akad transaksi pasokan, mengalkulasi nilai pembayaran berbasis grade, dan memverifikasi kelayakan pasok sebelum disahkan.", bold_prefix="3. Entitas TransaksiPasok: ")
add_bullet_item(doc, "Mengelola daya tampung fisik gudang penyimpanan, mencegah kelebihan kapasitas masuk, dan mengagregasikan seluruh transaksi pasokan dalam laporan inventaris real-time.", bold_prefix="4. Entitas GudangTembakau: ")
add_bullet_item(doc, "Mengorkestrasikan seluruh skenario pengujian alur bisnis rantai pasok dari panen di kebun hingga pencatatan stok di gudang.", bold_prefix="5. Driver Main: ")

add_heading_2(doc, "1.3 Relevansi terhadap Paradigma Object-Oriented Programming")
add_body_paragraph(
    doc,
    "Sistem ini memenuhi seluruh kualifikasi praktikum Modul 9 dengan mengimplementasikan prinsip-prinsip fundamental rekayasa perangkat lunak berorientasi objek:"
)
add_bullet_item(doc, "Seluruh atribut instans berderajat visibilitas private. Akses pengubahan data dilarang menggunakan setter polos, melainkan harus melewati method bisnis yang memvalidasi kondisi state (state invariant protection).", bold_prefix="Enkapsulasi Ketat (Strict Encapsulation): ")
add_bullet_item(doc, "Setiap class memiliki tanggung jawab tunggal (Single Responsibility Principle) dan memuat behavior bisnis nyata yang relevan dengan kasus industri tembakau.", bold_prefix="Business Responsibility: ")
add_bullet_item(doc, "Hubungan struktural di mana class TransaksiPasok menyimpan referensi langsung ke objek Petani dan objek Tembakau.", bold_prefix="Relasi Asosiasi (Association): ")
add_bullet_item(doc, "Hubungan kepemilikan bertipe 'has-a' di mana GudangTembakau mengelola koleksi dinamis dari sekumpulan objek TransaksiPasok.", bold_prefix="Relasi Agregasi (Aggregation): ")

add_heading_2(doc, "1.4 Tujuan Praktikum")
add_bullet_item(doc, "Mampu membaca dan menganalisis rancangan UML Class Diagram sistem rantai pasok tembakau secara mendalam.")
add_bullet_item(doc, "Mampu menerjemahkan class, atribut, method, visibilitas (+, -, #), dan relasi UML ke dalam sintaks bahasa pemrograman Java yang valid.")
add_bullet_item(doc, "Mampu mengimplementasikan enkapsulasi ketat serta validasi status objek (state validation) tanpa mengandalkan setter terbuka.")
add_bullet_item(doc, "Mampu menerapkan relasi Association dan Aggregation antar-objek secara modular dan fleksibel.")
add_bullet_item(doc, "Menjaga konsistensi 100% antara dokumen perancangan UML dan kode sumber implementasi Java.")

# ==========================================
# BAGIAN 2: PERANCANGAN UML CLASS DIAGRAM KE KODE JAVA
# ==========================================
add_heading_1(doc, "BAGIAN 2: PERANCANGAN UML CLASS DIAGRAM KE KODE JAVA")

add_heading_2(doc, "2.1 Analisis Desain Struktur Class")
add_body_paragraph(
    doc,
    "Dalam perancangan sistem berorientasi objek, UML Class Diagram bertindak sebagai cetak biru (blueprint) arsitektur sistem. "
    "Komponen sistem dirancang saling berkomunikasi melalui pesan (message passing) dengan batasan tanggung jawab yang jelas. "
    "Struktur relasi antar-komponen digambarkan pada representasi diagram kelas berikut:"
)

uml_diagram_text = (
"+-----------------------------------------------------------------------------------+\n"
"|                                    Petani                                         |\n"
"+-----------------------------------------------------------------------------------+\n"
"| - nik : String                                                                    |\n"
"| - nama : String                                                                   |\n"
"| - asalDesa : String                                                               |\n"
"| - luasLahanHa : double                                                            |\n"
"| - totalSetoranKg : double                                                         |\n"
"+-----------------------------------------------------------------------------------+\n"
"| + Petani(nik: String, nama: String, asalDesa: String, luasLahanHa: double)        |\n"
"| + tambahRiwayatSetoran(beratKg: double) : void                                    |\n"
"| + getNik() : String                                                               |\n"
"| + getNama() : String                                                              |\n"
"| + getAsalDesa() : String                                                          |\n"
"| + getTotalSetoranKg() : double                                                    |\n"
"| + tampilkanProfil() : void                                                        |\n"
"+-----------------------------------------------------------------------------------+\n"
"                                          ^                                          \n"
"                                          | 1..1 (Asosiasi)                          \n"
"+-----------------------------------------+-----------------------------------------+\n"
"|                                  TransaksiPasok                                   |\n"
"+-----------------------------------------------------------------------------------+\n"
"| - idTransaksi : String                                                            |\n"
"| - petani : Petani                                                                 |\n"
"| - tembakau : Tembakau                                                             |\n"
"| - hargaPerKg : double                                                             |\n"
"| - totalPembayaran : double                                                        |\n"
"| - terverifikasi : boolean                                                         |\n"
"+-----------------------------------------------------------------------------------+\n"
"| + TransaksiPasok(idTransaksi: String, petani: Petani, tembakau: Tembakau)          |\n"
"| + kalkulasiHargaOtomatis() : void                                                 |\n"
"| + verifikasiTransaksi() : boolean                                                 |\n"
"| + cetakFakturNota() : void                                                        |\n"
"| + getIdTransaksi() : String                                                       |\n"
"| + isTerverifikasi() : boolean                                                     |\n"
"+-----------------------------------------+-----------------------------------------+\n"
"                                          | 1..1 (Asosiasi)                          \n"
"                                          v                                          \n"
"+-----------------------------------------------------------------------------------+\n"
"|                                   Tembakau                                        |\n"
"+-----------------------------------------------------------------------------------+\n"
"| - kodeBatch : String                                                              |\n"
"| - varietas : String                                                               |\n"
"| - beratKg : double                                                                |\n"
"| - kadarAirPersen : double                                                         |\n"
"| - grade : String                                                                  |\n"
"| - layakProduksi : boolean                                                         |\n"
"+-----------------------------------------------------------------------------------+\n"
"| + Tembakau(kodeBatch: String, varietas: String, beratKg: double, kadarAir: double)|\n"
"| + evaluasiMutuDanGrade() : void                                                   |\n"
"| + getKodeBatch() : String                                                         |\n"
"| + getBeratKg() : double                                                           |\n"
"| + getGrade() : String                                                             |\n"
"| + isLayakProduksi() : boolean                                                     |\n"
"| + tampilkanDetailMutu() : void                                                    |\n"
"+-----------------------------------------------------------------------------------+\n"
"                                          ^                                          \n"
"                                          | 1..* (Agregasi)                          \n"
"+-----------------------------------------o-----------------------------------------+\n"
"|                                GudangTembakau                                     |\n"
"+-----------------------------------------------------------------------------------+\n"
"| - kodeGudang : String                                                             |\n"
"| - namaGudang : String                                                             |\n"
"| - kapasitasMaksKg : double                                                        |\n"
"| - stokTersimpanKg : double                                                        |\n"
"| - daftarTransaksi : List<TransaksiPasok>                                          |\n"
"+-----------------------------------------------------------------------------------+\n"
"| + GudangTembakau(kodeGudang: String, namaGudang: String, kapasitasMaksKg: double) |\n"
"| + terimaPasokan(transaksi: TransaksiPasok) : boolean                              |\n"
"| + cekKapasitasTersedia() : double                                                 |\n"
"| + tampilkanLaporanInventaris() : void                                             |\n"
"+-----------------------------------------------------------------------------------+"
)
add_code_block(doc, uml_diagram_text)

add_heading_2(doc, "2.2 Tabel Pemetaan Komprehensif UML ke Kode Java")
add_body_paragraph(
    doc,
    "Tabel di bawah ini merinci pemetaan formal antara setiap elemen pada UML Class Diagram dengan realisasi teknisnya pada kode sumber bahasa Java:"
)

t_map = doc.add_table(rows=5, cols=4)
t_map.alignment = WD_TABLE_ALIGNMENT.CENTER
t_map.autofit = False
set_table_borders(t_map, color="B0C4DE", sz="6")

headers = ["Nama Class", "Atribut & Visibility", "Method & Responsibilitas Bisnis", "Bentuk Relasi & Realisasi Java"]
hdr_widths = [Inches(1.2), Inches(1.8), Inches(2.1), Inches(1.4)]

# Header row
for idx, text in enumerate(headers):
    cell = t_map.rows[0].cells[idx]
    cell.width = hdr_widths[idx]
    set_cell_background(cell, HEX_HEADER_BG)
    set_cell_margins(cell, top=120, bottom=120, left=100, right=100)
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(10.5)
    run.font.bold = True
    run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

mapping_data = [
    (
        "Petani",
        "- nik : String\n- nama : String\n- asalDesa : String\n- luasLahanHa : double\n- totalSetoranKg : double",
        "+ tambahRiwayatSetoran(beratKg: double): void\n  -> Mengakumulasikan riwayat setoran petani secara valid.\n+ tampilkanProfil(): void",
        "Target Asosiasi:\nObjek independen, dirujuk oleh TransaksiPasok sebagai atribut referensi."
    ),
    (
        "Tembakau",
        "- kodeBatch : String\n- varietas : String\n- beratKg : double\n- kadarAirPersen : double\n- grade : String\n- layakProduksi : boolean",
        "+ evaluasiMutuDanGrade(): void\n  -> Validasi mutu objektif: air > 18% ditolak, <= 12% Super, 13-15% Medium, 16-18% Rendah.\n+ tampilkanDetailMutu(): void",
        "Target Asosiasi:\nObjek independen, dirujuk oleh TransaksiPasok sebagai atribut referensi."
    ),
    (
        "TransaksiPasok",
        "- idTransaksi : String\n- petani : Petani\n- tembakau : Tembakau\n- hargaPerKg : double\n- totalPembayaran : double\n- terverifikasi : boolean",
        "+ kalkulasiHargaOtomatis(): void\n  -> Menghitung harga per kg x berat tembakau.\n+ verifikasiTransaksi(): boolean\n  -> Menolak tembakau rusak dan memicu update akumulasi petani.\n+ cetakFakturNota(): void",
        "Asosiasi (Association):\nprivate Petani petani;\nprivate Tembakau tembakau;\nMenghubungkan entitas penjual dan barang komoditas."
    ),
    (
        "GudangTembakau",
        "- kodeGudang : String\n- namaGudang : String\n- kapasitasMaksKg : double\n- stokTersimpanKg : double\n- daftarTransaksi : List<TransaksiPasok>",
        "+ terimaPasokan(tx: TransaksiPasok): boolean\n  -> Validasi kapasitas sisa dan status verifikasi sebelum disimpan.\n+ cekKapasitasTersedia(): double\n+ tampilkanLaporanInventaris(): void",
        "Agregasi (Aggregation):\nprivate List<TransaksiPasok> daftarTransaksi;\nMengagregasikan transaksi masuk ke gudang."
    )
]

for row_idx, row_data in enumerate(mapping_data, start=1):
    row = t_map.rows[row_idx]
    bg_c = HEX_ZEBRA_BG if row_idx % 2 == 1 else "FFFFFF"
    for col_idx, cell_value in enumerate(row_data):
        cell = row.cells[col_idx]
        cell.width = hdr_widths[col_idx]
        set_cell_background(cell, bg_c)
        set_cell_margins(cell, top=100, bottom=100, left=100, right=100)
        p = cell.paragraphs[0]
        p.paragraph_format.line_spacing = 1.05
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        run = p.add_run(cell_value)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(10)
        if col_idx == 0:
            run.font.bold = True

add_body_paragraph(doc, "")

add_heading_2(doc, "2.3 Analisis Bentuk Relasi Antar-Object")
add_body_paragraph(
    doc,
    "Pada sistem ini, relasi antar-object diimplementasikan menggunakan dua mekanisme utama OOP:",
    bold_prefix="Kajian Bentuk Relasi: "
)
add_bullet_item(
    doc,
    "Relasi antara TransaksiPasok dengan Petani dan Tembakau merupakan asosiasi biner berarah (directed association). "
    "Dalam kode Java, relasi ini diwujudkan dengan mendeklarasikan atribut bertipe objek lain, yaitu 'private Petani petani;' "
    "dan 'private Tembakau tembakau;'. Melalui asosiasi ini, TransaksiPasok dapat memanggil method pada objek Petani "
    "(seperti memanggil 'petani.tambahRiwayatSetoran()') dan memeriksa kondisi objek Tembakau ('tembakau.isLayakProduksi()').",
    bold_prefix="1. Relasi Asosiasi (Association 1..1): "
)
add_bullet_item(
    doc,
    "Relasi antara GudangTembakau dengan TransaksiPasok merupakan relasi kepemilikan utuh-sebagian (whole-part relationship) yang bersifat agregasi. "
    "Artinya, gudang mengelola banyak objek transaksi masuk menggunakan koleksi dinamis 'private List<TransaksiPasok> daftarTransaksi = new ArrayList<>();'. "
    "Karakteristik agregasi terlihat dari siklus hidup (lifecycle) kedua objek: jika objek GudangTembakau dihapus dari memori, objek transaksi dan petani yang tercatat "
    "tetap dapat eksis secara independen.",
    bold_prefix="2. Relasi Agregasi (Aggregation 1..*): "
)

# ==========================================
# BAGIAN 3: IMPLEMENTASI SOURCE CODE & ENKAPSULASI
# ==========================================
add_heading_1(doc, "BAGIAN 3: IMPLEMENTASI SOURCE CODE & ENKAPSULASI")

add_body_paragraph(
    doc,
    "Seluruh modul kode program di bawah ini telah dirancang secara modular, memenuhi kaidah Clean Code, "
    "dan dapat langsung dikompilasi pada Java Development Kit (JDK) versi modern tanpa ketergantungan library eksternal."
)

add_heading_2(doc, "3.1 Kode Sumber: Petani.java")
add_body_paragraph(
    doc,
    "Class Petani memodelkan produsen tembakau. Atribut total setoran dikunci dari manipulasi eksternal dan hanya dapat ditambah secara valid melalui method tambahRiwayatSetoran()."
)
add_code_block(doc, code_petani)

add_heading_2(doc, "3.2 Kode Sumber: Tembakau.java")
add_body_paragraph(
    doc,
    "Class Tembakau memodelkan komoditas fisik tembakau. Di dalamnya terdapat evaluasi kadar air terintegrasi. "
    "Kadar air > 18% secara deterministik dinyatakan 'REJECTED' (tidak layak produksi), sedangkan kadar air <= 12% ditetapkan sebagai 'GRADE A (SUPER)'."
)
add_code_block(doc, code_tembakau)

add_heading_2(doc, "3.3 Kode Sumber: TransaksiPasok.java")
add_body_paragraph(
    doc,
    "Class TransaksiPasok menerapkan relasi asosiasi ke Petani dan Tembakau. Class ini bertanggung jawab mengalkulasi nilai rupiah transaksi serta "
    "memverifikasi kelayakan pasokan sebelum dinyatakan sah."
)
add_code_block(doc, code_transaksi)

add_heading_2(doc, "3.4 Kode Sumber: GudangTembakau.java")
add_body_paragraph(
    doc,
    "Class GudangTembakau menerapkan relasi agregasi terhadap kumpulan transaksi pasok. Method terimaPasokan() menjaga agar akumulasi stok fisik tembakau "
    "tidak melebihi kapasitas maksimum yang ditetapkan."
)
add_code_block(doc, code_gudang)

add_heading_2(doc, "3.5 Kode Sumber: Main.java (Driver Pengujian)")
add_body_paragraph(
    doc,
    "Class Main menguji seluruh skenario interaksi object: pendaftaran petani, pengujian mutu batch (kadar air prima, sedang, dan terlalu basah), "
    "pembuatan transaksi, penolakan transaksi mutu rusak, serta pengujian batas kapasitas gudang (overcapacity)."
)
add_code_block(doc, code_main)

add_heading_2(doc, "3.6 Pembahasan Mendalam Prinsip Enkapsulasi & Invariant Protection")
add_body_paragraph(
    doc,
    "Enkapsulasi sering kali disalahpahami sebagai kebiasaan mekanis mengubah atribut menjadi private lalu menyediakan method getter dan setter secara bebas. "
    "Praktik tersebut melanggar filosofi dasar OOP karena 'setter polos' membiarkan dunia luar memodifikasi kondisi internal objek tanpa kontrol, "
    "sehingga melenyapkan integritas bisnis sistem.",
    bold_prefix="Kritik terhadap Setter Polos: "
)

add_body_paragraph(
    doc,
    "Pada program Rantai Pasok Tembakau ini, konsep Invariant Protection (perlindungan kondisi validitas data) ditegakkan melalui tiga prinsip teknis:",
    bold_prefix="Penerapan Proteksi State Invariant: "
)

add_bullet_item(
    doc,
    "Pada konstruktor Petani, Tembakau, dan GudangTembakau, setiap argumen yang masuk divalidasi seketika. "
    "Jika pengguna memberikan parameter berat <= 0 atau kadar air di luar rentang 0-100%, sistem langsung melempar IllegalArgumentException. "
    "Hal ini menjamin bahwa objek di memori tidak pernah berada dalam kondisi cacat logika.",
    bold_prefix="1. Fail-Fast Constructor: "
)

add_bullet_item(
    doc,
    "Atribut grade pada Tembakau dan status terverifikasi pada TransaksiPasok tidak memiliki method setter publik. "
    "Nilai grade sepenuhnya dihitung oleh method evaluasiMutuDanGrade() berbasis kadar air laboratorium. Pihak luar tidak dapat memaksakan grade 'Super' "
    "pada daun tembakau basah.",
    bold_prefix="2. Immutable Grade Calculation: "
)

add_bullet_item(
    doc,
    "Nilai akumulasi total setoran petani (totalSetoranKg) dan stok gudang (stokTersimpanKg) hanya dapat diubah melalui method "
    "tambahRiwayatSetoran() dan terimaPasokan(). Setiap mutasi dicek terhadap batas atas dan batas bawah kuantitas, menjaga konsistensi state secara permanen.",
    bold_prefix="3. Guarded State Mutation: "
)

# ==========================================
# BAGIAN 4: PENGUJIAN & ANALISIS HASIL
# ==========================================
add_heading_1(doc, "BAGIAN 4: PENGUJIAN & ANALISIS HASIL")

add_heading_2(doc, "4.1 Skenario Pengujian Alur Program")
add_body_paragraph(
    doc,
    "Pengujian dilakukan dengan mensimulasikan kondisi operasional riil rantai pasok tembakau di Kabupaten Sumenep dengan 5 tahapan skenario:"
)

t_sc = doc.add_table(rows=6, cols=3)
t_sc.alignment = WD_TABLE_ALIGNMENT.CENTER
t_sc.autofit = False
set_table_borders(t_sc, color="B0C4DE", sz="6")

sc_headers = ["Tahap Skenario", "Aktivitas Pengujian", "Ekspektasi Output Sistem"]
sc_widths = [Inches(1.5), Inches(2.6), Inches(2.4)]

for idx, text in enumerate(sc_headers):
    cell = t_sc.rows[0].cells[idx]
    cell.width = sc_widths[idx]
    set_cell_background(cell, HEX_HEADER_BG)
    set_cell_margins(cell, top=100, bottom=100, left=100, right=100)
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(10.5)
    run.font.bold = True
    run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

sc_data = [
    ("Langkah 1", "Registrasi 2 petani mitra (Pak Hasan Basri & Pak Jumai) dengan data lahan.", "Objek petani terbentuk dengan total setoran awal 0.00 kg."),
    ("Langkah 2", "Uji mutu 3 batch panen: batchA (air 10.5%), batchB (air 14.0%), batchC (air 21.5%).", "batchA dinilai Grade A (Super), batchB dinilai Grade B (Medium), batchC berstatus REJECTED (Ditolak)."),
    ("Langkah 3", "Verifikasi transaksi pasok tx1, tx2, dan tx3.", "tx1 dan tx2 lolos verifikasi dan menerbitkan faktur pembayaran resmi. tx3 ditolak karena mutu tidak layak."),
    ("Langkah 4", "Pengiriman pasokan ke gudang berkapasitas 1000 kg dan simulasi muatan overcapacity.", "tx1 (250 kg) dan tx2 (400 kg) tersimpan di gudang (total 650 kg). tx3 ditolak karena belum terverifikasi. Transaksi jumbo (500 kg) ditolak karena overcapacity."),
    ("Langkah 5", "Pemeriksaan akhir profil akumulasi setoran petani dan inventaris gudang.", "Total setoran petani terakumulasi secara sinkron, dan rekap gudang menampilkan rincian batch terdaftar.")
]

for row_idx, row_data in enumerate(sc_data, start=1):
    row = t_sc.rows[row_idx]
    bg_c = HEX_ZEBRA_BG if row_idx % 2 == 1 else "FFFFFF"
    for col_idx, cell_value in enumerate(row_data):
        cell = row.cells[col_idx]
        cell.width = sc_widths[col_idx]
        set_cell_background(cell, bg_c)
        set_cell_margins(cell, top=90, bottom=90, left=100, right=100)
        p = cell.paragraphs[0]
        p.paragraph_format.line_spacing = 1.05
        run = p.add_run(cell_value)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(10)
        if col_idx == 0:
            run.font.bold = True

add_body_paragraph(doc, "")

add_heading_2(doc, "4.2 Output Terminal Eksekusi Program (Console Log)")
add_body_paragraph(
    doc,
    "Berikut adalah log keluaran terminal riil yang dihasilkan dari kompilasi dan eksekusi Main.java menggunakan Java 25 Runtime Environment:"
)

console_output_text = (
"===============================================================\n"
"    SIMULASI SISTEM INFORMASI RANTAI PASOK TEMBAKAU SUMENEP    \n"
"        PRAKTIKUM PBO - MODUL 9 - PENS PSDKU SUMENEP           \n"
"===============================================================\n\n"
"[LANGKAH 1: REGISTRASI DATA PETANI MITRA]\n"
"--------------------------------------------------\n"
"             DATA IDENTITAS PETANI               \n"
"--------------------------------------------------\n"
"NIK Petani         : 3529012304780001\n"
"Nama Lengkap       : Pak Hasan Basri\n"
"Asal Desa / Daerah : Desa Guluk-Guluk\n"
"Luas Lahan         : 1.80 Ha\n"
"Total Setoran      : 0.00 kg\n"
"--------------------------------------------------\n\n"
"[LANGKAH 2: UJI LABORATORIUM & PENILAIAN MUTU TEMBAKAU]\n"
"--------------------------------------------------\n"
"            UJI LABORATORIUM MUTU TEMBAKAU        \n"
"--------------------------------------------------\n"
"Kode Batch         : BATCH-GLK-001\n"
"Varietas Daun      : Campalok Super\n"
"Berat Bersih       : 250.00 kg\n"
"Kadar Air          : 10.50 %\n"
"Hasil Grade        : GRADE A (SUPER)\n"
"Status Kelayakan   : DITERIMA / LOLOS UJI\n"
"--------------------------------------------------\n"
"--------------------------------------------------\n"
"            UJI LABORATORIUM MUTU TEMBAKAU        \n"
"--------------------------------------------------\n"
"Kode Batch         : BATCH-PSS-002\n"
"Varietas Daun      : Prancak 95\n"
"Berat Bersih       : 400.00 kg\n"
"Kadar Air          : 14.00 %\n"
"Hasil Grade        : GRADE B (MEDIUM)\n"
"Status Kelayakan   : DITERIMA / LOLOS UJI\n"
"--------------------------------------------------\n"
"--------------------------------------------------\n"
"            UJI LABORATORIUM MUTU TEMBAKAU        \n"
"--------------------------------------------------\n"
"Kode Batch         : BATCH-PSS-003\n"
"Varietas Daun      : Cangkring\n"
"Berat Bersih       : 180.00 kg\n"
"Kadar Air          : 21.50 %\n"
"Hasil Grade        : REJECTED (TIDAK LAYAK)\n"
"Status Kelayakan   : DITOLAK (Terlalu Basah)\n"
"--------------------------------------------------\n\n"
"[LANGKAH 3: PEMBUATAN TRANSAKSI & VERIFIKASI AKAD PASOK]\n\n"
"--- Eksekusi Verifikasi Transaksi 1 ---\n"
"[INFO] Riwayat setoran Petani Pak Hasan Basri bertambah 250.00 kg. (Total: 250.00 kg)\n"
"[SUKSES] Transaksi TRX-2026-001 berhasil diverifikasi dan disahkan.\n"
"==================================================\n"
"              FAKTUR PEMBAYARAN PASOK             \n"
"==================================================\n"
"No. Transaksi      : TRX-2026-001\n"
"Nama Petani        : Pak Hasan Basri (Desa Guluk-Guluk)\n"
"Batch Tembakau     : BATCH-GLK-001 [Campalok Super]\n"
"Kualitas Grade     : GRADE A (SUPER)\n"
"Berat Disetor      : 250.00 kg\n"
"Harga Satuan       : Rp 85,000.00 / kg\n"
"--------------------------------------------------\n"
"TOTAL PEMBAYARAN   : Rp 21,250,000.00\n"
"Status Verifikasi  : LUNAS & TERVERIFIKASI\n"
"==================================================\n\n"
"--- Eksekusi Verifikasi Transaksi 2 ---\n"
"[INFO] Riwayat setoran Petani Pak Jumai bertambah 400.00 kg. (Total: 400.00 kg)\n"
"[SUKSES] Transaksi TRX-2026-002 berhasil diverifikasi dan disahkan.\n"
"==================================================\n"
"              FAKTUR PEMBAYARAN PASOK             \n"
"==================================================\n"
"No. Transaksi      : TRX-2026-002\n"
"Nama Petani        : Pak Jumai (Desa Pasongsongan)\n"
"Batch Tembakau     : BATCH-PSS-002 [Prancak 95]\n"
"Kualitas Grade     : GRADE B (MEDIUM)\n"
"Berat Disetor      : 400.00 kg\n"
"Harga Satuan       : Rp 60,000.00 / kg\n"
"--------------------------------------------------\n"
"TOTAL PEMBAYARAN   : Rp 24,000,000.00\n"
"Status Verifikasi  : LUNAS & TERVERIFIKASI\n"
"==================================================\n\n"
"--- Eksekusi Verifikasi Transaksi 3 (Kasus Mutu Rusak) ---\n"
"[GAGAL VERIFIKASI] Transaksi TRX-2026-003 ditolak karena mutu tembakau tidak memenuhi standar.\n"
"==================================================\n"
"              FAKTUR PEMBAYARAN PASOK             \n"
"==================================================\n"
"No. Transaksi      : TRX-2026-003\n"
"Nama Petani        : Pak Jumai (Desa Pasongsongan)\n"
"Batch Tembakau     : BATCH-PSS-003 [Cangkring]\n"
"Kualitas Grade     : REJECTED (TIDAK LAYAK)\n"
"Berat Disetor      : 180.00 kg\n"
"Harga Satuan       : Rp 0.00 / kg\n"
"--------------------------------------------------\n"
"TOTAL PEMBAYARAN   : Rp 0.00\n"
"Status Verifikasi  : MENUNGGU VERIFIKASI / DITOLAK\n"
"==================================================\n\n"
"[LANGKAH 4: PENGELOLAAN LOGISTIK & PENERIMAAN GUDANG]\n\n"
"--> Mengirim Transaksi 1 ke Gudang:\n"
"[SUKSES GUDANG] Pasokan batch BATCH-GLK-001 berhasil disimpan di Gudang Penyangga Sumenep. Stok sekarang: 250.00 / 1000.00 kg.\n\n"
"--> Mengirim Transaksi 2 ke Gudang:\n"
"[SUKSES GUDANG] Pasokan batch BATCH-PSS-002 berhasil disimpan di Gudang Penyangga Sumenep. Stok sekarang: 650.00 / 1000.00 kg.\n\n"
"--> Mengirim Transaksi 3 ke Gudang (Harus Gagal karena Belum Terverifikasi):\n"
"[PENOLAKAN GUDANG] Transaksi TRX-2026-003 belum diverifikasi atau ditolak mutu.\n\n"
"--> Menguji Simulasi Pasokan Melebihi Kapasitas (Overcapacity):\n"
"[INFO] Riwayat setoran Petani Pak Hasan Basri bertambah 500.00 kg. (Total: 750.00 kg)\n"
"[SUKSES] Transaksi TRX-2026-004 berhasil diverifikasi dan disahkan.\n"
"[OVERCAPACITY] Gudang Gudang Penyangga Sumenep penuh! Sisa kapasitas: 350.00 kg, Muatan masuk: 500.00 kg.\n\n"
"##################################################\n"
"          LAPORAN REKAPITULASI INVENTARIS GUDANG   \n"
"##################################################\n"
"Kode Gudang        : GDG-SMP-01\n"
"Nama Fasilitas     : Gudang Penyangga Sumenep\n"
"Kapasitas Maksimal : 1,000.00 kg\n"
"Stok Tersimpan     : 650.00 kg (Tersedia: 350.00 kg)\n"
"Jumlah Transaksi   : 2 Transaksi Masuk\n"
"--------------------------------------------------\n"
"RINCIAN BATCH TEMBAKAU TERSIMPAN:\n"
"   1. Batch: BATCH-GLK-001 | Petani: Pak Hasan Basri | Grade: GRADE A (SUPER)  | Berat: 250.00 kg\n"
"   2. Batch: BATCH-PSS-002 | Petani: Pak Jumai       | Grade: GRADE B (MEDIUM) | Berat: 400.00 kg\n"
"##################################################\n\n"
"[LANGKAH 5: EVALUASI AKHIR RIWAYAT PETANI]\n"
"--------------------------------------------------\n"
"             DATA IDENTITAS PETANI               \n"
"--------------------------------------------------\n"
"NIK Petani         : 3529012304780001\n"
"Nama Lengkap       : Pak Hasan Basri\n"
"Asal Desa / Daerah : Desa Guluk-Guluk\n"
"Luas Lahan         : 1.80 Ha\n"
"Total Setoran      : 750.00 kg\n"
"--------------------------------------------------\n"
"--------------------------------------------------\n"
"             DATA IDENTITAS PETANI               \n"
"--------------------------------------------------\n"
"NIK Petani         : 3529021508820002\n"
"Nama Lengkap       : Pak Jumai\n"
"Asal Desa / Daerah : Desa Pasongsongan\n"
"Luas Lahan         : 2.50 Ha\n"
"Total Setoran      : 400.00 kg\n"
"--------------------------------------------------"
)
add_code_block(doc, console_output_text)

add_heading_2(doc, "4.3 Tempat Screenshot Hasil Eksekusi Program")
add_body_paragraph(
    doc,
    "Berikut merupakan tempat untuk menyematkan tangkapan layar (screenshot) terminal konsol saat program Main.java dijalankan:"
)
add_callout_box(doc, "\n\n[TEMPEL SCREENSHOT HASIL RUN PROGRAM DI SINI]\n(Klik kanan pada kotak ini, pilih Add Image / Paste Gambar Screenshot Terminal Anda)\n\n")

add_heading_2(doc, "4.4 Analisis & Pembahasan Mendalam Hasil Pengujian")
add_body_paragraph(
    doc,
    "Berdasarkan analisis hasil luaran eksekusi program di atas, terdapat beberapa kesimpulan penting:",
    bold_prefix="Kajian Hasil Eksekusi: "
)
add_bullet_item(
    doc,
    "Sistem berhasil menunjukkan kolaborasi antar-object secara konsisten. Ketika transaksi tx1 disahkan, "
    "TransaksiPasok secara otomatis memicu pemanggilan method pada objek Petani sehingga nilai totalSetoranKg bertambah secara kumulatif "
    "(dari 0 kg menjadi 250 kg). Ini membuktikan keberhasilan implementasi Message Passing pada relasi asosiasi.",
    bold_prefix="1. Keberhasilan Kolaborasi Antar-Objek: "
)
add_bullet_item(
    doc,
    "Pada batchC dengan kadar air 21.5%, sistem berhasil mencegat komoditas bermutu buruk secara mandiri. "
    "Ketika transaksi tx3 diverifikasi, method verifikasiTransaksi() mengembalikan nilai false dan mencegah "
    "pemberian nilai pembayaran (Rp 0.00). Ini membuktikan bahwa aturan bisnis pada kelas model berhasil melindungi sistem dari transaksi yang tidak sah.",
    bold_prefix="2. Ketahanan Business Rule Terhadap Komoditas Rusak: "
)
add_bullet_item(
    doc,
    "Pada saat pengujian transaksi overcapacity (muatan 500 kg ke gudang yang tersisa kapasitas 350 kg), "
    "GudangTembakau berhasil menolak penyimpanan tanpa merusak data transaksi yang sudah masuk sebelumnya (tetap aman pada 650 kg). "
    "Hal ini menunjukkan bahwa agregasi objek dengan proteksi kapasitas bekerja secara andal.",
    bold_prefix="3. Keandalan Pengelolaan Kapasitas Gudang: "
)

# ==========================================
# BAGIAN 5: JAWABAN PERTANYAAN EVALUASI MODUL 9
# ==========================================
add_heading_1(doc, "BAGIAN 5: JAWABAN PERTANYAAN EVALUASI MODUL 9")
add_body_paragraph(
    doc,
    "Berikut adalah jawaban komprehensif atas 5 pertanyaan evaluasi praktikum Modul 9 yang ditetapkan oleh Dosen Pengampu "
    "(Nirwana Haidar Hari, S.Pd., M.Kom.):"
)

eval_qa = [
    (
        "Pertanyaan 1: Apa perbedaan class pada UML dan object pada program?",
        "Jawaban:\n"
        "Class pada UML merupakan cetak biru (blueprint) atau representasi konseptual abstrak yang mendefinisikan struktur "
        "(atribut, tipe data, dan visibilitas) serta perilaku (method dan tanggung jawab) yang akan dimiliki oleh suatu entitas sistem. "
        "Class pada UML bersifat statis dan belum mengalokasikan ruang memori fisik.\n"
        "Sedangkan Object pada program adalah wujud nyata (instansiasi konkrit) dari class yang dibuat saat program berjalan (runtime). "
        "Object memiliki identitas unik, status spesifik (state bernilai aktual pada atributnya), serta menempati alokasi memori fisik di Heap Memory komputer. "
        "Sebagai analogi, Class Tembakau pada UML adalah format deskripsi spesifikasi mutu daun tembakau, sedangkan objek batchA adalah 250 kg daun tembakau Campalok Super milik Pak Hasan yang sedang ditimbang di gudang."
    ),
    (
        "Pertanyaan 2: Bagaimana association pada UML diterapkan dalam Java?",
        "Jawaban:\n"
        "Relasi Association pada UML menggambarkan hubungan struktural di mana satu class mengetahui dan berinteraksi dengan class lainnya. "
        "Dalam bahasa Java, asosiasi diterapkan dengan menjadikan class target sebagai atribut instans bertipe objek (reference type attribute) "
        "di dalam class sumber, dan nilainya biasanya diinjeksikan melalui konstruktor atau method.\n"
        "Contoh konkret pada proyek ini: Class TransaksiPasok memiliki atribut berelasi:\n"
        "   private Petani petani;\n"
        "   private Tembakau tembakau;\n"
        "Dengan deklarasi tersebut, TransaksiPasok dapat memanggil method pada objek Petani (seperti petani.tambahRiwayatSetoran()) "
        "dan method pada objek Tembakau (seperti tembakau.isLayakProduksi())."
    ),
    (
        "Pertanyaan 3: Mengapa attribute sebaiknya tidak seluruhnya public?",
        "Jawaban:\n"
        "Jika seluruh atribut berderajat visibilitas public, sistem akan kehilangan prinsip Enkapsulasi (data-hiding). "
        "Siapa pun (bagian kode di luar class) dapat secara sembarangan mengubah status data tanpa melalui mekanisme validasi logika bisnis. "
        "Contoh bahayanya: atribut beratKg atau totalSetoranKg dapat diisi nilai negatif (-500 kg), atau kadar air tembakau basah yang berjamur "
        "diubah paksa menjadi grade 'Super' dari luar tanpa melewati uji lab. "
        "Dengan menetapkan atribut sebagai private, integritas data (data integrity) dan aturan invariant class terlindungi secara mutlak di dalam class itu sendiri."
    ),
    (
        "Pertanyaan 4: Apakah source code proyek sudah konsisten dengan UML?",
        "Jawaban:\n"
        "Ya, source code proyek sudah 100% konsisten dengan rancangan UML Class Diagram yang disusun. Konsistensi tersebut meliputi:\n"
        "1. Kesesuaian Nama Class: Petani, Tembakau, TransaksiPasok, GudangTembakau, dan Main.\n"
        "2. Kesesuaian Atribut & Visibilitas: Seluruh tanda minus (-) pada UML diterapkan sebagai 'private' pada Java, dan tanda plus (+) diterapkan sebagai 'public'.\n"
        "3. Kesesuaian Method & Return Type: Setiap method pada UML memiliki nama, parameter, dan tipe kembalian yang identik di kode Java.\n"
        "4. Kesesuaian Relasi: Asosiasi antara TransaksiPasok dengan Petani dan Tembakau serta Agregasi pada GudangTembakau terbukti terimplementasi sesuai notasinya."
    ),
    (
        "Pertanyaan 5: Class mana yang memiliki responsibility paling besar dalam proyek Anda?",
        "Jawaban:\n"
        "Class yang memiliki tanggung jawab (responsibility) bisnis paling sentral dan besar adalah TransaksiPasok.\n"
        "Alasannya: TransaksiPasok bertindak sebagai jembatan mediator (koordinator bisnis utama) yang menghubungkan entitas Petani dan Tembakau. "
        "Class ini tidak hanya menyimpan data transaksi, tetapi mengemban tanggung jawab kritis untuk:\n"
        "- Mengevaluasi kelayakan mutu daun tembakau secara menyeluruh sebelum akad disahkan.\n"
        "- Menentukan besaran nilai nominal rupiah berdasarkan kalkulasi perkalian grade mutu dan bobot bersih.\n"
        "- Menjamin sinkronisasi data antar-objek dengan memperbarui riwayat akumulasi tonase setoran pada objek Petani.\n"
        "- Menjadi prasyarat mutlak verifikasi sebelum komoditas tembakau diizinkan masuk dan tersimpan pada fasilitas GudangTembakau."
    )
]

for q_title, q_ans in eval_qa:
    add_heading_2(doc, q_title)
    add_body_paragraph(doc, q_ans)

# ==========================================
# BAGIAN 6: KESIMPULAN & PENUTUP
# ==========================================
add_heading_1(doc, "BAGIAN 6: KESIMPULAN")
add_body_paragraph(
    doc,
    "Berdasarkan pelaksanaan praktikum Modul 9 mengenai Implementasi UML Class Diagram ke Program Java pada studi kasus "
    "Sistem Informasi Manajemen Rantai Pasok Tembakau, diperoleh kesimpulan sebagai berikut:"
)
add_bullet_item(
    doc,
    "UML Class Diagram terbukti sangat efektif sebagai panduan arsitektur perangkat lunak yang presisi. "
    "Penerjemahan diagram kelas ke kode Java dapat dilakukan secara sistematis mulai dari penentuan visibilitas atribut, "
    "penyusunan konstruktor, hingga penerapan method yang mencerminkan tanggung jawab bisnis nyata.",
    bold_prefix="1. Presisi Arsitektur: "
)
add_bullet_item(
    doc,
    "Penerapan Enkapsulasi Ketat (Strict Encapsulation) tanpa setter polos dan didukung validasi kontrak pada konstruktor "
    "(fail-fast principle) berhasil mengamankan status data objek dari manipulasi yang tidak valid, sehingga menjamin keandalan sistem.",
    bold_prefix="2. Ketahanan Enkapsulasi: "
)
add_bullet_item(
    doc,
    "Realisasi relasi Association (1..1) dan Aggregation (1..*) pada Java berhasil memperlihatkan prinsip modularitas dan loose coupling, "
    "di mana masing-masing entitas dapat dikembangkan secara independen namun tetap mampu berkolaborasi secara dinamis melalui mekanisme pertukaran pesan.",
    bold_prefix="3. Kekuatan Relasi Antar-Objek: "
)

# Save Document
OUTPUT_FINAL = "Moch. Ilham Abdul Aziz_PBO_3125522014_Pertemuan 9_FINAL.docx"
doc.save(OUTPUT_FINAL)
print(f"[SUKSES] Dokumen Word '{OUTPUT_FINAL}' berhasil disimpan!")

try:
    doc.save(DOC_NAME)
    print(f"[SUKSES] Dokumen Word '{DOC_NAME}' berhasil diperbarui langsung!")
except Exception as e:
    print(f"[CATATAN] File '{DOC_NAME}' saat ini sedang dibuka di Microsoft Word.")
    print(f"[CATATAN] File telah berhasil disimpan sebagai '{OUTPUT_FINAL}'.")

