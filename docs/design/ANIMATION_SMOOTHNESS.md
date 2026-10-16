# Animasyon akıcılığı rehberi

**Tarih:** 16 Ekim 2026 · **Sürüm:** 0.1 · **Durum:** Araştırma destekli öneri. Kullanıcı onayı, Figma eşlemesi ve fiziksel TV ölçümü yok.

[MOTION.md](MOTION.md) hangi hareketin ne anlattığını ve MOT01-12 başlangıç adaylarını tanımlar. Bu belge, aynı hareketlerin düşük uçlu TV çiplerinde de akıcı görünmesi için algı bulgularını, Compose for TV kurallarını ve ölçüm yöntemini toplar. Çelişkide MOTION.md ve [karar kaydı](../decisions/DECISIONS.md) geçerlidir. Buradaki sayılar karar değildir.

## 1 Kapsam ve etiketler

Kapsam: odak, sol menü, panel/diyalog, liste kaydırma, sayfa geçişi, oynatıcı kontrolleri, yükleme ve açılış örtüsü. Videonun kendi kare hızı ve codec akıcılığı kapsam dışıdır; yalnız arayüzle kesiştiği yerde anılır.

"Akıcı" iki ayrı koşuldur: hareketin algısal tasarımı (süre, eğri, mesafe, kesilebilirlik) ve her karenin zamanında üretilmesi. Biri diğerini telafi etmez; ayrı ölçülür.

- **Bulgu:** hakemli çalışma veya derleme. Koşulu ve sınırı yanında yazılıdır.
- **Platform:** Android, Compose, Material, W3C resmî belgesi ya da androidx/AOSP kaynak kodu.
- **Proje:** yazım anındaki kod gözlemi. Kod değişebilir.
- **Hesap:** kaynaktaki formülden bizim çıkardığımız sayı. Ölçüm değildir.
- **Öneri:** bu belgenin önerisi. Kullanıcı onaylı karar değildir.

## 2 Bilimsel bulgular

| # | Bulgu | Kaynak | Sınır ve bu üründeki anlamı |
| --- | --- | --- | --- |
| B1 | Algısal işlem sabiti yaklaşık 0.1 sn; animasyon yanılsaması için ekran en az her 0.1 sn yenilenmeli. Durum geri bildirimi yaklaşık 1 sn'den seyrek olmamalı. Çok kısa büyük geçişte nesne sürekliliği kaybolur, çok uzunda kullanıcı bekler. Döngü yavaşlarsa hareket sırasında çizim kalitesi düşürülür ("Governor"). | [1] | Önerilen ~1 sn, büyük 3D ağaç dönüşü içindir; sık ve küçük TV odak hareketine aktarılmaz. Governor fikri: yavaş cihazda önce süsü azalt. |
| B2 | Kullanıcı tuş, telefon ve TV kanal değişimi gibi eylemlerde onda bir saniye içinde yanıt bekler. Ortalamanın ±%50'si kadar değişkenlik çoğu görevde tolere edilir; hayal kırıklığı ancak beklenenin en az iki katı gecikmede belirginleşir. | [2] | 1980'ler terminal çalışmalarının derlemesi; Miller'ın 2 sn sınırını da özetler [3]. Kumanda tepkisi için 100 ms hedefinin gerekçesidir, kesin eşik değildir. |
| B3 | Ani durum değişimi kullanıcıyı şaşırtır; sağlamlık, slow-in/slow-out ve takip hareketi anlaşılırlığı artırır. Kullanıcı animasyonu beklediğini hissetmemeli; kullanıcının doğrudan yönettiği nesneye efekt dayatılmaz. Nesne iki kare arasında boyunun yarısından fazla yer değiştirirse hareket kopuk görünür. | [4] | Self arayüzü 20-30 fps'e ayarlanmış. Yarım boy kuralı deney değil, animatör deneyimidir. |
| B4 | Çizgi film tekniklerinin doğrudan manipülasyon arayüzünde uygulanabilir olduğu ve geri bildirimi güçlendirdiği deneylerle gösterildi. | [5] | Masaüstü çizim editörü. "Ölçülü uygulandığında" koşulu korunur. |
| B5 | Slow-in/slow-out, nesne takibinde sabit hız, fast-in/fast-out ve uyarlamalı hızdan daha doğru sonuç verdi. Açıklama öngörülebilirliktir: göz takibi yaklaşık 100 ms balistik başlar, durmayı yaklaşık 100 ms gecikmeyle algılar; ani duruş hedefi kaybettirebilir. Yazarlar hızdaki ani değişimlerden kaçınmayı da hipotez olarak önerir. | [6] | 12 katılımcı, 1 sn'lik nokta bulutu, tek hedef. Hareketin ne zaman başlayacağını bilen göz takibi önceden başlatabilir; kullanıcının başlattığı odak hareketinde yavaş başlangıç daha az kritik olabilir, kendiliğinden başlayan harekette (otomatik gizlenen kontrol, yenilenen raf) daha önemlidir. |
| B6 | Öngörülebilirliği artır; basit geçiş kullan (öteleme ve büyüme/küçülme dönmeden kolay anlaşılır); örtüşmeyi azalt; karmaşık geçişi aşamalara böl; geçişi gerektiği kadar uzun, daha uzun olmayacak şekilde tut. Az hareketli geçişler 1 sn'den kısa olabilir. | [7] | Veri grafiği geçişleri, 24 katılımcı. Aşamalandırmanın yararı sınırlı kaldı. |
| B7 | Animasyon kendiliğinden öğrenme avantajı sağlamaz; hareketin yapısı anlatılan değişimle örtüşmeli ve kolay algılanmalı. | [8] | Açıklanacak değişim yoksa hareket eklenmez. |
| B8 | Animasyonlu kaydırma anlık sıçramaya göre okuma hatalarını %54'e kadar azalttı; sembol saymada 500 ms ile süre %24'e kadar kısaldı, okumada 300 ms ile kazanç yaklaşık %3 kaldı. | [9] | Masaüstü belge kaydırma; özet düzeyinde doğrulandı. Normal modda odakla kaydırmayı anlık sıçramaya çevirmeyin. |
| B9 | Örneklenmiş hareketin sürekli görünmesi için gereken örnekleme hızı nesnenin hızı ve ince ayrıntısıyla (uzamsal frekans) artar. | [10] | Psikofizik model. Çıkarım: aynı kare hızında hızlı kayan metin, yavaş büyüyen karttan daha kolay kesik görünür. |
| B10 | Ortalama kare hızı deneyimi kötü tahmin eder; kare süresi değişkenliği anlamlı etkiler. En kötü %5'lik kare hızı tabanı en iyi tahminciydi. | [11] | 33 kişi, üç oyun. UI raporunda ortalama yerine p95/p99/max okumanın gerekçesi. |
| B11 | Algılanan bekleme süresi ilerleme davranışına bağlıdır: sona yakın duraklama daha uzun, sona doğru hızlanma daha kısa hissedilir. Geriye akan ve yavaşlayan çizgili çubuk algılanan süreyi %11 kısalttı. | [12], [13] | Sahte ilerleme gerekçesi değildir; MOTION ilke 6 gereği gösterge gerçek durumu izler. |
| B12 | Geniş görüş alanlı hareket güçlü vection (sahte kendi-hareket hissi) üretebilir ve görsel kaynaklı hareket hastalığı riskiyle ilişkilidir. | [14] | Çoğunlukla VR ve geniş ekran derlemesi. Büyük TV'de tam ekran kaymayı azaltmak için temkinli gerekçe. |

**Boşluk:** TV kumandası/D-pad odak animasyonu süresini doğrudan ölçen hakemli çalışma bulunamadı. Süreler MOTION §7'deki küçük cihaz turuyla seçilmeli.

## 3 Platform rehberi ve kaynak kodu

| # | Kural veya olgu | Kaynak |
| --- | --- | --- |
| P1 | 60 fps için kare 16 ms altında üretilmeli (90 Hz 11 ms, 120 Hz 8 ms). Bütçe 1 ms aşılsa bile kare geç değil, tamamen düşer. 16-700 ms yavaş, 700 ms üstü donmuş karedir. 50 Hz'de bütçe 20 ms'dir (Hesap). | [17] |
| P2 | Compose her kareyi composition → layout → draw aşamalarıyla üretir; state hangi aşamada okunduysa o aşama ve sonrası yeniden çalışır. `offset {}`, `layout {}`, `graphicsLayer {}`, `drawBehind {}` içindeki okuma composition'ı atlar. | [18] |
| P3 | Pahalı işi `remember` ile tut; lazy listelerde stabil `key`; eşik durumu için `derivedStateOf`; okumayı ertele; composition içinde okunan state'e yazma. Sık değişen değeri modifier'a lambda ile ver. | [19], [20] |
| P4 | `graphicsLayer` yalnız çizimi etkiler, ölçü/yerleşimi değiştirmez; dönüşümler çizim lambdasını yeniden çalıştırmadan uygulanır; animasyonda lambda sürümü önerilir. Varsayılan stratejide alpha < 1 ekran dışı tampon açar; çakışmayan içerikte `ModulateAlpha` açmaz; `Offscreen` her zaman açar. | [21] |
| P5 | Spring, hedef animasyon ortasında değişince hız sürekliliğini korur. `animate*AsState` varsayılanı spring'dir (sönüm 1.0, `StiffnessMedium` = 1500). `tween` varsayılanı 300 ms ve `FastOutSlowInEasing`. | [22], [23], [29] |
| P6 | `BringIntoViewSpec` animasyon spec özelleştirmesi artık desteklenmez; odakla kaydırma varsayılan `spring()` kullanır. | [30] |
| P7 | tv-material yüzey ölçeği `tween` ile çalışır: odak 300 ms, odak kaybı 500 ms, basma 120 ms, bırakma 300 ms, eğri (0, 0, 0.2, 1). Animasyonlu değer composition'da okunup `graphicsLayer(scaleX = …)` olarak verilir. | [31] (androidx-main; token dosyası 2023'ten beri işlevsel değişmedi, 1.1.0 kaynak jar'ında ayrıca doğrulanmalı) |
| P8 | Compose pencere recomposer'ı sistemin `ANIMATOR_DURATION_SCALE` değerini izler ve `MotionDurationScale` olarak uygular; ölçek 0 iken animasyon ilk karede bitiş değerine atlar. `ValueAnimator.areAnimatorsEnabled()` geliştirici seçeneği 0 ya da pil tasarrufunda false döner. | [32], [33] |
| P9 | AOSP varsayılanı: tuşun ilk tekrarı 400 ms sonra, ardından 50 ms aralıkla gelir. Basılı tutulan D-pad saniyede yaklaşık 20 odak değişimi üretebilir; kumanda ve üretici ayarı farklı olabilir. | [34] |
| P10 | Baseline Profile, dahil edilen kod yollarında ilk çalıştırmadan itibaren yaklaşık %30 daha hızlı yürütme sağlar. Compose kütüphane olarak geldiği için cihazda önceden derlenmiş değildir; varsayılan profili vardır ama uygulamaya özgü profil önerilir. Performans release + R8 ile ölçülür; debug yavaştır. | [20], [24] |
| P11 | Material 3 süre tokenları 50-1000 ms (short1 50, short4 200, medium1 250, medium2 300). Eğriler: emphasized decelerate (0.05, 0.7, 0.1, 1), standard (0.2, 0, 0, 1), standard accelerate (0.3, 0, 1, 1). MOTION §4'teki üç eğri bunlardır. | [27], [35] |
| P12 | Google TV odak sistemi varsayılan ölçekleri 1.025, 1.05 ve 1.1'dir; değer öğe boyutuna göre değişir. Süre önermez. | [28] |
| P13 | WCAG 2.3.3 (AAA): etkileşimle tetiklenen, zorunlu olmayan hareket kapatılabilmeli; vestibüler bozuklukta baş dönmesi, bulantı ve baş ağrısı görülebilir. WCAG 2.2.2 (A): kendiliğinden başlayan ve 5 sn'den uzun süren hareket durdurulabilmeli. | [15], [16] |

## 4 Mevcut kodun durumu (Proje)

16 Ekim 2026'da okundu. [HomeScreen.kt](../../apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/HomeScreen.kt) yazım sırasında değişiyordu (menü süresi 240/180 ms'den 160/120 ms'ye indi); süreleri koddan ve [SIDE_NAV_SEARCH_28.md](SIDE_NAV_SEARCH_28.md) belgesinden yeniden okuyun.

- **Sol menü:** `animateDpAsState` + `tween` + `FastOutSlowInEasing`; Hareketi azalt'ta `snap()`. Genişlik yalnız `layout {}`, içerik kayması yalnız `graphicsLayer { translationX }` içinde okunur; etiketlerin görünmesi `derivedStateOf` eşiğiyle yalnız iki recomposition üretir. P2-P4 ile uyumlu. Kalan: menü sütunu her karede yeniden ölçülür (küçük ağaç, ölçülmeli). `tween`, hedef ortada değişince hızı sürdürmez (P5).
- **Kart ve düğme odağı:** `CardDefaults.scale` / `ButtonDefaults.scale` ile 1.025; süre ve eğri tv-material'den gelir (P7: 300/500 ms). MOTION'daki MOT01 adayı (160 ms) ile uyuşmaz. Hareketi azalt'ta hedef 1f olduğu için ölçek animasyonu çalışmaz.
- **Basılı tutma riski (Hesap):** 50 ms tekrarda 500 ms'lik odak kaybı ölçeği aynı anda yaklaşık 10 kartı canlandırabilir; her biri P7 gereği animasyon boyunca her karede yeniden oluşturulur. Ölçülmedi.
- **Odakla kaydırma:** [PilotApp.kt](../../apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PilotApp.kt) içindeki global `BringIntoViewSpec` yalnız kaydırma mesafesini belirler; hareket P6 varsayılan spring'idir. [CardVisibility.kt](../../apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/CardVisibility.kt) odak değişince eski isteği iptal eder; kesilebilirlik için doğru.
- **Kenar maskesi:** [DesignSystem.kt](../../apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/DesignSystem.kt) içindeki `scrollEdgeMask` `CompositingStrategy.Offscreen` kullanır; kaydırırken her karede ekran dışı tampon çizilir (P4). Düşük uçlu GPU'da ölçülmeli.
- **Doğru örnekler:** [LaunchCover.kt](../../apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/LaunchCover.kt) opaklığı `graphicsLayer {}` içinde okur, Hareketi azalt ve `areAnimatorsEnabled()` kontrol eder. Kütüphane ızgarası azaltılmış harekette `scrollToItem` kullanır.
- **Oynatıcı kontrolleri:** şu an geçişsiz görünür; MOT08 uygulanmadı.

## 5 Süre ve eğri önerileri (Öneri)

Değerler MOTION.md adaylarıyla aynı aralıkta tutuldu. Seçim fiziksel TV turundan sonra yapılır.

| Hareket | MOT | Normal | Uygulama önerisi | Hareketi azalt |
| --- | --- | --- | --- | --- |
| Odak işareti (çerçeve, zemin) | MOT01/02 | 0 ms, aynı kare | Anında; ölçeğin bitmesi beklenmez | Aynı |
| Kart odak ölçeği | MOT01 | 120-160 ms; 1.025 (mevcut) veya 1.05 (MOT01 adayı) | `spring(1.0, 800)` (~170 ms'de %95) ya da `tween(160)` + standard; odak kaybı odak süresinden uzun olmaz | 1.0 |
| Basılı tutulan D-pad | MOT01/07 | Dekoratif ölçek yok | `repeatCount > 0` iken ölçek hedefi 1.0; çizgi ve renk kalır | Aynı |
| Yoğun satır, EPG hücresi | MOT02 | 100 ms renk | Ölçek yok; metin geometrisi sabit | 0 ms |
| OK basılı | MOT03 | 80 ms | Eylem animasyonu beklemez | 0 ms |
| Sol menü aç/kapa | MOT04 | 160-240 / 120-180 ms | `FastOutSlowInEasing` veya standard; emphasized decelerate değil (§6 AK04) | `snap()` |
| Panel, diyalog | MOT04/09 | 180-240 / 140-180 ms | ≤24 dp öteleme + opaklık; ekranda kalan için standard, ekrandan çıkan için standard accelerate | Opaklık 0-80 ms |
| Liste otomatik kaydırma | MOT07 | Platform spring (%95 ~120 ms, Hesap) | Varsayılan korunur; her yeni odakta güncel konumdan yeniden yönlenir | `scrollToItem` |
| Aynı seviye sekme/tür | MOT06 | 120 ms içerik fade | Odak hemen hedefe geçer | 0-80 ms |
| Katalog → ayrıntı | MOT05 | 220 ms | 16 dp öteleme + fade, standard | Opaklık ≤80 ms |
| Oynatıcı kontrolleri | MOT08 | 120 / 100 ms | Yalnız opaklık, `graphicsLayer {}` içinde; video yüzeyi sabit | 0 ms |
| Kapak yüklenmesi | MOT10 | 100 ms fade | Ölçü sabit; ekran dışında çalışmaz | 0 ms |
| Yükleme göstergesi | - | ~1 sn'den kısa işte gösterge açılmaz; uzun işte durum metni | Gerçek ilerleme; sona yakın yapay duraklama yok (B1, B11) | Sabit metin |
| Açılış örtüsü | - | 220 ms opaklık (mevcut) | Değişiklik yok | 0 ms |

Spring süresi sabit parametre değildir. Kritik sönümlü (1.0) spring'in duruştan başlayıp yolun %95'ine varma süresi yaklaşık 4.74/√stiffness saniyedir (Hesap; Compose kütleyi 1 alır). Tam yerleşme görünürlük eşiğine kadar daha uzun sürer.

| stiffness | 200 (Low) | 400 (MediumLow) | 800 (MOTION adayı) | 1500 (Medium, varsayılan) |
| --- | --- | --- | --- | --- |
| %95 süresi | 335 ms | 237 ms | 168 ms | 122 ms |

## 6 Akıcılık kuralları (Compose for TV)

- **AK01** Animasyonlu değeri composition'da okumayın; `graphicsLayer {}`, `offset {}`, `layout {}` ya da `drawBehind {}` kullanın (P2-P4).
- **AK02** İçeriği her karede yeniden dizen genişlik, yükseklik veya padding animasyonu yapmayın (MOT04). Küçük ve sabit ağaçlar (menü sütunu) istisnadır ve ölçülür.
- **AK03** Eşik değişimlerini (`expanded`, "kaydırıldı mı") `derivedStateOf` ile alın; lazy listelerde stabil `key` kullanın (P3).

**AK04** Kare başına yer değiştirmeyi sınırlayın (B3, B9). 136 dp menü yolu için en büyük tek kare adımı (Hesap):

| Süre | Eğri | 60 Hz | 30 fps (kare düşerse) |
| --- | --- | --- | --- |
| 240 ms | FastOutSlowIn | 25 dp | 46 dp |
| 160 ms | FastOutSlowIn | 34 dp | 68 dp |
| 120 ms | FastOutSlowIn | 46 dp | 72 dp |
| 240 ms | emphasized decelerate | 72 dp (ilk kare) | 95 dp |
| 160 ms | emphasized decelerate | 86 dp (ilk kare) | 108 dp |

Emphasized decelerate kısa sürede yolun yarısından fazlasını ilk karede geçer; metin içeren büyük alanda sıçrama gibi görünebilir. Öneri: menü ve büyük ötelemede FastOutSlowIn/standard; emphasized decelerate yalnız opaklıkta veya küçük mesafede. Süre kısaldıkça kare düşmesinin görünürlüğü artar. Raf adımı (136 dp poster + 20 dp boşluk) varsayılan spring ile 60 Hz'de en çok ~37 dp/kare, 30 fps'te ~74 dp/kare ilerler (Hesap: tepe hız = ω·D/e); ikincisi yarım poster (68 dp) sınırını aşar.

- **AK05** Aynı anda çalışan animasyon sayısını sınırlayın. Basılı tutmada ölçek, gölge ve renk geçişlerini kapatın; odak çizgisi kalsın (B1 Governor, P7, P9). Öneri.
- **AK06** Konum ve ölçek için kesilebilen spring kullanın. `tween` kalırsa hızlı ters yönde takılma kabul edilmiş olur; MOTION ilke 2'ye göre test edin (P5).
- **AK07** Tam ekran opaklık ve maske ekran dışı tampon açar. Mümkünse opak örtü, `ModulateAlpha` veya yalnız değişen küçük alan kullanın. Blur, `RenderEffect` ve glow düşük uçta kapalı kalsın (P4, MOTION §3).
- **AK08** Hareket eden alanı küçük tutun. Menü açılırken bütün içeriği kaydırmak yerine yalnız menü ve örtü hareket edebilir (B12, AK04). Bu bir ürün kararıdır; SIDE_NAV ile birlikte değerlendirilmeli. Öneri.
- **AK09** Görsel decode, ağ ve katalog işi kare içinde yapılmaz; kapak geldiğinde ölçü değişmez (MOT10).
- **AK10** Sürekli animasyon (shimmer, nabız, otomatik carousel) yoktur; eklenir ve 5 sn'den uzun sürerse durdurulabilir olmalı (MOTION ilke 8, P13).
- **AK11** Bütçe aktif yenileme hızından hesaplanır: 60 Hz'de 16.67 ms, 50 Hz'de 20 ms. Video ekran modunu değiştirebilir; oynatıcı üstündeki UI o anki moda göre değerlendirilir [25].
- **AK12** Kritik yolculuklar (açılış, raf gezinmesi, menü aç/kapa, oynatıcı kontrolleri) için uygulamaya özgü Baseline Profile (P10). Yeni modül ve bağımlılık gerektirir; AGENTS gereği eklemeden önce kullanıcı onayı alınır. Öneri.

## 7 Hareketi azalt

- Tek kaynak `LocalReducedMotion`'dır; her yeni animasyon bu değeri okur. Azaltılmış durumda ölçek 1.0, `snap()` ve `scrollToItem` kullanılır; odak çizgisi ve renk kalır; opaklık 0-80 ms olabilir (MOTION §6).
- Sistem animasyon ölçeği Compose animasyonlarına zaten uygulanır (P8). Uygulama ayarı yine gereklidir: TV kullanıcısı geliştirici seçeneğini bulamayabilir; sistem ölçeği bütün animasyonları kaldırırken uygulama ayarı yalnız zorunlu olmayan hareketi kapatır ve anlam taşıyan kısa opaklığı korur (P13, B12).
- Öneri (onay bekler): ilk açılışta `areAnimatorsEnabled()` false ise "Hareketi azalt" açık başlasın.
- Azaltılmış modda işlev kaybı olmaz: odak görünür, kaydırma doğru hedefe gider, Back her katmanı kapatır.
- Her MOT için normal ve azaltılmış hareket ayrı test edilir.

## 8 Ölçme ve doğrulama

**Koşul:** release ya da profileable ve R8 açık APK; aynı TV, sürüm, kaynak ve font ölçeği; aktif yenileme hızı kaydedilir. Emülatör ve debug sonuçları fiziksel kabul değildir. Kullanıcı TV'si otomatik test veya fixture cihazı olarak kullanılmaz. Kayıt alanları için [FRAME_DIAGNOSTICS.md](../architecture/FRAME_DIAGNOSTICS.md), genel sınırlar için [PERFORMANCE.md](../architecture/PERFORMANCE.md).

**framestats + yerel çözümleyici:** eski AOSP belgesine göre `gfxinfo framestats` yakın geçmişteki ~120 kareyi (~2 sn) verir; `reset` ile pencere tek animasyona daraltılır [26]. Kısa senaryo için (kullanıcının onayladığı, otomatik teste ayrılmış cihazda):

```powershell
adb shell dumpsys gfxinfo <paket> reset
# senaryoyu oynat (ör. menüyü bir kez aç)
adb shell dumpsys gfxinfo <paket> framestats > .private/frames.txt
python tools/analyze_framestats.py .private/frames.txt --frame-budget-ms <aktif Hz bütçesi> --output .private/frame-report.json
```

[Çözümleyici](../../tools/analyze_framestats.py) yalnız Flags=0 karelerde FrameCompleted − IntendedVsync süresinin p50/p95/p99/max değerlerini ve açıkça verilen bütçenin aşımını raporlar. Ekranda düşen kare, tuştan piksele gecikme veya ürün kabulü çıkarmaz; bütçe varsayılanı yoktur.

- **Macrobenchmark `FrameTimingMetric`:** `frameDurationCpuMs` ve `frameOverrunMs` (pozitif değer düşen kare; Android 12+) için P50/P90/P95/P99 verir [24]. Projede henüz yok; ayrı test modülü ister (AK12 ile aynı onay).
- **Perfetto/sistem izi ve Compose izleme:** hangi composable'ın her karede çalıştığını gösterir [36].
- **Layout Inspector recomposition sayıları:** menü açılırken menü öğeleri yalnız eşik geçişlerinde, kart ölçeği sırasında yalnız ilgili kartlar recompose olmalı [37].

**Senaryolar:** S1 menüyü 20 kez aç/kapa; S2 rafta 30 sn sağa basılı tut; S3 hızlı ters yön; S4 ızgara ve EPG kaydırma; S5 video oynarken kontrolleri aç/kapa; S6 Hareketi azalt açık; S7 font 1.0/1.5/2.0. MOTION §7 senaryoları geçerlidir.

**Kabul hedefi önerileri:** MOTION §7'deki odak işareti p95 ≤100 ms ve 60 sn'de geç kare <%1 korunur. Ek öneri: rapor ortalama değil p95/p99/max verir (B10); Macrobenchmark varsa `frameOverrunMs` P95 ≤ 0. Bunlar ölçülmüş sonuç değil, başlangıç eşiğidir. Algı tercihi MOTION §7'deki 5 katılımcılı turla, sırası dengelenerek ayrıca alınır.

## 9 Kontrol listesi

- [ ] Hareket bir değişimi açıklıyor; yoksa eklenmedi (B7).
- [ ] Odak mantığı ve işareti aynı karede değişiyor; ölçek beklenmiyor.
- [ ] Animasyonlu değer yalnız `graphicsLayer {}`, `offset {}`, `layout {}` veya draw içinde okunuyor.
- [ ] Genişlik/padding animasyonu içerik ağacını yeniden dizmiyor.
- [ ] Konum/ölçek kesilebilir; ters yönde güncel değerden dönüyor.
- [ ] En büyük kare adımı 60 Hz ve 30 fps için hesaplandı; büyük alanda emphasized decelerate yok.
- [ ] Basılı tutmada dekoratif animasyon kapalı; odak çizgisi görünür.
- [ ] Tam ekran opaklık, maske, blur ve glow maliyeti cihazda ölçüldü.
- [ ] Sürekli animasyon yok; varsa ve 5 sn'den uzunsa durdurulabilir.
- [ ] Hareketi azalt açıkken ölçek 1.0, kayma yok, işlev tam.
- [ ] Süreler tablo aralığında; MOTION ile çelişki yoksa karar kaydına gerek yok, varsa nedeni yazıldı.
- [ ] Release APK'da framestats veya Macrobenchmark ile p95/p99/max kaydedildi; aktif Hz ve cihaz bilgisi raporda.
- [ ] Sonuç HANDOFF'a yazılmadan "akıcı" veya "doğrulandı" denmedi.

## 10 Kaynakça

**Bilimsel (hakemli)**

1. Card, S. K., Robertson, G. G., Mackinlay, J. D. (1991). The information visualizer, an information workspace. CHI '91, 181-186. [doi:10.1145/108844.108874](https://doi.org/10.1145/108844.108874)
2. Shneiderman, B. (1984). Response time and display rate in human performance with computers. ACM Computing Surveys 16(3), 265-285. [doi:10.1145/2514.2517](https://doi.org/10.1145/2514.2517)
3. Miller, R. B. (1968). Response time in man-computer conversational transactions. AFIPS Fall Joint Computer Conference, 267-277. [doi:10.1145/1476589.1476628](https://doi.org/10.1145/1476589.1476628) (tam metin okunmadı; [2] üzerinden)
4. Chang, B.-W., Ungar, D. (1993). Animation: from cartoons to the user interface. UIST '93, 45-55. [doi:10.1145/168642.168647](https://doi.org/10.1145/168642.168647)
5. Thomas, B. H., Calder, P. (2001). Applying cartoon animation techniques to graphical user interfaces. ACM TOCHI 8(3), 198-222. [doi:10.1145/502907.502909](https://doi.org/10.1145/502907.502909) (özet düzeyinde)
6. Dragicevic, P., Bezerianos, A., Javed, W., Elmqvist, N., Fekete, J.-D. (2011). Temporal distortion for animated transitions. CHI '11, 2009-2018. [doi:10.1145/1978942.1979233](https://doi.org/10.1145/1978942.1979233)
7. Heer, J., Robertson, G. (2007). Animated transitions in statistical data graphics. IEEE TVCG 13(6), 1240-1247. [doi:10.1109/TVCG.2007.70539](https://doi.org/10.1109/TVCG.2007.70539)
8. Tversky, B., Morrison, J. B., Bétrancourt, M. (2002). Animation: can it facilitate? International Journal of Human-Computer Studies 57(4), 247-262. [doi:10.1006/ijhc.2002.1017](https://doi.org/10.1006/ijhc.2002.1017)
9. Klein, C., Bederson, B. B. (2005). Benefits of animated scrolling. CHI '05 Extended Abstracts, 1965-1968. [doi:10.1145/1056808.1057068](https://doi.org/10.1145/1056808.1057068) (özet düzeyinde)
10. Watson, A. B., Ahumada, A. J., Farrell, J. E. (1986). Window of visibility: a psychophysical theory of fidelity in time-sampled visual motion displays. JOSA A 3(3), 300-307. [doi:10.1364/JOSAA.3.000300](https://doi.org/10.1364/JOSAA.3.000300) (özet düzeyinde)
11. Liu, S., Kuwahara, A., Scovell, J., Claypool, M. (2023). The effects of frame rate variation on game player quality of experience. CHI '23. [doi:10.1145/3544548.3580665](https://doi.org/10.1145/3544548.3580665)
12. Harrison, C., Amento, B., Kuznetsov, S., Bell, R. (2007). Rethinking the progress bar. UIST '07, 115-118. [doi:10.1145/1294211.1294231](https://doi.org/10.1145/1294211.1294231)
13. Harrison, C., Yeo, Z., Hudson, S. E. (2010). Faster progress bars: manipulating perceived duration with visual augmentations. CHI '10, 1545-1548. [doi:10.1145/1753326.1753556](https://doi.org/10.1145/1753326.1753556)
14. Keshavarz, B., Riecke, B. E., Hettinger, L. J., Campos, J. L. (2015). Vection and visually induced motion sickness: how are they related? Frontiers in Psychology 6, 472. [doi:10.3389/fpsyg.2015.00472](https://doi.org/10.3389/fpsyg.2015.00472)

**Standart**

15. W3C. WCAG 2.2, Understanding SC 2.3.3 Animation from Interactions. [w3.org](https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html)
16. W3C. WCAG 2.2, Understanding SC 2.2.2 Pause, Stop, Hide. [w3.org](https://www.w3.org/WAI/WCAG22/Understanding/pause-stop-hide.html)

**Platform belgeleri** (16 Ekim 2026'da okundu)

17. [Slow rendering](https://developer.android.com/topic/performance/vitals/render)
18. [Compose phases](https://developer.android.com/develop/ui/compose/phases)
19. [Compose performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices)
20. [Compose performance](https://developer.android.com/develop/ui/compose/performance) ve [stability](https://developer.android.com/develop/ui/compose/performance/stability)
21. [Graphics modifiers: graphicsLayer](https://developer.android.com/develop/ui/compose/graphics/draw/modifiers)
22. [Customize animations](https://developer.android.com/develop/ui/compose/animation/customize)
23. [Spring reference](https://developer.android.com/reference/kotlin/androidx/compose/animation/core/Spring)
24. [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/overview), [Compose için](https://developer.android.com/develop/ui/compose/performance/baseline-profiles), [Macrobenchmark metrics](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-metrics)
25. [Frame rate guidance](https://developer.android.com/media/optimize/performance/frame-rate)
26. [dumpsys](https://developer.android.com/tools/dumpsys) ve [AOSP framestats/reset açıklaması](https://android.googlesource.com/platform/frameworks/base/+/562ae3a/docs/html/training/testing/performance.jd)
27. [Material 3 easing and duration](https://m3.material.io/styles/motion/easing-and-duration)
28. [Google TV focus system](https://developer.android.com/design/ui/tv/guides/styles/focus-system)

**Kaynak kodu** (androidx-main / AOSP, 16 Ekim 2026'da okundu)

29. [AnimationSpec.kt](https://github.com/androidx/androidx/blob/androidx-main/compose/animation/animation-core/src/commonMain/kotlin/androidx/compose/animation/core/AnimationSpec.kt)
30. [BringIntoViewSpec.kt](https://github.com/androidx/androidx/blob/androidx-main/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/gestures/BringIntoViewSpec.kt)
31. tv-material [SurfaceScale.kt](https://github.com/androidx/androidx/blob/androidx-main/tv/tv-material/src/main/java/androidx/tv/material3/SurfaceScale.kt) ve [SurfaceTokens.kt](https://github.com/androidx/androidx/blob/androidx-main/tv/tv-material/src/main/java/androidx/tv/material3/tokens/SurfaceTokens.kt)
32. [WindowRecomposer.android.kt](https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/platform/WindowRecomposer.android.kt) ve [SuspendAnimation.kt](https://github.com/androidx/androidx/blob/androidx-main/compose/animation/animation-core/src/commonMain/kotlin/androidx/compose/animation/core/SuspendAnimation.kt)
33. [ValueAnimator.areAnimatorsEnabled](https://developer.android.com/reference/android/animation/ValueAnimator#areAnimatorsEnabled())
34. [ViewConfiguration.java](https://github.com/aosp-mirror/platform_frameworks_base/blob/main/core/java/android/view/ViewConfiguration.java)
35. [MotionTokens.kt](https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/tokens/MotionTokens.kt)
36. [Compose tracing](https://developer.android.com/develop/ui/compose/tooling/tracing)
37. [Layout Inspector](https://developer.android.com/studio/debug/layout-inspector)
