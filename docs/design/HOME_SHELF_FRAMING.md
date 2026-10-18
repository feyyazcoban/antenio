# Ana sayfa raf geçişi · 0.14.24

**0.14.58 · D088:** Kartın kaydırma sırasında kırpılmış durumdan görünür hâle gelmesi, sürmekte olan görünürlük isteğini iptal etmez. Ölçü/kırpılma değişimleri sırayla değerlendirilir; odak veya gerçek viewport/hedef değişimi eski işi hâlâ keser. D078 mesafesi ve 7/27 dp payları aynı. Test artık yalnız son konumu değil, hareket sürerken iptal sayısını ve karelik konumu da kaydeder. Fiziksel TV kare süresi kabulü ayrıca açık.

**0.14.48 güncellemesi · D078:** Fiziksel TV bildirimi üzerine, vitrin altındaki ilk raf artık ekranın en üstüne taşınmaz. Başlık ve kartı 27 dp alt güvenlik payıyla görünür kılacak en kısa kaydırma kullanılır. Alt raflar ve vitrinsiz ana sayfa aşağıdaki D051 davranışını korur. Önce/sonra kontrolü normal yazıda yapılır; font matrisi kullanıcının mağazaya hazırlık talebini bekler.

15 Ekim 2026. Önceki sürüm üst kenardaki yazı kalıntısını yalnız yumuşatıyordu. Bu sürümde ekrana sığan rafın başlığı, kart görseli ve alt bilgileri birlikte kadraja alınır.

## Davranış

- Odak bir raf kartına geldiğinde ölçülmüş başlık yüksekliği, başlık/kart aralığı ve görünür alan yüksekliği aynı görünürlük isteğinde değerlendirilir. İkinci bir bağımsız kaydırma işi başlatılmaz.
- Başlık, kart ve 27 dp alt güvenlik payı birlikte sığıyorsa başlık menünün altında yaklaşık 7–9 dp payla yerleşir. Böylece önceki raftan rastlantısal birkaç piksellik yazı kalmaz.
- Aynı rafta sağ/sol hareket yatay kaydırır; dikey raf konumu korunur. Odak değiştiğinde eski görünürlük isteği iptal edilir.
- Başlık ve kart bütünü sığmıyorsa ana kartın görünürlüğü önceliklidir; büyük yazı zorla küçültülmez. Bu sınır, bütün olası sağlayıcı metinlerinin sığdığı anlamına gelmez.
- Son raf için ölçülmüş raf yüksekliğine göre gerekli alt kaydırma payı ayrılır. Sabit büyük boşluk bütün raflara eklenmez. Yükseklikler raf kimliğiyle tutulur; son raf katalog yenilemesiyle değişebilir.
- Ana vitrin, kütüphane, arama ve bölüm kartlarının mevcut görünürlük davranışı korunur. Yeni bağlam parametreleri yalnız ana sayfa raf kartlarına verilir.

## Kontrol

[QA kaydı](../project/native-shelf-24-qa.json). Yeni test ilk kanaldan gerçek D-pad ile filmlere, dizilere ve geri kanallara üç tur gider; başlık ve bütün kart sınırlarını kontrol eder. Sekiz sağ/sol çifti sonunda aynı rafta kalındığı doğrulanır. Önceki afiş/Devam et testinde de başlığın görünürlüğü ve üst hizası artık ölçülür; programatik kaydırmadan önce menüye odak verilmesi test altyapısı davranışıdır.

Fiziksel TV akıcılığı, bütün katalog çeşitleri ve kullanıcı görsel kabulü bu kontrollerden çıkarılamaz. Figma eşitlenmedi.

## Ek yön değişimi kontrolü

[Ek QA](../project/native-shelf-reversal-qa.json): sekiz zıt tuş çiftinden sonra aynı film kimliği, OK ile doğru detay ve Geri ile aynı kart artık ayrı doğrulanır. 1.5/2.0 geçti; normal yazı testi emülatör askıda olduğu için sonuç bekliyor. Üretim APK değişmedi. Girdi gecikmesi ölçülmüş sayılmaz.

0.14.26: Sonraki raf alt kenarda 20 dp içinde zemine solar; "Tümünü gör" de kartlar gibi 7/27 dp görünürlük payı kullanır. Başlık satırları kompakt düğme yüksekliğindedir ([UI_REPAIR_26](UI_REPAIR_26.md)).
