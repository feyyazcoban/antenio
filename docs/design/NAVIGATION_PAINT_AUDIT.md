# Menü aralığı doğrulaması

15 Ekim 2026 · Mevcut üretim APK0.14.13 değişmedi.

Önceki turda2.0yazı ölçeğindeki bazı görüntülerde Filmler/Diziler yakın göründüğü için yeni bir sorun olabileceği not edilmişti. Bu yorum yeni ölçümlerle doğrulanmadı; mevcut aralık korunuyor.

## Ölçüm

SearchNavigationTest artık içerik odaktayken ve beş menü öğesinin her birine sağ yön tuşuyla gidildiğinde bütün menü etiketlerinin gerçek karakter kutularını kontrol eder. Tam görünür düğmelerde solda/sağda en az13dp iç pay ve metin taşması olmaması gerekir. Önceki kontrol yalnız o anda odaklı etiketin yerleşimine bakıyordu. Ayarlara gitme/Geri ile dönme ve menüden içeriğe inme kontrolleri korunur.

VisualPreviewTest'in afişli ana sayfa/yenileme akışında da Filmler/Diziler karakter kutuları ölçülür; en az31dp aralık aranır.2.0fontta Filmler sağ sınırı495.5dp,Diziler sol sınırı527.5dp:32dp. Bu14dp sağ iç pay+4dp düğme aralığı+14dp sol iç payla tutarlıdır.

Önceki `hero-layout/font-2.0/presentation-home-poster.png` ve yeni `nav-paint/font-2.0/navigation-content-focused.png` görüntülerinde fiziksel piksel kontrolü de yapıldı.1920×1080görsellerde x800–1219,y80–131aralığındaki açık metin pikselleri (RGB kanallarının her biri>180) tarandı. Her ikisinde x990–1058aralığında69boş sütun bulundu. Bu yalnız bu iki görüntünün görünür aralığını karşılaştırır; genel kontrast veya font kalitesi onayı değildir.

## Kanıt

[Yeni büyük yazı ekranı](../../design/reviews/nav-paint/font-2.0/navigation-content-focused.png) · [Afişli akış](../../design/reviews/nav-paint/font-2.0/presentation-home-poster.png) · [Bütün etiketlerin ilk ölçümü](../../design/reviews/nav-paint/font-2.0/content-focused.json) · [Yenileme sonrası ölçüm](../../design/reviews/nav-paint/font-2.0/poster.json).

Testlerin font kapsamı ve sonuçları [QA kaydında](../project/native-nav-paint-qa.json). Üretim arayüzünde düzeltme yapılmadı ve yeni APK çıkarılmadı. Figma, fiziksel TV ve kullanıcı görsel kabulü açık kalır.
