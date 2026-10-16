# Native PIN ve gizleme: entegrasyon kapsamı

**0.14.19 güncellemesi:** Native modele categoryId ve parentLocalId eklendi; yeni Xtream/M3U ayrıştırmaları ve v3 kayıt bunları korur. V1/v2 kayıtlarda bilinmeyen alanlar boş/null kalır. Aşağıdaki alan yokluğu tespitleri 0.14.18 başlangıç durumudur. M3U ad tabanlı kategori kapsamı, eski bölüm ebeveynini doğrulama ve gerçek PIN/erişim motoru halen açık. [Kayıt sözleşmesi](CATALOG_CACHE.md).

15 Ekim 2026 · SAFE01 / BKL-028 · Uygulama öncesi sözleşme taslağı. Bu belge uygulamada PIN koruması bulunduğunu göstermez. Üretim sürümü 0.14.18 değişmedi.

## Doğrulanan başlangıç

- PRD SAFE01 kaynak/kategori/kanal kilidi ile gizlemeyi ayrı ister. Arama, favori, geçmiş ve doğrudan erişim aynı denetimi kullanmalıdır.
- TV-071/F06 ilk rakam odağı, doğru PIN sonrası bekleyen hedef, Back/iptal sonrası erişim açılmaması davranışını tanımlar.
- `prototypes/tv-flow/flows.js` sabit 1234 ve bellekte unlocked kümesi kullanır. Bunlar yalnız örnek akıştır; üretim doğrulayıcısı veya saklama tasarımı değildir.
- `SettingsScreen.kt` PIN yönetiminin bulunmadığını açıkça bildirir. Bu açıklama ancak uçtan uca koruma devreye alınıp doğrulandığında değiştirilmelidir.
- `open` ve `play` yalnız adult/kaynak varlığı kontrol eder. `TitleIndex`, `homeLibrary`, kütüphane ve rehber ayrı adult filtreleri uygular. Bu filtrelerin varlığı SAFE01'i sağlamaz.

## Kimlik ve karar modeli

Kaynak kapsamı sourceId, tek içerik kapsamı mevcut ContentRef(sourceId, localId) ile tutulmalı. Görünen başlık, URL veya sağlayıcının yalnız sayısal kimliği kural anahtarı olmamalı; farklı kaynaklarda aynı içerik kimliği mümkündür.

Kategori kapsamı en az sourceId + MediaKind + kategori kimliği gerektirir. Mevcut MediaEntry yalnız kategori adını taşır; kalıcı sağlayıcı category_id alanı yoktur. Uygulamadan önce veri yolu kararı verilmelidir: Xtream category_id taşınacak mı, eski önbellek ve M3U ad tabanlı kategoriler nasıl ele alınacak? Ad değişince kuralın aynı kategoriyi takip ettiği iddia edilemez. İsim normalleştirmesiyle farklı kategoriler sessizce birleştirilmemeli. Bu açık karar çözülmeden kategori koruması tamamlandı sayılamaz.

Önerilen bağımsız durumlar: görünürlük (göster/gizle) ve erişim (serbest/PIN gerekir). Gizleme kaydı silmez; favori/geçmiş/ilerleme verisini başka kimliğe taşımaz. Kaynak, kategori ve içerikten herhangi birinin gizleme kuralı varsa normal gezinmede gizli kalır; herhangi birinin kilidi varsa erişim PIN ister. Bir serbest içerik kuralı üst kapsamın kilidini kaldırmamalı. Mevcut adult yasağı aynen korunur; bu iş yetişkin içeriğini açma özelliği değildir.

Bölümler kendi scoped kimliğinin yanında ait oldukları dizinin erişim kuralını devralmalı. Mevcut modelde parentId her MediaEntry üzerinde yok; WatchRecord.parentId ve seçili dizi/bölüm listesi üzerinden bağlam var. Rastgele seçili diziyi ebeveyn kabul etmek yerine aynı kaynak ve doğrulanmış bölüm üyeliği gerekir. Yetki bağlamı eksik eski bir bölüm kaydı doğrudan oynatılmamalı; önce dizi bağlamı yeniden çözülmeli.

## Giriş noktaları ve gerekli davranış

| Mevcut yer | Entegrasyon gereği | Kanıtlayacak kontrol |
| --- | --- | --- |
| TvViewModel.publishLibrary / SourceLibrary.combine | Ham katalog korunur; görünür katalog/projeksiyon aynı kurallardan üretilir | Gizleme değişince bütün yüzeyler birlikte yenilenir |
| HomeScreen → homeLibrary | Hero, raf, favori, devam ve son kanallarda gizli içerik adı/afişi görünmez | Kaynak/kategori/tek kayıt gizleme, eski geçmiş dahil |
| PilotApp.Library | Kategori seçenekleri ve içerik sayıları görünür veriyle uyumlu | Boş kategori/filtre, odak kurtarma |
| TitleIndex / updateSearch / updateLibrarySearch | Gizli kayıt sonuçlarda ve sayılarda bulunmaz; politika değişince eski sorgu sonucu yayımlanamaz | Sorgu çalışırken gizleme, kaynak ayrımı |
| GuideScreen / guideRows | Gizli kanal ve program adları normal rehberde gösterilmez | Kanal satırı/şimdi-sonra ve doğrudan program seçimi |
| open / retryDetail / loadDetail | Detay isteğinden önce erişim kontrolü; asenkron yanıt sonrası politika yeniden kontrolü | Kilitliyken metadata isteği açılmaz; iptal sonrası eski yanıt ekran açamaz |
| play | En son erişim kapısı; PlayingMedia/stream URL yayımlanmadan kontrol | Önceden elde edilmiş MediaEntry ile doğrudan çağrı |
| changeChannel | Komşu kanal seçimi gizleme kurallarını kullanır; kilit atlanmaz | Sonraki/önceki kanalda kilit ve iptal |
| nextEpisodeChoice / playNextEpisode | Dizi/bölüm yetkisi yeniden kontrol edilir | Otomatik geri sayım esnasında kilitlenen veya kaldırılan hedef |
| toggleFavorite / recordViewed / saveProgress | Gizleme veri silmez; kısıtlı içerik normal yüzeye tekrar sızmaz | Mevcut favori/ilerleme, kaynak kaldırma ve yeniden ekleme |
| Ayarlar/kural düzenleme | Koruma kaldırma, PIN değiştirme ve gizli kayıt yönetimi yetkili işlem olmalı | Back/iptal/yanlış PIN ayarı değiştirmez |
| Yaşam döngüsü ve restore | Geçici izin arka planda ve süreç yeniden açılışında kapanır | Kilitli oynatma/geri sayım arka plan geçişi |

Kontrol yalnız kartın onClick'ine eklenmemeli. Güncel `play` yoluna ulaşan doğrudan çağrılar ve asenkron devamlar da kapsanmalıdır. UI durumu ile kalıcı politika arasındaki sürüm farkı dikkate alınmalı; bekleyen isteğin hedef/source/politika sürümü değişirse eski işlem yürütülmemeli.

## TV-071 ve bekleyen işlem

Mevcut SourceDialog/PilotDialog, ortak Action, Roboto ve odak düzeni kullanılmalı; ikinci görsel sistem oluşturulmamalı. Rakamlar maskeli gösterilmeli; odak başlangıcı ilk rakam, fiziksel rakam tuşları ve D-pad desteklenmeli. Sil ve İptal ayrı erişilebilir eylemler olmalı. Hata yalnız renkle anlatılmamalı; girilen PIN duyuruya veya günlüğe yazılmamalı.

Bekleyen işlem çalıştırılabilir lambda yerine hedef kimliği, işlem türü ve geri dönüş odağıyla tanımlanmalı. Doğrulama sonrası kaynak/kimlik/politika yeniden kontrol edilmeli; kaybolan hedef açılmamalı. Back hiçbir izin üretmeden kaynak ekrana ve açan kontrole dönmeli. Öneri: başarılı doğrulama yalnız bekleyen hedef için geçici izin verir; tüm kaynağı farkında olmadan açmaz. Otomatik sonraki bölüm yeni hedef olduğundan yeniden denetlenir.

PIN oluşturma iki kez girişle, değiştirme mevcut PIN doğrulamasıyla tasarlanmalı. Varsayılan PIN konmamalı. Doğrulayıcı, deneme sınırı, kalıcı kilit süresi, unutulan PIN kurtarması ve bozuk politika kaydı davranışı uygulamadan önce ayrı güvenlik/saklama tasarımında netleştirilmeli. Sadece şifreli hesap deposu mevcut diye PIN güvenliği tamamlanmış sayılmaz. Bu belge bir kriptografik uygulama seçmez.

## Teslim sırası ve geçiş koşulları

1. Kategori ve bölüm ebeveyn kimliği kararını mevcut cache/migration sözleşmesiyle çöz. Aynı adlı iki kaynak/kategori, eski kayıt ve değişen katalog örnekleriyle test et.
2. UI'dan bağımsız kapsam/effective decision ve politika revizyonunu uygula; gizle/koru çakışmaları ve kaynak ayrımını test et.
3. Kalıcı PIN/politika saklaması ve bozuk kayıt/kurtarma davranışını uygula. Ham PIN veya hesap bilgisini test fixture/ekran/günlüğe koyma.
4. Bütün tablo giriş noktalarını tek erişim kararına bağla; görünürlük projeksiyonunu ve sorgu iptalini tamamla.
5. TV-071 ve kural yönetimini ortak bileşenlerle uygula. Normal/1,5/2,0 yazı, yanlış PIN, Back, kaynak silinmesi ve süreç yeniden açılışını test et.
6. Ayarlardaki “henüz yok” açıklamasını ancak bütün kapsam geçince kaldır. Prototip başarılarını native kanıt diye kullanma; kullanıcı görsel kabulü ve fiziksel TV kapıları ayrı kalır.
