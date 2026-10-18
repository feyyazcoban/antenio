# Ana sayfa afiş ve devam rafları

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
