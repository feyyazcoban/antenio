# Proje durumu

## 0.14.68 · Splash çizim hazırlığı ve kalan işlerin durumu · 19 Ekim 2026

Kullanıcının “diğer işler ne oldu” sorusunda, vitrin tesliminden sonra kalan işe dönülmediği açıkça bildirildi; splash'e dönüldü. D098: gradient/path ilk karede hazırlanır, giriş saati ve ses frame commit sonrasında başlar. Kumanda atlama, hareketi azalt ve içerik kapısı korunur. Eski release ölçümünde animasyon ortası COMMAND_ISSUE405–416ms; yeni finalde3,9ms. Bunun karşılığında ilk frame268→897ms; ilk2,8sn pencerede sonraki en büyük vsync aralığı483→600ms. **Genel açılış beklemesi bitmedi; fiziksel splash/ses kabulü açık.** Ayrıntı ve kapsam sınırı [QA](native-launch-prime-68-qa.json).

362 JVM sonucu (1 atlanan/0 hata), lint0/40; full build8m26s. Yalnız5558/betik: final prepare1/1, release1/1, ayrı release-visual1/1, kontrollü logo/reduced-motion/içerik kapısı3/3. Ara kareler incelendi; font matrisi yok. Kaynak ayrı geçişte incelendi, ayrı ajan kullanılmadı. Kod82/.68 aynı imza/debug kapalı/R8 ile TV'ye install-r kuruldu; firstInstallTime ve çekilen APK hash korundu. SHA-256 f0926965128692456de709a72ea9ddb703997c988d99b5372a1cd7508d01a6f2. TVapp/yayın açılmadı;5556 dokunulmadı,5558/Gradle kapalı.

Diğer başlıklar: S15 önceki/son kanal altyapısı taslak stash'te, henüz tamamlanıp test edilmedi; S16 seçenekler ve S17–S29 geri sarma/EPG/arşiv/hatırlatıcı uygulamasına başlanmadı. Genel plan veya BKL kayıtları done değildir. Tam devralma ve WIP kimliği HANDOFF'ta.

## 0.14.67 · Vitrin karartması · 19 Ekim 2026

D097: kullanıcının onayladığı görsel oranı/tam ekran yerleşim korunur. Karartma ekranın en solunda, menü ikonlarının arkasında koyu başlar; sağa doğru yumuşar. Sol kenar ve metin bölgesi biraz koyulaştı. Kaydırma, tek İzle ve diğer davranışlar değişmedi.

362 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/40 uyarı; release/debug/androidTest başarılı (8m12s). İlk birleşik ekran turunda emülatör kapandı; final ayrı normal ölçek koşuları 1/1 + 2/2 geçti ve vitrin görseli incelendi. Kaynak ayrı geçişte incelendi; ayrı ajan kullanılmadı. [QA](native-hero-overlay-67-qa.json), [görsel](../../design/reviews/LATEST.md). Font matrisi yok.

Kod81/.67 TV'ye install-r ile kuruldu; ilk kurulum zamanı ve çekilen APK hash eşitliği doğrulandı. SHA-256 494157011ef27a70a1a84c910534cf3e7ba6da9bf6d47f1ecc343d9239202472. TV uygulaması/yayın açılmadı. 5556 bağlı değildi, açılmadı; son teslimi .66. 5558 ve Gradle kapalı. Splash ve kalan S15/S16/S17–S29 işleri açık; ön çalışma ve release ölçüm kanıtları HANDOFF'ta korunur.

## Ana sayfa kaydırması kullanıcı kabulü · 19 Ekim 2026

Kullanıcı TV'de ana sayfanın iki aşamalı kaydırma düzeltmesini tamam olarak onayladı. D095/Home kaydırma geri bildirimi kapatıldı; genel BKL-019/020 kapsamının diğer kabul maddeleri aynı. Splash donması açık. TV son teslim0.14.66. Kullanıcı kalan dört başlıkta çalışmaya devam edilmesini istedi: splash, S15 kanal paneli devamı, S16 oynatıcı seçenekleri ve S17–S29 geri sarma/EPG/geçmiş yayın/hatırlatıcı işleri.

## 0.14.66 · İlk açılış yükü ve ölçümü · 19 Ekim 2026

D096: HTTP hazırlığı ilk IO isteğine ertelendi; katalog kaynak sayımları Default'a taşındı, gereksiz kategori/favori taramaları kaldırıldı. Splash glow shader'ları boyuta göre cache edilir. Zaman çizgisi/atlama/reduced motion ve veri sözleşmesi aynı.

362 JVM/1skip/0hata, lint0/40; final full build3m15s. 5558/betik ile baseline3/3, final cold3/3 ve ilişkili7/7; bağımsız inceleme tamam. [QA](native-cold-launch-66-qa.json). Tek debug emülatör örneğinde restore5399→4924ms; ilk2,8snvsyncmaxgap1233→1217ms, belirgin değişim yok. **Splash donması tamamen çözüldü sayılmıyor.** Kontrollü saatle logo/harf ilerleyişi geçti; gerçek açılışın seyrek görselleri sürekli akıcılık kanıtı değil.

Kod80/.66 TV ve5556'ya install-r kuruldu; ilk kurulum zamanı ve kuruluAPKhash aynı. SHA-256 2e59f56a1612e511776916ca6b22376a753200b3ea3fec7a7d8983cdfc28391f. TVapp açılmadı,5556 kullanıcı için açık/çalışır/crash yok. 5558/Gradle kapalı. Kod main üzerinde (b936014); tamamlanan dallar silindi, audit checkout eşlendi. İlk soğuk kare gecikmesi, fiziksel akıcılık kabulü ve genel S15a–c açık; font matrisi mağaza öncesine ertelendi.

## 0.14.65 · İlk kaydırma hedefi · 19 Ekim 2026

D095: Home ilk odak isteği ve geç resize kontrolü aynı tam kart/caption/pay alanını hedefler. Eski APK 120dp ilk hedef gönderirken yeni test 228dp beklediği için başarısızdı; düzeltme final testlerde geçti. Normalizasyon yalnız Home; Search eski yolu korur.

Bağımsız inceleme tamam. 362 JVM/1skip/0hata, lint0/40; full build6m04s; yalnız5558/betik10/10 ilgili test. Sentetik Continue görseli incelendi. [QA](native-home-reveal-65-qa.json). Font matrisi/fizikselFPS yok.

Kod79/.65 TV ve5556'ya install-r kuruldu; ilk kurulum zamanı ve çekilen APKhash aynı. SHA-256 8b60db603c3f882b1cd4f09b865f564a0ca0a4dd10b71d31df79cbeb89328837. TVapp açılmadı;5556 kullanıcı için açık/çalışır,crash kaydı yok. 5558/Gradle kapalı. Sonraki iş gerçek cold splash; genel S15a–c ve fiziksel akıcılık kabulü açık.

## 0.14.64 · Canlı Kanallar çekmecesi · 19 Ekim 2026

D094: Play yanındaki ipucu yerine Kanallar düğmesi; soldan kayan hızlı seçim listesi. Aynı player, kaynak/kategori/favori kapsamı, PIN kapısı ve liste odağı korunur. Favoriler yenilenince veya silinince odak geçerli satıra/Kapat'a döner; Geri Kanallar düğmesine döner.

Bağımsız inceleme ve düzeltmeler tamam; 362 JVM (1skip/0hata), lint0/40, release/debug/androidTest başarılı. Yalnız 5558/betik ile 5/5 hedefli test ve sentetik görsel inceleme. [QA](native-channel-drawer-64-qa.json). Font matrisi/fiziksel yayın ölçümü yok.

Kod78/.64 TV ve 5556'ya install-r kuruldu; firstInstallTime ve kurulu APK hash doğrulandı. SHA-256 8c68de5c6711a5d375620afec5288c11d6e41640df781b2205d5712da0e9a8f1. TV app/yayın açılmadı; 5556 çalışır/açık, crash kaydı yok. Evim etiketi tam ekran Home'da görünmediği için UI kontrolü false; veri temizleme yok. 5558/Gradle kapalı. Sıradaki iki-scroll ve gerçek cold splash; genel S15a–c açık.

## 0.14.63 · Gezinme ve ekran geri bildirimleri · 18 Ekim 2026

D093: kategori ve ana menü kayarak açılır/kapanır; PIN rakamları/geri sil ortalı, Vazgeç halkası tam görünür. Scrollbar gerçek listeyi izler ve sürüklenir. Kategori adları daha küçük ve dile duyarlı; afiş başlığı 16/22 sp, poster boşluğu 12 dp, yıl gerçek başlığın 4 dp altında daha soluk. Player ayarlar çarkı kullanır. Vitrin tüm genişliği, sol menünün arkasını ve viewport yüksekliğini kaplar; FillWidth/TopCenter oranı korur, sinema oranında altta boşluk kalabilir.

Bağımsız inceleme tamamlandı; 358 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/39 uyarı. Release/debug/androidTest başarılı; yalnız 5558/betik üzerinden ilgili normal ölçek kontrolleri geçti. İlk test/ara kare sorunları ve düzeltmeleri [QA](native-navigation-feedback-63-qa.json)'da; [görseller](../../design/reviews/LATEST.md) incelendi. Font matrisi yapılmadı.

Kod77/.63 TV ve 5556'ya install -r kuruldu; firstInstallTime korundu, kurulu APK hash doğrulandı. SHA-256 9a8e33134c90334e38cb16505fd168411affe62801b727cb7e67956d22b19469. TV uygulaması/yayın açılmadı. 5556 Ana Sayfa/İzle görünür, kullanıcı testi için açık; 5558 ve Gradle kapalı.

Sıradaki kullanıcı işi canlı player Kanallar drawer. İki aşamalı Home kaydırma ve gerçek soğuk splash donması ayrı açık işler; bu sürümde çözülmüş sayılmadı.

## 0.14.62 · Oynatıcı bilgi hiyerarşisi · 18 Ekim 2026

D092: geçen/kalan süre dış kenarlarda, film/dizi türü başlığın üstünde; kaynak adı kaldırıldı. Hata/yeniden bağlanma üstte kalıcı ayrı bildirim; yeniden deneme sonrası kaybolur. Bağımsız inceleme tamam; 357 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. 5558/betik normal ölçekte 5/5 + 3/3; görseller incelendi. [QA](native-player-info-62-qa.json).

Kod76/.62 TV ve 5556'ya install -r kuruldu, firstInstallTime ve kurulu APK hash eşitliği doğrulandı. SHA-256 ca2f3d6b78e4f5edc1dc5bfd5c62ffc9e394d2995a33822a8affa974122d0e50. TV saati 23:09:49; uygulama/yayın açılmadı. 5556 Evim görünür, kullanıcı testi için açık. 5558/Gradle kapalı.

Aktif kullanıcı geri bildirimleri: kategori→afiş ve ana menü kayma geçişleri; PIN Cancel kesilmesi/sayı hizası/Delete ikonu; kategori yönetimindeki scrollbar; sol kategori adlarını biraz küçültüp dil güvenli title case. Bunlar .63'te ele alınacak. Önceki listeleme afiş başlık/yıl, iki scroll hissi ve soğuk splash donması da açık; genel S15a bunlardan sonra. Font matrisi mağaza hazırlığına ertelendi.

## 0.14.61 · Kompakt son kanal kartları · 18 Ekim 2026

Kullanıcı D084 kartlarını dağınık buldu. Son kanallar tek düz yatay kartta: solda 80×56 dp logo, sağda kanal adı ve yakın kaynak; 272 dp genişlik/minimum 100 dp yükseklik, boş ikinci başlık satırı yok (D091). Bağımsız incelemede somut bulgu yok. 357 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. İlk birleşik ekran turunda emülatör son testte koptu; ayrı tekrarlar 1/1 + 3/3 geçti. Normal görseller incelendi, font matrisi yok. [QA](native-recent-channel-redesign-61-qa.json), [görseller](../../design/reviews/LATEST.md).

Kod75/0.14.61 TV'ye ve kullanıcının test isteğiyle 5556'ya install -r kuruldu; iki cihazda firstInstallTime ve kurulu APK hash doğrulandı. SHA-256 7ed8e9ab379dff74c6e315fc113c4e2e47f351b2bacaace3c13b18c0748c235e. TV saatiyle 22:53:32; TV'de uygulama/yayın açılmadı. 5556 eski default_boot ile .46/kod60 açıldı, .61'e yükseltildi; Evim görünüyor, uygulama kullanıcı testi için görünür/açık. Eski .51 bekleyişi güncel cihaz durumu değildir. 5558/Gradle kapalı.

**Yeni açık kullanıcı işleri:** listeleme sayfalarında boş ikinci başlık satırını kaldırma, afiş–başlık aralığını artırma, başlığı 1–2 sp büyütme ve yılı daha soluk yapma; vitrin→ilk raf hâlâ iki kez kayıyor; splash tamamen kapatıp açınca da donuyor. Bunlar .61'e dahil değil. Tam inceleme bulguları HANDOFF'ta. Sonraki paket en az .62/kod76; genel S15a planından önce bu geri bildirimler.

## 0.14.60 · Her durumda dolu, yuvarlatılmış oynat simgesi · 18 Ekim 2026

Kullanıcı isteğiyle ortak İzle/Oynat eylemleri odaktan bağımsız Material Symbols Rounded FILL1 simgesini kullanır (D090, D083'ün odak şartını değiştirir). 16 dp simge alanı, düğme geometrisi ve diğer simgelerin seçili durumları korunur.

Bağımsız incelemede açık bulgu yok. 357 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558 ve tools/run-device-tests.sh ile mevcut 3/3 ekran testi geçti. Normal ölçekte odaklı/odaksız görseller incelendi; yeni test/font matrisi yok. [QA](native-rounded-play-60-qa.json), [görseller](../../design/reviews/LATEST.md).

0.14.60-design-preview / kod74 TV'ye install -r ile kuruldu; firstInstallTime korundu, kurulu APK hash eşleşti. Güncelleme 2026-10-18 22:20:53; SHA-256 427f6d4b17a66941a82f394d29536639beb918a76f2172f203f47da5a45201fb. TV uygulaması/gerçek yayın açılmadı. 5556 .51/kod65'te kapalı; 5558/Gradle kapalı. Sonraki paket en az .61/kod75. Açık planda S15a sırada; fiziksel/Figma kabulü açık.

## 0.14.59 · Splash girişinin görünür kalması · 18 Ekim 2026

Kullanıcının “Splash screen donuyor animasyonu göremiyorum” bildirimi için iki yol düzeltildi (D089): Home/ikinci kapak giriş sırasında oluşturulmaz; logo yerleştikten ve kaynaklar açıldıktan sonra içerik opak kapak altında hazırlanır. Tek başına launcher tuş bırakması artık animasyonu iptal etmez. İlk yeni yön/OK/Enter/Back/Esc basışı yalnız atlar; aynı basışın tekrar/bırakması tüketilir, ses/medya tuşları normal iletilir. Süreç başına bir kez ve Hareketi azalt/D069 davranışı korunur.

Yeni Home testi değişmemiş .58 üretim kaynaklı debug APK'da başarısız, finalde başarılı. 357 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558 ve betikle normal ölçekte 7/7 ekran testi. Logo piksel sayımları 0→7332→16187→16399; ara kareler gözle incelendi. İki bağımsız inceleme; RestrictedApi riski public Window.Callback ile giderildi. [QA](native-splash-59-qa.json), [görseller](../../design/reviews/LATEST.md). Fiziksel TV kare süresi/soğuk ilk kare ve ses eşzamanlaması ölçülmedi; Home hazırlık maliyeti kaldırılmadı, girişin sonrasına taşındı. Font matrisi yok.

0.14.59-design-preview / kod73 TV'ye install -r ile kuruldu; firstInstallTime korundu, kurulu APK hash eşleşti. Güncelleme 2026-10-18 22:08:48; SHA-256 eddbff0231167ea4857ae8e6e51ac37dac46797151d205b48b67cb9958f2cc64. TV uygulaması/gerçek yayın açılmadı. 5556 .51/kod65'te kapalı; 5558/Gradle kapalı. Sonraki paket en az .60/kod74. Kullanıcının fiziksel geri bildirimi öncelikli; açık planda S15a sırada.

## 0.14.58 · Aşağı geçişte hareket kesintisi · 18 Ekim 2026

Kullanıcının “Hala kasarak iniyor aşağıya” bildirimi üzerine D078 mesafesine ek olarak hareketin sürekliliği düzeltildi (D088). Kart görünür hâle geldiğinde devam eden görünürlük işi artık iptal edilmez; gerçek odak/viewport değişimi iptal edebilir. Vitrinden çıkarken sıradaki görsel yeniden ön yüklenmez.

Yeni gerçek scroll regresyonu eski APK'da 1 iptal ve ortada 2 hareketsiz kare yakaladı. Finalde 0 iptal; aynı 154 px hedefe kesintisiz ilerleme ve viewport küçülünce 27 dp payın korunması geçti. Ana sayfa gereken/gerçek mesafe 86,5/86,5 dp. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558/betik üzerinden normal ölçekte 9/9 ekran testi; bağımsız incelemede açık bulgu yok. [QA](native-home-scroll-58-qa.json), [görsel](../../design/reviews/LATEST.md). Font matrisi yok; bu ölçüm fiziksel kare süresi iddiası değildir.

0.14.58-design-preview / kod 72 TV'ye install -r ile kuruldu; firstInstallTime korundu ve kurulu APK hash eşleşti. Son güncelleme 2026-10-18 21:48:10; SHA-256 b997029526dfe3cc59ec0c1a3dc705d0171f7b0017fd94cf6a00db8ddbe4fe0a. TV uygulaması/gerçek yayın açılmadı. 5556 .51/kod65'te kapalı; 5558/Gradle kapalı. Sırada S15a; sonraki paket en az .59/kod73. Fiziksel akıcılık ve Figma kabulü açık.

## 0.14.57 · Oynatıcı ikonları ve etiket hizaları · 18 Ekim 2026

Fotoğrafla bildirilen yukarı kayan ikonlar ve sıkışık odak etiketi düzeltildi (D087). Düğme içeriği gerçek boyutu doldurup merkezlenir; etiketlerin alt çizgisi ortak, zaman çubuğuyla en az 8 dp ve dış odak halkasıyla en az 6 dp aralık vardır. Düğme boyutları ve oynatma/sarma/panel davranışı korunur.

Eski üretim APK'sında yeni test ana ikonun 12 dp yukarı kaymasını yakaladı. Düzeltmeden sonra 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558/betik üzerinde normal ölçekte 7/7 ekran testi, panel dönüşü görsel tekrarı 1/1 geçti. İki bağımsız inceleme tamamlandı; beklenen etiketlerin varlık kontrolü inceleme sonrası güçlendirildi. İlk eksik yakalama ve başarılı tekrar [QA](native-player-layout-57-qa.json)'da ayrı kayıtlı. [Görseller](../../design/reviews/LATEST.md). Font matrisi yok.

0.14.57-design-preview / kod 71 TV'ye install -r ile kuruldu; firstInstallTime aynı, kurulu APK hash eşleşti. Son güncelleme 2026-10-18 21:37:17; SHA-256 5dfb67081992a47b2179f6c33393cabd21ab38c081764bef177b77dbc122fb3b. TV uygulaması/gerçek yayın açılmadı. 5556 kapalı, .51/kod65'te; 5558/Gradle kapalı. Sırada S15a; sonraki paket en az .58/kod72. Fiziksel/Figma kabulü açık.

## 0.14.56 · Dizi adı ile yıl yakın · 18 Ekim 2026

Kullanıcının “Başlık ve tarih birbirinden çok uzak duruyor” geri bildirimi düzeltildi (D086). Dizi rafında başlık boş ikinci satır ayırmaz; yıl/kaynak gerçek başlığı 4 dp aralıkla izler. Uzun adlar en fazla iki satır; .55 afiş/ad boyutları korunur.

Bağımsız salt okunur incelemede bulgu yok. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558/betik üzerinden normal ölçekte mevcut iki raf testi 2/2. Görselde kısa adların altında yılın yakın konumu doğrulandı. Yeni test/font matrisi yok. [QA](native-series-spacing-56-qa.json), [görsel](../../design/reviews/LATEST.md).

0.14.56-design-preview / kod70 TV'ye install -r ile kuruldu; firstInstallTime aynı, kurulu APK hash eşleşti. Son güncelleme 21:26:34; SHA-256 60b1910bf655d0357b6d9636700d75da69dff539042b80ba1bc77216468318bd. TV uygulaması/gerçek yayın açılmadı. 5556 kapalı, .51/kod65'te; 5558/Gradle kapalı. Sırada S15a; sonraki paket en az .57/kod71. Fiziksel/Figma kabulü açık.

## 0.14.55 · Daha büyük dizi afişleri ve adları · 18 Ekim 2026

Kullanıcı isteğiyle ana sayfadaki “Dizi kütüphanen” rafı büyütüldü (D085): afiş 136×204→160×240 dp, dizi adı 14/19→16/22 sp Medium. İki satırlık başlık, 2:3 oran ve ortak odak/görünürlük korunur.

Bağımsız salt okunur incelemede bulgu yok. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558/betik üzerinden normal ölçekte mevcut iki raf testi 2/2: afiş+başlık görünürlüğü, yatay/dikey gezinme ve ayrıntıdan dönüş. Yeni test/font matrisi yok. [QA](native-series-shelf-55-qa.json), [görsel](../../design/reviews/LATEST.md).

0.14.55-design-preview / kod69 TV'ye install -r ile kuruldu; firstInstallTime aynı, kurulu APK hash eşleşti. Son güncelleme 21:21:38; SHA-256 db997ea033d063575af4522e7bf47b3f3c4f30264268bc3bc8ba81f68b45ed65. TV uygulaması/gerçek yayın açılmadı. 5556 kapalı, .51/kod65'te; 5558/Gradle kapalı. Sırada S15a; sonraki paket en az .56/kod70. Fiziksel/Figma kabulü açık.

## 0.14.54 · Son izlenen kanal kartları · 18 Ekim 2026

Kullanıcı isteğiyle son izlenen kanalların logo, ad ve kaynak bilgisi tek 200 dp kartta birleşti (D084). Eşit logo/başlık alanları, hafif yüzey geçişi ve bütün kartı saran beyaz odak halkası kullanılır. Geçmiş sırası, PIN, basılı OK ve geri dönüş odağı korunur.

Bağımsız salt okunur incelemede bulgu yok. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558/betik üzerinden normal ölçekte 4/4 ekran testi; çoklu kaynak, uzun ad, eksik logo, yatay görünürlük, hızlı menü ve oynatıcıdan dönüş doğrulandı. İlk birleşik koşu emülatör bağlantısı düştüğü için yarım kaldı; ayrı tekrarlar 1/1 ve 3/3 geçti. Font matrisi koşulmadı. [QA](native-recent-channels-54-qa.json), [görseller](../../design/reviews/LATEST.md).

0.14.54-design-preview / kod68 TV'ye install -r ile kuruldu; firstInstallTime aynı, kurulu APK hash eşleşti. Son güncelleme 21:16:09; SHA-256 75497e18a42baf82a26aaa2a46f1f01333a46864c1162c27029a53f7c1916d85. TV uygulaması/gerçek yayın açılmadı. 5556 kapalı, .51/kod65'te; 5558/Gradle kapalı. Sırada S15a; sonraki paket en az .55/kod69. Fiziksel/Figma kabulü açık.

## 0.14.53 · İzle simgesi odakta dolu · 18 Ekim 2026

Kullanıcı isteğiyle ortak Action bileşenindeki play simgesi odakta dolu üçgen, odak dışında çizgili (D083). Vitrin, ayrıntı/Devam et, hızlı İzle ve bölüm sonu aynı davranışı alır. Favori yıldızının açık iconFilled durumu korunur.

Bağımsız salt okunur incelemede bulgu yok. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558/betik üzerinden normal ölçekte mevcut HeroLayoutTest ve ContinueCardLayoutTest 3/3; dolu/çizgili iki durum görsel doğrulandı. Yeni test/font matrisi yok. [QA](native-play-icon-53-qa.json), [görseller](../../design/reviews/LATEST.md).

0.14.53-design-preview / kod67 TV'ye install -r ile kuruldu; firstInstallTime aynı, kurulu APK hash eşleşti. Son güncelleme 21:07:04; SHA-256 76fe4076709e81174c0bdc30fa84517f4ea4a6c76ea062058df27b74a0018cd3. TV uygulaması/gerçek yayın açılmadı. 5556 kapalı, .51/kod65'te; 5558/Gradle kapalı. Sırada S15a; sonraki paket en az .54/kod68. Fiziksel/Figma kabulü açık.

## 0.14.52 · Daha küçük YENİ EKLENEN · 18 Ekim 2026

Kullanıcı isteğiyle vitrin üst etiketi 13→12 sp (D082). Aynı stil KÜTÜPHANENDEN için de geçerli; içerik başlığı, iki satır açıklama, 420 dp yükseklik, tek İzle ve kaydırma aynı.

Bağımsız salt okunur incelemede bulgu yok. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558/betik üzerinden normal ölçekte mevcut HeroLayoutTest 2/2; görsel incelendi. Yeni test/font ölçeği matrisi yok. [QA](native-hero-label-52-qa.json), [görsel](../../design/reviews/LATEST.md).

0.14.52-design-preview / kod 66 TV'ye install -r ile kuruldu; firstInstallTime aynı, kurulu APK hash eşleşti. Son güncelleme 21:02:21; SHA-256 237186f5ea5c4cce508307feb0b40b41919a7f91a85f38e0944e361c6a5bb86e. TV uygulaması/gerçek yayın açılmadı. 5556 açılmadı, .51/kod65'te; 5558/Gradle kapalı. Sırada S15a; sonraki paket en az .53/kod67. Fiziksel/Figma kabulü açık.

## 0.14.51 · Vitrin açıklaması iki satır · 18 Ekim 2026

Kullanıcı isteğiyle vitrin açıklaması normal ölçekte üç yerine en fazla iki satır (D081); taşan sonu üç nokta. Büyük yazıdaki mevcut tek satır sınırı, 420 dp vitrin, tek İzle ve odak/kaydırma davranışları korunur. Normal görüntüde uzun açıklamanın iki satırda kesildiği görüldü.

Bağımsız salt okunur diff incelemesinde bulgu yok. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Mevcut HeroLayoutTest normal ölçekte yalnız 5558/betik ile 2/2 geçti; yeni test veya font matrisi yok. [QA](native-hero-description-51-qa.json), [görsel](../../design/reviews/LATEST.md).

0.14.51-design-preview / kod 65, 5556 ve TV'ye install -r ile kuruldu; aynı ilk kurulum tarihleri ve kurulu APK hash eşitliği doğrulandı. 5556'da Evim görünür, uygulama çalışıyor, çökme kaydı yok. TV güncellemesi 20:57:57; SHA-256 161c16b930413e64a774836404c12a20d3129562929a10b84c3df1974283bd4e. TV'de uygulama/gerçek yayın açılmadı. Emülatörler/Gradle kapalı. Fiziksel/Figma kabulü açık; sırada S15a, sonraki paket en az 0.14.52/kod 66.

## 0.14.50 · Vitrin aşağı uzatıldı · 18 Ekim 2026

Kullanıcı isteğiyle vitrin minimum yüksekliği 372→420 dp (+48 dp, yaklaşık %13) oldu (D080). Yazı boyutları ve yatay ölçüler aynı; ilk raf başlığı/kart önizlemesi görünür. Tek İzle ve D078 korunur; devam kartına gereken/gerçek kaydırma 86,5 dp.

Bağımsız salt okunur incelemede işlev bulgusu yok; eski oranı anlatan kod açıklaması güncellendi. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Mevcut HeroLayoutTest ve ContinueCardLayoutTest normal ölçekte yalnız 5558/betik ile 3/3 geçti; iki sentetik ekran incelendi. Font matrisi yapılmadı. [QA](native-hero-taller-50-qa.json), [görseller](../../design/reviews/LATEST.md).

0.14.50-design-preview / kod 64, 5556 ve TV'ye install -r ile kuruldu; ilk kurulum tarihleri ve kurulu APK hash eşitliği doğrulandı. 5556'da Evim görünür, uygulama çalışıyor, çökme kaydı yok. TV güncellemesi 20:52:28; SHA-256 26ad22181ef0c7512a8c2608b7a88d0325abdea0be7c3058d8d0f5e7bdcf1b2a. TV'de uygulama/gerçek yayın açılmadı. Emülatörler/Gradle kapalı. Fiziksel/Figma kabulü açık; sırada S15a, sonraki paket en az 0.14.51/kod 65.

## 0.14.49 · Vitrinde tek İzle düğmesi · 18 Ekim 2026

Kullanıcı isteğiyle vitrindeki Ayrıntılar kaldırıldı (D079); yalnız İzle kaldı. Sağ/Sol slaytı değiştirir, ilk slaytta Sol menüyü açar, menüden Sağ İzle'ye döner. Tek slaytta Sağ odağı korur. İzle'nin mevcut oynatma/PIN davranışı ve D078 kısa aşağı kaydırması değişmedi.

Bağımsız salt okunur incelemede bulgu yok. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558/betik üzerinden normal ölçekte ilgili 4/4 ekran kontrolü geçti; ilk rafa gereken/gerçek kaydırma 38,5 dp. Font matrisi yapılmadı. [QA](native-hero-single-49-qa.json), [görsel](../../design/reviews/LATEST.md).

0.14.49-design-preview / kod 63, 5556 ve TV'ye veri silmeden install -r ile kuruldu; ilk kurulum tarihleri ve kurulu APK hash eşitliği doğrulandı. 5556'da Evim görünür, uygulama çalışıyor, çökme kaydı yok. TV güncellemesi 20:46:40; SHA-256 2de0ca68cd80c5aba01133876413340a9956eae4ffcc18587779aa14911bdf95. TV'de uygulama/gerçek yayın açılmadı. Emülatörler/Gradle kapalı. Fiziksel/Figma kabulü açık; sırada S15a, sonraki paket en az 0.14.50/kod 64.

## 0.14.48 · Ana sayfa aşağı geçişi · 18 Ekim 2026

Kullanıcının hero Ayrıntılar → İzlemeye devam et geçişindeki takılma bildirimi düzeltildi (D078). İlk raf artık ekranın tepesine taşınmak yerine kart ve başlığı gösterecek kadar kayar; diğer rafların D051 davranışı korunur. Aynı sentetik senaryoda gereken 38,5 dp iken eski kaynak 381 dp kaydırıyordu; düzeltme 38,5 dp kaydırır. Fiziksel TV kare süresi ölçülmedi; kullanıcının akıcılık kabulü açık.

Kontroller: eski kaynakta yeni regresyon testi beklenen nedenle başarısız; son kaynakta yalnız 5558 / tools/run-device-tests.sh ile ilgili 5/5 kontrol geçti. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest derlemeleri başarılı. Yazı boyutu matrisi çalıştırılmadı. [QA](native-home-scroll-48-qa.json) ve [görsel](../../design/reviews/LATEST.md) kayıtlı.

0.14.48-design-preview / kod 62, 5556 ve TV'ye install -r ile kuruldu; ilk kurulum tarihleri ve kurulu APK hash eşitliği doğrulandı. 5556'da Evim görünür, uygulama açık, çökme kaydı yok. TV güncellemesi 20:38:51; veri silme ve gerçek yayın açma yok. SHA-256: 71014ce84547b954b796585f902e98e40222987114a4d055a0d871ce8a0ec88a. Emülatörler ve Gradle kapalı. Sıradaki kapsam S15a; sonraki paket en az 0.14.49/kod 63. Font matrisi yalnız kullanıcı “yayına hazırla” dediğinde.

## 0.14.47 · Yeni oynatıcı TV'de · 18 Ekim 2026

S14b (D072) native kontrol yenilemesi **46f4743** ile tamamlandı ve main'e fast-forward edildi. Yeni alt bilgi bloğu, simgeli kontroller, odaklanabilir sarma çubuğu (bekleyen hedef/tek uygulama/Geri iptali), gizli sarma önizlemesi ve canlı kanal çipleri var; D077 hız seçimi korundu. Bağımsız incelemedeki çubuk odağına özel OK/Geri ipucu düzeltildi. Önceki 0.14.46 hız ekliyordu; yeni kontrol tasarımı bu 0.14.47 teslimindedir.

**Kullanıcının yeni çalışma talimatı:** yazı boyutu matrisi geliştirme sırasında koşulmayacak; "yayına hazırla" dendiğinde mağaza öncesine bırakılacak. WORKFLOW ve STEPS güncellendi. Bu talimattan önce bitmiş font koşuları tarihsel kanıt olarak korunur.

Kontroller: 354 JVM sonucu (0 hata, 1 atlanan), lint 0 hata/37 uyarı; release/debug/androidTest derlemeleri. İpucu düzeltmesinden önce 57 cihaz kontrolü (24 kontrol/hız, 30 regresyon, 3 büyük yazı hata/canlı durumu) geçti; son düzeltmeden sonra ilgili odak/sarma vakaları toplam 6/6. Normal yazıdaki son çubuk ipucu görsel olarak da doğrulandı. İlk başarısızlıklar ve düzeltmeleri [QA](native-player-controls-47-qa.json) içinde açıkça kayıtlı. Testler yalnız 5558 ve test betiğiyle. `check_project.py` artık yalnız güncel Git dosyalarını tarar (72fc520); 17 ekran/401 değişken doğrulaması ve `git diff --check` geçti.

Teslim: **0.14.47-design-preview / kod 61**, `releases/NEXTV_TV_Design_Preview_0.14.47.apk`. SHA-256 `d67222b483a00570aa1fcb579b8f2e1f7ae4908fbe844a4d2076bf7beb73b37e`. 5556'da 0.14.46 üstüne kurulum, açılış, Evim görünürlüğü ve çökme kaydı olmaması doğrulandı. TV'ye `install -r` saat 20:27'de başarılı; firstInstallTime `2026-10-13 23:50:03` değişmedi, kurulu APK çekilip hash eşleştirildi. TV'de uygulama/gerçek yayın açılmadı. 5556/5558 ve Gradle daemon kapalı; 5554'e dokunulmadı.

Ana checkout/main güncel ve ürün dalı birleşti. Birleşmiş `codex/player-controls-s14b` ve `claude/app-ui-ux-audit-a78e98` dalları kapatıldı; eski geliştirme klasörü güncel kaynakla detached olarak korunur. Sırada **S15a kanal kapsamı/son ve önceki kanal verisi**, sonra S15b–S17. Sonraki teslim en az 0.14.48/kod 62. S14b fiziksel kumanda/ses/TalkBack ve Figma/görsel kabulü; S16'nın ek seçenekleri, İngilizce inceleme ve satış/fiyat kararları açık. BKL-031 in progress kalır.

## Main birleşmesi ve TV kurulumu · 18 Ekim 2026

Kullanıcının isteğiyle güncel geliştirme dalının 45 commit'i main'e fast-forward ile alındı. Daha önce ff48b68 içinde uygulanıp düzeltilen S14 WIP geçmişi a87db7c ile birleştirildi; PlayerScreen çakışması incelenip test edilmiş 0.14.46 davranışı korundu. Birleşme ağacı 2bc7f54 ile aynı; uygulama ve cihaz testi betiği 7ef216a ile aynı. `wip/s14-review-fixes` birleştiği doğrulandıktan sonra silindi. Merge bekleyen dal kalmadı; ana checkout artık güncel.

TV'ye doğrulanmış `releases/NEXTV_TV_Design_Preview_0.14.46.apk` yeniden `install -r` ile kuruldu (kod 60). Çekilen kurulu APK'nın SHA-256'sı eşleşti: `b826893b7a9b1fdea69ff06339cbbd94c1778c01c146c5228fcc8208b179d185`. firstInstallTime aynı (`2026-10-13 23:50:03`), lastUpdateTime `2026-10-18 19:15:09`; veri silinmedi. Uygulama kodu değişmediğinden yeni paket/sürüm veya uygulama testi üretilmedi. Emülatörler açılmadı; TV'de uygulama/oynatma başlatılmadı.

Kontroller: kaynak ağacı eşitliği, Git atalık ilişkileri ve `git diff --check`. `check_project.py` eski/nested worktree'lerdeki eksik tarihî teslimler ve kısmen kaldırılmış logo klasörünün bağlantıları nedeniyle başarısız; teslim APK'sına ilişkin hata değil. Sıradaki ürün işi STEPS S14b; fiziksel kumanda/ses/TalkBack kabulü ve önceki ürün kararları hâlâ açık.

## Birleşmiş dal temizliği · 18 Ekim 2026

Kullanıcının isteğiyle main'e tamamen birleşmiş üç yerel dal silindi: `claude/logo-splash-files-f4683f`, `claude/sabah-kahvaltisi-oneri-870a0c`, `claude/trusting-margulis-73aa19`. Silmeden önce ilgili HEAD'ler/main atalık ilişkisi ve temiz çalışma ağaçları doğrulandı. Güncel uygulama dalı ve `wip/s14-review-fixes` korundu; main'e merge yapılmadı. TV'deki 0.14.46 değişmedi.

Klasör temizliği sınırlı kaldı: logo worktree kaydı Git tarafından kaldırıldı, fakat Windows erişim hatası nedeniyle dizin kısmen silinmiş durumda. Kalan dosyaları özyinelemeli silme girişimini otomatik onay incelemesi politika nedeniyle engelledi; tekrar denenmedi. Diğer iki eski worktree kendi commit'lerinde detached HEAD olarak tutuldu. Bu temizlik kaydı dışında kod değişmedi; uygulama testi gerekmedi, `git diff --check` kullanıldı.

## 0.14.46 · Oynatma hızı ve TV teslimi · 18 Ekim 2026

Kullanıcının hız ayarı isteği D077 ile tamamlandı ve en son sürümü kurma talebi kapsamında TV'ye veriler silinmeden kuruldu. Film/dizide Seçenekler → Oynatma hızı: 0,5×–2× (yedi değer). Ses perdesi 1'de tutulur; hız mevcut oynatıcı oturumu boyunca geçerli, çıkışta normal; canlıya geçince 1×. OK uygular, Geri uygulamadan bir düzey döner ve açan düğmeyi odaklar. Motorun gerçek hızı gösterilir, tahmini bitiş buna göre hesaplanır. Tünelli oynatma açıkken açıklamalı kapalı durum bulunur. S14b'nin kontrol tasarımı yenilemesi hâlâ sırada.

Kontroller: 344 JVM testi (0 hata, 1 atlanan), lint 0 hata/37 uyarı; release/debug/androidTest derlemesi. Kontrol menüsü eklendikten sonra 5 ilgili oynatıcı regresyonu geçti (iki yeniden bağlanma, iki bölüm sonu, mevcut kontrollerin geometrisi). İlk hız testinin kurulumunda ana iş parçacığında DNS hatası düzeltildi. Görsel incelemede alt hız satırının odak halkası kesildiği için kaydırmada 8 dp pay eklendi; test halka payını da denetler. Son kodda 5558'de yalnız `tools/run-device-tests.sh`: normal/1,5/2,0 ölçekte ayrı ayrı 3 hız testi geçti (toplam 9); normal ve 2,0 ekranları gözle de doğrulandı. Hızın gerçek Player parametrelerine uygulanması, pitch, tüm değerler, iptal/geri/odak, canlıya geçiş ve tünel sınırı testli. `git diff --check` temiz.

5556: 0.14.45 üstüne `install -r`, açılış/Evim görünürlüğü, aynı ilk kurulum tarihi ve çökme kaydı olmaması doğrulandı; gerçek yayın açılmadı. TV: 0.14.46 (kod 60), firstInstallTime `2026-10-13 23:50:03` değişmedi. Kurulu APK çekilip SHA-256 karşılaştırıldı: `b826893b7a9b1fdea69ff06339cbbd94c1778c01c146c5228fcc8208b179d185`. Teslim `releases/NEXTV_TV_Design_Preview_0.14.46.apk` + `.sha256`; ana checkout arşivinde de var. TV'de oynatma/kumanda/ses çıkışı denenmedi. 5556 ve 5558 kapalı, Gradle daemon durduruldu; 5554'e dokunulmadı.

Sonraki adım S14b (D072); hız seçimi yeni Seçenekler düzeninde korunmalı. Sonraki teslim en az kod 61 / 0.14.47. Satış/fiyat, İngilizce incelemesi, fiziksel kumanda/TalkBack ve diğer gap adımları açık.

## 0.14.45 · S14 inceleme düzeltmeleri ve TV teslimi · 18 Ekim 2026

Kullanıcının güncel “televizyona son sürümü yükleyebilir miyiz” isteğiyle S14b beklenmeden son hazır sürüm kuruldu. 0.14.45 (kod 59), S11–S14 ve S14 inceleme düzeltmelerini içerir: PCM destek uyarısı, doğru decoder hata kapsamı, açık panel opaklığı, uzun değerler/büyük yazı düzeni ve bölüm sırasına bağlı gezinme testleri. S14b kontrol yenilemesi ve istenen oynatma hızı henüz bu pakette yok.

Kontroller: 340 JVM testi (0 hata, 1 atlanan), lint 0 hata/37 uyarı, release ve androidTest derlemesi. Yalnız 5558'de ve `tools/run-device-tests.sh` ile normal ölçekte 16, 1,5 ve 2,0 ölçekte ayrı ayrı 12 ekran testi geçti; font_scale 1.0'a geri geldi. İngilizce 1,5/2,0 ekranları görsel olarak da okundu. `git diff --check` geçti. `check_project.py` bu worktree'de bulunmayan tarihî `releases` dosyalarının bağlantılarını bildiriyor; başarılı sayılmadı.

5556'da 0.14.44 üstüne `install -r`, açılış, gerçek Evim kaynağının görünmesi ve Ayarlar açılması doğrulandı; ilk kurulum tarihi aynı, çökme kaydı yok. TV'de `install -r` başarılı, firstInstallTime `2026-10-13 23:50:03` kaldı; kurulu APK çekilerek SHA-256 eşleştirildi: `e4e771a9ef98e31a556b976426676df6ef4622c16092ae89d3503289f0619f64`. Teslim `releases/NEXTV_TV_Design_Preview_0.14.45.apk` ve `.sha256` (ana checkout'taki teslim arşivine de kopyalandı). TV'de uygulama açılmadı; fiziksel kumanda/TalkBack ve desteklenmeyen gerçek AC-3 akışı denenmedi.

Devirde eksik kalan kayıtlar tamamlandı: D073–D076, PIN_ATTEMPTS/PROTECTION_STORE ve BKL-029…041. Altı bölümlü Ayarlar kapsamı, Android 8–12 dil yolu, İngilizce incelemesi ve satış kararları açık. Sıradaki iş film/dizi hızı, sonra S14b; bir sonraki TV paketi en az kod 60 / 0.14.46.

## Gap çalışması: Claude bölümü sona erdi · 18 Ekim 2026

Kullanıcının "Tüm gap'i kapatmak için çalışmaya başla" isteğiyle [gap planı](../design/gap/PLAN.md) uygulanıyor. Dalda bitenler (her adım uygulama + üç-dört bağımsız inceleme + düzeltme; derleme, birim testleri, lint ve test emülatöründe ekran testleri geçti): metin katmanı ve ayarlar çerçevesi, erişim kapısı, oynatıcı ve veri temelleri (S01–S07); başlık öneki düzeltmesi (X01); PIN veri katmanı, PIN penceresi ve Ebeveyn denetimi, içerik kilidi, kilit arayüzü, kaynak ve açılış koruması (S08–S12); ekran testlerinin onarımı ve üç ürün hatası (T01); İngilizce varsayılan, Türkçe ve Dil seçimi (S13); oynatıcı ayarları ve iki düzeyli Ayarlar (S14, inceleme düzeltmeleri yarım). Ayrıca: satış stratejisi raporu ([SALES_STRATEGY](../product/SALES_STRATEGY.md)), oynatıcı kontrolleri tasarımı (D072, kullanıcı onayladı), ayrı test emülatörü (5558) ve `tools/run-device-tests.sh`. TV'de 0.14.44 (a0f1908); S11–S14 TV'de yok. Devam yeri: [STEPS](../design/gap/STEPS.md) ve HANDOFF'un en üst kaydı.

## 0.14.44 · TV teslimi · 18 Ekim 2026

Kullanıcının isteğiyle ("tv yi güncelle") gap planının incelenmiş son hâli TV'ye kuruldu: commit a0f1908 (Faz A temelleri S01–S07, X01 ad düzeltmesi, PIN veri katmanı S08, Ebeveyn denetimi ayarları S09, içerik kilidi kararı S10, ekran testi onarımı T01). Kilit arayüzü (S11) incelemesi bitmediği için dahil değil. Sürüm 0.14.44 (kod 58); sürüm artışı yalnız temiz derleme kopyasında yapıldı, dalda henüz 57 (bir sonraki commit'te 58'e çıkar). Teslim: releases/NEXTV_TV_Design_Preview_0.14.44.apk (+ .sha256), SHA-256 186bc0f74a62468bf6ac8d5f7550736cbb1fb08ec3eeffc216581dda2df6ce32, imza önceki sürümle aynı. `adb install -r` ile 0.14.43'ün üstüne kuruldu; kurulu dosyanın SHA-256'sı eşleşti, firstInstallTime değişmedi (veri korundu); uygulama açıldı, çökme kaydı yok.

Kontroller: a0f1908'de `testDebugUnitTest`, `lintDebug`, `assembleRelease`, `assembleDebugAndroidTest` geçti; test emülatöründe (emulator-5558) 25 sınıf 167 ekran testi geçti. 5556'da gerçek "Evim" verisiyle 0.14.43'ün üstüne kurulup açıldı: kaynak kaydı v1→v2 geçişi sorunsuz, ana sayfa ve Ayarlar açıldı. Uyarı: kaynak kaydı artık v2; TV'ye 0.14.43 veya daha eski bir sürüm kurulursa kurtarma ekranı açılır (sıfırlanmazsa veri kaybolmaz).

## 0.14.43 · TV teslimi · 18 Ekim 2026

Kullanıcının isteğiyle ("Televizyonda son halini görmek istiyorum") D069, D070, bölüm araması düzeltmeleri ve D071 (vitrinde İzle + Ayrıntılar, kullanıcının seçimi) TV'ye kuruldu. Sürüm 0.14.43 (kod 57). Teslim: releases/NEXTV_TV_Design_Preview_0.14.43.apk (+ .sha256), SHA-256 954ed138e22140288d57e73a7c9bed85932c9a297b25aaca03d0daffc3742a92, imza önceki sürümle aynı. `adb install -r` ile 0.14.42'nin üstüne kuruldu; kurulu dosyanın SHA-256'sı eşleşti, firstInstallTime değişmedi (veri korundu); uygulama TV'de açıldı.

Kontroller: `testDebugUnitTest` geçti, `lintDebug` 0 hata, `assembleRelease` ve androidTest derlemesi geçti (HeroLayoutTest İzle/Ayrıntılar ve slayt dönüşü; PilotFlow/VisualPreview/ContinueCardLayout ilk odak `hero-watch`). 5556'da vitrin görüldü (İzle odakta, Ayrıntılar bilgi ikonuyla); emülatörde oynatma denenmedi. Enstrümantasyon testleri koşulmadı.

## Açılış kapağı, ana sayfa odağı, bölüm araması, raporlar · 18 Ekim 2026

Kullanıcının bildirimleri ve istekleriyle: (1) süreç canlıyken ikinci açılışta eski yükleme kartı yerine NEXTV kapağı (D069; bağımsız incelemenin 6 bulgusu giderildi: hareket azaltmada sabit gösterge, kurtarma ekranında kart, 14 sp etiket, test); (2) Ana Sayfa açılışta vitrinin "Ayrıntılar" düğmesinde (D070); (3) "Bu bölümde ara" düzeltmeleri: alan açılınca liste başına kayar, alan etiketiyle tam görünür (önce yarısı başlığın altında kalıyordu); klavye kapalıyken alandan Aşağı ilk sonuca gider (önce soldaki kategori sütununa atlıyordu; TV klavyesi tam bu durumu yaşar); her harfte sonuçlar boşalıp "Aranıyor…" ile yanıp sönmez; kategori seçiliyken etiket "Bu kategoride ara", sonuç yoksa "Bu kategoride bulunamadı" ve tek tuşla "Tüm filmlerde ara" (dizilerde, kanallarda, favorilerde karşılığı). Raporlar: [Play Store hazırlığı ve hukuki riskler](PLAY_STORE_READINESS.md), [TiviMate rekabet farkı](../product/TIVIMATE_GAP.md); ikisindeki öneriler kullanıcı kararı değildir.

Kontroller: `testDebugUnitTest` geçti, `lintDebug` 0 hata, `assembleRelease` ve androidTest derlemesi geçti (SearchNavigationTest: Aşağı her durumda sonuca, yeni `searchInACategoryOffersTheWholeSection`; LoadingLayoutTest: yeni `restoringCoverShowsProgressAndALabel`; PilotFlow/VisualPreview/ContinueCardLayout ilk odak vitrinde). 5556'da gerçek Evim ile görüldü: açılış odağı vitrinde, devam kartından Geri ile aynı karta dönüş; Filmler'de arama alanı tam görünür, Aşağı ilk sonuç (1 / 2), YERLI 2026'da "Bu kategoride bulunamadı" → "Tüm filmlerde ara" → ilk sonuçta odak; süreç canlıyken yeniden açılışta eski kart görünmedi. Enstrümantasyon testleri koşulmadı (5556'daki gerçek kaynağı silerler). Sürüm 0.14.42'de; TV'ye kurulmadı.

## 0.14.42 · TV teslimi · 17 Ekim 2026

Kullanıcının isteğiyle ("Sürümü 0.14.42 yap ve TV'ye veri silmeden kur") D067/D068 TV'ye kuruldu. Sürüm 0.14.42 (kod 56). Teslim: releases/NEXTV_TV_Design_Preview_0.14.42.apk (+ .sha256), SHA-256 73f2bf9531163b659a5ef38874a17c71eaab4c51e72e72c36306284790187c3a, imza 0.14.41 ile aynı. `adb install -r` ile 0.14.41'in üstüne kuruldu; kurulu dosyanın SHA-256'sı eşleşti, firstInstallTime 13 Ekim (veri korundu). TV uykudaydı, uygulama açılmadı. Xtream vitrin görselleri ve kumanda kısayolları TV'de kullanıcının değerlendirmesinde.

## Büyük vitrin, rehber kısayolları, kategori gizleme · 17 Ekim 2026

Kullanıcının istekleriyle (D067, D068): vitrin üst kenara kadar, tam genişlik, 372 dp ve geniş görselle (Xtream'de slayt ayrıntısından çekilir; yoksa bulanık afiş); rehberde Geri → gün/saat düğmeleri, CH+/CH− sayfa, İleri/Geri sarma gün, rakamla kanal, alt bant kaldırıldı, 50 dp satırla altı kanal; Canlı TV listesinin üst paneli kaldırıldı; Ayarlar → İçerik'te yetişkin kategorilerini gizle (varsayılan açık) ve kategorileri tek tek aç/kapat.

Kontroller: 163 JVM testi geçti (1 atlanan; CategoryVisibilityTest yeni), lint 0 hata, release/debug/test APK'ları derlendi. 5556'da gerçek Evim ile görüldü: vitrin geniş görselle (slaytlar sentetik Demo'dan), Canlı TV listesi panelsiz (yetişkin filtresiyle 4.531 → 4.290 kanal), rehberde Geri/CH+/rakam, kategori sayfaları; GREECE kapatılınca 4.247 kanal, geri açılınca 4.290. Xtream'de slayt ayrıntısının çekilmesi emülatörde görülmedi (Evim M3U); TV'de görülecek. Enstrümantasyon testleri koşulmadı. Sürüm 0.14.41'de, TV'ye kurulmadı.

## 0.14.41 · NEXTV teslimi · 17 Ekim 2026

Kullanıcının isteğiyle ("Televizyona yükle") bu dalın üç commit'i (UX denetimi, D064, D065/D066) TV'ye kuruldu. Sürüm 0.14.41 (kod 55). Teslim: releases/NEXTV_TV_Design_Preview_0.14.41.apk (+ .sha256), SHA-256 5fa8f07da7b7572a14402fde2f819ad4bb35d3eeedd92a16e885646bd661cc61. Dosya adı ürün adıyla (D066) NEXTV oldu; önceki teslimler eski adla kaldı. İmza sertifikası 0.14.40 ile aynı (4fe97f80…6065). TV'ye `adb install -r` ile kuruldu (0.14.40'tan); kurulu dosyanın SHA-256'sı eşleşti, firstInstallTime 13 Ekim olarak kaldı, yani veri korundu. TV kurulum sırasında uykudaydı; uygulama açılmadı. Launcher'daki ad NEXTV.

Kontroller: 161 JVM testi geçti (1 atlanan), lint 0 hata (D065 turunda), release derlendi. Enstrümantasyon testleri koşulmadı. TV'de görsel kontrol ve fiziksel kumanda (basılı OK süresi, CH+/CH−, rakam tuşları, rehber tuşu) kullanıcıda.

## NEXTV adı, sade adlar ve denetimin kalan maddeleri · 17 Ekim 2026

Kullanıcının kararıyla ürün adı NEXTV (D066): uygulama etiketi, sol menüde amblem/logo, kurulum, yükleme, kurtarma ve Uygulama hakkında'da logo, User-Agent. Paket kimliği değişmedi. Kullanıcının "önce açık maddelere devam" isteğiyle D065: sağlayıcı adları görüntüde sadeleşti ("TR: ", "(2024) DE", "TR/FILM ~"), rehbersiz kaynakta tekrar eden metin kalktı, rehber 56 dp satır ve kanal numarası, kartlarda basılı OK menüsü, kalan süre, yükleme göstergesi, kurulumda "Bilgileri göster", 12 sp altı yazı kalmadı.

Kontroller: 161 JVM testi geçti (1 atlanan), lint 0 hata, release/debug/test APK'ları derlendi. 5556'da gerçek Evim ile görüldü (HANDOFF). Ayarlar'da yanlışlıkla bir kez "Kataloğu yenile" çalıştı; gerçek sağlayıcıdan liste indirildi ve başarılı oldu (Evim 21.314 içerik), yeni User-Agent bununla da doğrulandı. Enstrümantasyon testleri koşulmadı. Sürüm 0.14.40'ta kaldı, TV'ye kurulmadı.

## Denetim düzeltmeleri, büyük afişler ve gradient oynatıcı · 17 Ekim 2026

Kullanıcının isteğiyle (kütüphane bandını kaldır, afişleri ve yazıları büyüt; oynatıcıyı alttan gradient ile çöz) ve devam istemesiyle UX denetiminin 15 maddesi uygulandı (D064, [denetim](../design/UX_AUDIT_0.14.40.md)). Öne çıkanlar: Filmler/Diziler'de bilgi paneli yok, 4/5 sütun büyük afiş, 15/12 sp kart yazıları; oynatıcıda opak panel yerine alt scrim; canlıda CH+/CH−, Yukarı/Aşağı ve rakamla kanal; filmde Sol/Sağ ve FF/RW ile sarma; kütüphanede katmanlı Geri ve menüden girişte doğru odak; 2 dk/%2 altı izleme devam sayılmaz; "8x8" gibi başlıklar sezon sayılmaz; bölüm kartında yalnız bölüm adı; odakta tek mint dil.

Kontroller: 160 JVM testi geçti (1 atlanan; 3 yeni test), lint 0 hata / 33 uyarı (yeni hata yok), release, debug ve test APK'ları derlendi. 5556'da gerçek "Evim" ile elle doğrulandı (STATUS altındaki denetim kaydı ve HANDOFF). Enstrümantasyon testleri koşulmadı (5556'daki gerçek Evim'i silerler); güncellenen beklentiler (5 sütun konum sayacı, "Favorilerden çıkar", bölüm adı) cihazda koşulmadı. Sürüm numarası değişmedi (0.14.40), teslim APK'sı üretilmedi, TV'ye kurulmadı.

## UI/UX denetimi · 17 Ekim 2026

Kullanıcının isteğiyle 0.14.40 için ayrıntılı UI/UX denetimi yapıldı: [UX_AUDIT_0.14.40](../design/UX_AUDIT_0.14.40.md). Kod okundu, ardından kullanıcının isteğiyle ("gerçek data ile kontrol et") bulgular 5556'da gerçek "Evim" M3U kaynağıyla (4.531 kanal, 14.626 film, 2.172 dizi) ekranda denendi. 26 bulgu; 6'sı yüksek öncelik. Gerçek veride ayrıca dört yeni sorun çıktı: bölüme yeniden girişte odağın Rehber/Sırala düğmesine kaçması, 19 sn'lik oynatmanın "İzlemeye devam et"e girip film rafını silmesi, sağlayıcı adlarının temizlenmeden gösterilmesi, "8x8" adlı dizinin Sezon 8 · B8 sanılması. Kod değişmedi; öneriler kullanıcı önceliklendirmesini bekliyor. Fiziksel TV'ye dokunulmadı.

## 0.14.40 · Birleşik teslim · 16 Ekim 2026

Kullanıcının isteğiyle bütün değişiklikler birleştirildi ve TV'ye kuruldu:
- Kişisel ana sayfa, vitrin slider'ı ve eş merkezli odak halkası (D063).
- NEXTV logo, ikon, banner, splash ve açılış sesi (D062, logo dalı).
- main'deki 0.14.38 belgeleri ve SearchNavigation odak kontrolü.

Sürüm 0.14.40 (kod 54). 0.14.39 (kod 53) numarası başka bir oturumun emülatöre kurduğu, commit edilmemiş bir derlemede kullanılmıştı; çakışmasın diye atlandı.

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.40.apk (+ .sha256), SHA-256 9272cbe502ef1857cdfd71bb2f3307eaeded899ce80c885822a77a8fa805909e. TV'ye `adb install -r` ile kuruldu (0.14.38'den). Kurulu dosyanın SHA-256'sı eşleşti; firstInstallTime 13 Ekim olarak kaldı, yani veri korundu. Uygulama açılmadı.

Kontroller (birleşik ağaç): 157 JVM testi geçti (1 atlanan), lint 0 hata / 32 uyarı, release/debug/test APK'ları derlendi, imza doğrulandı. Enstrümantasyon testleri koşulmadı (5556'daki gerçek Evim kaynağını silerler). Slider ve logolar TV'de henüz görülmedi.

## Kişisel ana sayfa ve vitrin slider'ı · 16 Ekim 2026

Kullanıcının istekleri ve seçimleriyle (D063) ana sayfa değişti:
- Vitrindeki "Kanalları aç" kalktı.
- Vitrin, ekleme tarihi olan en yeni 6 film ve diziden bir slider oldu: 8 sn'de bir geçiyor, Sağ/Sol ile elle değişiyor, nokta göstergesi var, Hareketi azalt açıksa durur.
- Geçmiş oluşunca "Canlı yayınlar"ın yerini "Son izlediğin kanallar", "Film kütüphanen"in yerini "İzlemeye devam et" alıyor. Geçmiş yokken bu iki raf başlangıç rafı olarak kalıyor.
- Devam rafı, son bölümü bitirilen diziyi de tutuyor; kart sıradaki bölümü açıyor.

Kontroller:
- 157 JVM testi geçti (1 atlanan; en yeni seçimi ve bitmiş bölüm için 3 yeni/güncel test). Release, debug ve test APK'ları derlendi.
- 5556'da (veri korunarak release kuruldu) "Kanalları aç"ın kalktığı, kategori yazısının ortalandığı (dolgu 228–307, yazı 257–277) ve halkaların eş merkezli olduğu görüldü.
- 5556'daki "Evim" gerçek sağlayıcının **M3U** listesi (Kaynaklar: "Kaynak 1 · M3U", 21.308 içerik). M3U'da tarih ve puan yok, bu yüzden orada vitrin tek başlık kalıyor. Aynı sağlayıcının özel Xtream dökümünde 14.609 filmin hepsinde `added`, 2.173 dizinin hepsinde `last_modified` var. Genişletilmiş PrivateCatalogPresentationAuditTest bu dökümü ayrıştırma, kayıt ve kaynak birleştirme yolundan geçirdiğinde 6 slayt çıkıyor. Ayarlar → Kataloğu yenile bir kez çalıştırıldı (18:28, gerçek sağlayıcı, yalnız liste).
- Slider'ı göstermek için ikinci kaynak olarak sentetik "Demo" eklendi (Xtream, `http://10.0.2.2:8766`, yalnız oturum boyunca çalışan yerel sunucu, fikstür görselleri, sentetik tarih). Doğrulananlar: "YENİ EKLENEN", 6 nokta, 8 sn'de kendiliğinden geçiş, Sağ ileri gidiyor ve sonda başa sarıyor, Sol geri gidiyor, ilk slaytta Sol menüyü açıyor, odak Ayrıntılar'da kalıyor, raflar yerinden oynamıyor, metin sıralı soluyor.
- Çok ajanlı inceleme 5 hata doğruladı, hepsi düzeltildi: `hero-dots` etiketinin semantikle silinmesi, ilk izlemeden sonra odağın menüye kaçması, slayta göre vitrin yüksekliği, geri yüklenen slayt indeksinin sınırlanmaması, ayrıntıdan çıkıldıktan sonra devam oynatımının başlaması. 1 bulgu çürütüldü.
- HeroLayoutTest koşulmadı: test sınıfları 5556'daki gerçek Evim kaynağını siliyor.
- Devam kartının "sıradaki bölüm" akışı cihazda denenmedi; bu emülatörde izleme geçmişi yok.

## Odak halkası köşeleri ve kategori yazısı · 16 Ekim 2026

Kullanıcı iki görsel kusur gösterdi. İlki: odak halkası ile dolgunun köşeleri eş merkezli değildi. tv-material halkayı yüzeyin 3 dp dışına çiziyor ama sabit köşe yarıçapını büyütmüyordu, bu yüzden köşelerdeki boşluk kenarlardakinden genişti. Yeni `focusRing(radius)`, halka yarıçapını dolgu yarıçapı + 3 dp yapıyor. Sabit yarıçaplı bütün kart ve satırlar bunu kullanıyor: ana sayfa, kütüphane, ayrıntı, arama, ayarlar, oynatıcı parça paneli, kategori ve kanal satırları. Hap biçimli eylemler zaten eş merkezliydi, değişmedi. İkincisi: kütüphane kategori satırında yazı yaklaşık 2 dp yukarıdaydı (5556'da ölçüm: dolgu y=228–307, yazı 253–274). Surface içeriğini uzatmadığı için satır kendi yazı yüksekliğinde kalıp üste yapışıyordu. Satır artık 40 dp'yi kendisi taşıyor. Yeni karar yok.

## NEXTV logo ve splash · 16 Ekim 2026

Kullanıcının isteğiyle ("buradan logoyu ve splash'i al") NEXTV logo/splash paketi uygulandı (D062), ardından logolar kullanıcının üç SVG'siyle (`design/brand`: amblem, dikey, yatay) güncellendi: amblemli launcher ikonu, siyah zeminli yatay logolu vektör TV banner'ı, siyah başlangıç penceresi ve splash'teki işaretin yerinde amblem, dikey logoyla 2,8 sn'lik animasyonlu splash ve paketin açılış sesi. Splash en az 3 sn görünür; kayıtlı kaynaklar hâlâ açılıyorsa gösterge ve "Hazırlanıyor..." ile bekler. Hareketi azalt, ses ayarı ve "her tuş splash'i kaldırır" korunur. Tuş artık sesi de durdurur ve kaynaklar yüklenirken (Compose'da odak yokken) de çalışır; eskiden bu durumda tuş geçişe ulaşmıyordu. Google Sans eklenmedi (lisans), etiket Roboto. Uygulama adı değişmedi (D007). Sürüm numarası değişmedi, teslim APK'sı üretilmedi.

Kontroller (logo güncellemesi): assembleRelease ve assembleDebugAndroidTest geçti; lint 0 hata/32 uyarı, tek yeni uyarı 320×180 dp vektör banner için VectorRaster (projenin eski vektör banner'ı da aynı uyarıyı veriyordu). Logo yolları kaynak SVG'lerle birebir, launcher ikonu 66 dp güvenli daire içinde, banner logosu ortalı, pencere işareti 960×540 dp'de splash işaretiyle 0,06 dp içinde hizalı (bağımsız doğrulama iş akışıyla hesaplandı). 5556'da yeni logolu splash ve launcher yeniden başlatıldıktan sonra yeni banner görüldü. Paket turundaki kontroller: 5556'da (nextv_design_api36) release derlemesiyle görüldü: splash'in zaman çizelgesi, ~3,9 sn'de ana sayfaya geçiş ve ilk odak "Ayrıntılar"da, tuşla anında kalkma ve ses oynatıcısının kapanması, normal açılışta sesin başlaması (dumpsys audio). Enstrümantasyon ve JVM testleri çalıştırılmadı. Kullanıcı sesin kendi seçtiği telifsiz bir ses olduğunu ve kullanılabileceğini bildirdi (D062). Açık: banner'ın siyah zemini launcher'ın koyu arka planında kart kenarını kaybettiriyor (zemin rengi kullanıcı kararı); uygulama içi "IPTV" başlıkları (sol menü, Kurulum, Yükleme) değişmedi, logo ile değiştirilip değiştirilmeyeceği kullanıcıya sorulacak (D007); ses eskisinden yüksek (tepe ≈ −0,4 dBFS); "Hazırlanıyor..." beklemesi görülmedi (yükleme 3 sn'den kısa sürdü); Hareketi azalt yolu denenmedi; fiziksel TV görsel/ses kabulü ve Figma.

## 0.14.38 · Sıralama inceleme düzeltmeleri, Esc ve düşen cihaz testleri · 16 Ekim 2026

0.14.37 salt okunur bir inceleme iş akışından geçti (6 inceleyici, bulgu başına 2 şüpheci, bir eleştirmen; 11 bulgu doğrulandı, 4'ü çürütüldü) ve doğrulananlar düzeltildi: Ad (A–Z)/Son eklenen sağlayıcı ayraçlarını başa taşımıyor; sıralama artık kütüphaneye her girişte ana iş parçacığında yapılmıyor, modelde önbellekte (seçim arka planda sıralanır); ana sayfa afişlerinde de puan rozeti var; Geçmiş izleme sırasını korur ve orada Sırala yok; büyük yazıda düğme yalnız "Sırala". 0.14.36'dan beri düşen altı cihaz testi ayrı bir teşhis iş akışıyla incelendi: beşi D059/D060 sonrası eskimiş ya da yerleşmemiş ekrana tuş gönderen testti, biri ürün hatasıydı: klavye açıkken Esc, klavyenin açık olduğunu pencere girintilerinden tahmin ediyordu; TV klavyesi geç bildirince tek Esc hem klavyeyi kapatıp hem Geri yapıyordu. Esc artık pencereye gerçek bir Geri olarak verilir (platform kumandanın Geri'si gibi yönlendirir). Ayrıca Ayarlar açılırken hızlı basılan Aşağı'nın geç gelen ilk-satır odağıyla geri alınması düzeltildi. [QA](native-sort-rating-38-qa.json), D058/D061 notları. 155 JVM, lint 0 hata/32 uyarı; 5556'da SearchNavigation iki sınıf turunda 21/21, SourceRegistryStorage 12/12, HeroLayout 1/1, VisualPreview 11/11; oynatıcı sınıfları: after an emulator restart PilotFlow 8/8, GuideFlow 13/13, EpisodeAutoplayFlow 11/11; before the restart the emulator (2 h under load, -no-audio) logged AudioTrack 'device stall' and 4 player cases failed (position did not advance), the same cases passed in the first round of this version. SHA-256 7e173b44d9ef748b3e9ae9340dd1feafaa490c876029669d675308b5597f60a0. Kullanıcının isteğiyle TV'ye önce 0.14.37, ardından 0.14.38 kuruldu (`adb install -r`, SHA eşleşti, veri korundu, speed derleme, uygulama açılmadı).

## Ara/Rehber/Oynatıcı/Ana sayfa Geri aralığı · 16 Ekim 2026

Çıkış sorusu düzeltmesindeki ilke ekranların kendi Geri işleyicilerine de uygulandı: model başka rotaya geçtiği halde ekran bir sonraki kareye kadar duruyorsa, o arada gelen Geri artık eski ekranın kararını vermez, PilotApp'in rota kuralına gider (ana sayfa → çıkış sorusu, diğerleri → `model.back()`). Örnek: Ara'nın menüsünde Geri ana sayfaya döner, hemen ardından basılan ikinci Geri eskiden kayboluyordu, artık çıkış sorusunu açar. Rehber ve Oynatıcı için aynısı; 0.14.36'da eklenen ana sayfa "Geri menüyü açar" işleyicisinde de aynı aralık vardı (Ana sayfa → Kütüphane anındaki Geri kayboluyordu) ve düzeltildi. Ekranlar kendi rotalarındayken davranış aynı; yeni karar yok. [QA](native-screen-back-gap-qa.json).

Yeni üç belirlenimci vaka (çift Geri aynı ana iş parçacığı turunda) eski kodla 0/3, yeni kodla 9/9. 5556'da sınıf turu: SearchNavigation 17/21, GuideFlow 12/13, PilotFlow 7/8, EpisodeAutoplay 11/11; düşen 6 vaka değişiklik öncesi derlemede de aynı satırda düşüyor (0.14.36'da uyarlanıp hiç koşulmamış testler: kaldırılan "Tüm kaynaklar", kategori sütunu odağı, Esc sonrası alan odağı, ana sayfada ilk Geri'de menü odağı). 155 JVM (1 atlanan), lint 0 hata/32 uyarı. Kullanıcının onayıyla testler 5556'daki kendi kaynağını sildi; 5556'da yayımlanan 0.14.37 release ve sentetik "Evim" kaynağı kurulu, uygulama ana sayfada. 0.14.37 Ara/Rehber/Oynatıcı kısmını içerir; ana sayfa eki bir sonraki teslimle çıkar.

## 0.14.37 · Sırala ve puan rozeti · 16 Ekim 2026

Kullanıcının seçimiyle: Filmler ve Diziler başlığında "Sırala: …" düğmesi (Varsayılan, Son eklenen, Ad (A–Z), Yıl, Puan; seçim bölüm başına cihazda saklanır), afişlerin sol üstünde sağlayıcı puanı ("★ 7,5"), bilgi panelinde ve ayrıntıda aynı puan. Puan ve eklenme zamanı Xtream listesinden okunur (M3U'da yok); katalog kaydı v4'e geçti, eski kayıttaki başlıklar bir sonraki yenilemeye kadar puansız/tarihsiz görünür (6 saatten eski katalog açılışta kendiliğinden yenilenir, ya da Ayarlar → Kataloğu yenile). [QA](native-sort-rating-37-qa.json), karar D061. 155 JVM (3 yeni), lint 0 hata/32 uyarı; emülatörde sürüm derlemesiyle Filmler başlığında "Sırala: Varsayılan" etiketle doğrulandı (ekran görüntüsü yok); iletişim kutusu, sıralama sonucu ve rozet elle bakılmadı; enstrümantasyon çalıştırılmadı. SHA-256 2c18cbbe41b35be1546bc34d562661fdfae415883d48baebaa5899cfe8fad69d. TV 0.14.36'da kaldı.

## 0.14.36 · Gezinme: menüye Geri, kategori OK ile, kaynak seçici yok · 16 Ekim 2026

Kullanıcının istekleriyle: ana sayfada içerikteyken Geri sol menüyü "Ana Sayfa"da açar (menüdeyken Geri çıkış sorusunu açar); "Tüm kaynaklar" seçicisi bütün ekranlardan (Kütüphane, Ara, Rehber) kaldırıldı, kaynaklar Ayarlar'dan yönetilir ve eski kayıtlı kaynak seçimi açılışta "tüm kaynaklar"a sıfırlanır; kategori sütununda gezinmek listeyi değiştirmez, OK değiştirir; menüden Canlı TV/Filmler/Diziler/Favoriler'e girince odak kategori sütununda seçili "Tüm …" satırındadır (afişe atlanmaz), Geri ile bir başlıktan dönüşte o kart korunur. [QA](native-navigation-36-qa.json), karar D060. 152 JVM, lint 0 hata/31 uyarı; emülatörde sürüm derlemesiyle arayüz etiketleri okunarak elle doğrulandı (ekran görüntüsü yok); enstrümantasyon testleri uyarlandı ama çalıştırılmadı. SHA-256 edb832493d729fa507494b000051ddba35683e844946477c4687800b843e4ebf. Kullanıcının isteğiyle fiziksel TV `adb install -r` ile 0.14.34'ten 0.14.36/code50'ye güncellendi (SHA-256 eşleşti, veri korundu, speed derleme, uygulama açılmadı). Emülatörün tam ekran kipi yok; pencere köşeden büyütülebilir.

## 0.14.34 · Ana sayfada Tümünü gör ve kaynak seçici yok · 16 Ekim 2026

Kullanıcının isteğiyle ana sayfadan raf başlıklarındaki "Tümünü gör" düğmeleri ve "Tüm kaynaklar" seçicisi (vitrinde, vitrinsiz başlıkta ve boş ana sayfada) kaldırıldı. Kaynak seçici Kütüphane, Ara ve Rehber'de kalır; bölümlere sol menüden gidilir. Başlık satırlarının yüksekliği korunduğu için vitrin ve raflar kaymaz. [QA](native-home-buttons-34-qa.json), karar D059. 152 JVM, lint 0 hata/32 uyarı; dört enstrümantasyon testi yeni düzene uyarlandı ama çalıştırılmadı. Emülatör kapalı olduğu için ekranda kontrol edilmedi. SHA-256 d33cfd6fe943b11d9538206e82649543184d335e430d89cc13df5b8f6ce0b467. Kullanıcının isteğiyle fiziksel TV `adb install -r` ile 0.14.33'ten 0.14.34/code48'e güncellendi (TV'deki APK SHA-256'sı eşleşti, ilk kurulum zamanı 13 Ekim korunarak veri silinmedi, speed derleme; uygulama açılmadı, test yapılmadı).

## 0.14.33 · Alandan çıkınca klavye kapanır · 16 Ekim 2026

Kullanıcı klavyenin ekranda kalıp kapanmadığını bildirdi. Yeni alan yapısında Compose'un odak kaybında klavyeyi kapatma çağrısı alana ulaşmıyordu. Artık hiçbir metin alanı odakta kalmayınca klavye kapanıyor; alanlar arasında geçişte açık kalıyor. [QA](native-keyboard-on-ok-33-qa.json), [sözleşme](../design/KEYBOARD_ON_OK_31.md). 152 JVM, lint 0 hata/32 uyarı. Cihaz testi çalıştırılmadı (emülatörde kullanıcının kendi kaynağı var, testler kaynakları siler). Bu yeni davranış elle de denenmedi; kullanıcının denemesine bırakıldı. SHA-256 daedf7ece05f6c38491a7e88ce02538440087f455c4db34f181c0d0ead68d583. Emülatöre kuruldu. Kullanıcının isteğiyle fiziksel TV `adb install -r` ile 0.14.29'dan 0.14.33/code47'ye güncellendi (TV'deki APK SHA-256'sı eşleşti, ilk kurulum zamanı 13 Ekim korunarak veri silinmedi, `cmd package compile -m speed` ile derlendi; uygulama açılmadı, test yapılmadı).

## 0.14.32 · Klavye gerçekten OK ile açılır, Esc Geri'dir · 16 Ekim 2026

Kullanıcı 0.14.31'de klavyenin hâlâ kendiliğinden açıldığını ve emülatörde Geri için bastığı Esc'nin çalışmadığını bildirdi. Neden: odakta gizleme hilesi kaynak formunda tutmadı (klavye isteği ~0,3 sn sonra geliyordu); Compose Esc'yi "odak grubundan çık" sayıyordu. Metin alanları (`EntryField`, `SearchField`) `TextFieldState` API'sine taşındı (`showKeyboardOnFocus = false`); OK klavyeyi platformdan açar, klavye açıkken Geri yalnız klavyeyi kapatır; Esc kökte Geri'ye bağlandı. Emülatörde elle doğrulandı. [Sözleşme](../design/KEYBOARD_ON_OK_31.md), [QA](native-keyboard-on-ok-32-qa.json).

152 JVM (1 atlanan), lint 0 hata/32 uyarı. Cihaz testleri tamamlanmadı: MultiSource 7/7, SetupLayout 1/1, GuideFlow 11/11, VisualPreview 11/11 geçti; SearchNavigation 16/20, SourceLayout 2/5, PilotFlow 6/8 düştü ve incelenmedi (çoğu metin alanı olmayan ekranlarda odak denetimi, emülatör yükü yüksekti). Kullanıcı uzun turları durdurmamı, tam testi yayın öncesine bırakmayı istedi. 0.14.32/code46 sürüm APK'sı releases içinde; SHA-256 1ee0605a94052886f84d2b84b9228536d7f95994d2d11d6cde311b14e128bea0. 5556'da kurulu, kaynaksız açılır (kullanıcı kendi listesini girecek). Fiziksel TV'ye kurulmadı.

## Çıkış sorusu kararsızlığı giderildi · 16 Ekim 2026

`SearchNavigationTest#homeBackAsksBeforeLeavingTheApp` 5556'da tek başına 1/3 geçiyordu: ana sayfadaki ilk Geri çıkış sorusunu açmıyordu. Kök neden üründeydi: PilotApp'teki iki Geri işleyicisinin etkinliği ancak yeni rota bir sonraki karede uygulanınca güncelleniyor. İçe aktarma bitip model HOME'a geçtiğinde ilk ana sayfa karesi 0,1–0,4 s sürebiliyor; bu arada gelen Geri hâlâ etkin olan yükleme ekranı işleyicisine düşüyor, `model.back()` HOME'da bir şey yapmadığı için kayboluyordu. Kütüphane ya da ayrıntıdan dönüşte hızlı basılan Geri de aynı biçimde kaybolabiliyordu. Kanıt: geçici günlükle test Geri'yi 11.215'te gönderdi, genel işleyici 11.227'de aldı, kompozisyon HOME'a 11.339'da geçti. İki işleyici birleşti; karar Geri geldiği anda modelin rotasına göre veriliyor (HOME → çıkış sorusu, diğerleri → `model.back()`). D055 davranışı aynı, yeni karar yok. Sınıf turunda ikinci bir test aralığı da çıktı: soru kapandıktan sonra sistem ~100 ms girdi odağını kapanan diyalog penceresinde tutuyor ve o arada gönderilen tuşu uygulamaya hiç ulaştırmadan atıyor (InputDispatcher "no input channel"); test artık sonraki tuştan önce ana sayfa penceresinin odağı geri almasını bekliyor. [QA](native-exit-back-gap-qa.json).

Yeni belirlenimci vaka (rota değişimi ve Geri aynı ana iş parçacığı turunda) eski kodla 0/2, yeni kodla 3/3. Son kodla çıkış vakası tek başına 6/6, SearchNavigation 20/20, PilotFlow 8/8; 152 JVM (1 atlanan), lint 0 hata/32 uyarı. Yeni APK yayımlanmadı; düzeltme bir sonraki teslimle çıkacak. 5556 yeniden 0.14.31 release ve sentetik "Evim" kaynağıyla ana sayfada bırakıldı. Açık: Ara/Rehber/Oynatıcı'nın kendi Geri işleyicilerinde aynı aralık, fiziksel TV.

## 0.14.31 · Klavye yalnız OK ile açılır · 16 Ekim 2026

Kullanıcı metin alanlarının klavyeyi kendiliğinden açmamasını, kendisi tıklayınca açılmasını istedi. Artık hiçbir alan odak alınca klavye açmaz (Ara, bölüm araması, Kaynak ekle/düzenle formu, rehber adresi, saat dilimi araması); odaktaki alanda OK klavyeyi açar, Geri yalnız klavyeyi kapatır ve odak alanda kalır. Eski metin alanı API'si odakta klavyeyi her zaman istediği için aynı karede gizleme isteği gönderiliyor; sistem günlüğünde gösterme isteği yok, yani klavye bir an bile görünmüyor. [Sözleşme](../design/KEYBOARD_ON_OK_31.md), [QA](native-keyboard-on-ok-31-qa.json), [görseller](../../design/reviews/LATEST.md), karar D058.

152 JVM (1 atlanan) başarılı; lint 0 hata/32 uyarı. 5556'da (pencereli, normal yazı) SourceLayout 5/5, MultiSource 7/7, SetupLayout 1/1, GuideFlow 11/11, PilotFlow 8/8, VisualPreview 11/11, SearchNavigation 18/19 (yeni klavye vakası geçiyor; düşen `homeBackAsksBeforeLeavingTheApp` bu değişiklikten önce de aynı oranda kararsızdı). Klavyenin odakta açıldığını varsayan üç test yeni davranışa uyarlandı. 0.14.31/code45 sürüm APK'sı releases içinde; SHA-256 94970a72ddee2d9711ba146aaa3723e967b2c52514cca49bbdd3a563653daf35. Sürüm derlemesi 5556'da elle denendi; emülatör kullanıcının denemesi için açık bırakıldı. Fiziksel TV'ye kurulmadı. Açık: TCL klavyesiyle TV'de deneme, kullanıcı kabulü.

## 0.14.30 · Sezon rafları ve konum sayacı · 16 Ekim 2026

0.14.29'da açık kalan TiviMate parçaları uygulandı. Dizi ayrıntısında sezon düğmeleri ve üçlü bölüm ızgarası yerine her sezon için "Sezon N", hemen yanında hatırlanan bölümün "2 / 10" sayacı ve tek satır 16:9 bölüm rafı ("E1." + bölüm adı, süre, Devam et/İzlendi, ilerleme çizgisi). İlk odak doğrudan bölümde (oynatılan ya da Oynat'ın başlatacağı); Aşağı/Yukarı sezonlar arasında rafın hatırladığı bölüme gider. Kütüphanede normal yazıda başlık ızgaranın üstünde sabit, odak afişlerdeyken "9 / 80" konum sayacı gösterir (büyük yazıda başlık eskisi gibi kayar). Sayaçlar konumu yalnız kendi metninde okur; odak hareketi ekranı yeniden oluşturmaz, odakta büyüme yok. [Sözleşme](../design/TIVIMATE_SEASONS_30.md), [QA](native-tivimate-seasons-30-qa.json), [görseller](../../design/reviews/LATEST.md), karar D057.

152 JVM (1 atlanan) başarılı; lint 0 hata/32 uyarı. Son derlemede 5556'da (normal yazı) VisualPreview 11/11, PilotFlow 8/8, EpisodeAutoplay 11/11, SearchNavigation 17/18: düşen `homeBackAsksBeforeLeavingTheApp` (ana sayfa çıkış sorusu, değişen ekranlara dokunmuyor) aynı kütüphane koduyla önceki emülatör örneğinde iki kez geçti, pencere açılmadan çalışan emülatörde tek başına 1/3. EpisodeAutoplay'deki arka plan konum vakası önceki örnekte kararsızdı; 0.14.29 DetailScreen'iyle de 3/4 düştü, değişiklikten değil. 0.14.30/code44 sürüm APK'sı releases içinde; SHA-256 2bd50f94506fba31ecae397a8ef8c48284a1606aa4c6356d6e25b7580de87034, v2 imza, hata ayıklama kapalı; 5556'da açılışı denendi (çökme yok). Fiziksel TV'ye kurulmadı. Açık: TV'de görsel kabul ve kare ölçümü, büyük yazı turu, sıralama/puan rozeti, Figma.

## 0.14.29 · Akıcılık ve TiviMate yapısı · 16 Ekim 2026

Kullanıcı 0.14.28'de her hareketin takıldığını, TiviMate geçişlerinin anında hissettirdiğini söyledi; TiviMate'in Canlı TV, Filmler, Diziler ve ayrıntı ekranlarını gösterip aynı kategori mantığını, menüde Google'ın yuvarlak simgelerini ve menü açılınca içeriğin kaymasını istedi; gece boyunca kararı bana bırakıp sabah akıcı bir sürüm görmek istedi. TV'de ölçüm (gfxinfo): karelerin %81,6'sı gecikmeli. Nedenler ve çözüm: teslimler hata ayıklama derlemesiydi → `release` (R8, 18 MB → 3 MB, hata ayıklama kapalı, aynı imza); odakta büyüme animasyonu bileşimde 300/500 ms çalışıyordu → kaldırıldı, odak renk ve çerçeveyle; ana sayfada her odak hareketi tüm durumu güncelliyordu → gözlemlenmeyen bellek. Emülatörde aynı tuş dizisinde gecikmeli kare %69 → %28, ortanca 61 → 34 ms, %99'luk dilim 450 → 69 ms (yazılım GPU; oran anlamlı, mutlak değer değil). Canlı TV, Filmler, Diziler: solda kategori sütunu (sanal kategoriler, ardından sağlayıcı sırası), Canlı TV'de numaralı satır ve şimdi/sonra, Filmler/Diziler'de bilgi paneli ve afiş ızgarası (odakta 7 sütun). Menü simgeleri Material Symbols Rounded, seçili olan dolu; menü 160/120 ms, içerik menü kadar kayar. [Sözleşme](../design/FLUID_TIVIMATE_29.md), [TiviMate referansı](../design/TIVIMATE_REFERENCE.md), [akıcılık rehberi](../design/ANIMATION_SMOOTHNESS.md), [QA](native-fluid-29-qa.json), [görseller](../../design/reviews/LATEST.md), karar D056.

152 JVM (1 atlanan) başarılı; lint 0 hata/32 uyarı. Normal yazıda menü ve kütüphaneden etkilenen sınıflar son kodla geçti (ayrıntı QA'da); VisualPreview artık 11/11 (odak büyümesi kalkınca eski hizalama vakası da geçiyor). 0.14.29/code43 sürüm APK'sı releases içinde; SHA-256 befe19eed7035e55da55e5e8dbe4bbaa33b28df2a1a83188b8e2e05fbfc9890c. Kullanıcının gece verdiği izinle fiziksel TV'ye `adb install -r` ile kuruldu (veri korundu, TV'deki APK özeti eşleşti, `cmd package compile -m speed` ile önceden derlendi; uygulama açılmadı, test yapılmadı). TV'deki eski katalog kaydında sağlayıcı kategori sırası yok; Ayarlar → Kataloğu yenile ya da 6 saatlik otomatik yenileme sonrası TiviMate sırası gelir. Açık: TV'de görsel kabul ve kare ölçümü, dizi ayrıntısının TiviMate düzeni, konum sayacı/sıralama, Figma.

## 0.14.28 · Sol menü, TCL düzeninde Ara, çıkış sorusu · 15 Ekim 2026

Kullanıcı TV'deki iki örneği (TCL arama ekranı, NEXTV sol menüsü) ekran görüntüsüyle gösterip bizim stilde istedi; ayrıca ana sayfada Geri'de çıkış sorusu istedi. Üst gezinme çubuğu sola taşındı: kapalıyken 72 dp simge şeridi, odakta içeriğin üstüne açılan 208 dp yazılı menü (içerik yeniden dizilmez, arka plan kararır); menüye uzaktan kumandayla her giriş bulunduğun bölüme düşer, sağ ok ekranın hatırladığı öğeye döner. Ara üstte geniş hap alan ve türe göre yatay raflarla yeniden yapıldı; yazarken en iyi eşleşmenin rafı alanın hemen altında kalır. Ana sayfada Geri "Uygulamadan çıkmak istiyor musunuz?" sorusunu açar (odak Vazgeç). [Sözleşme](../design/SIDE_NAV_SEARCH_28.md), [QA](native-side-nav-28-qa.json), [görseller](../../design/reviews/LATEST.md), karar D055.

Çok ajanlı haritalama ve inceleme (dört bakış açısı, her bulguya bağımsız doğrulayıcı) 13 bulguyu doğruladı, 6'sını çürüttü; doğrulananlar ve testlerde çıkan 3 kusur düzeltildi. 150 JVM (1 atlanan) başarılı; lint 0 hata/32 uyarı. Normal yazıda 16 sınıf/100 vakalık turda kalan düşenler bilinen iki ortam vakası ve tek başına geçen iki zamanlama vakası; son APK ile SearchNavigation 16/16. 2.0 yazıda SearchNavigation 16/16 (önceki sürümlerde düşen iki başlık vakası artık geçiyor), VisualPreview 10/11 (aynı ortam vakası). 0.14.28/code42 APK releases içinde; SHA-256 9140b6cceb50a15daa90c4541b1cc57588212550d876f882fdcfd604061cbc2d. 5556'da kurulu, yazı 1.0; test sınıfları sentetik kaynakları sildiği için uygulama Kaynak ekle ekranında açılır. Ardından kullanıcının isteğiyle fiziksel TV `adb install -r` ile 0.14.27'den 0.14.28/code42'ye güncellendi: TV'deki APK SHA-256'sı eşleşti, ilk kurulum zamanı (13 Ekim) değişmedi, veri korundu; uygulama açılmadı, test yapılmadı. Açık: TV'de görünüm, kullanıcı kabulü, Figma, rehberde sol okun menüye geç ulaşması.

## 0.14.27 · Yazarken arama sonuçları · 15 Ekim 2026

Kullanıcı fiziksel TV'de arama yaparken sonuçları göremediğini, ardından bölüm içi aramada da aynı sorunu bildirdi. TV klavyesi ekranın alt yaklaşık %43'ünü kaplıyor. Ara ve "Bu bölümde ara" artık klavye açıkken yazma kipine geçiyor: başlık, filtre ve kategori/tür satırları çekiliyor, alan menünün altına çıkıyor, sonuç sayısı alan etiketinde, bölüm sonuçları Ara'daki satır kartıyla gösteriliyor. Klavye Geri ile kapanınca veya odak alandan ayrılınca tam düzen dönüyor. İlk sürümde bölüm aramasında yazmaya başlamak uygulamayı kapatıyordu (ClassCastException); neden giderildi. [Sözleşme](../design/SEARCH_TYPING_27.md), [QA](native-search-typing-27-qa.json), [görseller](../../design/reviews/LATEST.md), karar D054.

150 JVM (1 atlanan) başarılı; lint 0 hata/32 uyarı. Normal yazıda SearchNavigation 13/13, MultiSource 7/7, PilotFlow 8/8, GuideFlow 11/11, VisualPreview 10/11 (kalan bilinen ortam vakası, 0.14.25/26'da da aynı). 2.0 yazıda SearchNavigation 11/13 (iki vaka 0.14.25/26'da da aynı biçimde, yeni koda ulaşmadan duruyor); bölüm araması 2.0'da elle kontrol edildi. 0.14.27/code41 APK releases içinde; SHA-256 6933dfe8f51cdd6b9f612547171f14ce884a592bbb77fddc7945664e591665da. 5556 güncel, yazı 1.0. Ardından kullanıcının isteğiyle fiziksel TV (TCL Smart TV Pro) `adb install -r` ile 0.14.26'dan 0.14.27/code41'e güncellendi: TV'deki APK SHA-256'sı eşleşti, ilk kurulum zamanı (13 Ekim) değişmedi, veri korundu; uygulama açılmadı, test yapılmadı. TV'de arama görünümü ve kullanıcı kabulü açık. Figma değişmedi.

## 0.14.26 · UI bozukluk analizi ve onarımı · 15 Ekim 2026

Kullanıcının "projeyi UI olarak analiz et ve bozuklukları düzelt" isteği tamamlandı. 0.14.25'in 137 güncel emülatör görüntüsü ve ekran kodu 11 ekran grubunda tarandı; her bulgu iki bağımsız doğrulayıcıyla piksel ve kod üzerinden kontrol edildi: 56 doğrulandı, 4 ayrıştı, 1 reddedildi. Native tarafta 46 bulgu düzeltildi: kaydırma kenarlarında yarım metin satırı yerine solma/tam satır, seçim çizgisinin konumu/rengi ve liste satırlarında ✓, devre dışı eylemlerin odak almaması, üst üste pencerenin gizlenmesi, kurulum hata alanı, oynatıcı yükleme/rozet/parça satırı, rehber tam satır ve şimdi çizgisi, Ayarlar/Arama/Kaynaklar/Ana sayfa hizaları. Prototipte 8 bulgu düzeltildi. [Sözleşme](../design/UI_REPAIR_26.md), [önce/sonra](../../design/reviews/LATEST.md), [QA](native-ui-repair-26-qa.json), karar D053.

150 JVM (1 atlanan) başarılı; lint 0 hata/32 uyarı. Normal yazıda 16 sınıf/98 vakadan 95'i geçti; kalan üçü ortam/kararsızlık (ikisi 0.14.25 sürümünde de aynı başarısız, biri tekrarda geçti). 2.0 yazıda 6 sınıf/34 vakadan 31'i geçti; kalanlar 0.14.25'te de aynı. İlk turda yakalanan "uncancelled requests" çökmesi (görünürlük payı liste boşluğundan büyük) giderildi. Prototip 13/13. 0.14.26/code40 APK releases içinde; SHA-256 69ff70d499384f6c3ef1914907624f910b3a99c1124f4644bce1b86c5a7c17a8. 5556 güncellendi; ardından kullanıcının isteğiyle fiziksel TV (TCL/iFFALCON, 192.168.1.126) veri silinmeden 0.14.22'den 0.14.26/code40'a kuruldu, kayıtlı tercihler duruyor. Uygulama TV'de açılmadı, test çalıştırılmadı; kullanıcı AVD'si değişmedi. Figma eşitlenmedi; BKL-020 kullanıcı/Figma/fiziksel kabul nedeniyle in progress.

## 0.14.25 · Proje testi ve kontrol yüzeyleri · 15 Ekim 2026

Kullanıcının test ve kozmetik düzenleme isteği tamamlandı. Ortak seçim düğmelerine genişliği değiştirmeyen alt çizgi ve erişilebilir seçim bilgisi eklendi; kaynak formu alanları koyu yüzey ve daha belirgin çerçeveyle ayrıldı. Arama türleri/Temizle, katalogdaki hafif yardımcı kontrol görünümüne uyarlandı. [Tasarım](../design/CONTROL_POLISH_25.md), [önce/sonra](../../design/reviews/LATEST.md), [QA](native-polish-25-qa.json).

149 JVM testi başarılı, 1 özel denetim önceden tanımlı olarak atlandı. Normal yazıda 135 farklı emülatör kontrolünün son sonucu başarılı; 1.5 ve 2.0 yazıda 10'ar ek kontrol geçti. Tarayıcı prototipinin 13 kontrolü Chrome ile, kare çözümleyicisinin 9 sentetik testi ve proje bağlantı kontrolü başarılı. Derleme/Android test APK'si başarılı; lint 0 hata/32 uyarı. Önceki yarım normal-font raf kontrolü de 0.14.24 başlangıç kontrolünde ve yeni sürümde geçti.

Host GPU turu 72 başarılı vakadan sonra emülatör kapanmasıyla kesildi; 73. vaka başarılı sayılmadı. Günlükte ColorBuffer hataları vardı, kesin neden kanıtlanmadı. Aynı ayrılmış AVD veri silmeden SwiftShader ile açıldı; kalan 62 vaka geçti. Eski görsel yakalama testinin başlık/raf varsayımı güncel davranışa uyarlandı ve geçti. Bu, fiziksel akıcılık sonucu değildir.

0.14.25/code39 APK ve SHA-256 releases içinde; APK v2 imzası doğrulandı. Test cihazı 5556 güncel, yazı ölçeği 1.0. Fiziksel TV'nin son bilinen sürümü 0.14.22; bu tur değiştirilmedi. Figma eşitlenmedi. BKL-020 kullanıcı/Figma/fiziksel kabulü nedeniyle in progress kalır; native PIN ayrı açık iştir. Sonraki adım 0.14.25 görsel incelemesini kullanıcı geri bildirimiyle değerlendirmek.

## Etkileşimli emülatör kurtarma · 15 Ekim 2026

Kullanıcı donma ve kontrol edememe bildirdi. İncelemede emülatör süreci artık yoktu; önceki logda Qt UpdateLayeredWindowIndirect hataları ve kapanış vardı. Kesin kök neden kanıtlanmadı. Aynı ayrılmış AVD veri silmeden host GPU, Vulkan kapalı, no-skin ve hw.keyboard=yes ile yeniden açıldı. NVIDIA RTX 4050 renderer, boot tamamlanması, MainActivity ön planı ve Windows Responding=True doğrulandı. Ekran görüntüsü kaynak ekleme formunu gösteriyor; Back ve yön komutları gönderildi. Kullanıcının TeamViewer üzerinden tuş iletimi ve uzun süreli kararlılık henüz doğrulanmadı. Qt pencere uyarısı yeni logda da var; tamamen düzeldi denmemeli. Üretim kodu/APK değişmedi, TV ve kullanıcı AVD'sine dokunulmadı. Yerel config yedeği ve loglar .private altında. Sonraki adım kullanıcının görünür pencerede etkileşimini kontrol etmek.

## Görünür emülatör açıldı · 15 Ekim 2026

Kullanıcının “emülatörü aç” isteğiyle yalnızca nextv_design_api36 / emulator-5556 kimliği doğrulanan askıdaki headless süreçler durduruldu; aynı AVD veri silmeden görünür pencerede yeniden başlatıldı. Android boot tamamlandı, font_scale=1.0 ayarlandı ve dev.iptvplayer.tv.MainActivity ön planda doğrulandı. Görünür pencere: Android Emulator - nextv_design_api36:5556. Kullanıcı emülatörü ve fiziksel TV değiştirilmedi.

Eski session38142 normal-font testi kurtarma sırasında kesildi (adb exit255); başarılı test sayılmaz. Session72702 tamamlandı. Yeni test başlatılmadı. Fixture temizliği doğrulanmadı; test verileri korunmuş olabilir. Son APK 0.14.24, önceki 1.5/2.0 test sonuçları korunur. Önceki ortam engeli notu tarihsel durumdur; kapsamlı görsel/performans kabulü hâlâ açık.

## Doğrulama ortamı engeli · 15 Ekim 2026

Önceki tur çevrimdışı kare çözümleyicisiyle somut ilerleme sağladı. Bu tur canlı test session38142 ve salt okunur session72702 tekrar yoklandı; ikisi de terminal sonuç vermedi. PID35776 mevcut, 3709 thread hâlâ Suspended. Bu aynı engelin ilk hızlı-yön turu, analiz-aracı turu ve mevcut tur olmak üzere üçüncü ardışık doğrulamasıdır.

Ek host kanıtı: emülatör PrivateMemorySize64 yaklaşık 41.204 MiB, working set yaklaşık 1.660 MiB. Bunlar Windows emülatör süreci ölçüleridir; Android uygulamasının heap’i veya fiziksel TV belleği değildir. Askı sebebi kanıtlanmadı. Normal-font sonucu hâlâ yok; 1.5/2.0 geçişleri korunur. Süreç/oturumlar öldürülmedi veya yeniden başlatılmadı.

Mevcut görsel öncelikte anlamlı sonraki çalışma gerçek UI/akıcılık doğrulaması gerektiriyor. Çevrimdışı hazırlık tamam; aynı testleri/dokümanları tekrarlamak veya kullanıcı geri bildirimi olmadan rastgele stil değiştirmek yeni ilerleme sayılmayacak. Kullanıcıya daha önce gönderilen emülatörü bilerek askıya alıp almadığı sorusu yanıtsız. Çalışma bu dış durum açıklığa kavuşana kadar engelli olarak işaretlenecek; tamamlanmış sayılmaz.

Devam ederken: önce kullanıcı yanıtını ve aynı PID/oturumların güncel durumunu kontrol et. Gözlem zaman aşımı tek başına testin bittiği anlamına gelmez. Kurtarma gerekiyorsa ayrılmış nextv_design_api36/5556 kimliğini yeniden doğrula; kullanıcı cihazlarını/TV’yi değiştirme. Bekleyen koşunun sonucu ve fixture/font temizliği kaydedilmeden yeni koşu başlatma. Son teslim 0.14.24; fiziksel TV son bilinen 0.14.22. Figma, kullanıcı görsel onayı, fiziksel performans/medya ve native PIN kapsamı açık.

## Arayüz kare süreleri için çevrimdışı tanı aracı · 15 Ekim 2026

Önceki tur test kimlik kontrolü ve iki yeni sonuçla ilerlemeydi; normal-font testi hâlâ canlı bekliyor. Session38142 bu tur tekrar yoklandı, terminal sonuç yok; PID35776 hâlâ mevcut ve 3709 thread Suspended. Kullanıcının askıya almanın kasıtlı olup olmadığı yanıtı henüz gelmedi. Yeniden başlatma/askıdan çıkarma yapılmadı; logcat session72702 de devralma kaydında kalır.

Bağımsız olarak tools/analyze_framestats.py ve dokuz sentetik test eklendi. Gerçek bir kaydı sütun adına göre çözer; Flags0 sürelerini, dışlamaları ve pencere bloklarını ayrı raporlar. Eşik yalnız açık bütçeyle; boş veri null/başarısız CLI, kesilmiş/uyumsuz blok hata. Ham pencere adı/yol rapora taşınmaz, mevcut çıktı dosyası üzerine yazılmaz. [Sözleşme/kaynaklar](../architecture/FRAME_DIAGNOSTICS.md), [QA](frame-diagnostics-tool-qa.json).

Dokuz test geçti. Bu uygulama performans sonucu değildir: cihazdan alınmış yeni kare sayısı 0. Üretim kodu/APK değişmedi; 0.14.24 korunur. Fiziksel TV/Figma değişmedi, uygulama testleri yeniden başlatılmadı.

Tam sonraki adım: aynı canlı test oturumunu ve askı durumunu kontrol et; kullanıcı yanıtı olmadan gözlem zaman aşımını süreç sonlanması kabul etme. Test tamamlanınca fixture temizliği ve font1.0 doğrulanmalı. Ardından kontrollü kısa gerçek raf gezinmesinden kare kaydı alınabilir; uzun senaryo için son-kare tamponu yeterli sayılmaz. Aynı sentetik testleri tekrar etmek veya yeni ölçüm yokken performans kararı vermek ilerleme değildir. Görsel kabul ve fiziksel gereksinimler açık; genel hedef tamamlanmadı.

## 0.14.24 · Hızlı yön değişiminde hedef kimliği · 15 Ekim 2026

Önceki raf yerleşimi turu somut ilerlemeydi. VisualPreviewTest hızlı sağ/sol kısmı güçlendirildi: sağında komşusu bulunan belirli filmden başlar; sekiz zıt tuş çiftinin her birinden sonra aynı içerik kimliğini, ardından OK ile aynı detayın açıldığını ve Geri ile aynı karta dönüldüğünü doğrular. Önceki yalnız aynı rafta kalma kontrolünden daha güçlüdür. Test APK derlendi; üretim kodu/APK değişmedi.

1.5 ve 2.0 yazıda yeni kontrol geçti. Kurulu 5556 base.apk SHA-256 yayımlanmış 0.14.24 ile eşleşti. Normal 1.0 testi sonuçlanmadı: emulator host süreci canlı, gözlenen 3709 thread Wait/Suspended; adb devices/get-state cihazı görüyor, guest date/pidof ve emu avd name süre aşımına uğradı. Bu bir crash veya terminal test sonucu değildir. [QA](native-shelf-reversal-qa.json).

**Canlı iş devri:** unified exec test session 38142, bekleyen salt okunur logcat session 72702. Emülatör PID 35776. Tam yerel kayıt .private/shelf-reversal-pending.json. Süre aşımından yeniden başlatma çıkarımı yapma; aynı oturumları yokla. Kullanıcıya askıya almayı bilerek yapıp yapmadığı async soruldu; yanıt bekleniyor. Süreç sonlandırılmadı/yeniden başlatılmadı/askıdan çıkarılmadı. Son testin fixture temizliği ve font okuması henüz doğrulanamadı.

Genel hedef açık; önceki üretim teslimi 0.14.24 değişmedi, fiziksel TV/Figma değişmedi. Sonraki adım: kullanıcı yanıtı ve mevcut canlı süreç durumuna göre bu testi sonuçlandır; ardından 5556 font1.0/fixture temizliğini doğrula. Normal test tamamlanmadan 3/3 yazma. Bu sonuçlar girişten piksele gecikmeyi veya bütün tekil girdilerin kayıpsızlığını kanıtlamaz; gerçek akıcılık ölçümü ayrıca açık.

## 0.14.24 · Ana sayfa raflarını birlikte kadraja alma · 15 Ekim 2026

Önceki tur somut görsel ilerlemeydi. Bu tur HomeScreen ve CardVisibility, rafın ölçülmüş başlığını ve kart/alt bilgilerini tek görünürlük isteğiyle hizalar. Sığan rafta başlık üst kenardan 7–9 dp; sığmayan bağlamda kart öncelikli. Son rafın yüksekliğine göre gerekli alt pay hesaplanır; ölçüler raf kimliğiyle tutulur. Böylece büyük yazıdaki önceki raf yazısı kalıntısı incelenen ekranlarda giderildi. [Sözleşme](../design/HOME_SHELF_FRAMING.md).

Yeni gerçek kumanda testi canlı→film→dizi→film→canlı döngüsünü üç tur, ardından sekiz sağ/sol çiftini dener. Başlık ve bütün kart sınırları ölçülür. Önceki afiş/Devam et testine başlık üst hizası da eklendi. Final APK üzerinde normal/1.5/2.0 yazıda üçer akış; 2.0 yazıda yenileme ve uzun kaynak/bölüm/Devam et akışları: 11/11 geçti. Lint 0 hata/32 uyarı, imza doğrulandı; [QA](native-shelf-24-qa.json). Bu görsel değişiklikte JVM paketi yeniden çalıştırılmadı.

Teslim 0.14.24/code38; SHA-256 eb093112d5757ac9abd43f576851b6c565abe422fc8c2e8f5e9dba736615540a. [İnceleme](../../design/reviews/LATEST.md) önce/sonra rafları içerir. Yalnız ayrılmış 5556 değiştirildi; test kaynakları temizlendi, yazı 1.0. Fiziksel TV tekrar yoklanmadı ve son bilinen 0.14.22 sürümünde; Figma değişmedi. Genel hedef, kullanıcı kabulü, fiziksel performans/medya ve native PIN entegrasyonu nedeniyle açık.

Sonraki iş: kullanıcı görsel geri bildirimi öncelikli. Raf davranışının farklı kaynak sayısı ve gerçekten sığmayan metin bağlamındaki sınırları ayrıca değerlendirilebilir; geçen aynı testleri sebepsiz tekrarlama. Yeni sürüm için fiziksel görünüm/akıcılık kabulü henüz yok. Yeni özellikleri otomatik olarak görsel önceliğin önüne koyma.

## 0.14.23 · Görsel hiyerarşi ve raf düzeni · 15 Ekim 2026

Kullanıcının yeni arayüz iyileştirme isteği önceliklendirildi; SAFE01 devamına geçilmedi. Ana vitrinde daha dar açıklama alanı, 44/50 sp normal başlık ve tür/yıl satırı; daha hafif yardımcı eylemler; katalog başlığı 26/32 sp. Ana sayfa film/dizi raflarında iki satırlık sabit başlık, hizalı yıl alanı ve eksik afişte yinelenmeyen başlık. 24 dp dış kenar ve Google TV yönü korunur. Üst kaydırma sınırında 7 dp geçiş önceki raftan kalan ince yazı parçalarını yumuşatır; tam raf snapping değildir. [Sözleşme](../design/VISUAL_POLISH_23.md).

İlk test grubu programatik kaydırma/odak görünürlüğü çekişmesi sırasında durduruldu; başarı sayılmadı. Testte programatik raf kaydırmasından önce menüye odak verildi; kumanda ve geri dönüş doğrulamaları korunur. Normal/2.0 yazıda üçer ilgili akış geçti. Son kenar düzenlemesinden sonra ana gezinme ve afiş/devam et akışları iki fontta yeniden 4/4 geçti. Derleme başarılı, lint 0 hata/32 uyarı; [QA](native-visual-23-qa.json). JVM iş mantığı testleri bu görsel değişiklikte tekrarlanmadı.

0.14.23/code37 APK releases içinde. SHA-256: 8a088ae7634e64bb84ef3ea556415e5017ef607bec399aad245985b8cb0d6256. Yalnız doğrulanmış tasarım emülatörü 5556 güncellendi; fixture temizliği tamamlandı, yazı 1.0. Fiziksel TV uyandırılmadı/güncellenmedi, son bilinen sürüm 0.14.22. Figma değişmedi; PIN henüz kullanıcıya açık değil.

[Önce/sonra incelemesi](../../design/reviews/LATEST.md) güncellendi. Genel görsel kabul ve fiziksel test açık. Sonraki iş: bu sürümü kullanıcıya göster; gelecek ekran bazlı geri bildirimi önceliklendir. Daha kapsamlı raf kaydırma davranışı değerlendirilecekse üst sınır, bütün kart görünürlüğü ve büyük yazı birlikte ele alınmalı. Görsel önceliği otomatik olarak yeni özelliklerle değiştirme.

## SAFE01 · Şifreli koruma deposu · 15 Ekim 2026

Önceki deneme bütçesi somut ilerlemeydi. ProtectionSnapshot/ProtectionCodec/ProtectionStore eklendi. PIN doğrulayıcı, deneme durumu ve typed kurallar tek sürümlü kayıtta; source anahtarından bağımsız Keystore AES-GCM, namespace AAD, AtomicFile + descriptor sync + readback kullanılır. Anahtar varsa eksik dosya, dosya varsa eksik anahtar Unavailable; yalnız ikisi de yoksa Unconfigured. Otomatik reset API'si yok. [Sözleşme](../architecture/PROTECTION_STORE.md).

Nesiller beklenen generation ile karşılaştırılıp artırılır; kural/PIN değişimi policyRevision artışı gerektirir. Giriş boyutu 1 MiB/1024 kural/metin başına 4096 karakter. Kapsam çoğaltma, bilinmeyen tür/sürüm, kesilmiş/fazla kayıt reddedilir. Çağıran bütün yetki denetimini ve seri doğrulamayı ayrıca yapmalıdır.

Dört yeni JVM testi; toplam 150 kayıtta 149 başarılı/1 önceden atlanan. Lint 0 hata/32 uyarı. Gerçek Android Keystore üzerinde ProtectionStoreTest 4/4: roundtrip/doğru PIN, yeni store örneği, eski yazıcı, anahtar/dosya kaybı, değiştirilen/başka depodan ciphertext, yazma hatasında önceki kayıt ve politika revizyonu. [QA](native-protection-store-qa.json). Özel loglar protection-store-build.log/device.log. Gerçek süreç ölümü/güç kesintisi veya fiziksel TV kanıtı değildir.

Yalnız 5556 test namespace'leri kullanıldı ve test temizliği tamamlandı. UI/AccessPolicy/ViewModel henüz çağırmıyor; Ayarlar PIN yok açıklaması korunur. Yeni yayımlanmış APK yok; TV 0.14.22 değişmedi. Test sonrası 5556 yayımlanmış 0.14.22 APK'sine geri döndürüldü. Genel hedef açık.

Tam sonraki iş: Android boot/elapsed saat adaptörü ve seri PIN kontrolcüsü. KDF öncesi reservation.next kalıcılaştırılmalı; başarılı KDF sonrası temizleme de yazılmadan izin verilmemeli. İptal/arka plan veya politika değişiminde eski yanıt grant üretmemeli; immutable snapshot generation üzerinden CAS kullan. Kaynak kurtarma koruma anahtarını silmez; kesilmiş ilk kurulum ve unutulan PIN için kullanıcı akışı hâlâ tasarlanmalı. En son canonical hedef ve bütün arama/favori/geçmiş/oynatma kapıları bağlanacak. Kullanıcı görsel geri bildirimi öncelikli, fiziksel ekran doğrulaması bekliyor.

## SAFE01 · PIN deneme bütçesi · 15 Ekim 2026

Önceki PIN doğrulayıcı somut ilerlemeydi. PinAttempts.kt eklendi: doğrulama öncesi kalıcılaştırılacak rezervasyon, beşinci denemeden sonra 30/60/120/240/300 saniyelik bekleme, 9'da doyan sayaç. Aynı boot'ta deadline korunur; boot değişimi veya monoton geri gidişte mevcut tam gecikme yeniden başlar. Duvar saati girdi değildir. Sonuç yalnız KDF çalıştırma iznidir; içerik yetkisi değildir. [Sözleşme](../architecture/PIN_ATTEMPTS.md).

Sekiz yeni test geçti. Tam JVM raporu: 146 test kaydı, 145 başarılı, 1 önceden atlanan, 0 hata/başarısızlık. Lint 0 hata/32 uyarı. [QA](native-pin-attempts-qa.json), .private/pin-attempts-check.log. Süreç dönüşü/reboot testleri saf kayıt yeniden oluşturma/zaman girdileridir; gerçek süreç ölümü veya diske kalıcılık iddiası değildir.

Henüz repository/saat adaptörü/UI bağlı değil. next durumu kaydedilmeden mayVerify kullanılmamalı; paralel çağrılar seri hale getirilmeli; başarılı PIN sonrası temizleme yazısı başarısızsa izin üretilmemeli. Ayarlarda PIN yok açıklaması korunur. Yeni APK oluşturulmadı; TV/5556 kurulumu 0.14.22, fiziksel TV tekrar yoklanmadı. Genel hedef açık.

Tam sonraki iş: ayrı koruma anahtarı ve atomik PIN/politika/deneme kaydı oluştur. DeviceStore.resetUnreadableSources mevcut kaynak anahtarını siliyor; koruma deposunu o anahtara bağlamak kaynak kurtarmasıyla koruma kaybına yol açabilir. Missing-after-configured/bozuk kayıtta kilitli hata durumu, belirgin kurtarma sınırı ve yazılamama davranışı şart. Ardından Android boot/elapsed adaptörü, seri doğrulama kontrolcüsü ve canonical hedef/revizyon kontrolü. Kullanıcı görsel geri bildirimi gelirse öncelik ver; aynı geçen testleri tekrarlama.

## SAFE01 · PIN doğrulayıcı ilkel · 15 Ekim 2026

Önceki fiziksel açılış kontrolü gerçek ekran-uyku engelini gösterdi; TV tekrar yoklanmadı/uyandırılmadı. Bağımsız ürün kapısı SAFE01 için PinCredential.kt eklendi: PBKDF2-HMAC-SHA256/600.000, rastgele 16 bayt tuz, 32 bayt çıktı, sürümlü sabit 52 bayt kayıt. Verilen CharArray işlem sonunda sıfırlanır, PBEKeySpec ve geçici karşılaştırma çıktısı temizlenir. MessageDigest.isEqual, savunmacı kopyalar ve redacted toString kullanılır. Bozuk kayıt hata, geçersiz giriş ret verir. [Sözleşme ve kaynaklar](../architecture/PIN_CREDENTIAL.md).

Dört yeni JVM testi geçti; tüm pakette 138 test kaydı, 137 başarılı ve önceden atlanan 1 test, 0 hata/başarısızlık. Bağımsız Python hashlib referansı aynı çıktı verdi. Lint 0 hata/32 uyarı. [QA](native-pin-credential-qa.json), özel build kaydı .private/pin-credential-check.log. Android sağlayıcı/gecikme testi yapılmadı. Önceki tarihli toplam test ifadelerindeki atlanan test ayrımı bu turun gerçek raporunda açıkça gösterilir.

Bu ilkel tek başına güvenlik özelliği değildir: TvViewModel/UI/AccessPolicy çağırmıyor. Disk kaydı, kalıcı deneme sınırı, kurtarma ve izin kapsamı yok; Ayarlar'daki PIN henüz yok açıklaması korunur. Üretim APK yayımlanmadı, cihazlar değişmedi; son teslim/TV 0.14.22. Genel hedef açık.

Tam sonraki iş: PIN doğrulayıcı/politika/deneme sayacını tek atomik, kimliği doğrulanmış şifreli kayıt içinde tasarla. Var olan kaynak anahtarını silen kurtarma ekleme. Kalıcı yanlış deneme gecikmesi için reboot, wall-clock ileri/geri ve yazma hatasını ele al; doğrulama ana thread dışında tek iş olarak çalışmalı, kayıt yazılamıyorsa izin üretmemeli. Bozuk/missing-after-configured kayıt korumayı sessizce kapatmamalı. Ardından canonical hedef/politika revizyonu ve tüm erişim girişleri. Kullanıcı görsel geri bildirimi gelirse öncelikle uygula; fiziksel TV ekranı için kullanıcı gözlemi bekleniyor.

## 0.14.22 · Fiziksel açılış kontrolü, ekran uyuyor · 15 Ekim 2026

Önceki TV kurulumu somut ilerlemeydi. Fiziksel kimlik doğrulandı, önceki ön plan Google TV launcher idi. Güncellenmiş MainActivity açıkça başlatıldı: Status ok, WaitTime 3021 ms. Bu değer görünür ana sayfa veya ilk kare süresi değildir. Sonraki kontrolde aynı uygulama süreci yaşıyordu; o PID için crash tamponunda FATAL EXCEPTION işareti görülmedi.

UI dump iki denemede null root döndürdü. Power/window durumu mWakefulness=Asleep, mAwake=false, mScreenOnFully=false: TV ekranı uyuyor. Bu nedenle ana sayfa/katalog gösterimi doğrulanamadı. Cihaz uyandırılmadı, yayın başlatılmadı. Şifreli katalog klasöründe bir .bin dosyası ve boş olmayan device_state.xml var; içerikleri/sırlar okunmadı. Bu dosya varlığı favorilerin/katalog içeriğinin semantik doğruluğunu kanıtlamaz. [Fiziksel QA](physical-tv-22-opening-qa.json).

Üretim kodu/APK ve Figma değişmedi. TV 0.14.22 kurulu; uygulama süreci açıldı fakat ekran uyuyor. Genel hedef açık. Kullanıcıya kütüphanenin kesin korunduğu veya fiziksel medya testlerinin geçtiği söylenmemeli.

Tam sonraki iş: TV kullanıcı tarafından açıkken görünür açılış/katalog ve tasarım kabulü gerekir; sırf bu kapı için cihazı tekrar tekrar yoklama veya uzaktan uyandırma. Bağımsız eksik iş olarak SAFE01 politika/PIN saklama henüz uygulanmadı; mevcut AccessPolicy ve PARENTAL_ACCESS sözleşmesi üzerinden devam edilebilir. Kullanıcının son tasarım önceliği ve yeni görsel geri bildirimi varsa korunur. Önceki geçen görsel kontrolleri tekrar etmek yeni ilerleme sayılmaz.

## 0.14.22 · Fiziksel TV güncellendi ve görsel inceleme dosyası · 15 Ekim 2026

Önceki karışık bölüm verisi testi somut ilerlemeydi. Bağlı cihaz envanterinde fiziksel TV bulundu; model/önceden bilinen seri kimliği doğrulandı. Paket sorgusunda 0.14.4/code18 vardı. Ön planda Google TV launcher görünüyordu. Kullanıcının daha önceki TV güncelleme isteği kapsamında doğrulanmış 0.14.22 APK install -r ile mevcut kurulum üzerine yüklendi: Success; son paket sorgusu versionCode36/versionName0.14.22-design-preview.

Uygulama kaldırılmadı, veri temizleme veya test fixture kurulumu yapılmadı. Güncelleme veri koruyan yöntemle yapıldı; kullanıcı katalog/favorilerinin açılışta korunduğu bu tur uygulama içinden ayrıca gözlemlenmedi. Uygulama başlatılmadı ve gerçek yayın oynatılmadı. Fiziksel medya/performans kabulü değildir. 5556 sürümü de 0.14.22; 5554 bu kontrolde bağlı değildi.

[Karşılaştırmalı görsel inceleme](../../design/reviews/LATEST.md) ana sayfa, bölüm kartları, odak çerçevesi ve eksik kapak örneklerini tek dosyada toplar. Her görüntünün sürümü/yazı ölçeği ve emülatör kaynağı belirtilir. Yeni APK üretilmedi; SHA-256 f0a37653bc7a50bb792c2b1c2a78a563574e47b0723d00399c4a0f32e3732660. Figma değişmedi.

Tam sonraki iş: kullanıcıdan TV'deki yeni sürümün görsel geri bildirimi ve kayıtlı kütüphanenin açılışı için gözlem bekleniyor. Genel hedefi tamamlandı sayma. Bağımsız ilerleme için native PIN/politika saklama ve tüm giriş noktaları hâlâ eksik; bu eksiklik somut tamamlanma kapısıdır. Geçen görsel testleri veya kullanıcı geri bildirimini ikame eden yeni rastgele stil değişikliklerini tekrar etme. Fiziksel cihazda izinsiz test/reset/otomatik yayın başlatma yok.

## 0.14.22 · Karışık bölüm verisi doğrulaması · 15 Ekim 2026

Önceki kart düzeni somut ilerlemeydi; kalan eksik kapak/uzun ad/Devam et durumları için VisualPreviewTest'e mixedEpisodeArtworkTitlesAndResumeKeepAlignedCaptions eklendi. Yerel fixture ilk bölümde uzun ad ve çözülemeyen görsel, ikinci bölümde kısa ad, üçüncüde 45/120 saniye kayıt içerir. Gerçek sağ tuşlarıyla üç kart, tam kart görünürlüğü, metadata üst hizası, başlık tekrarı ve izleme konumu doğrulanır.

Normal/1.5/2.0 yazıda 3/3 geçti. Büyük yazı görüntüsü incelendi: eksik kare simgesi, iki satırlı kısaltma, hizalı sezon bilgileri ve Devam et etiketi görünür. [QA](native-episode-mixed-qa.json), [görsel](../../design/reviews/episodes-0.14.22/font-2.0/series-mixed-episodes.png). Üretim kodu/APK değişmedi; yalnız test APK derlendi. JVM/lint tekrarlanmadı. Son teslim 0.14.22 SHA-256 değişmedi. 5556 font1.0, fixture temiz; TV/5554 korunur.

Bu üç kontrol her sağlayıcı başlığını veya tüm sayı uzunluklarını kapsamaz. Genel hedef, fiziksel medya/performans, kullanıcı görsel kabulü, Figma ve native PIN gibi gereksinimler nedeniyle açık.

Tam sonraki iş: son birkaç görsel teslimin üretim sürümü artık 0.14.22. Kullanıcıya yeni sürümü gerçekten inceletecek test ortamının mevcut durumunu belirle; ayrılmış 5556 fixture temizliği sonrası kurulum ekranındadır, gerçek kullanıcı 5554/TV'yi izinsiz sıfırlama. Kullanıcının bekleyen görsel geri bildirimi gelirse uygula. Figma kota/cihaz kapılarını yalnız tarihli notla tamamlandı varsayma. Aynı görsel testleri sebepsiz tekrarlama; kalan ürün kabul kapılarına dön.

## 0.14.22 · Bölüm kartlarının bilgi hiyerarşisi · 15 Ekim 2026

Önceki odak çerçevesi düzeltmesi somut ilerlemeydi. Detay ekranı ve eski görüntülerde tek satırlı bölüm başlıklarının 2.0 yazıda bölüm adını gizlediği görüldü. EpisodeCard başlığı 12/17 sp iki sabit satır, metadata 10/14 sp iki sabit satır; yatay iç pay 10 dp. Sabit Container/odakta High yüzeyi arka plan fotoğrafından bağımsız okunabilirlik sağlar. Kapaksız görselin yinelenen başlığı kaldırıldı. Kimlik/oynatma/üç sütun/2:1 görsel değişmedi. [Tasarım](../design/EPISODE_CARDS.md).

Üç yazı ölçeğinde detay açıklaması/okuyucu/Geri ve kart görünürlüğü 3/3; normal yazıda sonraki sezon oynatma ve aynı karta dönüş 1/1 geçti. Normal ve 2.0 bölüm görüntüleri incelendi; fixture bölüm adları büyük yazıda artık görünür. Derleme başarılı, lint 0 hata/32 uyarı, imza doğrulandı. [QA](native-episode-cards-22-qa.json). JVM bu sunum değişikliğinde tekrarlanmadı. Devam et satırının sarılması ve kapaksız kart bu dört senaryoda ayrı örneklenmedi; tüm içeriklerde kusursuzluk iddiası yok.

[0.14.22 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.22.apk), SHA-256 `f0a37653bc7a50bb792c2b1c2a78a563574e47b0723d00399c4a0f32e3732660`. Yalnız 5556, font 1.0'a döndü. TV/5554/Figma değişmedi. Kullanıcı görsel kabulü ve genel hedefin fiziksel/ürün kapıları açık.

Sonraki iş: daha uzun bölüm adları, eksik kare ve kısmen izlenmiş bölümün aynı sırada olduğu hedefli görsel senaryoyu oluştur; yeni düzenin eksik veri ve Devam et satırını doğrula. Mevcut testleri değişiklik olmadan tekrar etme. Kullanıcının görsel yön geri bildirimi gelirse öncelikle uygula. SAFE01 entegrasyonu tamamlandı sayılmaz; yeni özellikler görsel önceliği geçmemeli.

## 0.14.21 · Ses/altyazı odak çerçevesi · 15 Ekim 2026

Önceki görsel tur somut ilerlemeydi. Ortak kenar değişiminden sonra oynatıcı ve parça paneli normal/2.0 yazıda 4/4 kontrolü geçti; ancak görüntü incelemesi son altyazı seçeneğinin dış odak çizgisinin kesildiğini gösterdi. Eski test yalnız satır kutusunu ölçüyordu. PlayerScreen TracksDialog içine 8 dp güvenli paylı BringIntoViewSpec eklendi; görsel test en az 7 dp payı denetler. Üst başlık, seçim ve Geri davranışı korunur.

Güçlendirilmiş test eski 0.14.20 üzerinde başarısız: son satır ve viewport altı 443.5 dp. Yeni sürümde 2.0 yazı parça seçimi + 3 metadata/uzun açıklama testi 4/4, 1.5 ve 1.0 yazı seçim testleri 1/1'er geçti. Son görselde çerçeve tamamen görünür. [QA](native-track-focus-21-qa.json), [tasarım](../design/TRACKS_PANEL.md). Derleme başarılı, lint 0 hata/32 uyarı, imza doğrulandı. JVM bu sunum değişikliğinde tekrarlanmadı.

[0.14.21 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.21.apk), SHA-256 `195561e5eaeb4c29c92a6a6f3773525664337a54b27e7ebce361fdb72def12da`. Yalnız 5556, font tekrar 1.0. TV/5554 ve Figma değişmedi. Genel hedef tamamlanmadı; kullanıcı görsel kabulü, fiziksel cihaz ve SAFE01 native entegrasyon gibi kapılar açık.

Sonraki iş: kullanıcının genel görünüm/kart yoğunluğu/hizalama hakkındaki bekleyen geri bildirimini varsa uygula. Son ana sayfa yönü ile tutarlı detay/sezon ekranında uzun başlık ve bölüm bilgisi hiyerarşisini güncel görüntü ve kodla incele; yalnız somut görsel sorun varsa düzelt. Aynı geçen testleri yeni gerekçe olmadan tekrarlama. Yeni özellikler görsel önceliğin önüne geçmemeli.

## 0.14.20 · Geniş içerik ve görsel vitrin · 15 Ekim 2026

Kullanıcının yeni görsel kalite isteği BKL-020 önceliğine alındı. Ortak kenar 32→24 dp; menü üst/alt payı 27/17→18/14 dp. Ana sayfa vitrini minimum 264 dp, yatay görsel %78, nötr çift yönlü karartma, normal başlık 40/46 sp. Üst etiket/kaynak ve alt başlık/eylemler ayrıldı. Büyük yazı ve eksik afiş davranışı korunur. [Tasarım sözleşmesi](../design/VISUAL_POLISH_20.md).

Görsel incelemede 2.0 yazılı katalog başlığının son birkaç pikselinin üstte kaldığı görüldü. PilotApp kaydırma gözlemcisi beforeContentPadding değerini hem görünürlük hem durma hesabına katar. Yenileme/başlığa geri dönme testi bu düzeltmeyle geçti. Ana kaynak değişiklikleri DesignSystem.kt, HomeScreen.kt, PilotApp.kt ve sürüm dosyasında.

Adayda normal yazı gezinme + uzun/kapaksız başlık 2/2; 2.0 yazı gezinme + afiş/ayırıcı + uzun başlık 3/3; 1.5 yazı uzun başlık 1/1 geçti. Son kaydırma düzeltmesinin kontrol kaydı native-visual-polish-20-qa.json içinde. Derleme başarılı, lint 0 hata/32 uyarı. JVM mantığı değiştirilmedi; bu tur JVM paketi tekrarlanmadı. APK imzası doğrulandı. Yalnız ayrılmış 5556 kullanıldı; TV ve 5554 değişmedi. Figma değişmedi.

[0.14.20 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.20.apk), SHA-256 `ae4d541bbfdb57d1cb361705e9a4aedbb268b94e5404956ad098a6fdf82bb9df`.

Sonraki adım: kullanıcının yeni ana sayfa görselini değerlendirmesi ve fiziksel TV'de kenar/okunabilirlik kontrolü; bu önizlemeyi TV kurulmuş sayma. Büyük yazıda odak dışındaki alt satırın kısmen görünmesi kaydırılabilir içeriğin normal davranışıdır. BKL-020 görsel kullanıcı kabulü açık. SAFE01 saf karar bölümü korunur; PIN henüz bağlı değildir, yeni özellik işi görsel önceliğin önüne geçmemeli.

## SAFE01 · Ortak erişim karar bölümü · 15 Ekim 2026

Önceki kimlik teslimi somut ilerlemeydi. AccessPolicy.kt eklendi: source/content/category kapsamları, ayrı ProviderId/M3uName anahtarları, gizleme/PIN ayrımı ve ALLOW/REQUIRE_PIN/HIDDEN/UNRESOLVED/DENIED kararları. Etkin kurallar kaynak bazında gruplanır, dış liste kopyalanır. Bölüm dizi kuralını doğrulanmış scoped üyelik üzerinden devralır; ilgili kural varken eksik kategori/ebeveyn izin sayılmaz. [Sözleşme](../architecture/ACCESS_DECISIONS.md), [QA](native-access-decisions-qa.json).

12 yeni politika testiyle toplam 134 JVM testi geçti; lint 0 hata/32 uyarı. Saf, henüz bağlanmamış bölüm için cihaz testi yapılmadı. Güncel kaynak/entry ve repository üyeliğini çağıranın çözmesi gerekir; bu sınıf tek başına eski UI nesnesinin otoritesini kanıtlamaz.

TvViewModel/UI henüz karar bölümünü çağırmıyor. PIN doğrulayıcısı, kalıcı politika, oturum izni ve gizli kayıt projeksiyonu yoktur. Ayarlardaki PIN yok açıklaması korunur. Yeni APK yayımlanmadı; son teslim 0.14.19, cihazlar değiştirilmedi. Genel hedef açık.

Tam sonraki iş: PIN ve politika saklamasını tasarla/uygula; güncel Android/OWASP birincil kaynaklarıyla doğrulayıcı, deneme sınırlaması ve bozuk kayıt/kurtarma tercihlerini incele. Kaynak sırrı veya ham PIN loglama. Sonra canonical hedef çözümü ve politika revizyonuna bağlı bekleyen işlem/izin kontrolcüsü; en son bütün UI/arama/rehber/oynatma girişlerini bağla. Saf karar testlerinin varlığını SAFE01 tamamlanması sayma. Görsel sistem ortak TV bileşenleriyle kalacak; fiziksel TV ve kullanıcı görsel kabulü açık.

## 0.14.19 · Kategori ve dizi ebeveyn kimliği · 15 Ekim 2026

Önceki entegrasyon haritası somut eksikleri belirledi. MediaEntry'ye categoryId ve parentLocalId eklendi; Xtream kategori ID'si ve bölümün yerel dizi ID'si korunur, M3U gruplanmış bölüm mevcut showId'yi taşır. M3U için sağlayıcı kategori ID'si uydurulmaz. Mevcut içerik/source/local kimlikleri değişmedi.

Yeni kayıt v3, tam 22 elemanlı dizi kullanır; ilk 20 alan v2 ile aynı. V1/v2 okuyucu korunur ve eski eksik kimlikleri boş/null bırakır. AES-GCM hesap bağlamı/AtomicFile/limitler değişmedi. Sabit v2 fixture eklendi. 122 JVM geçti: aynı adlı kategori farklı ID, ad değişimi, source ayrımı, Xtream/M3U ebeveyn, v1/v2 yükseltme ve mevcut 63.175 kayıt roundtrip dahil. Android şifreli depolama 12/12 ve canlı/film/dizi/parça/favori/geri pilotu 1/1. Lint 0 hata/32 uyarı, v2 APK imzası. [QA](native-content-identity-qa.json), [kayıt sözleşmesi](../architecture/CATALOG_CACHE.md).

[0.14.19 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.19.apk), SHA-256 `806ac448dcf0a4479fbfade5a0887f028452ec7ace93d97dffedc6035dd99807`. Yalnız 5556; TV/5554 korunur. Görsel sistem değişmedi, Figma değişmedi. Eski APK v3 kayıt okuyamaz; downgrade desteği yok. PIN ekranı/erişim motoru henüz yok; genel hedef açık.

Tam sonraki iş: PARENTAL_ACCESS sözleşmesine göre source/type/category kimlik kapsamını ve görünürlük/erişim karar motorunu uygula. Xtream için ID, M3U için ayrı ad tabanlı kategori türü kullan; ikisini aynı ham anahtarda karıştırma. V1/v2'den gelen bilinmeyen kimlikte politika migrasyonu ve eski bölüm parent çözümü açık: korunması gereken hedefte belirsizliği izin gibi yorumlama. PIN saklama/deneme sınırlaması ayrı doğrulanacak. Bütün open/play/asenkron/arama/favori/geçmiş girişleri bağlanmadan SAFE01 tamamlanmış sayılmaz. Fiziksel TV/codec/uzun oturum ve kullanıcı görsel kabulü açık.

## SAFE01 · Native PIN/gizleme entegrasyon haritası · 15 Ekim 2026

Önceki yaşam döngüsü turu somut test kanıtı üretti. Bu tur PRD SAFE01, TV-071/F06, prototip flows.js ve native kaynak/kimlik/arama/oynatma yolları incelendi. [Entegrasyon sözleşmesi](../architecture/PARENTAL_ACCESS.md) gerçek giriş noktalarını ve teslim kapılarını eşler. Uygulamaya PIN eklenmiş değildir; APK 0.14.18 değişmedi.

Somut bulgular: MediaEntry kategori adı taşır, sağlayıcı category_id taşımıyor; kaynak+tür olmadan kategori kuralı isim çakışmasına açıktır. Bölüm ebeveyni MediaEntry'de kalıcı alan değil; WatchRecord.parentId veya doğrulanmış seçili dizi üyeliği gerekir. open/play adult/kaynak kontrolü yapar, native PIN kapısı yoktur. Prototip sabit 1234 yalnız örnektir. SourceLibrary ContentRef kaynak kapsamını zaten sağlar; mevcut kimlik yeniden icat edilmemeli.

Belge; görünürlük ile erişimi ayırır, 12 giriş noktası grubunu, eski/asenkron hedef kontrolünü, geçici izin ve odak dönüşünü, kategori/ebeveyn ve saklama karar kapılarını tanımlar. Geçici izin kapsamı ve saklama tasarımı uygulama önerisidir; kullanıcı kararı gibi kaydedilmedi. Kriptografik algoritma seçilmedi ve güvenlik tamamlandı iddiası yok. Doküman değişikliği için uygulama testi/derleme çalıştırılmadı. Kullanıcı cihazları değişmedi; genel hedef açık.

Tam sonraki iş: SourceRepository'nin Xtream category_id→ad dönüşümünü ve M3U bölüm gruplamasını incele. Kategori/ebeveyn kimliğini korumak için gereken en küçük geriye uyumlu model/cache değişikliğini tasarla ve gerçek v1/v2 fixture'larla doğrula; mevcut içerik kimliklerini/favori/geçmişi değiştirme. Ardından kapsam karar motoru ve bütün giriş kapıları. PIN/veri saklaması güncel birincil güvenlik belgeleriyle ayrı doğrulanmalı. Görsel sistem mevcut TV bileşenleriyle kalacak; fiziksel TV ve görsel kullanıcı kabulü açık.

## 0.14.18 · Oynatma ve duraklatma yaşam döngüsü · 15 Ekim 2026

Önceki ekran bütünlüğü kontrolü somut kanıt üretti. Kod/test incelemesi, bölüm sonu ve kurtarma arka plan testlerinin varlığını, normal oynayan ve kullanıcı tarafından duraklatılmış videonun dönüşüne doğrudan ölçüm eksikliğini gösterdi. EpisodeAutoplayFlowTest'e playingAndPausedEpisodesPreservePositionAcrossBackgroundAndExit eklendi. Gerçek Media3 Player, PlayerView üzerinden okunur; üretime test kancası eklenmez.

Yeni test ve mevcut sonraki sezon/geri odağı ile geri sayım arka plan iptali toplam 3/3 geçti (52,366 s). Arka planda oynatma durmuş, >1 s konum kaydedilmiş, 1,2 s beklemede konum farkı <150 ms kalmış; geri dönünce aynı Player oynatmaya devam etmiş. Kullanıcı duraklatması ikinci dönüşte korunmuş. Detaya model.back ile dönüşte aynı bölüm ilerlemesi ve kart odağı doğrulanmış. Sonraki bölüm testindeki gerçek Back tuşları da geçti. [QA](native-player-lifecycle-qa.json).

Bu test küçük, tamponlanmış yerel MP4'te yeni istek sayısını kontrol eder; tüm HLS/canlı soketlerin arka planda kapandığı veya fiziksel TV'de tek bağlantı garantisi sayılmaz. Uzun oturum/codec/süreç ölümü kapsamı değildir. Üretim kodu/APK 0.14.18 değişmedi; yalnız test APK derlendi, JVM/lint tekrarlanmadı. Yalnız 5556/sentetik kaynak, fixture cleanup geçti; TV ve 5554 korunur. Genel hedef açık.

Tam sonraki iş: PRD SAFE01/BKL-028 native eksikliğini somutlaştır. Mevcut prototip PIN/gizleme tasarımını ve kaynak/kategori/kanal kimliklerini inceleyerek ortak erişim denetimi ve kalıcı veri kapsamını yaz; üretim PIN yokken tamamlandı varsayma. Kullanıcının tasarım önceliği nedeniyle yeni bağımsız görsel sistem veya ilgisiz özellik ekleme. Önce tüm giriş noktalarını (arama/favori/geçmiş/doğrudan detay/oynatma) eşleştir; mevcut yetişkin filtrelemesini PIN yerine sayma. Fiziksel TV ve kullanıcı görsel kabulü ayrı açık kapılardır.

## 0.14.18 · Ekranlar arası bütünlük kontrolü · 15 Ekim 2026

Önceki kütüphane teslimi somut ilerlemeydi. Bu tur mevcut 0.14.18 APK üzerinde normal/2.0 yazıda captureGoogleTvRevision ve playerPanelKeepsTitleAndRemoteActionsInsideScreen toplam 4/4 geçti. 20 güncel ekran ve iki karşılaştırma panosu kaydedildi. Gerçek afişler yerel test sunucusundan, video sentetik yerel fixture'dan geldi; gerçek sağlayıcı oynatılmadı. İki ayrı test senaryosu tek kesintisiz uçtan uca oturum diye sunulmaz. [QA](native-cohesion-qa.json).

İncelenen normal/büyük yazı panolarında ortak kenar hizası, yüzey, odak dili tutarlı; yeni somut görsel hata görülmedi. Büyük yazıda odaksız alt satırlar kısmen görünür, seçili kart/eylem kaydırılarak görünür tutulur. Üretim kodu veya APK değişmedi, yeni sürüm çıkarılmadı; bu nedenle build/JVM/lint tekrar edilmedi. SHA-256 d20e80a933e5a4a1ab5b493088dd397803b3d948f40eee13206e1c144d7b3dd8. Yalnız 5556; font 1.0'a döndü, fixture temiz. TV ve 5554 korunur.

Tamamlanma yeniden değerlendirmesi: bu dört kontrol tüm PRD'yi kanıtlamaz. SAFE01 native PIN henüz yok (SettingsScreen açıkça bildirir); mevcut prototip PIN kanıtı native kabulü değildir. Fiziksel TV medya/performans/uzun oturum, 5 kişilik kumanda turu, Figma ve görsel kullanıcı kabulü açık. Bunlar nedeniyle genel hedef tamamlanmadı.

Tam sonraki iş: Güncel görsel karşılaştırma panolarını referans alarak gereksiz yeni stil değişiklikleri üretme. Oynatıcıda arka plana alma/geri dönme ve sonraki bölümden çıkışın izleme ilerlemesi/tek oturum davranışını güncel kod ve mevcut testlerle gözden geçir; kapsanmayan gerçek bir geçiş varsa hedefli test ve düzeltme yap. SAFE01 için ayrı kapsam/uygulama işi backlog'a eklendi; prototip ve native tamamlanma ayrımı korunmalı. Fiziksel cihaz izni/erişimi olmadan 5554 veya TV'yi test/reset etme.

## 0.14.18 · Eksik kapak ve yıl hizası · 15 Ekim 2026

Önceki kaynak kartı teslimi somut ilerlemeydi. Kütüphane film/dizi kartlarında eksik afiş içindeki tekrar başlık kaldırıldı; ortak simge ve dış başlık korunur. Yıl bilinmediğinde bir satırlık alan korunarak aşağıdaki kaynak etiketinin hizası sabitlenir. MediaCard değişti; diğer Artwork kullanımları korunur. [Tasarım](../design/LIBRARY_CAPTIONS.md), [QA](native-library-captions-qa.json).

Mevcut gerçek afiş/sentetik sağlayıcı testi, yılı olan/olmayan satırların üst kenarı/yüksekliği ve kapaksız kartın tam görünürlüğü kontrolleriyle güçlendirildi. Aday üç font 3/3; sadece sürüm değişimi sonrası final 2.0 font 1/1 ve normal D-pad/aynı karta dönüş 1/1. Altı görüntü; normal ve 2.0 kapaksız görünüm incelendi. İlk test derlemesindeki DpRect.height erişimi bottom-top olarak düzeltildi, ardından derleme/testler başarılı. Lint 0 hata/32 uyarı, v2 imza; JVM bu yerleşim turunda tekrarlanmadı.

[0.14.18 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.18.apk), SHA-256 `d20e80a933e5a4a1ab5b493088dd397803b3d948f40eee13206e1c144d7b3dd8`. Yalnız 5556; font 1.0 ve fixture temiz. TV 0.14.4 / kullanıcı 5554 korunur. Figma değişmedi. Genel hedef açık.

Tam sonraki iş: Son birkaç yerleşim düzeltmesini ayrı ayrı sürdürmek yerine güncel sürümün ana sayfa→kütüphane→detay→oynatıcı→geri akışını bütün olarak görsel incele; captureGoogleTvRevision ve uygun oynatıcı görsel akışı güncel ekran kanıtı sağlar. Gerçek afiş/sentetik yerel medya kullan, fiziksel cihaz onayı yerine geçirme. Ekranlar arası belirgin tutarsızlık veya eksik davranış varsa düzelt. Fiziksel TV/codec/uzun oynatma, Figma, kullanıcı görsel kabulü ve PRD'deki diğer kalite kapıları açık.

## 0.14.17 · Kaynak kartlarının bilgi hiyerarşisi · 15 Ekim 2026

Önceki arama yerleşimi somut ilerlemeydi. SourcesScreen içinde kaynak numarası/türü ve içerik sayısı 11/16 sp üst satıra ayrıldı; kaynak adı 18/24 sp Medium, güncelleme ayrı satır. Adın ortadan kısaltılması ve tam ad okuyucusu korunur. Kart içindeki işlem odaktayken 1 dp mint grup sınırı, diğer kartlarda ayırıcı sınır kullanılır. Düğme odağı ve eylem sırası değişmedi. [Tasarım](../design/SOURCE_HIERARCHY.md), [QA](native-source-hierarchy-qa.json).

16 uzun adlı kaynakla üç fontta başlık/kart/eylem görünürlüğü ve düzenle/kaldırma iptali/bilgi dönüşü aday 6/6, final 6/6 geçti. Final 2.0 font hata durumu ayrıca 1/1. Normal ve büyük yazı final görüntüleri incelendi; 16 ekran görüntüsü kaydedildi. Lint 0 hata/32 uyarı, APK v2 imzası. Yerleşim değişikliği nedeniyle JVM tekrarlanmadı.

[0.14.17 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.17.apk), SHA-256 `d4f2f5650713315fe10760aaaaf5919645749e7f2af25799d54cecd4323c750f`. Yalnız 5556 güncellendi; font 1.0'a döndü, sentetik fixture temizlendi. TV 0.14.4 ve kullanıcı 5554 korunur. Figma değişmedi. Genel hedef açık.

Tam sonraki iş: Kütüphane yüzeyinin güncel native görüntülerini incele. Film/dizi afişleri, eksik kapaklar, kategori şeridi ve odaklı başlıkların uyumunu değerlendir; mevcut ortak bileşenlerde somut görsel kusurları düzelt. Kaynak ekranında yeni test eklemek yerine mevcut yedi kontrol yeterli; ancak yeni kusur görülürse genişlet. Fiziksel TV/codec/uzun oynatma, Figma ve kullanıcı görsel kabulü açık.

## 0.14.16 · Arama sonuçlarında afiş ve alan kullanımı · 15 Ekim 2026

Önceki arama indeks düzeltmesi somut ilerlemeydi. Görsel incelemede iki geniş kartın içindeki boş alan ve küçük afişler hedeflendi. SearchScreen normal yazıda 260 dp, büyük yazıda 360 dp minimum uyarlanan sütun genişliği kullanır; 960 dp görünümde üç/iki sütun. Afiş 56×84 yerine 72×108 dp, kart minimum 132 dp. Kanal logo oranı korunur. Eksik görselde ikinci başlık kaldırıldı. Ortak CardVisibility ve 28 dp alt güvenli pay seçili kartı bütün gösterir. [Tasarım](../design/SEARCH_RESULTS.md), [QA](native-search-density-qa.json).

Adayda üç font 3/3; sürüm/biçimlendirme sonrası finalde üç font 3/3 ve Türkçe arama/iptal/odak/Back 2/2. Altı final görüntü kaydedildi; normal ilk satır ve 2.0 son satır görsel olarak incelendi. Odaksız komşu satırın kısmi görünmesi normal kaydırma davranışıdır; odaklı kart güvenli payla tam görünür. Lint 0 hata/32 uyarı, APK v2 imzası. JVM yalnız yerleşim değiştiği için tekrarlanmadı.

[0.14.16 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.16.apk), SHA-256 `e6a3467c2faaf7dd4caf175d224c76092f7fe97e05d9944fd9b6ef41956a2e3c`. Yalnız 5556 güncellendi, font 1.0'a döndü ve fixture temizlendi. TV 0.14.4 / kullanıcı 5554 korunur. Figma değişmedi. Genel hedef açık.

Tam sonraki iş: Kaynak yönetimi ve kütüphane yüzeylerini güncel gerçek native görüntülerle incele; kaynak adları uzunken, birden fazla kaynak ve eksik kapak varken hiyerarşi/eylem odağını kontrol et. Aramada 2.0 yazıda üst kontroller çok yer kaplar; tüm sonuçlar görünmek zorunda değildir ama odaklı kartın sığması korunmalı. Tasarım değişikliklerini ortak bileşenlerle yap; yeni özellik yerine görsel kaliteyi sürdür. Fiziksel TV/codec/uzun oynatma, Figma ve kullanıcı görsel kabulü açık.

## 0.14.15 · İlk aramada hazır indeksin yeniden kullanılması · 15 Ekim 2026

Önceki tanı somut ilerlemeydi; bu tur neden ve düzeltme doğrulandı. Değişmeyen katalog tekrar yayımlandığında StateFlow eşit yeni değeri atıp eski Catalog nesnesini tutabiliyor. publishLibrary yerel yeni nesne için indeks hazırlıyordu; ilk sorgu durumdaki eski nesneyle ikinci indeks oluşturuyordu. Artık state.value.allCatalog hazırlanıyor. Geçici aşama tanısında ilk sorgu indeks beklemesi 593 ms, eşleştirme 128 ms; geçici günlük kodu finalde kaldırıldı.

LargeCatalogProbeTest beş aynı restore sonrasında hazırlanmış katalog kimliğini ve 24 sorgunun aynı deferred indeksi kullanmasını doğruluyor. Eski kodda kimlik kontrolü başarısız, düzeltmede geçti. Ayrıca ölçüm gözlemcisi önce searching=true bekler: önceki sonraki-sorgu 1–3 ms değerleri yeni sorgu/eski sonuç ara durumunu yakaladığı için geçersizdi. Önceki QA'ya düzeltme eklendi; ilk sorgu ve toplam süreler bu hatadan etkilenmedi.

Son sürümde ilk sorgu model hazır 309 ms, test görünürlük/idle dahil 450 ms (önceki toplam 860–891 ms). Sonraki ilk beş sorgu model hazır 45–100 ms. Bunlar debug emülatör ölçümleri; fiziksel p95/ilk piksel iddiası yok. 118 JVM geçti; büyük katalog, Türkçe arama/iptal/tür/yenileme/odak ve Back akışı 3/3. Lint 0 hata/32 uyarı; APK v2 imzası doğrulandı. [QA](native-search-index-reuse-qa.json).

[0.14.15 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.15.apk), SHA-256 `ed273513824b8fdbf6d0220b49a1e35a43fd1cba5bfb045ac6eca552d1118c24`. Yalnız 5556 güncellendi; TV 0.14.4 ve kullanıcı emülatörü korundu. Üretim görünümü ve Figma değişmedi. Genel hedef açık.

Tam sonraki iş: Performans düzeltmesini bitmiş sayıp yalnız ölçüm üretmeye devam etme; güncel native ekran görüntülerinden arama/kütüphane/kaynaklar yüzeylerinin görsel hiyerarşisini, içerik yoğunluğunu ve odak görünürlüğünü incele. Önce mevcut tasarım sözleşmelerini oku; somut görsel kusur bulup ortak bileşenlerde düzelt. İlk sorgudaki kalan süre veya fiziksel performans için yeni kanıt gerektiğinde ayrı tanı yapılabilir. Fiziksel TV/codec/uzun oynatma, Figma ve kullanıcı görsel kabulü halen açık.

## 0.14.14 · İlk arama beklemesinin ayrılması · 15 Ekim 2026

Önceki kompakt katalog teslimi somut ilerlemeydi. LargeCatalogProbeTest artık StateFlow sonucunu doğrudan gözleyerek model hazır olma anını testin görünürlük/idle beklemesinden ayırıyor. Gözlemci test temizliğinde iptal edilir. Üretim kodu ve APK değişmedi.

Aynı 60 bin kayıtla iki çalıştırma 1/1 + 1/1 geçti. İlk sorgu model yayını 786/748 ms; görünürlük ve idle dahil toplam 891/860 ms. Son çalıştırmanın sonraki beş sorgusunda model 1.1–1.7 ms, toplam 176–300 ms. Bu ölçüm ilk beklemenin çoğunu model tarafına daraltır; indeks bekleme, eşleştirme ve ana iş parçacığı yayını henüz ayrılmadı. Kodda debounce yok. Yayın sonrası süre saf çizim süresi değildir; test polling/idle da içerir. [Ham QA](native-search-stage-qa.json).

Yalnız doğrulanmış nextv_design_api36 / emulator-5556 kullanıldı; fixture cleanup geçti. Final test APK derlendi. Üretim değişmediği için JVM/lint tekrarlanmadı. TV ve kullanıcı emülatörü korunur. Genel hedef açık.

Tam sonraki iş: TitleIndexCache.prepare/get ve updateSearch yolunda indeks hazırlama/await, find ve ana iş parçacığı yayını sürelerini ayrı tanıla. Gerekirse yalnız test/debug tanısı kullan; üretime kalıcı günlük veya hesap/başlık bilgisi ekleme. İlk sorguyu indeks tamamlandıktan sonra ayrı kontrolle kıyasla; gecikmenin indeks olduğuna henüz kesin hüküm verme. Sonrasında ölçüm destekli düzeltme, arama iptal/odak regresyonları ve görsel inceleme. Fiziksel TV, Figma ve kullanıcı görsel kabulü açık.

## 0.14.14 · Kompakt katalog üretim teslimi · 15 Ekim 2026

Önceki aşama/biçim denemesi somut ölçüm sağladı. Yeni kayıtlar gzipJSONv2dizi satırıyla yazılır; v1okuyucu ve hesap bağlı AES-GCM/AtomicFile korunur. Başlık, boş bölüm grupları, bütün içerik alanları, rehber adresleri ve zaman damgası korunur. CatalogSnapshot.kt/CatalogStore.kt değişti; sabit catalog-v1.jsonl, uyumluluk/limit ve cihaz şifreli geçiş testleri eklendi. Tarihli CompactCatalogExperimentTest Ignore ile kapatıldı; artık encoder-v1karşılaştırması yapamaz.

118JVM geçti; bilinmeyen sürüm/eksik satır/150bin satır ve160MiB açılmış veri reddi testli. Cihaz depolama11/11; v1şifreli kayıt→v2, bozulma, başka hesap, EPG başlığı doğrulandı. Aşama/büyük katalog/PilotFlow3/3. Son yalnız biçimlendirme sonrası final build/JVM ve harici soğuk yeniden açılış3/3.63175kayıt, favori,45sn ilerleme/geçmiş korundu; PID değişimi doğrulandı. Lint0hata/32uyarı,v2imza. [Sözleşme](../architecture/CATALOG_CACHE.md), [QA](native-compact-cache-qa.json).

60bin çözüm medyanı1905→1004ms; aynıtestte yükleme→ana sayfa1800–2242ms. İlk sorgu893ms, düzelmiş sayılmaz. Soğuk verifier state hazır3536ms; harici am-start2802ms ilk içerik zamanı değildir. Emülatör/debug ölçümleri, fiziksel p95 değil.

[APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.14.apk), SHA-256 `73a6b4314467793a18bda25577d681cc989e88fc1dc5b916fc857b225aaf8bdb`. Yalnız5556,font1.0; fixture temiz ve uygulama durduruldu. TV0.14.4ve5554korundu; Figma değişmedi. Genel hedef tamamlanmadı.

Tam sonraki iş: İlk arama beklemesinin indeks hazır olma/debounce/model yayını/Compose ilk çizim/klavye etkileşimi payını ayır. Yaklaşık0.9snölçümün tamamını indeks maliyeti varsayma. Mevcut arama iptal/kuşak/odak davranışını koru. Fiziksel TV performans/codec/uzun oynatma ve görsel kullanıcı kabulü açık.

## 0.14.13 · Katalog aşama tanısı · 15 Ekim 2026

Önceki menü ölçümü somut kanıt üretti; üretim menüsü korunur. CatalogStageProbeTest60bin kayıtla1/1 geçti: tam çözüm yaklaşık1.9–2.0sn, indeks393–527ms, hazır sorgular25–95ms. Ardışık debug bileşen ölçümleridir; eklenebilir açılış aşamaları veya fiziksel p95 değildir.

Yeni CompactCatalogExperimentTest1/1:60bin zengin kayıt, bütün MediaEntry alanları eşit, dönüşümlü nesne/dizi denemesi. Çözüm medyanı2222→1152ms (yaklaşık%48azalma). Test prototipi grup/başlık uyumluluğu, boyut sınırı, şifreleme ve UI içermez; henüz üretime alınmadı. [Rapor](CATALOG_STAGE_PROFILE.md), [ham veri](native-catalog-stage-qa.json).

Üretim APK değişmedi:0.14.13 SHA-256 ca9d2042a72f9ba145b0fe669b9cf38d19f480d22b98cb96338e61c6df28c262. Yalnız5556; stage-probe cachefinallyile kaldırıldı, compact denemesi bellekte. TV0.14.4ve5554korundu. Bu tur test APK derlendi; lint/JVM tekrarlanmadı. Figma değişmedi; genel hedef tamamlanmadı.

Tam sonraki iş: Ölçüm destekli kompakt katalogv2uygulaması için v1okuyucuyu koru; bölüm grupları/boş gruplar/header/EPG, bütün alanlar, bozuk sürüm/limitler, kimlik doğrulama ve atomik yazım kapılarını rapordaki sırayla doğrula. CatalogSnapshotCodec CatalogSnapshot.kt içindedir; CatalogStore.readEpgFeeds de sürüm kontrolü yapar. Eski sürüm testleri güncel encoderı değiştirerek fixture üretiyor: gerçekv1sabit fixture kullanarak geriye uyumluluğu bağımsız test et. Ardından güncel aşama profili, büyük katalog UI ve süreç yeniden açılış kontrolü. Fiziksel cihaz/codec ve kullanıcı görsel kabulü açık.

## 0.14.13 · Menü aralığı şüphesinin doğrulanması · 15 Ekim 2026

Önceki hero teslimi somut ilerlemeydi. Yeni ölçüm, son görsel incelemede şüphelenilen Filmler/Diziler sıkışmasını doğrulamadı. Karakter kutuları32dp, önceki ve yeni2.0font ekranlarının piksel boşluğu69px. Önceki görsel yorum düzeltilmiştir; gereksiz aralık değişikliği yapılmadı.

SearchNavigationTest içerik odaktayken ve beş menü odağı boyunca bütün etiketlerin karakter kutusu/iç pay/taşmasını ölçer. VisualPreview yenileme+afişli hero akışında iki etiket aralığını kontrol eder. Üçfontta2test6/6; dokuz görüntü ve21ölçüm JSON'u. [İnceleme](../design/NAVIGATION_PAINT_AUDIT.md), [QA](native-nav-paint-qa.json).

Üretim kodu/APK değişmedi; son0.14.13 SHA-256 ca9d2042a72f9ba145b0fe669b9cf38d19f480d22b98cb96338e61c6df28c262. Yalnız5556,font1.0. TV0.14.4ve5554korundu. Bu test turunda lint/JVM tekrarlanmadı; test APK derlemesi başarılı. Figma değişmedi; genel hedef tamamlanmadı.

Tam sonraki iş: CatalogStageProbeTest dosyasını tekrar okuyup ayrı5556üzerinde çalıştır; kayıt okuma/decode/birleştirme/home/indeks ölçümlerini topla. Sonuç görmeden üretim performansını değiştirme; debug tanısını fiziksel p95 sayma. Fiziksel performans/codec, Figma ve kullanıcı görsel kabulü açık. Menü aralığı artık açık hata sayılmamalı.

## 0.14.13 · Ana sayfa üst alanı · 15 Ekim 2026

Önceki0.14.12devam kartı işi somut ilerlemeydi. Bu tur HomeScreen hero kaynak filtresi ayrı başlık satırına alındı; görselsiz içerik tam genişlik kullanıyor. Büyük yazıda28/33sp başlık ve tek satır özet; normal36/41sp. Hero odağı ortak görünürlük kapsayıcısıyla bütün alanı gösterir. Poster üst payı62/76dp; kaynak düğmesinden en az8dp ayrım ölçülür. Sürüm27/0.14.13, yeni HeroLayoutTest ve VisualPreview poster kontrolü.

Adayda üçfont×3test9/9; son afiş boşluğu düzeltmesinden sonra üçfont×2test6/6. Son2.0font ContinueCardLayout1/1 ve normalPilotFlow1/1. Lint0hata/32uyarı,v2imza; JVM bu UI turunda koşulmadı. Normal/2.0görseller incelendi. [Sözleşme](../design/HOME_HERO.md), [QA](native-hero-layout-qa.json). Yatay arka planlı görseller adaydan; son değişen afiş ve kapaksız görseller yenilendi.

[APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.13.apk) SHA-256 `ca9d2042a72f9ba145b0fe669b9cf38d19f480d22b98cb96338e61c6df28c262`. Yalnız5556,font1.0. TV0.14.4ve kullanıcı5554korundu. Figma değişmedi. Genel hedef tamamlanmadı.

Sonraki iş:2.0fonttaki bazı görüntülerde Filmler/Diziler etiketleri görsel olarak yakın; önceki ölçü testinin gerçek boyanmış metin aralığını kapsadığını varsayma. Menü ölçüsünü, odak durumunu ve yatay kaydırmayı yeniden incele. Ardından bekleyen CatalogStageProbeTest ile yükleme/ilk sorgu aşama tanısı. Fiziksel performans/codec, Figma ve kullanıcı görsel kabulü açık.

## 0.14.12 · Devam kartı uzun içerik kontrolü · 15 Ekim 2026

Önceki0.14.11afiş teslimi somut ilerlemeydi. Bu tur uzun başlık/iki kaynak/sekiz dizi devam kartı gerçek şifreli kayıt açılışıyla sınandı. Aynı test eski0.14.11üzerinde2.0fontta süre kesilmesini doğruladı; yeni sürümde geçiyor. Sezon/bölüm ve süre ayrı satırlarda, yükseklik içeriğe göre, yer değiştirme ölçüsü dış kapsayıcıda. Kapaksız küçük alanda tekrar eden kırpılmış başlık kaldırıldı; erişilebilir durum açıklaması eklendi. HomeScreen/PilotApp/sürüm ve ContinueCardLayoutTest değişti.

İlk izole setContent testi idle zaman aşımı verdi. Geometri tanısı sürekli boyut değişimi göstermedi; test native açılışa taşındı. Bu olay üretim donması olarak raporlanmaz. Geçici tanı kodu kaldırıldı. Üç fontta iki vaka6/6; son derlemede2.0fontta uzun kart1/1, normalde raf/eksik kapak/PilotFlow3/3. Lint0hata/32uyarı,v2imza; JVM bu UI turunda koşulmadı. [Tasarım](../design/CONTINUE_CARDS.md), [QA](native-continue-cards-qa.json), dokuz görsel.

[0.14.12APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.12.apk) SHA-256 `fd4cf7ad2fa0df7d6de1c4cfa447c382a7bf7a9c8c2d3634b5ceeaa21fd95411`. Yalnız5556 kullanıldı; font1.0. TV0.14.4ve kullanıcı5554korundu; Figma değişmedi. Genel hedef tamamlanmadı.

Tam sonraki iş: Ana sayfa hero/raf ilk açılış kompozisyonunu uzun başlık ve kapak yokluğu ile iyileştir; büyük yazıda odaksız hero kırpılması ile odaklı içeriğin görünürlüğünü ayır. Önceki CatalogStageProbeTest mevcut fakat çalıştırılmadı; yükleme/ilk arama aşama tanısı bekliyor. Fiziksel performans/codec, Figma ve kullanıcı görsel kabulü açık.

## 0.14.11 · Ana sayfa afişleri ve devam kartları · 15 Ekim 2026

Kullanıcının son isteğiyle öncelik yeniden görsel kaliteye verildi. HomeScreen film/dizi kartları 136×204dp afiş düzeninde; devam kartları 284dp, yazı ölçeğine duyarlı yükseklik, iki satır başlık ve belirgin ilerleme çizgisiyle yenilendi. Ortak renk/odak sistemi ve32dp kenarlar korunur. DesignSystem, HomeScreen, VisualPreviewTest ve sürüm bilgisi değişti.

Üç yazı ölçeğinde yeni raf testi3/3; normal fontta genel görsel akış, eksik kapak/ayırıcı ve PilotFlow3/3 geçti. İlk programatik kaydırma test hatası düzeltildi ve log korundu. Derleme başarılı, lint0hata/32uyarı, v2imza doğrulandı; JVM bu UI turunda koşulmadı. Dokuz yeni görsel, [tasarım sözleşmesi](../design/HOME_SHELVES.md) ve [QA](native-home-shelves-qa.json) hazır.

[APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.11.apk), SHA-256 `8e1213f0a77d1ca8973268bdec7e91309fdedb731949b2f35ddfa4a6e0440cd9`. Yalnız ayrılmış5556 kullanıldı; font1.0. TV0.14.4 ve kullanıcı5554 korundu. Figma değişmedi. Gerçek görseller yerel test sunucusundan; özel sağlayıcıya istek yok.

Sonraki iş: Kullanıcının yeni rafları değerlendirmesi; ana sayfanın ilk ekran kompozisyonu ve çoklu kaynak/uzun başlıklı devam kartlarının görsel kontrolü. Fiziksel TV kurulumu istenirse veriyi koruyan güncelleme. Önceki performans profil işi bekliyor: CatalogStageProbeTest dosyası mevcut ama bu tur çalıştırılmadı. Fiziksel performans/codec, Figma ve genel görsel kabul açık; genel hedef tamamlanmadı.

## 0.14.10 · Büyük katalog ve süreç yeniden açılışı · 15 Ekim 2026

Önceki rehber teslimi somut ilerlemeydi. Üretim APK değişmedi (7b50a46f26819e6779b6c7010d80144830fcaa49e94a278ba210b28cb01610ca). LargeCatalogProbeTest60bin kayıtla geçti:5yeniden yükleme 2766–3081ms;24arama 165–911ms, medyan200ms. İlk sorgu911ms. Debug emülatör/test senkronizasyonu içerir; fiziksel p95 değildir.

CatalogColdLaunchTest genişletildi:63175kayıt+favori+45sn ilerleme+devam geçmişi. Harici force-stop/start sonrası ana sayfa ve devam rafı görüldü. Ayrı PID'deki Activity/ViewModel doğrulaması kayıt/kaynak/favori/ilerleme/geçmişi doğruladı. Paylaşılan tools/check_cold_catalog.py uçtan uca geçti; yalnıznextv_design_api36, açık serial/reset bayrağı, fiziksel cihaz reddi. İlk taşıma log-değişken çakışması düzeltildi. Temizleme geçti;5556uygulaması durduruldu, test kaynakları kaldırıldı.

[Rapor](LARGE_CATALOG_CURRENT.md), [ham ölçümler ve kapsam](native-large-catalog-current-qa.json). Bu tur yeni APK yok; son0.14.10. TV0.14.4 ve5554korundu; Figma değişmedi. Genel hedef tamamlanmadı.

Tam sonraki iş: Yeniden yükleme ve ilk sorgunun zamanını okuma/çözme/decode/birleştirme/indeks/ilk UI aşamalarına ayıran tanısal profil çıkar. Ölçümden önce optimizasyon yapma; kalıcılık/hata güvenliğini koru. Fiziksel kumanda/ilk kare/codec/uzun oynatma, Figma ve görsel kullanıcı kabulü açık.

## 0.14.10 teslim · Rehber logoları · 15 Ekim 2026

Önceki tur boş tür/hata başlığı ve akış incelemesi somut ilerlemeydi; bu teslimde korunur. GuideScreen logo kutusu72×40dp (büyük yazı72×44),iç pay2dp; kanal sütunu196/232dp. Oran koruyan Artwork değişmedi, zaman modeli aynı. VisualPreview altı gerçek logo satırını D-pad ile dolaşıp sınır/genişlik ölçer; GuideFlow uzun ad/ayrıntı/saat dilimi/kaynak ayarlarını kontrol eder. İki vaka üçfontta geçti,15görsel. İlk2.0hatası oluşturulmamış üçüncü satırı ölçen test varsayımıydı; gezerek ölçmeye düzeltildi, log korundu.

Son normal fontta PilotFlow ve GuideFlow zaman konumu/ayrıntı/oynatıcı dönüşü2/2. Lint0hata/32uyarı,v2imza; JVM bu UI turunda koşulmadı. [Tasarım](../design/GUIDE_LOGOS.md), [QA](native-guide-logo-qa.json).

[0.14.10APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.10.apk) SHA-256 7b50a46f26819e6779b6c7010d80144830fcaa49e94a278ba210b28cb01610ca. Sadece5556,font1.0. TV0.14.4 ve kullanıcı5554 korundu; gerçek sağlayıcıya erişilmedi. Figma kota nedeniyle eşleşme açık; genel hedef tamamlanmadı.

Tam sonraki iş: Görsel turdan sonra güncel sürümde büyük katalog ve süreç sonlandırma sonrası açılış/kalıcılık kanıtını yenile. LargeCatalogProbeTest ve CatalogColdLaunchTest kaynaklarını önce incele; yalnız5556 ve sentetik veri. Soğuk açılış/arama ölçümünü debug emülatör tanısı olarak raporla; fiziksel p95/codec/HDR/uzun oynatma onayı sayma. Ardından gerçek artwork/Figma ve kullanıcı görsel değerlendirmesi açık kalır.

## 0.14.10 çalışma adayı · Boş tür ve bütün akış incelemesi · 15 Ekim 2026

Önceki0.14.9turundaki teslim somut ilerlemeydi. Boş ana sayfada hata varsa başlık artık “İçerikler gösterilemiyor”. Yeni test sağlayıcının film listesini gerçekten boş döndürür, canlı içeriği korur; kaynak yönetimine gidip Geri ile kütüphaneye dönmeyi doğrular. Bu vaka ve EmptyHomeLayoutTest üç fontta2/2 geçti. Yeni normalfont VisualPreview genel akışı1/1,8görsel üretildi; ana sayfa/film/detay/canlı/rehber/arama görsel olarak karşılaştırıldı. Lint0/32; JVM bu UI turunda koşulmadı.

[İnceleme](../design/FLOW_VISUAL_AUDIT.md), [QA](native-flow-audit-qa.json). En belirgin yeni bulgu rehberde yatay logoların küçük kare kutuda okunamayacak kadar küçülmesi. Kaynak sırları kullanılmadı. Figma kişisel hesap doğrulandı fakat204:32screenshot Starter MCP kotasına takıldı; güncel eşleme yok.

Aday0.14.10/code24 SHA-256 113c9b24a963890e4781cc4eb5c1cdb0db305f1f6d9910c72710ca69802b537f; henüz releases teslimi/imza yok, son dağıtım0.14.9. TV0.14.4 ve kullanıcı5554 korunur. Test yalnız5556,font1.0. Genel hedef tamamlanmadı.

Tam sonraki iş: GuideScreen kanal sütunu/logo kutusunu birlikte düzenle; yatay ve kare logo, uzun kanal adı, üç fontta bütün satır ve gerçek D-pad/rehber gezinmesini doğrula. Ardından0.14.10paket/regresyon/imza değerlendirmesi. Gerçek artwork/Figma eşlemesi, fiziksel performans/codec ve kullanıcı görsel onayı açık.

## 0.14.9 teslim · Boş içerik ve menü aralıkları · 15 Ekim 2026

Önceki yükleme/kurtarma adayı bu teslimde yer alır. Bu tur boş kütüphane nedeni/eylemi ayrıldı; filtreleri temizle ve favorilerden keşfet akışları eklendi. Arama sürerken yanlış öneri kaldırıldı. Boş ana sayfa gerektiğinde kayar. Büyük yazıda yakınlaşan menü etiketleri gerçek metin ölçüsü+28dp genişlikle düzeltildi; test artık iç payı ölçer.

Final3vaka üç fontta geçti: boş filtre/favori, menü/ayarlar, boş/hatalı ana sayfa. İzole ana sayfa testinin eksik zemini düzeltildikten sonra1vaka üçfontta tekrarlandı,6görsel yenilendi. Son normal fontta PilotFlow canlı/film/dizi/dil/favori/Geri, source recovery ve indirme iptali3/3. Önceki Türkçe arama ve yükleme kontrolleri kapsamı [QA](native-empty-state-qa.json) içinde ayrıdır. Lint0hata/32uyarı,v2imza. JVM bu UI turunda koşulmadı. [Tasarım](../design/EMPTY_STATES.md).

[0.14.9 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.9.apk) SHA-256 b14c027a64899f4e378451dae8cf7d8f42b1d60eeb09773e5a29f2647a65b3a6. Yalnız5556 kullanıldı,font1.0. TV0.14.4 ve kullanıcı5554 korundu; Figma değişmedi, gerçek sağlayıcıya istek yok.

Sonraki iş: Gerçekten boş içerik türü kütüphane dalını test et; boş ana sayfadaki hata başlığını belirginleştir. Ardından ana ekran/kütüphane/detay/oynatıcı görsellerini bütün akış olarak karşılaştırıp gerçek artwork/Figma tutarlılığına dön. Fiziksel performans/codec, kullanıcı görsel kabulü ve PRD kapıları açık; genel hedef tamamlanmadı.

## 0.14.9 çalışma adayı · Yükleme ve kurtarma · 15 Ekim 2026

Önceki0.14.8 turu somut ilerlemeydi. LoadingScreen ayrıldı: 720dp geniş yüzey, net işlem başlığı/ayrı durum, yüzde uydurmayan ilerleme semantiği ve yalnız indirmede ilk odaklı İptal. Kurtarma ekranı ortak32dp kenar ve içeriğe göre yükseklikli24dp yüzey kullanır. Kaynak işleme modeli değişmedi.

Son adayda üç test üç fontta geçti: beş yükleme işleminin sınır/iptal/semantik kontrolü; recovery yeniden dene/iptal/odak/kayıt koruma/başarılı geri yükleme; kaynak indirme iptali sonrası eski kaynağın kalması.18görsel var. Normal ve büyük yazı görüntüleri incelendi. [Tasarım](../design/LOADING_RECOVERY.md), [QA](native-loading-layout-qa.json). Derleme başarılı; son lint sonucu .private/loading-candidate-lint.log. JVM bu UI turunda koşulmadı.

Aday SHA-256 46b1e48b300ef2efdb6466928204153f6301c7f5aa9644ae36f60e002607d7ac; sürüm0.14.9/code23. Henüz releases teslimi/imza kontrolü yok; son dağıtım0.14.8. Yalnız5556 kullanıldı,font1.0'a döndü. TV0.14.4 ve5554 korundu. Figma değişmedi; gerçek sağlayıcı isteği yok.

Tam sonraki iş: Boş ana sayfa/kütüphane/arama durumlarını sentetik verilerle üç fontta görsel ve kumanda açısından incele; yanıltıcı genel boş mesajlarını duruma göre ayır. Sonra0.14.9 final paket/ilgili regresyon/imza teslimini tamamla. Kullanıcının daha iyi görsel kalite talebi, gerçek artwork/Figma, fiziksel akıcılık/codec ve görsel kabul kapıları açık; genel hedef tamamlanmadı.

## 0.14.8 · Kurulum yerleşimi · 15 Ekim 2026

32dp geniş düzen korunarak kaynak ekleme ekranı yenilendi. 24dp köşeli form yüzeyi, sabit başlık/ana eylem, kayan alanlar, daha kısa karşılama metni. SetupEntry mevcut EntryField ve CardVisibility ile etiketi ve odak çerçevesini birlikte görünür tutar. Kaynak işleme modeli değişmedi.

SetupLayoutTest üç fontta geçti; 12 görüntü kaydedildi. Son alanın kesilen çerçevesi görsel incelemede bulunup düzeltildi; test form sınırını kontrol eder. İlk klavye odak testi başarısızlığı ve derleme hatası logları korundu; final derleme başarılı. Test gezinme adımlarında sistem klavyesini kapatır, tam IME testi değildir. SourceOperation iptal/önceki kaynağı koruma ve kaydetme sırası normal fontta 2/2 geçti. Lint 0 hata/32 uyarı; JVM bu UI turunda tekrarlanmadı. APK imzası doğrulandı. [Tasarım](../design/SETUP_LAYOUT.md), [QA](native-setup-layout-qa.json).

[0.14.8 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.8.apk) SHA-256: 72eeed4b5e86e0973e75e4801e9686daae8d9a427b7502e0d2151d28c1591cbe. Yalnız 5556 kullanıldı, font1.0'a döndü. TV0.14.4 ve kullanıcı5554 korundu; gerçek sağlayıcıya erişilmedi. Figma değişmedi.

Sonraki iş: yükleme, boş ve kurtarma ekranlarının kompozisyonunu incele. Gerçek artwork/Figma, fiziksel performans/codec ve kullanıcı görsel kabulü açık. Genel hedef tamamlanmadı.

## 0.14.7 · Detay ve arama düzeni · 15 Ekim 2026

Önceki0.14.6turundaki geniş düzen korunur. Aramada24sp başlık, filtrelerle sonuç sayısının aynı satırı paylaşması ve tek kaynaktaki tekrar eden kaynak adının kaldırılmasıyla sonuçlar daha yukarıda. Çoklu kaynak adı korunur; canlı logo96×54, afiş56×84; kart12dp/afiş6dpköşe. Film başlığı36sp, özet3satır; gereksiz oynatıcı bilgilendirme satırı kaldırıldı. Bölüm kartları710dp sınırlaması yerine ortak32dpkenarlar arasında üç sütuna yayıldı.

Açıklama mevcut SourceDialog/RemoteTextReader ile tam başlık+metni okutur; ilk odak metinde,↑↓sona gider, Geri açıklama düğmesine döner. Yeni test yaklaşık1700karakter metni ve geniş bölümlerin sınırlarını doğrular. İlk bölüm sınırı testi geçse de ekran görüntüsünde odak çizgisi altta kesildi; CardVisibility ile tam kart taşınır ve test7dp güvenli alan ister. Son normal bölüm görüntüsünde çerçeve ve açıklama tamamen görünür.

Kontroller aşamalı: ilk düzen5vaka üçfontta geçti (VisualPreview genel görünüm, uzun açıklama/bölüm, afiş fallback; SearchNavigation Türkçe arama/filtre ve Geri). Son kenar düzeltmesinden sonra açıklama/bölüm ve üst menü2vaka üçfontta yeniden geçti. Son APK'de PilotFlow canlı/film/dizi/dil/favori/Geri ayrıca normal fontta1/1. Lint0hata/32uyarı; v2imza; JVM bu UI turunda tekrarlanmadı. [Tasarım](../design/DETAIL_SEARCH.md), [QA](native-detail-search-qa.json),21görsel. Loglar .private/detail-search-* ve detail-edge-*.

[0.14.7APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.7.apk) ve hash hazır: df38bdebe1a26323118f187fc04051846832ad6aae9c8044ebb45cd785e0e693. Sadece5556test; sonfont1.0. TV0.14.4 ve kullanıcı5554 korundu. Gerçek sağlayıcıya erişilmedi, Figma değişmedi. Genel hedef tamamlanmadı.

Tam sonraki iş: Kurulum, yükleme/boş/hata ekranlarının aynı32dp kenar, tipografi ve odak standardıyla görsel tutarlılığını incele. Detay başlığı kaydırılırken bağlamın anlaşılması ve çok uzun çoklu kaynak arama etiketleri de izlenecek. Kullanıcının daha yüksek görsel kalite talebi sürer; gerçek artwork/Figma, fiziksel performans/codec, kullanıcı kabulü ve tüm PRD kapıları açık.


## 0.14.6 · Daha geniş düzen ve ana sayfa · 15 Ekim 2026

Önceki tur0.14.5teslimi ilerlemeydi. Kullanıcı sağ/sol boşluğu azaltmayı ve görsel kaliteyi daha ileri taşımayı istedi. Ortak dış kenar58→32dp; kurulumdaki48dp de bu değere bağlandı. Ana sayfa, kütüphane, detay, rehber, arama, kaynak ve oynatıcı ortak kenarı kullanır. Kaynak kartları/başlığı aynı sol çizgide, kaynak grubu20dp ve seçici12dp köşeli/beyaz odaklıdır. Header↓ görünür ilk kaynağın Bilgiler eylemine gider. Görünür kaynak derivedStateOf ile izlenir; yeni lint uyarısı giderildi.

Ana sayfa görseli sağdaki%68alan/top hizasıyla daha az kırpılır; incelenen Inception görselinde yüzler artık görünür. Metin%58, başlık36sp; Ayrıntılar üzerindeki oynatma simgesi kaldırıldı. Canlı raf kartları196→168dp, aynı satırda daha çok kanal görünür. Odaklanan kartın sağ sınırı ekran kenarına dayanıyordu; mevcut görünürlük yardımcısına HomeCard için7dp yatay pay eklendi (diğer çağrılar varsayılan0). Gerçek görselle normal ana sayfa/filmler ve kaynak ekranı incelendi.

Kontroller aşamalıdır: İlk geniş düzen/kaynak APK'sinde5SourceLayout+2VisualPreview üç fontta7/7. Ana sayfa kompozisyonu/kenar düzeltmesiyle genişletilmiş kumanda+afiş fallback iki vaka üç fontta2/2. Son derived kaynak değişikliğinden sonra finalAPK'de normal fontta aynı birleşik8vaka8/8. Tekrarlar ayrı özellikler olarak sayılmaz; QA kapsamı ayırır. Son lint0hata/32uyarı, v2 imza doğrulandı; JVM bu UI turunda tekrar koşulmadı. [QA](native-wide-layout-qa.json), [görsel düzen](../design/WIDE_LAYOUT.md), [kaynak düzeni](../design/SOURCE_PRESENTATION.md).

[0.14.6APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.6.apk) ve hash hazır: 5d0e9503b3f0f2f0f1f760c724c45c4e45f770f9e07d01eb012adafa5035a598. Testler yalnız5556; sonfont1.0. FizikselTV0.14.4 ve kullanıcı5554 korunur, gerçek sağlayıcı isteği yok. Figma değişmedi. İlk başarısız loglar .private/source-style-* ve home-focus-diagnostic.log içinde korunur. player-last-control adlı yanlış/eski kopya görüntüler incelemeden çıkarıldı; finalnormal doğru player-last-action.png üretildi.

Tam sonraki iş: Kullanıcının daha yüksek görsel kalite isteğini sürdürerek detay ve arama ekranlarının kompozisyonunu gerçek görsellerle incele; gereksiz boşluk, başlık/metadata hiyerarşisi ve odak geçişlerini iyileştir. Gerçek artwork/Figma eşlemesi, fiziksel akıcılık/codec, kullanıcı görsel kabulü ve tüm PRD kapıları açık. Genel hedef tamamlanmadı.


## 0.14.5 önizleme · Ses/altyazı paneli teslimi

Önceki tur kalabalık listede kaydırma döngüsünü giderdi. Bu tur ekranı aşan etiketler beş satır + yalnız taşma olduğunda Tam açıklama eylemiyle düzenlendi. Mevcut SourceDialog/RemoteTextReader kullanılır; ↑↓ ile metnin sonuna erişilir, Geri aynı eyleme döner. Desteklenmeyen parçanın açıklaması da seçim yapılmadan okunabilir. Yaklaşık1600karakter metadata testi üç yazı ölçeğinde geçti. İlk test ExoPlayer'ı yanlış thread'den okudu; assertion main thread'e düzeltildi, başarısız kayıt korundu. Üretim hatası diye raporlanmaz.

Son APK'de üç metadata vakası + iki mevcut VisualPreview ses/altyazı/oynatıcı akışı üç fontta geçti; ayrıca normal fontta PilotFlow canlı/film/dizi/dil/favori/Geri akışı1/1 geçti. Lint0hata/32uyarı, v2 imza doğrulandı. JVM bu UI turunda tekrar koşulmadı. SHA256 df205c80d78821ed73f605256682fe879049a0d9018c6ebe8a6f07b98006e225. [0.14.5 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.5.apk) ve hash hazır; [tasarım](../design/TRACKS_PANEL.md), [QA](native-tracks-panel-qa.json). Yalnız5556 kullanıldı; TV0.14.4 ve kullanıcı emülatörü korunur. Gerçek sağlayıcı isteği yok, Figma değişmedi.


Görsel doğrulama notu: İlk normal font okuma ekranı platform pencere animasyonu sırasında yakalandı. Test görüntü yakalama yardımcısına1200ms bekleme eklendi (uygulama süresi değişmedi); yalnız uzun açıklama vakası üç fontta yeniden1/1 geçti,9görsel yenilendi. Normal font yeni başlangıç görüntüsü ve büyük font son görüntüsü incelendi. .private/tracks-reader-*.log; test cihazı font1.0. Proje belge/bağlantı kontrolü geçti.

Tam sonraki iş: Kaynak yönetimi yüzeylerinin ortak hizalama, odak ve büyük yazı düzenini gerçek ekran görüntüleriyle incele; yeni özellik yerine bu yüzeylerin kalitesini iyileştir. Gerçek artwork/Figma eşlemesi, fiziksel cihaz akıcılığı/codec ve kullanıcı görsel kabulü/PRD kapıları açık. Genel hedef tamamlanmadı.


## 0.14.5 · Kalabalık parça listesi kaydırma düzeltmesi

Önceki tur fiziksel TV güncellemesi somut ilerlemeydi; TV 0.14.4'te kalır. Bu tur yalnız5556 üzerinde TracksLayoutTest eklendi: 12ses+12altyazı, iki desteklenmeyen biçim, yaklaşık100karakter açıklama ve Tracks.EMPTY. İki vaka üç fontta geçti. RadioButton rolü, disabled, gerçek D-pad ile23 seçilebilir satıra erişim, satır sınırları, sabit başlık ve Geri kontrol edildi. 9yeni ekran design/reviews/tracks-panel altında; büyük yazıda uzun açıklama ve boş durum, normal yazıda son satır görsel olarak incelendi.

Yeni test özel rememberCardVisibility ile sürekli recomposition timeout'u ortaya çıkardı. Desteklenmeyen TV Button disabled olsa da odak alabiliyordu; canFocus=false eklendi. Bu tek başına timeout'u çözmedi. TrackChoice'tan özel görünürlük yardımcısı çıkarılıp yerleşik verticalScroll/TV Button odak kaydırması kullanıldığında üç fontta test geçti. Ortak CardVisibility diğer yüzeylerde değiştirilmedi. TracksDialog instrumentation erişimi için internal oldu. İlk başarısız log .private/tracks-metadata-initial-failure.log; adım tanısı test logundadır. Lint0hata/32uyarı. APK a27c3f406a5dcc38bf149fdf2e65aea00eb7304662e79156ff1f167947009702; dağıtılmadı.

Son üretim APK üzerinde VisualPreviewTest ses/altyazı seçimi ve oynatıcı paneli dönüşü de normal/1.5/2.0 fontta ikişer geçti (.private/tracks-panel-*.log). Toplam dört farklı vaka üç ölçekte doğrulandı; mevcut PilotFlow geniş akışı henüz bu APK üzerinde tekrar koşmadı. Test cihazı font1.0'a döndü. Proje belge/bağlantı kontrolü geçti.

Tam sonraki iş: Ekran yüksekliğini aşan çok uzun tek etiketin okunabilirliğini çöz/doğrula; mevcut son PilotFlow canlı/film/dizi/dil akışını finalAPK üzerinde kontrol et, sonra imza/teslim. Bu turdaki yaklaşık100karakter etiketi sınırsız uzun metin garantisi değildir. Kaynak yönetimi, gerçek artwork, Figma, kullanıcı görsel onayı ve fiziksel kalite/PRD kapıları açık; genel hedef tamamlanmadı.

## Fiziksel TV güncellendi · 14 Ekim 2026

Kullanıcının açık isteğiyle fiziksel 65U95A, 0.14.1 (code15) sürümünden son teslim edilmiş 0.14.4-design-preview (code18) sürümüne güncellendi. Teslim SHA-256 değeri 51af10e8d87876b563cc6d3171fe0b399efb3b33beed3dadb936618b6c7f5e2f ve APK imzası doğrulandı. Hedef cihaz kimliği doğrulandı; yalnız bu TV üzerinde install -r başarılı oldu. Uygulama verisi silinmedi; MainActivity başlatıldı ve yeni sürüm ile çalışan süreç doğrulandı. Yayın/codec veya kullanıcı görsel kabulü bu işlemle doğrulanmış sayılmaz. Emülatörlere müdahale edilmedi.

0.14.5 çalışma taslağı yayımlanmadı ve TV'ye kurulmadı. Sonraki geliştirme işi aşağıdaki 0.14.5 devir kaydında belirtildiği gibi uzun/çok/boş/desteklenmeyen parça durumlarını doğrulamaktır. Önceki fiziksel TV sürümü kayıtları tarihçedir.

## 0.14.5 taslağı · Ses/altyazı yüzeyi

Önceki0.14.4teslimi ilerlemeydi. Bu tur TracksDialog440dp/sabit başlık-Kapat/sabit açıklama/24dp dış köşe/ayrı kayan listeye geçti. TrackChoice mevcut TV Button üzerinde12dp köşeli tam genişlik, çok satırlı etiket, seçili işareti, selected ve RadioButton semantiği kullanır; odak scale1/beyaz çizgi ve rememberCardVisibility ile satır bütünüyle görünür. Card'ın enabled parametresi olmadığından ilk derleme başarısız oldu, Button ile desteklenmeyen seçimlerin gerçekten disabled kalması korundu. Media3 onTrackSelectionParametersChanged state'e bağlandı; Kapalı göstergesi güncel parametreyi izler.

İlk tur2.0 geçti,1.5 ilk tracks-close odak assertion başarısızdı. Dialog LocalWindowInfo odak bekleme ve focusGroup onEnter eklendi; test de gerçek odağı en çok5sn bekler, zorla focus vermez. Son normal/1.5/2.0 ikişer vaka geçti: tracksPanelKeepsHeaderAndSelectionReadable + önceki playerPanelKeepsTitleAndRemoteActionsInsideScreen. İngilizce ses seçimi, altyazı Kapalı/açık, selected semantiği, tekrar açılışta tercihler, sabit başlık, tam satır ve geri akışları. Son lint0/32; JVM koşulmadı. APK53a9d8faa4da3902daf986b5b7c2ce28bf971a25d47e0f22243e51eb9a0bb16c. Henüz0.14.5dağıtım APK/imza teslimi yok; son releases0.14.4. [Tasarım](../design/TRACKS_PANEL.md), [QA](native-tracks-panel-qa.json),12görsel güncel. Loglar .private/tracks-*; runner .private/test_tracks_panel.py. Sadece5556, font1.0'a döndü; fizikselTV0.14.1 ve kullanıcı5554 korunur.

**Tam sonraki iş:** Uzun etiket, çok sayıda parça, desteklenmeyen ve boş track durumlarını sentetik metadata ile özellikle2.0fontta doğrula; gerekirse boyut/odak/okuma davranışını düzelt. Role.RadioButton semantiğini de assertion ile doğrula. Sonra mevcut PilotFlow canlı/film/dizi/dil akışını finalAPK'de kontrol edip imza/0.14.5teslimini hazırla. Kaynak yüzeyleri, Figma/kullanıcı/gerçek artwork/fiziksel kalite/PRD kapıları hâlâ açık. Genel hedef tamamlanmadı.

## 0.14.4 · Ayarlar bölüm başlığı

Önceki tur0.14.3oynatıcı teslimi somut ilerlemeydi. Bu tur SettingsScreen ana listesinde üç kayan bölüm etiketi kaldırılıp sabit başlıkta odaktaki ayar grubu gösterildi. Aynı ayar sırası ve grup aralığı korunur; diğer grupların satırları da aynı viewport'ta görünebilir. Açıklama ve12görüntü [SETTINGS_SECTIONS](../design/SETTINGS_SECTIONS.md). Section modifier odaktaki satırdan activeSection günceller; etiket heading semantiğinde. Hakkında sayfasındaki belge içi başlıklar aynı kaldı.

SearchNavigationTest.settingsSectionStaysFixedDuringRemoteTraversal dokuz satırı aşağı/yukarı gezer; etiket metni/sabit üst-alt koordinat, görünürlük ve odaktaki satır sınırları, Hakkında→Geri odağını doğrular. Bununla mevcut settingsKeepsHeaderRestoresSubpageFocusAndCancelsRemoval, failedRefreshRemainsReadableAndCachedContentStaysReachable ve VisualPreview refresh testi üç yazı ölçeğinde4'er geçti. Lint0hata/32uyarı; v2 imza doğrulandı. JVM bu UI turunda tekrar koşulmadı. Loglar .private/settings-section-*; runner .private/test_settings_sections.py. Sadece5556, sonfont1.0; fizikselTV0.14.1 ve kullanıcı5554 değişmedi. Figma yazılmadı.

[0.14.4APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.4.apk), SHA-256 ve [QA](native-settings-sections-qa.json) hazır. Önceki APK'ler korundu; eskiZIP güncel değildir. Yeni menü/özellik eklenmedi; odaksız satırların kaydırma sınırında kırpılması liste davranışı olarak kalır.

**Sonraki iş:** Kaynak yönetimi ve Ses/Altyazı panelinin ortak yüzey, odak ve büyük yazı düzenini gerçek ekranlarla incele. Figma eşleme/kullanıcı görsel onayı, gerçek artwork ve fiziksel performans/codec/PRD kapıları açık. Genel hedef tamamlanmadı.

## 0.14.3 önizleme teslimi · Oynatıcı durumları doğrulandı

Önceki tur panel ve odak güvenli alanını iyileştirdi. Bu tur yeni PlaybackRecoveryFlowTest.longTitleRecoveryAndFailureKeepControlsInsideScreen uzun başlıkla503yeniden bağlanma/kalıcı hata/elle yeniden deneme/duraklatma/Geri akışını ve panel sınırlarını doğruladı. GuideFlowTest.liveCardsAndPlayerKeepProgrammeAndRemoteControlsVisible uzun kanal başlığı, şimdi/sonra ve kontrollerin panel içinde kalmasını ölçer. Normal/1.5/2.0 fontta2’şer yeni durum akışı geçti; önceki1’er panel/sağ yön turuyla toplam3vaka üç ölçekte doğrulanmış durumda. SonAPK'de PilotFlowTest.liveMovieSeriesTracksFavoritesAndBack ayrıca geçti. Test sayıları tekrarları benzersiz vaka gibi toplamaz. Üretim APK önceki güvenli kenar düzeltmesinden beri değişmedi.

[0.14.3 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.3.apk) ve SHA-256 dosyası hazır; hash8263bbb7363a03743945a449ee2ca7563d301417ffe6d5404e3abeae7af25032, v2 imza doğrulandı. [QA](native-player-presentation-qa.json), [tasarım ve24görsel](../design/PLAYER_PRESENTATION.md) güncel. Son lint0/32; JVM bu görsel/test turunda tekrar koşulmadı. Özel kanıt player-states-*.log, player-final-regression.log, player-signature.log. Yerel testvideosu/yerel503sunucusu kullanıldı; gerçek sağlayıcı isteği veya yayını yok. Sadece5556; font1.0'a döndü. FizikselTV0.14.1 ve kullanıcı5554 değişmedi, Figma yazılmadı. EskiZIP güncel kaynak değildir.

**Tam sonraki iş:** Ayarlardaki kısmi bölüm başlığını ve kaydırma kenarını tutarlı hâle getir; ardından kaynak ve ses/altyazı yüzeylerini incele. Gerçek artwork eşleşmesi, Figma ve kullanıcı görsel onayı, fiziksel akıcılık/codec ve PRD kapıları açık. Genel hedef tamamlanmadı; yeni özellik önceliği yok.

## 0.14.3 oynatıcı taslağı · 14 Ekim 2026

Önceki tur ilerleme: 0.14.2 ayrı teslimi ve üç fontta12’şer ilgili kontrol. Bu tur PlayerScreen başlık/zaman/kumanda eylemleri tek20dp panelde toplandı;58dp ortak kenar,24/20dp iç boşluk,4dp yuvarlak ilerleme. Yeni özellik veya sağlayıcı yayını yok. [Tasarım ve görüntüler](../design/PLAYER_PRESENTATION.md). Kaynak code17/0.14.3-design-preview; henüz releases teslimi yok.

Yeni VisualPreviewTest.playerPanelKeepsTitleAndRemoteActionsInsideScreen yerel video ile duraklatır; panel sınırları, başlık/eylem sırası, üç sağ tuşla ses/altyazı, Geri, sekiz sağ tuşla son eylem ve Kapat→DETAIL kontrol eder. İlk test playing==null bekliyordu; mevcut sözleşmede playing saklanabildiği için gerçek dönüş Route.DETAIL olarak düzeltildi. İlk görselde Kapat çerçevesi kesiliyordu; sadece metin sınırları bunu kaçırmıştı. Row'a özel BringIntoViewSpec,12dp yatay/8dp dikey pay ve en az11dp odak güvenli alan assertion eklendi. Son normal/1.5/2.0 birer genişletilmiş akış geçti;9 görüntü design/reviews/player-panel altında. Renk çubukları yerel fixture, gerçek film değildir.

Build player-safe-edge-build.log başarılı; lint player-safe-edge-lint.log 0hata/32uyarı. Son APK SHA256 8263bbb7363a03743945a449ee2ca7563d301417ffe6d5404e3abeae7af25032. Player-existing-flows.log: canlı/film/dizi/track/favori/geri ve erişim reddi/elle yeniden deneme2/2 geçti, fakat bu iki akış son12dp kenar değişikliğinden ÖNCE koştu. Son kenar değişikliğinden sonra yalnız üç yeni akış tekrarlandı; tüm testler tekrarlandı iddiası yok. JVM bu tur koşulmadı (son115+1skip tarihçede). Runner .private/test_player_layout.py; yalnız5556, sonunda font1.0. TV0.14.1 ve kullanıcı5554 değişmedi, Figma eşitlenmedi.

**Sonraki iş:** Uzun iki satırlı başlık, canlı program/sonra bilgisi, yeniden bağlanma ve kalıcı hata panelinin özellikle2.0 fontta tüm kontrolleri görünür tuttuğunu gerçek ölçüm/görselle sınayıp düzelt. Sonrasında ilgili oynatma akışlarını sonAPK'de tekrar doğrula, imza ve0.14.3teslimini hazırla. Ayarlar yarım bölüm etiketi, kaynak/track yüzeyleri, artwork/Figma/kullanıcı/fiziksel/PRD kapıları açık. Aktif hedef tamamlanmadı.

## 0.14.2 önizleme · Başlık dönüşü ve geri bildirim doğrulandı

Kesilen kart kaydırması başlık gözlemcisini artık durdurmuyor; gerçek odak/ekran iptali korunuyor. VisualPreview üç kez ↑↑/↓↓ dönüşü, tam afiş/açıklama ve başlık sınırını denetler. SearchNavigation hata mesajına iki gerçek Up tuşuyla erişir, tam metin sınırlarını ölçer ve aşağı döner; önceki zorla scroll testi kullanıcı davranışını temsil etmiyordu. Normal/1,5/2,0 fontta 12’şer akış geçti (aynı 12 vakanın üç ölçeği; 36 ayrı özellik değildir). 115 JVM başarı +1 özel skip; lint0/32, v2 imza doğrulandı. Son test değişikliği yalnız test APK'sindedir. Önceki başarısız kanıtlar QA tarihçesinde korunur.

[0.14.2 APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.2.apk), SHA-256 ve [QA](native-feedback-settings-qa.json) güncel. Üç fontta 30 görüntü yenilendi; özel loglar heading-regression-*.log, heading-lint-recheck.log. Test emülatörü5556 font1.0'a döndü. Fiziksel TV kullanıcı isteğiyle önceki0.14.1'de; yeni aday otomatik yüklenmedi. Kullanıcı5554 korunur. Figma değişmedi. Eski ZIP güncel kaynak değildir.

**Sonraki iş:** Ayarlardaki bölüm başlığının kaydırma kenarında yarım görünmesini, kaynak/oynatıcı yüzeylerini ve gerçek kapak eşleşmesini aynı tasarım sözleşmesiyle ele al. Kullanıcı görsel onayı, fiziksel akıcılık/codec ve PRD kalite kapıları açık; genel hedef tamamlanmadı.

## TV güncellemesi ve başlık düzeltmesi · 14 Ekim 2026

Kullanıcının açık isteğiyle fiziksel TV 0.2'den **0.14.1-design-preview / code15** sürümüne güncellendi. Mevcut APK ile imza sertifikası ve teslim SHA-256 eşleşmesi doğrulandı; install -r başarılı, yeni sürüm paket yöneticisinden okundu, MainActivity önde ve süreç canlı. Veri silme veya instrumentation yapılmadı. Kayıtlı hesap kurulum istemeden tanındı; ilk açılışta katalog yenilemesi başladı. Son ekran okumasında film oynatıcı sayfasına ulaşıldığı görüldü; bu sırada uzaktan gezinme/oynatma komutu gönderilmedi. Yayın henüz açılıyor durumundaydı, oynatma başarısı onaylanmış sayılmaz. Fiziksel kalite/codec kabulü yapılmış sayılmaz. Önceki “TV 0.2 korunur” kayıtları tarihçedir; kullanıcı 5554 hâlâ korunur. Özel ekran ve eski APK `.private` içinde.

0.14.2 WIP: PilotApp Library kaydırmasının CancellationException ile gözlemciyi durdurması düzeltildi; currentCoroutineContext().ensureActive() gerçek ekran/odak iptalini korur, tek kaydırma kesintisinden sonra gözlem devam eder. Geçici log kaldırıldı. VisualPreviewTest başlık→kart dönüşünü üç kez kontrol eder, movies-return görüntüsü ekler. İlk font2 tek akış geçti; genişletilmiş normal font 12/12 geçti. Font1.5 11/12: SearchNavigationTest.failedRefreshRemainsReadableAndCachedContentStaysReachable satır371 uyarının görünürlüğünde başarısız; test kart odağı dururken performScrollToIndex(0) yapıyor, yeni yerleşme davranışı başlığı geri gizleyebilir. Bunu test hatası varsaymadan gerçek başlık odağıyla erişimi kontrol et. Runner burada durdu ve font1.0'a döndü; genişletilmiş font2 paketi henüz koşmadı. 115 JVM başarı +1 özel skip; lint0/32. Taşımadaki local.properties sürücü ayıracı kaçışı düzeltildi; lint önbelleği eski hatayı tuttuğundan --rerun-tasks ile doğrulandı.

**Sonraki iş:** Uyarı için başlık odağı/gezinme akışını doğrula; gerekiyorsa test/üretim davranışını düzelt. 1.5 ve2.0 paketlerini tamamla, 0.14.2 QA/görsellerini güncelle, sonra ayrı önizleme teslimi üret. Ayarlardaki kaydırma kenarında yarım bölüm başlığı da görsel incelemede görüldü; sıradaki görsel iş. 0.14.2 yayımlanmadı, TV'ye kurulmadı. Mevcut kanıt: `.private/heading-*` logları; `.private/run_heading_regression.py` yeni kökü kullanır ve yalnız5556'da çalışır. Normal font görselleri güncel; 1.5/2.0 eski aday görsellerini yeni test kanıtı sayma.

## Proje kökü ve araçlar arası yapı · 14 Ekim 2026

Taşıma tamamlandı. Bu klasör tek etkin proje kökü; README ana giriş, AGENTS ortak sözleşme, CLAUDE aynı kurala yönlendirme ve [WORKFLOW](WORKFLOW.md) kurulum/araç geçişi rehberidir. Önceki kökün AGENTS dosyası yeni konuma yönlendirir. Kaynaklar, .git ve mevcut değişiklikler korundu; henüz ilk commit yok. Eski teslimler releases altında Git dışında; eski ZIP güncel proje değildir.

**Taşıma doğrulaması:** Belge düzenlemesinden önce 4.027 proje dosyası, 248 teslim ve hedefte önceden bulunan PDF SHA-256 ile birebir doğrulandı. Sonrasında yerel bağlantılar / 17 ekran / 401 değişken kontrolü geçti. Yeni kökten assembleDebug başarıyla tamamlandı (50 sn / 38 görev). Özel kanıtlar `.private/migration-manifest.json` ve `.private/migration-build.log` içinde. SDK/özel eski work alanı yerinde; yerel yollar Git dışında saklı. Cihaz kurulumu veya emülatör testi yapılmadı.

**Sonraki iş değişmedi:** Aşağıdaki 0.14.2 büyük yazı dönüş hatasını gider; son yayımlanmış sürüm 0.14.1. Derleme başarısı bu görsel hatanın çözüldüğü veya genel hedefin tamamlandığı anlamına gelmez.

## Güncel çalışma · Konum taşıma ve 0.14.2 taslağı

Asıl proje kökü artık kullanıcının seçtiği **Desktop/TV** klasörüdür; `apps`, `docs`, `design`, `prototypes`, `tools`, AGENTS ve CLAUDE doğrudan bu köktedir. Eski teslimler `releases/` içindedir. Önceki özel `work` alanı ve SDK yerinde korunur; makine yolları Git dışında `local.properties` ve `.private/local-environment.json` içindedir.

0.14.2 kodunda yenileme mesajları işlem/dikkat türü taşır; başarı yalnız işlem yanında gösterilir, hatalar katalogda kalır. Ayarlar ortak yüzey/odak sistemiyle yenilendi. **Henüz yayımlanmadı:** 115 JVM geçti, 1 özel audit atlandı; lint 0 hata/32 uyarı. Normal font 28, 1,5 font 12 kontrol geçti. 2,0 fontta 11/12; düzeltilen testin tekrarı başlığa çıkıp tekrar karta inince kalan kısmi başlık sorununu yakaladı. [Gerçek QA](native-feedback-settings-qa.json) ve [aday görünüm](../design/FEEDBACK_AND_SETTINGS.md). Son yayımlanmış tasarım sürümü 0.14.1 olarak kalır; hedef aktif.

## Son görsel iyileştirme · 0.14.1 katalog sunumu

**14 Ekim 2026:** Sağlayıcı ayırıcıları otomatik ana sayfa raflarında öne çıkarılmıyor; kütüphanede en sona taşınıyor. Kayıt silinmiyor, arama/favori/geçmiş erişimi korunuyor. Gerçek snapshot'ta 51 canlı / 40 film adayı bulundu; normal INFO kanalları korunuyor. Ana kapak tüm katalogdan seçiliyor; görseli olan içerikler otomatik raflarda öncelikli. Yalnız afişi olan film dikey oranını koruyor; eksik görsel için ortak ikon/başlık görünümü var. [Kurallar ve 15 Android görüntüsü](../design/CATALOG_PRESENTATION.md).

**Doğrulama:** 116 JVM, lint 0 hata / 32 uyarı; normal fontta 19 akış, 1,5 fontta bir sunum akışı, 2,0 fontta 10 sunum/arama/gezinme akışı. [QA](native-catalog-presentation-qa.json) · [0.14.1 önizleme APK](../../releases/IPTV_Player_TV_Design_Preview_0.14.1.apk). Yalnız emulator-5556 kullanıldı; kullanıcı 5554 ve fiziksel TV korunuyor. Figma güncellenmedi. Hedef aktif; yanlış eser/kapak eşleşmesi, yenileme bildiriminin yerleşimi, kalan yüzeyler ve kullanıcı/fiziksel onay açık.

## Son görsel çıktı · Android 0.14 tasarım önizlemesi

**14 Ekim 2026:** Google TV yönü mevcut Android bileşenlerine uygulandı: yatay üst menü, yumuşak yüzeyler, yuvarlak afişler, ortak hizalama ve daha sakin odak görünümü. Ana sayfa, canlı TV, filmler, diziler, ayrıntı, arama ve rehber girişinin [gerçek Android ekranları](../design/NATIVE_VISUAL_REVISION.md) hazır. Roboto korundu. [Ayrı kurulum dosyası](../../releases/IPTV_Player_TV_Design_Preview_0.14.0.apk) · [QA ve sınırlar](native-visual-revision-qa.json).

**Ayrı emulator-5556 üzerinde:** 109 JVM kontrolü; lint 0 hata / 32 uyarı; APK v2 imza doğrulaması. İlk 37 akışın iki sorunu giderildi ve ilgili 11 kontrol geçti. Ardından 1,5 ve 2,0 fontta 17'şer kontrol geçti. Son arama göstergesi/temizleme düzeninden sonra normal ve 2,0 fontta 10'ar arama/gezinme/görsel kontrol yeniden geçti. Bu tekrarlar ayrı test sayısı olarak toplanmaz; eski 77'lik paketin tümü yeniden çalıştırılmış sayılmaz.

Kullanıcı emülatörü **5554 / 0.13**, kayıtlı hesabı ve fiziksel **65U95A / 0.2** korunuyor. TeamViewer için mevcut canlı kumandada sağ yön, OK ve Geri çalışıyor. Figma bu tur güncellenmedi. BKL-020 aktif: kullanıcı görsel onayı, gerçek katalogdaki INFO/ayırıcı kayıtlar ve yanlış kapaklar, kalan yüzeyler, büyük yazıda başlık kenarı ve fiziksel kalite açık. Genel hedef tamamlanmadı.

## Güncel öncelik · Görsel kalite ve kullanıcı simülatörü

**Gezinme kontrolü tekrarlandı:** Kullanıcı sorunun pencere taşıma değil uygulama içinde gezinme olduğunu netleştirdi. Mevcut canlı kumandada fareyle sağ yön → Kanalları aç, OK → Canlı TV, Geri → ana sayfa yeniden doğrulandı. Kumanda sekmesi kullanıcı için açık bırakıldı; kayıtlı kaynaklar ve APK değiştirilmedi.

**14 Ekim 2026:** Kullanıcı yeni özellikler yerine tasarım kalitesine öncelik verdi. Seçilen yön: Google TV'ye yakın yumuşak yüzeyler, yuvarlak kartlar ve ferah düzen. Ana sayfa, canlı TV ve film ekranları önce gerçek görsellerle değerlendirilir; mevcut native ekranlar bu yön için onaylanmış değildir. PIN taslağı üretime bağlanmadan proje dışındaki özel çalışma alanına taşındı. Yeni özellik geliştirmesi bekliyor.

`emulator-5554` artık kullanıcının etkileşimli test cihazıdır; izinli gerçek kaynak ve 0.13 kuruludur. Bu cihazda fixture/reset instrumentation, kaynak temizliği veya izinsiz APK değişimi yapılmaz. Otomatik testler ayrı emülatörde yürütülmelidir. Fiziksel TV değişmedi. TeamViewer üzerinden gezinme için yalnız bu bilgisayarda çalışan geçici canlı ekran/kumanda aracı açıldı; tarayıcı düğmeleriyle sağ yön, OK ile canlı katalog açma ve Geri komutları doğrulandı. Bu araç ürün özelliği veya yeni tasarım değildir; görüntü kısa aralıklarla yenilenir, video performansı ölçümü için kullanılmaz.

Figma kişisel hesap üzerinden yeniden denendi; tasarım bağlamı ve ekran görüntüsü çağrıları Starter MCP kotasına takıldı. Figma güncellenmedi; yerel tasarım çalışması ilerleyebilir.

## Son kontrol · Gerçek sağlayıcı rehberi

**14 Ekim 2026:** İzinli kaynaktan XMLTV ve canlı katalog sırayla alındı; iki HTTP 200, sıfır medya yayını. Android emülatörde mevcut üretim veri yoluyla üç yerel yenileme, tam yeniden okuma, kaynak sınırı ve silme doğrulandı. **10.588 program / 253 farklı kanal** saklandı; 31 geçersiz kayıt atlandı ve 29 tekrar temizlendi. 4.525 canlı katalog satırının 402'si, 252 farklı rehber kanalına eşleşiyor. 48 satırda çakışan şimdiki program kesin bilgi olarak gösterilmiyor. [Araştırma](../research/PROVIDER_EPG_AUDIT.md) · [QA ve süreler](provider-guide-qa.json).

Bu tur üretim APK'si değişmedi: yayımlanan **0.13 aynı SHA-256** değerinde. Yeni opt-in emülatör denetimi 1/1 geçti; eski 109 JVM/77 akış vakası yeniden çalıştırılmış sayılmaz. Testin özel girdileri/rehber kaydı temizlendi. Fiziksel TV, Figma ve görseller değişmedi. Proje ZIP'i yeni test/araştırma/QA belgeleriyle güncellendi.

## Son teslim · 0.13

**14 Ekim 2026 · 0.13.0-pilot:** Native yayın rehberi, canlı kartlarda/oynatıcıda şimdi–sonra, program ayrıntısı, kalıcı saat dilimi ve kaynak başına şifreli XMLTV ayarı eklendi. APK ve proje paketi güncellendi. Fiziksel **65U95A hâlâ 0.2.0**; açılmadı/güncellenmedi. Genel hedef aktif.

- Rehberde dikey hareket zaman konumunu korur; OK şimdiki programı canlı açar, diğer programların ayrıntısını gösterir. Odak tek başına oynatma başlatmaz. Geri aynı hücreye döner; boş/bozuk rehber canlı izlemeyi engellemez.
- Rehber cache'i yeniden açılınca kullanılır; başarısız/iptal yenileme son sağlam bilgiyi korur. Silinen/değişen kaynak gecikmiş sonuç yayımlayamaz. İptal edilen ilk indirme ekran yeniden çizildi diye başlamaz.
- Rehber ayarları önce durum ekranını açar; klavye yalnız adres düzenlerken açılır. Uzun metinler kaydırılır, eylemler sabit kalır. Parlak videoda oynatıcı başlığı koyu yüzeyle okunur.
- **109 JVM ve 77 ilgili emülatör vakasının son sonucu başarılı.** 77'lik turdaki eski menü adımı sayan tek test düzeltildi;9 arama/menü vakası tekrar geçti. 16 gezinme/yerleşim vakası1.5 ve2.0 fontta ayrıca geçti. Lint0 hata/32 uyarı; APK v2 imza doğrulandı. [QA ve kapsam](native-guide-qa.json).
- 41 yeni Native013 görsel:14 genel ekran, üç fontta27 rehber yüzeyi. Logolar/afişler gerçek, program çizelgesi ve oynatma test verisidir. Görseller Figma'ya aktarılmadı.
- Aralıklı rehber takılması bulundu: SQLite işlemi askıya alındıktan sonra başka fiziksel thread'e geçebiliyordu. Parse/seal/rollback aynı GuideWriter üzerinde, ağ iptali ayrı IO gözlemcisinde yürür. [EPG sözleşmesi](../architecture/EPG.md), D039.

## Açık kalite ve ürün işleri

Bir sağlayıcının metadata uyumluluğu/eşleşmesi doğrulandı; diğer sağlayıcılar, gerçek TV’de rehberden oynatma ve büyük rehberin fiziksel performansı, ortak PIN ve kalan SYS01; katalog açılışı/ilk arama optimizasyonu; tüm ekran font/odak matrisi, HDR/codec/5.1/iki saat oynatma ve beş kişilik tur açık. [Kalite kapıları](QUALITY_AUDIT.md). BKL-011/013/027 done değildir.

0.12 arama/görsel cache tanı kaydı [önceki QA](native-performance-qa.json) içinde korunur;60bin kayıt restore2,38–2,63s/ilk sorgu823ms ölçümü fiziksel p95 kabulü değildir. QR/web kurulum öneri aşamasında. Figma kota engeli yeniden kontrol edilmedi. Genel test paketi fiziksel TV'de çalıştırılmaz; sağlayıcıda ikinci paralel yayın açılmaz.
