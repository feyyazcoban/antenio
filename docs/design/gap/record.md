# Kayıt (anlık ve zamanlanmış, cihaz/USB), canlı duraklatma ve geri sarma (timeshift), çoklu ekran (2–4 yayın)

18 Ekim 2026 · TiviMate gap tasarımı (`record`), salt okunur tasarım ajanı; öneriler kullanıcı kararı değildir.

**Özet:** Üç yetenek ortak bir yakalama motoruna dayanıyor. Motor OkHttp ile tek bağlantıdan TS ya da HLS okuyor ve klasöre yerel HLS (index.m3u8 + parçalar) yazıyor. Media3 bu büyüyen EVENT listesini oynatabildiği için "kaydederken izle", geri sarma ve kayıt oynatma tek yoldan çözülüyor; parçalı yazım FAT32 USB sınırına da takılmıyor. Teslim yedi aşamada öneriliyor: önce bağlantı bütçesi ve sağlayıcının HLS penceresinde geri sarma (S), sonra motor ve anlık kayıt (L). Ardından duraklatınca başlayan arabellek, USB, zamanlanmış kayıt (exact alarm ve dataSync ön plan hizmeti, Android 12/14/15 kısıtlarıyla), 2–4 ekran ve son olarak "her zaman arabellek" geliyor; toplam yaklaşık 42 mühendis-günü. Kurallar kodda karşılığını buluyor: paralel yayın yalnız bilinen bağlantı sınırı içinde açılıyor (Xtream max_connections ya da izleyicinin girdiği değer; ikisi de yoksa sınır 1), adres ve kimlik bilgisi diske yazılmıyor, şifreli yayın kaydedilmiyor, film/dizi için "İndir" yok. Kullanıcının asıl sorduğu fiyat ve dil konusuna bu kümedeki bağlantı: üç özellik bir FeatureGate arkasında kuruluyor, ücretli paket kararı sahipte. Yeni metinler ileride çevrilebilsin diye strings.xml'e alınıyor.

**Tahmini iş:** 42 gün

**Aşamalar:**
1. Aşama 0 (S, 3 g): StreamBudget, Xtream max_connections ve Ayarlar → Bağlantı sınırları; HLS sağlayıcı penceresinde canlı geri sarma (T0). Sol/Sağ, −10/+10 ve Canlıya dön gelir; motor gerekmez.
2. Aşama 1 (L, 14 g): Yakalama motoru (TsSegmenter, HlsPlaylistReader/HlsCapture, LocalPlaylist, CaptureHub); ilk iş Media3'ün yerel EVENT listesini oynattığının cihaz testiyle doğrulanması. dataSync RecordingService, şifreli RecordingStore, oynatıcıda Kaydet/Kaydı durdur, Kayıtlar ekranı (kaydediliyor ve kaydedilenler; oynat, sil), cihaz depolaması ve kota.
3. Aşama 2 (M, 5 g): Duraklatınca arabellek (T1) ile TS ve penceresiz HLS'de duraklat, geri sar ve Canlıya dön; aynı kanalı kayıttan izleme (tek bağlantı); geri sarmayı kayda yükseltme.
4. Aşama 3 (S, 3 g): USB hedefi (getExternalFilesDirs), Kayıt yeri sayfası, takılı olmayan ya da salt okunur birim durumları.
5. Aşama 4 (M–L, 6 g): Zamanlanmış kayıt: rehberde Kaydı planla, hızlı menü ve işaret; exact alarm, açılış ve saat alıcıları; izin akışı ve izin yokken uygulama içi zamanlayıcı; çakışma diyaloğu ve geri sayım kartı.
6. Aşama 5 (L, 7 g): Çoklu ekran (2–4): bağlantı uyarısı (ilk odak Vazgeç), düzenler, odaktaki karoda ses, kanal seçici, cihaz kademesi, karo başına bütçe.
7. Aşama 6 (M, 4 g): 'Her zaman' arabellek seçeneği (ölçüm kapısıyla), elle zamanlı kayıt, EPG saat kaymasında planı taşıma, fiziksel cihaz matrisi.

**Kullanıcı kararları (varsayılanla):**
- Kayıt, canlı geri sarma ve çoklu ekran ücretsiz mi olsun, ücretli pakette mi (PRD §12 gelir modeli)? → varsayılan: Hepsi bir FeatureGate arkasında kurulur ve karar verilene kadar ücretsiz açık kalır. Önerim: geri sarma (sağlayıcı penceresi ve duraklatınca arabellek) ücretsiz farklılaştırıcı olsun; kayıt ve çoklu ekran ücretli paket adayı olsun. Fiyat ve ülkeler gelir modeli işinde belirlenir.
- Kayıtlar nerede tutulsun ve ne kadar yaşasın? → varsayılan: Cihaz ya da USB'de NEXTV'ye özel klasör: uygulama kaldırılınca silinir, başka uygulamalar görmez. Ortak Filmler klasörüne yazma ve dışa aktarma olmasın. Kayıtlar ek şifreleme olmadan saklansın (TV işlemcisinde maliyetli; şu an bilinen bir gerekçe yok).
- Android 14 ve sonrasında 'Alarmlar ve hatırlatıcılar' (exact alarm) izni istensin mi? → varsayılan: İlk planlamada bir kez, açıklamayla istensin. Reddedilirse ya da TV'de ayar ekranı yoksa planlı kayıtlar yalnız NEXTV açıkken başlasın ve listede bu açıkça yazsın.
- Canlı geri sarma varsayılan olarak nasıl çalışsın? → varsayılan: 'Duraklatınca' varsayılan açık olsun: arabellek yalnız duraklatınca başlar, kanal geçişi yavaşlamaz, sürekli yazma olmaz. 'Her zaman' seçeneği Aşama 6'da isteğe bağlı gelsin; 65U95A'da kanal geçişi p50 doğrudan oynatmadan en fazla 3 sn yavaşsa gösterilsin.

**Riskler:**
- Android 15+ dataSync ön plan hizmeti arka planda 24 saatte toplam 6 saatle sınırlı. Açık uçlu kayıt 4 saatte duruyor; aynı gün birden çok uzun kayıt OS_TIMEOUT ile yarıda kalabilir. Play incelemesi kayıt için dataSync türünü sorgulayabilir; specialUse gerekçesi daha zor.
- TV bekleme ya da kapalıyken zamanlanmış kayıt cihaza bağlı. Düşük Güçlü Bekleme ağı ve wake lock'u kesebilir; bazı TV'ler bekleme modunda gücü tamamen keser. Android 14+'ta exact alarm izni varsayılan kapalı ve TV'de ayar ekranı olmayabilir.
- TV'nin iç depolaması küçük (8–16 GB) ve flaş belleği dayanıksız. HD kayıt saatte ~3,6 GB; 'Her zaman' arabellek sürekli yazma demek. Bu yüzden varsayılan 'Duraklatınca' ve kota payı var.
- Sağlayıcı bağlantı sınırı aşılırsa hesap kilitlenebilir. Xtream max_connections yanlış olabilir; izleyicinin girdiği değer yalnız onun beyanı. Kullanıcıya yazılı uyarı gösteriliyor ve sınır bilinmiyorsa 1 kabul ediliyor.
- Çoklu ekranda donanım çözücü ve video düzlemi sayısı birçok TV'de 2 ile sınırlı olabilir. getMaxSupportedInstances iyimser değer bildirebilir; birden çok SurfaceView siyah karo gösterebilir. Isınma ve bellek de sınırlıyor.
- TS akışında rastgele erişim bayrağı yoksa ya da PTS bozuksa parça GOP ortasında kesilir ve aramada görüntü bozulur. Yerel EVENT listesinin yenilenmesi ve canlı hız davranışı Media3'te cihazda doğrulanmalı. Geri sarmada 8–12 sn ek gecikme var.
- Hukuk ve algı: kayıt özelliği 'korsan aracı' algısını artırabilir. Mağaza ve uygulama dili PLAY_STORE_READINESS 3.2'ye uymalı; VOD indirme yok, şifreli yayın kaydı yok, mağaza görselleri sentetik.
- Kapsam yaklaşık 42 mühendis-günü ve çekirdek oynatıcıya (PlayerScreen) dokunuyor. Oynatıcı kanal listesi işiyle aynı dosyada çakışabilir; aşamalar sırayla yürümeli.
- Keystore sıfırlanırsa kayıt listesi kaybolur (dosyalar 'Adsız kayıt' olarak döner). USB'de NTFS salt okunur, FAT32/exFAT desteği ise üreticiye göre değişiyor.

**Dokunulacak dosyalar:**
- `apps/android-tv/app/src/main/AndroidManifest.xml`: İzinler: FOREGROUND_SERVICE, FOREGROUND_SERVICE_DATA_SYNC, WAKE_LOCK, SCHEDULE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED. Yeni bileşenler: RecordingService (dataSync, exported=false), RecordingAlarmReceiver, RecordingBootReceiver.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/MainActivity.kt`: TvApplication'a captureHub, recordings (RecordingRepository) ve streamBudget eklenir. Açılışta bildirim kanalı "Kayıtlar" kurulur, timeshift ve .part artıkları temizlenir.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/TvViewModel.kt`: Route.RECORDINGS ve Route.MULTIVIEW; TvState alanları (recordings, captures, storage, timeshiftMode, streamLimits, multiView, streamConflict, scheduledByChannel). Kayıt, plan ve çoklu ekran fonksiyonları; play() içinde bütçe kontrolü; removeSource'ta planların iptali; maxConnections'ın saklanması. PlayingMedia'ya origin ve recordingId.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt`: Kaynak fabrikası liveMediaSourceFactory'ye çıkarılır. Yeni düğmeler: Kaydet/Kaydı durdur, Çoklu ekran, canlıda −10/+10 ve Canlıya dön. Yeni tuşlar: MediaRecord, canlıda Sol/Sağ sarma. "● KAYIT" hapı, geride kalma durum metni, pencere çubuğu, keepContentOnPlayerReset. Kayıt oynatma kipleri (VOD ve takip), konumun rp:<id>'ye yazılması.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/LiveTimeshift.kt`: Yeni dosya. TimeshiftController: T0 pencere algılama, T1 duraklat/sürdür, BEHIND_WINDOW'da başa dönme, oturumun bitişi ve kayda yükseltme.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlaybackRecovery.kt`: newMedia(isLive, rejoinLive) parametresi ve onBehindWindow kancası; yerel listeden oynatırken canlıya atlamaz.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/HomeScreen.kt`: Navigation'a "Kayıtlar" (8. hedef): defaults, order, widestLabel ve activeIndex güncellenir.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/MenuSymbols.kt`: radio_button_checked simgesinin dış hat ve dolu Material Symbols yolları.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/DesignSystem.kt`: iconPaths'e "record" ve "grid" çizgi simgeleri (Action düğmeleri için).
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/GuideScreen.kt`: Program ayrıntısına Kaydı planla/Planı iptal et; hücrede basılı OK hızlı menüsü; MediaRecord tuşu; planlı hücrede kırmızı nokta ve stateDescription.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SettingsScreen.kt`: "Kayıt ve canlı yayın" bölümü. Yeni sayfalar: RECORDING_TARGET, TIMESHIFT, PADDING, STREAM_LIMITS. İzin satırı, kişisel kullanım notu ve kaldırma sayfasına plan satırı.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PilotApp.kt`: RECORDINGS ve MULTIVIEW rotalarının yönlendirilmesi; BackHandler istisnasına MULTIVIEW.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/RecordingsScreen.kt`: Yeni dosya. TV-080 Kayıtlar listesi: bölümler, depolama özeti, hızlı menü, sil onayı, boş ve hata durumları.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/RecordDialog.kt`: Yeni dosya. TV-082 Kaydet paneli, TV-083 plan onayı, çakışma diyaloğu ve planlı kayıt geri sayım kartı.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/MultiViewScreen.kt`: Yeni dosya. TV-090 karolar, MultiViewLayout, ses odağı, bağlantı uyarısı, karo menüsü.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/ChannelPickerDialog.kt`: Yeni dosya. TV-091 numaralı kanal seçici; oynatıcı kanal listesi işi önce biterse onu yeniden kullanır.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/capture/`: Yeni paket. CaptureClient, CaptureSession, TsSegmenter, HlsPlaylistReader, HlsCapture, LocalPlaylist, CaptureHub, StreamBudget, Recording(Models), RecordingStore, RecordingRepository, RecordingPlanner, RecordingQuota, StorageTargets, RecordingScheduler, RecordingService, RecordingAlarmReceiver, RecordingBootReceiver.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/DeviceTier.kt`: Yeni dosya. Çözücü örnek sayısı, PerformancePoint ve bellek sınıfından çoklu ekran karo sayısı.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/DeviceStore.kt`: Yeni anahtarlar: rec:target, rec:padBefore/After, ts:mode, ts:minutes, streams:<id>, providerStreams:<id>, rp:<id>, exactAlarmAsked. saveSources ve forgetSource bu anahtarları temizler.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/SourceRepository.kt`: user_info.max_connections okunur ve Catalog.maxConnections'a konur.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/Models.kt`: Catalog'a anlık maxConnections: Int? alanı (snapshot codec'ine yazılmaz).
- `apps/android-tv/app/src/main/res/values/strings.xml`: Yeni dosya. Bu kümenin bütün Türkçe metinleri, biçim dizgeleri ve plurals.
- `apps/android-tv/app/build.gradle.kts`: Yeni bağımlılık yok. recovery-fixture.ts sharedTest/resources'a taşınınca mevcut srcDir ayarı onu iki test türüne de verir.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/capture/`: Yeni JVM testleri: TsSegmenterTest, LocalPlaylistTest, HlsPlaylistReaderTest, CaptureSessionTest, StreamBudgetTest, RecordingPlannerTest, RecordingCodecTest, RecordingQuotaTest.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/ui/`: Yeni JVM testleri: TimeshiftControllerTest, MultiViewLayoutTest. data/ altında DeviceTierTest; SourceTest'e max_connections.
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/`: Yeni cihaz testleri: CaptureStorageTest, RecordingFlowTest, TimeshiftFlowTest, ScheduledRecordingTest, MultiViewFlowTest, RecordingsLayoutTest.
- `docs/design/SCREENS.md`: Yeni ekran kimlikleri TV-080, 081, 082, 083, 090, 091; ilk odak, OK ve Back.
- `docs/decisions/DECISIONS.md`: Sahip kararları ve teknik politika (tek bağlantı, şifreli yayın, dataSync, yerel HLS) için yeni D kaydı.
- `docs/project/PLAY_STORE_READINESS.md`: dataSync FGS beyanı, exact alarm, kayıt mağaza dili ve görselleri.
- `docs/product/TIVIMATE_GAP.md`: Üç satırın durumu aşamalara göre güncellenir.
- `docs/project/BACKLOG.md`: Aşama başına yeni BKL maddeleri ve kabul ölçütleri.

---

# Kayıt, canlı geri sarma ve çoklu ekran: uygulama tasarımı

18 Ekim 2026 · TIVIMATE_GAP'teki "Kayıt", "Canlı duraklatma ve timeshift" ve "Çoklu ekran" satırları · dayanak 0.14.43 kod okuması. Buradaki her şey öneridir; sahip kararları §17'de toplandı.

## 0. Kapsam ve değişmez ilkeler

- **Üç ayrı yetenek** (PRD §10 ve §3'teki Sparkle notu):
  - **Geri sarma:** canlı yayının cihazda tutulan son dakikaları.
  - **Kayıt:** yeni dosya ya da iş oluşturur.
  - **Geçmiş yayınlar:** sağlayıcı arşivi; EXT02'nin konusu, bu kümede yok.
  
  Bunlar düğmede ve metinde birbirine karışmaz. Xtream'in "timeshift" uç noktası aslında catch-up'tır ve burada kullanılmaz.
- **Yalnız canlı kanal kaydedilir** (`MediaKind.LIVE`, `!entry.adult`). Film/dizi için "İndir" eklenmez; bu, korsan aracı algısını önler (PLAY_STORE_READINESS 3.2).
- **Şifreli ya da DRM'li yayın kaydedilmez.** HLS'te `#EXT-X-KEY` METHOD≠NONE, Widevine ya da DASH görülürse metin "Bu yayın kaydedilemiyor" olur. Şifre çözme ya da aşma yoktur (3.2).
- **Tek bağlantı:**
  - Aynı kanal hem izlenip hem kaydediliyorsa tek bağlantı açılır; oynatıcı kaydın yerel listesinden oynar.
  - Paralel yayın yalnız kaynağın bağlantı sınırı biliniyorsa ve sınır içinde açılır. Sınır ya Xtream `max_connections`'tan ya da izleyicinin kendi girdiği değerden gelir; ikisi de yoksa sınır 1'dir (AGENTS: "eşzamanlı bağlantı limiti bilinmiyorsa paralel yayın açılmaz").
- **URL ve kimlik bilgisi diske yazılmaz.** Kayıt kaydı yalnız kaynak ve kanal kimliğini tutar; adres başlangıç anında şifreli kayıttan üretilir. Klasör adlarında başlık geçmez.
- **Analitik yok.** Medya günlüğü zaten kapalı (`TvApplication.onCreate` → `Log.setLogLevel(LOG_LEVEL_OFF)`).
- **Dil:** "kendi kaynağın, kişisel kullanım". Bu kümenin metinleri `res/values/strings.xml`'e alınır (§18).

## 1. Bugünkü kod (dayanak)

- **`ui/PlayerScreen.kt`**
  - Tek `ExoPlayer` composable içinde `remember` ile kuruluyor; `DisposableEffect`'te `release()` ediliyor.
  - `LaunchedEffect(media)` önce `player.stop()` yapıyor (yorum: "one source session at a time"). Ardından `DefaultHttpDataSource` kuruyor; OkHttp kullanılmıyor.
  - Ağ ayarları: UA `NEXTV/<sürüm> AndroidTV`, `media.entry.headers` (M3U `#EXTVLCOPT` UA/Referer, `CatalogParser`), 8 sn bağlanma/okuma, `SingleAttemptLoadPolicy`.
  - Canlıda "Duraklat" var. Sol/Sağ ise yalnız `!live` iken `seekBy` ile sarıyor; canlı TS'de ilerleme çubuğu yok.
  - ON_STOP'ta `recovery.background()` duraklatıyor (TV-NP).
- **`ui/PlaybackRecovery.kt`:** `prepare()` canlıda `seekToDefaultPosition()` çağırıyor; `ERROR_CODE_BEHIND_LIVE_WINDOW` tekrar denenebilir sayılıyor. Uzun canlı duraklatmanın TS'de ne yaptığı ölçülmedi.
- **`data/SourceRepository.kt`:** OkHttp istemcisinde `callTimeout(120 s)` var, bu yüzden uzun akış için kullanılamaz. `user_info`'dan yalnız `auth` ve `status` okunuyor.
- **Manifest ve bağımlılıklar:** Manifestte yalnız INTERNET ve ACCESS_NETWORK_STATE var; hizmet ya da alıcı yok. Bağımlılıklarda WorkManager ve Room yok; Media3 HLS ve OkHttp var.
- **Kalıcılık:**
  - `DeviceStore`: SharedPreferences ve Keystore AES-GCM ile `seal/unseal`.
  - `CatalogStore`: AtomicFile ve seal.
- **UI:**
  - Metinler satır içi Türkçe; `strings.xml` yok.
  - Gezinme `ui/HomeScreen.kt` içindeki `Navigation`'da (7 hedef).
  - Ayarlar `SettingsScreen.kt` içindeki `SettingsPage` enum'unda.
  - Simgeler `DesignSystem.kt` `iconPaths` ve `MenuSymbols.kt`'te.
- **Testler:** `build.gradle.kts`'te `unitTests.isReturnDefaultValues = true`; Robolectric yok. MockWebServer hem JVM hem cihaz testinde var. `androidTest/assets/recovery-fixture.ts` sentetik bir TS dosyası.

## 2. Yakalama motoru (ortak çekirdek)

Paket `dev.iptvplayer.tv.capture`. Saf JVM parçalar Android'e bağlı değil.

**Çıktı biçimi:** Her oturum bir klasördür: `index.m3u8` (yerel HLS medya listesi) ve `seg-000001.ts` dosyaları. fMP4 kaynaklarda bunların yerine `init-1.mp4` ve `.m4s` parçaları yazılır.

Neden bu biçim:
- Media3 `file://` HLS'i `DefaultDataSource` ile oynatır.
- Büyüyen EVENT listesi "kaydederken izle", geri sarma ve sarmayı doğal olarak destekler.
- Küçük parçalar FAT32'nin 4 GB sınırına takılmaz.
- Halka arabellekte eski parça silinebilir.

Tek büyük `.ts` dosyası olsaydı Media3 dosya sonunda yayını bitmiş sayardı ve ilerlemesiz canlı TS'de arama yapamazdı.

| Sınıf | Görev |
|---|---|
| `CaptureClient` | Ayrı bir OkHttpClient. Bağlanma 8 sn, okuma 15 sn, `callTimeout` yok, `followSslRedirects(false)` (SourceRepository ile aynı ilke). Oynatıcıyla aynı UA ve `entry.headers`. Bir oturumda aynı anda tek istek. |
| `CaptureSession` | `start/stop`; `state: StateFlow<CaptureProgress>` (bayt, parça, süre, bit hızı, yeniden bağlanma, hata). Yanıtın türünü koklar: URL `.m3u8`, Content-Type `mpegurl` ya da gövde `#EXTM3U` ise HLS; ilk bayt 0x47 ise TS; diğer her şey FORMAT hatası. |
| `TsSegmenter` (saf) | 188 baytlık pakete hizalar; senkron kaybolunca yeniden hizalar. PAT → PMT zinciriyle video PID'ini ve türünü bulur (0x1B, 0x24, 0x02); video yoksa ilk ses PID'ini kullanır (radyo). PES PTS'i okur (33 bit sarmayla). **Kesim:** video PID'de PUSI ve rastgele erişim bayrağı varken, parça ≥ 4 sn olduysa keser. Bayrak hiç gelmezse IDR/IRAP NAL taraması yapar; hiçbiri olmazsa 10 sn'de zorla keser. Her parçanın başına son PAT ve PMT eklenir. PTS geri giderse ya da 10 sn'den fazla sıçrarsa parça "discontinuity" olarak işaretlenir. |
| `HlsPlaylistReader` (saf) | Adresleri okhttp `HttpUrl.resolve` ile çözer. Çok varyantlı listede sesi gömülü (muxed), en yüksek BANDWIDTH'li varyantı seçer (RESOLUTION ≤ 1920×1080); ayrı ses grubu gerekiyorsa FORMAT. Okuduğu etiketler: EXTINF, MEDIA-SEQUENCE, TARGETDURATION, DISCONTINUITY, MAP, BYTERANGE, PROGRAM-DATE-TIME, ENDLIST; KEY METHOD≠NONE ise ENCRYPTED. Media3'ün `HlsPlaylistParser`'ı `android.net.Uri` istediği için JVM testinde çalışmaz; kendi küçük okuyucumuzun nedeni bu. |
| `HlsCapture` | Listeyi yeniler (yeni parça yoksa hedef sürenin yarısı kadar bekler). Parçaları sırayla indirir. MEDIA-SEQUENCE'ta boşluk varsa discontinuity yazar. Parça 403/404 verirse en çok 5 dakikada bir özgün adresi yeniden ister (token yenileme). |
| `LocalPlaylist` | Başlık: `VERSION:3` (fMP4'te 7), `TARGETDURATION:10`, `MEDIA-SEQUENCE`. EVENT kipinde `PLAYLIST-TYPE:EVENT` ve her parçada `PROGRAM-DATE-TIME`; bitince `ENDLIST`. RING kipinde PLAYLIST-TYPE yazılmaz; baştan düşen parça listeden çıkar, dosyası iki parça gecikmeyle silinir ve `DISCONTINUITY-SEQUENCE` güncellenir. Parça önce `.part` olarak yazılıp yeniden adlandırılır; liste geçici dosyaya yazılıp yerine taşınır. `fsync` yalnız bitişte. |
| `CaptureHub` | Süreç ömürlü (`TvApplication.captureHub`), `CoroutineScope(SupervisorJob()+IO)`. Oturumun sahibi SCREEN (geri sarma; ON_STOP'ta biter) ya da SERVICE (kayıt) olur. `sessionFor(channel)` aynı kanalın açık oturumunu verir. `promote(session, recordingId)` geri sarmayı kayda çevirir: hedef birim aynı, klasör yeniden adlandırılır. |

**Yeniden bağlanma:**
- Tekrar denenenler: ağ hatası, 408/5xx, akışın kendiliğinden bitmesi. Bekleme sırasıyla 1, 3 ve 10 sn; 10 dakikada en çok 6 deneme, sonra PARTIAL.
- Tekrar denenmeyenler: 401/403 (HLS token yenilemesi dışında), 404/410, 429, ENCRYPTED, FORMAT.

### Bağlantı bütçesi

`StreamBudget`, `SourceTransactions.shared` gibi süreçte tek örnektir.

- **API:** `acquire(sourceId, purpose): Lease?`, `release`, `inUse`, `limit`.
- **Sınır:** min(`streams:<id>`, `providerStreams:<id>`). İkisi de yoksa 1.
- **Sayılanlar:** oynatıcının doğrudan bağlantısı, her yakalama oturumu, her çoklu ekran karosu.
- **Sayılmayanlar:** kayıttan oynatma, katalog ve EPG istekleri, bir oturumdan oynayan oynatıcı.
- **Xtream sınırı:** `SourceRepository.load`, `user_info.max_connections` değerini okur (metin ya da sayı gelebilir). Değer `Catalog.maxConnections` alanına konur; bu alan anlıktır, snapshot codec'ine yazılmaz. ViewModel değeri `store.providerStreams(id, n)` ile saklar.

## 3. Depolama ve kota

- **Birimler:**
  - Liste: `getExternalFilesDirs(Environment.DIRECTORY_MOVIES)`. [0] cihaz depolamasıdır, diğerleri USB/SD. İzin gerekmez; SAF seçicisi birçok TV'de yok.
  - Ad ve durum: `StorageManager.getStorageVolume(file)` → `getDescription`, `isRemovable`, `state`.
  - Boş alan: cihazda `getAllocatableBytes(getUuidForPath)`, USB'de `usableSpace`.
  - Seçimde deneme yazımı yapılır; NTFS salt okunursa "Bu sürücüye yazılamıyor."
- **Klasörler:** `<birim>/recordings/<rastgele-id>/` ve `<birim>/timeshift/<oturum>/`. Uygulamaya özel: NEXTV kaldırılınca silinir, başka uygulama görmez (§17-2).
- **Ayrılan pay:** cihazda 1 GB + %5, USB'de 512 MB.
  - Başlamak için payın üstünde en az 10 dakikalık yer gerekir. Bit hızı bilinmiyorsa 8 Mbps varsayılır (yaklaşık 600 MB / 10 dk).
  - Kayıt sırasında 30 sn'de bir kontrol edilir. Pay altına inilirse kayıt STORAGE_FULL ile durur; o ana kadarki kısım oynatılabilir kalır.
- **Geri sarma halkası:** min(ayar dakikası × ölçülen bit hızı, ayrılabilir alanın %25'i, 4 GB). Varsayılan 30 dk.
- **Açılışta temizlik:** bütün birimlerde `timeshift/*` ve `*.part` silinir.
- **Hedef yoksa:** USB takılı değilse yeni kayıt cihaza yazılır ve not düşülür. Geri sarma kayıt hedefinde tutulur (kayda yükseltmede yeniden adlandırma aynı birimde kalsın diye); hedef yoksa cihazda.

## 4. Anlık kayıt (Aşama 1) · TV-082

Canlı kanal izlenirken kumanda akışı:
1. OK kontrolleri açar. Sağ ile "Kaydet"e gidilir; kumandadaki Kayıt tuşu (`Key.MediaRecord`) da aynı işi görür.
2. OK, "Kaydet: Kanal 1" panelini açar (`SourceDialog` 480 dp).
   - Seçenekler radyo satırıdır (D053 ✓): "Program bitene kadar · 22:30" (yalnız EPG `currentProgramme.end` varsa; varsa varsayılan), "30 dakika", "1 saat", "2 saat", "Ben durdurana kadar (en çok 4 saat)".
   - Altında "Kayıt yeri: Cihaz depolaması · 5,2 GB boş" ve kişisel kullanım notu.
   - Eylemler: [Kaydı başlat] [Vazgeç]. İlk odak varsayılan seçenekte. Geri paneli kapatır, odak "Kaydet"e döner.
3. "Kaydı başlat" `model.recordNow` çağırır ve bütçeyi kontrol eder. Ardından:
   - Oynatıcı kendi bağlantısını bırakır (`player.stop()`; `PlayerView.keepContentOnPlayerReset = true` ile son kare ekranda kalır).
   - Hub oturumu açar.
   - Oynatıcı yerel EVENT listesini canlı kenarda açar (`LiveConfiguration.targetOffsetMs = 8000`, hız min=max=1,0).
   - Bu arada durum satırında "Kayıt başlıyor…" görünür (~2–4 sn).
4. Üst hapta "CANLI TV"nin yanında "● KAYIT 00:12:31" görünür. Nokta `Ink.Error` renginde ve yanıp sönmez. TalkBack dakikada bir "Kayıt sürüyor, 12 dakika" der. "Kaydet" düğmesi "Kaydı durdur" olur.
5. Oynatıcıdan çıkmak kaydı durdurmaz; hizmet sürdürür. Aynı kanala dönülünce yayın kayıttan oynar (ikinci bağlantı açılmaz) ve kaydın başına kadar geri sarılabilir.
6. "Kaydı durdur" onay istemez. Not "Kayıt durdu · Kayıtlar'a eklendi" görünür ve oynatıcı doğrudan bağlantıya döner.

**Ön plan hizmeti `RecordingService` (dataSync):**
- Başlatma: `ServiceCompat.startForeground(…, FOREGROUND_SERVICE_TYPE_DATA_SYNC)`.
- Bildirim: kanal "Kayıtlar" (LOW), metin "NEXTV kaydediyor · Kanal 1 · 00:12", eylem "Durdur"; 30 sn'de bir güncellenir.
- Kilitler: kısmi wake lock (bitiş + 10 dk zaman aşımı) ve cihaz destekliyorsa Wi-Fi kilidi.
- SERVICE sahipli oturum kalmayınca `stopForeground(REMOVE)` ve `stopSelf`.
- API 35+ `onTimeout(startId, type)` gelirse bütün kayıtlar OS_TIMEOUT ile kapanır.
- Süreç öldürülür de hizmet yeniden başlarsa: RECORDING durumundaki öğeler ENDLIST ile kapatılıp PARTIAL olur. Planlı bitiş geçmediyse yeni parça discontinuity ile sürdürülmeye çalışılır; `startForeground` reddedilirse öğe PARTIAL kalır.
- POST_NOTIFICATIONS istenmez: hizmet bildirim izni olmadan da çalışır ve TV'de bildirim çekmecesi yok.

**Kanal değiştirme çakışması.** Bütçe doluysa `model.play()` yeni bağlantı açmaz:
- CH±, Yukarı/Aşağı ya da rakamla geçişte köşe notu çıkar: "Kayıt sürüyor: bu kaynaktan aynı anda 1 yayın açılabilir."
- Liste ya da rehberden seçimde diyalog açılır: "Kanal 1 kaydediliyor. Evim aynı anda 1 yayına izin veriyor." [Kaydı durdur ve aç] [Vazgeç]; ilk odak Vazgeç.

## 5. Kayıtlar (TV-080) ve kayıt oynatma (TV-081)

**Menü ve rota:**
- Sol menüde Favoriler'in altına "Kayıtlar" eklenir.
- `Navigation` değişiklikleri: `defaults` 8 olur, `order` listesine 7 eklenir, `widestLabel` listesine "Kayıtlar" girer, `activeIndex` RECORDINGS için 7 olur.
- Simge `MenuSymbols.kt`'ye eklenir: Material Symbols Rounded `radio_button_checked`, dış hat ve dolu.
- Yeni rota `Route.RECORDINGS`.

**Yerleşim:**
- Başlıkta "Kayıtlar"; sağında "Cihaz · 2,1 GB kayıt · 4,8 GB boş".
- Bölümler: "Kaydediliyor", "Planlanan", "Kaydedilenler" (yeniden eskiye).
- Satır tam genişliktedir (D053) ve en az 56 dp'dir:
  - Solda kanal logosu: `Artwork(logo = true)`, katalogdan kanal kimliğiyle; yoksa harf.
  - Başlık 15 sp: program adı, yoksa kanal adı.
  - Alt satır 12 sp: "Kanal 1 · 18 Eki Cmt 21:00 · 1 sa 32 dk · 3,4 GB".
- Durum etiketleri: "● KAYIT 00:12", "Planlandı · 21:00", "Yarıda kaldı: bağlantı kesildi", "USB sürücü takılı değil".

**Odak ve tuşlar:**
- İlk odak, ilk dolu bölümün ilk satırında. Oynatıcıdan dönüşte aynı satıra dönülür.
- Odak görünümü beyaz 2 dp halkadır (`focusRing(CardRadius)`); büyüme yok (D064).
- OK:
  - Kaydedilen satırda oynatır; `PlaybackMark.resumable` ise kaldığı yerden.
  - Süren kayıtta baştan izletir.
  - Planlananda menü açar.
- Basılı OK, D065'teki `QuickActions` desenini kullanır: "Baştan izle", "Canlı kenara git", "Kaydı durdur", "Sil", "Planı iptal et", "Vazgeç".
- Sil onayı: "Kaydı sil?" / "Akşam Haberleri bu cihazdan kalıcı olarak silinir." [Vazgeç] [Sil]; ilk odak Vazgeç.
- Geri: satırdan menüye, menüden Ana Sayfa'ya (D060/D065 deseni).

**Boş ve hata durumları:**
- Boş: "Henüz kayıt yok" / "Canlı yayında Kaydet'e bas ya da rehberde bir programı seç." [Rehberi aç].
- Liste okunamazsa: "Kayıt listesi okunamadı; dosyalar korunuyor." Klasörlerden "Adsız kayıt · 18 Eki 21:00" öğeleri kurulur.

**Görünürlük:** Kanal kategorisi D068'e göre gizliyse öğe listelenmez (`CategoryVisibility.key(LIVE, category)` ya da `looksAdult`). PIN (BKL-028) bağlanınca aynı erişim kontrolünden geçer.

**Oynatma:**
- `PlayingMedia(origin = RECORDING, recordingId)`, URL `file://…/index.m3u8`. `toString` REDACTED kalır.
- Bitmiş kayıt VOD gibi davranır: Sol/Sağ 10/30/60 sn, −10/+10, ilerleme çubuğu.
- Süren kayıt "takip" kipindedir: geri sarma kontrolleri gösterilir.
- Üst hap "KAYIT" olur. Favori yıldızı, sonraki bölüm ve geçmiş kaydı yok. Konum `rp:<id>` anahtarına yazılır.

## 6. Canlı geri sarma

### T0 · Sağlayıcı penceresi (Aşama 0, S)

Bazı HLS canlı yayınlar oynatıcıda zaten aranabilir bir pencere verir. Koşul: `isCurrentMediaItemLive && isCurrentMediaItemSeekable` ve `currentTimeline`'daki pencere ≥ 60 sn. Bu durumda motor gerekmez; canlıda da şunlar açılır:
- Kontroller gizliyken Sol/Sağ ile sarma.
- İleri/Geri sarma tuşları.
- "−10 sn", "+10 sn" ve "Canlıya dön" düğmeleri.

"Duraklat" düz `pause()` olarak kalır. Konum pencerenin dışına düşerse mevcut BEHIND_LIVE_WINDOW kurtarması canlıya döner.

### T1 · Duraklatınca arabellek (Aşama 2, M)

TS ve penceresi olmayan HLS yayınlar içindir.

1. **Duraklatma.** Canlı doğrudan oynarken Oynat/Duraklat tuşu ya da "Duraklat" `TimeshiftController.pause()` çağırır.
   - Koşullar: kip "Kapalı" değil, T0 değil, boş yer ayrılan pay + 500 MB'tan fazla, bütçe uygun.
   - Koşullar sağlanırsa `player.pause(); player.stop()` ile bağlantı bırakılır; son kare ekranda kalır. Hub bir SCREEN oturumu (RING) açar.
   - Not: "Duraklatıldı · geri sarma açık". Durum satırı duvar saatiyle artar: "Duraklatıldı · 1 dk 05 sn geride".
2. **Sürdürme.** Oynat, yerel listeyi `setMediaSource(local, startPositionMs = 0)` ile açar. Pencerenin başı yaklaşık duraklatma anıdır; yeniden bağlanırken ~1–3 sn yayın kaybolur.
   - `LiveConfiguration`'da hız min=max=1,0: Media3'ün canlı hız denetimi geride kalan oynatmayı hızlandırmasın.
   - `targetOffsetMs` 8000.
3. **Oturum sürerken:**
   - Sol/Sağ 10/30/60 sn, İleri sarma +30, Geri sarma −10.
   - "Canlıya dön" `seekToDefaultPosition()` çağırır; geride kalma ≥ 15 sn iken etkindir.
   - Durum: geride kalma < 15 sn ise "● CANLI", değilse "4 dk 12 sn geride · 21:03" (saat PDT'den).
   - Program ilerleme çubuğunun yerinde pencere çubuğu görünür; program satırı metin olarak kalır.
4. **Halka sınırı.** Duraklatma halka sınırını (30 dk) aşarsa BEHIND_LIVE_WINDOW'u controller yakalar; bunun için `PlaybackRecovery`'ye bir `onBehindWindow` kancası eklenir. Controller `seekTo(0); prepare()` yapar; not "Arabellek sınırı: en eski noktadan devam ediyor."
5. **Oturumun bitişi** (klasör silinir):
   - Kanal değişimi; not "Canlı yayın arabelleği silindi."
   - Oynatıcıdan çıkış.
   - ON_STOP (TV-NP); dönüşte canlı doğrudan açılır.
   - "Kaydet": oturum kayda yükseltilir. Kayıt yükseltme anından başlar, önceki halka parçaları silinir.
6. **Uygun değilse** (yer yok, kip Kapalı, ENCRYPTED, FORMAT): bugünkü düz duraklatma kalır; not "Canlı yayın duraklatıldı. Uzun duraklatmada yayın yeniden bağlanır."
7. Oturum yokken kontroller gizli ve canlıda Sol'a basılırsa not çıkar: "Geri sarmak için önce duraklat."

Uygulama ayrıntıları:
- `PlaybackRecovery.newMedia(isLive, rejoinLive)`: yerel listeden oynatırken tekrar deneme canlıya atlamaz. Motor yeniden bağlanırken oynatıcı yalnız tamponda bekler.
- PlayerScreen'deki `opening` hesabı (131. satır), oturum "duraklatıldı" durumundayken "Yayın açılıyor…" göstermez.
- Mantık `ui/LiveTimeshift.kt` dosyasına konur; PlayerScreen zaten 542 satır.

### T2 · Her zaman arabellek (Aşama 6, M)

Ayar "Her zaman" seçilirse oturum kanal açılır açılmaz başlar ve duraklatmadan önceki dakikalar da geri sarılabilir. Bedeli:
- Kanal geçişi en az bir parça kadar (~4 sn) uzar.
- Gecikme ~8–12 sn artar.
- Sürekli yazma olur: HD'de yaklaşık 3,6 GB/saat; bu, TV'nin flaş belleğini yıpratır.

Kabul ölçütü: 65U95A'da kanal geçişi p50 ≤ doğrudan + 3 sn. Sağlanamazsa bu seçenek gösterilmez.

## 7. Zamanlanmış kayıt (Aşama 4, M–L) · TV-083

**Rehberde akış:**
1. Gelecekteki bir hücrede OK, mevcut "Program ayrıntısı" diyaloğunu açar (`GuideScreen` `detail`). Diyaloğa "Kaydı planla" eklenir; ilk odak yine metin okuyucuda kalır.
2. "Kaydı planla" onay panelini açar: "Akşam Haberleri · Kanal 1 · Bugün 21:00–22:30" ve "Başlamadan 1 dk önce başlar, bittikten 3 dk sonra durur." [Planla] [Vazgeç]. İzin ve bekleme uyarıları gerekiyorsa bu panelde görünür.
3. Planlanan hücrede küçük kırmızı nokta görünür (`stateDescription` "Kayıt planlandı"). Ayrıntıdaki düğme "Planı iptal et" olur. Rehber bu bilgiyi ViewModel'in hazırladığı `scheduledByChannel` alanından okur; odak hareketi durumu güncellemez (D056).
4. Basılı OK hızlı menü açar:
   - Şu an yayındaki hücrede: "Canlı izle", "Program bitene kadar kaydet", "Vazgeç".
   - Gelecekteki hücrede: "Ayrıntılar", "Kaydı planla" ya da "Planı iptal et", "Vazgeç".
   
   `MediaRecord` tuşu odaktaki programı planlar ya da planı iptal eder.

**Çakışmalar:**
- Aynı kaynakta aynı anda planlı/süren kayıtlar sınırı aşarsa: "Bu saatte Spor Gecesi kaydedilecek. Evim aynı anda 1 yayına izin veriyor." [Diğerini iptal et ve planla] [Vazgeç].
- Aynı kanalda üst üste binen plan: "Bu kanal o saatte zaten kaydediliyor."
- Aynı program ikinci kez planlanırsa aynı plan kalır.
- Başlama anında uygulama öndeyse, oynatıcı aynı kaynaktan başka bir kanal oynatıyorsa ve sınır doluysa: 30 sn geri sayımlı kart çıkar: "Planlı kayıt başlıyor: Kanal 1 · Akşam Haberleri" [Kaydı başlat ve kanala geç] [Kaydı atla]. Süre dolunca kayıt başlar ve oynatıcı kaydı izler.

**Tetikleme:**
- **Alarm.** `RecordingScheduler`, her plan için `AlarmManagerCompat.setExactAndAllowWhileIdle(RTC_WAKEUP, başlangıç − pay − 30 sn)` kurar. PendingIntent yalnız kayıt kimliğini taşır. Tam alarm, Android 12+'da arka plandan ön plan hizmeti başlatma istisnasıdır.
- **İzin (`canScheduleExactAlarms()`):**
  - API 31–33'te kurulumda verilidir; 34+'ta yeni kurulumda kapalıdır.
  - Kapalıysa ilk planlamada panelde satır çıkar: "TV beklemedeyken kaydın zamanında başlaması için 'Alarmlar ve hatırlatıcılar' izni gerekir." [Ayarları aç] [Şimdilik geç]. "Ayarları aç", `ACTION_REQUEST_SCHEDULE_EXACT_ALARM` ile `package:` URI açar.
  - Bu ekran çözülemezse satır şöyle olur: "Bu TV'de izin ekranı bulunamadı; planlı kayıt NEXTV açıkken başlar."
- **İzin yokken:**
  - Uygulama öndeyse hub içindeki zamanlayıcı (`Handler.postAtTime`) kaydı tam zamanında başlatır.
  - Uygulama kapalıysa alıcının hizmeti başlatma denemesi `ForegroundServiceStartNotAllowedException` ile NOT_ALLOWED olur.
  - Planlanan satırında "Yalnız NEXTV açıkken başlar" yazar.
- **Kullanılmayanlar:**
  - `USE_EXACT_ALARM`: Play bunu alarm ve takvim uygulamalarıyla sınırlıyor.
  - WorkManager: uzun iş yine arka plan FGS kısıtına takılır, tam zamanlı çalışmaz ve yeni bağımlılık olurdu.
- **Yeniden kurma.** `RecordingBootReceiver` şu yayınlarda alarmları yeniden kurar: BOOT_COMPLETED, TIME_SET, TIMEZONE_CHANGED, `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`. Android 15+'da BOOT_COMPLETED'dan dataSync hizmeti başlatılamaz. Bu yüzden penceresi o an süren bir plan için, izin varsa şimdi + 10 sn'ye tam alarm kurulur; hizmeti o alarm başlatır.
- **Başlangıçta:**
  - Kanal şifreli katalogda kimlikle aranır (`CatalogStore.read`). Bulunamazsa aynı kaynakta tam adla aranır; yine yoksa CHANNEL_GONE.
  - Adres o anda `SourceConfig.streamUrl(entry)` ile üretilir; düzenlenen kimlik bilgileri de böylece kullanılır.
- **Bekleme uyarısı.** API 33+'ta `PowerManager.isLowPowerStandbyEnabled()` doğruysa panelde: "Bu TV beklemedeyken uygulamaların internetini kesiyor; planlı kayıt için TV açık kalmalı." Tamamen kapanan TV'de kayıt yapılamaz; bu, Kayıtlar yardım metninde yazar.
- **EPG kayması.** Programın saati sonradan değişirse plan kendini taşımaz (Aşama 6). Paylar bunu kısmen karşılar.

## 8. Çoklu ekran (Aşama 5, L) · TV-090/091

Giriş, canlı oynatıcı kontrollerindeki "Çoklu ekran" düğmesidir. Düğme yalnız canlıda ve cihaz en az 2 karo destekliyorsa görünür. Yeni rota `Route.MULTIVIEW`.

**Akış:**
1. **Bağlantı uyarısı.** OK uyarıyı açar. Süreçte ilk girişte, sınırı bilinmeyen kaynak varsa her girişte gösterilir.
   - Başlık: "Çoklu ekran".
   - Metin: "Her ekran sağlayıcına ayrı bir bağlantı açar. Sağlayıcının izin verdiğinden fazla bağlantı yayınların kesilmesine ya da hesabının geçici olarak kilitlenmesine yol açabilir. Kaynağının aynı anda kaç yayına izin verdiğini sağlayıcından öğrenebilirsin."
   - Kaynak satırları: "Evim · Sağlayıcıya göre en çok 2" ya da "Evim · Sınır bilinmiyor · Aynı anda en çok: 1". OK değeri 1'den 4'e döndürür; sağlayıcı değeri biliniyorsa o aşılamaz.
   - [Devam et] [Vazgeç]; ilk odak Vazgeç.
2. **Açılış.** Devam edince oynatıcı bağlantısını bırakır ve karo 1 aynı kanalı açar (kısa kesinti). Karo 2 boş gelir: "+ Kanal ekle".
3. **Düzenler** (960×540 dp, 24 dp kenar, 12 dp ara; düzen değişimi animasyonsuz):

   | Karo | Düzen | Ölçü |
   |---|---|---|
   | 2 | Yan yana, dikeyde ortada | 450×253 dp |
   | 3 | Solda büyük, sağda iki küçük | 607×341 dp ve 293×165 dp |
   | 4 | 2×2, yatayda ortalı | 427×240 dp |

   Kanal adı karonun altında, scrim üstünde 14 sp tek satır. Odaktaki karoda ses simgesi var.
4. **Kumanda:**
   - Yön tuşları karolar arasında gezer; komşular `MultiViewLayout`'tan `focusProperties` ile verilir.
   - Odak beyaz 2 dp halka, büyüme yok. Ses odaktaki karodan gelir.
   - Dolu karoda OK menü açar: "Tam ekran izle", "Kanalı değiştir", "Ekran ekle" (sınır ve cihaz izin veriyorsa), "Ekranı kaldır", "Vazgeç".
   - Boş karoda OK kanal seçiciyi açar.
   - CH± ve rakamlar odaktaki karonun kanalını değiştirir; Oynat/Duraklat da odaktaki karoyu etkiler.
   - Geri: menüden ya da seçiciden ızgaraya; ızgaradan çıkışa. Çıkışta önce bütün karolar bırakılır, sonra oynatıcı odaktaki karonun kanalını açar. "Tam ekran izle" aynı şeyi yapar.
5. **Kanal seçici (TV-091).** `SourceDialog` tabanlı, numaralı kanal listesi (`catalogDisplayOrder`, Canlı TV numaraları); rakamla atlanır. "Oynatıcıda kanal listesi" (TIVIMATE_GAP P0) önce yapılırsa o katman yeniden kullanılır.

**Karo oynatıcıları:**
- PlayerScreen'deki kaynak kurulumu (210–214. satırlar) ortak `liveMediaSourceFactory(context, headers)` fonksiyonuna çıkarılır.
- Tampon küçük tutulur: `DefaultLoadControl` 5/15 sn, ≤ 8 MB. HLS varyantlarında `setMaxVideoSize`: 2 karoda 1280×720, 3–4 karoda 960×540.
- Bütün karolarda `setAudioAttributes(…, handleAudioFocus = false)`; ses odağını `MultiViewScreen` tek istekle alır ve bırakır.
- Odak dışındaki karolarda ses parçası kapalıdır (`setTrackTypeDisabled(AUDIO)`). Bu, passthrough çakışmasını ve fazladan çözücü kullanımını önler.
- Odak değişince sesin gelmesi ~0,5–1 sn gecikebilir; ölçülecek. Kabul edilmezse alternatif: bütün karolarda ses açık, odak dışında volume 0, ses çıkışı PCM'e zorlanmış.

**Cihaz kademesi `DeviceTier.multiViewTiles()`:**
- `isLowRamDevice` ise 0.
- `MediaCodecList`'teki donanım AVC/HEVC çözücülerinde `getMaxSupportedInstances()` okunur; API 29+'ta `PerformancePoint` 1080p × 30·n'yi kapsıyor mu bakılır.
- `memoryClass < 192` ise en çok 2. Sonuç 2'den azsa düğme gösterilmez.
- Çalışırken bir karo çözücü hatası verirse karoda "Bu cihaz bu kadar yayını aynı anda çözemiyor." yazar ve oturumun karo sınırı düşer.

**Bütçe ve yaşam döngüsü:**
- Her karo `acquire` çağırır. Reddedilirse karoda "Bağlantı sınırı dolu (2/2)" ve "Sınırı değiştir" kısayolu görünür.
- ON_STOP'ta bütün karolar bırakılır; ON_START'ta yeniden açılır.
- TalkBack: karo "Ekran 2 / 4, Kanal 7, ses kapalı" diye okunur; hatalar canlı bölgeden duyurulur.

## 9. Ayarlar (SettingsScreen)

"İçerik" ile "İzleme ve uygulama" arasına yeni bölüm: "Kayıt ve canlı yayın". `SettingsPage`'e `RECORDING_TARGET`, `TIMESHIFT`, `PADDING`, `STREAM_LIMITS` eklenir.

| Satır | İçerik |
|---|---|
| "Kayıt yeri" | Radyo satırları: "Cihaz depolaması · 5,2 GB boş", "USB sürücü (SanDisk) · 28 GB boş". Not: "Kayıtlar NEXTV'ye özel klasörde tutulur; uygulama kaldırılınca silinir." |
| "Canlı yayını geri sarma" | "Duraklatınca" (varsayılan), "Her zaman" (Aşama 6), "Kapalı". Ayrıca "Geri sarma süresi": 15 / 30 / 60 dk. |
| "Kayıt payları" | Önce 0/1/2/5 dk, sonra 0/3/5/10 dk. |
| "Bağlantı sınırları" | Kaynak başına "Evim · Sağlayıcı: en çok 2 · Şu an 1 kullanımda"; OK değeri döndürür. |
| "Zamanlanmış kayıt izni" | Yalnız API 34+'ta ve izin yokken görünür. |

- Bölümün altında odaklanmayan not: "Kayıtlar yalnız bu cihazda, kişisel kullanımın için saklanır. Yalnız izleme hakkın olan yayınları kaydet."
- "Kaynağı kaldır" sayfasına bir satır eklenir: "Bu kaynağın planlı kayıtları iptal edilir; kaydedilenler Kayıtlar'da kalır."

## 10. Veri modeli ve kalıcılık

**`Recording` alanları:**
- Kimlik ve kaynak: `id`, `sourceId`, `channelId`, `channelName`, `category`, `programmeTitle?`.
- Zaman: `start`, `end?`, `padBeforeS`, `padAfterS`, `createdAt`, `startedAt?`, `endedAt?`.
- Durum: `state` (SCHEDULED, RECORDING, COMPLETED, PARTIAL, FAILED, CANCELLED), `reason`, `exactAlarm`.
- Dosya: `volumeUuid` ("primary" ya da birim UUID'si), `folder` (rastgele), `bytes`, `durationMs`.
- URL, kullanıcı adı ve şifre **yok**.

**`RecordingStore`:**
- Dosya `filesDir/recordings-index.bin`; AtomicFile ve `DeviceStore.seal(…, "recordings-v1")`.
- Biçim JSON `{"v":1,"items":[…]}`; 2 MB sınırı.
- Okunamazsa liste klasörlerden "Adsız kayıt" olarak yeniden kurulur. `resetUnreadableSources` Keystore anahtarını sildiğinde de bu yol işler.

**`RecordingRepository`:** `TvApplication`'da durur; hizmet ve ViewModel ortak kullanır. `items: StateFlow`, değişiklikler `@Synchronized`.

**DeviceStore anahtarları** (gizli değil):

| Anahtar | Tür | Varsayılan |
|---|---|---|
| `rec:target` | String | "primary" |
| `rec:padBefore` / `rec:padAfter` | Int (sn) | 60 / 180 |
| `ts:mode` | String | "pause" (pause/always/off) |
| `ts:minutes` | Int | 30 |
| `streams:<sourceId>` | Int | yok (sınır 1) |
| `providerStreams:<sourceId>` | Int | yok |
| `rp:<recordingId>` | JSON (PlaybackMark biçimi) | yok |
| `exactAlarmAsked` | Boolean | false |

Kaynak silindiğinde `saveSources`, o kaynağın `streams:` ve `providerStreams:` anahtarlarını siler; `forgetSource()` hepsini siler. Çoklu ekran uyarısının görüldüğü bilgisi diske yazılmaz, süreç belleğinde tutulur.

## 11. ViewModel API

**TvState'e eklenenler:**
- `recordings`
- `captures: Map<String, CaptureProgress>`
- `storage: List<StorageTarget>`
- `timeshiftMode`
- `streamLimits`
- `multiView: MultiViewState?`
- `streamConflict: StreamConflict?`
- `scheduledByChannel: Map<String, List<LongRange>>`

Yeni rotalar: `Route.RECORDINGS`, `Route.MULTIVIEW`.

```kotlin
fun recordings()
fun recordNow(entry: MediaEntry, until: RecordUntil): RecordResult          // Started | Blocked(msg) | Conflict
fun stopRecording(id: String)
fun schedule(entry: MediaEntry, p: EpgProgramme): ScheduleResult            // Planned | NeedsExactAlarm | Conflict | Rejected(msg)
fun cancelSchedule(id: String); fun deleteRecording(id: String)
fun playRecording(id: String, fromStart: Boolean = false)
fun saveRecordingPosition(id: String, positionMs: Long, durationMs: Long)
fun recordingTarget(volume: String); fun timeshiftMode(m: TimeshiftMode); fun timeshiftMinutes(v: Int)
fun recordingPadding(before: Int, after: Int); fun streamLimit(sourceId: String, value: Int)
fun resolveConflict(accept: Boolean)
fun openMultiView(); fun multiViewSet(slot: Int, entry: MediaEntry?); fun multiViewAddSlot()
fun multiViewRemove(slot: Int); fun exitMultiView(playSlot: Int?)
fun captureFor(entry: MediaEntry): CaptureHandle?
```

Mevcut fonksiyonlarda değişiklikler:
- `play()` bütçeyi kontrol eder.
- `removeSource` kaynağın planlarını iptal eder ve süren yakalamalarını SOURCE_GONE ile bitirir.
- `connect` ve `refreshSources`, `Catalog.maxConnections`'ı saklar.

`PlayingMedia` iki alan kazanır: `origin` (DIRECT | RECORDING) ve `recordingId`. Geri sarma ViewModel'de değil, PlayerScreen'e bağlı `TimeshiftController`'da yaşar (ekran ömrü, TV-NP).

## 12. Manifest ve Play

**İzinler:** FOREGROUND_SERVICE, FOREGROUND_SERVICE_DATA_SYNC, WAKE_LOCK, SCHEDULE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED. Hiçbiri çalışma zamanı izin diyaloğu açmaz.

**Bileşenler:**
- `RecordingService`: `foregroundServiceType="dataSync"`, `exported=false`.
- `RecordingAlarmReceiver`: `exported=false`.
- `RecordingBootReceiver`: `exported=true` (BOOT_COMPLETED ve sistem saat yayınları için).

**Tür seçimi:**
- dataSync ("veri indirme/aktarma") en yakın tür.
- mediaPlayback kayıt için uygun değil; mediaProcessing dönüştürme işleri içindir; specialUse ancak başka tür uymazsa kullanılabilir.
- Android 15+'da dataSync arka planda 24 saatte toplam 6 saatle sınırlı. Bu yüzden açık uçlu kayıt 4 saatte durur; 5 saati aşan EPG programında uyarı gösterilir: "Android bu uzunlukta arka plan kaydını durdurabilir."

**Play Console:**
- "Ön plan hizmeti izinleri" beyanı: dataSync, kullanım açıklaması ve kaydın başlatıldığı kısa bir video (sentetik "Kanal 1").
- Data safety değişmez; veri cihazdan çıkmıyor.
- Mağaza görselleri sentetik içerikle hazırlanır; "maç", "film kaydet", "ücretsiz" gibi ifadeler kullanılmaz.

## 13. Hata durumları ve metinler

| Neden | Metin |
|---|---|
| NETWORK | "Bağlantı kesildi." Kısmi kayıtta: "Yarıda kaldı: bağlantı kesildi · 48 dk kaydedildi" |
| AUTH | "Sağlayıcı yayına erişim vermedi. Hesap bilgilerini kontrol et." |
| NOT_FOUND | "Yayın artık bulunamıyor." |
| LIMIT (429) | "Sağlayıcının bağlantı sınırına ulaşıldı." |
| ENCRYPTED | "Bu yayın şifreli olduğu için kaydedilemiyor." |
| FORMAT | "Bu yayın biçimi kaydedilemiyor." |
| STORAGE (başlamadan) | "Kayıt için yeterli boş alan yok. En az 1,6 GB gerekir." |
| STORAGE_FULL | "Depolama doldu." |
| VOLUME_REMOVED | "USB sürücü çıkarıldı." |
| READ_ONLY | "Bu sürücüye yazılamıyor." |
| NOT_ALLOWED | "NEXTV kapalıyken başlatılamadı." |
| OS_TIMEOUT | "Android arka plan süre sınırına ulaşıldı." |
| CHANNEL_GONE | "Kanal artık kaynakta yok." |
| SOURCE_GONE | "Kaynak kaldırıldı." |
| KILLED | "Kayıt yarıda kaldı." |
| BUDGET | "Kayıt sürüyor: bu kaynaktan aynı anda 1 yayın açılabilir." |
| MV_DECODER | "Bu cihaz bu kadar yayını aynı anda çözemiyor." |
| MV_BUDGET | "Bağlantı sınırı dolu (2/2)" |

Notlar: "Kayıt başladı · Kanal 1", "Kayıt durdu · Kayıtlar'a eklendi", "Duraklatıldı · geri sarma açık", "Arabellek sınırı: en eski noktadan devam ediyor", "Canlı yayın arabelleği silindi", "Geri sarmak için önce duraklat."

Ham hata ya da URL hiçbir yerde gösterilmez (PlaybackRecovery ilkesi).

## 14. Kenar durumları

- **İzleme + kayıt aynı kanalda:** tek bağlantı açılır, oynatıcı kayıttan oynar.
- **Yeni kanal sınır doluyken:** kayıt sürerken sınır 1 ise yeni kanal açılmaz (§4).
- **Depolama ve USB:**
  - Kayıt ortasında USB çıkarılırsa VOLUME_REMOVED; o ana kadarki kısım kalır, liste öğeyi "takılı değil" diye gösterir.
  - Kayıt ortasında yer biterse STORAGE_FULL.
  - NTFS sürücü salt okunursa seçimde reddedilir.
- **Süreç ve anahtar:**
  - Süreç ölürse yeniden başlayan hizmet öğeyi PARTIAL olarak kapatır.
  - Keystore sıfırlanırsa dosyalar korunur, liste "Adsız kayıt" olarak kurulur; alarmlar boş çalışır ve kaldırılır.
- **Kaynak ve kanal değişiklikleri:**
  - Kaynak kaldırılırsa planlar iptal edilir. Kimlik bilgisi düzenlenirse sorun yok, adres başlangıçta yeniden üretilir.
  - Yenilemeden sonra kanal kimliği değişirse ad ile eşlenir, olmazsa CHANNEL_GONE.
- **Zaman:**
  - Zamanlar epoch ms saklanır, `guideZone()` ile gösterilir. Yaz saati ve saat değişiminde TIME_SET ile alarmlar yeniden kurulur.
  - TV bekleme ya da kapalıysa kayıt cihaza bağlı (§7).
  - Uzun programda 6 saat uyarısı (§12).
- **İçerik:**
  - Gizli kategori ya da yetişkin kanal kaydedilmez ve listelenmez.
  - HLS token süresi dolarsa yeniden istenir.
  - Radyo (yalnız ses) desteklenir. Reklam geçişleri ve PTS sıçramaları discontinuity olarak yazılır.
  - 4K karo, çoklu ekranda diğer karoları düşürebilir.
  - Çoklu ekranda karolar farklı kaynaklardan olabilir; bütçe kaynak başına sayılır.
- **Erişilebilirlik:**
  - fontScale 1,5'te paneller `SourceDialog(scrollBody = true)` ile kayar, satırlar yükselir, karo adı tek satırda kesilir.
  - Hareketi azalt: yanıp sönme ve düzen animasyonu yok.
  - TalkBack ilerlemeyi dakikada bir okur, saniyede bir değil.

## 15. Testler

**JVM (önce bunlar):**
- `TsSegmenterTest`: sentetik paket kurucusuyla:
  - RAI ≥ 4 sn'de kesim, 10 sn'de zorla kesim, PAT/PMT ekleme;
  - 33 bit sarma, çöp bayttan sonra yeniden hizalama, geri giden PTS'te discontinuity;
  - yalnız ses akışı.
- `LocalPlaylistTest`: EVENT metni, ENDLIST, RING'de MEDIA-SEQUENCE ve DISCONTINUITY-SEQUENCE, 2 parça gecikmeli silme, PDT, atomik yazım (geçici klasör).
- `HlsPlaylistReaderTest`: varyant seçimi, göreli adres, sıra boşluğu, KEY → ENCRYPTED, MAP/BYTERANGE, ENDLIST.
- `CaptureSessionTest` (MockWebServer):
  - parçalı TS gövdesi;
  - istekler arasında büyüyen HLS listesi;
  - aynı anda en çok 1 istek (dispatcher sayacı);
  - 401/403/429'da tekrar yok ve mesajlar;
  - kopmada yeniden bağlanma ve discontinuity;
  - `.ts` adresinden `#EXTM3U` gelmesi.
- `StreamBudgetTest`: bilinmeyen sınır = 1, sağlayıcı tavanı, lease bırakma, oturumdan oynayan oynatıcının sayılmaması.
- `RecordingPlannerTest`: paylar, çakışma ve tekrar, 4 saat tavanı, alarm zamanı, açılışta geçmiş/süren/gelecek planın ayrımı, yaz saati.
- `RecordingCodecTest`: çift yönlü kodlama, sürüm; kodlanmış metinde "password" ya da "http" geçmemesi.
- `RecordingQuotaTest`: ayrılan pay, tahmin, durma eşiği, halka boyutu.
- `DeviceTierTest`: yetenek tanımlarından 0/2/3/4 karo.
- `MultiViewLayoutTest`: 2/3/4 karo dikdörtgenleri ve odak komşuları.
- `TimeshiftControllerTest`: PlaybackRecoveryTest'teki FakePlayer deseniyle duraklat → sürdür, Canlıya dön, BEHIND_WINDOW → başa, kanal değişiminde silme.
- `SourceTest` genişletmesi: `max_connections` metin, sayı ve yok durumları.

**Cihaz (emülatör, yalnız MockWebServer ve sentetik TS):**
- `recovery-fixture.ts` dosyası `sharedTest/resources`'a taşınır; build ayarı onu hem JVM hem androidTest'e verir.
- `CaptureStorageTest` **Aşama 1'in ilk işi**: Media3, büyüyen yerel EVENT listesini yeniliyor mu, arayabiliyor mu ve `targetOffsetMs`'e uyuyor mu doğrulanır. Ayrıca `getExternalFilesDirs` denenir.
- `RecordingFlowTest`: başlat → hap → çık → hizmet sürüyor → Kayıtlar → durdur → oynat ve sar → sil.
- `TimeshiftFlowTest`: duraklat → sürdür → "geride" → Sol → Canlıya dön → kanal değişince klasör yok → Geri ile çıkış.
- `ScheduledRecordingTest`: rehber → planla → işaret; alarm alıcısına intent doğrudan verilir.
- `MultiViewFlowTest`: uyarıda ilk odak Vazgeç; sınır 1 ikinci karoyu engeller; sınır 2 ile iki karo; ses odakta; Geri tek karoya döner.
- `RecordingsLayoutTest` (fontScale 1,5) ve TalkBack semantik denetimleri.

Gerçek sağlayıcıyla paralel akış testi yapılmaz.

## 16. Aşamalar ve efor (mühendis-günü)

| Aşama | İçerik | Boyut | Gün |
|---|---|---|---|
| 0 | StreamBudget, Xtream `max_connections`, "Bağlantı sınırları" sayfası, T0 (sağlayıcı penceresinde geri sarma) | S | 3 |
| 1 | Motor (TsSegmenter, HlsPlaylistReader/HlsCapture, LocalPlaylist, CaptureHub) ve Media3 yerel EVENT doğrulaması; RecordingService; RecordingStore; Kaydet/Kaydı durdur; Kayıtlar (kaydediliyor ve kaydedilenler; oynat, sil); cihaz depolaması ve kota | L | 14 |
| 2 | T1 duraklatınca arabellek; aynı kanalı kayıttan izleme; geri sarmayı kayda yükseltme | M | 5 |
| 3 | USB hedefi, "Kayıt yeri" sayfası, takılı olmayan ya da salt okunur birim durumları | S | 3 |
| 4 | Zamanlanmış kayıt: rehber eylemleri, işaret, alarm ve açılış alıcıları, izin akışı, çakışma kartı | M–L | 6 |
| 5 | Çoklu ekran (2–4), uyarı, kanal seçici, cihaz kademesi | L | 7 |
| 6 | "Her zaman" arabellek, elle zamanlı kayıt, EPG kaymasında planı taşıma, cihaz matrisi | M | 4 |

Toplam yaklaşık **42 gün**. Her aşama kendi başına teslim edilebilir.

Her aşamanın kabulü fiziksel TV ölçümüne bağlıdır: bekleme durumu, FAT32/exFAT USB, çözücü sayısı, 6 saat sınırı. PlayerScreen'e dokunan aşamalar, "oynatıcıda kanal listesi" işiyle sırayla yürür (AGENTS: aynı dosyada eşzamanlı değişiklik olmasın).

## 17. Sahip kararları

Her birinin önerilen varsayılanı owner_decisions alanında: ücretli sınır, kayıtların yeri, alarm izni, geri sarma varsayılanı.

## 18. Satış ve dil bağlantısı

- **Ücretli paket.** Üç özellik bir `FeatureGate` (`isEnabled(Feature.RECORDING | TIMESHIFT | MULTIVIEW)`) arkasında kurulur; varsayılan hepsi açık. Ücretli paket kararı PRD §12'deki gelir modeliyle verilir. Play Billing ülke fiyatlarını tek ürün kimliğiyle yönetir; fiyatlandırma bu kümenin konusu değil.
- **Dil.** Yeni metinler `stringResource` ile `res/values/strings.xml`'den okunur. Yazımlar şöyle:
  - "%1$s geride" gibi biçim dizgeleri.
  - Dakika ve saat için plurals; Türkçede tekil/çoğul farkı yok, İngilizcede var.
  - Boyut `Formatter.formatShortFileSize`, tarih `guideZone()` ve yerel ayarla.
  
  Uygulamanın geri kalanı satır içi kalır; bütün uygulamanın taşınması ayrı bir dil işidir.

## 19. Doğrulanamayanlar

Bunlar cihazda denenmeden kesin sayılmaz:
- Media3'ün `file://` EVENT listesini yenileme ve canlı hız davranışı (Aşama 1'in ilk testi).
- TV'lerde "Alarmlar ve hatırlatıcılar" ekranının bulunup bulunmadığı.
- Düşük Güçlü Bekleme'de ağ ve alarm davranışı.
- Sistem tarafından yeniden başlatılan hizmette `startForeground` izni.
- Android 12+'da `setWindow` alt sınırı.
- 65U95A'nın Android sürümü ve çözücü örnek sayısı.
- Bugünkü canlı TS duraklatmanın sağlayıcı tarafında ne zaman koptuğu.
- Play incelemesinin kayıt için dataSync türünü kabul edip etmeyeceği.