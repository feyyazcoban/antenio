# Ses ve altyazı paneli

0.14.5 önizleme teslimi; fiziksel TV 0.14.4 sürümünde kalır. Panel ayarlar ile aynı440dp genişlik,24dp dış köşe ve koyu zemin kullanır. Başlık/Kapat ve açıklama sabit; seçenekler kayar. Seçenek satırları12dp köşe,14sp/20sp metin ve sabit boyutta beyaz odak çerçevesi taşır. Seçili seçenek hem yüzey rengi hem onay işaretiyle ayrılır; selected/RadioButton semantiği eklenmiştir. Onay simgesi ayrıca okunmaz.

TrackSelectionParameters değişikliği dinlenir: altyazı Kapalı seçimi yalnız track grup değişikliği olmasına bağlı kalmaz. Desteklenmeyen satır etkinleştirilemez. Yalnız yayındaki parçalar listelenir; dış altyazı/arama özelliği eklenmez. Sabit pencere odağı alındıktan sonra ilk Kapat odağı istenir.

## Kanıt

Yerel video iki ses dili ve bir gömülü altyazı içerir. Üç yazı ölçeğinde ses seçimi, altyazı kapat/aç, selected semantiği, paneli yeniden açınca tercih ve ilk odak, sabit başlık ve odaktaki satır sınırları kontrol edildi. Önceki oynatıcı düğme/track dönüş testi de her ölçekte geçti. Bu kanıt uzun sağlayıcı etiketleri, çok sayıda parça, desteklenmeyen codec veya boş track listesinin eksiksiz doğrulaması değildir; onlar sıradaki kontroldür.

| Ölçek | Ses | Kapalı altyazı | Seçili altyazı |
| --- | --- | --- | --- |
| 1.0 | [Görüntü](../../design/reviews/tracks-panel/font-1.0/tracks-audio.png) | [Görüntü](../../design/reviews/tracks-panel/font-1.0/tracks-off.png) | [Görüntü](../../design/reviews/tracks-panel/font-1.0/tracks-subtitle.png) |
| 1.5 | [Görüntü](../../design/reviews/tracks-panel/font-1.5/tracks-audio.png) | [Görüntü](../../design/reviews/tracks-panel/font-1.5/tracks-off.png) | [Görüntü](../../design/reviews/tracks-panel/font-1.5/tracks-subtitle.png) |
| 2.0 | [Görüntü](../../design/reviews/tracks-panel/font-2.0/tracks-audio.png) | [Görüntü](../../design/reviews/tracks-panel/font-2.0/tracks-off.png) | [Görüntü](../../design/reviews/tracks-panel/font-2.0/tracks-subtitle.png) |

Renk çubukları yerel testvideosudur; gerçek film/yayın değildir. [QA](../project/native-tracks-panel-qa.json).

## Kalabalık ve eksik metadata kontrolü

TracksLayoutTest, üretim panelini 12 ses + 12 altyazı parçası ve boş Tracks metadata ile açar. Her grupta bir desteklenmeyen biçim vardır. Üç yazı ölçeğinde (1.0/1.5/2.0) iki vaka geçti: desteklenmeyen satır disabled ve kumanda odağının dışında; 23 seçilebilir satır boyunca RadioButton rolü, odaktaki satırın viewport sınırları, sabit başlık ve Geri kontrol edildi. Yaklaşık 100 karakterlik açıklama büyük yazıda da bütünüyle görünür. Boş listede iki açıklama ve Kapalı erişilebilir.

İlk testte özel rememberCardVisibility kaydırma takibi sürekli yeniden çizime neden oldu; timeout kanıtı özel logda korundu. TrackChoice bu yardımcıyı kullanmıyor; verticalScroll ve TV Button'ın yerleşik odak kaydırması kullanılıyor. Başka ekranlardaki ortak yardımcı bu tur değiştirilmedi. Desteklenmeyen satırlarda canFocus=false açıkça uygulanır; TV Button'ın enabled=false değeri tek başına odak almayı engellemiyordu.

Yeni görseller her font klasöründe tracks-empty.png, tracks-long.png, tracks-many-last.png. Bunlar metadata-only test ekranlarıdır; arkadaki karartılmış açılış yüzeyi gerçek oynatma olarak sunulmaz. Bu kontrol sınırsız uzunlukta etiketi, ekran okuyucu oturumunu veya gerçek donanım codec desteğini doğrulamaz. Çok daha uzun, ekran yüksekliğini aşan tek satır için ayrı okuma davranışı hâlâ incelenmelidir.

## Ekranı aşan açıklamalar

Parça satırları en fazla beş satır gösterir. Metin gerçek yerleşimde taşarsa, satırın altında **Tam açıklama** eylemi görünür. Bu ölçüm karakter sayısına dayanmaz; font ve mevcut genişliği dikkate alır. Seçim satırı aynı işi yapar; açıklama okumak parçayı seçmez. Desteklenmeyen parçaların açıklaması da okunabilir.

Açıklama mevcut SourceDialog ve RemoteTextReader ile açılır. İlk odak metindedir; ↑↓ kaydırır, metnin sonundan aşağı tuşu Geri eylemine geçer. Geri tuşu veya eylemi paneli kapatmadan Tam açıklama odağına döner. Seçim göstergesi ve oynatıcı tercihleri okuma sırasında değiştirilmez. Sabit başlık ve kapanış eylemi uzun metinle kaybolmaz.

Yeni oversizedDescriptionCanBeReadWithoutSelectingTrack, yaklaşık1600karakterlik desteklenmeyen ses parçasıyla metnin başından sonuna gerçek tuşlarla ulaşmayı, seçim override oluşmamasını, Geri odağını ve sonraki Kapalı seçeneğine erişimi denetler. İlk denemede test ExoPlayer durumunu yanlış thread'den okudu; test kontrolü main thread'e alındı. Bu uygulama çökmesi değildi; kanıt .private/tracks-reader-test-thread-failure.log içinde saklandı.

Son APK üzerinde üç metadata vakası ve iki video/panel akışı üç fontta geçti. PilotFlow canlı/film/dizi/dil/favori/Geri kontrolü ayrıca normal fontta geçti. Lint0hata/32uyarı; v2 imza doğrulandı. [APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.5.apk). Önceki kontrol sınırları tarihçedir; çok uzun açıklama okuma kontrolü bu bölümde tamamlandı. Fiziksel codec/performans ve kullanıcı görsel kabulü hâlâ açıktır.

Görsel doğrulama notu: İlk normal font okuma ekranı platform pencere animasyonu sırasında yakalandı. Test görüntü yakalama yardımcısına1200ms bekleme eklendi (uygulama süresi değişmedi); yalnız uzun açıklama vakası üç fontta yeniden1/1 geçti,9görsel yenilendi. Normal font yeni başlangıç görüntüsü ve büyük font son görüntüsü incelendi. .private/tracks-reader-*.log; test cihazı font1.0. Proje belge/bağlantı kontrolü geçti.


## 0.14.21 · Odak çerçevesinin güvenli payı

15 Ekim görsel incelemesinde, 2.0 yazıda son altyazı satırının yerleşim sınırı viewport içine sığsa da dışa çizilen odak çerçevesinin alttan kesildiği görüldü. Yerleşik kaydırmaya verilen BringIntoViewSpec, panel seçenekleri için üstte/altta 8 dp pay bırakır. Ekrandan yüksek bir hedefte üst kenar önceliklidir; kaydırma ileri/geri arasında döngüye sokulmaz. Başlık ve açıklama alanı sabit, odak/selection ayrımı aynı kalır.

Görünürlük testi sadece satır kutusunu değil en az 7 dp çerçeve payını da kontrol eder. Güçlendirilmiş test 0.14.20 üzerinde başarısız oldu: son satırın altı ile viewport altı 443.5 dp'de eşitti. Bu, önceki yeşil testin görsel çerçeveyi kapsamadığını gösterir. Yeni sürüm sonuçları native-track-focus-21-qa.json dosyasında tutulur.
