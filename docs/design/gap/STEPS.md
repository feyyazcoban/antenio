# Gap planı: kalan adımlar ve çalışma yöntemi

18 Ekim 2026 · Claude oturumu kullanıcının kullanım sınırına takıldığında yazıldı; Codex (ya da başka bir araç) buradan devam eder. Plan ve küme tasarımları: [PLAN.md](PLAN.md) ve bu klasördeki `*.md`. Öneriler kullanıcı kararı değildir; onaylananlar DECISIONS'ta (D069–D072).

## Durum (ana checkout güncel; S14b teslim kaydı STATUS/HANDOFF'ta)

- Bitti ve yeşil: Faz A (S01–S07), X01, Faz B (S08–S12), T01 (ekran testleri onarımı), Faz C (S13), S14 (`b6157c9`: kontrolleri ve ekran testleri geçti; incelemesi yapıldı, düzeltmeleri yarım).
- S14 inceleme düzeltmeleri: `c54a9f7` alındı ve tamamlandı. PCM destek uyarısı mevcut `flash` yolundan medya başına bir kez; ses çıkışı hataları genel hata; panellerde açık opaklık; uzun değerlerde alta geçen değer etiketi; gezinme testleri `SectionId` sırasından. 340 JVM testi (1 atlanan), lint 0 hata, release/androidTest derlemesi; 5558'de normal yazıda 16, 1,5 ve 2,0 ölçekte ayrı ayrı 12 ekran testi geçti. AC-3 destek kontrolü saf JVM testinde; gerçek cihazda desteklenmeyen AC-3 akışı/TalkBack doğrulanmadı. Altı bölümlü Ayarlar sınırı D076'da açıkça kayıtlı.
- Teslim tarihçesi: 0.14.45 (kod 59) S14 düzeltmeleri; 0.14.46 (kod 60) D077 oturumluk film/dizi hızı (0,5×–2×). Kullanıcının son paketi TV'ye veri silmeden kurma talimatı sürer.
- S14b (D072) native uygulaması 0.14.47 / kod 61'de: alt blok, yeni kontroller, bekleyen sarma ve gizli sarma önizlemesi, büyük yazı düzeni; D077 hızı korunur. Gerçekten koşulan testler ve cihaz teslimi [QA](../../project/native-player-controls-47-qa.json), STATUS/HANDOFF'ta. Bağımsız incelemedeki P3 çubuk odak ipucu düzeltildi. Fiziksel/Figma/görsel kabul açık; BKL-031 done değildir.
- 0.14.59 / D089: kullanıcı splash donması bildirdi; girişten sonra içerik oluşturma ve yeni basış/tek başına bırakma ayrımı. Yeni normal animasyon/gate testleri; font matrisi yok. Kanıt ve teslim STATUS/HANDOFF, [QA](../../project/native-splash-59-qa.json).
- Sırada: S15a–S17 → Faz E (S18–S29) → Faz F–J (PLAN §2). S15a kanal kapsamı/son ve önceki kanal verisiyle başla. D093 kullanıcı geri bildirim paketi 0.14.63/kod77 teslim edildi; kontrol/teslim STATUS/HANDOFF esas alınır. D094 Kanallar drawer dar kullanıcı isteği 0.14.64/kod78 teslim edildi (genel S15 değil); açık Home scroll/splash geri bildirimleri, sonra genel plan. D095 Home ilk kaydırma hedefi .65/kod79 teslim edildi; QA native-home-reveal-65-qa.json. D096 ilk açılış yükü/gerçek Activity ölçümü .66/kod80 teslim edildi; ilk duraklama belirgin düzelmedi, splash kabulü açık. Sonraki paket en az kod81/0.14.67 olmalı. D077 hız seçimi S16'da korunmalı; canlı yayın normal hızda kalır.

## Her adımda yöntem

**Güncel kullanıcı istisnası · 18 Ekim 2026:** aşağıdaki görevlerde geçen 1,5 / 2,0 yazı boyutu testleri, kullanıcı "yayına hazırla" dediğinde mağaza öncesine bırakılır. Bu aşamada font matrisi koşma; normal yazıda değişiklikle ilgili anlamlı kontrolleri yap. [WORKFLOW](../../project/WORKFLOW.md) esas alınır.

1. **Uygula:** adımın PLAN.md satırını ve ilgili küme tasarımını okuyun, değişikliği yapın, kontrolleri geçirin, yalnız o adımın dosyalarını commit edin (`<Adım>: <başlık>`).
2. **Bağımsız inceleme:** değişikliği şu bakışlarla okuyun: (a) doğruluk ve geriye dönük bozulma; (b) PLAN §1 API'sine uygunluk ve mimari (mantık saf sınıflarda, TvViewModel ince); (c) TV kullanımı ve erişilebilirlik (D-pad, Geri bir düzey, D064 odak dili, TalkBack, 1,5/2,0 yazı, Hareketi azalt); (d) PIN/erişim ve oynatma adımlarında güvenlik ve gizlilik (kilitli/gizli/yetişkin içeriğe kapıyı atlayan yol, kimlik bilgisi veya adres sızıntısı, StreamBudget'sız ikinci akış). Yalnız kanıtlı bulgular.
3. **Düzelt:** her bulguyu kodda doğrulayın; yanlışsa gerekçesini yazın, doğruysa düzeltin, sonraki adıma aitse erteleyin. Kontrolleri yeniden geçirip `<Adım>: review fixes` commit'i atın.

**Kontroller (her commit'ten önce):**

```bash
cd apps/android-tv && export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" && ./gradlew --console=plain -q :app:testDebugUnitTest :app:lintDebug :app:assembleRelease :app:assembleDebugAndroidTest
```

**Ekran testleri:** yalnız `bash tools/run-device-tests.sh SinifA,SinifB` (ya da `Sinif#metot`). Betik test emülatörünü (`emulator-5558`, AVD `nextv_test_api36`) doğrular ve yalnız orada kurar; başka cihaza dokunmaz. `--font-scale 1.5` / `2.0` ve önceden derlenmiş APK'lar için `--no-build` kullanılabilir; font ölçeği koşu sonunda geri gelir. Gradle `installDebug` / `connectedAndroidTest` yasak: TV dahil bağlı bütün cihazlara kurar. Test emülatörünün dili İngilizce; Türkçe metin bekleyen testler S13'teki yerel ayar kuralını kullanır. Cihaz kuralları ve emülatörü açma komutu: [WORKFLOW](../../project/WORKFLOW.md) "Cihaz ayrımı".

**Ürün kuralları (AGENTS.md):** gerçek IPTV adresi, kullanıcı adı, parola ve token commit edilmez; `.private/` açılmaz; korsanlık çağrıştıran ifade kullanılmaz; tek oynatma oturumu (StreamBudget); metinler `values` (İngilizce) ve `values-tr` (Türkçe) dosyalarına yazılır; STATUS, HANDOFF ve DECISIONS her faz sonunda güncellenir.

## S14 incelemesinin bulguları (tarihçe; sonuç yukarıda)

- [medium] "Cihazda çöz" (PCM) can mute a stream silently: the unsupported-audio note from the design is missing (apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt): In PlayerScreen's onTracksChanged, when `screenState.playback.audioOutput == AudioOutput.PCM`, check for at least one TRACK_TYPE_AUDIO group with no supported track (`Tracks.Group.isSupported`). If so, flash the note once per media through the existing `note`/`flash` path. Add the string to values and values-tr. Keep the check in a small pure helper, such as `unplayableAudio(tracks, output)`, with a JVM test. Add a device case to PlayerSettingsFlowTest, or a FakePlayer test with an AC-3-only Tracks.
- [low] Two-level Ayarlar ships 6 of the 10 sections the step and plan decision 4a-8 list, so later steps must reshuffle hard-coded traversal tests (apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/settings/SettingsPages.kt): Either add the Rehber and Yedekleme ve ana ekran sections now (Rehber could hold a note pointing to the guide's own settings), or raise the reduced list to the owner as an open 4a-8 question in the documentation step. Also make the traversal tests derive their counts from `SectionId.entries`, not literal repeat counts, so later sections do not break them.
- [low] Large-font check for the new Oynatıcı and ChoicePage layouts is never actually run at 1.5 or 2.0 (apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/PlayerSettingsFlowTest.kt): Run SettingsFrameworkTest, PlayerSettingsFlowTest and the SearchNavigationTest settings cases on the test emulator at font_scale 1.5 and 2.0 (`settings put system font_scale`, via the script), and report the results. Or add a font-scale parameter to tools/run-device-tests.sh so these runs are repeatable.
- [low] TracksDialog defaults to OPAQUE while the shipped default is TRANSLUCENT; the see-through, undimmed panel has no assertion (apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt): Remove the default, or make it `PlaybackPreferences().panelOpacity`, so callers and tests use the production value. Add a TracksLayoutTest case with SHEER/TRANSLUCENT that asserts the dialog window's `attributes.dimAmount == 0f` and that the panel still lays out.
- [medium] At font scale 2.0, the Player rows' value chips leave about 100 dp for the text, so the default 'Match frame rate' row is taller than the screen (apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/settings/SettingsRows.kt): Stop the value chip from taking the row's text width. Any of these works:
- [low] AudioTrack init/write failures now show the decoder message, which goes beyond the planned decoder hint (apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlaybackRecovery.kt): Keep the hint for the four DECODER/DECODING codes as the plan says, and send the AUDIO_TRACK_* codes back to playback_failed. If a passthrough hint is wanted, add a separate message that is shown only when preferences.audioOutput == AudioOutput.AUTO (the design's 'Ses çıkışını Otomatik/Cihazda çöz' note), and add a PlaybackRecoveryTest case for it.

## Faz D, kısım 1: S14b

### S14b · Oynatıcı kontrollerinin görsel yenilemesi

Implement decision D072 (approved by the owner on 18 Oct 2026) as specified in docs/design/gap/player-controls.md (read it fully, and open the mockups design/reviews/player-controls-2026-10-18/proposal.html and current.html as text). Owner choices use the recommended defaults listed there unless docs/decisions says otherwise: "Kapat" removed (first Back hides controls, second leaves), ‹ Kanal / Kanal › out of the transport cluster with temporary "Kanal −/+" chips until S15b adds Kanallar and Önceki kanal, remaining time on the right of the scrub bar, 1 s auto-commit on the bar (OK commits at once, Back cancels), white LIVE dot, clock in the title row and end time in the meta row, Önceki kanal disabled-but-visible when there is none, focus labels (name on icon-only controls, state on labelled chips). Build: one bottom scrim, a single PlayerInfoBlock (title row, meta row, bar row) with the 64/52 dp transport cluster (−10 · play/pause · +10), right-aligned chips (Ses / Altyazı, Seçenekler placeholder that S16 fills, ★), a focusable scrub bar with pending target, delta and time bubble (Left/Right 10 s, held 30 s then 1 min, FF/RW move the target while controls are open, Down applies and returns to the control it came from), PlaybackClock so only the clock and bar recompose (not the whole overlay), focus labels drawn by the control itself, key hints that fit, font scale 1.5/2.0 behaviour (labels drop in the documented order, no horizontal scroll), the info-band and hidden-seek-preview modes of the same block if the spec places them here, PlayerSymbols.kt for icons (Apache-2.0 path data; record the licence in the code comment), D064 focus language, no focus scale. Keep PlayerLayer/PlayerKeyRouter as the single key authority (add cases; update PlayerKeyRouterTest). Keep the player-panel / player-title / player-controls test tags or update tests that rely on them. Strings in values (English) and values-tr (Turkish). Tests: PlayerKeyRouterTest, PlaybackClock/scrub-target JVM tests; PlaybackRecoveryFlowTest, TracksLayoutTest, EpisodeAutoplayFlowTest, VisualPreviewTest player captures and a new PlayerControlsLayoutTest (font scale 1.0/1.5/2.0, focus order, Back order) on the test emulator.

## Faz D, kısım 2: S15a–S17

### S15a · Kanal bağlamı, son ve önceki kanal verisi

docs/design/gap/overlay.md stage S15a (read the whole design; owner decisions there use their defaults — short OK on live opens the list, OK on another row switches and keeps the list, 0 without pending digits is previous channel, start-on-last-channel off, info band 4 s, held zap previews and opens on release, Back to Canlı TV focuses the last channel, Geçmişi temizle forgets the last channel, panel idles out after 15 s): data/ChannelScope.kt (ChannelScope, LiveIndex built once per publish with O(1) numbers), data/LiveRecall.kt (LiveRecall, LastLive codec, AutoStart.decide), liveScope set by every play path; changeChannel, playChannelNumber, channelNumber, zapTarget on LiveIndex (fixes: CH± in Favoriler did nothing; zap order differed from numbers; guide key after zapping opened the old channel); AccessTarget.ChannelPick through the gate (locked channels skipped by zap, PIN on pick); lastLive key in DeviceKeys with cleanup in saveSources/forgetSource/clearHistory; PlayerKeyRouter cases of the design table that need no UI yet. Tests: ChannelScopeTest, LiveRecallTest, AutoStartTest, PlayerKeyRouterTest additions, AccessGateTest ChannelPick cases (JVM); GuideFlowTest guideKeyAfterZapOpensPlayingChannel on the test emulator.

### S15b · Oynatıcıda kanal listesi paneli

docs/design/gap/overlay.md stage S15b and §3–§6, §11–§15: ui/ChannelPanel.kt (channel column, category column on Left, mini guide on Right, programme detail dialog), short/held OK language via the router (KeyUp handling so the release never clicks a row), "Kanallar" in the live control row, focus memory and initial focus on the playing row, Back one layer, 15 s idle close (never with TalkBack), EPG only for visible rows (debounced, merged per panel session), locked rows with badge and PIN, hidden/adult never listed, TalkBack paneTitle/row descriptions, font scale 1.5 and 2.0 (single column at 2.0). VOD unchanged. Tests: ChannelListFlowTest part 1 on the test emulator (synthetic M3U + local XMLTV like GuideFlowTest), PlayerKeyRouterTest cases.

### S15c · Bilgi bandı, önceki kanal ve açılışta son kanal

docs/design/gap/overlay.md stage S15c and §7–§9: ui/ChannelBand.kt with the pure ChannelBandModel (number, name, now/next, progress, 4 s, Back hides only the band, reduced motion), ChangeOrigin replacing the quiet flag, previous channel via 0 (no pending digits), LAST_CHANNEL key and the "Önceki kanal" actions, held Up/Down/CH± preview that opens the stream only on release (one connection), "Açılışta son kanalı aç" toggle on the Oynatıcı settings page (default off) with the launch cover interplay (buffer behind the cover, stop the startup chime when the route is PLAYER), library focus sync on Back (decision 7). Tests: ChannelBandModelTest, PanelAutoClose/paging tests (JVM); ChannelListFlowTest part 2, PlaybackRecoveryFlowTest band expectation, ParentalFlowTest rows on the test emulator.

### S16 · Oynatıcı seçenekleri paneli

Plan row S16 and playback.md (options panel): ui/PlayerOptions.kt — a 440 dp side panel from the player controls with aspect ratio/zoom (fit, fill, zoom, 16:9, 4:3; one default for all streams per plan 4b, stored in PlaybackPreferences), subtitle style quick access and a stream info section (codec, resolution, frame rate, bitrate, audio format — only what Media3 reports; no URLs), integrated with PlayerLayer/PlayerKeyRouter (Back one layer) and the S14 settings sub-page. Tests: AspectModeTest (JVM); PlayerOptionsLayoutTest at font scale 1.0/1.5 on the test emulator.

### S17 · Canlı geri sarma T0 ve akış bütçesi

Plan row S17, record.md stage 0 and PLAN §3 rule 9: StreamBudget per source (limit unknown = 1; Xtream user_info max_connections when the provider sends it; Ayarlar → Kaynaklar → Bağlantı sınırları override), SourceRegistry "limits" field (S07 codec), live rewind inside the provider's own HLS live window only (Left/Right seek within the window, −10/+10, "Canlıya dön", live edge indicator), the button and keys absent on TS channels or HLS without a window, PlayerKeyRouter priority (window → seek; otherwise S15's behaviour), no second connection ever. Tests: StreamBudgetTest, LiveWindowTest (JVM); a device test with a local HLS stream that has a DVR window (serve it from MockWebServer like the existing playback fixtures) on the test emulator.

## Faz E: S18–S29

### S18 · Bağlantı ayarları

Plan row S18 and docs/design/gap/organize.md stage 1 (connection part): SourceRegistry "connection" field (custom User-Agent, Referer, extra headers; stored encrypted with the source), RequestHeaders used by list, EPG (only for the source's own host or explicitly scoped hosts) and playback (PlaybackRequest), User-Agent unified to D066's "NEXTV/<version> AndroidTV" for list/EPG too (plan 4b), a "Bağlantı ayarları" dialog from setup/source edit ("Varsayılan (NEXTV)" or free text; no presets imitating other products). Tests: RequestHeadersTest and SourceTest with MockWebServer (JVM); SourceLayoutFlowTest/SetupLayoutTest additions on the test emulator.

### S19 · EPG I: birden çok rehber adresi ve şema v2

Plan row S19 and docs/design/gap/epg.md (read fully): SourceRegistry "guides" field (up to 4 XMLTV addresses per source, order = priority, plus the M3U header ones), EpgPlan extension, EpgStore schema v2 (feeds, titles, aliases.folded, title_id + index) with migration (old guides treated stale; old query path kept until the new download finishes so programmes stay visible), GuideMatching. Tests: EpgSourceTest, XmltvParserTest (JVM); EpgStorageTest v1→v2 on the test emulator.

### S20 · EPG II: adres başına sonuç ve öncelik

Plan row S20 and epg.md: per-address results (a failing address keeps its previous data, the others publish), priority matching per plan 4b (same channel id in several guides → the first in the viewer's order that has programmes; ambiguous name matches never assigned), Rehber ayarları page listing addresses with status, rewrite the test multipleFeedsDoNotSilentlyMergeConflictingChannelIds for the new rule, update docs/design (EPG notes). Tests: EpgSourceTest (JVM); GuideFlowTest on the test emulator.

### S21 · EPG saat kaydırma

Plan row S21 and epg.md: data/GuideTuning.kt per-source time offset (guide:<id> key registered in DeviceKeys), applied at query time in EpgStore (no re-download), a StepperRow (−30/+30 min, Sıfırla) with live preview in Rehber ayarları. Tests: GuideTuningTest (JVM); GuideFlowTest offset case on the test emulator.

### S22 · Catch-up veri katmanı

Plan row S22 and docs/design/gap/catchup.md (read fully; verify formats against the design's cited sources): CatchupSpec parsed from Xtream (tv_archive, tv_archive_duration, server_info.timezone) and M3U (catchup, catchup-source templates, catchup-days, timeshift), catalog snapshot codec v4→v5 storing it, data/Catchup.kt URL builders (Xtream timeshift path and timeshift.php forms; M3U default/append/shift/flussonic/xc templates with time placeholders), ArchiveTimeline. No visible change yet. Credentials only inside URLs built in memory, never stored or logged. Tests: CatchupParserTest, CatchupUrlTest, CatchupStateTest, ArchiveTimelineTest, CatalogSnapshotTest (JVM).

### S23 · Catch-up rehber penceresi

Plan row S23 and catchup.md: GuideWindow.around, past days (up to the channel's archive window, max 7) only for channels with archive, using the S21-shifted time, archive markers on channels and programme cells, programme detail dialog variants (past with archive: "Baştan izle"/"Geçmiş yayın" per the design's neutral wording; D039 kept: OK opens details, Down+OK starts the archive). Tests: GuideTimelineTest, EpgSourceTest (JVM); GuideFlowTest on the test emulator.

### S24 · Catch-up oynatıcı kipi

Plan row S24 and catchup.md: PlaybackMode.ARCHIVE ("GEÇMİŞ YAYIN" label), seeking by reopening the archive URL at the new offset (one connection), ArchiveEndDialog as a PlayerLayer at programme end, request(Archive) through the gate, Feature.CATCHUP check (AllFree for now), PlaybackRecovery for archive. Tests: PlaybackRecoveryTest (JVM); CatchupFlowTest with a synthetic archive server on the test emulator.

### S25 · Baştan izle ve yedek biçimler

Plan row S25 and catchup.md: "Baştan izle" on a live programme with archive, switching to live at 90 s from the edge, timeshift.php and Flussonic fallbacks, held OK in the guide opens details, priority rules with S17 (provider window seek first; Baştan izle only with a CatchupSpec). Tests: CatchupUrlTest (JVM); CatchupFlowTest additions on the test emulator.

### S26 · Elle rehber eşleme I

Plan row S26 and epg.md: MANUAL match kinds, candidate search over the stored guides (folded names), a "Rehber eşlemesi" dialog, held OK on a guide channel opens "Kanal seçenekleri" (Rehberi eşle, Eşlemeyi kaldır). Tests: GuideMatchingTest (JVM); GuideFlowTest on the test emulator.

### S27 · Elle rehber eşleme II ve eşleşme özeti

Plan row S27 and epg.md: the same channel options from Canlı TV and Home quick menus, a "Kanal eşleşmeleri" page with a summary (eşleşen / eşleşmeyen / elle), clear empty-state texts, TalkBack custom action instead of held OK. Tests: GuideFlowTest at font scale 1.5 with TalkBack semantics on the test emulator.

### S28 · Ara'da program araması

Plan row S28 and epg.md: EpgStore titles search (LIKE over the titles table with folded text, bounded results, off the main thread), GuideController/TvViewModel API, a "Programlar" row in Ara with "Rehberde göster" and focus return, locked channels' programmes hidden. Tests: ProgrammeSearchTest (JVM); SearchNavigationTest additions on the test emulator.

### S29 · Program hatırlatıcısı

Plan row S29 and catchup.md (reminders): foreground scheduler T6 (a minute ticker only between appStarted and appStopped), set/remove a reminder from a future programme, conflict window, persistence via DeviceKeys, ReminderAlert as an AppOverlay ("İzle"/"Kapat"; opening goes through the gate), a player note when watching, Ayarlar → Hatırlatıcılar page, honest copy "Yalnız NEXTV açıkken uyarır" (no background notifications in v1, plan 4b), Feature.REMINDERS (AllFree). Tests: RemindersTest, ReminderSchedulerTest (JVM); ReminderFlowTest on the test emulator.

## Sonrası

Faz F (S30–S34), G (S35–S37; satış kapalı kurulur, model ve fiyat kararı kullanıcıda, bkz. [SALES_STRATEGY](../../product/SALES_STRATEGY.md)), H (S38–S44), I (S45–S48), J (S49–S65): PLAN.md §2 tabloları. Faz J'den önce S49 cihaz kapısı (Media3'ün büyüyen yerel listeyi oynatması).
