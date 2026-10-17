# Geçmiş yayın (catch-up / sağlayıcı arşivi) ve program hatırlatıcısı — TIVIMATE_GAP satırları "Catch-up" ve "Program hatırlatıcısı" (PRD EXT02 + yeni P1 satırı)

18 Ekim 2026 · TiviMate gap tasarımı (`catchup`), salt okunur tasarım ajanı; öneriler kullanıcı kararı değildir.

**Özet:** Bu tasarım iki şeyi ekler. Kaynak kanal için arşiv bildirdiğinde (Xtream tv_archive/tv_archive_duration; M3U catchup, catchup-source, catchup-days, timeshift ya da tvg-rec) rehberde "Geçmiş yayını izle", canlı oynatıcıda "Baştan izle" çıkar. Arşiv, mevcut tek ExoPlayer oturumunda "GEÇMİŞ YAYIN" kipiyle oynar: programın içinde sarılır, gerekirse yayın sırayla yeniden açılır, program bitince bir kart çıkar, devam eden programda yayın yakalanınca canlıya geçilir. Hatırlatıcı rehberdeki başlamamış programdan kurulur, cihazda saklanır, çakışmada sorar ve yalnız NEXTV açıkken uyarır; uygulama kapalıyken güvenilir uyarı Android TV'de mümkün değil, bu yüzden v1'de sistem bildirimi yok. Değişiklik kodda bugünkü yerlere bağlanıyor: MediaEntry/CatalogParser/M3uParser alanları, katalog önbelleği v4'ten v5'e, GuideWindow.around'a geçmiş gün sayısı, GuideScreen ayrıntı penceresi, PlayerScreen ve PlaybackRecovery. Yeni bağımlılık ve manifest izni yok; toplam iş yaklaşık 10 gün, 4 aşama ve bir koşullu aşama. Not: sizin sorunuz fiyat, satış ve dil üzerineydi, bu belge onu cevaplamıyor. İlgili iki nokta sahip kararlarına eklendi (bu özellikler ücretli pakette mi, metinler hangi dil altyapısına yazılacak). Xtream başlangıç saatinin sunucu saat dilimiyle kurulması da sağlayıcı ile izleyicinin farklı ülkelerde olduğu durumu doğrudan kapsar.

**Tahmini iş:** 10 gün

**Aşamalar:**
1. Aşama 1 · Veri temeli, görünür değişiklik yok (≈2 gün): CatchupSpec ayrıştırma (Xtream tv_archive/tv_archive_duration; M3U catchup*, timeshift, tvg-rec, başlık varsayılanları), katalog önbelleği v5, server_info.timezone, Catchup.state/request ve ArchiveTimeline ile JVM testleri.
2. Aşama 2 · Rehberden geçmiş yayın (≈3,5 gün): arşivli kanallar için 7 güne kadar geçmiş rehber penceresi ve kırpma, kanal/hücre işaretleri, ayrıntı penceresi varyantları ve yeni notlar, oynatıcının GEÇMİŞ YAYIN kipi (baştan oynatma, sarma, tek yeniden açmayla sarma, bitiş kartı), arşiv hata metinleri, CatchupFlowTest.
3. Aşama 3 · Canlıda Baştan izle ve canlıya yetişme (≈1,5 gün): canlı oynatıcı düğmesi, rehberde basılı OK ayrıntısı, sıralı yeniden açma zinciri ve 90 sn'de canlıya geçiş, Xtream timeshift.php ve Flussonic rel yedekleri, kurtarma kancaları.
4. Aşama 4 · Uygulama içi hatırlatıcı (≈2,5 gün): kurma/kaldırma, çakışma penceresi, DeviceStore'da kalıcılık ve kaynak temizliği, zamanlayıcı, uyarı kartı, oynatıcı notu, Ayarlar → Hatırlatıcılar, ReminderFlowTest; ardından belgeler ve QA (≈0,5 gün).
5. Aşama 5 · Koşullu, ayrıca tahmin edilir (+1–2 gün): PrivateGuideAuditTest XMLTV'de geçmiş program olmadığını gösterirse Xtream get_simple_data_table ile kanal başına geçmiş liste; sahip onaylarsa uygulama kapalıyken sistem bildirimi.

**Kullanıcı kararları (varsayılanla):**
- Uygulama kapalıyken hatırlatıcı için sistem bildirimi eklensin mi? (Aşama 5: AlarmManager.setWindow ile yaklaşık zaman, POST_NOTIFICATIONS ve RECEIVE_BOOT_COMPLETED izinleri. Google TV'de bildirimin görünmesi garanti değil, uygulama kendini açamaz.) → varsayılan: v1'de hayır. Yalnız NEXTV açıkken uyarı verilir ve arayüz bunu açıkça yazar. Gerçek TV'de bildirim paneli görüldükten sonra yeniden değerlendirilir.
- Geçmiş yayın ve hatırlatıcı ücretsiz mi olacak, yoksa gelecekteki ücretli pakete mi girecek? (Gelir modeli PRD §12'de açık; TiviMate catch-up'ı Premium'da satıyor.) → varsayılan: Şimdilik ücretsiz geliştirilir. Erişim tek giriş noktasında (TvViewModel.playArchive / addReminder) toplanır; fiyat ve ücretli paket kararı verilince tek satırla bağlanır.
- Rehberde arşivi olan bitmiş bir programa OK basınca ne olsun: ayrıntı penceresi mi açılsın, doğrudan arşiv mi oynasın? (D039'a göre bugün ayrıntı açılıyor.) → varsayılan: D039 korunur: OK ayrıntıyı açar, Aşağı + OK 'Geçmiş yayını izle'yi başlatır. Yayındaki programda kısa OK canlıyı açmaya devam eder, basılı OK ayrıntıyı açar.
- Bu kümenin yeni metinleri hangi dil altyapısıyla yazılsın? (Uygulamada strings.xml yok, bütün metinler Kotlin içinde Türkçe; PRD §12 Türkçe + İngilizce'yi aday diller sayıyor.) → varsayılan: Mevcut kodla tutarlı biçimde Türkçe sabit kalsın; tarih ve saat mevcut guideTime/guideDate yardımcılarından geçsin. Bütün uygulamanın strings.xml'e taşınması ve İngilizce, ayrı bir yerelleştirme işi olarak ele alınsın.

**Riskler:**
- Xtream başlangıç saatinin saat dilimi sağlayıcıya göre değişiyor (sunucu dilimi mi, UTC mi; topluluk kaynakları çelişiyor). server_info.timezone yoksa cihaz dilimi kullanılır ve sağlayıcı ile izleyici farklı ülkelerdeyse yanlış parça açılabilir. v1'de kullanıcının düzeltebileceği bir ayar yok.
- Xtream xmltv.php geçmiş program vermeyebilir; o zaman rehberde geçmiş hücre görünmez ve yalnız Baştan izle çalışır. Aşama 5 bunun için koşullu yedek.
- Arşiv biçimleri topluluk kaynaklı, resmî Xtream belgesi yok. Yol biçimini ya da Flussonic archive biçimini desteklemeyen sağlayıcı için sıralı yedek biçimler var, ama gerçek sağlayıcıyla doğrulanmadı.
- Geçmiş rehber penceresi veritabanını büyütür. Kırpma yalnız arşivli kanallarda geçmişi tutuyor; 1M programlık sınıra yine de ulaşılırsa pencere dün ve bugüne düşer (yedek yol ve not var).
- TS arşivi çoğu zaman sarılamaz. Sarma yeniden açmayla yapılır, Xtream'de 1 dakikalık adımla ve gecikmeyle. Bekletme tek isteği garanti eder, ama sağlayıcının bağlantıyı geç kapatması bağlantı sınırına kısa süre takılabilir.
- Tvg-id vermeyen, adla eşleşen arşivli kanallarda geçmiş yalnız dün ve bugün için görünür.
- Hatırlatıcı yalnız uygulama açıkken uyarır. Kullanıcı telefondaki gibi bir bildirim bekleyebilir; arayüz metni bunu açıkça söylüyor.
- Önceki önbellekte arşiv alanı yok; özellik ilk katalog yenilemesine kadar (açılışta 6 saat eskime kuralı) görünmez.
- Fiziksel TV (65U95A), görsel kabul, TalkBack ile elle deneme ve gerçek sağlayıcı arşivi doğrulanmadı. Bütün davranışlar öneri, kullanıcı onayı değil.
- Diğer kümelerle çakışma: EPG düzeltme (EXT03) program zamanlarını kaydırırsa arşiv, düzeltilmiş zamanı kullanmalı; oynatıcı üstü mini rehber kümesi PlayerScreen'e aynı anda dokunabilir. D072 ve BKL numaraları birleştirmede yeniden atanabilir.

**Dokunulacak dosyalar:**
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/Models.kt`: Add CatchupMode, CatchupSpec (redacted toString), MediaEntry.catchup and Catalog.serverZone.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/CatalogParser.kt`: CatalogParser.xtream: read tv_archive and tv_archive_duration for LIVE rows. M3uParser.parse: catchup defaults from the #EXTM3U header; per #EXTINF catchup, catchup-type, catchup-source, catchup-days, timeshift, tvg-rec and catchup-correction; placeholder allow-list; xc regex.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/Catchup.kt`: NEW: pure Catchup.state (NONE/UPCOMING/ARCHIVE/START_OVER/EXPIRED/NO_END), Catchup.request (Xtream PATH/PHP, DEFAULT/APPEND/SHIFT/FLUSSONIC templates, server-zone and device-zone time formatting, correction, URL validation), XtreamForm, ArchiveRequest.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/ArchiveTimeline.kt`: NEW: pure timeline: logical position, seek plan (InPlace vs Reopen, minute granularity), end plan (Finished/truncated, Reopen catch-up chain, GoLive within 90 s).
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/Reminders.kt`: NEW: Reminder, ReminderCodec (JSON, corrupt input gives empty), Reminders.conflicts/add/due/prune, ReminderResult, LIMIT = 50.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/CatalogSnapshot.kt`: VERSION 5: append catchupMode, catchupDays, catchupTemplate and catchupCorrection to each row (length 28) and serverZone to the header; v1–v4 still decode with catchup = null.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/SourceRepository.kt`: load(): keep server_info.timezone from the existing auth response (validated ZoneId) in Catalog.serverZone.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/DeviceStore.kt`: reminders()/reminders(list) under the 'reminders' key; saveSources drops removed sources' reminders in the same commit; forgetSource() removes the key.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/EpgModels.kt`: GuideWindow.around(now, zone, pastDays = 1), pastDays in 1..7; ArchiveScope(days, epgIds).
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/GuideController.kt`: Per-owner ArchiveScope from snapshots in synchronize(); stale() and the refresh window use the owner's pastDays; pass keepPast and baseFrom to the repository; on GuideTooLarge retry once with days = 1 and show a status note.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/EpgRepository.kt`: refresh(..., keepPast, baseFrom): wrap each stage sink so programmes older than yesterday are kept only for archive channels' epg ids.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/EpgStore.kt`: Throw a GuideTooLarge SourceFailure subclass at the 1,000,000-programme cap instead of a bare check failure.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/TvViewModel.kt`: PlayingMedia.archive and ArchivePlayback (redacted); TvState.reminders and dueReminders; catchupState, playArchive (same gate as play), archiveSegment, in-memory Xtream form per source; reminder API (reminderFor, addReminder, removeReminder, dismissReminders, watchReminder, remindersForeground, internal remindersTick); scheduler with injectable clock; refresh reminders after removeSource.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/GuideScreen.kt`: Retention uses the archive pastDays; replay and bell markers with semantics on channel rows and cells; long-press OK opens the detail; detail dialog variants (Geçmiş yayını izle / Baştan izle / Hatırlat / Hatırlatıcıyı kaldır, new notes); conflict SourceDialog; vertical actions at fontScale 1.5; hint text.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt`: Archive mode (GEÇMİŞ YAYIN label, programme title and status, VOD-like seeking, CH± and digits exit to live, Up/Down show controls); debounced single reopen for unseekable seeks; end handling through ArchiveTimeline (reopen chain, go live with note, end card); live 'Baştan izle' action; no progress or history for archive; reminder note when the reminded channel is already playing.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/ArchiveEndDialog.kt`: NEW: end card using EpisodeEndDialog geometry: Program bitti / Arşivdeki kayıt burada bitiyor; Sonraki program, Canlı kanalı izle, Baştan izle, Kapat; no countdown.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/ReminderAlert.kt`: NEW: top-right reminder Dialog (420 dp): İzle/Kapat or a list of up to 3 channels, 600 ms input guard, 60 s auto-dismiss, paneTitle and liveRegion, no animation.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PilotApp.kt`: Host ReminderAlert above the routes when allowed (not during opening, LOADING, RECOVERY or SETUP); ON_START calls model.remindersForeground().
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlaybackRecovery.kt`: newMedia(isLive, archive); archive-specific 404/410/400 message; fallback hook (Xtream PHP form, Flussonic rel form) checked before failure; reprepare hook so an unseekable archive reopens at the logical position without resetting the retry budget.
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SettingsScreen.kt`: 'Hatırlatıcılar' row in İzleme ve uygulama; SettingsPage.REMINDERS list (OK removes a row, focus moves to a neighbour, empty state).
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/DesignSystem.kt`: Add 'replay' and 'bell' stroke icon paths.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/CatchupParserTest.kt`: NEW JVM tests for Xtream and M3U catchup parsing.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/CatchupUrlTest.kt`: NEW JVM tests for URL building, time zones, placeholders and encoding.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/CatchupStateTest.kt`: NEW JVM tests for availability boundaries.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/ArchiveTimelineTest.kt`: NEW JVM tests for seek and end plans.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/RemindersTest.kt`: NEW JVM tests for codec, conflicts, limit, due and prune.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/ui/ReminderSchedulerTest.kt`: NEW virtual-time scheduler tests.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/CatalogSnapshotTest.kt`: Add v5 round trip and v4 backward-compatibility cases.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/EpgSourceTest.kt`: Add GuideWindow.around(pastDays), trimming sink and GuideTooLarge fallback cases.
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/ui/PlaybackRecoveryTest.kt`: Add archive message, fallback hook and reprepare budget cases.
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/CatchupFlowTest.kt`: NEW instrumentation: guide to archive playback, request counts, at most one concurrent connection, end card, live Baştan izle, fontScale 1.5, semantics.
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/ReminderFlowTest.kt`: NEW instrumentation: set and remove, persistence, conflict focus, alert via remindersTick, OK plays the channel, alert over the player, Settings list.
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/PrivateGuideAuditTest.kt`: Report the archive channel count and past-programme coverage as numbers only (no URLs, no stream opened).
- `docs/decisions/DECISIONS.md`: D072 (proposal): catch-up and reminder behaviour, in-app-only reminders, guide past window.
- `docs/design/SCREENS.md`: TV-021, TV-050 and TV-051 rows: archive actions, long-press OK, GEÇMİŞ YAYIN mode, end card, reminder alert.
- `docs/product/PRD.md`: EXT02 acceptance criteria and a new P1 reminder row.
- `docs/product/TIVIMATE_GAP.md`: Update the Catch-up and Program hatırlatıcısı rows after delivery.
- `docs/project/BACKLOG.md`: Add BKL-029 (geçmiş yayın) and BKL-030 (hatırlatıcı) with acceptance criteria.
- `docs/architecture/EPG.md`: Document the per-source past window, the archive-only trimming and the cap fallback.
- `docs/project/native-catchup-qa.json`: NEW QA record for the stages.

---

# Geçmiş yayın ve program hatırlatıcısı: uygulama tasarımı

18 Ekim 2026 · salt okunur tasarım, kod çalıştırılmadı. Buradaki ürün davranışları **öneridir**, kullanıcı kararı değildir. Karar kaydına D072 olarak "öneri" etiketiyle girer; birleştirmede numara doluysa sıradaki kullanılır. Yeni backlog maddeleri: BKL-029 (geçmiş yayın), BKL-030 (hatırlatıcı).

> Not: Bu iş akışını başlatan kullanıcı mesajı satış stratejisi, fiyat, yabancı dil ve farklı ülkelerden satın alma üzerineydi. Bu belge o soruyu cevaplamıyor. Bu kümeye düşen kısımlar şunlar: özelliklerin ücretli pakette olup olmayacağı (Sahip kararı 2), dil altyapısı (Sahip kararı 4) ve sağlayıcının saat dilimi (§3.3).

## 0. Bugünkü kod

- `data/CatalogParser.kt` `CatalogParser.xtream`: canlı satırdan yalnız `stream_id`, `name`, `category_id`, `stream_icon`, `epg_channel_id`, `is_adult` ve `container_extension` okunuyor; `tv_archive` ile `tv_archive_duration` atılıyor. `M3uParser.parse`: `#EXTM3U` satırında yalnız `x-tvg-url`/`url-tvg` okunuyor; `#EXTINF` öznitelikleri haritaya giriyor ama `catchup*` kullanılmıyor.
- `data/Models.kt` `MediaEntry`'de arşiv alanı yok. `SourceConfig.streamUrl` canlı için `/live/{user}/{pass}/{id}.{ext}` kuruyor (uzantı yoksa `ts`); ayrıca `xtreamBase` ve `apiUrl` var.
- `data/CatalogSnapshot.kt` `CatalogSnapshotCodec.VERSION = 4`. Satır 24 alan; sürüm 2/3/4 için satır uzunluğu 20/22/24.
- `data/EpgModels.kt` `GuideWindow.around`: dünden 7 gün sonrasına kadar. `GuideController.stale` ve `EpgRepository.refresh` bu pencereyi kullanıyor. `XmltvParser` pencere dışındaki programı atıyor. `EpgStore.Stage`'de en çok 1.000.000 program (`check(++inserted <= 1_000_000)`) ve 96 MB sınırı var.
- `ui/GuideScreen.kt`:
  - `open()`: yayındaki program OK ile canlı açılır, diğerleri "Program ayrıntısı" penceresini açar (`detail`, satır 331–354).
  - Pencerede bitmiş program için not: "Bu program için geçmişten izleme desteklenmiyor…". Tek eylem "Canlı kanalı izle".
  - İlk odak `RemoteTextReader`'dadır, Aşağı eyleme iner (GuideFlowTest bunu doğruluyor).
- `ui/PlayerScreen.kt` `PlayerScreen`:
  - `live = kind == LIVE`. Canlıda CH±, rakam, Yukarı/Aşağı ve rehber tuşu çalışır; VOD'da Sol/Sağ, FF ve RW sarar (`seekBy`).
  - `LaunchedEffect(media)` önce `player.stop()` yapar: tek oturum.
  - STATE_ENDED'de birincil düğme "Baştan izle" olur.
- `ui/PlaybackRecovery.kt`: tek `prepare()`; 404/410 → "Bu yayın artık bulunamıyor…".
- `data/DeviceStore.kt`: düz `SharedPreferences` (geçmiş ve favoriler); kaynak bilgisi Keystore ile şifreli. `saveSources` kaldırılan kaynağın kayıtlarını aynı commit'te siler.
- `AndroidManifest.xml`: bildirim, alarm ya da açılış izni yok. `values/` altında yalnız `styles.xml` var; bütün arayüz metni Kotlin içinde Türkçe sabit.

## 1. Desteklenen arşiv biçimleri (web'de doğrulandı)

| Kaynak | Bildirim | URL kuralı | Not |
|---|---|---|---|
| Xtream | `tv_archive: 1` ve `tv_archive_duration: N` (gün) | Yol: `{base}/timeshift/{user}/{pass}/{süreDk}/{YYYY-MM-DD:HH-MM}/{streamId}.{ts\|m3u8}` · Yedek: `{base}/streaming/timeshift.php?username=&password=&stream={id}&start={YYYY-MM-DD:HH-MM}&duration={süreDk}` | Süre dakika, gün sayısı gün. Başlangıç saati sunucunun saat diliminde (UniPlayer belgesi, polyfin PR); `server_info.timezone` girişte geliyor. Biçimler topluluk kaynaklı; resmî Xtream belgesi yok. |
| M3U `catchup="default"` | `catchup-source` tam şablon | Şablon açılır | Şablon yoksa Kodi "append"e düşer; bizde kapalı. |
| M3U `catchup="append"` | `catchup-source` sorgu parçası | `uri + parça` (`?`/`&` birleştirilir) | |
| M3U `catchup="shift"`, ayrıca `timeshift=` / `tvg-rec=` gün değeri | Gün değeri | `uri + (?\|&)utc={utc}&lutc={lutc}` | Kodi `timeshift`/`tvg-rec`'i SIPTV gün sayısı sayar; mod yoksa eski SIPTV kipi açılır. |
| M3U `catchup="flussonic"`, `flussonic-hls`, `flussonic-ts`, `fs` | Kanal adresi | TS: `{host}/{yol}/timeshift_abs-{utc}.ts` · HLS: `{host}/{yol}/archive-{utc}-{süreSn}.m3u8` | Flussonic belgesi: `archive-FROM-DURATION.m3u8` (VOD tipi liste, sarılabilir) ve `timeshift_abs-UTC.ts`. Kodi HLS'te `timeshift_rel-{offset:1}` kullanıyor; 404'te bir kez o biçim denenir. |
| M3U `catchup="xc"` | `…/{user}/{pass}/{id}(.ts\|.m3u8)` kanal adresi | Xtream yol biçimi | Kodi düzenli ifadesi: `^(https?://[^/]+)/(?:live/)?([^/]+)/([^/]+)/([^/.]+)(\.m3u8?)?$`. `.ts` de kabul edilir. |
| `catchup="vod"`, `{catchup-id}` ve bilinmeyen modlar | — | Desteklenmez | Düğme hiç görünmez (PRD §12 "yanlış yetenek vaadi"). |

Şablon yer tutucuları (Kodi IPTV Simple):
- `{utc}` / `${start}`: başlangıç, epoch saniye.
- `{utcend}` / `${end}`: başlangıç + süre.
- `{lutc}` / `${now}` / `${timestamp}`: şimdi.
- `{duration}` / `${duration}`: süre, saniye. `{duration:X}` süre/X, `{offset:X}` (şimdi − başlangıç)/X.
- `{Y}{m}{d}{H}{M}{S}`: başlangıç, cihaz saatinde (Kodi `localtime` kullanıyor).
- `{utc:YmdHMS}` gibi biçimli sürümler.
- `catchup-correction` (saat): Kodi bunu tüm alanlardan önce başlangıçtan çıkarıyor.
- `#EXTM3U` başlığındaki `catchup`, `catchup-source`, `catchup-days` ve `catchup-correction` bütün kanallar için varsayılandır.

## 2. Veri modeli ve kalıcılık

```kotlin
// data/Models.kt
enum class CatchupMode { XTREAM, DEFAULT, APPEND, SHIFT, FLUSSONIC }
data class CatchupSpec(val mode: CatchupMode, val days: Int, val template: String = "",
    val correctionMinutes: Int = 0, val tsHint: Boolean = false) {
    override fun toString() = "CatchupSpec(mode=$mode, days=$days, template=REDACTED)"
}
// MediaEntry: + val catchup: CatchupSpec? = null   (yalnız LIVE)
// Catalog:    + val serverZone: String = ""       (Xtream server_info.timezone, geçerli ZoneId değilse "")
```

**Önbellek v5** (`CatalogSnapshotCodec`):
- `VERSION = 5`. Satıra 4 alan eklenir: `24 catchupMode` (String, "" = yok), `25 catchupDays` (Int), `26 catchupTemplate` (String), `27 catchupCorrection` (Int dakika).
- Satır uzunluğu `when (version) { 2 -> 20; 3 -> 22; 4 -> 24; else -> 28 }`. v5'te 24 ve 26 String, 25 ve 27 Number olarak denetlenir.
- Başlığa `serverZone` yazılır; okurken `optString`.
- v1–v4 okunur ve `catchup = null` olur. Açılıştaki 6 saatlik eskime yenilemesi (`TvViewModel.restore`, satır 172) ya da "Kataloğu yenile" alanları doldurur. Düğme yenilemeden önce görünmez.
- Şablonda kimlik bilgisi olabilir. Dosya zaten `DeviceStore.seal` ile şifreli; hiçbir yere yazdırılmaz.

**Hatırlatıcı** (`data/Reminders.kt`, yeni):
```kotlin
data class Reminder(val id: String, val sourceId: String, val channelId: String /* kaynak içi yerel id */,
    val channelTitle: String, val title: String, val start: Long, val end: Long?, val createdAt: Long)
// id = stableId("$sourceId\u0000$channelId\u0000$start")
object ReminderCodec { fun encode(list): String; fun decode(raw: String?): List<Reminder> /* bozuksa boş */ }
object Reminders {
    const val LIMIT = 50
    fun conflicts(list, candidate): List<Reminder>      // [start, end ?: start+1 dk) kesişimi, başka id
    fun add(list, candidate, replacing: Set<String>, now): ReminderResult
    fun due(list, now): List<Reminder>                   // start <= now < (end ?: start+30 dk)
    fun prune(list, now): List<Reminder>                 // (end ?: start+30 dk) <= now olanlar çıkar
}
sealed interface ReminderResult { object Added; data class Conflict(val with: List<Reminder>); object Limit; object TooLate }
```

`DeviceStore` değişiklikleri:
- `fun reminders(): List<Reminder>` ve `fun reminders(list)`, anahtar `"reminders"`. Düz prefs; geçmiş ve favorilerle aynı sınıf veri, kimlik bilgisi değil.
- `saveSources` kaldırılan kaynakların hatırlatıcılarını aynı `editor`'da filtreler.
- `forgetSource()` `"reminders"`ı siler; `clearHistory` dokunmaz.

Bellekte, kalıcı olmayan iki veri:
- `TvViewModel.archiveForm: MutableMap<String, XtreamForm>` (PATH ya da PHP).
- Bu oturumda gösterilen hatırlatıcı id'leri.

## 3. Ayrıştırma ve URL kurma

### 3.1 Xtream
`CatalogParser.xtream`, yalnız `kind == LIVE` için:
```kotlin
val archive = row.optString("tv_archive").trim() == "1"
val days = row.optString("tv_archive_duration").trim().toIntOrNull() ?: 0
catchup = if (archive && days > 0) CatchupSpec(CatchupMode.XTREAM, days.coerceAtMost(30)) else null
```
`tv_archive=1` ama süre yok ya da 0 ise düğme çıkmaz.

`SourceRepository.load` mevcut `auth` nesnesinden `server_info.timezone` okur, `ZoneId.of` ile doğrular ve `Catalog(serverZone = …)` olarak saklar.

### 3.2 M3U
`M3uParser.parse`:
- `#EXTM3U` satırında başlık varsayılanları yerel değişkenlerde tutulur: `catchup`/`catchup-type`, `catchup-source`, `catchup-days`, `catchup-correction`.
- Adres satırında, `kind == LIVE` için şu sırayla çözülür:
  - Mod: `catchup` → `catchup-type` → başlık.
  - Şablon: `catchup-source` → başlık. Kodi'de başlık şablonunun kanala ulaşmaması bir hata; biz bilerek uygularız.
  - Gün: `catchup-days` → başlık → `timeshift` → `tvg-rec`. Mod bilinip gün yoksa 1; değer 1..30 aralığına kırpılır.
  - Mod yok ama `timeshift`/`tvg-rec` > 0 ise SHIFT.
- Eşleme:
  - `default`: DEFAULT (şablon şart).
  - `append`: APPEND (şablon şart).
  - `shift`: SHIFT.
  - `flussonic`, `flussonic-hls`: FLUSSONIC. `flussonic-ts`, `fs`: FLUSSONIC ve `tsHint`.
  - `xc`: XTREAM, uri xc düzenli ifadesine uyuyorsa.
  - Diğerleri: null.
- Şablonda izin listesi dışında bir yer tutucu (ör. `{catchup-id}`) varsa null.
- `catchup-correction` "1.5" gibi ondalık olabilir; dakikaya çevrilip ±24 saate kırpılır.

### 3.3 `data/Catchup.kt` (yeni, saf Kotlin, JVM testli)
```kotlin
object Catchup {
    const val GUIDE_PAST_DAYS_MAX = 7
    const val EDGE_MARGIN_MS = 15 * 60_000L     // silinmek üzere olan programı önermez
    const val MAX_PROGRAMME_MS = 6 * 3_600_000L
    enum class State { NONE, UPCOMING, ARCHIVE, START_OVER, EXPIRED, NO_END }
    fun state(entry: MediaEntry, p: EpgProgramme, now: Long): State
    fun request(source: SourceConfig, entry: MediaEntry, p: EpgProgramme, offsetMs: Long, now: Long,
        deviceZone: ZoneId, serverZone: String, form: XtreamForm = XtreamForm.PATH): ArchiveRequest?
}
enum class XtreamForm { PATH, PHP }
data class ArchiveRequest(val url: String, val segmentStart: Long, val segmentEnd: Long, val granularityMs: Long) {
    override fun toString() = "ArchiveRequest(url=REDACTED)" }
```

`state` sırası:
1. `catchup == null` → NONE.
2. `start > now` → UPCOMING.
3. `end == null` → NO_END.
4. `end - start > 6 sa` → NONE.
5. `start < now - days×24 sa + 15 dk` → EXPIRED.
6. `end > now` → START_OVER, yalnız `now - start ≥ 60 sn` ise; değilse NONE.
7. Kalan → ARCHIVE.

`request`:
- `segStart = p.start + offset`; XTREAM'de dakikaya aşağı yuvarlanır (`granularityMs = 60_000`, diğerlerinde 1000).
- `segEnd = min(p.end, now - 30 sn)`; süre `segEnd - segStart`. Xtream dakikası `ceil`, en az 1.
- Xtream saat dilimi: `serverZone` geçerliyse o, değilse cihaz dilimi (Kodi gibi). Kaynak ve izleyici farklı ülkelerdeyse doğru parçayı açan budur.
- `catchup-correction` ve şablon zamanları Kodi'deki gibi uygulanır: düzeltme, biçimlemeden önce başlangıçtan çıkar.
- XTREAM yolu `xtreamBase` ve `addPathSegment` ile kurulur; parola kodlanır, `:` yol parçasında kalır. M3U `xc`'de host, kullanıcı, parola ve id uri'den gelir.
- Sonuç `toHttpUrlOrNull`, http/https olmalı ve kullanıcı bilgisi içermemeli; değilse null olur, düğme gizlenir.
- URL hiçbir zaman gösterilmez ya da loglanmaz (`PlayingMedia.toString` zaten gizliyor).

### 3.4 `data/ArchiveTimeline.kt` (yeni, saf)
```kotlin
class ArchiveTimeline(val programme: EpgProgramme, val segmentStart: Long, val granularityMs: Long) {
    fun logical(playerMs: Long) = segmentStart - programme.start + playerMs
    fun planSeek(target: Long, seekable: Boolean, segmentMs: Long, now: Long): SeekPlan   // InPlace(playerMs) | Reopen(offset)
    fun afterEnd(logicalEnd: Long, now: Long): EndPlan        // Finished(truncated) | Reopen(offset) | GoLive
}
```

`planSeek`:
- Hedef `[0, min(programSüresi, now - start - 30 sn)]` aralığına kırpılır.
- Sarılabilir ve aynı parçadaysa InPlace, değilse Reopen. Xtream'de Reopen dakikaya yuvarlanır.

`afterEnd`:
- Program sürüyor ve canlıya fark < 90 sn → GoLive.
- Program sürüyor ve fark ≥ 90 sn → Reopen(logicalEnd). Bu "canlıya yetişme" zinciri.
- Program bitmiş → Finished. `logicalEnd < süre − 2 dk` ise `truncated = true`.
- Aynı konumda 5 sn içinde ikinci kez bitiş → Finished(truncated).

## 4. Rehber geçmiş penceresi

- `GuideWindow.around(now, zone, pastDays: Int = 1)`, `require(pastDays in 1..7)`.
- `GuideController.synchronize` her kaynak için anlık görüntüden `ArchiveScope(days, epgIds)` hesaplar:
  - `days` = arşivli canlı kanalların en büyük gün sayısı, 1..7.
  - `epgIds` = bu kanalların `epgId` kümesi.
  - Bu değer `Owner`'a eklenir; kapsam değişirse Owner yenilenir.
- `stale` sahibin `days` değerini kullanır. Eski dosyanın `from_time` değeri dündür, bu yüzden bir kez yenilenir. Şema değişmez.
- `EpgRepository.refresh(…, keepPast: (String) -> Boolean, baseFrom: Long)` her sink'i sarar: `(end ?: start+1) <= baseFrom` (dün 00:00) olan program yalnız `channelId in epgIds` ise yazılır. Böylece büyüme yalnız arşivli kanallardadır.
- 1M sınırı:
  - `EpgStore` sınırda `GuideTooLarge : SourceFailure` fırlatır.
  - `GuideController` bir kez `days = 1` ile yeniden dener ve oturum boyunca kısıtlı kalır.
  - Durum notu: "Rehber çok büyük olduğu için geçmiş yayınlar yalnız dün ve bugün için gösteriliyor."
- `GuideScreen`'de `retention = GuideWindow.around(now, zone, pastDays)`. `pastDays` görünen kanalların arşiv günlerinin en büyüğü (1..7). "‹ Önceki gün" ve Geri sarma tuşu bu sınıra kadar gider.
- Sınır: `tvg-id` vermeyen ve adla eşleşen arşivli kanalda geçmiş yalnız dün ve bugün içindir; belgelenir.
- Xtream `xmltv.php` geçmiş program vermeyebilir. Aşama 5'te koşullu yedek var.

## 5. Kumanda akışları

### A. Rehberden geçmiş yayın (TV-021 → TV-051 benzeri arşiv kipi)
1. Menü → Rehber (ya da canlıda rehber tuşu). Odak ızgarada, şimdiki hücrede.
2. Sol eski programlara gider; "‹ Önceki gün" ya da Geri sarma tuşu günü değiştirir. Arşivli kanalın arşiv penceresindeki geçmiş hücrelerinde başta ↺ işareti var; kanal sütununda adın yanında da ↺.
3. OK → "Program ayrıntısı". İlk odak okuma alanında (mevcut davranış).
4. Aşağı → **Geçmiş yayını izle** (birincil, mint dolgu odak). OK → oynatıcı programın başından açılır. Kontroller görünür, odak "Duraklat"ta.
5. Kontroller gizliyken: Sol/Sağ 10 sn (basılı tutunca 30, sonra 60 sn), İleri sarma +30 sn, Geri sarma −10 sn; −10/+10 düğmeleri de var.
   - Sarılamayan yayında hedef 0,7 sn bekletilir, sonra tek yeniden açma yapılır. Köşe notu "+30 sn · 12:40 / 58:00".
6. Geri: kontroller açıksa gizler; gizliyse rehbere döner. İmleç aynı programda kalır (`playArchive` `guideAnchor = programme.start` yazar).
7. Bitiş: §6.3'teki kart.

### B. Canlıda baştan izle
1. Canlı TV → kanal → oynatıcı; kontroller açık.
2. Sağ: Duraklat → ‹ Kanal → Kanal › → **Baştan izle** → Ses / Altyazı. Düğme yalnız kanal arşivliyse ve şimdiki program `START_OVER` durumundaysa görünür. OK → arşiv kipi, şimdiki programın başından.
3. Arşivin sonuna gelince sıralı yeniden açılır, canlıya 90 sn kalınca canlı yayına geçilir. Not: "Canlı yayına yetiştin".
4. "Canlı kanalı izle" her an canlıya döndürür.
5. Rehberde: yayındaki programda **basılı OK** (KeyDown `repeatCount == 1`) ayrıntıyı açar. Eylemler: Canlı kanalı izle (birincil) · Baştan izle · Kapat. Kısa OK eskisi gibi canlıyı açar. Basılı OK kartlardaki hızlı menüyle (D065) aynı dil. Bırakılan KeyUp tüketilir, ilk odak okuma alanında olduğu için yanlış tıklama olmaz.

### C. Hatırlatıcı
1. Rehber → Sağ ile gelecek bir program → OK → ayrıntı → Aşağı → **Hatırlat** (birincil).
2. OK:
   - **Çakışma yok:** Düğme "Hatırlatıcıyı kaldır" olur (onay işareti, `selected`). Not güncellenir ve `liveRegion` ile "Hatırlatıcı kuruldu" okunur. Odak düğmede kalır.
   - **Çakışma var:** Ayrıntı penceresi gizlenir (`SourceDialog(hidden = …)`, SourcesScreen.kt:117'deki desen). "Aynı saatte hatırlatıcı var" açılır, ilk odak **İkisini de tut**; Sağ "Öncekini kaldır", "Vazgeç". Geri = Vazgeç; ayrıntıya ve Hatırlat'a dönülür.
   - **Sınır (50) ya da < 60 sn kaldı:** Hatırlat yerine not çıkar.
3. Geri ayrıntıyı kapatır; hücrede zil işareti görünür.
4. Program başlarken NEXTV açıksa sağ üstte uyarı kartı çıkar (Dialog).
   - İlk odak **İzle**; ilk 600 ms girdi yok sayılır (D064'teki 600 ms kuralı gibi).
   - OK canlı kanalı açar; Sağ → Kapat; Geri kapatır.
   - 60 sn dokunulmazsa kendiliğinden kapanır.
5. Hatırlatılan kanal zaten oynuyorsa kart çıkmaz; oynatıcı köşe notu gösterir: "Hatırlatıcı" / "{Program} başladı".
6. Ayarlar → İzleme ve uygulama → **Hatırlatıcılar** → liste; OK satırı kaldırır, odak komşuya geçer. Geri satıra döner.

## 6. Ekran değişiklikleri

### 6.1 `ui/GuideScreen.kt`
- Kanal sütunu: arşivli kanalda adın sağında `LineIcon("replay")` (12 dp, `Ink.Muted`). Satır anlatımına ", geçmiş yayınlar var" eklenir.
- Hücre: `cellWidth > 48.dp` ise başlığın soluna ↺ (`ARCHIVE`/`START_OVER`) ya da zil (hatırlatıcı). Semantik ekleri: ", geçmiş yayın izlenebilir" / ", hatırlatıcı kurulu". Izgaranın `contentDescription`'ı da aynı eki alır.
- `onPreviewKeyEvent`: basılı OK yukarıdaki gibi. Başlık ipucu metnine " · Basılı OK: ayrıntı" eklenir (tek satır; sığmazsa kırpılır).
- Ayrıntı penceresi (`detail`):
  - Durum `model.catchupState(entry, programme)`, hatırlatıcı `s.reminders` üzerinden okunur.
  - Not ve eylemler §8'deki tabloya göre seçilir.
  - Çakışan programlarda (`slot.programmes.size > 1`) yalnız canlı eylem.
  - `fontScale >= 1.5` iken eylemler alt alta dizilir; Yukarı ilk eylemden okuma alanına çıkar.
- Çakışma penceresi: `SourceDialog("Aynı saatte hatırlatıcı var", …)` içinde `RemoteTextReader` (liste) ve üç `Action`.

### 6.2 `ui/PlayerScreen.kt`
- `PlayingMedia`'ya `archive: ArchivePlayback? = null` eklenir. `ArchivePlayback(programme, segmentStart, segmentEnd, granularityMs, form)`, toString gizlenir. `val live = kind == LIVE && archive == null`.
- Arşiv kipi:
  - Üst etiket `LineIcon("replay")` + "GEÇMİŞ YAYIN".
  - Başlık program adı.
  - Durum satırı `"{konum} / {süre} · {kanal} · {d MMM HH:mm}"`; açılırken "Geçmiş yayın açılıyor…".
  - İlerleme çubuğu mantıksal konum / program süresi.
  - Kısayol ipucu "Kontroller kapalıyken Sol/Sağ: sar".
- Tuşlar:
  - Arşivde Sol/Sağ, İleri ve Geri sarma VOD gibi.
  - Gizliyken Yukarı/Aşağı kontrolleri açar (yanlışlıkla arşivden çıkmasın).
  - CH± ve rakamlar canlı kanal değiştirir (arşivden çıkar).
  - Rehber tuşu rehberi programın üstünde açar.
- Arşiv kontrolleri: [Duraklat/Oynat/Baştan izle] [−10 sn] [+10 sn] [Canlı kanalı izle] [Ses / Altyazı] [★ kanal] [Kapat].
- Canlı kontrollerine "Baştan izle" eklenir (§5B).
- Yeniden açma `PlayerScreen` içinde, `PlayingMedia` değişmeden yapılır:
  - `player.stop(); clearMediaItems(); setMediaSource(model.archiveSegment(…)); prepare()`.
  - Yerel `timeline` güncellenir. `recovery.newMedia` çağrılmaz, bütçe korunur.
  - Önceki istek kapanmadan yenisi açılmaz.
- STATE_ENDED: `timeline.afterEnd` → Reopen / GoLive (`model.play(entry)` + not) / Finished → `ArchiveEndDialog`.
- `save()` arşivde ilerleme yazmaz (`saveProgress` LIVE'ı zaten atlar). `recordViewed` arşivde çağrılmaz.
- `LaunchedEffect(screenState.dueReminders)`: §5C-5'teki not, ardından `model.dismissReminders`.

### 6.3 `ui/ArchiveEndDialog.kt` (yeni)
- `EpisodeEndDialog` ölçüleri: 480 dp, sağ alt, yatay 38/dikey 26 dp dış boşluk, 24 dp iç boşluk, 16 dp köşe. Geri sayım ve animasyon yok.
- Başlık "Program bitti" ya da kesik kayıtta "Arşivdeki kayıt burada bitiyor". Altında program adı.
- Eylemler: **Sonraki program** (aynı kanalda sonraki program ARCHIVE/START_OVER ise; alt satır "{HH:mm} {başlık}"), Canlı kanalı izle, Baştan izle, Kapat.
- İlk odak Sonraki program, yoksa Canlı kanalı izle.
- Geri ve Kapat kartı kapatıp kontrolleri gösterir. Otomatik geçiş yok.

### 6.4 `ui/ReminderAlert.kt` (yeni), `ui/PilotApp.kt`
- `PilotApp` route'ların üstünde, Settings/Exit'ten önce kartı çizer: `state.dueReminders.isNotEmpty()`, açılış kapağı yok, route LOADING/RECOVERY/SETUP değil. Bu koşul sağlanana kadar kart bekler, `due` korunur.
- Kart: Dialog, `usePlatformDefaultWidth = false`, Box `TopEnd`, 420 dp, `Ink.Container`, 18 dp köşe, 24 dp iç boşluk.
  - "HATIRLATICI" 12 sp mint harf aralıklı; program 20/26 sp, 3 satır; "{Kanal}" ve "Başlangıç {HH:mm}" 14 sp `Ink.Muted`. "21:00'de" yazılmaz, Türkçe ek sayıya göre değişir.
  - Birden çok kart: başlık "{n} program başladı"; satır başına `Action("{Kanal} · {Program}")` (en çok 3), sonra Kapat.
- `paneTitle = "Hatırlatıcı"`, metin `liveRegion = Polite`. Hareketi azalt'ta ve normalde giriş animasyonu yok.
- `LifecycleEventEffect(ON_START) { model.remindersForeground() }`.

### 6.5 `ui/SettingsScreen.kt`, `ui/DesignSystem.kt`
- `SettingsPage.REMINDERS` eklenir. "İzleme ve uygulama" bölümünde `SettingRow("Hatırlatıcılar", …)`. Sayfa satırları `SettingRow`'dur (beyaz 2 dp halka, büyüme yok); boş durum notu var.
- `iconPaths`'e iki çizgi ikon: `"replay" to "M4 12a8 8 0 1 0 2.3-5.7M4 4v4h4"`, `"bell" to "M6 16v-5a6 6 0 0 1 12 0v5l2 2H4ZM10 20a2 2 0 0 0 4 0"`.

## 7. ViewModel API (`TvViewModel.kt`)
```kotlin
// TvState: + val reminders: List<Reminder> = emptyList(), + val dueReminders: List<Reminder> = emptyList()
fun catchupState(entry: MediaEntry, programme: EpgProgramme): Catchup.State
fun playArchive(entry: MediaEntry, programme: EpgProgramme, offsetMs: Long = 0)   // play() ile aynı kapı: adult, sourceFor
internal fun archiveSegment(entry: MediaEntry, programme: EpgProgramme, offsetMs: Long, form: XtreamForm): ArchiveRequest?
fun reminderFor(entry: MediaEntry, programme: EpgProgramme): Reminder?
fun addReminder(entry: MediaEntry, programme: EpgProgramme, replacing: Set<String> = emptySet()): ReminderResult
fun removeReminder(id: String)
fun dismissReminders(ids: Collection<String>)
fun watchReminder(reminder: Reminder)        // kanalı sourceId + yerel id ile bulur; yoksa no-op
fun remindersForeground()
```

- `serverZone` birleşik katalogda değil, `snapshots[source.id]?.catalog?.serverZone` üzerinden okunur. `SourceLibrary.combine` bu alanı taşımaz.
- Zamanlayıcı `viewModelScope` içinde çalışır, `clock: () -> Long` enjekte edilir:
  - Bir sonraki başlangıca kadar `delay`, en çok 60 sn (saat değişikliğine dayanıklı).
  - Her uyanmada `prune`, `due` ve `dueReminders` güncellenir.
  - Test için `internal fun remindersTick()`.
- `removeSource` sonrasında `reminders` yeniden okunur.
- Gelecekteki PIN kapısı (BKL-028) `play` ile birlikte `playArchive`'ı da korumalı.

## 8. Durumlar, kenar durumları ve Türkçe metinler

| Durum | Ayrıntı penceresinde eylemler | Not / metin |
|---|---|---|
| ARCHIVE | **Geçmiş yayını izle** · Canlı kanalı izle · Kapat | "Kaynağının arşivinden oynatılır." |
| START_OVER (basılı OK) | **Canlı kanalı izle** · Baştan izle · Kapat | — |
| EXPIRED | **Canlı kanalı izle** · Kapat | "Bu program kaynağının arşiv süresinin ({n} gün) dışında kaldı. Yalnız canlı yayın açılabilir." |
| NONE, bitmiş program | **Canlı kanalı izle** · Kapat | "Kaynağın bu kanal için geçmiş yayın sunmuyor. Yalnız canlı yayın açılabilir." (eski "geçmişten izleme desteklenmiyor" yerine) |
| NO_END | **Canlı kanalı izle** · Kapat | "Programın bitiş saati bilinmediği için geçmiş yayın açılamıyor." |
| UPCOMING, hatırlatıcı yok | **Hatırlat** · Canlı kanalı izle · Kapat | "Program başladığında NEXTV açıksa ekranda haber verilir." |
| UPCOMING, hatırlatıcı var | **Hatırlatıcıyı kaldır** · Canlı kanalı izle · Kapat | "Hatırlatıcı kuruldu. Program başladığında NEXTV açıksa ekranda haber verilir." |
| UPCOMING, < 60 sn | **Canlı kanalı izle** · Kapat | "Program birazdan başlıyor." |
| Sınır | (Hatırlat pasif) | "En çok 50 hatırlatıcı kurulabilir. Ayarlar → Hatırlatıcılar'dan eskilerini kaldırabilirsin." |
| Çakışan programlar | **Canlı kanalı izle** · Kapat | "Bu saatte çakışan programlar var. Geçmiş yayın ve hatırlatıcı tek program için kullanılabilir." |

Hatalar ve kenar durumları:
- **Arşiv 404/410/400:** `playbackProblem(error, archive = true)` → "Bu program kaynağının arşivinde bulunamadı. Arşiv süresi dolmuş olabilir." Otomatik tekrar yok. Xtream yol biçiminde ilk oynatmadan önce gelen 404/400 hata gösterilmeden bir kez `timeshift.php` biçimini dener ve seçimi kaynak için oturum boyunca hatırlar. Flussonic HLS'te bir kez Kodi `timeshift_rel` biçimi denenir. Bu, `PlaybackRecovery`'ye `fallback: (PlaybackException) -> Boolean` kancasıyla girer ve sıralıdır.
- **401/403/429:** mevcut metinler.
- **Kurtarma:** arşivde `PlaybackRecovery(reprepare = …)` kancası. Sarılamayan parçada `prepare()` yerine mevcut mantıksal konumdan yeniden açar; 1/2 bütçesi ve 20 sn sınırı korunur.
- **Rehber saati yanlış** (EXT03 kaydırması yok): arşiv yanlış parçayı açar. v1'de kullanıcı düzeltemez. EXT03 kümesi kaynak başına kaydırma eklerse rehberin düzeltilmiş program zamanları `Catchup.request`'e verilir; ayrı kaydırma yapılmaz.
- **Xtream `server_info.timezone` yok:** cihaz dilimi kullanılır (risk, §11).
- **Kanal arşivli, XMLTV'de geçmiş yok:** geçmiş hücre çıkmaz. Baştan izle yalnız şimdiki programın başlangıcı biliniyorsa çalışır; rehbersiz kaynakta görünmez.
- **Program > 6 sa:** NONE, düğme yok.
- **Gece yarısı ve yaz saati:** zamanlar epoch'tur, biçimleme dilimde yapılır; JVM testi var.
- **Kaynak arşiv oynarken kaldırılırsa:** mevcut `removeSource` `playing = null` yapar.
- **Yenileme kanaldan arşivi kaldırırsa:** açık oynatma sürer; sonra düğme görünmez.
- **Yetişkin ya da gizli kategori:** kanal katalogdan çıktığı için hiçbir yerde görünmez. Hatırlatıcı tetiklenirse kart "Bu kanal artık listende görünmüyor." der ve yalnız Kapat sunar.
- **Rehber yenilemesi saati kaydırmış program:** hatırlatıcı kayıtlı saatte tetiklenir, kayıtlı başlığı gösterir (v1 kabulü).
- **Süreç ölürse:** hatırlatıcılar kalıcıdır. Açılışta program hâlâ sürüyorsa kart route HOME olunca çıkar; bitmişse sessizce silinir.
- **Uygulama arka plandayken başlayan program:** ON_START'ta hâlâ sürüyorsa kart "Başlangıç {HH:mm}" ile çıkar.
- **Birden çok kaynak:** kartta ve listede kaynak adı `s.sourceName(id)` ile gösterilir, `sources.size > 1` ise.
- **Ağ:** arşiv tek HTTP oturumu açar. Yedek biçim, yeniden açma ve yetişme zinciri sıralıdır. Sonraki program önceden açılmaz, HEAD isteği atılmaz.

## 9. Uygulama kapalıyken (dürüst durum)

v1'de **yalnız uygulama içi uyarı** var; arayüz bunu "NEXTV açıksa" diye açıkça söyler. Gerekçeler:
- Android TV ve Google TV'de telefondaki gibi bir bildirim gölgesi yok. Resmî TV kalite yönergesi bildirimi yalnız "Now Playing" bağlamında anıyor. Google TV kullanıcıya uygulama bazında bildirim ayarı veriyor, ama üstte açılan uyarının üreticiye göre görünüp görünmediğine dair resmî belge bulunamadı ve 65U95A'da denenmedi.
- Android 14+'da yeni kurulumda `SCHEDULE_EXACT_ALARM` varsayılan olarak reddediliyor. `USE_EXACT_ALARM` Play politikasıyla yalnız alarm ve takvim uygulamalarına açık. Kalan seçenekler `setWindow()` (en az 10 dk pencere) ya da `set()`.
- Arka plandan Activity başlatmak ve tam ekran intent kısıtlı; uygulama kendini açamaz.
- Alarmlar yeniden başlatmada silinir; `RECEIVE_BOOT_COMPLETED` ve API 33+ için `POST_NOTIFICATIONS` izni gerekir. TV beklemedeyken zaten görünür bir şey yok.

Sistem bildirimi isteğe bağlı Aşama 5'tir (Sahip kararı 1).

## 10. Testler

JVM (`app/src/test`):
- `data/CatchupParserTest`:
  - Xtream `tv_archive` "1"/1/"0"; süre "3"/3/""/"0"; canlı olmayan satır yok sayılır.
  - M3U kanal ve başlık varsayılanları, `catchup-type` eşi, `timeshift`/`tvg-rec` → SHIFT, bilinmeyen mod, `{catchup-id}` reddi.
  - `xc` uyan/uymayan uri, `flussonic-ts`; gün varsayılanı 1, üst sınır 30; correction "1.5".
- `data/CatalogSnapshotTest` (ekleme): v5 gidiş-dönüşü (catchup + serverZone); v4 satırları `catchup = null`; yanlış satır uzunluğu reddi.
- `data/CatchupUrlTest`:
  - Xtream yolu `Europe/Istanbul` ve `UTC` sunucuda aynı an için farklı metin; dakika yuvarlama, süre `ceil`, yayındaki programda now−30 sn sınırı; m3u8 korunur; `timeshift.php` biçimi.
  - Parolada `/ ? # %` kodlaması.
  - DEFAULT'ta bütün yer tutucular (`{utc:YmdHMS}`, `{duration:60}`, `{offset:1}`, `${start}`); APPEND'de `?`/`&` birleştirme; SHIFT; Flussonic ts ve m3u8, token sorgusu korunur.
  - correction; http dışı sonuç reddi; `Europe/Berlin` yaz saati dönüş günü.
- `data/CatchupStateTest`: altı durumun sınırları (15 dk pay, 60 sn, 6 sa).
- `data/ArchiveTimelineTest`: mantıksal konum, InPlace/Reopen, Xtream yuvarlaması, yayındaki programda kırpma, GoLive (< 90 sn), Reopen (≥ 90 sn), Finished/kesik, 5 sn döngü koruması.
- `data/RemindersTest`: codec ve bozuk girdi, id kararlılığı, kesişim (bitişik programlar çakışmaz, `end = null`), sınır 50, TooLate, prune, due penceresi, kaynak kaldırma filtresi.
- `data/EpgSourceTest` (ekleme): `GuideWindow.around(pastDays)` yaz saatinde; kırpan sink yalnız arşivli kanalların eski programını tutar; `GuideTooLarge` sonrası `days = 1` yeniden deneme.
- `ui/ReminderSchedulerTest` (kotlinx-coroutines-test, sanal zaman): başlangıçta tetiklenir, önce tetiklenmez; foreground yeniden denetimi; kapatılan yeniden tetiklenmez; saat sıçraması.
- `ui/PlaybackRecoveryTest` (ekleme): arşiv 404 metni; fallback kancası hata göstermeden tüketilir; `reprepare` kullanılır, bütçe sıfırlanmaz.

Cihaz testleri (`app/src/androidTest`):
- **CatchupFlowTest**, GuideFlowTest düzeni: M3U `DEFAULT` şablonu `/archive/{n}?utc={utc}&d={duration}`, `playback-fixture.mp4`; istek sayacı ve eşzamanlı bağlantı sayacı.
  1. Arşivdeki geçmiş program → ayrıntı → Aşağı → "Geçmiş yayını izle" → "GEÇMİŞ YAYIN": tam 1 arşiv isteği, 0 canlı istek, `utc` program başlangıcına eşit.
  2. Arşivsiz kanal → düğme yok, not metni doğru.
  3. Sarma: eşzamanlı bağlantı hiçbir an 1'i aşmaz.
  4. Bitiş kartında odak doğru; Geri kartı kapatır ve kontroller görünür.
  5. Canlıda arşivli kanalda "Baştan izle" var, arşivsizde yok.
  6. fontScale 1.5'te eylemler pencerenin içinde ve ulaşılabilir (`insideDialog`).
  7. TalkBack semantiği: hücre ve kanal ekleri.
- **ReminderFlowTest**:
  - Hatırlat → hücre semantiğinde "hatırlatıcı kurulu"; `model.restore()` sonrası kalıcı.
  - Çakışmada ilk odak "İkisini de tut"; Geri iptal eder.
  - `remindersTick()` ile kart çıkar; 600 ms sonra OK canlıyı açar (1 istek); oynatıcı üstünde kart çıkınca video PLAYING kalır; Geri kapatır.
  - Ayarlar listesinden kaldırma.
- Mevcut GuideFlowTest değişmeden geçmeli (okuma alanı önce, Aşağı eylem).
- **Özel denetim:** `PrivateGuideAuditTest` arşivli kanal sayısını ve geçmiş program kapsamını yalnız sayı olarak raporlar (URL yok, akış açılmaz).
- QA kaydı: `docs/project/native-catchup-qa.json`.

## 11. Riskler (özet)

Sağlayıcı farkları:
- Xtream başlangıç saat dilimi.
- `xmltv.php`'de geçmiş olmaması.
- Yol biçimini desteklemeyen sunucu.
- "Flussonic benzeri" ama gerçek Flussonic olmayan kaynak.

Teknik:
- Rehber veritabanının büyümesi ve 1M sınırı.
- TS arşivinin sarılamaması (yeniden açma gecikmesi, 1 dk adım).
- Hatırlatıcının yalnız uygulama içi olması.
- Fiziksel TV ve gerçek sağlayıcı arşivi doğrulanmadı.

## 12. Belgeler (kod dışı)
- DECISIONS'a D072 (öneri).
- SCREENS TV-021/050/051 satırları.
- PRD EXT02 kabul ölçütü ve hatırlatıcı satırı.
- TIVIMATE_GAP'te iki satır.
- BACKLOG'a BKL-029/030.
- `docs/architecture/EPG.md` geçmiş pencere.
- STATUS/HANDOFF.

## 13. Kaynaklar
- Kodi IPTV Simple README (catchup bölümü): https://github.com/kodi-pvr/pvr.iptvsimple/blob/Omega/README.md
- Kodi kaynakları: `PlaylistLoader.cpp` ve `PlaylistLoader.h` (öznitelik adları), `Channel.cpp` (xc/flussonic/shift üretimi), `CatchupController.cpp` (yer tutucular, yerel saat, düzeltme, yayındaki programda süre sınırı): https://github.com/kodi-pvr/pvr.iptvsimple/tree/Omega/src/iptvsimple
- UniPlayer arşiv tanımı (Xtream yolu, dakika, sunucu saat dilimi): https://uniplayer.net/docs/specs/catchup/
- polyfin PR #199 (Xtream yolu, `server_info.timezone`): https://github.com/moodiness/polyfin/pull/199
- LiveWireTV PR #36 (yol biçiminde 404 olursa timeshift.php yedeği): https://github.com/namillis/LiveWireTV/pull/36
- Dispatcharr issue #133 (`streaming/timeshift.php` sorgu biçimi, `YYYY-MM-DD:HH-MM`, dakika): https://github.com/Dispatcharr/Dispatcharr/issues/133
- Flussonic belgeleri: https://flussonic.com/doc/iptv-ott-quickstart/ , https://flussonic.com/doc/mcaster/dvr/
- Android 14 tam zamanlı alarm değişikliği: https://developer.android.com/about/versions/14/changes/schedule-exact-alarms
- TV uygulama kalitesi (TV-NP): https://developer.android.com/docs/quality-guidelines/tv-app-quality
- Google TV uygulama bildirim ayarları (9to5Google): https://9to5google.com/2024/01/16/google-tv-call-notifications/
