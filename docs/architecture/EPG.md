# Yayın rehberi veri sözleşmesi

14 Ekim 2026 · **0.13 native rehber:** XMLTV veri yolu, kumanda ızgarası, canlı kartlar/oynatıcıda şimdi–sonra ve kaynak ayarları bağlıdır. Bir sağlayıcının indirilen metadata uyumluluğu ve eşleşmesi doğrulandı; fiziksel cihaz ve başka sağlayıcılar açıktır. [PRD](../product/PRD.md) EPG01/EPG02 ve [ekran davranışları](../design/SCREENS.md) kapsamı korunur.

## Kaynaktan rehbere

- M3U `tvg-id` ve `tvg-name`, MediaEntry'nin `epgId`/`epgName` alanlarına taşınır. `#EXTM3U` içindeki `x-tvg-url` ve `url-tvg` virgülle ayrılan adresleri destekler; göreli adres liste URL'sine göre çözülür. HTTP/HTTPS kabul edilir, URL içinde basic kullanıcı bilgisi kabul edilmez. İlk dört farklı adres alınır; fazlası için katalog uyarısı vardır.
- Xtream canlı katalogda `epg_channel_id` korunur. XMLTV adresi aynı hesabın `xmltv.php` uç noktasından üretilir; hesap alanları URL oluşturucuyla kodlanır. `get_short_epg` JSON yolu bu aşamada uygulanmadı; bir gerçek kaynağın XMLTV gövdesi Android üretim veri yolunda doğrulandı; başka sağlayıcılar ve doğrudan TV ağ yolu açık.
- Kullanıcıya ait adresler şifreli katalog/kaynak kayıtlarında ve geçici istek planında bulunur. Plan/katalog `toString()` çıktıları adresleri açığa çıkarmaz. Çoklu kaynak birleşiminde rehber adresleri bir havuzda toplanmaz; her işlem kendi ham kaynak snapshot'ından plan üretmelidir.
- CatalogSnapshot sürüm 1'e isteğe bağlı alanlar eklendi. Eski snapshot'lar okunur; rehber alanları boş olur. Eski M3U cache'in rehber bilgisini kazanması için liste yenilenmelidir. Özel XMLTV adresi kaynak düzenleme ve rehber ayarlarından kaydedilir; otomatik adresin önüne geçer.

## Saat ve eksik bilgi

XMLTV'de başlangıç ve kanal zorunlu, bitiş isteğe bağlıdır. Kanal tanımı olmadan program bulunabilir. Sayısal ofset korunarak zaman UTC anına çevrilir; ofsetsiz tarih UTC sayılır. Okuyucu dakika/saniye hassasiyetini, UTC/GMT/Z ve açıkça tanımlı BST'yi destekler. Daha kaba tarihler ve CST gibi belirsiz kısaltmalar tahmin edilmez; geçersiz kayıt sayacına eklenir. [XMLTV DTD](https://raw.githubusercontent.com/XMLTV/xmltv/master/xmltv.dtd).

`GuideWindow.around` seçili saat diliminde dünün başlangıcından, yedi sonraki günün sonuna kadar pencere üretir. Bugün dahildir; DST geçişinde süre sabit 216 saat olmak zorunda değildir. Kayıtlar bu pencereyle kesişiyorsa tutulur. Native ekranlar seçili bölgede gösterir; saat dilimi tercihi cihazda kalır.

Bitişi olmayan program saklanır, fakat “şu anda” sayılmaz; süre ve bitiş uydurulmaz. Boş aralıklar boş kalır. Aynı anda birden fazla program varsa `now()` kesin bir program seçmez. Aynı başlangıçlı iki farklı gelecek kayıtta `next()` de belirsizliği korur. `GuideSlice` sorgu sırasını kullanır ve satır sınırına ulaşınca `truncated` döndürür; ekran bunu tam rehber gibi sunmamalıdır.

## Eşleştirme

1. Dolu `tvg-id`/Xtream EPG kimliği birebir ve büyük/küçük harf duyarlı aranır. Yanlış kimlik varsa adla sessiz düzeltme yapılmaz (`MISSING_ID`).
2. Kimlik boşsa `tvg-name` ve görünen kanal adı, XMLTV'nin bütün adları arasında aranır. NFC, boşluk ve küçük harf normalizasyonu uygulanır; HD/UHD, bölge veya noktalama silinmez. Tek sonuç `NAME`, çelişen sonuçlar `AMBIGUOUS` olur.
3. Farklı feed'lerde aynı kimlik varsa otomatik birleştirme yapılmaz. Kaynaklar arasında eşleştirme yoktur; runtime entry sahibinin plan sahibiyle uyuşması gerekir.

`ID`, `NAME`, `MISSING_ID`, `AMBIGUOUS`, `NONE` ekranın açıklayabileceği ayrı durumlardır. XMLTV tanımları olmayan bir kanal yine programdaki kimlik üzerinden bulunabilir. Elle eşleştirme arayüzü bu aşamada yoktur.

## Okuma ve depolama

SAX ile akıştan okunur; bütün XML ve bütün programlar bellekte biriktirilmez. UTF-8 ve BOM içeren UTF-16 desteklenir. Normal dış DOCTYPE bildirimi ağdan alınmadan kaldırılır; iç DTD/entity tanımları parser'a ulaşmadan reddedilir. UTF-32 ve diğer kodlamalar bu pilotta desteklenmez. XML başlığı 64 Ki karakter, alan metni 16 Ki karakter, derinlik 32 ile sınırlıdır. Bir feed'de 20 bin kanal tanımı ve 1 milyon program sınırı vardır. Ayrıştırma hataları ham URL/yanıt/cause taşımaz; iptal ayrı yayılır. [Android SAXParserFactory](https://developer.android.com/reference/javax/xml/parsers/SAXParserFactory).

OkHttp çağrısı gövde okunurken de iptal edilebilir. Sıkıştırılmış dosya başlığı tanınır; HTTP gzip açıldıysa yeniden açılmaz. Okunan ve açılan akışlarda ayrı 128 MiB sınırı vardır. Feed'ler sırayla alınır; herhangi biri başarısızsa hiçbiri yayımlanmaz. Bu işlem yayın oynatmaz.

Her kaynak için rehber metaverisi uygulamanın özel `files/guides` alanında SQLite indeksidir. Hesap, feed URL'si, yayın URL'si ve görsel URL'si tabloya yazılmaz; kanal kimliği/adı, başlık, açıklama, kategori ve zamanlar bulunur. Metadata ayrıca uygulama düzeyinde şifrelenmez; Android uygulama alanında ve yedeklemeler kapalıdır. Bu tercih D038'de kayıtlıdır. Katalog ve hesap şifreleme kuralları değişmez.

Bir yeni indirme `cache/guide-staging` altında ayrı veritabanına yazılır. SQLite sayfa limiti ana DB için 96 MiB'dir; toplam cihaz, journal, eski+yeni kopya veya bütün kaynaklar için 96 MiB sınırı değildir. Birleşik kayıt sayısı 1 milyondur. SQL sorgusu tek kanalın zaman penceresini getirir; varsayılan en çok 512, API üst sınırı 2.000 programdır.

Başarılı parse sonrasında staging DB kapatılır. `SourceTransactions.shared.write` içinde çağıranın güncel kaynak/ayar bağlamını denetleyen `accept` koşulu ve EpgStore kuşak kontrolü sağlanırsa AtomicFile ile yayımlanır. Kaynak kaldırma veya daha yeni istek eski stage'i geçersiz yapar; farklı EpgStore örnekleri aynı süreç kilidini paylaşır. Dosya kilidini AtomicFile tek başına sağlamaz. [AtomicFile](https://developer.android.com/reference/android/util/AtomicFile).

İptal/bozuk XML/HTTP hatasında eski snapshot kalır. Gerçekten boş geçerli XML boş rehber olarak yayımlanabilir; bütün programları geçersiz bir feed eski sağlam rehberi silemez. Son güncelleme, tutulan zaman penceresi, geçersiz ve pencere dışı kayıt sayısı snapshot'ta saklanır. Son hata ve yenileme durumu ViewModel üzerinden kaynak bilgileri ve rehber ayarlarında gösterilir.

## Native ekran ve yaşam döngüsü

TV-021 ilk açılışta son canlı kanal ve mevcut zamana gider. Dikey kumanda hareketi zaman ankrajını korur; yatay hareket program sınırına, boş aralıkta yarım saat ileri/geri gider. Izgara tek kararlı odak hedefidir; değişken hücre genişlikleri odağı silmez. OK bırakıldığında mevcut mantıksal hedef açılır. Şimdiki program doğrudan canlı yayını, geçmiş/gelecek/eksik/çakışan program ayrıntıyı açar. Ayrıntıdaki **Canlı kanalı izle** her zaman mevcut yayını açar; catch-up yoktur. Odaklanmak oynatma başlatmaz.

Geri: ayrıntı → aynı hücre; ızgara → Rehber menüsü; menü → Ana Sayfa. Oynatıcıdan dönüş kanal/zamanı korur; kanal değiştirilmişse yeni kanal seçilir. Boş katalogda erişilemeyen ızgaraya odak atanmaz. Uzun açıklamalar kumandayla kaydırılır; oynatma/kapatma düğmeleri sabit kalır. Büyük fontta zaman alanı üç saatten iki saate iner, satır ve kanal sütunu genişler. Kesilmiş yoğun rehber sorgusu açıkça belirtilir.

TV-020 görünen canlı kartların şimdi/sonra bilgisini; TV-050 tek oynatıcı üzerinde program, saat ve bilinen süreye göre ilerlemeyi gösterir. Bilinmeyen/çakışan bilgi tahmin edilmez. Görünen ve yakın satırlar kaynak başına tek DB oturumunda IO'da sorgulanır; tüm rehber Compose belleğine alınmaz.

Kaynaklar → Bilgiler → Rehber ayarları: son güncelleme, eski/boş/bozuk rehber, yenileme/iptal ve **Adresi düzenle**. Okuma ekranı klavyeyi açmaz. Özel XMLTV adresi kaynakla birlikte AES-GCM registry'de ve eski tek kaynak biçiminde saklanır; kaydetme katalog dosyasını yeniden indirmez. Boş adres otomatik M3U/Xtream rehberine döner. Yeni kaynak formunda da isteğe bağlı adres bulunur. Şehir/saat dilimi seçimi cihaz tercihi olarak kalır; anlık zaman değiştirilmez. DST geçişinde farklı ofsetler program ayrıntısında birlikte gösterilir.

GuideController ViewModel kapsamındadır. Kaynaklar sırayla yenilenir; altı saat tazelik ve seçili saat diliminin takvim aralığı kontrol edilir. Hata/iptal ardından otomatik istek beş dakika bekler; elle yenileme hemen denenebilir. Cache kontrolü sırasında açılan ekranın indirme talebi kaybolmaz. İptal tamamlanmadan ikinci aynı kaynak işi başlamaz; yeniden çizim iptali geri almaz. Kaldırma/düzenleme/restore eski sahibin işini geçersiz kılar. Publish koşulu SourceTransactions altında güncel şifreli kaynak ve M3U header bağını denetler.

SQLite transaction'ları fiziksel iş parçacığına bağlıdır. İndirme, SAX sink, seal ve iptal rollback/close her yenilemenin tek GuideWriter iş parçacığında yürür; dispatcher sonunda kapatılır. Ağ çağrısını iptal eden gözlemci ayrı IO yürütmesindedir, bloklayan okumayı beklemez. Stage yanlış thread kullanımını erken reddeder. Bu düzen aralıklı yenileme takılması ve staging kalıntısı sonrasında eklendi. [Android SQLiteSession sahiplik sözleşmesi](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/database/sqlite/SQLiteSession.java).

## Kanıt ve kalan doğrulama

İki sentetik XML fixture hem JVM hem Android tarafından kullanılır: Avrupa DST/çok dil/eksik bitiş ve Amerika DST/kanal tanımsız program/ofsetsiz UTC. Native akışlar yerel test sunucusu, gerçek kanal logoları ve sentetik programlar kullanır. Bunlar gerçek sağlayıcı incelemesinin yerine geçmez. [Veri temeli kanıtı](../project/epg-foundation-qa.json) · [Native 0.13 doğrulaması](../project/native-guide-qa.json).

14 Ekim’de bir kaynağın gerçek XMLTV/canlı metadata’sı indirildi; Android üretim okuyucu/yenileme/SQLite yolunda 253 kanal ve 10.588 program yeniden açıldı. 402 canlı katalog satırı 252 farklı rehber kanalına eşleşti. Aynı anda çakışan 26 kanal kesin şimdi bilgisine dönüştürülmedi. [Yöntem ve sınırlamalar](../research/PROVIDER_EPG_AUDIT.md), [ölçümler](../project/provider-guide-qa.json).

Açık: diğer sağlayıcıların XMLTV uyumluluğu, TV’de rehberden oynatma, büyük rehberin düşük bellekli/fiziksel cihaz performansı, 4K/izleme mesafesi ve kullanıcı turu. Bu native adım PIN, genel SYS01 veya tüm kalite kapılarını tamamlamaz. Xtream kısa EPG API yolu ve elle kanal eşleştirme uygulanmadı.
