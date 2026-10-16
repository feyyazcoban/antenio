# Boş içerik ve gezinme · 0.14.9

Kütüphane boş durumu artık nedenini ayırır: arama/kategori eşleşmiyorsa Filtreleri temizle; favori eklenmediyse İçerikleri keşfet; türde kayıt yoksa Kaynakları kontrol et. İlk eylem sorgu ve kategoriyi temizler, kategoriye odak döner. Favorilerden keşfet ana sayfayı açar. 20dp köşeli yüzey,24sp başlık ve mevcut Action/CardVisibility kullanılır. Odaklı eylemle birlikte açıklama da görünür alana taşınır.

Arama sürerken yeniden arama önerisi yerine işlem bilgisi görünür. Tür filtresi etkinken Tümü önerilir. Boş ana sayfa gerektiğinde kayar ve kaynak kontrol eylemi belirgindir.

Büyük yazıda üst menü etiketleri birbirine yaklaşıyordu. IntrinsicSize.Max yerine aynı Roboto/TextStyle ile ölçülen metin genişliği + iki14dp iç pay kullanılır. Önceki taşma testi tek başına bunu yakalamıyordu; artık iç pay da doğrulanır. Normal ve2.0 görüntülerinde mesafe incelendi.

## Kontrol kapsamı

Final APK'de boş filtre/favori eylemleri, üst menü/ayarlar gidiş-dönüşü, boş/hatalı ana sayfada kaynaklara erişim üç fontta geçti. Türkçe arama/filtre testi önceki aynı arama kodunda üç fontta geçti. Yükleme beş işlem testi bu turun menü değişiminden önce üç fontta geçti; bileşeni değişmedi. Final normal fontta canlı/film/dizi/dil/favori/Geri, kaynak kurtarma ve indirme iptali geçti. [QA](../project/native-empty-state-qa.json).

Test görüntüleri için pencere geçişi beklemesi eklendi. İzole HomeScreen testinde PilotApp'ın zeminini atlamak açılış görselini arkada gösteriyordu; test aynı zeminle düzeltildi, altı görüntü yenilendi. Üretim açılış hatası diye raporlanmaz.

[Normal favoriler](../../design/reviews/empty/font-1.0/empty-favorites.png), [büyük yazıda menü](../../design/reviews/empty/font-2.0/navigation-large.png), [büyük yazıda kaynak hatası](../../design/reviews/empty/font-2.0/failed-home.png).

Açık: kaynaktaki türün gerçekten boş olduğu kütüphane dalı için ayrı etkileşim testi; boş ana sayfadaki hata başlığının daha belirgin olması; gerçek artwork/Figma/fiziksel performans ve kullanıcı görsel kabulü.
