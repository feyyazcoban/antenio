# Ana sayfa afiş ve devam rafları

**20 Ekim · TV-010 / D108:** Home alt payı sabit28dp; son rafı üste zorlamak için ekran yüksekliğinde boş alan ayrılmaz. Son afiş rafı sayfa sonunda sabitlenir; kısa/uzun başlıklar arasında yatay geçiş dikey kaydırma yapmaz. İlk rafta Yukarı vitrine (yoksa aynı kartta), son rafta Aşağı ve satırın sağ ucunda Sağ aynı kartta kalır. Sol/Geri menüyü, menüde Sağ hatırlanan kartı korur.

**20 Ekim · D106:** Devam rafından kompakt son kanal rafına geçiş yalnız başlık/kart/odak payını görünür kılacak kadar kayar. Son kanallar ekranın tepesine zorlanmaz; afiş raflarının üst hizalaması sürer. Başlık ölçüsü0dan başlamaz; ilk normal odak hedefi son ölçümle aynı kalır.

**20 Ekim · Son izlenen kanal logoları / D105:** D102 beyaz logo zemini kaldırıldı. Logo kart yüzeyi üzerinde şeffaf;80×56dp alan/8dp iç pay korunur. Eksik logo simgesi Ink.Muted; ad13sp tek satır, kart/odak ölçüleri aynı.

## 18 Ekim 2026 · Listeleme başlıkları · D093

Ortak kütüphane/arama/favori afiş kartında poster–başlık aralığı 12 dp; başlık 16/22 sp Medium, bir/iki gerçek satır. Yıl/kaynak gerçek başlığın 4 dp altında 12/16 sp, muted rengin %78 opaklığıyla. Kısa ad için boş ikinci satır ayrılmaz. Canlı kart düzeni değişmez. Ana sayfa dizi rafındaki D086 yakınlık yaklaşımı listeleme kartlarına da uygulanır.

## 18 Ekim 2026 · Kompakt son kanal kartları · D091

Kullanıcı D084 kartlarını dağınık buldu. `recent` rafı artık 272 dp genişlik ve en az 100 dp yükseklikte yatay kartlardan oluşur. Tek düz yüzeyde 16 dp iç boşluk; solda oranı korunan 80×56 dp logo, 16 dp sonra sağda kanal adı ve 4 dp altında kaynak adı. Başlık 14/19 sp Medium, en fazla iki gerçek satır; kısa adda boş ikinci satır ayrılmaz. Kaynak yalnız çoklu kaynakta, 12/16 sp tek satır ve taşarsa üç nokta. Metin grubu dikey ortalanır; yükseklik gerekirse içerikle büyür.

Üst logo sahnesi ve gradient kaldırıldı. Eksik/yüklenemeyen logo ortak yer tutucuyu kullanır; kilit mevcut LockMark ile kanal adının yanında ve erişilebilir durumda belirtilir. Beyaz odak halkası, odakta büyümeme, OK/PIN, basılı OK, geçmiş sırası ve dönüş odağı korunur. Yalnız `recent` kartının D084 görsel yerleşiminin yerine geçer; diğer raflar aynı. Normal ölçekte kumanda/görsel kontrolü yapılır; yazı ölçeği matrisi kullanıcı “yayına hazırla” dediğinde.

## 18 Ekim 2026 · Dizi adı ve yıl birlikte · D086

Kullanıcının başlık ve yılın uzak durduğu geri bildirimiyle `series` rafında başlık artık boş ikinci satır ayırmaz: minLines 1, maxLines 2. Yıl/kaynak satırı gerçek başlığın 4 dp altında gelir (önce boş satır +8 dp). Afiş–başlık aralığı 8 dp, 160×240 dp afiş ve 16/22 sp Medium başlık korunur. D085'in sabit iki satırlık yer ayırma kısmının yerine geçer. Diğer rafların metin aralıkları aynı kalır. Font ölçeği matrisi mağaza öncesi kullanıcı talebini bekler.

## 18 Ekim 2026 · Daha büyük dizi rafı · D085

Ana sayfadaki “Dizi kütüphanen” (`series`) rafının afişi 136×204 dp yerine 160×240 dp; 2:3 oran korunur. Dizi adı 14/19 sp yerine 16/22 sp, Medium ağırlıkta ve iki satırlık yer tutar; sığmayan sonu üç nokta. Yıl/kaynak satırı 12 sp kalır. Raf başlığı, 20 dp kart aralığı ve odak halkası mevcut sistemi kullanır. Ortak görünürlük hesabı afiş ve alt metnin tümünü kapsar; OK ayrıntı, basılı OK hızlı menü ve Geri odağı korunur.

Bu değişiklik yalnız ana sayfanın dizi rafındadır. Film/favori/kanal/devam rafları ve ayrı Diziler kütüphane ekranı aynı kalır. Normal ölçekte ilgili ekran akışları kontrol edilir; yazı ölçeği matrisi kullanıcı “yayına hazırla” dediğinde. Figma ve fiziksel görsel kabul açık.

## 18 Ekim 2026 · Son izlenen kanallar · D084

Ana sayfanın `recent` rafında logo, kanal adı ve çoklu kaynakta kaynak adı tek kart içinde yer alır. Kart genişliği 200 dp, köşe 12 dp; logo bölümü 84 dp ve oranı korunan logo alanı 140×48 dp. Hafif dikey yüzey geçişi logoyu ayırır. Başlık 14 sp Medium, aynı yükseklikte iki satır; kaynak adı 12 sp, tek satır. Taşan metin üç noktayla kesilir. Logo yoksa ortak kanal yer tutucusu görünür.

Beyaz odak halkası kartın bütününü sarar; odakta ölçek/ek animasyon yok. OK kanalı mevcut erişim/PIN kapısından açar, basılı OK hızlı menüyü açar, dönüşte odak hatırlanır. Kilit rozeti ve erişilebilir durum korunur; çoklu kaynak adı erişilebilir duruma eklenir. D078 ilk rafın kısa kaydırması geçerlidir. Başlangıç Canlı TV rafı ve favori kartları mevcut biçimini korur. Font matrisi kullanıcı “yayına hazırla” dediğinde; Figma ve fiziksel görsel kabul açık.

Aşağıdaki 0.14.11 kaydı tarihçedir; `recent` kanal kartları için bu bölüm önceliklidir.

15 Ekim 2026 · Android 0.14.11 · TV-010 · Kullanıcı görsel incelemesi bekliyor.

Film ve dizi afişleri önce 196dp yatay kartın ortasında küçük görünüyordu. Ana sayfada artık 136×204dp, 2:3 kart kullanılır. Afiş, kütüphanedeki gibi kartı doldurur. Eksik afişte ortak Artwork yer tutucusu görünür; yatay bir görsel dikey afişmiş gibi kesilmez. Canlı kanal logoları 168dp yatay kartta oranını korur. Karışık favori rafı da içerik türüne uygun kart kullanır.

Devam kartı 242dp genişlik ve 48/66dp sabit yükseklik yerine 284dp genişlik kullanır. Yükseklik yazı ölçeğine ve kaynak adı varlığına göre hesaplanır. İki satırlık 13sp başlık, 11sp mint izleme konumu, 76dp kapak alanı ve ilerlemenin tamamını gösteren zemin çizgisi vardır. Bölüm numarası ve çoklu kaynak etiketi korunur. Ortak CardVisibility ile odaklı kart güvenli kenar boşluğuyla görünür alana taşınır.

32dp ekran kenarı, 20dp kart aralığı, 12dp köşe, Roboto, mevcut renk/odak sistemi korunur. Yeni hareket, bağımlılık veya ürün özelliği eklenmedi. Figma güncellenmedi; bu belge native değişikliği kaydeder.

## Kontrol

VisualPreviewTest.homePostersAndContinueRemainVisibleWithRemote normal, 1.5 ve 2.0 yazı ölçeklerinde geçti. Film/dizi afiş oranı, odaklı afiş ve başlığın tamamının görünmesi, sağ yön tuşu, detaydan ana sayfaya dönüş ve 45 saniyelik devam kartı kontrol edildi. Testin ilk denemesinde odaklı alt raf otomatik görünürlüğü üst rafa programatik kaydırmayla yarıştı; test önce odağı menüye taşıyacak şekilde düzeltildi. Bu bir ürün başarısı olarak sayılmadı; ilk log özel inceleme alanında korundu.

Görseller: [Normal film rafı](../../design/reviews/home-refinement/font-1.0/home-movies-posters.png), [normal devam kartı](../../design/reviews/home-refinement/font-1.0/home-continue.png), [büyük yazı film rafı](../../design/reviews/home-refinement/font-2.0/home-movies-posters.png). Gerçek kapak dosyaları yerel test sunucusunda kullanıldı; kullanıcı hesabı veya sağlayıcı yayınına erişilmedi. Fiziksel TV ve kullanıcının görsel değerlendirmesi henüz yapılmadı.

0.14.12 güncellemesi: devam kartı yüksekliği artık metnin gerçek yerleşiminden türetilir; sezon/bölüm ve süre ayrılır. [Güncel devam sözleşmesi](CONTINUE_CARDS.md) bu kart için önceliklidir.
