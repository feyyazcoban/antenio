# Yenileme, katalog başlığı ve ayar paneli

**0.14.2 önizlemesi hazır:** Normal, 1,5 ve 2,0 yazıda 12’şer ilgili akış geçti. Üç tekrarlı başlık/kart dönüşü ve kumandayla hata mesajına erişim doğrulandı. Görseller tam ürün kabulü anlamına gelmez.

**14 Ekim 2026 · Android 0.14.2 tasarım önizlemesi.** Önceki [katalog sunumu](CATALOG_PRESENTATION.md) üzerine yerleşim ve geri bildirim iyileştirmesi. Bu çalışma Figma eşlemesi veya fiziksel TV onayı değildir. Son test sonuçları [QA kaydında](../project/native-feedback-settings-qa.json) tutulur.

## İçerik yerinde kalır

Başarılı katalog yenilemesi ana sayfaya veya kütüphane başlığına yeni bir satır eklemez. Aynı verinin yenilenmesi ana kapağın, ilk rafın ve odaktaki afişin konumunu değiştirmez. İçeriğin kendisi eklenmiş/silinmişse mevcut kimlik ve odak kurtarma kuralları geçerlidir.

Başarı bilgisi Ayarlar'da **Kataloğu yenile** satırının açıklamasında, Kaynaklar'da işlem sonucu olarak kalır. Yenilenirken aynı satır bekleme durumunu gösterir; tekrar OK yeni indirme başlatmaz. Mesajlar metin eşleştirmeyle ayrılmaz: `CatalogNotice` işlem türünü ve dikkat gerektirip gerektirmediğini taşır. Kaynak kaldırma/seçim sonucu yenileme sonucuymuş gibi gösterilmez.

Başarısız/kısmi yenileme ve kayıt sorunları katalogda kalıcı açıklama olarak gösterilir. Ayarlar'daki yenileme satırı da hatayı açıklar; yalnız geçici bildirime güvenilmez. Kaynak ayrıntısındaki asıl hata ve son sağlam katalog korunur. Boş ana sayfada hata açıklaması, genel boş kütüphane metninden önce gelir.

## Büyük yazıda bölüm sınırı

Kütüphane başlığı ve araçları kartlarla kaymaya devam eder. Büyük yazıda odak kartlara geçtiğinde, kaydırma yarım başlık üzerinde durmaz; kategori satırının başlangıcına yerleşir. Kısa, tek satırlık katalogda bunun için gereken alt boşluk gerçek kart, kategori ve görünür alan ölçülerinden hesaplanır. Afiş küçültülmez, kullanıcının yazı ölçeği sınırlandırılmaz.

Yukarı ile kategoriye, ardından başlık araçlarına dönülür. Başlıkta odak varsa başlık grubu bütünüyle görünür alana alınır. Tekrar aşağı gelince odaktaki afiş ve açıklaması güvenli boşlukla görünür. Yeni odak, devam eden yerleşme hareketini iptal eder; hareketi azalt açıkken ek yerleşme animasyonu kullanılmaz. Bu davranış genel liste/oynatıcı konum kurallarının yerine geçmez.

## Ayarlar

- Sağdan açılan 440 dp panel; 24 dp dış köşe, ortak koyu zemin ve 12 dp köşeli tonlu satırlar.
- Başlık, Kapat/Geri ve kaydırma ipuçları sabit; içerik kumandayla kayar.
- Satır odağı açık renk 2 dp çizgi ve 3 dp ayrımla gösterilir. Görünür alana alma, çizginin kırpılmaması ve 14 dp kenar solmasının dışında kalması için üstte/altta 18 dp pay bırakır (0.14.26; [UI_REPAIR_26](UI_REPAIR_26.md)).
- Açık/Kapalı durumu metin ve tonlu işaretle gösterilir; erişilebilirlikte tek Switch durumu olarak okunur. Seçim ile odak birbirinden ayrıdır.
- Başarı açıklaması yenileme satırına bağlıdır. Uzayan hata satırı da bütünüyle görünür alana alınır; odak aynı satırda kalır.
- Uygulama bilgisi artık mevcut yayın rehberini doğru anlatır; rehberin kaynak desteğine bağlı olduğunu ve PIN'in henüz bulunmadığını açıklar.

## Görsel kanıt

Görseller gerçek Android uygulamasından, yerel sentetik kaynakla alındı. Afiş/logolar daha önce incelenmiş gerçek varlıklardır; sağlayıcı hesabı veya yayını kullanılmaz. Her yazı ölçeğinde on ekran bulunur.

| Görünüm | Normal | 1,5 yazı | 2,0 yazı |
| --- | --- | --- | --- |
| Yenilenmiş ana sayfa | [Aç](../../design/reviews/feedback-settings/font-1.0/home.png) | [Aç](../../design/reviews/feedback-settings/font-1.5/home.png) | [Aç](../../design/reviews/feedback-settings/font-2.0/home.png) |
| Kart odağında filmler | [Aç](../../design/reviews/feedback-settings/font-1.0/movies.png) | [Aç](../../design/reviews/feedback-settings/font-1.5/movies.png) | [Aç](../../design/reviews/feedback-settings/font-2.0/movies.png) |
| Başlığa geri dönüş | [Aç](../../design/reviews/feedback-settings/font-1.0/movies-controls.png) | [Aç](../../design/reviews/feedback-settings/font-1.5/movies-controls.png) | [Aç](../../design/reviews/feedback-settings/font-2.0/movies-controls.png) |
| Ayarlar | [Aç](../../design/reviews/feedback-settings/font-1.0/settings.png) | [Aç](../../design/reviews/feedback-settings/font-1.5/settings.png) | [Aç](../../design/reviews/feedback-settings/font-2.0/settings.png) |
| Yenileme sonucu | [Aç](../../design/reviews/feedback-settings/font-1.0/settings-result.png) | [Aç](../../design/reviews/feedback-settings/font-1.5/settings-result.png) | [Aç](../../design/reviews/feedback-settings/font-2.0/settings-result.png) |
| Yenileme hatası | [Aç](../../design/reviews/feedback-settings/font-1.0/settings-warning.png) | [Aç](../../design/reviews/feedback-settings/font-1.5/settings-warning.png) | [Aç](../../design/reviews/feedback-settings/font-2.0/settings-warning.png) |

Diğer görüntüler aynı klasörlerde `settings-bottom`, `about` ve `catalog-warning` adlarıyla saklanır. Kullanıcının 5554 emülatörü ve fiziksel TV korunur; kontroller ayrı 5556 üzerinde yapılır. Kalan kaynak/oynatıcı yüzeyleri, gerçek görsel eşleşmesi, kullanıcı onayı ve fiziksel akıcılık açık işlerdir.

Teknik dayanak: [Compose LazyGridState](https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/grid/LazyGridState) ve [TV odak sistemi](https://developer.android.com/design/ui/tv/guides/styles/focus-system). Kesin ölçüler bu projenin [görsel sözleşmesidir](NATIVE_VISUAL_REVISION.md); Google'ın evrensel zorunlu değerleri olarak sunulmaz.

Kartlara dönüş: [normal](../../design/reviews/feedback-settings/font-1.0/movies-return.png), [1,5](../../design/reviews/feedback-settings/font-1.5/movies-return.png), [2,0](../../design/reviews/feedback-settings/font-2.0/movies-return.png).

Kaydırma kesintisi gözlemciyi durdurmaz; gerçek ekran/odak iptali korunur. Teknik dayanak: [Kotlin ensureActive](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/ensure-active.html). Ayarlar bölüm etiketinin kaydırma sınırında kısmi görünmesi sonraki görsel iştir.
