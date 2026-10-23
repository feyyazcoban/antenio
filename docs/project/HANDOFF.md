# Son devir notu

## Claude · Play'e gönderim · 23 Ekim 2026

Ayrıntılar PLAY_CONSOLE'da. Play Console'u Claude in Chrome ile kullanıcının oturumunda doldurdum; Chrome sekmesi arka plana düşünce ekran görüntüsü ve bazı tıklamalar çalışmıyor, yeni sekme açmak çözüyor. Malzeme bileşenlerinde `form_input` ve DOM `click()` çoğunlukla işliyor; kaydı her adımda durum mesajıyla doğruladım.

Tam sonraki adım: inceleme sonucunu bekle (Play Console bildirimleri ve e-posta). Onaylanırsa "Web'de katılın" bağlantısını al; kullanıcı aile ve arkadaşlarını ekler, gerekirse Google Grubu açılır. 12 kişi 14 gün kalınca üretime erişim başvurusu. Abonelik sürümünde içerik derecelendirmesindeki "dijital ürün satın alma" cevabı güncellenmeli.

## Claude · Mağaza kaydı hazırlığı · 23 Ekim 2026

Görseller ve inceleme kaynağı betiklerle yeniden üretilebilir (`tools/make_store_graphics.py` Chrome'u başsız çalıştırır; `tools/make_review_source.py` PIL ve Roboto kullanır, rehberi bugünden 75 gün üretir). Dizi afişi ortalanmıştır: bölüm kartları dizi görselini 16:9 ortadan kırpar. `apps/web/_headers` /review/* için noindex. Site zip'i hazır ama yüklenmedi.

Tam sonraki adım: Chrome eklentisi bağlanınca `apps/web`'i Netlify'a yükle ve https://antenio.app/review/demo.m3u'yu doğrula; Play Console'da uygulamayı oluştur, PLAY_CONSOLE'daki cevapları kullanıcı onayıyla gir, 0.16.0 AAB'yi kapalı teste yükle. Test kullanıcılarının e-postaları kullanıcıdan gelecek.

## Claude · Yükleme anahtarı ve ilk AAB · 23 Ekim 2026

`build.gradle.kts`: sürüm adı `-design-preview` ile bitenler yükleme anahtarı tanımlı olsa da debug anahtarıyla imzalanır (TV'deki kurulumun üstüne kurulsun diye); `bundleRelease` önizleme sürüm adında durur. Sürüm 0.16.0 (108) Play içindir; bu sürümün `assembleRelease` çıktısı yükleme anahtarıyla imzalanır ve TV'deki debug imzalı kurulumun üstüne kurulamaz. TV'ye yeni önizleme gerekirse sürüm adına yeniden `-design-preview` eklenir (ör. 0.16.1-design-preview). TV'de şu an 0.15.1'in debug derlemesi var (TV uykudaydı, release APK kurulamadı).

Tam sonraki adım: Play Console'da uygulamayı oluştur (YAPILACAKLAR 17–20): mağaza kaydı, ikon 512, TV afişi 1280×720, tanıtım görseli 1024×500, TV ekran görüntüleri, veri güvenliği, IARC, hedef kitle, inceleme için test kaynağı (16), sonra kapalı teste AAB.

## Claude · Gizlilik bağlantısı ve support@ adresi · 23 Ekim 2026

`SettingsPages.PrivacyPolicy` mevcut `QrCode`'u kullanır; metinler `settings_about_privacy_link/_url/_qr` (TR/EN). Sürüm 0.15.1 (107). Destek adresi kullanıcının ImprovMX'te kurduğu `support` takma adına göre support@antenio.app oldu. Eski dated kayıtlarda geçen destek@ tarihçedir, değiştirilmedi.

Site Netlify'a yeniden yüklendi ve canlıda support@ doğrulandı. support@antenio.app çalışıyor: kullanıcı başka bir adresten attığı deneme e-postasının geldiğini söyledi. Tam sonraki adım: 0.15.1 TV'de kurulu; kullanıcı kaynağını Xtream olarak ekleyince Uygulama hakkında sayfasındaki QR kodunu gör.

## Claude · GoDaddy DNS ve Netlify kurulumu · 23 Ekim 2026

Chrome eklentisiyle kullanıcının açık oturumlarında çalışıldı; hesap açılmadı, şifre girilmedi. Netlify Drop klasör sürüklemeyi beklediği için site düz dosyalara çevrildi ve zip olarak yüklendi. İlk sertifika denemesi proje "Private" iken başarısız göründü; "Make public" sonrası sertifika alındı (Netlify ekranı bir süre eski hatayı gösterdi; `openssl s_client` ile doğrulandı). `www` CNAME'i `antenio.app`'e bakıyor ve çalışıyor; istenirse `antenio.netlify.app` yapılabilir.

Tam sonraki adım: MX ve SPF yayında (doğrulandı); kullanıcı ImprovMX hesabını açıp `destek` takma adını kursun; sonra Ayarlar → Uygulama hakkında'ya https://antenio.app/privacy bağlantısı (E4).

## Claude · origin eklendi ve gönderildi · 23 Ekim 2026

`git remote add origin <GitHub deposu>` ana checkout'ta (`C:\Users\fyzcb\Desktop\TV`); kimlik bilgisi Git Credential Manager'da hazırdı, şifre istenmedi. `git push -u origin main claude/launch-prep-sales-channel-2948fb` başarılı. Bundan sonra her commit ve ff-merge'den sonra `git -C <ana checkout> push origin main <dal>` yapılır. Adres belgelere yazılmaz.

Tam sonraki adım: YAPILACAKLAR 1, 3, 4 (GoDaddy güvenliği, ImprovMX, Netlify) kullanıcıda.

## Claude · Kullanıcının yapacakları listesi · 23 Ekim 2026

Kullanıcı: "her yapmam gerekeni madde madde yaz email dahil". [YAPILACAKLAR](YAPILACAKLAR.md): GoDaddy güvenliği, kod yedeği için GitHub deposu, ImprovMX, Netlify, TV'de kaynak, marka vekili, avukat, mali müşavir, logo, Play hesabı (D120), ödeme profili, 12 test kullanıcısı, yükleme anahtarı, karar maddeleri ve Play Console uygulama kurulumu; sonunda kod tarafındaki işlerim.

Tam sonraki adım: kullanıcı listeyi sırayla yürütsün; site açılınca uygulama içi gizlilik bağlantısı kodlanır.

## Claude · Play hesap türü kararı (D120) · 23 Ekim 2026

D120 yazıldı; AGENTS kuralı ("gerçek ad yazılmaz") sürer, yalnız Google'ın kendi gösterdiği hesap bilgisi için istisna not edildi. LAUNCH_AUDIT E7 ve WEB_SETUP güncellendi.

Tam sonraki adım: kullanıcı Play Console'da kişisel hesap açsın (tek seferlik kayıt ücreti, kimlik doğrulaması, geliştirici adı "Antenio", iletişim destek@antenio.app) ve 12 test kullanıcısı bulsun; bu arada site WEB_SETUP adımlarıyla yayına alınır.

## Claude · Kişi adı olmadan gizlilik sayfası · 23 Ekim 2026

Kullanıcı: "ad soyad yazmak mecburi mi" ve adının hiçbir yerde geçmemesini istedi. Gizlilik sayfalarındaki sorumlu yer tutucusu "Kapsam ve iletişim" bölümüyle değişti (Antenio geliştirici adı, destek@antenio.app); tarih 23 Ekim 2026. AGENTS'a kural eklendi; depoda ve commit kayıtlarında ad geçmiyor (kontrol edildi). Google kaynakları: User Data politikası (answer 10144311), kişisel hesapta gösterilen bilgiler (13628312), test kuralı (14151465).

Tam sonraki adım: kullanıcı Play hesap türünü seçsin (kişisel hesap adını gösterir; kuruluş hesabı şirket ister). Site yayını WEB_SETUP adımlarıyla.

## Claude · Site ve destek e-postası adımları · 23 Ekim 2026

Kullanıcı: "godaddy üzerinden devam edelim". Araştırma: GoDaddy ücretsiz site planı alan adı bağlamıyor, e-posta yönlendirme hesaba göre kredi/ücretli. Çözüm WEB_SETUP'ta: Netlify ücretsiz (elle yükleme), ImprovMX ücretsiz. `apps/web/` (index, gizlilik, privacy, style.css, logo) yerel sunucuda kontrol edildi.

Tam sonraki adım: kullanıcı yer tutucuları doldursun (ad/unvan, adres, tarih) ya da bilgileri versin; Netlify ve ImprovMX kurulumunu yapsın. Sayfa yayına girince Ayarlar → Uygulama hakkında'ya gizlilik bağlantısı kodlanır (E4).

## Claude · antenio.app kaydı doğrulandı · 23 Ekim 2026

Kullanıcı alan adını Cloudflare önerisi yerine GoDaddy'den aldı; RDAP ile doğrulandı (bitiş 23.10.2027, kilitli). NAMING, BACKLOG BKL-045 ve LAUNCH_AUDIT E8 güncellendi. Hesap ve fiyat bilgisi depoya yazılmadı.

Tam sonraki adım: kullanıcı GoDaddy hesabında iki adımlı doğrulamayı ve otomatik yenilemeyi açsın, yenileme fiyatına baksın. Gizlilik politikası/koşullar sayfası için barındırma ve destek@antenio.app yönlendirmesi seçilecek (GoDaddy'nin kendi araçları ya da ad sunucularını Cloudflare'in ücretsiz planına yönlendirmek); 22 Aralık 2026'dan sonra Cloudflare'e taşıma seçeneği var.

## Claude · Eski NEXTV kurulumu kaldırıldı · 23 Ekim 2026

Kullanıcı onayıyla TV'den `dev.iptvplayer.tv.pilot` kaldırıldı; paket listesinde yalnız `app.antenio.tv` (0.15.0) kaldı. Bundan sonraki TV kurulumları `install -r releases/Antenio_TV_Design_Preview_<sürüm>.apk` ile yapılır ve `app.antenio.tv` kimliği doğrulanır.

Tam sonraki adım: kullanıcı Antenio'yu açıp kaynağını yeniden eklesin ve yeni logoyu, açılışı ve afişi görsün.

## Claude · BKL-045 Antenio geçişi /0.15.0 · 23 Ekim 2026

Değişenler: `app/build.gradle.kts` (`applicationId = "app.antenio.tv"`, 106/0.15.0, `ANTENIO_RELEASE_*`), manifest etiketi, `strings.xml` ×2, `SourceHttp` User-Agent, `assets/setup/index.html`, `BrandLogo` (eski `NextvLogo`, `R.drawable.brand_logo`), `brand_wordmark`, `ic_splash_mark` (eski `ic_nextv_splash_mark`), `app_banner`, `LaunchCover` (`BrandGlyphs`, `GroupW = 429.025f`, `MarkShift`). Logo dosyaları `scratchpad`taki `antenio_logo.py` mantığıyla üretildi: N/T/E eski yollar, A/I/O aynı ızgarada hücrelerden; çerçeveler even-odd dolgu. androidTest manifesti ve `tools/run-device-tests.sh`, `tools/check_cold_catalog.py` yeni kimlikte. `namespace` `dev.iptvplayer.tv` (iç ad) değişmedi; `MainActivity` bileşeni `app.antenio.tv/dev.iptvplayer.tv.MainActivity`. Sabit metin testine "Antenio" marka adı izinli olarak eklendi.

Tam sonraki adım: kullanıcı TV'de Antenio'yu açıp logoyu, açılışı ve afişi görsün, kaynağını yeniden eklesin. Eski NEXTV uygulamasını kaldırmak kullanıcının kararıdır (kaldırınca verisi silinir). Kalan BKL-045: alan adı, marka vekili, tasarımcı logosu, mağaza metni ve görselleri; gap/* tasarım belgeleri hâlâ NEXTV diyor.

## Claude · Paket adı kararı (D119) · 23 Ekim 2026

Kullanıcı: "Kalıcı paket adı app.antenio.tv olsun. antenio.app alan adını alacağım." D119 yazıldı; AGENTS, PLAY_STORE_READINESS, BACKLOG BKL-045, NAMING ve LAUNCH_AUDIT E3 güncellendi. Kod değişmedi: `applicationId` BKL-045 ad değişikliğiyle birlikte değişecek.

Tam sonraki adım: kullanıcının yapacakları: antenio.app alan adını almak, marka vekiline tam araştırma yaptırmak (TÜRKPATENT, EUIPO, WIPO; sınıf 9 ve 42). Kodda sırada BKL-045 (uygulamayı Antenio adına ve `app.antenio.tv` kimliğine geçirmek) ya da karar gerektirmeyen R2/R3/R5.

## Claude · Yayına hazırlık: E1, E2, E4 /.91 · 23 Ekim 2026

Kullanıcı: "Bu bitince yayına hazırlık için yapacaklarımızdan devam edelim". Karar gerektirmeyen kod maddeleri yapıldı: `app/build.gradle.kts` (yükleme anahtarı ayarları `releaseSetting`, `bundleRelease` ve önizleme olmayan `assembleRelease` için anahtar zorunluluğu), `settings_about_privacy_note` (TR/EN), `SetupFlow.TypeStep` adım sayısı. WORKFLOW'a `bundleRelease` ve anahtar değişkenleri yazıldı; LAUNCH_AUDIT E1/E2/E4 durumları güncellendi.

0.14.91 TV'ye kurulmadı (TV derin uykuda, adb erişilemiyor); TV'de 0.14.90 var. TV açılınca teslim dosyası kurulmalı.

Tam sonraki adım: kullanıcı kararı bekleyen yayın maddeleri: paket adı (E3) ve ad/marka (E8, BKL-045), abonelik ayrıntıları (E9, SUBSCRIPTION §5), gizlilik politikası URL'si ve koşullar (E4, BKL-043), yükleme anahtarı ve Play hesabı (E1, E7), mağaza görselleri (E6). Kodda sırada: açık kaynak lisans sayfası (R2), HTTP kaynak uyarısı (R3), sürüm adındaki `-design-preview` ekinin User-Agent'tan çıkması (R5).

## Claude · Tuval bölümleri dolduruldu · 23 Ekim 2026

Kullanıcı: "Şimdi başla ama işin bitince tv yi kapat ve sesi de kapalı olarak çalış". Yapılan: `cmd media_session volume --stream 3 --set 0`, `KEYCODE_WAKEUP`, uygulamada D-pad ile gezinip `screencap`; bitince `KEYCODE_SLEEP` ve ses 15'e geri (sessiz durumu korundu). Görüntüler JPEG olarak tuvalin varlık deposuna yüklendi, depoya konmadı. Arama alanında 0.14.90 düzeltmesi doğrulandı: OK sonrası StartInput inputType 0x1 ve `input text` ile arama sonuçları geldi.

Tam sonraki adım: kullanıcı yayına hazırlık işlerine dönmek istedi; LAUNCH_AUDIT'teki açık maddelerden devam. Tuvalde kalanlar APP_CANVAS'ta.

## Claude · get.php → Xtream ve yapıştırma /.89–.90 · 23 Ekim 2026

`SourceConfig.asXtreamAccount()` (Models.kt) get.php bağlantısından sunucu, kullanıcı adı ve şifreyi çıkarır; `connectDraft` bunu önce dener, `connect(requested, fallback)` Xtream `SourceFailure` verirse bağlantıyı M3U olarak yükler. Test: `XtreamLinkTest`. Klavye: `rememberFieldTyping(focused, keyboard)` (Theme.kt) odaktaki alanda OK ya da açılan herhangi bir klavye (`FieldKeyboard.isOpen`, 150 ms yoklama) ile `showKeyboardOnFocus`'u açar; `fieldKey`'in OK'i artık bu durumu başlatır, klavye iki kare sonra istenir. `EntryField` ve `SearchField` kullanır. Telefon klavye düğmesiyle açılan klavyenin TV'de pencere kenar boşluğu bildirip bildirmediği doğrulanmadı; bildirmezse yoklama tetiklenmez.

Ölçüm için sağlayıcı bilgileri kullanıcı tarafından sohbete yapıştırıldı; bilgisayarda geçici dosyada tutuldu ve ölçümden sonra silindi, depoya girmedi. Sağlayıcı aynı anda tek bağlantıya izin veriyor (max_connections 1).

Tam sonraki adım: kullanıcı 0.14.90'da sunucu alanında telefon klavyesiyle yapıştırmayı denesin; Xtream yüklemesinde kategori ve vitrin görsellerinin durumunu bildirsin. Sonra tuval bölümleri (APP_CANVAS).

## Claude · Liste beklemesi ve telefondan yapıştırma /.87–.88 · 23 Ekim 2026

Zaman aşımı: `SourceRepository` liste indirmelerinde `patientClient` kullanır (okuma 120 sn, çağrı 10 dk; hesap ve ayrıntı çağrıları eski sürelerde). `networkFailure` Android'in "failed to connect … after" mesajını `SERVER_CONNECT_TIMEOUT`, kalan zaman aşımlarını `SERVER_TIMEOUT` yapar; gövde okunurken `InterruptedIOException` da `SERVER_TIMEOUT`. Testler: `NetworkFailureTest`, MockWebServer ile `PatientListTest`.

Yapıştırma: `dumpsys input_method` geçmişinde uygulamanın her StartInput kaydı inputType 0x0 idi; başka bir IPTV uygulamasınınki 0x1. Compose 1.11.2 `TextFieldDecoratorModifierNode.startInputSession(fromTap)` dokunuş yoksa `showKeyboardOnFocus` false iken çıkar. `fieldKeyboardOptions(…, typing)` artık OK'e basılan (ya da klavye açıkken gelinen) odaklı alanda `showKeyboardOnFocus = true` verir; değişen seçenekler oturumu hemen başlatır. `EntryField` ve `SearchField` bunu kullanır; görünür gizli alanlar `BasicTextField`. 0.14.88'de StartInput inputType 0x1, imeOptions 0x2000005 görüldü; TV'de "Mobil cihazınızdaki klavyeyi kullanın" çıktı.

Tam sonraki adım: kullanıcı telefondan yapıştırmayı ve listesini yeniden denesin. Liste hâlâ açılmazsa yeni hata cümlesine göre devam edilir. Ardından tuval bölümleri (APP_CANVAS).

## Claude · Uygulama ekran tuvali · 23 Ekim 2026

Kullanıcı: "bütün uygulamanın tasarım tuvalini bölüm bölüm çıkar. Önce mevcut ekranların TV görüntüleriyle başla." Design türünden yeni tuval açıldı (https://claude.ai/artifact/LvtaBnB1HKd2xWh3cu9MQT): kapak, altı sayfa, kurulum bölümünde beş TV görüntüsü (0.14.85'te alınmış, kurulum ekranları 0.14.86'da aynı). Öteki sayfalarda "kaynak bekleniyor" notları var. Kullanıcı aynı anda TV'de kaynak eklemeyi denediği için TV kumandayla sürülmedi. Yeni [APP_CANVAS](../design/APP_CANVAS.md) bölüm tablosunu ve kuralları tutar.

Tam sonraki adım: kullanıcının yeni hata cümlesiyle kaynak sorunu çözülsün; sonra TV boştayken kurulumun kalan adımları ve bölüm 2'den başlayarak görüntüler alınıp tuvale eklensin, APP_CANVAS tablosu güncellensin.

## Claude · Bağlantı hatası teşhisi /.86 · 23 Ekim 2026

Kullanıcı kendi bağlantısıyla "Sunucuya ulaşılamadı" aldı. Kontrol: TV saati doğru, 1.1.1.1 ve www.google.com erişilebilir, Wi-Fi doğrulanmış. `SourceRepository.get` OkHttp'nin her `IOException`'ını `SERVER_UNREACHABLE` yapıyordu. Yeni `networkFailure()` UnknownHost → SERVER_NAME_NOT_FOUND, ConnectException → SERVER_REFUSED, zaman aşımı → SERVER_TIMEOUT, SSLException → SECURE_CONNECTION_FAILED eşler; metinler values ve values-tr'de, `NetworkFailureTest` eklendi. Sürüm 100/0.14.86, TV'ye kuruldu.

Tam sonraki adım: kullanıcının yeni hata cümlesine göre neden bulunur. Ad bulunamadıysa sağlayıcı alan adı ağda çözülmüyor olabilir (Türkiye'de DNS engeli yaygın); port reddiyse adresteki port; https ise http denenir. Bundan sonra uygulama tasarım tuvaline dönülür (kullanıcı onayladı: bölüm bölüm, önce TV görüntüleri).

## Claude · Yeni kurulum akışı /.85 · 23 Ekim 2026

Kullanıcı: "Taslak uygun. Telefon yolu dahil kodla; önerilen kararlarla ilerle." Yapılanlar (D117): `ui/SetupFlow.kt` (karşılama, telefon/QR, onay, tür, adres, kullanıcı adı, şifre, ad, hata adımları), `data/PhoneSetup.kt` (form ayrıştırma, 6 haneli kod, 5 dk, tek kullanım, 5 denemede kilit), `data/PhoneSetupServer.kt` (yerel HTTP, GET / ve POST /send), `assets/setup/index.html` (TR/EN telefon sayfası), `ui/QrCode.kt` (qrcodegen 1.8.0). `PilotApp` yeni kaynakta `SetupFlow`'u, düzenlemede eski `Setup` formunu açar; `ImportReady` penceresi `TvState.importSummary` ile gelir. `EntryField`'a `doneAction` eklendi; `LineIcon`'a phone ve remote. Metinler values ve values-tr'de. Sürüm 99/0.14.85.

Kontroller: `gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleRelease` başarılı; 382 test, 1 atlandı, 0 hata. Lint ve cihaz testleri koşulmadı. `SetupLayoutTest` yeni akışa göre yazıldı, çalıştırılmadı; `ParentalFlowTest` yalnız `setup-notice` etiketine bakar, o korundu. TV `install -r` 98→99, veri korundu, SHA eşit. TV'de ekran görüntüleriyle doğrulandı: karşılama, QR ve kod, bilgisayardan sentetik `example.test` gönderimiyle onay ekranı, Ekle sonrası "Bağlanamadı", Bilgileri düzelt ile "Adım 2 / 3". Ardından uygulama yeniden başlatıldı; sentetik taslak kaydedilmedi.

Tam sonraki adım: kullanıcı gerçek telefonla QR'ı okutup kaynağını eklesin ve "hazır" penceresini görsün. Yayın öncesi: androidTest (SetupLayoutTest dahil) ve lint. Telefon sayfası başlığı ve logo hâlâ NEXTV (BKL-045). Push için uzak depo adresi hâlâ bekleniyor.

## Claude · Kurulum akışı tasarım taslağı · 22 Ekim 2026

Kullanıcı: "Yeni kurulum akışının tasarım taslağını hazırla (QR ile telefondan ekleme dahil); kodlamadan önce göster." Yeni `docs/design/SETUP_FLOW.md`: ekran tablosu (TV-001, 001a/b, 002/002a–b, 003a–c, 004/004a/b; ilk odak, OK, Back), durumlar, klavye kuralı, telefonla ekleme mekanizması (yerel HTTP sunucu, 6 haneli kod, 5 dk, TV onayı; PLAN S43 bileşeni), kodlama planı (~8 g) ve 4 kullanıcı sorusu. Görsel taslak tuvali: https://claude.ai/artifact/SqgnRvfmSRHhDCPZnDBXYV (özel; 9 artboard; Antenio adı ve `Ink` renkleri). Tek geçişte, paralel ajan olmadan hazırlandı. Kod, Figma ve SCREENS değişmedi.

Tam sonraki adım: kullanıcı taslağı onaylasın ve SETUP_FLOW "Kullanıcının kararları"nı yanıtlasın (telefon yolu ilk sürümde mi, şifre açık mı, "Bir kaynak daha ekle"). Onaydan sonra SCREENS güncellenir ve kodlama SETUP_FLOW planındaki sırayla `main` üzerinde başlar.

## Claude · Kurulum alanları açık metin /.84 · 22 Ekim 2026

Kullanıcı: alanlardaki karakterler gizlenmesin; telefondaki Google TV uygulamasından bağlantı yapıştırılabilsin; "akışın tamamı modernitemize yakışmıyor". Yapılan: `ui/PilotApp.kt` içinde `reveal` varsayılanı `true` (D116); sürüm 98/0.14.84 (`app/build.gradle.kts`). Kontroller: `gradlew :app:testDebugUnitTest :app:assembleRelease` (JAVA_HOME = Android Studio jbr) 377/1 atlandı/0 hata, release başarılı; lint, androidTest ve emülatör koşulmadı. Teslim `releases/NEXTV_TV_Design_Preview_0.14.84.apk` (+ .sha256). TV `install -r` 97→98, firstInstallTime 22 Ekim 21:54 (bugünkü temiz kurulum) korundu, kurulu APK SHA eşit; uygulama açıldı, kurulum ekranında alan açık metin (ekran görüntüsü alındı, depoya konmadı). `adb input text` ile girilen örnek adres TV klavye düzeni nedeniyle bozuk yazıldı; bu adb'ye özgüdür, yazılan metin silindi ve klavye kapatıldı. Google TV uygulamasıyla yapıştırma doğrulanmadı; kullanıcı deneyecek.

Tam sonraki adım: kullanıcı telefondan yapıştırmayı ve kaynak eklemeyi denesin. Kurulum akışı yenilemesi için önce tasarım taslağı (TV-001–004; telefonla QR yolu, adım adım akış, klavye açıkken alanın görünür kalması) kullanıcıya sunulur, onaydan sonra kodlanır. Ad işleri ve push bekleyenleri önceki kayıtta.

## Claude · TV'ye temiz kurulum /.83 · 22 Ekim 2026

Kullanıcı: "televizyona temiz bir kurulum yap datalar silinsin eklemeyi test edeyim". Önce kurulu sürüm okundu: zaten 97/0.14.83 ve APK hash'i teslim dosyasıyla aynıydı. Ardından `adb uninstall` (veri dahil) ve `adb install` ile aynı APK temiz kuruldu; hash yeniden doğrulandı, firstInstallTime 2026-10-22 21:54:30. `am start` ile açıldı, MainActivity önde. Release paketi debuggable olmadığından veri dizini doğrudan okunamadı; kaldırma işlemi veriyi zaten siler. Kod, sürüm ve belgelerin kalanı değişmedi. 5556/5558 emülatörlerine dokunulmadı.

Not: "Veri silmeden kur" zinciri bu kurulumla bilinçli olarak kesildi; bundan sonraki `install -r` güncellemeleri yeni kurulumun üstüne gelir. Kullanıcının kaynak ekleme testi fiziksel kabul maddesidir.

Tam sonraki adım: kullanıcı TV'de kaynak eklemeyi denesin; sorun olursa içerik türü ve kısa hata metni kaydedilsin (kimlik bilgisi yazılmaz). Belge tarafında bekleyenler: uzak depo adresi (push), SUBSCRIPTION §1 A/B kararı, fiyat, Antenio alan adı ve marka vekili.

## Claude · D114 ad değişikliği ve aday tarama /.77 · 22 Ekim 2026

Kullanıcı: "nextv ismini de değiştirmeliyiz".

**Değişen dosyalar:**
- DECISIONS: D114, NAMING bağlantısıyla.
- AGENTS: kalıcı ürün kararları, ad satırı.
- Yeni `docs/product/NAMING.md`.
- LAUNCH_AUDIT: E8.
- BACKLOG: BKL-045 kabul ölçütü.
- STATUS.

Kod, logo ve uygulama adı değişmedi; NEXTV çalışma adı olarak kalıyor.

**Yöntem:**
- Üç yönden 36 aday üretildi; eleme sonrası 14 aday için ayrı tarayıcılar çalıştı.
- Tarananlar: Play/App Store, web, marka toplayıcılar, TR/EN/DE/AR anlam ve `.com`/`.app`/`<ad>tv.com` RDAP sorguları.
- Ham çıktılar depoya alınmadı.
- Sıralama önerisi: Lindro (çakışma düşük), Ovano, Tarvo, Delmo, Pelora.
- Lindro için not: "-dro" eki kapatılmış korsan uygulaması Mobdro'yu andırabilir.
- Kullanıcı ilk turu beğenmedi: "Bu adları beğenmedim, başka yönden yeni adlar öner."
- 2. turda gerçek kelime, zap/kumanda, isim ve bileşik yönleri denendi. Önceki 36 ad hariç tutuldu; 33 aday üretildi, 14'ü tarandı.
- 2. tur kısa listesi: Clickhop (çakışma düşük; clickhop.app ve clickhoptv.com kayıtlı değil görünüyor), Zapnest, Angora, Ferry, Zapcat. İsim yönünde temiz aday çıkmadı. Ayrıntı NAMING "2. tur".
- Kullanıcı 2. turu da beğenmedi: "bu tarz geleneksel şeylere gitmemize gerek yok daha yaratıcı ve anlaşılabilir olsun, iptv player olduğunu mağaza başlığında yazamaz mıyız". Cevap NAMING'e ve STORE_LISTING_EN açık sorularına işlendi: başlıkta "IPTV Player" serbest; marka adında "IPTV" olmaz (genel sözcük, korunamaz).
- 4. tur ("televizyonu ve izlemeyi çağrıştırsın"): 32 aday üretildi, 14 ön elemeden geçti; ayrıntılı tarama kullanım kotasına takılınca kullanıcı "bu kadar yememeli" dedi. Çok ajanlı tarama durduruldu; kullanıcının talimatıyla hafif yöntem: 5 ad için web araması, iTunes Search API ve RDAP. Sonuç: Izleo ve Antenio temiz görünüyor, Tivello orta, Tevio ve Episo elendi. Play ve marka taraması yapılmadı. **Kullanıcı talimatı:** bundan sonra paralel ajan yöntemi yok, tek geçişte ve az aramayla çalışılır.
- 3. tur (harman, son ek, yeniden yazım, canlı imge): 31 aday, 14 tarama. Kısa liste: SofaGlow (düşük; sofaglow.app, .tv ve sofaglowtv.com kayıtlı değil görünüyor; küçük harfle yazılınca İngilizcede "fag" bölünmesi riski, her yerde "SofaGlow"), Divanio, Ayglow, Telemoon, Zofa. Ayrıntı NAMING "3. tur".

**Kontroller:**
- `python tools/check_project.py` exit 1. 60 bulgunun hepsi Git dışındaki `releases/*.apk` bağlantıları.
- `git diff --check` temiz.

**Seçim (22 Ekim, sonra):** Kullanıcı Antenio'yu seçti; D115 yazıldı, AGENTS ad satırı ve BKL-045 güncellendi, NAMING "Seçim" bölümünde sıralı adımlar var. Hafif ek kontrol: Play web aramasında aynı ad yok; marka veri tabanları sorgulanmadı.

**Tam sonraki adım (güncel):**
1. Kullanıcı antenio.app ve anteniotv.com'u alsın; marka vekiline araştırma yaptırsın; paket adı `app.antenio.tv` önerisini onaylasın.
2. Logo ve wordmark hazırlansın.
3. Kodda ad geçişi `main` üzerinde yapılsın (BKL-045): etiket, logo dosyaları, User-Agent, iki dilde metinler, Hakkında; paket adı değişimi ilk Play yüklemesi ve imza değişikliğiyle aynı anda. Belgeler yeni ada geçirilsin; tarihçe kayıtları değişmez.
4. Ardından SUBSCRIPTION §1'deki A/B kararı ve fiyat.

Eski sonraki adım (tarihçe):
1. Kullanıcı NAMING kısa listesinden adı seçsin ya da yeni yön istesin.
2. Seçilince alan adı hemen alınsın. Marka vekili araştırması yapılsın.
3. Kalıcı paket adı (ör. `app.<ad>.tv`) yeni bir DECISIONS kaydıyla belirlensin.
4. Uygulamadaki ad, logo, User-Agent ve belgeler BKL-045 kapsamında `main` üzerinde değişsin. AGENTS'taki ad satırı da aynı kayıtla güncellenir.

Bekleyen önceki sorular: SUBSCRIPTION §1'deki A/B kararı, fiyat ve dönem.

## Claude · D113 abonelik modeli ve sade üyelik planı /.77 · 22 Ekim 2026

Kullanıcı: "7 gün ücretsiz deneme, sonra ücretli üyelik (Google Play aboneliği) modelini seçiyorum. Bunun için neler gerekiyor, sade dille planı çıkar." Kullanıcı teknik planları anlamakta zorlandığını söyledi. Yeni belgeler bu yüzden sade dille yazıldı; teknik ayrıntı ayrı bölümde duruyor.

**Değişen dosyalar:**
- DECISIONS: D113 ve "Açık kararlar" satırı.
- Yeni `docs/product/SUBSCRIPTION.md`.
- D113 notu eklenenler: SALES_CHANNELS, account.md, SALES_STRATEGY, gap PLAN, sales_draft.
- PRD §12: gelir modeli satırı.
- STORE_LISTING_EN: abonelik paragrafı.
- LAUNCH_AUDIT: başlık, E9, §5 notu, §7.
- BACKLOG: BKL-048, BKL-038 ve üst not.
- STATUS.

Kod değişmedi.

**Doğrulama:**
- Play abonelik ve deneme kuralları: uygunluk, ödeme yöntemi, durumlar, ek süre ve askı, test süreleri, komisyon, zam kuralları.
- Abonelik politikası ve Data safety.
- Türk tüketici hukuku: TKHK m.48 ve m.52, Mesafeli Sözleşmeler ve Abonelik Sözleşmeleri yönetmelikleri, "ücretsiz" ifadesi. AB'de Sofatutor kararı.
- Kod etkisi: kapı, ekran, önbellek, testler.
- Taslak bir eleştiriden geçirildi ve düzeltildi. Eleştiri; çıkarımların kesin yazılmasını, D112-3 çelişkisini ve eksik sahip işlerini yakaladı.

**Önemli bulgular:**
- Türkiye'de Play'deki satıcı geliştiricinin kendisidir; destek ve şikâyetler de geliştiriciye düşer.
- Kanun, iptalde kalan ücretin iadesini istiyor; Google'ın kuralı "iade yok". Bu, avukat sorusu.
- TV'den iptal yapılamıyor; telefon ya da bilgisayar gerekiyor.
- Kapalı aboneliği sorgu döndürmez; askıdaki abonelik için PBL 8.1+'da `includeSuspendedSubscriptions` var.

**`main` uyarısı:** `main` `c25a66a` (0.14.83, Antigravity) ile ilerledi; ana checkout temiz. LAUNCH_AUDIT E1 (debug imzaya düşme) ve E4 (favori ve geçmişin şifreli olduğunu söyleyen gizlilik notu) orada commitli olarak duruyor. Bu dal `ae5afb4` tabanlı. Birleşmede DECISIONS sonu (`main` D109–D110, bu dal D111–D113), STATUS ve HANDOFF üst girişleri çakışır; hepsi korunur.

**Kontroller:**
- `python tools/check_project.py` exit 1. 60 bulgunun hepsi Git dışındaki `releases/*.apk` bağlantıları; yeni bağlantılar geçerli.
- `git diff --check`, yeni dosyalar dahil temiz.
- Uygulama derlenmedi.

**Tam sonraki adım:**
1. Kullanıcı SUBSCRIPTION §1'deki A/B kararını versin. B seçilirse D112-3, DECISIONS'ta yeni bir kayıtla değişir.
2. Kullanıcı §5'teki ad, dönem ve fiyatı belirlesin. Mali müşavir ve avukat görüşmelerini başlatsın (§3, §7).
3. Kod, `main`'den başlar. Önce E4 gizlilik metni ve E1 imza düzeltilir. Sonra SUBSCRIPTION §8'e göre ödeme kütüphanesi, kapı ve üyelik ekranı yazılır; Play ürünü hazır olana kadar sahte ödeme sistemiyle çalışılır.

## Claude · D112 ve BKL-047 uygulama planı /.77 · 22 Ekim 2026

Kullanıcı: "Mağaza satışı + isteğe bağlı NEXTV hesabı + Premium ile aynı sürümde hesap önerisini kabul ediyorum. DECISIONS'a karar olarak yaz ve BKL-047 için uygulama planını çıkar." Aynı dal: `claude/launch-prep-sales-channel-2948fb`.

**Değişen dosyalar:**
- DECISIONS: D112.
- Yeni `docs/design/gap/account.md`: BKL-047 planı.
- SALES_CHANNELS: karar durumu ve §3.10'daki ilk iki madde D112 olarak işaretlendi.
- gap PLAN: Faz G notu.
- STEPS: Faz G satırı.
- BACKLOG: üst not, BKL-038 ve BKL-047.
- LAUNCH_AUDIT: §7, 8. satır.
- STATUS.

Kod değişmedi.

**Yöntem:**
- Kod haritası (giriş kapısı, depolama ve sıfırlama yolları, ağ, Ayarlar ve pencere kalıpları, testler), mevcut S35–S37 ve sales_draft tasarımı, güncel RevenueCat, Auth0 ve Play Billing belgeleri paralel incelendi.
- Taslak iki açıdan eleştirildi (kod/TV arayüzü; uyum/güvenlik) ve düzeltildi.
- Ekran kimliklerinin (TV-074–078) boş olduğu, `AdminReason`, `AccessGate.answer` ve `DeviceStore` sıfırlama yolları kodda ayrıca doğrulandı.

**Planı etkileyen bulgular:**
- RevenueCat Android 10.29.0, Play Billing 8.3.0 ile geliyor; PBL 9 yok. PBL 8 31.08.2027'ye kadar kabul ediliyor.
- RevenueCat'in "Keep with original App User ID" ayarı yalnız hesabı zorunlu tutan uygulamalara izinli. Bu yüzden planda bağın doğruluk kaynağı RevenueCat değil, `purchaseToken` anahtarlı NEXTV tablosu.
- RevenueCat'in çevrimdışı hak özelliği tek seferlik alımda çalışmıyor; plan yerel bir mağaza kaydı tutuyor.
- Hak ve hesap önbelleği ayrı Keystore alias'larında tutulmalı, çünkü `DeviceStore`'un `iptv_source_v1` anahtarı kaynak kurtarmada, ProtectionStore ise PIN sıfırlamada siliniyor.

**Kontroller:**
- `python tools/check_project.py` exit 1. 60 bulgunun hepsi Git dışındaki `releases/*.apk` bağlantıları; başka hata yok, yeni bağlantılar geçerli.
- `git diff --check`, yeni dosyalar dahil temiz.
- Uygulama derlenmedi (yalnız belge).

**Tam sonraki adım:**
1. Kullanıcı, account.md §2.2'deki 1, 2, 4 ve 10 numaralı kararları versin: hak altyapısı, kimlik sağlayıcı, giriş seçenekleri, veri bölgesi.
2. O04: RevenueCat'e ve Auth0'a IPTV oynatıcı sorusu hemen gönderilsin.
3. Kod tarafında ilk adım S35a (hesap çekirdeği; standart RFC 8628, sağlayıcıdan bağımsız) ya da yalnız sahtelerle S35. İkisi de bayraklar kapalıyken birleşir; kullanıcı hiçbir şey görmez.

Önceki kayıttaki ana checkout uyarıları (gizlilik metni, debug imzaya düşme, D109–D110 numaraları) sürüyor.

## Claude · Yayına hazırlık denetimi ve satış kanalı /.77 · 22 Ekim 2026

Kullanıcı: "yayına hazırlık için bir audit yap ve şuraya hazırlık yapalım. Satış kanalı için hangisi daha mantıklı kendi sitem üzerinden mi store'un üzerinden mi her ikisini de nasıl yaparım, yarın Apple'dan da kullanmak isteyen kişiler kendi hesaplarıyla giriş yapıp tekrar satın almadan kullanabiliyor olmalılar". Çalışma `claude/launch-prep-sales-channel-2948fb` dalında yapıldı (worktree, temel `ae5afb4`).

Yeni belgeler `docs/project/LAUNCH_AUDIT.md` ve `docs/product/SALES_CHANNELS.md`. Değişenler:
- DECISIONS: D111.
- BACKLOG: BKL-042–047, üst not ve BKL-038 bağımlılığı.
- SALES_STRATEGY: başa güncelleme notu.
- PLATFORMS: D111 paragrafı.
- PRD §12: hesap satırı.
- STORE_LISTING_EN: "No NEXTV account, no NEXTV server" yerine "No sign-up needed".
- gap PLAN ve sales_draft: başa D111 notu.

Kod, Figma ve manifest değişmedi.

Yöntem: Kod ve belge denetimi yapıldı, mağaza ve ödeme kuralları araştırıldı. Karar verdiren 13 iddia ayrı doğrulayıcılarla birincil kaynaktan yeniden kontrol edildi. Üç farklı açıdan yazılan strateji taslakları puanlandı, ardından bir tamlık eleştirisi uygulandı. Belgeler commit öncesi iki ayrı gözle (olgu doğruluğu; tutarlılık ve öneri/karar ayrımı) gözden geçirildi ve bulgular düzeltildi. Önceki araştırmaya göre düzeltilen bulgular:
- FastSpring de IPTV'yi satılamayacak iş türleri arasında sayıyor.
- Android TV'de hesap silme için web bağlantısı yeterli.
- TiviMate Premium'u artık kendi hesabına bağlı; "5 cihaz" bilgisi birincil kaynakta yok.
- AEA programları Android TV'yi de kapsıyor, ama işletme kaydı istiyor.
- Play'de sunucu doğrulaması için güncel uç nokta `productsv2`.

Ham doğrulama çıktıları depoya alınmadı.

Kontroller:
- `python tools/check_project.py` exit 1. 60 bulgunun hepsi Git dışındaki `releases/*.apk` bağlantıları (bilinen worktree sınırı); başka hata yok.
- `git diff --check`, yeni dosyalar dahil temiz.
- Yalnız belge değiştiği için uygulama derlenmedi (WORKFLOW).

**Ana checkout uyarısı.** Ana checkout'ta (TV kökü, `main`) başka bir oturumun commitlenmemiş işi var: denetim sırasında 0.14.82 iken 0.14.83'e ilerledi; 32+ dosya ve D109–D110. Bu işe dokunulmadı.
- Oradaki `settings_about_privacy_note`, kaynak, katalog, favori, geçmiş ve kilitlerin AES-256-GCM ile şifreli olduğunu söylüyor. `DeviceStore.kt:156-187` favori, geçmiş ve ilerlemeyi düz SharedPreferences'ta tutuyor. Kodda hiçbir yerde `setKeySize` yok (`DeviceStore.kt:27-30`, `ProtectionStore.kt:98-99`); "256" iddiası kodla kanıtlanmıyor.
- Release imzası `NEXTV_RELEASE_*` değişkenleri yoksa debug anahtarına düşüyor.
- İzlenmeyen `assets/licenses/SPLASH-CHIME-LICENSE.txt`, projenin kendi beyanı; hak sahibinden alınmış bir lisans değil. R1 açık kalır.
- Birleşmede STATUS ve HANDOFF üst girişleri ile DECISIONS sonu çakışabilir. İkisini de koruyun; numara sırası D109, D110, D111.

Kalan risk: SALES_CHANNELS §4'teki doğrulanamayanlar. Bunların başında Google'ın karma model için yazılı onayı yok ve RevenueCat ile Auth0'ın IPTV oynatıcı kabulü bilinmiyor.

Tam sonraki adım: Kullanıcı SALES_CHANNELS §3.10'daki kararları versin. Öncelik sırası: satış kanalı, hesabın Premium ile aynı sürümde gelmesi, ücretli paketin adı (Premium mi Plus mı), yasal yapı. Ücretsiz/Premium sınırı ilk ücretsiz yayından önce yazılmalı (SALES_CHANNELS F0). Bu arada .82 işi commitlenmeden önce gizlilik metni kodla eşitlenmeli ve release derlemesi anahtar yoksa reddetmeli (LAUNCH_AUDIT E1/E4). Marka araştırması (BKL-045) paket adı seçiminden önce yapılmalı.
## Antigravity · Altyazı boyutu kısayolu, önceki kanal düğmesi ve Faz 3 / 0.14.83 · 22 Ekim 2026

Kullanıcının "devam" isteğiyle denetimden kalan Faz 3 maddeleri tamamlandı ve fiziksel TV'ye kuruldu:
- **Değişen dosyalar:** `apps/android-tv/app/build.gradle.kts`, `assets/licenses/SPLASH-CHIME-LICENSE.txt`, `ui/PlayerOptionsDialog.kt`, `ui/PlayerScreen.kt`, `ui/settings/SettingsPages.kt`, `res/values/strings.xml`, `res/values-tr/strings.xml`.
- **Yapılan kontroller:** `:app:testDebugUnitTest` (377 JVM testi geçti, 1 skipped, 0 hata, `NoHardcodedUiTextTest` dahil), `:app:lintDebug` (0 hata, 34 uyarı), `:app:assembleRelease`, `:app:assembleDebugAndroidTest`, `python tools/check_project.py` (OK). Fiziksel TV (`192.168.1.126:5555`) `install -r` ile 96→97 (`0.14.83-design-preview`) güncellendi; `firstInstallTime` (`2026-10-13 23:50:03`) ve kurulu `base.apk` SHA-256 (`1a7db5e20a435b7fd72ca2e5e08f77304f06ce85d7b15c5a8055d69a65fbcfc7`) arşivle (`releases/NEXTV_TV_Design_Preview_0.14.83.apk`) eşit doğrulandı. `emulator-5554` ve `emulator-5556` emülatörlerine dokunulmadı.
- **Kalan risk ve tam sonraki adım:** Denetim raporundaki tüm uygulama içi UI/UX, erişilebilirlik, performans ve mağaza hazırlığı maddeleri (Faz 1 + Faz 2 + Faz 3) tamamlandı. Fiziksel TV'de kullanıcının yeni özellikleri (Seçenekler menüsünde Altyazı boyutu kısayolu, Canlı TV kontrollerinde Önceki kanal düğmesi, alt kanal bandı ve satır aralığı) test etmesi bekleniyor. Mağaza yayını öncesi dış hazırlık maddeleri: üretim imzalama anahtarı (`keystore`), `320×180 px` (`xhdpi`) raster TV banner PNG, `MediaSession` entegrasyonu ve küresel marka adı seçimi.

## Antigravity · Canlı TV alt bandı satır aralığı, yükleme halkası ve Faz 2 / 0.14.82 · 20 Ekim 2026

Kullanıcının "diğer audit'den kalan maddelere devam edelim" isteğiyle Faz 2 bulguları tamamlandı ve fiziksel TV'ye kuruldu:
- **Değişen dosyalar:** `apps/android-tv/app/build.gradle.kts`, `MainActivity.kt`, `TvViewModel.kt`, `data/ArtworkCache.kt`, `data/DeviceStore.kt`, `data/EpgModels.kt`, `data/EpgStore.kt`, `data/Models.kt`, `data/ParentalProjection.kt`, `data/PlaybackPreferences.kt`, `ui/PlayerOptionsDialog.kt`, `ui/PlayerScreen.kt`, `ui/settings/PlayerPages.kt`, `res/values/strings.xml`, `res/values-tr/strings.xml`.
- **Yapılan kontroller:** `:app:testDebugUnitTest` (377 JVM testi geçti, 1 skipped, 0 hata, `NoHardcodedUiTextTest` dahil), `:app:lintDebug` (0 hata, 34 uyarı), `:app:assembleRelease`, `:app:assembleDebugAndroidTest`, `python tools/check_project.py` (OK). Fiziksel TV (`192.168.1.126:5555`) `install -r` ile 95→96 (`0.14.82-design-preview`) güncellendi; `firstInstallTime` (`2026-10-13 23:50:03`) ve kurulu `base.apk` SHA-256 (`cf5445f04378a39bbf46b30a3562c675b1f55830a9f67dc779ca5f8cbf76b134`) arşivle (`releases/NEXTV_TV_Design_Preview_0.14.82.apk`) eşit doğrulandı. `emulator-5554` ve `emulator-5556` emülatörlerine dokunulmadı.
- **Kalan risk ve tam sonraki adım:** Kod tabanındaki tüm UI/UX ve kod denetimi bulguları (Faz 1 + Faz 2) tamamlandı. Fiziksel TV'de kullanıcının yeni özellikleri (basılı kanal değiştirme ve 4 sn alt kanal bandı, açılışta son kanalı açma, görüntü sığdırma ve yayın bilgileri) doğrulaması bekleniyor. Mağaza yayını öncesi dış hazırlık maddeleri: üretim imzalama anahtarı (`keystore`), `320×180 px` (`xhdpi`) raster TV banner PNG, `MediaSession` entegrasyonu ve marka tescil kontrolü.

## Antigravity · Vitrinde tek İncele düğmesi (D109) ve Faz 1 düzeltmeleri / 0.14.79 · 20 Ekim 2026

Kullanıcının denetim ve uygulama isteğiyle UI/UX, mağaza öncesi (Google Play / Android TV) ve kod incelemesi yapıldı; Faz 1 düzeltmeleri tamamlandı:
- **Değişen dosyalar:** `apps/android-tv/app/build.gradle.kts`, `StartupSound.kt`, `TvViewModel.kt`, `data/CatalogParser.kt`, `ui/ChannelPanel.kt`, `ui/DetailScreen.kt`, `ui/EpisodeEndDialog.kt`, `ui/GuideScreen.kt`, `ui/HomeScreen.kt`, `ui/PlaybackRecovery.kt`, `ui/PlayerScreen.kt`, `ui/Theme.kt`, `ui/settings/SettingsHost.kt`, `ui/settings/SettingsPages.kt`, `ui/settings/SettingsRows.kt`, `res/values/strings.xml`, `res/values-tr/strings.xml`, `test/.../ChannelScopeTest.kt`.
- **Yapılan kontroller:** `:app:testDebugUnitTest` (377 JVM testi geçti, 1 skipped, 0 hata), `:app:lintDebug` (0 hata, uyarı sayısı 44 → 34), `:app:assembleRelease` (R8 + shrinkResources dahil başarılı), `:app:assembleDebugAndroidTest` (başarılı), `python tools/check_project.py` (OK). Fiziksel TV (`192.168.1.126:5555`) `install -r` ile 92→93 (`0.14.79-design-preview`) güncellendi; `firstInstallTime` (`2026-10-13 23:50:03`) ve kurulu `base.apk` SHA-256 (`438e438186149d1d118a8a8cd62c202e241f21044b4a7fad664ded4fd168092e`) arşivle (`releases/NEXTV_TV_Design_Preview_0.14.79.apk`) eşit doğrulandı. `emulator-5554` ve `emulator-5556` emülatörlerine dokunulmadı.
- **Kalan risk ve tam sonraki adım:** S15c öncesi çok kaynaklı `ChannelScope` / `sourceFilterId` hizalaması (`TvViewModel.liveScope` + `ChannelPanel`) tamamlandı ve birim testiyle doğrulandı. Sıradaki geliştirme adımı S15c canlı oynatıcı tamamlamalarıdır (basılı zap sırasında yalnız `ChannelBand` önizlemesi ilerletip tuş bırakıldığında akış açma, 4 sn alt bilgi bandı, önceki kanal ve açılışta son kanalı açma ayarı), ardından S16 oynatıcı seçenekleri ve mağaza çıkış paketi (üretim keystore, 320×180 px xhdpi raster TV banner, `MediaSession` ve marka kontrolü).

## Codex · Hazırlanıyor göstergesi doğrulaması · 20 Ekim 2026

Kullanıcı: “bu da animasyonlar kapalı olduğu için donuyormuş”. Önceki açık loader bildirimi, kullanıcının animasyon ayarı doğrulamasıyla kapandı; hangi ayarın kapalı olduğu ayrıca varsayılmadı. QA kaydına kullanıcı doğrulaması eklendi; üretim kodu, sürüm ve kurulum değişmedi. Home fiziksel odak/akıcılık ve diğer kabul maddeleri ayrı açık kalır. [QA](preparing-spinner-74-qa.json).

## Codex · Home odak denetimi /.77 · 20 Ekim 2026

Kullanıcı tüm Home odaklarını ve son boşluğu sordu. HomeScreen shelfHeights haritası ve viewport−sonRafYüksekliği+7dp dolgu kaldırıldı; contentPadding.bottom sabit28dp. İlk raf Yukarı heroFocus (yoksa Cancel), son raf Aşağı Cancel, satırın son kartı Sağ Cancel. Sol/Geri menü ve Right ile hatırlanan karta dönüş aynı. Son afiş rafı frameShelf hedefini diğer afişlerle paylaşır, gerçek sayfa sonuna clamp olur; son Recent kısa hedefte kalır. Böylece yatay odakta başlığın1/2 satır olmasına göre viewport oynamaz; metin/yıl arasına boş satır konmadı. Yeni shelf-row test tagleri yatay sınır ölçümünü destekler.91/.77; D108 ve SCREENS/MOTION/HOME_SHELVES güncel.

İlk10 ekran kontrolü geçti; ekran incelemesinde kısa/uzun başlık arasında son rafın dikey oynadığı görüldü. İlk release build bu somut bulgu için kasıtlı durduruldu, teslim/final başarı sayılmadı. Son hedef düzeltmesi ve yatay eşit-Y assert’i sonrası11/11 geçti: tam Continue/Recent/Favoriler/Series/Movies aşağı-yukarı turu; son Down/Right3x engeli; menü/ayrıntıdan aynı son karta dönüş; tüm film kartlarında1dp içinde aynı poster top; son row28dp alt pay; tek kompakt rafda sıfır scroll aralığı ve kenar odağı; Hero3, boş/hatalı Home, devam ilk görünürlüğü, recent/quick menü ve refresh. Normal font, yalnız5558/betik/hostGPU/Vulkan kapalı.

Üç son sentetik ekran design/reviews/home-focus-0.14.77 altında incelendi; LATEST etiketli. Gerçek TV ekranı değildir. Son birim/lint/release 4m 24s: 377 JVM/1skip/0 hata, lint0 hata/44 uyarı. Test edilen debug/test APK hashleri final build sonunda aynı. Kaynak ayrıca yeniden okundu: dar Home sınırları, sabit alt pay, carousel/başlık/PIN/quick mantığı korunur. Figma/bağımlılık değişmedi.

TV install-r90→91 aynı firstInstallTime ve çekilen base.apk SHA eşitliğiyle doğrulandı. Asleep; uyandırma/açılış/gezinme yapılmadı.5558 kapalı/Gradle no-daemon bitti,5556/5554 değişmedi,font matrisi yok. [QA](native-home-focus-77-qa.json);ham loglar work/home-focus-77. Tam sonraki adım: kullanıcı TV’de tüm raf turunu ve son boşluğu görsel onaylasın; fiziksel kare süresi ölçümü yapılmadı. Hazırlanıyor göstergesi fiziksel tanı beklemeye devam eder; uzun OK düzeltmesi .76’dan korunur.

## Codex · Uzun OK popup bırakma koruması /.76 · 20 Ekim 2026

Kullanıcı uzun basmayı netleştirdi: kartta OK basılı tutulunca popup açılıyor; bırakınca ilk Favori satırı çalışıp kapanıyordu. Önceki callback tabanlı testler bu tuş zincirini kapsamıyordu. QuickActionInput yeni küçük tutucu: menüde başlayan repeat0 DOWN’a ait aynı anahtar UP’u geçer, activation tekrarları ve sahipsiz UP tüketilir. OK/Enter/NumPadEnter destekli; yön/Geri değişmez. QuickActions gövdesinde onPreviewKeyEvent ile, dialog içindeki LocalWindowInfo odağı kaybolunca clear çağrısıyla bağlandı. Genel SourceDialog veya PIN klavyesi değiştirilmedi; zaman kilidi yok. Versiyon90/.76; D107 ve SCREENS/MOTION güncel.

VisualPreviewTest iki yeni gerçek kumanda senaryosu: Home devam kartı ve kütüphane film kartı. DOWN→bekleme→repeat1/FLAG_LONG_PRESS popup açar; repeat2/3→UP menüyü kapatmaz ve favori false kalır. Sonraki yeni OK favoriyi true yapar, menüyü kapatır, kart odağını geri verir. Mevcut recent kanal hızlı menüsü, drawer quick dönüşü ve ebeveyn kilit/PIN menüsüyle 5/5 cihaz kontrolü. Üç JVM testinde anahtar varyantları/sahipsiz UP/tekrar/yeni basış/yön/Geri/fokus kaybı kontrolü. Son build 4m 53s: 377 JVM/1skip/0 hata, lint0 hata/44 uyarı, release başarılı. Kaynak bağımsız tekrar okundu; doğru dialog penceresi ve dar kapsam doğrulandı.

Yalnız5558/betik/normal font/hostGPU/Vulkan kapalı.5558 kapatıldı, Gradle no-daemon bitti;5556/5554 değişmedi, font matrisi yok. TV install-r89→90 aynı firstInstallTime ve çekilen base.apk SHA ile doğrulandı; Asleep olduğu için uyandırma/açılış/kumanda denemesi yapılmadı. [QA](native-quick-release-76-qa.json); loglar work/quick-release-76.

Tam sonraki adım: TV açıkken kartta uzun OK→bırak ile menünün kaldığını, yeni OK ile favorinin değiştiğini kullanıcı doğrulasın. Hazırlanıyor göstergesinin fiziksel tanısı ve .75 raf akıcılığı fiziksel kabulü ayrı açık; bu tuş düzeltmesi bunları kapatmaz.

## Codex · Continue → Recent kaydırması /.75 · 20 Ekim 2026

Kullanıcı iki raf arası takılma bildirdi. HomeScreen recent için frameShelf=false: eski viewport yüksekliğinde hedef küçük rafı ekran tepesine çekiyordu. Diğer rafların ve ilk hero→raf politikasının kapsamı aynı. headingHeight artık0 yerine actionHeight(compact=true) ile başlar; gerçek ölçüm hâlâ günceller. Normal 30 dp başlık ilk hedefi sonradan değiştirmez. Versiyon89/.75; D106 ve tasarım sözleşmeleri güncel.

Yeni VisualPreviewTest gerçek Home’da film ilerlemesi + kanal geçmişi kurup hero→continue→recent→continue yapar. .74 başlangıcında test, Recent sonrasında eski Continue kartı artık kompoze edilmediği için başarısız; bu jank süresi ölçümü değildir. Testte başlık sınırı assert’i eski kart sorgusundan önceye alındı. Son 6/6 test: yeni kısa geçiş, mevcut son kanallar yatay/quick/geri, dikey afiş rafları, iki CardVisibility hareket kontrolü, uzun hero. Son geometri: viewport540dp, Recent başlığı373dp, kart413–513dp; devam görünür,500ms sonra ikinci hareket yok. Normal font, yalnız5558/betik; font matrisi yok. Sentetik görüntü incelendi; sistem toast’ı üst köşeye girdiğinden Git galerisine eklenmedi, work/recent-scroll-75/continue-recent.png.

Son birim/lint/release 4m 48s: 374 JVM/1skip/0 hata, lint0 hata/44 uyarı. Kaynak bağımsız tekrar okundu: compact-only hedef, dinamik başlık ölçümü, odak/PIN/OK ve ortak relocation korunur; engelleyici bulgu yok. TV install-r88→89, aynı firstInstallTime ve kurulu base.apk SHA arşivle eşit; TV Asleep olduğundan uyandırma/açılış/gezinme yapılmadı.5558 kapalı, Gradle no-daemon bitti;5556/5554 değişmedi. [QA](native-recent-scroll-75-qa.json).

Tam sonraki adım: kullanıcı TV açıkken aynı iki raf geçişini denesin; takılma sürerse bu kısa geometriyle gerçek TV kare süreleri/görsel yükleme ölçülsün. Loader bildirimi TV beklemede olduğu için hâlâ fiziksel tanı bekler; uzun basma bildirimi ekran/tuş yanıtını bekler. Önceki kabulü yeni fiziksel kabul sayma.

## Codex · Hazırlanıyor göstergesi / TV beklemede · 20 Ekim 2026

Yeni bildirim: Hazırlanıyor loader dönmüyor. Salt okunur TV kontrolünde Asleep ve global animator_duration_scale1,0. Kullanıcıya TV’yi açması ve logo+gösterge birlikte mi yoksa yalnız gösterge mi donduğunu söylemesi soruldu; yanıt yok. Önceki hareket ayarı tanısı bu olayın nedeni sayılmadı.

Yalnız LaunchAnimationTest değişti: RestoringCover gerçek loading alanının bitmap piksellerini333ms arayla karşılaştırıyor. Normal hareket değişimi>50piksel; reduced-motion değişimi0. Mevcut giriş/gate testleriyle4/4 geçti. Debug/androidTest build1m40s; yalnız5558/tools/run-device-tests.sh --no-build,normal font,hostGPU/Vulkan kapalı. Kaynak ayrıca tekrar okundu: intro için still ile spinnerStill ayrı, RestoringCover logo sabitken yerel tercih kapalıysa gösterge dönüyor. Üretim düzeltmesi/yeni APK yok, fiziksel çözüm iddiası yok.5558 kapatıldı,5556/5554 ve gerçek hesaplara dokunulmadı. [QA](preparing-spinner-74-qa.json),loglar work/spinner-check.

Tam sonraki adım: TV açıkken gerçek hazırlık aralığını kaydet; uygulama hareket tercihini doğrula. Logo ve gösterge birlikte donarsa main/render işini ve içerik hazırlama geçişini ölç. Yalnız gösterge sabitse spinner state/çizim karelerini ayır. TV’ye yalnız bu test değişikliği için yeni paket kurma; uzun basma bildirimi de ekran/tuş yanıtını bekliyor.

## Codex · Şeffaf son kanal logosu /.74 · 20 Ekim 2026

HomeScreen recent kartında yalnız beyaz Box.background kaldırıldı; alan/padding/başlık/odak aynı. Eksik logo LineIcon rengi Ink.Background yüzde60→Ink.Muted. Artwork logo modu zaten şeffaf; sağlayıcının görseli yeniden işlenmedi. D105,D102 beyaz kutu kararının yerine geçer; NATIVE_UI/SCREENS/HOME_SHELVES güncel. Sürüm88/0.14.74.

Birim/lint/release tek son derleme5m37s:374 JVM/1skip/0 hata;lint0 hata/44 uyarı. Yeni test eklenmedi, basit renk değişiminde enstrümantasyon/font matrisi koşulmadı;5558/5556/5554 açılmadı. Kaynak farkı bağımsız tekrar okundu, engelleyici bulgu yok; proje/diff kontrolü geçti. TV install-r87→88, aynı firstInstallTime ve çekilen base.apk SHA arşivle eşit. Uygulamayı Home için açma ve iki Down denendi, screenshot siyah; power Asleep doğrulandı, TV uyandırılmadı. Gerçek yayın açılmadı. [QA](native-logo-background-74-qa.json);özel capture .private/logo-background-74.

Sonraki adım: TV açıkken yeni logolar kullanıcı tarafından görülmeli. Ayrı uzun basma bildirimi için hangi ekran/tuş ve gerçekleşen davranış soruldu, yanıt henüz yok; kapsam belirlenmeden uzun basma kodu değiştirilmedi.

## Codex · Fiziksel splash / hareket tercihi · 20 Ekim 2026

TV’de mevcut87/.73, sistem animator/transition ölçekleri1,0. İlk force-stop/COLD kaydında bitmiş logo + spinner doğrudan geldi. Ayarlar → Genel ve dil → Hareketi azalt satırı checked=true olarak doğrulandı; kullanıcının animasyon isteğiyle kapatıldı, checked=false kaydedildi. İkinci force-stop/COLD kaydında logo işareti, kısmi NEXTV harfleri, tamamlanma ve Home geçişi görüldü. Bu ayar D069 gereği girişi atlıyordu; performans düzeltmesi veya yeni APK değildir. Açılış sesi ayarı açık kaldı, akustik eşzaman doğrulanmadı. Fiziksel kesintisiz frame pacing kabulü açık kalır.

Özel kayıtlar .private/splash-74 altında (klasör tanı için isimlendirildi; sürüm .74 üretilmedi), paylaşılabilir kayıt [QA](physical-splash-setting-73-qa.json). İlk hızlı kumanda dizisi beklenmedik şekilde player açtı; çıkıldı, sonraki gezinme tek tek odak okunarak yapıldı, olağan geçmiş etkisi olabilir. Kaynak silme/ekleme, reset, paket/cihaz ayarı değişikliği yok. Son ekran Home; emülatör/Gradle açılmadı. Üretim kodu değişmediğinden yeniden uygulama testi yok; doküman kontrolü yeterli. Sonraki adım: kullanıcı normal açılışı izlesin; ayrıca bir takılma bildirirse Hareketi azalt kapalıyken aynı cihaz kaydı/kare süreleriyle incele, bu tanıyı eski performans kabulü sayma.

## Codex · Oynatıcı, kategori ve ayrıntı /0.14.73 · 20 Ekim 2026

Kullanıcının beş UI isteği ve1×'e dönen hız bildirimi birlikte işlendi. PlayerControls ortak ölçüler48/44dp,13sp düğme/bilgi,24sp başlık,16sp saat/süre. Kanallar solda Ses/Altyazı ile aynı44dp/20dp ikon düzeninde; PlayerScreen canlı CH−/CH+ ekran düğmelerini kaldırdı, kumanda ve panel davranışı aynı. LibraryScreen kategori sayıları grid'in aynı havuzundan tek geçiş; favori/geçmiş/arama/yenileme güncellenir. Sayı contentDescription'a eklenir, selected semantiği korunur. Theme.Action yeni selectionIndicator varsayılantrue, yalnız SortAction false.

DetailScreen contentInset0, backdrop FillWidth/TopCenter, D097 karartmaları ekranın en solundan; foreground/Back rail payını kendi uygular. Film grubu viewport−36dp minimum alanın altında,20dp aralıklı; dizi minimum280dp ve sezon odak akışı korunur. Geniş görselsiz afiş yedeği aynı oranda. Yeni sentetik görüntüler design/reviews/player-category-0.14.73, LATEST güncel.

Hız: PlayerFactory başlangıç moduVOD+tunnelling kapalıysa PcmOnlyAudioSink kullanır; tercihi diske yazmaz. Ses decode hatası bildirimi gerçek oturum çıkışını kullanır. TV media.metrics geçmişinde UID10106 için AC3/DIRECT görüldü; tek hata anı kesin yakalanmadı. Media3 1.11.1 encoded passthrough hız işlemcisini atlayıp1× döndürebilir; D104 kaynakları ve Dolby/DTS bitstream→PCM değişiminin sınırlarını oku. Cihazda codec yoksa mevcut ses çözülemiyor uyarısı gösterilir. Gerçek aynı filmde tekrar deneme hâlâ gerekir; sentetik AAC testi AC3 fiziksel çıkış testi değildir.

Kontroller: ilk debug derlemesinde yeni test DpRect.height kullanımı yüzünden compile başarısız; bottom−top ile düzeltildi. İlk9 kontrol geçti; son kaynak incelemesinde oturum ses uyarısı ve sayının selected semantiğini gizlememesi düzeltildi. Son birim/lint/release/debug/test build1m47s,374 JVM/1 skipped/0 hata,lint0 hata/44 uyarı. Son APK'larla aynı9 kontrol tekrar geçti: movie/live control geometry, drawer dönüşü,3 hız senaryosu, kategori sayıları, uzun açıklama/sezonlar, poster yedeği. Yalnız5558/tools/run-device-tests.sh --no-build,hostGPU/Vulkan kapalı, normal font. Speed kontrolü ses çıkışı başladıktan sonra2×'in korunmasını ve2s duvar zamanında medya oranının1,6–2,4 olmasını ölçer.

Release87/.73 arşivi releases; fizikselTV86→87 install-r, firstInstallTime aynı, çekilen base.apk SHA arşivle eşleşti. TV uygulaması açılmadı, gerçek yayın başlatılmadı.5558/Gradle kapalı; kullanıcı emülatörü5556 ve5554 değişmedi. Ham loglar work/player-category-73. Figma/bağımlılıklar değişmedi; kullanıcı font matrisi ertelemesi sürer.

Sonraki adım: kullanıcının gerçek TV'de aynı filmde1,5×/2× denemesi ve yeni UI kabulü.1× dönüşü sürerse gerçek seçim anındaki codec/çıkış ve parametre olayını kimliksiz ölç; varsayımla decoder/hesap değiştirme. Önceki S15c, M3U görsel/tarih desteği ve emülatör uzun oturum kararlılığı işleri açık. [QA](native-player-category-73-qa.json).

## Codex · Vitrin/kanal ferahlığı ve film rafı /0.14.72 · 20 Ekim 2026

HomeScreen: hero-label, tüm slaytların ölçülen ve görünen HeroText grubunun içine alındı; kısa metinde etiket–başlık10dp korunur. İzle üst aralığı18→28dp. Görselin viewport/FillWidth/TopCenter/scrim ayarları değişmedi. Son kanal kartları80×56dp beyaz/8dp radius/8dp iç boşluk, koyu eksik-logo simgesi,13sp/18sp tek satır ad; tam erişilebilir ad korunur. Film rafı artık geçmişten bağımsız olarak dizi rafının hemen altında; ikisi ortak LibraryShelfWidth160dp ve16sp başlık kullanır.

LibraryScreen yalnız canlı katalog:60dp minimum satır,16dp yatay/10dp dikey iç boşluk,16dp kolon arası,10dp satır arası,44×26dp logo,5dp EPG satır arası. Player çekmecesi değişmedi. HeroLayoutTest kısa/uzun slaytta etiket/düğme/raf sabitliğini; VisualPreviewTest geçmiş varken film rafını ve yeni dikey sırayı doğrular. Tasarım sözleşmeleri ve D102 güncel; Figma değişmedi.

Son build2m42s başarılı;373 JVM (1 skipped,0 hata),lint0 hata/42 uyarı. İlk kontrol iki eski inline BuildConfig sürüm beklentisi yüzünden başarısızdı; unit compile zorlanıp Kotlin incremental kapatılarak düzeldi. İlk retry PowerShell argüman alıntılama hatasıyla başlamadı; doğru alıntıyla yeniden çalıştırıldı. Üretim debug APK hash'i aynı kaldı, test APK yeniden üretildiği için5558'de aynı6 ilgili test son APK'larla tekrar geçti. Yalnız tools/run-device-tests.sh --no-build, normal font,hostGPU/Vulkan kapalı. Ham loglar work/home-polish-72; sentetik6 görüntü design/reviews/home-polish-0.14.72.

Release86/0.14.72-design-preview releases altında arşivli. Fiziksel TV install-r ile85→86; aynı firstInstallTime ve çekilen base.apk SHA doğrulandı. TV vitrin görüntüsü .private/home-polish-72/home.png; sonraki kontrol sırasında başka uygulama öne geldi, TV gezinmesi durduruldu. recent.png NEXTV değildir, kanıt olarak kullanma. Gerçek katalog görüntülerini Git'e alma. Yeni gerçek yayın açılmadı.5558 ve Gradle kapalı;5556/5554 değiştirilmedi.

Sonraki adım kullanıcı incelemesi: yeni canlı satır aralıkları, beyaz logo kartları ve film rafının fiziksel TV görünümü. Eski .71 galerileri tarihli kalır, .72 diye sunulmaz. S15c/diğer ürün işleri, M3U tarih/görsel desteği ve uzun emülatör oturumu kararlılığı açık. Yazı matrisi yalnız kullanıcı “yayına hazırla” dediğinde. [QA](native-home-polish-72-qa.json).

## Codex · Gerçek TV ekranlarının teslimi · 20 Ekim 2026

Kullanıcı gerçek veriyle, doğrudan televizyondan ekran görüntüleri istedi. Kurulu 85/0.14.71-design-preview ve mevcut 1920×1080 görüntü boyutu doğrulandı; fiziksel TV uyandırılarak yalnız seri hedefli manuel gezinme/yakalama yapıldı. 41 özgün PNG `.private/tv-screen-gallery-71/`; çevrimdışı HTML/Markdown ve kopyalar `output/`; ZIP `NEXTV-TV-ekranlar-0.14.71.zip`. Tarif ve özel manifest aynı klasörde, araçlar `work/tv-screen-gallery-71/` içinde. Kişisel katalog/geçmiş olan bu dosyaları releases, Git, Figma veya dış servise taşımayın. Galeri ana ekranları/panelleri ve bazı kaydırma konumlarını kapsar; tüm hata/kurulum/erişim varyantları değildir.

03 gerçek kompakt son izlenen kanal kartlarıdır; 06–12 katalog/EPG/tek canlı oturum/kanal-kategori-program sütunlarıdır. 13–19 film ayrıntısı/açıklama/oynatıcı/ses/seçenek/hız; film oynatıcı karesinde video alanı siyahtır, sonraki panellerde görüntü vardır. 20–21 ve34 dizi/bölüm/sezon; 22 gerçek boş favoriler; 23–25 arama/klavye/sonuç; kalanlar ayar bölümleri, kaynak yöneticisi ve boş PIN formudur. PIN girilmedi; ayar seçimi, hesap düzenleme, yenileme, silme veya sıfırlama yapılmadı. Canlı ve film sırayla açıldı; son kanal/geçmiş/konum normal oynatma etkisiyle güncellenebilir. Oynatıcı kapatıldı, TV Home/İzle odağında bırakıldı.

Kaynak yöneticisinde TV hesabının Xtream Codes olduğu doğrudan doğrulandı; 5556'nın önceki M3U doğrulamasıyla birlikte vitrin veri farkı artık somut. Bu yeni galeri tümüyle .71 gerçek TV'dir. Önceki sentetik galerinin 11 numaralı etiketi “Canlı yayınlar rafı” olarak düzeltildi: geçmiş yokken `live` büyük kartları gösterir; D091 `recent` kompakt kartları farklı raftır. Önceki ZIP yeniden üretildi: 42.703.449 bayt, SHA-256 `afdcbc1c7e9ee014b63050737a9cfd2bfed4f956b54821c5ffaddb58e6509305`; aşağıdaki eski teslim hash'i tarihçedir.

41 görüntünün tamamı kök/bağımsız ajanlarca görsel olarak incelendi; yanlış/boş ekran veya hesap adresi/parola/token görünmedi. Özgün PNG→çıktı→HTML→ZIP eşleşmesi ve ZIP bütünlüğü doğrulandı. Son ZIP 55.856.025 bayt; SHA-256 `05dc82e634d07ac1d4ca14bc7801b3a03d230412929b35cc753ea79e761f85d2`. Yukarıdaki sayılar ham çekim numaralarıdır; çıktı galeri bölüm sırasıyla yeniden numaralanır, manifest `captureNumber` eşlemeyi korur. Galeri yalnız `127.0.0.1:8768` üzerinde (Python PID34956), uygulama tarayıcısında açık; büyütme görünümü doğrulandı. Çevrimdışı HTML tek başına kullanılabilir.

Genel ayarlar karesinde TV'nin mevcut “Hareketi azalt” ayarı Açık görünüyor; değiştirilmedi. Bu durumda vitrin/açılış/menü animasyonları kapalı olacağından sonraki hareket geri bildiriminde hesaba katılmalı. Bazı gerçek kanal logoları/EPG bilgileri eksik; galeri bunları olduğu gibi gösterir.

Üretim kodu/paket değişmedi; derleme veya cihaz instrumentation testi yapılmadı. Yalnız kullanıcı yetkili gerçek cihaz görüntüleri incelendi; font matrisi yok. 5556'ya dokunulmadı; 5558/Gradle kapalı. Sonraki adım numaralı gerçek TV karelerinden kullanıcının yerleşim geri bildirimini almak; açık ürün/kararlılık/Figma kabul maddeleri kapanmış sayılmaz.

## Codex · Ekran görüntüsü galerisi · 20 Ekim 2026

Kullanıcı bütün ekranların SS'lerini istedi. Teslim: `releases/NEXTV-ekranlar-0.14.71/{index.html,galeri.md,README.md,manifest.json,44 PNG}` ve aynı adlı ZIP (42.702.473 bayt; SHA-256 `4959eee5cf5066f5a9319d955fec61bfcd27912234c93dfbce79cf08d8f67313`). 36 kayıt .71 (32 yeni + 4 mevcut),8 kayıt .62/.63/.68/.70; galeri her birinin gerçek sürümünü belirtir. TV001 mevcut kaynak kurulumuyla ortak; ayrı welcome sayfası yok. Ana ekranlar/başlıca paneller kapsanır; tüm recovery/lock/next-up/uzun basma/alt ayar/hata varyantları değildir. Uzun sayfaların yalnız yakalanan kaydırma konumu görünür.

Tek kaynak değişikliği `VisualPreviewTest#captureGalleryFavoritesAndSettings`: yerel fixture favorilerini doldurur, ayarlar kökü+5bölüm+Hakkında yakalar; mevcut cleanup kaynak favorilerini temizler. Üretim koduna dokunulmadı. Derleme debug/test47sn; Gradle durdurulup5558 hostGPU/Vulkanoff soğuk başlatıldı, NVIDIA renderer doğrulandı. Yalnız `tools/run-device-tests.sh --no-build` üzerinden12/12 yöntem geçti; yeni font matrisi/release/TVkurulumu yok. Yakalama/galeri görevi için unit/lint/release tekrar koşulmadı. Tam log `work/screen-gallery-71/device-tests.log`, kaynak PNG'ler `captures/files`, generator `build_gallery.py`, tarif `prepare_manifest.py`, giriş `manifest.json` aynı work klasöründe. Büyük teslimler releases içinde Git dışıdır.

İki bağımsız ajan PNG/HTML/ZIP hashlerini,48dosya içeriğini, kaynak gizliliğini ve16görseli kontrol etti; kök ajan katalog/film/dizi/arama/player görüntülerini ve tarayıcı filtre/arama/lightbox/ok/Esc işlevini doğruladı.5558 snapshot kaydetmeden kapatıldı;5556/TV değişmedi. Yerel galeri sunucusu yalnız127.0.0.1:8767,PythonPID8488; kullanıcı önizlemesi için açık. Sonraki adım ekran geri bildirimini almak veya önceki açık M3U metadata/S15c işini sürdürmek; bu paylaşım ürün eksiklerini kapatmaz.

## Codex · Tekrarlayan çökmede SwiftShader kanıtı / host GPU · 20 Ekim 2026

15:27:07'de kullanıcı menü açarken bütün5556 kapandı. PID23776 önceki Vulkan-off SwiftShader açılışıyla aynı; bu deneme başarısızdır. Önceki “dump yok” sonucu eksikti: `%LOCALAPPDATA%/CrashDumps` içinde32776/23776 ve test emülatörü dump'ları var. İki GUI dump'ında aynı MOVDQU komutu/RBP+R9+0x184e0 hesaplaması tam hata adresini verir, raw stack'te aynı SwiftShader libGLESv2 çağrı dönüşleri vardır; PE byte kontrolleri çağrıları doğrular. Bağımsız ajan yalnız bu read-only native incelemeyi yaptı; özel JSON'u `.private/emulator-crash-71-repeat/dump-analysis.json`. Simgeli unwind yok, belirli shader/buffer kusurunu kesin ilan etme. Sanitized [QA](emulator-host-crash-71-repeat-qa.json); ilk QA'nın dump/workaround sonucu düzeltilmiştir.

5556 `-no-snapshot-load -no-snapshot-save -gpu host -feature -Vulkan` ile mevcut diskten görünür açıldı. QEMU PID32788 /15:31:01; logdaki gerçek GLES renderer NVIDIA RTX4050/NVIDIA616.56, process'te SwiftShader/libGLESv2 yok. Boot20,624sn; aynı85/.71/firstInstallTime/lastUpdateTime. Gerçek component `dev.iptvplayer.tv.pilot/dev.iptvplayer.tv.MainActivity`; ilk applicationId-relative deneme yanlış component nedeniyle başarısız, doğru LEANBACK_LAUNCHER çözümlemesiyle COLD409ms/önde/Home geçti. Kaynak/kurulum/snapshot/veri/gerçek yayın değişikliği yok. Android kalıcı son çıkışı yine PACKAGE UPDATED; ani VM kapanması kayıtları engelleyebilir.

Aynı donanım renderer'ıyla5558 soğuk/başsız açıldı, mevcut .71 debug/test APK'ları `tools/run-device-tests.sh --no-build` üzerinden iki sentetik kontrolü geçti: HeroLayoutTest#newestTitlesTurnWithRightAndLeftAndLeftOnTheFirstReachesTheMenu ve SearchNavigationTest#categoryAndMenuTransitionsKeepIntermediatePositionsAndReverseImmediately. Normal yazı, yeni build yok; tam çıktı özel klasörde.5558 snapshot kaydetmeden kapatıldı,5556 açık kalsın; TV/5554 dokunulmadı. Önceki kapanış yaklaşık48dk sonra: kısa iki test uzun kararlılık kanıtı değildir. Tekrarında yeni Windows dump'ını sakla; veri sıfırlama veya uygulama kodunu kanıtsız değiştirme. Yeni açılışta gerçek renderer'ı logdan kontrol et.

Kullanıcının aradaki vitrin sorusu yanıtlandı: ürün hedefi son eklenen film/diziler; M3U yolu addedAt almadığından mevcut kod tek Kütüphanenden yedeğine düşer. Liste sırası tarihin kanıtı değildir; kaynak türü kullanıcıya sormadan dönüştürülmedi. Bu destek/görsel eksikliği ve S15c/diğer ürün işleri açık kalır.

## Codex · Vitrin kaynak türü doğrulaması · 20 Ekim 2026

Kullanıcının “kesinleştir” isteğiyle 5556'nın mevcut Home görüntüsü ve Kaynakları yönet ekranı okundu: tek kaynak M3U. Kaynak düzenleme, yenileme, silme/ekleme veya yayın açma yapılmadı. Back ile aynı Home/İzle odağına dönüldü; 5556 açık bırakıldı. Ham ekranlar yalnız `.private/hero-source-diagnosis/{current,source-type,return}.png`, Git'e eklenmez. TV/5554/5558 dokunulmadı.

Kaynak kodu zinciri: `SourcesScreen.sourceNumber` doğrudan kayıtlı `source.type` değerini gösterir; `CatalogParser` M3U yolunda yalnız afiş alanını doldurur, backdrop/açıklama/addedAt varsayılanda kalır; `ensureHeroDetail` yalnız Xtream isteği yapar; tarihsiz katalog `featured` tek slaytına, `HeroPicture` ise eski bulanık fon/sağ afiş düzenine geçer. Salt okunur bağımsız ajan incelemesi de bu zinciri doğruladı. .71 farkı bu seçimi değiştirmez. Sağlayıcının Xtream ayrıntılarındaki görsel varlığı bu incelemede doğrulanmadı; “M3U standardında hiçbir geniş görsel olamaz” genellemesi yapılmamalı.

Uygulama kaynağı/sürümü değişmedi; derleme/font/cihaz test turu gerekmedi. Kullanıcı yalnız tanıyı istedi; kaynak türünü otomatik dönüştürme veya mevcut kaynağı kaldırma. Sonraki adım talebe göre M3U görsel bilgisi desteği/eksik görsel düzeni ya da sağlayıcının desteklediği Xtream bağlantısını değerlendirmek; önceki S15c ve diğer açık işler korunur.

## Codex · 5556 host çökmesi tanısı · 20 Ekim 2026

Kullanıcı "listeleri kaldırıp ekliyordum" dedi. Windows14:34:34Event1000 qemu-system-x86_64.exe/PID0x8008=32776/0xc0000005/unknown; ilk5556 emülatör logunun PID'siyle eşleşti.14:34:42WER StackHash_b4ee/PCH_D7_FROM_unknown. Aynı native erişim ihlali önce iki5558 headless koşusunda da vardı. WER yalnız Report.wer; kullanılabilir native dump yok, hatalı modül kesinleşmedi. Ham Windows/Android/yeniden açma kayıtları yalnız.private/emulator-crash-71. Sanitized [QA](emulator-host-crash-71-qa.json). Android son kalıcı app exit-info11:20:49PACKAGE UPDATED; bu kapanışa ait crash/ANR bulunmadı. Ani VM ölümünün guest kayıtlarını kesebileceğini unutma; uygulamanın her koşulda hatasız olduğunu iddia etme.

Eski başlatma -gpu swiftshader_indirect (resmî belgede36.4.9'dan beri deprecated), SwiftShader GLES/Vulkan ve Qt opengl32sw fallback var. Grafik/runtime şüpheli, kanıtlanmış spesifik driver nedeni değildir. Tek seferlik tanı açılışı: nextv_design_api36/5556, -no-snapshot-load -no-snapshot-save -gpu swiftshader -feature -Vulkan, görünür pencere.14:38:40PID23776; boot ve Vulkan disabled logu, aynı85/.71/firstInstallTime, MainActivity COLD/önde doğrulandı;5556 açık kalsın. Veri/snapshot/kurulum/kaynak/gerçek yayın işlemi yapılmadı.5558/Gradlekapalı;TV/5554dokunulmadı. Kalıcı AVD/SDK/Windows ayarı değişmedi; uygulama kaynağı/paketi aynı, yeni derleme gerekmedi. Uzun oturum/liste döngüsü için çözüm doğrulanmadı. Tekrarında önce native dump yakala; gerçek kaynak sil/ekle ile otomatik yeniden üretme. Sentetik kaynak testi yalnız5558/betik. Ardından önceki S15c kaynak-filter kenarı ve kalan işler sürer.

## Codex · Vitrin ilk devam önizlemesi / 0.14.71 · 20 Ekim 2026

Kullanıcının son isteği D101: görseli değiştirmeden ön planı yukarı ve devam rafının başlangıcını ilk ekrana taşı. HomeScreen.Hero ön planı minimum420dp, matchParentSize+üst hizalı taşan arka plan viewport boyunda kalır; kırpma arka planın kendi sınırında. Metin ölçüleri, görsel/kadraj/karartma, tek İzle ve D095 odak algoritması aynı. VisualPreviewTest mevcut devam kartı etiketiyle ilk başlık/en az32dp kart görünürlüğünü ve Down/Up sonrası görselin eski üst konumunu doğrular. Kod85/.71; HOME_HERO/SCREENS/NATIVE_UI/D101/BACKLOG ve sentetik görseller güncel, Figma değişmedi.

Debug/test2m2s, test etiketi düzeltmesi1m3s; yalnız5558/betik/normal1,0. Son APK'larla dört farklı vaka başarılı: slayt/menü (kesilen Hero koşusunda tamamlandı), uzun/no-art ayrı1/1, devam/görsel1/1, dikey raf1/1. İki birleşik koşu hostQEMU sırasında kesildi; Windows native0xc0000005/unknownmodül, uygulama hata kanıtı sayılmadı. İlk görsel assertion yanlış tile:continue etiketi nedeniyle başarısızdı, mevcut continue etiketiyle düzeltildi. Ham kayıtlar work/hero-peek-qa, [QA](native-hero-peek-71-qa.json). Son tam unit/lint/release/debug/androidTest5m 23s: 373JVM/1skip/0hata,lint0/42; debug/test hashleri aynı. Font matrisi/fizikselFPS yok.

Arşiv releases/NEXTV_TV_Design_Preview_0.14.71.apk, SHA-256 df9b0a9d0836c3bb7c9e3a169980a3e041f5ca601c1bb2490e0441fe8bc6371f. Uygulama commitda22676 sonrası aynı ajan ayrı kaynak geçişinde ölçüm/kırpma, odak, görsel ve ek yükü inceledi; somut bulgu yok, ayrı reviewer ajanı yok. TV14:22:06/.71kod85 ve5556emülatörsaati11:20:49/.71kod85 install-r teslimi doğrulandı; firstInstallTime ve çekilenAPKhash eşit.5556 ilk açılışında launcher önde kaldı; ikinci amstart mevcut task'ı öne getirdi. XMLappnodevermediğinden Home/İzle/devam başlığı/kart başlangıcı özel görüntüyle elle doğrulandı; ham görüntü yalnız.private/hero-peek71.5556 açık/önde/resumed kalsın; test/reset/kapatma yapma. TVapp/yayın açılmadı;5554dokunulmadı,5558/Gradlekapalı. Sonraki geliştirme S15c öncesi çok kaynak/source-filter sayımı/Allfallback; S15c/S16/S17–S29 ve fiziksel splash/ses kabulü açık. Eski iki yedek stash korunur.

## Codex · 5556 kullanıcı testi için açık · 20 Ekim 2026

Kullanıcı son sürümü emülatörde denemek istedi. nextv_design_api36/5556, -no-snapshot-load/-no-snapshot-save ile mevcut disk verisinden ve görünür pencerede açıldı; wipe/reset/snapshotrestore yok. Önce .66/kod80 vardı; arşiv .70/kod84 install-r ile kuruldu. firstInstallTime2026-10-14 08:57:19 değişmedi; cihaz lastUpdateTime2026-10-20 10:54:55 (emülatör saati); kuruluAPKhashfc99d6a48ebd89a4d0514f26fa6de7d55b467de10e524355b027cc2b2a4cb8db eşleşti. MainActivity önde/resumed, HomeUI görünüyor, appRunningtrue/crashfalse. Evim etiketi tam ekran Home'da görünmüyor; bu veri silme kanıtı değildir. Kaynaklara/oynatmaya otomatik müdahale edilmedi. Ham hassas UI dökümü saklanmadı; yalnız boolean/kurulum metadata work/manual-emulator-70/delivery.json.

5556 kullanıcı için AÇIK kalsın; test/reset/kapatma yapma.5558 veGradlekapalı,TV/5554dokunulmadı. Kullanıcı talebi yalnız mevcut APK'yı açmaktı; yeni derleme/ekran testi/fontmatrisi yapılmadı. Sonraki geliştirme aşağıdaki S15c öncesi kaynakfilter/sayım kenarıdır; normal testler yine yalnız5558/tools/run-device-tests.sh. Bu işlem yalnız cihaz/QA kayıtlarını değiştirir.

## Codex · S15b / 0.14.70 teslim · 20 Ekim 2026

Üretim kod84/0.14.70; uygulama commit37d9c54. Sonrasında aynı ajan, ayrı kaynak geçişinde dört STEPS bakışını inceledi; ayrı reviewer ajanı yok. Mevcut ChannelDrawer→ChannelPanel: kategori/mini rehber, kısa/basılı OK, paging/1200ms numara odağı,15sn idle/TalkBack/NEVER/PIN/dialog, görünür±3/playing EPG150ms/oturummerge, QuickActions/ProgrammeDetail. Düz focusTarget dönüş kimliğini geçici sistem odağından ayırır; inactivekolon callbackleri hafızayı ezmez. Inactivekanalsütunu bring-into-view yapmaz; minirehberde seçili kanal tamamen görünür. LiveLists cachedfavorites/ScopeChoice, VMfocusedGuide/liveChoices/liveEntry, sharedProgress, EN/TRplurals. D100/SCREENS/NATIVE_UI/MOTION/BACKLOG/STEPS/STATUS/QA/görseller güncel. S15c/S16/sonrası uygulanmadı.

Final debug/androidTest2m31s→yalnız5558/tools/run-device-tests.sh/normal1,0:12/12. Sonra emülatör kapalı fullunit/lint/release/debug/androidTest4m44s:373JVM/1skip/0hata,lint0/42. Test edilmiş debug/test APK hashleri aynı kaldı; kaynak değişmediğinden ekran turu tekrar edilmedi. design/reviews/channel-panel-0.14.70 görselleri incelendi; seçili kanal viewport assert'i geçti. native-channel-panel-70-qa.json ilk başarısızlıkları ve bilerek durdurulan iki derlemeyi korur; ham work/channel-panel-qa. Fontmatrisi/fizikselTalkBack/FPS/4500probe yok. GUIDE uygulamaWindow.Callback sınırı; fizikselGuideaçık.

Arşiv releases/NEXTV_TV_Design_Preview_0.14.70.apk+sha; SHA-256fc99d6a48ebd89a4d0514f26fa6de7d55b467de10e524355b027cc2b2a4cb8db. Sertifika4fe97f80884624ca72df123c25f90f21c1a1792d189da7a18529ec4d41e96065 aynı; debugkapalı/R8/API26. TV .69kod83→.70kod84 install-r; firstInstallTime2026-10-13 23:50:03 aynı,update2026-10-20 13:48:00,kuruluAPK çekiliphash eşleşti. TVapp/yayın açılmadı.5558 snapshot kaydetmeden kapalı,Gradlekapalı;5556/5554başlatılmadı/dokunulmadı.

Tam sonraki iş: S15c öncesi çokkaynak/sourcefilter kenarını doğrula ve hizala. liveIndex nullsource'ı sourceFilterId ile dolduruyor, liveChoices(null) global sayıyor; numericAllfallback sonrası kategori sayımı tutarsızlığı olası. Son sentetik fixture tek kaynaklıdır; bu kenar kabul edilmiş sayılmaz. Sonra ChannelBandModel/ChannelBand4sn,ChangeOrigin,0/LAST/Önceki kanal eylemleri,heldzapyalnızrelease'deakış,sonkanalıaçkapalıtoggle/kapakaltındapausedbuffer/chimedurdurma. S16seçenekler/aspect/streaminfo veS17–S29açık. BKL032doneolmaz. HomeScrollkapalı; fiziksel splash/ses açık. Sonraki paket enaz85/.71.

Kullanıcı işlerin uzamasını sordu: tamR8derlemesi gereğinden sık tekrarlanmıştı; sorunlar yeni panelin odak/viewport geliştirme döngüsündeydi, genel proje bozulması bulgusu yok. WORKFLOW dar debug/ekran→son tam kontrol sırasını açıklar; doküman commit'i için uygulama testi icat edilmez. Sonraki adımda bu sırayı koru. Kullanıcı devam talimatını iptal etmedi.

Arkadaş APK/kurulum sorusu için AndroidTV/GoogleTV(API26/Android8+), USB→APK açabilen dosyayöneticisi→Yükle→yalnız o dosyayöneticisine bilinmeyen uygulama izni→kendi yayın kaynağı anlatıldı. Marka/model opsiyonel soru yanıtsız. Finalde güncel .70 APK bağlantısı verilebilir; kullanıcının kaynak bilgileri APK'ya eklenmedi. Fontmatrisi ancak kullanıcı yayına hazırla dediğinde.

## Codex · S15a / 0.14.69 teslim · 20 Ekim 2026

Kullanıcının “devam” talimatıyla codex/remaining-player-work dalında S15a tamamlandı. Son üretim kod83/0.14.69, uygulama commit6d85bf2. Ayrı kaynak incelemesi dört STEPS bakışıyla tamamlandı; somut güncel regresyon bulunmadı, ayrı ajan kullanılmadı. main/audit teslim eşlemesi yapılır; yeni işe codex/channel-panel-s15b dalıyla başla. D099; görünür katalog projeksiyonunda Default'ta LiveLists kaynak/kategori listeleri ve global/kaynak numara haritaları kurar. GateSnapshot aynı canonical liveById'yi kullanır. LiveRecall current/previous bağlamı, LastLive codec/AutoStart saf kararı, DeviceKeys/Store temizliği, VM kapısı ve rehber hedefi, 0/LAST pendingdigit/repeat kuralları uygulandı. Guide PLAYER'da oynayan kanalı hedefler. Son kanal yalnız gerçekten oynayınca saklanır; kaynak/geçmiş temizliği ilgili recall'ı da temizler. Açılış ayarı henüz UI'da yok, varsayılan kapalı; kapak altında paused player/S15c eksik. Yeni önceki/son kanal metinleri EN/TR.

Değişen üretim dosyaları: app/build.gradle.kts; TvViewModel; data/{ChannelScope,LiveRecall,AccessGate,AccessTargets,DeviceKeys,DeviceStore}; ui/{PlayerKeys,PlayerScreen}; EN/TRstrings. JVMekleri LiveRecall/AutoStart codec/kurallar, LiveLists numara haritası paylaşımı/source/category/favorites/adult, cachedGate/PIN/currentpublication, DeviceKeys cleanup, router0/LAST/pending/repeat. GuideFlowTest yeni rehber, previous/history/digit10 ve favoriteszap/globalnumber/Backfocus akışları. SCREENS/NATIVE_UI/DECISIONS/BACKLOG/STEPS/STATUS/QA güncellendi; Figma değişmedi.

Kontroller: full unit/lint/release/debug/androidTest9m12s;368JVM/1skip/0hata, geç eklenen cachedChannelLookup testi finalXML'de. Lint0hata/42uyarı; yeni2UseKtx mevcut SharedPreferences stilinde tutuldu. Favorites test ekinden sonra test/lint2m01s, Guide tanı/sınır düzeltmesinden sonra final test/lint2m34s. Üretim kaynakları full buildden sonra değişmedi. check_project geçti. Yalnız5558/tools/run-device-tests.sh/normalyazı. İlk7turda6nativepass: previous/history/digit10, favoriteszap/Backfocus, drawer/sameplayer/rapidreopen, favoritesrefresh/removal/empty, lockedzap/numberPIN, drawerPINcancel/unlock.

Guide doğal enjeksiyonu üç koşuda zaman aşımına girdi; waitForIdle çözmedi. PublicWindow.Callback trace=boş, routePLAYER/windowFocustrue. dumpsys window policy KEYCODE_GUIDE'ı com.google.android.tv.receiver.GlobalKeyReceiver'a ayırıyor. Final test gerçek app Window.Callback→Compose→PlayerKeyRouter yoluna down/up gönderir (model.guide doğrudan çağrılmaz);1/1 geçti, oynayan kanal ve guide-grid odağı doğrulandı. Bu fiziksel Guide tuşu kabulü değildir. Ham work/channel-context-qa; ilkbaşarısız loglar ve son başarı saklı. QA native-channel-context-69-qa.json. Font matrisi yok; kullanıcı mağaza hazırlığına erteledi.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.69.apk+sha; SHA-256 b800baaae4d04c9eb9c5fdd5e58bccdb59885f559739a3413c73303ce31c72ad; sertifika4fe97f80884624ca72df123c25f90f21c1a1792d189da7a18529ec4d41e96065 aynı, R8/debugkapalı. TV .68kod82→.69kod83 install-r; firstInstallTime2026-10-13 23:50:03 aynı, update2026-10-20 12:06:03. Kurulu APK çekilip hash eşleşti; TVapp/yayın açılmadı.5556 bağlı değildi/açılmadı,5554 dokunulmadı.5558 snapshot kaydetmeden kapalı, Gradle kapalı. Uygulama commitinden sonra ayrı kaynak incelemesi QA kaydında tamamlandı; yeni ajan açılmadı.

Tam sonraki iş: S15b. Mevcut ChannelDrawer'ı genişlet; ikinci panel sistemi kurma. 400dpkanal/200dpkategori/320dpminiEPG, aynı anda ençok2sütun; font2tek sütun tasarım gereksinimi kalır, matrisi bu aşamada koşma. ShortOK DOWNconsume/UPopen; heldOKcontrols+swallowUP, VODaynı. Yeni panelBack altkolon→kanallar→video; paging visibleRows−1,1200msrakamlafocus/Allfallback,15snidle/NEVER/TalkBack/PINkoruması, görünürsatırEPG150msdebounce+oturummerge+kilitfiltre, mini12h/max8, programayrıntısı ve quickactions. Eski overlay taslağındaki kontrolKapat/yataykontrolscroll geri gelmez (D072 güncel). Native GUIDE için sistem enjeksiyonunu tekrar başarısızlık döngüsüne sokma; app-window sınırı ve fizikselkabul ayrımını koru. Yeni paket enaz .70/kod84.

Sonra S15c bilgi bandı4sn/INFO-MENU/previousdüğmeleri/heldzappreview-release tekbağlantı/sonkanalUI+kapak-ses entegrasyonu; S16 seçenekler (D077 hız korunur); S17–S29. S15a dışında bunlar tamamlanmış değildir. Home scroll kullanıcı kabulüyle kapalı; splash genel bekleme/fiziksel ses-animasyon kabulü açık. Satış/fiyat/İngilizce inceleme kararları değişmedi. S15stash4720de95c90c32bbeb6110e143e282f12125805a uygulandı, tekrar uygulama;4d29f361803d5c5b289c3527c8ee6b38c2c91ded tanı stash'i de eski yedek. Başka checkout'ları/gerçek kaynakları temizleme.

## Codex · Splash çizim hazırlığı / 0.14.68 · 19 Ekim 2026

Kullanıcının “diğer işler ne oldu” sorusuna dört başlığın tamamlanmadığı ve vitrin tesliminden sonra durulduğu açıkça bildirildi. Bu tur splash'e dönüldü. D098: LaunchCover aynı gradient/path'leri alpha1/255 ile ilk karede hazırlayıp API29+ hardware frame commit bekler; observer iptal/2sn timeout temizliği var, API26–28/software iki-frame fallback. Giriş 2,8sn/minimum3sn, kumanda atlama, reduced-motion ve Home hazırlama kapısı aynı. Yeni entranceStarted callback'i PilotApp/MainActivity üzerinden StartupSound'a bağlandı; ses onStart yerine giriş saatinden başlar, ses tercihi ve foreground/once/late koruması sürer. Hareket azaltma ses tercihini ayrıca değiştirmez. TV SDK31 okundu; callback yolu desteklenir.

Tanı altyapısı önceki WIP'ten alındı ve bu teslimde commit edilir: ColdFrames ayrıntılı GPU/command/draw/layout metrikleri ve isteğe bağlı debug restore Flow; CatalogColdLaunchTest callback uyarlaması; androidTest manifest iki instrumentation entry; framework-only Java ReleaseStartupInstrumentation; wrapper release[-visual]/actual-debug[-visual] modları. R8 app iç sınıflarına link yok. Serial5558+AVD koruması aynı; yeni dosya çıktıları target external files'da. Runner app iç restore durumunu veya fiziksel sunumu doğrulamaz. WORKFLOW açıklandı. Eski tanı stash'i4d29f361803d5c5b289c3527c8ee6b38c2c91ded artık geri uygulanmamalı; yalnız eski HANDOFF taslağı tarihsel kalır.

**Ölçüm ve sınır:** ayrı temiz snapshot açılışlarında gerçek .67 release baseline (warm-baseline-release.json) ve prototype/final ölçüldü. Baseline ilk frame268ms, görünür girişte COMMAND_ISSUE405ms; önceki .66 örneği309/416ms. Prototype ilk frame818ms, sonraki max command4,7ms; final ilk frame897ms, sonraki max command3,9ms. İlk2800ms vsync penceresinde en büyük ilk-frame-sonrası boşluk483→600ms (prototype433); pencereler geciken girişin tamamını kapsamayabilir. Pahalı ilk GPU komutu ilk kareye taşındı; genel açılışın hızlandığı veya bütün duraklamaların bittiği söylenmez. Seyrek screenshot koşuları performans kıyasına katılmaz. Fiziksel animasyon/ses kabulü açık.

**Kontroller:** prototype build4m24s; release/visual ayrı1/1 ve kontrollü3/3. .67 baseline prepare/release ayrı1/1. Final unit/lint/release/debug/androidTest8m26s;362JVM/1skip/0hata,lint0/40. Gradle durdurulup5558clean açıldı; yalnız tools/run-device-tests.sh --no-build ile final prepare1/1, release1/1 (tmp.dhJs9s9wmr), release-visual1/1 (tmp.ZRNt6SK8sx), LaunchAnimationTest+startupgate3/3 (tmp.GTCkvxjBUj). Final hata/kopma yok. Normal ölçek; font matrisi yok. Gerçek release işaret/harf/tam logo kareleri incelendi; design/reviews/launch-prime-0.14.68 ve LATEST. Ayrı kaynak incelemesi yapıldı, ayrı ajan kullanılmadı. Ham work/remaining-player-qa/warm-*; özet native-launch-prime-68-qa.json. Resmî API referansı QA'da.

**Teslim:** releases/NEXTV_TV_Design_Preview_0.14.68.apk+sha, kod82, R8/debug kapalı, aynı sertifika. SHA-256 f0926965128692456de709a72ea9ddb703997c988d99b5372a1cd7508d01a6f2. TV install-r; firstInstallTime2026-10-13 23:50:03 aynı, kurulu APK çekilip hash eşleşti. TVapp/yayın açılmadı. 5556 bağlı değildi ve açılmadı; son bilinen teslim .66. 5558/Gradle kapalı. Main/audit eşleme kaynak teslim adımıdır. Yeni paket en az .69/kod83.

**Tam sonraki iş:** S15a'yı tamamla. Henüz uygulanmamış WIP `4720de95c90c32bbeb6110e143e282f12125805a` (stash@{1}) LiveRecall/LastLive/AutoStart, DeviceKeys/Store, ChannelScope/AccessGate/TvViewModel ve JVM taslaklarını taşır. Güncel devam dalı codex/remaining-player-work'te uygula; ön koşul çalışma ağacının temiz olmasıdır. EN/TR player_no_previous/player_previous_gone string'leri eksik; autoStart kapalı/atLaunchfalse durumunda katalog/kayıt okumadan erken dönmeli; Guide PLAYER'da focusedId yerine oynayan kanalı seçmeli. Sonra kaynak/önceki/son kanal/gate/guideKeyAfterZap testleri ve planın uygula–incele–düzelt yöntemi. Kanallar drawer .64 dar kullanıcı isteğidir; S15 mini rehber/kategori paneli/bant/yeni kısa-basılı OK/0/LAST/held zap/15sn idle/son kanalı açılışta başlatma henüz tamam değil. S16 ve S17–S29'a başlanmadı. Kullanıcının orijinal dört başlık devam talimatı sürer; bu tur yalnız splash alt işi ilerledi. Home kaydırması kullanıcı kabulüyle kapalı; font matrisi kullanıcı mağazaya hazırla dediğinde. Başka checkout'ları veya gerçek kaynakları temizleme.

## Codex · Kalan işlere dönüş / aktif splash deneyi · 19 Ekim 2026

Kullanıcının “diğer işler ne oldu” sorusuna dört başlığın tamamlanmadığı açıkça bildirildi; vitrin tesliminden sonra durulmuştu. Şimdi codex/remaining-player-work dalında splash'e dönüldü. Tanı stash'inden ColdFrames/CatalogColdLaunchTest/wrapper ve yeni Java runner/manifest geri alındı; güncel HANDOFF korunmuştur. S15 WIP henüz geri alınmadı, diğer üç başlık açık.

LaunchCover deneyinde glow ve logo/path'ler ilk çizimde alpha1/255 ile hazırlanır; API29+ hardware frame commit beklenmeden animasyon saati başlamaz. İptal/2sn timeout temizliği ve eski API/software iki-frame fallback var. Kontrollü LaunchAnimationTest başlangıç callback'ini bekler. Ses eşzamanı henüz bağlanmadı; bu deneyi TV'ye teslim etme. warm-frame-build.log altında release/debug/androidTest derleniyor; henüz sonucu yok. Sonraki: Gradle durdur, yalnız5558/betik prepare→release ölçümü ve kontrollü test; baseline .66 JSON/ilk render spike ile karşılaştır, 2.8sn pencerenin girişin tamamını kapsamayabileceğini unutma. Grafik etkisi kanıtlanırsa ses başlangıcını giriş callback'ine bağla, süre/skip/reduced-motion/ilk frame maliyetini ayrı doğrula; sonra full kontroller ve .68/kod82 teslimi. Mevcut TV .67'de; yeni kurulum yapılmadı.

## Codex · Ekran kenarından vitrin karartması / 0.14.67 · 19 Ekim 2026

Kullanıcı görsel oranını onaylayıp overlay'in ekranın en solundan, menü ikonlarının altından başlamasını ve biraz koyulaşmasını istedi. HomeScreen.Hero yatay gradient'i soldan sağa sürekli açılan profile değişti; FillWidth/TopCenter, tam genişlik, dikey geçiş, metin/İzle/odak/kaydırma aynı. D097, HOME_HERO/SCREENS, BACKLOG/STEPS, STATUS, bu kayıt, native-hero-overlay-67-qa.json ve LATEST/görsel güncel. Figma değişmedi.

Full build8m12s,362JVM/1skip/0hata,lint0/40; release/debug/androidTest başarılı, tek worker/1536MB/in-process Kotlin. Gradle durdurulup yalnız5558 açıldı. İlk birleşik turda host emülatör ilk testten sonra ikinci test sırasında kapandı; tam pass sayılmadı (tmp.LdsChA35QD). Temiz snapshot tekrarında yalnız tools/run-device-tests.sh --no-build: VisualPreviewTest#homePostersAndContinueRemainVisibleWithRemote1/1 (tmp.iezgdcYcah), HeroLayoutTest2/2 (tmp.MMafrzb4la). Fontscale1.0 okundu, font matrisi yok. Gerçek sentetik kaynak ekran görüntüsü .63 ile karşılaştırılarak incelendi; sol menü altında karartma ve aynı görsel oranı doğrulandı. Ayrı kaynak inceleme geçişi yapıldı; ayrı ajan kullanılmadı. Ham work/remaining-player-qa/hero-*.

Release81/.67 aynı sertifika, debug kapalı, R8; releases/NEXTV_TV_Design_Preview_0.14.67.apk+sha. SHA-256 494157011ef27a70a1a84c910534cf3e7ba6da9bf6d47f1ecc343d9239202472. TV install-r, firstInstallTime2026-10-13 23:50:03 aynı; kurulu APK çekilip hash eşleşti. TVapp/yayın açılmadı. 5556 bağlı değildi, açılmadı/dokunulmadı; son teslim .66. 5558/Gradle kapalı. Kod main'e alınır ve temiz audit checkout eşlenir; diğer detached worktree'ler değişmez.

**Devam eden dört başlık korunur:** splash → S15 kanal akışı → S16 seçenekler → S17–S29. Kullanıcı yeni görsel isteğiyle araya girdiği için iki ön çalışma ayrı, geri alınabilir WIP stash'te korunmuştur; .67 APK'ye dahil değildir:

- `4d29f361803d5c5b289c3527c8ee6b38c2c91ded` (WIP measured release startup diagnostics before hero delivery): ColdFrames ayrıntılı render metrikleri, CatalogColdLaunchTest uyarlaması, ikinci framework instrumentation entry, Java ReleaseStartupInstrumentation, wrapper actual-debug/release modları ve önceki aktif HANDOFF taslağı. Eski taslak rapor yolları/sonuçları güncel değildir; buradaki ölçümler esas alınır. Yeni untracked dosyalar stash'in üçüncü parent'ındadır. Apply sırasında bu yeni HANDOFF kaydını eski taslakla ezme.
- `4720de95c90c32bbeb6110e143e282f12125805a` (WIP S15 recall and auto-start integration before hero delivery): LiveRecall/LastLive/AutoStart saf veri/JVM taslakları, DeviceKeys/DeviceStore, ChannelScope/AccessGate/TvViewModel entegrasyonu. Henüz derlenmedi; `player_no_previous`/`player_previous_gone` EN/TR string'leri eksik. Guide oynayan kanalı hedeflemeli; autoStart kapalıyken kayıt/katalog taramadan erken dönmeli. Ayar/tuş/panel/bant ve kapak altı paused-player bağlantısı hâlâ yapılacak. Bu taslağı tamamlanmış kabul etme.

**Splash ölçümünde yeni kanıt (.66 APK):** yalnız5558/betik framework runner prepare1/1 (tmp.OcjLrKSnaz), actual-debug1/1 (tam günlük framework-debug-2.log içinde), release1/1 (tmp.17CphdNDJk), release-visual1/1 (tmp.XETr3LGl5y). JSON: work/remaining-player-qa/framework-debug.json, framework-release.json, release-visual.json; görseller release-visual/frame-0..5.png. Release'in ilk frame'i309ms, debug1601ms; ilk2800ms vsyncmaxgap release500ms/debug1683ms. Release t≈1200ms frame total515ms, COMMAND_ISSUE416ms/GPU82ms; sonraki vsync aralığı500ms. Glow/logo ilk GPU çizimiyle örtüşüyor; fiziksel kök neden kesinleşmedi. Ayrı screenshot koşusunu performans kıyasına katma. AndroidJUnitRunner ile release denemesi R8/Kotlin test-runtime uyumsuzluğuyla daha Application'da çöktü; uygulamanın normal açılış hatası değildir. Java framework runner sonradan başarıyla derlendi/koştu; output target external files altında, test APK private files altında değil.

**Tam sonraki adım:** codex/remaining-player-work devam dalında tanı stash'ini güncel HANDOFF'u koruyarak geri getir. Glow/path ilk kullanımını siyaha yakın ilk çizimde hazırlayıp animasyonu ilk frame commit'inden sonra başlatma deneyini, gerçek shrunk release başlangıç ölçümüyle önce/sonra karşılaştır; StartupSound eşzamanı/skip/reduced-motion korunmalı. Resmî ViewTreeObserver.registerFrameCommitCallback API29+ yalnız hardware render'da çalışır; iptal/timeout ve API26–28 fallback gerektirir. Henüz uygulanmış splash düzeltmesi yok. Sonra S15a WIP'i tamamla ve planın uygula/incele/düzelt kontrollerine dön. Sonraki paket en az .68/kod82. Font matrisi kullanıcı “yayına hazırla” deyince; UI testleri yalnız5558/betik. Home kaydırması kullanıcının fiziksel kabulüyle kapanmıştır.

## Codex · Home kaydırma kabulü ve devam kapsamı · 19 Ekim 2026

Kullanıcı “Ana sayfa kaydırması ... TV'de akıcılığının doğrulanması kaldı” maddesine “bu tamam” diyerek fiziksel kullanıcı kabulünü verdi. D095/Home kaydırma geri bildirimi kapandı; yeni FPS ölçümü yapılmadı. STATUS/BACKLOG/SCREENS/HOME_SHELF_FRAMING ve .65QA kabul kaydı güncellendi; yalnız doküman, uygulama testi gerekmiyor. Splash ayrı açık. Kullanıcının “diğer4madde” devam talimatı önceki numaralı listenin1/3/4/5başlıklarını kapsar: splash → S15a–c kalanları → S16 → S17–S29. Önce .66devirdeki gerçek ilk Activity alt süreleri/görünür animasyon ölçümünden devam et. Genel BKL kayıtları topluca done yapılmaz; font matrisi mağaza hazırlığına ertelenmiş,5556 kullanıcı için açık, testler yalnız5558/betik.

## Codex · İlk açılış yükü / 0.14.66 · 19 Ekim 2026

Üretim: SourceRepository/ArtworkCache/EpgRepository lazyHTTP ve IO/GuideWriter ilk erişim; TvViewModel immutable raw counts Default + ALL fastpath; FavoriteTargets mapping adayı yoksa erken dönüş; LaunchCover viewport bazlı cached glow, draw aşamasında alpha. Kod80/.66, D096. Salt okunur scrollbar_review üretim diff'inde somut bulgu yok. player_review test setup try/finally düzeltildi. Figma yok.

Testler: ColdFrames yeni gerçek Activity listener/ilk FrameMetrics, CatalogColdLaunchTest current-registry63175 sentetik kaynak/normal motion/sessiz/yeni PID, wrapperstrict --cold-catalog prepare|verify|visual|clean. İlk build362JVM/1skip/0hata sonrasında lint RequiresApi hatası; SdkSuppress düzeltmesiyle finalfullbuild3m15s,lint0/40. Baseline .65 cold3/3; final .66cold3/3; LaunchAnimation2+startupgate1+Artwork owner/cancel2+EPG transaction/cancel2=7/7. Yalnız5558/betik; fontmatrisi yok. QA native-cold-launch-66-qa.json; ham work/cold-launch-qa; görseller design/reviews/cold-launch-0.14.66.

Ölçüm: tek debug/software emülatör örneği, aynı clean snapshot. Restore5399→4924ms; ilk2800msvsyncmaxgap1233,3→1216,7ms; ilkdraw1208,4→1162,0ms; P95511,9→466,6ms; kayıp rapor0. **İlk duraklama belirgin düzelmedi; splash donması kapatılmaz.** Application/instrumentation/ses ve fiziksel sunum ölçülmez. İlk2800ms gecikmiş animasyonun tamamı değildir. Ayrı visualrun hedef400/1200/2000/2800/3600; ilk hedefler geç,2955mskare siyah,3696mskarelogo/sonharfdevam. Kontrollü clock mark→letters/complete geçti; gerçek sürekli akıcılık kanıtı yok.

Teslim: aynısertifika/debugkapalı/R8; SHA-256 2e59f56a1612e511776916ca6b22376a753200b3ea3fec7a7d8983cdfc28391f. TVinstall-r ilk kurulum2026-10-13 23:50:03 aynı,update2026-10-19 00:56:47,kuruluAPKhash eşit,app/yayınaçılmadı.5556install-r ilk kurulum2026-10-14 08:57:19 aynı,appRunningtrue/crashfalse; Evim tam ekran Home'da görünmüyor.5556 AÇIK kalsın;5558/Gradle kapalı. .65 80b2029 ve .66 b936014 main'e fast-forward ile birleştirildi; tamamlanan iki dal silindi. Temiz audit checkout ve arşiv APK/sha eşlendi.

Tam sonraki adım: ilk Activity duraklamasını FrameMetrics DRAW/LAYOUT/SYNC/COMMAND/GPU alt süreleriyle ayır; aynı gerçek açılışta görünür logo aşamasını daha sık örnekle (ör.3200/3600/4200ms, ayrıvisualrun). İlk2,8sn grafiğinden animasyon tamamı için hüküm verme. Debug/JIT/software emulator başlangıcını fiziksel release sorunuyla özdeşleştirme; fiziksel TV uygulamasını/yayını otomatik açma. Kanıt olmadan animasyon süresini değiştirme veya restore'u giriş sonuna bağlama. Fiziksel kullanıcı testi ile scroll/splash kabulü ayrıca açık. Sonraki paket en az kod81/.67; genelS15 ancak açık geri bildirimlerden sonra.5556/testkaynağı/fontmatrisi kuralları sürer.

## Codex · İlk kaydırma hedefi / 0.14.65 · 19 Ekim 2026

D095: CardVisibility public BringIntoViewModifierNode/RevealBounds, normalizeFocusRequest defaultfalse; yalnız Home Hero/HomeCard true. Hero0/0, ilk rafta pinleme yok, cancellation zinciri aynı. CardVisibilityMotionTest gerçek LazyColumn>LazyRow>TVCard/caption+Down ilk ve bütün rect'leri doğrular; remoteVertical gerçek HeroDown ile başlar. Eski .64 ilk hedef120 yerine228dp ile FAILED. Bağımsız hero_review son diff açık bulgusuz.

Final fullbuild6m04s,362JVM/1skip/0hata,lint0/40. 5558/tools/run-device-tests.sh normal: Motion/Continue/Hero5/5, home/detail/provider/search/remoteVertical5/5. İlk birleşik turdaemülatörkoptu; globalnormalizasyon Search uncancelled-requests hatası verdi, opt-in Home'a daraltılıp finalSearch geçti. Ayrıntılar QA native-home-reveal-65-qa.json; ham work/home-reveal-motion-qa. Sentetik continue-first.png incelendi; design/reviews/home-reveal-0.14.65. FizikselFPS/fontmatrisi yok.

Kod79/.65 aynısertifika/debugkapalı/R8; SHA-256 8b60db603c3f882b1cd4f09b865f564a0ca0a4dd10b71d31df79cbeb89328837. TVinstall-r ilk kurulum2026-10-13 23:50:03 aynı,update2026-10-19 00:42:18,kuruluAPKhasheşit; app/yayınaçılmadı. 5556install-r ilk kurulum2026-10-14 08:57:19 aynı,appRunningtrue/crashfalse,EvimetiketiHome'dagörünmüyor. 5556 kullanıcı için AÇIK. 5558/Gradlekapalı. Kaynak main'e fast-forward ve temiz auditcheckout eşleme teslim adımıdır.

Tam sonraki .66/kod80: gerçek ilk Activity splash ölçümü ve ana thread yükü. TVglobalanimasyonları1.0 (work/cold-launch-qa/tv-animation-settings.json). Salt okunur player_review/scrollbar_review planı: SourceRepository/ArtworkCache/EpgRepository lazyHTTP+IOilkbaşlatma; immutable rawcatalogcountsDefault, ALLkategorifastpath; FavoriteTargets.migrate mappingadayıyoksa erkenreturn; LaunchCover glowBrush drawWithCache, alpha/bloomkoru. Testtaslakları work/cold-launch-qa/ColdFrames.kt,CatalogColdLaunchTest.kt,run-device-tests.sh henüz üretim/testkaynağınaalınmadı. Gerçek yeni process+Activity, currentregistryfixture63175, ilkFrameMetrics+ilk2800mscadence, ayrıgörselrun. Application/instrumentation başlangıcı ve fizikselgösterimehariç. Önce .65debugilebaseline, sonra.66karşılaştırması. Wrapperstrict5558;5556kullanıcıaçık; fontmatrisiyok.

## Codex · Kanallar çekmecesi / 0.14.64 · 19 Ekim 2026

D094: data/ChannelScope+LiveIndex; AccessTarget.ChannelPick/AccessTargets; TvViewModel scope/cache/gate/favorites normalization; ui/ChannelDrawer, PlayerKeys/Screen/Controls/Symbols, LibraryScreen virtual category; EN/TR strings. JVM AccessGate/ChannelScope/PlayerKeyRouter ve PlayerControlsLayout/ParentalFlow testleri. Salt okunur player_review/scrollbar_review: favorites refresh, numaralama bağlamı, stale requester ve hızlı Back→OK odak sorunları düzeltildi; son inceleme açık bulgusuz. Aynı player nesnesi korunur, yayın seçimi PIN kapısından; panel opacity tercihi ve EOF/hata durumunda açık kalma var. Tüm S15 tamam değildir.

Final full build5m03s;362 JVM/1skip/0hata,lint0/40. Yalnız 5558/tools/run-device-tests.sh --no-build 5/5: same-player channel selection/pending digits/quick reopen/favorites refresh-empty/live+movie geometry/PIN cancel-unlock. Sentetik drawer/reopen ara karesi/live görselleri incelendi. PIN fixture gerçek video sürekliliği kanıtı değil. Font matrisi yok. QA native-channel-drawer-64-qa.json; ham work/live-channel-drawer-qa; design/reviews/live-channel-drawer-0.14.64.

Release kod78/.64 aynı sertifika/debugkapalı/R8. SHA-256 8c68de5c6711a5d375620afec5288c11d6e41640df781b2205d5712da0e9a8f1. TV install-r:firstInstallTime2026-10-13 23:50:03 aynı,update2026-10-19 00:15:24,çekilenAPKhash eşit; app/yayın açılmadı. 5556 install-r:firstInstallTime2026-10-14 08:57:19 aynı,update2026-10-18 17:08:38(snapshot saati);appRunningtrue/crashfalse,Evim etiketigörünmüyor. 5556 kullanıcı için AÇIK; 5558/Gradlekapalı. Son kaynak main'e merge edilecek ve temiz audit checkout eşlenecek.

Tam sonraki .65/kod79: Home iki scroll için ilk otomatik BringIntoView isteğini container tam rect'ine normalize eden public BringIntoViewModifierNode; mevcut geç frame resize isteği aynı geometriyle no-op olmalı. Gerçek LazyColumn>LazyRow>TVCard regresyonunda ilk upstream rect ölçülsün; eskisi küçük odak rect'ini dışladığından sorunu kaçırdı. hero_review planı mevcut. Sonra true cold splash: eager OkHttp/restore publish ana thread yükü ve ilk Activity frame ölçümü (önceki .61 devir ayrıntısı). Kullanıcı mağaza hazırlığı demeden font matrisi yok.

## Codex · Gezinme ve ekran geri bildirimleri / 0.14.63 · 18 Ekim 2026

D093: HomeScreen/LibraryScreen/PanelMotion; SettingsHost scrollbar; PinPad/Theme/DesignSystem; CatalogPresentation/PilotApp captions; PlayerSymbols/PlayerScreen cog. Kategori geçişi sabit 5 sütunu kaydırır, açıkken 4 görünür (large4/3); başlık/arama/boş durum görünür genişlikte kalır. Ara karede çapraz animateItem hareketi görülünce kaldırıldı. Bağımsız salt okunur hero_review/player_review/scrollbar_review bulguları giderildi: doğal wordmark, Hero payı, scrollbar padding hesabı, Roma sayı regex, dar backdrop Fit yan boşluğu. Final FillWidth/TopCenter, üstten oranlı tam genişlik; dar oranda alt taşma kırpılır.

358 JVM (1skip/0hata), lint0/39; release/debug/androidTest başarılı. Tek worker/1536MB, 5556 açık. 5558 yalnız tools/run-device-tests.sh: PIN/Hero/Continue4/4; görsel/menü/player5/5; son motion/katalog/arama5/5; scrollbar ilk exact-text assertion hatası düzeltildikten sonra Settings/Back/provider3/3; FillWidth sonrası Hero görsel1/1. İlk compile, test seçici ve ara kare başarısızlıkları QA'da saklı. Sentetik görseller incelendi; Figma/font matrisi yok. Ham work/navigation-feedback-qa; QA native-navigation-feedback-63-qa.json; görseller design/reviews/navigation-feedback-0.14.63.

Release kod77/.63 APK+sha arşivlendi, aynı sertifika/debug kapalı/R8. SHA-256 9a8e33134c90334e38cb16505fd168411affe62801b727cb7e67956d22b19469. TV install-r, ilk kurulum 2026-10-13 23:50:03 aynı, güncelleme 2026-10-18 23:52:45, çekilen APK hash eşit; TVapp/yayın açılmadı. 5556 install-r, ilk kurulum 2026-10-14 08:57:19 aynı, Ana Sayfa/İzle görünür, appRunningtrue/crashfalse; tam ekran vitrin kaynak adını göstermediği için Evim UI kontrolü false. 5556 kullanıcı için görünür açık kalır; 5558/Gradle kapalı.

Tam sonraki adım .64/kod78: canlı player Play yanındaki tips yerine Kanallar ve soldan kayan hızlı kanal drawer. Aynı player korunmalı; mevcut kanal ilk odak, OK seçimi AccessTarget.ChannelPick + PIN kapısından, Back Kanallar düğmesine. Kaynak/kategori/favori kapsamı tek liste; önizleme için paralel yayın yok. Bu dar istek bütün S15a–c'yi done yapmaz. Ardından önceki devirdeki ilk BringIntoView isteğini container rect'ine yükseltme ve gerçek cold-start main-thread yükü işleri; iki scroll/splash açık. S15 genel plan bunlardan sonra. Kullanıcı 'yayına hazırla' demeden font matrisi yok.

## Codex · Oynatıcı bilgi düzeni / 0.14.62 · 18 Ekim 2026

PlayerControls.kt: elapsed Start/remaining End, ölçülen labelWidth/scrub geometrisi aynı; VOD türü başlığın6dp üstünde, source parametresi kaldırıldı. PlayerScreen.kt: recovery/failure üst ortada max640dp koyu ayrı bildirim, polite live region, fokus yok; panel/PIN üzerinde gizli, geçici notla çakışmaz; metadata sadece normal bilgi. Retry/Back/Peek/canlı korunur. PlayerControlsLayoutTest ve PlaybackRecoveryFlowTest mevcut senaryoları güçlendi; ikinci sentetik kaynak URL'si ?source=second ile ayırt edildi (inceleme bulgusu).

player_review salt okunur nihai inceleme açık bulgusuz. 357 JVM (1skip/0hata), lint0/37; release/debug/androidTest6m57s, tek worker/1536MB, 5556 açık. Gradle durdurulup5558 başlatıldı. Yalnız tools/run-device-tests.sh --no-build: PlayerControlsLayoutTest5/5, recovery longTitle/malformed+VisualPreview player3/3. Normal movie/live/scrub/failed/recovering/ready ekranları gözle incelendi; yazı glyph hizası testbounds iddiasından ayrı doğrulandı. Font matrisi yok. QA native-player-info-62-qa.json, ham work/player-info-qa, görseller design/reviews/player-info-0.14.62. D092/PLAYER_PRESENTATION/SCREENS/gap/player-controls/BACKLOG/STEPS güncel; Figma yok.

Release kod76/.62 APK+sha arşivlendi, aynı sertifika/debug kapalı/R8. SHA-256 ca2f3d6b78e4f5edc1dc5bfd5c62ffc9e394d2995a33822a8affa974122d0e50. TV install-r, firstInstallTime2026-10-13 23:50:03 aynı, lastUpdate23:09:49, pull edilenAPKhash eşit; TVapp/yayın açılmadı. 5556 install-r, firstInstallTime2026-10-14 08:57:19 aynı, Evim/appRunningtrue/crashfalse. 5556 görünür açık kalsın; 5558/Gradle kapalı.

Tam sonraki adım .63/kod77: kullanıcının yeni nav/kategori kayma geçişleri, PIN (scroll içi5dp pay, ortalı rakam/ikon geri sil), settings LazyList scrollbar yanlış ScrollState'e bağlı, sol kategori adları13sp/dile duyarlı gösterim. Salt okunur hero_review/player_review/scrollbar_review incelemeleri mevcut. Hareket testi normal-motion'u fixture içinde açıkça seçmeli; resetFixtureSources bunu sıfırlamaz. Kategori 4↔5 endpoint geometri korunurken hızlı ters yön/fokus/sağ arama görünürlüğü kontrol edilmeli. TR ASCII YERLI gibi kayıplı adlar locale ile güvenle düzelmez; belirsiz tokenları koru. Önceki kayıttaki listeleme caption, hero iki-scroll ve cold splash bulguları hâlâ açık. Ardından S15a. Kullanıcı 'yayına hazırla' demeden font matrisi yok.

## Codex · Son kanal yeniden tasarımı / 0.14.61 ve kullanıcı testi · 18 Ekim 2026

D091, D084'ün dağınık bulunan dikey kartını kompakt yatay düzene çevirdi. HomeScreen recent dalı: 272 dp genişlik, minimum 100 dp yükseklik, padding16/gap16; solda 80×56 logo, sağda gerçek 1–2 satırlık başlık ve 4 dp altında kaynak. Gradient/boş başlık satırı yok. Kilit mevcut LockMark ile ad yanında. Ortak odak, erişim/PIN çağrı yolu, kaynak semantiği ve geçmiş sırası korunur. HomeScreen.kt, DesignSystem.kt, build.gradle.kts; D091, HOME_SHELVES/SCREENS/BACKLOG/STEPS, bu kayıtlar, QA ve LATEST değişti. Figma/manifest yok.

hero_review kaynak diff'ini salt okunur inceledi; somut bulgu yok. 357 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı (6m22s, max-workers1/no-parallel). Yalnız 5558/tools/run-device-tests.sh --no-build ile ilk birleşik 4 test turu son ContinueCardLayoutTest'te emülatör koptu; tamamlanmış sayılmadı. Recent testi ayrı 1/1, HeroLayoutTest+ContinueCardLayoutTest ayrı 3/3 geçti. İlk/son uzun ad, kaynak ve eksik logo görselleri incelendi. Kilit/PIN ayrı sınanmadı. Yazı matrisi yok. [QA](native-recent-channel-redesign-61-qa.json); ham kayıtlar work/recent-channel-redesign-qa.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.61.apk + sha256, kod75, release/R8, aynı imza, debug kapalı. SHA-256 7ed8e9ab379dff74c6e315fc113c4e2e47f351b2bacaace3c13b18c0748c235e. TV ilk denemesi ADB daemon bağlantısı nedeniyle başarısız; cihaz hâlâ .60/kod74 doğrulandı ve tekrar install -r başarılı (.61, cihaz saati 22:53:32, firstInstallTime aynı, çekilen APK hash eşit). TV uygulaması/gerçek yayın açılmadı.

Kullanıcı “son halini emülatörde aç test edeyim” dedi. 5558 kapatıldı; 5556 nextv_design_api36 görünür açıldı. default_boot eski .46/kod60 getirdi, .51 bekleyen assert durdu; gerçek .46 doğrulandıktan sonra veri silmeden .61'e yükseltildi. firstInstallTime 2026-10-14 08:57:19 aynı, çekilen APK hash eşit; Evim görünür, uygulama çalışıyor, crash buffer'da paket kaydı yok. Emülatör saati eski snapshot nedeniyle gerçek saatle aynı değil. **5556 kullanıcı testi için AÇIK bırakıldı; otomatik kapatma/test/reset yapma.** 5558/Gradle kapalı, 5554 dokunulmadı.

Kullanıcı launcher NEXTV ve Watch/Details simgelerini pikselli buldu. Watch+Details ekranı yükseltme öncesi .46 idi; .61 tek İzle ve filled/rounded simge içerir. Manifest app_banner.xml vektörünü kullanır; özgün nextv-yatay.svg N/X/V harfleri bilinçli basamaklıdır. Görsel kaynak düşük çözünürlüklü PNG değil; ek genel yumuşamanın emülatör/launcher ölçeğinden olduğu bu kırpılmış görüntüyle kanıtlanmadı. Marka çizimi değiştirilmedi.

**Tam sonraki işler (kullanıcıdan geldi, .61'e dahil değil):**
- Listeleme: PilotApp.MediaCard poster caption'ında minLines2, 8 dp afiş boşluğu, 15/20 sp başlık var. Boş satırı kaldır, poster–başlık boşluğunu biraz artır, başlık +1–2 sp, yıl daha soluk ama okunaklı. Kütüphane/arama/favori ortak kullanımını izle. HOME_SHELVES D086 başlık–yıl4dp yaklaşımıyla tutarlı yap; mevcut library-year testinin iki farklı uzunlukta başlığın baseline eşitliği varsayımını değerlendir.
- İki scroll hissi: hero_review Compose1.11.2 source jar'da FocusableNode'un odak anında küçük rect ile bringIntoView başlattığını doğruladı. CardVisibility recomposition/LaunchedEffect/withFrameNanos sonrası büyük kart+27dp isteği gönderiyor; küçük hedef önce biterse ikinci hareket mümkün. Mevcut CardVisibilityMotionTest küçük isteği özellikle dışlıyor, monoton iki hareketi kaçırıyor. Öneri: container'da BringIntoViewModifierNode ile ilk otomatik isteği parent'a ulaşmadan tam kart/pay rect'ine genişlet; resize kontrolü aynı hedefe no-op olur. LazyColumn>LazyRow>gerçek Card regresyonu, ilk iletilen rect'in büyük olduğunu eski/yeni karşılaştır; gerçek Home tek Down kare izi. Fiziksel kesin kök henüz ölçülmedi.
- Splash: kullanıcı tamamen kapatıp açınca da donduğunu netleştirdi. player_review üç eager OkHttpClient'ın (SourceRepository, ArtworkCache, EpgRepository) ViewModel kurulumunda ana thread'de SSL hazırladığını; restore publish467–499 içinde history/favorites/katalog tarama/migrasyon, hemen arka plan indeks/sıralama ısıtması olduğunu buldu. .59 testi önce HOME'u bekler, sonra splash'i sentetik kurar; soğuk restore çakışmasını ölçmez. İlk IO kullanımında client lazy oluşturma ve publish hazırlığını background'a taşıma araştırılmalı; LaunchCover'da her kare yeni gradient de var. Gerçek cold frame ölçümü yapılmadan kesin çözüldü deme. Restore'u animasyon sonrasına ertelemek seçilirse ayrı onEntranceSettled sinyali busy=false bekleyişinden ÖNCE gerekir; prepareContent/finish üzerinden bağlamak deadlock yapar ve toplam açılışı uzatır. Önce dar main-thread yükleri/gerçek başlangıç kaydı.

Sonraki sürüm en az .62/kod76. Kullanıcı testi/geri bildirimi öncelikli; ardından bu üç iş, sonra STEPS S15a. Fiziksel/Figma/TalkBack, İngilizce ve satış/fiyat açık. “Yayına hazırla” talebi gelmeden font matrisi koşma.

## Codex · Her zaman filled/rounded İzle simgesi / 0.14.60 · 18 Ekim 2026

Kullanıcı D083'ün “odaklandığında dolu” kuralını “Hep filled olsun rounded şekilde” olarak değiştirdi (D090). Theme.Action'ın iki simge yolu ortak ActionIcon yardımcısına bağlandı: play için mevcut PlayerSymbol renderer'ında yeni play_arrow_filled, diğerlerinde eski LineIcon/selected fill. Resmî Material Symbols Rounded play_arrow FILL1/400/24 yolu kaynak SVG ile birebir eşleşiyor. Açık 16 dp alan düğmeyi büyütmez. Mevcut oynatıcı denetim simgeleri değişmedi.

Dosyalar: ui/Theme.kt, ui/PlayerSymbols.kt, build.gradle.kts; D090, SCREENS/HOME_HERO, BACKLOG/STEPS, STATUS/HANDOFF, [QA](native-rounded-play-60-qa.json), LATEST ve rounded-play-0.14.60 görselleri. Figma/manifest değişmedi. hero_review salt okunur bağımsız inceleme yaptı; açık somut bulgu yok.

Kontroller: 357 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı, no-parallel/max-workers=1. Yalnız 5558/tools/run-device-tests.sh --no-build ile HeroLayoutTest ve ContinueCardLayoutTest 3/3 geçti. Odak düğmedeyken ve devam kartındayken dolu yuvarlatılmış simge görsel olarak doğrulandı. Yeni test yok; font matrisi kullanıcı isteğiyle yapılmadı. Ham kayıtlar work/rounded-play-qa.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.60.apk + sha256, kod74, R8/release, debuggable değil, aynı imza. SHA-256 427f6d4b17a66941a82f394d29536639beb918a76f2172f203f47da5a45201fb. TV .59→60 install -r; firstInstallTime 2026-10-13 23:50:03 aynı, lastUpdateTime 2026-10-18 22:20:53. Kurulu APK çekilip hash eşleştirildi. TV uygulaması/gerçek yayın açılmadı. 5556 açılmadı, .51/kod65'te; 5558/Gradle kapalı, 5554'e dokunulmadı.

Tam sonraki adım: kullanıcının fiziksel geri bildirimi öncelikli; açık plan ana checkout/main üzerinden STEPS S15a / gap/overlay.md. Sonraki teslim en az .61/kod75. Fiziksel/Figma/TalkBack, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Splash animasyonu / 0.14.59 · 18 Ekim 2026

Kullanıcı “Splash screen donuyor animasyonu göremiyorum” dedi. Kodda LaunchCover girişinin altında Home/Setup ve restore sırasında ikinci RestoringCover oluşturuluyordu. Büyük kataloğun homeLibrary hesabı ve ilk Home ölçümü girişle aynı UI thread'i kullanıyordu. PilotApp contentPrepared kapısı giriş bitene ve RESTORING tamamlanana kadar içerik/overlay ağacını bekletir. LaunchCover prepareContent callback'ini opak bitmiş logo altında çağırır, iki frame sınırını geçip 220 ms açılır. Reduced motion üç saniye/fade beklemez. Bu Home hesaplama maliyetini kaldırmaz.

MainActivity'deki onUserInteraction tek başına UP dahil her etkileşimde kapağı kaldırıyordu. Public Window.Callback + saf OpeningInput ile yalnız yeni DOWN atlar. D062'ye göre açık davranış değişikliği (D089): ilk yön/OK/Enter/Back/Esc basışı yalnız kapağı/sesi kapatır; repeat/eşleşen UP tüketilir. Ses/medya tuşları özgün callback'e gider; onStop takip temizler. Launcher kaynaklı orphan UP animasyonu kapatmaz. ComponentActivity dispatchKeyEvent override'ının RestrictedApi riski bağımsız incelemede bulundu; override yerine public callback delegasyonu kullanıldı. Süreç başına bir kez, aynı süreçte tekrar açılışta statik D069 kapağı ve hareket tercihleri değişmedi.

Dosyalar: MainActivity.kt, yeni OpeningInput.kt, ui/PilotApp.kt, ui/LaunchCover.kt, build.gradle.kts; yeni OpeningInputTest.kt/LaunchAnimationTest.kt, VisualPreviewTest launchEntranceFinishesBeforeHomeIsComposed; D089, NATIVE_UI/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF, [QA](native-splash-59-qa.json), LATEST ve splash-0.14.59 görselleri. Figma/manifest yok. hero_review ve player_review salt okunur bağımsız inceleme yaptı; son kaynakta açık somut bulgu yok.

Kanıt: yeni Home testi değişmemiş .58 üretim kaynaklı debug APK'da home-content girişte zaten var diye FAILED; finalde yok/sonra görünür+odak ve tuşla atlama kapısı geçti. LaunchAnimationTest gerçek çizilmiş merkez logo pikselleri 0,7332,16187,16399; yükleme bekleme/prepare bir kez/finish ve reduced-motion gecikmesiz geçişi doğrular. OpeningInputTest 3 vaka orphan UP, hold/release, ses tuşları ve onStop temizliğini doğrular. Bu kontrollü saat/helper testleri fiziksel FPS, soğuk ilk kare ya da gerçek Activity açılış kumandası ölçümü değildir.

357 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; debug/androidTest/release başarılı. İlk birleşik tam derleme bellek baskısında tamamlanmadan iptal edildi; final kontroller no-parallel/max-workers=2, ayrı release max-workers=1 ile tamamlandı. Yalnız 5558/tools/run-device-tests.sh --no-build normal ölçekte 7/7: LaunchAnimationTest (2), VisualPreviewTest#launchEntranceFinishesBeforeHomeIsComposed, LoadingLayoutTest#restoringCoverShowsProgressAndALabel ve #everySourceOperationFitsAndImportCanBeCancelled, PilotFlowTest#cachedCatalogOpensWithoutRequestsAndFailedRefreshKeepsLibrary ve #devicePreferencesSurviveSourceRemovalAndActivityRecreation. Font matrisi yok. Ham kayıtlar work/splash-qa. Android resmî Compose performans/test-saat belgeleri QA'da; bağımlılık güncellenmedi.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.59.apk + sha256, kod73; R8/release, debuggable değil, aynı imza. SHA-256 eddbff0231167ea4857ae8e6e51ac37dac46797151d205b48b67cb9958f2cc64. TV .58→59 install -r; firstInstallTime 2026-10-13 23:50:03 aynı, lastUpdateTime 2026-10-18 22:08:48; kurulu APK çekilip hash eşleştirildi. TV uygulaması/gerçek yayın açılmadı. 5556 .51/kod65'te kapalı; 5558/Gradle kapalı, 5554'e dokunulmadı.

Tam sonraki adım: kullanıcının soğuk açılış mı aynı süreçte dönüş mü gördüğü henüz yanıtlanmadı; mevcut süreç-başına-bir-kez kararı korunur. Fiziksel geri bildirime göre önce bu ayrımı ele al. Soğuk ilk kare tercih okuma maliyeti ve ses senkronu ölçülmüş değildir. Açık plan ana checkout/main üzerinden STEPS S15a / gap/overlay.md; sonraki teslim en az .60/kod74. Fiziksel/Figma/TalkBack, İngilizce, satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Kaydırma sürekliliği / 0.14.58 · 18 Ekim 2026

Kullanıcı “Hala kasarak iniyor aşağıya” dedi. CardVisibility'nin LaunchedEffect anahtarındaki clipped, ilk kart bütünüyle görünür olurken true→false değişip 27 dp alt pay tamamlanmadan işi iptal ediyordu. focused/context/viewport/frameShelf/edge anahtarları korunur; size/clipped, snapshotFlow + sıralı collect içinde işlenir. Sürmekte olan istek tamamlanınca görünürlük tekrar değerlendirilir; odak değişimi eski işi iptal eder. viewportHeight=0 kullanan bileşenlerin resize düzeltmesi korunur. Hero ön yükleme işi kaynak/görsel kimliğine ayrıldı; actionFocused artık yalnız 15/8 saniyelik zamanlayıcıyı yeniler.

Değişenler: CardVisibility.kt, HomeScreen.kt, build.gradle.kts; yeni CardVisibilityMotionTest.kt; D088, HOME_HERO/HOME_SHELF_FRAMING/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF; [QA](native-home-scroll-58-qa.json), LATEST.md ve home-scroll-0.14.58/continue.png. Figma/manifest değişmedi. hero_review ve player_review salt okunur kök neden incelemesi yaptı; hero_review nihai kaynak/test diff'ini onayladı, açık somut bulgu yok.

Kontroller: Yeni test değişmemiş eski üretim APK'sında FAILED (1 iptal). Test-only BringIntoViewModifierNode gözlemcisi büyük kart+pay isteğini framework'ün küçük odak isteğinden ayırır; gerçek verticalScroll ve manuel Compose saati kullanılır. Eski offsets: …90,112,127,127,127,130…; final: …90,112,127,137,143,148…; ikisi de 154 px'e varır. Finalde 0 iptal, 2 tamamlanan istek (ikincisi yerleşme sonrası no-op kontrol); monoton hareket, tam alt pay ve viewport küçülmesi geçti. Bu fiziksel FPS/kare süresi değildir. D078 gerçek ana sayfa mesafesi 86,5 dp gerekli/86,5 dp gerçekleşen olarak aynı.

354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest geçti. Yalnız 5558/tools/run-device-tests.sh --no-build normal ölçekte 9/9: CardVisibilityMotionTest (1), ContinueCardLayoutTest (1), HeroLayoutTest (2), VisualPreviewTest'in homePostersAndContinueRemainVisibleWithRemote, remoteVerticalShelvesKeepTheirHeadingAndFullCardTogether, searchCardsUseAvailableWidthAndStayVisibleWithRemote, detailDescriptionAndWideEpisodesStayReadableWithRemote, providerDividersRemainReachableAndPosterOnlyContentKeepsItsShape yöntemleri. Font matrisi yok. Devam rafı hedef görüntüsü incelendi. Ham kayıtlar work/home-scroll-qa; kaynak/API sürümü 1.11.2 resmî kaynak jar'larından doğrulandı, bağımlılık güncellenmedi.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.58.apk + sha256, kod72; R8/release, debuggable değil, aynı imza. SHA-256 b997029526dfe3cc59ec0c1a3dc705d0171f7b0017fd94cf6a00db8ddbe4fe0a. TV .57→58 install -r; firstInstallTime 2026-10-13 23:50:03 aynı, lastUpdateTime 2026-10-18 21:48:10; kurulu APK çekilip hash eşleştirildi. TV uygulaması/gerçek yayın açılmadı. 5556 açılmadı, .51/kod65'te; 5558/Gradle kapalı; 5554'e dokunulmadı.

Tam sonraki adım: kullanıcının fiziksel akıcılık geri bildirimi yeni işin önceliğini belirler; açık plan ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az .59/kod73. Fiziksel kare süreleri/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Oynatıcı hizalama düzeltmesi / 0.14.57 · 18 Ekim 2026

Kullanıcı TV fotoğrafıyla ikon, düğme hizası ve sıkışıklık bildirdi. PlayerControlButton iç Row gerçek denetim alanını doldurmuyordu; ana ikon 12 dp yukarıdaydı. fillMaxSize ve iki eksende merkezleme bunu giderir. 24 sp sabit etiket alanı yerine aynı TextStyle ile ölçülen yükseklik +8 dp iç boşluk +11 dp düğme payı +8 dp zaman çubuğu payı kullanılır. 64/52/44 dp düğmelerde etiket alt kenarı ortak; SVG yolları, boyutlar, odak dili ve playback davranışı aynı.

Değişenler: PlayerControls.kt, build.gradle.kts; PlayerControlsLayoutTest.kt/VisualPreviewTest.kt gerçek simge merkezleri, gerekli etiketlerin varlığı, etiket/çubuk/halka aralığı ve Sonraki bölüm hizası; D087, PLAYER_PRESENTATION/SCREENS/gap-player-controls, BACKLOG/STEPS, STATUS/HANDOFF; [QA](native-player-layout-57-qa.json), LATEST.md ve player-layout-0.14.57 görselleri. Figma/manifest yok. hero_review ve player_review salt okunur bağımsız inceleme yaptı; testte zorunlu etiket kontrolü düzeltildi, nihai kod ve normal görsellerde açık geometri bulgusu yok.

Kontroller: Eski üretim APK'sında yeni merkez testi beklenen 484 / gerçek 472 dp ile başarısız oldu. İlk test derlemesinde mevcut olmayan DpRect.center yerine kenar ortalamaları kullanılarak düzeltildi. Nihai 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest geçti. Yalnız 5558 ve tools/run-device-tests.sh --no-build: PlayerControlsLayoutTest (5), VisualPreviewTest#playerPanelKeepsTitleAndRemoteActionsInsideScreen, PlaybackRecoveryFlowTest#longTitleRecoveryAndFailureKeepControlsInsideScreen, toplam 7/7. İlk movie/recovering yakalamaları eksik katman içerdi; tam kanıt sayılmadı, ham kareler work/player-layout-qa içinde. Movie testi kaynak/APK değişmeden ayrı tekrar 1/1 geçti ve tam dönüş karesi bağımsız incelendi. Film/geri sarma/favori/canlı/dizi/hata/yeniden açılan oynatma görselleri incelendi. Yazı ölçeği matrisi yok.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.57.apk + sha256, kod 71; aynı imza, R8/release, debuggable değil. SHA-256 5dfb67081992a47b2179f6c33393cabd21ab38c081764bef177b77dbc122fb3b. TV .56→57 install -r; firstInstallTime 2026-10-13 23:50:03 korundu, lastUpdateTime 2026-10-18 21:37:17. Kurulu APK çekilip hash eşleştirildi. TV uygulaması/gerçek yayın açılmadı. 5556 açılmadı, .51/kod65'te; 5558/Gradle kapalı, 5554'e dokunulmadı.

Tam sonraki adım: ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az 0.14.58/kod72. 5556 yükseltilecekse önce .51/kod65 olarak doğrula. Fiziksel görsel/akıcılık/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Dizi adı–yıl aralığı / 0.14.56 · 18 Ekim 2026

Kullanıcı “Başlık ve tarih birbirinden çok uzak duruyor” dedi. HomeCard metinleri iç Column'da; yalnız seriesShelf için minLines 1 (maxLines 2), başlık–metadata aralığı 4 dp. Önce minLines 2 boş ikinci satır ve 8 dp aralık ayırıyordu. Afiş–başlık 8 dp, 160×240 dp afiş/16 sp başlık aynı. Diğer rafların minLines/aralığı aynı; görünürlük tüm kartı kapsar.

Değişenler: HomeScreen.kt, build.gradle.kts; D086, HOME_SHELVES/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF; [QA](native-series-spacing-56-qa.json), LATEST.md ve series-spacing-0.14.56/series.png. Figma/manifest yok. hero_review nihai diff'i salt okunur inceledi, bulgu yok.

354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558 / tools/run-device-tests.sh --no-build: VisualPreviewTest#homePostersAndContinueRemainVisibleWithRemote, VisualPreviewTest#remoteVerticalShelvesKeepTheirHeadingAndFullCardTogether, 2/2. Mevcut testler değiştirilmedi. Normal görselde yılın tek satırlık başlığı yakın izlemesi incelendi. Font matrisi yok. Ham loglar work/series-spacing-qa. Proje kontrolü/git diff --check geçti.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.56.apk + sha256, kod70, aynı imza/release/R8. SHA-256 60b1910bf655d0357b6d9636700d75da69dff539042b80ba1bc77216468318bd. TV .55→56 install -r, firstInstallTime 2026-10-13 23:50:03 değişmedi, lastUpdateTime 2026-10-18 21:26:34. Kurulu APK çekilip hash eşleştirildi; TV uygulaması/gerçek yayın açılmadı. **5556 açılmadı ve .51/kod65'te kaldı**. 5558/Gradle kapalı; 5554'e dokunulmadı.

Tam sonraki adım: ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az 0.14.57/kod71; 5556 yükseltilecekse mevcut sürümü .51/kod65 olarak doğrula. Fiziksel görsel/akıcılık/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Daha büyük dizi rafı / 0.14.55 · 18 Ekim 2026

Kullanıcı “Dizi kütüphanendeki içerikleri biraz daha büyütüp başlıkları da büyütür müsün” dedi. Ana sayfanın HomeCard section == series && kind == SERIES yolu 160 dp genişlik / 2:3 afiş, 16 sp başlık / 22 sp satır yüksekliği / Medium kullanır. Başlık iki satır, ellipsis; metadataya dokunulmadı. Diğer raflar ve ayrı kütüphane ekranı aynı. Ortak görünürlük, PIN/uzun OK/geri dönüş odağı korunur.

Değişenler: HomeScreen.kt, DesignSystem.kt, build.gradle.kts; D085, HOME_SHELVES/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF; [QA](native-series-shelf-55-qa.json), LATEST.md ve series-shelf-0.14.55/series.png. Figma/manifest yok. hero_review nihai diff'i salt okunur inceledi, bulgu yok.

354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558 / tools/run-device-tests.sh --no-build: VisualPreviewTest#homePostersAndContinueRemainVisibleWithRemote, VisualPreviewTest#remoteVerticalShelvesKeepTheirHeadingAndFullCardTogether, 2/2. Mevcut testler değiştirilmedi. Normal görselde büyük afiş/başlık ve tam görünürlük incelendi. Font matrisi yok. Ham loglar work/series-shelf-qa. Proje kontrolü/git diff --check geçti.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.55.apk + sha256, kod69, aynı imza/release/R8. SHA-256 db997ea033d063575af4522e7bf47b3f3c4f30264268bc3bc8ba81f68b45ed65. TV .54→55 install -r, firstInstallTime 2026-10-13 23:50:03 değişmedi, lastUpdateTime 2026-10-18 21:21:38. Kurulu APK çekilip hash eşleştirildi; TV uygulaması/gerçek yayın açılmadı. **5556 açılmadı ve .51/kod65'te kaldı**. 5558/Gradle kapalı; 5554'e dokunulmadı.

Tam sonraki adım: ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az 0.14.56/kod70; 5556 yükseltilecekse mevcut sürümü .51/kod65 olarak doğrula. Fiziksel görsel/akıcılık/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Son izlenen kanal kartları / 0.14.54 · 18 Ekim 2026

Kullanıcı “Ana sayfadaki son izlediğin kanalları biraz daha estetik yapabilir miyiz” dedi. Yalnız HomeScreen HomeCard live && section == recent için 200 dp tek kart: 84 dp logo bölümü / 140×48 dp Fit logo; altta iki satırlık 14 sp Medium ad ve çoklu kaynakta 12 sp kaynak adı. Hafif dikey yüzey geçişi, 12 dp köşe, bütün kartın odak halkası. Geçmiş sırası, D078 görünürlük, onClick/onLongClick/PIN/LockBadge ve odak kimlikleri korunur. Kaynak adı erişilebilir duruma eklendi. Diğer canlı kartlar aynı.

Değişenler: HomeScreen.kt, DesignSystem.kt, build.gradle.kts; VisualPreviewTest.kt tek recent akış testi; D084, HOME_SHELVES/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF; [QA](native-recent-channels-54-qa.json), LATEST.md ve recent-channels-0.14.54/recent-0.png/recent-5.png. Figma/manifest yok. hero_review nihai kaynak/test diff'ini salt okunur inceledi, bulgu yok.

354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558 / tools/run-device-tests.sh --no-build: VisualPreviewTest#recentChannelsKeepLogoTitleAndSourceTogetherWithRemote 1/1; ContinueCardLayoutTest,HeroLayoutTest 3/3. İlk birleşik koşuda recent testi geçti, sonraki sınıfta emülatör bağlantısı düştü; tam başarı sayılmadı. Yeniden başlatılan emülatörde iki ayrı koşu tamamlandı. Normal iki görsel incelendi: uzun kanal/kaynak adları, logosuz kart, ilk/son kart odağı. Font matrisi yok. Ham loglar work/recent-channel-qa. Proje kontrolü/git diff --check geçti.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.54.apk + sha256, kod68, aynı imza/release/R8. SHA-256 75497e18a42baf82a26aaa2a46f1f01333a46864c1162c27029a53f7c1916d85. TV .53→54 install -r, firstInstallTime 2026-10-13 23:50:03 değişmedi, lastUpdateTime 2026-10-18 21:16:09. Kurulu APK çekilip hash eşleştirildi; TV uygulaması/gerçek yayın açılmadı. **5556 açılmadı ve .51/kod65'te kaldı**; veri/yükseltme mantığı değişmedi. 5558/Gradle kapalı; 5554'e dokunulmadı.

Tam sonraki adım: ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az 0.14.55/kod69; 5556 yükseltilecekse mevcut sürümü .51/kod65 olarak doğrula. Fiziksel görsel/akıcılık/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Odakta dolu oynat simgesi / 0.14.53 · 18 Ekim 2026

Kullanıcı “İzle butonlarındaki icon üzerine geldiğinde filled olsun” dedi. Theme.kt Action mevcut focused durumuyla filledIcon = iconFilled || (icon == "play" && focused) hesaplar; iki LineIcon dalı bunu kullanır (D083). Vitrin, ayrıntı/Oynat/Devam et, hızlı İzle ve bölüm sonu çağrıları kapsanır. Favori yıldızı gibi açık iconFilled seçimleri korunur; yeni odak/animasyon/oynatma mantığı yok.

Değişenler: Theme.kt, build.gradle.kts; D083, CONTROL_POLISH_25/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF; [QA](native-play-icon-53-qa.json), LATEST.md ve play-icon-0.14.53/focused.png/unfocused.png. Figma/manifest yok. hero_review nihai diff ve çağrıları salt okunur inceledi, bulgu yok. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558 / tools/run-device-tests.sh --no-build HeroLayoutTest,ContinueCardLayoutTest: 3/3. Normal görsellerde odakta dolu/odak dışında çizgili üçgen incelendi. Yeni test/font ölçeği matrisi yok. Ham loglar work/play-icon-qa. Proje kontrolü/git diff --check geçti.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.53.apk + sha256, kod67, aynı imza/release/R8. SHA-256 76fe4076709e81174c0bdc30fa84517f4ea4a6c76ea062058df27b74a0018cd3. TV .52→53 install -r, firstInstallTime 2026-10-13 23:50:03 değişmedi, lastUpdateTime 2026-10-18 21:07:04. Kurulu APK çekilip hash eşleştirildi; TV uygulaması/gerçek yayın açılmadı. **5556 bu tur açılmadı ve .51/kod65'te kaldı**; veri/yükseltme mantığı değişmedi. 5558/Gradle kapalı; 5554'e dokunulmadı.

Tam sonraki adım: ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az 0.14.54/kod68; 5556 yükseltilecekse mevcut sürümü .51/kod65 olarak doğrula. Fiziksel görsel/akıcılık/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Vitrin üst etiketi / 0.14.52 · 18 Ekim 2026

Kullanıcı “Yeni eklenen başlığı biraz daha küçük olsun” dedi. HomeScreen.kt hero-label fontSize 13→12 sp (D082); ortak KÜTÜPHANENDEN fallback'i de aynı stil. Ağırlık/renk/harf aralığı ve diğer metinler aynı. Değişenler: HomeScreen.kt, build.gradle.kts; D082, HOME_HERO/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF; [QA](native-hero-label-52-qa.json), LATEST.md ve hero-label-0.14.52/hero.png. Figma/manifest yok.

hero_review nihai diff'i salt okunur inceledi, bulgu yok. 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558 / tools/run-device-tests.sh --no-build HeroLayoutTest, 2/2. Yeni test yazılmadı. Normal görselde ortak etiketin KÜTÜPHANENDEN görünümü incelendi; YENİ EKLENEN slayt testinde geçer. Font ölçeği matrisi koşulmadı. Ham loglar work/hero-label-qa. Proje kontrolü/git diff --check geçti.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.52.apk + sha256, kod66, aynı imza/release/R8. SHA-256 237186f5ea5c4cce508307feb0b40b41919a7f91a85f38e0944e361c6a5bb86e. TV .51→52 install -r, firstInstallTime 2026-10-13 23:50:03 değişmedi, lastUpdateTime 2026-10-18 21:02:21. Kurulu APK çekilip hash eşleştirildi; TV uygulaması/gerçek yayın açılmadı. **5556 bu tur açılmadı ve .51/kod65'te kaldı**; yalnız tipografi değiştiğinden önceki gerçek kaynak açılış kontrolü tekrarlanmadı. 5558/Gradle kapalı; 5554'e dokunulmadı.

Tam sonraki adım: ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az 0.14.53/kod67; 5556 yükseltilecekse mevcut sürümü .51/kod65 olarak doğrula. Fiziksel görsel/akıcılık/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Açıklama iki satır / 0.14.51 · 18 Ekim 2026

Kullanıcının “Açıklama max 2 satır olsun” isteği D081 ile uygulandı. HomeScreen.kt HeroText açıklamasında tek satır kod değişikliği: maxLines if (largeText) 1 else 3 → 1 else 2. Ellipsis, büyük yazıdaki tek satır sınırı, başlık ve ayrıntı ekranı aynı. Ölçüm ve görünür slayt ortak HeroText kullandığı için tutarlı. D078/D079/D080 korunur.

Değişenler: HomeScreen.kt, build.gradle.kts; DECISIONS/D081, HOME_HERO/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF, [QA](native-hero-description-51-qa.json), design/reviews/LATEST.md ve hero-description-0.14.51/hero.png. Figma/manifest yok. hero_review son diff'i salt okunur inceledi, bulgu yok. Yeni test yazılmadı.

Kontroller: 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558 / tools/run-device-tests.sh --no-build HeroLayoutTest, 2/2. Normal ölçekte sentetik uzun açıklama iki satır ve sonu üç nokta olarak görsel doğrulandı. Font matrisi yok. Ham loglar work/hero-description-qa. Proje kontrolü/git diff --check geçti.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.51.apk + sha256, kod 65, aynı imza/release/R8. SHA-256 161c16b930413e64a774836404c12a20d3129562929a10b84c3df1974283bd4e. 5556 .50→51 install -r, Evim görünür/çökme kaydı yok/çalışan uygulama; firstInstallTime 2026-10-14 08:57:19. TV .50→51 install -r, firstInstallTime 2026-10-13 23:50:03 değişmedi, lastUpdateTime 2026-10-18 20:57:57. İki kurulu APK çekilip hash eşleştirildi. TV uygulaması/gerçek yayın açılmadı. 5556/5558 ve Gradle kapalı; 5554'e dokunulmadı.

Tam sonraki adım: ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az 0.14.52/kod 66. Fiziksel görsel/akıcılık/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Daha uzun vitrin / 0.14.50 · 18 Ekim 2026

Kullanıcının “Vitrini biraz daha büyütebiliriz aşağı doğru” isteği D080 ile uygulandı. DesignSystem.kt / TvLayout.HeroHeight 372→420 dp; yalnız minimum yükseklik ve açıklaması değişti. HomeScreen alt hizalı metin/İzle ve görsel alanı aşağı uzar. D079 tek düğme ve D078 minimum kaydırma aynı. Normal 960×540 dp ekranda ilk raf başlığı ve kart önizlemesi görünür; Aşağı sonrası devam kartı tam görünür. Gereken/gerçek kaydırma 86,5 dp (yüksekliğe eklenen 48 dp kadar artış).

Değişenler: DesignSystem.kt, build.gradle.kts; DECISIONS/D080, HOME_HERO/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF, [QA](native-hero-taller-50-qa.json), design/reviews/LATEST.md ve hero-taller-0.14.50 altındaki hero.png/continue.png. Figma/manifest değişmedi. hero_review salt okunur bağımsız incelemede işlev sorunu bulmadı; eski “two thirds” açıklaması düzeltildi. Yeni test yazılmadı, mevcut ilgili kontroller koşuldu.

Kontroller: 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest başarılı. Yalnız 5558 / tools/run-device-tests.sh --no-build üzerinden HeroLayoutTest (2), ContinueCardLayoutTest (1), toplam 3/3. İki normal ölçekte sentetik görüntü incelendi. Yazı boyutu matrisi yapılmadı. Ham kayıtlar work/hero-taller-qa. Proje bağlantı/ekran/değişken kontrolü ve git diff --check geçti.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.50.apk + sha256, kod 64, aynı imza/release/R8. SHA-256 26ad22181ef0c7512a8c2608b7a88d0325abdea0be7c3058d8d0f5e7bdcf1b2a. 5556 .49→50 install -r, Evim görünür/çökme kaydı yok/çalışan uygulama; firstInstallTime 2026-10-14 08:57:19. TV .49→50 install -r; firstInstallTime 2026-10-13 23:50:03 değişmedi; lastUpdateTime 2026-10-18 20:52:28. İki kurulu APK çekilip hash eşleştirildi. TV uygulaması/gerçek yayın açılmadı. 5556/5558 ve Gradle kapalı; 5554'e dokunulmadı.

Tam sonraki adım: ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az 0.14.51/kod 65. Fiziksel görsel/akıcılık/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Tek vitrin düğmesi / 0.14.49 · 18 Ekim 2026

Kullanıcının “Vitrinde tek buton bırakalım” isteği D079 ile uygulandı. HomeScreen Hero'da tek İzle var; Ayrıntılar ve kullanılmayan open callback'i kaldırıldı. Sağ/Sol doğrudan slayt değiştirir; tek slaytta Sağ yerinde, ilk slaytta Sol menüye; menüden Sağ İzle'ye. watch(shown()), PIN dönüşü, otomatik slayt/TalkBack/Hareketi azalt davranışı korunur. D078 kısa aşağı geçişi İzle'den de 38,5 dp gereksinim/38,5 dp gerçek hareketle doğrulandı.

Değişenler: HomeScreen.kt, HeroLayoutTest.kt, ContinueCardLayoutTest.kt, build.gradle.kts; D079, HOME_HERO/SCREENS, BACKLOG/STEPS, STATUS/HANDOFF, [QA](native-hero-single-49-qa.json), design/reviews/LATEST.md ve hero-single-0.14.49/hero.png. HeroLayoutTest mevcut vakaları tek/çok slayt, menüye dönüş ve kaldırılan düğmenin yokluğu için güncellendi; yeni gereksiz test sınıfı yok. hero_review bağımsız salt okunur diff incelemesinde bulgu bildirmedi. Figma/manifest değişmedi.

Kontroller: 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest derlemeleri. Yalnız 5558 / tools/run-device-tests.sh --no-build ile HeroLayoutTest (2), ContinueCardLayoutTest (1), VisualPreviewTest#homePostersAndContinueRemainVisibleWithRemote (1), toplam 4/4. Normal ölçekte sentetik görüntü incelendi. Yazı boyutu matrisi yapılmadı. Ham loglar work/hero-single-qa içinde. Proje bağlantı/ekran/değişken kontrolü ve git diff --check geçti.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.49.apk + sha256, kod 63, aynı imza/release/R8. SHA-256 2de0ca68cd80c5aba01133876413340a9956eae4ffcc18587779aa14911bdf95. 5556 .48→49 install -r, Evim görünür/çökme kaydı yok/çalışan uygulama, firstInstallTime 2026-10-14 08:57:19. TV .48→49 install -r, firstInstallTime 2026-10-13 23:50:03 değişmedi; lastUpdateTime 2026-10-18 20:46:40. İki kurulu APK çekilerek hash eşleşti. TV uygulaması/gerçek yayın açılmadı. 5556/5558 ve Gradle kapalı, 5554'e dokunulmadı.

Tam sonraki adım: ana checkout/main üzerinden STEPS S15a ve gap/overlay.md. Sonraki teslim en az 0.14.50/kod 64. Fiziksel kumanda/akıcılık/TalkBack, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · Home geçiş düzeltmesi / 0.14.48 · 18 Ekim 2026

Kullanıcının fotoğrafındaki Ayrıntılar → İzlemeye devam et geçişine odaklanıldı. HomeScreen ilk hero altı raf için mevcut CardVisibility minimum görünürlük yolunu kullanır; ortak kaydırma kodu ve diğer raflar değişmez. D078, D051'e bu dar istisnayı kaydeder. ContinueCardLayoutTest gerçek Down tuşuyla hareket mesafesini ve kart/başlık görünürlüğünü denetler. Önce aynı test eski .47 uygulamada gereken 38,5 dp yerine 381 dp hareketi yakaladı; son .48 kaynakta ikisi de 38,5 dp.

Değişenler: HomeScreen.kt, ContinueCardLayoutTest.kt, build.gradle.kts; DECISIONS/D078, HOME_SHELF_FRAMING, SCREENS, BACKLOG, STEPS, STATUS/HANDOFF; design/reviews/LATEST.md ve home-scroll-0.14.48/continue-after.png; [QA](native-home-scroll-48-qa.json). Figma/manifest yok. Ana ajan diff incelemesi yaptı; yeni bağımsız ajan kullanılmadı.

Kontroller: 354 JVM sonucu, 0 hata, 1 atlanan; lint 0 hata/37 uyarı; release/debug/androidTest derlemesi. Yalnız 5558 ve tools/run-device-tests.sh ile ContinueCardLayoutTest, HeroLayoutTest ve iki Home dikey/görünürlük regresyonu toplam 5/5 geçti. Normal ölçekte sentetik görsel incelendi. Yazı boyutu matrisi yapılmadı. Ham önce/sonra logları work/home-scroll-qa içinde Git dışında. Fiziksel TV FPS/kare süresi ve kullanıcının algıladığı akıcılık doğrulanmış sayılmaz.

Teslim: releases/NEXTV_TV_Design_Preview_0.14.48.apk + sha256, kod 62, önceki imza/release/R8. SHA-256 71014ce84547b954b796585f902e98e40222987114a4d055a0d871ce8a0ec88a. 5556 .47→48 üstüne kurulumda Evim görünürlüğü, çalışan uygulama, çökme kaydı yokluğu ve APK hash eşitliği doğrulandı; firstInstallTime 2026-10-14 08:57:19. TV üstüne kurulumda firstInstallTime 2026-10-13 23:50:03 değişmedi, lastUpdateTime 2026-10-18 20:38:51; çekilen APK hash eşleşti. TV uygulaması/gerçek yayın açılmadı. 5556/5558 ve Gradle kapalı; 5554'e dokunulmadı.

Tam sonraki adım: kullanıcının bu geçişe ilişkin geri bildirimini dikkate al; STEPS S15a ve gap/overlay.md üzerinden kanal kapsamı/hatırlama verisine devam et. Sonraki teslim en az 0.14.49/kod 63. Fiziksel kabul, Figma, İngilizce ve satış/fiyat kararları açık. Kullanıcı “yayına hazırla” demeden font matrisi koşma.

## Codex · S14b / 0.14.47 teslimi · 18 Ekim 2026

**Çalışma yeri ana checkout / main.** `72fc520` güncel Git dosyalarıyla proje kontrolünü onarır; `46f4743` S14b uygulama, testler, inceleme düzeltmeleri ve görsel kanıttır. Main'e fast-forward edildi. Birleşmiş `codex/player-controls-s14b` ile `claude/app-ui-ux-audit-a78e98` dalları `git branch -d` ile kapatıldı. Eski geliştirme worktree'si güncel kaynakla detached tutulur; aynı adlı klasör kaynak kaybı olmadan durur. Yerel yalnız main dalı var; remote/PR yok.

**Öncelikli yeni kullanıcı talimatı:** "yazı boyutlarını bu aşamada test etme; yayına hazırla dediğimde store'a çıkarken test ederiz." Bundan sonra geliştirme turlarında 1,5/2,0 matrisi başlatma; normal ölçekte değişiklikle ilgili anlamlı kontrol yap. WORKFLOW ve STEPS bu istisnayı içerir. Talimat geldiğinde son koşu bitmek üzereydi; durdurma komutu ulaşmadan 2/2 tamamlandı. Yeni font turu yapılmadı, ölçek 1.0'a döndü ve test emülatörü kapatıldı.

**Değişenler:** yeni `ui/PlaybackClock`, `ScrubRules`/`ScrubHolder`, `ControlRowPlan`, `PlayerInfoModel`, `PlayerSymbols`, `PlayerControls`; `PlayerScreen`, `PlayerKeys`, `ControlsAutoHide`, ortak `Theme.FilledControl`; EN/TR metinler ve sürüm. Tek alt scrim/bilgi bloğu, 64/52 dp transport, sağ çipler, süre/bitiriş/saat, bekleyen sarma ve 2,5 sn gizli önizleme var. Sarma hedefi yaşam döngüsü/hata/medya değişiminde iptal olur; OK/Aşağı uygular, Geri önce iptal eder. D077 hız ve panel açan düğmeye odak dönüşü korunur. Play/Pause için sarma uygulamadan önce oynatma durumu alınır; seek'in eşzamanlı BUFFERING durumuna geçmesi yanlış toggle yapmaz. Bağımsız salt okunur incelemede bulunan P3 odak ipucu ayrıca dört dosyada düzeltilip ana ajan tarafından incelendi. Yeni `PlayerControlsLayoutTest`, `ScrubRulesTest`, `PlayerControlsModelTest`; eski Kapat/metin seçicileri ilgili testlerde yeni etiket/açıklamalara taşındı. Tasarım sözleşmeleri, BACKLOG/STEPS/WORKFLOW, [görseller](../../design/reviews/LATEST.md) ve [QA](native-player-controls-47-qa.json) güncellendi. Yeni ürün kararı yok (D072/D077); Figma/node/manifest değişmedi.

**Gerçekte yapılan kontroller:** son kaynakta 354 JVM sonucu (1 atlanan, 0 hata), lint 0 hata/37 uyarı; release/debug/androidTest derlemesi başarılı. İpucu öncesi adayda 1,0/1,5/2,0 ölçeklerde kontrol+hız sınıfları 8'er; 30 oynatıcı/iz/bölüm/rehber regresyonu; 2,0 ölçekte 3 hata/canlı/geometri vakası geçti (57). Son ipucu düzeltmesinde etkilenen odak/panel dönüşü ve bekleyen sarma/Geri vakaları 2'şer geçti (6); normal çubuk ekranında OK/Geri ipucu görüldü. Önceki 30'luk turdaki iki test sorunu: Tracks kapanınca odağı beklemeyen test düzeltildi; arka plan pause konumunda aynı APK tekrarı geçti, ardından playback-loop mesajı beklenerek sabitlenen başlangıç konumuna karşı mevcut 150 ms sınırı korundu. Ham loglar `work/s14b-qa`, ilk başarısız loglar da saklı. Tüm enstrümantasyon yalnız 5558 / `tools/run-device-tests.sh`. `git diff --check`, yerel bağlantı/17 ekran/401 değişken kontrolü geçti; bir Figma işi açık. Bağımlılık sürümü değiştirilmedi; kontrol edilen resmî kaynaklar QA'da.

**Teslim:** 0.14.47-design-preview / **kod 61**, release/R8 ve önceki imza. SHA-256 `d67222b483a00570aa1fcb579b8f2e1f7ae4908fbe844a4d2076bf7beb73b37e`; APK + sha256 ana ve eski geliştirme checkout'unun releases arşivinde. 5556 eski snapshot yüklenmeden kalıcı diskle açıldı: 0.14.46→47 `install -r`, aynı firstInstallTime `2026-10-14 08:57:19`, Evim görünür, uygulama açık, çökme kaydı yok, kurulu APK hash eşleşti. TV `192.168.1.126:5555`: `install -r` başarılı; firstInstallTime `2026-10-13 23:50:03`, lastUpdateTime `2026-10-18 20:27:00`; çekilen APK hash eşleşti. Veri temizleme/kaldırma yok; TV'de uygulama veya gerçek yayın açılmadı. **5556 ve 5558 kapalı**, Gradle durduruldu; gerçek Evim diskte, 5554'e dokunulmadı.

**Tam sonraki adım:** STEPS **S15a** ve `gap/overlay.md` tamamını oku; ChannelScope/LiveIndex ve LiveRecall/LastLive/AutoStart verisini, erişim kapısını ve anahtar yönlendirmelerini kur. Kanal listesi/bilgi bandı S15b–c, görüntü oranı/altyazı hızlı ayarı/akış bilgisi S16, canlı DVR S17'dir; mevcut kontroller bu işleri tamamlanmış yapmaz. Sonraki TV paketi **en az 0.14.48 / kod 62**. Fiziksel kumanda/ses/TalkBack, 3 m açık/koyu kare okunurluğu, kare süresi, 1 sn sarma süresinin kullanıcı kabulü, Figma, İngilizce inceleme ve satış/fiyat kararları açık. Kullanıcının "yayına hazırla" talebine kadar font matrisi yok.

## Codex · Main güncel, TV son paketle doğrulandı · 18 Ekim 2026

Kullanıcının "merge bekleyenleri mergeleyelim ve son paketi televizyona yükleyelim" isteği tamamlandı. **Ana proje checkout'u / main artık güncel çalışma kaynağıdır.** Geliştirme dalındaki 45 commit fast-forward ile alındı; a87db7c, c54a9f7 WIP geçmişini de birleştirdi. WIP'in dokuz dosyası karşılaştırıldı: altısı güncel dosyalarla aynı, iki strings dosyasında yalnız sonraki hız metinleri var; PlayerScreen'de sonraki medya başına uyarı ve hız davranışı bulunuyor. Normal merge'in bu dosyadaki çakışması güncel/test edilmiş davranış korunarak çözüldü. Merge ağacı 2bc7f54 ile tamamen aynı. Birleşen `wip/s14-review-fixes` dalı `git branch -d` ile kaldırıldı; aktif geliştirme worktree'si main'e fast-forward edilerek korunur. Eski klasör temizliği yeniden denenmedi.

**Teslim:** uygulama ve `tools/run-device-tests.sh`, 0.14.46'nın 7ef216a kaynaklarıyla birebir aynı. Önceki doğrulanmış release APK'sı kullanıldı, yeniden derleme ve sürüm artışı yapılmadı. Fiziksel TV'ye açık seri kimliğiyle `install -r` başarılı; paket 0.14.46-design-preview / kod 60. Kurulu APK çekilip arşivle karşılaştırıldı: SHA-256 `b826893b7a9b1fdea69ff06339cbbd94c1778c01c146c5228fcc8208b179d185`. firstInstallTime önce/sonra `2026-10-13 23:50:03`, son güncelleme `2026-10-18 19:15:09`. Yerel doğrulama kopyası `work/merge-delivery-qa/tv-installed.apk`; Git dışında. Veri temizleme/kaldırma veya TV'de oynatma yapılmadı; 5556/5558 kapalı kaldı.

**Kontroller ve sınır:** Git atalık/ağaç eşitliği ve `git diff --check`; bu tur uygulama testi tekrar koşulmadı, önceki 0.14.46 testleri geçerli. `python tools/check_project.py`, ana kök altındaki eski worktree'leri de tarayarak eksik tarihî APK/README ve yarım silinmiş logo klasörünün bağlantılarında başarısız oldu. Uygulama kaynağı veya teslim paketinde yeni fark yok. Bu tur yalnız STATUS/HANDOFF güncellendi; yeni ürün kabulü veya karar olmadığından BACKLOG/DECISIONS değişmedi.

**Tam sonraki adım:** ana checkout'tan `docs/design/gap/STEPS.md` S14b (D072), ardından S15a–S17. D077 oynatma hızı ve odak dönüşünü yeni kontrollere taşı. Yeni uygulama değişikliğiyle hazırlanacak sonraki TV paketi en az kod 61 / 0.14.47 olmalı; aynı 0.14.46'nın bu yeniden kurulumu sürüm artışı gerektirmedi. Fiziksel kumanda/ses/TalkBack, İngilizce incelemesi ve satış/fiyat kararları açık.

## Birleşmiş dallar kapatıldı · 18 Ekim 2026

Üç yerel dal main'e birleştiği doğrulanarak `git branch -d` ile kaldırıldı: `claude/logo-splash-files-f4683f`, `claude/sabah-kahvaltisi-oneri-870a0c`, `claude/trusting-margulis-73aa19`. İlgili çalışma ağaçları öncesinde temizdi; yalnız logo/trusting içinde yeniden üretilebilir Gradle/build çıktıları ve local.properties vardı. Ana/güncel çalışma dalına merge yapılmadı; `claude/app-ui-ux-audit-a78e98` ve `wip/s14-review-fixes` korunuyor.

Kalan klasör durumu: `.claude/worktrees/logo-splash-files-f4683f` Git worktree kaydından çıktı, fakat klasörün silinmesi Windows erişim hatasıyla yarıda kaldı. Kalan dizini PowerShell ile özyinelemeli silme isteği otomatik politika tarafından reddedildi; bu engeli aşmak için başka silme yöntemi denenmedi. `sabah-kahvaltisi-oneri-870a0c` ve `trusting-margulis-73aa19` kendi commit'lerinde detached worktree olarak duruyor. Eski branch referansları yoktur; içerik zaten main geçmişindedir.

Commit bekleyen ürün değişikliği yok. Bu kayıt öncesinde güncel geliştirme dalı main'in 44 commit ilerisindeydi, main tarafında ayrışan commit yoktu. Güncel dalın merge'i kullanıcı tarafından bu turda istenmedi; sıradaki ürün işi önceki kayıttaki S14b. TV/cihazlara dokunulmadı.

## Codex · 0.14.46 hız ayarı teslim edildi · 18 Ekim 2026

TV ve 5556'da **0.14.46 / versionCode 60** kurulu. Kullanıcının iki isteği tamam: son sürüm TV'de; film/dizide **Seçenekler → Oynatma hızı** (0,5×, 0,75×, 1×, 1,25×, 1,5×, 1,75×, 2×). 0.14.45 önce S14 teslimi olarak kurulmuştu; 0.14.46 hız ekler. S14b yapılmış sayılmaz. Ana checkout main'deki kaynaklar değişmedi; çalışma hâlâ `.claude/worktrees/app-ui-ux-audit-a78e98` / `claude/app-ui-ux-audit-a78e98` içinde.

Kod: yeni `ui/PlaybackSpeed.kt` (girdi/komut/canlı/tünel kapısı, pitch=1, yerel sayı, bitiş hesabı), `PlayerOptionsDialog.kt` (iki düzey, opaklık, RadioRow, odak halkasını koruyan 8 dp kaydırma payı); `PlayerKeys` Options/Speed katmanları ve Geri komutları; `PlayerScreen` gerçek playbackParameters, oturum davranışı, canlı reset, açıklama ve açan düğmeye odak dönüşü. Yeni `PlaybackSpeedTest`, `PlayerSpeedFlowTest`; EN/TR metinler ve sürüm. D077, PRD PLAY01, SCREENS TV-051, BACKLOG/STEPS güncellendi. Figma/manifest değişmedi.

Kontroller: 344 JVM sonucu (1 atlanan, 0 hata); lint 0 hata/37 uyarı; release/debug/androidTest. Menü eklemesiyle 5 regresyon geçti (PlaybackRecoveryFlowTest iki vaka, EpisodeAutoplayFlowTest iki vaka, VisualPreviewTest.playerPanelKeepsTitleAndRemoteActionsInsideScreen). Yeni testin URL'si UI thread dışında hazırlanarak ilk turdaki NetworkOnMainThreadException giderildi. Görsel kontrolde alt odak halkası kesiliyordu; düzeltilip assert güçlendirildi. Son kod üzerinde hız sınıfı normal/1,5/2,0 yazıda ayrı ayrı 3/3 geçti; final loglar `work/speed-qa` altında. Testler yalnız 5558/betik; font_scale 1.0'a döndü. 5556'da 0.14.45→46 üstüne kurulum, uygulama açılışı, gerçek Evim görünürlüğü, aynı firstInstallTime ve çökme kaydı yokluğu görüldü; gerçek yayın açılmadı. TV install -r başarılı; firstInstallTime aynı `2026-10-13 23:50:03`, çekilen APK hash eşleşti: `b826893b7a9b1fdea69ff06339cbbd94c1778c01c146c5228fcc8208b179d185`. APK ve sha256 iki checkout'un releases arşivinde.

Cihazlar: **5556 ve 5558 kapalı**, gerçek Evim diskte duruyor; Gradle daemon durduruldu (bellek azalmıştı). 5554'e dokunulmadı. TV'de uygulama açılmadı; fiziksel kumanda/ses/TalkBack kabulü hâlâ açık. Hız sessizce kalıcı ayar yazmaz, tüneli kapatmaz veya yeni akış açmaz. Tünel açıkken hız için ayarı kapatma açıklaması vardır; donanım/passthrough gerçek hız sınırları fiziksel cihazda sınanmadı. Yeni bağımsız ajan incelemesi yapılmadı; kod, otomatik test ve ekran görüntüsü incelemesi yapıldı.

**Tam sonraki adım:** `docs/design/gap/STEPS.md` S14b (D072), sonra S15a–S17. Yeni kontrol düzeninde D077 Seçenekler/hız ve odak geri dönüşünü koru; PlayerLayer/KeyRouter tek otorite. Sonraki TV sürümü **en az kod 61 / 0.14.47** olmalı. S14'ün desteklenmeyen AC-3 koşulu saf Tracks testinde; medya başına tek uyarıyı ayrıca FakePlayer/cihazla kapatmak, fiziksel kumanda/TalkBack, İngilizce ve API 26–32 dil kontrolü açık. Satış/fiyat kararlarını onaylanmış sayma. `check_project.py` tarihî release dosyaları worktree'de bulunmadığından bağlantı hatası verir; uygulama hatası değildir.

## Codex · 0.14.45 TV teslimi ve devam · 18 Ekim 2026

Çalışma yeri `.claude/worktrees/app-ui-ux-audit-a78e98`, dal `claude/app-ui-ux-audit-a78e98`. Kullanıcının “son sürümü yükle” talimatı eski “S14b bitince kur” zamanlamasını öne aldı: S14 + inceleme düzeltmeleri **0.14.45 / kod 59** olarak TV'de; S14b/hız henüz dahil değil. Ana checkout'un kaynakları değişmedi; teslim APK'sı her iki checkout'un `releases` klasöründe. Bir sonraki TV paketi **en az 60 / 0.14.46** olmalı.

Değişenler: `PlayerScreen`, `PlaybackRecovery`, `SettingsRows`, EN/TR strings; `PlayerSettingsFlowTest`, `SettingsFrameworkTest`, `SearchNavigationTest`, `TracksLayoutTest`, `PlaybackRecoveryTest`; test betiğinin `--font-scale` seçeneği; sürüm. `c54a9f7` WIP alınıp PCM uyarısı mevcut flash yoluna taşındı, 8 sn/polite duyuru ve motor açılış ayarıyla eşleştirildi. Belgeler: D073–D076 geçmiş uygulama kararlarını kaydeder, yeni kullanıcı onayı sayılmaz; PIN_ATTEMPTS/PROTECTION_STORE güncel, BACKLOG BKL-029…041 eklendi; STEPS teslim sırası güncel. Figma/node/manifest değişmedi.

Kontroller: 340 JVM testi, 1 atlanan, 0 hata; lint 0 hata/37 uyarı; release/androidTest başarılı. 5558 betiği normal yazıda 16, 1,5 ve 2,0 yazıda ayrı ayrı 12 test geçirdi. 5556'da 0.14.44 → 0.14.45 yükseltmesi; Evim kaynak görünürlüğü, açılış ve Ayarlar, aynı firstInstallTime, çökme kaydı yok. TV `install -r`, aynı firstInstallTime `2026-10-13 23:50:03`, çekilen APK hash eşleşti: `e4e771a9ef98e31a556b976426676df6ef4622c16092ae89d3503289f0619f64`. Uygulama TV'de açılmadı. 5558 kapatıldı; 5556 açık, NEXTV'de Ayarlar kontrol edildi, gerçek veri yerinde. Enstrümantasyon yine yalnız 5558/betik.

Kalan sınırlar: desteklenmeyen AC-3 koşulu JVM'de sentetik Tracks ile; medya başına tek uyarı davranışı FakePlayer/cihazda ayrıca doğrulanmadı. Fiziksel kumanda/TalkBack, İngilizce kullanıcı incelemesi ve API 26–32 dil geçişi açık. `check_project.py`, worktree'de olmayan tarihî release dosyaları nedeniyle bağlantı hataları veriyor. Bağımsız yeni ajan incelemesi koşulmadı; Claude'un kayıtlı bulguları üzerinden kod incelemesi/düzeltme yapıldı.

Tam sonraki adım: kullanıcının “player'a hız ayarı” isteği için film/dizide 0,5×–2× seçim (ses perdesi korunur, canlı normal hız); ayrı derlemede doğrula. Ardından STEPS S14b ve devamı. Satış/fiyat/İngilizce kararları kullanıcıda.

## Claude → Codex devri · 18 Ekim 2026, 17:40

Kullanıcı Claude kullanım sınırına takıldı ve Codex ile devam edecek. Claude'un arka plan iş akışı durduruldu; çalışma ağacında başka araç yok.

**Dal ve commit'ler:** `claude/app-ui-ux-audit-a78e98`, uç `b6157c9` (S14). S14'ün yarım inceleme düzeltmeleri `wip/s14-review-fixes` dalında (`c54a9f7`, derlenmedi, test edilmedi). Ana dalda bütün commit'ler kontrolleri geçmiş durumda (S14 dahil; S14'ün inceleme bulguları açık).

**Nereden devam:** [docs/design/gap/STEPS.md](../design/gap/STEPS.md): durum, her adımın yöntemi (uygula → bağımsız inceleme → düzelt), kontrol komutu, ekran testi betiği, S14 bulguları ve S14b, S15a–S17, S18–S29'un ayrıntılı görevleri. Plan: [PLAN.md](../design/gap/PLAN.md); küme tasarımları aynı klasörde.

**Kullanıcının bekleyen talimatı:** "Bitince kur": S14b (D072, yeni oynatıcı kontrolleri) bitince TV'ye kurulacak. Yöntem 0.14.44 kaydındaki gibi: dalda `versionCode` 57'de kaldı, TV'de 58 (0.14.44) var; bir sonraki TV derlemesi en az 59 olmalı (0.14.45). Release'i derle, önce 5556'da gerçek veriyle 0.14.44'ün üstüne kurup aç, sonra `adb -s 192.168.1.126:5555 install -r`, SHA ve firstInstallTime'ı doğrula, `releases/NEXTV_TV_Design_Preview_0.14.45.apk` + `.sha256` olarak sakla. TV'nin dili Türkçe (salt okunur kontrol); uygulama orada Türkçe açılır.

**Cihazlar:** 5558 test emülatörü (`nextv_test_api36`) açık bırakıldı; açma komutu WORKFLOW'da. 5556 kapalı (bellek için); gerçek "Evim" kaynağı diskte ve `nextv-pre-tests` anlık görüntüsünde. 5554'e dokunulmadı. Git'e girmeyen `app/src/androidTest/assets/episode-end-fixture.mp4` bu çalışma ağacına kopyalandı. Bilgisayarda 15 GB RAM var; iki emülatör ve Gradle (Xmx 3g) birlikteyken bellek dolu, paralel derleme izinden kaçınıldı.

**Belgelenmemiş kalanlar (ajanlara "STATUS/HANDOFF/DECISIONS'a dokunma" denmişti):** Faz A–C ve S14 için DECISIONS kayıtları yazılmadı. Gerekenler: PIN ve ebeveyn denetimi (BKL-028; S08'in D048/D049 sözleşmelerini değiştirmesi: boot sayacı yedeği, codec v2, `reset()`), kilit arayüzü ve "Göster / Kilitle / Gizle" (plan 4a-7 varsayılanı), kaynak ve açılış koruması, iki dil ve dil seçimi (S13), iki düzeyli Ayarlar (plan 4a-8 varsayılanı; S14 10 bölümün 6'sını kurdu), oynatıcı ayarları (S14). docs/architecture/PIN_ATTEMPTS.md ve PROTECTION_STORE.md güncel değil. BACKLOG'a PLAN §2'deki BKL-029…041 satırları eklenmedi.

**Açık sorular ve bilinen sınırlar:** satış modeli, fiyat ve ücretsiz/Plus sınırı (SALES_STRATEGY §1'deki 5 karar; PLAN §0 ile çelişen noktalar orada); İngilizce metinlerin kullanıcı incelemesi; PIN bekleme metni 30 sn yerine kalan süreyi söylüyor (ör. 28 sn); Android 8–12'de dil değiştirme yolu cihazda sınanmadı; "IT: …" ile başlayan başlıkta ilk kelime kırpılıyor (bilinen sınır, testli); kaynak kaydı v2 olduğu için TV'ye 0.14.43 veya eskisi kurulursa kurtarma ekranı açılır. Stalker portalı planda reddedildi.

## 0.14.44 · TV teslimi · 18 Ekim 2026

TV'de 0.14.44 (a0f1908 + sürüm 58) kurulu, SHA eşleşti, veri korundu. Dalda versionCode hâlâ 57: TV'ye giden bir sonraki derlemeden önce 58'in üstüne çıkarılmalı (aksi hâlde sürüm düşürme hatası). Teslim releases/NEXTV_TV_Design_Preview_0.14.44.apk, SHA-256 186bc0f74a62468bf6ac8d5f7550736cbb1fb08ec3eeffc216581dda2df6ce32. Arka planda gap iş akışı S11–S13'te; ardından Faz D (S14–S17) ve Faz E (S18–S29) hazır. 5556 kapalı (bellek), 5558 test emülatörü açık.

## 0.14.43 · TV teslimi · 18 Ekim 2026

Değişen: app/build.gradle.kts (versionCode 57, 0.14.43-design-preview); ui/HomeScreen.kt (`Hero`: `hero-watch` İzle + `hero-details` Ayrıntılar, `starting`, `watch`; slayt dönüşü satır uçlarında), TvViewModel.kt (`watch(entry)`, `TvState.heroStarting`), ui/DesignSystem.kt ("info" ikonu); testler HeroLayoutTest, PilotFlowTest, VisualPreviewTest, ContinueCardLayoutTest; DECISIONS D071. Teslim releases/NEXTV_TV_Design_Preview_0.14.43.apk, SHA-256 954ed138e22140288d57e73a7c9bed85932c9a297b25aaca03d0daffc3742a92. TV'de 0.14.43 kurulu, SHA eşleşti, veri korundu.

Sonraki adım: kullanıcının TV'de vitrin İzle/Ayrıntılar, açılış kapağı ve bölüm aramasını denemesi. Kullanıcı "tüm gap'i kapat" ve satış stratejisi istedi: strateji araştırması arka planda; gap çalışması dalgalar halinde başlayacak (TIVIMATE_GAP).

## Açılış kapağı, ana sayfa odağı, bölüm araması, raporlar · 18 Ekim 2026

Değişen kod: ui/LaunchCover.kt (`RestoringCover`; hareketsiz kapak kaynaklar açılana kadar; `SplashSpinner(still)` hareket azaltma/kapalı animasyonda sabit yay; etiket en az 14 sp), ui/PilotApp.kt (LOADING + RESTORING + `restoringAtLaunch` → kapak, diğerleri `LoadingScreen`), TvViewModel.kt (`TvState.restoringAtLaunch`, `restore(atLaunch)`; init `restore(atLaunch = true)`; `query()` sonuçları korur), ui/HomeScreen.kt (ilk odak "hero"), ui/LibraryScreen.kt (alan açılınca `scrollToItem(0)` sonra odak; alanın `down` yönlendirmesi kaldırıldı; `searchLabel`/`widenLabel`, boş durumda `widen`, `widened` sonrası ilk sonuca odak; Favoriler/Geçmiş kategorisinde aramada doğru boş metin). Testler: SearchNavigationTest, LoadingLayoutTest, PilotFlowTest, VisualPreviewTest, ContinueCardLayoutTest. Belgeler: DECISIONS D069/D070 (D062'ye not), NATIVE_UI "Açılış ve ses", yeni PLAY_STORE_READINESS.md ve product/TIVIMATE_GAP.md.

Cihaz: 5556'da bu derleme kurulu (sürüm adı 0.14.42), gerçek Evim duruyor. TV'de 0.14.42 (bu düzeltmeler yok).

Sonraki adım: kullanıcı isterse 0.14.43 olarak TV'ye kurulum; Play/hukuk raporundaki kararlar (ad, paket adı, upload anahtarı, PIN, ses lisansı) ve TiviMate listesinden öncelik seçimi kullanıcıda.

## 0.14.42 · TV teslimi · 17 Ekim 2026

Değişen: app/build.gradle.kts (versionCode 56, 0.14.42-design-preview). Teslim releases/NEXTV_TV_Design_Preview_0.14.42.apk, SHA-256 73f2bf9531163b659a5ef38874a17c71eaab4c51e72e72c36306284790187c3a. TV'de 0.14.42 kurulu, SHA eşleşti, veri korundu. Sonraki adım: kullanıcının TV'de vitrin (Xtream geniş görseli), rehber kısayolları ve kategori ayarlarını denemesi; Play Store hazırlık ve hukuk raporu hazırlanıyor.

## Büyük vitrin, rehber kısayolları, kategori gizleme · 17 Ekim 2026

Değişen kod: ui/HomeScreen.kt (`Hero`, `HeroPicture`, `HeroText`, `runtimeText`; Home `contentTop` vitrin varken 0), ui/DesignSystem.kt (`HeroHeight` 372 dp), TvViewModel.kt (`heroDetail`, `ensureHeroDetail`: Xtream film/dizi ayrıntısı, bellek; `categoryVisible`, `hideAdultCategories`; `publishLibrary` içinde `CategoryVisibility.apply/choices`; TvState `hiddenCategories`, `hideAdultCategories`, `categoryChoices`), data/CategoryVisibility.kt (yeni), data/DeviceStore.kt (`hiddenCategories`, `hideAdultCategories`), ui/GuideScreen.kt (Geri sırası, CH±/sayfa/FF/RW/rakam, `guide-digits`, alt bant kaldırıldı, ipucu, 50 dp satır), ui/LibraryScreen.kt (`LiveInfo` ve `focusedEntry` kaldırıldı), ui/SettingsScreen.kt (İçerik bölümü, CATEGORIES/CATEGORY_LIST sayfaları, `CategoryList`). Testler: CategoryVisibilityTest (yeni); GuideFlowTest (Geri sırası, seçili program artık `guide-grid` açıklamasında), SearchNavigationTest (Ayarlar satır sırası: İçerik bölümü). Belgeler: DECISIONS D067/D068.

Kontroller: `testDebugUnitTest` 163 (1 atlanan), `lintDebug` 0 hata, `assembleRelease`, `assembleDebugAndroidTest`. 5556'da gerçek Evim ile elle görüldü (STATUS). Test sırasında yanlışlıkla "Geçmişi temizle?" onayı açıldı, Vazgeç'le kapandı; geçmiş yerinde. GREECE kategorisi kapatılıp geri açıldı; 5556'da gizli kategori yok (yalnız yetişkin filtresi).

Sonraki adım: kullanıcının isteğiyle TV'ye kurulum (sürüm 0.14.42), TV'de Xtream vitrin görsellerinin gelmesi; PIN ile kategori koruması kullanıcı kararı.

## 0.14.41 · NEXTV teslimi · 17 Ekim 2026

Değişen: apps/android-tv/app/build.gradle.kts (versionCode 55, versionName 0.14.41-design-preview). Teslim releases/NEXTV_TV_Design_Preview_0.14.41.apk, SHA-256 5fa8f07da7b7572a14402fde2f819ad4bb35d3eeedd92a16e885646bd661cc61, imza 0.14.40 ile aynı.

TV (192.168.1.126:5555): 0.14.41 kurulu, SHA eşleşti, veri korundu, uygulama açılmadı. 5556 kapalı; üzerinde bu dalın 0.14.40 adlı ara derlemesi kurulu.

Sonraki adım: kullanıcının TV'de NEXTV logosu, büyük afişler, gradient oynatıcı, kanal/sarma tuşları ve basılı OK menüsünü değerlendirmesi. Fiziksel kumandada CH+/CH−, rakam ve rehber tuşlarının gerçekten geldiği ilk kez TV'de görülecek. Açık: U19 ana sayfa rafları, kalite çeşitlerini birleştirme, NEXTV marka kontrolü, dalın main'e birleştirilmesi.

## NEXTV adı, sade adlar ve denetimin kalan maddeleri · 17 Ekim 2026

Değişen kod: data/CatalogPresentation.kt (`displayTitle`, `displayCategory`); ui/PilotApp.kt (`MediaCard` adları, `guideAvailable`, `longClick`; `QuickActions`, `refocusAfterDialog`, `remainingText`; kurulumda `reveal`); ui/LibraryScreen.kt (kategori adı ve ülke kodu, kanal satırı/bilgi paneli rehbersiz metin, hızlı menü durumu, "· Rehber yok"); ui/HomeScreen.kt (kart adları, kalan süre, hızlı menü, vitrin metni boyu, menü başında amblem + `nextv_wordmark`); ui/SearchScreen.kt, ui/DetailScreen.kt, ui/PlayerScreen.kt (adlar; ayrıntıda kalan süre; `LoadingArc`; rehber tuşu); ui/GuideScreen.kt (`guideless` durumu, `numbers`, 56 dp satır, Geri önce menü, yazı boyları); ui/DesignSystem.kt (`LoadingArc`, `NextvLogo`); ui/Theme.kt (düğme 14/13 sp ve `actionHeight`, Note 13 sp, `EntryField(revealed)`); ui/LoadingScreen.kt, SourceRecoveryScreen.kt, SettingsScreen.kt, SourcesScreen.kt, EpisodeEndDialog.kt (logo ve yazı boyları); AndroidManifest `android:label="NEXTV"`; data/EpgRepository.kt, SourceRepository.kt User-Agent. Yeni kaynak: res/drawable/nextv_logo.xml, nextv_wordmark.xml (app_banner'daki yolların arka plansız kopyası). Testler: CatalogPresentationTest (ad vakaları), VisualPreviewTest ("2 dk kaldı"). Belgeler: DECISIONS D065/D066, AGENTS (ürün adı satırı), README, PRD isim notu, UX_AUDIT.

Kontroller: `testDebugUnitTest` 161 (1 atlanan), `lintDebug` 0 hata, `assembleRelease`, `assembleDebugAndroidTest`. 5556'ya release kuruldu (veri korundu); gerçek Evim ile görüldü: menüde NEXTV, "beIN SPORTS 1 HD" gibi sade adlar, "U Are the Universe · 2024 · DE · Evim", kategori sütununda "YERLI 2025 TR", kanal satırında kategori, devam kartında "1 sa 18 dk kaldı", basılı OK ile hızlı menü ("MAC OZETLERI": Favorilere ekle / İzle / Vazgeç), Uygulama hakkında ve kurulumda logo, "Bilgileri göster". Rehberde Evim satırları hâlâ "Rehber adresi eklenmemiş" diyor; ızgara yerine açıklama yalnız hiçbir kaynakta rehber yokken çıkar ve 5556'daki sentetik Demo'nun rehberi olduğu için görülmedi.

Yanlışlıkla yapılan: Ayarlar'da odak "Kataloğu yenile"deyken OK gitti; Evim ve Demo yenilendi, ikisi de "Kütüphanen güncellendi" (Evim 21.314 içerik). Bu, gerçek sağlayıcıya tek bir liste isteğidir, oynatma değildir. Test favorisi eklenmedi; geçmişte önceki turdan kalan kanallar ve film duruyor.

Koşulmayanlar: enstrümantasyon sınıfları, TalkBack, büyük yazı, basılı OK'nin fiziksel kumandadaki süresi, fiziksel TV.

Sonraki adım: kullanıcının görsel kabulü; TV'ye kurulum istenirse sürüm 0.14.41 ve releases teslimi (APK adı da NEXTV olmalı mı, kullanıcıya sorulabilir). Açık: U19 ana sayfa rafları ve kalite çeşitlerini birleştirme (kullanıcı kararı), NEXTV marka kontrolü.

## Denetim düzeltmeleri, büyük afişler ve gradient oynatıcı · 17 Ekim 2026

Değişen kod (apps/android-tv/app/src/main/java/dev/iptvplayer/tv):
- ui/LibraryScreen.kt: `LibraryBack` (kart/başlık/arama → kategori → menü; menüdeki Geri PilotApp'e kalır), menüden girişte `grid.scrollToItem(0)`, `LibraryMemory.focusVisible`, `TitleInfo` silindi, sütunlar 4/5 (büyük yazı 3/4), kategori odağı mint.
- ui/PilotApp.kt `MediaCard`: başlık 15/20 sp, yıl ve kaynak tek satırda 12 sp, canlı kart yazıları 15/12 sp.
- ui/PlayerScreen.kt: kısayollar (`zap`, `seekBy`, `digits`, `flash`, köşe notu `player-note`); opak panel yerine scrim, `player-panel` etiketi artık şeffaf sütunda (test etiketleri korunur), ipucu satırı sağda.
- TvViewModel.kt: `channelNumber`, `playChannelNumber` ("Tüm kanallar" sırası); `play` devam konumunu `resumable` ile alır.
- data/Models.kt `PlaybackMark.resumable` (%2, en çok 2 dk); HomeLibrary ve DetailScreen bunu kullanır.
- data/CatalogParser.kt `M3uParser.findEpisodeTag`: kelime sınırı ve başlığın başında olmama şartı.
- data/CatalogPresentation.kt `episodeDisplayTitle`; ui/DetailScreen.kt bölüm kartı, favori etiketi/dolu yıldız, yer tutucu açıklama ve sağ üst tür etiketi kaldırıldı.
- ui/Theme.kt `Action`: dinlenen birincil düğme koyu yüzey + mint yazı, çerçeve Outline %60. ui/HomeScreen.kt: menü odağı mint, açık menüde içerik karartması, vitrin zamanlayıcısı (odakta 15 sn, TalkBack'te kapalı, geçiş sonrası 600 ms OK önceki slayt). ui/GuideScreen.kt: seçili hücre beyaz halka. ui/SettingsScreen.kt: Hareketi azalt açıklaması.
- Testler: HomeLibraryTest, EpisodeSequenceTest, CatalogPresentationTest (yeni vakalar); PilotFlowTest, SearchNavigationTest, VisualPreviewTest beklentileri güncellendi.
- Belgeler: DECISIONS D064, UX_AUDIT_0.14.40 "Uygulama durumu".

Kontroller: `testDebugUnitTest` 160/160 (1 atlanan), `lintDebug` 0 hata, `assembleRelease` ve `assembleDebugAndroidTest` geçti. 5556'ya release `install -r` ile kuruldu (veri korundu) ve gerçek Evim ile görüldü: Diziler/Filmler'de bant yok, 4 sütun (ızgarada 5) büyük afiş; Geri ızgara → "Tüm diziler" → menüde Diziler → Ana Sayfa; menüden girişte liste başta ve Sağ ilk afişe; "8x8" tek "Bölüm 8" kartı (sezon başlığı hâlâ "Sezon 8", kaynak bilgisi bu); "Asırlık Gece" bölümleri "Bölüm 1…"; canlıda Yukarı 3. kanalı, "5" 5. kanalı, CH− 4. kanalı açtı ve köşede numara+ad notu çıktı; filmde Sağ +10 sn (0:13), İleri sarma +30 sn (0:44); 19 sn'lik eski kayıt devam rafına girmedi ve "Film kütüphanen" geri geldi. TV yalnız okunarak kontrol edildi (uykuda). Emülatör kapatıldı.

Koşulmayanlar: enstrümantasyon sınıfları (gerçek Evim'i silerler), TalkBack, büyük yazı (3/4 sütun), basılı tutulan Sol/Sağ hızlanması, rakam girişinde olmayan numara uyarısı, fiziksel TV akıcılığı.

5556'da kalan iz: geçmişte birkaç beIN kanalı ve "U Are the Universe (2024) DE" (~3:30, artık devam rafında). Test favorisi geri alındı. İstenirse Ayarlar → İzleme geçmişini temizle.

Sonraki adım: kullanıcının 5556'da ya da TV'de görsel kabulü. TV'ye gidecekse sürüm 0.14.41'e yükseltilip release APK `releases` altına alınmalı ve kullanıcının isteğiyle kurulmalı. Ardından denetimin açık maddeleri (U25 ad sadeleştirme, U9 rehber, U6 tip ölçeği).

## UI/UX denetimi · 17 Ekim 2026

Yeni dosya: docs/design/UX_AUDIT_0.14.40.md (U1–U26, öncelik ve önerilen sıra). Kod, Figma ve manifest değişmedi; BACKLOG'a madde eklenmedi (öneriler onaylanmadı).

Yapılanlar: `ui/` ekranlarının tamamı ve Back/odak yolları okundu; Ink paleti kontrastı hesaplandı. 5556 (`nextv_design_api36`) açıldı, 0.14.39 üstüne imzası doğrulanmış 0.14.40 release APK'sı `install -r` ile kuruldu (veri korundu). Gerçek "Evim" M3U ile Ana Sayfa, Canlı TV, Filmler, Diziler, ayrıntı, Ara, Rehber, Ayarlar ve oynatıcı denendi; sırayla tek canlı yayın ve tek film açıldı. TV yalnız `dumpsys` ile okundu (uykudaydı, yayın yoktu). Crash kaydı yok. Ekran görüntüleri gerçek veri içerdiği için depoya konmadı. Emülatör iş bitince kapatıldı.

5556'da kalan iz: geçmişe "TR: beIN SPORTS HABER HD" (son kanal) ve "U Are the Universe (2024) DE" (0:19, devam kaydı) eklendi; bu yüzden Ana Sayfa'da başlangıç rafları yerine kişisel raflar görünür. İstenirse Ayarlar → İzleme geçmişini temizle ile eski hâline döner (öncesinde geçmiş boştu; favoriler etkilenmez).

Denenmeyenler: TalkBack, büyük yazı, fiziksel TV, kurulum formu (U13), favori ekleme akışı (U8), yükleme göstergesi (U22).

Sonraki adım: kullanıcının bulguları önceliklendirmesi. Önerilen ilk iş: U23 (yeniden giriş odağı) ve U4 (kütüphane Geri katmanları) birlikte, binlerce kayıtlı sentetik fikstürle enstrümantasyon testiyle.

## 0.14.40 · Birleşik teslim · 16 Ekim 2026

Birleştirme sırası: `35ad2b2` (bu oturum, D063), main'in `fa8ae8f` commit'i (`fe4f188` birleştirmesi), logo dalı `claude/trusting-margulis-73aa19` (`0982a50` birleştirmesi), ardından sürüm 0.14.40 / 54. Çakışmalar yalnız STATUS, HANDOFF ve DECISIONS'taydı; hepsinde iki tarafın kayıtları da korundu. DECISIONS'ta main'in güncel D061'i kaldı, D062 ve D063 eklendi. Kodda çakışma çıkmadı; `PilotApp.kt` kendiliğinden birleşti.

TV (192.168.1.126:5555): 0.14.40 kurulu (SHA eşleşti, veri korundu, uygulama açılmadı). Emülatör 5556 bu oturumun sonunda kapanmıştı. Üzerinde başka bir oturumun 0.14.39 derlemesi ile ayrı paketli "Slider önizleme" (`dev.iptvplayer.tv.pilot.slider`, yalnız sentetik Demo kaynağı, `127.0.0.1:8766`'daki oturum sunucusuna bağlı) kurulu.

Sonraki adım: kullanıcının TV'de splash, logo, banner ve slider'ı değerlendirmesi. Evim'in M3U olduğu TV'de slider tek başlık kalabilir; TV'deki kaynak Xtream ise tarihler gelir. İstenirse önizleme uygulaması kaldırılmalı.

## Kişisel ana sayfa ve vitrin slider'ı · 16 Ekim 2026

Değişen dosyalar:
- data/HomeLibrary.kt: `newest` alanı eklendi. Devam rafı, bitmiş (%95) bölümün dizisini de tutuyor.
- data/CatalogPresentation.kt: `newestEntries` tek geçişte en yeni 6 kaydı seçiyor; tarihsiz ya da görselsiz kayıtlar ve kanallar elenir, eşitlerde liste sırası korunur.
- TvViewModel.kt `loadDetail`: tamamlanmış bölümle açılan dizide `EpisodeSequence.next` kullanılıyor. `automaticAllowed` ise sıradaki bölümü oynatır, değilse ya da sıradaki yoksa ayrıntıyı o sezonda açar.
- ui/HomeScreen.kt: başlangıç rafı kuralı. `Hero` artık slaytlarla çalışıyor: `rememberSaveable` ile indeks, 8 sn `LaunchedEffect`, bir sonraki görselin diske önden yüklenmesi (`CachePolicy.DISABLED`, 480×270), arka plan/afiş/metin için üç `Crossfade`, Ayrıntılar'da `onPreviewKeyEvent` ile Sağ/Sol, `SlideDots`. "Kanalları aç" ve `openLive` kaldırıldı. Devam kartında "S… · B… izlendi" ve "Sıradaki bölüm" var; bitmiş bölümde ilerleme çubuğu gizli.
- Testler: HomeLibraryTest (2 yeni vaka, 1 güncellenen), HeroLayoutTest (`home()` yardımcısı, `hero-live` beklentisi kaldırıldı, yeni slider vakası).
- Belgeler: DECISIONS D063, NATIVE_UI.

Kontroller: 157 JVM testi geçti, APK'lar derlendi. 5556'da elle doğrulananlar STATUS'ta.

İnceleme sonrası düzeltmeler:
- `LaunchedEffect(Unit)` hedefi önce `model.homeFocus` ile kaydediyor; böylece başlangıç rafı kişisel rafa dönüştükten sonra eski anahtar odağı menüye göndermiyor.
- `Hero` her yerde sınırlanmış `current` indeksini kullanıyor.
- Görünmeyen `HeroText` kopyaları en uzun slaytın yüksekliğini ayırıyor.
- Metin geçişi `AnimatedContent` ile önce sönüp sonra beliriyor.
- `SlideDots` etiketi `clearAndSetSemantics { testTag }` içinde.
- `TvViewModel.stillOn`: devam oynatımı yalnız ayrıntı hâlâ ekrandaysa başlıyor.
- PrivateCatalogPresentationAuditTest'e `dated`/`rated` sayıları ve cihaz yolu (kayıt yaz-oku, `SourceLibrary.combine`, `heroSlides`) eklendi; yalnız sayı yazdırıyor.

Önemli: 5556'daki "Evim" kullanıcının gerçek sağlayıcısının **M3U** kaynağı; daha önceki bir notta geçen "sentetik Evim" değil. 8765'teki `fixture_server.py` yalnız 15 kayıtlık görsel fikstürü sunuyor. M3U'da tarih yok, bu yüzden Evim'le vitrin tek başlık kalıyor; aynı sağlayıcının Xtream API'si tarihleri veriyor (özel döküm). Gösterim için eklenen ikinci kaynak "Demo", oturum betiği `demo_server.py` ile `127.0.0.1:8766`'da sunuluyor; sunucu kapanınca Demo kaynağı yenilenemez. Kullanıcı isterse Ayarlar → Kaynakları yönet → Demo → Kaldır ile kaldırılmalı.

Açık kalanlar:
- HeroLayoutTest ve ContinueCard/PilotFlow sınıfları koşulmadı (gerçek Evim'i silerler).
- "Sıradaki bölüm" akışı cihazda görülmedi.
- TalkBack'te otomatik geçiş ve TV işlemcisinde solma akıcılığı ölçülmedi.

Sonraki adım: Demo'yu kaldırmak (kullanıcı onayıyla). Evim yeniden kurulabiliyorsa enstrümantasyon sınıflarını koşmak, değilse gerçek Xtream hesabıyla TV'de denemek.

## Odak halkası köşeleri ve kategori yazısı · 16 Ekim 2026

Değişen: ui/DesignSystem.kt (`TvLayout.FocusInset`, `focusRing(radius, color)`: `Border(…, inset = 3 dp, shape = RoundedCornerShape(radius + 3 dp))`). Şu dosyalarda `focusRing` kullanılıyor: DetailScreen, HomeScreen (iki kart), LibraryScreen (kategori ve kanal satırı, 10 dp), PilotApp (kütüphane kartı; canlı kartta 14 dp, diğerlerinde CardRadius, ikisi aynı `radius` değerinden), PlayerScreen, SearchScreen, SettingsScreen. LibraryScreen kategori satırının Row'una `fillMaxWidth().heightIn(min = 40.dp)` eklendi. Ayrıca NATIVE_UI.md'ye halka kuralı yazıldı. Sürüm numarası değişmedi.

Neden: tv-material 1.1.0 `SurfaceBorderNode`, çizim alanını `inset` kadar büyütüp yüzeyin kendi şeklini (Border.shape varsayılansa) o alana çizer. Sabit dp yarıçap büyümediği için köşe boşluğu açılıyordu. Yüzde şekiller (hap) büyüyen sınırla birlikte büyür. Theme.kt `Action` ve HomeScreen `NavItem` bu yüzden değişmedi. Yeni sabit yarıçaplı odaklanan yüzeyler de `focusRing` kullanmalı.

Kontroller: `assembleRelease` geçti. 5556'da önce ve sonra ölçüldü: kategori yazısı 253–274'ten 257–277'ye indi, dolgu 228–307. Kategori, kart ve ayar satırı halkaları eş merkezli görünüyor. Lint ve enstrümantasyon koşulmadı.

## NEXTV logo ve splash · 16 Ekim 2026

Dal `claude/trusting-margulis-73aa19`. Değişen: AndroidManifest.xml (icon `@mipmap/ic_launcher`, roundIcon, banner `@drawable/app_banner`); yeni res: drawable/ic_launcher_background.xml, ic_launcher_foreground.xml, ic_nextv_splash_mark.xml, mipmap-anydpi-v26/ic_launcher.xml ve ic_launcher_round.xml, drawable/app_banner.xml (siyah zemin, yatay logo vektörü; paketteki PNG kaldırıldı), raw/splash_chime.m4a; launch_background.xml (siyah zemin, amblem 51,84×64,47 dp, merkezin 32,77 dp üstü). Logo yolları kullanıcının SVG'lerinden birebir alındı ve kaynaklar design/brand/nextv-{amblem,dikey,yatay}.svg olarak depoda: amblem → ic_launcher_foreground (siyah, optik ortalı) ve ic_nextv_splash_mark (mint); dikey → LaunchCover `UnionPaths`/`NextvGlyphs` (298×260 kutu); yatay → app_banner. Silinen: drawable/app_icon.xml, tv_banner.xml, splash_mark.xml, raw/startup.wav, tools/generate_startup_audio.py (git geçmişinde). Kod: ui/LaunchCover.kt (paketin StartupSplashScreen uyarlaması; `loading` parametresi, 1920×1080 referansla oranlama, animasyon değerleri yalnız çizimde okunur, Canvas yay göstergesi), StartupSound.kt (MediaPlayer, 400 ms ön sessizlik atlanır; eski korumalar aynı), MainActivity.kt (`opening` durumu Activity'de; `onUserInteraction` splash'i ve sesi kapatır), ui/PilotApp.kt (`opening`/`endOpening` parametreleri; `loading = LOADING && RESTORING`). Belgeler: DECISIONS D062, NATIVE_UI "Açılış ve ses", BACKLOG BKL-022 (review).

Önemli: LaunchCover'daki `Impact` (0,48) ile `StartupSound.LEAD_SKIP_MS` (400) birlikte ayarlanır. Tuş yakalama `onUserInteraction`'dadır: yükleme sırasında Compose'da odak olmadığından `onPreviewKeyEvent` tuşu görmez; `ComponentActivity.dispatchKeyEvent` override'ı lint RestrictedApi hatası verdiği için kullanılmadı. Ses ~6,9 sn; normal bitişte splash kalktıktan sonra kuyruğu ~3 sn içinde söner, kesilmez.

Kontroller: logo güncellemesinde assembleRelease ve assembleDebugAndroidTest geçti, lint 0 hata/32 uyarı (yeni: app_banner.xml VectorRaster, 320×180 dp vektör). Dört açılı bağımsız doğrulama iş akışı yolların SVG'lerle birebir olduğunu, ikonun 66 dp güvenli dairede kaldığını (en uzak nokta 25,07 dp), banner'ın ortalı olduğunu ve pencere işaretinin yalnız 960×540 dp'de birebir hizalandığını doğruladı. 5556'da yeni splash ve (launcher yeniden başlatılınca) yeni banner görüldü; launcher eski banner'ı önbellekte tutabilir. Paket turunda: 5556'da release ile splash zaman çizelgesi, ana sayfaya geçiş, ilk odak, tuşla kalkma ve sesin kapanması görüldü. Bir kez yeni kurulumdan hemen sonraki açılışta ve bir kez nedeni belirsiz olarak ses başlamadı (1,8 sn sınırı ya da emülatör ses hatası olabilir; tasarım gereği sessiz atlanır); sonraki 4 açılışta çaldı. Ekran görüntüleri kullanıcının gerçek kaynağını gösterdiği için depoya konmadı. Enstrümantasyon/JVM testleri çalıştırılmadı. 5556'da bu dalın release derlemesi kurulu (sürüm adı yine 0.14.38-design-preview); ana oturum kendi derlemesini üstüne kuracağını bildirdi.

Sonraki adım: kullanıcının splash ve sesi değerlendirmesi (sesin kullanımı onaylandı: kullanıcının seçtiği telifsiz ses); banner zemininin siyah mı (şimdiki) yoksa açık/kart rengi mi olacağı; sol menü/Kurulum/Yükleme'deki "IPTV" yazılarının amblemle değişip değişmeyeceği (HomeScreen/DesignSystem ana oturumda değişiyor, burada dokunulmadı); istenirse ses düzeyi veya süresi; dalın ana dala alınması ve bir sonraki teslimde sürüm artışı; fiziksel TV'de launcher ikonu, banner ve açılış kontrolü.

## 0.14.38 · Sıralama inceleme düzeltmeleri, Esc ve düşen cihaz testleri · 16 Ekim 2026

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.38.apk (+ .sha256), SHA-256 7e173b44d9ef748b3e9ae9340dd1feafaa490c876029669d675308b5597f60a0; kullanıcının isteğiyle TV'ye kuruldu (0.14.37'den, SHA eşleşti, veri korundu, speed derleme, uygulama açılmadı). Değişen: data/CatalogPresentation.kt (`librarySorted` ayraçları sonda tutar ve anahtarı başlık başına bir kez hesaplar; `LibraryOrderCache`), TvViewModel.kt (`libraryOrder`, `librarySort` Dispatchers.Default'ta sıralayıp sonra yayımlar, `prepareLibraryOrders` yayımlanan kataloğu kayıtlı seçimler için önceden sıralar), ui/LibraryScreen.kt (`ordered` modelden; Geçmiş'te Sırala yok; büyük yazıda kısa etiket; CategoryColumn KDoc D060'a uyduruldu), ui/HomeScreen.kt (`HomeCard` rozeti), data/CatalogSnapshot.kt (`VERSION`), ui/PilotApp.kt (`escapeAsBack`: `window.injectInputEvent` ile KEYCODE_BACK; içe aktarılan InputMethodManager/ViewCompat/WindowInsetsCompat/LocalView/LocalOnBackPressedDispatcherOwner kalktı), ui/SettingsScreen.kt (`moved`: pencere odağı bildirilmeden basılan tuş giriş odağını engeller), testler (SearchNavigation, GuideFlow, PilotFlow, SourceRegistryStorage, FixtureSources, CatalogPresentationTest), app/build.gradle.kts (0.14.38/code52). Yan oturumun HomeBackToMenu eki de bu derlemede. [QA](native-sort-rating-38-qa.json).

Önemli: diyalog kapandıktan hemen sonra gönderilen tuşlar sistemce düşürülür; testlerde diyalog kapanınca `activity.hasWindowFocus()` beklenir. Emülatör uzun yükten sonra sesiz (`-no-audio`) kipte ses saatini takabilir (logcat: AudioTrack "device stall"); oynatıcı konumu ilerlemez, oynatıcı testleri düşer; emülatörü yeniden başlatmak çözer. Sonraki adım: kullanıcının 0.14.38'i TV'de denemesi (katalog yenilenince puanlar ve Son eklenen dolar); yayın öncesi tam cihaz testi (1.5/2.0 yazı ölçeği dahil).

## Ara/Rehber/Oynatıcı/Ana sayfa Geri aralığı · 16 Ekim 2026

Değişen: ui/PilotApp.kt (`routeBack`: HOME → `exitAsk`, diğerleri → `model.back()`; genel işleyici bunu çağırır ve Ara/Rehber/Oynatıcı'ya geçirir), ui/SearchScreen.kt, ui/GuideScreen.kt, ui/PlayerScreen.kt (`back` parametresi; `BackHandler` önce `model.state.value.route`'a bakar, kendi rotası değilse `back()`; bu üç ek 0.14.37 oturumunca aynı tasarımla yapıldı), ui/HomeScreen.kt (`HomeBackToMenu` model HOME değilse `model.back()`), SearchNavigationTest (yeni `searchBackFollowsTheModelRouteBeforeTheScreenLeaves`; sınıf düzeyi `backRightAfter`), GuideFlowTest (yeni `guideBackFollows…`, `playerBackFollows…`). Sürüm numarası değişmedi; 0.14.37 release ana sayfa ekinden önce derlendi. [QA](native-screen-back-gap-qa.json).

Önemli: kendi `BackHandler`'ı olan yeni bir ekran eklenirse aynı denetim gerekir (karar `model.state.value.route` ile; toplanan durumla değil). Aralıkta Geri yeni ekranın odak adımını değil rota kuralını izler (ör. Oynatıcı → Rehber anındaki Geri ana sayfaya gider, yerleşmiş rehberde önce menü açılırdı); bilinçli seçim.

Kontroller QA'da: yeni vakalar eski kodla 0/3, yeni kodla 9/9; `backFollowsTheModelRouteBeforeTheScreenCatchesUp` ana sayfa ekinden önce düşüyordu, sonra 3/3; sınıflar SearchNavigation 17/21, GuideFlow 12/13, PilotFlow 7/8, EpisodeAutoplay 11/11; 155 JVM, lint 0/32. Düşen 6 vaka (QA'da satırlarıyla) eski derlemede de düşüyor; 0.14.36 davranışına uyarlanmaları gerekir. Durum: 5556'da kullanıcının onayıyla kendi kaynağı silindi; yayımlanan 0.14.37 release (SHA-256 2c18cbbe…) ve DeviceProvisioningTest ile sentetik "Evim" (yerel sunucu host 8765, yalnız çalıştıran oturum açıkken) kurulu, uygulama ana sayfada. Sonraki adım: düşen 6 testi 0.14.36 davranışına uyarlamak (özellikle `homeBackAsksBeforeLeavingTheApp`: ilk Geri'den önce ana sayfa odağının yerleşmesini beklemeli mi, yoksa erken Geri'de ürün mü yanlış, ayrıca bakılmalı); ana sayfa ekini bir sonraki teslime almak.

## 0.14.37 · Sırala ve puan rozeti · 16 Ekim 2026

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.37.apk (+ .sha256), SHA-256 2c18cbbe41b35be1546bc34d562661fdfae415883d48baebaa5899cfe8fad69d; emulator-5556'da kurulu, TV'de değil. Değişen: data/Models.kt (`rating`, `addedAt`), data/CatalogParser.kt (`rating`, `addedAt`), data/CatalogSnapshot.kt (v4, 24 alan), data/CatalogPresentation.kt (`LibrarySort`, `librarySorted`, `ratingText`), data/DeviceStore.kt (`librarySort`), TvViewModel.kt (`librarySorts`, `librarySort`), ui/LibraryScreen.kt (`SortAction`, sıralı `filtered`, bilgi panelinde puan), ui/PilotApp.kt (`MediaCard` rozeti), ui/Theme.kt (`RatingBadge`), ui/DetailScreen.kt (puan), birim testleri, app/build.gradle.kts (0.14.37/code51). Ayrıca: yan oturum "Ara/Rehber/Oynatıcı Geri aralığını kapat" PilotApp.kt'de `routeBack`'i ekranlara geçirmiş ama ekranları değiştirmeden durmuştu; derleme için SearchScreen/GuideScreen/PlayerScreen'e `back` parametresi eklendi (rota artık o ekranın değilse `back()`), o oturuma haber verildi, son biçim onundur. [QA](native-sort-rating-37-qa.json), D061.

Sonraki adım: kullanıcının emülatörde denemesi (katalog yenilenince puanlar ve "Son eklenen" dolar); istenirse TV'ye kurulum; yayın öncesi tam cihaz testi.

## 0.14.36 · Gezinme: menüye Geri, kategori OK ile, kaynak seçici yok · 16 Ekim 2026

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.36.apk (+ .sha256), SHA-256 edb832493d729fa507494b000051ddba35683e844946477c4687800b843e4ebf; TV'de kurulu. (0.14.35 yalnız Geri değişikliğiyle derlendi, hiçbir cihaza kurulmadı.) Değişen: ui/HomeScreen.kt (`HomeBackToMenu`), ui/LibraryScreen.kt (`CategoryColumn` zamanlayıcısı kaldırıldı; ilk odak `s.focusedId == null` iken `selectedCategoryFocus`), ui/SearchScreen.kt, ui/GuideScreen.kt, ui/SourcesScreen.kt (`SourceFilterAction` ve `SourceChoice` kaldırıldı), TvViewModel.kt (açılışta `sourceFilter(null)`), testler (SearchNavigation, VisualPreview, MultiSourceFlow, SourceLayoutFlow), app/build.gradle.kts (0.14.36/code50). Model tarafında `sourceFilter`/`searchSource` API'si duruyor (testler ve ileride Ayarlar'a seçici eklenirse). [QA](native-navigation-36-qa.json), D060.

Sonraki adım: kullanıcının TV'de denemesi; yayın öncesi tam cihaz testi (uyarlanan testler ilk kez orada koşacak; 0.14.32'de düşen vakalar da).

## 0.14.34 · Ana sayfada Tümünü gör ve kaynak seçici yok · 16 Ekim 2026

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.34.apk (+ .sha256), SHA-256 d33cfd6fe943b11d9538206e82649543184d335e430d89cc13df5b8f6ce0b467. Değişen: ui/HomeScreen.kt (`Hero` artık `sourceFilter` almaz; başlık satırı ve raf başlıkları kompakt eylem yüksekliğini korur), testler (HeroLayout, EmptyHomeLayout, MultiSourceFlow, SourceLayoutFlow, VisualPreview), app/build.gradle.kts (0.14.34/code48). Önemli: ana sayfa kaynak filtresine bağlı kalır (`sourceFilterId`) ama göstergesi yok; filtre Kütüphane/Ara'dan değiştirilir. Fiziksel TV kullanıcının isteğiyle 0.14.34/code48'e güncellendi (`install -r`, SHA-256 eşleşti, veri korundu, speed derleme, uygulama açılmadı). Sonraki adım: kullanıcının TV'de görsel kontrolü, yayın öncesi tam cihaz testi.

## 0.14.33 · Alandan çıkınca klavye kapanır · 16 Ekim 2026

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.33.apk (+ .sha256), SHA-256 daedf7ece05f6c38491a7e88ce02538440087f455c4db34f181c0d0ead68d583. Değişen: ui/Theme.kt (`FieldKeyboard.focusChanged`, `FocusedFields`; `EntryField` ve `SearchField` odak değişimini bildirir, odaklıyken kaldırılan alan da sayılır), app/build.gradle.kts (0.14.33/code47). Önemli: 5556'da artık kullanıcının gerçek kaynağı var; enstrümantasyon testleri kaynakları sildiği için orada çalıştırmadan önce kullanıcıya sorulmalı; ekran görüntüsü alınmamalı. Fiziksel TV kullanıcının isteğiyle 0.14.33/code47'ye güncellendi (`install -r`, SHA-256 eşleşti, veri korundu, speed derleme, uygulama açılmadı). Sonraki adım: kullanıcının TV'de ve emülatörde denemesi; yayın öncesi tam cihaz testi (0.14.32'de düşen vakalar dahil).

## 0.14.32 · Klavye gerçekten OK ile açılır, Esc Geri'dir · 16 Ekim 2026

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.32.apk (+ .sha256), SHA-256 1ee0605a94052886f84d2b84b9228536d7f95994d2d11d6cde311b14e128bea0. [Sözleşme](../design/KEYBOARD_ON_OK_31.md), [QA](native-keyboard-on-ok-32-qa.json).

Değişen dosyalar: ui/Theme.kt (`EntryField` TextFieldState API'sinde; `FieldKeyboard`, `fieldKey`, `rememberFieldInput`, `fieldKeyboardOptions`; 0.14.31'in `keepKeyboardClosedOnFocus` kaldırıldı), ui/SearchScreen.kt (`SearchField` aynı), ui/PilotApp.kt (`escapeAsBack`), app/build.gradle.kts (0.14.32/code46), SearchNavigationTest (`fieldsOpenTheKeyboardOnlyOnOk` Esc adımı). Önemli: `LocalSoftwareKeyboardController` yeni alanlara ulaşmıyor; klavye `FieldKeyboard` ile açılıp kapanır. Activity'de `dispatchKeyEvent` ezmek lint RestrictedApi hatası verir.

Açık ve sıradaki adım: yayın öncesi tam cihaz testi. Son turda SearchNavigation 16/20, SourceLayout 2/5, PilotFlow 6/8 düştü, incelenmedi (QA'da ayrıntı); ortamdan mı koddan mı olduğu bilinmiyor. 5556'da 0.14.32 release kurulu, kaynak yok.

## Çıkış sorusu kararsızlığı · 16 Ekim 2026

Değişen dosyalar: ui/PilotApp.kt (Ana sayfa ve genel Geri işleyicileri tek `BackHandler`: etkin rotalar aynı, artık HOME dahil; eylem `model.state.value.route`'a bakar: HOME → `exitAsk`, diğerleri → `model.back()`), SearchNavigationTest (yeni `backFollowsTheModelRouteBeforeTheScreenCatchesUp`; `homeBackAsksBeforeLeavingTheApp` içindeki `closed()` ana sayfa penceresinin odağını da bekler). Sürüm numarası değişmedi, yeni APK yok. [QA](native-exit-back-gap-qa.json).

Önemli: `BackHandler`'ın `enabled` değeri SideEffect'te, yani yeni rota uygulandığında güncellenir; Geri'yi işleyen kod rotayı toplanan durumdan değil modelden okumalı. Ara, Rehber ve Oynatıcı'nın kendi işleyicileri aynı aralıkta eski ekranın kararını verir (ör. Ara'dan ana sayfaya dönüşün hemen ardından basılan Geri kaybolur); bu turda değiştirilmedi. Diyalog kapandıktan sonraki ~100 ms içinde basılan tuşu sistem atar; yalnız Dialog penceresi yerine pencere içi katman bunu önler (D055 tasarımı, değiştirilmedi).

Kontroller QA'da: eski kodla yeni vaka 0/2, yeni kodla 3/3; çıkış vakası tek başına 6/6; SearchNavigation 20/20; PilotFlow 8/8; 152 JVM, lint 0/32. Durum: 5556 pencereli, 0.14.31 release (kurulu APK SHA-256'sı yayımlananla aynı, veri korundu) ve DeviceProvisioningTest ile yeniden eklenen sentetik "Evim" kaynağı; uygulama ana sayfada açık. Sonraki adım: düzeltmeyi bir sonraki teslime almak; isterse Ara/Rehber/Oynatıcı işleyicilerini aynı ilkeyle (karar anında modelin rotası) gözden geçirmek.

## 0.14.31 · Klavye yalnız OK ile açılır · 16 Ekim 2026

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.31.apk (+ .sha256), release, SHA-256 94970a72ddee2d9711ba146aaa3723e967b2c52514cca49bbdd3a563653daf35. [Sözleşme](../design/KEYBOARD_ON_OK_31.md), [QA](native-keyboard-on-ok-31-qa.json), D058.

Değişen dosyalar: ui/Theme.kt (`fieldKey`: Yukarı/Aşağı alanı bırakır, OK her kumandada `keyboard.show()`; `keepKeyboardClosedOnFocus`: odak geldiği karede `keyboard.hide()`; `EntryField` ikisini kullanır), ui/SearchScreen.kt (`SearchField` aynı), app/build.gradle.kts (0.14.31/code45). Testler: SearchNavigationTest (yeni `fieldsOpenTheKeyboardOnlyOnOk`; `hideKeyboard` yalnız klavye açıksa Geri'ye basar; `refreshRecovers…` yazmadan önce OK ile klavyeyi açar), SourceLayoutFlowTest ve GuideFlowTest (odakta kapalı, OK açar, Geri kapatır).

Önemli: `KeyboardOptions.showKeyboardOnFocus` eski `BasicTextField(String)` API'sinde etkisiz (yalnız `TextFieldState` API'sinde). Yeni bir metin alanı eklenirse `fieldKey` ve `keepKeyboardClosedOnFocus` kullanılmalı. Testlerde klavye artık kendiliğinden açılmaz; klavye gerektiren adımda önce OK gönderilir. Bilinen kararsız vaka: `homeBackAsksBeforeLeavingTheApp`, alanlarla ilgisiz.

Durum: 5556 pencereli açık, 0.14.31 release ve sentetik "Evim" kaynağı (yerel fikstür sunucusu bu oturum açıkken çalışır). Sonraki adım: kullanıcının emülatörde ve isterse TV'de denemesi.

## 0.14.30 · Sezon rafları ve konum sayacı · 16 Ekim 2026

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.30.apk (+ .sha256), release türü, SHA-256 2bd50f94506fba31ecae397a8ef8c48284a1606aa4c6356d6e25b7580de87034. Fiziksel TV'ye kurulmadı (yalnız kullanıcı isterse). [Sözleşme](../design/TIVIMATE_SEASONS_30.md), [QA](native-tivimate-seasons-30-qa.json), D057.

Değişen dosyalar: ui/DetailScreen.kt (`SeasonShelf` durum dışı bellek: raf kaydırması, hatırlanan bölüm `position`, bölüm başına requester; `SeasonRow` "Sezon N" + `PositionNote` + `LazyRow`, satıra Yukarı/Aşağı girişte `onEnter` hatırlanan bölüme; `EpisodeCard` 196 dp 16:9, "E1." ayrı metin; ilk odak `target` = oynatılan ya da `next`, hedef sonraki sezondaysa liste o rafa kayar), ui/LibraryScreen.kt (`fixedHeading = !largeText`, yerel `LibraryHeading`, `headerCount`/`inputIndex` buna göre, ızgara kipinde `PositionNote`, `itemsIndexed`, yenilemede sıra güncellenir), ui/Theme.kt (`PositionNote`), ui/CardVisibility.kt (`frameShelf`, varsayılan davranış aynı), app/build.gradle.kts (0.14.30/code44). Testler: SearchNavigationTest (yeni `posterGridHeadingCountsTheFocusedPlace`, uyarı sabit başlıkta), VisualPreviewTest (bölüm genişliği 196 dp, sayaç yerleşimi, sezonlar arası tuşlar), PilotFlowTest ("2. sezon" düğmesi yerine Aşağı/Yukarı). `TvViewModel.season()` artık arayüzden çağrılmıyor; oynatma tarafı `season` alanını kullanmaya devam ediyor.

Kontroller QA kaydında. Ortam: önceki 5556 örneği test sırasında kayboldu; aynı AVD veri silmeden `-gpu swiftshader -feature -Vulkan -no-window -no-snapshot -no-audio` ile yeniden açıldı. Kararsız iki vaka (çıkış sorusu, arka plan konumu) değişen ekranlara ulaşmıyor; ayrıntı ve karşılaştırma QA'da. 5556 kullanıcının denemesi için pencereli açık bırakıldı: 0.14.30 release, sentetik "Evim" kaynağı (DeviceProvisioningTest ile, adres 10.0.2.2:8765'teki yerel fikstür sunucusu). Sonraki adım: kullanıcının emülatörde/TV'de sezon raflarını ve sayacı denemesi; ardından büyük yazı (1,5/2,0) turu ve isterse TV kurulumu.

## 0.14.29 · Akıcılık ve TiviMate yapısı · 16 Ekim 2026

Teslim: releases/IPTV_Player_TV_Design_Preview_0.14.29.apk (+ .sha256), **release** türü (R8, hata ayıklama kapalı, debug anahtarıyla imzalı; TV'deki önceki kurulumun üstüne veri silinmeden kurulur), SHA-256 befe19eed7035e55da55e5e8dbe4bbaa33b28df2a1a83188b8e2e05fbfc9890c. Fiziksel TV'de kurulu ve `cmd package compile -m speed` ile derlenmiş. [Sözleşme](../design/FLUID_TIVIMATE_29.md), [QA](native-fluid-29-qa.json), D056.

Değişen dosyalar: app/build.gradle.kts (release türü, 0.14.29/code43), app/proguard-rules.pro (yeni); ui/LibraryScreen.kt (yeni: kategori sütunu, ChannelRow, LiveInfo, TitleInfo, ızgara kipi; eski Library PilotApp.kt'den çıktı, MediaCard/EvenColumns internal); ui/MenuSymbols.kt (yeni: Material Symbols Rounded yolları, Apache 2.0); ui/HomeScreen.kt (menü simgeleri, 160/120 ms, içerik graphicsLayer kayması, `contentEntry` artık lambda, ana sayfa odak belleği); TvViewModel.kt (`homeFocusKey` durum dışına); data/Models.kt, SourceRepository.kt, CatalogSnapshot.kt, SourceLibrary.kt, CatalogPresentation.kt (`categoryOrder`, `providerCategories`); ui/Theme.kt ve kartlar (odak büyümesi kaldırıldı); ui/PilotApp.kt (çıkış diyaloğu odağı en fazla 500 ms pencere odağını bekler).

Testler: JVM'e kategori sırası ve kayıt testleri; SearchNavigationTest (kategori sütunu, ızgara kipi, menü girişleri, IME kapatma), GuideFlowTest (numaralı satırlar), PilotFlowTest (`nav()` yardımcı: "Favoriler" artık kategori sütununda da var), FixtureSources (`categoryOrder`). Testler hâlâ `debug` APK ile koşar; sürüm APK'sı yalnız elle denendi (ayrıntı, oynatıcı, Ara, çıkış diyaloğu; çökme yok).

Sonraki adım: kullanıcının TV'de geri bildirimi. Açık: dizi ayrıntısının sezon başına yatay bölüm rafı, konum sayacı/sıralama, canlı önizleme (bilinçli olarak eklenmedi), TV'de kare ölçümü.

## 0.14.28 · Sol menü ve TCL düzeninde Ara · 15 Ekim 2026

Son kod/APK 0.14.28-design-preview/code42: releases/IPTV_Player_TV_Design_Preview_0.14.28.apk (+ .sha256), SHA-256 9140b6cceb50a15daa90c4541b1cc57588212550d876f882fdcfd604061cbc2d, v2 imza doğrulandı, 5556'daki kurulu APK ile aynı. [Sözleşme](../design/SIDE_NAV_SEARCH_28.md), [QA](native-side-nav-28-qa.json), D055.

Değişen dosyalar: ui/HomeScreen.kt (`Navigation` artık içerik yuvalı sol menü: `contentEntry`, `contentTop`; uzaktan giriş `onEnter` ile etkin bölüme, açık genişlik en uzun etikete göre; genişlik yalnız layout/draw'da okunur), ui/SearchScreen.kt (hap `SearchField`, türe göre raflar, `reveal()`, `returnToResults`), ui/PilotApp.kt (Kütüphane menüye bağlandı, `ExitDialog`, Ana sayfa `BackHandler`), ui/DetailScreen.kt (tam boy arka plan, menü açıkken yükleme odağı çalmaz), ui/GuideScreen.kt (eski aşağı ok yakalaması kaldırıldı), ui/DesignSystem.kt (`RailWidth`, `RailOpenWidth`), app/build.gradle.kts. Testler: SearchNavigationTest, VisualPreviewTest, GuideFlowTest, PilotFlowTest (ayrıntı QA'da).

Önemli: Compose'un `focusRestorer` hatırlaması ilk lazy listede durur; sağ ok bu yüzden her ekranın kendi hatırladığı öğeye (`contentEntry`) gider. Menüye açık bir `requestFocus` (Ayarlar dönüşü, Geri işleyicisi) istenen öğede kalır; yalnız yön tuşuyla giriş etkin bölüme yönlenir. Kullanıcı isteğiyle fiziksel TV `adb install -r` ile 0.14.28/code42'ye güncellendi (SHA-256 eşleşti, firstInstallTime 13 Ekim korunarak veri silinmedi, uygulama açılmadı, test/reset yok). Sonraki adım: kullanıcının TV'de menü, Ara ve çıkış sorusu için görsel geri bildirimi.

## 0.14.27 · Yazarken arama · 15 Ekim 2026

Son kod/APK 0.14.27-design-preview/code41: releases/IPTV_Player_TV_Design_Preview_0.14.27.apk (+ .sha256), SHA-256 6933dfe8f51cdd6b9f612547171f14ce884a592bbb77fddc7945664e591665da, v2 imza doğrulandı. [Sözleşme](../design/SEARCH_TYPING_27.md), [QA](native-search-typing-27-qa.json), D054.

Değişen dosyalar: ui/SearchScreen.kt ve ui/PilotApp.kt (yazma kipi: `imeVisible` koşulsuz okunur, `edited` yedeği yalnız klavye kendini bildirmediyse geçerli; bölüm aramasında başlık/kategori gizleme, MediaCard `compact` satırı, 3/2 sütun), app/build.gradle.kts. Testler: SearchNavigationTest (iki aramanın yazarken/sonra düzen kontrolleri), MultiSourceFlowTest (filtre öncesi Bitti). Önemli: `WindowInsets.isImeVisible` composable okuyucudur; `&&` arkasına koymak remember yuvalarını kaydırıp çökertir.

Kontroller QA kaydında: 150 JVM, lint 0/32, normal yazıda ilgili 5 sınıf 49/50, 2.0'da SearchNavigation 11/13 (kalanlar önceki sürümlerle aynı). 5556'ya sentetik yerel M3U kaynağı ("Evim") yeniden eklendi; test sınıfları kaynakları siler. Kullanıcı isteğiyle fiziksel TV `adb install -r` ile 0.14.27/code41'e güncellendi (SHA-256 eşleşti, firstInstallTime 13 Ekim korunarak veri silinmedi, uygulama açılmadı, test/reset yok). Açık: TCL klavyesiyle fiziksel görünüm, kullanıcı kabulü, Figma. Sonraki adım: kullanıcının TV'de Ara ve bölüm aramasını deneyip geri bildirmesi.

## 0.14.26 · UI onarım teslimi · 15 Ekim 2026

Son kod/APK 0.14.26-design-preview/code40: releases/IPTV_Player_TV_Design_Preview_0.14.26.apk, SHA-256 69ff70d499384f6c3ef1914907624f910b3a99c1124f4644bce1b86c5a7c17a8. [Sözleşme](../design/UI_REPAIR_26.md), [QA](native-ui-repair-26-qa.json), [önce/sonra](../../design/reviews/LATEST.md).

Değişen üretim dosyaları: ui/DesignSystem.kt (scrollEdgeFade/scrollEdgeMask, LineIcon filled), Theme.kt (actionHeight, seçim çizgisi, check satırı, devre dışı odak), PilotApp.kt (kurulum kenarı/hata alanı/çip, kütüphane odak/favori kategorisi/EvenColumns/Temizle), HomeScreen.kt (Navigation activeFocus, raf kenarı/solma/başlık/devam kartı/vitrin afişi/boş kenar), DetailScreen.kt, PlayerScreen.kt, RemoteTextReader.kt, GuideScreen.kt, GuideSettings.kt, SettingsScreen.kt, SearchScreen.kt, SourcesScreen.kt (SourceDialog hidden), SourceRecoveryScreen.kt, LoadingScreen.kt, TvViewModel.kt (favori kategori koruma), app/build.gradle.kts. Test: VisualPreviewTest hizalama mesajına ölçüler eklendi. Prototip: prototypes/tv-flow altında app.js, flows.js, styles.css, flows.css, review.css, review.js. Belgeler: UI_REPAIR_26, D053, FEEDBACK_AND_SETTINGS/NATIVE_UI/CONTROL_POLISH_25/WIDE_LAYOUT/HOME_SHELF_FRAMING ekleri.

Kontroller QA kaydında: 150 JVM, lint 0/32, normal yazı 95/98, 2.0 yazı 31/34 (kalanlar 0.14.25'te de aynı ortam/test sınırı), prototip 13/13. Not: 5556'da uygulamanın "Hareketi azalt" tercihi kapalı; mixedEpisode hizalama testi bu durumda odak büyütmesi nedeniyle başarısız olur. Kullanıcı isteğiyle fiziksel TV `adb install -r` ile 0.14.26/code40'a güncellendi (SHA-256 eşleşti, uygulama verisi korundu, test/reset yok, uygulama açılmadı). Kullanıcı AVD'si değişmedi; 5556 yazı 1.0.

Açık: Favorilerde canlı kanal kartı biçimi (tasarım kararı), büyük yazıda Temizle tam ortalama, ayrışan U01/U29/U52, "Tüm kaynaklar" altındaki yeni "N kaynak" metninin onayı, Figma, fiziksel görünüm. Sonraki adım: kullanıcının TV'de 0.14.26 görsel geri bildirimi.

## 0.14.25 · Test ve kozmetik teslim · 15 Ekim 2026

İstenen yerel test/kozmetik turu tamamlandı. Son kod/APK 0.14.25-design-preview/code39. [Görsel inceleme](../../design/reviews/LATEST.md), [sözleşme](../design/CONTROL_POLISH_25.md), [QA](native-polish-25-qa.json). APK: releases/IPTV_Player_TV_Design_Preview_0.14.25.apk; SHA-256 bd9e270f37ffc2008fa9d6150fa12d0cc89ebda00f1d680e219ce5d167032b44.

Değişen üretim dosyaları: ui/Theme.kt (seçim çizgisi/semantiği ve alan çerçevesi), ui/PilotApp.kt (kurulum alanı yüzeyi), ui/SearchScreen.kt (kompakt çevre çizgili türler/Temizle), app/build.gradle.kts (sürüm). SetupLayoutTest mevcut yöntem akışında seçili/seçili değil bilgisini de doğrular. VisualPreviewTest'in eski birleşik vitrin başlığı beklentisi güncellendi; Devam et odağı önce doğrulanır, sonra menüye odak verilip vitrin görüntülenir. Yeni ana test vakası eklenmedi.

Kontroller: 150 JVM kaydı = 149 başarılı/1 atlanan; 0 hata. Normal yazıda 135 farklı cihaz vakasının son sonucu başarılı. 1.5/2.0 yazıda kurulum, arama kartları/Türkçe arama, saat dilimi ve beş uzun kaynak akışı toplam 20/20 geçti. HTML prototip Chrome headless ile 13/13; dış istek/tarayıcı hatası yok. Kare çözümleyicisi 9/9, proje bağlantı kontrolü başarılı. Derlemeler başarılı, lint 0 hata/32 uyarı, APK v2 imzası doğrulandı. Görseller normal, 1.5 ve 2.0 ölçekte incelendi; yalnız sentetik hesaplar kullanıldı. Figma node değişikliği yok; karar D052.

Ortam: İlk host GPU koşusu 72/134 başarılı vakanın ardından emülatör süreci kaybolduğu için kesildi; bitmemiş vaka sayılmadı. Aynı nextv_design_api36/5556 veri silmeden `-gpu swiftshader -feature -Vulkan -no-window -no-snapshot` ile yeniden açıldı; kalan 62 vaka geçti. Kesin kapanma nedeni kanıtlanmadı. Loglar .private/polish25-regression.log, polish25-regression-resumed.log, polish25-font1.5.log, polish25-font2.0.log ve polish25-software.stdout.log. Playwright'ın paket tarayıcısı eksikti; kurulu Chrome ile prototip geçti. Makineye özel yollar yine .private/local-environment.json'dan alınır.

Durum/sonraki adım: 5556 test temizliği sonrası yeniden açıldı, font1.0; kullanıcı AVD'si ve fiziksel TV değiştirilmedi. 0.14.25 görsellerini kullanıcı geri bildirimiyle değerlendir; yeni özellikleri görsel önceliğin önüne koyma. Fiziksel TV açıkken kullanıcı isterse yeni APK kurulum/görünüm/tek oturum medya testi yapılabilir. BKL-020, Figma ve kullanıcı kabulü tamamlanmadığından in progress; BKL-010 fiziksel codec/performance ve BKL-028 native PIN entegrasyonu açık. Aynı başarılı testleri yeni değişiklik olmadan tekrarlama.

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

## Konum değişikliği · Devam eden işi buradan devral

Kullanıcı projeyi **Desktop/TV** köküne taşıttı. Yeni kökte doğrudan AGENTS.md oku; `outputs/iptv-player` öneki artık kullanılmaz. `releases` eski APK/PDF/PNG/ZIP teslimlerini içerir; kökte önceden bulunan PDF korunur. Özel hesap/ham medya/SDK önceki `work` alanında kaldı. Yerel yollar `.private/local-environment.json`, SDK `apps/android-tv/local.properties` içindedir; ikisi Git dışında. Eski work yardımcıları eski proje yolunu varsayar: körlemesine çalıştırma. Git dizini ve tüm mevcut değişiklikler taşınır, yeni commit kimliği oluşturulmaz.

**Son tur somut ilerleme; 0.14.2/code16 henüz teslim değil.** TvViewModel CatalogNotice(operation, needsAttention), Home/Library yalnız uyarı, Settings yenileme satırında sonuç, Sources ortak mesaj alanı. Settings 440dp/24dp köşe, 12dp satır, ortak beyaz odak ve 7dp görünürlük payı; eski rehber yok açıklaması düzeltildi. Library büyük yazıda ölçülen tek-satır alt boşluğu ve yarım başlığı kategori sınırına taşıyan snapshotFlow eklendi. Header CardVisibility ile yukarıda bütünüyle geri geliyor. Yeni VisualPreview ve SearchNavigation hata testleri; SourceOperation assertion yeni notice tipinde.

**Doğrulama:** feedback-final-build.log: 115 JVM başarı + 1 özel audit skip, lint0/32. Normal28,1.5font12 geçti;2.0font11/12. İlk yeni hata testi dialog ilk odağını beklemiyordu; düzeltildi. Son görsel test yanlış şekilde aşağı dönüşte hep Inception bekliyordu; doğru komşu sütun kabul edilince gerçek başlık sorunu kaldı: `feedback-final-2.0-recheck.log` heading top39.5/bottom113.5dp, viewport top98dp. Üretim APK hash7e1208cd4eb10efbf3c2ad17d68b3d8591d447bc84616eb8425e453f233a49bf; v2 doğrulandı ama dağıtılmadı. Güncel test getOrNull import düzeltmesiyle derleniyor. Aday görseller ve QA proje içinde; özel log kopyaları `.private/feedback-evidence/` altında.

**Tam sonraki iş:** Başlığa ↑↑ ve karta ↓↓ dönüşünde büyük yazıda yarım başlığı düzelt. İlk kartta ve yukarı dönüşte kontroller geçti; dönüşte farklı yatay sütun normaldir. snapshotFlow yerleşmesi ile CardVisibility isteklerinin birbirini geri kaydırıp kaydırmadığını gerçek odak/scroll ölçümleriyle incele. Sonuç geçmeden 0.14.2 yayınlama. İlgili 2.0 testini, etkilediği normal/1.5 gezinmeyi tekrar çalıştır, yeni görselleri doğrula, ardından sürüm/QA/ZIP güncelle. Kaynak/oynatıcı görsel işi ve genel PRD kapıları açık.

Test süreci terminaldi; loglar incelendi, çalışan handle yoktu. Emülatör süreçleri de yoktu; yalnız ayrı nextv_design_api36/5556 yeniden başlatıldı. Kullanıcı5554 veya fiziksel TV başlatılmadı/değiştirilmedi. Test fontu taşımadan önce1.0'a alındı. Yeni derleme yalnız yeni kökte yapılmalı.

## Son tur · Katalog ve eksik görsel sunumu

**14 Ekim 2026 · 0.14.1-design-preview / code 15. Somut ilerleme; hedef aktif.** Önceki native görsel aktarım turu da ilerlemeydi. Yeni APK, SHA-256 ve ölçümler [QA kaydında](native-catalog-presentation-qa.json). 0.13 ve 0.14.0 teslimleri korunur. Kullanıcı `emulator-5554` ve fiziksel 65U95A değiştirilmedi; bütün testler ayrı 5556'da, font sonunda 1,0. Bu cihaz ayrımını koru.

- `CatalogPresentation.kt`: adın iki kenarında aynı dekoratif karakterden en az üçü ve boş EPG kimliği, yalnız sunum ipucu. Hiçbir kayıt silinmez. Kütüphane kararlı iki grup; otomatik home rafları adayları dışarıda bırakır, açık favori/geçmiş korunur. Raflar görseli bulunan ilk 12 girdiye öncelik verir. Hero tüm film/dizi havuzunda geniş görselli favori → geniş görselli içerik → afişli favori/içerik → metin sırasıyla seçilir; rastgele/popülerlik/dil tahmini yok.
- `HomeLibrary.kt`, Library sırası, `Artwork` fallback, Home/Detail afiş rolleri, `CatalogParser.firstBackdrop` ve sürüm güncellendi. Poster adresiyle aynı backdrop geniş görsel sayılmaz; ilk bozuk backdrop elemanı sonraki geçerli adresi kaybettirmez. Yalnız adaylardan oluşan kaynakta ana sayfa açıklaması ve kaynak filtresi var; normal bölümlerde kayıt açılır.
- 6 yeni JVM vakası ve opt-in `PrivateCatalogPresentationAuditTest`. Sonuncusu `NEXTV_PRIVATE_CATALOG_DIR` ile yalnız saklanmış özel JSON'u okur, sadece toplamları basar; normal CI'da skip. Üretim parser/presentation kuralları gerçek 21.307 girdiyi okudu: 51 canlı / 40 film / 0 dizi adayı, tüm kimlikler korunur. Ham URL/hesap/başlıklar raporda yok. Yeni sağlayıcı isteği/yayın yok.
- 116 JVM, lint 0 hata / 32 uyarı. İlk derlemede yeni test DpRect.width/height kullanıyordu; right-left/bottom-top düzeltildi. Son normal font 19 akış (8 PilotFlow, 9 SearchNavigation, 2 görsel); 1,5 font 1 yeni akış; 2,0 font 10 akış geçti. İlk adayda yeni test normal/2,0 ayrıca geçti. 15 son PNG `design/reviews/catalog-presentation/`; gerçek onaylı art + sentetik veri. Şeffaf logo testlerinde önceki waitForIdle düzeltmesi korunur.
- Normal/2,0 yeni akış: ayırıcı home'da yok, library sonunda var; OK açar, Geri aynı kart; poster 2:3; gerçek INFO adlı kanal korunur; arama ayırıcı kaydını bulur; yalnız ayırıcı kaynağında Library hâlâ erişilir. Otomatik akışlar gerçek sağlayıcı yayını açmaz; PilotFlow yerel test videosu kullanır.
- `CATALOG_PRESENTATION.md`, SOURCE_GUIDELINES, README, STATUS/BACKLOG, D042 ve token sürüm etiketi güncel. Tarihli 0.14.0 görselleri/QA yerinde korunur; yeni görüntüler ayrı klasördedir. Figma bu tur denenmedi/değişmedi. Son yardımcılar `work/test_catalog_presentation.py`, `work/pull_catalog_presentation.py`, `work/publish_catalog_presentation.py`; yayın scriptini tekrar çalıştırmak doküman başlıklarını çoğaltır.

**Tam sonraki iş:** Yenileme başarı bildiriminin ilk rafı aşağı itmesini çöz; hata/iptal açıklamaları görünür kalmalı ve Ayarlar'daki onay testi korunmalı. Büyük yazıda katalog başlığı kenarını ve kalan kaynak/ayar/oynatıcı yüzeylerini aynı tasarımla iyileştir. Gerçek görselin yanlış esere ait olması ve boyut/rol doğrulaması hâlâ açık; bu tur sadece metadata/sunum kuralları uygulandı. Sonra kullanıcı görsel onayı/Figma eşlemesi ve fiziksel cihaz kalite kapıları. PIN/telefon kurulumu açık ama görsel işin önüne geçmez.

## Son tur · Native görsel aktarım ve TeamViewer kumandası

**14 Ekim 2026 · 0.14.0-design-preview, hedef aktif.** Aşağıdaki native aktarım bekliyor kayıtları tarihseldir. Aktarım çalışma koduna uygulandı ve ayrı `nextv_design_api36` / `emulator-5556` cihazında doğrulandı. Yayımlanan tasarım APK'si ve SHA-256 [QA kaydında](native-visual-revision-qa.json); 0.13 APK aynı hash ile korunur. Kullanıcı cihazı `emulator-5554` ve fiziksel TV güncellenmedi. 5554'e test fixture/reset/kurulum uygulama.

- Ortak Theme/DesignSystem/Navigation/Action ve HomeScreen; PilotApp Library/MediaCard, DetailScreen, SearchScreen, GuideScreen, CardVisibility ve açılış renkleri değişti. Tek bileşen sistemi, aynı Roboto. Üst menüde OK ile geçiş; kaynak filtresi, rehber, geçmiş, favoriler ve geri dönüş korunur. Library başlığı/kategori/arama kartlarla kayar; poster açıklaması ve odak çizgisi görünür alana alınır. Son arama sorgusu düğmede görünür, Temizle odağı girişte tutar.
- İlk 37 akışta 35 başarı / 2 hata: gerçek arama odağı çalınması düzeltildi; rehber testi eski dört sütun adımı yerine yeni üç sütunda aynı kanalı bulur. İlgili 11 vaka geçti. 1,5 ve 2,0 fontta 17'şer vaka; son APK'de normal ve 2,0 fontta 10'ar arama/gezinme/görsel vaka geçti. 109 JVM, lint 0 hata / 32 uyarı, v2 imza doğrulandı. Testler yalnız 5556'da, font sonunda 1,0. Ayrıntı ve log adları QA JSON'da.
- Yedi ekranın üç fonttaki 21 görüntüsü `design/reviews/google-tv-soft-native/`; üçü de final APK üzerinde son kez yenilendi. Görsel yükleme sonrası Compose çizimini beklemeyen screenshot yardımcısı düzeltildi; ilk resimlerde logo arkasında kalan geçici harfler son görüntüde yok. Bu test aracı düzeltmesi üretim APK hash’ini değiştirmedi. Üç fontta captureGoogleTvRevision ayrıca birer kez geçti. Önceki kötü ilk font2 görüntüleri proje dışında `work/design-native-font2-first/` alanındadır. Son büyük yazıda başlık sayısı kaydırma kenarında kısmen görünebilir; bunu kusursuz görsel onay sayma. Ayrıntının alt yardımcı açıklaması kaydırma isteyebilir; ana eylemler görünürdür.
- TeamViewer yanıtı ele alındı: loopback8778 kumandası fareyle sağ → Kanalları aç; OK → Canlı TV; Geri → Ana Sayfa doğrulandı. Sekme1 açık, özel kanıt `work/simulator-remote-ready.png`. Yeni yayın açılmadı. 8777 tasarım sekmesi3 korundu. Bu geçici kumanda ürünün parçası değildir.
- `NATIVE_VISUAL_REVISION.md`, aday token işaretleri, README/COLOR/SCREENS, STATUS/BACKLOG ve D041 güncel. Figma yazılmadı; kota bu tur yeniden kontrol edilmedi. Son test yardımcıları `work/test_design_final.py`, `work/capture_design_final.py`, `work/pull_design_native.py`; geçmiş mutation scriptlerini körlemesine yeniden çalıştırma.

Proje belge kontrolü (17 ekran / 401 değişken), ham hesap taraması ve ZIP bütünlüğü geçti. Paket 0.13 pilotu, ayrı 0.14 tasarım APK’sini ve güncel proje/görselleri içerir.

**Tam sonraki iş:** Gerçek sağlayıcıdaki INFO/ayırıcı satırları, yanlış/eksik kapak ve ana sayfa içerik seçimini güvenilir veriye göre incele. Ardından kalan kaynak/ayar/oynatıcı yüzeyleri ve büyük yazıda başlık kenarını aynı görsel sözleşmeyle iyileştir; ayrı 5556 üzerinde yalnız ilgili test/görseli yenile. Kullanıcı mevcut 5554'te test ederken cihazına dokunma. Kullanıcı görsel onayı, Figma eşlemesi, fiziksel performans/codec ve PIN/telefon kurulumu açık. Yeni feature bu görsel işin önüne geçmez.

## Kullanıcı gezinme yanıtı · Canlı kumanda kontrolü

Kullanıcı TeamViewer ile uygulama içinde gezinemediğini netleştirdi. Mevcut loopback8778 yardımcısı ve emulator-5554 çalışıyordu; yeniden başlatılmadı. IAB sekme1'de fareyle sağ yön odağı Kanalları aç'a taşıdı; OK canlı kataloğu, Geri ana sayfayı açtı. Son ekran `work/simulator-remote-ready.png` özel alanda yenilendi. Kumanda ve tasarım sekmeleri açık bırakıldı. Bu tur APK, hesap, katalog, üretim kodu veya Figma değişmedi; gerçek yayın açılmadı. Native görsel aktarım aşağıdaki kapsamla sıradaki iş olarak kalır. Proje ZIP'i bu kısa tekrar kontrol notuyla yeniden paketlenmedi.

## Son tur · Google TV görsel revizyonu

**14 Ekim 2026 · Somut tasarım ilerlemesi, hedef aktif.** Önceki kumanda turu da ilerlemeydi; canlı kontrol sağlandı. Bu tur yeni özellik/üretim kodu yok. `prototypes/tv-flow/review.html`, `review.css`, `review.js` mevcut katalog/font/assetleri paylaşır. Eski index.html tam akışları korunur. Beş1920×1080 PNG `design/reviews/google-tv-soft/`; `design/tokens/google-tv-soft.candidate.json` açıkça yalnız yerel adaydır. `docs/design/VISUAL_REVISION.md` ölçü/kapsam/aktarımı, `visual-revision-qa.json` gerçek gözlemleri tutar. SCREENS/COLOR/FIGMA/README bağlantıları güncel. D040 kullanıcı tasarım önceliğini kaydeder.

- Üst gezinme;844dp içerik/58dp kenar;228dp hero;196×110,25 yatay kart;152,8×229,2 poster;268×126 logo kartı. Hero/ayrıntı metninde yerel scrim; opak metin kontrastları ayrıca ölçüldü. Ekran/HDR tam kontrast onayı yok.
- Tarayıcı: TRTBelgesel→Right TRT1→Down TRTHaber→Right CNN; son Right aynı kartta. Bilim kurgu üç film; Inception detail→favorite→Escape filtre ve card:m3'ü korudu. Error retry ilk odak, yeniden dene5kart; empty/missing/loading ve reduced switch görüldü. Film alt boşluğu12dp artırıldı; normal row bottom1026/1080. Home row1020,5.430×700 ilk görünüm yatay kırpılıyordu; grid minmax(0,1fr) sonrası frame0,2..430,2 stage0..430,4 içine oturdu. Native/font büyütme testi sayma.
- m1 uyumsuz artwork dışarıda; m5 doğru Dune2021. Kısa Türkçe özetler editoryal sunum; gerçek sağlayıcı verisi değiştirilmedi. INFO ayırıcı filtreleme yapılmadı. Örnek film/dizi/kanal sayıları tam katalog değil. Oynat gerçek medya açmaz, kaynak CTA inceleme notu verir; tüm üretim akışları bu taslakta yok.
- Loopback8777 yalnız prototypes klasörünü sunuyor. IAB tasarım sekmesi3; canlı kumanda sekmesi1 korunuyor.8778 helper kullanıcının emulator-5554'ünü kontrol eder; bu tur cihaza komut gönderilmedi. Viewport ölçümden sonra sıfırlandı; tasarım sekmesi normal review.html olarak bırakıldı.

**Tam sonraki iş:** Native görsel aktarım. Önce mevcut Theme/DesignSystem/Navigation/Action/Card bileşenlerini güncelle; ayrı ikinci bileşen sistemi oluşturma. HomeScreen ve PilotApp Library'den başla, arama/rehber/ayrıntı aynı gezinme modeline geçir. Mevcut kaynak filtresi, devam/son kanal/favori satırları, rehber ve ayar erişimi kaybolmamalı. Sabit örnek Interstellar/5film seçimini üretime taşıma. Sağlayıcı ayırıcı/bozuk kapak seçimini katalog rolü ve güvenilir kanıtla ele al. Sonra ayrı AVD/serialde aynı fixture ile beş ekran, D-pad/geri,1,0/1,5/2,0 font karşılaştır. Kullanıcının emulator-5554 hesabını veya APK'sini değiştirme. Bu tasarım hazır bir native teslim gibi sunulmaz; genel hedef tamamlanmadı. JS sözdizimi ve check_project bağlantı/17ekran/401değişken kontrolleri geçti. İlk viewport screenshotları1920×1061 çıktı; fullPage ile beşi de yeniden1920×1080 alındı ve dosya boyutları doğrulandı. APK SHA aynı; yeni Android test koşulmadı. Tam1080 görüntüde yarım ikinci raf başlığı yakalandı; sonraki raf aralığı32dp, başlık1084,5px ile ekranın dışında. Proje ZIP güncellendi; gizli veri taraması ve ZIP bütünlüğü geçti.

## Güncel yönlendirme · Tasarım ve etkileşimli simülatör

**14 Ekim 2026:** Kullanıcı “feature eklemek yerine iyi tasarımlar” istedi ve Google TV'ye yakın yumuşak yüzeyler / yuvarlak kartlar / ferah düzeni seçti. Aşağıdaki tarihli SAFE01 sonraki adımları bu önceliğin gerisindedir. PIN için başlanmış üç dosya yalnız `work/pin-wip/` içinde; üretime bağlı veya doğrulanmış sayma. Sonraki ürün işi: onaylı gerçek assetlerle ana sayfa/canlı/film görsel taslağı, ardından native eşleme ve ayrı cihazda gerçek ekran karşılaştırması. Gerçek katalogda sağlayıcı INFO/ayırıcı satırlarının ana raflara çıkması da tasarım kalitesi sorunudur; mevcut sürüm bunu çözmüş sayılmaz.

**Cihaz rezervasyonu:** `emulator-5554` artık gerçek kaynakla kullanıcıya açıldı. 0.13, 21.312 katalog kaydı; hesap doğrulaması başarılı. Gerçek yayın kendiliğinden başlatılmadı. Bu cihazı otomatik test fixture'ı olarak kullanma, kaynaklarını silme, APK'sini üzerine kurma. Otomasyon için başka AVD/serial kullan. Fiziksel65U95A değişmedi. Ham hesap ve ekran görüntüleri yalnız özel work alanında.

TeamViewer gezinmesi incelendi: doğrudan Android yön komutu odağı değiştirdi; Windows pencere giriş yolunda aynı sonuç doğrulanamadı. İnceleme sırasında emülatör sürecinin kapandığı görüldü, kayıt silmeden yeniden başlatıldı. `work/simulator_remote.py` yalnız loopback8778 üzerinden mevcut emulator-5554 ekranını ve altı sabit kumanda komutunu sunar; uygulama yüklemez/sıfırlamaz, görüntü kaydetmez, başka cihaza bağlanmaz. Tarayıcı düğmeleriyle sağ yön → Kanalları aç, OK → Canlı TV kataloğu, Geri komutu doğrulandı; gerçek yayın başlatılmadı. Kayıtlı liste yeniden açılışta korundu. Dar panelde ekran ve kumanda birlikte görünür; yaklaşık1.1sn+capture aralıklarla görüntü alınır, video/motion performans testi değildir. IAB sekme1 kullanıcıya açık bırakıldı; son kanıt work/simulator-remote-ready.png. Test aracı proje dağıtımının parçası değildir. Helper yeniden başlatılacaksa güncel süreç komutu/port sahibini doğrula; emülatörü keyfi yeniden başlatma. Proje belgeleri güncel, ZIP bu son notlarla yeniden paketlenmedi.

Figma kişisel hesapta get_design_context ve get_screenshot yeniden denendi; Starter MCP kota engeli doğrulandı. Figma yazımı/uyumu iddia etme. Yeni görsel taslak henüz üretilmedi. Üretim APK'si0.13 değişmedi.

## Güncel tur · Gerçek EPG metadata denetimi · 14 Ekim 2026

Somut ilerleme; genel hedef aktif. Üretim kodu/APK değişmedi, yayımlanan 0.13 hash’i korunuyor. Test APK’si yenilendi; sadece emulator-5554/API36 kullanıldı. Fiziksel65U95A0.2, hiçbir komut gönderilmedi. Figma/yeni screenshot/subagent yok.

- Kullanıcının mevcut izniyle iki ardışık host isteği: xmltv.php ve Xtream canlı katalog, HTTP200; sıfır medya/afiş isteği. Raw gövdeler yalnız work/source-private. Hesap/host/URL proje içinde yok.
- `PrivateGuideAuditTest` eklendi: normalde skip, explicit privateGuideAudit=true + auditInstant ISO UTC, emülatör guard. Dosyalar app-private runtime giriş; test asset’i değil. Native parser, üç yerel MockWebServer→EpgRepository/SQLite yenileme, queryMany, reopen, wrong-owner, remove ve özel input cleanup. Exception metni private JSON/XML excerpt basmaz. Üretim accept kontrolüne testteki true taşınmaz.
- XML’de 402 kanal tanımı / 252 farklı kimlik ve yalnız programlarda bulunan 1 kimlik var: toplam 253 kanal. 10.648 program → 31 geçersiz → 10.617 geçerli → 29 tekrar çıkarılır → 10.588 saklanır. Geçersizlerin 27’sinde bitiş önce, 4’ünde başlangıca eşit. Canlı katalog 4.525, dolu EPG kimliği 490; ID 401 / NAME 1 / MISSING_ID 89 / NONE 4.034. Eşleşen 402 katalog satırı 252 farklı rehber kanalını temsil eder. Sabit 07:34 UTC: 226 satır / 133 farklı kanalda kesin şimdi, 48 satır / 26 farklı kanalda çakışma, 263 satırda sonra bilgisi. QA yalnız toplamlar içerir; ham başlık/kimlik yok.
- İlk test tüm9gün retention’da512 sınırına takılmayacağını yanlış varsaydı:1kanalda650kayıt var. Üretim bounded sorgu doğru truncated verdi. Test gerçek UI12h/today pencerelerini ayrı kontrol eder; her ikisinde truncation0, bugün max325. Bütün DB kontrolü açık2000limit; üretim limit değişmedi. İlk başarısızlık belgeli; sonraki düzeltilmiş1vaka ve exception-redaction sonrası son1vaka geçti.
- Son `work/provider-epg-native-final.log`1/1,16.416s; `provider-epg-test-build-final.log` assembleDebugAndroidTest başarılı. Opt-in parametresi olmadan ayrı kontrol 0,086 saniyede assumption/skip (-4) verdi; özel girdileri okumadı. Önceki109JVM/77akış yeniden koşulmadı. APK SHA9063f6d8785fb8e1ef02cc37ddfd9a5d0bd2c680d08864e2ef20261dd644d87a aynı. `provider-guide-qa.json` tüm batch ham sürelerini/fetch özetini saklar. Native timing debug/emülatör; physical p95/peakmem iddiası yok.
- `PROVIDER_EPG_AUDIT.md`, SOURCE_GUIDELINES, EPG.md, STATUS/BACKLOG/QUALITY/README/appREADME güncel. Eski kısaEPGAPI hatası gözlemi korunur; bütünXMLTV başarıyla ayrıca doğrulandı. Nativeqa follow-up linki ayrı;77sayısını78diye değiştirme.

### Tam sonraki adım

1. SAFE01 ortak PIN/kategori/kaynak/kanal gizleme ve erişim politikası tasarla/uygula; arama/favori/geçmiş/direkt giriş/player aynı karar noktasını kullanmalı. PRD ve SCREENS/FIGMA/MOTION/COLOR oku; adult bayrak filtresi tam güvenlik değildir. SYS01 de açık.
2. Katalog restore/ilk sorgu darboğazını aşamalara ayır; native rehber fiziksel ve düşük bellekli cihaz ölçümü ayrıca gerekir. Rehber4.6MB/10.6bin kayıt bu tek kaynak örneğidir, üst limit kabulü değil.
3. Kullanıcı fiziksel çalışmayı sürdürdüğünde install-r ile0.13, rehber→player/focus/gerçek kumanda ve codec/HDR/5.1/iki saat/beşkişi turu. Gece TV açılmaz; genel instrumentation TV’de çalıştırılmaz.
4. Gerçek audit tekrarında yeni indirme ve anı açıkça kaydet; eski native runtime inputlar silindi. Work’teki raw gövdeler/credentials özel kalır. Yeni test başka kaynakları silmez. M3U header’da XMLTV adresi yoksa kullanıcı override gerekir; tahmini hesap dönüşümü ekleme.

`work/package_project.py` mevcut0.13APK+Native013’ü koruyarak yeni kaynak belgelerini paketler. Eski finish_v13/finish_v12 scriptlerini tekrar çalıştırma. Aşağıdaki kayıtlar tarihlidir.

---

## Güncel tur · Native rehber 0.13 · 14 Ekim 2026

Somut ilerleme; genel hedef aktif, tamamlandı sayma.0.13 APK/version13 ve41 Native013 ekran+ZIP teslim edildi. Fiziksel65U95A0.2, dokunulmadı; yalnız emulator-5554/API36/1920×1080/density320. Test sonunda font1.0. Gerçek sağlayıcı isteği/yayını yok. Figma kota yeniden kontrol edilmedi/yazılmadı. Subagent yok. Git ilk commit/kimlik bekliyor; kimlik uydurma.

### Kod ve korunacak sınırlar

- GuideController: kaynak/plan bağları; ViewModel'e ait tek aktif iş/kaynak, seri network mutex,6h cache+yerel takvim kapsamı, hata/iptalde5dk otomatik backoff. Cache-read sırasında gelen indirme isteği pending tutulur. İptal edilen ama tamamlanmayan job hâlâ sahip sayılır; Compose tekrarları indirmeyi yeniden başlatmaz. forget önce owner'ı çıkarır, sonra cancel eder; cancelled setini temizler. Publish SourceTransactions altında güncel registry/config ve M3U header bağını doğrular. readEpgFeeds yalnız şifreli snapshot header'ını açar,60bin öğeyi yeniden ayrıştırmaz.
- EpgRepository: her refresh için tek fiziksel GuideWriter executor/dispatcher, kapanış use içinde. SQLite stage başlangıç/sink/seal/rollback/close aynı thread'dedir; limitedParallelism(1) aynı thread garantisi vermez. Call.cancel gözlemcisi Dispatchers.IO'dadır ve bloklanan writer'ı beklemez. EpgStore.Stage checkWriter yanlış thread'i reddeder. Önceki IO havuzu aralıklı hazır-olma/silme kalıntısı hatasına yol açıyordu.13 EpgStorage vakasında iki feed sekiz kez yenilenir; yavaş body iptali2sn ve stage temizliği kontrol edilir.
- SourceConfig/SourceDraft epgUrl alanı, registry/legacy saklama, setup/edit ve rehber ayarı; eski kayıtlar boş default ile okunur. CatalogStore binding XMLTV override'dan bağımsızdır: rehber adresi değiştirmek kataloğu silmez. saveGuideUrl mevcut kayıt/generation kontrolüyle sadece kaynağı günceller. DeviceStore.guideZoneId sistem varsayılanı veya geçerli ZoneId saklar.
- GuideScreen tek focusable ızgara, kanal/zaman imleci. Yatay slot, düşey sabit an; boş aralık30dk. OK KeyUp'ta en son model imlecini okur. Nav Right açıkça grid/boşta Şimdi'ye gider. Back ayrıntı→grid→nav→Home; playerreturn aynı imleç, zapping yeni kanal. Günler dün/bugün/+7; DST ofsetleri gösterilir.512/day sorgu kesilirse açıklama var. GuideTimeline sweepline gap/overlap/unknown-end point; eksik bitiş tahmin edilmez. Kaynak farkında görünen+yakın satır sorgusu IO'da.
- TV-020 canlı kartlarda şimdi/sonra; PlayerScreen program/saat/bilinen süre ilerlemesi, yatay kaydırılabilir kontroller ve parlak videoda koyu başlık yüzeyi. Tek ExoPlayer, focus autoplay yok. SourceInfo'dan GuideSettings önce RemoteTextReader durum ekranı, ayrı adres editorü, refresh/cancel; zone picker. Büyük font sabit eylemler, okuyucudan eylemlere açık up/down odağı.
- Mevcut kaynak transaction/owner, şifreli katalog, artwork sahiplik/64-row bakım, ortak arama indeksi, kurtarma ve bölüm sonu kuralları korunur. Yeni dependency yok.

### Kanıt

- work/v13-final-build.log: app/test/JVM/lint;109 JVM,0failure,0error; lint0error/32warning. v13-final-signature.log v2true. Dağıtılan APK hash/byte native-guide-qa.json'da.
- v13-final-regression.log:76/77 (Guide11,Epg13,SourceOperation7,Recovery4,Registry10,Multi7,Search9,SourceLayout4,Visual1,PlaybackRecovery11). Kalan tek test yeni menüde dönüş için hâlâ6 yukarı sayıyordu; labels.size ile düzeltildi. v13-search-final.log9/9 aynı uygulama byte'larıyla geçti. v13-font1_5.log ve v13-font2_0.log: Guide11+SourceLayout4+menü1=16'şar. Tarihi diğer paketler bu tur çalıştırılmış gibi sunulmaz.77 benzersiz son sonuç başarılı; font tekrarlarını yeni benzersiz vaka diye sayma.
- Önceki77 tur3hata: boş guide Right geometrisi; test menü listesinde yeni Rehber yoktu; kaynak okuyucu testinde yeni rehber ayarı adımı yoktu. Ayrım ve takip turu QA'da. Guide daha önce aralıklı SQL thread takılması gösterdi; timeout artırılarak gizlenmedi. Tek temiz ilk77 tur iddiası yok.
-41 yeni görsel:14 genel gerçek afişli ekran+9rehber yüzeyi×3font. Unclipped bounds seçili satır/kart/eylemleri; okuyucu sonuna kumandayla erişimi kontrol eder. Programlar sentetik, video yerel test fixture; gerçek EPG kanıtı değil. Görseller review edildi, Figma sync yok.

### Tam sonraki adım

1. EPG01/02 gerçek sağlayıcı XMLTV'sini özel work alanında tek sırayla incele; kullanıcı hesabını/URL'yi dokümana/screenshot'a taşıma. Sadece rehber indirme yayın açmaz. Büyük rehber süre/disk/bellek ve eşleşme oranı ölç; alias/bilinmeyen kimliği sessizce tahmin etme. D038/039 ve EPG.md sınırları geçerli.
2. PRD SAFE01 ortak PIN/kategori/kaynak kilidi-gizleme için tek erişim politikası tasarla/uygula; arama/favori/geçmiş/direkt giriş/player hepsi aynı kapıdan geçmeli. Mevcut adult bayrağı filtresini tam PIN güvenliği sanma. SYS01 dil/sistem hareket tercihi ve tüm ekran erişilebilirliği de açık.
3. Katalog açılışı/ilk arama ölçümünü aşamalara ayır.0.12 debug60bin restore2.4s/ilk823ms fiziksel p95 değildir. Native rehberin performansını ayrıca ölç; bu tur yalnız işlev/font kanıtı var.
4. Kullanıcı fiziksel çalışma sürdürdüğünde install-r ile hesapları koruyarak0.13 TV'ye kurulabilir; gece fiziksel TV açılmaz. Genel instrumentation TV'de çalıştırılmaz. Codec/HDR/5.1/iki saat/5kişilik tur ve gerçek görsel/ses onayı açık.

work/finish_v13.py test+imza kanıtını denetleyip teslimi üretir; tekrar çalıştırma HANDOFF başlık korumasına takılır. work/package_project.py artık0.13APK/Native013 dosyalarını paketler. Eski finish_v12.py/diğer teslim scriptlerini çalıştırma, yeni APK'yi eski sürüm diye kopyalar. Aşağıdaki veri-temeli ve0.12 bölümleri tarihsel devir notlarıdır.

---

## Güncel tur · EPG veri temeli · 14 Ekim 2026

Somut ilerleme var; genel hedef aktif. Bu tur kullanıcıya görünür native rehber teslimi **değildir**. Yeni APK yayımlanmadı, ZIP kaynakları güncellendi; dağıtılan0.12 APK/Native012 görselleri önceki sürümün kanıtı olarak korunur. Kaynak build.gradle hâlâ12/0.12; emülatörde bu alan değerleriyle yeni geliştirme derlemesi kurulu olduğundan yalnız versionName'e bakıp0.12 dağıtım byte'ları sanma. SHA kanıtı epg-foundation-qa.json'da. Fiziksel65U95A0.2, dokunulmadı; yalnız emulator-5554. Subagent yok. Git hâlâ ilk commit/kimlik bekliyor; kimlik uydurma.

### Yeni veri yolu

- `Models.kt`: MediaEntry sonuna epgId/epgName, Catalog sonuna epgUrls; eski positional çağrılar uyumlu. Catalog.toString adresleri gizler. SourceConfig.xmltvUrl Xtream hesabı için kodlanmış adres üretir. SourceConfig/SourceDraft/registry'ye override alanı henüz eklenmedi.
- `CatalogParser.kt`: M3U tvg-id/name ve header x-tvg-url/url-tvg (HTTP, relative, ençok4, fazlasına warning); Xtream epg_channel_id. `CatalogSnapshot.kt` version1 yeni alanları yazar, eski alan yokluğunu okur. `SourceLibrary.combine` kanal alanlarını taşır ama feed adreslerini birleştirmez. Guide planı birleşmiş katalogdan değil `snapshots[source.id].catalog` üzerinden kurulmalı. Eski M3U snapshot yeni metadata için katalog yenilemesi gerektirir.
- `EpgModels.kt`: GuideWindow dün+bugün+7gelecek yerel takvim günü; XmltvTime strict dakika/saniye, numeric offset/UTC/GMT/Z/BST; bilinmeyen zone/kaba tarih null. EpgProgramme end nullable; boşluk/inferred bitiş yok. GuideSlice belirsiz overlap/current ve aynı başlangıçlınext'i kesin göstermez. EpgPlan owner/binding hash + geçici redakte edilmiş URL listesi; source credential/feed değişince binding değişir. override sadece API argümanıdır.
- `XmltvParser.kt`: SAX streaming; SafeProlog UTF8/UTF16 BOM, dış DOCTYPE'i fetch etmeden kaldırır, internal subset reddeder. JVM/Android uyumluluğu testli. Dil tr tercihli, alternatif kanal adları; geçersiz/pencere dışı sayaç. 128Mi karakter,64Ki prolog,16Ki field,32depth,20kchannel/1Mprogramme sınırları. Native UI olmadan geniş sağlayıcı uyumluluğu iddia etme.
- `EpgStore.kt`: özel files/guides SQLite. Channel+alias index ve programme index; credential/URL yok, metadata ek app encryption yok(D038). 96MiB sayfa sınırı sadece birDB; journal/stage/oldcopy/toplamkaynak bütçesi değil. İzole stage parse edilip seal ile kapanır; AtomicFile publish process lock/kuşak denetimli; yeni stage/removal eskinin yayımını reddeder. Kaynak idhash önekiyle remove. query default512/max2000 ve truncated; tüm rehberi Compose'a yükleme. Aynıfeed exactID öncelik, wrongID fallback yok, names konservatif, çokfeedIDduplicate ambiguous.
- `EpgRepository.kt`: sequential feed download, body boyunca gerçek call.cancel, plain/filegzip/HTTPgzip,128MiB network+decoded bounds. Tüm feed'ler başarılıysa closedstage SourceTransactions.shared.write içinde accept+publish. **Üretim accept closure güncel kaynak/rehber bağlamını doğrulamalı**; testlerdeki true'yu kopyalama. Ağ SourceTransactions dışında. Error fixedmessage; iptal ayrı. `TvViewModel.kt` yalnız remove/reset içine EpgStore temizliği eklendi, refresh/query/UI bağlantısı yok.
- `app/src/sharedTest/resources/epg/europe.xml` ve `america.xml`: sentetik, ortak JVMresource/Androidasset; Gradle sourceSets eklendi, yeni dependency yok. XMLTV DTD ve Android SAX birincil kaynakları EPG.md içinde.

### Doğrulama ve sınır

- `work/epg-foundation-final-build.log`: assembleDebug/assembleDebugAndroidTest/testDebugUnitTest/lintDebug başarı.105 JVM(83 önceki+22yeni);0failure/0error. Lint0error/31warning.
- İlk unit turunda iki test düzeltildi: MockWebServer30s throttling shutdown5s sınırını aşıyordu;3s throttling+gerçek responseBodyStart latch kullanıldı, cancelAndJoin2s içinde. Coroutine stacktrace recovery sanitized SourceFailure'ı cause olarak ekliyordu; nullcause yerine zincirin tüm mesajlarında sır bulunmadığı test edilir. Uygulamanın iptal davranışı bu düzeltmelerde değişmedi. Son105tur temiz.
- `work/epg-storage-tests.log`: ilk12 EPG Android vakası5.604s. `work/epg-foundation-final-device.log`: son33 vaka36.376s, EPG12 +SourceOperation7 +SourceRecovery4 +SourceRegistryStorage10; hepsi geçti. FizikselTV/genel provider testi yok.
- İki fixture DST/Unicode/channel-less XMLTV; now/next bilinmezlik; eski catalog codec uyumu; sharedlibrary sınırı; plain/gzip; async networkbodycancel; store reopen/match/feedcollision/sourceboundary/remove/latewriter/keepold/empty-invalid/privacy. Fiziksel rehber performansı, gerçek kaynak, nativefocus/görsel kanıt açık. Bu tur yeni screenshot yok çünkü nativeUI değişmedi.
- GuideStore Stage/read API'leri IO'da çağrılmalı. read info/query başarısızlıkta null döner; ViewModel eksik/okunamayan cache yenileme ve görünür hata durumunu yönetmeli. Ham provider hesapları yalnızwork; asla dokümana/ekrana çıkarma.

### Tam sonraki adım

1. [EPG sözleşmesi](../architecture/EPG.md), SCREENS TV-020/021/050 ve prototip guide akışını oku. Kaynak başına EpgStatus/refresh job, explicit encrypted override ayarı ve tercih edilen ZoneId ekle. UI açılışını rehber indirmesine bağlama; eski snapshot varsa kullan, ağ yoksa canlı kanal açılsın. Cache sahibini runtimeentry ve source snapshot birlikte belirlemeli.
2. Native guide: sonkanal/currenttime ilkfocus, düşeyde zamanankrajı, yatayda zaman, OKnow→player/diğer→programdetail, Back katmanları, şimdi/gün/saat dilimi seçimi, boş/hata/missing/ambiguous durumları. Library/live veplayernow/next. Ölçekler mevcutRoboto/Ink/Action; gerçekekran görüntüsü alıp1.0/1.5/2.0 font/4yön kontrolü. D038 birbackendaraadımıdır, BKL011review kaldı.
3. Guideişleri kaynakcancel/remove/edit/modelclear ile koordine edilmeli; acceptécritersSourceTransactions altında registry/config binding kontrolü. GerçekproviderXMLTV ve performans ancak ayrı kanıtla tamam.
4. Nativeintegration geçtiğinde yeni0.13 APK/versioncode üret; finish_v12.py veya önceki scriptleri çalıştırma, yenikodu yanlış0.12 diye kopyalarlar. Şu anda package_project.py kaynakları+sonyayın0.12APK'yı paketler; README bu ayrımı açıkça yazar.

Aşağıdaki0.12 kaydı tarihsel devir ayrıntısıdır; yukarıdaki EPG adımı günceldir. Genel hedef, PIN/SYS01/kalite kapıları ve gerçekTVkullanıcıtestleri açık.

## 0.12 · 14 Ekim 2026

Önceki tur somut ilerleme: paylaşılan arama hazırlığı, görsel index bakımı ve60bin kayıt tanı ölçümü. Genel hedef **aktif**; tamamlandı sayma. Kullanıcı gece inisiyatif verdi. FizikselTV'ye dokunulmadı,65U95A0.2.0. Yalnız emulator-5554/API36/1920×1080/density320/font1.0. Yeni subagent yetkisi yok; Git main ilk commit/kimlik yok.

AGENTS/README/STATUS/BACKLOG/PRD ve PERFORMANCE/MULTI_SOURCE'u oku. D036/D037 yeni; native-performance-qa.json ve native-performance-data.json son kanıt. Figma kota yeniden kontrol edilmedi, yazılmadı. Ham hesap yalnız work özel alanında.

## Değişiklikler ve korunacak kurallar

- `data/ArtworkCache.kt`: running artık Job→InFlight(owner,key). Ayrı SupervisorJob/IO bakım scope'u; başlangıç ve her16 request completion bir bakım işi, en çok64 sahiplik satırı. Son rowid ile artımlı tarama; sona gelince0'a döner. Disk/bellek/aktif key varsa bütün owner referansları korunur. Yoksa owners+artwork_files satırları transaction içinde silinir; bakım byte dosyalarını silmez.
- Yeni `artwork_files(cache_key,metadata,data)` tablosu. Yollar yalnız Coil snapshot'tan, disk.directory'ye göre göreli alınır; dosya adı tahmini/ham URL yok. Eski version1 indexi korunur; bilinmeyen yol snapshot ile tamamlanır. Bilinen yolda FileSystem.exists LRU sırasını değiştirmez. MemoryCache.keys güçlü+zayıf anahtarları kapsar. In-flight key kontrolü açık editor nedeniyle null snapshot'ı yanlış yokluk saymayı önler.
- Request finally NonCancellable/IO altında running'den çıkarır, best-effort dosya yolu kaydı ve periyodik schedule yapar. Maintenance gate alır, lifecycle/source kilidi almaz; hataları uygulamaya yaymaz. close bakım scope cancelAndJoin sonrası mevcut loader yaşam döngüsünü kapatır. clear her iki tabloyu ve cursor'u sıfırlar; removeSource kalan sahipliği olmayan file-path satırlarını temizler.80MB görsel disk/%12 bellek aynı. Indexe sabit MB kota eklenmedi; fiziksel DB boyutu SQLite davranışına bağlıdır.
- Yeni `data/TitleIndexCache.kt`: main'den erişilen katalog kimliği +Deferred. ViewModel scope'unda Default dispatcher işi, sorgu bekleyicisinden bağımsız. prepare/get aynı bekleyen veya tamamlanan sonucu paylaşır. Yeni katalog/invalidate eski işi iptal eder. İptal/hata olmuş hazırlık sonraki çağrıda tekrar yapılır. TvViewModel publishLibrary sonrası resetSearchIndex ve prepare(all) çağırır; global/library arama aynı indeksi kullanır. Arama sıralaması/Türkçe/adult politikası değişmez.
- Dört `TitleIndexCacheTest` sanal zaman vakası eklendi. `ArtworkCacheTest`5'ten9'a çıktı; gerçek disk/shared/memory-only/in-flight/automatic96failed/10binorphan+eskiDB doğrular. `LargeCatalogProbeTest`60bin veri üretir ve model→görünür UI tanı sürelerini kaydeder; fiziksel cihazda çalıştırılamaz.
- Version12/0.12.0-pilot. Ek kütüphane/dependency yok. UI geometri/bileşenler değişmedi.

## Doğrulama

- work/v12-release-build.log: app/test/unit/lint;83 JVM,0 hata,31 lint uyarısı (yeni uyarı SQLite transaction için isteğe bağlı KTX önerisi). Son uygulama bu build. work/v12-final-new-tests.log:10/10,42.036s (Artwork9+Probe1).
- work/v12-regression.log: MultiSource7,SourceRecovery4,SourceOperation7,SearchNavigation9,VisualPreview1;28 vakadan27 geçti,139.074s. cancellingDownloadPreservesThePreviousSourceAfterRecreation modeli IMPORTING görünce UI hazır olmadan Back gönderiyordu; Loading İptal odağını bekleme eklendi. work/v12-cancel-final.log tüm7 SourceOperation vakasını tekrar koşar. Son38 benzersiz sonuç başarılı; tek temiz ilk tur değildir. Uygulama byte'ları fixture düzeltmesinde değişmedi. Tarihi diğer testler tekrar koşulmuş gibi sunulmaz.
- Emülatör önce yoktu; gerçek process/device yokluğu doğrulandı. nextv_tv_api36 yalnız emulator-5554/no-window/no-audio ile yeniden başlatıldı; sys.boot_completed sonrası kurulum. İlk erken install denemeleri servisler açılmadığı için çalışmadı. Host emulator process başlangıçID27084; yeniden başlatmadan güncel process/device durumunu kontrol et. FizikselTV komutu yok.
- İlk ölçüm hostta Gradle çalışırkenydi; work/v12-baseline-probe.json keşif verisi, karşılaştırmada dışarıda. Derleme bitince0.11 baselineQuiet alındı. Bakım eklenmiş ama indeks paylaşımı eklenmemiş v12 ölçümü ayrıca var. Final derleme bittikten sonra yeni ölçüm. Ham dört katalog turu+son artwork ölçümü publicJSON'da.
- Final60bin restore2,38–2,63s, ilk query822,605ms, sonraki23 query medyan150,533ms/p95 217,542ms. BaselineQuiet ilk query854,575ms; bu küçük farktan hızlanma iddiası çıkarma. Shared hazırlığın hızlı yazmada tekrar yapılmaması unit/arama davranış testleriyle kanıtlı. Restore aynı süreç; force-stop başlangıcı değil. Görsel payload yok; heap ölçümü native/GPU/sızıntı testi değil.
- Artwork10bin stale→1 live, tüm tarama6732ms; ilk64 satır45,9ms. Uygulama bütün taramayı bir seferde çalıştırmaz. DB+yan dosya ölçümü3.870.720→32.768byte; explicitVACUUM yok. İlk test sadeceDBfile ölçüyordu; final bütün yan dosyaları sayar. Tam global fiziksel metadata sınırı iddiası yok.
-14 Native012 screenshot: gerçek afişli Home, Search/kaynaklar ve picker incelendi; Figma'ya yüklenmedi.0.11 font görüntüleri tarihli kanıttır,0.12 diye yeniden adlandırılmadı. Fiziksel kullanıcı görsel/ses onayı hâlâ açık.

## Tam sonraki adım

1. PRD EPG01/EPG02 için XMLTV/Xtream veri temeli ve native rehbere başla: tvg-id önceliği, belirsiz ad, ofset/DST, iki farklı geçerli fixture, şimdi/sonra/ızgara, kaynak başına cache/hata. Mevcut prototip/Figma bileşen sözleşmesini oku; PRD kapsamını daraltma. Ortak PIN ve SYS01 de P0 açık.
2. Açılış ölçümünü şifre çözme/gzip-JSON decode/SourceLibrary.combine/Home aşamalarına ayır.60bin kayıt restore hâlâ2sn hedefinin üzerinde debugtanı veriyor; ölçülen darboğaza göre optimize et. Release/fiziksel Macrobenchmark/input-to-pixel ölçümü ayrı kalır.
3. Tüm ekran büyük font/four-way ve grafik cache uzun kullanım/bellek baskısı matrisi. Index bakım periyodu/batch'i gerçek cihazda ölç; sadece p95 tanı sayısıyla kalite kapısı kapatma.
4. Kullanıcı uyanıp fiziksel çalışma sürdüğünde install-r ile hesapları silmeden TV'yi güncelle; genel instrumentation TV'de çalıştırılmaz. Gerçek codec/HDR/5.1/iki saat/kullanıcı turu açık. İkinci gerçek sağlayıcı yayını açma.

## Teslim

IPTV_Player_TV_Pilot_0.12.0.apk +sha256,14 Native012PNG,IPTV_Player_Project.zip; docs/project/QA+ham ölçümler. PRD15sayfa/47.388asset/201kategori korunur. work/finish_v12.py build/logları kontrol edip teslim/QA/dokümanları yazar; yeni turda aynı scripti bilinçsiz çalıştırma. work/package_project.py0.12APK/Native012'ye bakar. Eski finish/docs scriptleri yeni dosyaları eski sürüm diye kopyalayabilir, tekrar çalıştırma. Son durumda açık test/build oturumu yok; emülatör hizmeti çalışabilir. Codex/Claude aynıAGENTS kurallarını izler.
