# Çoklu kaynak · uygulama sözleşmesi

**14 Ekim 2026 · geliştirme devam ediyor.** SRC03 ve FIND01 kapsamı birden fazla M3U/Xtream kaynağını aynı kütüphanede kullanmak, tür/kaynak filtresi, ekleme/düzenleme/yenileme/silmedir. Yalnız aktif hesabı değiştiren bir seçim ekranı bu kapsamı tamamlamaz. TvViewModel ve native kaynak yönetimi/filtre ekranları bu sözleşmeye bağlandı. Doğrulama ve teslim durumu STATUS ile sürüme özel QA kaydından izlenir.

## Kimlik ve kayıt sınırı

- `SourceConfig.id` cihazın verdiği kalıcı kaynak kimliğidir. Ad/hesap düzenlemesi bu kimliği değiştirmez. Yeni kaynak yeni kimlik alır; aynı görünen ad benzersizlik kanıtı değildir.
- `ContentRef` kaynak kimliği ve sağlayıcının yerel içerik kimliğini uzunluk önekli tek anahtara çevirir. İsimle birleştirme yapılmaz. Aynı sağlayıcı ID'si iki hesapta ayrı kalır.
- `MediaEntry.inSource` yalnız bellekteki ortak kütüphaneye kimlik/owner ekler; `withoutSource` sağlayıcının yerel girdisini geri verir. Ham URL, providerId ve başlık değiştirilmez. Farklı sahibin girdisi yeniden etiketlenemez.
- `SourceLibrary.combine` katalog ve M3U dizi/bölüm bağlantılarını aynı kaynak anahtarıyla birleştirir. Favori/geçmiş toplama da bu kimliği kullanır. Her kaynağın geçmişi diskte ayrı kalır; toplu Home geçmişi sıralanır.
- Katalog snapshot ve geçmiş codec'leri kapsam eklenmiş runtime girdilerini doğrudan diske yazmayı reddeder. Disk formatında yerel kimlik saklanır; okurken kaynağı eklenir. Böylece eski favori/ilerleme kimliklerini topluca değiştirmek gerekmez.

## Şifreli kaynak listesi

`DeviceStore.loadSources/saveSources/forgetSource(id)` yeni API'dir. `SourceRegistry` kaynak listesi ve nullable kaynak filtresini saklar; null tüm kaynaklar demektir. TvViewModel yalnız registry API’sini kullanır. Eski tek kaynak API’si geçiş testleri için tutulur; normal kaynak yönetimi parametresiz toplu silme API’sini çağırmaz. Yalnız okunamayan kayıtlarda açık recovery onayı tüm kaynakları sıfırlayabilir (D034).

Kaynak listesi `sources_v2` preference içinde mevcut Android Keystore anahtarıyla AES-GCM ve `source-registry-v1` bağlamına bağlı şifrelenir. İlk okumada registry yoksa eski `source` okunur; registry varsa bozuk içerik eski hesaba sessizce düşmez. Şema/ID/filtre/adres doğrulaması vardır. Kaydetme listesi en fazla 1 MB düz veri kabul eder; bu güvenlik/depoma bütçesidir, UI kaynak sayısı taahhüdü değildir.

İlk liste yazımı, şifreli yeni registry'yi ve eski tek kaynak anahtarının kaldırılmasını aynı preference commit'inde yapar. İşlemler `SourceTransactions.shared` içinde çağrılmalıdır. Ayrı DeviceStore örnekleri için instance kilidi tek başına yeterli değildir. UI uygulamau, güncellemeden hemen önce registry'yi bu kilidin içinde tekrar okuyup değiştirmelidir; eski ViewModel anlık kaydı yeni kaynağı silemez.

## İlerleme ve eski kayıt geçişi

Yeni konum anahtarı `p2:<kaynak uzunluğu>:<kaynak kimliği>:<eski içerik özeti>` biçimindedir. Hash hesabı aynı kalır; kaynak öneki yalnız o kaynağın kayıtlarını temizlemeyi sağlar. Favori `f:<kaynak>`, geçmiş `history:<kaynak>` zaten ayrıydı.

Registry'ye ilk geçişte eski `p:` kayıtları tek eski kaynak bağlamına taşınır. Son 50 geçmiş kaydında olmayan konumlar da korunur. Varsa daha yeni `p2:` kopyası tercih edilir. Hash içinde eski kaynak kimliği de bulunduğundan önceki pilot hesaplarından kalmış ulaşılamayan konumlar başka içeriğe bağlanmaz. Kaynak kaldırma sadece o kaynağın p2/favori/geçmişini siler; cihaz hareket/ses tercihlerine dokunmaz. `forgetSource()` normal çoklu kaynak yönetiminde kullanılmaz; açık recovery sıfırlaması bunun ve anahtar yenilemenin ayrı birleşik akışını çağırır.

## Katalog dosyaları

Yeni dosya adı kaynak kimliğinin SHA-256 özeti ile başlar; devamında hesap bağlama özeti bulunur. Hesap adresi/parolası dosya adına girmez. Kaynak kimliği aynıyken parola değişse de `CatalogStore.remove(source)` bu sahibin tüm yeni adlandırılmış sürümlerini ve AtomicFile yan dosyalarını temizler; başka kaynağın dosyalarına dokunmaz.

Eski bağlama özetiyle adlandırılmış dosya okumada desteklenir. İlk registry yazımından önce `CatalogStore.migrateLegacy` tek eski kaynak bağlamında çalışır: okunabilir güncel snapshot yeni adla yazılır, ardından eski adsız bağlama dosyaları ve AtomicFile yan dosyaları temizlenir. Güncel kayıt yazılamazsa registry geçişi durur. İşlem tekrarlanabilir; yeni kaynak önekli dosyalara dokunmaz. Böylece önceki tek hesap değişikliklerinden kalan sahipsiz dosyaların sahibi çoklu kaynak kurulduktan sonra tahmin edilmez.

0.10 görsel cache sahipliği ve okunamayan kaynak kurtarması aşağıdaki sözleşmeyi ekler. Disk dosyaları arasında tek bir güç-kesintisi işlemi iddiası yoktur.

## Native davranış

1. TvState: kaynak listesi, kaynak bazında güncelleme/hata durumu, katalog filtresi, arama kaynak filtresi ve düzenlenen kaynak kimliği. Ortak katalog anahtarları her zaman kapsamlı olmalı; tek kaynakta eski, iki kaynakta yeni anahtar kullanarak odağı/favoriyi değiştirme.
2. Başlangıç bütün kayıtlı kaynakları ve taze cache'lerini yükler. Eksik/eski cache sırayla yenilenir; mevcut kullanılabilir kaynaklar görünür kalır. Ağ işleri disk kilidi dışında; aynı anda ikinci gerçek oynatıcı açılmaz.
3. Ekleme ve düzenleme ayrıdır. Düzenlemede kimlik korunur, başarısız doğrulama eski kaynağı değiştirmez. Aynı bağlantıyı ikinci kez ekleme açık bir mesajla ele alınır; sessizce başka hesabın üstüne yazılmaz.
4. Oynatma/ayrıntı/sonraki bölüm/favori/ilerleme, girdinin sahibini kullanır. `state.source` adlı tek kaynak varsayımı bu çağrılarda kaldırılmalı. M3U detay isteği yerel ID ve sahibin ham kataloğuyla yapılır; dönen bölümler kapsamlanır.
5. Kaynak yönetimi ekranında ekle, düzenle, yenile, kaldır, içerik sayısı ve sabit hata/son güncelleme. Kaldırma onayında ilk odak Vazgeç; işlem sırasında eski player son kaydı diriltemez. Yalnız seçilen kaynak temizlenir, filtre kaldırılan kaynaktaysa Tüm kaynaklar'a döner.
6. Home/katalog/Favoriler ve aramada kaynak filtresi; kartta gerektiğinde kaynak adı. Aynı adlı iki sonuç ayrı görünür. Geri dönüş sorgu/tür/kaynak/odak/scroll'u korur. Filtre kaynak kaldırılınca güvenle kurtarılır.
7. Eski tek kaynaklı testlerde sabit `movie:3` runtime anahtarları ContentRef ile uyarlanmalı; disk testleri yerel ID kullanmaya devam eder. Her testin temizliği tüm test kaynaklarını kapsamalı. Genel test paketi kullanıcı TV'sinde çalıştırılmaz.

## Tamamlanma kanıtı

Veri katmanı testleri yalnız altyapıyı kanıtlar. SRC03/FIND01 için iki sahte sunucuyla aynı ID/farklı hesap, bağımsız favori/ilerleme, başarılı ve hatalı düzenleme, kaynak filtresi/odak, tek kaynağı kaldırma, yeniden açılış, kısmi/başarısız yenileme ve kalan kaynağın kullanılabilirliği UI üzerinden sınanmalı. Sonra gerçek Android görüntüleri incelenir ve yeni APK/paket üretilir. Fiziksel TV ve kullanıcı görsel onayı ayrı kapılardır.

## Ekran ve geri dönüş

`Route.SOURCES` TV-070 kaynak yönetimi yüzeyidir; var olan Roboto/Ink/TV Action bileşenlerini kullanır. Ekleme Kaynak ekle’ye, düzenleme ilgili Düzenle düğmesine, kaldırma en yakın kalan Kaldır düğmesine döner. Kaldırma onayı başlangıçta Vazgeç’i odaklar. Kaynak seçicisi mevcut seçime kayar ve odaklar; Back seçim yapmaz.

Ana Sayfa/katalog/Favoriler filtresi registry’de kalır. Arama kendi oturum içi kaynak filtresini korur ve tüm katalogda çalışır. Arama ekranında tek kaynak filtresi başlıktadır; sol menüde ikinci farklı seçim gösterilmez. Sonuç, katalog kartı, ayrıntı ve player gerektiğinde kaynak adı gösterir. İki kaynak aynı adı kullanıyorsa kararlı liste sırası görünür ada eklenir; medya kimliği hiçbir zaman ada dayanmaz.

Kaynaklar sırayla yenilenir. Eksik cache diğer hesabı engellemez; ilk indirme boş listeden ayrı mesaj gösterir. Kayıtlı tam katalog varken kısmi yenileme/düzenleme eski listeyi korur; ilk eklemede kısmi kullanılabilir içerik uyarıyla kabul edilir. Kaynak sağlık satırı içerik sayısını, son zamanı ve hatayı taşır.

## Test yaklaşımı

`MultiSourceFlowTest` iki bağımsız MockWebServer’da aynı ID/başlıklarla arama filtreleri, favori/ilerleme ayrımı, ikinci hesaptan playback/sonraki bölüm, düzenleme/silme/odak, taze cache, başarısız/kısmi yenileme, M3U dizi haritası ve yinelenen bağlantıyı sınar. Disk codec beklentileri yerel, UI beklentileri ContentRef kimliğidir. `FixtureSources` tüm sentetik kaynakları yalnız emülatörde ve işlem kilidi altında temizler. Fiziksel TV’de test paketini çalıştırmayın.

## Görsel önbelleği · 0.10

`TvApplication.artworkCache` tek ImageLoader/disk-cache sahibidir. Tüm kapak, logo, hero, ayrıntı ve bölüm sonu görselleri `SourceArtwork(owner, url)` taşır. Sahipsiz ham görsel isteği kabul edilmez. URL yalnız istek belleğinde ve Coil'in özel görsel/cache katmanında kalır; sahiplik tablosu kaynak ID ve tam URL'nin SHA-256 özetlerini saklar. Bu tablo hesap sırrı deposu değildir; görsel cache şifreli katalogla aynı güvence olarak sunulmaz.

Disk anahtarı URL özeti olduğundan iki kaynak aynı dosyayı paylaşabilir. Bellek anahtarı owner+URL özeti; kaynak kaldırma bu sahibin bütün boyut varyantlarını temizler. Başarıdan önce owner/key referansı SQLite'a yazılır; başarısız indirme de temizlenebilir bir referans bırakır. Kullanılmayan katalog URL'leri için önceden indirme/referans kaydı yapılmaz. Koruma, görseli gerçekten istemiş kalan kaynakları kapsar.

Kaldırma önce yeni istekleri engeller, o sahibin coroutine'lerini ve gerçek OkHttp çağrılarını iptal eder. Gövde kopyalama yalnız coroutine iptaline bırakılmaz. Job'lar ve disk snapshot'ları kapanınca başka owner referansı olmayan dosya/metadata silinir; referans en son kaldırılır. Başka hesabın aynı görseli ve devam eden isteği korunur. Kaldırma başarısızsa diskteki güncel kayıtlı hesaplar yeniden etkinleştirilir; sabit hata mesajı görünür.

Uygulama yeniden açılırken kayıtlı hesaplarla sahiplik tablosu uzlaştırılır. İlk 0.10 geçişinde önceki adsız Coil cache bir kez temizlenir; kataloglar/favoriler korunur, afişler gerektiğinde yeniden alınır. Migration işareti temizliğin ardından yazılır. 80 MB toplam görsel disk bütçesi ve %12 bellek sınırı korunur; SQLite referans tablosu bu 80 MB sınırına dahil değildir. Çok büyük kaynak sayısında tablo büyümesi/periyodik budama performans işi olarak izlenir.

Uygulama source transaction kilidi → artwork lifecycle → artwork request kilidi sırasını izler. İstek bitişi source/lifecycle kilidi almaz; kaldırma işlerin bitmesini beklerken request kilidini tutmaz. Böylece silme ve bitiş birbirini kilitlemez. Ek bağımlılık eklenmedi; [Coil interceptor](https://raw.githubusercontent.com/coil-kt/coil/3.3.0/coil-core/src/commonMain/kotlin/coil3/intercept/Interceptor.kt), [disk snapshot sözleşmesi](https://raw.githubusercontent.com/coil-kt/coil/3.3.0/coil-core/src/commonMain/kotlin/coil3/disk/DiskCache.kt) ve [NetworkClient](https://raw.githubusercontent.com/coil-kt/coil/3.3.0/coil-network-core/src/commonMain/kotlin/coil3/network/NetworkClient.kt) API'leri kullanılır.

## Okunamayan kaynaklar · 0.10

`StoredSourcesUnavailable` yalnız legacy/registry okuma, çözme veya şema doğrulama hatasını ayırır. Restore hatası ayrı `RECOVERY` ekranı açar; otomatik silme ya da yeni hesabın bozuk kayıt üstüne yazılması yoktur. Önce Yeniden dene odaklanır. Diğer I/O hatalarında geri dönüş vardır fakat kayıtları sıfırlama önerilmez.

Bozuk kaynak kaydında Kaynakları sıfırla ayrı onay açar. Vazgeç başlangıç odağıdır; OK/Back iptal eder ve açan düğmeye döner. Onay tüm yerel kaynaklar/kataloglar/görseller/favoriler/geçmişi ve yeniden giriş gereksinimini açıklar. Sağlayıcı aboneliğini etkilemez; cihaz ses/hareket/dil/otomatik bölüm tercihi korunur.

Sıfırlama kilit içinde kaydın hâlâ aynı bozuk snapshot olduğunu doğrular. Bu arada başka model kaydı düzeltmiş/değiştirmişse silmez, yeniden okur. Açık onaydan sonra görseller, şifreli kataloglar, eski Keystore anahtarı ve kaynak/kişisel kayıtlar temizlenir. Yeni anahtar sonraki kayıt için üretilir; geçersiz eski anahtar yeniden kurulum döngüsü yaratmaz. İşlem sırasında Back başka iş başlatmaz. Hata olursa sabit mesajla tekrar denenebilir; çok dosyalı atomik rollback iddiası yoktur.

## Uzun adlar ve kalabalık kaynak listesi · 0.11

SourceConfig.name tam saklanır; yalnız tek satırlık liste ve filtre görünümü MiddleEllipsis kullanır. Kaynak sırası görünür bağlamdır; SourceConfig.id veya ContentRef yerine geçmez. Aynı adların mevcut sıra eki korunur. Seçici seçili kaynak konumunda açılır; iptal açık filtreyi ve odağı korur. Bilgiler tam ad/tür/sıra/içerik sayısı ve durum açıklamasını sunar; hesap adresi ve sırlar bu pencereye eklenmez.

RemoteTextReader sınırlı dikey alanda tam metni sunar; kumandayla yukarı/aşağı kayar, sınırda yön odağına izin verir. Açıklama uzadığı için silme veya kapatma denetimleri ekran dışına itilmez. SourceLayoutFlowTest 16 kaynak, iki yinelenen ad, yaklaşık bin karakterlik ad, alt sıralara dönüş, seçiciden arama filtresine dönüş, hata kartı ve onay/iptal için gerçek D-pad kullanır. Ayrı emülatör, sentetik hesap ve kapalı loopback portu kullanılır; sağlayıcıya bağlanmaz. 1.0/1.5/2.0 sonuçları native-source-layout-qa.json içinde; gerçek cihaz veya tüm uygulama font sertifikası değildir.

## Görsel sahiplik bakımı · 0.12

0.10'daki index budama maddesi için başlangıçta ve her16 biten istekte arka planda en çok64 satırlık bakım eklendi. Diskte dosyası, bellekte varyantı veya devam eden işi olmayan anahtarların bütün eski owner referansları silinir. Geçerli ortak afişlerin referansları korunur; kaynak kaldırma son-sahip kuralı değişmez. Snapshot API'den alınan göreli dosya yolları ayrı artwork_files tablosundadır; URL veya sır eklenmez. Önceki DBversion1 kayıtları silinmez, yollar ihtiyaç oldukça tamamlanır. Fiziksel index için sabit byte sınırı iddiası yoktur. [Ayrıntı ve ölçüm sınırları](PERFORMANCE.md).
