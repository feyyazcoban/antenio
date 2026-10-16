# Arayüz bozukluk onarımı

15 Ekim 2026 · 0.14.26 tasarım önizlemesi · BKL-020.

Kullanıcının "projeyi UI olarak analiz et ve bozuklukları düzelt" isteğiyle güncel 0.14.25 emülatör görüntüleri (normal, 1.5 ve 2.0 yazı) ve ekran kodu ekran gruplarına göre tarandı. Her bulgu iki bağımsız doğrulayıcıyla piksel ölçümü ve kod izi üzerinden kontrol edildi: 56 bulgu doğrulandı, 4 bulguda doğrulayıcılar ayrıştı, 1 bulgu reddedildi. Yeni özellik, yeni renk değeri veya ikinci tasarım sistemi eklenmedi; Google TV yönü, Ink rolleri, Roboto ve 24 dp dış kenar korunur.

## Ortak kurallar

- **Kaydırma kenarı.** Kaydırılan liste kenarda yarım bir metin satırı bırakmaz. Arka planı düz renk olan alanlarda içerik, kenarın 9–20 dp'lik bandında aynı zemin rengine solar (`scrollEdgeFade`); görsel arka planlı dizi ayrıntısında alfa maskesi kullanılır (`scrollEdgeMask`). Solma yalnız o yönde kaydırılacak içerik varken çizilir. Odaklı öğe kendi görünürlük payıyla solma bandının dışında kalır; bu pay listenin iç boşluğundan büyük tutulmaz, aksi halde ilk/son satır isteği karşılanamaz.
- **Tam satır.** Satır yüksekliği sabit listeler (rehber ızgarası, saat dilimi listesi) ve metin okuyucu görünür alanı tam satıra yuvarlanır. Okuyucu her adımda görünür metnin yaklaşık %65'i kadar, tam satıra yuvarlanarak kayar.
- **Seçim işareti.** Kompakt seçim düğmelerindeki 12 × 2 dp çizgi metin kutusunun 4 dp altına (dolgu içinde kalacak şekilde) iner ve odak dışında gezinmedeki gibi mint olur; odaktaki seçili düğmede koyu içerik rengi korunur. Tam genişlikteki liste satırları (saat dilimi) çizgi yerine sağda ✓ kullanır; kaynak seçimindeki "Tüm kaynaklar" diğer kaynak satırlarıyla aynı kart ve ✓ işaretini kullanır.
- **Devre dışı eylem.** Devre dışı düğme kumanda odağı almaz. Odaktayken devre dışı kalan düğme odağı kaybetmez ve 2 dp Ink.Muted çerçeve gösterir.
- **Üst üste pencere.** Alt pencere açılınca üst pencere görünmez olur; odak dönüş yolu korunur.

## Ekranlar

| Ekran | Değişiklik |
| --- | --- |
| Kaynak ekle | Alan kenarları solar; doğrulama hatasında hatalı alan görünür alana alınır; yöntem çiplerinin odak çerçevesi kırpılmaz; başlık, çip, alan ve ana eylem aynı sol kenarda. |
| Ana sayfa | Odaklı kart rafın 24 dp kenarında durur; sonraki raf alt kenarda solar; başlık satırları aynı yükseklikte; devam kartları ortak başlık/bölüm satırı ayırır; yalnız afişli vitrinde afiş karartılmaz; boş ana sayfa 24 dp kenarda. |
| Kütüphane | Boş bölümde odak etkin sekmede kalır; Favoriler yalnız favorilerin kategorilerini gösterir; afiş sütunları eşit; bölüm içi Temizle, Ara ekranıyla aynı. |
| Ayrıntı | Bölümlere inince açıklama Geri altında yarım kalmaz; yukarı dönüşte başlık grubu geri gelir; Baştan başlat diğer eylemlerle aynı satırda. |
| Oynatıcı | "Yayın açılıyor…" panel açıkken durum satırında; tür etiketi kendi koyu yüzeyinde; parça satırları seçimle yükseklik değiştirmez; desteklenmeyen parça nedenini ayrı satırda gösterir; süre gelince panel zıplamaz; favori yıldızı ortak simge. |
| Rehber | Izgara tam kanal satırı gösterir; saat kayması yalnız gerektiğinde tekrar edilir; şimdi çizgisi metnin altında; başlamamış program için doğru açıklama ve başlıktaki saat adı; yarım saatlik dilimlerde tam saat cetveli. |
| Ayarlar | Liste kenarları solar (odak payı 18 dp); odaklı satırda Açık etiketi görünür; Son güncelleme notu Kataloğu yenile satırına bağlı; Hakkında metni başlık kenarında. |
| Arama | Büyük yazıda sonraki satır solar; kart içeriği dikeyde ortalı; çok kaynakta kart yükseklikleri eşit. |
| Kaynaklar / pencereler | Liste kenarları solar; pencere içi sol kenarlar hizalı; rehber durum metni başlığı tekrarlamaz; geri yükleme açıklaması durumu tekrarlamaz. |

HTML prototipte eksik logo yedeği, review.html odak/birincil eylem ayrımı, kaynak adı tutarlılığı, kaydırılan dizi ayrıntısı, kısmi katalog odağı, karşılama okunabilirliği, kanal paneli simgeleri ve arama kanal karosu düzeltildi.

## Açık bırakılanlar

- Favorilerde canlı kanalların afiş ızgarasına uydurulması tasarım kararı gerektirir; uygulanmadı.
- Büyük yazıda Temizle düğmesinin alanla tam ortalanması için EntryField'a ortak bir yan yuva gerekir; şimdilik iki ekran aynı 4 dp düzeltmeyi kullanır.
- Ayrışan bulgular (Ink.Selected ile Ink.High tonu, simgelerin yazı ölçeğiyle büyümesi, açıklama penceresinin üst menüyle örtüşmesi) uygulanmadı.

Kaynak seçimindeki "Tüm kaynaklar" satırına "N kaynak" alt metni eklendi; kullanıcı onayı bekler. Figma değişmedi; kullanıcı görsel kabulü ve fiziksel cihaz kontrolü açık.
