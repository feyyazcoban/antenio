# Gap uygulama planı

18 Ekim 2026 · planlama ajanı; öneriler kullanıcı kararı değildir.

# NEXTV entegrasyon planı: TiviMate eksikleri, satış, dil ve ülkeler arası satın alma

Bu plan sekiz küme tasarımını (pin, satış/dil/satın alma, playback, epg, catchup, organize, backup, record) birleştiriyor. Dayanağı 0.14.43 sürümünün kod okuması; çalışma ağacı temiz. Kod değiştirilmedi, Gradle ve adb çalıştırılmadı.

- **Dosya yolları:** Aksi yazılmadıkça yollar `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/` altındadır. Testler `app/src/test/...` ve `app/src/androidTest/...` altındadır.
- **Öneri durumu:** Fiyatlar, ücretsiz/Plus sınırı ve sıralama öneridir, kullanıcı kararı değildir.
- **Tasarımı olmayan satır:** TIVIMATE_GAP'teki P0 satırı (oynatıcı üstünde kanal listesi, mini rehber, bilgi bandı, son kanal) için tasarım üretilmedi. O yuvada kullanıcının satış sorusu yanıtlandı. Planda bu iş için S15 yer tutucusu var; ayrı bir tasarım çalıştırması gerekiyor.

---

## 0. Satış, fiyat, yabancı dil ve farklı ülkeler: kısa yanıt (öneri)

**Bugünkü durum (kodda):**
- Gelir altyapısı yok. `app/build.gradle.kts` içinde billing bağımlılığı yok ve release paketi debug anahtarıyla imzalanıyor.
- `res/values` altında yalnız `styles.xml` var. Metinler Kotlin dosyalarına Türkçe olarak gömülü.
- Beş dosyada `Locale.forLanguageTag("tr")` geçiyor: CatalogPresentation, GuideScreen, PlayerScreen `trackLabel`, SettingsScreen, SourcesScreen.
- Rehber dili sabit: `XmltvParser(language = "tr")`.
- Sonuç: İngilizce bir TV'de uygulama baştan sona Türkçe açılıyor.

**Gelir modeli:**
- Çekirdek ücretsiz kalır. Bugün ücretsiz olan hiçbir özellik ücretliye geçmez.
- Güç kullanıcısı özellikleri tek seferlik, ömür boyu geçerli **NEXTV Plus** uygulama içi ürünüyle açılır.
  - Ürün kimliği nötr tutulur: `plus_lifetime`.
  - "Plus" adı marka kontrolünden sonra kesinleşir.
- Reklam yok.
- Abonelik yalnız sunucu maliyeti doğuran bir özellik gelirse eklenir (senkronizasyon, QR eşleştirme).
- Etkinleştirme kodu, bayi ve web satışı yok. Play raporu 3.2'ye göre satıcı ve aktivasyon kodu modeli korsan algısı yaratır.

**Fiyat:**
- Önerilen fiyat, vergi dahil: 14,99 € / 14,99 $ / 12,99 £ / 299,99 TL.
- İlk 30 gün açılış fiyatı: 9,99 € / 9,99 $ / 8,99 £ / 199,99 TL.
- Diğer ülkelerde Play'in yerel fiyat çevirisi kullanılır. TL fiyatı 6 ayda bir gözden geçirilir.
- Kayıt ve çoklu ekran Plus'a eklendiğinde fiyat yalnız yeni alıcılar için yeniden değerlendirilir; mevcut alıcılar etkilenmez.
- Rakip fiyatı yalnız üçüncü taraf kaynaklardan biliniyor ve doğrulanamadı: TiviMate yaklaşık 9,99 $/yıl, ömür boyu 33,99 $.

**Ücretsiz / Plus sınırı (tasarımlar birleştirildi):**
- **Plus'ta:** geçmiş yayın (catch-up) ve Baştan izle, program hatırlatıcısı, ikinci ve sonraki favori grupları, Taşı (özel sıra), yeniden adlandırma, kayıt, çoklu ekran, "Her zaman" timeshift arabelleği.
- **Ücretsiz kalanlar:**
  - Bugünkü her şey.
  - PIN, ebeveyn denetimi ve yetişkin filtresi (Play'in yetişkin içerik istisnası buna bağlı).
  - İki dil ve birden çok kaynak.
  - Oynatıcı üstü kanal listesi.
  - Bütün oynatıcı ayarları: en-boy, altyazı, arabellek, kod çözücü, AFR, ses gecikmesi, PiP, harici oynatıcı.
  - EPG düzeltme ve program araması.
  - Bağlantı ayarları ve otomatik güncelleme.
  - Tek favori listesi ve tek içerik gizleme.
  - Yedekleme ve Watch Next.
  - Canlı geri sarma: sağlayıcı penceresi ve "Duraklatınca" arabelleği.
- **Satış tasarımından farkı:** EPG araçları, yedekleme ve timeshift ücretsize alındı. Gerekçe ilgili küme tasarımlarında:
  - Güvenilirlik ve güven özellikleri paraya bağlanmamalı.
  - "PIN'i unuttum" sıfırlamasının tek telafisi yedektir.
  - Ücretsiz geri sarma, rakibe karşı en görünür farktır.

**Yabancı dillerde davranış:**
- İlk dalga Türkçe ve İngilizce. Varsayılan `values/` İngilizce, `values-tr/` Türkçe; böylece tanımsız bir TV dilinde uygulama İngilizce açılır.
- Ayarlar → Dil seçenekleri: Sistem dili / Türkçe / English.
- Tarih, saat, sayı ve sıralama uygulama dilinin yerel ayarıyla gösterilir.
- Sağlayıcının verdiği adlar değişmez. D065 görüntü sadeleştirmesi aynen sürer.
- Rehberde program adı seçim sırası: uygulama dili → cihaz dili → dil etiketi olmayan → herhangi biri.
- Sağdan sola diller ilk dalgada yok. Düzen soldan sağa kilitlenir, D-pad yönleri aynalanmaz.
- Play'in makine çevirisi kullanılmaz. Almanca ikinci dalgada.

**Farklı ülkelerden satın alma:**
- Satış yalnız Google Play Billing üzerinden yapılır. Para birimi, yerel ödeme yöntemleri ve vergi Play'dedir.
- AB, Birleşik Krallık ve Türkiye'de vergiden Google'ın sorumlu olduğunu satış tasarımı Play yardım sayfasına dayandırıyor. Bu sayfa uygulama günü yeniden okunmalı.
- Uygulama fiyatı koda yazmaz. Ekranda her zaman `ProductDetails` içindeki yerel fiyat metni gösterilir.
- Satın alma Google hesabına bağlıdır. Aynı hesaptaki her TV'de ve başka ülkeye taşınınca "Satın alımı geri yükle" ile geri gelir.
- Bazı ülkelerde nakit gibi ödeme yöntemleri bekleyen (PENDING) durumuna düşer; bu durum ayrıca ele alınır.
- Satın alma 3 gün içinde onaylanmazsa Play otomatik iade eder. Bu yüzden onay her açılışta yapılır.
- Hak bilgisi cihazda şifreli tutulur. Sunucu ve analitik yok.
- Satış ülkeleri: Play'in ücretli içerik desteklediği bütün ülkeler, avukatın sakıncalı bulduğu ülkeler hariç.
- Play'siz kutu kullananlar satın alamaz (bilinçli tercih). Fire TV (Amazon IAP) ikinci faz.

**Planda nerede:**
- S01–S03: metinler kaynak dosyalarına taşınır, görünür değişiklik yok.
- S13: İngilizce ve Dil seçimi.
- S35–S36: Play Billing altyapısı, satış kapalı.
- S37: satış açılışı. En az üç Plus özelliği yayımlanınca açılır; en erken S34'ten sonra, yaklaşık 77 mühendis-günü.
- Takvimi asıl belirleyen sahibin ön koşullarıdır (§4c): marka, paket adı, upload anahtarı, ödeme profili, vergi ve hukuk.

---

## 1. Önce kurulacak ortak temeller

| # | Temel | Bugünkü kod | Ne kurulur | Kullanacak kümeler | Adım |
|---|---|---|---|---|---|
| T1 | Metin ve biçim katmanı | `SourceFailure` Türkçe mesaj taşıyor (`data/Models.kt`). `TvState.progressLabel/error/...` hazır metin tutuyor. `"Tümü"` 16 yerde kategori işareti olarak kullanılıyor (TvViewModel, LibraryScreen, SearchScreen). `MediaKind.label` Türkçe. | `data/UiText.kt`, `FailureReason`, `CategoryFilter.ALL`, `ui/Formats.kt`, soldan sağa kilit, sözde diller, sabit metin yasağı testi | Hepsi | S01–S03 |
| T2 | Ayarlar çerçevesi | `ui/SettingsScreen.kt`: `SettingsPage` enum'u, tek bir `when`, sayfa başına FocusRequester (`remove`, `history`, `about`, `categories`). `activeSection` Türkçe etiketi durum olarak tutuyor. | Sayfa yığını, satır anahtarıyla odak dönüşü, satır bileşenleri, sayfa kaydı, PIN ve Plus koruma kancası | pin, dil/Plus, playback, epg, catchup, organize, backup, record | S04 (iki düzey S14'te) |
| T3 | Erişim ve yetenek kapısı | `open`/`watch`/`play` yalnız `entry.adult \|\| sourceFor(entry) == null` denetliyor, üstelik ekranın verdiği kopyayla. `playChannelNumber`, `changeChannel`, `playNextEpisode` doğrudan `play` çağırıyor. | `AccessGate` (saf), `AccessTarget`, `Feature`/`Entitlements`, `AppOverlay`, hızlı menü modeli, yaşam döngüsü kancası | pin (çekirdek), catchup, backup (derin bağlantı), record, organize, epg, kanal listesi, Plus | S05 |
| T4 | Oynatıcı temeli | `ui/PlayerScreen.kt`: `ExoPlayer.Builder(context).build()`. `LaunchedEffect(media)` içinde sabit User-Agent'lı `DefaultHttpDataSource`. Katmanlar `visible`/`trackPanel`/`endState` değişkenleri. Bütün tuşlar tek `onPreviewKeyEvent` içinde. Gizlenme süresi 8000 ms sabit. | `PlayerFactory`, `PlaybackRequest`, `PlayerLayer`, `PlayerKeyRouter` (saf); davranış değişmez | playback, catchup, organize S18, record, pin, kanal listesi, PiP | S06 |
| T5 | Veri temeli | `SourceRegistryCodec.decode` yalnız `version == 1` kabul ediyor. `DeviceStore.saveSources` silinen kaynak için yalnız `f:`, `history:`, `p2:` anahtarlarını siliyor. Liste/EPG istekleri `"NEXTV AndroidTV"` (SourceRepository.get, EpgRepository) gönderiyor; oynatıcı `"NEXTV/<sürüm> AndroidTV"`. D066 sürümlü biçimi söylüyor. | Kayıt codec'i v2 (eklemeli alanlar), `DeviceKeys` anahtar kataloğu, `SourceHttp` | epg, organize, record, catchup, playback, pin (sıfırlama), backup | S07 |
| T6 | Ön plan zamanlayıcısı | `restore` içinde sabit 6 saat (TvViewModel, yaklaşık satır 172), `GuideController.stale` içinde sabit 6 saat (satır 124) | Yalnız `appStarted`–`appStopped` arasında çalışan dakikalık sayaç | catchup (hatırlatıcı), organize (yenileme) | S29 (S30 da kullanır) |
| T7 | Akış bütçesi | Yok. Kural AGENTS.md'de: sınır bilinmiyorsa paralel yayın açılmaz. | Kaynak başına `StreamBudget`; sınır bilinmiyorsa 1 | record, harici oynatıcı, catch-up yeniden açma | S17 |

**Temellerin API taslağı.** Uygulayan mühendis için bağlayıcı olan kısım budur:

```kotlin
// T1
sealed interface UiText { data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
  data class Plural(@PluralsRes val id: Int, val count: Int, val args: List<Any> = emptyList()) : UiText
  data class Raw(val text: String) : UiText }            // only provider data
class SourceFailure(val reason: FailureReason, vararg val args: Any) : Exception(reason.name)

// T2
class SettingsNav { fun open(route: SettingsRoute, fromRowKey: String?); fun back(): Boolean; val top: Entry }  // pure
data class SettingsPageSpec(val id: String, val title: UiText, val section: SectionId,
  val guard: PageGuard = PageGuard.None /* Pin | Plus(feature) */, val content: @Composable SettingsScope.() -> Unit)
// SettingsScope: ToggleRow, ActionRow, ChoiceRow -> ChoicePage (✓, D053), StepperRow(−/+/Sıfırla), NoteRow

// T3
sealed interface AccessTarget { data class Open(val id: String, val resume: WatchRecord?) ; data class Play(val id: String, val restart: Boolean)
  data class Watch(val id: String); data class ChannelNumber(val n: Int); data class ChannelStep(val direction: Int)
  data class NextEpisode(val sourceId: String, val currentId: String, val nextId: String, val automatic: Boolean) }
  // later: Archive, ChannelPick, Resume(deep link), Reminder, SettingsPage, SourceManagement
sealed interface GateResult { data class Allow(val entry: MediaEntry); data object Deny
  data class NeedsPin(val target: AccessTarget); data class NeedsPlus(val feature: Feature) }
object AccessGate { fun decide(target: AccessTarget, snapshot: GateSnapshot): GateResult }
enum class Feature { CATCHUP, REMINDERS, FAVORITE_GROUPS, CUSTOM_ORDER, RENAME, RECORDING, MULTI_VIEW, ALWAYS_BUFFER }
interface Entitlements { fun has(feature: Feature): Boolean }   // AllFree until S37

// T4
data class PlaybackRequest(val entry: MediaEntry, val url: String, val headers: Map<String, String>,
  val userAgent: String, val startMs: Long, val mode: PlaybackMode /* LIVE, VOD, ARCHIVE */)
object PlayerKeyRouter { fun route(key: PlayerKey, ctx: KeyContext): PlayerCommand? }   // pure, JVM-tested
```

**Temellerle ilgili notlar:**
- **Kapının canonical çözümü:** Hedef, `allCatalog` içinden kimlikle çözülür. `allCatalog` zaten `CategoryVisibility.apply` ile süzülmüş, dolayısıyla gizli kategori kendiliğinden reddedilir. Bölümler `episodes`/`selected` üzerinden çözülür.
- **Çağrı yerleri değişmez:** Mevcut `open`, `play`, `watch` vb. ince sarmalayıcıya dönüşür. `play()` son kapı olarak kalır.
- **PIN penceresi:** Bir Dialog penceresidir; Ayarlar penceresinin üstünde açılır. Kapanınca odak `refocusAfterDialog` ile çağıran satıra döner.
- **Kayıt codec'i v2:** v1 ve v2'yi kabul eder. Kaynak başına isteğe bağlı nesneler: `guides`, `connection`, `limits`. Bilinmeyen anahtarlar yok sayılır. Sonraki adımlar sürüm artırmadan alan ekler.
- **`DeviceKeys`:** Her tercih anahtarı veya önekini şu bilgilerle tanımlar: kapsam (DEVICE/SOURCE), sıfırlamada silinir mi, yedek sınıfı (NONE/SETTINGS/PERSONAL). `saveSources`, `forgetSource` ve "her şeyi sıfırla" bu kataloğu kullanır.

---

## 2. Sıralı uygulama adımları

**Her adımın bitiş ölçütü:**
- `testDebugUnitTest`, `lintDebug` (0 hata), `assembleRelease` ve `assembleDebugAndroidTest` geçer.
- **Enstrümantasyon:**
  - Yalnız adımın kendi sınıfları, ayrı bir test emülatöründe, seri kimliği açıkça verilerek koşulur.
  - 5554'e dokunulmaz ve toplu `connectedAndroidTest` çalıştırılmaz (WORKFLOW).
  - 5556'daki gerçek kaynağı silen testler orada koşulmaz.
  - Test emülatörü yoksa test "koşulmadı" diye yazılır.
- **Elle doğrulama:** 5556'da yalnız veri silmeyen akışlarla yapılır. PIN sıfırlama, geri yükleme ve kaynak silme yalnız test emülatöründe denenir.
- **Erişilebilirlik:** 1,0/1,5/2,0 yazı ölçeği, TalkBack etiketleri, Hareketi azalt ve D064 odak dili (ölçek büyümesi yok) her adımda kontrol edilir.
- **Belgeler:**
  - STATUS, HANDOFF ve BACKLOG güncellenir.
  - Sözleşme değişirse DECISIONS'a sıradaki boş numarayla yazılır. epg ve catchup tasarımlarının ikisi de "D072" diyor; bu numara yer tutucudur.
  - BACKLOG satırı önerisi:

    | BKL | İş | Adımlar |
    |---|---|---|
    | 029 | Metinler ve iki dil | S01–S03, S13 |
    | 030 | Ortak temeller | S04–S07 |
    | 028 | PIN | S08–S12 |
    | 031 | Oynatıcı ayarları | S14, S16, S45–S48 |
    | 032 | Oynatıcıda kanal listesi | S15 |
    | 033 | Timeshift | S17, S56–S57, S64 |
    | 034 | EPG | S19–S21, S26–S28 |
    | 035 | Catch-up ve hatırlatıcı | S22–S25, S29 |
    | 036 | Bağlantı ve güncelleme | S18, S30 |
    | 037 | Kütüphane düzeni | S31–S34 |
    | 038 | NEXTV Plus | S35–S37 |
    | 039 | Watch Next | S38–S40 |
    | 040 | Yedekleme | S41–S44 |
    | 041 | Kayıt ve çoklu ekran | S49–S65 |

- TV'ye kurulum yalnız kullanıcı isteyince yapılır.

**Bütün adımlarda geçerli kurallar:**
1. Her yeni açıcı için: bir `AccessTarget`, `request` çağrısı, bir `AccessGateTest` vakası ve `ParentalFlowTest`'e bir satır eklenir.
2. Her Plus özelliği girişinde tek bir `Feature` denetimi bulunur.
3. Metinler string kaynağına yazılır; S13'ten sonra İngilizce ve Türkçe birlikte. `TvState` içinde hazır metin tutulmaz.
4. Kaynağa bağlı her kalıcı anahtar `DeviceKeys`'e kaydedilir.
5. Yeni bir ayar, çerçevede bir sayfa olarak eklenir. PIN gerektiren bir ayarsa sayfa korumalı (`guard`) olur.
6. Yeni bir oynatıcı katmanı `PlayerLayer` ve `PlayerKeyRouter` içinde bir vaka olarak eklenir. Geri tuşu bir katman çıkar.
7. İkinci bir akış açılmadan önce `StreamBudget`'a sorulur.
8. Mantık ayrı bir controller veya saf sınıfta durur (`GuideController` örneği gibi); TvViewModel yalnız ince delegedir. TvViewModel bugün 868 satır ve AndroidViewModel olduğu için JVM'de test edilemiyor.
9. Metinlerde korsanlık çağrıştıran ifade olmaz. Plus metinleri "yalnız uygulama özelliklerini açar" der.

### Faz A · Temeller (14 g)

| # | İş | Başlıca dosyalar | Testler (önce JVM, sonra cihaz) | Emülatörde elle | g |
|---|---|---|---|---|---|
| S01 | Metinler I: UiText, FailureReason, TvState metin alanları, `CategoryFilter.ALL`, `MediaKind` etiketi; `values/strings.xml` şimdilik Türkçe. Katalogda saklanan "Diğer" veri olarak kalır (D068 anahtarları buna bağlı), yalnız görüntüde çevrilir. **Metin değişiklikleri S03 bitene kadar dondurulur.** | data/UiText.kt (yeni), Models.kt, SourceRepository.kt, CatalogParser.kt, EpgModels.kt, EpgStore.kt, EpgRepository.kt, DeviceStore.kt, TvViewModel.kt, res/values/strings.xml | Mesaj karşılaştıran testler reason karşılaştırmasına geçer (SourceTest, EpgSourceTest, SourceRegistryTest); UiTextTest | Kurulum hataları, yenileme bildirimi ve kurtarma metni aynı | 2 |
| S02 | Metinler II: `ui/*.kt` içindeki metinler `stringResource`/plurals'a taşınır; Ayarlar `activeSection` bölüm kimliğine geçer | ui/*.kt (yaklaşık 20 dosya) | Cihaz testleri Türkçe beklemeye devam eder: VisualPreviewTest, SearchNavigationTest, PilotFlowTest (hedefli) | Her ekran 1,0 ve 1,5 yazıda öncekiyle aynı | 2,5 |
| S03 | Biçimlendirme, soldan sağa kilit, sözde diller, sabit metin yasağı testi | ui/Formats.kt (yeni), Locale kullanan 5 dosya, AndroidManifest (`supportsRtl=false`), ui/Theme.kt (`LocalLayoutDirection` Ltr), build.gradle.kts (debug `pseudoLocalesEnabled`) | FormatsTest, NoHardcodedUiTextTest (ui/ ve TvViewModel'de Türkçe literal arar, izin listesi testTag ve ikon adları) | ar-XB'de düzen aynalanmaz, Sol/Sağ yönü doğru; en-XA'da kesilen metinler listelenir | 1,5 |
| S04 | Ayarlar çerçevesi, **görünür değişiklik yok** | ui/SettingsScreen.kt → ui/settings/{SettingsHost, SettingsNav, SettingsRows, SettingsPages}.kt, PilotApp.kt | SettingsNavTest; cihaz: SettingsFrameworkTest (Geri bir düzey çıkar, odak açan satıra döner, 1,5/2,0 yazı), SearchNavigationTest | Mevcut alt sayfalar, Kategoriler listesi, Hakkında okuma çerçevesi | 2 |
| S05 | Kapılar ve kancalar: `request(target)`, `Features`/`AllFree`, `AppOverlay` sunucusu, `QuickAction` listesi (1,5 yazıda kayar), `appStarted`/`appStopped` | data/AccessGate.kt, data/Features.kt (yeni), TvViewModel.kt, ui/PilotApp.kt, HomeScreen.kt, LibraryScreen.kt, MainActivity.kt | AccessGateTest (yetişkin, gizli kategori, kaynak yok, bölüm bağlamı, eski UI kopyası); cihaz: PilotFlowTest, MultiSourceFlowTest, ContinueCardLayoutTest | Ana Sayfa, kütüphane, Ara, rehber, favori, devam, rakam ve CH± ile açma aynı; hızlı menü aynı | 2 |
| S06 | Oynatıcı temeli, davranış değişmez | ui/PlayerScreen.kt, ui/PlayerFactory.kt, ui/PlayerKeys.kt (yeni), TvViewModel.kt (`PlayingMedia.mode`) | PlayerKeyRouterTest (CH±, Yukarı/Aşağı, rakam 1,5 sn, rehber tuşu, basılı Sol/Sağ 10/30/60 sn, FF/RW), PlaybackRecoveryTest; cihaz: TracksLayoutTest, EpisodeAutoplayFlowTest, PlaybackRecoveryFlowTest | Kanal geçişi, sarma, ses/altyazı paneli, sonraki bölüm geri sayımı, Geri sırası | 2,5 |
| S07 | Veri temeli | data/SourceRegistry.kt, DeviceStore.kt, DeviceKeys.kt, SourceHttp.kt (yeni) | SourceRegistryTest (v1→v2, bilinmeyen alan, toString gizli kalır), DeviceKeysTest; cihaz: SourceRegistryStorageTest (v1 kaydın üstüne açılış) | 0.14.43 verisinin üstüne kurulumda kaynaklar açılır (test emülatörü) | 1,5 |

### Faz B · Ebeveyn denetimi, BKL-028 (9,5 g)

| # | İş | Başlıca dosyalar | Testler | Elle | g |
|---|---|---|---|---|---|
| S08 | PIN veri katmanı: AndroidPinClock, `PinAttempts.waitMs`, ParentalGuard (deneme rezervasyonu → ana iş parçacığı dışında KDF → kalıcı kayıt), ProtectionCodec v2, `ProtectionStore.reset` | data/AndroidPinClock.kt, ParentalGuard.kt, PinAttempts.kt, ProtectionSnapshot.kt, ProtectionStore.kt | ParentalGuardTest, ProtectionCodecTest, PinAttemptsTest; cihaz: ProtectionStoreTest, AndroidPinClockTest | KDF süresi emülatörde ölçülür | 1,5 |
| S09 | PIN penceresi ve ayarlar: PinPad/PinDialog (AppOverlay), Ayarlar → İçerik → Ebeveyn denetimi (oluştur, değiştir, kaldır), "PIN'i unuttum → Her şeyi sıfırla" (`DeviceKeys` ile), yetişkin filtresini kapatma ve Kategorileri yönet için koruma; Hakkında'daki "PIN yönetimi henüz yok" cümlesi güncellenir | ui/PinPad.kt, ui/settings/ParentalPages.kt, TvViewModel.kt, DeviceStore.kt | ParentalFlowTest (kur, değiştir, kaldır; 5 hatadan sonra 30 sn bekleme) | Rakam tuşları ve D-pad ile giriş; sıfırlama yalnız test emülatöründe | 2 |
| S10 | İçerik kilidi kararı: `AccessPolicy.evaluate`, ParentalProjection (+uzlaştırma), ParentalSession (`appStopped`'da temizlenir), kapıda NeedsPin ve bekleyen hedef; bütün açıcılar ve CH± atlama | data/ParentalProjection.kt, ParentalSession.kt, AccessTargets.kt, HomeLibrary.kt, CategoryVisibility.kt, TvViewModel.kt, ui/EpisodeEndController.kt | AccessPolicyTest, ParentalProjectionTest, ParentalSessionTest, AccessTargetsTest, HomeLibraryTest, EpisodeEndControllerTest | CH± kilitli kanalı atlar; rakamla açmada PIN sorulur | 2,5 |
| S11 | Kilit arayüzü: rozetler, vitrin ve otomatik raf dışlaması, Göster/Kilitle/Gizle (ChoicePage), hızlı menüde Kilitle, Kilitli içerikler sayfası, kilitli kanalın program adları gizli, onStop'ta kilitli oynatımdan çıkış | ui/DesignSystem.kt, HomeScreen.kt, LibraryScreen.kt, SearchScreen.kt, GuideScreen.kt, PlayerScreen.kt, ayar sayfaları | ParentalFlowTest (bütün yollar) | Home tuşundan dönünce kilit yeniden gelir | 2 |
| S12 | Kaynak ve açılış koruması: kaynak kilidi, kaynak yönetiminin bütün girişlerinde PIN (Ayarlar, SourcesScreen, kurulumda düzenleme, rehberde "Kaynakları yönet"), "Açılışta PIN iste" + AppLockScreen (`Route.LOCKED`), kayıt okunamazsa kilit ekranı, PlayerScreen ON_START koruması | SourcesScreen.kt, PilotApp.kt, PlayerScreen.kt, TvViewModel.kt, MainActivity.kt | ParentalFlowTest, LoadingLayoutTest | Arka plandan dönüşte kilit ekranından önce ses gelmez | 1,5 |

### Faz C · İki dil (3 g ve çeviri incelemesi)

| # | İş | Başlıca dosyalar | Testler | Elle | g |
|---|---|---|---|---|---|
| S13 | `values/` İngilizce, `values-tr/` Türkçe; `res/resources.properties` (`unqualifiedResLocale=en-US`) ve `generateLocaleConfig` (AGP 9.0.1 destekliyor); LocaleController (API 33+ `LocaleManager`, 26–32 kayıtlı dil + `attachBaseContext` + `recreate`); Ayarlar → Dil; çok dilli yetişkin kategori algısı; XmltvParser dil sırası ve dil değişince rehberin bayat sayılması; cihaz testlerinde `TurkishLocaleRule`; mağaza metni İngilizce taslağı | MainActivity.kt, LocaleController.kt (yeni), res/values*/strings.xml, CategoryVisibility.kt, XmltvParser.kt, GuideController.kt, build.gradle.kts, AndroidManifest | CategoryVisibilityTest (İngilizce/Almanca adlar), XmltvParserTest (dil sırası); cihaz: LocaleFlowTest; bütün Türkçe testler kuralla | İngilizce, Almanca ve Türkçe cihaz dilleri, ar-XB; dil değişince açık ekranlar yeni dilde | 3 |

### Faz D · Oynatıcının P0 eksikleri (11 g)

| # | İş | Başlıca dosyalar | Testler | Elle | g |
|---|---|---|---|---|---|
| S14 | Oynatıcı ayarları (playback aşama 1'in kalanı) ve **Ayarlar'ın iki düzeye geçmesi** (karar 4a-8) | data/PlaybackPreferences.kt, PlayerFactory.kt, PlayerScreen.kt (OSD süresi, panel saydamlığı), PlaybackRecovery.kt (kod çözücü ipucu), settings/PlayerPages.kt | PlaybackPreferencesTest, ControlsAutoHideTest, PlaybackRecoveryTest; cihaz: PlayerSettingsFlowTest, TracksLayoutTest | Arabellek, tünel ve PCM değişince yayın açılır; ses/altyazı dil hafızası bozulmaz | 2 |
| S15 | Kanal listesi, mini rehber, bilgi bandı, son kanal. **Tasarımı yok, ayrı tasarım çalıştırması gerekir.** Yer tutucu 2 adım × 2 g: `PlayerLayer.ChannelList`, `request(ChannelPick)`, son kanal `DeviceKeys` ile | PlayerScreen.kt, PlayerKeys.kt, TvViewModel.kt | Tasarıma göre | Tasarıma göre | ≈4 |
| S16 | Oynatıcı seçenekleri paneli: en-boy, altyazı stili, yayın bilgisi | ui/PlayerOptions.kt, PlayerScreen.kt, ayar alt sayfası | AspectModeTest; cihaz: PlayerOptionsLayoutTest | 440 dp panel, 1,5 yazı | 2 |
| S17 | Canlı geri sarma T0 (record aşama 0): StreamBudget, Xtream `max_connections`, Ayarlar → Bağlantı sınırları, sağlayıcının HLS penceresinde Sol/Sağ, −10/+10, Canlıya dön | ui/LiveTimeshift.kt, PlayerKeys.kt, SourceRepository.kt, SourceRegistry (`limits`), ayarlar | StreamBudgetTest, LiveWindowTest | Pencereli HLS kanalında çalışır; TS kanalda düğme görünmez | 3 |

### Faz E · Rehber ve arşiv (24 g)

| # | İş | Başlıca dosyalar | Testler | Elle | g |
|---|---|---|---|---|---|
| S18 | Bağlantı ayarları (organize aşama 1): `connection` alanı, RequestHeaders; liste, EPG (host kapsamıyla) ve oynatmada (PlaybackRequest) kullanım; User-Agent D066 biçimine eşitlenir; Kurulum → Bağlantı ayarları penceresi | Models.kt, SourceRegistry.kt, SourceHttp.kt, SourceRepository.kt, EpgRepository.kt, PlayerFactory.kt, ui/ConnectionDialog.kt, PilotApp.kt, SourcesScreen.kt | RequestHeadersTest ve SourceTest (MockWebServer) | Sentetik sunucuda özel UA ile liste yenilenir | 1,5 |
| S19 | EPG I: `guides` alanı (en çok 4, sıra = öncelik), EpgPlan genişlemesi, şema v2 (feeds, titles, aliases.folded, title_id + dizin), eski rehberlerin bayat sayılması ve bu arada eski sorgu yolu | Models.kt, SourceRegistry.kt, EpgModels.kt, EpgStore.kt, EpgRepository.kt, data/GuideMatching.kt, GuideController.kt | EpgSourceTest, XmltvParserTest; cihaz: EpgStorageTest (v1→v2) | Rehber bir kez yeniden iner; o sırada eski programlar görünür | 3 |
| S20 | EPG II: adres başına sonuç, alınamayan adresin önceki verisinin taşınması, öncelikli eşleşme (karar 4b), Rehber ayarları → adresler; `multipleFeedsDoNotSilentlyMergeConflictingChannelIds` testi yeniden yazılır; EPG.md ve karar kaydı | GuideController.kt, EpgStore.kt, settings/GuidePages.kt, ui/GuideSettings.kt, SourcesScreen.kt | EpgSourceTest; cihaz: GuideFlowTest | Bir adres düşünce diğerleri yayımlanır | 2 |
| S21 | EPG saat kaydırma (`guide:<id>` anahtarı, `DeviceKeys` ile) | data/GuideTuning.kt, EpgStore.kt (sorgu), GuideController.kt, StepperRow | GuideTuningTest; cihaz: GuideFlowTest | −30/+30 önizleme, yeniden indirme yok | 1,5 |
| S22 | Catch-up veri katmanı: CatchupSpec, katalog önbelleği v4→v5, `server_info.timezone`, Catchup, ArchiveTimeline | Models.kt, CatalogParser.kt, CatalogSnapshot.kt, SourceRepository.kt, data/Catchup.kt, ArchiveTimeline.kt | CatchupParserTest, CatchupUrlTest, CatchupStateTest, ArchiveTimelineTest, CatalogSnapshotTest | Görünür değişiklik yok | 2 |
| S23 | Catch-up rehber penceresi: arşivli kanallarda 7 güne kadar geçmiş (kaydırılmış zamanla) ve kırpma, kanal/hücre işaretleri, ayrıntı penceresi varyantları | EpgModels.kt (`GuideWindow.around`), EpgStore.kt, GuideController.kt, GuideScreen.kt, DesignSystem.kt | GuideTimelineTest, EpgSourceTest; cihaz: GuideFlowTest | Geçmiş günler yalnız arşivli kanallarda | 2 |
| S24 | Catch-up oynatıcı kipi: GEÇMİŞ YAYIN (`PlaybackMode.ARCHIVE`), yeniden açmayla sarma (tek bağlantı), ArchiveEndDialog (`PlayerLayer`), `request(Archive)` + `Feature.CATCHUP` | PlayerScreen.kt, PlayerKeys.kt, ui/ArchiveEndDialog.kt, PlaybackRecovery.kt, TvViewModel.kt | PlaybackRecoveryTest; cihaz: CatchupFlowTest (sentetik arşiv) | Program sonunda kart çıkar; Geri rehbere döner | 1,5 |
| S25 | Baştan izle, 90 sn'de canlıya geçiş, timeshift.php ve Flussonic yedekleri; rehberde basılı OK ile ayrıntı | PlayerScreen.kt, PlayerKeys.kt, GuideScreen.kt, Catchup.kt | CatchupUrlTest; cihaz: CatchupFlowTest | T0 ile birlikteyken öncelik kuralı (§3) | 1,5 |
| S26 | Elle eşleme I: MANUAL eşleşme türleri, aday araması, Rehber eşlemesi penceresi, ızgarada basılı OK ile Kanal seçenekleri | GuideMatching.kt, GuideController.kt, ui/GuideRepair.kt, GuideScreen.kt | GuideMatchingTest; cihaz: GuideFlowTest | Basılı OK menüyü açar | 2 |
| S27 | Elle eşleme II: Canlı TV ve ana sayfa hızlı menüsü, Kanal eşleşmeleri sayfası, eşleşme özeti, boş durum metinleri | LibraryScreen.kt, HomeScreen.kt, settings/GuidePages.kt | GuideFlowTest (1,5 yazı, TalkBack eylemi) | Rapor süresi | 2 |
| S28 | Ara'da program araması | EpgStore.kt (titles üzerinde LIKE), GuideController.kt, TvViewModel.kt, SearchScreen.kt | ProgrammeSearchTest; cihaz: SearchNavigationTest | Programlar satırı, Rehberde göster, odak dönüşü | 2,5 |
| S29 | Hatırlatıcı: T6 zamanlayıcısı, kurma ve kaldırma, çakışma penceresi, kalıcılık (`DeviceKeys`), ReminderAlert (AppOverlay), oynatıcı notu, Ayarlar → Hatırlatıcılar, `Feature.REMINDERS` | data/Reminders.kt, ui/ReminderAlert.kt, TvViewModel.kt, GuideScreen.kt, PlayerScreen.kt | RemindersTest, ReminderSchedulerTest; cihaz: ReminderFlowTest | "Yalnız NEXTV açıkken uyarır" metni görünür | 2,5 |

### Faz F · Kütüphane düzeni (10 g)

| # | İş | Başlıca dosyalar | Testler | g |
|---|---|---|---|---|
| S30 | Otomatik güncelleme: T6 üzerinde zamanlayıcı (ön planda, oynatma/düzenleme/işlem yokken); `restore` ve `GuideController.stale` içindeki sabit 6 saat aralık ayarına bağlanır | data/RefreshSchedule.kt, TvViewModel.kt, GuideController.kt, ayarlar | RefreshScheduleTest | 1,5 |
| S31 | `library:v1` temeli, tek favori listesinin "Favoriler" grubuna taşınması, CustomIndex, LibraryViews, channelContext (ParentalProjection ile birlikte), displayName | data/LibraryCustomizations.kt, CustomIndex.kt, CustomOrder.kt, DeviceStore.kt, DeviceKeys.kt, FavoriteTargets.kt, SourceLibrary.kt, HomeLibrary.kt, TvViewModel.kt | LibraryCustomizationsTest, CustomIndexTest, FavoriteTargetsTest, SourceLibraryTest; elle: tek grupla her şey aynı | 2 |
| S32 | Tek içerik gizleme ve yeniden adlandırma (`Feature.RENAME`), QuickContext, Gizlenen içerikler | LibraryScreen.kt, ui/LibraryEditDialogs.kt, TitleIndex.kt | TitleIndexTest (iki adla arama); cihaz: hızlı menü (7 satır, 1,5 yazıda kaydırma) | 1,5 |
| S33 | Favori grupları (ikinci gruptan itibaren `Feature.FAVORITE_GROUPS`) | ui/FavoriteGroupsDialog.kt, LibraryScreen.kt, HomeScreen.kt | JVM ve cihaz | 2 |
| S34 | Taşıma kipi (`Feature.CUSTOM_ORDER`), kategori aralığında numaralandırma, Sıralamayı ve adları sıfırla, dengeleme | LibraryScreen.kt, TvViewModel.kt, GuideScreen.kt | CustomOrderTest; cihaz: MoveModeTest (KeyDown/KeyUp tüketimi, Sol ile kategori sütunu çakışması) | 3 |

### Faz G · NEXTV Plus (5,5 g; sahibin ön koşullarına bağlı)

| # | İş | Başlıca dosyalar | Testler | g |
|---|---|---|---|---|
| S35 | Billing çekirdeği, satış kapalı: StoreBilling arayüzü, PlayStoreBilling ve FakeBilling, PlusController (onay, bekleyen ödeme, iade sonrası hakkın düşmesi), `plus_v1` şifreli önbellek (`DeviceStore.seal`, bağlam "plus-v1", yedek sınıfı NONE), `PlusEntitlements(saleOpen = BuildConfig.PLUS_SALE_OPEN = false)`. Play Billing sürümü uygulama günü resmî notlardan doğrulanır (tasarımda 9.1.0). | build.gradle.kts, billing/StoreBilling.kt, data/PlusController.kt, DeviceStore.kt, DeviceKeys.kt | PlusControllerTest (sahte mağaza: satın alma, bekleyen, 3 günlük onay, iade, çevrimdışı) | 2 |
| S36 | Plus sayfası ve "Satın alımı geri yükle": fiyat `formattedPrice` ile; Play yoksa "Satın alma şu an kullanılamıyor"; iç test kanalında lisans test kullanıcılarıyla deneme | settings/PlusPages.kt, TvViewModel.kt, MainActivity.kt | Cihaz: PlusPageTest (FakeBilling); Play iç testi | 2 |
| S37 | Satış açılışı: `PLUS_SALE_OPEN`, Feature kapıları, PlusLocked penceresi (AppOverlay), iade sonrası veri korunur ama düzenleme kilitlenir; Console'da ülke fiyatları ve açılış indirimi; gizlilik politikasına ödeme paragrafı; fiyat kuralı kararı (ebeveyn özellikleri her yerde ücretsiz) | Features.kt, TvViewModel.kt, PilotApp.kt, DECISIONS.md, PLAY_STORE_READINESS.md | AccessGateTest (NeedsPlus), EntitlementsTest; cihaz: PlusGateFlowTest | 1,5 |

### Faz H · Ana ekran ve yedekleme (13 g)

| # | İş | Başlıca dosyalar | Testler | g |
|---|---|---|---|---|
| S38 | Watch Next planlayıcısı ve deposu: CONTINUE ve sırası kesin NEXT girişleri; kilitli, yetişkin ve gizli içerik hariç | build.gradle.kts (tvprovider 1.1.0), data/home/WatchNextPlanner.kt, WatchNextStore.kt | WatchNextPlannerTest | 1,5 |
| S39 | Yayıncı, salt okunur ArtworkProvider (tahmin edilemez jeton), kaldırma alıcısı, Ayarlar → Ana ekran | data/home/*, AndroidManifest | Cihaz: WatchNextPublisherTest (Google TV emülatörü) | 2 |
| S40 | `nextv://watchnext` derin bağlantısı: singleTask + onNewIntent → `request(Resume)` | MainActivity.kt, AndroidManifest, TvViewModel.kt | WatchNextDeepLinkTest, PilotFlowTest (çıkış sorusu) | 2 |
| S41 | Yedek çekirdeği: BackupCrypto, BackupCodec (.nextv), BackupMerge (koruma hiçbir durumda gevşemez, Plus hakkı yedeğe girmez, `DeviceKeys` sınıfları) | data/backup/* | BackupCryptoTest, BackupCodecTest, BackupMergeTest | 2,5 |
| S42 | Geri yükleme: tek commit ve kaynakların sırayla yeniden indirilmesi; giriş Ayarlar ve Kurulum'dan (kurtarma ve PIN sıfırlaması sonrası dahil) | TvViewModel.kt, DeviceStore.kt, ui/BackupScreen.kt, PilotApp.kt, LoadingScreen.kt | BackupRestoreFlowTest (yalnız test emülatöründe) | 1,5 |
| S43 | Yerel ağ ve QR aktarımı; sunucu `appStopped`'da kapanır | data/backup/LocalTransferServer.kt, TransferPage.kt, NetworkAddress.kt, ui/QrCode.kt, build.gradle.kts (qrcodegen 1.8.0) | LocalTransferServerTest (loopback) | 2 |
| S44 | USB/SAF yolu (destekleyen cihazda) ve belgeler | ui/BackupScreen.kt | Cihaz | 1,5 |

### Faz I · Oynatıcının kalanları (8 g)

| # | İş | Başlıca dosyalar | Testler | g |
|---|---|---|---|---|
| S45 | Harici oynatıcı: kaynak başına bir kez uyarı; önce kendi oynatıcı durur, sonra harici başlar | ui/ExternalPlayer.kt, AndroidManifest (`<queries>`), PlayerScreen.kt | StreamExposureTest; cihaz: ExternalPlayerFlowTest | 2 |
| S46 | Resim içinde resim (yalnız FEATURE varsa) | AndroidManifest (supportsPictureInPicture, configChanges), MainActivity.kt, ui/PictureInPicture.kt, PlayerScreen.kt, res/drawable/ic_pip_* | PipAspectTest; cihaz: PictureInPictureFlowTest + configChanges sonrası bütün yerleşim testleri | 2 |
| S47 | Ses gecikmesi (deneysel) | PlayerFactory.kt (ForwardingAudioSink), PlayerOptions.kt | PlayerAudioSinkTest | 1,5 |
| S48 | AFR "Her zaman" | ui/FrameRateMatching.kt, PlayerScreen.kt | FrameRateEstimatorTest, DisplayModePickerTest; anlamlı doğrulama yalnız cihaz turunda | 2,5 |

### Faz J · Kayıt ailesi (39 g; her alt adım 3 günü geçmez)

| # | İş | g |
|---|---|---|
| S49 | Git/geç kapısı: Media3'ün büyüyen yerel EVENT listesini oynattığının cihaz testiyle doğrulanması. Olmazsa Faz J yeniden tasarlanır. | 1 |
| S50–S52 | TsSegmenter; HlsPlaylistReader ve HlsCapture; LocalPlaylist ve CaptureHub (`capture/*`, JVM ve MockWebServer) | 2 + 2 + 2 |
| S53 | RecordingService (dataSync) ve şifreli RecordingStore; adres ve kimlik bilgisi diske yazılmaz (AndroidManifest, `capture/`) | 3 |
| S54 | Kaydet / Kaydı durdur, Kayıtlar ekranı (`Route.RECORDINGS`), `Feature.RECORDING` | 3 |
| S55 | Depolama ve kota | 1 |
| S56–S57 | "Duraklatınca" arabelleği (TS ve penceresiz HLS), aynı kanalı kayıttan izleme (tek bağlantı), geri sarmayı kayda yükseltme | 2,5 + 2,5 |
| S58 | USB hedefi | 3 |
| S59–S60 | Zamanlanmış kayıt: rehberde Kaydı planla, exact alarm, açılış ve saat alıcıları, çakışma | 3 + 3 |
| S61–S63 | Çoklu ekran (2–4): `Feature.MULTI_VIEW`, uyarıda ilk odak Vazgeç, karo başına bütçe, cihaz kademesi | 3 + 2 + 2 |
| S64–S65 | "Her zaman" arabelleği (ölçüm kapısıyla), elle zamanlı kayıt, EPG kaymasında planın taşınması, cihaz matrisi | 2 + 2 |

### Fiziksel cihaz turları

Bunlar yalnız kullanıcı isterse yapılır; TV'nin açık olduğu varsayılmaz ve sıfırlama yapılmaz.

- **K1 (S12'den sonra, ≈1 g):**
  - PIN KDF süresi: 600.000 tur ve iki Keystore yazımı; 1–4 sn bekleniyor.
  - `BOOT_COUNT` okunabiliyor mu.
  - PIN akışları, 1,0/1,5/2,0 yazı ve TalkBack.
  - PIN aşama 4 belgeleri.
- **K2 (S17'den sonra):**
  - Tünel, PCM, kesintisiz AFR ve T0 geri sarma.
  - BKL-010 codec matrisi ve BKL-013 büyük liste p95 ölçümüyle birleştirilir.
- **K3 (S29'dan sonra):**
  - Fiziksel kumandada basılı OK algısı.
  - Gerçek sağlayıcıda catch-up (tek oturum, ham veri `.private` altında).
  - Program araması süresi.
- **K4 (S44'ten sonra):**
  - 65U95A'da Watch Next görünürlüğü.
  - Yedek PBKDF2 süresi.
  - Misafir ağında aktarım.
- **K5 (S48'den sonra):** 24/25/50 Hz ekran modları ve PiP desteği.
- **K6 (Faz J içinde):** çözücü sayısı, bekleme modunda zamanlı kayıt, USB dosya sistemleri.

### Kilometre taşları (birikimli mühendis-günü)

| Taş | Adımlar | Birikimli g | İçerik |
|---|---|---|---|
| M1 | A + B | 23,5 | PIN; Play raporundaki 6. madde |
| M2 | + C | 26,5 | İki dil. Sahibin ön koşulları hazırsa 12 test kullanıcısı × 14 günlük kapalı test burada başlayabilir. |
| M3 | + D + E | 61,5 | Oynatıcı, rehber ve arşiv |
| M4 | + F | 71,5 | Kütüphane düzeni |
| M5 | + G | 77 | Satış açılabilir |
| — | Sonrası | +60 | H, I, J |

- **Toplam:** yaklaşık 137 g ve yaklaşık 3 g cihaz turu. Küme tasarımlarının toplamı 128,5 g; artı kanal listesi için yaklaşık 4 g ve temeller için yaklaşık 5 g ek.
- **Satışı öne çekmek istenirse:** S13'ten sonra şu sıra izlenebilir: S22 → S24–S25 (yalnız Baştan izle ve canlıdan arşiv) → S31 → S33 → S32 → S35–S37. Satış yaklaşık 43. günde açılır.
  - Bedeli: EPG ve oynatıcıdaki P0 işler yaklaşık 5 hafta kayar.
  - Rehberden geçmiş yayın (S23) sonradan şema v2'ye taşınır; yaklaşık 1 g ek iş.

---

## 3. Dosya çakışmaları: birlikte yürümemesi gereken adımlar

1. **S01–S03 tek başına yürür.** Bütün `ui/` dosyalarına ve `strings.xml`'e dokunur; metin değişiklikleri o süre boyunca dondurulur. Bunlar bitmeden hiçbir özellik adımı başlamaz.
2. **S04 (Ayarlar çerçevesi)**, ayar sayfası ekleyen bütün adımlardan önce bitmelidir. Sonrasında her küme kendi sayfa dosyasını yazar; çakışma yalnız `SettingsPages.kt` kayıt listesinde kalır.
3. **S06 (`PlayerScreen.kt`)**, oynatıcıya dokunan şu adımlardan önce gelir: S11, S12, S14–S17, S24, S25, S29, S45–S48, S54, S56, S61. Bu adımlar kendi aralarında da sırayla yürür.
4. **S05 ve S10 (kapı)**, açıcı ekleyen adımlardan önce gelir: S15, S24, S28, S29, S40, S61.
5. **EpgStore / GuideController / EpgModels:** S19 → S20 → S21 → S23 → S26 → S27 → S28 → S30 kesin sıralıdır. S23, S21'den sonra gelir ki arşiv baştan kaydırılmış zamanı kullansın.
6. **Kayıt codec'i ve kurulum ekranı:** S07 → S17 (`limits`) → S18 (`connection`) → S19 (`guides`) sıralıdır.
7. **`MainActivity.kt` ve `AndroidManifest.xml`:** S03, S05, S12, S13, S39, S40, S45, S46, S53, S59 sıralıdır. S46'daki configChanges, S13'teki `recreate` ile dil değiştirme davranışını yeniden sınamayı gerektirir.
8. **`changeChannel`, `numberedChannels` ve HomeLibrary:** S10/S11 (kilitli atlama) S31'den önce gelir. Sıra organize'den, süzme ParentalProjection'dan gelir.
9. **Canlı sarma anlamı (`PlayerKeyRouter`):** S17 (T0), S25 (Baştan izle) ve S56 (arabellek) sıralıdır. Öncelik kuralı:
   - Sağlayıcı penceresi varsa Sol/Sağ pencere içinde sarar.
   - "Baştan izle" yalnız CatchupSpec varsa görünür.
   - Arabellek yalnız pencere yokken devreye girer.
10. **`build.gradle.kts`:** S03, S13, S35, S38, S43 ve TV teslimleri (sürüm kodu) aynı dosyaya dokunur. Çakışma küçüktür ama aynı anda yürümez.

**İkinci bir mühendis olursa ayrı worktree'de güvenle yürüyebilecekler:**
- S41 yedek çekirdeği (yalnız JVM, `data/backup/`).
- S50–S52 yakalama parçaları (`capture/`).
- S22 catch-up ayrıştırma; S19–S21 ile yalnız `Models.kt` paylaşılır.
- S35 billing çekirdeği.

---

## 4. Sahip kararları (birleştirilmiş)

### 4a. Sahibin vermesi gerekenler

| # | Karar | Varsayılan (cevap gelmezse) | Gerektiği adım |
|---|---|---|---|
| 1 | Gelir modeli | Çekirdek ücretsiz; tek seferlik ömür boyu Plus; reklam yok; abonelik yalnız sunucu gerektiren özellik gelirse; satış yalnız Play'den; bugünkü özellikler ücretliye geçmez | S35 |
| 2 | Ücretsiz/Plus sınırı ve satış zamanı (pin, playback, epg, catchup, organize, backup ve record tasarımlarındaki ücret soruları burada birleşti) | §0'daki birleşik liste. Satış en az üç Plus özelliğinden sonra açılır (varsayılan sırada S34 sonrası); öne çekme seçeneği §2'de | S05 (Feature listesi), S37 |
| 3 | Plus fiyatı | 14,99 € / 14,99 $ / 12,99 £ / 299,99 TL vergi dahil; 30 gün 9,99 / 9,99 / 8,99 / 199,99; diğer ülkelerde Play çevirisi; TL 6 ayda bir gözden geçirilir | S37 |
| 4 | Satış ülkeleri ve kanallar | Play'in ücretli içerik desteklediği bütün ülkeler, avukatın sakıncalı bulduğu ülkeler hariç; yan yükleme, web satışı ve aktivasyon kodu yok; Fire TV ikinci faz | S37 |
| 5 | Diller ve tanımsız dil (rehber dili ve telefon yedek sayfası dili dahil) | Türkçe ve İngilizce; tanımsız dilde İngilizce; Ayarlar'da Dil seçimi; Almanca ikinci dalga; sağdan sola dil yok, düzen kilitli; makine çevirisi yok; rehber dili sırası uygulama → cihaz → etiketsiz → herhangi; yedek sayfası `Accept-Language` başlığına göre Türkçe/İngilizce | S03, S13, S43 |
| 6 | PIN unutulursa ne olacak | Uygulamada "PIN'i unuttum → Her şeyi sıfırla": kaynaklar, favoriler, geçmiş ve kilitler silinir; yetişkin filtresi açık kalır; kurulum açılır. Yedekleme geldikten sonra geri yükleme önerilir. | S09 |
| 7 | D068'deki "Kategorileri yönet" satırı Göster/Kilitle/Gizle penceresine dönüşsün mü; PIN varken sayfa PIN istesin mi | Evet, ikisi de | S11 |
| 8 | Ayarlar iki düzeyli olsun mu (SETTINGS_SECTIONS'taki tek liste sözleşmesi değişir) | Evet. S04'te görünür değişiklik yok; S14'te bölüm sayfalarına geçilir: Genel ve dil, Kaynaklar, İçerik ve ebeveyn denetimi, Oynatıcı, Rehber, Kütüphane, Yedekleme ve ana ekran, Kayıtlar, NEXTV Plus, Hakkında. Görsel kabul sahipte. | S14 |
| 9 | Eski Watch Next API'sinin Google TV desteği 2027'nin ikinci yarısında bitiyor; Engage SDK'ya geçilsin mi | Şimdi kayıt yok; eski API ile çıkılır; 2027'nin ilk çeyreğinde hesap ve gelir kararıyla yeniden değerlendirilir | S38 öncesi |
| 10 | Enstrümantasyon için ayrı, boş bir emülatör kullanılsın mı | Evet (ör. `nextv-test`). Onay gelene kadar cihaz testleri "koşulmadı" diye raporlanır. | S01 |

### 4b. İtiraz gelmezse varsayılanla ilerlenecek tasarım kararları

| Konu | Varsayılan | Adım |
|---|---|---|
| PIN ile açılan kilit ne kadar açık kalır | onStop'a kadar; elle "Açılan kilitleri kapat"; süre sınırı yok; izin yalnız hedefi kilitleyen kapsamlar için | S10 |
| PIN tanımlıyken varsayılan korumalar | Kaynak yönetimi PIN ister: Açık. Açılışta PIN: Kapalı. Kilitli içerik rozetli görünür, vitrinde ve otomatik raflarda yer almaz, kilitli kanalın program adları gizlenir. | S11–S12 |
| Aynı kanal kimliği birden çok rehberde | Kullanıcının adres sırasına göre, o kanal için programı olan ilk rehber; belirsiz ad eşleşmesi hiçbir zaman atanmaz | S20 |
| Arşivi olan bitmiş programda OK | D039 korunur: OK ayrıntıyı açar, Aşağı + OK geçmiş yayını başlatır. Yayındaki programda kısa OK canlıyı, basılı OK ayrıntıyı açar. | S23, S25 |
| Uygulama kapalıyken hatırlatıcı bildirimi | v1'de yok | S29 |
| Ses gecikmesi | "Deneysel" etiketiyle, −500…+500 ms, tünel açıkken gizli | S47 |
| Harici oynatıcı uyarısı | Kaynak başına bir kez; Ayarlar'dan sıfırlanabilir | S45 |
| Ana ekran tuşuyla otomatik PiP | Kapalı (TV-NP kalite kuralı) | S46 |
| En-boy hafızası | Bütün yayınlar için tek varsayılan | S16 |
| WorkManager ile arka plan güncellemesi | Yok; yalnız ön planda; varsayılan aralık 6 saat | S30 |
| Hazır User-Agent seçenekleri (VLC, Kodi gibi) | Yok; "Varsayılan (NEXTV)" ve serbest metin | S18 |
| Özel sırada kanal numaraları | Yalnız o kategorinin numara aralığında yeniden sıralanır | S31, S34 |
| Watch Next varsayılanı | Açık; yetişkin, gizli ve PIN'li içerik yayımlanmaz | S39 |
| Ana ekranda uygulama kanalı | v1'de yok | Ertelendi |
| Kayıt yeri ve ömrü | NEXTV'ye özel klasör (cihaz veya USB); uygulamayla silinir; dışa aktarma ve ek şifreleme yok | S53, S58 |
| Android 14+ exact alarm izni | İlk planlamada bir kez açıklamayla istenir; reddedilirse yalnız uygulama açıkken | S59 |
| Canlı geri sarma varsayılanı | "Duraklatınca"; "Her zaman" yalnız ölçüm geçerse | S56, S64 |
| User-Agent'ın eşitlenmesi | Liste ve EPG istekleri D066'daki "NEXTV/<sürüm> AndroidTV" biçimine geçer (koddaki sapmanın düzeltilmesi) | S18 |

### 4c. Sahibin eylemleri (karar değil, ön koşul)

- **Marka:** Play mağazasında "NexTv" adlı bir rakip var. Marka araştırması ve ad kararı S37'den önce gerekiyor; Plus adı da buna bağlı.
- **Paket ve imza:**
  - Kalıcı paket adı, upload anahtarı ve Play App Signing seçimi S36'dan önce gerekiyor.
  - Debug imzadan yeni imzaya geçişte TV'deki kurulum bir kez kaldırılır ve yerel veriler silinir.
- **Hesap ve ödeme:**
  - Geliştirici hesabı türü (bireysel ya da şirket; AB'de tacir adresi görünürlüğü) ve ödeme profili: S36.
  - Console'da `plus_lifetime` ürünü ve ülke fiyatları: S37.
- **Yasal metinler:**
  - Ödeme paragrafı içeren gizlilik politikası URL'si.
  - Türkçe/İngilizce kullanım koşulları.
  - S37'den önce.
- **Danışmanlık:**
  - Mali müşavir: GVK 20/B istisnasının TV uygulamasını kapsayıp kapsamadığı, tevkifat.
  - Avukat: mesafeli sözleşme ve cayma istisnası, satış ülkeleri.
  - S37'den önce.
- **Çeviri:** İngilizce metinlerin incelenmesi (S13).
- **Kapalı test:**
  - App access için yasal bir test kaynağı.
  - 12 test kullanıcısı × 14 gün; M2'den sonra başlayabilir.
  - Açılış sesinin lisans belgesi.

---

## 5. Ertelenen ve reddedilenler

| Konu | Neden |
|---|---|
| Stalker portalı | PRD kapsam dışı; MAC tabanlı portallar satıcı ekosistemiyle özdeş ve hukuki ve algı riski taşıyor (Play raporu 3.2). Reddedildi. |
| Nihai iş modeli | Strateji araştırması ayrı yürüyor (HANDOFF). Bu plandaki model, fiyat ve sınır önerilerdir; nihai karar sahipte (4a-1…4). |
| Kanal listesi, mini rehber, bilgi bandı, son kanal | Tasarım üretilmedi. S15 yer tutucusu; ayrı tasarım çalıştırması gerekiyor. |
| Fiziksel TV gerektirenler | KDF ve PBKDF2 süreleri, `BOOT_COUNT`, AFR "Her zaman", PiP desteği, basılı OK, Watch Next görünürlüğü, çoklu ekran çözücüleri, bekleme modunda kayıt, büyük liste p95 (BKL-010/013). K1–K6 turlarında ve yalnız kullanıcı isteyince. |
| Abonelik, senkronizasyon, QR eşleştirme (EXT01/EXT05) | Sunucu gerektiriyor; ilk ürün sunucusuz. Gelirse abonelik gerekçesi olur. |
| Uygulama kapalıyken hatırlatıcı bildirimi, Xtream `get_simple_data_table` yedeği | Catch-up aşama 5'e bağlı ve koşullu; ertelendi. |
| Engage SDK ve ana ekran uygulama kanalı | 4a-9 ve 4b; 2027'nin ilk çeyreğinde yeniden. |
| Almanca, Fire TV / Amazon IAP | İkinci dalga ve ikinci faz. |
| Sağdan sola diller | Ayrı tasarım gerektiriyor; düzen kilitli. |
| Film ve dizi indirme ("İndir"), şifreli yayın kaydı, DRM aşma | Hukuki risk ve Play politikası; reddedildi. |
| Debrid ve içerik bulma entegrasyonları | Telif ve Play riski (TIVIMATE_GAP); reddedildi. |
| Başka ürünleri taklit eden hazır User-Agent seçenekleri | Marka ve "kısıtlama aşma" algısı; reddedildi. |
| Yan yükleme ve web satışı, etkinleştirme kodu | Play raporu 3.2; ilk aşamada reddedildi. |
| "Xtream Codes" etiketinin değişmesi | Play raporundaki sahip kararı. S02'de etiket değişmez; karar verilirse tek string değişir. |
| Kalite çeşitlerini birleştirme (D065'te açık) | TIVIMATE_GAP satırlarında yok; bu planın dışında. |

**Doğrulanamayanlar:**
- Play Billing Library'nin güncel sürümü ve ücret yapısı.
- Google'ın AB, Birleşik Krallık ve Türkiye'deki vergi sorumluluğu (satış tasarımındaki Play yardım bağlantılarına dayanıyor).
- TiviMate fiyatları (yalnız üçüncü taraf kaynak).
- 65U95A'nın Google TV mi Android TV mi olduğu.
- `qrcodegen 1.8.0` ve `tvprovider 1.1.0` sürümleri uygulama günü resmî kaynaklardan yeniden doğrulanmalı.