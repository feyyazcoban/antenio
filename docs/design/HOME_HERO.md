# Ana sayfa üst alanı

## 20 Ekim 2026 · İlk ekranda devam rafı · D101

Kullanıcı metin ve İzle grubunu yukarı, İzlemeye devam et rafının başlangıcını ilk ekrana istedi. D093'ün tam ekran görseli ve D097'nin karartması korunur: FillWidth/TopCenter, ekranın en solundan başlayan yatay karartma ve viewport boyundaki alt geçiş aynı kalır. Yalnız ön planın minimum alanı 420 dp olur; normal 960×540 dp ekranda yazı/İzle grubu 120 dp yukarı çıkar, devam başlığı ve kartın üst bölümü görünür. Arka plan aynı viewport yüksekliğinde, ilk rafın arkasına kadar çizilir. Uzun metin alanı gerektiğinde büyüyebilir; metin boyutları değişmez. D093'ün rafı mutlaka ilk ekranın altında tutan bölümü bu kullanıcı kararıyla değişir. Tek İzle, sağ/sol slayt ve D095'in tek hedefli Yukarı/Aşağı odağı korunur.

## 19 Ekim 2026 · Ekran kenarından başlayan karartma · D097

Görselin onaylanan tam genişliği ve oranı korunur. Yatay karartma en sol ekran kenarında, saydam kapalı menünün ikonlarının altında en koyu başlar; metin alanından sağa doğru yumuşakça azalır. Sol kenarı daha açık bırakan eski profil kaldırılır ve metin bölgesi biraz daha koyulaştırılır. Alt dikey geçiş ve tek İzle aynı kalır. Home kaydırması kullanıcı tarafından onaylandı; splash ayrı açık iştir.

## 18 Ekim 2026 · Tam ekran vitrin · D093

Vitrin içerik menüsünün sağıyla sınırlı değildir: ekranın sol kenarından sağ kenarına, en az viewport yüksekliğine uzanır (normal ekranda 960×540 dp). Backdrop oranı korunarak FillWidth/TopCenter ile tüm genişliği kaplar; geniş sinema oranında altta koyu zemin kalabilir, daha dar oranlarda taşan alt bölüm kırpılır. Kapalı menünün zemini saydamdır, ikonlar görselin üzerinde kalır; açıldıkça mevcut koyu zemin gelir. Metinler önceki sol güvenli hizalarını korur, metin arkasında karartma vardır. Poster-only keskin afiş 420 dp alan sınırını korur. D080'in 420 dp/ilk raf önizlemesi bölümü değişir; ilk raf vitrin altında başlar. Geri yukarı odağında vitrin üstü tam görünür olmalıdır. İki aşamalı Home kaydırma ve cold splash sorunu ayrı açık işlerdir.

**18 Ekim 2026 · 0.14.60 / D090:** İzle simgesi odakta ve odak dışında her zaman dolu, Material Symbols Rounded play_arrow FILL 1. 16 dp ikon kutusu ve tek İzle düğmesinin ölçüleri/etkileşimi aynı.

**18 Ekim 2026 · 0.14.58 / D088:** Sonraki slayt görselinin ön yüklemesi yalnız kaynak/görsel kimliği değişince başlar. İzle'ye odak girip çıkması yalnız otomatik slayt zamanlayıcısını yeniler; aynı görseli yeniden çözmez. 15/8 saniye, TalkBack, Hareketi azalt, sağ/sol slayt ve tek İzle davranışı aynı.

**18 Ekim 2026 · 0.14.52 / D082:** Vitrin üst etiketi 13→12 sp; Medium/mint/1,6 sp harf aralığı/tek satır aynı. Ortak YENİ EKLENEN ve KÜTÜPHANENDEN etiketi için geçerli; içerik başlığı ve açıklama ölçüleri değişmez.

**18 Ekim 2026 · 0.14.51 / D081:** Vitrin açıklaması normal ölçekte en fazla iki satır ve taşma sonu üç nokta. Büyük yazıdaki mevcut tek satır sınırı korunur. Hem gizli slayt ölçümü hem görünür metin aynı HeroText ile uygulanır; 420 dp yükseklik ve tek İzle değişmez.

**18 Ekim 2026 · 0.14.50 / D080:** Vitrin 372→420 dp: aşağıya 48 dp daha geniş görsel alanı. Üst kenar ve metin ölçüleri aynı; alt hizalı metin/İzle aşağı taşınır. Normal 960×540 dp ekranda ilk raf başlığı ve kartların bir kısmı görünür; Aşağı yalnız kartı tamamlayacak kadar kaydırır. D079 tek düğme korunur.

**18 Ekim 2026 · 0.14.49 / D079:** Güncel vitrinde tek eylem İzle. Ayrıntılar kaldırıldı; Sağ sonraki slayt, Sol önceki slayt/ilk slaytta menü; tek slaytta Sağ yerinde kalır. Menüden Sağ İzle'ye döner, Aşağı D078 ile ilk rafa geçer. İzle'nin film devamı/dizi bölüm seçimi ve yükleniyor davranışı değişmez. Aşağıdaki 0.14.13 ölçüleri tarihçedir; güncel büyük vitrin D067 ve D079'dur.

15 Ekim 2026 · Android0.14.13 · TV-010.

Kaynak seçimi ve bölüm etiketi ayrı bir üst satıra taşındı. Başlık, yıl, özet ve ana eylemler bu satırın altında yerleşir; kaynak düğmesi başlığın üstüne binmez. İç pay28dp yatay/20dp dikey, satırlar arası12dp. Mevcut32dp ekran kenarı,20dp köşe ve koyu/mint palet korunur.

Görsel varsa metin alanı kullanılabilir iç genişliğin%62'sini alır. Görsel yoksa tam iç genişliği kullanır; sağda boş bir afiş alanı ayrılmaz. Normal başlık36/41sp; font ölçeği1.5ve üzerindeyken28/33sp. Başlık en fazla iki satır, özet normalde iki/büyük yazıda bir satırdır. Uzun özet ayrıntı ekranından okunabilir.

Sadece dikey afiş varsa2:3oran korunur. Afişin üst payı normalde62dp,büyük yazıda76dp; kaynak seçimiyle arasında en az8dp boşluk kontrol edilir. Arka plan görselinin mevcut kırpma ve geçiş davranışı korunur.

Üst alanın çocuklarından biri odaklandığında ortak CardVisibility bütün üst alanı görünür tutar. Alt raf odaktayken üst alanın yukarı kayabilmesi normaldir; bütün sayfanın aynı anda görünmesi vaat edilmez.

## Kontrol kapsamı

HeroLayoutTest gerçek şifreli kayıt açılışında uzun başlık/uzun özet/kapaksız içerikle üst alanın, başlığın, kaynak seçiminin ve düğmelerin sınırlarını ölçer; sağ/sol kumanda geçişlerini kontrol eder. VisualPreviewTest gerçek afiş ve yatay görselli içerikle genel gezinmeyi ve afiş oranını kontrol eder. Son afiş payı düzenlemesinde kaynak düğmesinin altında8dp boşluk doğrulaması eklendi.

[Normal görünüm](../../design/reviews/hero-layout/font-1.0/soft-home.png) · [Büyük yazı/kapaksız](../../design/reviews/hero-layout/font-2.0/no-art.png) · [Büyük yazı/afiş](../../design/reviews/hero-layout/font-2.0/presentation-home-poster.png).

Son ölçümler ve hangi derlemede koşulduğu [QA kaydında](../project/native-hero-layout-qa.json). Normal/yatay görselli ekran görüntüleri afiş payı son düzeltmesinden önceki adaydan; değişen afişli ve kapaksız görüntüler son derlemeden yenilenir. Figma ve fiziksel TV onayı değildir.
