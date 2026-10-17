# S15 tasarımı: oynatıcıda kanal listesi, mini rehber, bilgi bandı, önceki ve son kanal

18 Ekim 2026 · tasarım ajanı, yalnız okuma. Kod değişmedi; Gradle ve adb çalıştırılmadı. Bu belgedeki her karar **öneridir**, sahip onayı değildir. Aksi yazılmadıkça yollar `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/` altındadır.

## Özet

- **Liste nasıl açılır:** Canlı yayında kontroller kapalıyken kısa OK, soldan açılan bir kanal panelini getirir (TV-052). Panel videonun üstüne biner; video küçülmez.
- **Panelin içeriği:**
  - Kanallar, kanalın açıldığı bağlamdan gelir (kategori, Favoriler ya da Tüm kanallar). CH± de aynı listeyi kullanır.
  - Sol tuş kategori sütununu açar. Sağ tuş satırdaki kanalın sonraki programlarını gösteren mini rehberi açar.
  - Paneldeki OK kanalı değiştirir ve panel açık kalır. Oynayan satırda OK paneli kapatır. Geri yalnız bir katman kapatır.
- **Bilgi bandı:** Kontroller ve liste kapalıyken her kanal değişiminde alt bant 4 sn görünür (TV-053): numara, ad, şimdi/sonra, saatler ve ilerleme.
- **Önceki kanal:** Bekleyen rakam yokken `0` tuşu, varsa kumandanın `LAST_CHANNEL` tuşu, panel başlığındaki "Önceki kanal" ve kontrol satırındaki aynı düğme.
- **Son kanal:** Kaynak başına kalıcı tutulur (`lastLive:<kaynak>`). "Açılışta son kanalı aç" ayarı varsayılan kapalıdır. Açıksa kanal açılış kapağının arkasında tamponlanır ve kapak kalkınca başlar; Geri Ana Sayfa'ya döner.
- **Temellere bağlantı:** T3 kapısına (`AccessTarget.ChannelPick`), T4'e (`PlayerLayer.ChannelList`, `PlayerKeyRouter` vakaları) ve T2'ye (Oynatıcı sayfasında tek bir anahtar satırı) takılır.
- **Kilitli ve gizli kanallar:** Kilitli kanal listede rozetle görünür; açmak PIN ister ve PIN gelene kadar mevcut yayın sürer. Gizli ve yetişkin kanallar hiçbir zaman listelenmez.

## Efor

**Toplam yaklaşık 6 mühendis-günü.** PLAN'daki yer tutucu ≈4 g idi, yani +2 g. M3 kilometre taşı 61,5 → 63,5 g olur.

| Aşama | İçerik | Ayrı yayınlanabilir mi | g |
|---|---|---|---|
| S15a · Veri ve kurallar | `ChannelScope` + `LiveIndex`; bütün oynatma yollarında `liveScope`; `changeChannel`, `playChannelNumber` ve `channelNumber` LiveIndex'e geçer. Böylece Favoriler'de CH± çalışır ve zap sırası numara sırasıyla aynı olur. Ayrıca: `LiveRecall` (önceki kanal), `lastLive` anahtarı ve temizliği, `AutoStart.decide`, `ChannelPick` hedefi, oynatıcıdan rehber tuşunun oynayan kanala gitmesi, `PlayerKeyRouter` vakaları ve JVM testleri | Evet. Görünür fark yalnız hata düzeltmeleri | 1,5 |
| S15b · Panel | `ui/ChannelPanel.kt` (kanal sütunu, kategori sütunu, mini rehber, program ayrıntısı); kısa/basılı OK dili; kontrol satırına "Kanallar"; odak, Geri ve boşta kapanma; görünen satırlar için EPG; kilitli satır ve PIN; TalkBack; 1,0/1,5/2,0 yazı; ChannelListFlowTest 1. kısım; karar kaydı | Evet | 2,5 |
| S15c · Bant ve son kanal | `ui/ChannelBand.kt`; `0`/`LAST_CHANNEL`/"Önceki kanal"; basılı zapta önizleme ve bırakınca açma; "Açılışta son kanalı aç" ve açılış kapağı/ses etkileşimi; kütüphaneye dönüşte odak eşitleme; SCREENS/NATIVE_UI; ChannelListFlowTest 2. kısım; ParentalFlowTest satırları | Evet | 1,5 |
| Dengeleme | 1,5/2,0 yazı, TalkBack turu, 4.500 kanallık ölçüm sondası | — | 0,5 |

**Ön koşullar:**
- S04 ve S14: ayar çerçevesi ve Oynatıcı sayfası; S14'teki `ControlsAutoHide` ve OSD süresi.
- S05: `request(target)`, `AppOverlay`.
- S06: `PlayerKeys.kt`, `PlayerLayer`.
- S10 ve S11: `nextZap`, `lockedIds`, PIN istemi.
- S10/S11 ertelenirse kilit dalları `lockedIds` boş olduğu için etkisizdir; tasarım değişmez.

**Sıra (PLAN §3):**
- S15, `PlayerScreen.kt`'ye S14'ten sonra ve S16'dan önce dokunur.
- organize S31 bu belgedeki `ChannelScope`/`LiveIndex` API'sini devralır; ikinci bir "kanal bağlamı" yazmaz.
- record TV-091 kanal seçicisi `ChannelPanelRow` ve `LiveIndex`'i yeniden kullanır.

## Sahip kararları (cevap gelmezse varsayılan)

| # | Karar | Varsayılan | Alternatif |
|---|---|---|---|
| 1 | Canlıda kontroller kapalıyken kısa OK ne yapsın? Bugün kontrolleri açıyor (D064 sonrası alışkanlık). | **Kanal listesini açar.** Kontrollere basılı OK, Menü, iki kez Bilgi, Sol/Sağ (canlı pencere yoksa) ve listedeki "Kanallar" düğmesinin karşılığıyla ulaşılır. Gerekçe §5. | OK kontrolleri açmaya devam eder; liste yalnız "Kanallar" düğmesinden açılır (iki basış). |
| 2 | Listede başka bir kanala OK | **Kanal değişir, liste açık kalır**, odak yeni satırda. Oynayan satırda OK listeyi kapatır ("OK, OK" = seç ve izle). | Kanal değişir, liste kapanır. |
| 3 | `0` tuşu | **Bekleyen rakam yoksa önceki kanal**; varsa basamaktır ("1","0" → 10). | `0` yalnız basamak; önceki kanal yalnız düğmeden. |
| 4 | "Açılışta son kanalı aç" | **Kapalı.** Ayarlar → Oynatıcı → Canlı TV altında. Kilitli kanal kendiliğinden açılmaz; "Açılışta PIN iste" açıksa önce kilit ekranı gelir. | Açık varsayılan (önerilmez: soğuk açılışta beklenmedik ses ve bağlantı). |
| 5 | Kanal değişiminde bilgi bandı | **4 sn**, yalnız kontroller ve liste kapalıyken. Geri yalnız bandı kapatır. | Süre OSD süresine (S14) bağlanır. |
| 6 | Basılı Yukarı/Aşağı/CH± | **Bant hedef kanalı önizler; yayın yalnız tuş bırakılınca açılır.** Bugün her tekrar yeni bir akış açıyor, bu da tek bağlantı kuralını zorluyor. | Bugünkü gibi her tekrarda kanal değişir. |
| 7 | Oynatıcıdan Geri ile Canlı TV'ye dönüş | **Liste, son izlenen kanalın kategorisine ve satırına döner.** | Oynatıcıya girilen kategori ve satır korunur (bugünkü davranış). |
| 8 | "Geçmişi temizle" son kanalı da unutsun mu | **Evet** (izleme izidir). | Son kanal kalır. |
| 9 | Panelin boşta kapanması | **15 sn girdi yoksa kapanır**; TalkBack açıkken hiç. S14'te "Hiçbir zaman" seçilmişse kapanmaz. | Yalnız Geri ile kapanır. |

**İtiraz gelmezse uygulanacak tasarım varsayılanları:**
- Panel her açılışta zap bağlamını (oynayan kanalın listesi) gösterir ve odak oynayan satırdadır.
- Listenin uçlarında dönme yoktur; CH± zap'ta döner (bugünkü gibi).
- Panelde CH±/PageUp/PageDown "görünen satır − 1" kadar sayfa atlar (rehberle aynı).
- Panelde rakam kanala odaklanır, oynatmaz (rehber D067 ile aynı). Kanal o listede yoksa "Tüm kanallar"a geçilir.
- Kategori sütununda gezinmek listeyi değiştirmez, OK değiştirir (D060). Kategori seçmek oynatmayı değiştirmez.
- "Son izlenenler" bir zap bağlamı değildir: CH± her izlemede sırası değişen bir listede sekerdi.
- Mini rehber önümüzdeki 12 saati, en çok 8 programı gösterir. Odak değiştikçe yeniden sorgulanmaz.
- VOD'da panel, bant ve önceki kanal yoktur.
- Kanal numarası her yerde "Tüm kanallar" sırasıdır.
- Liste ücretsiz katmandadır (PLAN §0); `Feature` denetimi yoktur.

## Dokunulacak dosyalar

| Dosya | Değişiklik |
|---|---|
| `data/ChannelScope.kt` (yeni) | `ChannelScope`, `ScopeChoice`, `LiveIndex.build/list/number/scopes/resolve` (saf) |
| `data/LiveRecall.kt` (yeni) | `LivePick`, `LiveRecall` (bellek), `LastLive` codec'i, `AutoStart.decide` (saf) |
| `data/AccessGate.kt` / `AccessTargets.kt` (S05/S10) | `AccessTarget.ChannelPick(id, scope, origin)`, `PickOrigin`; `nextZap` LiveIndex listesiyle |
| `data/DeviceStore.kt`, `data/DeviceKeys.kt` (S07) | `lastLive(source)`, `startLastChannel`; `saveSources`, `forgetSource` ve `clearHistory` temizliği |
| `TvViewModel.kt` | TvState: `live`, `liveScope`, `previousLiveId`, `startLastChannel`. `play(scope)`, `changeChannel`/`zapTarget`, `playChannelNumber`, `channelNumber`, `pickChannel`, `previousChannel`, `startLastChannel(Boolean)`. `guide()` oynatıcıdan oynayan kanalı kullanır. `recordViewed` → `lastLive`. `restore(atLaunch)` → AutoStart. `back()` → kütüphane odağı. Mantık saf sınıflarda, VM ince delege (PLAN kuralı 8). |
| `ui/PlayerKeys.kt` (S06) | `PlayerLayer.ChannelList`, `KeyPhase`, yeni `PlayerCommand`'lar, `back(ctx)` |
| `ui/PlayerScreen.kt` | Katman bağlama; `quiet` yerine `ChangeOrigin`; OK KeyUp/basılı ayrımı; `0`/`LAST_CHANNEL`/`INFO`/`MENU`; basılı zap önizlemesi; bant; kontrol satırına "Kanallar" ve "Önceki kanal"; ipucu metni; tuval semantiği; başarısızlıkta panel davranışı; açılış kapağında `playWhenReady` |
| `ui/ChannelPanel.kt` (yeni) | `ChannelPanel`, `ChannelPanelRow`, `ScopeColumn`, `MiniGuide`, `ProgrammeDetailDialog`, `ChannelPanelState` (düz tutucu) |
| `ui/ChannelBand.kt` (yeni) | `ChannelBand`, `ChannelBandModel.from(...)` (saf) |
| `ui/LibraryScreen.kt` | Özel `Progress` → `DesignSystem.kt`; dönüşte `focusedId` ve `category` eşitlemesi VM'den gelir (ekran değişmez) |
| `ui/DesignSystem.kt` | `Progress` ortaklaşır; `iconPaths`'e "list" ve "recall" |
| `ui/settings/PlayerPages.kt` (S14) | `ToggleRow("Açılışta son kanalı aç")` |
| `ui/PilotApp.kt`, `MainActivity.kt` | `LocalLaunchCover`; kapak bittiğinde rota PLAYER ise açılış sesi durur |
| Testler | `test/.../ui/PlayerKeyRouterTest.kt`, `test/.../data/{ChannelScopeTest, LiveRecallTest, AutoStartTest, ChannelBandModelTest}.kt`, `AccessGateTest` eki; `androidTest/.../ChannelListFlowTest.kt`, `ParentalFlowTest` ve `GuideFlowTest` ekleri |
| Belgeler | SCREENS (TV-052, TV-053), NATIVE_UI oynatıcı bölümü, DECISIONS (sıradaki boş numara; D072 epg/catchup'ın yer tutucusu), BACKLOG BKL-032, STATUS, HANDOFF. Figma çerçeveleri yok; görsel kabul sahipte. |

---

## 1. Bugünkü kod: tasarımı etkileyen bulgular

1. **Favoriler'de CH± çalışmıyor.** `TvViewModel.changeChannel` (743–751) kanalları `it.category == s.category` ile süzüyor. Canlı TV'de `@favorites` seçiliyken hiçbir kanalın kategorisi `@favorites` olmadığından liste boş kalıyor ve `return` ediliyor (organize.md de bunu not etmiş). Favoriler bölümünde (`onlyFavorites`) ise kategori "Tümü" olduğundan CH± bütün kanalları geziyor.
2. **Zap sırası numaralardan farklı.** `changeChannel` ham `catalog.items` sırasını kullanıyor. Numaralar ise `catalogDisplayOrder` sırasından geliyor; bu sıralama sağlayıcı ayraçlarını sona alıyor (`numberedChannels` 733, LibraryScreen 72, GuideScreen 78).
3. **Rehber tuşu yanlış kanala gidiyor.** `guide()` (424–428) `focusedId` kullanıyor; bu, kütüphaneden açılan ilk kanal. Oynatıcıda zap'tan sonra rehber tuşu eski kanalda açılıyor.
4. **`numberedChannels()` her çağrıda bütün listeyi yeniden süzüyor.** `channelNumber`, her kanal değişiminde O(n) çalışıyor. Panel satır başına numara isteyeceği için O(1) harita gerekiyor.
5. **Basılı tutulan Yukarı/Aşağı/CH± her tekrarda yeni akış açıyor.** `onPreviewKeyEvent` tekrarları da `zap()`'a gönderiyor (PlayerScreen 272–275). Her seferinde `LaunchedEffect(media)` önce `stop()`, sonra `prepare()` çalıştırıyor.
6. **Tek başına `0` bugün hata veriyor.** Rakam tamponu "0" ise 1,5 sn sonra `playChannelNumber(0)` false döner ve "0 numaralı kanal yok" yazar (149–155). `0`'ı önceki kanala ayırmak hiçbir çalışan davranışı bozmaz.
7. **Kanal değişimi bildirimi.** Kontroller kapalıyken yapılan değişim yalnız köşe notu (`flash`, 202–205) olarak görünüyor. Bilgi bandı bu dalın yerine geçer.
8. **Rehber penceresi her 15 sn değişiyor.** PlayerScreen'deki pencere `guideNow` ile oluşuyor (88), dolayısıyla her 15 sn yeni bir sorgu atılıyor. Panelde pencere başlangıcı 5 dakikaya yuvarlanır.
9. **Sol/Sağ S17'de geri sarmaya gidecek.** Canlıda kontroller kapalıyken Sol/Sağ bugün yalnız kontrolleri açıyor (286–288). PLAN S17 bu tuşları sağlayıcının HLS penceresinde geri sarmaya veriyor. Bu yüzden liste Sol'a bağlanamaz.

## 2. Kanal bağlamı: hangi kanallar listelenir

```kotlin
sealed interface ChannelScope {
    data object All : ChannelScope                                  // "Tüm kanallar"
    data class Category(val name: String) : ChannelScope            // sağlayıcı kategorisi
    data class Favorites(val category: String? = null) : ChannelScope
    // S33: data class Group(val id: String) : ChannelScope
}
```

**`LiveIndex.build(catalog, favoriteIds)`:**
- `publishLibrary` ve favori değişiminde `Dispatchers.Default` üzerinde bir kez kurulur.
- Kaynak: `state.catalog` içindeki `LIVE && !adult` girdiler. Bu katalog `CategoryVisibility.apply` sonrası olduğu için gizli kategoriler ve yetişkin filtresinin yakaladıkları zaten yoktur.
- Sıra `catalogDisplayOrder`: ayraçlar sonda, Canlı TV ile aynı.
- Tuttukları: `numbers: Map<id, Int>`, kategori başına liste, sağlayıcı kategori sırası (`categoryOrder[LIVE]` + `providerCategories`).
- `data class` değildir. Böylece TvState karşılaştırması kimlikle kısa devre yapar; büyük liste eşitliği hesaplanmaz.

**Bağlamın belirlenmesi:** `play(entry, scope)`, LIVE için `liveScope`'u şöyle yazar:

| Açılan yer | `liveScope` |
|---|---|
| Canlı TV, "Tüm kanallar" | `All` |
| Canlı TV, sağlayıcı kategorisi | `Category(ad)` |
| Canlı TV, `@favorites` · Favoriler bölümü (+kategori) | `Favorites()` · `Favorites(kategori)` |
| Ana Sayfa, rehber, Ara, son kanallar rafı | `All` |
| Rakamla kanal | `All` (numaralar global) |
| Panelden seçim | Panelde seçili bağlam |
| Önceki kanal | O kanalın kayıtlı bağlamı |
| Açılışta son kanal | `All` |

- `changeChannel`, `nextZap` ve panelin ilk listesi `live.list(liveScope)` kullanır. Bu, bulgu 1 ve 2'yi düzeltir.
- Bağlam boşalırsa (son favori çıkarıldı, kategori gizlendi) `resolve` `All` döndürür.
- **Kategori sütunu (`live.scopes()`):** "Favoriler" (her zaman; boşsa boş durum), "Tüm kanallar", ardından sağlayıcı kategorileri. Her satırda sayı; S11 sonrası kilitli kategoride kilit simgesi. Gizli kategoriler kaynakta olmadığı için görünmez.
- **S31/S33 geçişi:** organize'ın `channelContext()` işlevi `live.list(liveScope)` olur. `LiveIndex.build` sıra kaynağını `liveChannels`/`favoriteOrder`'dan alır, `Group(id)` eklenir. API değişmez.

## 3. Tuş dili, katmanlar ve OK kararı

### 3.1 Liste neden OK ile açılıyor
- **Sol olamaz:** S17'de canlı geri sarmaya gidiyor (PLAN §3-9).
- **Yukarı/Aşağı olamaz:** D064'ten beri kanal değiştiriyor.
- **Geriye OK kalıyor.** Uzaktan kumandadaki en sık eylem ve TiviMate alışkanlığı. U1 denetiminin önerisi de "OK ile kanal listesi".
- **Kontroller canlıda kayıp değil:** Duraklat için Oynat/Duraklat tuşu var; ses/altyazı ve favori kontrol satırında. Kontrollere ulaşmanın birden çok yolu korunur: basılı OK (D065'teki "basılı OK = ek eylemler" diliyle aynı), Menü, Bilgi (ikinci basış), canlı pencere yokken Sol/Sağ, listedeki "Kanallar" düğmesinin tersi. Hiçbiri tek bir tuşa bağlı değildir; bu yüzden S17 Sol/Sağ'ı alınca da kontroller erişilebilir kalır.
- **VOD'da OK değişmez.**

### 3.2 Katmanlar (T4)

```kotlin
sealed interface PlayerLayer {
    data object Video : PlayerLayer                       // hiçbir şey açık değil; bant bir katman değil, süslemedir
    data object Controls : PlayerLayer
    data class ChannelList(val column: ListColumn) : PlayerLayer   // CHANNELS, CATEGORIES, PROGRAMMES
    data object Tracks : PlayerLayer                       // S16: Options
    data object EpisodeEnd : PlayerLayer
}
enum class KeyPhase { DOWN, REPEAT, UP }
data class KeyContext(val mode: PlaybackMode, val layer: PlayerLayer, val healthy: Boolean,
    val digitsPending: Boolean, val bandVisible: Boolean, val liveWindow: Boolean /* S17 */, val hasPrevious: Boolean)
```

- PIN penceresi `AppOverlay`'dir (S05), katman değildir; kendi odağını tutar.
- `canvas.focusable(...)` ve `LaunchedEffect(visible, trackPanel, …)` odak isteği `layer == Video` koşuluna bağlanır; panel açıkken tuval odağı çalmaz.
- S14'teki `ControlsAutoHide.shouldHide`'ın `panelOpen` girdisi `ChannelList`'i de kapsar.

### 3.3 `PlayerKeyRouter` vakaları (canlı)

| Katman | Tuş | Komut |
|---|---|---|
| Video | OK `DOWN` | `Consume` (henüz eylem yok) |
| Video | OK `REPEAT` (basılı) | `ShowControls`; ardından gelen `UP` yutulur |
| Video | OK `UP` (kısa) | `OpenChannelList` (`UP` yutulur, panelin satırına tıklama gitmez) |
| Video, Controls | CH± `DOWN` | `Zap(±1)` (CH+ ve Yukarı = sonraki numara) |
| Video | Yukarı/Aşağı `DOWN`, sağlıklı | `Zap(±1)` |
| Video | Yukarı/Aşağı/CH± `REPEAT` | `ZapPreview(±1)`; `UP` → `CommitZap` |
| Video | Sol/Sağ | `ShowControls` (S17: `liveWindow` ise `Seek`) |
| Video, Controls | 1–9 · 0 (bekleyen varken) | `AppendDigit(d)` (1,5 sn, en çok 4 hane) |
| Video, Controls, ChannelList | 0 (bekleyen yokken) · `LAST_CHANNEL` | `PreviousChannel` |
| Video | `INFO` | bant kapalıysa `ShowBand`, açıksa `ShowControls` |
| Video | `MENU` | `ShowControls` |
| Video, Controls | `GUIDE` | `OpenGuide(PLAYING)` (bulgu 3) |
| ChannelList | D-pad, OK | `null` (panelin odak sistemi) |
| ChannelList | CH±, PageUp/PageDown | `PanelPage(±1)` |
| ChannelList | 1–9 | `PanelDigit(d)` (1,2 sn, rehberle aynı) |
| ChannelList | `GUIDE` | `OpenGuide(FOCUSED)` |
| Her canlı katman | Oynat/Duraklat | bugünkü gibi |
| Tracks | rakam | yutulur (bugünkü `!trackPanel`) |
| VOD | OK | `ShowControls` (`DOWN`'da, değişmez) |
| VOD | rakam, 0, `LAST_CHANNEL`, `INFO` | `null`; panel hiçbir zaman açılmaz |

**`back(ctx)`, her basışta bir katman:**
1. Alt sütun (kategori ya da mini rehber) → kanal sütunu.
2. Kanal sütunu → paneli kapat, tuval odağı.
3. Tracks → kapat.
4. Yeniden bağlanıyorsa → iptal ve çık.
5. Controls → gizle.
6. Bant görünürse → bandı gizle.
7. Hiçbiri yoksa oynatıcıdan çık.

**Yeni ipucu metni (kontrol satırı, tek satır, kırpılabilir):** "Gizliyken OK: kanal listesi · Yukarı/Aşağı: kanal · 0: önceki kanal"

**Canlı kontrol satırı:** Duraklat · **Kanallar** (ikon "list") · ‹ Kanal · Kanal › · **Önceki kanal** (yalnız varsa) · Ses / Altyazı · ★ · Kapat. Satır zaten `horizontalScroll`; 1,0 yazıda yaklaşık 880 dp ile 912 dp'ye sığar.

## 4. Panel yerleşimi (960 × 540 dp)

```
0                    400                         720                 960
┌────────────────────┬───────────────────────────┐
│ Tüm kanallar 4.290 │ Program akışı · TRT 1 HD  │
│ [↶ Önceki kanal · 7 Kanal D]                   │      video
│────────────────────│ ŞİMDİ  20:00 — 21:00      │   (küçülmez,
│ 11 [logo] TRT 1 HD●│ Ana Haber          ▬▬▬░░  │    kararmaz)
│    Ana Haber ▬▬▬░░ │ 21:00 — 22:30             │
│ 12 [logo] Kanal D  │ Dizi adı                  │
│    Dizi adı ▬░░░░  │ …                         │
│ … (7 satır)        │                           │
│ Sol: kategoriler · Sağ: program akışı          │
└────────────────────┴───────────────────────────┘
```

**Yüzey:**
- Sol kenara bağlı, tam yükseklik, sağ köşeler 24 dp (ses paneli kabuğunun aynası).
- Zemin `Ink.Background` alfa ,94. S14'teki panel saydamlığı gelirse o değer kullanılır, ancak en az ,80 (playback.md kontrast notu).
- Videonun geri kalanına karartma yok; kontrol scrim'i çizilmez.

**Kanal sütunu (1,0 yazıda 400 dp):**
- **Başlık:** üst 22 dp (köşe notuyla aynı), sol 24 dp (`TvLayout.Gutter`).
  - Satır 1: bağlam adı (18 sp Medium, D065 `displayCategory`) ve sayı ("4.290 kanal", 13 sp `Ink.Muted`).
  - Satır 2: önceki kanal varsa kompakt çerçeveli `Action("Önceki kanal · 7 Kanal D", icon = "recall")`, yoksa boş.
  - Başlık yaklaşık 94 dp.
- **Satır (`ChannelPanelRow`, en az 52 dp, aralık 4 dp; 1,0 yazıda 7 satır görünür):**
  - Numara 36 dp (14 sp `Ink.Muted`), logo 56 × 32 dp (`Artwork`, logo kipi).
  - Ad: 15 sp Medium, 1 satır, favoride "★ " öneki. Birden çok kaynak varsa sağda kaynak etiketi (12 sp, en çok 80 dp).
  - Satır 2: şimdiki program adı (13 sp), altında 3 dp ilerleme (ortak `Progress`).
  - Rehber yoksa kategori adı (D065 kuralı); rehber var ama veri yoksa "Program bilgisi yok".
- **Satır durumları:**
  - Odak: `Ink.High` dolgu ve 2 dp beyaz halka (D064 yüzey dili), ölçek 1,0.
  - Oynayan: `Ink.Selected` dolgu ve sağda mint nokta.
  - Kilitli: adın yanında kilit; satır 2 "Kilitli kanal", ilerleme yok.
- **Alt ipucu:** 12 sp `Ink.Muted`, "Sol: kategoriler · Sağ: program akışı · CH±: sayfa". 1,5 yazı ve üstünde gizlenir.

**Kategori sütunu (Sol ile):**
- 200 dp, kanal sütununun soluna açılır; kanal sütunu sağa kayar ve panel 600 dp olur.
- Öğeler dolgulu denetimdir: odakta mint dolgu, seçili bağlamda `Ink.Selected` ve mint nokta (Canlı TV `CategoryColumn` ile aynı dil).
- Sütun başlığı "Kategoriler".

**Mini rehber (Sağ ile):**
- 320 dp, kanal sütununun sağına açılır; panel 720 dp olur.
- Aynı anda en çok iki sütun açıktır: mini rehber açılırken kategori sütunu kapanır.

**Hareket:**
- Panel girişi 240 ms yatay + opaklık, çıkışı 180 ms (MOT04). Sütun açılışı 160 ms.
- Hareketi azalt açıkken anında; en çok 80 ms opaklık (MOTION azaltılmış durum).

## 5. Odak, gezinme, OK, Geri

**Açılış:**
- Bağlam `liveScope`'tur. `LazyListState(initialFirstVisibleItemIndex = max(0, i − 3))` ile 4.000 satır kaydırılmadan oynayan satıra gelinir ve odak oraya verilir.
- Oynayan kanal bağlamda yoksa odak ilk satırdadır.

**Gezinme:**
- Yukarı/Aşağı bir satır; uçta durur.
- CH±/PageUp/PageDown sayfa atlar (CH+ yüksek numaraya).
- Rakamlar köşede rozetle birikir ve 1,2 sn sonra o numaranın satırına odaklanır. Numara bağlamda yoksa bağlam `All`'a geçer, odak satırdadır. Hiç yoksa köşe notu "{n} numaralı kanal yok".

**Odak hafızası:**
- Odaktaki kimlik ve sütun düz bir tutucudadır (`ChannelPanelState`), TvState'te değil. Odak hareketi ekranı yeniden oluşturmaz (D056).
- Panel kapanıp açılınca her zaman oynayan kanala döner (öngörülebilir, testli).

**OK:**
- Başka satır → `pickChannel(id, panelScope)`. Kanal değişir, panel açık kalır, odak yeni oynayan satırdadır. Bant gösterilmez; bilgi zaten satırdadır.
- Oynayan satır → panel kapanır.
- Oynayan kanal hata durumundaysa satır 2 "Yayın açılamadı · Tekrar denemek için OK" der ve OK `recovery.retryManually()` çalıştırır.

**Basılı OK (satır):** `QuickActions`: Favorilere ekle/çıkar, Program akışı, (S11) Kilitle, Vazgeç. Kapanınca odak `refocusAfterDialog` ile satıra döner.

**Sol:** Kategori sütununu açar; odak seçili kategoridedir. Sütunda OK → bağlam değişir, sütun kapanır, odak oynayan satıra (bağlamdaysa) ya da ilk satıra gider. Sağ ya da Geri sütunu değiştirmeden kapatır.

**Sağ:** Mini rehberi açar (§6).

**Geri:** §3.3 sırası. Panel kapanınca odak tuvale gider; kontroller kapalı kalır.

**Boşta kapanma:** 15 sn girdi yoksa panel kapanır. TalkBack açıkken kapanmaz. Saf `PanelAutoClose.shouldClose(now, lastInput, timeoutMs, talkBack)`.

**Yayın hatası panel açıkken:** `LaunchedEffect(recovering, failure)` bugün kontrolleri açıyor. Panel açıkken açmaz; durum oynayan satırda görünür. Panel kapanınca kontroller hata ve "Tekrar dene" ile gelir.

**Odak hapsi:** Panel bir `Dialog` değil, aynı pencerede `focusGroup` ve `focusProperties { onExit = { cancelFocusChange() } }` ile çizilir. Böylece pencere odağı beklenmez ve video, bant ve not aynı pencerede kalır.

## 6. Mini rehber

- **Tetik:** Kanal sütununda Sağ. Odaktaki satırın kanalı için `GuideWindow(floor5(now), +12 sa)` sorgulanır; odak ilk öğeye gider.
- **Başlık:** "Program akışı" (13 sp mint) ve kanal adı (16 sp).
- **Öğe:** Yüzey; odakta beyaz halka. Saat aralığı (12 sp `Ink.Muted`, `programmeTime`) ve başlık (14 sp, en çok 2 satır). Şimdiki öğede "ŞİMDİ" etiketi ve 3 dp ilerleme. En çok 8 öğe.
- **Boş ve eksik veri:**
  - Veri yoksa `guideEmptyText(status, slice)` (mevcut metinler).
  - Çakışan programlarda `GuideSlice.now()` null döner; "Bu saatte çakışan programlar var" yazar.
- **OK:**
  - Şimdiki öğe → kanal oynamıyorsa `pickChannel`, mini rehber kapanır; oynuyorsa panel kapanır.
  - Gelecek öğe → `ProgrammeDetailDialog` (SourceDialog + RemoteTextReader, rehber ayrıntısıyla aynı kabuk). Eylemler: "Kanala geç" (birincil), "Kapat". S29 "Hatırlat", S24 arşiv öğelerini ekler.
- **Sol veya Geri:** Mini rehber kapanır, odak satıra döner.
- **Kilitli kanal (oturumda açılmamış):** Program adları gösterilmez. "Kilitli kanal. Programları görmek için kanalı PIN ile aç." ve `Action("Kanalı aç")` → PIN → oynatma.
- **Odak takibi yok:** Mini rehber odak hareketini izlemez, yani Yukarı/Aşağı ile sorgu atılmaz.

## 7. Bilgi bandı (TV-053)

- **Ne zaman:**
  - `ChangeOrigin` ZAP, NUMBER, PREVIOUS ya da ZAP_PREVIEW olduğunda; ayrıca kontroller ve panel kapalıyken.
  - `INFO` tuşu bandı yeniden gösterir.
  - Panelden (LIST) ve kontrol düğmesinden (CONTROLS) yapılan değişimde gösterilmez; bilgi zaten ekrandadır.
  - PlayerScreen'deki `quiet: Boolean` yerine `origin: ChangeOrigin?` gelir. Köşe notu rakam ve sarma için kalır.
- **İçerik (`ChannelBandModel.from(entry, number, slice, now, zone, sourceLabel)`, saf):**
  - Logo 72 × 40 dp.
  - "11  TRT 1 HD": numara `Ink.Muted`, ad 24 sp Medium (kontrollerdeki başlıkla aynı).
  - "Ana Haber · 20:00 — 21:00" (14 sp) ve 4 dp ilerleme.
  - "Sonra · 21:00 Dizi adı" (12 sp `Ink.Muted`).
  - Sağda birden çok kaynak varsa kaynak adı.
  - Açılırken `LoadingArc` ve "Açılıyor…". Bu sırada ortadaki "Yayın açılıyor…" hapı bant görünürken çizilmez.
  - Rehber yoksa kategori; veri yoksa "Program bilgisi yok". Kilitli kanalda (PIN sonrası açıksa normal) program adı yok.
  - Program satırlarının yeri ayrılır; rehber yanıtı gelince bant zıplamaz.
- **Yer:**
  - Sol alt: kenar 24 dp, alt 18 dp, en çok 640 dp. Kontrollerin alt scrim'inin hafif hâli üzerinde (bandın arkasında 0 → ,82). D064: opak panel yok.
  - Bant görünürken altyazı alt boşluğu kontrollerdeki gibi yükselir (playback.md §3.1).
- **Süre ve hareket:**
  - 4 sn; her yeni değişim süreyi sıfırlar. Geri yalnız bandı kapatır.
  - Giriş 120 ms, çıkış 100 ms opaklık (MOT08/MOT11). Hareketi azalt açıkken anında.
  - PiP'te (S46) çizilmez.
- **Basılı zap önizlemesi:**
  - `REPEAT` evresinde hedef `zapTarget(previewId, dir)` ile ilerler ve bant hedefi gösterir: numara, ad, şimdiki program. Satır başına tek EPG sorgusu, 150 ms gecikmeyle.
  - Akış açılmaz. `UP` → `pickChannel(previewId, liveScope)`.
  - Önizlemede kilitliler atlanır (`nextZap`).

## 8. Önceki kanal

- **`LiveRecall` (bellek, saf):** `played(pick)`, LIVE bir `play` geldiğinde ve kimlik değiştiğinde `previous = current; current = pick` yapar. VOD dokunmaz. Kanallar arasında film izlemek önceki kanalı silmez. Kalıcı değildir.
- **Erişim:**
  - Bekleyen rakam yokken `0`: hemen, 1,5 sn beklemeden. Numaralar 1'den başladığı için baştaki 0 anlamsızdır; "05" artık 5 değildir, bilinçli bedel.
  - `LAST_CHANNEL` tuşu.
  - Panel başlığındaki eylem. Odak oraya ilk görünen satırdan Yukarı ile gider.
  - Kontrol satırındaki "Önceki kanal".
- **Sonuçlar:**
  - Açıldıysa önceki ve şimdiki yer değiştirir (toggle).
  - Önceki yoksa köşe notu "Önceki kanal yok".
  - Artık bulunmuyorsa (gizlendi, kaynak kaldırıldı) "Önceki kanal artık listede yok."
  - Kilitliyse PIN istenir; mevcut yayın PIN'e kadar sürer.

## 9. Son kanal ve "Açılışta son kanalı aç"

**Kayıt:** `recordViewed` yalnız gerçekten oynamaya başlayınca çalışır (`onIsPlayingChanged(true)`). LIVE ise `store.lastLive(source.id, LastLive(localId, now))` yazar.
- Neden geçmiş listesi değil: `WatchHistory.LIMIT = 50` film ve dizilerle paylaşılıyor; kanal oradan düşebilir.
- Değerde akış adresi yok, yalnız yerel kimlik var.

**Karar (`AutoStart.decide`, saf):** Yalnız `restore(atLaunch = true)` başarılı olunca çalışır. Kurtarmadaki "Yeniden dene" (`atLaunch = false`) çalıştırmaz.

| Koşul | Sonuç |
|---|---|
| Ayar kapalı · derin bağlantı (S40) var · hiç `lastLive` yok | Ana Sayfa (bugünkü) |
| En yeni `at` → kanal `live` içinde yok (gizlendi, yetişkin, kaynak silindi) | Ana Sayfa, sessiz |
| Kapı `NeedsPin` (kilitli) | Ana Sayfa. Açılışta PIN istemi açılmaz. |
| "Açılışta PIN iste" açık (`appLocked`) | `pendingAutoStart = id`; kilit ekranı doğru PIN'le açılınca oynar |
| İzinli | `route = PLAYER`, `playerReturn = HOME`, `liveScope = All` (Ana Sayfa hiç çizilmez) |

**Açılış kapağıyla etkileşim:**
- `PilotApp`, `LocalLaunchCover provides opening` sağlar. PlayerScreen kapak varken `prepare()` eder ama `playWhenReady = false` tutar. Kanal splash sırasında tamponlanır ve kapak kalkınca anında başlar.
- `MainActivity`'deki `endOpening` rota PLAYER ise `startupSound.stop()` çağırır; açılış sesi yayının üstüne binmez.
- D069 hareketsiz kapağında (splash yok) rota LOADING'den doğrudan PLAYER'a geçer; kapak rota değişince kalkar.

**Ana Sayfa ve HOME tuşu:**
- Geri → Ana Sayfa; odak D070'e göre vitrinde.
- HOME tuşu ve dönüş (süreç canlı) bugünkü gibi aynı kanalda sürer; yeniden karar yok.
- Süreç öldürüldüyse ya da çıkış sorusuyla kapatıldıysa sonraki açılış yeniden karar verir.

**Ayar satırı:** S14 Oynatıcı sayfası → "Canlı TV" grubu.
- `ToggleRow("Açılışta son kanalı aç", "NEXTV açılınca en son izlediğin canlı kanal başlar; Geri ile Ana Sayfa'ya dönersin. Kilitli kanallar kendiliğinden açılmaz.")`
- Rol Switch, durum Açık/Kapalı.

## 10. VOD

Film ve bölümde panel, bant, önceki kanal, `0`/`LAST_CHANNEL`/`INFO` yoktur. OK, Sol/Sağ sarma ve FF/RW değişmez. Kontrol satırına "Kanallar" eklenmez. Router'ın VOD vakaları JVM'de korunur.

## 11. Kilitli kanallar (pin.md ile)

- **Panelde:** Kilitli kanallar listelenir, rozetlidir; program adı yerine "Kilitli kanal" yazar (pin.md §4). Satır açıklaması "{ad}, kilitli".
- **Açma:** OK, önceki kanal ya da mini rehberdeki "Kanalı aç" → `request(AccessTarget.ChannelPick(id, scope, origin))`.
  - `NeedsPin` → PIN penceresi (AppOverlay). Mevcut akış durmaz, yeni akış PIN'den önce açılmaz.
  - Vazgeç → odak satıra döner. Doğru PIN → hedef güncel katalogdan yeniden çözülür ve oynar.
- **CH±, basılı önizleme ve rakam:** `nextZap` kilitliyi atlar. Rakamla kilitliye gidince PIN istenir (pin.md §3.7).
- **Arka plan:** `onStop`'ta oturum temizlenir; kilitli yayındaysa oynatıcıdan çıkılır (pin.md §3.9). Panel bu yola karışmaz.
- **Açılış:** Otomatik açılış kilitli kanalı açmaz (§9).
- **Kapı vakaları (`AccessGateTest`):** ChannelPick gizli → `Deny`, yetişkin → `Deny`, kilitli/izinsiz → `NeedsPin`, kilitli/oturumda açık → `Allow`, kaynak yok → `Deny`.

## 12. Gizli ve yetişkin içerik

Liste, kategori sütunu, bant, önceki kanal ve otomatik açılış `state.catalog` (görünürlük uygulanmış) ve `!adult` üzerinden çalışır. Gizli kategoriler hiçbir yüzeyde görünmez. Ayarlarda bir kategori gizlenince `publishLibrary` yeni bir `LiveIndex` kurar; panel açıksa liste güncellenir. Odaktaki satır kalktıysa aynı sıradaki komşuya geçilir (kütüphanedeki `recoverRemoved` deseni).

## 13. TalkBack

- **Tuval (Video katmanı):** Bugün semantiği yok.
  - `contentDescription = "Canlı yayın, {no} {ad}. OK kanal listesini açar."`
  - `onClick("Kanal listesi")`; özel eylem "Oynatıcı kontrolleri" (TalkBack'te basılı OK yerine).
- **Panel:** `paneTitle = "Kanal listesi"`, `isTraversalGroup`.
- **Satır:**
  - `contentDescription = "{no}, {ad}, {program ya da kategori}"`.
  - `stateDescription`: "Oynatılıyor" / "Kilitli" / "Favori" birleşimi.
  - `onClick("Kanala geç")`; özel eylem "Program akışı".
- **Kategori öğesi:** `selected`, "{ad}, {n} kanal[, kilitli]".
- **Mini rehber öğesi:** "{saat aralığı}, {başlık}[, şimdi]".
- **Bant:** `liveRegion = Polite`, tek açıklama: "{no}, {ad}, şimdi {program}, {saat}". Odak almaz.
- **Kapanma:** TalkBack açıkken panel ve kontroller kendiliğinden kapanmaz (playback.md kuralı).

## 14. Büyük yazı

| Ölçek | Kanal sütunu | Kategori | Mini rehber | Satır | Not |
|---|---|---|---|---|---|
| 1,0 | 400 dp | 200 dp | 320 dp | en az 52 dp, 7 satır | alt ipucu görünür |
| 1,5 | 440 dp | 232 dp | 320 dp (en çok 760 dp) | en az 72 dp, 2. satır tek satır | alt ipucu gizli |
| 2,0 | 520 dp | — | — | en az 92 dp | **tek sütun:** alt sütunlar kanal sütununun yerine açılır, başlıkta iz ("Kategoriler" / "Program akışı · {ad}") |

- Numara sütunu ölçekle genişler (`hasVisualOverflow` testi). Başlık sayısı kırpılabilir; bağlam adı önce gelir.
- Bant 1,5'te iki satıra kadar büyür; 2,0'da "Sonra" satırı gizlenir.

## 15. Performans (4–5 bin kanal)

- **Tek seferlik hazırlık:** `LiveIndex` yayında bir kez kurulur. Numara O(1); bağlam listesi hazır liste ya da önbellekli favori alt kümesidir. Panel açılışında sıralama veya süzme yapılmaz.
- **LazyColumn:** `key = id`, `contentType`, sabit en az yükseklik. İlk konum `initialFirstVisibleItemIndex` ile verilir; kaydırma animasyonu yok. Satırda `indexOf` yok.
- **EPG yalnız görünen satırlar için:**
  - `derivedStateOf { visibleItemsInfo }` görünen ± 3 satırı ve oynayan kanalı verir.
  - 150 ms gecikmeyle `guideRows(…, GuideWindow(floor5(now), +6 sa))`.
  - Sonuçlar panel oturumu boyunca bir haritada birleşir; geri kaydırınca satırlar "bilgi yok"a dönüp yanıp sönmez. Harita `guideRevision` ya da `guideZoneId` değişince temizlenir.
- **Mini rehber:** tek kanal, tek sorgu.
- **Odak:** Odak durumu satırın kendi `onFocusChanged`'ında kalır; ekran yeniden oluşmaz.
- **Ölçüm (geçme ölçütü değil, sonda):** 4.500 sentetik kanalla panel açılışından ilk kareye, 10 sn basılı Aşağı'da geciken kare oranı (MOTION hedefi <%1). LargeCatalogProbeTest deseni. Fiziksel cihaz p95'i K2/K3 turunda.

## 16. Tek oynatma oturumu

- Her seçim `play()` ile `PlayingMedia`'yı değiştirir. Mevcut `LaunchedEffect(media)` önce `save()`, `player.stop()`, `clearMediaItems()` çalıştırır, sonra yenisini açar (PlayerScreen 194–217). Tek ExoPlayer korunur.
- Panelde odak hareketi akış açmaz (önizleme yok).
- Basılı zap yalnız bırakınca açar (karar 6). Rakam 1,5 sn bekler.
- PIN istemi sırasında yeni akış açılmaz.
- Mini rehber ve bant ağ akışı açmaz.
- Otomatik açılış tek bir akış hazırlar; açılış sesi dışında başka bir medya yoktur.

## 17. Durumlar

| Durum | Panel | Bant |
|---|---|---|
| EPG yükleniyor | Satır 2'de `guideEmptyText` → "Rehber yükleniyor…"; yer ayrılmış | Program satırları boş yer tutucu |
| Boş bağlam | "Bu kategoride kanal yok" ve `Action("Tüm kanallar")` | — |
| Favori yok | "Favori kanalın yok" ve Not "Bir kanalda OK'yi basılı tutup Favorilere ekle'yi seçebilirsin." ve "Tüm kanallar" | — |
| Oynayan kanal hata/yeniden bağlanıyor | Oynayan satırda "Yayın açılamadı · Tekrar denemek için OK" / "Yeniden bağlanılıyor…" | Kontroller hata ile açılır, bant çekilir |
| Logo yok | `Artwork` harf yedeği | aynı |
| Rehber yok | Kategori adı | Kategori adı |
| Erişim kısıtı | Kilit rozeti ve PIN (§11) | PIN sonrası normal |

## 18. API

```kotlin
// TvState eklemeleri
val live: LiveIndex = LiveIndex.EMPTY
val liveScope: ChannelScope = ChannelScope.All
val previousLiveId: String? = null
val startLastChannel: Boolean = false
val pendingAutoStart: String? = null           // appLocked iken

// TvViewModel (ince delege)
fun channelNumber(entry: MediaEntry): Int?                       // live.number
fun zapTarget(fromId: String?, direction: Int): MediaEntry?      // live.list(liveScope) + nextZap
fun changeChannel(direction: Int)                                // zapTarget → pickChannel(origin = ZAP)
fun playChannelNumber(number: Int): Boolean                       // scope All; kilitliyse istem, true
fun pickChannel(id: String, scope: ChannelScope, origin: PickOrigin = PickOrigin.LIST): PickResult
fun previousChannel(): PickResult
fun startLastChannel(enabled: Boolean)
fun guide()                                                      // PLAYER'dan: oynayan ya da paneldeki odak kimliği
fun play(entry: MediaEntry, restart: Boolean = false, scope: ChannelScope? = null)
enum class PickResult { STARTED, ALREADY_PLAYING, NEEDS_PIN, UNAVAILABLE }

// T3
data class ChannelPick(val id: String, val scope: ChannelScope, val origin: PickOrigin) : AccessTarget
enum class PickOrigin { LIST, ZAP, PREVIOUS, AUTOSTART }

// T4 komutları
sealed interface PlayerCommand { /* mevcutlar + */ data object OpenChannelList; data object ShowControls
    data class Zap(val direction: Int); data class ZapPreview(val direction: Int); data object CommitZap
    data class AppendDigit(val digit: Int); data object PreviousChannel; data object ShowBand
    data class PanelPage(val direction: Int); data class PanelDigit(val digit: Int)
    data class OpenGuide(val target: GuideTarget); data object Consume }
```

`back()`: PLAYER → LIBRARY dönüşünde LIVE oynuyorsa `category = liveScope.libraryKey`, `focusedId = playing.id` (karar 7).

## 19. Kalıcılık anahtarları (`DeviceKeys`)

| Anahtar | Kapsam | Sıfırlamada | Yedek sınıfı | Değer | Silindiği yerler |
|---|---|---|---|---|---|
| `lastLive:<sourceId>` | SOURCE | silinir | PERSONAL | `{"id":"<yerel kimlik>","at":<ms>}` | `saveSources` (kaldırılan kaynak), `forgetSource` (önek), `clearHistory(source)` (karar 8) |
| `startLastChannel` | DEVICE | kalır (ses ve hareket tercihleri gibi) | SETTINGS | Boolean, varsayılan `false` | — |

Önceki kanal, panel bağlamı ve odak yalnız bellekte tutulur.

## 20. Metinler (Türkçe, string kaynağına)

- **Panel:**
  - "Kanal listesi" (paneTitle), "{n} kanal", "Kategoriler", "Favoriler", "Tüm kanallar".
  - "Önceki kanal · {no} {ad}", "Sol: kategoriler · Sağ: program akışı · CH±: sayfa".
  - "Kilitli kanal", "Program bilgisi yok", "Yayın açılamadı · Tekrar denemek için OK", "Yeniden bağlanılıyor…", "Açılıyor…".
  - "Bu kategoride kanal yok", "Favori kanalın yok".
- **Mini rehber:**
  - "Program akışı", "ŞİMDİ", "Bu saatte çakışan programlar var".
  - "Kilitli kanal. Programları görmek için kanalı PIN ile aç.", "Kanalı aç".
  - "Program ayrıntısı", "Kanala geç", "Kapat".
- **Bant:** "Sonra · {saat} {program}", "Açılıyor…".
- **Notlar:** "Önceki kanal yok", "Önceki kanal artık listede yok.", "{n} numaralı kanal yok" (mevcut).
- **Kontroller:** "Kanallar", "Önceki kanal", "Gizliyken OK: kanal listesi · Yukarı/Aşağı: kanal · 0: önceki kanal".
- **TalkBack:** "Canlı yayın, {no} {ad}. OK kanal listesini açar.", "Kanala geç", "Program akışı", "Oynatıcı kontrolleri", "Oynatılıyor".
- **Ayar:** "Açılışta son kanalı aç" ve açıklaması (§9).
- **Ebeveyn denetimi notu (pin.md):** değişmez, zaten "listeden PIN ile açılır" diyor.

## 21. Testler

**JVM · `PlayerKeyRouterTest` (yeni vakalar):**
1. `okDownOnLiveVideoIsConsumedWithoutCommand`
2. `shortOkReleaseOpensChannelList`
3. `heldOkShowsControlsAndSwallowsRelease`
4. `okOnVodVideoStillShowsControlsOnKeyDown`
5. `okInsideControlsAndListIsLeftToFocus`
6. `upIsNextNumberDownIsPreviousOnLiveVideo`
7. `upDownDoNotZapWhileUnhealthyButChannelKeysDo` (bugünkü `healthy` kuralı)
8. `heldZapKeysPreviewAndReleaseCommits`
9. `channelKeysZapFromVideoAndControls`
10. `channelAndPageKeysPageInsideList`
11. `dpadInsideListIsLeftToFocus`
12. `digitsAppendInVideoAndControlsAndJumpInList`
13. `zeroWithoutPendingDigitsIsPreviousChannel`
14. `zeroAfterADigitAppends` ("1","0" → 10)
15. `lastChannelKeyIsPreviousInEveryLiveLayer`
16. `digitsZeroLastChannelAndInfoIgnoredOnVod`
17. `infoShowsBandThenControls`
18. `menuShowsControls`
19. `leftRightShowControlsWithoutLiveWindow` (S17 için `liveWindow = true` → Seek, şimdilik @Ignore değil, bekleyen vaka)
20. `guideOpensPlayingFromVideoAndFocusedFromList`
21. `listNeverOpensOnVodOrEpisode`
22. `tracksLayerSwallowsDigits`
23. `episodeEndLayerUnchanged`
24. `backPopsSubColumnListTracksRecoveringControlsBandThenLeaves`
25. `backWhileRecoveringClosesListFirst`

**JVM · diğer:**
- **`ChannelScopeTest`:**
  - Numaralar "Tüm kanallar" sırasında, ayraçlar sonda.
  - Kategori ve favori listeleri; `Favorites(kategori)`.
  - Gizli ve yetişkin hiç yok; kaybolan kategori `All`'a düşer.
  - `@favorites` bağlamında zap çalışır (bulgu 1 regresyonu); zap sırası numara sırası (bulgu 2).
  - S10 sonrası kilitliyi atlama.
- **`LiveRecallTest`:** Değiş tokuş; aynı kanal etkisiz; VOD dokunmaz; bulunamayan önceki.
- **`AutoStartTest`:** §9 tablosunun her satırı; kaynaklar arasında en yeni `at`; bozuk codec → Ana Sayfa; `atLaunch = false` → Ana Sayfa.
- **`ChannelBandModelTest`:** Şimdi/sonra; çakışma; rehbersiz → kategori; bilgi yok; kilitli; kaynak etiketi.
- **`PanelPagingTest` ve `PanelAutoClose`:** Sayfa = görünen − 1, uçta sınır; TalkBack'te kapanmama.
- **`AccessGateTest`:** ChannelPick vakaları (§11).
- **`DeviceKeysTest`:** `lastLive` kapsam ve temizlik.

**Enstrümantasyon · `ChannelListFlowTest`:**
- **Ortam:** Ayrı test emülatörü, seri kimliğiyle; 5554 ve 5556 değil. Toplu `connectedAndroidTest` çalıştırılmaz.
- **Veri:** Sentetik M3U: 3 kategori, 12 kanal, bir ayraç, bir yetişkin ve bir gizli kategori. GuideFlowTest desenindeki yerel XMLTV.

| Test | Doğrular |
|---|---|
| `okOpensListOnPlayingChannel` | `channel-row:<id>` odakta ve durumu "Oynatılıyor" |
| `okOnAnotherRowSwitchesAndKeepsListSecondOkCloses` | Seçim paneli kapatmaz, ikinci OK kapatır |
| `backClosesOnlyTheList` | Rota PLAYER kalır |
| `leftOpensCategoriesFavoritesScopeDrivesChannelKeys` | Favoriler'den seçimden sonra CH+ favoride kalır |
| `rightShowsMiniGuideLeftReturns` | Gelecek program ayrıntısı; "Kanala geç" |
| `channelKeysPageTheList` | CH± sayfa atlar |
| `digitsJumpInsideList` | Rakam odaklar, oynatmaz |
| `bandAfterZapShowsNumberNameAndHides` | 4 sn sonra kalkar; panelden seçimde bant yok |
| `zeroReturnsToPreviousChannel`, `oneThenZeroOpensChannelTen` | `0` kuralı |
| `heldDownPreviewsWithoutOpeningUntilRelease` | Oynayan kimlik tuş bırakılana kadar değişmez; KeyEvent `repeatCount` ile enjekte |
| `longOkShowsControls` | Basılı OK kontrolleri açar |
| `vodHasNoListOrBand` | VOD'da ikisi de yok |
| `hiddenAndAdultChannelsNeverListed` | Gizli ve yetişkin hiç görünmez |
| `layoutAtFontScale15And20` | Sınırlar; 2,0'da tek sütun; numara kırpılmaz; odaklı satır tam görünür |
| `talkBackSemantics` | paneTitle, satır açıklaması, bandın liveRegion'ı, tuval onClick |
| `startOnLastChannelOpensPlayerAndBackGoesHome` | Eski ViewModel temizlenerek soğuk açılış (CatalogColdLaunchTest deseni) |
| `backToLibraryFocusesLastChannel` | Karar 7 |

**Diğer enstrümantasyon ekleri:**
- `GuideFlowTest`: `guideKeyAfterZapOpensPlayingChannel` (bulgu 3).
- `ParentalFlowTest` (S10 sonrası): listede kilitli satır → PIN, mevcut yayın kimliği değişmez; Vazgeç satıra döner; CH± ve basılı önizleme atlar; otomatik açılış kilitli kanalda Ana Sayfa'yı açar.
- `PlaybackRecoveryFlowTest`: kanal değişiminde köşe notu yerine bant beklenir.

**Elle (5556, veri silmeyen):**
- Gerçek "Evim" (4.290 kanal) ile panel açılışı ve 10 sn basılı Aşağı. EPG satırları, logolar, mini rehber, bant, `0`.
- Ayar açık → uygulamayı durdur → yeniden aç: kapaktan sonra kanal başlar, ses üst üste binmez.
- Fiziksel TV (K3, kullanıcı isterse): basılı OK süresi, kumandada `LAST_CHANNEL`/`INFO`/`MENU` var mı, basılı zap.

## 22. Riskler

1. **OK davranış değişikliği.** 0.14.41'den beri OK'ye kontrol olarak alışan kullanıcıyı şaşırtabilir. Azaltma: ipucu metni, "Kanallar" düğmesi, kontrollere çoklu yol, karar kaydı. Sahip karar 1'i reddederse §3.3'te yalnız 3 satır değişir.
2. **Basılı OK bazı kumandalarda gelmeyebilir.** Bazıları `DPAD_CENTER` tekrarı göndermez, ya da `ENTER` gönderir. O zaman OK hep listeyi açar. Kontroller Menü, Bilgi ve Sol/Sağ ile erişilebilir kalır. K3'te doğrulanmalı.
3. **KeyUp sızıntısı.** Liste KeyUp ile açılıp o KeyUp yutulmazsa ilk satıra tıklanıp kanal değişir. Kök `onPreviewKeyEvent` KeyUp'ı tüketmeli; bunun için ayrı bir test var.
4. **S17 ile Sol/Sağ.** Kontrollere erişim yalnız Sol/Sağ'a bağlanırsa S17'de kaybolur. Router testi 19 bunu korur.
5. **organize S31 ile çift kavram.** S31 ikinci bir `channelContext` yazarsa CH± ve liste ayrışır. HANDOFF'a "S31 `ChannelScope`/`LiveIndex`'i genişletir" notu düşülmeli.
6. **Rehber ızgarası sırası.** S31'e kadar ham sırada kalır, panel ise Canlı TV sırasındadır. Ayraçlı listelerde rehber ile panel sırası farklı görünür. Kabul edilir; organize düzeltir.
7. **Hızlı ardışık seçimler.** Tek bağlantılı sağlayıcıda eski bağlantı sunucu tarafında geç kapanırsa yeni istek 429 alabilir; kurtarma 429'u otomatik denemez. Önizleme ve bırakınca açma bunu azaltır; panelde odak akış açmaz.
8. **Soğuk açılışta beklenmedik ses ve bağlantı.** Bu yüzden varsayılan kapalıdır. Kapak sırasında tamponlama, kaynak yenilemesiyle aynı anda ağ kullanır.
9. **Bandın köşe notunun yerine geçmesi.** Notu bekleyen cihaz testleri güncellenmeli.
10. **Odak hapsi.** Panel `Dialog` olmadığı için odak tuvale ya da kontrollere kaçabilir. `onExit` iptali ve `layer == Video` koşulları test edilmeli.
11. **Doğrulanamayanlar.** TiviMate'in liste davranışı (OK'de açık kalma, önceki kanal tuşu) üçüncü taraf bilgisine dayanıyor (TIVIMATE_GAP: "Önceki kanal tuşu TiviMate'te doğrulanamadı"). 65U95A kumandasında `LAST_CHANNEL`/`INFO` tuşlarının varlığı bilinmiyor.

## 23. Belgeler ve kayıt

- **SCREENS:**
  - TV-052 "Oynatıcı kanal listesi". İlk odak oynayan kanal; OK değiştirir ve açık kalır, oynayanda kapanır; Geri bir sütun, sonra panel.
  - TV-053 "Kanal bilgi bandı". Odak almaz; 4 sn; Geri gizler.
  - TV-050 satırına "kısa OK: kanal listesi; basılı OK: kontroller" eklenir.
- **NATIVE_UI:** Oynatıcı bölümüne §4, §7 ve §14 ölçüleri.
- **DECISIONS:** Sıradaki boş numarayla "öneri" olarak işaretli kayıt. İçerik: OK dili, `0` = önceki kanal, basılı zap, "Açılışta son kanalı aç" varsayılanı, zap bağlamı ve Favoriler'de CH± düzeltmesi. D064'teki "kontroller kapalıyken OK kontrolleri açar" ifadesinin canlı için yerine geçtiği yazılır.
- **BACKLOG:** BKL-032 (S15). **STATUS/HANDOFF:** Her aşama sonunda.
- **Figma:** Çerçeve yok. TV-052/053 için FIGMA.md sürecine göre çerçeve ve manifest kaydı görsel kabulden önce gerekir; bu çalışmada yapılmadı.