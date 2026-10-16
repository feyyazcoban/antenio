# Native TV görünüm sözleşmesi

14 Ekim 2026 · 0.7 pilot. Bu belge mevcut COLOR/MOTION ve SCREENS belgelerini uygulama ölçülerine bağlar. Yeni bir tasarım sistemi değildir. Figma'ya aktarım kota nedeniyle yapılmadı; tam ekran referansı `prototypes/tv-flow` ve daha önce gösterilen prototip görüntüleridir.

## Ortak yerleşim

Temel kontrol alanı 960 × 540 dp; 1920 × 1080, density 320 emülatörde doğrulanır. 65U95A'nın daha önce okunan uygulama alanı da bu ölçektedir; bu sürüm henüz fiziksel TV'de denenmedi. Piksel değerlerini dp sanarak iki kat büyük arayüz üretmeyin.

| Rol | Uygulanan ölçü / davranış |
| --- | --- |
| Sol gezinme | 144 dp; iç yatay 12 dp; üst 28 dp; 36 dp satır |
| Ana içerik kenarı | Gezinmeden sonra 36 dp; bağımsız ayrıntıda 48 dp |
| Satırlar / kart arası | Dikey 20 dp; yatay 14 dp |
| Home hero | 268 dp; tam genişlik; sol ve alt koyu scrim |
| Home devam kartı | 242 × 48 dp; kısa başlık, bölüm/konum ve ilerleme |
| Kanal / film | Kanal 142 dp ve 2.2:1; Home poster 134 dp ve 2:3 |
| Film ayrıntısı | Metin solda, 174 × 261 dp afiş sağda; sabit üst geri alanı |
| Dizi ayrıntısı | Üç sütun bölüm, 2:1 kare; sezon seçimi ve tam bölümden devam |
| Oynatıcı | 38 dp yatay / 26 dp alt pay; 12 dp köşeli kontrol paneli |
| Ayarlar | Sağda 440 dp tam yükseklik; sabit başlık/kapatma, 28 dp iç kenar, kaydırma göstergesi |
| Ses ve altyazı | Sağ kenara bağlı 340 dp panel; gerçek parça listesi |
| Odak | 2 dp mint çizgi; düğme 1.025, poster 1.035 büyüme; azaltılmış harekette 1.0 |

Roboto paket içinde kalır. Hero 42/46 sp; film 42/47, dizi 34/39; bölüm/satır başlığı 15–18; gövde 12–13/18–19; ikincil bilgi 10–12 sp. Dar alanda metin sınırlandırılır; uzun açıklama ayrı okunabilir panelde açılır. Kaynağın vermediği süre, tür, yayın programı veya puan uydurulmaz.

Odak, seçim ve normal durum farklıdır: odak mint çerçeve; seçili gezinme/kategori koyu yeşil yüzey; normal eylem şeffaf koyu yüzey ve ince kenar. Logo oranı `Fit`, afiş `Crop`; başarıyla yüklenen şeffaf logonun altında harf fallback'i kalmaz. Yanlış Dune görsel eşleşmesi önizleme veri setine alınmaz.

## Kumanda ve kaydırma

Görünür kart odağa geldi diye bütün ekran yukarı zıplamaz. Compose `BringIntoViewSpec` ile yalnız görünür alan dışındaki hedef için gerekli kaydırma kullanılır; [resmî TV liste rehberi](https://developer.android.com/training/tv/playback/compose/lists) bu davranışın özelleştirilmesini açıklar. Bu düzen prototip hero'sunun ilk devam kartında kaybolmasını önler.

Home açıldığında önce devam kartı, sonra son kanal, sonra hero seçilir. Ayrıntıdan geri dönüş son karta; oynatıcıdan geri dönüş doğru bölüme gider. İlk odak hazır metadata'yı bekler. Ayrıntı listesi sabit geri alanının altında kırpılır; metin geri düğmesinin arkasına taşmaz. Back oynatıcıda önce dil panelini, sonra kontrolleri, sonra oynatıcıyı kapatır.

## Açılış ve ses

Android TV'de platform `SplashScreen` özel animasyonu desteklenmez; [resmî TV uygulama rehberi](https://developer.android.com/training/tv/get-started/create) bunu açıkça belirtir. 16 Ekim 2026'dan beri (D062) kullanıcının verdiği NEXTV paketi kullanılır. Logo kaynakları kullanıcının verdiği üç SVG'dir ve [design/brand](../../design/brand) altında durur: amblem (yalnız işaret), dikey (işaret üstte, NEXTV altta) ve yatay (yan yana). Başlangıç pencere zemini siyahtır; amblem (`ic_nextv_splash_mark`, 51,84×64,47 dp) 960×540 dp TV'de splash'teki işaretin oturacağı yerde, merkezin 32,77 dp üstünde durur. Pencere işareti sabit dp'dir, splash ise ekrana oranlanır; bu yüzden hizalama yalnız 960×540 dp'de birebirdir. 1280×720 dp ekranda (ör. 240 dpi 1080p) splash'teki işaret 69×86 dp olur, üst kenarı 22 dp daha yukarıdadır; 1920×1080 dp'de 103×129 dp, 65 dp yukarıda. Splash'in ilk karesinde işaret zaten gizlidir ve ~0,87 sn sonra belirir. İlk Compose karesinde [LaunchCover](../../apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/LaunchCover.kt) splash'i oynatır: siyah sahne, iki köşede mint (#1CECAB) ışıma, dikey logodaki katlanmış oynat işaretinin açılış sesinin vuruşunda hafif taşmayla belirmesi ve "NEXTV" harflerinin soldan sağa yükselmesi. Tek doğrusal 2,8 sn saat; her öğe kendi penceresinde yumuşar. Ölçüler paketin 1920×1080 çerçevesinden ekrana oranlanır (960×540 dp TV'de logo 149×130 dp). Değerler yalnız çizim aşamasında okunur. Veri yükleme splash'le eşzamanlı başlar. Splash en az 3 sn görünür; kayıtlı kaynaklar hâlâ açılıyorsa (yükleme rotası, `RESTORING`) logonun altında dönen mint yay ve "Hazırlanıyor..." (Roboto) belirir. Açılış bitince splash 220 ms'de içeriğe açılır. Paketteki Google Sans eklenmedi (dosyanın lisans alanı açık kaynak olmadığını söylüyor); paketin material3 göstergesi yerine kendi çizimimiz kullanılır.

Splash süreç başına bir kez oynar; herhangi bir kumanda girdisi splash'i ve açılış sesini hemen kaldırır, girdi normal ekrana iletilir. Uygulamanın hareketi azalt ayarı veya kapalı sistem animasyonları splash'i ilk karede kaldırır. Ayrı splash Activity kullanılmaz. Android 8+ aynı pencere/Compose yaklaşımını kullanır; fiziksel üretici TV'sinde başlangıç penceresi ayrıca görülmelidir.

Launcher ikonu uyarlanabilir: `mipmap-anydpi-v26/ic_launcher` (ve `_round`), mint zemin üstünde siyah amblem (paketteki ters renk düzeni korunur), 48 dp yükseklik, alan ağırlık merkezine göre optik ortalı; tek renkli katman aynı amblem. TV banner'ı `drawable/app_banner.xml` vektörü: siyah zemin, ortada 200 dp genişliğinde yatay logo (mint işaret, beyaz NEX, mint TV). Paketteki 1280×720 PNG banner (mint zemin, farklı yazı tipinde "Nextv") kaldırıldı. Siyah zemin, Google TV launcher'ının koyu arka planında kart kenarını belirsizleştirir; logo boşlukta durur gibi görünür (emülatörde görüldü). Zemin rengi kullanıcı kararıdır. Uygulama etiketi değişmedi; NEXTV hâlâ çalışma adıdır (D007).

Açılış sesi `res/raw/splash_chime.m4a`, NEXTV paketinden: AAC 44,1 kHz mono, 6,9 sn. İlk 0,4 sn neredeyse sessiz olduğundan atlanır. Böylece vuruş splash'teki işaretle çakışır (`Impact` 0,48 × 2,8 sn ≈ 1,34 sn); biri değişirse diğeri de ayarlanır. En gürültülü yarım saniye tepe yaklaşık −0,4 dBFS / ortalama −11 dB, splash kalktıktan sonra yaklaşık 3 sn içinde söner (ffmpeg ölçümü; cihazdaki algılanan yüksekliği garanti etmez). Kullanıcının beyanına göre kendi seçtiği telifsiz bir sestir ve kullanılabilir (D062); lisans belgesi depoda yok. Önceki özgün sentez ses (`startup.wav`, 1,12 sn, tepe −12 dBFS) ve üreteci `tools/generate_startup_audio.py` kaldırıldı; git geçmişinde durur.

Ses varsayılan açık, Ayarlar'dan kapatılır; kaynak kaldırma bu tercihi sıfırlamaz. Süreç başına bir kez, yalnız ön planda ve zamanında (1,8 sn içinde) hazırlanırsa çalar. Mevcut müzik çalıyorsa atlanır; arka plana geçince ya da splash kumandayla geçilince serbest bırakılır. Sistem ses akışının düzeyine uyar, düzeyi yükseltmez. Başlangıçtaki sessizliği atlayabilmek için [MediaPlayer](https://developer.android.com/reference/android/media/MediaPlayer) kullanılır.

## Görsel kontrolün sınırı

`VisualPreviewTest` mevcut gerçek afiş/logo/bölüm karelerini yerel sentetik API üzerinden sunar. Görseller yalnız test APK'sındadır. Oynatıcıda sentetik renk videosu ve ses/altyazı parçaları kullanılır; gerçek sağlayıcıdan ikinci yayın açılmaz. Bu görüntüler gerçek Android ekranlarıdır; gerçek film oynatma veya tasarımın kullanıcı tarafından onaylandığı anlamına gelmez.

Yeni sürümde Home, film kataloğu, film ayrıntısı, dizi ayrıntısı, oynatıcı, dil paneli ve ayarlar tekrar çekilir. Uzun başlık, bozuk/eksik afiş, boş liste, ağ hatası, hızlı D-pad, silinen odak ve font ölçeği ayrıca kontrol edilmelidir. Sonuçların kaynağı `docs/project/native-polish-qa.json`; yalnız derleme başarısını görsel onay yerine kullanmayın.

## 0.4 arama ve kart görünürlüğü

Sol menüde **Ara** tek ekranda kanal, film ve dizi bulur. Başlık, kaynak adı, geniş giriş alanı, Temizle, dört tür filtresi ve sonuç sayısı sıralanır. Sonuçlar iki sütundur: en az 104 dp yükseklik, 10 dp iç boşluk, 56 × 84 dp afiş, 14 dp aralık; başlık 14/19 sp ve en çok iki satır, tür/yıl 11 sp, kaynak 10 sp. Kaynak adı yalnız bağlamdır; çoklu kaynak filtresi henüz yoktur.

Arama ve bölüm içi arama aynı başlık indeksini kullanır. Türkçe karakterler klavye varyantlarıyla eşleşir; kelimeler farklı sırada bulunabilir. Tam başlık önce, başlık başlangıcı sonra, diğer eşleşmeler katalog sırasıyla gelir. İndeks/arama UI iş parçacığının dışında çalışır; yeni sorgu eskisini iptal eder. Sorgular diske/analitiğe yazılmaz. Yetişkin işaretli kayıtlar ve ayrı bölüm kayıtları indekse girmez; bölüm, dizinin ayrıntısından açılır.

Home ve katalogda afiş ile altındaki metin birlikte görünür alana alınır. Katalog başlık alanı iki satır yer ayırır; geri dönüşte filtre, kart ve kaydırma korunur. Yenileme seçili kartı kaldırırsa aynı konumdaki komşu seçilir; sonuç kalmazsa güvenli kontrol alanına dönülür. Yazı alanında olan kullanıcının odağı yenilemeyle alınmaz. Kategori kaldırılırsa Tümü seçilir. Arama alanından Aşağı, kategori grubuna gider; hedef tek bir ekran dışı kategoriye bağlanmaz.

TV klavyesinde Geri önce klavyeyi kapatır. IME arama eylemi klavyeyi kapatıp sonuca odaklanır. Ayrıntıdan Geri, arama metnini/türü/seçimi korur. 0.5 ile Geri önce klavyeyi kapatır; giriş/filtre alanından sonuçlara, sonuçlardan Ara gezinme satırına, oradan ana sayfaya döner. Sonuç yoksa sonuç basamağı atlanır. Sorgu ve tür korunur. Resmî dayanaklar: [metin alanı odağı](https://developer.android.com/develop/ui/compose/touch-input/focus/focus-in-text-fields), [programatik odak](https://developer.android.com/develop/ui/compose/touch-input/focus/request-focus). Kayan TV klavyesi emülatörde pencere insets bilgisiyle güvenilir görülmedi; test gerçek Geri girdisi ve klavye hizmeti durumuyla doğrulanır.

## 0.5 ayar paneli ve alan değişimi

Ayarlar, prototipteki sağ panel düzenini izler. 440 dp genişlik, tam yükseklik, sol köşeler 18 dp; üst başlık 24/30 sp ve Kapat sabit kalır. Kaydırılabilir gövdede Görünüm ve ses / Kaynağın / İzleme ve uygulama bölümleri bulunur. Satırlar en az 62 dp, 12 dp iç boşluk; başlık 14/19 sp, açıklama 11/16 sp. Sık kumanda hareketinde satır ölçeği 1.0; mint odak çizgisi 2 dp. Toggle satırı Switch rolü ve Açık/Kapalı durumu taşır. Kaydırma çubuğu ve kalan içerik yönü gösterilir; yalnız alt boşluk kalınca Daha fazla yazılmaz.

Kaynak kaldırma/geçmiş temizleme alt ekranında ilk odak Vazgeç'tedir. Geri/Vazgeç aynı ayar satırına ve kaydırma konumuna döner. Uygulama hakkında, kumandayla odaklanıp aşağı/yukarı kaydırılabilen okuma alanıdır; başlık ve Geri görünür kalır. Yenileme satırı işlem sırasında odağı kaybetmez, ikinci OK yeni iş başlatmaz. Görünen katalog sayısı yetişkin işaretli ve bağımsız bölüm kayıtlarını içermez.

Kart resmini ve alt metni kapsayan görünürlük, odak değişimine ek olarak üst/alt alan daralmasını da izler. Kırpılma durumunun değişmesi yer değiştirme isteğini yeniler; her kaydırma pikselinde animasyon başlatılmaz. AndroidX [LayoutCoordinates kaynağı](https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/layout/LayoutCoordinates.kt) köke göre sınırın aradaki kırpmayı içerdiğini gösterir. Test sadece ekran altını değil, gerçek katalog viewport'unu karşılaştırır.

## 0.6 oynatıcı bağlantı durumları

Oynatıcı aynı kontrol panelinde yeniden bağlanma durumunu gösterir; ekranın başka bölümünde ikinci hata katmanı açılmaz. Beklerken kalan saniye ve deneme (1/2 veya 2/2); bağlantı başlatılınca “Yeniden bağlanılıyor…” görünür. Birincil düğme İptal et'tir. Son hata veya iptalden sonra aynı odak Tekrar dene olarak kalır. Geri, bekleyen bağlantıyı durdurup gelinen ekrana döner. Arka plana alınan bağlantı denemesi durur; geri gelince kullanıcı tekrar başlatır. Sağlıklı oynatmanın mevcut arka plan duraklat/devam davranışı korunur.

Uygulama tercihi: geçici ağ hataları, canlı pencere kaybı ve HTTP 408/500/502/503/504 için 1 ve 3 saniye bekleyerek en çok iki otomatik deneme. İlk hatadan itibaren 20 saniye kurtarma sınırı; normal açma/buffer için 30 saniye üst sınır. Ağ bağlanma/okuma süreleri 8 saniyedir. En az 10 saniye kesintisiz oynatma bütçeyi yeniler; kısa hazır/oynuyor sinyali yenilemez. Kullanıcının Tekrar dene eylemi yeni bütçe açar. VOD konumu korunur, canlı tekrar güncel pencereye katılır.

401/403 erişim, 404/410 bulunamayan yayın, 429 bağlantı sınırı; sertifika, biçim ve decoder hataları otomatik tekrar yapmaz. Mesajlar sabit ve Türkçedir; ham hata/URL gösterilmez. Deneme kontrolü tek yerdedir; Media3'ün iç yükleme tekrarları/fallback'i ayrıca sayıyı çoğaltmaz. Bu pilot politika farklı sağlayıcı/HLS/cihaz matrisiyle ayrıca doğrulanır.

Dayanaklar: Media3 [oynatıcı hataları ve prepare ile tekrar](https://developer.android.com/media/media3/exoplayer/listening-to-player-events), [yükleme hatası politikası](https://developer.android.com/reference/androidx/media3/exoplayer/upstream/DefaultLoadErrorHandlingPolicy). Süre/sayı değerleri araştırmanın evrensel sonucu değil, ölçülecek ürün tercihleridir.

## 0.7 bölüm sonu ve otomatik geçiş

Bölüm sonu kartı sağ altta 480 dp, yatay 38/dikey 26 dp dış boşluk, 24 dp iç boşluk ve 16 dp köşe kullanır. 112 × 63 dp bölüm karesi; başlık 20/26 sp iki satır, bölüm bilgisi 12/17 sp. Geri sayım 10 saniye; saniye metni ve 3 dp doluluk çizgisi vardır, sürekli sıçrama/ölçek animasyonu yoktur. İlk odak Bu bölümde kal; Şimdi oynat ve Bölümlere dön ayrı eylemlerdir. Video yüzeyi boyut değiştirmez. Sonraki hedef yoksa başlık Bölüm tamamlandı ve tekrar izleme seçeneği görünür.

Geri sayım yalnız gerçekten oynatılmış bölümün Media3 STATE_ENDED durumunda başlar. [Media3 olayları](https://developer.android.com/media/media3/exoplayer/listening-to-player-events) bu durumu medyanın sonu olarak tanımlar. Başlık süresi, yüzde 95 tamamlandı eşiği veya tahmini jenerik anı geçiş tetiklemez. Yükleme/hata ve canlı/film akışları bu bölüm sayacını başlatmaz.

Sıra politikası aynı bölüm listesindeki bir sonraki geçerli kaydı kullanır. Eksik sayı, tekrarlanan numara, numara aralığı veya eksik sezon otomatik geçişi kapatır; numarasız girdiye sırf fallback indeksi verildi diye sıra kanıtı üretilmez. Yetişkin/oynatılamayan ilk hedef atlanıp daha sonraki kayda sıçranmaz. Kaynak adresi ve hedef kimliği, oynatma öncesi tekrar doğrulanır. M3U SxxExx/1x01 başlık numaralarının çıkarım olduğu açıkça yazılır. Yeni episodeOrderKnown alanı şifreli katalog formatına geriye uyumlu eklenir; eski cache okunur ve sıra kanıtı yoksa otomatik geçiş kapalı kalır.

İptal, Geri, yeni medya, hedef değişikliği, ayarın kapanması ve arka plan sayacı durdurur; arka plandan dönünce aynı sayaç yeniden başlamaz. Tek oynatıcı tutulur, sonraki medya önceden açılmaz. Kullanıcı Şimdi oynat ile veya sayaç bittiğinde geçerse bölüm sıfırdan açılır; daha önceki hedef ilerlemesi otomatik olarak atlatılmaz. Dönüşte yeni sezon ve yeni bölüm odağı korunur.

Kart açıkken alttaki oynatıcı düğmeleri gizlenir; iki odak görünümü oluşmaz. Ayarlar'da Açık/Kapalı metni veya açıklama değişimi satır yüksekliğini artırırsa, odaktaki satırın açıklaması da görünür alana alınır. Sınır, sabit başlık ve alt kaydırma ipuçları arasındaki gerçek gövde alanıdır; yalnız ekran kenarına göre kontrol edilmez.

## 0.8 ölçeklenen gezinme ve kaynak durumu

Menü etiketi Roboto 11/16 sp, Normal, sıfır harf aralığı kullanır. Aynı stil ve sistem yazı ölçeğiyle en geniş etiket ölçülür; ray genişliği en az 144 dp, gerektiğinde ölçülen yazı + 76 dp olur. İkon, metin ve kenar boşlukları korunur. Satır en az 36 dp, içerik büyüdükçe yükselir. Orta alan dikey kayar; logo ve Ayarlar/kaynak alt alanı ayrıdır. 1.5/2.0 ölçekte etiketin görsel taşması ve odaklı satırın gerçek kaydırma alanına sığması kontrol edilir; tüm ekran metinlerinin denetimi yerine geçmez.

Ayarlar panelinin [odak grubu giriş hedefi](https://developer.android.com/develop/ui/compose/touch-input/focus/focus-group) açıkça tanımlıdır; platformun varsayılan Kapat hedefi yerine ilgili satır seçilir. Sayfa değişiminde bir kez [WindowInfo.isWindowFocused](https://developer.android.com/reference/kotlin/androidx/compose/ui/platform/WindowInfo) beklenir. Sonradan kumanda girişini geri çekebilecek ek frame gecikmesi yoktur; arka plandan dönüş her seferinde odağı başa atmaz.

Bölüm oynatıcısında yıldızın erişilebilir adı Diziyi favorilere ekle/çıkar, durumu Favorilerde/Favorilerde değil. Yıldız aynı dizinin tüm bölümlerinde ortak kalır; Favoriler ekranında dizi afişi görünür. Dizisi güvenle çözülemeyen bölüm için işlem kapalıdır.

Kaynak yükleme ekranı Hazırlanıyor, Kaydediliyor ve Kaldırılıyor durumlarını ayırır. İndirme İptal düğmesi ve Geri ile durdurulabilir. Başlamış disk kaydı/silme sırasında İptal gösterilmez; Geri tüketilir ve yeni kaynak açılmaz. Durum açıklaması işlemi Türkçe anlatır. Başarısız kaldırma kütüphanede görünür bir uyarı bırakır; kullanıcı tekrar deneyebilir.

Diskten yeniden açılış testleri eski ViewModel'i özellikle temizler: sıradan Activity yeniden yaratımı modeli korur. [Android ViewModel yaşam döngüsü](https://developer.android.com/topic/libraries/architecture/viewmodel). Sonuçlar [0.8 QA](../project/native-lifecycle-qa.json) içinde.

## Native 0.9 · TV-070 kaynak yönetimi

Kaynak yönetimi tam sayfa, Ayarlar sağ paneldir. Kaynak ekle başlangıç odağıdır; düzenlemeden ilgili Düzenle’ye, silmeden komşu Kaldır’a dönülür. Kaldırma onayı Vazgeç’le açılır. Seçici480dp/maksimum460dp; mevcut seçime kayar, Back değişiklik yapmadan kapanır. Ana sayfa/katalog/Favoriler seçimi kalıcı; arama kendi kaynak filtresini ve detay dönüşü odağını korur. Çoklu hesapta kaynak adı kart/ayrıntı/player bağlamında görünür. İlk indirme, boş liste ve kaynak hatası ayrı mesaj taşır. Native09 gerçek afiş ve1.5/2.0 ekranları QA’dadır; Figma’ya aktarım yapılmadı. Tüm ekran büyük font erişilebilirlik onayı değildir.

## 0.11 uzun kaynak adları ve metin okuma

Kaynak seçici genişliği 560 dp olur; en çok 460 dp yükseklik korunur. Kaynak satırı 14 sp tek satır ad, 11 sp sıra/tür, seçili onayı ve ayrı mint odak çerçevesi içerir. Uzun adın başı ve sonu ortadan kısaltmayla görünür. Yönetimde ad 20/26 sp tek satırdır; sıra/tür ve güncelleme bilgisi ayrı satırlardadır. Sıra bilgisi görsel ayraçtır, kalıcı hesap kimliği değildir. [TV liste kılavuzu](https://developer.android.com/design/ui/tv/guides/components/lists) birincil ve destekleyici metni ayrı rollerde ele alır. Android [MiddleEllipsis](https://raw.githubusercontent.com/androidx/androidx/androidx-main/compose/ui/ui-text/src/commonMain/kotlin/androidx/compose/ui/text/style/TextOverflow.kt) yalnız tek satırda kullanılır.

Her kaynakta Bilgiler, Yenile, Düzenle, Kaldır sırası bulunur. Herhangi bir düğme odaklanınca kaynak kartı bütünüyle görünür alana taşınır. Hata özeti tek satır; tam hata ve tam ad Bilgiler'de okunur. Bu pencere 480 dp genişlik ve en çok 460 dp yükseklik kullanır. Metin başlangıç odağıdır; Yukarı/Aşağı görünür alanın yaklaşık %65'i kadar, tam satıra yuvarlanarak kaydırır, sınırda komşu denetime geçer. Kapat sabit kalır. Back açan Bilgiler düğmesine döner.

Kaldırma onayında kaynak sırası/türü ve silinecek veriler önce, uzun tam ad sonra gelir. Metin ayrı kayar; Vazgeç ve Kaynağı kaldır sabit alandadır. İlk odak Vazgeç; Yukarı metne, Back veya Vazgeç önceki Kaldır düğmesine döner. Yazı ölçeği düşürülmez. 16 kaynak, aynı başlayan ve yinelenen adlar, bin karakterlik ad ve yenileme hatası 1.0/1.5/2.0 ölçekte sınanır; diğer ekranların tüm büyük font durumları ayrıca izlenir. Figma eşitlemesi değildir.
