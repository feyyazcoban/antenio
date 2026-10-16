# Geniş yerleşim ve ana sayfa kompozisyonu

Kullanıcının 15 Ekim isteği: sağ/sol boşluğu azalt, içeriği ekrana yay ve görsel kaliteyi bütün olarak yükselt. 0.14.6 önizleme teslimi.

Ortak kenar58→32dp: 960dp genişlikte içerik alanı844→896dp olur. Kaynakların başlığı/kartları aynı çizgidedir. Kurulumdaki eski48dp de ortak değere bağlandı. Kartların kendi iç boşlukları, metin okuma pencereleri ve odak çizgisinin güvenli alanı ayrı tutulur.

Ana sayfada geniş görsel artık bütün ekran oranına kırpılmaz; sağdaki%68alan içinde üstten hizalanır. Bu, incelenen Inception görselinde yüzlerin kesilmesini giderdi. Her sağlayıcı görselinin ideal odak noktası olduğu iddia edilmez. Başlık36sp, metin alanı%58; Ayrıntılar düğmesinde yanıltıcı oynatma simgesi kaldırıldı. Canlı raf kartları168dp ile aynı satırda daha fazla kanal gösterir; film/dizi kartları196dp kalır. Afiş-only fallback2:3oranını korur.

## Kontrol

İlk geniş yerleşim APK'sinde SourceLayoutFlowTest5vaka + VisualPreview ana ekran ve oynatıcı2vaka, üç fontta7/7 geçti. Kaynak başlığı/kart eşitliği testinde tag önce iç20dp padding'i ölçüyordu; tag gerçek dış kart sınırına taşındı. İlk headerDown sorunu açık odak hedefiyle giderildi.

Ana sayfa kompozisyonu sonrasında genişletilen kumanda kontrolü sağdaki canlı kartın kenara dayandığını buldu: kart sağ sınırı960dp idi. HomeCard'ın mevcut görünürlük yardımcısına7dp yatay pay eklendi; diğer çağrılarda varsayılan0dp korunur. İlk başarısız kanıt home-composition-initial-focus-failure.log ve home-focus-diagnostic.log içinde. Son doğrulamalar QA kaydında tutulur.

Görüntüler design/reviews/wide-layout/font-* ve source-presentation/font-* altındadır. Yerel fixture ve gerçek inceleme görselleridir; yayın/provider erişimi yapılmadı. FizikselTV0.14.4; yeni taslak test cihazındadır. Figma ve kullanıcı görsel kabulü açık.

Son kontrol kapsamı ve önceki/son APK ayrımı [QA](../project/native-wide-layout-qa.json) içinde. [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.6.apk).

0.14.26: HomeCard görünürlük payı satırın kendi 24 dp kenarına çıkarıldı; odaklı kart başlık ve "Tümünü gör" ile aynı çizgide durur, karşı kenarda önceki kartın kesik metni kalmaz ([UI_REPAIR_26](UI_REPAIR_26.md)).
