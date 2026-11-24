package id.ac.pens.rantaipasok;
 
/**
 * Akar hierarki kegagalan bisnis. Turunan Exception (bukan
 * RuntimeException) sehingga bersifat CHECKED: kompilator memaksa
 * pemanggil menangani atau mendeklarasikannya dengan throws.
 */
public abstract class DomainRantaiPasokException extends Exception {
    private static final long serialVersionUID = 1L;
 
    private final String kodeKesalahan;
 
    protected DomainRantaiPasokException(String kode, String pesan) {
        super(pesan);
        this.kodeKesalahan = kode;
    }
 
    public String getKodeKesalahan() { return kodeKesalahan; }
}
