# Katalog sunumu ve eksik görseller

**14 Ekim 2026 · Android 0.14.1 tasarım önizlemesi.** Gerçek kaynak incelemesinde görülen ayırıcı kayıtlar ve eksik görseller için ortak sunum kuralları. Ham katalog, hesaplar, kimlikler, arama ve oynatma yeteneği korunur. [Google TV görsel sözleşmesi](NATIVE_VISUAL_REVISION.md).

## Ayırıcı adayları

Adın iki kenarında aynı `*`, `=`, `_`, `~`, çizgi veya tire karakterinden en az üç tane bulunması bir sunum ipucudur. Dolu EPG kimliği varsa bu ipucu uygulanmaz. INFO kelimesi, başlık içindeki yıldızlar veya yalnız bir kenardaki süsler yeterli değildir. Bu, kaydın oynatılamadığına ilişkin bir karar değildir; bilgi videosu veya gerçek kanal olabilir.

Otomatik ana sayfa rafları ve büyük kapak bu adayları öne çıkarmaz. Tür/kategori kütüphanesinde normal içerikler önce, adaylar sonra gelir; her iki grubun kendi kaynak sırası korunur. Hiçbir kayıt silinmez veya aramadan çıkarılmaz. Kullanıcının açıkça eklediği favori ve izleme geçmişi bu sıralama ipucundan üstündür. Yalnız adaylardan oluşan kaynakta üst menü üzerinden tüm kayıtlara erişim sürer.

Yerel olarak saklanan gerçek kaynak snapshot'ı üretim ayrıştırıcısı ve sunum kurallarıyla tekrar incelendi: 4.525 canlı kayıtta 51, 14.609 filmde 40 aday; 2.173 dizide sıfır aday. Tüm kimlikler korundu. Yeni sağlayıcı isteği veya yayın açılmadı. Bu sayılar yeni bir çevrimiçi katalog snapshot'ı değildir.

## Ana sayfa seçimi

Otomatik raflar en çok 12 içerik gösterir; görsel adresi bulunanlar kendi kaynak sıralarıyla önce gelir, kalan yerler eksik görselli içeriklerle tamamlanır. Kütüphanenin asıl sırası yalnız ayırıcı adaylarının sona alınmasıyla değişir. Geçmiş ve favori rafları kendi davranışını korur.

Büyük kapak seçimi ilk 12 öğeyle sınırlı değildir. Önce geniş arka planı bulunan favori, ardından kaynak sırasındaki geniş arka planlı film/dizi seçilir. Bunlar yoksa afişi bulunan favori/içerik, ardından metinle sunulabilen içerik kullanılır. Film ve dizi içinde rastgele seçim yapılmaz; popülerlik, yayın dili veya kullanıcıya özel öneri iddiası yoktur. Kaynak filtresi aynı seçimlere uygulanır.

## Görsel rolü ve yedek görünüm

- Sağlayıcının `backdrop_path` dizisinde ilk geçerli HTTP(S) adresi kullanılır; ilk elemanın boş veya bozuk olması sonraki geçerli görseli kaybettirmez.
- Posterle birebir aynı adres, geniş arka plan olarak değerlendirilmez. Gerçek görsel içeriğinin doğru esere ait olduğu bu kontrolle kanıtlanmış sayılmaz.
- Geniş arka plan yoksa ana sayfada afiş 2:3 oranında ayrı bir alanda, film ayrıntısında sağda gösterilir. Dikey afiş ekran genişliğine kırpılmaz. Ana sayfa rafındaki yatay kart da gerektiğinde ortalanmış dikey afiş kullanır.
- Logo yoksa ortak TV ikonu; afiş yoksa nötr yüzey, film ikonu ve okunaklı başlık gösterilir. Yükleme sırasında geometri değişmez. Eksik afiş veya açıklama oynatma erişimini kapatmaz.

## Açık sınırlar

Yanlış esere bağlanmış ama teknik olarak geçerli görseller otomatik teşhis edilmiş değildir. Yeni harici metadata servisi, dil tahmini veya hazır film listesi eklenmedi. Sağlayıcının gerçek görsel oranını piksel düzeyinde doğrulama, bozuk görsel sonrası farklı rol seçimi ve ana sayfa içerik çeşitliliği sonraki kalite adımlarıdır. Kullanıcı görsel onayı, Figma eşlemesi ve fiziksel TV ölçümleri ayrıca gereklidir.


## Kontrol ve gerçek Android görüntüleri

[Kurulum dosyası](../../releases/IPTV_Player_TV_Design_Preview_0.14.1.apk) · [Test kapsamı ve SHA-256](../project/native-catalog-presentation-qa.json). 116 JVM kontrolü (bir tanesi gerçek snapshot üzerinde opt-in denetim), lint 0 hata / 32 uyarı. Normal fontta 19 akış; 1,5 fontta yeni sunum akışı; 2,0 fontta yeni sunum ve dokuz arama/gezinme akışı geçti. Kullanıcının test cihazı yerine ayrı emülatör kullanıldı.

| Durum | Normal yazı | 2,0 yazı |
| --- | --- | --- |
| Yalnız afişi olan ana kapak | [Görüntü](../../design/reviews/catalog-presentation/font-1.0/home-poster.png) | [Görüntü](../../design/reviews/catalog-presentation/font-2.0/home-poster.png) |
| Eksik afiş ve ayırıcı sırası | [Görüntü](../../design/reviews/catalog-presentation/font-1.0/movies.png) | [Görüntü](../../design/reviews/catalog-presentation/font-2.0/movies.png) |
| Ayrıntıda dikey afiş | [Görüntü](../../design/reviews/catalog-presentation/font-1.0/detail-poster.png) | [Görüntü](../../design/reviews/catalog-presentation/font-2.0/detail-poster.png) |
| Eksik kanal logosu | [Görüntü](../../design/reviews/catalog-presentation/font-1.0/live.png) | [Görüntü](../../design/reviews/catalog-presentation/font-2.0/live.png) |
| Yalnız ayırıcı içeren kaynak | [Görüntü](../../design/reviews/catalog-presentation/font-1.0/divider-only.png) | [Görüntü](../../design/reviews/catalog-presentation/font-2.0/divider-only.png) |

Yenileme sonrası başarı bildirimi ilk rafı aşağı itebilir; odaklı karta kaydırma çalışsa da ana sayfanın ilk görünümündeki alt boşluk hâlâ iyileştirme gerektirir. Bu görüntüler kusursuz tasarım veya bütün ekranların erişilebilirlik onayı sayılmaz.
