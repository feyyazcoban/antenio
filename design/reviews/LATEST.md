# Güncel görsel inceleme

18 Ekim 2026 · **0.14.48 · Vitrinden ilk rafa kısa kaydırma (D078).** Sentetik aynı geçişte 381 dp yerine gereken 38,5 dp kaydırılır. Kart ve başlığı görünür; vitrin gereksiz yere tamamen kaybolmaz. Normal yazı, debug, yalnız 5558. [QA](../../docs/project/native-home-scroll-48-qa.json). Fiziksel kare süresi ölçülmedi.

![İlk devam kartında odak](home-scroll-0.14.48/continue-after.png)

## Önceki: 0.14.47

18 Ekim 2026 · **0.14.47 · S14b oynatıcı kontrolleri.** Sentetik yerel medya, yalnız 5558 test emülatörü, debug derlemesi. Yeni alt bilgi bloğu, kontrol simgeleri, odak etiketi ve sarma önizlemesi. Cihaz testi sonuçları ve release teslimi [QA](../../docs/project/native-player-controls-47-qa.json) ile [HANDOFF](../../docs/project/HANDOFF.md) içinde; görseller fiziksel TV kabulü değildir. [Davranış ve sınırlar](../../docs/design/PLAYER_PRESENTATION.md).

| Konu | Görüntü |
| --- | --- |
| Film, normal yazı; Seçenekler odağı | ![](player-controls-2026-10-18/native/movie-1.0.png) |
| Çubuk odağı; son derlemede OK/Geri ipucu | ![](player-controls-2026-10-18/native/scrub-1.0.png) |
| Canlı, normal yazı; geçici kanal çipleri | ![](player-controls-2026-10-18/native/live-1.0.png) |
| Film, 2 kat yazı; etiketler simgeye düşer | ![](player-controls-2026-10-18/native/movie-2.0.png) |
| Canlı, 2 kat yazı | ![](player-controls-2026-10-18/native/live-2.0.png) |
| Favori odağı, 2 kat yazı | ![](player-controls-2026-10-18/native/player-favorite-2.0.png) |
| Gizli kontrollerde sarma önizlemesi; sentetik medya tamponlama anı | ![](player-controls-2026-10-18/native/peek-1.0.png) |

Fiziksel kumanda/TalkBack, Figma ve kullanıcı görsel kabulü açık. Canlı kanal listesi S15, diğer Seçenekler S16 kapsamındadır.

Normal film/çubuk görselleri son derlemedendir; canlı, sarma önizlemesi ve büyük yazı görselleri yalnız odak ipucu düzeltmesinden önceki 0.14.47 adayındandır. Kullanıcının 18 Ekim talimatıyla bundan sonraki font matrisi mağaza öncesi "yayına hazırla" turuna bırakılır.

## Önceki: 0.14.31

16 Ekim 2026 · **0.14.31 tasarım önizlemesi · Klavye yalnız OK ile açılır.** Metin alanları odak alınca klavye açmaz; odaktaki alanda OK açar, Geri kapatır. Sentetik kaynak, ayrılmış emülatör, normal yazı. [Tasarım ayrıntıları](../../docs/design/KEYBOARD_ON_OK_31.md) · [QA](../../docs/project/native-keyboard-on-ok-31-qa.json) · [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.31.apk)

| Konu | Görüntü |
| --- | --- |
| Ara açılınca odak alanda, klavye kapalı (sürüm derlemesi) | ![](keyboard-0.14.31/ara-klavye-kapali.png) |
| Alanda OK: klavye açılır | ![](keyboard-0.14.31/ara-ok-ile-klavye.png) |
| Kaynak formunda alanlar arasında gezinirken klavye kapalı | ![](keyboard-0.14.31/kaynak-formu.png) |

## Önceki: 0.14.30

16 Ekim 2026 · **0.14.30 tasarım önizlemesi · Sezon rafları ve konum sayacı.** Dizi ayrıntısında her sezon için bir bölüm rafı ve "Sezon N  2 / 10" sayacı; film ızgarasında sabit başlık ve "30 / 80" konum sayacı. Sentetik kaynak, ayrılmış emülatör (hata ayıklama derlemesi), normal yazı. [Tasarım ayrıntıları](../../docs/design/TIVIMATE_SEASONS_30.md) · [QA](../../docs/project/native-tivimate-seasons-30-qa.json) · [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.30.apk)

| Konu | Görüntü |
| --- | --- |
| Dizi ayrıntısı: odak doğrudan ilk bölümde, bilgi bölümü yerinde | ![](tivimate-0.14.30/dizi-ayrinti.png) |
| Aşağı: sonraki sezonun rafı; ilk sezon "2 / 3" hatırlanır | ![](tivimate-0.14.30/dizi-sonraki-sezon.png) |
| Yarım kalan bölüm: ilerleme çizgisi ve "Devam et"; eksik görsel, uzun ve kısa ad aynı hizada | ![](tivimate-0.14.30/dizi-devam-et.png) |
| Filmler ızgarası: başlık sabit, odaktaki afişin sırası "30 / 80" | ![](tivimate-0.14.30/filmler-konum-sayaci.png) |

Fiziksel TV görünümü, kullanıcı görsel kabulü ve Figma açık.

## Önceki: 0.14.29

16 Ekim 2026 · **0.14.29 tasarım önizlemesi · Akıcılık ve TiviMate yapısı.** Teslim artık sürüm (release) derlemesi; odakta büyüme kaldırıldı; sol menüde yuvarlak simgeler; Canlı TV, Filmler ve Dizilerde kategori sütunu. Sentetik kaynak, ayrılmış emülatör (hata ayıklama derlemesi), normal yazı. [Tasarım ayrıntıları](../../docs/design/FLUID_TIVIMATE_29.md) · [QA](../../docs/project/native-fluid-29-qa.json) · [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.29.apk)

| Konu | Görüntü |
| --- | --- |
| Canlı TV: numaralı satırlar, üstte odaktaki kanal; menüde bulunulan bölüm dolu simge | ![](fluid-0.14.29/canli-tv.png) |
| Soldan kategori sütununa: seçili kategoride odak | ![](fluid-0.14.29/canli-tv-kategori.png) |
| Menü açılınca içerik menü kadar sağa kayar | ![](fluid-0.14.29/menu-acik.png) |
| Filmler: odak afişteyken kategori sütunu çekilir, 7 sütun | ![](fluid-0.14.29/filmler-izgara.png) |
| Filmler kategori sütunu: Favoriler, Geçmiş, Tüm filmler, sonra sağlayıcı sırası | ![](fluid-0.14.29/filmler-kategori.png) |

Fiziksel TV'de 0.14.29 kare ölçümü, kullanıcı görsel kabulü ve Figma açık.

## Önceki: 0.14.28

15 Ekim 2026 · **0.14.28 tasarım önizlemesi · Sol menü, TCL düzeninde Ara ve çıkış sorusu.** Kullanıcının TV'de gösterdiği iki örnek bizim renk, yazı ve odak stilimizle uygulandı. Sentetik kaynak, ayrılmış emülatör, normal yazı. [Tasarım ayrıntıları](../../docs/design/SIDE_NAV_SEARCH_28.md) · [QA](../../docs/project/native-side-nav-28-qa.json) · [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.28.apk)

| Konu | Görüntü |
| --- | --- |
| Kapalı menü: simge şeridi, seçili bölüm koyu yeşil dairede | ![](side-nav-0.14.28/menu-kapali.png) |
| Açık menü: içeriğin üstüne açılır, arka plan kararır | ![](side-nav-0.14.28/menu-acik.png) |
| Ara: üstte geniş alan, türe göre yatay raflar | ![](side-nav-0.14.28/ara.png) |
| Ara yazarken: en iyi eşleşmenin rafı alanın hemen altında, klavyenin üstünde | ![](side-nav-0.14.28/ara-yazarken.png) |
| Ana sayfada Geri: çıkış sorusu, odak Vazgeç'te | ![](side-nav-0.14.28/cikis-sorusu.png) |
| Rehber menüyle | ![](side-nav-0.14.28/rehber.png) |
| Dizi ayrıntısı: arka plan üst kenara kadar | ![](side-nav-0.14.28/dizi-ayrinti.png) |

Fiziksel TV görünümü, kullanıcı görsel kabulü ve Figma açık.

## Önceki: 0.14.27

15 Ekim 2026 · **0.14.27 tasarım önizlemesi · Yazarken arama.** Klavye açıkken başlık, filtre ve kategori satırları çekilir; sonuçlar alanın hemen altında, klavyenin üstünde kalır. Sentetik yerel kaynak, ayrılmış emülatör. [Tasarım ayrıntıları](../../docs/design/SEARCH_TYPING_27.md) · [QA](../../docs/project/native-search-typing-27-qa.json) · [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.27.apk)

| Konu | Görüntü |
| --- | --- |
| Ara: yazarken sonuçlar klavyenin üstünde, sayı etikette | ![](search-0.14.27/ara-yazarken.png) |
| Canlı TV bölüm araması | ![](search-0.14.27/canli-tv-yazarken.png) |
| Filmler bölüm araması: satır kartı | ![](search-0.14.27/filmler-yazarken.png) |
| Geri ile klavye kapanınca tam düzen | ![](search-0.14.27/filmler-klavye-kapali.png) |
| Büyük yazıda (2.0) iki sütun | ![](search-0.14.27/filmler-yazarken-2x.png) |

Fiziksel TV'de görünüm, kullanıcı görsel kabulü ve Figma açık.

## Önceki: 0.14.26

15 Ekim 2026 · **0.14.26 tasarım önizlemesi · UI bozukluk onarımı.** Aynı ayrılmış emülatörde, aynı testlerle alınmış önce/sonra görüntüleri. [Tasarım ayrıntıları](../../docs/design/UI_REPAIR_26.md) · [QA](../../docs/project/native-ui-repair-26-qa.json) · [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.26.apk)

| Konu | Karşılaştırma |
| --- | --- |
| Üst üste pencere artık arkada kesik görünmüyor | ![](repair-0.14.26/ust-uste-pencere.png) |
| Büyük yazıda hata gösterilince hatalı alan görünür | ![](repair-0.14.26/kurulum-hata-2x.png) |
| Saat dilimi: tam satır, ✓ seçim | ![](repair-0.14.26/saat-dilimi.png) |
| Kaynak seçimi: tek satır dili | ![](repair-0.14.26/kaynak-secimi.png) |
| Dizi bölümleri: Geri altında yarım satır yok | ![](repair-0.14.26/dizi-bolumleri.png) |
| Ayarlar kenarı | ![](repair-0.14.26/ayarlar-alt.png) |
| Devam rafı 24 dp kenarda | ![](repair-0.14.26/devam-rafi.png) |
| Oynatıcı tür etiketi ve yıldız | ![](repair-0.14.26/oynatici.png) |
| Rehber: şimdi çizgisi metnin altında | ![](repair-0.14.26/rehber.png) |
| Büyük yazıda rehber tam satır | ![](repair-0.14.26/rehber-2x.png) |
| Büyük yazıda arama alt kenarı | ![](repair-0.14.26/arama-2x.png) |
| Okuyucu tam satır | ![](repair-0.14.26/okuyucu.png) |

Figma ve kullanıcı görsel kabulü bekliyor; fiziksel TV'nin son bilinen sürümü 0.14.22.

## Önceki: 0.14.25

15 Ekim 2026 · **0.14.25 tasarım önizlemesi.** Ortak seçim işareti, kaynak formu ve arama kontrolleri düzenlendi. Aşağıdaki görüntüler aynı ayrılmış Android emülatöründe, sentetik kaynakla ve normal yazı boyutunda alındı.

## Kaynak formu

Alanlar panelden koyu yüzeyle ayrılıyor; çerçeveleri daha belirgin. Seçili yöntemin altında küçük bir çizgi var. Kumanda odağı dış çerçeveyle ayrıca gösteriliyor.

| Önce · 0.14.24 | Sonra · 0.14.25 |
| --- | --- |
| ![Önceki kaynak formu](polish-0.14.25/before-font-1.0/xtream.png) | ![Yeni kaynak formu](polish-0.14.25/font-1.0/xtream.png) |

## Arama

Tür filtreleri ve Temizle düğmesi daha hafif. Seçili tür, odak sonuç kartındayken alt çizgiyle okunabiliyor. Sonuç kartları ve sütun sayısı aynı.

| Önce · 0.14.24 | Sonra · 0.14.25 |
| --- | --- |
| ![Önceki arama](polish-0.14.25/before-font-1.0/soft-search.png) | ![Yeni arama](polish-0.14.25/font-1.0/soft-search.png) |

[Büyük yazıda form](polish-0.14.25/font-2.0/xtream.png) · [Büyük yazıda arama](polish-0.14.25/font-2.0/soft-search.png) · [Hata ve odak ayrı gösterilir](polish-0.14.25/font-2.0/validation.png).

149 birim testi (1 ek test atlandı), 135 farklı normal-font emülatör kontrolü ve 20 büyük yazı tekrarı başarılı. Lint: 0 hata/32 uyarı.

[Tasarım ayrıntıları](../../docs/design/CONTROL_POLISH_25.md) · [Test kaydı](../../docs/project/native-polish-25-qa.json) · [0.14.25 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.25.apk). Figma ve kullanıcı görsel kabulü bekliyor; fiziksel TV'nin son bilinen sürümü 0.14.22.

## Önceki raf düzenlemesi · 0.14.24

15 Ekim 2026 · 0.14.24 tasarım önizlemesi. Gerçek Android emülatör ekranları; yerel test kaynağından gerçek afiş ve logolar. Fiziksel TV bu tur güncellenmedi ve 0.14.22 sürümünde; Figma eşitlenmedi.

## Raf geçişi · yeni

Kumandayla raf değiştirildiğinde ekrana sığan başlık, afiş ve alt bilgiler birlikte hizalanıyor. Büyük yazıdaki üst kenar kalıntısı yalnız örtülmek yerine kaydırma yerleşimiyle giderildi. Aşağıdaki iki görüntü aynı film rafında, aynı yazı ölçeğinde alınmıştır.

| Önce · 0.14.23 / 2.0 yazı | Yeni · 0.14.24 / 2.0 yazı |
| --- | --- |
| ![Önceki raf yerleşimi](visual-0.14.23/font-2.0/home-movies-posters.png) | ![Başlığıyla hizalanan raf](shelves-0.14.24/font-2.0/home-movies-posters.png) |

Son raf da aynı biçimde yerleşir:

![Dizi rafı](shelves-0.14.24/font-2.0/home-series-posters.png)

[Raf davranışı](../../docs/design/HOME_SHELF_FRAMING.md) · [0.14.24 QA](../../docs/project/native-shelf-24-qa.json) · [0.14.24 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.24.apk)

Aşağıdaki 0.14.23 görüntüleri önceki hiyerarşi iyileştirmelerinin tarihli kaydıdır; yeni sürüm bu görsel dili korur.

## Ana sayfa

Açıklama görselin solunda daha dar bir alanda; tür/yıl aynı satırda. Yardımcı eylemler daha hafif. Dış kenarlar 24 dp olarak korunuyor.

| Önce · 0.14.20–22 düzeni | Yeni · 0.14.23 |
| --- | --- |
| ![Önce](visual-0.14.20/font-1.0/soft-home.png) | ![Sonra](visual-0.14.23/font-1.0/soft-home.png) |

## Film kütüphanesi

Başlık daha kompakt; kaynak ve arama düğmeleri afişlerle yarışmıyor. Afişler, kart oranları ve belirgin kumanda odağı korunuyor.

| Önce | Yeni |
| --- | --- |
| ![Önce](visual-0.14.20/font-1.0/soft-movies.png) | ![Sonra](visual-0.14.23/font-1.0/soft-movies.png) |

## Ana sayfa afiş rafı

Kısa/uzun başlıklarda yıl satırları hizalı. Eksik yıl da aynı alanı korur. Örnek kaynağın beş filmi var; sağdaki boş alan bu örneğin içerik sayısından kaynaklanır.

![Afiş rafı](visual-0.14.23/font-1.0/home-movies-posters.png)

## Büyük yazı

![Büyük yazıda afişler](visual-0.14.23/font-2.0/home-movies-posters.png)

Uzun adlar iki satır sonunda kısaltılır; ayrıntı ekranı tam içerik için korunur. Kayan önceki rafın üst sınırındaki ince yazı kalıntısı yumuşatıldı.

[Tasarım kararları](../../docs/design/VISUAL_POLISH_23.md) · [Kontrol kapsamı](../../docs/project/native-visual-23-qa.json) · [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.23.apk)

## Önceki tamamlanan iyileştirmeler

Bölüm kartları: [büyük yazı](episodes-0.14.22/font-2.0/series-episodes.png) ve [eksik kapak/Devam et](episodes-0.14.22/font-2.0/series-mixed-episodes.png).
Ses/altyazı paneli: [kesilmeyen odak çerçevesi](track-focus-0.14.21/font-2.0/tracks-subtitle.png).

Kullanıcının görsel kabulü ve fiziksel TV kontrolü bekliyor. Emülatör görüntüleri fiziksel akıcılık veya codec kabulü değildir.
