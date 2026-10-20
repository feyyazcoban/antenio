# TV animasyon ve hareket rehberi

**20 Ekim · D106:** Devam rafından kompakt son kanal rafına geçiş yalnız başlık/kart/odak payını görünür kılacak kadar kayar. Son kanallar ekranın tepesine zorlanmaz; afiş raflarının üst hizalaması sürer. Başlık ölçüsü0dan başlamaz; ilk normal odak hedefi son ölçümle aynı kalır.

## 20 Ekim 2026 · Kanal paneli · D100

Mevcut soldan giriş 240 ms, çıkış 180 ms korunur. Kategori veya mini rehber sütunu açılırken panel genişliği 160 ms değişir; aynı anda en çok iki sütun vardır. Hareketi azalt açıkken bu süreler sıfırdır. Odak hareketi satırı büyütmez; odağın kimliği düz panel tutucusunda kalır. Kategori/mini rehber açıkken etkin olmayan kanal sütunu otomatik bring-into-view kaydırması yapmaz; listeye dönünce normal görünürlük kuralı sürer. Kapanma odağı çıkış animasyonu bitene kadar panelde hapsetmez; etkin olmayan içerik odak/erişilebilirlik ağacından çıkarılır.

## 19 Ekim 2026 · İlk açılış yükü · D096

Splash zaman çizgisi ve atlama davranışı aynı; köşe ışıkları boyuta göre cache edilir, opaklık animasyonu çizim sırasında okunur. Kaynak hazırlığı ve katalog sayımları ana thread dışına taşınır. İlk Activity kareleri gerçek saatle, normal animasyon ve sentetik büyük katalogla ölçülür; Application/instrumentation hazırlığı ve fiziksel sunum hariç. Ölçüm sonuçları STATUS/HANDOFF/QA'da.

## 18 Ekim 2026 · Kategori ve ana menü · D093

Native panel açılma 240 ms, kapanma 180 ms FastOutSlowIn; hedef hareket sırasında değişirse mevcut konumdan tersine döner. Büyük afiş listesi sabit beş sütun ölçüsünde yalnız yatay kayar (büyük yazı dört); kategori açıkken dört sütun görünür, beşinci sağda ekran dışında kalır (büyük yazı üç görünür). Satırlar yeniden gruplanmaz, kartlar çapraz hareket etmez. Normal ekranda açık panelin sağ afiş payı yaklaşık 7,5 dp, kapalıda 24 dp. Tam satır arama/boş içerik küçük alanları görünür genişliğe ölçülür. Küçük üst başlık sağdaki Sırala/Ara eylemlerini ekranda tutar. Kapanırken hâlâ çizilen kategori satırları fokus alamaz. Ana menü yazı/wordmark doğal genişliğini koruyarak açılan alanda ortaya çıkar. Hareketi azalt tüm konumları doğrudan hedefe alır.

**Tarih:** 13 Ekim 2026 · **Sürüm:** 0.1 · **Durum:** Araştırma destekli tasarım önerisi; gerçek TV doğrulaması bekliyor.

Hedef, kullanıcının kumandaya bastığında sonucu hemen görmesi, nerede olduğunu koruması ve yeni komut için animasyonun bitmesini beklememesidir. Canlı TV, film ve dizi akışlarının tamamında aynı davranış dili kullanılır. Aşağıdaki süre ve ölçekler ürün için başlangıç adaylarıdır; bilimsel olarak kanıtlanmış evrensel optimum değerler değildir.

## 1 Kanıtlar ve uygulamaya aktarımı

| Kaynak | Ne gösteriyor | Bu üründe çıkarım ve sınır |
| --- | --- | --- |
| [Google TV focus system](https://developer.android.com/design/ui/tv/guides/styles/focus-system) | TV'de odak temel durumdur; ölçek, çizgi, yüzey ve renk göstergeleri birlikte kullanılabilir. Örnek ölçekler 1.025, 1.05 ve 1.1. | Kart boyutuna göre vurgu; her bileşene aynı büyütme uygulanmaz. Kaynak tek bir animasyon süresi belirlemez. |
| [Apple Designing Fluid Interfaces, WWDC18](https://developer.apple.com/videos/play/wwdc2018/803/) | Apple'ın iPhone etkileşim örnekleri hızlı tepkiyi, yönlendirilebilir ve kesilebilir hareketi, tutarlı mekânsal davranışı vurgular. | D-pad girdisi animasyon sırasında da kabul edilir. Dokunmatik jest mesafeleri TV'ye kopyalanmaz; kontrollü IPTV karşılaştırması değildir. |
| [Material motion physics](https://m3.material.io/styles/motion) | Güncel sistem spring, spatial/effects ayrımı ve standard/expressive şemaları kullanıyor. Efektlerde aşma istenmiyor. | Sık kullanılan TV gezinmesinde düşük sıçrama; konum ve opaklık ayrı ayarlanır. Mobil Material varsayımlarının Compose for TV'ye aynen geldiği varsayılmaz. |
| [Heer ve Robertson, 2007](https://sites.stat.columbia.edu/gelman/communication/HeerRobertson2007.pdf) | 24 katılımcılı iki kontrollü deneyde, belirli veri grafiği geçişlerinde animasyon izleme ve değişimi anlama görevlerini iyileştirebildi. Karmaşık aşamalandırmanın yararı sınırlıydı. | Yer değiştiren öğenin kimliğini ve bağlamını korumak için kısa devamlılık. Çalışma TV kumandası, katalog veya ideal 160 ms süresi hakkında kanıt sağlamaz. |
| [Tversky, Morrison ve Bétrancourt, 2002](https://www.tc.columbia.edu/faculty/bt2158/faculty-profile/files/_Morrison_Betrancourt_AnimationCanitfacilitate.pdf) | Literatür değerlendirmesi, animasyonun kendiliğinden öğrenme avantajı sağlamadığını; hızlı/karmaşık hareketin anlaşılmayı zorlaştırabileceğini tartışır. | Açıklamak istediğimiz değişim yoksa hareket eklemeyelim; birçok satırı sırasıyla sahneye sokmak varsayılan olmasın. |
| [W3C etkileşim kaynaklı animasyon](https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html) | WCAG 2.3.3, web için AAA düzeyinde, zorunlu olmayan etkileşim hareketini kapatma olanağını tanımlar. | TV uygulamasında “Hareketi azalt” bir ürün gereksinimi olarak benimsenir; bu belge tek başına WCAG uygunluk beyanı değildir. |

Bu referanslar neden-sonuç ilişkisini ve davranış tercihini destekler. TiviMate, IPTVX veya başka bir IPTV uygulamasının milisaniye değerleri ölçülmedi; onlara aitmiş gibi sayılar kullanılmaz.

## 2 Ortak davranış sözleşmesi

1. **Önce tepki:** Odak mantığı ve görünür işaret aynı güncellemede yeni hedefe geçer. Ölçek/renk tamamlanmasını bekleyerek odağı geciktirmeyin.
2. **Kesilebilirlik:** Sağa giderken sola basılırsa güncel görsel değerden yeni hedefe yönlenin. Eski animasyonları sıraya eklemeyin.
3. **Tek yetkili hedef:** Geçiş sırasında OK yeni mantıksal odaktaki öğeye uygulanır. Eski karta yanlışlıkla basılmamalı.
4. **Mekân tutarlılığı:** Panel açılıp kapanırken aynı kenarı kullanır. Back önce mevcut katmanı kapatır; katalogdaki konum korunur.
5. **Sabit geometri:** Kartı görsel olarak büyütmek komşuların yerleşimini itmez. Çizgi ve büyüme için güvenli boşluk bırakılır.
6. **Bekleme eklememe:** Video hazırsa dekoratif geçişi bitirmek için bekletilmez. Yükleme bildirimi gerçek yükleme durumunu izler.
7. **Anlam ayrımı:** Odak, seçim, oynuyor ve basılı durumları ayrı görsellerdir. Hareket seçili öğe anlamına gelmez.
8. **Sürekli hareket yok:** Katalog dururken kartlar, arka plan veya odak çerçevesi kendiliğinden titreşmez.

## 3 Başlangıç hareket reçeteleri

Süreler tamamlanma hedefidir. Spring kullanıldığında sabit süre parametresi değildir; gerçek yerleşme süresi ölçülür. Figma veya süre tabanlı prototip için tabloda verilen süreler kullanılır.

| ID | Etkileşim | Başlangıç adayı | Hareket ve kesilme davranışı |
| --- | --- | --- | --- |
| MOT01 | Poster/kart odağı | 160 ms; ölçek 1.00 → 1.05 | Çizgi anında; ölçek güncel değerden. 120 ve 200 ms ile karşılaştır. |
| MOT02 | Yoğun kanal ve EPG satırı | 100 ms yüzey/çizgi; ölçek 1.00 | Metin ve zaman geometrisi sabit; komşu hücre kapanmaz. |
| MOT03 | OK basılı durumu | 80 ms; normal karta göre 1.02 hedef | Odaktaki 1.05'ten hafif geri çekilme; bırakınca geri. Eylem animasyona bağlanmaz. |
| MOT04 | Modal gezinme/panel açma | 240 ms; çıkış 180 ms | Kısa yatay hareket + opaklık; Back her an kapatabilir. Ana içeriği genişlik animasyonuyla tekrar tekrar dizme. |
| MOT05 | Katalog → ayrıntı | 220 ms; 16 dp yatay hareket + fade | Ana kartın kimliği korunur; büyük poster zoom zorunlu değil. |
| MOT06 | Aynı seviyede sekme/tür | 120 ms içerik fade | Tam ekran sağa-sola kayan büyük hareket yok; hedef hemen seçilir. |
| MOT07 | Otomatik liste kaydırma | 160 ms başlangıç adayı | Odaktaki öğeyi görünür alana almak için gereken mesafe. Yeni hedefte yeniden yönlenir. |
| MOT08 | Oynatıcı kontrolleri | giriş 120 ms; çıkış 100 ms | Opaklık; video yüzeyi yeniden boyutlanmaz. |
| MOT09 | Ses/altyazı paneli | giriş 180 ms; çıkış 140 ms | Kısa panel hareketi; seçim oynatmayı yeniden başlatmaz. |
| MOT10 | Yeni kapak yüklenmesi | 100 ms fade | Ölçüler sabit. Ekran dışı görsellere animasyon çalıştırma. |
| MOT11 | Hata, toast, bağlantı durumu | 120 ms fade | Sarsma yok. Kritik bilgi yalnız geçici toast içinde kalmaz. |
| MOT12 | Sonraki bölüm | geri sayım ayrı ürün davranışı | İptal her an erişilebilir. Yeni video süs geçişi için bekletilmez. |

Hızlı D-pad tekrarında odağı tek tek geçmiş animasyonların bitişine bağlamayın. Yavaş cihazda önce glow, blur ve gereksiz hareketi kaldırın; temel odak işaretini koruyun.

## 4 Easing ve spring uygulaması

Konum/ölçek için kesintilerde hızı koruyabilen spring başlangıcı; opaklık/renk için aşmasız geçiş önerilir. Compose belgeleri, hedef animasyon ortasında değiştiğinde spring'in hız sürekliliğini koruduğunu açıklıyor. Bu özellik akıcı his için yararlıdır; doğru odak mantığının yerine geçmez. [Compose animation customization](https://developer.android.com/develop/ui/compose/animation/customize)

İlk teknik deneme için **önerilen** spring: dampingRatio 1.0, stiffness 800. Bu bizim ayar adayımızdır; Material'ın resmî “TV spring” tokenı değildir ve 160 ms garantisi vermez. Kısa ölçek geçişlerinde hissi gerçek cihazda karşılaştırın.

Süre tabanlı prototipte mevcut Figma eğrileri başlangıç olarak korunabilir:
- Giriş: cubic-bezier(0.05, 0.7, 0.1, 1).
- Küçük durum değişimi: cubic-bezier(0.2, 0, 0, 1).
- Çıkış: cubic-bezier(0.3, 0, 1, 1).

Figma Smart Animate, gerçek spring'in hedef değişirken hızını birebir temsil etmeyebilir. Prototipte iyi görünmesi, kumanda tekrarlarının uygulamada doğru işlendiğini kanıtlamaz.

## 5 Mevcut Figma ile eşleme

[Motion · State](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV?node-id=161-244) içinde 100, 250, 400 ve 700 ms süreler; 1.1 odak ölçeği bulunuyor. Bu rehberin oluşturulması mevcut değişkenleri değiştirmedi.

- Genel `focus/scale=1.1` her öğeye uygulanmasın. Kart, küçük düğme ve yoğun EPG ayrı hareket rolleri alsın.
- 400/700 ms sık kanal/kart geçişinin varsayılanı olmasın; ihtiyaç yoksa kullanılmasın.
- Yeni tokenlar rol adı taşısın: focus/card, focus/dense, panel/enter, panel/exit, player/controls.
- Uzun ondalık değerler kullanıcıya gösterilen belgede 1.1, 0.12 gibi kısa yazılsın; ham snapshot değiştirilmesin.
- Her örnekte normal ve hareketi azalt durumu bulunmalı.

## 6 Hareketi azalt

Ayarlar içinde Normal / Azaltılmış hareket sunulur. Platformun kullanılabilir sistem tercihi de dikkate alınır; platform API'si teknik denemede doğrulanır.

Azaltılmış durumda ölçek, spring sıçraması, parallax, büyük kayma, shimmer ve sıralı girişler kapatılır. Odak çizgisi anında görünür; basit opaklık değişimi 0-80 ms olabilir. Zorunlu liste kaydırma en kısa anlaşılır davranışa indirilir. Yükleniyor metni veya sabit gösterge korunur. Bu ayar videonun kendi hareketini değiştirmez.

## 7 Akıcılık ve ölçüm

60 Hz için bir ekran yenileme aralığı yaklaşık 16.67 ms, 50 Hz için 20 ms, 120 Hz için 8.33 ms'dir. Gerçek aktif yenileme hızını ölçün. UI kare süresiyle video kare hızını karıştırmayın. Android'in render rehberi de geciken karelerin akıcılığı bozduğunu vurgular. [Android rendering](https://developer.android.com/topic/performance/issues/render)

**Önerilen yayın hedefleri:** odak işaretinde p95 ≤100 ms; sabit cihaz/ağ koşulunda 60 saniyelik gezinmede zamanında sunulamayan UI karelerinin oranı <%1. Bunlar gerçekleşmiş sonuçlar değil, başlangıç kabul hedefleridir. Mantıksal odak zaman damgası, ilk görünür tepki ve son yerleşme ayrı kaydedilir.

Kontrol senaryoları:
- 30 saniye hızlı sağ/sol ve yukarı/aşağı; ters yöne ani dönüş.
- EPG'de kısa ve uzun program hücreleri arasında geçiş.
- Poster yüklenirken odak değişimi; boş afiş ve katalog yenilenmesi.
- Video oynarken ses/altyazı panelini aç/kapat ve Back.
- Detaydan listeye dönüşte aynı öğe/scroll.
- Hareketi azalt açıkken tüm işlerin tamamlanması.
- Düşük bellekli TV, güncel Google TV, 1080p ve 4K.

En az 5 katılımcıyla küçük bir keşif turunda 120/160/200 ms odak ve 180/240/300 ms panel seçeneklerini sırası dengelenerek deneyin. Görev süresi, yanlış seçim, geri dönüş başarısı, akıcılık puanı ve rahatsızlık kaydedilsin. Bu örneklem güçlü bir evrensel tercih iddiası için yeterli değildir.

## 8 Tamamlanma koşulu

MOT01-12 ekranlara eşlenmiş; yeni girdi geçişi kesebiliyor; odak kaybolmuyor; azaltılmış hareket işlevleri koruyor; kare süreleri cihazda ölçülmüş; Figma node kimlikleri manifestte. Sonuçlar HANDOFF'a yazılmadan hareket sistemi “doğrulandı” olarak işaretlenmez.
