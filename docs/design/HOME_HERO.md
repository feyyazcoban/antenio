# Ana sayfa üst alanı

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
