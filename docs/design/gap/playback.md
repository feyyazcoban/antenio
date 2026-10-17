# Oynatıcı ayarları + Tema ve görünüm (OSD süresi, panel saydamlığı) + Harici oynatıcı + Resim içinde resim (PiP)

18 Ekim 2026 · TiviMate gap tasarımı (`playback`), salt okunur tasarım ajanı; öneriler kullanıcı kararı değildir.

**Özet:** Altı ayrı teslim edilebilir aşamalı bir tasarım. Oynatıcı ayarları cihazda kalıcı tek bir PlaybackPreferences modelinde toplanıyor. Ayarlar'a yeni bir "Oynatıcı" bölümü, oynatıcıya da 440 dp genişliğinde bir "Oynatıcı seçenekleri" paneli ekleniyor. ExoPlayer, PlayerScreen.kt'deki doğrudan `ExoPlayer.Builder(context).build()` çağrısı yerine yeni bir PlayerFactory'den kuruluyor; arabellek, kod çözücü yedeği, yalnız PCM, tünel ve kare hızı stratejisi buradan veriliyor. Kodlar Media3 1.11.1 kaynağında doğrulandı. Media3'te yalnız OFF ve ONLY_IF_SEAMLESS kare hızı stratejileri var. TS H.264 yayınlarında Format.frameRate hiç doldurulmuyor; bu yüzden "Her zaman" AFR, kare zaman damgalarından kare hızını tahmin edip pencerenin preferredDisplayModeId değerini ayarlıyor. Context verilince DefaultAudioSink.Builder.setAudioCapabilities etkisiz kalıyor; PCM'e zorlama ve ses gecikmesi bu yüzden ForwardingAudioSink ile yapılıyor. Ses gecikmesi resmî bir API değil; deneysel, tünelli oynatmada kapalı. PiP yalnız FEATURE_PICTURE_IN_PICTURE varsa görünüyor. Harici oynatıcıda yayın adresi Xtream kullanıcı adı ve şifresini taşıdığı için kaynak başına bir kez uyarı soruluyor, kendi oynatıcımız tek bağlantı kuralı gereği önce durduruluyor. Not: sohbette aktarılan kullanıcı isteği satış, fiyat, yabancı dil ve ülkeler arası satın alma hakkında. Bu tasarım o soruyu yanıtlamıyor; yalnız ücretli paket sınırı ve metinlerin yerelleştirmeye hazır tutulması sahibin kararı olarak eklendi.

**Tahmini iş:** 13 gün

**Aşamalar:**
1. 1. Tercih altyapısı (PlaybackPreferences, DeviceStore, TvViewModel) + Ayarlar 'Oynatıcı' bölümü ve seçim sayfası + PlayerFactory (arabellek, kod çözücü yedeği / yazılım öncelikli, cihazda çöz (PCM), tünel, AFR Kapalı/Yalnız kesintisiz) + OSD süresi + panel saydamlığı + kod çözücü hata ipucu; JVM testleri — 2,5 gün
2. 2. Oynatıcı seçenekleri paneli: en-boy (Sığdır/Doldur/Uzat/16:9/4:3), altyazı stili (Ayarlar alt sayfası + panelde boyut, kontroller açıkken alt boşluk), yayın bilgisi; enstrümantasyon testleri — 2 gün
3. 3. AFR 'Her zaman': FrameRateEstimator, DisplayModePicker, DisplayModeController (preferredDisplayModeId), VOD'da ekran hazır olana kadar bekleme, çıkışta geri dönüş; 65U95A ve HDMI kutusunda ölçüm — 2,5 gün
4. 4. Harici oynatıcı: <queries>, StreamExposure uyarısı (kaynak başına onay), uygulama seçici, önce kendi oynatıcıyı durdurup başlatma, Ayarlar'da sıfırlama — 2 gün
5. 5. Resim içinde resim: manifest (supportsPictureInPicture + configChanges), PipHost/LocalPip, panel düğmesi, kapanma (oynatıcıdan çıkış) ve genişleme, oynat/duraklat RemoteAction, 'Ana ekrana dönünce küçük pencere' ayarı — 2 gün
6. 6. Ses gecikmesi (deneysel): ForwardingAudioSink saat kaydırma, panelde −/+50 ms ve Sıfırla, tünelde gizleme; elle dudak senkronu QA — 1,5 gün (+ belgeler ve cihaz QA ≈ 0,5 gün)

**Kullanıcı kararları (varsayılanla):**
- Ses gecikmesi resmî bir Media3 API'si değil (saat kaydırma ile kendi çözümümüz, tünelde çalışmaz). Deneysel etiketle yayımlansın mı? → varsayılan: Evet; 6. aşamada "deneysel" etiketiyle, −500…+500 ms, cihaz genelinde tek değer, tünel açıkken gizli.
- Harici oynatıcı uyarısı (adres kullanıcı adı ve şifreyi taşıyor) her seferinde mi sorulsun, kaynak başına bir kez mi? → varsayılan: Kaynak başına bir kez; Ayarlar > Oynatıcı > Harici oynatıcı uyarıları ile sıfırlanabilir.
- Oynatırken Ana ekran tuşu yayını kendiliğinden küçük pencereye alsın mı? → varsayılan: Hayır; ayar var ama varsayılanı Kapalı (TV kalite kuralı TV-NP: çıkınca duraklat). Küçük pencere yalnız düğmeyle açılır.
- Oynatıcıda seçilen en-boy yalnız o yayına mı, tüm yayınlara mı uygulansın? → varsayılan: Tüm yayınlar için varsayılan olarak kaydedilir (ses/altyazı dil hafızası gibi); kanal başına hafıza sonraki iş.
- Bu ayarlardan hangileri ücretli pakette olacak? (Satış ve fiyat sorusuyla bağlantılı; gelir modeli PRD §12'de açık) → varsayılan: Gelir modeli kararlaşana kadar hepsi ücretsiz. Kod tek bir yetenek kontrolüne hazır tutulur; oynatma sorunlarını çözen ayarlar (en-boy, altyazı, arabellek, kod çözücü, ses çıkışı) ücretli sınırın dışında bırakılmalı.

**Riskler:**
- AFR: TV panelleri ve kutular farklı Display.Mode listeleri bildiriyor; 65U95A'nın 24/25/50 Hz modu sunup sunmadığı bilinmiyor. HDMI'da geçiş 1–3 sn kararma yapar; preferredDisplayModeId'nin Android 12+ 'içerik kare hızını eşleştir' tercihiyle etkileşimi cihazda doğrulanmalı.
- Kare hızı tahmini: TS H.264 Format'ında kare hızı yok (H264Reader). VideoFrameMetadataListener tünelli oynatmada çağrılmayabilir; o durumda AFR çalışmaz.
- Configuration değişikliklerini MainActivity'nin kendisinin işlemesi (PiP için gerekli), ekran boyutu ve düzen değişikliklerinde Activity'nin yeniden yaratılmasını da durdurur; yeniden yaratmaya güvenen bir davranış varsa etkilenir. PiP Android TV'de isteğe bağlı; birçok OEM TV'de FEATURE yok, kullanıcı uygulama için kapatabilir.
- Ses gecikmesi resmî API değil: medya saatini kaydırmak geçişte kısa kare düşmesine yol açabilir, gelecekteki Media3 sürümlerinde ForwardingAudioSink davranışı değişebilir (UnstableApi).
- 'Cihazda çöz' seçeneğinde cihazın DTS/AC-3 çözücüsü yoksa ses tamamen kaybolabilir; 'Yazılım öncelikli' yüksek çözünürlükte takılma yapar. Ayar metinleri uyarıyor, ama destek talebi riski var.
- Tünelli oynatma bazı SoC'lerde siyah ekran ya da A/V kayması yapabilir; deneysel ve varsayılanı kapalı.
- Harici oynatıcı: Xtream adresi kullanıcı adı ve şifreyi yolun içinde taşıdığı için seçilen uygulama bunları görür ve saklayabilir (Play User Data riski). Uyarı bunu söylüyor ama engellemiyor. Başlık bilgisi isteyen M3U yayınları birçok oynatıcıda açılmaz; ilerleme geri gelmez.
- Panel saydamlığı 'Saydam' (%80) beyaz sahnede Ink.Muted için 5,1:1 veriyor; daha düşük alfa önerilmez. Dialog dim kapatma tv-material/Compose sürümüne göre DialogWindowProvider gerektirir.
- Buffer 'Çok geniş' canlı kanal geçişini birkaç saniye uzatır. Bayt sınırı varsayılan kaldığı için 4K'da hedef saniyeye ulaşılamayabilir; bellek güvenli kalır.
- Oynatıcının PlayerFactory'den kurulması (DefaultTrackSelector parametre birleştirmesi) mevcut ses/altyazı dil hafızasını bozmamalı; TracksLayoutTest ve PilotFlowTest tekrar koşulmalı.
- Kapsam uyumsuzluğu: Sohbetteki kullanıcı isteği satış, fiyat ve çok dilli satın alma hakkında; bu tasarım o soruyu yanıtlamıyor, yalnız ücretli sınır ve yerelleştirme hazırlığını karar olarak not ediyor.

**Dokunulacak dosyalar:**
- `apps/android-tv/app/src/main/AndroidManifest.xml`: MainActivity'ye android:supportsPictureInPicture="true" ve configChanges="screenSize|smallestScreenSize|screenLayout|orientation" eklenir; http/https video/* için ACTION_VIEW <queries> eklenir
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/PlaybackPreferences.kt`: Yeni: enum'lar, PlaybackPreferences, SubtitleStyle, BufferPreset değerleri, saf PlaybackPreferencesCodec
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/DeviceStore.kt`: player.* anahtarları için var playback; externalAck ve externalLastPackage; saveSources içinde kaldırılan kaynakların ack temizliği; forgetSource external.ack'ı siler, player.* anahtarlarını korur
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/TvViewModel.kt`: TvState.playback ve externalAck (açılış ve resetUnreadableSources içindeki kurulum); playback(change), externalPlayerUsed, resetExternalWarnings
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerFactory.kt`: Yeni: tercihlerden ExoPlayer kurulumu (DefaultRenderersFactory + decoder fallback + yazılım öncelikli MediaCodecSelector, DefaultLoadControl ön ayarı, DefaultTrackSelector tüneli, kare hızı stratejisi); PlayerAudioSink (ForwardingAudioSink: yalnız PCM + saat kaydırma) ve AudioDelay
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/FrameRateMatching.kt`: Yeni: saf FrameRateEstimator ve DisplayModePicker; preferredDisplayModeId + DisplayListener kullanan DisplayModeController
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerOptions.kt`: Yeni: PlayerOptionsDialog (Görüntü, Altyazı boyutu, Ses gecikmesi, Eylemler, Yayın bilgisi), ortak RadioRow, saf ControlsAutoHide, AspectMode eşlemesi, PlayerCopy metinleri
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/ExternalPlayer.kt`: Yeni: StreamExposure, ExternalLaunchSpec, uygulama sorgusu, uyarı ve seçici pencereleri (SourceDialog kalıbı), tek oturumlu başlatma yardımcısı
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PictureInPicture.kt`: Yeni: PipHost, LocalPip (varsayılan Unsupported), pipAspect kıstırma, RemoteAction oynat/duraklat alıcısı, otomatik giriş
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt`: Oynatıcı PlayerFactory'den; trackPanel yerine PlayerPanel enum'u; Seçenekler düğmesi; AndroidView update bloğunda resizeMode, en-boy kutusu ve altyazı stili/alt boşluk; OSD süresi ve TalkBack; panel alfası ve dim; AFR bağlantısı; PiP çizimi, kapanma ve genişleme; harici başlatma; desteklenmeyen ses notu
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SettingsScreen.kt`: Oynatıcı bölümü; PLAYER_CHOICE ve SUBTITLES sayfaları; satır başına FocusRequester haritası ve geri zinciri; harici uyarı sıfırlama satırı; durum notları
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlaybackRecovery.kt`: Kod çözücü hata mesajına Ayarlar > Oynatıcı ipucu
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/MainActivity.kt`: PipHost oluşturma, addOnPictureInPictureModeChangedListener (kapanma ve genişleme ayrımı), API 31 altı için onUserLeaveHint, LocalPip sağlama
- `apps/android-tv/app/src/main/res/drawable/ic_pip_play.xml, ic_pip_pause.xml`: Yeni vektör simgeler (PiP RemoteAction)
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/PlaybackPreferencesTest.kt`: Yeni JVM testi
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/ui/{FrameRateEstimatorTest,DisplayModePickerTest,StreamExposureTest,PlayerAudioSinkTest,ControlsAutoHideTest,PipAspectTest,AspectModeTest}.kt`: Yeni JVM testleri
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/ui/PlaybackRecoveryTest.kt`: Kod çözücü mesajı ipucu doğrulaması
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/{PlayerSettingsFlowTest,PlayerOptionsLayoutTest,ExternalPlayerFlowTest,PictureInPictureFlowTest}.kt`: Yeni enstrümantasyon testleri
- `apps/android-tv/app/src/androidTest/AndroidManifest.xml`: Harici oynatıcı testi için ACTION_VIEW video/* alan taslak Activity (yoksa oluşturulur)
- `docs/decisions/DECISIONS.md, docs/design/SCREENS.md, docs/design/NATIVE_UI.md, docs/product/TIVIMATE_GAP.md, docs/project/STATUS.md, docs/project/HANDOFF.md, docs/project/BACKLOG.md`: Yeni karar kaydı (sahip yanıtlarıyla), oynatıcı seçenekleri paneli ve Ayarlar Oynatıcı bölümü sözleşmesi, gap satırlarının güncellenmesi, devir notları

---

# Oynatıcı ayarları, görünüm, harici oynatıcı ve PiP — uygulama tasarımı

18 Ekim 2026 · kod okuması (0.14.43 çalışma ağacı) + Media3 1.11.1 kaynak kontrolü. Öncelik ve varsayılanlar **öneridir**; sahip kararları en sonda.

> Kapsam notu: Sohbette aktarılan kullanıcı isteği satış stratejisi, fiyat, yabancı dil davranışı ve farklı ülkelerden satın alma hakkında. Bu belge, iş akışının verdiği oynatıcı ayarları kümesini tasarlıyor; fiyat ve satış sorusuna cevap değildir. Bağlantılı iki nokta §11'de karar olarak duruyor: ücretli sınır ve metinlerin yerelleştirilmesi.

## 0. Bugünkü durum (kodda)

- `ui/PlayerScreen.kt` · `PlayerScreen`: oynatıcı `remember { ExoPlayer.Builder(context).build() }` ile kuruluyor. Yalnız `setAudioAttributes(DEFAULT, true)`, `setHandleAudioBecomingNoisy` ve dil tercihleri (`TrackSelectionParameters`) veriliyor. LoadControl, RenderersFactory ve TrackSelector Media3 varsayılanı. Kare hızı stratejisi de varsayılan, yani `ONLY_IF_SEAMLESS`.
- Video `AndroidView(PlayerView)`: `useController=false`, varsayılan SurfaceView, varsayılan `RESIZE_MODE_FIT`. Altyazı stili PlayerView'in varsayılanı, yani sistem altyazı ayarı.
- Kontroller sabit **8000 ms** sonra gizleniyor (`LaunchedEffect(player)` döngüsü). Köşe notu ve üst etiket `Ink.Background.copy(alpha = .9f)`. `TracksDialog` opak `Ink.Background`.
- `ON_STOP` anında `recovery.background()` duraklatıyor; `ON_START` anında `resumeOnStart` ise devam ediyor. `LaunchedEffect(media)` yeni yayından önce eskisini durduruyor (tek oturum).
- `MainActivity.kt`: PiP yok. `AndroidManifest.xml`: `supportsPictureInPicture`, `configChanges` ve `<queries>` yok.
- `data/Models.kt` · `SourceConfig.streamUrl`: Xtream adresi `/live/<kullanıcı>/<şifre>/<id>.ts` biçiminde, yani kimlik bilgisi **yolun içinde**.
- `data/DeviceStore.kt`: cihaz tercihleri düz anahtarlarda (`reduced`, `autoNextEpisode`, `subtitlesOff`…). `forgetSource()` bunları bilinçli olarak korur.
- `ui/SettingsScreen.kt`: `SettingsPage { MAIN, REMOVE, HISTORY, ABOUT, CATEGORIES, CATEGORY_LIST }`. `SettingRow` yalnız aç/kapat satırı; çoklu seçim satırı yok.
- UI metinleri Kotlin'e gömülü Türkçe; `res/values` içinde yalnız `styles.xml` var.

## 1. Media3 1.11.1 ve Android ne sunuyor (doğrulandı)

| Özellik | Gerçek API | Bu tasarımda |
|---|---|---|
| AFR, kesintisiz geçiş | `ExoPlayer.setVideoChangeFrameRateStrategy`: **yalnız** `C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF` ve `_ONLY_IF_SEAMLESS` var (C.java). İkincisi varsayılan; API 30+ cihazda `Surface.setFrameRate` çağırır. | "Kapalı" ve "Yalnız kesintisiz" doğrudan bu iki sabit |
| AFR, zorunlu geçiş | Media3'te ALWAYS stratejisi yok. Android belgesi, Android TV'de ağır mod geçişini zorlamak için `WindowManager.LayoutParams.preferredDisplayModeId` (API 23+) diyor. `Surface.setFrameRate(…, CHANGE_FRAME_RATE_ALWAYS)` ise kullanıcının "içerik kare hızını eşleştir" tercihine bağlı. | "Her zaman": strateji OFF + `preferredDisplayModeId`. Aynı çözünürlükte yalnız yenileme hızı değişir |
| Kare hızını bilmek | TS H.264'te `H264Reader` Format'a `setFrameRate` yazmıyor; Xtream canlıda varsayılan uzantı `ts`. HLS'te `FRAME-RATE` ve MP4'te değer olabilir. | Önce `videoFormat.frameRate`, yoksa `setVideoFrameMetadataListener` zaman damgalarından tahmin |
| Çözünürlük geçişi | `preferredDisplayModeId` ile mümkün | **Yapılmıyor** (TV paneli yerel çözünürlükte kalır) |
| En-boy / zoom | `PlayerView.setResizeMode`: FIT, FILL, ZOOM, FIXED_* | Sığdır, Doldur, Uzat + 16:9 ve 4:3 (Compose `aspectRatio` kutusu + FILL). Yüzdeyle zoom yok, SurfaceView ölçeklenmez |
| Arabellek | `DefaultLoadControl.Builder.setBufferDurationsMs(min, max, playback, rebuffer)`. Varsayılan 50 000 / 50 000 / 1 000 / 2 000 ms. Kısıt: min ≥ playback, min ≥ rebuffer, max ≥ min | 3 ön ayar |
| Kod çözücü | `DefaultRenderersFactory.setEnableDecoderFallback`, `setMediaCodecSelector`. Uzantı renderer'ları (ör. `FfmpegAudioRenderer`) yansımayla aranır; projede yoklar, Maven'da hazır yayımlanmıyorlar. | Yedek kod çözücü açık + "Yazılım öncelikli" seçici. FFmpeg uzantısı **yok** |
| Tünel | `DefaultTrackSelector.Parameters.Builder.setTunnelingEnabled` | Deneysel anahtar |
| Passthrough | 1.11.1'de yetenekler `AudioTrackAudioOutputProvider` içindeki `AudioCapabilitiesReceiver`'dan gelir. Context varken `DefaultAudioSink.Builder.setAudioCapabilities` deprecated ve etkisiz; passthrough'u kapatan genel bir seçenek yok. | "Otomatik" = varsayılan. "Cihazda çöz" = `ForwardingAudioSink` kodlu biçimi reddeder, `MediaCodecAudioRenderer` PCM'e çözer |
| Ses gecikmesi | Resmî API **yok** | Deneysel: `ForwardingAudioSink.getCurrentPositionUs` saat kaydırması, −500…+500 ms. Tünelde kapalı |
| Altyazı stili | `SubtitleView.setStyle(CaptionStyleCompat)`, `setFractionalTextSize`, `setApplyEmbeddedStyles`, `setBottomPaddingFraction`, `setUserDefaultStyle` | Android ayarı / NEXTV ayarı |
| PiP | `Activity.enterPictureInPictureMode`, `PictureInPictureParams`; `FEATURE_PICTURE_IN_PICTURE` TV'de isteğe bağlı | Özellik varsa düğme |
| Harici oynatıcı | `ACTION_VIEW`; API 30+ paket görünürlüğü için `<queries>` | Uyarılı, tek oturum korunarak |

Yapılmayanlar: kanal başına en-boy hafızası, çözünürlük geçişi, harici oynatıcıdan ilerleme geri alma, MediaSession (yeni bağımlılık), FFmpeg, yüzde zoom, deinterlace seçimi.

## 2. Kumanda akışları

**A. Oynatırken en-boy.** Kontroller açıkken Sağ ile "Seçenekler"e gel → OK. Sağda "Oynatıcı seçenekleri" açılır; ilk odak Görüntü bölümündeki seçili satırdadır (ör. Sığdır). Aşağı ile "Doldur"a in → OK: hemen uygulanır, onay işareti kayar, panel açık kalır. Geri paneli kapatır ve odak "Seçenekler"e döner. İkinci Geri kontrolleri gizler, üçüncüsü oynatıcıdan çıkar (mevcut `BackHandler` sırası).

**B. Ayarlar'da çoklu seçim.** Ayarlar → Aşağı ile Oynatıcı bölümünde "Arabellek" → OK. "Arabellek" seçim sayfası açılır, odak geçerli değerdedir. Aşağı → OK "Geniş": seçim kaydedilir, sayfa kapanır, odak "Arabellek" satırına döner; açıklama "Geniş · Bir sonraki yayında geçerli" olur. Geri seçim yapmadan aynı satıra döner.

**C. Altyazı görünümü.** Ayarlar → "Altyazı görünümü" → OK → alt sayfa (Kaynak, Boyut, Zemin, Renk). Her satır B'deki seçim sayfasını açar. Geri sırası: seçim → alt sayfa → ana liste.

**D. Harici oynatıcı.** Seçenekler → Aşağı ile "Harici oynatıcıda aç" → OK.
1. Bu kaynak için ilk kezse uyarı penceresi açılır; ilk odak "Vazgeç". Sağ ile "Devam et" → OK.
2. Birden çok uygulama varsa "Oynatıcı seç" listesi gelir; odak son kullanılandadır. OK.
3. Kendi oynatıcımız durur, harici uygulama açılır.

Harici uygulamada Geri basınca NEXTV'de oynatıcıdan önceki ekrana dönülür. Uyarıda Geri = Vazgeç; odak "Harici oynatıcıda aç" satırına döner.

**E. PiP.** Seçenekler → "Küçük pencerede oynat" → OK. Panel kapanır ve yayın küçük pencerede sürer. Ana ekran tuşu launcher'a gider; tam ekran ve kapatma sistemin PiP menüsündedir. "Ana ekrana dönünce küçük pencere" açıksa Ana ekran tuşu oynatırken doğrudan PiP'e geçer.

**F. AFR "Her zaman".** Yayın açılır, kare hızı bulunur ve gerekirse ekran modu değişir; köşe notunda "Ekran 50 Hz'e geçiyor…" görünür. Filmde ve bölümde oynatma, ekran hazır olana kadar (en çok 5 sn) duraklar; durum satırında "Ekran ayarlanıyor…" yazar, birincil düğme "Hemen oynat" olur. Canlıda yayın durmaz.

## 3. Ekran ve UI değişiklikleri

### 3.1 PlayerScreen (`ui/PlayerScreen.kt`)
- `var trackPanel: Boolean` yerine `var panel by remember { mutableStateOf<PlayerPanel?>(null) }`; `enum class PlayerPanel { TRACKS, OPTIONS }`. `trackPanel` geçen her yerde (`controlsShown`, `hidden`, `BackHandler`, `LaunchedEffect(visible, …)`, rakam koşulu, açılış pill'i ofseti) `panel != null` kullanılır. `TracksDialog` ve test etiketleri değişmez.
- Kontrol satırında "Ses / Altyazı"dan sonra `Action("Seçenekler", icon = "settings")` gelir. Satır zaten `horizontalScroll`; büyük yazıda kayar.
- Oynatıcı `remember { PlayerFactory.create(context, screenState.playback, audioDelay) }` ile kurulur. Tercihler oynatıcı kurulurken okunur. Ayarlar oynatıcıdan açılmadığı için (PilotApp'te `settings` yalnız diğer rotalarda) değişiklik doğal olarak bir sonraki yayında geçerli olur.
- `AndroidView` bir `update` bloğu alır:
  - `resizeMode`: FIT, ZOOM veya FILL.
  - 16:9 ve 4:3 için dıştaki `Box` `Modifier.aspectRatio(r).align(Center)`, iç mod FILL.
  - `subtitleView` stili (§6.5) ve kontroller görünürken `setBottomPaddingFraction(.30f)`, değilse `SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION`.
  - PiP'te her zaman FIT.
- OSD: 8000 yerine `rememberUpdatedState(screenState.playback.osdTimeout.ms)`. Karar saf fonksiyonda: `ControlsAutoHide.shouldHide(now, lastInteraction, timeoutMs, playing, panelOpen, failure, recovering, talkBack)`. TalkBack (`isTouchExplorationEnabled`, HomeScreen'deki gibi) açıkken kontroller kendiliğinden gizlenmez.
- Panel saydamlığı: köşe notu, üst etiket ve "Yayın açılıyor…" pill'i `Ink.Background.copy(alpha = playback.panelOpacity.alpha)` kullanır. `TracksDialog` ve `PlayerOptionsDialog` gövdesi de aynı alfa değerini alır; alfa < 1 iken `DialogWindowProvider` penceresinde `setDimAmount(0f)` çağrılır. Scrim değişmez.
- PiP aktifken (`LocalPip.current.active`) yalnız video çizilir: not, pill, kontroller, paneller ve `EpisodeEndDialog` çizilmez.

### 3.2 Yeni `ui/PlayerOptions.kt` · `PlayerOptionsDialog`
- `TracksDialog` ile aynı kabuk: 440 dp, sağa yaslı, başlık "Oynatıcı seçenekleri", sağ üstte "Kapat", kaydırma ve `BringIntoViewSpec`, giriş odağı `focusProperties { onEnter }`.
- Bölümler sırasıyla:
  1. **Görüntü**: 5 seçenek satırı.
  2. **Altyazı boyutu**: 4 satır.
  3. **Ses gecikmesi** (aşama 6): değer metni + `Action("− 50 ms")`, `Action("+ 50 ms")`, `Action("Sıfırla")`.
  4. **Eylemler**: "Küçük pencerede oynat" yalnız `pip.supported` ise; "Harici oynatıcıda aç".
  5. **Yayın bilgisi**: `Note`.
- Satır bileşeni yeni ortak `RadioRow(label, description?, selected, enabled, status?, onClick)`: `TrackChoice` görünümü, `Role.RadioButton`, `selected`, `stateDescription` "Seçili" veya "Seçili değil". Bu bir yüzey; odakta `Ink.High` dolgu ve `focusRing(TvLayout.CardRadius)` (beyaz 2 dp), ölçek 1.0. Eylemler mint dolgulu `Action`. Bunlar D064'e uygun.
- Yayın bilgisi `player.videoFormat` ve `audioFormat` ile tahmini kare hızından oluşur: "1920×1080 · 25 kare/sn · Ekran 50 Hz · Ses AC-3 5.1". AFR'nin cihazda doğrulanması için de kullanılır.

### 3.3 Ayarlar (`ui/SettingsScreen.kt`)
- "İçerik" ile "İzleme ve uygulama" arasına `section("Oynatıcı")` gelir. Satırlar:
  1. Kare hızı eşleştirme
  2. Arabellek
  3. Kod çözücü
  4. Ses çıkışı
  5. Tünelli oynatma (deneysel, aç/kapat)
  6. Altyazı görünümü (alt sayfa)
  7. Kontrollerin kapanma süresi
  8. Oynatıcı panelleri
  9. Ana ekrana dönünce küçük pencere (yalnız PiP destekliyse, aç/kapat)
  10. Harici oynatıcı uyarıları (yalnız onaylı kaynak varsa)
- `SettingsPage`'e `PLAYER_CHOICE` ve `SUBTITLES` eklenir. `var choice by remember { mutableStateOf<PlayerChoice?>(null) }`; `PlayerChoice` her çoklu seçimin anahtarıdır.
- Her satıra bir `FocusRequester` (`Map<PlayerChoice, FocusRequester>`) bağlanır; `entryFocus` dönüşte açan satırı seçer.
- `back()`: `PLAYER_CHOICE` → `choice.parent` (MAIN veya SUBTITLES); `SUBTITLES` → MAIN.
- Seçim sayfası `RadioRow` listesidir; OK seçer, kaydeder ve bir basamak geri döner.
- Satır açıklaması "geçerli değer · kısa açıklama" biçimindedir. Durum notları (§7) `Note` ile satırın altında durur.

### 3.4 `MainActivity.kt` + yeni `ui/PictureInPicture.kt`
- `PipHost` ve `LocalPip`. MainActivity `setContent { CompositionLocalProvider(LocalPip provides pip) { PilotTheme { … } } }` yazar. Testlerdeki `PilotApp` çağrıları değişmez; varsayılan `PipHost.Unsupported`.

## 4. Veri modeli ve kalıcılık

Yeni `data/PlaybackPreferences.kt`:
```kotlin
enum class FrameRateMatching { OFF, SEAMLESS, ALWAYS }            // varsayılan SEAMLESS (bugünkü davranış)
enum class BufferPreset(val minMs: Int, val maxMs: Int, val playMs: Int, val rebufferMs: Int) {
    NORMAL(50_000, 50_000, 1_000, 2_000),   // Media3 varsayılanı = bugünkü davranış
    LARGE(50_000, 60_000, 2_500, 5_000),
    MAX(60_000, 120_000, 5_000, 10_000) }   // bayt sınırı varsayılan kalır (prioritizeTime=false), bellek patlamaz
enum class DecoderMode { AUTO, SOFTWARE_FIRST }
enum class AudioOutput { AUTO, PCM }
enum class AspectMode { FIT, ZOOM, STRETCH, RATIO_16_9, RATIO_4_3 }
enum class SubtitleSource { SYSTEM, APP }
enum class SubtitleSize(val factor: Float) { SMALL(.8f), NORMAL(1f), LARGE(1.25f), XLARGE(1.5f) }
enum class SubtitleBackground { NONE, SEMI, SOLID }
enum class SubtitleColor { WHITE, YELLOW }
enum class OsdTimeout(val ms: Long) { S4(4_000), S8(8_000), S15(15_000), S30(30_000) }
enum class PanelOpacity(val alpha: Float) { OPAQUE(1f), DEFAULT(.9f), SHEER(.8f) }
data class SubtitleStyle(val source: SubtitleSource = SYSTEM, val size: SubtitleSize = NORMAL,
    val background: SubtitleBackground = SEMI, val color: SubtitleColor = WHITE)
data class PlaybackPreferences(
    val frameRate: FrameRateMatching = SEAMLESS, val buffer: BufferPreset = NORMAL,
    val decoder: DecoderMode = AUTO, val audioOutput: AudioOutput = AUTO, val tunneling: Boolean = false,
    val audioDelayMs: Int = 0, val aspect: AspectMode = FIT, val subtitle: SubtitleStyle = SubtitleStyle(),
    val osdTimeout: OsdTimeout = S8, val panelOpacity: PanelOpacity = DEFAULT, val pipOnHome: Boolean = false) {
    fun normalized() = copy(audioDelayMs = (audioDelayMs / 50 * 50).coerceIn(-500, 500))
}
internal object PlaybackPreferencesCodec {   // saf; JVM testli
    fun decode(read: (String) -> Any?): PlaybackPreferences   // bilinmeyen ad → varsayılan
    fun encode(p: PlaybackPreferences): Map<String, Any>
}
```
Panel alfa değerleri hesapla seçildi: beyaz karenin üstünde %80 `Ink.Background` iken `Ink.Muted` 5,1:1, `Ink.Text` 8,4:1; %75'te Muted 4,26:1'e düşüp 4,5 hedefinin altında kalıyor (COLOR.md).

**DeviceStore anahtarları** (adresler ve hesaplar asla yazılmaz):
- `player.frameRate`, `player.buffer`, `player.decoder`, `player.audioOutput`, `player.aspect`, `player.osdTimeout`, `player.panelOpacity` (enum adı)
- `player.tunneling`, `player.pipOnHome` (Boolean); `player.audioDelayMs` (Int)
- `player.subtitle.source`, `.size`, `.background`, `.color`
- `external.lastPackage` (String; paket adı)
- `external.ack` (StringSet; yalnız kaynak kimlikleri)

`DeviceStore`'a `var playback: PlaybackPreferences` eklenir (tek `edit()…apply()`), ayrıca `var externalAck: Set<String>` ve `var externalLastPackage: String?`. `saveSources` içinde `removed.forEach` kaldırılan kimliği `external.ack`'tan da çıkarır. `forgetSource()` `external.ack`'ı siler, `player.*` anahtarlarını korur (oradaki yoruma uygun).

## 5. ViewModel API (`TvViewModel.kt`)
- `TvState`'e `val playback: PlaybackPreferences = PlaybackPreferences()` ve `val externalAck: Set<String> = emptySet()` eklenir. İkisi de açılış `MutableStateFlow(TvState(...))` çağrısında ve `resetUnreadableSources()` içindeki `TvState(route = SETUP, …)` kurulumunda `store`'dan doldurulur.
- `fun playback(change: (PlaybackPreferences) -> PlaybackPreferences)` → `normalized()`, `store.playback = it`, `mutable.update { it.copy(playback = …) }`.
- `fun externalPlayerUsed(sourceId: String, packageName: String)` → kaynağı `externalAck`'a ekler, `externalLastPackage`'ı yazar.
- `fun resetExternalWarnings()`.
- Mevcut `back()` harici oynatıcıya geçişte ve PiP kapanınca oynatıcıdan çıkmak için kullanılır (`Route.PLAYER → playerReturn`).

## 6. Teknik ayrıntılar

### 6.1 `ui/PlayerFactory.kt`
```kotlin
internal object PlayerFactory {
    fun create(context: Context, p: PlaybackPreferences, delay: AudioDelay): ExoPlayer {
        val renderers = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(c: Context, float: Boolean, params: Boolean): AudioSink? =
                super.buildAudioSink(c, float, params)?.let { PlayerAudioSink(it, pcmOnly = p.audioOutput == AudioOutput.PCM, delay) }
        }.setEnableDecoderFallback(true)
         .apply { if (p.decoder == DecoderMode.SOFTWARE_FIRST) setMediaCodecSelector(SoftwareFirstSelector) }
        val load = DefaultLoadControl.Builder().setBufferDurationsMs(p.buffer.minMs, p.buffer.maxMs, p.buffer.playMs, p.buffer.rebufferMs).build()
        val selector = DefaultTrackSelector(context).apply { setParameters(buildUponParameters().setTunnelingEnabled(p.tunneling)) }
        return ExoPlayer.Builder(context, renderers).setLoadControl(load).setTrackSelector(selector).build().apply {
            setVideoChangeFrameRateStrategy(if (p.frameRate == FrameRateMatching.SEAMLESS) C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_ONLY_IF_SEAMLESS
                else C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF)
            setAudioAttributes(AudioAttributes.DEFAULT, true); setHandleAudioBecomingNoisy(true)
        }
    }
}
private val SoftwareFirstSelector = MediaCodecSelector { mime, secure, tunneling ->
    MediaCodecUtil.getDecoderInfos(mime, secure, tunneling).sortedBy { if (it.softwareOnly) 0 else 1 } }
```
- PlayerScreen'deki dil parametreleri (`setPreferredAudioLanguage`…) olduğu gibi kalır. `DefaultTrackSelector.setParameters(TrackSelectionParameters)` gelen değeri mevcut parametrelerle birleştirir; `buildUpon()` `DefaultTrackSelector.Parameters.Builder` döner. Bu yüzden `TracksDialog` ve `LaunchedEffect(media)` tünel ayarını silmez. Testte doğrulanacak.
- `PlayerAudioSink : ForwardingAudioSink`:
  - `pcmOnly` iken `supportsFormat` ve `getFormatSupport`, `sampleMimeType != MimeTypes.AUDIO_RAW` için false / `SINK_FORMAT_UNSUPPORTED` döner. Renderer o zaman cihazın MediaCodec çözücüsüyle PCM üretir.
  - `getCurrentPositionUs(ended)`: `CURRENT_POSITION_NOT_SET` olduğu gibi geçer, değilse `+ delay.us` eklenir.
  - `AudioDelay` bir `AtomicLong`; UI iş parçacığından canlı değişir, oynatıcı yeniden kurulmaz.
  - İşaret: pozitif değer sesi geciktirir (saat ileri, görüntü önce gelir), negatif değer sesi öne alır.
  - `enableTunnelingV21()` çağrıldıysa gecikme 0 sayılır.

### 6.2 AFR "Her zaman" (`ui/FrameRateMatching.kt`)
- `FrameRateEstimator` (saf): `add(presentationTimeUs)`, `reset()`, `estimate(): Float?`.
  - Ardışık farkların medyanı alınır; en az 48 kare gerekir; farklar medyandan %5'ten fazla sapan kareler atılır.
  - Sonuç {23.976, 24, 25, 29.97, 30, 47.952, 48, 50, 59.94, 60}'a %1 içinde oturtulur; yoksa null.
  - Zaman geri giderse veya 1 sn'den büyük atlama olursa (arama, kanal geçişi) sıfırlanır.
- `DisplayModePicker.pick(modes: List<ModeSpec>, current: ModeSpec, fps: Float): ModeSpec?` (saf):
  - Yalnız mevcut çözünürlükle aynı modlar aday olur.
  - Yenileme hızı fps'in tam katı olmalı (göreli hata ≤ %0,2).
  - Tercih ≥ 47 Hz'lik en düşük kat, yoksa en yüksek kat (24→48 varsa, yoksa 24; 25→50; 29.97→59.94).
  - Mevcut mod uyuyorsa veya aday yoksa null.
- `DisplayModeController(window, display, scope)`:
  - `apply(fps)` aday varsa `window.attributes = window.attributes.apply { preferredDisplayModeId = id }` yazar ve `DisplayManager.DisplayListener.onDisplayChanged`'i bekler (en çok 5 sn).
  - `restore()` → `preferredDisplayModeId = 0`.
- PlayerScreen bağlantısı (yalnız `ALWAYS` iken):
  - `player.setVideoFrameMetadataListener` oynatma iş parçacığında estimator'ı besler; sonuç ana iş parçacığına yollanır. Önce `player.videoFormat?.frameRate > 0` denenir.
  - Kanal geçişinde yeni fps aynı modu istiyorsa geçiş yok.
  - VOD'da geçiş sırasında `player.pause()`; hazır olunca 1,5 sn sonra `play()`. "Hemen oynat" bekleme yapmadan oynatır. Canlıda duraklatma yok.
  - `restore()` şu durumlarda çağrılır: `onDispose`, `ON_STOP`, PiP'e girişte.
  - Tünelde metadata dinleyicisi çağrılmayabilir; o durumda yalnız `Format.frameRate`, o da yoksa geçiş yok (cihazda doğrulanacak).
- Ayarlar satırının durum notu:
  - Aynı çözünürlükte tek yenileme hızı varsa "eşleştirme yapılamaz" notu.
  - API 31+ cihazda SEAMLESS seçiliyken `display.matchContentFrameRateUserPreference == MATCH_CONTENT_FRAMERATE_NEVER` ise sistem ayarı notu.

### 6.3 PiP (`ui/PictureInPicture.kt`)
- Manifest, MainActivity üzerine: `android:supportsPictureInPicture="true"` ve `android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"`. Bu olmazsa PiP'e geçiş Activity'yi yeniden yaratır, `remember`'daki ExoPlayer yeniden kurulup ikinci bağlantı açılır.
- `PipHost(activity)`:
  - `supported = packageManager.hasSystemFeature(FEATURE_PICTURE_IN_PICTURE)`.
  - Durum alanları `active` ve `dismissals` (state).
  - `enter(width, height): PipResult` → `OK`, `DISABLED`, `FAILED`. `IllegalStateException` yakalanır. AppOps `OPSTR_PICTURE_IN_PICTURE` ile kullanıcı kapatmış mı bakılır (API 29+ `unsafeCheckOpNoThrow`, altında `checkOpNoThrow`).
  - `setAutoEnter(enabled)`: API 31+ `setPictureInPictureParams(setAutoEnterEnabled)`. API 26–30 için `MainActivity.onUserLeaveHint()` → `pip.onUserLeaveHint()`.
- En-boy: `pipAspect(w, h, pixelRatio)` saf fonksiyonu sonucu 100:239 … 239:100 aralığına kıstırır (aşılırsa platform istisna atar); boyut bilinmiyorsa 16:9.
- `addOnPictureInPictureModeChangedListener { info -> … }`:
  - false geldiğinde `lifecycle.currentState == CREATED` ise pencere **kapatılmıştır** → `dismissals++`.
  - `STARTED` ise tam ekrana dönülmüştür.
- PlayerScreen:
  - Girişten önce `panel = null`, bir kare sonra `enter`.
  - `dismissals` değişince `save()`, `resumeOnStart = false`, `model.back()`. Böylece bağlantı kapanır; uygulamaya dönen kullanıcı listeyi görür.
  - Tam ekrana dönüşte `interact()` ve odak.
- PiP penceresinde tek eylem: `RemoteAction` Duraklat/Oynat. `PendingIntent.FLAG_IMMUTABLE` + `setPackage` kullanılır. Alıcı `ContextCompat.registerReceiver(…, RECEIVER_NOT_EXPORTED)` ile `DisposableEffect`'te kaydedilir. Simgeler yeni `ic_pip_play` ve `ic_pip_pause` vektörleri.
- `ON_PAUSE` duraklatmaz (bugünkü gibi), bu yüzden PiP'te oynatma sürer. Ses odağını başka uygulama alınca `setAudioAttributes(…, true)` zaten duraklatır.

### 6.4 Harici oynatıcı (`ui/ExternalPlayer.kt`)
- Manifest: `<queries><intent><action android:name="android.intent.action.VIEW"/><data android:scheme="http" android:mimeType="video/*"/></intent>` ve aynısı `https` için `</queries>`.
- `StreamExposure.of(url, source): Exposure { CREDENTIALS, POSSIBLE }` (saf, OkHttp `HttpUrl`):
  - CREDENTIALS sayılanlar: Xtream kaynağı; URL'de userinfo olması; yol parçasında veya sorgu değerinde kaynağın boş olmayan kullanıcı adı ya da şifresinin geçmesi.
  - Geri kalan her şey POSSIBLE. Yani her durumda uyarı çıkar, metin farklıdır.
- `ExternalLaunchSpec(uri, title, headers)` (saf) → ince Android katmanında `Intent(ACTION_VIEW).setDataAndType(uri, "video/*").setPackage(pkg)`.
  - `putExtra("title", title)` eklenir; `headers` boş değilse `putExtra("headers", arrayOf(k1, v1, …))` (bazı oynatıcıların kabul ettiği alan).
  - URL hiçbir yerde gösterilmez ve loglanmaz.
- Uygulamalar `queryIntentActivities` ile bulunur; kendi paketimiz çıkarılır; son kullanılan başa alınır. Liste panel açılınca bir kez çözülür.
- Başlatma sırası (tek oturum):
  1. `save()`.
  2. `player.stop()`: duraklatma değil; canlı TS'de duraklatılmış oynatıcı sağlayıcı bağlantısını açık tutabilir.
  3. `startActivity`. Başarılıysa `model.externalPlayerUsed(...)` ve `model.back()`.
  4. `ActivityNotFoundException` veya `SecurityException` olursa `player.prepare(); player.play()` ve not.

### 6.5 Altyazı stili
- `SYSTEM`: `setUserDefaultStyle()`, `setUserDefaultTextSize()`, `setApplyEmbeddedStyles(true)`.
- `APP`: `setStyle(CaptionStyleCompat(fg, bg, TRANSPARENT, edge, BLACK, null))` ve `setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * size.factor)`, `setApplyEmbeddedStyles(false)`.
  - fg: beyaz veya sarı.
  - bg: NONE → saydam + `EDGE_TYPE_DROP_SHADOW`; SEMI → %60 siyah; SOLID → siyah.
- Panelden boyut seçilince kaynak APP'e geçer; not bunu söyler.

### 6.6 Hata eşlemesi
- `ui/PlaybackRecovery.kt` · `playbackProblem`: kod çözücü hata kodlarının metnine Ayarlar ipucu eklenir (§7).
- PlayerScreen'de `onTracksChanged` içinde ses grubu olup hiçbiri desteklenmiyorsa ve `audioOutput == PCM` ise bir kez not gösterilir.

## 7. Kenar durumları ve Türkçe metinler

**Ayarlar satırları (etiket — açıklama):**
- **Kare hızı eşleştirme**
  - Kapalı — "Ekranın kare hızı hiç değişmez."
  - Yalnız kesintisiz — "Ekran görüntüyü kesmeden geçebiliyorsa yayının kare hızına geçer. Önerilen."
  - Her zaman — "Ekran yayının kare hızına geçer; geçişte ekran 1–3 saniye kararabilir. Filmler ekran hazır olunca başlar."
  - Durum notları: "Bu ekran tek kare hızı bildiriyor; eşleştirme yapılamaz." · "Android ekran ayarlarında içerik kare hızı eşleştirmesi kapalı."
- **Arabellek**
  - Normal — "Yayın en hızlı açılır."
  - Geniş — "Açılış biraz uzar; kısa bağlantı dalgalanmalarına daha dayanıklıdır."
  - Çok geniş — "Açılış birkaç saniye uzar; zayıf bağlantılar için."
  - Ortak not: "Değişiklik bir sonraki yayında geçerli olur."
- **Kod çözücü**
  - Otomatik — "Donanım kod çözücüsü kullanılır; açılamazsa cihazdaki başka bir kod çözücü denenir."
  - Yazılım öncelikli — "Görüntü bozuluyorsa dene. Yüksek çözünürlükte takılma yapabilir."
- **Ses çıkışı**
  - Otomatik — "Ses sistemin destekliyorsa Dolby ve DTS sesi olduğu gibi gönderilir."
  - Cihazda çöz — "Ses gelmiyor ya da kesiliyorsa dene. Cihazın çözemediği biçimler desteklenmiyor görünür."
- **Tünelli oynatma (deneysel)** — "Bazı cihazlarda görüntü ve ses uyumunu iyileştirir. Sorun olursa kapat. Bu modda ses gecikmesi ayarlanamaz."
- **Altyazı görünümü** — "Android ayarı" ya da "Büyük · Yarı saydam zemin · Beyaz". Alt sayfa:
  - Kaynak: "Android ayarı" ("Android'in altyazı tercihlerini kullanır.") veya "NEXTV ayarı".
  - Boyut: Küçük, Normal, Büyük, Çok büyük.
  - Zemin: "Yok (gölgeli)", "Yarı saydam", "Opak".
  - Renk: Beyaz, Sarı.
  - Not: "NEXTV ayarında yayının kendi altyazı biçimi kullanılmaz."
- **Kontrollerin kapanma süresi** — 4 sn / 8 sn / 15 sn / 30 sn — "Kumandaya dokunulmazsa oynatıcı kontrolleri bu süre sonunda gizlenir. TalkBack açıkken kendiliğinden gizlenmez."
- **Oynatıcı panelleri** — Opak / Yarı saydam / Saydam — "Oynatıcıdaki not ve panellerin arkasındaki görüntü ne kadar görünsün."
- **Ana ekrana dönünce küçük pencere** — "Oynatırken Ana ekran tuşuna basınca yayın küçük pencerede sürer."
- **Harici oynatıcı uyarıları** — "{n} kaynak için uyarı kapalı. Yeniden sormak için seç." Seçince: "Uyarılar yeniden sorulacak."

**Oynatıcı paneli:**
- Görüntü: "Sığdır", "Doldur" (açıklama "Kenarlar kırpılır."), "Uzat", "16:9", "4:3".
- Altyazı notu (sistem stilindeyken): "Şu an Android altyazı ayarı kullanılıyor; boyut seçersen NEXTV ayarına geçer."
- Ses gecikmesi değeri: "Ses gecikmesi yok" · "Ses 150 ms geç" · "Ses 100 ms erken". Not: "Ses görüntüden sonra geliyorsa eksi yönde, önce geliyorsa artı yönde ayarla." Tünelde: "Tünelli oynatmada ses gecikmesi ayarlanamaz."
- Eylemler: "Küçük pencerede oynat", "Harici oynatıcıda aç". Uygulama yoksa satır kapalıdır, durum metni: "Bu yayını açabilecek başka bir oynatıcı yüklü değil."

**Harici oynatıcı uyarısı:**
- Başlık: "Harici oynatıcıda açılsın mı?"
- CREDENTIALS: "Bu yayının adresi kaynak hesabının kullanıcı adını ve şifresini içeriyor. Seçeceğin uygulama bu adresi görebilir, geçmişinde saklayabilir ve paylaşabilir. Yalnız güvendiğin bir oynatıcı seç."
- POSSIBLE: "Bu yayının adresi hesabına ait bilgiler içerebilir. Seçeceğin uygulama bu adresi görebilir ve geçmişinde saklayabilir."
- Başlık bilgisi varsa ek satır: "Bu yayın özel bağlantı bilgileri kullanıyor; bazı oynatıcılarda açılmayabilir."
- VOD ise ek satır: "Harici oynatıcıda izlediğin kısım NEXTV'deki devam kaydına eklenmez."
- Düğmeler: "Vazgeç" (ilk odak), "Devam et". Seçici başlığı "Oynatıcı seç", etiket "Son kullanılan".
- Başlatma hatası: "Harici oynatıcı açılamadı. Yayın burada sürüyor."

**PiP notları:** "Küçük pencere bu uygulama için Android ayarlarında kapalı." · "Küçük pencere açılamadı."

**AFR:** köşe notu "Ekran {50} Hz'e geçiyor…"; VOD durum satırı "Ekran ayarlanıyor…"; birincil düğme "Hemen oynat". 5 sn içinde ekran değişmezse sessizce oynatılır.

**Hatalar:**
- Kod çözücü: "Bu cihaz yayının görüntü veya ses biçimini çözemiyor. Ayarlar > Oynatıcı'da kod çözücüyü ya da ses çıkışını değiştirmeyi deneyebilirsin."
- Desteklenmeyen ses: "Bu yayının sesi bu ayarla çalınamıyor. Ses çıkışını Otomatik yapmayı dene."

**Kenar durumları (kısa):**
- **AFR:** Kare hızı bulunamazsa (3 sn / 90 kare), değişken kare hızında, tek modlu ekranda ve tünelde fps bilinmiyorsa geçiş yapılmaz. Oynatıcıdan çıkışta varsayılan moda dönülür; ekran bir kez daha kararabilir. HDMI değişirse `onDisplayChanged` ile yeniden değerlendirilir.
- **Arabellek:** Çok geniş bayt sınırını aşmaz. Canlıda ilk açılış uzar, kanal geçişi yavaşlar; metin bunu söylüyor.
- **Yazılım öncelikli + 4K HEVC:** Kare kaybı olabilir. Otomatik algılanmaz; metin uyarıyor.
- **Cihazda çöz + DTS çözücüsü yok:** `TracksDialog` "Bu cihaz desteklemiyor" der; ses grubu hiç çalınamıyorsa yukarıdaki not çıkar.
- **Tünel desteklenmiyor:** Sessizce normal oynatma.
- **Kurtarma ve hata:** Kontrollerin otomatik gizlenmemesi korunur (bugünkü koşul).
- **PiP:**
  - Bölüm sonu kartı çizilmez; otomatik sonraki bölüm ayara göre çalışır.
  - Hata PiP'te görünmez, tam ekranda kontrollerde görünür.
  - Kanal tuşları PiP'te gelmez.
  - Açık panel girişten önce kapanır.
- **Kaynak silinince:** Onay kaydı temizlenir.
- **Büyük yazı (1.5):** Satırlar sarar (`heightIn(min = 62.dp)`, `BringIntoView` mevcut). Seçenek paneli dikey kayar. Kontrol satırı yatay kayar.
- **Hareketi azalt:** Yeni bir animasyon yok; dönen yay zaten durur.

## 8. Testler

**JVM (`app/src/test`), önce yazılır:**
- `data/PlaybackPreferencesTest`: varsayılanlar bugünkü davranışa eşit; bilinmeyen enum adı varsayılana düşer; gecikme 50 ms adıma yuvarlanıp ±500'e kıstırılır; encode→decode gidiş-dönüşü; `BufferPreset` değerleri `DefaultLoadControl` kısıtlarını sağlar (min ≥ playback, min ≥ rebuffer, max ≥ min).
- `ui/FrameRateEstimatorTest`: 25, 23.976 ve 59.94 fps PTS dizileri; ±2 ms titreşim; arama ya da geri giden zaman sıfırlar; 48 kare altı null; alan kodlu 25p (40 ms) → 25.
- `ui/DisplayModePickerTest`: {1080p 24/25/30/50/60} için 23.976→24, 25→50, 29.97→60 (59.94 yoksa null); farklı çözünürlük adayı reddedilir; mevcut uyuyorsa null; tek mod → null.
- `ui/StreamExposureTest`: Xtream → CREDENTIALS; userinfo → CREDENTIALS; yolda kullanıcı adı → CREDENTIALS; düz M3U → POSSIBLE; `toString` URL içermez. Sentetik adresler `http://kaynak.test/…` (gerçek URL yok).
- `ui/PlayerAudioSinkTest`: `java.lang.reflect.Proxy` ile sahte `AudioSink`. Gecikme ekleniyor; `CURRENT_POSITION_NOT_SET` aynen geçiyor; `pcmOnly` AC-3 `Format`'ı reddediyor, PCM'i kabul ediyor; tünelden sonra gecikme 0.
- `ui/ControlsAutoHideTest`: süreler, TalkBack, panel açık, hata ve kurtarma.
- `ui/PipAspectTest`: 4:3, 21:9 ve 32:9 (kıstırma), bilinmeyen boyut → 16:9.
- `ui/AspectModeTest`: mod → resizeMode ve oran eşlemesi.
- `ui/PlaybackRecoveryTest` güncellenir: kod çözücü mesajı ipucunu içerir, URL içermez.

**Enstrümantasyon (`app/src/androidTest`):**
- `PlayerSettingsFlowTest`:
  - Oynatıcı satırları görünür; "Arabellek" → seçim sayfasında odak geçerli değerde; "Geniş" seçilince odak satıra döner, açıklama güncellenir.
  - Seçim sayfasından Geri değişiklik yapmaz.
  - Altyazı alt sayfasında Geri zinciri doğru.
  - fontScale 1.5'te odaklı satır tam görünür.
  - Semantik: RadioButton, selected ve stateDescription.
- `PlayerOptionsLayoutTest` (`TracksLayoutTest` kalıbında, gerçek ExoPlayer ve sentetik yayın):
  - İlk odak seçili Görüntü satırında; "Doldur" sonrası `PlayerView.resizeMode == RESIZE_MODE_ZOOM`.
  - Geri ile odak "Seçenekler"e döner; panel açıkken kontroller gizlenmez.
  - Alfa < 1 iken pencere dim değeri 0.
- `ExternalPlayerFlowTest`:
  - androidTest manifestine `ACTION_VIEW video/*` alan bir taslak Activity eklenir.
  - Uyarıda ilk odak Vazgeç; Devam et taslağı açar.
  - Açılıştan önce `player.playbackState == STATE_IDLE` (durduruldu) ve rota oynatıcıdan çıkmış.
  - Onay ikinci seferde sorulmaz; Ayarlar'daki sıfırlama yeniden sordurur.
  - Taslak yokken satır kapalı ve metin doğru.
- `PictureInPictureFlowTest` (FEATURE varsa, değilse `assumeTrue` ile atlanır): giriş sonrası `isInPictureInPictureMode` true, kontroller çizilmiyor, tam ekrana dönüşte odak geri geliyor. Kapatma ve otomatik giriş elle QA.
- AFR: emülatör tek mod bildirir; "Her zaman" seçiliyken çökme olmadığı ve `preferredDisplayModeId == 0` kaldığı test edilir. Gerçek geçiş 65U95A ve mümkünse bir HDMI kutusunda elle ölçülür: 24/25/50/59.94 içerik, kararma süresi, çıkışta geri dönüş.
- Elle QA: passthrough ve tünel (ARC'li ses sistemi), ses gecikmesi (dudak senkronu).

## 9. Dosyalar
Liste `files_touched` alanında. Yeni bağımlılık yok: media3-ui-compose, media3-session ve FFmpeg uzantısı eklenmiyor.

## 10. Aşamalar ve efor (mühendis-günü)
1. **Tercih altyapısı + Ayarlar "Oynatıcı" + PlayerFactory** (2,5): arabellek, kod çözücü (yedek + yazılım öncelikli), cihazda çöz, tünel, Kapalı/Yalnız kesintisiz, OSD süresi, panel saydamlığı, hata ipucu; JVM testleri.
2. **Oynatıcı seçenekleri paneli** (2): en-boy, altyazı stili (Ayarlar alt sayfası + panelde boyut), yayın bilgisi; enstrümantasyon.
3. **AFR "Her zaman"** (2,5): estimator, picker, controller, VOD bekleme, geri dönüş; cihaz ölçümü.
4. **Harici oynatıcı** (2): `<queries>`, risk sınıflaması, uyarı, seçici, tek oturumlu başlatma.
5. **PiP** (2): manifest, PipHost, panel düğmesi, kapanma/genişleme, RemoteAction, otomatik giriş ayarı.
6. **Ses gecikmesi (deneysel)** (1,5): saat kaydırma, panel satırı, tünelde kapatma.

Belgeler (DECISIONS'a yeni D kaydı, SCREENS'te oynatıcı paneli, NATIVE_UI, TIVIMATE_GAP, STATUS/HANDOFF/BACKLOG) ve cihaz QA ile toplam yaklaşık 13 gün.

## 11. Sahip kararları (varsayılanla iş ilerler)
Bkz. `owner_decisions`.

## 12. Notlar
- **Yerelleştirme:** Yeni metinler bir `PlayerCopy` nesnesinde toplanır; ileride kaynak dosyalarına geçerken tek yerden taşınır (PRD §12 TR+EN adayı). `trackLabel` dil adlarını sabit `Locale.forLanguageTag("tr")` ile, Ayarlar'daki tarih de `SimpleDateFormat(…, "tr")` ile yazıyor. İngilizce arayüzde bunlar arayüz diline bağlanmalı.
- **Hukuk ve gizlilik:** Hiçbir metin içerik vaadi ya da sağlayıcı önerisi içermez. Harici oynatıcı uyarısı, Play raporu 3.6'daki "kimlik bilgisi URL'de" riskini kullanıcıya açıkça söyler. Analitik ve log eklenmez.