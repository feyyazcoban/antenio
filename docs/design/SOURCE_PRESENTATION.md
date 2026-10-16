# Kaynak yönetimi görsel düzeni

0.14.6 önizlemesi; fiziksel TV 0.14.4 üzerinde kalır.

Kaynakların başlığı ve kaynak kartları ortak 32dp dış kenarı paylaşır. Listeye ayrıca yatay dolgu eklenmez; odak eylemleri kartın 20dp iç alanında kalır. Kaynak grupları ortak20dp köşe, seçme penceresindeki seçenekler12dp köşe ve beyaz odak çizgisi kullanır. Seçili durum ile kumanda odağı ayrı kalır.

Üst başlıktaki eylemlerden ↓, görünür ilk kaynak kartındaki Bilgiler eylemine gider. Bu yön açıkça belirtilmediğinde mekânsal odak araması sağdaki Kaldır eylemini seçebiliyordu. Kartlar arasında aynı eylem sütununda gezinme, düzenlemeden dönüş ve kaldırma iptali odağı korunur. Uzun adın tamamı mevcut Bilgiler okuma alanından erişilir.

## Kontrol kapsamı

SourceLayoutFlowTest yeni vakası16kartın tamamını gerçek kumanda tuşlarıyla gezer. Başlığın sabitliği, başlık/kart sol kenar eşitliği, tam kart görünürlüğü ve dört eylemin kart sınırları içinde kalması ölçülür. Mevcut dört vaka kaynak seçimi, uzun ad okuma, düzenlemeden dönüş, kaldırmayı iptal, yenileme hatası ve bilgi panelini denetler.

Önceki görünüm font2.0 ile before-font-2.0 klasöründe saklanır. İlk yeni kontrol başlıktan ↓ beklentisinde başarısız oldu; testin beklentisini gevşetmek yerine açık odak hedefi eklendi. Özel kanıt source-style-initial-focus-failure.log. Test kaynakları yereldir; gerçek hesap/sağlayıcı kullanılmaz. Kullanıcı kabulü, fiziksel akıcılık ve gerçek içerik kapakları bu kontrolün kapsamı değildir.

Son kontrol kapsamı ve önceki/son APK ayrımı [QA](../project/native-wide-layout-qa.json) içinde. [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.6.apk).
