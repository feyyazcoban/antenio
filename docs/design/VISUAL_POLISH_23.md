# Görsel hiyerarşi · 0.14.23

15 Ekim 2026. Kullanıcının arayüz kalitesine öncelik verme isteği kapsamında ana sayfa, katalog başlığı ve yardımcı kontroller düzenlendi. Google TV yönü, Roboto, nötr yüzeyler ve mint odak korunur. Bu çalışma Figma güncellemesi değildir.

## Vitrin

Normal yazıda metin alanı iç genişliğin %48’i; büyük yazıda eylemlerin sığması için %62. Görsel yoksa tam genişlik. Başlık normal yazıda 44/50 sp, büyük yazıda 28/33 sp. Tür ve yıl tek metadata satırında; açıklama normalde iki, büyük yazıda bir satır. Tam açıklama ayrıntı ekranından okunur. Vitrin en az 264 dp ve içerikle büyüyebilir; dış kenar 24 dp kalır.

Ayrıntılar birincil, Kanalları aç çevre çizgili ikincil eylem. Görsel üstündeki kaynak seçimi opak yüzeyini korur: ilk denemede saydam çerçevenin parlak görsel üstünde metni zayıflattığı görüldü ve düzeltilmiştir.

## Raflar ve katalog

Ana sayfadaki film/dizi kartlarında iki satırlık başlık alanı ve bir satırlık yıl alanı ayrılır. Yıl boşsa satır korunur. Eksik afişte başlık kart içinde tekrar edilmez. Canlı kanal başlıklarında gereksiz ikinci boş satır ayrılmaz. Kayıtlı izleme konumu bulunan kartların ilerleme sunumu korunur.

Tümünü gör ve düz zemin üzerindeki kaynak/arama/rehber eylemleri çevre çizgili olur. Seçili kaynak tonlu dolgu alır; kumanda odağı yine açık çizgi ve mint dolgu ile ayırt edilir. Katalog başlığı 26/32 sp ile afişlere daha az rakip olur. Beş afiş sütunu, 2:3 oran ve mevcut odak güvenli payı korunur. Ana sayfanın sabit menü altındaki ilk 7 dp bölgesi kaydırma sırasında zemine yumuşak geçiş yapar; önceki raftan kalan birkaç piksellik yazı parçalarını örter. Odak görünürlüğünün 7 dp üst payı değiştirilmez.

## Kontrol ve sınır

Gerçek afiş/logolar yerel test sunucusundan gelir; hesaplar sentetiktir. Kontroller ve sonuçlar [QA kaydında](../project/native-visual-23-qa.json). Fiziksel TV, Figma eşlemesi ve kullanıcının görsel kabulü bu emülatör kontrolüyle tamamlanmış sayılmaz.
