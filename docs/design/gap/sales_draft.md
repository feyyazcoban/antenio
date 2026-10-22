# Satış stratejisi ve fiyat, çok dilli arayüz, farklı ülkelerden satın alma (Google Play Billing ile "NEXTV Plus"). Not: iş akışının hesapladığı görev "oynatıcı içi kanal listesi" idi. Kullanıcının aktarılan isteği bu konu olduğundan ve kurallara göre kullanıcı isteği öncelikli olduğundan bu tasarım onu yanıtlıyor. Kanal listesi tasarımı ayrıca çalıştırılmalı.

18 Ekim 2026 · TiviMate gap tasarımı (`overlay`), salt okunur tasarım ajanı; öneriler kullanıcı kararı değildir.

**22 Ekim 2026 · D113:** Tek seferlik "NEXTV Plus" modeli tarihçedir. Gelir modeli 7 gün ücretsiz deneme ve Google Play aboneliği oldu. Güncel plan [SUBSCRIPTION](../../product/SUBSCRIPTION.md) içindedir.

**22 Ekim 2026 · D111:** Bu taslaktaki "satın alma bilgisi cihazdan çıkmaz, sunucu yok" varsayımı geçersiz. Platformlar arası Premium için hesap ve sunucu tarafında hak kaydı gerekir. Ayrıca dil bölümündeki "yalnız `styles.xml` var" bilgisi eskidi; uygulama İngilizce varsayılan ve Türkçe kaynaklarla geliyor. Güncel öneri [SALES_CHANNELS](../../product/SALES_CHANNELS.md).

**Özet:** Bugün uygulamada hiçbir gelir altyapısı yok. Billing bağımlılığı yok, release paketi debug anahtarıyla imzalanıyor, gelir modeli ve hedef diller DECISIONS'ta "Açık kararlar" altında ve PRD §12'de açık duruyor. Dil tarafında `res/values` altında yalnız `styles.xml` var: yaklaşık 500 kullanıcıya görünen metin Kotlin'e Türkçe olarak gömülü. Tarih, sayı, sıralama ve parça dili adları da `Locale.forLanguageTag("tr")` ile sabit. Bu yüzden İngilizce ya da Almanca bir TV'de uygulama baştan sona Türkçe görünüyor. Önerim üç parçalı. (1) Çekirdek ücretsiz kalsın. Güç kullanıcısı özellikleri tek seferlik "NEXTV Plus" ile açılsın (öneri: 14,99 € / 14,99 $ / 299,99 TL). Reklam olmasın. Abonelik yalnız sunucu maliyeti doğuran özellikler (senkronizasyon) gelirse eklensin. (2) Metinler kaynak dosyalarına taşınsın. İlk dalgada Türkçe ve İngilizce olsun. Tanımsız dillerde İngilizce açılsın. Uygulama içinde dil seçimi, yerel tarih/sayı biçimi olsun ve RTL tasarlanana kadar düzen soldan sağa kilitlensin. (3) Uluslararası satın alma yalnız Google Play Billing ile yapılsın. Para birimi, vergi (AB, Birleşik Krallık ve Türkiye'de Google vergiden sorumlu satıcıdır) ve ödeme yöntemlerini Play üstlenir. Uygulama hak bilgisini cihazda şifreli tutar ve sunucu ya da analitik eklemez. Fiyat ve sınırlar öneridir, kullanıcı kararı değildir.

**Tahmini iş:** 14.5 gün

**Aşamalar:**
1. 1. Metinleri kaynağa taşıma, görünür değişiklik yok: values/strings.xml şimdilik Türkçe; UiText ve FailureReason; "Tümü" yerine @all; activeSection ve widestLabel düzeltmesi; yerel biçimlendirme yardımcısı; soldan sağa kilit; sözde diller; eşlik ve sabit metin yasağı testleri (L, 5 gün)
2. 2. İngilizce ve dil seçimi: values/ İngilizce, values-tr/ Türkçe; LocaleController ve Ayarlar → Dil sayfası; çok dilli yetişkin kategori algısı; Play mağaza kaydı EN (M, 3 gün + çeviri incelemesi)
3. 3. Billing temeli, satış kapalı: StoreBilling sarmalayıcısı, PlusController, DeviceStore plus_v1, Plus sayfası ve geri yükleme; yalnız lisans test kullanıcılarıyla (M, 4 gün)
4. 4. Plus satış açılışı: PlusFeature kapıları ilk üç Plus özelliğine bağlanır, kilitli öğe penceresi, Console ülke fiyatları ve açılış indirimi, gizlilik politikası ödeme paragrafı (S, 1,5 gün; özelliklerin kendisi hariç)
5. 5. İsteğe bağlı: Almanca (values-de) ve Fire TV / Amazon IAP araştırması (S, 1 gün + çevirmen)

**Kullanıcı kararları (varsayılanla):**
- Gelir modeli ne olsun? → varsayılan: Çekirdek ücretsiz ve bugünkü özellikler hiç ücretliye geçmez. Güç kullanıcısı özellikleri tek seferlik, ömür boyu 'NEXTV Plus' ile açılır. Reklam yok. Abonelik yalnız sunucu gerektiren bir özellik (senkronizasyon, QR eşleştirme) gelirse eklenir. Ödeme yalnız Google Play üzerinden; etkinleştirme kodu ya da bayi yok.
- Plus fiyatı ne olsun? → varsayılan: 14,99 € / 14,99 $ / 12,99 £ / 299,99 TL, vergi dahil. İlk 30 gün açılış fiyatı 9,99 € / 9,99 $ / 8,99 £ / 199,99 TL. Diğer ülkelerde Play'in otomatik çevirisi. TL fiyatı 6 ayda bir gözden geçirilir.
- Ücretsiz ile Plus arasındaki sınır nerede olsun, satış ne zaman açılsın? → varsayılan: PIN ve ebeveyn denetimi, yetişkin filtresi, erişilebilirlik, oynatıcı içi kanal listesi, Watch Next ve birden çok kaynak ücretsiz. Plus'ta catch-up, EPG araçları, yedekleme, favori grupları ve sıralama, hatırlatıcı, timeshift ve çoklu ekran. Satış, en az üç Plus özelliği yayımlanınca açılır; önceden satış yapılmaz.
- Hangi diller desteklensin, tanımsız dilde ne gösterilsin? → varsayılan: İlk dalga Türkçe ve İngilizce. Tanımsız TV dillerinde İngilizce. Ayarlar'da Dil seçimi olur. İkinci dalga Almanca. Arapça gibi sağdan sola diller şimdilik yok ve düzen soldan sağa kilitli. Play'in makine çevirisi kullanılmaz.
- Hangi ülkelerde satılsın, Play dışında satış olsun mu? → varsayılan: Google Play'in ücretli içerik desteklediği tüm ülkeler, avukatın sakıncalı bulduğu ülkeler hariç. Yan yükleme, Play'siz kutu ve web satışı ilk aşamada yok. Fire TV (Amazon IAP) ikinci faz değerlendirmesi.

**Riskler:**
- Ad ve marka: NEXTV ile aynı kategoride ve mağazada 'NexTv' var (Play raporu §3.1). 'NEXTV Plus' adı ve yerelleştirilmiş mağaza kayıtları ad kararından sonra kesinleşmeli; ürün kimliği bu yüzden nötr 'plus_lifetime'.
- Uygulamada analitik yok: satış hunisi uygulamada ölçülemez; yalnız Play Console'un toplu verisi kullanılır.
- IPTV kullanıcılarının önemli bir kısmı Play'siz kutu ya da yan yüklenmiş APK kullanıyor: bu kitleden gelir alınamaz (bilinçli tercih).
- Düşük TL fiyatı bölgeler arası fırsatçılığı çeker (TiviMate'in Türk Play üzerinden 599 TL'ye satıldığı indirim örneği); Play'in ülke bağlaması riski sınırlar.
- Bir IPTV oynatıcısında uygulama içi satış inceleme dikkatini artırır: Plus metinleri hiçbir dilde kanal ya da içerik ima etmemeli ('Plus yalnız uygulama özelliklerini açar').
- Metinleri kaynağa taşımak yaklaşık 30 dosyaya ve Türkçe metin bekleyen JVM ve enstrümantasyon testlerine dokunur: 1. aşama görünür değişiklik olmadan tek seferde yapılmalı, bu sırada metin değişiklikleri dondurulmalı.
- RTL: supportsRtl=true ile Arapça bir TV aynalanmış düzen ve ters D-pad anlamı görebilir; soldan sağa kilit ar-XB ile doğrulanmalı.
- Durumda çözülmüş metin kalırsa dil değişince ViewModel eski dilde metin gösterir; UiText geçişi eksiksiz olmalı.
- Hukuk ve vergi: mesafeli sözleşme ve cayma istisnası, GVK 20/B'nin TV uygulamasını kapsayıp kapsamadığı ve AB'de tacir adresi avukat ve mali müşavire sorulmalı; Data safety'de satın alma bilgisinin nasıl beyan edileceği doğrulanamadı.
- Play Billing Library'nin büyük sürümleri yaklaşık iki yılda bir zorunlu güncelleme ister (eski sürümlerin kabul sonu takvimi); satın almalar 3 gün içinde onaylanmazsa Play otomatik iade eder.
- Rakip fiyatları (TiviMate yaklaşık 9,99 €/yıl, 33,99 € ömür boyu) yalnız üçüncü taraf ve indirim sitelerinden; resmî olarak doğrulanamadı.
- Play ücret yapısı değişiyor: ABD, Birleşik Krallık ve AEA'da abonelik için %10 + %5 faturalama ücreti, diğer ülkelere kademeli yayılım; tek seferlik üründe %15 katmanına kayıt gerekiyor.

**Dokunulacak dosyalar:**
- `apps/android-tv/app/build.gradle.kts`: billing-ktx bağımlılığı; androidResources generateLocaleConfig ve localeFilters (en, tr); debug yapısında isPseudoLocalesEnabled; BuildConfig.PLAY_PUBLIC_KEY alanı
- `apps/android-tv/app/src/main/res/resources.properties`: Yeni: unqualifiedResLocale=en-US
- `apps/android-tv/app/src/main/res/values/strings.xml`: Yeni: tüm arayüz metinleri ve plurals; 1. aşamada Türkçe, 2. aşamada İngilizce varsayılan; ui_locale (translatable=false)
- `apps/android-tv/app/src/main/res/values-tr/strings.xml`: Yeni (2. aşama): bugünkü Türkçe metinler birebir
- `apps/android-tv/app/src/main/AndroidManifest.xml`: localeConfig (otomatik üretilir); supportsRtl kalır, düzen Compose'da soldan sağa kilitlenir
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/MainActivity.kt`: API 26–32 için attachBaseContext ile dil sarmalama; dil değişiminde recreate
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/LocaleController.kt`: Yeni: API 33+ LocaleManager, API 26–32 için configuration context; appLocale() yardımcısı
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/UiText.kt`: Yeni: Res/Plural/Raw metin anahtarı ve @Composable resolve()
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/Models.kt`: SourceFailure(message) yerine SourceFailure(reason: FailureReason); MediaKind.label yerine labelRes; StoredSourcesUnavailable nedeni
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/SourceRepository.kt, XmltvParser.kt, EpgRepository.kt, EpgModels.kt, EpgStore.kt, CatalogParser.kt, DeviceStore.kt`: Türkçe hata metinleri yerine FailureReason değerleri
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/DeviceStore.kt`: appLanguage anahtarı (doğrulamalı); plus_v1 şifreli hak kaydı (seal/unseal, bağlam plus-v1); forgetSource bu anahtarları korur
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/CategoryVisibility.kt`: Çok dilli yetişkin kelime listesi, \p{L} sınırları, NFKD katlama
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/CatalogPresentation.kt`: turkish yerine uygulama dili: ratingText ve librarySorted Collator; LibrarySort.label yerine labelRes
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/PlusController.kt`: Yeni: PlusState, Entitlement, PlusFeature, saf reduce kuralları
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/billing/StoreBilling.kt`: Yeni: arayüz ve Play Billing uygulaması (bağlanma, ürün, başlatma, sahip olunanlar, onay, imza doğrulama)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/TvViewModel.kt`: Durumdaki metinler UiText; "Tümü" yerine AllCategory; SourceDraft varsayılan adı kaynaktan; appLanguage(), plus durumu, plusOpened/buyPlus/restorePlus/allows; ON_START'ta sahip olunanlar sorgusu
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SettingsScreen.kt`: Metinler stringResource; activeSection enum; yeni LANGUAGE ve PLUS sayfaları; NEXTV Plus ve Dil satırları; page rememberSaveable; SimpleDateFormat uygulama diline
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PilotApp.kt`: settings rememberSaveable; metinler kaynağa; kurulumdaki Xtream etiketi (karara göre)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/HomeScreen.kt`: widestLabel çözülmüş etiketleri ölçer; metinler kaynağa
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/LibraryScreen.kt`: AllCategory sabiti, etiketler kaynaktan; metinler kaynağa
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/GuideScreen.kt, GuideSettings.kt`: guideLocale yerine uygulama dili, 12/24 saat TV ayarından; metinler kaynağa
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt`: trackLabel uygulama dilinde; satın alma başlatılmadan önce oynatma duraklatılır; metinler kaynağa
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/Theme.kt`: PilotTheme içinde LocalLayoutDirection = Ltr (RTL tasarlanana kadar)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SourcesScreen.kt, SearchScreen.kt, DetailScreen.kt, LoadingScreen.kt, SourceRecoveryScreen.kt, PlaybackRecovery.kt, EpisodeEndDialog.kt, EpisodeEndController.kt, LaunchCover.kt, RemoteTextReader.kt`: Metinler stringResource / UiText; tarih biçimi uygulama diline
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/... (yeni ve güncellenen)`: StringResourcesParityTest, NoHardcodedUiTextTest, FailureReasonTest, LocaleFormattingTest, CategoryVisibilityTest eklemeleri, AllCategoryMigrationTest, PlusControllerTest, EntitlementCodecTest, PurchaseSignatureTest, DeviceStoreLanguageTest; Türkçe metin bekleyen testler nedene geçer
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/... (yeni ve güncellenen)`: Ortak appLanguage=tr kurulumu; LanguageSwitchTest, PseudoLocaleLayoutTest, RtlFallbackTest, PlusPageTest (sahte StoreBilling)
- `docs/decisions/DECISIONS.md, docs/product/PRD.md (§12), docs/project/PLAY_STORE_READINESS.md, docs/project/STATUS.md, docs/project/HANDOFF.md, docs/project/BACKLOG.md`: Sahip kararları D072 ve sonrası; gelir modeli ve diller; Data safety ve gizlilik politikasına ödeme notu; yeni backlog maddeleri

**Yeni bağımlılıklar:** com.android.billingclient:billing-ktx:9.1.0 (Google Play Billing Library, 18.06.2026; sürüm uygulama günü resmî sürüm notlarından doğrulanır). Kaynaklar: https://developer.android.com/google/play/billing/release-notes · Ücretler: https://support.google.com/googleplay/android-developer/answer/112622 · Vergi (Türkiye'de Google'ın sorumlu olduğu ve vergi dahil fiyat listesi): https://support.google.com/googleplay/android-developer/answer/138000 · Satıcı kaydı desteklenen ülkeler: https://support.google.com/googleplay/android-developer/answer/9306917 · Yerel fiyatlar: https://support.google.com/googleplay/android-developer/answer/6334373 · TiviMate TR fiyat örneği (üçüncü taraf): https://www.mydealz.de/deals/tivimate-premium-fur-599-tr-ca-1160eur-uber-google-playstore-turkei-2725588

---

# Satış, fiyat, diller ve uluslararası satın alma: tasarım

18.10.2026 · salt okuma tasarımı (0.14.43 çalışma ağacı). Fiyatlar, sınırlar ve satış ülkeleri **öneridir**. Karar verilince DECISIONS'a D072 ve sonrası olarak yazılır. Hukuki ve vergisel noktalar avukat ve mali müşavir onayı ister (bkz. `docs/project/PLAY_STORE_READINESS.md` §3.7).

> Kapsam notu: Bu çalışmayı başlatan iş akışı "oynatıcı içi kanal listesi ve mini rehber" görevini hesapladı. Kullanıcının aktarılan isteği satış stratejisi, fiyat, yabancı dil davranışı ve farklı ülkelerden satın alma üzerine. Bu belge o isteği yanıtlıyor. Kanal listesi tasarımı ayrıca istenmeli.

---

## 1. Bugünkü durum (kodda doğrulandı)

**Gelir**
- Satın alma altyapısı yok: `app/build.gradle.kts` içinde billing bağımlılığı yok. `release` paketi debug anahtarıyla imzalanıyor (`signingConfig = signingConfigs.getByName("debug")`), Play bunu kabul etmez.
- Gelir modeli açık karar: DECISIONS "Açık kararlar" bölümü ("hedef pazar/diller, gelir modeli"), PRD §12 ("Ücretsiz/ücretli paket ayrımı henüz yok"), TIVIMATE_GAP §2 son satır ("İş modeli … Karar yok").
- Ayarlar → Uygulama hakkında'da "Uygulama içerik sağlamaz…" cümlesi var (`ui/SettingsScreen.kt`, `SettingsPage.ABOUT`).

**Dil: yabancı dilli bir TV'de bugün ne oluyor?**

| Konu | Kod | Sonuç |
|---|---|---|
| Arayüz metinleri | `res/values/` altında yalnız `styles.xml`. Metinler `Text("…")` içinde sabit; en yoğun dosyalar `SettingsScreen`, `LibraryScreen`, `TvViewModel`, `PilotApp`, `PlayerScreen`, `GuideScreen` (yaklaşık 500 metin) | Her TV dilinde Türkçe |
| Hata metinleri | `data/Models.kt` `open class SourceFailure(message: String)`. Veri katmanında yaklaşık 60 Türkçe mesaj (`SourceRepository`, `XmltvParser`, `EpgRepository`, `DeviceStore` …) | Hatalar Türkçe; ViewModel durumuna metin olarak yazılıyor (`CatalogNotice.message`, `SourceStatus.notice`, `progressLabel`, `guideSaveError`) |
| Kimlik gibi kullanılan Türkçe metin | `TvState.category = "Tümü"` (TvViewModel 54, 258, 398, 467, 526, 747; LibraryScreen 83–119, 272, 303); `SettingsScreen` `activeSection` bölüm adını kimlik olarak kullanıyor; `MediaKind(label)` | Doğrudan çevrilemez; önce anahtara çevrilmeli |
| Menü genişliği | `HomeScreen` 105: `widestLabel` sabit Türkçe listeyi ölçüyor | Uzun Almanca etiket ölçülmez, kesilir |
| Tarih ve saat | `GuideScreen.guideLocale = tr`, `"HH:mm"`, `"d MMM EEEE"`; `SettingsScreen` 161 ve `SourcesScreen` 93 `SimpleDateFormat(..., tr)` | "18 Eki Cumartesi", hep 24 saat |
| Sayı ve sıralama | `CatalogPresentation.turkish`: `ratingText` "7,5"; `librarySorted` `Collator.getInstance(turkish)` | Ondalık virgül; Türkçe harf sırası |
| Parça dili adı | `PlayerScreen.trackLabel`: `getDisplayLanguage(tr)` | "İngilizce · 1" |
| Yetişkin kategori algısı | `CategoryVisibility.adultWords`: xxx, adult(s), porn(o), yetiskin, erotik/erotic, sex, 18+ | Almanca "Erwachsene", İspanyolca "adultos", Fransızca "adulte", Kiril ve Arapça adlar **yakalanmaz**: güvenlik açığı |
| Ad sadeleştirme | `displayTitle` / `displayCategory` (`[A-Z]{2,3}` öneki) | Dilden bağımsız, sorun yok |
| Arama | `TitleIndex`: `Locale.ROOT` + `ı→i` + NFKD | Aksanları her dilde katlıyor, sorun yok |
| Yön | Manifest `android:supportsRtl="true"`. D-pad işleyicileri soldan sağa varsayıyor (D071 slayt uçları, sağ/sol sarma, Sol ile menü) | Arapça TV'de düzen aynalanabilir, tuş anlamları ters düşer |
| Klavye | Sistem TV klavyesi (`BasicTextField`) | Dil klavyeye bırakılmış, sorun yok |
| Font | Paketteki `roboto.ttf` | Latin, Kiril ve Yunanca var; Arapça sistem fontuna düşer |

---

## 2. Satış stratejisi (öneri)

### 2.1 Konumlandırma
"Kendi kaynağın için modern, reklamsız ve veri toplamayan TV oynatıcı. Film ve dizi kütüphanesi güçlü, Türkçe ve İngilizce." Dil yalnız **uygulama özelliklerini** anlatır. Kanal, maç, "ücretsiz", sağlayıcı adı ya da "Xtream Codes" mağaza başlığında ve anahtar kelimelerinde hiçbir dilde geçmez (Play raporu §3.2).

### 2.2 İş modeli
1. **Ücretsiz çekirdek:** Bugün çalışan her şey ücretsiz kalır: canlı TV, film, dizi, rehber, arama, favoriler, izlemeye devam, birden çok kaynak. Var olan bir özellik sonradan ücretli yapılmaz; yapılırsa güven kaybı ve tek yıldızlı yorum riski doğar.
2. **NEXTV Plus, tek seferlik ve ömür boyu** (Play'de tüketilmeyen tek seferlik ürün). Plus abonelik değildir. Gerekçeler:
   - TV kullanıcısı aboneliği sevmez.
   - 6502 m.52/3 kapsamındaki otomatik yenileme riskini almaz.
   - Fiyat artışında onay akışı gerekmez.
   - Plus'taki özellikler cihazda çalışır ve geliştiriciye sürekli maliyet doğurmaz.
3. **Abonelik ancak sunucu gerektiren bir özellik gelirse** (EXT05 senkronizasyon, EXT01 QR eşleştirme sunucusu): "Plus + Bulut" yıllık. Bugün önerilmez.
4. **Reklam yok.** Reklam SDK'sı veri toplar ve Data safety beyanını bozar. Ayrıca "analitik yok" kuralıyla çelişir, TV deneyimini düşürür ve reklam ağları IPTV uygulamalarını riskli sayar.
5. **Ödeme kanalı yalnız Google Play.** Etkinleştirme kodu, web satışı, bayi ya da satıcı ortaklığı yok (Play raporu §3.2 bayi ve kod modelini riskli sayar).

### 2.3 Ücretsiz ve Plus sınırı
İlke: **İzlemek ücretsiz; özelleştirme ve güç kullanıcısı araçları Plus. Güvenlik ve erişilebilirlik asla ücretli olmaz.**

| Ücretsiz (kalıcı) | Plus (özellik hazır oldukça) |
|---|---|
| Bugünkü tüm özellikler, birden çok kaynak | Catch-up, "Geçmiş yayınlar" (EXT02) |
| PIN ve ebeveyn denetimi (BKL-028), yetişkin filtresi | EPG araçları: birden çok XMLTV, saat kaydırma, elle eşleme (EXT03) |
| Oynatıcı içi kanal listesi, bilgi bandı, son kanal | Yedekleme ve geri yükleme (şifreli) |
| Büyük yazı, Hareketi azalt, altyazı ve ses seçimi | Favori grupları, kanal ve kategori sıralama, yeniden adlandırma |
| Watch Next / Play Next | Program hatırlatıcısı |
| Zamanlanmış liste ve EPG güncellemesi | Canlı timeshift, çoklu ekran (bağlantı limiti kuralıyla) |

**Satış eşiği:** Plus, en az üç Plus özelliği yayımlanmadan satışa açılmaz. Gelecekte gelecek özelliği önceden satmak iade ve tüketici şikâyeti riski taşır.

### 2.4 Fiyat önerisi

| Pazar | Liste fiyatı | Açılış (ilk 30 gün) | Not |
|---|---|---|---|
| Euro bölgesi | 14,99 € | 9,99 € | Vergi dahil gösterilir |
| ABD | 14,99 $ | 9,99 $ | Eyalet satış vergisini Play ekler |
| Birleşik Krallık | 12,99 £ | 8,99 £ | Vergi dahil |
| Türkiye | 299,99 TL | 199,99 TL | Vergi dahil; 6 ayda bir gözden geçirilir |
| Diğer ülkeler | Play'in otomatik çevirisi | — | Düşük alım gücü olan pazarlar elle düşürülebilir |

Gerekçe:
- Üçüncü taraf kaynaklara göre TiviMate Premium yaklaşık 9,99 €/yıl ya da 33,99 € ömür boyu (resmî olarak doğrulanamadı). Yeni ve bilinmeyen bir marka için ömür boyu fiyatın yaklaşık yarısı makul bir giriş.
- Bir indirim sitesinde TiviMate ömür boyu lisansın Türk Play hesabıyla 599 TL'ye alındığı görülüyor (Şubat 2026). Düşük TL fiyatı bölgeler arası fırsatçılığı çeker, ama Play satın almayı ödeme profilinin ülkesine bağladığı için risk sınırlı.
- Play, otomatik çevrilen fiyatı ayarlandığı günün kuruyla dondurur. TL fiyatı enflasyon nedeniyle düzenli güncellenmeli; tek seferlik üründe fiyat değişikliği eski alıcıları etkilemez.
- Play ücreti: tek seferlik ürünlerde %15 katmanı (yıllık ilk 1 milyon $, katmana kayıt gerekir). ABD, Birleşik Krallık ve AEA'da abonelik için %10 hizmet + %5 faturalama ücreti uygulanıyor.

### 2.5 Satış kanalları
- Google Play, Android TV form faktörü. Mağaza kaydı Türkçe ve İngilizce, ekran görüntüleri sentetik içerikle (Play raporu §2.2, §3.2).
- **Play'in makine çevirisi kullanılmaz.** Denetlenmeyen ifade korsan çağrışımı yapabilir.
- Kendi web sayfası: tarafsız metin, gizlilik politikası, telif bildirim adresi.
- Amazon Fire TV ayrı mağaza, ayrı IAP ve ayrı yapı türü demek; ikinci faz. Yan yükleme ve Play'siz kutularda satın alma yok (bkz. §4.5).
- Yapılmayacaklar: IPTV satıcılarıyla ortaklık ya da komisyon, beyaz etiket, "önerilen sağlayıcı", satış ortaklığı yapan inceleme kanallarıyla anlaşma.

### 2.6 Ölçüm
Uygulamada analitik yok ve olmayacak. Başarı yalnız **Play Console'un toplu verisiyle** izlenir: mağaza kaydı dönüşümü, kurulum, satın alma, iade ve puan. Mağaza kaydı denemeleri ile TR/EN metinleri karşılaştırılır.

### 2.7 Sıra
Önce Play raporundaki engeller kalkar: ad ve marka araştırması, kalıcı paket adı ve imza anahtarı, AAB, gizlilik politikası, 12 test kullanıcısı × 14 gün kapalı test. Ardından **ücretsiz yayın**, sonra eşik dolunca **Plus**. Ürün kimliği marka değişse de kalsın diye nötr olur: `plus_lifetime`. Play'de ürün kimliği sonradan değiştirilemez ve yeniden kullanılamaz.

---

## 3. Yabancı dillerde davranış (hedef)

### 3.1 Kurallar

| Konu | Hedef davranış |
|---|---|
| Arayüz dili | Ayarlar'daki seçim; "Sistem dili" seçiliyse TV dili. Desteklenen ilk dalga **Türkçe, İngilizce**; ikinci dalga Almanca |
| Desteklenmeyen dil (ör. Fransızca TV) | İngilizce. `values/` İngilizce, `values-tr/` Türkçe |
| Katalog içeriği | Hiç çevrilmez. Kanal, film ve kategori adları sağlayıcıdan geldiği gibi; `displayTitle`/`displayCategory` sadeleştirmesi değişmez |
| Tarih ve sayı | Arayüz dilinin yerel biçimi. 12/24 saat TV ayarından (`DateFormat.is24HourFormat`). Rehber ızgarası "10:30 PM" genişliğinde sınanır |
| Puan | "7,5" (tr, de) / "7.5" (en) |
| Sıralama | Arayüz diline göre `Collator` |
| Parça dili adı | Arayüz dilinde ("English · 1" / "İngilizce · 1") |
| Arama | Değişmez (`TitleIndex` katlaması dilden bağımsız) |
| Yetişkin kategori algısı | Çok dilli kelime listesi (de, fr, es, it, pt, nl, pl, ru, ar dahil), harf sınırı `\p{L}` ile; varsayılan gizleme sürer |
| Sağdan sola diller | İlk dalgada sunulmaz. **Düzen soldan sağa kilitlenir** (`PilotTheme` içinde `LocalLayoutDirection provides Ltr`). Arapça bir TV İngilizce metinle düzgün, aynalanmamış düzen görür |
| Saat dilimi | Değişmez; TV saat dilimi ilk sırada (`GuideSettings`) |
| Varsayılan kaynak adı | `SourceDraft(name = "Evim")` kaynaktan gelir ("Home" / "Evim"); kayıtlı adlar değişmez |
| Hitap | Türkçe "sen" sürer; İngilizce sade emir kipi; Almanca "du" |

### 3.2 Dil seçimi (kumanda akışı)
1. Ana menü → Ayarlar (OK). İlk odak "Hareketi azalt".
2. Aşağı → **Dil** satırı ("Görünüm ve ses" bölümü). Alt metin: "Sistem dili (Türkçe)".
3. OK → yeni `SettingsPage.LANGUAGE`. Başlık "Dil". Liste: **Sistem dili**, **Türkçe**, **English**. Diller kendi adlarıyla yazılır; seçili olanda onay işareti var (`Action(check = true)`, `GuideSettings` saat dilimi listesindeki kalıp). İlk odak seçili öğede.
4. OK ile bir dil seçilir. Etkinlik yeniden oluşur, panel açık kalır ve odak az önce seçilen dilde durur.
5. Geri → ana sayfa (`MAIN`), odak Dil satırında. Bir Geri daha → panel kapanır.
- TalkBack: liste öğeleri "English, seçili değil" biçiminde okunur; sayfa başlığı `heading()`. Dil değişiminde yeni dilde "Dil: English" duyurulur (polite live region).
- Büyük yazı (fontScale 1.5): mevcut kaydırmalı ayar sütunu ve kenar solması kullanılır; yeni sabit genişlik yok.

### 3.3 Teknik tasarım

**Kaynaklar**
- `res/values/strings.xml` İngilizce ve varsayılan. `res/values-tr/strings.xml` bugünkü Türkçe metnin birebir kopyası. Sayılar için `plurals` ("12 kategori · 3 gizli").
- `res/resources.properties`: `unqualifiedResLocale=en-US`.
- `build.gradle.kts`:
  - `androidResources { generateLocaleConfig = true; localeFilters += listOf("en", "tr") }` (AGP 9.0.1 destekler).
  - Debug yapısında `isPseudoLocalesEnabled = true` (en-XA metin uzatma, ar-XB yön testi).
- Etkin dili bilmek için `<string name="ui_locale" translatable="false">en</string>` (tr: `tr`). Biçimlendirme bu etiketi kullanır, böylece Fransızca bir TV'de İngilizce metinle Fransızca tarih karışmaz.

**Durumda metin değil anahtar**
- Dil değişince `TvViewModel` (`viewModels()`) yaşamaya devam eder. Bu yüzden durumda çözülmüş metin tutulamaz.
- Yeni `data/UiText.kt`: `sealed interface UiText { Res(@StringRes id, args); Plural(id, count, args); Raw(text) }` ve `@Composable fun UiText.resolve()`.
- `SourceFailure(message)` yerine `SourceFailure(val reason: FailureReason)`. `FailureReason` bir enum'dır ve her değeri bir `@StringRes`'e eşlenir. Ham URL ya da neden asla taşınmaz; bugünkü kural korunur.
- `CatalogNotice.message`, `SourceStatus.notice`, `TvState.progressLabel`, `guideSaveError`, `GuideStatus` metinleri `UiText` olur.

**Kimlik gibi kullanılan metinler**
- `"Tümü"` yerine `const val AllCategory = "@all"` (LibraryScreen'deki `FavoritesCategory = "@favorites"` kalıbı). Etiket kaynaktan gelir.
- `SettingsScreen.activeSection` bir enum kimliği tutar.
- `MediaKind.label` yerine `@StringRes labelRes`.
- `HomeScreen.widestLabel`, `stringResource` ile çözülen etiketleri ölçer.

**Dil uygulama**
- Yeni `LocaleController` (kök paket).
  - API 33+: `LocaleManager.applicationLocales = LocaleList.forLanguageTags(tag)`. Sistem kalıcı tutar; `localeConfig` otomatik üretilir.
  - API 26–32: `MainActivity.attachBaseContext(LocaleController.wrap(base, store.appLanguage))` ile `createConfigurationContext`, ardından `recreate()`.
- Tek doğruluk kaynağı DeviceStore'daki `appLanguage` anahtarı (`""` = sistem). API 33+'ta sistem kaydıyla eşitlenir.
- `PilotApp` 70'teki `var settings by remember` ve `SettingsScreen` `page` değerleri `rememberSaveable` olur; böylece yeniden oluşumdan sonra panel Dil sayfasında açık kalır.

**Biçimlendirme**
- `GuideScreen.guideLocale`, `CatalogPresentation.turkish` (`ratingText`, `Collator`), `PlayerScreen.trackLabel`, `SettingsScreen` 161 ve `SourcesScreen` 93, tek bir `appLocale()` yardımcısını kullanır.
- `Locale.ROOT` kimlik ve arama katlamasında kalır.

**Yetişkin algısı**
- `CategoryVisibility.adultWords` genişler: erwachsene, adulte(s), adulto(s), adulti, volwassen(en), dorośli, для взрослых, взрослые, للكبار, +18/18+ çeşitleri.
- Sınırlar `(?<![\p{L}\p{N}])…(?![\p{L}\p{N}])` olur; katlama önce NFKD.
- Not: "Adult Swim" gibi adlar bugün de gizleniyor; varsayılan temkinli kalır, kullanıcı Kategorileri yönet'ten açar.

### 3.4 Çeviri kuralları ve örnek metin
- Yasal cümle her dilde aynı anlamı taşır:
  - TR "Uygulama içerik sağlamaz. Kendi M3U veya Xtream kaynağını ekleyerek izleyebilirsin."
  - EN "NEXTV doesn't provide any content. Add your own M3U or Xtream source to watch."
- Türkçe metin değişmeden taşınır. İngilizce metni ana dili İngilizce biri gözden geçirir (sahip işi). Kaynak türü etiketi için Play raporu §3.2'deki "Xtream API uyumlu" önerisi bu geçişte uygulanabilir; karar sahibin.

---

## 4. Farklı ülkelerden satın alma (Google Play Billing)

### 4.1 Play'in üstlendikleri
- Yerel para birimi ve fiyat. Uygulama fiyatı **asla sabit yazmaz**; `ProductDetails.oneTimePurchaseOfferDetails.formattedPrice` gösterilir (ör. "₺299,99", "14,99 €").
- Vergi: AB, Birleşik Krallık ve Türkiye'de fiyat vergi dahildir. Google; Türkiye dahil birçok ülkede KDV'den sorumlu satıcıdır ve listelenen ABD eyaletlerinde satış vergisini toplar.
- Ödeme yöntemleri ülkeye göre değişir: kart, operatör faturası, hediye kartı, nakit gibi bekleyen ödemeler.
- Türkiye'den geliştirici ve satıcı kaydı destekleniyor (varsayılan para birimi TRY).

### 4.2 Mimari

Yeni bağımlılık: `com.android.billingclient:billing-ktx:9.1.0` (18 Haziran 2026). Sürüm, uygulama günü resmî sürüm notlarından doğrulanır. `BILLING` izni kütüphane manifestiyle gelir.

```kotlin
// billing/StoreBilling.kt: Play'e ince sarmalayıcı; testte sahte (fake) sürümü kullanılır
interface StoreBilling {
    suspend fun connect(): Availability            // OK, UNAVAILABLE (Play yok/eski), ERROR
    suspend fun product(): StoreProduct?           // id = "plus_lifetime", formattedPrice
    fun launch(activity: Activity, product: StoreProduct): LaunchResult
    suspend fun owned(): Owned                     // Ok(List<StorePurchase>) | Failed
    suspend fun acknowledge(token: String): Boolean
    val updates: SharedFlow<PurchaseUpdate>        // PURCHASED, PENDING, CANCELED, ERROR
}
// data/PlusController.kt: saf Kotlin, JVM'de test edilir
enum class PlusState { UNKNOWN, LOCKED, PENDING, ACTIVE, UNAVAILABLE }
data class Entitlement(val state: PlusState, val tokenHash: String?, val checkedAt: Long)
object PlusController {
    fun reduce(cached: Entitlement, event: BillingEvent, now: Long): Decision // yeni hak, onaylanacak tokenlar, UiText bildirimi
}
enum class PlusFeature { CATCH_UP, EPG_TOOLS, BACKUP, FAVORITE_GROUPS, REMINDERS, TIMESHIFT, MULTI_VIEW }
```

`PlusController.reduce` kuralları:
- Geçerli imzalı PURCHASED → ACTIVE. Onaylanmamışsa hemen onaylanır; Play 3 gün içinde onaylanmayan satın almayı iade eder.
- PENDING → PENDING. Plus açılmaz.
- İmza geçersiz → LOCKED.
- **Başarılı** sorgu boş döner ve önbellekte ACTIVE varsa → LOCKED (iade ya da iptal).
- Hata, bağlantı yokluğu ya da UNAVAILABLE önbellekteki ACTIVE'i asla düşürmez.
- İmza, Play lisans açık anahtarıyla cihazda doğrulanır (`BuildConfig.PLAY_PUBLIC_KEY`; gizli değildir). Sunucu yok.

DeviceStore anahtarları:
- `appLanguage`: düz metin, `""` ya da desteklenen etiket. Yazarken doğrulanır.
- `plus_v1`: mevcut `seal/unseal` ile AES-GCM, bağlam `"plus-v1"`. İçerik `{state, product, tokenHash, checkedAt}`. Sipariş numarası ya da ham token saklanmaz.
- `forgetSource()` bu iki anahtarı silmez.
- `resetUnreadableSources()` Keystore anahtarını sildiğinde `plus_v1` okunamaz olur ve UNKNOWN'a düşer. İlk sorgu hakkı Play'den geri getirir.

TvViewModel API:

```kotlin
// TvState
val appLanguage: String = ""
val plus: PlusUi = PlusUi()   // state, price: String?, notice: UiText?, busy
fun appLanguage(tag: String)
fun plusOpened()                     // gerekiyorsa bağlanır, ürün fiyatını çeker
fun buyPlus(activity: Activity)      // activity: LocalActivity.current (PilotApp zaten kullanıyor)
fun restorePlus()
fun allows(feature: PlusFeature): Boolean = plus.state == PlusState.ACTIVE
```

Uygulama açılışında ve `ON_START`'ta `owned()` çağrılır. Başka TV'de yapılan satın alma ve iadeler böyle yakalanır. Bu sorgu açılışı bekletmez: arka planda çalışır ve kaynak geri yükleme işiyle ilgisi yoktur.

### 4.3 Akışlar (kumanda)

**Satın alma**
1. Ayarlar'da en üstte yeni **NEXTV Plus** satırı (bölüm "NEXTV Plus").
2. OK → `SettingsPage.PLUS`. İlk odak **Satın al · ₺299,99** (birincil; D064'e göre odakta mint dolgu).
3. OK → oynatma açıksa önce duraklatılır, sonra Play satın alma penceresi açılır.
4. Onaylanınca sayfa "NEXTV Plus etkin. Teşekkürler!" der ve odak **Geri**'ye geçer.
5. İptal edilirse odak Satın al'da kalır.
6. Geri → `MAIN`, odak NEXTV Plus satırında.

**Kilitli özellik**
1. Satır ya da düğme görünür ve yanında "Plus" etiketi vardır.
2. OK → küçük pencere: "{Özellik} NEXTV Plus ile gelir." Düğmeler **Plus'ı gör** (ilk odak) / **Vazgeç**.
3. Geri ya da Vazgeç kaynağa döner, odak geri gelir.
4. Açılışta ya da oynatma sırasında satış penceresi **hiç** gösterilmez.

**Geri yükleme**
Plus sayfasında **Satın alımları geri yükle** → `owned()`.

**Erişilebilirlik**
- TalkBack: Satın al düğmesinin açıklaması fiyatı içerir. Kilitli öğe "…, NEXTV Plus gerekir" diye okunur. Sonuç bildirimi polite live region'dır.
- Hareketi azalt: yeni animasyon yok.
- Büyük yazı: sayfa mevcut kaydırmalı sütunu kullanır.

### 4.4 Durumlar ve metinler (TR / EN)

| Durum | Ayar satırı alt metni | Sayfa metni |
|---|---|---|
| LOCKED | Ek özellikler · tek seferlik ödeme / Extra features · one-time payment | "Tek seferlik ödeme. Aynı Google hesabıyla tüm TV'lerinde açılır." + özellik listesi + "NEXTV içerik satmaz; Plus yalnız uygulama özelliklerini açar." + "Ödeme ve vergiler Google Play tarafından işlenir." |
| Fiyat bekleniyor | — | Düğme devre dışı: "Fiyat yükleniyor…" / "Loading price…" |
| ACTIVE | Etkin · Teşekkürler! / Active · Thank you! | "NEXTV Plus etkin. Teşekkürler!" |
| PENDING | Ödeme onay bekliyor / Payment pending | "Ödemen onay bekliyor. Onaylanınca Plus kendiliğinden açılır." |
| İptal | — | "Satın alma iptal edildi." (hata rengi değil) |
| Geri yüklemede bulunamadı | — | "Bu Google hesabında NEXTV Plus bulunamadı." |
| ERROR | — | "Google Play'e bağlanılamadı. Tekrar dene." |
| UNAVAILABLE | Bu cihazda Google Play yok / Google Play isn't available | "Bu cihazda Google Play satın alma kullanılamıyor. Ücretsiz özellikler çalışmaya devam eder." |

### 4.5 Uç durumlar
- **Play'siz kutu ya da yan yükleme:** UNAVAILABLE; satın alma yolu yok, ücretsiz özellikler tam çalışır. Bilinçli tercih: kod ve web satışı kapsam dışı.
- **Çevrimdışı açılış:** önbellekteki hak geçerli. Tek seferlik üründe süre sınırı yok; çevrimiçi olunca yeniden doğrulanır.
- **İade ya da ters ibraz:** başarılı sorguda LOCKED. Plus ile oluşturulan kullanıcı verisi (gruplar, hatırlatıcılar) **silinmez**, gizlenir ya da salt okunur olur.
- **TV'de birden çok Google hesabı:** hak Play'deki etkin hesaba bağlıdır; geri yükleme metni hesabı anar.
- **Promosyon kodu:** Play'de kullanılır (telefondan aynı hesapla). Uygulama bir sonraki sorguda ya da güncelleme olayında açar.
- **Family Library:** tüketilmeyen üründe açılabilir; öneri açık.
- **Onay başarısız:** her açılışta onaylanmamış tokenlar yeniden denenir.
- **Çift dokunma:** `busy` iken Satın al devre dışıdır.

### 4.6 Play Console işleri (sahip)
- Ödeme profili ve satıcı hesabı (TRY), %15 hizmet ücreti katmanına kayıt, vergi bilgileri.
- `plus_lifetime` tek seferlik ürünü, ülke fiyatları (§2.4), açılış indirimi.
- Lisans test kullanıcıları, kapalı testte test kartlarıyla deneme.
- Satış ülkeleri: Play'in ücretli içerik desteklediği tüm ülkeler; avukatın işaret ettikleri hariç.

### 4.7 Gizlilik ve hukuk
- Satın alma bilgisi cihazdan çıkmaz. Data safety'de "veri toplanmıyor" beyanının sürdüğü Play yönergesiyle doğrulanmalı (doğrulanamadı).
- Gizlilik politikasına Google Play ödeme işlemi paragrafı eklenir.
- Mesafeli sözleşme ön bilgilendirmesi, dijital içerikte cayma istisnası ve GVK 20/B'nin TV uygulamasını kapsayıp kapsamadığı avukat ve mali müşavire sorulur (Play raporu §3.7).

---

## 5. Testler

**JVM (önce)**
- `StringResourcesParityTest`: `values/` ve `values-tr/` dosyalarını okur. Anahtar kümeleri, `%1$s`/`%d` yer tutucuları ve plural nicelikleri aynı olmalı.
- `NoHardcodedUiTextTest`: `ui/*.kt` içinde `Text("<harf>` ve `Action("<harf>` kalıbını yakalar (izin listeli).
- `FailureReasonTest`: her `FailureReason` bir metne eşlenir. Ayrıştırıcılar Türkçe metin yerine neden fırlatır. `CatalogPresentationTest`, `EpgSourceTest` ve `XmltvParserTest` buna göre güncellenir.
- `LocaleFormattingTest`:
  - `guideTime` 24 saat (tr) ve 12 saat (en-US),
  - `ratingText` "7,5" ve "7.5",
  - Almanca Collator sırası,
  - `trackLabel` için İngilizce ve Türkçe dil adı.
- `CategoryVisibilityTest` eklemeleri: Erwachsene, ADULTOS, adulte, Для взрослых, للكبار, "18+ Filme" gizlenir. "Adultes Bildung" gibi uç örnekler ve yanlış pozitifler belgelenir.
- `AllCategoryMigrationTest`: sağlayıcıda gerçekten "Tümü" adlı bir kategori olsa da tüm liste ile karışmaz.
- `PlusControllerTest`:
  - imzalı PURCHASED → ACTIVE ve onay isteği,
  - PENDING → kilit açılmaz,
  - geçersiz imza → LOCKED,
  - başarılı boş sorgu → LOCKED,
  - hatalı sorgu ya da UNAVAILABLE → ACTIVE korunur,
  - iptal → durum değişmez, bildirim çıkar,
  - onaylanmamış token tekrar denenir.
- `EntitlementCodecTest`: gidiş-dönüş; bozuk blok ve yanlış bağlam UNKNOWN verir, çökmez.
- `PurchaseSignatureTest`: testte üretilen RSA anahtar çiftiyle.
- `DeviceStoreLanguageTest`: yalnız `""`, `tr`, `en` kabul edilir.

**Enstrümantasyon**
- Ortak kural: mevcut 28 test sınıfı açılıştan önce `appLanguage = "tr"` yazar; Türkçe metin eşleşmeleri bozulmaz.
- `LanguageSwitchTest`: Ayarlar → Dil → English sonrası başlık "Settings" olur; panel açık kalır ve odak English'te durur. Türkçe'ye dönüş de denenir.
- `PseudoLocaleLayoutTest` (en-XA, fontScale 1.5): menü açık, Ayarlar paneli, oynatıcı bandı ve Plus sayfasında `TextLayoutResult.hasVisualOverflow` olmamalı.
- `RtlFallbackTest` (ar-XB): düzen soldan sağa kalır, D071'deki Sol/Sağ davranışı değişmez.
- `PlusPageTest` (sahte `StoreBilling` ile): kilitli → satın al → etkin; iptal; bekleyen; Play yok metni. Geri ile odak Plus satırına döner. TalkBack açıklamasında fiyat bulunur.

---

## 6. Aşamalar ve efor

| # | Aşama (her biri yayımlanabilir) | Efor |
|---|---|---|
| 1 | Metinleri kaynağa taşıma: `values/` **şimdilik Türkçe** (görünür değişiklik yok), `UiText`, `FailureReason`, `"Tümü"` → `@all`, `activeSection`/`widestLabel`, yerel biçimlendirme yardımcısı, soldan sağa kilit, sözde diller, eşlik testleri | L · 5 gün |
| 2 | İngilizce: `values/` İngilizce, `values-tr/` Türkçe; Dil sayfası ve `LocaleController`; çok dilli yetişkin algısı; mağaza kaydı EN | M · 3 gün (+ çeviri incelemesi) |
| 3 | Billing temeli: `StoreBilling`, `PlusController`, `plus_v1`, Plus sayfası ve geri yükleme; satış kapalı (yalnız lisans test kullanıcıları) | M · 4 gün |
| 4 | Satış açılışı: `PlusFeature` kapıları ilk üç Plus özelliğine bağlanır, kilitli öğe penceresi, Console fiyatları, gizlilik politikası güncellemesi. Özelliklerin kendisi bu kümenin dışında | S · 1,5 gün |
| 5 | İsteğe bağlı: Almanca (`values-de`), Fire TV araştırması | S · 1 gün (+ çevirmen) |

Toplam yaklaşık 14,5 mühendis-günü.