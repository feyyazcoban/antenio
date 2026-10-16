# Android Google TV tasarım önizlemesi

**14 Ekim 2026 · 0.14.0-design-preview.** Seçilen görsel yön mevcut Compose for TV bileşenlerine uygulandı. Bu sayfadaki görüntüler çalışan Android uygulamasından alınır; HTML taslağı veya Figma ekranı değildir. Kullanıcı görsel onayı, Figma eşlemesi ve fiziksel TV doğrulaması bekler. [İlk görsel yön ve ölçüler](VISUAL_REVISION.md).

**0.14.1 devamı:** [Katalog ve eksik görsel iyileştirmeleri](CATALOG_PRESENTATION.md) ayrı ekran/test kaydıyla yayımlandı. Aşağıdaki görüntüler 0.14.0 revizyonunun tarihli kaydıdır.

## Gerçek Android ekranları

Yerel test sunucusu üzerinden daha önce incelenmiş gerçek afiş ve logolar kullanıldı. Hesaplar ve yayın girdileri sentetiktir; gerçek sağlayıcıda yayın açılmadı. Katalog sıralaması test kaynağından gelir; üretime sabit film seçimi eklenmedi.

| Ekran | Normal yazı |
| --- | --- |
| Ana sayfa | [Android görüntüsü](../../design/reviews/google-tv-soft-native/font-1.0/home.png) |
| Canlı TV | [Android görüntüsü](../../design/reviews/google-tv-soft-native/font-1.0/live.png) |
| Filmler | [Android görüntüsü](../../design/reviews/google-tv-soft-native/font-1.0/movies.png) |
| Diziler | [Android görüntüsü](../../design/reviews/google-tv-soft-native/font-1.0/series.png) |
| Film ayrıntısı | [Android görüntüsü](../../design/reviews/google-tv-soft-native/font-1.0/detail.png) |
| Arama | [Android görüntüsü](../../design/reviews/google-tv-soft-native/font-1.0/search.png) |
| Rehber / veri yok | [Android görüntüsü](../../design/reviews/google-tv-soft-native/font-1.0/guide.png) |

## Ortak düzen ve gezinme

- Ana sayfa, canlı TV, filmler, diziler ve favoriler üstteki yatay menüde. Arama ve ayarlar sağdaki adlandırılmış ikonlardır. Sol/sağ menü içinde ilerler; odaklanmak ekranı değiştirmez, OK açar. Üst kenar ve menünün yatay uçları odağı dışarı kaçırmaz. Büyük yazıda menü yatay kayabilir.
- Normal yazıda 88 dp üst alan, 58 dp yatay kenar, 228 dp ana kapak ve 196×110,25 dp yatay raf kartları. Film/dizi kütüphanesi beş 2:3 afiş sütunu; canlı katalog üç 268 dp kart sütunu. İçeriğe göre büyüyebilen metin ve eylem alanları vardır.
- Seçili menüde tonlu dolgu ve alt çizgi; odakta açık dolgu/koyu metin. Kartlarda açık 2 dp odak çizgisi, 3 dp ayrım; afişte 1,025 ölçek, canlı kartta ölçek yok. Hareketi azalt ayarı ölçeği kapatır. Native TV Material geçişi kullanılır; 160 ms gerçekleşme süresi ölçülmüş sayılmaz.
- Kaynak filtresi ana sayfa, kütüphane, rehber ve aramada korunur. Rehber canlı katalog başlığından açılır. Bölüm içi arama düğmesi alanı açar; yazarken sonuç yenilenmesi odağı karta taşımaz. Sorgu düğmede görünür; Temizle ile kaldırılır ve odak girişe döner.
- Kütüphane başlığı, arama ve kategoriler kartlarla birlikte kayar. 1,5/2,0 fontta ilk kart için başlık yukarı kaydırılabilir; yukarı hareketle kontroller yeniden erişilir. Odaktaki görsel ve açıklama birlikte görünür alana alınır; alt güvenli boşluk korunur. Başlık/filtre konumu kartı küçülterek telafi edilmez.
- İzlemeye devam et, son kanallar, favoriler, kaynak kimliği, aynı karta geri dönüş ve tek oynatıcı davranışları korunur. Ayrıntıda uzun eylemler yeni satıra geçebilir. Sistem açılış zemini ve ikon renkleri ortak palete uyar.

## Doğrulama

[Kurulum dosyası](../../releases/IPTV_Player_TV_Design_Preview_0.14.0.apk) · [Test kapsamı ve SHA-256](../project/native-visual-revision-qa.json). Ayrı Android API 36 emülatöründe 109 JVM kontrolü; lint 0 hata / 32 uyarı; ilgili gezinme, odak ve kaynak/rehber yerleşim kontrolleri geçti. Son APK'de normal ve 2,0 fontta 10'ar arama/gezinme/görsel vaka tekrarlandı; önceki adayda 1,5 ve 2,0 fontta 17'şer vaka geçti.

| Karşılaştırma | 1,5 yazı | 2,0 yazı |
| --- | --- | --- |
| Filmler | [Görüntü](../../design/reviews/google-tv-soft-native/font-1.5/movies.png) | [Görüntü](../../design/reviews/google-tv-soft-native/font-2.0/movies.png) |
| Ayrıntı | [Görüntü](../../design/reviews/google-tv-soft-native/font-1.5/detail.png) | [Görüntü](../../design/reviews/google-tv-soft-native/font-2.0/detail.png) |

Üç yazı boyutundaki 21 görüntü son APK üzerinde yeniden alındı; görsel yüklemesinden sonraki arayüz çizimi de beklenir. Şeffaf logoların arkasında eski yükleme harfi kalan ilk test görüntüleri değiştirildi. Büyük yazıda katalog başlığı kaydırılırken içerik sayısı üst kenarda kısmen görünebilir; bu kenar düzeni hâlâ iyileştirme gerektirir. Ayrıntının alt yardımcı açıklaması kaydırma isteyebilir; ana eylemler görünür kalır. Bu kontroller tüm ekranların erişilebilirlik veya fiziksel akıcılık onayı değildir.

## Sınırlar

Figma Starter MCP kotası önceki turda engeldi; bu tur yeni Figma yazımı veya uyum doğrulaması yapılmadı. Aday token dosyası native önizlemeyi açıkça belirtir; tarihli Figma snapshot’ı değiştirilmez. Ayarlar, kaynak yönetimi ve oynatıcı ortak renk/düğmeleri alır; bu yüzeylerin tamamı yeniden tasarlanmış sayılmaz.

Gerçek sağlayıcının INFO/ayırıcı kayıtlarının ilk raflarda görünmesi, eksik/yanlış kapak seçimi ve büyük katalogdan iyi ana sayfa seçimi açık görsel kalite işleridir. Örnek katalogdaki beş afiş bu sorunları çözmüş sayılmaz. Fiziksel akıcılık, HDR/codec/ses matrisi, PIN ve kolay telefon kurulumu mevcut açık iş listesinde kalır. Kullanıcının emulator-5554 hesabı/APK’si ve 65U95A bu çalışma sırasında değiştirilmez.
