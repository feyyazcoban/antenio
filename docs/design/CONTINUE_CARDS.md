# Devam kartlarında uzun içerik ve çoklu kaynak

15 Ekim 2026 · Android 0.14.12 · TV-010.

0.14.11 üzerinde aynı native test çalıştırıldığında 2.0 yazı ölçeğinde `S120 · B350 · 1:22:36` satırının süre kısmı üç noktayla kesiliyordu. `ContinueCardLayoutTest` metnin gerçek yerleşimini okuyarak bunu doğruladı. Yeni sürüm sezon/bölüm ve süreyi ayrı satırlarda gösterir.

Kart yüksekliği toplu bir sp değerinden hesaplanmaz; başlık, kaynak adı, bölüm bilgisi ve süre gerçek yükseklikleriyle yerleşir. Üstte14dp, altta17dp iç pay bırakılır. 284dp genişlik, iki satırlı başlık, mint süre ve ilerleme çizgisi korunur. Odak kaydırma ölçüsü animasyonlu kartın dışındaki kapsayıcıda tutulur. Erişilebilir durum açıklaması kaynak, sezon/bölüm ve izleme konumunu içerir.

Kapak bulunamadığında dar76dp görsel alanda uzun başlık ikinci kez kırpılarak gösterilmez; film simgesi görünür. Ortak Artwork bileşeninin diğer kullanımları başlıklı yer tutucuyu korur.

## Kanıt ve sınırlar

- Sekiz devam kartı, iki kaynak, uzun başlık/kaynak adı, sezon120/bölüm350 ve1:22:36 izleme konumu sentetik şifreli kayıtlardan gerçek uygulama açılışıyla yüklenir.
- Normal,1.5ve2.0 yazı ölçeğinde bütün kartlar sağ yön tuşuyla gezildi; görünür alan ve süre metninde üç nokta olmaması doğrulandı. Gerçek afişli normal film devam kartı ve afiş raflarının testi de üç ölçekte geçti.
- İlk izole `setContent` test kurulumu Compose idle zaman aşımı üretti. Geometri tanısı sürekli ölçü değişimi göstermedi. Test gerçek kayıtlı kaynak açılışına taşındı; eski APK bu kurulumda idle hatası değil, somut süre kesilmesi hatası verdi. İzole testin beklemesi üretim donması olarak sunulmaz.
- Ekran görselleri [normal uzun içerik](../../design/reviews/continue-layout/font-1.0/card-0.png), [büyük yazı](../../design/reviews/continue-layout/font-2.0/card-0.png), [son kart](../../design/reviews/continue-layout/font-2.0/card-7.png), [gerçek kapaklı film](../../design/reviews/continue-layout/font-1.0/movie.png).

Fiziksel TV, kullanıcı görsel kabulü ve Figma eşleşmesi açık. Uzun başlık ve kaynak adı için mevcut iki/tek satırlı kısaltma sürer; tam metin erişilebilir açıklamalarda mevcuttur. Büyük yazıda odaklı rafa kaydırılırken odaksız üst hero kısmen ekran dışında kalabilir.
