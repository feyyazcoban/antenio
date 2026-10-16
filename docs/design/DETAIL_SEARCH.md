# Detay ve arama ekranları

0.14.7 önizleme teslimi. Ortak32dp dış kenar korunur.

Arama başlığı24sp/30sp, filtreler ve sonuç sayısı aynı sıradadır. Böylece sonuçlar daha yukarıdan başlar. Tek kaynak varsa her kartta aynı kaynak adı tekrarlanmaz; çoklu kaynakta ad korunur. Kart12dp köşe, afiş6dp köşe kullanır. Canlı kanal logosu96×54alana, film/dizi afişi56×84alana yerleşir; logo afiş gibi daraltılmaz. Kaynak ve tür filtreleri mevcut davranışını korur.

Film başlığı36sp/41sp, özet en fazla3satırdır. Oynatıcı ses/altyazı açıklaması bu ekrandan kaldırıldı; oynatma eylemleri daha yalın görünür. Açıklama eylemi mevcut SourceDialog/RemoteTextReader ile başlık ve tam metni açar. İlk odak okuma alanında;↑↓sona ulaşır, Geri açıklama düğmesine döner. Bölüm kartlarının eski710dp genişlik sınırı kaldırıldı; üç sütun ortak dış kenarlar arasında yayılır. Odak büyümesi için görünürlük yardımcısı ve7dp yatay güvenli pay kullanılır.

## Kontroller

Yeni detailDescriptionAndWideEpisodesStayReadableWithRemote, yaklaşık1700karakter açıklamanın sonuna gerçek tuşlarla ulaşır; Geri odağına döner ve üç bölüm kartının genişliğini/tam sınırını ölçer. İlk sınır testi geçse de görselde büyüyen çerçevenin ekran altına değdiği görüldü; test7dp güvenli alanı kapsayacak şekilde sıkılaştırıldı. Bu nedenle ilk yeşil sonuç tüm görsel kaliteyi kanıtlamaz.

Mevcut VisualPreview genel görünüm ve afiş fallback; SearchNavigation Türkçe arama/tür filtresi/eski sorgu iptali ve Geri→sonuçlar→gezinme akışlarıyla birlikte sınanır. Testler yalnız5556yerel fixture ile yapılır. FizikselTV0.14.4korunur, Figma değişmez. Gerçek sağlayıcı/codec/performans ve kullanıcı görsel kabulü bu kontrollerle tamamlanmış sayılmaz.

Son kontroller ve aşama ayrımı [QA](../project/native-detail-search-qa.json) içinde. [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.7.apk).
