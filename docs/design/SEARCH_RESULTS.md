# Arama sonuçlarının yerleşimi

15 Ekim 2026 · 0.14.16 native önizleme. Figma yazımı değildir; kullanıcı görsel kabulü ve fiziksel TV kontrolü açıktır.

Arama sonuçları ekran genişliğine uyarlanır. Normal yazıda minimum 260 dp sütun genişliği kullanılır; 960 dp test ekranında üç sütun oluşur. 1,5 ve üzeri yazı ölçeğinde minimum 360 dp kullanılır; aynı ekranda iki sütun oluşur. Sütun sayısı sabit cihaz piksel çözünürlüğüne bağlanmaz.

Film/dizi afişleri 72×108 dp, kanal logoları 96×54 dp'dir. Görsel ile başlık arasındaki boşluk 14 dp; kart en az 132 dp yüksektir, metin gerektirdiğinde büyür. Başlık en fazla iki satırdır; tam adı erişilebilirlik açıklamasında korunur. Tür/yıl ve gerektiğinde kaynak adı ayrı satırlardadır. Eksik afişte kart başlığını görselin içinde tekrarlayan ikinci küçük yazı gösterilmez.

Ortak CardVisibility davranışı görsel ve yazıyı birlikte görünür alana alır. Izgara kenarları 7 dp, alt güvenli payı 28 dp'dir. Seçili kartın tamamı görünür kalır; odaksız komşu satır kaydırma sırasında kısmen görünebilir. Büyük yazıda sabit üst kontrollerin kapladığı alan, sonuçların görünür sayısını azaltır; bu davranış bütün ekranın erişilebilirlik onayı değildir.

Mevcut sorgu, tür/kaynak filtresi, detaydan geri dönüş, Back basamakları ve azaltılmış hareket davranışı korunur. Yeni bağımsız tasarım sistemi eklenmedi.

## Kanıt

VisualPreviewTest.searchCardsUseAvailableWidthAndStayVisibleWithRemote, gerçek afişleri yerel test sunucusundan yükler. Normal/1,5/2,0 yazı boyutlarında ilk satırın sütun yerleşimini, sağ/sol/aşağı kumanda hareketini ve seçili kartın alt güvenli payını denetler. Sağlayıcıya bağlantı açmaz.

- [Normal yazı, ilk satır](../../design/reviews/search-density/font-1.0/search-dense-first-row.png)
- [Büyük yazı, son satır](../../design/reviews/search-density/font-2.0/search-dense-last-row.png)

Testler dört film/dizi sonucu kullanır; bu kanıt bütün kaynak adı uzunlukları, canlı kanal dağılımları veya fiziksel televizyonların tamamını kapsamaz.
