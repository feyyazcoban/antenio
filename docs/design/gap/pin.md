# Ebeveyn PIN'i (TIVIMATE_GAP satırı "Ebeveyn PIN'i", BKL-028 / PRD SAFE01)

18 Ekim 2026 · TiviMate gap tasarımı (`pin`), salt okunur tasarım ajanı; öneriler kullanıcı kararı değildir.

**Özet:** PIN altyapısı (PinCredential, PinAttempts, AccessPolicy, ProtectionSnapshot/ProtectionStore) testli ama hiçbir yere bağlı değil. TvViewModel.open/play/watch yalnız `entry.adult` ve `sourceFor` kontrol ediyor, D068 ayarları ise kumandası olan herkesin geri açabildiği düz bir tercih. Tasarım bu parçaları yeni bir kontrolcüye (ParentalGuard: deneme rezervasyonu → KDF → kalıcı sıfırlama), bellekte duran bir açma oturumuna (ParentalSession) ve tek bir ViewModel kapısına bağlıyor. Kapı, bekleyen işlemi lambda olarak değil hedef kimliğiyle tutuyor; böylece Ana Sayfa, kütüphane, Ara, rehber, favoriler, devam, geçmiş, kanal numarası, CH±, sonraki bölüm ve ileride gelecek derin bağlantıların hepsi aynı karardan geçiyor. Arayüz Ayarlar → İçerik altında "Ebeveyn denetimi" sayfası, kumandanın rakam tuşlarıyla ve D-pad ile çalışan 4 haneli tuş takımlı PIN penceresi, Kategorileri yönet'te Göster/Kilitle/Gizle seçimi, hızlı menüde Kilitle ve kilit rozetlerinden oluşuyor; tümü D064 odak diline uyuyor ve hareket içermiyor. PIN'i unutma yolu sunucusuz: cihazdaki kaynaklar, favoriler, geçmiş ve kilitler silinir; ebeveyn özellikleri her pakette ve her ülkede ücretsiz kalır. Üç aşamada, her biri ayrı yayınlanabilir şekilde yaklaşık 10,5 mühendis-günü; yeni bağımlılık yok.

**Tahmini iş:** 10.5 gün

**Aşamalar:**
1. Aşama 1 — PIN ve ayar koruması (~3,5 g): AndroidPinClock, PinAttempts.waitMs, ParentalGuard, ProtectionCodec v2, ProtectionStore.reset, PinPad/PinDialog; Ayarlar → Ebeveyn denetimi (oluştur, değiştir, kaldır), PIN'i unuttum sıfırlaması; yetişkin filtresini kapatma ve Kategorileri yönet için PIN; open/play'de kanonik hedef denetimi (gizlenen hiçbir yoldan açılmaz); Hakkında metni; JVM ve cihaz testleri.
2. Aşama 2 — İçerik kilitleri (~4,5 g): AccessPolicy.evaluate, ParentalProjection (+ uzlaştırma), ParentalSession, ViewModel kapısı ve hedef kimlikli bekleyen işlem bütün yollarda (kütüphane, Ara, rehber, Ana Sayfa, favori, devam, geçmiş, ayrıntı, kanal numarası, CH± atlama, sonraki bölüm); rozetler; vitrin ve raf dışlaması; Göster/Kilitle/Gizle penceresi; hızlı menüde Kilitle; Kilitli içerikler sayfası; onStop'ta oturumu temizleme ve kilitli oynatımdan çıkış; testler.
3. Aşama 3 — Kaynak ve açılış koruması (~1,5 g): kaynak kilidi (Bilgiler penceresi), 'Kaynak yönetimi PIN istesin' ile bütün kaynak girişleri, 'Açılışta PIN iste' ve AppLockScreen, koruma kaydı okunamazsa kilit ekranı, PlayerScreen ON_START koruması; testler.
4. Aşama 4 — Cihaz turu ve belgeler (~1 g): 65U95A'da KDF gecikmesi ve PIN akışları, 1,0/1,5/2,0 yazı ölçekleri, TalkBack; PARENTAL_ACCESS, PIN_ATTEMPTS, PROTECTION_STORE, ACCESS_DECISIONS, SCREENS, PRD, Play raporu, BACKLOG, STATUS ve HANDOFF güncellemesi.

**Kullanıcı kararları (varsayılanla):**
- PIN ile açılan bir kilit ne kadar açık kalsın? → varsayılan: Uygulama ekrandan çıkana kadar (Home tuşu, TV beklemesi, ekran koruyucu = onStop), PIN veya kilitler gevşeyene kadar. Ayrıca Ebeveyn denetimi'nde elle 'Açılan kilitleri kapat'. Süre sınırı yok. Açılan izin yalnız hedefi kilitleyen kapsamları (kategori, içerik, kaynak) kapsar.
- PIN unutulursa ne olacak (sunucu yok)? → varsayılan: Uygulama içinde 'PIN'i unuttum → Her şeyi sıfırla': bu cihazdaki kaynaklar, favoriler, izleme geçmişi ve bütün kilitler silinir, yetişkin filtresi açık kalır, kurulum ekranı açılır. Alternatif: yalnız Android Ayarlar → Uygulamalar → NEXTV → Verileri temizle yolunu anlatmak.
- PIN tanımlıyken varsayılan korumalar ne olsun? → varsayılan: 'Kaynak yönetimi PIN istesin' Açık (kaynak bilgileri ve şifre de korunur), 'Açılışta PIN iste' Kapalı. Kilitli içeriğin adı ve afişi görünür ve rozetlidir; vitrinde ve otomatik raflarda yer almaz; kilitli kanalın program adları gizlenir. Tamamen görünmesin istenirse Gizle kullanılır.
- D068 'Kategorileri yönet' satırı Açık/Kapalı anahtarı yerine Göster/Kilitle/Gizle seçimine dönüşsün ve PIN varken sayfa PIN istesin mi? → varsayılan: Evet. Satırda OK, üç seçenekli bir pencere açar. Sayfa yetişkin kategori adlarını listelediği için PIN varken açılışta PIN ister; PIN yokken bugünkü gibi serbesttir.
- Ebeveyn denetimi ücretli paketin dışında mı kalacak (satış ve fiyat kararıyla birlikte)? → varsayılan: Evet: PIN, kilitler ve yetişkin filtresi her pakette, her ülkede ve her dilde ücretsiz; DECISIONS'a fiyatlandırma kuralı olarak yazılır. Play'in yetişkin içerik istisnası reşit olmayanların erişiminin kısıtlanmasına bağlı.

**Riskler:**
- PBKDF2 600.000 tur, iki Keystore yazımıyla birlikte TV SoC'ta ölçülmedi; doğrulama 1–4 sn sürebilir. Aşama 1'de 65U95A'da ölçülmeli. Tur sayısını düşürmek yeni kayıt sürümü ve karar kaydı ister (D047).
- Settings.Global.BOOT_COUNT bazı TV yazılımlarında okunamayabilir. Bu durumda yedek yol, cihaz daha uzun açık kaldıktan sonraki bir yeniden başlatmada beklemeyi en çok kalan süre kadar kısaltır; PIN_ATTEMPTS'te açıkça belgelenmeli.
- Koruma kaydı okunamazsa (Keystore kaybı vb.) uygulama hata kapalı tarafa düşerek kilitlenir; tek çıkış her şeyi sıfırlamaktır. Seyrek ama sert bir durum (D049 ile tutarlı).
- PIN'i unuttum sıfırlaması kumandası olan herkese açıktır. Caydırıcılık kaynakların ve hesap bilgilerinin silinmesidir; M3U adresini bilen biri kaynağı yeniden ekleyebilir. Yerel bir denetimdir, root'a veya uygulama kaldırmaya karşı koruma vaat edilmez.
- Kategori kilidi: M3U'da kategori adı değişince kilit düşer (D068 gizlemesiyle aynı). Xtream kimlik değişimi ada göre uzlaştırmayla kapanır. categoryId taşımayan eski önbellek kayıtları yenilemeye kadar gizlenir (UNRESOLVED).
- Yeni giriş noktaları (oynatıcı üstü kanal listesi, Watch Next, derin bağlantı) kapıyı atlarsa açık oluşur. play() son kapı olarak kalmalı ve her yeni açıcı için ParentalFlowTest'e bir satır eklenmeli.
- Açılan kilit uygulama ön planda kaldıkça sürer. Ebeveyn TV'yi açık bırakırsa çocuk erişebilir; 'Açılan kilitleri kapat' ve onStop temizliği bunu yalnız hafifletir.
- Kilitli içeriğin adı ve afişi tasarım gereği görünür (PRD FIND01 politikası). Tamamen görünmemesi istenirse Gizle kullanılmalı; bu ayrım arayüz metinlerinde açık olmalı.
- D068 kategori satırının anahtardan üç seçenekli pencereye dönüşmesi kullanıcının istediği bir davranışı değiştirir; görsel kabul ve sahip onayı gerekir.
- Arka plandan dönüşte PlayerScreen'in ON_START'ı kilit ekranından önce çalışıp kısa süre ses başlatabilir; route ve appLocked koruması unutulursa kilitli yayın PIN'siz duyulur.

**Dokunulacak dosyalar:**
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/AccessPolicy.kt`: Ekleme: AccessVerdict + evaluate() (pinScopes); decide() = evaluate().decision
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/ProtectionSnapshot.kt`: ProtectionOptions alanı; ProtectionCodec v2 (0x50525602, bayrak baytı), v1 okuma uyumu
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/ProtectionStore.kt`: ProtectionStorage arayüzü, reset() (dosyalar sonra anahtar), changed hesabına options
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/PinAttempts.kt`: Saf waitMs(now) eklemesi (hak düşmeden kalan bekleme)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/AndroidPinClock.kt`: Yeni: PinClockSource + Settings.Global.BOOT_COUNT/elapsedRealtime adaptörü ve belgelenmiş yedek
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/ParentalGuard.kt`: Yeni: Mutex ile sıralı create/verify/replacePin/edit/reset; rezervasyon → KDF → kalıcı sıfırlama
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/ParentalSession.kt`: Yeni: bellekte kapsam izinleri, ayar yetkisi, yalnız sıkılaşmada izin koruma
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/ParentalProjection.kt`: Yeni: lockedIds, kategori satırı ↔ kapsam eşlemesi, UNRESOLVED süzme, eksik kural uzlaştırması
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/AccessTargets.kt`: Yeni: kanonik hedef + SeriesContext çözümü, kilitliyi atlayan nextZap
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/HomeLibrary.kt`: homeLibrary(..., locked): vitrin ve otomatik raflardan dışlama
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/CategoryVisibility.kt`: KDoc güncellemesi (PIN varken gevşetme yetki ister); öneri: çok dilli yetişkin kelimeleri (ayrı karar)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/TvViewModel.kt`: TvState: parental/pinPrompt/accessNotice/lockEpoch; erişim kapısı open/watch/play/loadDetail/changeChannel/playChannelNumber/playNextEpisode; yetki ile kaynak yönetimi; restore'da koruma okuma; publishLibrary projeksiyonu; PIN API; forgotPinReset; appStopped/appStarted
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/MainActivity.kt`: onStop/onStart içinde model.appStopped()/appStarted()
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PinPad.kt`: Yeni: PinDialog, PinPad (CharArray tamponu, fiziksel rakamlar, LTR tuş takımı), AppLockScreen, ForgotPin onayı
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SettingsScreen.kt`: Ebeveyn denetimi satırı ve sayfaları (PARENTAL, PARENTAL_LOCKS, PARENTAL_REMOVE), yetki ile sayfa geçişi, CategoryAccessDialog, SettingRow chip, hakkında metni, closeSettings
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PilotApp.kt`: PinDialog/AppLockScreen yerleşimi, lockEpoch ile Ayarlar'ı kapatma, CornerNote; QuickActions'ta Kilitle/Kilidi kaldır; MediaCard kilit rozeti
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/HomeScreen.kt`: HomeCard kilit rozeti; homeLibrary'ye locked; kilitlilerde ensureHeroDetail yok
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/LibraryScreen.kt`: CategoryColumn'da kilit simgesi ve açıklaması; kartlara locked
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SearchScreen.kt`: SearchCard kilit rozeti ve açıklaması
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/GuideScreen.kt`: Kilitli kanal satırında tek 'Kilitli kanal' hücresi, program adları gizli; OK → open
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt`: ON_START'ta route/appLocked koruması; köşe notunu CornerNote'a çıkarma; rakam istemi davranışı
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/EpisodeEndController.kt`: Geri çağrı NextResult (STARTED/NEEDS_PIN/UNAVAILABLE) ve NEEDS_PIN notu
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SourcesScreen.kt`: Bilgiler penceresinde Kaynağı kilitle / kilidini kaldır
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/DesignSystem.kt`: iconPaths içine 'lock' çizgi simgesi
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/AccessPolicyTest.kt`: evaluate/pinScopes testleri
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/ProtectionCodecTest.kt`: v2 seçenekler, v1 uyumu, bilinmeyen bayrak
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/PinAttemptsTest.kt`: waitMs testleri
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/ParentalGuardTest.kt`: Yeni: rezervasyon önce, sıfırlama yazısı, bekleme, eşzamanlılık, revizyon
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/ParentalSessionTest.kt`: Yeni
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/ParentalProjectionTest.kt`: Yeni
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/AccessTargetsTest.kt`: Yeni: kanonik çözüm ve nextZap
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/HomeLibraryTest.kt`: Kilitlilerin vitrin ve rafta dışlanması
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/ui/EpisodeEndControllerTest.kt`: NEEDS_PIN davranışı
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/ProtectionStoreTest.kt`: reset(), v1→v2, seçenek revizyonu
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/AndroidPinClockTest.kt`: Yeni
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/ParentalFlowTest.kt`: Yeni: uçtan uca kumanda akışları, yazı ölçekleri, semantik
- `docs/architecture/PARENTAL_ACCESS.md`: Uygulanan kapı, oturum, bekleyen işlem ve erişim tablosu
- `docs/architecture/PIN_ATTEMPTS.md`: Android saat adaptörü, BOOT_COUNT yedeği, waitMs
- `docs/architecture/PROTECTION_STORE.md`: reset() çağıranları ve codec v2
- `docs/architecture/ACCESS_DECISIONS.md`: evaluate/pinScopes
- `docs/decisions/DECISIONS.md`: Yeni karar kaydı (öneri; sahip kararları seçilince)
- `docs/design/SCREENS.md`: TV-071 ayrıntısı, kilit ekranı
- `docs/product/PRD.md`: SAFE01 durumu; ebeveyn denetiminin ücretsiz olması (karar verilince)
- `docs/project/PLAY_STORE_READINESS.md`: 3.5 yetişkin içerik ve çocuk koruma durumunun güncellenmesi
- `docs/product/TIVIMATE_GAP.md`: Ebeveyn PIN'i satırı
- `docs/project/BACKLOG.md`: BKL-028 ilerlemesi
- `docs/project/STATUS.md`: İş sonu kaydı
- `docs/project/HANDOFF.md`: Değişen dosyalar, kontroller, kalan risk

---

# Ebeveyn PIN'i: uygulama tasarımı (BKL-028 / SAFE01)

18 Ekim 2026 · salt okunur tasarım; kod okunarak hazırlandı, çalıştırılmadı. "Öneri" olarak işaretli her şey kullanıcı kararı değildir (bkz. §14).

## 0. İlkeler

1. **Gizle ile kilitle ayrıdır** (PRD SAFE01, `AccessRule.hidden` ve `pinRequired` bağımsızdır). D068 gizlemesi içeriği bütün yüzeylerden çıkarmaya devam eder. Kilitli içerik görünür kalır, açmak PIN ister.
2. **Kilit sıkılaştırmak PIN istemez, gevşetmek ister.** Örnek: yetişkin filtresini açmak, kategori gizlemek ve kilit eklemek serbesttir. Kapatmak, göstermek ve kilit kaldırmak PIN ister (yalnız PIN tanımlıysa).
3. **Tek kapı.** Her açma ve oynatma `TvViewModel` içindeki aynı erişim kararından geçer. `play()` son kapıdır, kartın `onClick`'i kapı sayılmaz (PARENTAL_ACCESS).
4. **Hata kapalı tarafa düşer.** Kayıt okunamazsa (`ProtectionRead.Unavailable`) hiçbir şey serbest sayılmaz.
5. **Sırlar ekrana ve kayda çıkmaz.** PIN `String` olmaz, `TvState` içine girmez, günlüğe ve analitiğe yazılmaz (analitik zaten yok). Rakamlar maskeli gösterilir.
6. **Ücretsizdir.** Hiçbir ebeveyn özelliği ücretli paket kontrolüne bağlanmaz.

## 1. Bugünkü durum (kodda doğrulandı)

- `data/PinCredential.kt`: 4–8 ASCII rakam, PBKDF2-HMAC-SHA256 600.000 tur, 52 baytlık v1 kaydı. `matches`/`create` girdiyi sıfırlar.
- `data/PinAttempts.kt`: `reserve(PinClock)` önce deneme hakkını düşer. İlk dört deneme beklemesizdir; 5. denemeden sonra 30/60/120/240/300 sn beklenir, sayaç 9'da durur. `clear()` yalnız ilk kurulumda veya doğru PIN'den sonra çağrılır. Android saat adaptörü yok.
- `data/AccessPolicy.kt`: kapsamlar `Source`, `Content(ContentRef)` ve `Category(sourceId, kind, ProviderId|M3uName)`. Sonuçlar ALLOW / REQUIRE_PIN / HIDDEN / UNRESOLVED / DENIED; bölüm dizisinden miras alır. Hangi kuralın kilitlediğini döndürmüyor.
- `data/ProtectionSnapshot.kt` + `ProtectionStore.kt`: tek bir şifreli atomik kayıt (Keystore `iptv_protection_v1_primary`, AES-GCM, `generation`/`policyRevision`); "No reset API". Kural veya PIN değişirse revizyon artmak zorunda.
- `data/CategoryVisibility.kt` (D068): bölüm + ad anahtarıyla bütün kaynaklarda gizleme. Ayar `DeviceStore.hiddenCategories` / `hideAdultCategories` olarak düz tercih dosyasında. KDoc'ta "Not an access control" yazıyor.
- `TvViewModel`: `open`, `watch`, `play` yalnız `entry.adult || sourceFor(entry) == null` kontrol ediyor. Bu yollara bağlı olanlar: `changeChannel`, `playChannelNumber`, `playNextEpisode`, `loadDetail` (devam → `play`), `ensureHeroDetail` (ağdan ayrıntı çekiyor). `hideAdultCategories()` ve `categoryVisible()` koşulsuz değiştiriyor. `editSource()` şifreyi taslağa koyuyor ve kurulumdaki "Bilgileri göster" onu açıkça gösteriyor.
- `ui/SettingsScreen.kt`: hakkında metninde "PIN yönetimi henüz yok". İçerik bölümünde iki satır var. `CategoryList` her satırı Açık/Kapalı anahtarı olarak çiziyor.
- `AndroidManifest.xml`: yalnız MAIN/LEANBACK_LAUNCHER. Bugün derin bağlantı yok.
- `prototypes/tv-flow/flows.js` sabit 1234 kullanıyor; üretimde kullanılmaz.

## 2. Neyi korur (PIN tanımlıysa)

| Yer | Davranış | Aşama |
|---|---|---|
| "Yetişkin kategorilerini gizle" → Kapalı | PIN ister; açmak serbest | 1 |
| "Kategorileri yönet" sayfasını açmak | PIN ister (sayfa yetişkin kategori adlarını listeliyor) | 1 |
| "Ebeveyn denetimi" sayfası (değiştir, kaldır, seçenekler, kilit listesi) | PIN ister | 1 |
| Kilitli kategori, kanal, film veya dizi | Görünür ve rozetli; açmak PIN ister | 2 |
| Kilitli kaynak (bütün liste) | Kaynaktaki her şey PIN ister | 3 |
| Kaynak yönetimi (Kaynaklar ekranı, ekle, düzenle, kaldır, rehber adresi) | Varsayılan olarak PIN ister (seçenek) | 3 |
| Uygulama açılışı ve arka plandan dönüş | İsteğe bağlı (varsayılan kapalı) | 3 |

Korunmayanlar (bilinçli): izleme geçmişini temizleme, katalog yenileme, favori ekleme ve çıkarma, kilit ekleme, kaynak kurtarma ekranı (silmekten başka bir şey yapamaz).

**Kilit açma oturumu (öneri, §14-1):** doğru PIN yalnız o hedefi kilitleyen kapsamları açar. Örneğin kategori kilidiyle girilen bir kanal, aynı kategorideki diğer kanalları da açar; bütün kaynağı açmaz. Oturum şu durumlarda kapanır:
- uygulama ekrandan çıkınca (`MainActivity.onStop`: Home tuşu, bekleme, ekran koruyucu),
- PIN değişince,
- herhangi bir kilit gevşeyince,
- elle "Açılan kilitleri kapat" seçilince.

Yalnız kilit eklendiğinde oturum korunur. Ayar yetkisi açık Ayarlar paneli kapanana kadar, ya da Kaynaklar/Kurulum akışından çıkılana kadar sürer.

## 3. Akışlar (kumanda tuşlarıyla)

### 3.1 PIN oluşturma
1. Menü → Ayarlar → Aşağı ile "İçerik" → **Ebeveyn denetimi** → OK.
2. Sayfada (PIN yokken) "PIN oluştur" → OK. PIN penceresi "Yeni PIN" adımında açılır; odak **1** tuşundadır.
3. Rakamlar kumandadaki 0–9 (ve numpad) tuşlarıyla ya da ekrandaki tuş takımında D-pad ve OK ile girilir. "Sil" son rakamı siler. 4. rakamda adım kendiliğinden ilerler.
4. "PIN'i tekrar gir" adımında aynı 4 hane girilir. Eşleşirse kayıt yazılır. Sayfa PIN açık hâline döner; "PIN oluşturuldu." notu çıkar ve ayar yetkisi açık kalır.
5. Geri: tekrar adımında ilk adıma döner, ilk adımda pencereyi kapatır (kayıt yazılmaz). Eşleşmezse "PIN'ler eşleşmedi. Yeni PIN'i baştan gir." yazar ve ilk adıma dönülür. Tahmini kolay PIN'ler (aynı 4 rakam; 0123…6789 ve 9876…3210 sıraları) reddedilir: "Bu PIN kolay tahmin edilir. Farklı bir PIN seç."

### 3.2 PIN doğrulama (bütün amaçlar için aynı pencere)
- Açılışta odak **1**'dedir. Bekleme sürüyorsa odak **Vazgeç**'tedir.
- 4. rakamda "Kontrol ediliyor…" ve LoadingArc görünür, tuşlar devre dışı kalır. Doğruysa pencere kapanır ve bekleyen işlem yürütülür. Yanlışsa noktalar boşalır, odak son tuşta kalır ve hata satırı yazar.
- Geri ya da Vazgeç hiçbir izin üretmez. Odak açan denetime döner (pencere kapanınca alttaki pencerenin odağı geri gelir; ekranların mevcut `refocusAfterDialog` kullanımı korunur).
- "PIN'i unuttum" → §3.11.

### 3.3 Yetişkin filtresini kapatmak
- PIN varken satırda OK → doğrulama ("Yetişkin kategorilerini göstermek için PIN gir.") → doğruysa `setHideAdult(false)`.
- PIN yokken öneri penceresi çıkar: "Önce PIN oluşturmak ister misin?" / "PIN olmadan bu ayarı kumandası olan herkes geri açabilir." Seçenekler: **PIN oluştur** (ilk odak; oluşturunca filtre kapanır), "PIN olmadan kapat", "Vazgeç".

### 3.4 Kategorileri yönet ve kategori kilidi
1. PIN varken "Kategorileri yönet" → OK → doğrulama → Kategoriler sayfası (Canlı TV / Filmler / Diziler) → bölüm → liste.
2. Satır artık anahtar değildir; sağdaki etiket **Açık / Kilitli / Gizli** gösterir. OK ile `CategoryAccessDialog` açılır. Seçenekler (`Action(check = true)`, seçili olanda ✓): "Göster", "Kilitle · PIN ister" (PIN yoksa "Kilitle · önce PIN oluştur"), "Gizle", "Vazgeç". İlk odak geçerli seçimdedir.
3. PIN yokken "Kilitle" önce §3.1'i açar, sonra kilidi uygular.
4. Yetişkin filtresinin gizlediği satırlar eskisi gibi "Yetişkin filtresi gizliyor" der ve pencere açmaz.

### 3.5 Kanal, film veya dizi kilidi
- Kartta OK basılı tutulur (U8 hızlı menüsü, `QuickActions`). PIN varken "Kilitle" ya da "Kilidi kaldır" eklenir.
- Kilitlemek hemen olur. Kaldırmak PIN ister. Bölüm kilitlenmez; kilit diziye konur.
- Toplu görünüm için Ebeveyn denetimi → **Kilitli içerikler** sayfası: Kaynaklar, Kategoriler ve Kanallar ve içerikler başlıkları altında satırlar. Satırda OK → "Kilidi kaldır".

### 3.6 Kilitli içeriği açmak (her yüzey)
Kartta, satırda veya vitrinde OK → `model.open/watch/play` → kapı REQUIRE_PIN der → doğrulama ("“{ad}” kilitli. Açmak için PIN gir.").
- Doğruysa hedef güncel katalogdan yeniden çözülür. Güncel kilit kapsamları oturuma eklenir, işlem yürütülür (ayrıntı, oynatıcı, devam, sonraki bölüm).
- Hedef artık yoksa köşe notu: "Bu içerik artık bulunamıyor."
- Kilitliyken ayrıntı için ağ isteği atılmaz (`loadDetail` kapıdan sonra çalışır).

### 3.7 Oynatıcı
- **CH+/CH− ve kontroller gizliyken Yukarı/Aşağı:** oturumda açılmamış kilitli kanallar atlanır (öneri). Kilitli bir kanal hiç açılmaz, böylece tek akış kuralı korunur. Atlanacak başka kanal yoksa yerinde kalınır.
- **Rakamla kanal:** kilitli kanalın numarası girilirse PIN penceresi açılır. Mevcut kanal arkada oynamaya devam eder, yeni akış PIN'den önce açılmaz. Vazgeç mevcut kanalda bırakır. Doğruysa kanal açılır ve köşe notu numarayla adı gösterir (mevcut `quiet` akışı).
- **Sonraki bölüm:** bölüm dizinin kilidini miras alır. Dizi bu oturumda açıldıysa geçer. Oturum kapandıysa (ör. arka plandan dönüş) otomatik geri sayım durur ve "Sonraki bölüm PIN istiyor. Başlatmak için Sonraki bölüm'e bas." yazar. Elle basınca PIN sorulur.
- **Rehber tuşu:** değişmez.

### 3.8 Kaynak yönetimi ve kaynak kilidi (aşama 3)
- "Kaynak yönetimi PIN istesin" açıkken (varsayılan) aşağıdaki girişler doğrulama ister:
  - `manageSources()` (Ayarlar, rehberdeki Kaynaklar/Kaynakları yönet/Kaynakları aç, Ana Sayfa boş durumundaki "Kaynakları kontrol et", kütüphanedeki boş durum eylemi),
  - Ayarlar'daki tek kaynaklı "Kaynağı düzenle" ve "Kaynağı kaldır".
- Yetki Kaynaklar/Kurulum/Yükleniyor içinde sürer. Ana Sayfa'ya, kütüphaneye vb. dönülünce biter. Kaynak bilgileri ve şifre de böylece korunur.
- Kaynak kilidi: Kaynaklar → Bilgiler penceresinde "Kaynağı kilitle" / "Kaynağın kilidini kaldır". Satıra beşinci düğme konmaz, çünkü büyük yazıda sığmaz.

### 3.9 Açılış PIN'i ve arka plan (aşama 3)
- `onStop` olunca oturum ve ayar yetkisi temizlenir, açık PIN penceresi iptal edilir, Ayarlar kapanır (`lockEpoch`).
- Oynatılan içerik politikaya göre kilitliyse oynatıcıdan çıkılır (`route = playerReturn`; ilerleme PlayerScreen'in ON_STOP'unda zaten kaydedilir). Böylece TV beklemeden dönünce kilitli yayın PIN'siz sürmez.
- "Açılışta PIN iste" açıksa `appLocked = true` olur. Dönüşte ve soğuk açılışta (kaynaklar açıldıktan sonra) tam ekran **AppLockScreen** gelir. Geri uygulamayı kapatır (`finish`).

### 3.10 PIN'i değiştirmek ve kaldırmak
- **Değiştir:** Ebeveyn denetimi (yetki zaten açık) → "PIN'i değiştir" → yeni PIN → tekrar → kayıt (revizyon +1, oturum temizlenir).
- **Kaldır:** "PIN'i kaldır" → onay sayfası "PIN kaldırılsın mı?" (ilk odak Vazgeç) → `ProtectionStore.reset()`. Gizlenen kategoriler ve yetişkin filtresi değişmez.

### 3.11 PIN'i unuttum (sunucusuz, öneri §14-2)
- PIN penceresinde ve AppLockScreen'de "PIN'i unuttum" → onay penceresi (ilk odak Vazgeç) → "Her şeyi sıfırla" → `forgotPinReset()`.
- Sıra önemlidir (yarıda kesilirse açık kalmasın diye):
  1. `transactions.write { artwork.clear(); catalogStore.clear(); epgStore.clear(); store.forgetSource(); store.hideAdultCategories = true }`. Kaynaklar, favoriler, geçmiş ve ilerleme silinir.
  2. Ardından `guard.reset()`: koruma dosyası ve anahtarı silinir.
  3. Sonra `route = SETUP` ve "Sıfırlandı. Kaynağını yeniden ekleyebilirsin."
- Adım 2 başarısız olursa kaynaklar yine silinmiş olur. Aynı yol tekrar çalıştırılabilir (idempotent).
- Caydırıcılık: çocuk sıfırlarsa izleyecek hiçbir kaynak kalmaz ve hesap bilgileri olmadan yeniden ekleyemez. Bu, root'a veya uygulama verisini silmeye karşı koruma değildir (PIN_CREDENTIAL ile aynı sınır).

## 4. Ekranlar ve bileşenler

**Yeni `ui/PinPad.kt`:**
- `PinDialog(prompt, model)`: `SourceDialog(width = 640.dp, scrollBody = false)` içinde.
  - Üstte Heading (amaç başlığı).
  - Altında `Row`: sol sütun (`weight 1`) ile 236 dp'lik tuş takımı.
  - Sol sütun: Note (açıklama), 4 hane kutusu, geri bildirim satırı (yer ayrılmış, boşken `""`).
- Tuş takımı: 3×4 ızgara, `Action` 72 dp genişlik, `actionHeight()` yükseklik, 10 dp aralık. Satırlar `1 2 3 / 4 5 6 / 7 8 9 / [boş] 0 Sil`. Altında eylem satırı: "Vazgeç" (çerçeveli) ve doğrulamada "PIN'i unuttum" (çerçeveli).
- Ölçü: 960×540 dp tuvalde normal yazıda yaklaşık 640×336 dp, fontScale 1,5'te yaklaşık 397 dp (SourceDialog üst sınırı 460 dp). Sol sütun büyük yazıda kaydırılabilir.
- `PinPad(...)`: tuş takımı, hane kutuları ve tuş işleme. Hem pencere hem kilit ekranı kullanır. Hane tamponu `remember { CharArray(4) }` ve `mutableIntStateOf(count)` içinde tutulur.
  - 4. rakamda `val pin = buffer.copyOf(); buffer.fill('\u0000'); model.submitPin(pin)` çağrılır.
  - `DisposableEffect` kapanışta tamponu sıfırlar. PIN hiçbir Compose durumunda `String` olarak bulunmaz.
- Fiziksel tuşlar `onPreviewKeyEvent` ile işlenir. `KEYCODE_0..9` ve `KEYCODE_NUMPAD_0..9` ASCII '0'..'9' olarak eklenir (PlayerScreen'deki eşleme), `KEYCODE_DEL` siler. Odak hangi denetimde olursa olsun çalışır. Beklemede ve kontrol sırasında yok sayılır.
- Hane kutusu: 40×48 dp, `Ink.Background` zemin, `Ink.Outline` 1 dp kenar. Dolu kutuda "•" (`Ink.Text`, 24 sp). Sıradaki kutunun altında mint çizgi var; bu odak değil, kutular odak almaz.
- Yön: tuş takımı ve kutular `LocalLayoutDirection provides Ltr` içinde. RTL dillerde de telefon tuş sırası korunur (manifestte `supportsRtl=true`).
- **AppLockScreen:** tam ekran `Ink.Background`. Solda `NextvLogo(28.dp)`, Heading "NEXTV kilitli", açıklama, kutular ve geri bildirim; sağda `PinPad` ve "PIN'i unuttum". Kayıt okunamıyorsa (Unavailable) metin değişir, eylemler "Yeniden dene" ve "Sıfırla…" olur.
- **Odak dili (D064):**
  - Tuşlar, Vazgeç ve PIN'i unuttum dolgulu denetimdir; odakta mint dolgu.
  - Ayarlar satırları, kategori satırları ve kartlar yüzeydir; odakta beyaz 2 dp halka.
  - Odakta büyüme yok (`scale = 1f`), sallama veya titreşim yok.
  - Devre dışı tuş, `Action`'ın `canFocus = enabled || focused` kuralıyla odağı korur.
- **Hareket:** yalnız LoadingArc var; Hareketi azalt açıkken zaten durur. Kutular anında dolar.
- **TalkBack:**
  - Başlıkta `heading()`.
  - Kutu satırının açıklaması "PIN, 4 haneden {n} hane girildi" (rakam okunmaz), `liveRegion = Polite`.
  - Hata satırı `liveRegion = Assertive`; hata yalnız renkle anlatılmaz, metin her zaman var.
  - Bekleme sayacı bir kez okunur ("{n} saniye sonra tekrar deneyebilirsin"); görsel sayaç `clearAndSetSemantics`.
  - "Sil" açıklaması "Son rakamı sil".

**`ui/SettingsScreen.kt`:**
- `SettingsPage` sırası: `PARENTAL, PARENTAL_LOCKS, PARENTAL_REMOVE` eklenir; `FORGOT` onayı ortak pencere olarak `PinPad.kt` içinde.
- İçerik bölümü:
  - "Yetişkin kategorilerini gizle": PIN varken açıklamanın sonuna " Kapatmak PIN ister." eklenir.
  - "Kategorileri yönet".
  - Yeni **"Ebeveyn denetimi"** satırı: açıklama PIN yokken "PIN oluştur; kategori, kanal ve kaynakları kilitle.", PIN varken "PIN açık · {n} kilit"; `stateDescription` "PIN açık" / "PIN yok".
- PARENTAL sayfası:
  - PIN yokken: Note + "PIN oluştur" + "Yetişkin kategorileri PIN olmadan da gizli kalır."
  - PIN varken: Note, "PIN'i değiştir", "Kilitli içerikler", "Kaynak yönetimi PIN istesin" (anahtar, aşama 3), "Açılışta PIN iste" (anahtar, aşama 3), "Açılan kilitleri kapat" (yalnız oturumda açılan kilit varken), "PIN'i kaldır".
  - UNRESOLVED sayısı > 0 ise not: "{n} içerik eski kayıt nedeniyle gizlendi. Kataloğu yenileyince görünür."
- Sayfa geçişi arayüzde kalır:
  - Satıra OK → `needsAdmin && !s.parental.adminUnlocked` ise `pendingPage = X; model.requestAdmin(reason)`.
  - `LaunchedEffect(s.parental.adminUnlocked)` sayfayı açar. İstem iptal edilirse `pendingPage` temizlenir.
- ViewModel gevşetme yapan her yöntemde yetkiyi ayrıca denetler (savunma derinliği).
- `SettingRow` yeni `chip: String?` parametresi alır (üç durumlu satır için `Role.Button` + `stateDescription`). `CategoryList` bu parametreyi ve `CategoryAccessDialog`'u (SourceDialog, büyük yazıda `scrollBody = true`) kullanır.
- Kategoriler sayfası notu: "Gizlediğin kategoriler hiçbir yerde görünmez. Kilitlediğin kategoriler görünür kalır; içindekiler PIN ile açılır."
- Kapat çağrısı: `Settings(state, model) { settings = false; model.closeSettings() }`. "Kaynakları yönet", "Kaynağı düzenle" ve "Kaynağı kaldır" satırları önce modeli çağırır, sonra `dismiss()` (çifte PIN olmasın).
- Hakkında metni: aşama 1'den sonra "PIN, yetişkin filtresini ve kategori ayarlarını korur." Aşama 2'den sonra "Ebeveyn PIN'i ile kategori, kanal ve kaynak kilitlenebilir; PIN yalnız bu cihazda şifreli saklanır." "PIN yönetimi henüz yok" ancak ilgili kapsam geçince kalkar (PARENTAL_ACCESS teslim 6).

**Rozetler ve yüzeyler (aşama 2):**
- `ui/DesignSystem.kt` `iconPaths` içine "lock" eklenir: `M6 11h12v10H6ZM8 11V8a4 4 0 0 1 8 0v3`. Yeni varlık veya yazı tipi gerekmez.
- `MediaCard` (PilotApp), `HomeCard` (HomeScreen), `SearchCard` (SearchScreen) `locked: Boolean` alır:
  - Afişte sağ üstte koyu hap içinde 14 dp kilit (puan rozeti solda kalır).
  - Canlı kartta adın yanında kilit; "Şimdi:" ve "Sonra:" satırları yerine "Kilitli kanal".
  - `contentDescription = "{başlık}, kilitli"`.
- `LibraryScreen.CategoryColumn`: kilitli kategori satırında etiketten sonra kilit; açıklama "{ad}, kilitli". Kategori seçmek PIN istemez, içerik listelenir.
- `GuideScreen`: kilitli kanal satırında program hücreleri yerine görünüm boyunca tek bir "Kilitli kanal" hücresi (`Ink.Muted`). OK → `model.open(entry)` → PIN → canlı yayın. Gelecek program ayrıntısı kilitliyken açılmaz.
- `HomeScreen`: `homeLibrary(..., locked)` kilitlileri vitrinden (`newest`, `featured`) ve otomatik raflardan (`channels`, `movies`, `series`) çıkarır. Devam, son kanallar ve favoriler rozetle kalır. `ensureHeroDetail` kilitliler için çağrılmaz.
- Köşe notu: PlayerScreen'deki not kutusu ortak `CornerNote` olarak çıkarılır. `PilotApp` bunu `state.accessNotice` için 3 sn gösterir (Polite).
- `PilotApp` yerleşimi:
  - `appLocked` ise ve rota LOADING/RECOVERY değilse rota içeriği yerine `AppLockScreen` çizilir.
  - Değilse rota içeriğinin, Ayarlar'ın ve çıkış penceresinin ardından `state.pinPrompt?.let { PinDialog(it, model) }` çizilir; Ayarlar'ın üstünde açılır.
  - `LaunchedEffect(state.lockEpoch) { settings = false }`.

## 5. Veri modeli ve kalıcılık

| Veri | Yer | Not |
|---|---|---|
| PIN doğrulayıcısı, deneme sayacı, kurallar (Source/Category/Content, `pinRequired=true`), seçenekler | `ProtectionStore` → `files/protection/primary.bin`, ProtectionCodec **v2**, Keystore AES-GCM | Tek atomik kayıt. Kural, PIN veya seçenek değişince `policyRevision` +1 |
| Gizlenen kategoriler, yetişkin filtresi | `DeviceStore` `hiddenCategories`, `hideAdultCategories` (değişmez) | Yalnız adlar. PIN varken gevşetme yetki ister |
| Açma oturumu, ayar yetkisi, `appLocked`, bekleyen işlem, ilk adım PIN'i | Yalnız bellek (`TvViewModel`) | `onStop`, PIN değişimi ve kilit gevşemesinde temizlenir |

**Yeni DeviceStore anahtarı yok.** Korumayla ilgili her şey şifreli ve atomik kayıtta tutulur. Bu, D049 sözleşmesine uyar ve yedeğe gitmez (`allowBackup=false`).

**`ProtectionSnapshot`:**
- Yeni alan `options: ProtectionOptions(sourcesNeedPin = true, launchNeedsPin = false)`.
- Codec v2 sihirli değeri `0x50525602`. Kurallardan sonra 1 bayt bayrak yazılır: bit0 `sourcesNeedPin`, bit1 `launchNeedsPin`; diğer bitler 0 olmalı, değilse kayıt reddedilir.
- `decode` v1 (`0x50525601`, varsayılan seçeneklerle) ve v2 kayıtlarını okur; `encode` her zaman v2 yazar.
- `ProtectionStore.write` içindeki `changed` hesabına `options` eklenir.

**`ProtectionStore.reset()` (yeni):**
- `synchronized(lock)` içinde önce `primary.bin`, `.bak` ve `.new` dosyaları silinir, sonra Keystore alias'ı.
- Arada çökerse Unavailable olur; aynı sıfırlama tekrar çalışır.
- Yalnız "PIN'i kaldır" (yetkili) ve "PIN'i unuttum" çağırır. PROTECTION_STORE belgesindeki "No reset API" bu iki çağıranla güncellenir.
- JVM testleri için `internal interface ProtectionStorage { read(); write(snapshot, expectedGeneration); reset() }` eklenir, `ProtectionStore` bunu uygular.

**Kategori kuralı eşlemesi:**
- Ayarlar satırı bölüm + ad anahtarıyla ve bütün kaynaklarda çalışır, D068 ile aynı. Politika ise kaynağa ve tipe bağlıdır.
- `ParentalProjection.categoryScopes(sources, rawCatalogs, kind, name)` her kaynak için ham `snapshots[id].catalog` içinden kapsamı çıkarır:
  - Xtream: o bölümde bu adı taşıyan kayıtların farklı `categoryId` değerleri → `ProviderId`.
  - M3U: `M3uName(name)`.
  - `categoryId` boş eski Xtream kayıtları atlanır; bunlar UNRESOLVED olup gizlenir, katalog yenilenince düzelir.
- Satır durumu, kurallardan geriye eşlenerek "Kilitli" hesaplanır.
- **Uzlaştırma:** her `publishLibrary` sonunda kilitli bir satırın adına yeni bir kapsam eklenmişse (sağlayıcı aynı adla yeni kimlik açtı ya da yeni kaynak eklendi), kural yetkisiz eklenir; bu sıkılaştırmadır.
- M3U'da ad değişince kilit düşer; D068 gizlemesiyle aynı sınır, belgelenir.
- Kural sınırı 1024'tür; aşılırsa "Kilit sınırına ulaşıldı. Bazı kilitleri kaldır."

## 6. Kontrolcü, oturum ve kapı (yeni saf sınıflar)

**`data/AndroidPinClock.kt`:**
- `fun interface PinClockSource { fun now(): PinClock }`.
- Android uygulaması `PinClock(Settings.Global.getInt(cr, BOOT_COUNT), SystemClock.elapsedRealtime())` döndürür (API 24+).
- BOOT_COUNT okunamazsa `boot = 0` kullanılır ve geri giden monoton zamanla yeniden başlatma algılanır. Bu açık ve belgelenmiş bir yedektir. PIN_ATTEMPTS'e eklenir: daha uzun açık kalan bir yeniden başlatmada bekleme en çok kalan süre kadar kısalabilir.

**`PinAttempts.waitMs(now): Long` (yeni, saf):** `reserve` ile aynı yeniden tabanlama kuralını hak düşmeden uygular. Pencere bekleme hâlinde açılabilsin diye.

**`data/ParentalGuard.kt`:**
- `class ParentalGuard(storage, clock, io = Dispatchers.IO, cpu = Dispatchers.Default)`, tek bir `Mutex` ile sıralı çalışır.
- `read(): ProtectionRead`.
- `create(pin, options)`: yalnız Unconfigured'da; `write(gen 0, expected = null)`, deneme sayacı `clear(now)`.
- `verify(pin): Verify`, sonuç Correct(snapshot) | Wrong(snapshot, charged, retryAfterMs) | Wait(retryAfterMs) | Failed:
  1. Okunur; Ready değilse Failed.
  2. `reserve(now)` çağrılır. `mayVerify == false` ise tabanlanmış durum değiştiyse yazılır ve Wait döner.
  3. Rezervasyon `write(gen+1)` ile yazılır. Yazma başarısızsa Failed döner ve **karşılaştırma yapılmaz**.
  4. `credential.matches(pin)` `cpu` üzerinde çalışır.
  5. Doğruysa `attempts = clear(now)` yazılır; ancak bu yazma başarılıysa Correct döner.
- `replacePin(newPin, expected)`, `edit(expected) { rules, options -> … }`: revizyon değiştiyse +1.
- `reset()`.
- Kural: PIN yalnız `CharArray` olarak gelir ve tüketilir. İptal (Vazgeç) hakkı geri vermez (D048).

**`data/ParentalSession.kt`:**
- `grants: Set<AccessScope>`, `admin: Boolean`.
- `covers(pinScopes) = pinScopes.all { it in grants }`, `grant(scopes)`, `clear()`.
- `onPolicyChanged(old, new)`: doğrulayıcı baytları farklıysa ya da `new.rules` eski `pinRequired` kuralların hepsini içermiyorsa temizler; yalnız ekleme varsa korur.

**`AccessPolicy.evaluate(...)` (ek):**
- `data class AccessVerdict(val decision: AccessDecision, val pinScopes: Set<AccessScope>)`.
- `pinScopes`, `matches` içindeki `pinRequired` kuralların kapsamlarıdır: doğrudan, ebeveyn içerik ve kategori.
- `decide()` artık `evaluate().decision` döndürür; mevcut 12 test aynen geçmeli.

**`data/AccessTargets.kt` (saf):**
- `resolve(entry, allCatalog, selected, episodes): Target?`: kanonik kayıt ve `SeriesContext`.
  - Kütüphane kaydı `allCatalog.items` içinde kimliğiyle bulunmalı. D068 ile gizlenen burada olmadığı için hiçbir yoldan açılamaz.
  - Bölüm için kanonik `selected` (SERIES) + `episodes` üyeliği, ya da `allCatalog.m3uEpisodes` listesi aranır.
- `nextZap(channels, currentId, direction, allowed): MediaEntry?`: kilitliyi atlar, uçlarda döner, `null` ise yerinde kalınır.

**`data/ParentalProjection.kt` (saf, `Dispatchers.Default`):**
- `project(sources, raw, visible, policy)` → `ParentalView(lockedIds, lockedCategoryRows, lockedSources, contentLocks, unresolvedIds, missingRules)`.
- Kural yoksa hızlı yol: aynı katalog örneği döner, hesap yapılmaz.

## 7. ViewModel API (`TvViewModel`)

**Durum (`TvState`):**
- `parental: ParentalUi`: status LOADING/OFF/ON/UNAVAILABLE, `sourcesNeedPin`, `launchNeedsPin`, `lockedIds`, `lockedCategories` (`CategoryVisibility.key` biçiminde), `lockedSources`, `contentLocks`, `unresolved`, `adminUnlocked`, `sessionGrants: Int`, `appLocked`.
- `pinPrompt: PinPrompt?`: `id`, `purpose`, `step` NEW/CONFIRM/VERIFY, `title`, `message`, `phase` Entry/Checking/Waiting(retryAtElapsedMs), `feedback`, `canForget`. Sır taşımaz.
- `accessNotice: String?`, `lockEpoch: Int`.

**Özel alanlar:** `guard`, `session`, `snapshot: ProtectionSnapshot?`, `policy: AccessPolicy`, `pendingNew: CharArray?`, `pinJob`.

**Yeni yöntemler:**
- `submitPin(pin: CharArray)`, `cancelPin()`, `forgotPinReset()`, `retryProtection()`.
- `requestAdmin(reason: AdminReason, then: AdminFollowUp? = null)`: `then` değerleri ShowAdult | OpenSources | EditSource(id) | RemoveSource(id) | AddSource | UnlockScope(scope) | RemovePin. `closeSettings()`.
- `createPin(then: AdminFollowUp? = null)`, `changePin()`, `removePin()`.
- `setHideAdult(hide)`, `setCategoryAccess(kind, name, CategoryAccess.SHOWN | LOCKED | HIDDEN)`. Mevcut `categoryVisible` ve `hideAdultCategories` bunlara dönüşür.
- `setContentLock(entry, locked)`: hedef `FavoriteTargets.resolve` ile aynı kuralla seçilir, bölümde dizi. `setSourceLock(id, locked)`, `setSourcesNeedPin(v)`, `setLaunchNeedsPin(v)`, `relockNow()`.
- `appStopped()` ve `appStarted()`: `MainActivity.onStop` / `onStart` çağırır.

**Değişen yöntemler:**
- `open`, `watch`, `play`: önce `access(entry, pending)`. ALLOW ise devam eder. REQUIRE_PIN ise `pinPrompt` = Unlock(pending) olur ve işlem yapılmaz. UNRESOLVED ise not "Bu içeriğin kilit bilgisi doğrulanamadı. Kataloğu yenileyip tekrar dene." DENIED ise sessiz.
  - `pending` hedef kimliği taşır, lambda taşımaz: `Open(id, resumeId)`, `Watch(id)`, `Play(id, restart)`, `PlayNext(sourceId, currentId, nextId)`.
  - Doğru PIN'den sonra `resolve` ve `evaluate` yeniden çalışır, güncel `pinScopes` oturuma eklenir, işlem yürütülür.
- `loadDetail` / `retryDetail` kapıdan sonra çalışır. `ensureHeroDetail` `lockedIds` içindekileri atlar.
- `changeChannel(direction)`: `AccessTargets.nextZap(..., allowed = { access(it) == ALLOW })`.
- `playChannelNumber(n)`: kilitli kanalda istem açar ve `true` döner; PlayerScreen "kanal yok" demez.
- `playNextEpisode(...)`: `NextResult.STARTED | NEEDS_PIN | UNAVAILABLE` döner. Otomatik geçişte NEEDS_PIN istem açmaz; elle basılınca açar.
- `manageSources`, `addSource`, `editSource`, `removeSource`: `sourcesNeedPin && ON && !admin` ise istem açılır.
- `back()`, `home()`, `connect` ve `removeSource` tamamlanınca rota {SOURCES, SETUP, LOADING} dışına çıkıyorsa ayar yetkisi biter.
- `restore()`: kaynak kayıtlarından önce `guard.read()` (IO) çalışır. Durum yüklenmeden kapı hiçbir şeyi açmaz. Ready + `launchNeedsPin` ya da Unavailable ise `appLocked = true`.
- `publishLibrary()`:
  1. `CategoryVisibility.apply` uygulanır.
  2. `ParentalProjection.project` çalışır; `unresolvedIds` `allCatalog`'dan çıkar.
  3. `lockedIds` yayımlanır.
  4. `missingRules` boş değilse `guard.edit` ile eklenir.
- Kural veya PIN değiştikten sonra `policy` yeniden kurulur ve `publishLibrary()` çağrılır (seyrek bir işlem).

**`ui/PlayerScreen.kt`:** ON_START gözlemcisi `player.play()` öncesinde `model.state.value.route == Route.PLAYER && !appLocked` koşulunu arar; kilit ekranının arkasında ses kısa süre bile çalmaz.

**`ui/EpisodeEndController.kt`:** geri çağrı `NextResult` döner. NEEDS_PIN durumunda `next` korunur, `seconds = null` olur, not yazılır.

## 8. Erişim yolları: hepsi kapıdan geçer

| Giriş (kod) | Çağrı | Kapı |
|---|---|---|
| Kütüphane kartı, favoriler (`LibraryScreen` MediaCard) | `open` | open |
| Hızlı menü İzle/Ayrıntılar (`QuickActions`) | `open` | open |
| Ara sonucu (`SearchScreen` SearchCard) | `open` | open |
| Rehber: yayındaki program, "Canlı kanalı izle" (`GuideScreen.open`, satır 350) | `open` | open; satırda "Kilitli kanal" |
| Vitrin İzle ve Ayrıntılar (`HomeScreen` Hero) | `watch` / `open` | watch / open; kilitliler vitrinde yok |
| Raflar, devam, son kanallar (`HomeCard`) | `open(entry, record)` | open → `loadDetail` → play |
| Ayrıntı Oynat, Devam et, Baştan başlat, bölümler (`DetailScreen`) | `play` | play |
| Kanal numarası (`PlayerScreen` digits) | `playChannelNumber` | istem; arkada mevcut kanal |
| CH±, Yukarı/Aşağı, "‹ Kanal / Kanal ›" | `changeChannel` | kilitliyi atlar |
| Sonraki bölüm ve geri sayım | `playNextEpisode` | NEEDS_PIN akışı |
| Kaynak yönetimi girişleri (Ayarlar, Rehber, Ana Sayfa boş, Kütüphane boş) | `manageSources` / `editSource` / `removeSource` / `addSource` | ayar yetkisi (aşama 3) |
| Derin bağlantı (bugün yok) | gelecekte `openRef(ref)` | önce `appLocked`, sonra open |
| Watch Next (BKL ayrı küme, sonra) | yayımlama | `lockedIds` hiç yayımlanmaz; açılış PIN'i açıkken hiçbir şey yayımlanmaz; yeni kilitte kayıt kaldırılır; tıklama `openRef` |
| Oynatıcı üstü kanal listesi (P0 boşluk satırı 2, ayrı küme) | `play` | `lockedIds` rozeti; numarayla aynı istem |

## 9. Kenar durumları ve Türkçe metinler

| Durum | Metin / davranış |
|---|---|
| Yanlış PIN (1–3. deneme) | "PIN yanlış. Tekrar dene." |
| Yanlış PIN (4. deneme) | "PIN yanlış. Bir yanlış deneme daha 30 saniye bekletir." |
| Bekleme | "Çok fazla yanlış deneme. {d:ss} sonra tekrar dene." Tuşlar kapalı; Vazgeç ve PIN'i unuttum açık. Süre yeniden başlatmada sıfırlanmaz (D048) |
| Kayıt okunamadı veya yazılamadı | "PIN şu an kontrol edilemiyor. Tekrar dene." Karşılaştırma yapılmaz, izin yok |
| Kontrol sürerken Vazgeç | Pencere kapanır; hak harcanmış sayılır (D048), aynı PIN kendiliğinden yeniden gönderilmez |
| Hedef kayboldu (kaynak silindi, yenileme) | Köşe notu "Bu içerik artık bulunamıyor." |
| Bölümün dizi bağlamı yok ve kural var | "Bu bölüm için önce diziyi aç." |
| Eski Xtream kaydında kategori kimliği yok | İçerik gizlenir; Ebeveyn denetimi notu: "{n} içerik eski kayıt nedeniyle gizlendi. Kataloğu yenileyince görünür." |
| Koruma kaydı okunamıyor (UNAVAILABLE) | Kilit ekranı: "Ebeveyn denetimi kaydı açılamadı" / "Kilitli içerikleri korumak için uygulama kapalı tutuluyor. Kayıt onarılamazsa bu cihazdaki kaynakları ve kilitleri sıfırlayıp yeniden kurabilirsin." Seçenekler: "Yeniden dene", "Sıfırla…" |
| PIN oluşturuldu | "PIN oluşturuldu. Kilitler artık bu PIN'i ister." |
| PIN kaydedilemedi | "PIN kaydedilemedi. Boş alanı kontrol edip tekrar dene." |
| PIN'i kaldır onayı | "PIN kaldırılsın mı?" / "Bütün kategori, kanal ve kaynak kilitleri kaldırılır. Gizlediğin kategoriler gizli kalır; yetişkin filtresi değişmez." Seçenekler: Vazgeç / "PIN'i kaldır" |
| PIN'i unuttum | "PIN'i unuttun mu?" / "PIN yalnız bu cihazda şifreli saklanır ve hiçbir sunucuya gönderilmez; bu yüzden geri getirilemez. Sıfırlamak için bu cihazdaki kaynaklar, favoriler, izleme geçmişi ve bütün kilitler silinir. Kaynak bilgilerini yeniden girmen gerekir." Seçenekler: Vazgeç / "Her şeyi sıfırla"; sürerken "Bu cihazdaki kaynaklar ve kilitler temizleniyor…" |
| İstem başlıkları | "PIN oluştur", "PIN'i tekrar gir", "PIN gir", "NEXTV kilitli" |
| İstem açıklamaları | "“{ad}” kilitli. Açmak için PIN gir.", "Yetişkin kategorilerini göstermek için PIN gir.", "Kategori ayarlarını açmak için PIN gir.", "Ebeveyn denetimi ayarlarını açmak için PIN gir.", "Kaynak ayarlarını açmak için PIN gir.", "Kilidi kaldırmak için PIN gir.", "Devam etmek için PIN gir." |
| Kilitli içerikler sayfası boş | "Kilitli kanal veya içerik yok. Bir kartta OK'yi basılı tutup Kilitle'yi seçebilirsin." |
| Ebeveyn denetimi notu | "PIN; kilitli kategori ve kanalları açarken, yetişkin filtresini kapatırken ve kategori ayarlarında istenir. Kilitli kanallar CH+/CH− ile atlanır; numarasıyla ya da listeden PIN ile açılır." |
| Açılan kilitleri kapat | "Bu oturumda PIN ile açtığın içerikler yeniden kilitlenir." |
| Kaynak kilidi açıklaması | "Bu kaynaktaki bütün kanallar, filmler ve diziler PIN ister." |
| Ayar yazılamadı | "Ayar kaydedilemedi. Tekrar dene." (koruma değişmez) |

Dil kuralı (PLAY_STORE_READINESS 3.2/3.5): içeriği öven veya tanıtan ifade kullanılmaz. "Yetişkin içeriği aç" yerine tarafsız "Yetişkin kategorilerini göster" yazılır.

## 10. Diller, ülkeler ve ücretli paket

- **Ücretsiz:** ebeveyn denetimi hiçbir paket kontrolüne bağlanmaz; her ülkede ve her dilde aynıdır (öneri §14-5). Gerekçe: Play katalog uygulamalarındaki yetişkin içerik istisnası, reşit olmayanların erişiminin kısıtlanması şartına bağlı. Bu kısıtı ödeme arkasına koymak onu yalnız ödeyenlere vermek olur. Satış ve fiyat kararında yazılı kural olarak DECISIONS'a girmeli.
- **Sunucu yok:** PIN, kilit ve sıfırlama cihazdadır. Ülkeye göre arka uç, hesap veya veri aktarımı gerekmez (KVKK/GDPR açısından cihazdan veri çıkmaz).
- **Yerelleştirme:**
  - Bütün yeni metinler tek bir yerde toplanır: `ParentalCopy` nesnesi ya da, i18n kümesi kaynak dosyalarına geçtiyse `strings.xml`.
  - Rakam girişi her dilde ASCII 0–9'dur.
  - Tuş takımı RTL'de de soldan sağa kalır.
  - Bekleme sayacı `Locale.ROOT` ile biçimlenir.
- **Diğer dillerde yetişkin filtresi:** `CategoryVisibility.adultWords` yalnız TR/EN biçimlerini yakalar. `adults?` sonrasında harf gelmesine izin vermediği için "Adultos", "Adultes", "Adulti" ve "Erwachsene" eşleşmez. Çok ülkeli satıştan önce kelime listesinin testle genişletilmesi önerilir (ayrı, S, yaklaşık 0,5 gün). D068 varsayılanını etkilediği için i18n kümesiyle birlikte karara bağlanmalı.

## 11. Testler

**JVM önce (`app/src/test`):**
1. `AccessPolicyTest`: `evaluate` kaynak, içerik, kategori ve dizi mirasında doğru `pinScopes` döndürür; mevcut 12 testte `decide` ile aynı sonuç.
2. `ProtectionCodecTest`: v2 seçeneklerle gidiş-dönüş; v1 kaydı varsayılan seçeneklerle okunur; bilinmeyen bayrak biti reddedilir; `encode` v2 yazar.
3. `PinAttemptsTest`: `waitMs` hak düşmez, aynı boot ve yeniden başlatma tabanlaması `reserve` ile tutarlı.
4. `ParentalGuardTest` (yeni; sahte ProtectionStorage, sahte saat, sentetik PIN):
   - Rezervasyon yazılmadan karşılaştırma yok (yazma hatası → Failed, yazma sırası doğrulanır).
   - Doğru PIN'de sıfırlama yazısı başarısızsa Correct yok.
   - Bekleme hak düşmez; tabanlanmış durum kalıcı.
   - İki eşzamanlı `verify` sıralanır.
   - `create` yalnız Unconfigured'da çalışır.
   - `replacePin` ve `edit` revizyonu artırır; seçenek değişimi revizyon ister.
   - `reset` çağrılır.
   - KDF yavaş olduğu için PBKDF2 çağrısı az tutulur.
5. `ParentalSessionTest`: kısmi kapsam açmaz; PIN değişimi ve kural gevşemesi temizler; yalnız ekleme korur.
6. `ParentalProjectionTest`:
   - Kategori, içerik ve kaynak kuralıyla `lockedIds`.
   - İki kaynakta aynı ad → iki kapsam.
   - Xtream'de aynı ad, yeni kimlik → `missingRules`.
   - M3U ad değişimi kilidi düşürür (belgelenen sınır).
   - UNRESOLVED süzülür; D068 gizlileri dikkate alınmaz.
   - Kural yokken aynı katalog örneği döner.
7. `AccessTargetsTest`:
   - Eski veya gizli kayıt çözülmez.
   - Bölüm bağlamı `selected`/`episodes` ya da `m3uEpisodes` listesinden gelir.
   - `nextZap` kilitliyi atlar, uçlarda döner, hepsi kilitliyse `null`.
8. `HomeLibraryTest`: kilitliler vitrinde ve otomatik raflarda yok; devam, son kanallar ve favorilerde var.
9. `EpisodeEndControllerTest`: otomatik geçişte NEEDS_PIN `next`'i korur ve not gösterir; elle basmada katman kalır.
10. `PinFormatTest`: 4 hane; kolay PIN listesi.

**Cihaz (`app/src/androidTest`, ayrılmış 5556 emülatörü, `FixtureSources` sentetik):**
1. `ProtectionStoreTest`: `reset()` sonrası Unconfigured; v1 dosyası okunur, v2 yazılır; seçenek değişimi revizyon ister.
2. `AndroidPinClockTest`: BOOT_COUNT ≥ 0, elapsed tekdüze.
3. `ParentalFlowTest` (yeni):
   - D-pad ve KEYCODE rakamlarıyla PIN oluşturma; eşleşmeme yolu.
   - Filtreyi kapatırken Geri → ayar değişmez.
   - Kategori kilitle → rozet → OK → PIN → Geri ile ayrıntı veya oynatıcı açılmaz → doğru PIN ile açılır.
   - Ara ve rehberde kilitli satır.
   - Oynatıcıda rakamla kilitli kanal → arkada oynayan kimlik PIN'e kadar değişmez; CH+ kilitliyi atlar.
   - 5 yanlış → bekleme metni ve tuşlar kapalı.
   - PIN'i unuttum → SETUP ve Unconfigured.
   - Açılış PIN'i: `ActivityScenario` CREATED → RESUMED → kilit ekranı; kilitli film oynarken arka plana gidince oynatıcıdan çıkılır.
4. Yerleşim: fontScale 1,0/1,5/2,0'da PIN penceresi ve kilit ekranı 960×540 içinde (tuş takımı ve Vazgeç görünür), SetupLayoutTest kalıbıyla.
5. Semantik: kutu açıklamasında rakam yok, hata satırı liveRegion; kartta "kilitli".

Hızlı yinelemede yalnız ilgili testler koşar; tam cihaz turu yayın öncesidir (proje kuralı). Test PIN'leri sentetiktir, gerçek hesap bilgisi kullanılmaz.

## 12. Dokunulacak dosyalar
Bkz. `files_touched`. Belgeler:
- DECISIONS: yeni karar kaydı, öneri olarak.
- PARENTAL_ACCESS, PIN_ATTEMPTS (saat adaptörü), PROTECTION_STORE (reset, v2), ACCESS_DECISIONS (`evaluate`).
- SCREENS TV-071, PRD SAFE01, PLAY_STORE_READINESS 3.5, TIVIMATE_GAP satırı, BACKLOG BKL-028.
- STATUS / HANDOFF.

## 13. Aşamalar ve efor

1. **PIN ve ayar koruması (yaklaşık 3,5 gün):**
   - AndroidPinClock, `PinAttempts.waitMs`, ParentalGuard, codec v2, `ProtectionStore.reset`, PinPad/PinDialog.
   - Ebeveyn denetimi sayfası (oluştur, değiştir, kaldır), PIN'i unuttum sıfırlaması.
   - Yetişkin filtresini kapatma ve Kategorileri yönet (açma ve gösterme) için yetki.
   - `AccessTargets.resolve` ile open/play'de kanonik kayıt denetimi: gizlenen hiçbir yoldan açılamaz.
   - Hakkında metni ve testler.
2. **İçerik kilitleri (yaklaşık 4,5 gün):**
   - `evaluate`, ParentalProjection (uzlaştırma dahil), ParentalSession, ViewModel kapısı ve bekleyen işlem.
   - Rozetler (kart, kategori sütunu, Ara, rehber satırı), Ana Sayfa vitrin ve raf dışlaması.
   - CategoryAccessDialog, hızlı menüde Kilitle/Kilidi kaldır, Kilitli içerikler sayfası.
   - CH± atlama, rakam istemi, `NextResult`, Açılan kilitleri kapat.
   - `onStop`'ta oturumun temizlenmesi ve kilitli oynatımdan çıkış; testler.
3. **Kaynak ve açılış koruması (yaklaşık 1,5 gün):**
   - Kaynak kilidi, "Kaynak yönetimi PIN istesin" ile bütün kaynak girişleri.
   - "Açılışta PIN iste", AppLockScreen, Unavailable kilit ekranı, PlayerScreen ON_START koruması; testler.
4. **Cihaz turu ve belgeler (yaklaşık 1 gün):** 65U95A'da KDF gecikmesi ölçümü dahil.

Toplam yaklaşık **10,5 mühendis-günü (L)**. Gap tablosundaki "M" yalnız bağlama işini sayıyordu; bütün erişim yolları ve yaşam döngüsüyle L.

## 14. Sahip kararları (varsayılanla iş ilerler)

Bkz. `owner_decisions`. Belgelerde bunlar onay gibi yazılmaz; seçilince DECISIONS'a karar kaydı olarak girer.

## 15. Riskler
Bkz. `risks`.