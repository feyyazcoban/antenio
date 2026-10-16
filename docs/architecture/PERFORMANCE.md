# Büyük katalog ve önbellek ölçümü

Bu belge PRD performans kapılarını değiştirmez. Android'in [Macrobenchmark yaklaşımı](https://developer.android.com/topic/performance/benchmarking/benchmarking-overview) açılış ve kullanıcı etkileşimlerini uygulama dışından ölçer. Buradaki ilk araç, darboğaz bulmak için mevcut debug emülatöründe çalışan tanı testidir; fiziksel cihaz, release derleme veya gerçek kumanda olayından ekrandaki piksele kadar geçen süreyi kanıtlamaz.

## Tekrarlanabilir katalog denemesi

`LargeCatalogProbeTest` yalnız emülatörde 10.000 kanal +35.000 film +15.000 dizi üretir. Başlıklar sentetik Türkçe metin, 100 kategori, boş afiş alanı ve kapalı loopback adresi içerir. Gerçek sağlayıcıya bağlanmaz. Test kaynak/favori/geçmişi temizlediği için kullanıcı TV'sinde çalıştırılmaz.

Ölçülen süreler: şifreli katalog yazımı; beş kez diskten okuyup Ana Sayfa görünür semantiğine kadar restore; ilk indeks oluşturma dahil ilk sorgu; altı sorgu sınıfının dört turda sonuç görünümüne gelişi. Dar, geniş, aksan/büyük harf ve sayı sorguları vardır. Başlangıç `elapsedRealtimeNanos`, bitiş sonuç semantiği ve Compose idle kontrolüdür. Test bekleme maliyetleri süreye dahildir. Restore işlemi süreç soğuk açılışı değildir. Son Java heap kullanımı toplam süreç belleği, native/grafik bellek veya sızıntı ölçümü değildir.

24 sorgu örneğinin ham süreleri saklanır; ilk sorgu ayrı değerlendirilir. Az sayıdaki debug/emülatör örneğinin p95'i PRD kabul ölçütü gibi sunulmaz. Hostta eşzamanlı derleme yapılan keşif turu, derleme bittikten sonraki turdan ayrı tutulur. Yeni sürüm ölçümünde host derlemesi yapılmaz. Aynı uygulama sürümünde tekrarlanan gerçek cihaz Macrobenchmark ve frame/input ölçümü ayrıca gerekir.

## Görsel sahiplik kaydının temizliği

80 MB disk byte-cache'i ve %12 bellek limiti korunur. `ArtworkCache` başlangıçta ve her 16 tamamlanmış istekte arka planda bir bakım turu planlar. Aynı anda bir bakım işi çalışır; her tur en fazla 64 sahiplik satırını kontrol eder. Kayıtlar sırayla taranır, sona gelince başa dönülür. Bir kaynak/görsel çifti yeniden istendiğinde sahipliği istek başlamadan önce yine kaydedilir.

Bir anahtar için devam eden istek veya bellekte herhangi bir sahibin varyantı varsa bütün sahiplikler korunur. Diskte metadata ve veri dosyası varsa korunur. Disk/bellek/istek kalmadığında o anahtara ait geçersiz referanslar silinir. Bu bakım görsel dosyalarını silmez; başka sahibin ortak afişini silme yetkisi yaratmaz. Kaynak kaldırmanın son-sahip kontrolü aynı kalır.

Disk dosya isimleri tahmin edilmez. [Coil Snapshot API](https://raw.githubusercontent.com/coil-kt/coil/3.3.0/coil-core/src/commonMain/kotlin/coil3/disk/DiskCache.kt) tarafından döndürülen metadata/veri yolları cache köküne göre göreli olarak `artwork_files` tablosuna yazılır. Ham URL veya hesap alanı yoktur. Bilinen dosya yollarının varlığına bakmak, bütün görselleri tekrar snapshot olarak açıp LRU sırasını değiştirmez. Eski sahiplik kayıtlarının yolu bilinmiyorsa bir kez snapshot açılarak tamamlanır; tüm snapshot'lar hemen kapatılır. Açık editor sırasında snapshot yokluğu yanlış silme sebebi olmasın diye devam eden işler anahtar düzeyinde korunur. [Coil bellek anahtarları](https://raw.githubusercontent.com/coil-kt/coil/3.3.0/coil-core/src/commonMain/kotlin/coil3/memory/RealMemoryCache.kt) güçlü ve zayıf cache kayıtlarını birlikte kapsar.

Bakım IO üzerinde request gate altında çalışır; lifecycle/source kilidini almaz. İşler arası kilit sırası korunur. Bakım hatası oynatıcıyı kapatmaz; sonraki kullanımda yeniden denenir. `close()` bakım işini de bitirir. Uygulama VACUUM çağırmaz; fiziksel dosya boyutu SQLite'ın auto-vacuum/freelist/journal davranışına bağlıdır. Ölçüm DB ve journal/WAL yan dosyalarını birlikte sayar. Sahiplik indexi 80 MB görsel sınırının dışındadır. 64 satır sınırı bütün tablolar için sabit byte sınırı değildir; özellikle birçok kaynak aynı afişi kullandığında geçerli referanslar korunur. [SQLite freelist](https://www.sqlite.org/pragma.html#pragma_freelist_count).

## Arama indeksini önceden hazırlama

`TitleIndexCache` katalog yayımlandıktan sonra başlıkları arka planda bir kez hazırlar. Kütüphane ve ortak arama aynı tamamlanan/bekleyen indeksi paylaşır. Yazılan sorgu değişince sadece eski sorgunun beklemesi/sonuç araması iptal edilir; indeks oluşturma her harfte baştan başlamaz. Katalog değişirse veya ViewModel biterse ilgili hazırlık iptal edilir. Tamamlanan indeks katalog nesnesinin kimliğiyle yeniden kullanılır; hata vermiş hazırlık tekrar denenebilir. Gizli/yetişkin kayıt politikası ve Türkçe normalleştirme değişmez.

İndeks işi ViewModel scope'una ait bağımsız Deferred'dır. [Deferred.await](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-deferred/await.html) çağıranın iptalini izler; bekleyen sorgunun iptali shared işi sahiplenip kapatmaz. Bu seçim dört sanal zaman testiyle (bekleyen sorgu iptali, paylaşım, katalog değişimi, hata/tekrar) doğrulanır; kullanıcı arama sonuçları ayrıca emülatörde sınanır. Hazırlık CPU/bellek kullanır, ana sayfanın açılışını bekleterek hız kazanımı iddia edilmez.

## Açık ölçümler

- Şifre çözme, gzip/JSON ayrıştırma, kaynak kimliği birleştirme ve Home oluşturma sürelerini ayır; ölçülen darboğaza göre iyileştir.
- Gerçek cihazda min/orta/yüksek liste profili, release ve ısınma koşullarını sabitle.
- Tuş olayından çizilmiş odak karesine, ilk arama ve sıcak aramaya ayrı ölçüm ekle.
- Eksik/bozuk afiş, yüzlerce ortak görsel, uzun gezinme ve bellek baskısında artış eğrisini ölç.
- Fiziksel p95 açılış/odak/arama ve iki saat oynatma kapılarını tamamlanmış sayma.

## Kare kaydı çözümleyicisi

[Çevrimdışı framestats tanısı](FRAME_DIAGNOSTICS.md) hazır; dokuz sentetik biçim/hesap/CLI kontrolü geçti. Henüz bu araçla alınmış gerçek cihaz performans sonucu yoktur. Farklı pencere blokları ve dışlanan kareler ayrı tutulur; rapor ürün kabulü üretmez.
