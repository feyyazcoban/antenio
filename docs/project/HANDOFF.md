# Son devir notu

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
