# Koruma durumunun kalıcı deposu

18 Ekim 2026 · SAFE01/BKL-028 · S08–S12 ile UI/erişim yollarına bağlı (D073/D074).

ProtectionSnapshot, tek kayıtta PIN doğrulayıcısı, PinAttempts, kurallar, policyRevision ve generation tutar. ProtectionCodec v2 ikili biçimi yazar, v1 ve v2 okur; v2 kaynak/açılış koruması bayraklarını ekler; 1 MiB, 1024 kural ve her metinde 4096 karakter sınırı vardır. Kaynak/içerik/kategori ve ProviderId/M3uName ayrımı korunur; yinelenen kapsam, bilinmeyen sürüm/tür, eksik/fazla bayt ve geçersiz flag reddedilir. Hata varsayılan serbest politikaya dönüştürülmez.

## Şifreleme ve bütünlük

ProtectionStore, kaynak hesabının `iptv_source_v1` anahtarından bağımsız `iptv_protection_v1_primary` anahtarı kullanır. AES-GCM 12 bayt IV, 128 bit etiket ve namespace bağlı AAD ile çalışır. Kaynak kurtarma işlemi bu anahtarı silmez. Anahtar çıkarılabilir bir uygulama dosyasına yazılmaz; donanım destekli olduğu ayrıca ölçülmeden iddia edilmez. [Android Keystore](https://developer.android.com/privacy-and-security/keystore).

Diskte dosya bulunmaması tek başına ilk kurulum değildir. Anahtar varsa dosya eksikliği Unavailable olur. Dosya varsa anahtar eksikliği de Unavailable olur; okuma otomatik anahtar üretmez. Yalnız ikisi de yoksa Unconfigured döner. Anahtar oluşturulduktan sonra ilk yazma kesilirse güvenli hata durumunda kalır. Okunamayan koruma kaydı kilit ekranına gider. PIN unutma akışı açık uyarı/onayla bütün uygulama verisini sıfırlar; reset() koruma dosyalarını, sonra anahtarı siler. Yarıda kesilirse Unavailable kalır; çağrı tekrarlanabilir. Anahtar ve dosyanın birlikte silinmesi, root veya uygulama verisi sıfırlamasına karşı koruma vaat edilmez.

## Yazma sözleşmesi

AtomicFile öncesinde akış descriptor'ü sync edilir, bitişten sonra diskten şifreli baytlar tekrar okunup yazılanla karşılaştırılır. Başarısız yazı exception verir; çağıran izin üretmemelidir. AtomicFile kendi başına thread kilidi sağlamadığından bütün örnekler aynı süreç içi kilidi kullanır. Çok süreçli kullanım desteklenmez. [Android AtomicFile](https://developer.android.com/reference/android/util/AtomicFile).

Her yazma beklenen generation ile karşılaştırılır ve tam bir artırır; eski yazıcı reddedilir. Kurallar veya PIN değişirse policyRevision da artmalıdır. Yalnız deneme sayacı değişince politika sürümü sabit kalabilir. Bu sayaç tek başına yetki denetimi değildir: çağıran PIN değişikliği/kural düzenlemeyi doğrulamalıdır. Kimliği doğrulanmış eski dosyanın kötü niyetle geri konmasına karşı donanımsal rollback koruması yoktur.

Read sonuçları Unconfigured, Ready ve Unavailable'dır. Dosya/anahtar/şifre/biçim hatası Unavailable olur; kullanıcıya ham hata veya sır yazılmaz. Snapshot ve credential toString çıktıları gizlenmiştir. Geçici ana açık kayıt dizisi ve doğrulayıcı karşılaştırma kopyaları temizlenir; JVM/kriptografi sağlayıcısındaki bütün bellek kopyalarının silindiği garanti edilmez.

## Testler ve kalan bağlantı

Dört JVM testi kapsamları ve PIN/deneme durumunu birlikte roundtrip eder, kesilmiş/fazla/bilinmeyen kayıtları, limitleri ve savunmacı kopyayı kontrol eder. Dört Android testi ayrı test namespace'lerinde gerçek Keystore/AtomicFile kullanır: yeniden oluşturulan store ile okuma ve doğru PIN, eski yazıcı reddi, dosya/anahtar kaybı, şifreli veri değişimi ve başka deponun baytları, engellenmiş `.new` dosyası yüzünden başarısız yazıda eski kaydın korunması, politika revizyonu.

Testler yalnız ayrılmış 5556 emülatöründe çalıştı; primary depo/kaynak hesabı kullanılmadı. Android testleri gerçek süreç öldürme, güç kesilmesi veya fiziksel TV ölçümü değildir. Kalan iş Android boot/elapsed adaptörü ve seri doğrulama kontrolcüsüdür: deneme rezervasyonu durably yazılmadan KDF çalıştırma, başarılı temizleme yazılmadan izin verme, iptal/arka plan/politika değişimi sonrası eski yanıtla erişim açma engellenmelidir. Bütün UI/arama/oynatma yolları bağlanmadan SAFE01 etkinleştirilmez.

S08–S12 güncellemesi: AndroidPinClock, ParentalGuard, ParentalSession ve AccessGate bağlıdır; yukarıdaki “kalan bağlantı” paragrafı ilk temel testinin tarihçesidir. Kaynak/arama/oynatma girişleri ortak kapıyı kullanır. Güç kesilmesi ve fiziksel TV ölçümü hâlâ açık; yeni enstrümantasyon yalnız 5558'de tools/run-device-tests.sh ile koşulur.
