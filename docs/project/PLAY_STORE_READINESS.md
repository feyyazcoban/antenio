# Google Play hazırlığı ve hukuki riskler (0.14.42)

Tarih: 18.10.2026. Kapsam: depo taraması (0.14.42) + Google Play/Android belgeleri + Türk, AB ve ABD hukuku üzerine masa başı araştırma.

**Bu belge hukuki tavsiye değildir; risk analizidir.** Bağlayıcı kararlar için avukat, marka vekili ve mali müşavir gerekir. Buradaki öneriler (ad değişikliği, paket adı, PIN, imza anahtarı, metinler) **kullanıcı tarafından onaylanmış karar değildir**; karar verilince DECISIONS'a yazılır.

## 1. Özet

Uygulama teknik olarak Play'e yakın: hedef API 36, minSdk 26, leanback manifesti, banner, yalnız normal izinler, yedekleme kapalı, 16 KB hizalama doğrulandı, kodda kimlik bilgisi loglayan satır yok. Yayını **engelleyen** dört iş ve **başımıza iş açabilecek** dört risk var:

| | Konu | Durum | Kim |
|---|---|---|---|
| Engel | Release paketi debug anahtarıyla imzalanıyor; Play kabul etmez | `app/build.gradle.kts` | Siz (anahtar) + kod |
| Engel | Gizlilik politikası URL'si, Data safety, IARC, hedef yaş, TV görselleri, App access test kaynağı | Hiçbiri yok | Siz + kod (uygulama içi bağlantı) |
| Engel | AAB ile yükleme, Play App Signing | AAB görevi kullanılmadı | Kod |
| Engel | Bireysel hesapta 12 test kullanıcısı × 14 gün kapalı test (TV cihazlı) | Başlamadı | Siz |
| Risk: yüksek | **Ad:** aynı kategoride, aynı mağazada "NexTv IPTV player" (DCS apps, `com.dcsapp.iptv`) ve iOS'ta "NexTv" var | D066 ile ad NEXTV | **Marka vekili** |
| Risk: orta | Mağaza dili ve görselleri: gerçek kanal logosu, film afişi, "ücretsiz kanal" ifadesi ya da örnek liste kaldırılma sebebi | Henüz mağaza kaydı yok | Siz |
| Risk: orta | Açılış sesinin lisans belgesi depoda yok (D062: kullanıcı beyanı) | `res/raw/splash_chime.m4a` | Siz |
| Risk: orta | Açık kaynak lisans ekranı yok (Apache 2.0, paketlenen `roboto.ttf` için OFL 1.1) | Hakkında'da yok | Kod |

## 2. Play'e çıkışı engelleyenler

### 2.1 İmza, AAB, paket adı

- **Debug imza:** `release { signingConfig = signingConfigs.getByName("debug") }`. Play debug sertifikalı paketi reddeder. Kalıcı bir upload anahtarı (en az 2048 bit RSA, geçerliliği 2033 sonrasına uzanan, önerilen 25 yıl) üretilmeli; anahtar ve parolası **depoya girmez** (`keystore.properties` veya ortam değişkeni, `.gitignore`).
- **Kurulu cihazlar:** Bugün TV'de debug imzalı sürüm var. Play sürümü farklı imza taşıyacağından onu güncelleyemez; bir kez kaldırıp kurmak gerekir ve yerel veriler (kaynaklar, favoriler, izleme geçmişi) gider. "Veri silmeden kur" alışkanlığı bu geçişte bir kez bozulur; geçişten önce kaynak bilgilerini kullanıcı kendisi not etmeli.
- **Play App Signing** ilk yayında zorunlu; Google uygulama imzalama anahtarını üretirse Play dışında dağıtılan APK aynı imzayı taşıyamaz. Play dışı APK dağıtımı sürecekse bu seçim önceden yapılmalı (Console'da kendi anahtarını yükleme seçeneği doğrulanmadı).
- **AAB:** TV uygulamaları için zorunlu (TV-G1). `bundleRelease` görevine geçilmeli.
- **Paket adı:** `dev.iptvplayer.tv.pilot` ilk yüklemeden sonra değiştirilemez; değişirse Play yeni uygulama sayar. Adında "iptvplayer" ve "pilot" var. Kalıcı ad ilk yüklemeden önce seçilmeli (karar sizin). İmza zaten değişeceği için yeni bir paket adı ek veri kaybı getirmez.

### 2.2 Mağaza ve Console beyanları

- **Gizlilik politikası:** Hiç veri toplamayan uygulamada da zorunlu. Herkese açık, PDF olmayan bir URL'de; hem Console'da hem uygulama içinde (öneri: Ayarlar → Uygulama hakkında). İçermesi gerekenler: geliştirici adı; uygulamanın kullanıcının girdiği sunuculara (sağlayıcı, rehber, logo ve afiş adresleri) doğrudan bağlandığı ve bu tarafların IP adresini görebileceği; geliştiricinin veri almadığı; kimlik bilgilerinin cihazda şifreli tutulduğu ve yedeğe gitmediği; silme yolu (Kaynağı kaldır / Geçmişi temizle); destek e-postalarının nasıl işlendiği; iletişim adresi.
- **Data safety:** "Veri toplanmıyor" beyanı savunulabilir (veri cihazdan geliştiriciye gitmiyor, analitik veya üçüncü taraf SDK yok). Gri alan: kimlik bilgileri kullanıcının seçtiği sunucuya gidiyor. Beyanda ve politikada bu açıkça anlatılmalı.
- **İçerik derecelendirmesi (IARC):** Zorunlu; derecesiz uygulama kaldırılır. Uygulamanın kendi içeriğine göre, doğru doldurulmalı. Uygulama çocuklara yönelik işaretlenmemeli.
- **Hedef yaş grubu** beyanı her sürümde zorunlu.
- **TV'ye katılım:** Console'da Android TV form faktörü eklenir, TV inceleme politikası kabul edilir. Gerekenler: 1280×720 TV banner'ı (JPEG ya da alfasız 24-bit PNG), en az bir değiştirilmemiş TV ekran görüntüsü (TV-G4), açıklamada "Android TV" ifadesi. TV paketi onaylanmazsa mağaza kaydı güncellemeleri takılabilir.
- **App access (TV-G5):** İnceleyicinin uygulamayı işler görmesi için Console'a çalışan ve **yasal** bir test kaynağı girilmeli: kendi yayınınız ya da açık lisanslı içerikten oluşan M3U/Xtream hesabı. Boş açılan bir oynatıcı Functionality politikasına takılabilir. Gerçek sağlayıcı hesabı buraya girilmez.

### 2.3 Geliştirici hesabı

- 25 USD, 18 yaş, kimlik doğrulama. Kuruluş hesabı D-U-N-S ister.
- 13.11.2023 sonrası açılan **bireysel** hesapta üretime çıkmak için kapalı testte en az **12 test kullanıcısı 14 gün kesintisiz** kayıtlı kalmalı; ardından üretim başvurusu (genelde 7 gün). Uygulama yalnız TV'de çalıştığından test kullanıcılarının Android TV / Google TV cihazı gerekebilir (çıkarım).
- Bireysel hesapta e-posta mağazada görünür; gelir elde edilirse AB'de "tacir" sayılıp adres gösterilebilir (Google için ikincil kaynak). Ev adresi görünmesin istiyorsanız şirket hesabı değerlendirilmeli.

## 3. "Başımıza iş açmasın" uyarıları

### 3.1 Ad ve marka (en yüksek risk)

- NEXTV ile NexTv okunuşta aynı, ürün aynı (IPTV oynatıcı), kanal aynı (Google Play, Android TV). SMK 6769 m.6 ve m.7 açısından karıştırılma ihtimalinin en yüksek olduğu birleşim. Rakip tescilsiz olsa bile önceki kullanım (m.6/3), TTK haksız rekabet ve Play'in taklit/marka politikası üzerinden şikâyet edebilir. Google adı yayından önce denetlemez; şikâyet gelince kaldırır.
- Öneri: yayından önce marka vekiline tam araştırma (TÜRKPATENT, TMview, EUIPO, WIPO, USPTO, mağazalar, alan adları). Büyük olasılıkla yeni ad; ad kalacaksa rakiple birlikte var olma anlaşması. Ardından sınıf 9 ve 42'de tescil (38/41 savunma; 41'deki geniş "eğlence" ifadesi içerik sağlayıcı izlenimi verebilir).
- Uygulamanın adı D066'da kullanıcı kararıyla NEXTV oldu; değiştirme kararı yine sizin.

### 3.2 Telif ve "korsana yardım" algısı

Kural: içerik sunmayan, tarafsız pazarlanan bir oynatıcı Türkiye'de (5651 m.4/2, FSEK m.71–72), AB'de (Filmspeler C-527/15, GS Media, Ziggo, YouTube/Cyando) ve ABD'de (Grokster) sorumluluk çizgisinin dışında kalır. Çizgiyi aşmanın yolu **sunuş biçimi**dir: içeriği benimsemek, teşvik etmek, satıcıyla iş yapmak. Yazılı ret beyanı pazarlama korsanlığa işaret ediyorsa korumaz.

Yapılmayacaklar:
- Varsayılan, gömülü ya da "örnek" M3U listesi; "önerilen sağlayıcı" bağlantısı.
- "Ücretsiz kanal / maç / beIN", "abonelik gerekmez", "şifreli kanalları açar" gibi ifadeler (FSEK m.72 teknik önlemi aşma ürünlerinin pazarlamasını da suç sayar).
- IPTV satıcılarıyla ortaklık, satış komisyonu, beyaz etiket sürüm, satıcıya "sunucu adresi gönderme" (DNS push) ya da aktivasyon kodu modeli.
- DRM veya şifre kırma.
- Mağaza ekran görüntülerinde gerçek kanal logosu, EPG'den gelen gerçek programlar, film/dizi afişi, spor logosu. Perfect Player 2019'da tek bir yayıncı şikâyeti ve "kanallarımız oynuyor" görüntüsüyle kaldırılmıştı.
- Uygulama adında, mağaza başlığında ve anahtar kelimelerde "Xtream Codes" (ad 2019 Avrupa çapındaki operasyonla korsan ekosistemiyle özdeşleşti).

Yapılacaklar:
- Tarafsız dil: "Kendi yasal kaynağını oynat; uygulama içerik sağlamaz." Uygulamada "Uygulama içerik sağlamaz" cümlesi Ayarlar'da zaten var (`ui/SettingsScreen.kt`).
- Görseller sentetik içerikle: "Kanal 1", "Haber 24", kendi ürettiğiniz ya da lisansı açık afişler (AGENTS.md'deki sentetik örnek kuralı bunu zaten şart koşuyor).
- Telif ve marka şikâyetleri için ayrı bir e-posta; gelen bildirimi kaydetme ve yanıtlama süreci (FSEK Ek-4 uyar-kaldır; web sitesi ve mağaza sayfası "içerik" sayılır).
- İçerik sağlamadığınızı gösteren kanıt dosyası (kod, mağaza metinleri, sürüm notları).

Depodaki durum:
- Kurulum ekranındaki düğmede, kaynak türü etiketinde ve Ayarlar notunda "Xtream Codes" geçiyor (`ui/PilotApp.kt`, `ui/SourcesScreen.kt`, `ui/SettingsScreen.kt`). Uygulama içinde tanımlayıcı kullanım düşük risklidir; avukat önerisi "Xtream API uyumlu giriş" gibi tanımlayıcı bir ifade. Değişiklik kararı sizin.

### 3.3 Varlıkların kaynağı

- **Açılış sesi:** "Telifsiz" ifadesi lisanssız kullanım demek değildir; müzik eseri ve ses kaydı ayrı haklardır. D062'ye göre sesi kullanıcı seçti, lisans belgesi depoda yok. Belge bulunamazsa ses değiştirilmeli.
- **Logo:** Bir tasarımcı yaptıysa FSEK m.52 gereği yazılı ve hakları tek tek sayan devir alınmalı; yapay zekâ ile üretildiyse aracın kullanım şartları saklanmalı. Marka tescili logoyu korumanın asıl yoludur.
- Her varlık için kaynak, tarih, lisans ve fatura içeren bir kanıt klasörü tutulmalı (depo dışında).

### 3.4 Açık kaynak lisansları

- AndroidX, Compose, Media3, OkHttp, Coil: Apache 2.0, lisans kopyası ve varsa NOTICE atıfları kullanıcıya gösterilmeli.
- `res/font/roboto.ttf` pakette: Roboto bugün **OFL 1.1** ile dağıtılıyor; telif bildirimi ve lisans metni uygulamada görünür olmalı.
- Öneri: Ayarlar → Uygulama hakkında → "Açık kaynak lisansları" sayfası. FFmpeg eklentisi kullanılmıyor (LGPL/GPL yükü yok).

### 3.5 Yetişkin içerik ve çocuk koruma

- Play cinsel içerik tanıtan uygulamalara izin vermez; katalog uygulamaları için istisna, bu içeriğin küçük bir kısım olması, **tanıtılmaması** ve reşit olmayanların erişiminin kısıtlanması şartına bağlı.
- Depodaki durum: sağlayıcının yetişkin işaretli (`is_adult`) içerikleri hiç gösterilmiyor; adı yetişkin içerik bildiren kategoriler varsayılan olarak gizli ve Ana Sayfa, kütüphane, arama, rehber ve kanal numaralarından çıkıyor (D068). Vitrin bu filtrelenmiş katalogdan seçiliyor.
- Eksik: filtre bir anahtardır, PIN değil; kumandası olan herkes açabilir. PIN altyapısı (`PinCredential`, `PinAttempts`, `AccessPolicy`) var ama arayüze bağlı değil (BKL-028). Önerilen: yetişkin filtresini kapatmak ve gizli kategori açmak PIN istesin; PIN ilk kurulumda belirlensin.
- TCK m.226 kast arar; varsayılan gizleme iyi bir savunmadır. 7578 sayılı Kanun (01.11.2026) sosyal ağ ve oyun platformları içindir, NEXTV kapsamda görünmüyor.

### 3.6 Şifresiz HTTP ve kimlik bilgileri

- Manifestte `usesCleartextTraffic="true"` tüm uygulama için açık. Play bunu doğrudan yasaklamıyor, ama User Data politikası kimlik bilgilerinin modern kriptografiyle iletilmesini bekliyor; Xtream kullanıcı adı ve parolası HTTP'de URL içinde açık gider.
- Sunucuları kullanıcı belirlediği için alan adı istisnası pratik değil. Öneriler: HTTPS'i öne almak, HTTP kaynak eklenirken kısa bir uyarı, durumu gizlilik politikasında anlatmak.
- İyi olanlar: kimlik bilgileri cihazda şifreli; `allowBackup="false"`, `fullBackupContent="false"` ve `data_extraction_rules` ile yedeğe gitmiyor; kodda `Log.*` çağrısı yok.

### 3.7 RTÜK, KVKK/GDPR, tüketici, vergi

- **RTÜK:** NEXTV içerik seçmiyor, iletmiyor, hizmet sunmuyor; büyük olasılıkla yayın lisansı ya da platform iletim yetkisi kapsamı dışında. Uygulama içi kanal paketi, abonelik satışı, sağlayıcı ortaklığı ya da kendi sunucunuzdan proxy/transcode eklenirse kapsam değişebilir.
- **KVKK/GDPR:** Veri cihazdan çıkmadığı sürece geliştirici veri işlemiyor sayılabilir (yorum; Kurul kararı yok). Destek e-postası (kullanıcı ekran görüntüsünde adres ve parola gönderebilir), web sitesi kayıtları ve ileride eklenecek çökme raporu ya da senkronizasyon başladığı anda aydınlatma metni, hukuki sebep ve yurt dışı aktarım kuralları (Firebase, Gmail; standart sözleşme 5 iş günü içinde Kurum'a bildirilir) devreye girer. Çökme raporu eklenirse URL'deki kimlik bilgileri maskelenmeli. VERBİS: tek geliştirici büyük olasılıkla muaf.
- **Ücretli özellik eklenirse:** mesafeli sözleşme ön bilgilendirmesi, cayma istisnası onayı, 6502 m.52/3 (onaysız otomatik yenileme) ile Play aboneliğinin ilişkisi avukata sorulmalı.
- **Kullanım koşulları:** "yalnız oynatıcı", "kullanıcı eklediği kaynağın yasal olduğunu beyan eder", üçüncü taraf hizmetler, garanti reddi, sorumluluk sınırı (ağır kusur hariç, TBK m.115), telif bildirim adresi, Türk hukuku (tüketicinin emredici korumaları saklı).
- **Vergi:** GVK mükerrer 20/B istisnası "akıllı telefon veya tablet gibi mobil cihazlar" diyor; yalnız TV'de çalışan uygulamanın kapsamı belirsiz, GİB'den özelge alınmalı. Özel banka hesabı ve %15 tevkifat şartları var.

## 4. Android TV kalite kriterleri

| Kriter | Durum | Not |
|---|---|---|
| TV-ML LEANBACK_LAUNCHER | Var | |
| TV-MT dokunmatik zorunlu değil | Var | |
| TV-LB / TV-BN banner, ad içerir | Var | `app_banner.xml` yatay NEXTV logosu; mağaza için ayrıca 1280×720 PNG gerekir |
| TV-PS minSdk ≤ 31 | Var (26) | |
| TV-DP her işlev D-pad ile | Var | Uzun basma kısayoldur; aynı işlevler ayrıntı sayfasında da var |
| TV-DM Menü tuşuna bağımlılık yok | Var | Menü tuşu kullanılmıyor |
| TV-DB Geri sonunda ana ekrana döner | Var | Çıkışta onay soruluyor, ikinci Geri çıkar |
| TV-NP çıkınca duraklat | Var | `PlayerScreen` ON_STOP |
| TV-PC orta tuş oynat/duraklat, sol/sağ sarma | Var | D064: sağ/sol 10/30/60 sn |
| TV-PP medya tuşu | Var | Fiziksel kumandada tekrar denenmeli |
| TV-ME düşük bellekli cihaz | Ölçülmedi | 63 bin kayıtlı katalogla düşük RAM'li cihazda deneme yok |
| TV-G4 / TV-G5 ekran görüntüsü, test erişimi | Yok | Bkz. 2.2 |
| 64-bit ve 16 KB | Var | Tek `.so` (`libandroidx.graphics.path.so`) 16 KB hizalı doğrulandı |
| Hedef API (TV için 34) | Var (36) | |

Pre-launch raporu TV cihazında çalışmaz; TV testleri bizim sorumluluğumuzda.

## 5. Önerilen sıra

| # | Öncelik | Eylem | Kim |
|---|---|---|---|
| 1 | P0 | Ad kararı: tam benzerlik araştırması, gerekirse yeni ad, ardından tescil başvurusu | Marka vekili + siz |
| 2 | P0 | Kalıcı paket adı ve upload anahtarı; Play App Signing seçimi; `bundleRelease` | Siz (karar, anahtar) + kod |
| 3 | P0 | Gizlilik politikası ve kullanım koşulları (TR/EN), telif bildirim adresi | Siz taslak, avukat inceler |
| 4 | P0 | Ses lisans belgesi ya da yeni ses; logo devri; kanıt klasörü | Siz (+ avukat) |
| 5 | P0 | Uygulama içinde lisans sayfası ve gizlilik politikası bağlantısı | Kod |
| 6 | P0 | Yetişkin filtresi ve gizli kategoriler için PIN (BKL-028) | Kod (karar sizin) |
| 7 | P0 | Mağaza metni ve sentetik görseller; yasal test kaynağı (App access) | Siz + kod (görseller) |
| 8 | P0 | Geliştirici hesabı, kimlik doğrulama, 12 test kullanıcısı × 14 gün | Siz |
| 9 | P1 | HTTP kaynakta uyarı; "Xtream Codes" etiketinin tanımlayıcı ifadeye dönmesi | Kod (karar sizin) |
| 10 | P1 | Düşük bellekli TV'de büyük katalog denemesi (TV-ME) | Cihaz testi |
| 11 | P1 | Vergi: 20/B özelgesi, hesap türü, AB tacir adresi | Mali müşavir |
| 12 | P2 | Ücretli özellik, çökme raporu ya da senkronizasyon gelirse KVKK/GDPR ve tüketici işleri | Avukat |

## 6. Doğrulanamayanlar

- IPTV oynatıcılara özel bir Play uygulama pratiği (resmî sayfada yok; yalnız basında örnekler).
- Rakip "NexTv"nin TR, AB, ABD tescilleri; "Xtream Codes" adının tescil durumu.
- Console'da kendi uygulama imzalama anahtarını yükleme seçeneğinin sürüp sürmediği.
- IARC anketinin güncel soru metni; TV incelemesinin süresi.
- Bireysel hesapta adresin herkese açık görünüp görünmediği.
- İçerik sunmayan bir oynatıcı hakkında Türk mahkeme kararı; RTÜK'ün bu istemcilere ilişkin açıklaması.
- 7590 ve 7578 sayılı Kanunların resmî metinleri, AYM–TFF kararının resmî metni (yalnız haber kaynakları).
- 20/B istisnasının TV uygulamasını kapsayıp kapsamadığı.

## 7. Kaynaklar

Google Play ve Android:
- Fikri mülkiyet: https://support.google.com/googleplay/android-developer/answer/9888072
- Taklit: https://support.google.com/googleplay/android-developer/answer/9888689
- Metadata: https://support.google.com/googleplay/android-developer/answer/9898842
- Hedef API: https://developer.android.com/google/play/requirements/target-sdk
- 16 KB sayfa: https://developer.android.com/guide/practices/page-sizes
- Uygulama imzalama: https://developer.android.com/studio/publish/app-signing
- TV kalite yönergeleri: https://developer.android.com/docs/quality-guidelines/tv-app-quality
- TV dağıtımı: https://developer.android.com/training/tv/publishing/distribute
- Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- User Data: https://support.google.com/googleplay/android-developer/answer/10144311
- İçerik derecelendirmesi: https://support.google.com/googleplay/android-developer/answer/9898843
- Cinsel içerik: https://support.google.com/googleplay/android-developer/answer/9878810
- Kapalı test şartı: https://support.google.com/googleplay/android-developer/answer/14151465
- Geliştirici doğrulaması: https://developer.android.com/developer-verification
- Şifresiz trafik: https://developer.android.com/privacy-and-security/risks/cleartext-communications

Türk mevzuatı (mevzuat.gov.tr): FSEK 5846, 5651, KVKK 6698, SMK 6769, TKHK 6502, MÖHUK 5718, GVK 193.

AB ve ABD içtihadı:
- Filmspeler C-527/15: https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:62015CJ0527
- GS Media C-160/15: https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:62015CJ0160
- Stichting Brein / Ziggo C-610/15: https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:62015CJ0610
- YouTube/Cyando C-682/18: https://curia.europa.eu/jcms/jcms/p1_3562062/da
- MGM v. Grokster, 545 U.S. 913 (2005)

Piyasa ve marka:
- Perfect Player'ın kaldırılması: https://torrentfreak.com/google-play-removes-perfect-player-after-bogus-copyright-complaint-191019/
- NexTv (iOS): https://apps.apple.com/app/nextv/id1628393213
- NexTv IPTV player (Android TV): https://www.apkmirror.com/apk/dcs-apps/nextv-iptv-player-2/
- TÜRKPATENT araştırma: https://www.turkpatent.gov.tr/arastirma-yap
- Roboto lisansı (OFL 1.1): https://openfontlicense.org/open-font-license-official-text/
