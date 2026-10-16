# Geniş içerik ve ana sayfa vitrini

15 Ekim 2026 · Android 0.14.20 · BKL-020.

Kullanıcının daha güçlü görsel tasarım ve daha az yan boşluk isteği doğrultusunda mevcut Google TV yönü geliştirilmiştir. Yeni özellik eklenmez; ortak bileşenler, Roboto ve sakin mint palet korunur.

- Ortak yatay kenar 32 dp yerine 24 dp. 960 dp ekranda içerik alanı 896 dp'den 912 dp'ye çıkar. Menü, katalog, arama ve detay aynı ölçüyü kullanır. Odak çerçevesi için kenarda boşluk kalır; fiziksel TV'nin görüntü kırpması ayrıca kontrol edilmelidir.
- Üst menünün üst/alt payı 27/17 yerine 18/14 dp. Menü içindeki yazı ve düğme ölçüleri değişmez.
- Ana sayfa vitrini minimum 264 dp; kaynak/etiket üstte, başlık/özet/eylemler altta. Büyük yazı ve uzun içerikte doğal olarak büyür; sabit yükseklikle metin kırpılmaz.
- Yatay görsel sağdaki alanın %78'ini kaplar. Nötr arka planla yatay geçiş ve altta hafif karartma, görsel ile metni birleştirir. Fotoğrafın oranı bozulmaz.
- Normal başlık 40/46 sp; büyük yazıda mevcut 28/33 sp korunur. Eksik görselde metin tam genişlik kullanır; yalnız afiş varsa mevcut 2:3 sunum korunur.
- Büyük yazılı katalogda başlığın kaybolma kontrolü, listenin üst iç payını da hesaba katar. Önceden kaydırma koordinatında kaybolmuş görünen başlığın son birkaç pikseli ekranda kalabiliyordu; durma noktası bu iç pay kadar ileri alınır.

Bu bir native uygulama revizyonudur; Figma eşitlenmiş veya kullanıcı tarafından onaylanmış değildir. Önceki HOME_HERO belgesindeki kenar, minimum yükseklik ve normal başlık ölçülerinin yerine bu değerler geçer. Güncel test kapsamı STATUS/HANDOFF içindedir.

[Normal ana sayfa](../../design/reviews/visual-0.14.20/font-1.0/soft-home.png)
