# BKL-047 · NEXTV hesabı ve platformlar arası Premium: uygulama planı

22 Ekim 2026 · BKL-047 uygulama planı. Plan, kod haritası, mevcut S35–S37 tasarımı ve güncel sağlayıcı belgeleriyle hazırlandı; iki ayrı açıdan (kod/TV arayüzü; uyum/güvenlik) eleştirildi ve düzeltildi. Kullanıcı kararı yalnız D111 ve D112'dir. Bunların dışındaki her model, sağlayıcı, sayı, ad, ekran kimliği, sınıf adı ve sıra **öneridir**. Kod değişmedi. Yollar depo köküne göredir; `<tv>/` = `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/`. Gerekçe ve kaynaklar [SALES_CHANNELS](../../product/SALES_CHANNELS.md), yayın engelleri [LAUNCH_AUDIT](../../project/LAUNCH_AUDIT.md) içinde.

> **22 Ekim 2026 · D113:** Gelir modeli 7 gün ücretsiz deneme ve Google Play aboneliği oldu. Bu plan tek seferlik ürüne göre yazıldı. Şu kısımlar hesap işi başlamadan aboneliğe göre güncellenmeli:
> - `nextv_lifetime` ve non-consumable,
> - RevenueCat'te lifetime yetkisi,
> - mağaza okuyucusu,
> - iade akışları.
>
> Hesabın ilk yayında mı (A), Apple sürümünden önce mi (B) geleceği açık bir sorudur: [SUBSCRIPTION](../../product/SUBSCRIPTION.md) §1. Abonelik işinin kendisi o belgededir.

## Kısaca: teknik olmayan özet

**Müşteri ne yaşar?**
1. NEXTV'yi Google Play'den ücretsiz indirir. Hesap ya da kayıt yok.
2. Premium isterse Ayarlar → Premium'dan Google Play ile öder. Hesap açması gerekmez.
3. Ödemeden sonra bir kez sorulur: "Premium'u diğer cihazlarında da kullanmak için hesabına bağla: Şimdi / Sonra."
4. "Şimdi" derse TV'de bir QR kod çıkar. Telefonla okutur ve Google ya da Apple hesabıyla giriş yapar. Kumandayla şifre yazmaz.
5. Başka bir TV'de, ileride Apple TV'de, aynı yolla giriş yapınca Premium açılır. Tekrar ödemez.
6. Hesap bağlamayan kişi için her şey bugünkü gibi: aynı Google hesabındaki TV'lerde Premium kendiliğinden gelir.
7. İade alırsa Premium kapanır. Hesabını web sitesinden silebilir.

**Arkada ne çalışır?**
- **Google Play** ödemeyi alır.
- **Satın alma takip servisi** (öneri: RevenueCat) ödemenin gerçek olduğunu denetler. Aylık 2.500 $ gelire kadar ücretsiz.
- **Giriş servisi** (öneri: Auth0) QR ile girişi sağlar. 25.000 aktif kullanıcıya kadar ücretsiz.
- **Küçük NEXTV sunucusu** iki liste tutar: hangi satın almanın hangi hesaba ait olduğu ve hesabın cihazları. Kanal listeleri, IPTV şifreleri ve izleme geçmişi buraya hiç gitmez.
- **Güvenlik ağı:** NEXTV sunucusu çökse bile, Play'den alınan Premium aynı Google hesabındaki TV'lerde çalışmaya devam eder. Sunucu yalnız Premium'u başka platforma taşımak için gerekir.

**İş sırası:**
1. **Hazırlık (sen):** marka ve alan adı, Play geliştirici hesabı, mali müşavir. Ayrıca RevenueCat ve Auth0'a "IPTV oynatıcıyı kabul ediyor musunuz" sorusu.
2. **Altyapı (kod):** satın alma ve giriş kodu yazılır. Gizli anahtarlar kapalı kalır; müşteri hiçbir şey görmez.
3. **Ekranlar ve sunucu (kod + sen):** Premium sayfası, QR giriş ekranı, web'deki hesap sayfası.
4. **Deneme ve açılış:** İki gerçek TV'de denenir: TV A'da satın al, TV B'de aç. Sonra Play'de önce küçük bir kullanıcı grubuna, adım adım açılır.

Tahmin yaklaşık 31 iş günü kod (ölçülmedi), artı senin işlerin.

**Senden beklenen ilk dört karar** (ayrıntı §2.2'de; numaralar o tablonundur):
- **1 · Satın alma takibi:** RevenueCat mi (öneri; hazır, ücretsiz başlar), yoksa kendimiz mi yazalım? Kendimiz yazarsak yaklaşık 4–5 gün ek iş.
- **2 · Giriş servisi:** Auth0 mu (öneri)?
- **4 · Giriş seçenekleri:** Google ve Apple ile giriş mi (öneri)? Apple ile giriş için Apple geliştirici üyeliği gerekir, yıllık 99 $.
- **10 · Verilerin tutulacağı yer:** Avrupa mı (öneri)?

Tablodaki diğer maddelerin çoğu teknik ayrıntıdır ve öneriyle ilerler. Fiyat, paket adı (Premium mi Plus mı) ve cihaz sınırı (öneri: hesap başına 5) satış açılmadan önce senin kararındır.

## Teknik özet

Play alımı hesapsız yapılır ve aynı mağazada hesapsız açılır. İsteğe bağlı NEXTV hesabı, alımı Play `purchaseToken`'ıyla sunucudaki bağlama tablosuna yazar. Hesapla giriş yapılan başka bir cihaz, imzalı ve süreli bir hak belgesiyle Premium'u açar. Hesap ve satış aynı sürümde yayımlanır. İki ayrı derleme bayrağı birlikte açılır (D112-3).

**Tahmini iş:** yaklaşık 31,5 g kod (ölçülmedi) ve sahip işleri. PLAN Faz G'deki 5,5 günlük tahminin yerine geçer (öneri).

## 1. Amaç, kapsam ve sınırlar

**Amaç:** D111. Play'de alınan Premium, NEXTV hesabıyla giriş yapılan başka bir cihazda yeniden ödeme olmadan açılır. Model D112'dir.

**Kapsam:**
- TV'de isteğe bağlı hesap girişi (QR ve kod), çıkış, cihaz listesi ve cihaz sınırı. Başka bir cihazı çıkarmak ve giriş yöntemi eklemek web hesap merkezinden yapılır.
- Play alımını `purchaseToken` ile hesaba bağlama, hesap hakkını cihaza taşıma, iade ve geri alma, onaylanmamış alımı kurtarma.
- Belirteç ve hak önbelleği için PIN ve kaynak sıfırlamalarından etkilenmeyen depolama.
- `Entitlements` uygulaması: hak ya mağaza kaydından **ya da** hesaptan gelen imzalı belgeden gelir.
- Sunucu: kimlik sağlayıcı kiracısı, hak servisi, ince NEXTV API, web hesap merkezi ve silme sayfası. Hazırlık ve üretim ortamları ayrıdır.
- Kabul testi: TV A'da Play'den al → TV B'de (başka bir Android TV) giriş yap → Premium açılır.

**Kapsam dışı:**
- Premium özelliklerinin kendisi: catch-up, hatırlatıcı, favori grupları, sıralama, yeniden adlandırma, kayıt, çoklu ekran (S22–S34, S49–S65). BKL-047 kapıyı ve hakkı sağlar. Bu özelliklerin girişleri S37'ye kadar gizli kalır (§3.7).
- Apple uygulaması (F3). Bu planda yalnız veri modeli ve akış (h) hazırlanır. Apple kabul testi Apple işiyle birlikte izlenir.
- Telefon, LG, Samsung (F4, F5) ve web satışı (D112 ile yok).
- Kaynak verisinin sunucuya gitmesi. Hiçbir adımda yok.
- BKL-016 telefonla kurulum. Ortak QR bileşeni dışında ayrı kanal ve ayrı kod kullanır.

| İş | İlişki |
|---|---|
| BKL-038 / S35–S37 | Bu plan S35–S37'yi genişletir. Billing çekirdeği, Premium sayfası ve açılış aynı adımlarda kalır. Tek açılış S37'dir. |
| BKL-042 | Kalıcı paket adı, upload anahtarı, AAB ve Console'daki uygulama önce gelir. RTDN, RC'nin Play bağlantısı ve ürün tanımı bunlara bağlıdır (O02). |
| BKL-043 | Gizlilik v1 ücretsiz yayın içindir. v2, koşullar, Data safety ve silme URL'si S37'den önce gelir (O10–O12). |
| BKL-044 | Premium ve hesap ekran görüntüleri S37'den sonra, sentetik içerikle çekilir. |
| BKL-045 | Marka ve alan adı olmadan OAuth onay ekranı, Auth0 özel alan adı ve hesap merkezi kurulamaz. |
| BKL-046 | Kapalı test. Lisans test kullanıcıları ayrı bir listedir. |
| Apple (F3) | `appAccountToken` = hesap UUID'si; aynı RevenueCat projesi; `inAppOwnershipType` (karar 11). Platformlar arası kabul testleri orada. |

## 2. Kararlar

### 2.1 Verilmiş

- **D111:** Bir platformda alınan Premium, NEXTV hesabıyla diğer platformlarda yeniden ödemeden açılır.
- **D112-1:** Satış yalnız mağazadan yapılır: Android'de Play faturalandırma, Apple'da IAP. Site satış yapmaz; bilgi, destek ve hesap merkezi olarak çalışır.
- **D112-2:** Hesap isteğe bağlıdır. Ücretsiz kullanım ve aynı mağazada geri yükleme hesap istemez. Satın almanın önünde hesap engeli yoktur.
- **D112-3:** Hesap, Play'de Premium satışını açan Android sürümüyle gelir. Hesapsız Premium satışı yapılmaz.

Koddaki karşılığı (öneri): `PLUS_SALE_OPEN`, `ACCOUNT_ENABLED` olmadan açılamaz. Gradle bu durumda yapılandırma anında durur; ReleaseFlagsTest aynı kuralı `BuildConfig` üzerinden denetler.

### 2.2 Açık: öneri ve en geç karar adımı

| # | Karar | Öneri | Alternatif | En geç |
|---|---|---|---|---|
| 1 | Hak altyapısı | RevenueCat (doğrulama, onay, RTDN) + ince NEXTV API (bağlama, cihaz sınırı, imzalı hak, silme) | Yalnız kendi servis (`productsv2` + RTDN) | S35c |
| 2 | Kimlik sağlayıcı | Auth0, AB kiracısı, özel alan adı | Keycloak; Firebase + özel eşleştirme | S35c |
| 3 | TV girişi | RFC 8628: QR (`verification_uri_complete`) + kısa kod; onay sayfası kodu ve uyarıyı gösterir | — | S35a |
| 4 | Giriş seçenekleri | Google + Sign in with Apple; e-posta kodu sonra. Apple için Android açılışında Apple Developer üyeliği gerekir. E-postaya göre otomatik birleştirme yok; "giriş yöntemi ekle" hesap merkezinde. | Google + e-posta kodu; Apple F3'te | S35c |
| 5 | Cihaz sınırı, çevrimdışı süre | Son 30 günde geçerli belge alan cihazlar sayılır; mağaza hakkı ACTIVE olan TV belge istemez. 5 cihaz, 30 gün; 30 günde en çok 3 çıkarma; TV yalnız kendini çıkarır. | Girişli her cihaz sayılır; 3 cihaz, 14 gün | S35c; koşullar O11 |
| 6 | Ücretli paketin adı | Açık. Kod tanımlayıcıları `Plus` kalır, metinler karara uyar. | Premium / Plus | S36 |
| 7 | Ürün kimliği | Ada bağlı olmayan `nextv_lifetime`. Sonradan değiştirilemez ve yeniden kullanılamaz. | `plus_lifetime` | O06 |
| 8 | Fiyat ve TL bandı | SALES_STRATEGY §3 + arbitraj notu | — | S37 |
| 9 | Yasal yapı | Mali müşavire sorulur | 20/B / şahıs şirketi | O05 |
| 10 | Veri bölgesi | AB | ABD | S35c |
| 11 | Aile paylaşımı | Apple Family Sharing kapalı. Play'de aileyle paylaşılan alım hiçbir hesaba bağlanmaz. sales_draft §4.5'teki "Family Library açık" notu buna uyar. | İki mağazada da açık | S35c (Play bağlama), F3 (Apple) |
| 12 | Apple, LG/Samsung zamanı | F2'den sonra | — | Bu planı engellemez |
| 13 | Aynı mağazada okuma yolu | Play kaydı doğrudan, salt okuma (`queryPurchasesAsync`). Onay RC'de; yedek olarak okuyucu 48 saat sonra onaylar. | RevenueCat `CustomerInfo` | S35; S36c'de doğrulanır |
| 14 | "Her şeyi sıfırla" sonrası | Premium ve hesap girişi kalır. Gizlilik etkisi: TV el değiştirirse giriş ve maskeli e-posta sonraki kişiye kalır; çıkış TV-076'dadır. | Giriş silinir, Premium kalır | S35 |
| 15 | Satın alma sonrası öneri | Bir kez "Şimdi / Sonra" | Yok | S36a |
| 16 | Uygulama içi cümle | "Premium başka bir cihazında varsa hesabınla giriş yap.", politika incelemesiyle | — | S37 |
| 17 | Bağlama kuralı | Bir alım tek hesaba `purchaseToken` ile bağlanır. Taşıma destek üzerinden. Silinen hesabın alımı 30 gün yeniden bağlanamaz. | — | S35c |
| 18 | Sunucu verisi | §3.5 | — | S35c, O10 |
| 19 | Saat geri alma ve değiştirilmiş APK | Kabul edilmesi önerilen risk; karar sahibin. Saat kuralı (d)6'da. | Belge süresi 14 g | S35b |
| 20 | Satış sürerken girişin kapanması | `signIn=false` 7 günü aşarsa ürün de pasifleştirilir (D112-3) | Satış sürer | S37 |
| 21 | Play penceresi ve PIN kilidi | İstisna yok. Sonuç `PlusUi`'de bekler; TV-078 kilit açılınca gösterilir (pin.md §3.9). | Play penceresi açıkken kilit ertelenir | S36 |
| 22 | Premium özelliklerin S37'den önceki görünürlüğü | Satış bayrağı kapalıyken girişler gizli kalır ve satışla aynı sürümde açılır (PLAN 4a-1) | Ücretsiz yayımlayıp sonra kilitlemek (ayrı karar gerekir) | İlk Premium özellik adımı |

Her karar verildiğinde DECISIONS'a D113'ten başlayarak yazılır.

## 3. Mimari

### 3.1 İlkeler (öneri)

1. Mağaza alımı, mağazanın kendi kaydına dayanarak kendi mağazasında açılır. NEXTV API ya da RevenueCat (RC) kapalıyken de açılır.
2. Hesap yalnız platformlar arası taşımayı ekler.
3. Uygulamada satış yönlendirmesi yoktur.
4. Kaynak verisi (adres, kimlik bilgisi, liste, favori, geçmiş) hiçbir isteğe girmez.
5. Bayraklar kapalıyken RC kurulmaz, Play okuyucusu bağlanmaz, Auth0'a ve NEXTV API'ye istek gitmez. Ücretsiz yayının "veri toplanmıyor" beyanı böylece doğru kalır.
6. Bağlamada ve cihaz sınırında doğruluk kaynağı NEXTV tablosudur. Bağ Play `purchaseToken`'ıyla (Apple'da `originalTransactionId`) kurulur. RC'nin müşteri kimliği ve kimlik aktarımı hesap hakkını değiştirmez.

### 3.2 İstemci

| Sınıf | Dosya | Görev |
|---|---|---|
| `PlusEntitlements(plus, saleOpen)` | `<tv>/data/Features.kt` | `saleOpen=false` iken her özellik açıktır (bugünkü `AllFree`). Açıkken `plus.active && feature in plus.features`. UNKNOWN'da kapalıdır. |
| `PlusState`, `PlusUi`, `PlusController.reduce` | `<tv>/data/PlusController.kt` | Saf. Mağaza ve hesap kaynağını tek bir sırada birleştirir. sales_draft §4.2 kuralları: PENDING açmaz; başarılı boş sorgu mağaza hakkını kapatır; hata ACTIVE'i düşürmez. Her sorgu başlangıç sırasıyla etiketlenir; son PURCHASED olayından önce başlamış sorgunun boş sonucu yok sayılır. Hesap belgesi (d)6'daki etkin zamanla `exp`'te biter. Satın alma sonucu `PlusUi`'de bekler. |
| `EntitlementDoc`, `EntitlementVerifier` | `<tv>/data/EntitlementDoc.kt` | Saf. ES256 JWS. Başlıkta yalnız `alg=="ES256"` kabul edilir. `kid` yalnız derlemedeki listeden okunur. R‖S (64 bayt) → DER dönüşümü elle yazılır. Denetimler: bilinen `v`, `sub == accountId`, `dev`, `iat ≤ t + 1 g`, `exp > t`. Anahtarları kurucudan alır; `java.util.Base64` ve `java.security` kullanır, JVM'de koşar. |
| `DeviceFlow` | `<tv>/data/DeviceFlow.kt` | Saf RFC 8628 durum makinesi: `authorization_pending`, `slow_down` (+5 sn), `expired_token`, `access_denied`. Bekleme yanıttan sonra sayılır. Duraklatılabilir ve sürdürülebilir. |
| `AccountController` | `<tv>/data/AccountController.kt` | `StateFlow<AccountUi>`: giriş, iptal, yeni kod, çıkış, cihazlar, yenileme. Yenileme tek uçuşludur (`Mutex`). Yeni refresh token, başka bir çağrıdan önce `AccountStore`'a atomik yazılır. `ParentalController` kalıbı: `viewModelScope`, enjekte edilen dispatcher ve saat. |
| `StoreBilling` | `<tv>/billing/StoreBilling.kt` | `products()`, `launch()`, `owned()`, `updates` (sales_draft §4.2). |
| `RevenueCatBilling` | `<tv>/billing/RevenueCatBilling.kt` | Alım ve onay (asıl onay yolu), `syncPurchases`, `logOut`. `configure` geç çağrılır, yalnız bayrak açıkken: Satın al'da ya da `appStarted()` onaysız bir PURCHASED alım bulduğunda. Kimlik, girişliyse `accountId`, değilse RC'nin önbellekteki anonim kimliğidir. Bağlama için `logIn` çağrılmaz. Release'te `Purchases.logLevel = LogLevel.ERROR`. |
| `PlayPurchaseReader` | `<tv>/billing/PlayPurchaseReader.kt` | Ürün fiyatını, sahip olunan alımları ve `purchaseToken`'ı okur. `purchase.signature`'ı `BuildConfig.PLAY_PUBLIC_KEY` ile doğrular. Onaylamayı yalnız yedek kuralda yapar: PURCHASED alım 48 saat sonra hâlâ onaysızsa (karar 13). RC'nin getirdiği PBL sürümünü kullanır; ikinci bir PBL sürümü eklenmez. |
| `AccountApi` | `<tv>/account/AccountApi.kt` | Auth0 `/oauth/device/code`, `/oauth/token`, `/oauth/revoke` ve NEXTV API uçları. Tek, geç kurulan `OkHttpClient` (10/15/30 sn); `org.json`; iptal edilebilir çağrı (`SourceRepository.kt:88-127` kalıbı). Adresler ve istemci kimliği ortamın `BuildConfig` alanlarından gelir. |
| `AccountStore` / `AccountStorage` | `<tv>/account/AccountStore.kt` | ProtectionStore kalıbı: alias `nextv_account_v1`, `filesDir/account/account.bin`, AAD "account-v1", generation. Yazma atomiktir (geçici dosya + yeniden adlandırma). |
| `PlusStore` / `PlusStorage` | `<tv>/data/PlusStore.kt` | Alias `nextv_plus_v1`, `filesDir/plus/plus.bin`, AAD "plus-v1". Mağaza ve hesap bölümleri ayrıdır. |
| `PremiumServices` | `<tv>/MainActivity.kt` (`TvApplication`) | Lazy RC, Play okuyucusu, `AccountApi`, `enabled` ve `saleOpen`. Bayraklar kapalıyken hiçbir istemci oluşturulmaz. androidTest kendi sahtesini koyar. |
| `QrCode` | `<tv>/ui/QrCode.kt` | Compose `Canvas` ve qrcodegen (PLAN S43 ile aynı kütüphane; sürüm uygulama günü doğrulanır). Beyaz zemin üzerinde koyu modül, 4 modül boşluk, en az 200 dp. |

- **Depolama:** PLAN S35'teki `DeviceStore.seal` kullanılmaz, çünkü `iptv_source_v1` kaynak kurtarmada siliniyor (`DeviceStore.kt:151-154`). ProtectionStore da kullanılmaz; `reset()` PIN kaldırmada çalışıyor (`ProtectionStore.kt:128-141`).
- **Yeni `DeviceKeys`:**
  - `account_device_id`: rastgele UUID, ANDROID_ID değil. Çıkışta ve 401, `account_gone` ya da `device_removed` sonrasında yenisi üretilir.
  - `plus_link_offer_shown`, `plus_last_refresh`.
  - Hepsi `KeyScope.DEVICE`, `reset=false`, `BackupClass.NONE` ve `DeviceKeys.all` içinde.
- **Bağlama (DI yok):**
  - `TvViewModel` (`:168-207`) iki kontrolcüyü satır içinde kurar. Android istemcilerini `TvApplication.premium` (`PremiumServices`) verir.
  - `TvState.plus` ve `TvState.account` `init` içinde (`:220-236`) toplanır. `startedOver()` (`:402-405`) ikisini de taşır.
  - `val entitlements` (`:1033`) getter olur: `val entitlements: Entitlements get() = PlusEntitlements(state.value.plus, saleOpen)`. `gateSnapshot()` değişmez.
  - `AccessGate.answer` (`AccessGate.kt:88-93`): hedef bir `Feature` istiyorsa ve hak ACTIVE değilse `NeedsPlus` döner. Hak UNKNOWN ise `Deny` döner ve `request()` kısa bir bildirim gösterir. Hedefler S24, S29, S33 gibi özellik adımlarıyla gelir.
  - Pencereleri bileşim açar (`PilotApp.kt:391-460`, `rememberOverlayOpener`); ViewModel pencere açamaz. `open`, `watch` ve `play` (`:1101-1103`) Boolean yerine `GateResult` döndürür. Her çağıran ekran `NeedsPin`'de PIN'i, `NeedsPlus`'ta `AppOverlay.PlusLocked(feature)`'ı kendi açıcısıyla açar. Çağıranlar: HomeScreen, DetailScreen, LibraryScreen, GuideScreen, SearchScreen, PilotApp (Quick) ve PlayerScreen (kanal geçişi, ChannelPanel seçimi, EpisodeEndDialog; `NextResult.NEEDS_PLUS`). `rememberPinGate` (`ParentalPages.kt:71-72`) `PageGuard.Plus` için aynısını yapar.
  - `appStarted()` (`:248-251`) yenilemeyi arka planda başlatır ve açılışı bekletmez. Yenileme `now - last >= 6 sa || now < last` olduğunda çalışır; Premium sayfasında elle de çalıştırılabilir. Aynı yerde onaysız alım denetimi yapılır ((a)7) ve süresi dolmamış giriş kodu varsa hemen bir sorgu gönderilir. `appStopped()` kod sorgusunu duraklatır.
- **Ağ:** Yeni `res/xml/network_security_config.xml` gelir. `base-config` düz HTTP'ye izin verir (IPTV bugünkü gibi). API, giriş ve RC alanlarında `cleartextTrafficPermitted="false"`. Billing izni ve `<queries>` kütüphaneyle birleşir; başka izin yok.

### 3.3 Sunucu (öneri)

- **Ortamlar:**
  - Hazırlık ve üretim için ayrı Auth0 kiracısı, RC projesi ve bulut projesi.
  - İstemci `-Pnextv.env=staging|prod` ile derlenir. Auth0 alanı, istemci kimliği, API adresi, RC açık anahtarı ve hak anahtarı listesi ortamın `BuildConfig` alanlarından gelir. Bu alanlar `config/env/<env>.json`'dan üretilir; dosyada yalnız açık değerler durur.
  - Üretim derlemesinde yalnız üretim `kid`'leri bulunur.
  - Hazırlık derlemesi yalnız iç test kanalına yüklenir ve sürüm adı `-staging` eki taşır.
- **Auth0 kiracısı (AB):**
  - Native uygulama "NEXTV TV": Device Code grant, token kimlik doğrulaması "None", OIDC uyumlu.
  - API `nextv-api`: Allow Offline Access ve refresh rotasyonu. Reuse interval > 0 (öneri 30 sn). Hareketsizlik ömrü ≥ 30 g (öneri 90 g), mutlak ömür öneri 365 g. Varsayılanlar doğrulanacak.
  - Google ve Apple bağlantıları kendi anahtarlarıyla kurulur. Hesap bağlama ("giriş yöntemi ekle") hesap merkezinden yapılır.
  - Özel alan adı `giris.<alan>`.
  - `device-code-activation*` metinleri TR/EN. Onay sayfası `user_code`'u gösterir ve şunu söyler: "TV'ndeki kodla aynı mı? Kodu sana başkası gönderdiyse onaylama." Sayfada koşullar ve gizlilik bağlantıları bulunur. Kod 8 karakterdir ve belirsiz harf içermez.
  - Hesap silme `max_age=300` ile yeniden giriş ister.
- **RevenueCat:**
  - Play uygulaması ve Play hizmet hesabı kimliği.
  - RTDN Pub/Sub → RC. Aynı konuya NEXTV API için ikinci bir abonelik açılır (iade, void ve ters ibraz uyarısı).
  - Ürün `nextv_lifetime` "non-consumable", yetki `lifetime`.
  - Geri yükleme davranışı: "Transfer to new App User ID" (varsayılan). Aktarım bağı değiştirmez (ilke 6). S36c'de denenir.
  - Webhook → NEXTV API, Authorization başlığıyla. Başlık sabit zamanlı karşılaştırılır. Webhook yalnız tetikleyicidir: her olayda durum RC REST'ten ya da `productsv2`'den yeniden okunur.
- **NEXTV API:** ince ve sunucusuz. Öneri: Cloud Functions + Firestore, AB bölgesi. İmza anahtarı Cloud KMS'te durur. Kod `services/account-api/` altında (yeni), sırlar depo dışında. Dil, test ve dağıtım komutları WORKFLOW'a yazılır (öneri: Node + Firebase Emulator).

  | Uç | Görev |
  |---|---|
  | `GET /v1/me` | Auth0 `sub` → `accountId`; ilk girişte üretir |
  | `POST/GET/DELETE /v1/devices` | Kayıt, liste, çıkarma. TV yalnız kendi `deviceId`'sini çıkarır; başka bir cihaz hesap merkezinden, yeniden girişle çıkarılır. Başka hesaba kayıtlı `deviceId` için 409. 30 günde en çok 3 çıkarma. |
  | `POST /v1/link` | Gövde `{store, productId, purchaseToken}`. Token `productsv2` ile (ya da RC'de token aramasıyla) doğrulanır: durum PURCHASED olmalı, ürün ve paket adı tutmalı. Sonra `purchases`'a token anahtarıyla yazılır; aynı hesap için idempotenttir. Hatalar: alım başka hesaptaysa 409 `linked_elsewhere`, aile paylaşımlıysa 409 `family_shared`, silinmiş bir hesaptan 30 gün içindeyse 409 `relink_wait`. |
  | `GET /v1/entitlement` | Yalnız aynı `accountId` ile kayıtlı `deviceId` için imzalı belge verir. Kayıtsız cihaza 403 `device_removed`. Sınır doluysa 409 `device_limit`. Sınıra son 30 günde geçerli belge alan cihazlar sayılır. |
  | `POST /v1/hooks/revenuecat` | Olay kimliğiyle idempotent; yalnız tetikleyici. REVOKED yalnız iade ya da void görüldüğünde yazılır. Bekleyen alımın iptali (`ONE_TIME_PRODUCT_CANCELED`) PENDING → CANCELED yapar, ACTIVE'i düşürmez. |
  | `POST /v1/account/delete` | Hesap merkezinden; `auth_time` en fazla 5 dk önce olmalı |
  | `GET /v1/config` | `signIn`, `minVersion` |

- **Web hesap merkezi:** statik site, BKL-043 sayfalarıyla aynı alanda. İçerik: giriş, cihazlar (eklenme zamanı ve son görülme), başka cihazı çıkarma, giriş yöntemi ekleme, veri talebi (erişim, düzeltme), hesabı silme, gizlilik, koşullar, destek. Satış ve fiyat yok.
- **Silme sayfası:** uygulama olmadan çalışır; adresi Data safety'ye girer. Silinen ve tutulan verileri ve saklama sürelerini listeler. Giriş yapamayan kullanıcıya destek e-postası verir.

### 3.4 Akışlar

**(a) Hesapsız Play alımı (TV A)**
1. Ayarlar → Premium (TV-074). Fiyat `PlayPurchaseReader` ile `formattedPrice` olarak gelir; RC henüz kurulmaz. Satın al'ın üstünde sınır ve koşullar satırı durur.
2. Satın al → RC kurulur (girişsizse anonim, girişliyse `accountId` ile) → oynatma varsa duraklar → Play penceresi açılır.
3. PURCHASED → RC doğrular ve onaylar. `PlusStore`'un mağaza bölümüne durum, ürün, `tokenHash` ve zaman yazılır.
4. PENDING → "Ödemen onay bekliyor". Premium açılmaz.
5. Sonuç `PlusUi`'de bekler. Play penceresi `onStop`'u tetikler ve kilit ekranı gelirse, kilit açılınca TV-074 bu sonuçla yeniden açılır.
6. Kullanıcı girişliyse alım (b)2 ile kendiliğinden bağlanır. Değilse bir kez TV-078 gösterilir; kilit ekranı varsa kilit açıldıktan sonra.
7. Onay kurtarma: `appStarted()`'taki Play okuması `isAcknowledged=false` olan bir PURCHASED alım bulursa RC kurulur ve `syncPurchases()` alımı onaylatır. 48 saat sonra hâlâ onaysızsa `PlayPurchaseReader` doğrudan onaylar. Bu yol üç durumu karşılar: uygulama kapalıyken PURCHASED'a dönen PENDING, promosyon kodu ve alım anında RC'ye ulaşılamaması.

**(b) Satın alma sonrası bağlama**
1. TV-078 "Şimdi" → TV-077, (c)'nin 1–4. adımları.
2. Token `PlayPurchaseReader`'dan alınır → `POST /v1/link`. Alım başka bir hesaba bağlıysa: "Bu satın alma başka bir NEXTV hesabına bağlı. Destekle iletişime geç."
3. `POST /v1/devices`. Bu TV'nin mağaza hakkı ACTIVE olduğu için belge istenmez ve TV sınıra sayılmaz.
4. "Sonra" denirse TV-076'da "Satın alımı hesaba bağla" satırı kalır.

**(c) Yeni Android TV'de giriş (TV B)**
1. TV-076 Giriş yap → TV-077. `POST /oauth/device/code` (`openid email offline_access`, `audience=nextv-api`).
2. QR, kısa adres, `user_code` ve koşullar/gizlilik satırı görünür. `device_code` gösterilmez. Ekran açık tutulur (`keepScreenOn`).
3. Kullanıcı telefonda Google ya da Apple ile girer ve onaylar. TV `interval` aralığıyla `/oauth/token` sorgular.
   - Uygulama arka plana geçince sorgu duraklar.
   - Geri dönüşte, kodun süresi dolmamışsa hemen bir sorgu gönderilir.
   - Pencere başka bir yoldan kapanırsa (`DisposableEffect`) sorgu iptal edilir.
4. Belirteçler `AccountStore`'a yazılır. `GET /v1/me` → `accountId`.
5. `POST /v1/devices` (model adıyla).
6. Mağaza hakkı ACTIVE ise TV bir kez sorar: "Bu cihazdaki Premium'u hesabına bağla?" Evet denirse (b)2 çalışır. Belge istenmez.
7. Değilse `GET /v1/entitlement` → belge doğrulanır ve önbelleğe girer → ACTIVE (kaynak: hesap). 409 `device_limit` gelirse TV-077 cihaz sınırı durumuna geçer.
8. RC yalnız bu TV'de alım yapılırsa `accountId` ile kurulur.

**(d) Açılış, ön plana dönüş, çevrimdışı**
1. `init`: `PlusStore` ana iş parçacığı dışında okunur, belge doğrulanır, durum hemen yayımlanır. Okuma bitene kadar durum UNKNOWN'dur: özellik kapalıdır ama TV-075 açılmaz.
2. `appStarted()`: Play salt okuma sorgusu ve onay denetimi yapılır. Girişliyse ve koşul tutuyorsa refresh (tek uçuş, rotasyon) ve `GET /v1/entitlement` çalışır.
3. Mağaza ACTIVE'i hata ya da bağlantı yokluğunda düşmez. Hesap belgesi `exp`'e kadar (30 gün) geçerlidir. Sonra: "Hesap hakkını yenilemek için internete bağlan."
4. 401, `account_gone` ya da 403 `device_removed` → belirteçler ve hesap bölümü silinir, yeni `account_device_id` üretilir. Mağaza bölümü kalır.
5. Sürüm `minVersion`'un altındaysa hesap hakkı kapanır ve giriş sunulmaz: "Hesap için uygulamayı güncelle." Mağaza hakkı sürer.
6. Saat kuralı yalnız hesap belgesine uygulanır, mağaza hakkına uygulanmaz.
   - Belge etkin zaman `t = max(now, maxSeen)` ile değerlendirilir. `maxSeen` aynı açılışta `elapsedRealtime` ile ilerler (AndroidPinClock kalıbı, D073).
   - Her başarılı yenilemede `maxSeen = belge.iat` olur; sunucu saati ileri sapmayı sıfırlar.
   - Geri alınmış ya da 1970'te başlayan saat belgeyi tek başına bitirmez.
   - Saati geri alıp çevrimdışı kalan cihazda belge uzar. Bu, kabul edilmesi önerilen bir risktir (karar 19).

**(e) İade ve geri alma**
1. Console'dan iade "revoke" ile yapılır. RTDN `VoidedPurchaseNotification` → RC → webhook → durum yeniden okunur → `REVOKED`. İkinci abonelik sahibe uyarı gönderir.
2. Aynı mağazadaki TV: sonraki başarılı boş sorguda kilitlenir.
3. Hesapla açılan TV: sonraki yenilemede `ent=[]` belgesi gelir. Çevrimdışıysa en geç `exp`'te kapanır.
4. Premium ile oluşan veri silinmez, salt okunur olur (sales_draft §4.5).
5. Ters ibraz 24 saat içinde yanıtlanır (O14). Uyarı (e)1'deki ikinci abonelikten gelir.

**(f) Çıkış**
1. TV-076 Çıkış yap → onay; ilk odak Vazgeç'te.
2. `DELETE /v1/devices/{id}` ve `/oauth/revoke`. Hatalar görmezden gelinir.
3. RC `accountId` ile kurulmuşsa `logOut()`.
4. `AccountStore` ve hesap bölümü silinir, yeni `account_device_id` üretilir.
5. Bu TV'nin Play hesabı Premium'u satın aldıysa Premium açık kalır.

**(g) Hesap silme (web)**
1. Hesap merkezinde yeniden giriş (`max_age=300`) → "Hesabı sil" onayı.
2. NEXTV API sırasıyla şunları yapar:
   - cihazları, `accounts` kaydını, `idpSubjects`'i ve e-postayı siler;
   - `purchases.accountId`'yi boşaltır. Alım satırı iadeleri işleyebilmek için süreli tutulur (süre O10'da);
   - RC'de `accountId` kimlikli bir müşteri varsa `DELETE /v1/subscribers/{accountId}` ile siler (200 ve 404 başarıdır). Anonim RC müşterileri hesaba bağlı değildir;
   - Auth0'da `DELETE /api/v2/users/{id}` ile bağlı her kimliği siler;
   - Apple'da SIWA belirtecini iptal eder (doğrulanacak).
3. TV'ler sonraki yenilemede (d)4'e düşer.
4. Aynı Play hesabındaki TV'de "Satın alımı geri yükle" Premium'u açar. Alım 30 gün boyunca yeni bir hesaba bağlanamaz; bu süre dolmadan yalnız destek bağlayabilir.
5. TV-076'daki "Hesabı sil" satırı web adresini ve QR'ı gösterir. Android TV için bu yeterlidir; telefonda ve Apple'da uygulama içi yol gerekir.

**(h) Sonra: Apple alımı Android'de**
1. Apple TV'de giriş → `accountId`. RC `configure(appUserID=accountId)`, StoreKit `appAccountToken = accountId`.
2. RC webhook → durum yeniden okunur → `purchases` satırı (`APP_STORE`, `originalTransactionId`, `ownership`). `FAMILY_SHARED` alım hiçbir hesaba bağlanmaz.
3. Android TV B'nin sonraki yenilemesinde belge `src=[app_store]` ile gelir ve Premium açılır.
4. `REFUND` ve `REVOKE` hakkı kapatır, `REFUND_REVERSED` yeniden açar.

### 3.5 Veri modeli

| Yer | Alanlar |
|---|---|
| `PlusStore` | `store {state, productId, tokenHash, checkedAt}`; `account {doc, exp, checkedAt}`; `maxSeen`. Ham token ve sipariş numarası tutulmaz. |
| `AccountStore` | `refreshToken`, `accessToken`, `accessExp`, `accountId`, maskeli e-posta, giriş yöntemi |
| Belge (JWS) | Başlık: `alg=ES256`, `kid`. Gövde: `v`, `sub=accountId`, `dev`, `ent=["lifetime"]`, `src`, `iat`, `exp` |
| `accounts` | `accountId` (UUIDv4), `idpSubjects [{provider, sub}]`, e-posta (Apple'ın gizli e-postası olabilir), `createdAt` |
| `purchases` | Anahtar: Play `purchaseToken` ya da Apple `originalTransactionId`. Alanlar: `store`, `productId`, `orderId`, `rcAppUserId`, `ownership` (PURCHASED/FAMILY_SHARED), `accountId?`, `state` (ACTIVE/PENDING/CANCELED/REVOKED), `linkedAt`, `unlinkedAt`, `revokedAt` |
| `devices` | `deviceId`, `accountId`, model adı (kişi adı yok; hesap merkezinde değiştirilebilir), platform, uygulama sürümü, `createdAt`, `lastSeenAt`, `lastDocExp`, `removedAt` |
| `events` | Olay kimliği, tür, zaman. 30 gün sonra silinir. |
| İstek günlükleri | IP ve zaman. Saklama süresi O10'da (öneri 30 g). |

### 3.6 Güvenlik

- **Sunucuda token doğrulama:** Auth0 access token JWKS ile doğrulanır (RS256, `iss`, `aud`, `exp`). `sub` → `accountId` eşlemesi yalnız sunucudadır.
- **Hesap birleştirme:** E-postaya göre otomatik birleştirme yapılmaz. İkinci giriş yöntemi yalnız oturum açıkken, o sağlayıcıyla yeniden doğrulanarak `idpSubjects`'e eklenir.
- **İmzalı hak:**
  - Anahtar Cloud KMS'te, dışa aktarılamayan `EC_SIGN_P256_SHA256` olarak durur. İmza KMS API'siyle atılır.
  - Uygulama ortamına göre bir `kid` → açık anahtar listesi taşır (§3.3).
  - Doğrulama §3.2'deki gibidir. RFC 7515 Ek A.3 vektörü hem JVM'de hem bir cihaz testinde koşar.
  - **Rotasyon:** yeni `kid` önce bir sürümle dağıtılır, sonra imzaya geçilir. Eski anahtar bir sürüm sonra listeden çıkar.
  - **Sızıntı:** anahtar listeden çıkarılır. Zarar, anahtarı çıkaran sürüm ve `minVersion` zorunlu güncellemesi yayılana kadar sürer. Ağı engelleyen istemcide zarar süresizdir.
- **Uzaktan kimlik avı (RFC 8628 §5.4):** onay sayfası kodu ve uyarıyı gösterir (§3.3). Hesap merkezi cihazın eklenme zamanını gösterir.
- **Kişisel veri:** `obfuscatedAccountId`'nin değerini RC belirler; e-posta ya da ad girmez. Değer S36c'de `productsv2` yanıtındaki `obfuscatedExternalAccountId` ile doğrulanır. Orada ham bir kimlik görünürse RC'ye sorulur.
- **Sırlar:** Belirteçler ve belge loglanmaz; `toString` maskelidir. Release'te RC log düzeyi ERROR'dır. İç testte logcat'te `accountId` ya da token görünmediği denetlenir. Okuma hatası çıkış ve UNKNOWN anlamına gelir, asla ACTIVE değildir. Webhook gizli değeri ve RC gizli anahtarı yalnız sunucuda durur.
- **Refresh rotasyonu:** Yenileme tek uçuşludur ve yeni token kullanılmadan önce atomik yazılır; reuse interval yarışları örter. Yine de yeniden kullanım algılanırsa aile iptal edilir; TV çıkış yapar ve "Yeniden giriş yap" der.
- **Webhook:** §3.3'teki gibi tetikleyicidir; sabit zamanlı başlık denetimi yapılır ve durum yeniden okunur.
- **Taşıma:** HTTPS zorunludur. Sertifika sabitleme yoktur, çünkü rotasyon riski taşır (öneri).
- **Ayrı istemciler:** `AccountApi` ile `SourceHttp` ayrıdır. İstek gövdelerinde kaynak alanı olmadığı testle denetlenir.
- **Değiştirilmiş APK:** yerel kilidi atlar; imzalı belge bunu önlemez (karar 19).

### 3.7 Bayraklar ve durdurma

- **Bayraklar:**
  - `BuildConfig.ACCOUNT_ENABLED` (`-Pnextv.account=true`) ve `BuildConfig.PLUS_SALE_OPEN` (`-Pnextv.sale=true`) ayrı özelliklerdir; varsayılanları false. `sale && !account` Gradle yapılandırmasında derlemeyi durdurur. Ortam `-Pnextv.env` ile seçilir (§3.3).
  - Hesabı açık, satışı kapalı bir iç deneme derlemesi yapılabilir.
  - Bayraklar kapalıyken Premium ve Hesap satırları görünmez, `PremiumServices` hiçbir istemci oluşturmaz ve `Entitlements` her şeyi açar.
  - Premium özelliklerin girişleri `PremiumServices.saleOpen` kapalıyken görünmez. Böylece ücretsiz kullanılan bir özellik sonradan kilitlenmez (karar 22).
  - Görünürlük `BuildConfig`'ten değil, enjekte edilen `PremiumServices`'ten okunur. androidTest sahteyi, `createAndroidComposeRule`'dan önce koşan order 0 bir Rule ile `TvApplication`'a koyar (`TurkishLocaleRule` kalıbı).
  - İç test AAB'si iki bayrak açık ve `nextv.env=staging` ile derlenir.
- **Durdurma:**
  - Console'da ürünü pasifleştirmek satışı durdurur. Sayfa "Satın alma şu an kullanılamıyor" der.
  - `/v1/config` ile `signIn=false` girişi durdurur. Satış sürerken giriş uzun süre kapalı kalırsa ürün de pasifleştirilir (karar 20). Belge verme de durdurulabilir; önbellekteki belge `exp`'e kadar geçerli kalır.
  - Mağaza hakkı her durumda sürer. Ayrı bir uzaktan yapılandırma SDK'sı eklenmez.

## 4. Ekranlar ve durumlar

Ayarlar'a iki yeni bölüm gelir: `SectionId.PREMIUM` (TV-074) ve `SectionId.ACCOUNT` (TV-076). Her biri tek satır taşır; "bölüm başına tek satır" kuralı (`SettingsPages.kt:144-152`) böylece korunur. İkisi de Hakkında'nın üstünde durur; Ayarlar'ın ilk odağı değişmez. Bölümler yalnız `PremiumServices.enabled` açıkken görünür. `Settings(..., start = plusPage)` girişi, TV-075'ten doğrudan TV-074'ü açar. Kimlikler boş TV-074–079 aralığından seçildi (öneri). SCREENS.md S36 ve S36a'da güncellenir. Figma manifest'ine eklenen her ekran SCREENS'te de yer alır.

| ID | Ekran | Giriş | İlk odak | OK | Back |
|---|---|---|---|---|---|
| TV-074 | Premium (Ayarlar sayfası) | Ayarlar listesi; TV-075 (`start = plusPage`) | Kilitli: Satın al · fiyat. Etkin: bağsızsa Hesaba bağla, değilse ilk satır. Bekliyor: Geri yükle. Satın alma sonrası Satın al kaybolunca etkin durumun ilk satırı. | Satın al → Play; Geri yükle → sorgu; Hesaba bağla / Giriş → TV-077 | Listeden açıldıysa Ayarlar listesi, odak Premium satırında. TV-075'ten açıldıysa panel kapanır, odak kilitli öğeye döner. |
| TV-075 | Premium gerekir (AppOverlay) | Ekranların `NeedsPlus`'ı, `PageGuard.Plus` | Premium'u gör; oynatıcıda Tamam | Premium'u gör → TV-074. Oynatıcıda yalnız Tamam vardır ve pencereyi kapatır (Ayarlar oynatıcıda açılmaz). | Açan öğe |
| TV-076 | Hesap (Ayarlar sayfası) | Ayarlar listesi; TV-074. PIN Ayarlar'ı koruyorsa `PageGuard.Pin(AdminReason.ACCOUNT)` | Girişsiz: Giriş yap. Girişli: Cihazlar. | Giriş → TV-077; Cihazlar → liste; Çıkış → onay; Hesabı sil → adres ve QR | Ayarlar listesi, odak Hesap satırında |
| TV-077 | Hesap girişi (tam ekran AppOverlay) | TV-074, TV-076, TV-078 | Vazgeç (QR odak almaz) | Vazgeç: sorgu durur; Yeni kod; Tamam | Vazgeç ile aynı; odak açan satıra, o satır yoksa TV-074'ün ilk satırına döner |
| TV-078 | Hesaba bağlama önerisi (AppOverlay) | İlk ACTIVE, hesap yok, bir kez; kilit ekranı varsa kilit açıldıktan sonra | Şimdi | Şimdi → TV-077 (yeni pencere bunun yerini alır); Sonra kapatır | Sonra ile aynı; odak TV-074'ün ilk satırına döner |

**TV-077 durumları:**
- **Kod isteniyor:** `LoadingArc` ve live region.
- **Kod:** QR ve "Telefonunla QR'ı tara ya da {adres} adresine gidip kodu gir." Altında: "Giriş yaparak Kullanım Koşulları'nı kabul edersin · Gizlilik: {adres}". Kalan süre yalnız son dakikada güncellenir. Ekran açık tutulur.
- **Tamam:** "Giriş yapıldı: a•••@gmail.com". Varsa "Premium bu cihazda açıldı." Mağaza hakkı varsa: "Bu cihazdaki Premium'u hesabına bağla?" Bağla / Sonra; ilk odak Bağla.
- **Reddedildi:** "Giriş telefonda iptal edildi." İlk odak Yeni kod.
- **Süresi doldu:** "Kodun süresi doldu." İlk odak Yeni kod.
- **Çevrimdışı:** "İnternet bağlantısı yok." Tekrar dene.
- **Durduruldu:** "Hesap girişi şu an kullanılamıyor. Bu cihazdaki satın alma etkilenmez."
- **Güncelleme gerekli:** "Hesap girişi için uygulamayı güncelle."
- **Cihaz sınırı:** pencerenin iç durumu. Model adı ve son görülme salt okunur bir liste olarak gösterilir. "Cihazları yönet" QR'ı ve "Diğer cihazları {adres} adresinden çıkar." yazısı bulunur. İlk odak Tekrar dene; Back Vazgeç ile aynıdır.
- **Başka hesaba bağlı:** (b)2'deki metin.

**TV-076 Cihazlar:**
- Liste: model adı, son görülme, "Bu cihaz" etiketi. Başka bir cihazı çıkarma seçeneği yoktur; liste "Diğer cihazları {adres} adresinden çıkar." der.
- Durumlar: Yükleniyor (`LoadingArc`); Çevrimdışı ("İnternet bağlantısı yok." Tekrar dene); Hata ("Cihazlar şu an alınamadı." Tekrar dene).
- Back TV-076'ya döner, odak Cihazlar'da.

**TV-074 durumları:** sales_draft §4.4 tablosu ve şu ekler:
- Satın al'ın üstünde ön bilgilendirme: "Premium bu Google Play hesabında süresiz açılır. NEXTV hesabıyla en çok 5 cihazda. Koşullar: {adres}". Metin avukata sorulur (O11).
- Etkin satırında kaynak görünür: "Google Play" ya da "NEXTV hesabı" (S36a).
- "Hesap hakkını yenilemek için internete bağlan." (S36a)
- Satın alma sonucu `PlusUi`'den gelir; kilit ekranından sonra da kaybolmaz.
- Satın al PIN istemez; Play'in kendi satın alma doğrulaması geçerlidir.
- Play'siz cihazda satın alma yoktur, hesap girişi yine sunulur.
- UNKNOWN iken kilitli öğeye basılırsa TV-075 yerine kısa bir bildirim çıkar: "Premium durumu okunuyor. Birazdan tekrar dene."

**Metin kuralları:**
- "Hesap girişi" ve "kod" denir. "Aktivasyon", "lisans anahtarı" ya da "kodla Premium aç" denmez (Apple 3.1.1).
- Fiyat yalnız `formattedPrice` ile gösterilir. Karşılaştırma, indirim, "sitemizden" ifadesi ya da web satışı yoktur. Web adresi yalnız giriş, koşullar, gizlilik ve silme için kullanılır.
- Açılışta ve oynatma sırasında satış penceresi çıkmaz. TV-075 yalnız kilitli bir öğeye basınca açılır.
- Ham hata ve belirteç gösterilmez. Metinler `values` ve `values-tr` içindedir (`UiTextTest`, `NoHardcodedUiTextTest`). `NoHardcodedUiTextTest` taraması (`NoHardcodedUiTextTest.kt:114-115`) `account/`, `billing/`, `data/AccountController.kt` ve `data/PlusController.kt`'yi kapsayacak biçimde genişler. "Authorization" ve "Bearer" `Allowed` listesine girer.
- Karar 14 kabul edilirse `pin_forgot_note` ve `recovery_reset_kept` iki dilde "NEXTV hesabı girişin ve Premium kalır." der. "Kaynak hesapları" ifadesi NEXTV hesabıyla karışmasın diye "kaynak" sözcüğü korunur.
- "NEXTV içerik satmaz; Premium yalnız uygulama özelliklerini açar."
- **TalkBack:**
  - QR "Giriş için QR kodu" diye okunur.
  - QR ve kod odak almadığı için şu yolla duyurulur: kod göründüğünde adres, harf harf kod ve yönerge polite live region ve pencere başlığıyla okunur.
  - Sonuç polite live region'dır.
  - D064 odak dili geçerlidir.
  - Hareketi azalt açıkken `LoadingArc` durağan kalır.

## 5. Adımlar

Adım kimlikleri PLAN Faz G'yi alt harfle sürdürür (öneri). Commit biçimi `<Adım>: <başlık>`. Kontroller [WORKFLOW](../../project/WORKFLOW.md) ve `tools/run-device-tests.sh` ile yapılır. Bütün kod adımları bayraklar kapalıyken birleşir; kullanıcı S37'ye kadar hiçbir şey görmez.

| # | Hedef | Dosyalar | Bağımlılık | Kabul / test | Boy (ölçülmedi) | Kim |
|---|---|---|---|---|---|---|
| S35 | Billing çekirdeği, satış kapalı | `app/build.gradle.kts` (RC SDK, tek PBL sürümü, iki bayrak, ortam alanları), `config/env/*.json`, `<tv>/billing/*`, `data/PlusController.kt`, `data/PlusStore.kt`, `data/Features.kt`, `DeviceKeys.kt`, `TvViewModel.kt`, `MainActivity.kt` (`PremiumServices`, RC log düzeyi) | O04 yanıtı; yanıt yoksa yalnız arayüz ve sahte | JVM: PlusControllerTest (onaysız alım yeniden açılışta onaylanır; 48 saat yedeği; satın almadan önce başlayan boş sorgu hakkı kapatmaz; geçersiz imza), EntitlementsTest, ReleaseFlagsTest (`sale && !account` yok; üretimde yalnız üretim `kid`'i). Cihaz: PlusStoreTest (üç sıfırlamadan sonra kalır). Bağımlılık raporunda tek PBL sürümü. Bayrak kapalıyken `Purchases.isConfigured == false` ve okuyucu oluşmaz. | 3,5 g | kod |
| S35a | Hesap çekirdeği | `data/DeviceFlow.kt`, `data/AccountController.kt`, `<tv>/account/*`, `DeviceKeys.kt` (`account_device_id`), `res/xml/network_security_config.xml`, `AndroidManifest.xml`, `NoHardcodedUiTextTest.kt` | — (RFC 8628 standarttır) | JVM: DeviceFlowTest (duraklat/sürdür), AccountControllerTest (iki eşzamanlı yenileme tek ağ çağrısı yapar; yeni token yazılmadan süreç ölürse; çıkışta yeni cihaz kimliği), AccountApiTest (MockWebServer). Cihaz: AccountStoreTest. | 3 g | kod |
| S35b | Hak belgesi ve birleşik karar | `data/EntitlementDoc.kt`, `account/AccountApi.kt` (NEXTV uçları), `PlusController.kt`, `config/env/*.json` (anahtar listeleri) | S35, S35a | JVM: EntitlementVerifierTest (RFC 7515 A.3; `alg` none/HS256 reddi; yabancı `kid`; `sub`; `iat`), PlusControllerTest (mağaza ∨ hesap, iade, silinmiş hesap, `device_removed`, `minVersion`, 30 gün, ileri sıçrama sonra düzelme, 1970 saati). Cihaz: aynı A.3 vektörü (AccountStoreTest içinde). | 2,5 g | kod |
| S35c | Sunucu: API, Auth0, RC, webhook, silme, giriş yöntemi ekleme, ortamlar | `services/account-api/**` (yeni), `docs/architecture/ACCOUNT_API.md` (yeni), `docs/project/WORKFLOW.md` (dil, test, dağıtım, hazırlık/üretim) | O01, O02, O04, O07–O09; kararlar 1, 2, 4, 5, 10, 11, 17, 18 | Servis testleri: 409'lar, 403 `device_removed`, sınır ve çıkarma kotası, token ile bağlama, aile paylaşımlı alım bağlanmaz, idempotent webhook, `ONE_TIME_PRODUCT_CANCELED` ACTIVE'i kapatmaz, sahte başlık reddedilir, silme sırası, `auth_time`, giriş yöntemi ekleme. Hazırlık kiracısında uçtan uca cihaz akışı. | 7,5 g | kod + siz |
| S36 | Premium sayfası (TV-074, hesap satırları olmadan), geri yükleme | `ui/settings/PlusPages.kt`, `SettingsPages.kt` (`SectionId.PREMIUM`), `SettingsHost.kt` (`start`), `TvViewModel.kt`, `MainActivity.kt`, `strings.xml` (iki dil) | S35, karar 6 | Cihaz: PlusPageTest (FakeBilling; durumlar, ilk odak, satın alma sonrası odak, Back, kilit ekranından sonra sonuç). İç testte lisans kullanıcısıyla gerçek alım. Gerçek TV: PIN ve "Açılışta PIN iste" açıkken satın alma. | 2,5 g | kod |
| S36a | Giriş (TV-077), Hesap (TV-076), cihazlar, çıkış, öneri (TV-078), QR, TV-074'ün hesap satırları | `ui/AccountSignIn.kt`, `ui/settings/AccountPages.kt`, `ui/settings/PlusPages.kt`, `SettingsPages.kt` (`SectionId.ACCOUNT`), `ui/QrCode.kt`, `ui/PilotApp.kt` (`AppOverlay`), `data/ParentalController.kt` (`AdminReason.ACCOUNT`), `build.gradle.kts` (qrcodegen), `strings.xml`, `docs/design/SCREENS.md` | S35a–S35c, S36 | Cihaz: AccountSignInFlowTest (durumlar, live region, ekran açık, arka plandan dönüşte sorgu), AccountPageTest (PIN koruması, Cihazlar durumları), VisualPreviewTest çekimleri. QR telefonla, koyu temada elle okunur. | 5 g | kod |
| S36b | Web hesap merkezi, silme sayfası, giriş yöntemi ekleme, veri talebi; Auth0 TR/EN metinleri | `services/account-web/**` (yeni) | O01, S35c, BKL-043 | Uçtan uca silme: Auth0, RC ve tablo temizlenir, alım satırı hesapsız kalır; TV 401 alıp çıkar; sayfada fiyat ve satış yok. Onay sayfası kodu ve uyarıyı gösterir. Google + Apple tek `accountId`'de toplanır. Uygulamasız silme sayfası çalışır. | 2,5 g | kod + siz |
| S36c | Prototip turu, okuma yolu ve RC doğrulaması | İç test derlemesi (`nextv.env=staging`), DECISIONS | O02, O06, S36, S36a | RC sandbox senaryoları: bağlı alım varken girişsiz TV'de geri yükleme hesaptaki hakkı düşürmez; RC geri yükleme davranışı; `obfuscatedExternalAccountId` değeri; silme sonrası aynı mağazada geri yükleme; onaysız alım kurtarma; TV A → TV B. Sonuç DECISIONS'a yazılır. | 1 g | kod + siz |
| S37 | Açılış: bayraklar birlikte, `NeedsPlus` kapıları, TV-075, Premium özellik girişleri, iade sonrası salt okunur veri | `Features.kt`, `AccessGate.kt`, `TvViewModel.kt`, `ParentalPages.kt`, `PilotApp.kt`, `HomeScreen.kt`, `DetailScreen.kt`, `LibraryScreen.kt`, `GuideScreen.kt`, `SearchScreen.kt`, `PlayerScreen.kt`, `build.gradle.kts`, `DECISIONS.md` | S36–S36c, O03, O05, O10–O14, seçilen Premium özellikleri | JVM: AccessGateTest (NeedsPlus; UNKNOWN'da Deny, TV-075 yok), ReleaseFlagsTest. Cihaz: PlusGateFlowTest (her çağıran ekran, oynatıcıda Tamam, TV-074'ten Back). | 2,5 g | kod |
| S37a | Kabul turu ve kademeli yayın | QA json, STATUS, HANDOFF, BACKLOG | S37, O15 | §6.5 iki gerçek TV'de; BKL-047 done | 1,5 g | siz + kod |

**Sahip işleri:**

| # | İş | Önce gelmesi gereken adım |
|---|---|---|
| O01 | Marka ve alan adı (BKL-045) | O02, O08, S35c |
| O02 | Kalıcı paket adı, upload anahtarı, Play App Signing, imzalı AAB, Console'da uygulama (BKL-042) | O06, O07, O09 |
| O03 | Ücretli paket adı ve ücretsiz/Premium sınırı (sınır ilk ücretsiz yayından önce; BKL-038) | S36 |
| O04 | RevenueCat'e ve Auth0'a yazılı soru: IPTV oynatıcıyı kabul ediyorlar mı; onay sayfasında cihaz adı gösterilebilir mi (uzaktan kimlik avı önlemi, RFC 8628 §5.4) | Hemen; yanıt S35c'den önce |
| O05 | Mali müşavir: 20/B ya da şahıs şirketi. Play ödeme profili, vergi bilgisi, %15 kademesine kayıt | O06 |
| O06 | Billing'li ilk iç test yüklemesi (S35 derlemesi); `nextv_lifetime` (başlık ≤55, açıklama ≤200); RC'de non-consumable; lisans test kullanıcıları | S36c |
| O07 | RTDN: Pub/Sub konusu, Publisher rolü, "tüm bildirimler", test mesajı, RC bağlantısı; NEXTV API için ikinci abonelik | S35c |
| O08 | Hazırlık ve üretim Auth0 kiracıları; Google OAuth istemcisi ve onay ekranı doğrulaması; karar 4'e göre Apple üyeliği ve Services ID; refresh ömürleri ve reuse interval | S35c |
| O09 | Hazırlık ve üretim RevenueCat projeleri; Play hizmet hesabı kimlikleri (RC ve NEXTV API için ayrı); webhook gizli değeri; geri yükleme davranışı | S35c |
| O10 | Gizlilik politikası v2 (TR/EN), KVKK aydınlatma metni, yurt dışına aktarım, standart sözleşme ve Kurum'a bildirim, GDPR m.27 temsilcisi, veri işleme sözleşmeleri, saklama süreleri (silinen hesabın alım satırı, istek günlükleri) (avukat) | S37 |
| O11 | Kullanım koşulları: lisans kapsamı, cihaz sınırı ve çıkarma kotası, 30 gün, platformlar arası erişimin süresi, iade yolu; TV-074 ön bilgilendirme metni | S37 (ilk satıştan önce) |
| O12 | Data safety: e-posta, kullanıcı kimliği, cihaz ya da diğer kimlikler (`account_device_id`, RC kimliği), satın alma geçmişi, cihaz modeli; RC ve Auth0 SDK'larının topladıkları; aktarımda şifreleme; uygulamasız hesap silme URL'si | S37 |
| O13 | IARC anketinde dijital satın alma; STORE_LISTING_EN hesap cümlesi | S37 |
| O14 | Destek süreci: 48 saati geçen iadeler, 24 saatlik ters ibraz, hesap taşıma, cihaz çıkarma, veri talepleri (erişim, düzeltme; KVKK m.11, GDPR m.15–16), giriş yapamayanın silme talebi | S37 |
| O15 | İki gerçek Android TV, lisans test hesapları, iç test kanalı | S37a |

**Kritik yol:**
- Sunucu kolu: O04 ve O01 → O02 → O07–O09 → S35c.
- Uygulama kolu: S35 → S35a → S35b.
- Mağaza kolu: O02, O05 ve S35 derlemesi → O06 → S36c.
- Kollar birleşince: S36 → S36a → S36b → S36c → S37 → S37a.
- S37 ayrıca Premium özelliklerini bekler. Öne çekme yolu: S22 → S24–S25 → S31 → S33 → S32 (PLAN §2).

**Güncellenecek belgeler:**
- SCREENS (TV-074–078, Figma manifest'iyle birlikte), PLAN Faz G satırları, STEPS, PRD §12, PLATFORMS, PLAY_STORE_READINESS, STORE_LISTING_EN.
- sales_draft: §4.2'deki `billing-ktx:9.1.0` satırı geçersiz sayılır; §4.5'teki Family Library notu karar 11'e uyar.
- WORKFLOW (sunucu komutları), BACKLOG ve STATUS/HANDOFF.

## 6. Test planı

**6.1 JVM (önce):**
- Sınıflar: PlusControllerTest, EntitlementsTest, ReleaseFlagsTest, EntitlementVerifierTest, DeviceFlowTest, AccountControllerTest, AccountApiTest, AccessGateTest (NeedsPlus, UNKNOWN).
- AccountApiTest Auth0 hata kodlarını, 401, 403, 409 ve 503'ü dener. İstek gövdelerinde kaynak alanı olmadığını da denetler.
- K4 (30 gün çevrimdışı) yalnız enjekte edilen saatle burada ve androidTest'te koşar.
- Sahteler iç içe özel sınıflardır: `PlusStorage`, `AccountStorage`, `StoreBilling`, saat. Test adları cümle biçimindedir; `runTest` + `StandardTestDispatcher`.

**6.2 Emülatör:**
- Yalnız `bash tools/run-device-tests.sh` ile, emulator-5558'de. Premium bölümleri `PremiumServices` sahtesiyle açılır, betiğe `-P` gerekmez.
- Sınıflar: PlusStoreTest, AccountStoreTest (izole ad alanı, A.3 vektörü), PlusPageTest, AccountSignInFlowTest (MockWebServer), AccountPageTest, PlusGateFlowTest, SettingsFrameworkTest (yeni bölümler, ilk odak değişmez), VisualPreviewTest.
- Yazı ölçeği matrisi yayın öncesine kalır.

**6.3 Gerçek TV'de lisans test kullanıcıları:**
- İç test kanalı. Play penceresinin TV'deki görünümü ve `onStop`'u tetikleyip tetiklemediği.
- Yavaş test kartıyla PENDING; iptal; ödeme reddi; "revoke" ile iade ve RTDN.
- PIN ve "Açılışta PIN iste" açıkken satın alma. Alım sırasında ağ kesme.
- logcat'te `accountId` ya da token görünmemesi.

**6.4 RevenueCat sandbox:** Play test alımları sandbox olarak görünür ve webhook hazırlık ortamına gider. S36c senaryoları burada koşar.

**6.5 Kabul senaryoları:**

| # | Senaryo | Beklenen |
|---|---|---|
| K1 | TV A'da hesapsız al ve bağla; TV B'de (başka bir Google hesabıyla) giriş yap | TV B'de Premium açılır; cihaz listesinde iki TV görünür |
| K2 | K1'den sonra Console'dan iade ve revoke | TV A sonraki sorguda, TV B sonraki yenilemede kilitlenir; veri silinmez |
| K3 | Web'den hesabı sil | TV B çıkış yapar ve kilitlenir; TV A'da Geri yükle Premium'u açar; alım 30 gün yeni bir hesaba bağlanamaz |
| K4 | TV B 30 gün ağsız kalır (yalnız enjekte saatle, JVM ve androidTest) | 30. güne kadar açık; sonra uyarı ve kilit; bağlanınca yeniden açılır |
| K5 | PIN'i unuttum → Her şeyi sıfırla; kaynak kurtarma | Premium ve giriş kalır |
| K6 | 6. cihazda giriş | Cihaz sınırı durumu; hesap merkezinden bir cihaz çıkarılınca Tekrar dene Premium'u açar |
| K7 | Çıkış | Hesap hakkı gider, aynı Play hesabının mağaza hakkı kalır; yeni cihaz kimliği üretilir |
| K8 | Bayraklar kapalı derleme | Bölüm yok; `Purchases.isConfigured == false`; `AccountApi` ve `PlayPurchaseReader` hiç oluşturulmaz |
| K9 | RC ve API erişilemez | Mağaza hakkı açık kalır |
| K10 | Alım sırasında ağ kesilir, uygulama kapanır | Sonraki açılışta alım onaylanır; 3 gün içinde iade olmaz |

K4 gerçek TV turunda koşulmaz. "Apple'da al → Android'de aç" ve tersi F3'te koşulur.

## 7. Yayın sırası ve geri dönüş

1. **Ücretsiz ilk yayın:** bayraklar kapalı (BKL-042–046). RC SDK pakette olsa da kurulmaz; K8 bunu kanıtlar.
2. **İç test:** `-Pnextv.account=true -Pnextv.sale=true -Pnextv.env=staging` ve lisans testçileri (S36c–S37). Gerekirse önce hesabı açık, satışı kapalı bir deneme derlemesi.
3. **Kapalı test:** `nextv.env=prod` ile K1–K10.
4. **Üretim:** kademeli %5 → %20 → %50 → %100, her basamak en az 48 saat. İzlenenler:
   - Android vitals,
   - RC alım hataları,
   - API 4xx/5xx ve Auth0 logları (sunucu tarafı; istek günlüklerinin saklama süresi O10'da).

   İstemci sayaç toplamaz (ilke 5, Data safety).
5. **Sorun çıkarsa:**
   - Kademeli yayın durdurulur.
   - Satış sorunu → ürün pasifleştirilir. Giriş sorunu → `signIn=false`; sorun uzarsa karar 20 uygulanır.
   - Sürüm kodu geri alınamaz; düzeltme ileriye doğru yapılır.
6. **Sağlayıcı reddederse:**
   - **RC, açılıştan önce:** `StoreBilling` için doğrudan PBL 9 uygulaması yazılır. Sunucuda `productsv2` doğrulaması ve onay tek yoldan yapılır; RTDN kendi fonksiyonumuza gelir. Bağlama tablosu ve token ile bağlama zaten bizimdir. Ek iş yaklaşık 4–5 g (ölçülmedi).
   - **RC, açılıştan sonra:** Play alımları mağazada açık kalır (ilke 1). Bağlar kendi tablomuzda durur; sunucu B seçeneğine geçer.
   - **Auth0:** Keycloak'a geçilir. RFC 8628 yerleşik olduğu için `AccountApi` yalnız yapılandırmayla değişir. Firebase + özel eşleştirme ikinci seçenektir. `idpSubjects` Google ve Apple `sub` değerlerini sakladığı için kullanıcılar yeni sağlayıcıda aynı `accountId`'ye eşlenir.
   - **Hiçbir yol kurulamazsa:** D112-3 gereği Premium satışı açılmaz; ücretsiz yayın sürer.

## 8. Açık riskler ve doğrulanamayanlar

**Riskler:**
- **RC kimlik aktarımı:** Geri yükleme davranışı alımı bir müşteride tutar ya da başka bir müşteriye aktarır. Bağ `purchaseToken` ile kurulduğu ve webhook token ile eşlendiği için hesap hakkı etkilenmez; yalnız RC'nin kendi müşteri görünümü değişebilir. S36c'de doğrulanır.
- **Onaysız alım:** Alım anında RC'ye ulaşılamazsa onay, kurtarma yoluna ve 48 saat yedeğine kalır. Play 3 günde iade eder (K10).
- **PBL sürümü:** RC 10.29.0, PBL 8.3.0 ile paketleniyor. PBL 8, 31.08.2027'den sonra kabul edilmeyecek. Okuyucu aynı sürümü kullandığı için 9'a geçiş RC'ye bağlı. RC'nin geçiş tarihi bilinmiyor.
- **Yazılı onaylar yok:** RC'nin ve Auth0'ın IPTV oynatıcıyı kabul ettiğine dair yazılı yanıt yok (Okta AUP'ta IPTV geçmiyor). Google'ın karma model için yazılı onayı da yok. Uygulama içi cümle politika incelemesini bekliyor.
- **Apple maliyeti:** Sign in with Apple, Android açılışında Apple üyeliği gerektirir. Google OAuth onay ekranı doğrulamasının ne kadar süreceği belirsiz.
- **Hesap bölünmesi:** Google ve Apple ile ayrı açılan hesaplar, "giriş yöntemi ekle" kullanılmazsa ayrı kalır ve Premium yanlış hesapta görünür. Destek taşıması gerekir.
- **Uzaktan kimlik avı:** Onay sayfasındaki uyarı kullanıcının dikkatine dayanır.
- **Tek kişilik destek:** 24 saatlik ters ibraz, hesap taşıma, cihaz çıkarma ve veri talepleri tek kişide.
- **TL fiyatı:** Hesap, Türkiye fiyatı üzerinden arbitrajı büyütür.
- **Saat geri alma ve değiştirilmiş APK:** Kabul edilmesi önerilen risk; karar sahibin (karar 19). Değiştirilmiş APK kilidi atlar; imzalı belge bunu önlemez.
- **Hazırlık derlemesinin üretime çıkması:** Ayrı anahtar listesi ve sürüm adı eki etkisini sınırlar.
- **Keystore kaybı:** Bazı TV'lerde Keystore kaybı çıkış ve yeniden giriş ister. Premium mağazadan geri gelir.

**Doğrulanamayanlar:**
- RC'nin Android TV'yi resmen test edip etmediği. Play satın alma penceresinin TV'deki görünümü, `onStop`'u tetikleyip tetiklemediği ve lisans test kullanıcılarının TV'de çalışıp çalışmadığı.
- RC'nin `obfuscatedAccountId`'yi nasıl doldurduğu. RC REST v1 silme ucunun (`DELETE /v1/subscribers`) sürdürülüp sürdürülmeyeceği ve izin kapsamı. RC'de token ile müşteri araması olup olmadığı.
- RTDN konusuna RC'nin yanında ikinci bir abonelik açılıp açılamayacağı. RC'nin ters ibraz bildirimini iletip iletmediği ve `ONE_TIME_PRODUCT_CANCELED`'ı nasıl ilettiği.
- Auth0 onay sayfasında cihaz adı ve kodun gösterilip gösterilemeyeceği. Apple bağlantısına özel koşullar. Refresh rotasyonunun, ömürlerin ve reuse interval'ın Native uygulama ve cihaz akışındaki varsayılanları. Kiracı bölgesinin sonradan taşınamayacağı varsayımı.
- Silme sırasında Sign in with Apple belirteç iptalinin şart olup olmadığı.
- Play Family Library'nin tek seferlik uygulama içi üründe geçerli olup olmadığı.
- Android'de `SHA256withECDSAinP1363Format` bulunmadığı (R‖S → DER dönüşümü yine de yazılır).
- qrcodegen'in güncel sürümü ve boyutu.

## Uygulanmayan eleştiriler

- "TvViewModel kurucu bölümü 167–212": `:168-207` kontrolcülerin kurulduğu satırları doğru gösteriyor (`parental` 207'de bitiyor). Bu yüzden yalnız `AccessGate.answer` satırı `:88-93` olarak düzeltildi.
- "Belge iki seviye derinde değilse bağlantılar kırılır": belge SALES_CHANNELS'ın gösterdiği `docs/design/gap/account.md`'de duruyor ve `../../` bağlantıları doğru. Konum başa yazıldı.
- "TV-077'deki 'Bu cihazı çıkar' onayı iç durum olmalı": TV artık başka bir cihazı çıkarmıyor (karar 5), bu yüzden onay adımı kalmadı.
- "Açıcı listesinde ChannelPanel ve EpisodeEndDialog var": ikisi kapıyı doğrudan çağırmıyor, PlayerScreen'in `GateResult`/`NextResult` dalından geçiyor. Liste koddaki gerçek çağıranlarla yazıldı; LibraryScreen ve Quick eklendi.
