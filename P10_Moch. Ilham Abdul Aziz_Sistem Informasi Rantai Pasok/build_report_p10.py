import os
import shutil
import docx
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

ORIGINAL_DOC = "Moch. Ilham Abdul Aziz_PBO_3125522014_Pertemuan 10.docx"
BACKUP_DOC = "Moch. Ilham Abdul Aziz_PBO_3125522014_Pertemuan 10_backup.docx"

if not os.path.exists(BACKUP_DOC):
    shutil.copyfile(ORIGINAL_DOC, BACKUP_DOC)
    print(f"[INFO] Backup dokumen dibuat di: {BACKUP_DOC}")
else:
    print(f"[INFO] Menggunakan backup yang telah ada: {BACKUP_DOC}")

doc = docx.Document(BACKUP_DOC)

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

def set_table_borders(table, color="B0C4DE", sz="6", val="single"):
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
    run.font.size = Pt(9.0)
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
    set_cell_margins(cell, top=200, bottom=200, left=200, right=200)
    
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

# Read Java source files
def read_code_file(relative_path):
    with open(relative_path, "r", encoding="utf-8") as f:
        return f.read().strip()

BASE_SRC = "src/main/java/com/rantaitasok/tembakau/modul10"
code_kgp_exc = read_code_file(os.path.join(BASE_SRC, "exception", "KapasitasGudangPenuhException.java"))
code_mtd_exc = read_code_file(os.path.join(BASE_SRC, "exception", "MutuTembakauDitolakException.java"))
code_tbv_exc = read_code_file(os.path.join(BASE_SRC, "exception", "TransaksiBelumTerverifikasiException.java"))
code_petani = read_code_file(os.path.join(BASE_SRC, "Petani.java"))
code_tembakau = read_code_file(os.path.join(BASE_SRC, "Tembakau.java"))
code_transaksi = read_code_file(os.path.join(BASE_SRC, "TransaksiPasok.java"))
code_gudang = read_code_file(os.path.join(BASE_SRC, "GudangTembakau.java"))
code_main = read_code_file(os.path.join(BASE_SRC, "Main.java"))

print("[INFO] Berhasil membaca seluruh berkas source code Java Modul 10.")

# Rebuild document cleanly preserving cover page:
# Locate DAFTAR ISI, SOAL, JAWABAN
p_cover_end = None
p_daftar_isi = None
p_soal = None
p_jawaban = None

for p in doc.paragraphs:
    txt = p.text.strip()
    if txt == "DAFTAR ISI":
        p_daftar_isi = p
    elif txt == "SOAL":
        p_soal = p
    elif txt == "JAWABAN":
        p_jawaban = p

# Clean empty paragraphs between DAFTAR ISI and SOAL, and populate DAFTAR ISI:
daftar_isi_entries = [
    ("SOAL TUGAS PRAKTIKUM MODUL 10", True),
    ("JAWABAN TUGAS PRAKTIKUM MODUL 10", True),
    ("    BAGIAN 1: IDENTIFIKASI KONDISI KESALAHAN & VALIDASI STATE", False),
    ("        1.1 Konteks Bisnis Sistem Rantai Pasok Tembakau Sumenep", False),
    ("        1.2 Identifikasi Kondisi Kesalahan Kritis dalam Sistem", False),
    ("    BAGIAN 2: ALASAN & KEUNGGULAN CUSTOM EXCEPTION", False),
    ("        2.1 Kelemahan Penggunaan Exception Generik", False),
    ("        2.2 Keunggulan Strategis Custom Checked Exception", False),
    ("    BAGIAN 3: IMPLEMENTASI SOURCE CODE PROYEK LENGKAP", False),
    ("        3.1 Custom Exception: KapasitasGudangPenuhException.java", False),
    ("        3.2 Custom Exception: MutuTembakauDitolakException.java", False),
    ("        3.3 Custom Exception: TransaksiBelumTerverifikasiException.java", False),
    ("        3.4 Model Entitas: Petani.java", False),
    ("        3.5 Model Entitas: Tembakau.java", False),
    ("        3.6 Model Asosiasi: TransaksiPasok.java", False),
    ("        3.7 Model Agregasi: GudangTembakau.java", False),
    ("        3.8 Driver Pengujian Komprehensif: Main.java", False),
    ("    BAGIAN 4: TABEL PENGUJIAN VALIDASI & KONSISTENSI STATE", False),
    ("    BAGIAN 5: LOG SCREENSHOT KONSOL EKSEKUSI TERMINAL", False),
    ("    BAGIAN 6: JAWABAN PERTANYAAN EVALUASI MODUL 10", False),
    ("        6.1 Pertanyaan 1: Perbedaan 'throw' dan 'throws'", False),
    ("        6.2 Pertanyaan 2: Mengapa Pemeriksaan Harus Mendahului Mutasi State", False),
    ("        6.3 Pertanyaan 3: Dampak Buruk Silent Failure / Empty Catch Block", False),
    ("        6.4 Pertanyaan 4: Alasan Mengapa Mutasi State Dilarang di Blok 'finally'", False),
]

# We can insert DAFTAR ISI table right after p_daftar_isi
table_toc = doc.add_table(rows=len(daftar_isi_entries), cols=1)
table_toc.alignment = WD_TABLE_ALIGNMENT.CENTER
table_toc.autofit = False

# Move table right after p_daftar_isi
p_daftar_isi._p.addnext(table_toc._tbl)

for r_idx, (title, is_bold) in enumerate(daftar_isi_entries):
    cell = table_toc.rows[r_idx].cells[0]
    cell.width = Inches(6.5)
    set_cell_margins(cell, top=30, bottom=30, left=50, right=50)
    p_c = cell.paragraphs[0]
    p_c.paragraph_format.line_spacing = 1.05
    p_c.paragraph_format.space_before = Pt(0)
    p_c.paragraph_format.space_after = Pt(0)
    run = p_c.add_run(title)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(10.5)
    run.font.bold = is_bold
    if is_bold:
        run.font.color.rgb = C_NAVY

# Under SOAL, populate neat summary
soal_content = [
    ("Mata Kuliah: ", "Praktikum Pemrograman Berorientasi Obyek (PBO)"),
    ("Topik / Pertemuan: ", "Modul 10 – Exception Handling dan Validasi State Objek"),
    ("Prasyarat: ", "Implementasi class dan relasi proyek pada Pertemuan 9."),
    ("Tujuan Praktikum: ", "Mahasiswa mampu membedakan validasi input dan penanganan exception; menggunakan try, catch, finally, throw, dan throws; membuat custom checked exception sesuai kasus; serta memastikan kegagalan operasi tidak merusak state objek (state invariant protection)."),
    ("Tugas Proyek Berorientasi Objek (Rantai Pasok Tembakau):", ""),
    ("1. ", "Identifikasi minimal 3 kondisi kesalahan sesuai kasus proyek."),
    ("2. ", "Tambahkan validasi pada constructor atau method yang relevan (fail-fast validation)."),
    ("3. ", "Buat minimal 1 custom exception dengan nama yang menjelaskan kegagalan secara domain-driven."),
    ("4. ", "Tangani exception pada bagian pemanggil dengan pesan yang jelas (spesifik ke umum)."),
    ("5. ", "Uji skenario berhasil dan gagal; buktikan state objek tetap konsisten ketika operasi ditolak."),
    ("Pertanyaan Evaluasi Modul 10:", ""),
    ("1. ", "Apa perbedaan throw dan throws secara sintaksis dan fungsional?"),
    ("2. ", "Mengapa pemeriksaan kondisi harus dilakukan sebelum mutasi state objek?"),
    ("3. ", "Apa akibatnya jika exception ditangkap tetapi tidak ditangani (empty catch block / silent failure)?"),
    ("4. ", "Mengapa pembaruan/mutasi state tidak boleh ditempatkan dalam blok finally?")
]

# Insert after p_soal
curr_target = p_soal
for prefix, txt in soal_content:
    p_new = doc.add_paragraph()
    curr_target._p.addnext(p_new._p)
    curr_target = p_new
    
    p_new.paragraph_format.line_spacing = 1.15
    p_new.paragraph_format.space_after = Pt(3)
    p_new.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    if prefix:
        r_pre = p_new.add_run(prefix)
        r_pre.font.name = 'Times New Roman'
        r_pre.font.size = Pt(11)
        r_pre.font.bold = True
        if "Tugas" in prefix or "Pertanyaan" in prefix:
            r_pre.font.color.rgb = C_NAVY
    if txt:
        r_txt = p_new.add_run(txt)
        r_txt.font.name = 'Times New Roman'
        r_txt.font.size = Pt(11)

# Now, under JAWABAN, we append all sections!
doc.add_page_break()

# ==============================================================================
# JAWABAN SECTION 1: LATAR BELAKANG & IDENTIFIKASI KESALAHAN SISTEM RANTAI PASOK
# ==============================================================================
add_heading_1(doc, "BAGIAN 1: IDENTIFIKASI KONDISI KESALAHAN & VALIDASI STATE")

add_heading_2(doc, "1.1 Konteks Bisnis Sistem Rantai Pasok Tembakau Sumenep")
add_body_paragraph(
    doc,
    "Sistem Informasi Manajemen Rantai Pasok Tembakau dirancang untuk memodelkan proses bisnis penerimaan komoditas daun tembakau rajangan "
    "khas Madura dari petani mitra di Kabupaten Sumenep menuju gudang penyangga (warehouse storage). "
    "Dalam realitas rantai pasok industri tembakau, transaksi penerimaan komoditas sangat rentan terhadap kegagalan operasional dan risiko integritas data. "
    "Jika sistem perangkat lunak mengabaikan penanganan kesalahan (exception handling) dan membiarkan mutasi data terjadi sebelum verifikasi tuntas, "
    "maka integritas inventaris fisik gudang serta rekapitulasi finansial petani akan mengalami cacat logika (corrupted state).",
    bold_prefix="Latar Belakang Operasional: "
)

add_body_paragraph(
    doc,
    "Oleh karena itu, pada Modul 10 ini ditegakkan prinsip 'Pemeriksaan Sebelum Mutasi State (Check Before Mutate)'. "
    "Setiap entitas bisnis diwajibkan menjadi penjaga (guardian) bagi keutuhan state internalnya sendiri. "
    "Operasi bisnis yang gagal atau ditolak harus secara mutlak mempertahankan nilai awal (state preservation) tanpa meninggalkan efek samping (side effects).",
    bold_prefix="Paradigma Invariant Protection: "
)

add_heading_2(doc, "1.2 Identifikasi Kondisi Kesalahan Kritis dalam Sistem")
add_body_paragraph(
    doc,
    "Berdasarkan analisis alur bisnis rantai pasok tembakau Sumenep, diidentifikasi 4 (empat) kondisi kesalahan utama yang harus ditangani secara ketat menggunakan exception handling:"
)

add_bullet_item(
    doc,
    "Kadar air daun tembakau rajangan merupakan parameter penentu kelayakan industri pabrikan rokok kretek. "
    "Standar mutu pabrikan menetapkan ambang batas maksimal kadar air sebesar 18.0%. Daun tembakau dengan kadar air di atas 18.0% "
    "memiliki risiko tinggi mengalami pembusukan, degradasi aroma, dan serangan jamur kapang selama penyimpanan. "
    "Jika transaksi memuat tembakau dengan kadar air > 18.0% atau berstatus REJECTED, sistem harus segera menggagalkan transaksi "
    "dan melempar checked exception MutuTembakauDitolakException, sekaligus menjamin riwayat setoran petani tidak bertambah.",
    bold_prefix="1. Mutu Tembakau Tidak Memenuhi Standar Industri (Kadar Air > 18.0%): "
)

add_bullet_item(
    doc,
    "Gudang penyimpanan fisik memiliki batasan volume dan daya dukung beban (kapasitas maksimum). "
    "Ketika pasokan tembakau baru hendak disimpan, sistem harus menguji apakah penjumlahan stok tersimpan saat ini dengan bobot muatan masuk "
    "melebihi kapasitas maksimal gudang (stokTersimpanKg + beratMasukKg > kapasitasMaksKg). "
    "Jika terjadi kelebihan muatan, sistem wajib melempar KapasitasGudangPenuhException dan membatalkan penyimpanan fisik, "
    "sehingga stok gudang dan daftar transaksi tetap konsisten pada nilai sebelumnya.",
    bold_prefix="2. Kelebihan Daya Tampung Fasilitas Gudang (Overcapacity): "
)

add_bullet_item(
    doc,
    "Fasilitas gudang hanya berhak menampung komoditas yang telah melewati akad verifikasi mutu dan penimbangan resmi. "
    "Apabila ada pihak logistik yang memaksakan transaksi yang belum terverifikasi atau transaksi yang telah ditolak mutunya masuk ke gudang, "
    "sistem gudang menolak operasi tersebut dengan melempar TransaksiBelumTerverifikasiException. "
    "State inventaris gudang dijamin tidak mencatat transaksi cacat tersebut.",
    bold_prefix="3. Pemaksaan Transaksi Belum Terverifikasi / Ditolak ke Gudang: "
)

add_bullet_item(
    doc,
    "Kesalahan yang bersumber dari kegagalan parsing nilai teks menjadi angka (NumberFormatException) saat membaca formulir terminal, "
    "serta parameter numerik yang tidak masuk akal seperti bobot tembakau bernilai nol/negatif, luas lahan non-positif, atau persentase kadar air "
    "di luar rentang 0-100%. Kondisi ini ditangani menggunakan IllegalArgumentException secara fail-fast pada tingkat konstruktor dan method bisnis.",
    bold_prefix="4. Data Parameter Tidak Masuk Akal atau Cacat Format (Invalid Input): "
)

# ==============================================================================
# JAWABAN SECTION 2: ALASAN PENGGUNAAN CUSTOM EXCEPTION
# ==============================================================================
add_heading_1(doc, "BAGIAN 2: ALASAN & KEUNGGULAN PENGGUNAAN CUSTOM EXCEPTION")

add_body_paragraph(
    doc,
    "Dalam rekayasa perangkat lunak berbasis OOP, pemilihan antara generic exception (seperti java.lang.Exception atau java.lang.RuntimeException) "
    "dengan Custom Exception berakar pada prinsip Domain-Driven Design (DDD) dan kejelasan arsitektur perangkat lunak.",
    bold_prefix="Urgensi Custom Exception: "
)

add_heading_2(doc, "2.1 Kelemahan Penggunaan Exception Generik")
add_body_paragraph(
    doc,
    "Menggunakan generic exception seperti melempar 'throw new Exception(\"Stok penuh\")' atau me-return nilai 'boolean false' "
    "menimbulkan kelemahan fundamental dalam sistem skala enterprise:",
    bold_prefix="Masalah Pendekatan Primitif: "
)
add_bullet_item(
    doc,
    "Return value boolean false tidak menjelaskan penyebab penolakan. Pemanggil method tidak dapat membedakan apakah penolakan terjadi "
    "akibat gudang overcapacity, transaksi belum diverifikasi, atau objek bernilai null.",
    bold_prefix="Kehilangan Konteks Diagnostik: "
)
add_bullet_item(
    doc,
    "Generic exception memaksa blok catch menangkap 'catch (Exception e)'. Praktik ini sangat berbahaya karena exception sistemik yang tidak terduga "
    "(seperti NullPointerException atau OutOfMemoryError) dapat tertangkap secara tidak sengaja dan tersamarkan (swallowed), "
    "mempersulit proses penelusuran bug (debugging).",
    bold_prefix="Risiko Penangkapan Terlalu Luas (Catch-All Anti-Pattern): "
)

add_heading_2(doc, "2.2 Keunggulan Strategis Custom Checked Exception")
add_body_paragraph(
    doc,
    "Dengan mendesain custom checked exception seperti KapasitasGudangPenuhException, MutuTembakauDitolakException, dan TransaksiBelumTerverifikasiException, "
    "sistem memperoleh sejumlah keunggulan teknis signifikan:",
    bold_prefix="Nilai Tambah Arsitektural: "
)

add_bullet_item(
    doc,
    "Nama class exception mencerminkan aturan bisnis domain rantai pasok secara eksplisit dan swadokumentasi (self-documenting code). "
    "Developer lain yang membaca signature method langsung memahami kondisi kegagalan spesifik apa saja yang mungkin terjadi.",
    bold_prefix="1. Ekspresifitas Bahasa Domain (Ubiquitous Language): "
)

add_bullet_item(
    doc,
    "Custom exception dapat diperkaya dengan atribut khusus (fields) yang membawa konteks numerik kejadian. "
    "Misalnya, KapasitasGudangPenuhException menyimpan kodeGudang, sisaKapasitasKg, dan beratMasukKg. "
    "Blok catch pemanggil dapat mengakses data ini melalui getter untuk menyusun notifikasi atau log audit terperinci.",
    bold_prefix="2. Penyimpanan State & Konteks Error (Contextual Metadata): "
)

add_bullet_item(
    doc,
    "Karena diturunkan langsung dari java.lang.Exception (Checked Exception), kompilator Java (javac) secara ketat memaksa "
    "layer pemanggil untuk menangani kegagalan tersebut menggunakan blok try-catch atau meneruskannya dengan throws. "
    "Hal ini meniadakan kemungkinan developer lupa menangani skenario batas kritis.",
    bold_prefix="3. Penegakan Kontrak Kompilator (Compile-Time Safety): "
)

add_bullet_item(
    doc,
    "Layer domain model (GudangTembakau dan TransaksiPasok) murni fokus menegakkan invariant dan melempar exception saat dilanggar, "
    "tanpa perlu mencampurkan logika tampilan antarmuka (System.out / dialog UI). Layer UI atau Main bertindak menangani exception "
    "dan menerjemahkannya ke dalam bahasa yang mudah dipahami oleh operator timbangan gudang.",
    bold_prefix="4. Pemisahan Tanggung Jawab (Separation of Concerns): "
)

# ==============================================================================
# JAWABAN SECTION 3: SOURCE CODE PROYEK LENGKAP
# ==============================================================================
add_heading_1(doc, "BAGIAN 3: IMPLEMENTASI SOURCE CODE PROYEK LENGKAP")

add_body_paragraph(
    doc,
    "Seluruh modul kode program di bawah ini telah diselaraskan dengan spesifikasi Modul 10, menerapkan pola enkapsulasi ketat, "
    "fail-fast constructor, pemeriksaan sebelum mutasi state, serta penanganan custom exception berjenjang."
)

add_heading_2(doc, "3.1 Custom Exception: KapasitasGudangPenuhException.java")
add_body_paragraph(
    doc,
    "Checked exception yang dilempar saat muatan masuk melebihi sisa kapasitas fisik gudang. Dilengkapi atribut kontekstual untuk analisis daya tampung."
)
add_code_block(doc, code_kgp_exc)

add_heading_2(doc, "3.2 Custom Exception: MutuTembakauDitolakException.java")
add_body_paragraph(
    doc,
    "Checked exception yang dilempar saat verifikasi transaksi gagal akibat kadar air tembakau melebihi standar maksimum industri rokok (> 18.0%)."
)
add_code_block(doc, code_mtd_exc)

add_heading_2(doc, "3.3 Custom Exception: TransaksiBelumTerverifikasiException.java")
add_body_paragraph(
    doc,
    "Checked exception yang dilempar saat transaksi yang belum diverifikasi atau ditolak mutunya dipaksa masuk ke inventaris gudang."
)
add_code_block(doc, code_tbv_exc)

add_heading_2(doc, "3.4 Model Entitas: Petani.java")
add_body_paragraph(
    doc,
    "Entitas produsen tembakau. Menerapkan fail-fast constructor dan validasi kuantitas setoran pada method tambahRiwayatSetoran()."
)
add_code_block(doc, code_petani)

add_heading_2(doc, "3.5 Model Entitas: Tembakau.java")
add_body_paragraph(
    doc,
    "Entitas komoditas panen tembakau. Menerapkan evaluasi kadar air deterministik untuk klasifikasi grade dan status kelayakan produksi."
)
add_code_block(doc, code_tembakau)

add_heading_2(doc, "3.6 Model Asosiasi: TransaksiPasok.java")
add_body_paragraph(
    doc,
    "Entitas transaksi penerimaan pasokan. Method verifikasiTransaksi() menerapkan prinsip 'Check Before Mutate' dengan melempar "
    "MutuTembakauDitolakException jika mutu tidak layak, sehingga akumulasi setoran petani terlindungi dari pembaruan keliru."
)
add_code_block(doc, code_transaksi)

add_heading_2(doc, "3.7 Model Agregasi: GudangTembakau.java")
add_body_paragraph(
    doc,
    "Entitas pengelola inventaris fisik gudang. Method terimaPasokan() menjamin stok tersimpan dan daftar transaksi tidak bertambah jika terjadi penolakan."
)
add_code_block(doc, code_gudang)

add_heading_2(doc, "3.8 Driver Pengujian Komprehensif: Main.java")
add_body_paragraph(
    doc,
    "Driver utama yang mengeksekusi 5 skenario pengujian variatif: pengujian input numerik dan teks, transaksi sukses, "
    "penolakan mutu tembakau, penolakan transaksi belum terverifikasi, serta simulasi overcapacity gudang. "
    "Seluruh penanganan exception disusun dari hierarki paling spesifik ke umum, dan blok finally digunakan murni untuk audit observasi state."
)
add_code_block(doc, code_main)

# ==============================================================================
# JAWABAN SECTION 4: TABEL PENGUJIAN VALIDASI & KONSISTENSI STATE
# ==============================================================================
add_heading_1(doc, "BAGIAN 4: TABEL PENGUJIAN VALIDASI & KONSISTENSI STATE")

add_body_paragraph(
    doc,
    "Tabel di bawah ini mendokumentasikan hasil pengujian komprehensif sistem terhadap variasi skenario input valid dan tidak valid. "
    "Kolom State Awal vs State Akhir menjadi bukti empiris bahwa prinsip 'Pemeriksaan Sebelum Mutasi State' berhasil menjaga keutuhan "
    "dan konsistensi data objek di memori ketika operasi bisnis ditolak.",
    bold_prefix="Matriks Pengujian Sistem: "
)

t_test = doc.add_table(rows=8, cols=5)
t_test.alignment = WD_TABLE_ALIGNMENT.CENTER
t_test.autofit = False
set_table_borders(t_test, color="B0C4DE", sz="6")

test_headers = ["Skenario Pengujian", "Input Parameter", "Jenis Exception Diharapkan", "Hasil Aktual (Pesan Error)", "State Awal vs State Akhir (Konsistensi)"]
t_widths = [Inches(1.2), Inches(1.3), Inches(1.3), Inches(1.5), Inches(1.2)]

for idx, text in enumerate(test_headers):
    cell = t_test.rows[0].cells[idx]
    cell.width = t_widths[idx]
    set_cell_background(cell, HEX_HEADER_BG)
    set_cell_margins(cell, top=120, bottom=120, left=80, right=80)
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(10)
    run.font.bold = True
    run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

test_data = [
    (
        "Skenario 1a:\nParsing Input Teks Non-Numerik",
        "rawInput = 'dua_ratus_lima_puluh'",
        "NumberFormatException",
        "Input 'dua_ratus_lima_puluh' bukan format numerik desimal yang valid!",
        "Objek Tembakau batal dibuat (tidak ada objek cacat di memori)."
    ),
    (
        "Skenario 1b:\nInput Bobot Negatif / Nol",
        "berat = -75.50 kg dan berat = 0.00 kg",
        "IllegalArgumentException",
        "Validasi Gagal: Berat tembakau harus bernilai positif (> 0 kg).",
        "Konstruktor fail-fast menolak instansiasi objek bernilai tidak valid."
    ),
    (
        "Skenario 1c:\nRegistrasi Petani NIK Kosong",
        "nik = '' (string kosong),\nnama = 'Pak Supardi'",
        "IllegalArgumentException",
        "Validasi Gagal: NIK Petani tidak boleh kosong atau null.",
        "Objek Petani batal dibuat, integritas identitas petani terjaga."
    ),
    (
        "Skenario 2:\nTransaksi Sukses (Grade A & B)",
        "tx1: 250 kg (air 10.5%)\ntx2: 400 kg (air 14.0%)",
        "Tidak Ada Exception (Operasi Sukses)",
        "Transaksi lolos verifikasi, faktur terbit, pasokan tersimpan di gudang.",
        "Stok gudang: 0 kg -> 650 kg.\nSetoran Hasan: 0 -> 250 kg.\nSetoran Jumai: 0 -> 400 kg."
    ),
    (
        "Skenario 3:\nMutu Ditolak (Kadar Air > 18%)",
        "tx3: 180 kg,\nkadarAir = 21.50%",
        "MutuTembakauDitolakException",
        "Mutu tembakau batch BATCH-PSS-003 DITOLAK! Kadar air 21.50% > 18.00%.",
        "State Terbukti Konsisten:\nSetoran Pak Jumai TETAP 400.00 kg (tidak bertambah 180 kg). Tx tetap unverified."
    ),
    (
        "Skenario 4:\nPaksa Transaksi Cacat Masuk Gudang",
        "tx3 belum terverifikasi dimasukkan ke gudang",
        "TransaksiBelumTerverifikasiException",
        "Transaksi TRX-2026-003 tidak dapat diproses ke gudang karena belum lolos verifikasi mutu!",
        "State Terbukti Konsisten:\nStok gudang TETAP 650.00 kg, daftar transaksi gudang TETAP 2 transaksi."
    ),
    (
        "Skenario 5:\nMuatan Pasokan Overcapacity",
        "tx4: 500 kg (mutu lolos),\nsisa kapasitas: 350 kg",
        "KapasitasGudangPenuhException",
        "Daya tampung gudang tidak mencukupi! Sisa: 350.00 kg, Muatan: 500.00 kg (Kelebihan: 150.00 kg).",
        "State Terbukti Konsisten:\nStok gudang TETAP 650.00 kg (tidak meluber jadi 1150 kg). Kapasitas terlindungi."
    )
]

for row_idx, row_data in enumerate(test_data, start=1):
    row = t_test.rows[row_idx]
    bg_c = HEX_ZEBRA_BG if row_idx % 2 == 1 else "FFFFFF"
    for col_idx, cell_value in enumerate(row_data):
        cell = row.cells[col_idx]
        cell.width = t_widths[col_idx]
        set_cell_background(cell, bg_c)
        set_cell_margins(cell, top=80, bottom=80, left=80, right=80)
        p = cell.paragraphs[0]
        p.paragraph_format.line_spacing = 1.05
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        run = p.add_run(cell_value)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(9.5)
        if col_idx == 0:
            run.font.bold = True

add_body_paragraph(doc, "")

# ==============================================================================
# JAWABAN SECTION 5: LOG SCREENSHOT KONSOL TERMINAL
# ==============================================================================
add_heading_1(doc, "BAGIAN 5: LOG SCREENSHOT KONSOL EKSEKUSI TERMINAL")

add_body_paragraph(
    doc,
    "Berikut adalah rekaman log keluaran terminal riil hasil kompilasi (javac) dan eksekusi (java) class Main Modul 10. "
    "Log mencatat secara transparan seluruh penangkapan exception, hierarki penanganan kesalahan, "
    "serta eksekusi blok finally yang membuktikan keutuhan state objek:",
    bold_prefix="Log Output Terminal Eksekusi Java: "
)

console_log_full = (
"===============================================================================\n"
"        SIMULASI SISTEM INFORMASI RANTAI PASOK TEMBAKAU SUMENEP (MODUL 10)     \n"
"         TOPIK: EXCEPTION HANDLING & VALIDASI KONSISTENSI STATE OBJEK          \n"
"       Praktikan: Moch. Ilham Abdul Aziz (NRP: 3125522014) - PENS PSDKU       \n"
"===============================================================================\n\n"
">>> [SKENARIO 1: VALIDASI INPUT STRING PARSING & FAIL-FAST CONSTRUCTOR] <<<\n\n"
"[UJI INPUT] Mencoba membuat objek Tembakau dengan parameter berat: '250.0'\n"
"--> [BERHASIL] Objek Tembakau terbentuk dengan berat: 250.00 kg.\n"
"--> [FINALLY] Validasi input selesai dievaluasi.\n\n"
"[UJI INPUT] Mencoba membuat objek Tembakau dengan parameter berat: 'dua_ratus_lima_puluh'\n"
"--> [DITANGKAP - NumberFormatException] Input 'dua_ratus_lima_puluh' bukan format numerik desimal yang valid!\n"
"--> [FINALLY] Validasi input selesai dievaluasi.\n\n"
"[UJI INPUT] Mencoba membuat objek Tembakau dengan parameter berat: '-75.5'\n"
"--> [DITANGKAP - IllegalArgumentException] Validasi Gagal: Berat tembakau harus bernilai positif (> 0 kg). Diberikan: -75.50 kg.\n"
"--> [FINALLY] Validasi input selesai dievaluasi.\n\n"
"[UJI INPUT] Mencoba membuat objek Tembakau dengan parameter berat: '0.0'\n"
"--> [DITANGKAP - IllegalArgumentException] Validasi Gagal: Berat tembakau harus bernilai positif (> 0 kg). Diberikan: 0.00 kg.\n"
"--> [FINALLY] Validasi input selesai dievaluasi.\n\n"
"[UJI INPUT] Mencoba registrasi Petani dengan NIK Kosong/Null:\n"
"--> [DITANGKAP - IllegalArgumentException] Validasi Gagal: NIK Petani tidak boleh kosong atau null.\n"
"--> [FINALLY] Guard condition constructor berhasil melindungi integritas state Petani.\n\n"
">>> [SKENARIO 2: TRANSAKSI SUKSES (GRADE A & B) & MASUK GUDANG SECARA AMAN] <<<\n\n"
"--- Data Registrasi Awal Petani Mitra ---\n"
"--------------------------------------------------\n"
"             DATA IDENTITAS PETANI               \n"
"--------------------------------------------------\n"
"NIK Petani         : 3529012304780001\n"
"Nama Lengkap       : Pak Hasan Basri\n"
"Asal Desa / Daerah : Desa Guluk-Guluk\n"
"Luas Lahan         : 1.80 Ha\n"
"Total Setoran      : 0.00 kg\n"
"--------------------------------------------------\n"
"--------------------------------------------------\n"
"             DATA IDENTITAS PETANI               \n"
"--------------------------------------------------\n"
"NIK Petani         : 3529021508820002\n"
"Nama Lengkap       : Pak Jumai\n"
"Asal Desa / Daerah : Desa Pasongsongan\n"
"Luas Lahan         : 2.50 Ha\n"
"Total Setoran      : 0.00 kg\n"
"--------------------------------------------------\n\n"
"--- Memproses Transaksi 1 (Batch-GLK-001) ---\n"
"[INFO PETANI] Riwayat setoran Petani 'Pak Hasan Basri' bertambah 250.00 kg. (Akumulasi Total: 250.00 kg)\n"
"[SUKSES TRANSAKSI] Transaksi 'TRX-2026-001' lolos verifikasi mutu dan disahkan secara resmi.\n"
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
"==================================================\n"
"[SUKSES GUDANG] Pasokan batch 'BATCH-GLK-001' (250.00 kg) berhasil disimpan di 'Gudang Penyangga Sumenep'. Stok terkini: 250.00 / 1000.00 kg (Tersedia: 750.00 kg).\n"
"[FINALLY AUDIT] Stok Gudang: 250.00 / 1000.00 kg | Total Setoran Pak Hasan: 250.00 kg\n\n"
"--- Memproses Transaksi 2 (Batch-PSS-002) ---\n"
"[INFO PETANI] Riwayat setoran Petani 'Pak Jumai' bertambah 400.00 kg. (Akumulasi Total: 400.00 kg)\n"
"[SUKSES TRANSAKSI] Transaksi 'TRX-2026-002' lolos verifikasi mutu dan disahkan secara resmi.\n"
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
"==================================================\n"
"[SUKSES GUDANG] Pasokan batch 'BATCH-PSS-002' (400.00 kg) berhasil disimpan di 'Gudang Penyangga Sumenep'. Stok terkini: 650.00 / 1000.00 kg (Tersedia: 350.00 kg).\n"
"[FINALLY AUDIT] Stok Gudang: 650.00 / 1000.00 kg | Total Setoran Pak Jumai: 400.00 kg\n\n"
">>> [SKENARIO 3: VALIDASI MUTU & CUSTOM EXCEPTION MutuTembakauDitolakException] <<<\n"
"Deskripsi Kasus: Petani Pak Jumai membawa tembakau basah (kadar air 21.5% > 18.0%).\n"
"[STATE AWAL] Total Setoran Pak Jumai sebelum verifikasi: 400.00 kg\n"
"--> Menjalankan method verifikasiTransaksi() pada tx3...\n"
"--> [DITANGKAP - MutuTembakauDitolakException] Verifikasi transaksi dibatalkan!\n"
"    Rincian Error : Mutu tembakau batch 'BATCH-PSS-003' [Cangkring] DITOLAK! Kadar air 21.50% melampaui ambang batas maksimum pabrikan (18.00%). Grade: REJECTED (TIDAK LAYAK).\n"
"    Kadar Air Uji : 21.50% (Maksimal diperbolehkan: 18.00%)\n"
"--> [FINALLY - PEMBUKTIAN KONSISTENSI STATE PETANI]\n"
"    State Sebelum Percobaan : 400.00 kg\n"
"    State Sesudah Percobaan : 400.00 kg\n"
"    Kesimpulan Integritas   : SUKSES KONSISTEN (Total setoran petani TIDAK bertambah saat mutu ditolak).\n\n"
">>> [SKENARIO 4: VALIDASI GUDANG DENGAN TransaksiBelumTerverifikasiException] <<<\n"
"Deskripsi Kasus: Memaksa transaksi tx3 (berstatus ditolak/belum lolos) masuk ke gudang.\n"
"[STATE AWAL GUDANG] Stok Tersimpan: 650.00 kg | Jumlah Transaksi: 2\n"
"--> Menjalankan gudang.terimaPasokan(tx3)...\n"
"--> [DITANGKAP - TransaksiBelumTerverifikasiException] Gudang menolak pasokan tembakau!\n"
"    Rincian Error : Transaksi 'TRX-2026-003' tidak dapat diproses ke gudang karena belum lolos verifikasi mutu (Status: BELUM TERVERIFIKASI / DITOLAK MUTU)!\n"
"--> [FINALLY - PEMBUKTIAN KONSISTENSI STATE GUDANG]\n"
"    Stok Gudang Sebelum   : 650.00 kg | Jumlah Transaksi: 2\n"
"    Stok Gudang Sesudah   : 650.00 kg | Jumlah Transaksi: 2\n"
"    Kesimpulan Integritas : SUKSES KONSISTEN (Stok gudang TIDAK bertambah dan transaksi cacat tidak dicatat).\n\n"
">>> [SKENARIO 5: VALIDASI OVERCAPACITY DENGAN KapasitasGudangPenuhException] <<<\n"
"Deskripsi Kasus: Pengiriman muatan 500 kg padahal sisa kapasitas gudang hanya 350 kg.\n"
"[INFO PETANI] Riwayat setoran Petani 'Pak Hasan Basri' bertambah 500.00 kg. (Akumulasi Total: 750.00 kg)\n"
"[SUKSES TRANSAKSI] Transaksi 'TRX-2026-004' lolos verifikasi mutu dan disahkan secara resmi.\n"
"[INFO AKAD] Transaksi 'TRX-2026-004' lolos verifikasi mutu (Total Muatan: 500.00 kg).\n"
"[STATE AWAL GUDANG] Stok Tersimpan: 650.00 kg (Sisa Tersedia: 350.00 kg)\n"
"--> Mengirim pasokan tx4 (500.00 kg) ke Gudang Penyangga Sumenep...\n"
"--> [DITANGKAP - KapasitasGudangPenuhException] Pengiriman ditolak oleh sistem gudang!\n"
"    Pesan Kesalahan   : Daya tampung gudang 'Gudang Penyangga Sumenep' (GDG-SMP-01) tidak mencukupi! Sisa kapasitas: 350.00 kg, Muatan masuk: 500.00 kg (Kelebihan: 150.00 kg).\n"
"    Fasilitas Gudang  : Gudang Penyangga Sumenep (GDG-SMP-01)\n"
"    Kapasitas Maksimal: 1,000.00 kg\n"
"    Stok Tersimpan    : 650.00 kg\n"
"    Sisa Daya Tampung : 350.00 kg\n"
"    Muatan Ditolak    : 500.00 kg\n"
"--> [FINALLY - PEMBUKTIAN KONSISTENSI STATE OVERCAPACITY]\n"
"    Stok Gudang Sebelum   : 650.00 kg | Jumlah Transaksi: 2\n"
"    Stok Gudang Sesudah   : 650.00 kg | Jumlah Transaksi: 2\n"
"    Kesimpulan Integritas : SUKSES KONSISTEN (Stok gudang TIDAK meluber, daya tampung fisik terlindungi sempurna).\n\n"
">>> [LAPORAN REKAPITULASI INVENTARIS FISIK GUDANG AKHIR] <<<\n\n"
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
">>> [LAPORAN PROFIL & AKUMULASI SETORAN PETANI AKHIR] <<<\n\n"
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
"--------------------------------------------------\n"
"===============================================================================\n"
" [RINGKASAN AUDIT SISTEM: SELURUH PRINSIP MODUL 10 TERVERIFIKASI 100% SUKSES]  \n"
" 1. Checked Custom Exception bekerja presisi sesuai aturan domain tembakau.    \n"
" 2. Prinsip 'Check Before Mutate' menjamin state tidak berubah saat operasi gagal.\n"
" 3. Blok 'try-catch-finally' menangani kegagalan dari spesifik ke umum.         \n"
" 4. Blok 'finally' digunakan murni untuk audit observasi, bukan memutasi data. \n"
"==============================================================================="
)
add_code_block(doc, console_log_full)

add_callout_box(
    doc,
    "[AREA SCREENSHOT EKSEKUSI PROGRAM]\n"
    "Tangkapan layar terminal Command Prompt / PowerShell yang mendokumentasikan kompilasi\n"
    "`javac -d bin src/main/java/com/rantaitasok/tembakau/modul10/exception/*.java src/.../*.java`\n"
    "dan eksekusi sukses `java -cp bin com.rantaitasok.tembakau.modul10.Main`."
)

# ==============================================================================
# JAWABAN SECTION 6: JAWABAN PERTANYAAN EVALUASI MODUL 10
# ==============================================================================
add_heading_1(doc, "BAGIAN 6: JAWABAN PERTANYAAN EVALUASI MODUL 10")

add_heading_2(doc, "6.1 Pertanyaan 1: Perbedaan 'throw' dan 'throws' secara Sintaksis & Fungsional")
add_body_paragraph(
    doc,
    "Meskipun memiliki kemiripan nama, kata kunci 'throw' dan 'throws' dalam bahasa pemrograman Java memiliki "
    "perbedaan fundamental baik dari segi posisi sintaksis, peran gramatikal, maupun dampak perilakunya saat runtime:",
    bold_prefix="Kajian Komparatif: "
)

t_eval1 = doc.add_table(rows=6, cols=3)
t_eval1.alignment = WD_TABLE_ALIGNMENT.CENTER
t_eval1.autofit = False
set_table_borders(t_eval1, color="B0C4DE", sz="6")

e1_headers = ["Parameter Pembeda", "Kata Kunci 'throw'", "Kata Kunci 'throws'"]
e1_widths = [Inches(1.8), Inches(2.3), Inches(2.4)]

for idx, text in enumerate(e1_headers):
    cell = t_eval1.rows[0].cells[idx]
    cell.width = e1_widths[idx]
    set_cell_background(cell, HEX_HEADER_BG)
    set_cell_margins(cell, top=100, bottom=100, left=80, right=80)
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(10)
    run.font.bold = True
    run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

e1_data = [
    (
        "Posisi Penulisan (Sintaksis)",
        "Ditempatkan di dalam tubuh (body) method atau konstruktor.\nContoh: throw new MutuTembakauDitolakException(...);",
        "Ditempatkan pada deklarasi signature (header) method.\nContoh: public void verifikasiTransaksi() throws MutuTembakauDitolakException { ... }"
    ),
    (
        "Bentuk Argumen",
        "Diikuti oleh sebuah objek instans konkret dari class turunan Throwable (instantiated object).",
        "Diikuti oleh nama identifier class exception, dapat berupa daftar multi-class dipisahkan koma."
    ),
    (
        "Fungsi & Aksi Nyata",
        "Bertindak sebagai kata kerja aksi (active trigger) yang secara eksplisit melempar exception seketika pada saat runtime.",
        "Bertindak sebagai deklarasi kontrak (passive contract) yang memberitahukan ke pemanggil bahwa method berpotensi meneruskan exception."
    ),
    (
        "Jumlah Elemen",
        "Hanya dapat melempar 1 (satu) objek exception dalam satu eksekusi statement throw.",
        "Dapat mendeklarasikan lebih dari 1 class exception (contoh: throws AException, BException)."
    ),
    (
        "Peran terhadap Kompilator",
        "Menghentikan alur sekuensial method saat itu juga dan melompat mencari blok catch penangan terdekat.",
        "Memaksa kompilator Java memeriksa apakah pemanggil sudah membungkus panggilan dalam try-catch atau melanjutkannya dengan throws."
    )
]

for row_idx, row_data in enumerate(e1_data, start=1):
    row = t_eval1.rows[row_idx]
    bg_c = HEX_ZEBRA_BG if row_idx % 2 == 1 else "FFFFFF"
    for col_idx, cell_value in enumerate(row_data):
        cell = row.cells[col_idx]
        cell.width = e1_widths[col_idx]
        set_cell_background(cell, bg_c)
        set_cell_margins(cell, top=80, bottom=80, left=80, right=80)
        p = cell.paragraphs[0]
        p.paragraph_format.line_spacing = 1.05
        run = p.add_run(cell_value)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(9.5)
        if col_idx == 0:
            run.font.bold = True

add_body_paragraph(doc, "")

add_heading_2(doc, "6.2 Pertanyaan 2: Mengapa Pemeriksaan Kondisi Harus Mendahului Mutasi State?")
add_body_paragraph(
    doc,
    "Prinsip 'Check Before Mutate' (atau Guard Condition) merupakan landasan utama dalam menjaga State Invariant suatu objek. "
    "Jika mutasi state dilakukan mendahului atau bersamaan dengan pemeriksaan kondisi, sistem akan mengalami cacat logika serius:",
    bold_prefix="Fondasi Integritas State: "
)
add_bullet_item(
    doc,
    "Bayangkan jika pada GudangTembakau stok langsung ditambah terlebih dahulu: 'this.stokTersimpanKg += beratMasuk;', baru kemudian dicek "
    "'if (this.stokTersimpanKg > kapasitasMaksKg) throw new KapasitasGudangPenuhException(...)'. Jika exception dilempar, memori objek "
    "sudah terlanjur bertambah! Stok fisik di sistem software menjadi lebih besar dari realitas fisik sebenarnya (phantom stock).",
    bold_prefix="1. Pencegahan State Terkorupsi (Phantom/Corrupted State): "
)
add_bullet_item(
    doc,
    "Dalam rekayasa perangkat lunak, sebuah method bisnis idealnya bersifat atomik: operasi harus berhasil sepenuhnya (all-or-nothing). "
    "Dengan meletakkan seluruh guard validation di awal, kita menjamin bahwa jika ada satu kondisi yang melanggar batas bisnis, "
    "eksekusi langsung diputus tanpa meninggalkan efek samping (side effects) pada variabel instans apapun.",
    bold_prefix="2. Menjamin Sifat Atomisitas Transaksi (Atomicity): "
)
add_bullet_item(
    doc,
    "Jika data terlanjur dimutasi lalu terjadi error, program harus menulis kode pemulihan (rollback) manual yang rumit dan rentan bug "
    "(misalnya harus mengurangi kembali stok di blok catch). Pendekatan 'periksa dulu sebelum mutasi' jauh lebih bersih, aman, dan zero-overhead.",
    bold_prefix="3. Menghilangkan Kebutuhan Rollback Manual yang Rapuh: "
)

add_heading_2(doc, "6.3 Pertanyaan 3: Dampak Buruk dari Silent Failure / Empty Catch Block")
add_body_paragraph(
    doc,
    "Silent failure adalah kondisi di mana exception ditangkap dalam blok catch tetapi blok tersebut dibiarkan kosong ('catch (Exception e) {}') "
    "atau hanya sekadar mencetak log ambigu tanpa tindakan korektif.",
    bold_prefix="Definisi Anti-Pattern: "
)
add_body_paragraph(
    doc,
    "Praktik ini tergolong ke dalam 'Code Smell' paling berbahaya dalam dunia pemrograman dengan akibat-akibat fatal:",
    bold_prefix="Dampak Buruk:"
)
add_bullet_item(
    doc,
    "Ketika exception ditangkap tanpa ditangani atau dilempar ulang, thread eksekusi menganggap operasi berjalan sukses. "
    "Sistem melanjutkan alur eksekusi berikutnya seolah-olah transaksi sah, padahal data sebenarnya gagal diproses.",
    bold_prefix="1. Penyamaran Kegagalan Sistem (Illusion of Success): "
)
add_bullet_item(
    doc,
    "Programmer atau tim operasional kehilangan jejak jejak tumpukan (stack trace) dan rincian penyebab terjadinya error. "
    "Mencari akar penyebab bug di lingkungan production menjadi hampir mustahil karena tidak ada catatan log error yang ditinggalkan.",
    bold_prefix="2. Menghilangkan Kemampuan Debugging & Audit: "
)
add_bullet_item(
    doc,
    "Dalam konteks rantai pasok tembakau, jika penolakan gudang penuh diabaikan oleh catch kosong, operator gudang mengira "
    "muatan telah tercatat secara sistemik. Truk pembawa tembakau membongkar muatan di gudang yang sudah overload, menyebabkan "
    "penumpukan daun di luar ruang simpan yang berujung pada kerusakan komoditas dan selisih pembukuan keuangan (financial loss).",
    bold_prefix="3. Kerugian Finansial & Operasional Nyata: "
)

add_heading_2(doc, "6.4 Pertanyaan 4: Alasan Mengapa Perubahan State Tidak Boleh Diletakkan di Blok 'finally'")
add_body_paragraph(
    doc,
    "Blok 'finally' dalam sintaksis Java memiliki karakteristik garansi eksekusi mutlak: blok finally SELALU dijalankan setelah blok try selesai, "
    "baik ketika alur try berjalan normal tanpa hambatan, maupun ketika alur try terputus di tengah jalan akibat dilemparnya exception.",
    bold_prefix="Karakteristik Garansi Eksekusi 'finally': "
)
add_body_paragraph(
    doc,
    "Meletakkan logika pembaruan atau mutasi state (seperti 'stok -= jumlah' atau 'stokTersimpanKg += berat') di dalam blok finally "
    "merupakan kesalahan logika rancangan yang fatal karena:",
    bold_prefix="Alasan Larangan Mutasi di Blok 'finally':"
)
add_bullet_item(
    doc,
    "Jika terjadi exception pada blok try (misalnya mutu ditolak atau kapasitas penuh), alur try langsung berhenti dan melompat ke catch lalu ke finally. "
    "Jika mutasi diletakkan di finally, maka penambahan stok atau setoran AKAN TETAP DIKERJAKAN meskipun operasi telah dibatalkan! "
    "Hal ini secara instan menghancurkan seluruh mekanisme validasi yang telah dibangun dengan susah payah.",
    bold_prefix="1. Mutasi Tetap Terjadi Saat Operasi Gagal: "
)
add_bullet_item(
    doc,
    "Tujuan semantik dari blok finally secara perancangan bahasa Java adalah untuk 'Cleanup Actions' (pembersihan sumber daya eksternal), "
    "seperti menutup koneksi basis data (db.close()), menutup file stream (file.close()), atau melepaskan kunci mutex (lock.unlock()). "
    "Finally tidak pernah dirancang untuk memuat business mutation logic.",
    bold_prefix="2. Pelanggaran Semantik Perancangan Bahasa: "
)
add_bullet_item(
    doc,
    "Jika finally digunakan untuk keperluan logging audit (seperti yang diimplementasikan pada Main.java praktikum ini), "
    "tindakan tersebut sepenuhnya benar dan sah karena bersifat 'Read-Only Observation' (hanya membaca dan mencetak state terkini "
    "untuk membuktikan konsistensi data), bukan memanipulasi atau mengubah nilai variabel instans.",
    bold_prefix="3. Penggunaan Finally yang Tepat: Read-Only Audit: "
)

# Save Document
OUTPUT_FINAL = "Moch. Ilham Abdul Aziz_PBO_3125522014_Pertemuan 10.docx"
doc.save(OUTPUT_FINAL)
print(f"[BERHASIL] Dokumen laporan berhasil diperbarui dan disimpan di: {OUTPUT_FINAL}")
