# Kişisel kütüphane düzeni ve kaynak bağlantısı: favori grupları, özel kategori/kanal sırası, yeniden adlandırma, tek içerik gizleme, kaynak başına User-Agent/Referer/HTTP başlıkları, zamanlanmış liste ve EPG güncellemesi

18 Ekim 2026 · TiviMate gap tasarımı (`organize`), salt okunur tasarım ajanı; öneriler kullanıcı kararı değildir.

**Özet:** Not: aktarılan kullanıcı isteği fiyatlama, satış stratejisi, yabancı diller ve farklı ülkelerden satın alma hakkında. Bu tasarım ise iş akışının verdiği TiviMate eksik kümesini kapsıyor. İsteğe değdiği tek yerler şunlar: 3 numaralı sahip kararı (bu kümenin ücretsiz mi ücretli katmanda mı olacağı) ve yerelleştirmeye hazır olma kuralı.
Tasarım, gizlenmiş içerik, özel adlar, özel sıralar ve favori gruplarını device_state içinde tek bir sürümlü JSON belgesinde (`library:v1`) topluyor. Her kayıt kaynak kimliği ile yerel kimliği (ContentRef) ve sağlayıcı adından türetilen bir yedek anahtarı birlikte tutuyor; böylece M3U adresleri değişse de düzenlemeler katalog yenilemesinden sağ çıkıyor. Kaynak kaldırılınca bu kayıtlar, mevcut `f:` temizliği gibi `saveSources` içinde aynı kayıtta (commit) siliniyor. Bugünkü tek favori listesi ilk açılışta "Favoriler" varsayılan grubuna taşınıyor. Tek grup varken davranış hiç değişmiyor.
Yeniden sıralama, basılı OK menüsündeki "Taşı" ile açılan bir taşıma kipiyle yapılıyor: Yukarı/Aşağı yer değiştirir, OK bırakır, Geri vazgeçer. Kanal numaraları, kanalın kendi kategorisinin numara aralığı içinde yeni sıraya uyuyor. Kaynak başına UA, Referer ve en çok 3 ek başlık şifreli kayıt defterinde saklanıyor. Liste ve EPG için uygulamanın içinde, yalnız ön planda ve oynatma yokken çalışan bir zamanlayıcı öneriliyor; yeni bağımlılık yok.
Toplam efor yaklaşık 11,5 mühendis-günü. Altı aşamanın her biri tek başına yayınlanabilir.

**Tahmini iş:** 11.5 gün

**Aşamalar:**
1. 1 · Kaynak başına bağlantı ayarları: SourceConnection (şifreli kayıt defterinde), RequestHeaders, liste/EPG/oynatma isteklerinde kullanım (EPG'de host kapsamı), Kurulum'da Bağlantı ayarları diyaloğu, Bilgiler satırı, JVM ve MockWebServer testleri (1,5 gün)
2. 2 · Otomatik güncelleme: RefreshInterval tercihleri, uygulama içi zamanlayıcı (ön planda, oynatma/düzenleme/işlem yokken, ağ doğrulanmış), restore'daki sabit 6 saatin yerine aralık, GuideController bayatlığı, Ayarlar → Otomatik güncelleme, sessiz başarı ve dikkat çekmeyen hata (1,5 gün)
3. 3 · Temel ve geçiş: library:v1 modeli, codec, kilitli yazım, saveSources/forgetSource budaması; tek favori listesinin varsayılan gruba taşınması; CustomIndex (ref + alt), LibraryViews, liveChannels, channelContext (rehber sırası ve Favoriler'de CH± düzelir), displayName tesisatı, sanal kategorilerin yayında korunması; yeni UI yok (2 gün)
4. 4 · Tek içerik gizleme ve yeniden adlandırma: QuickContext ile genişleyen hızlı menü, kategori sütununda basılı OK menüsü, ad ve gizleme diyalogları, iki adla arama, Ayarlar → Gizlenen içerikler (1,5 gün)
5. 5 · Favori grupları: R1/R2 yıldız kuralı, grup seçici, Yeni gruba ekle, Favoriler sütununda gruplar ve tür bölümlerinde salt okunur grup kategorileri, Ayarlar → Favori grupları, grup sırasıyla ana sayfa rafı (2 gün)
6. 6 · Taşıma kipi: kategori, kanal (önek modeli, kategori yuvası numaralandırması, Başa taşı), grup ve grup öğesi taşıma; TalkBack canlı bölge, büyük yazı; Ayarlar → Sıralamayı ve adları sıfırla; ardından büyük yazı/TalkBack/enstrümantasyon dengelemesi (2 + 1 gün)

**Kullanıcı kararları (varsayılanla):**
- Liste ve rehber, uygulama kapalıyken de (WorkManager ile, TV bekleme modundayken) güncellensin mi? → varsayılan: Hayır. Güncelleme yalnız uygulama açıkken ve oynatma yokken uygulama içi zamanlayıcıyla yapılsın; varsayılan aralık iki ayar için de 6 saat (bugünkü davranış). Yeni bağımlılık eklenmez, bekleme modunda ağ ve şifre çözme işi olmaz.
- Bağlantı ayarlarında hazır User-Agent seçenekleri (başka uygulamaların adlarıyla, ör. VLC, Kodi) sunulsun mu? → varsayılan: Hayır. Yalnız 'Varsayılan (NEXTV)' ve serbest metin alanı olsun. Başka ürün adlarını taklit eden hazır seçenekler marka ve 'kısıtlama aşma' algısı riski taşır (PLAY_STORE_READINESS 3.2).
- Bu küme (birden çok favori grubu, elle sıralama, yeniden adlandırma, bağlantı ayarları, varsayılan dışı güncelleme aralığı) ücretsiz mi kalsın, ileride ücretli katmana mı alınsın? (PRD §12 iş modeli kararıyla bağlantılı) → varsayılan: Şimdilik kapısız, ücretsiz uygulansın. İş modeli kararı verilince kapı noktaları belli: ikinci grup oluşturma, Taşı, Yeniden adlandır, Bağlantı ayarları ve aralık seçimi. Hepsi TvViewModel'de tek yerden geçtiği için sonradan eklenebilir. Tek grup, gizleme ve varsayılan güncelleme her durumda ücretsiz kalsın.
- Bir kategori içinde kanal sırası değiştirilince kanal numaraları da değişsin mi? → varsayılan: Evet, yalnız o kategorinin numara aralığında: kategori içinde numaralar artan sırada kalır, diğer kategorilerin numaraları değişmez; Canlı TV, rehber, rakamla kanal açma ve CH± aynı sırayı kullanır.

**Riskler:**
- Alt (başlık) anahtarıyla yeniden bağlama, sağlayıcı iki kanalın adını takas ederse veya kanal adını başka bir kanala verirse düzenlemeyi yanlış kanala taşıyabilir; yalnız ref çözülemediğinde ve alt benzersizken kullanıldığı için risk sınırlı.
- Kategori adı ve sırası D068 gibi sağlayıcı kategori adıyla anahtarlanır; sağlayıcı kategoriyi yeniden adlandırırsa özel ad ve sıra düşer.
- Media3 DefaultHttpDataSource'un User-Agent'ı istek özelliklerinden sonra yazdığı varsayımı (bugün M3U satırındaki UA'nın ezildiği) Media3 1.11.1 kaynağında doğrulanmadı. Tasarım UA'yı ayrı geçirerek bu varsayıma bağımlılığı kaldırıyor, ama mevcut davranış hakkındaki iddia doğrulanmalı.
- Özel Referer, Cookie ve ek başlıklar OkHttp tarafından başka hosta yönlendirmede de taşınır (Authorization hariç); üçüncü taraf hosta sızma olasılığı var. EPG'de host kapsamı var, katalog ve oynatma yönlendirmelerinde yok.
- Görünür davranış değişiklikleri: 'Tüm favoriler' katalog sırası yerine grup sırasında, yeni favori sona ekleniyor; rehber kanal listesi ham sıra yerine Canlı TV sırasında; liste/EPG isteklerinin UA'sı 'NEXTV/<sürüm> AndroidTV' oluyor. Bunların kullanıcıya duyurulması ve karar kaydı gerekir.
- Yalnız ön planda çalışan zamanlayıcı, saatlerce canlı yayın izleyen kullanıcıda güncellemeyi oynatıcıdan çıkılana kadar erteler; rehber penceresi 9 gün olduğu için içerik bozulmaz ama 'Son güncelleme' eski görünür.
- device_state içindeki library:v1 belgesi büyük kataloglarda büyüyebilir (her değişiklikte tamamı yeniden yazılır); sınırlar (512 KB, öğe sınırları) en kötü durumu sınırlar ama SharedPreferences yükü izlenmeli.
- Taşıma kipindeki tuş yakalama; tv-material Card onClick/onLongClick, LibraryBack ve ızgaranın Sol ile kategori sütununa dönüş işleyicisiyle çakışabilir; işleyici sırası (zincirde önce) ve KeyDown/KeyUp tüketimi enstrümantasyonla doğrulanmalı.
- Hızlı menü 7 satıra çıkıyor; 1,5 yazı ölçeğinde kaydırma gerekir; odaktaki satırın görünürlüğü ve 460 dp diyalog sınırı doğrulanmalı.
- Özel User-Agent ayarı tarafsız dille sunulsa da 'sağlayıcı kısıtlamasını aşma' aracı gibi algılanabilir; mağaza metinlerinde bu özellik öne çıkarılmamalı (PLAY_STORE_READINESS 3.2).
- Kullanıcının aktarılan isteği (fiyatlama, satış stratejisi, yabancı diller, çok ülkeli satın alma) bu tasarımla yanıtlanmıyor; o soruların ayrı bir iş modeli ve yerelleştirme çalışması gerekir. Bu kümede yalnız ücretli katman kapı noktaları ve yerelleştirmeye hazır olma kuralı ele alındı.

**Dokunulacak dosyalar:**
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/LibraryCustomizations.kt`: Yeni: model (StoredRef, FavoriteGroup, NamedRef, HiddenRef, LibraryCustomizations), saf işlemler, sınırlar, LibraryCustomizationsCodec (JSON v1), FavoriteMigration.fromLegacy
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/CustomIndex.kt`: Yeni: ref + alt çözümü, belirsiz alt tespiti, rewrites; LibraryViews.build (liveChannels, favoriteGroups, favoriteOrder, categoryOrder, categoryLabels, hiddenItems); HiddenItems.apply
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/CustomOrder.kt`: Yeni: CustomOrder.apply/move (önek modeli, sınır), ChannelOrder.arrange (kategori yuvası düzeni)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/RequestHeaders.kt`: Yeni: defaultUserAgent, catalog/guide (host kapsamlı)/playback (öncelik birleşimi) başlıkları
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/RefreshSchedule.kt`: Yeni: RefreshInterval enum, dueSources/nextDelay, geri çekilme; GuideStaleness.isStale
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/Models.kt`: SourceConnection ve HeaderField (doğrulama, gizli toString); SourceConfig.connection alanı ve validated(); MediaEntry.displayName alanı
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/SourceRegistry.kt`: SourceRegistryCodec: isteğe bağlı connection nesnesini yazar ve okur (sürüm 1 kalır, varsayılanda biçim aynı)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/DeviceStore.kt`: library:v1 okuma ve kilitli updateCustomizations (companion customLock), okunamayan kopya; saveSources aynı commit'te budama; forgetSource library:v1 siler; refresh.catalog/refresh.guide tercihleri
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/SourceRepository.kt`: get() başlık parametresi; load/movie/episodes RequestHeaders.catalog(source) geçirir; sabit 'NEXTV AndroidTV' kaldırılır
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/EpgModels.kt`: EpgPlan adres başına başlıkları taşır (binding değişmez, toString gizli)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/EpgRepository.kt`: download() plan başlıklarını kullanır
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/GuideController.kt`: interval lambda ile bayatlık (GuideStaleness), ensure(onlyExisting) seçeneği
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/CategoryVisibility.kt`: choices(catalog, order) özel sırayı parametre alır
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/FavoriteTargets.kt`: Sıra koruyan migrateList (grup öğeleri için)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/SourceLibrary.kt`: favorites() yalnız geçiş için kalır (legacy okuma)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/TitleIndex.kt`: Satır metnine displayName eklenir (iki adla arama)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/HomeLibrary.kt`: homeLibrary favori rafını favoriteOrder sırasıyla kurar
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/CatalogPresentation.kt`: MediaEntry.shown() yardımcı (özel ad öncelikli DisplayTitle)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/TvViewModel.kt`: TvState yeni alanları; publishLibrary hattı (geçiş, gizleme, adlar, LibraryViews, rebind), hafif/tam yeniden yayın, sanal kategori koruması; favori/grup/sıra/ad/gizleme API'leri; channelContext ile numberedChannels/changeChannel; play() başlık hesaplar; SourceDraft.connection; otomatik güncelleme zamanlayıcısı (foreground, libraryEditing, rota izleme, refreshSources(automatic)); restore'daki sabit 6 sa bloğu kalkar
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/MainActivity.kt`: onStart/onStop → model.foreground(true/false)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/LibraryScreen.kt`: liveChannels ve özel kategori sırası/etiketleri; Favoriler sütununda gruplar ve tür bölümlerinde salt okunur grup kategorileri; 'Yeni grup' başlık eylemi; CategoryColumn onLongClick menüsü; taşıma kipi (MoveSession, tuş yakalama, hap, LibraryBack önceliği); QuickContext
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PilotApp.kt`: QuickActions QuickContext ile genişler (scrollBody); Setup'a 'Bağlantı ayarları' düğmesi; MediaCard shown() ve contentDescription
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/FavoriteGroupsDialog.kt`: Yeni: grup seçici (onay satırları, Yeni grup…), yeni grup diyaloğu
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/LibraryEditDialogs.kt`: Yeni: yeniden adlandırma, gizleme onayı, grup silme onayı, kategori/grup eylem menüleri
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/ConnectionDialog.kt`: Yeni: UA/Referer/en çok 3 ek başlık düzenleyici, doğrulama ve hata gösterimi
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SettingsScreen.kt`: Yeni sayfalar REFRESH, FAVORITE_GROUPS, HIDDEN_ITEMS, RESET_CUSTOM ve MAIN satırları; CategoryList özel etiket ve sıra
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt`: PlayingMedia'dan UA + başlıklar (setUserAgent ayrı, haritada User-Agent yok); yıldız R1/R2 ve favoritePicker otomatik gizlemeyi durdurur; shown() başlık
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/DetailScreen.kt`: Yıldız R1/R2 (seçici); shown()
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/HomeScreen.kt`: QuickContext (ad, gizle, grup); shown() vitrin ve kartlarda
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/GuideScreen.kt`: Kanal listesi ve numaralar liveChannels'tan; shown()
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SearchScreen.kt`: shown() ile görünen ad
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SourcesScreen.kt`: Bilgiler metnine 'Bağlantı: varsayılan/özel' satırı
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/GuideSettings.kt`: guideStatusText bayatlık eşiği aralığa bağlı
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/DesignSystem.kt`: LineIcon için edit, swap_vert, visibility_off yolları (Material Symbols, Apache 2.0)
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/`: Yeni testler: LibraryCustomizationsCodecTest, FavoriteMigrationTest, CustomIndexTest, CustomOrderTest, ChannelOrderTest, HiddenItemsTest, LibraryViewsTest, SourceConnectionTest, RequestHeadersTest, RefreshScheduleTest, GuideStalenessTest; genişletme: SourceTest, EpgSourceTest, TitleIndexTest, HomeLibraryTest, FavoriteTargetsTest, SourceRegistryTest
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/`: Yeni: LibraryEditingFlowTest, FavoriteGroupsFlowTest, SourceConnectionFlowTest; genişletme: MultiSourceFlowTest (budama), SourceRegistryStorageTest (bağlantı)
- `docs/decisions/DECISIONS.md`: Yeni karar kaydı (öneri olarak işaretli): veri modeli, R1/R2, taşıma kipi, numara kuralı, bağlantı ayarı kapsamı, uygulama içi zamanlayıcı
- `docs/project/BACKLOG.md`: Yeni BKL (altı aşama ve kabul ölçütleri)
- `docs/design/SCREENS.md`: Kütüphane taşıma kipi, kategori/grup menüsü, yeni Ayarlar sayfaları, Kurulum'da Bağlantı ayarları
- `docs/design/NATIVE_UI.md`: Taşıma çipi ve durum hapı görsel sözleşmesi (D064 odak dili korunur)
- `docs/product/TIVIMATE_GAP.md`: İlgili üç satırın durumu (uygulama bitince)

---

# Kişisel kütüphane düzeni ve kaynak bağlantı ayarları — tasarım (öneri)

Kapsam: TIVIMATE_GAP satırları "Favori grupları; kanal/grup sıralama, yeniden adlandırma, tek kanal gizleme", "User-Agent ve HTTP ayarları", "Liste ve EPG otomatik güncelleme". Bu belgedeki her karar **öneridir**; sahip onayı gibi sunulmaz (CLAUDE.md). Kod çalıştırılmadı, yalnız okundu.

## 0. Bugünkü durum (kodda okunan)

- **Favoriler:** `DeviceStore.favorites(source)` → `f:<sourceId>` StringSet (sırasız, yerel kimlikler). `SourceLibrary.favorites` bunları `ContentRef` anahtarlarına çevirir. `TvViewModel.toggleFavorite` / `repairEpisodeFavorites` (`FavoriteTargets.resolve/migrate`; bölüm → dizi). `DeviceStore.saveSources` kaldırılan kaynağın `f:` ve `history:` anahtarlarını aynı commit'te siler; `forgetSource()` `f:` önekini siler.
- **Sıra:** Kategori sırası `Catalog.categoryOrder` + `providerCategories` (sağlayıcı sırası, M3U'da ilk görülme). Kanal sırası `catalogDisplayOrder` (ayraçlar sonda). Kanal numaraları buradan gelir: `LibraryScreen.numbers`, `TvViewModel.numberedChannels`, `GuideScreen.numbers`. Ancak `GuideScreen.channels` ham katalog sırasındadır. `TvViewModel.changeChannel` de ham sırayı kategori adıyla süzer; sanal `@favorites` kategorisinde liste boş kalır, yani CH± çalışmaz.
- **Gizleme:** Yalnız kategori düzeyinde (D068): `CategoryVisibility.key = "KIND|ad"` bütün kaynaklarda geçerli, `hiddenCategories` StringSet, `publishLibrary` içinde `CategoryVisibility.apply`.
- **Görünen ad:** `displayTitle`/`displayCategory` yalnız görüntüde (D065). EPG eşleşmesi `EpgStore` içinde `epgId`, `epgName` ve `title` ile yapılır.
- **HTTP:** `SourceRepository.get` ve `EpgRepository.download` sabit `User-Agent: NEXTV AndroidTV` gönderir. Oynatıcı ise `"NEXTV/${VERSION_NAME} AndroidTV"` ve `setDefaultRequestProperties(entry.headers)` kullanır (`PlayerScreen`). D066'daki "NEXTV/<sürüm>" yalnız oynatıcıda uygulanmış. M3U satırındaki `#EXTVLCOPT:http-user-agent/http-referrer` değerleri `M3uParser` ile `MediaEntry.headers`'a girer. Media3 `DefaultHttpDataSource` `setUserAgent` değerini istek özelliklerinden sonra yazıyor olabilir; bu durumda satırdaki UA ezilir. **Media3 1.11.1 kaynağında doğrulanmalı.**
- **Güncelleme:** `restore()` 6 saatten eski katalogları açılışta yeniler (sabit). `GuideController.stale` 6 saatten eski veya pencereyi kapsamayan rehberi, bir ekran `ensureGuide` çağırınca indirir. Rehber indirmeleri `network` Mutex ile sıradadır.

## 1. Ortak veri modeli: `LibraryCustomizations`

### 1.1 Saklama

`device_state` içinde tek anahtar: `library:v1`. Değer JSON'dur; şifrelenmez, çünkü yalnız opak kimlikler ve adlar içerir (D068 `hiddenCategories` emsali); adres, hesap veya token yoktur. Yeni dosya: `data/LibraryCustomizations.kt` (model + `LibraryCustomizationsCodec`, saf Kotlin + org.json → JVM testli).

```json
{ "version": 1,
  "groups": [ {"id":"default","name":"Favoriler","items":[{"r":"<ContentRef.key>","a":"<alt>"}]},
              {"id":"<uuid>","name":"Spor","items":[...]} ],
  "categoryOrder": {"LIVE":["Ulusal","Spor"],"MOVIE":[],"SERIES":[]},
  "categoryNames": {"LIVE|TR/ULUSAL ~ HD":"Ulusal"},
  "channelOrder": {"Ulusal":[{"r":"…","a":"…"}]},
  "channelNames": [{"r":"…","a":"…","name":"TRT 1"}],
  "hidden": [{"r":"…","a":"…","k":"LIVE","t":"<sağlayıcı başlığı>"}] }
```

```kotlin
data class StoredRef(val ref: String, val alt: String = "")          // ref = ContentRef.key (kaynak + yerel kimlik)
data class FavoriteGroup(val id: String, val name: String, val items: List<StoredRef>)
data class NamedRef(val target: StoredRef, val name: String)
data class HiddenRef(val target: StoredRef, val kind: MediaKind, val title: String)
data class LibraryCustomizations(groups, categoryOrder: Map<MediaKind, List<String>>, categoryNames: Map<String,String>,
    channelOrder: Map<String, List<StoredRef>>, channelNames: List<NamedRef>, hidden: List<HiddenRef>) {
    fun withoutSources(ids: Set<String>): LibraryCustomizations      // ContentRef.parse(ref).sourceId ile budar
    fun rebind(rewrites: Map<StoredRef, StoredRef>): LibraryCustomizations
    // saf işlemler: addGroup/renameGroup/deleteGroup/moveGroup/setMember/reorderGroup/rename/hide/show/order…
    companion object { const val DEFAULT_GROUP = "default" }
}
```

**Sınırlar** (kod sabiti, JVM testli): 30 grup; ad 1–40 karakter, kırpılır, Türkçe büyük/küçük harf duyarsız benzersiz (`Locale("tr")`); toplam favori üyeliği 3.000; kategori adı 500; kanal adı 1.000; gizli 2.000; `channelOrder` listesi başına 300, toplam 3.000; kodlanmış belge ≤ 512 KB. Sınırlar yalnız **eklemeyi** engeller; geçişte gelen fazlalık korunur.

### 1.2 Kimlik: yenilemeden sağ çıkma

- **Birincil anahtar:** `ref` = yayınlanan `MediaEntry.id`, yani `ContentRef(sourceId, localId).key`. Xtream'de `live:<stream_id>` değişmez. M3U'da `m3u:<sha256(url)>`; adresteki token dönerse değişir.
- **Yedek anahtar:** `alt = "<KIND>|" + epgNameKey(entry.title)`. Ad olarak saklanır, adres değildir; kaynak içinde çözülür.
- Yeni `CustomIndex(catalog)` (`data/CustomIndex.kt`): kaynak + alt → kimlik haritası. İki girdi aynı alt'ı paylaşıyorsa alt **belirsiz** sayılır ve kullanılmaz (aynı adlı HD/SD çeşitleri).
- `resolve(stored)`: önce `ref`; bulunamazsa ve alt benzersizse alt'ın gösterdiği girdi. Alt ile çözülen ya da alt'ı boş kayıtlar `rewrites` listesine girer. Yayının sonunda tek bir `updateCustomizations { it.rebind(rewrites) }` yazılır.
- Kategori düzeyindeki ad ve sıra, D068 gibi `KIND|sağlayıcı adı` ile anahtarlanır ve bütün kaynaklarda geçerlidir. Sağlayıcı kategoriyi yeniden adlandırırsa kayıt düşer (D068 ile aynı risk).

### 1.3 Geçiş (tek favori listesi → varsayılan grup)

- `DeviceStore.customizations(): LibraryCustomizations?` null döndürürse anahtar yok demektir.
- İlk `publishLibrary` içinde saf `FavoriteMigration.fromLegacy(SourceLibrary.favorites(sources, store::favorites), combined)` çalışır. Sonuç: `groups=[default "Favoriler"]`; sıra birleşik katalog sırasıdır; katalogda olmayanlar (çözülmemiş bölüm kimlikleri) sözlük sırasıyla sona eklenir; `alt` mümkünse doldurulur.
- `f:<source>` anahtarları **silinmez** (eski APK'ya dönüşte favoriler kaybolmaz). Artık yazılmaz; mevcut `saveSources` ve `forgetSource` temizliği onları kaldırmaya devam eder. Ayrı bir sürümde silinmesi önerilir.
- `repairEpisodeFavorites` bundan sonra grup listelerinde sırayı koruyarak eşler (`FavoriteTargets.migrate` yanına sıralı `migrateList`).
- **Okunamayan belge:** Ham değer bir kez `library:v1.unreadable` anahtarına kopyalanır, varsayılan model kullanılır ve `CatalogNotice` gösterilir: "Kişisel düzenlemelerin okunamadı; varsayılan görünüm kullanılıyor." İlk yeni yazım `library:v1`'i değiştirir.

### 1.4 Temizlik ve eşzamanlılık

- `DeviceStore.saveSources`: aynı `editor` içinde `library:v1` → `withoutSources(removed)` budanır ve yazılır (tek commit, mevcut kural). `forgetSource()` `library:v1` ile `.unreadable` anahtarını da siler.
- Okurken de güvenlik: kaynağı kayıt defterinde olmayan ref'ler yok sayılır.
- `DeviceStore` companion'ında `private val customLock = Any()`; `updateCustomizations(change)` oku-değiştir-yaz işlemini bu kilit altında yapar (VM yeniden kurulsa da tek kilit). `saveSources` budamayı da aynı kilitle yapar. UI yazımları `apply()`, `saveSources` `commit()` kullanır.
- "Kaynağı kaldır" metnindeki "bu kaynağın favorileri … silinecek" ifadesi doğru kalır.

## 2. Yayınlama hattı (`TvViewModel.publishLibrary`)

Sıra (`Dispatchers.Default`):
1. `combined = SourceLibrary.combine(...)`
2. Geçiş (1.3) ve `index = CustomIndex(combined)`
3. `visible = CategoryVisibility.apply(combined, hidden, hideAdult)` → yeni `HiddenItems.apply(visible, çözülmüş gizli kimlikler)`. Dizi gizlenince `m3uEpisodes` içindeki bölümleri de düşer.
4. Ad uygulama: yeni alan `MediaEntry.displayName: String? = null`. Yalnız yeniden adlandırılan girdiler kopyalanır. Bu alan `CatalogSnapshotCodec` tarafından yazılmaz; yayınlanan girdiler zaten saklanmaz.
5. Saf `LibraryViews.build(visible, customizations, index)` → `LibraryView`:
   - `liveChannels`: `catalogDisplayOrder(LIVE, !adult)` + `ChannelOrder.arrange`
   - `categoryOrder`: tür başına kullanıcı öneki + sağlayıcı sırası
   - `categoryLabels`
   - `favoriteGroups: List<FavoriteGroupView(id, name, itemIds, missing, hiddenCount)>`
   - `favoriteOrder`: grupların sırayla birleşimi, tekrar edenler bir kez
   - `hiddenItems: List<HiddenItemView(key, title, kind, sourceId, present)>`
6. `rewrites` varsa bir kez yazılır.

**TvState eklemeleri:** `liveChannels`, `favoriteGroups`, `favoriteOrder` (`favoriteIds` birleşim kümesi olarak kalır; mevcut ★ ve süzme kodu çalışır), `categoryOrder`, `categoryLabels`, `hiddenItems`, `customNameCount`, `customOrderActive`, `catalogRefresh`, `guideRefresh`.

**Hafif ve tam yeniden yayın:**
- Favori ve sıra değişikliği yalnız `LibraryView`'u yeniden kurar; `Catalog` örneği değişmez, `TitleIndexCache` bozulmaz.
- Ad ve gizleme değişikliği tam `publishLibrary` ister (arama iki adı da dizinlemeli).
- Özel kategori sırası `Catalog.categoryOrder`'a **yazılmaz**. Library `s.categoryOrder[kind]` okur. `CategoryVisibility.choices(catalog, order)` sırayı parametre olarak alır, böylece Ayarlar listesi sütunla aynı sırayı gösterir.

**Sanal kategorilerin korunması:** `publishLibrary` içindeki `category = old.category.takeIf { …entry.category == category } ?: "Tümü"` her yayında `@favorites`/`@history` seçimini sıfırlıyor. `@` ile başlayan geçerli sanal anahtarlar (`@favorites`, `@history`, var olan `@group:<id>`) korunacak. Yoksa grup içinde ad değiştirmek kullanıcıyı "Tüm favoriler"e atar.

**Tek kanal bağlamı:** Yeni `TvViewModel.channelContext()` Library'nin gösterdiği listeyi verir: Tümü → `liveChannels`; sağlayıcı kategorisi → süzülmüş liste; `@favorites` veya `@group:` → `favoriteOrder` ∩ canlı. `changeChannel`, `numberedChannels` ve `GuideScreen` bunu ve `liveChannels`'ı kullanır. Bu, Favoriler'deki CH± hatasını giderir ve rehber sırasını Canlı TV ile aynı yapar (**görünür davranış değişikliği**).

## 3. Favori grupları

### 3.1 Kurallar

- **R1 · tek grup (bugünkü herkes):** Bütün yıldızlar bugünkü gibi varsayılan grupta ekler/çıkarır: ayrıntı (`DetailScreen`), oynatıcı (`PlayerScreen`), hızlı menü (`QuickActions`). Hızlı menüye tek bir yeni satır eklenir: **"Yeni gruba ekle…"** (keşif yolu).
- **R2 · iki veya daha çok grup:** Her yıldız **"Favori grupları…"** olur (içerik herhangi bir gruptaysa dolu yıldız) ve seçiciyi açar. Bölüm girdisi her durumda diziye gider (`FavoriteTargets.resolve`); oynatıcıda etiket "Dizinin favori grupları…".
- Yeni favoriler grubun **sonuna** eklenir. "Tüm favoriler" sırası katalog sırası değil, grup sırasıdır (görünür değişiklik; D061'deki "Favoriler kendi sırasını korur" ile uyumlu).

### 3.2 Seçici (`ui/FavoriteGroupsDialog.kt`, `SourceDialog`, `scrollBody = true`, 480 dp)

- Başlık "Favori grupları"; ilk satır not olarak içeriğin görünen adı.
- Her grup bir onay satırı: `Action(check = true, selected = üye)`, semantik `Role.Checkbox`, `toggleableState`, `stateDescription` "Grupta" / "Grupta değil". OK anında yazar ve diyalog açık kalır.
- Ardından "Yeni grup…", "Kapat".
- İlk odak: içeriğin bulunduğu ilk grup, yoksa son kullanılan grup (bellekte), yoksa ilk satır.
- Geri seçiciyi kapatır, odak açan düğmeye döner (`refocusAfterDialog`).
- Oynatıcıda seçici açıkken kontrollerin otomatik gizlenmesi durur. `PlayerScreen`'deki `trackPanel` koşuluna `favoritePicker` eklenir.

### 3.3 Yeni grup ve yeniden adlandırma diyaloğu

- `SourceDialog("Yeni favori grubu")`: `EntryField("Grup adı")` (klavye OK ile açılır, D058), "Oluştur" (birincil), "Vazgeç".
- Hatalar: "Grup adı boş olamaz." · "Bu adla bir grup zaten var." · "Ad en fazla 40 karakter olabilir." · "En fazla 30 grup oluşturabilirsin."
- "Yeni gruba ekle…" ile açılmışsa grup oluşturulur ve içerik o gruba eklenir.

### 3.4 Kütüphane

- **Favoriler bölümü, tek grup:** Bugünkü sütun aynen kalır.
- **Favoriler bölümü, iki veya daha çok grup:** Sütun "Tüm favoriler", ardından gruplar (`SectionCategory("@group:<id>", ad)`, kullanıcı sırasıyla), ardından favorilerin sağlayıcı kategorileri. Başlığa `Action("Yeni grup", compact, outlined)` eklenir (Ara'nın soluna).
- **Canlı TV / Filmler / Diziler, iki veya daha çok grup:** "Favoriler" sanal kategorisinin hemen altında, bu türden içeriği olan gruplar salt okunur sanal kategori olarak durur; etiket alanında "★" (`displayCategory` etiket yeri).
- **Boş grup:** "Bu grup boş" / "Bir içeriği basılı OK ile açılan menüden bu gruba ekleyebilirsin." Eylem "İçerikleri keşfet" (mevcut).
- **Grup satırında basılı OK** (`CategoryColumn` `Surface`'ine `onLongClick` eklenir): "Yeniden adlandır", "Taşı", "Grubu sil" (varsayılan grupta yok), "Vazgeç".
- **Grubu sil onayı:** "“Spor” grubu silinsin mi?" / "Gruptaki içerikler kaynağında kalır; yalnız bu liste silinir." İlk odak **Vazgeç** (kaynak kaldırma emsali), sonra "Grubu sil".
- **Ana Sayfa:** "Favorilerin" rafı `favoriteOrder` sırasıyla gelir (`homeLibrary` sıralı liste alır).

## 4. Elle sıralama: taşıma kipi

### 4.1 Nerede var, nerede yok

| Bağlam | Taşı | Saklama |
|---|---|---|
| Kategori sütunu, sağlayıcı kategorisi (her tür) | var | `categoryOrder[KIND]`: tam ad listesi, ≤ 1.000 |
| Canlı TV, sağlayıcı kategorisindeki kanal | var | `channelOrder[kategori]`: önek, ≤ 300 |
| Favoriler bölümünde bir grup (her tür, ızgara) | var | `groups[i].items`: tam liste |
| Favoriler sütunundaki grup satırı | var | `groups` sırası |
| "Tüm kanallar/filmler/diziler", "Favoriler" birleşimi, "Geçmiş", tür bölümlerindeki salt okunur gruplar, arama sorgusu açıkken, Filmler/Diziler kategorileri | yok | — |

Filmler/Diziler'de elle sıra yok; orada Sırala var (D061).

### 4.2 Önek modeli (`data/CustomOrder.kt`, saf)

- `CustomOrder.apply(items, prefix, key)`: önekte bulunanlar (tekrarsız, var olanlar) önce, geri kalanlar sağlayıcı sırasıyla. Yenilemede gelen yeni öğeler sona düşer.
- `CustomOrder.move(keys, from, to, oldPrefixLen, cap)`: yeni önek uzunluğu `max(oldPrefixLen, max(from, to) + 1)`. `to ≥ cap` reddedilir.
- Kanal kategorisinde 300'den sonraki bir öğe için hızlı menüde **"Başa taşı"** görünür (önek bir artar).
- `ChannelOrder.arrange(display, orders)`: her kategorinin "Tüm kanallar" içindeki yuva indeksleri korunur; o yuvalar kategorinin özel sırasıyla doldurulur. Sonuçlar:
  - Kategori içinde numaralar artan sırada kalır.
  - Diğer kategorilerin numaraları değişmez.
  - Rakamla kanal açma, CH± ve rehber aynı listeyi kullanır.
  - Özel sıra yoksa aynı liste örneği döner.

### 4.3 Kumanda akışı (kanal örneği)

1. Canlı TV → kategori sütununda "Ulusal" → OK → Sağ → "TRT 1" satırı.
2. OK'yi basılı tut → hızlı menü → **Taşı** → OK.
3. Taşıma kipi:
   - Satır odağı korur (yüzey: beyaz 2 dp halka; büyüme yok, D064).
   - Satırın sağında odaklanmayan bir çip durur: `Ink.Selected` zemin üstünde mint "↑↓", Ayarlar'daki Açık çipi gibi.
   - Liste alanının sağ altında odaklanmayan bir hap görünür: "Taşınıyor · 3 / 20". Altında ipucu: "Yukarı/Aşağı: yer değiştir · OK: bırak · Geri: vazgeç". Izgarada: "Yön tuşları: yer değiştir · …". Hap, büyük yazıda başlık kaysa da görünür kalır.
4. **Yukarı/Aşağı:** bir sıra. **CH+/CH−:** 10 sıra. Izgarada Sol/Sağ bir, Yukarı/Aşağı bir satır. Sol ve Sağ tüketilir; odak listeden çıkmaz, kategori sütunu geri gelmez.
5. **OK:** bırakır ve `model.saveChannelOrder(...)` çağrılır. Odak öğede kalır.
6. **Geri:** asıl sıra geri gelir, odak öğenin eski yerine döner. `LibraryBack` taşıma oturumunu ilk sırada denetler; Geri bir basamak çıkar.
7. Kip açıkken basılı OK tüketilir; Sırala ve Ara devre dışıdır. `model.libraryEditing(true)` otomatik güncellemeyi erteler. Rota değişirse (menü, oynatma) oturum kaydedilmeden kapanır.

**Uygulama:**
- Durum: `Library` içinde `var moving: MoveSession?` (öğe kimliği, asıl liste, çalışma listesi). `filtered`, oturum varken çalışma listesinden türer. Yazım yalnız OK'de yapılır; her tuşta ViewModel durumu değişmez (D056).
- Tuş yakalama: `LazyVerticalGrid` zincirinde mevcut Sol işleyicisinden **önce** gelen bir `onPreviewKeyEvent`. DirectionCenter/Enter için KeyDown ve KeyUp tüketilir; böylece kartın `onClick` kanalı açmaz.
- Kaydırma: `grid.scrollToItem`; Hareketi azalt kapalıyken mevcut desen gibi animasyonlu.
- Öğe animasyonu (`animateItem`) eklenmez.
- `memory.lastIndex` ve `position` güncellenir.
- Kategori sütununda aynı kip: sanal satırların üstüne çıkılamaz. Hap metni "Kategori taşınıyor · 4 / 25".
- Önek sınırında not: "Elle sıralama ilk 300 sırada kullanılabilir. Başa taşı ile öne alabilirsin."
- TalkBack: taşınan öğede `stateDescription = "Taşınıyor"`. Hapta `liveRegion = Polite` var ve her adımda "3 / 20" okunur.

## 5. Yeniden adlandırma

- **Kanal:** Hızlı menüde "Yeniden adlandır" yalnız `LIVE` için var. Model her tür için genel; UI kapsamı bilinçli dar.
- **Kategori:** Kategori sütununda basılı OK menüsü: "Yeniden adlandır", "Taşı", "Gizle", "Adı sıfırla" (yalnız özel ad varsa), "Vazgeç". Sanal satırlarda menü yok.
- **Diyalog** (`ui/LibraryEditDialogs.kt`):
  - Başlık "Kanalı yeniden adlandır" / "Kategoriyi yeniden adlandır" / "Grubu yeniden adlandır".
  - `EntryField("Yeni ad")`, şu anki görünen adla dolu.
  - Not: "Sağlayıcıdaki adı: …" ve "Ad yalnız bu cihazda değişir; aramada iki ad da bulunur."
  - Düğmeler: "Kaydet" (birincil), "Varsayılan ada dön" (özel ad varsa), "Vazgeç". Boş ad varsayılana döner. Hata: "Ad en fazla 40 karakter olabilir."
- **Gösterim:** Yeni `MediaEntry.shown(): DisplayTitle` özel ad varsa `DisplayTitle(displayName, year, "")`, yoksa `displayTitle(title, year)`. `displayTitle(entry.title…)` çağrılan yerler buna geçer: `ChannelRow`, `MediaCard`, `QuickActions` başlığı, `HomeScreen` (vitrin ve kart), `SearchScreen`, `DetailScreen`, `PlayerScreen` (başlık ve kanal geçiş notu), `GuideScreen` satırı. `contentDescription` için `displayName ?: title`.
- **Kategori etiketi:** `categoryLabels[key] ?: displayCategory(name)`. Özel adda ülke etiketi gösterilmez. Kategori sütunu, Ayarlar `CategoryList`, `ChannelRow`'daki kategori metni ve canlı `MediaCard`'ın kategori satırı bunu kullanır.
- **Arama:** `TitleIndex` satırı `normalize(title + " " + displayName)` olur.
- **EPG:** Eşleşme değişmez; `EpgStore.match` `title` ve `epgName` okur, `displayName` okumaz (JVM testi).

## 6. Tek içerik gizleme

- Hızlı menüde **"Gizle"** kanal, film ve dizi için var (aynı mekanizma).
- Onay: "Gizlensin mi?" / "“{ad}” ana sayfada, kütüphanede, aramada ve rehberde görünmeyecek. Ayarlar → İçerik → Gizlenen içerikler'den geri açabilirsin." Düğmeler "Gizle" (ilk odak; işlem geri alınabilir), "Vazgeç".
- Kategori gizleme kategori menüsünden: "“Spor” kategorisi gizlensin mi?" … "Kategorileri yönet'ten geri açabilirsin." Arkada mevcut `categoryVisible(kind, name, false)` çalışır.
- **Etki:** D068 ile aynı. Gizli içerik yayınlanan katalogdan çıkar; ana sayfa, kütüphane, arama, rehber, kanal numaraları ve CH± onu görmez. Favori üyeliği korunur, yalnız gösterilmez; Ayarlar'daki grup satırında "· 2 gizli".
- Gizlenen öğe odaktaysa mevcut `recoverRemoved` odağı komşuya verir.
- **Bu bir kilit değildir.** PIN (BKL-028) geldiğinde "Gizle" bir tercih olarak kalır; kilit `AccessRule(Content, pinRequired)` olur. İki mekanizma ayrı ama aynı `ContentRef`'i kullanır.

## 7. Kaynak başına bağlantı ayarları

### 7.1 Model ve saklama

```kotlin
data class HeaderField(val name: String, val value: String) { override fun toString() = "HeaderField(name=$name, value=REDACTED)" }
data class SourceConnection(val userAgent: String = "", val referer: String = "", val headers: List<HeaderField> = emptyList()) {
    val isDefault get() = userAgent.isBlank() && referer.isBlank() && headers.isEmpty()
    fun validated(): SourceConnection
    override fun toString() = "SourceConnection(custom=${!isDefault}, values=REDACTED)"
}
```

- `SourceConfig` yeni alan alır: `connection: SourceConnection = SourceConnection()`. `validated()` bunu da doğrular.
- `SourceRegistryCodec`: yalnız `!isDefault` ise `"connection"` nesnesi yazılır; `optJSONObject` ile okunur. `version` 1 kalır; eski sürüm bilinmeyen alanı yok sayar.
- Her şey şifreli kayıt defterinde (`seal`, "source-registry-v1"). Başlık değerleri token veya çerez içerebilir; günlüğe, anlık görüntüye ya da analitiğe gitmez.
- `CatalogStore.binding` ve `EpgPlan.binding` bağlantı ayarını **içermez**. Başlık değişince önbellek ve rehber atılmaz. `GuideController.synchronize` içindeki `old.source == source` karşılaştırması yeni sahip oluşturur, yeni başlıklar sonraki indirmede kullanılır.

### 7.2 Doğrulama (`SourceConnection.validated`, mesajlar `SourceFailure`)

| Kural | Mesaj |
|---|---|
| UA ≤ 256 karakter | "User-Agent en fazla 256 karakter olabilir." |
| Değerler yalnız yazdırılabilir ASCII (0x20–0x7E); OkHttp ASCII dışını reddeder | "Başlık değerinde yalnız İngilizce harf, rakam ve simgeler kullanılabilir." |
| Değer ≤ 512 karakter, satır sonu yok | "Başlık değeri satır sonu içeremez." |
| Referer `toHttpUrlOrNull` geçerli; kullanıcı bilgisi içermez | "Referer geçerli bir http veya https adresi olmalı." |
| Ad, RFC 7230 token, ≤ 64 | "Başlık adı yalnız harf, rakam ve - içerebilir." |
| Ad yasak listede değil (büyük/küçük harf duyarsız): Host, Content-Length, Transfer-Encoding, Connection, Accept-Encoding, Range, TE, Upgrade, Keep-Alive, Proxy-*, User-Agent, Referer | "Bu başlık değiştirilemez: {ad}" |
| ≤ 3 ek başlık | "En fazla 3 ek başlık eklenebilir." |

### 7.3 Uygulama (`data/RequestHeaders.kt`, saf)

- `defaultUserAgent() = "NEXTV/${BuildConfig.VERSION_NAME} AndroidTV"`. Liste ve EPG istekleri de buna geçer (D066 ile hizalanır).
- `catalog(source)`: UA (özel veya varsayılan) + Referer + ek başlıklar. `SourceRepository.get(url, maxBytes, headers)` bunu alır; `load`, `movie` ve `episodes` çağrıları geçirir.
- `guide(source, url)`: **UA her zaman.** Referer ve ek başlıklar yalnız rehber adresinin hostu kaynak hostuyla aynıysa gider (Xtream `xmltv.php`, aynı sunucudaki M3U rehberi). Üçüncü taraf bir XMLTV sitesine kaynağın çerezi veya Referer'ı sızmaz. `EpgPlan` her adres için başlıkları taşır (`toString` gizli kalır); `EpgRepository.download(url, headers, …)`.
- `playback(source, entry)`: öncelik **M3U satırı > kaynak ayarı > varsayılan**, adlar büyük/küçük harf duyarsız birleştirilir. Kanala özgü ipucu sağlayıcıdan gelir. Dönüş `(userAgent, others)`. `TvViewModel.play` bunu hesaplayıp `PlayingMedia`'ya koyar (`toString` gizli). `PlayerScreen`: `setUserAgent(ua)`, `setDefaultRequestProperties(others)`; haritada User-Agent **bulunmaz**. Media3'ün yazma sırasına bağımlılık böylece ortadan kalkar.
- Coil afiş istekleri değişmez (kapsam dışı).

### 7.4 UI

- **Kurulum (`Setup`):** XMLTV alanının altında `Action("Bağlantı ayarları · isteğe bağlı", outlined)`; ayar varsa "Bağlantı ayarları · özel". Diyalog yalnız taslağı değiştirir. "Kütüphaneyi aç" (`connectDraft` → `connect`) yeni ayarla içe aktarır ve böylece doğrular. Düzenleme de aynı yoldan geçer (`editSource` bağlantıyı taslağa kopyalar). Ayrı bir kaydetme yolu yoktur.
- **Diyalog** (`ui/ConnectionDialog.kt`, `SourceDialog` 600 dp, `scrollBody`):
  - Not: "Sağlayıcın belirli bir uygulama kimliği (User-Agent) ya da başlık istiyorsa buraya gir. Emin değilsen boş bırak."
  - Alanlar: "User-Agent · boş: NEXTV varsayılanı", "Referer · isteğe bağlı". En çok 3 satır "Ek başlık adı" / "Ek başlık değeri" (değer gizli) ve satır başına "Kaldır".
  - Düğmeler: "Başlık ekle", "Değerleri göster/gizle", "Tamam" (birincil; doğrular, hatayı diyalogda gösterir ve oraya kaydırır), "Temizle", "Vazgeç".
- **Sources → Bilgiler:** "Bağlantı: varsayılan" ya da "Bağlantı: özel ayarlar (User-Agent, 1 ek başlık)". Değerler hiçbir yerde gösterilmez.
- **Dil:** Hazır UA listesi (başka uygulama adları) yok; bkz. karar 2. "Kısıtlamayı aş" türü ifade yok (PLAY_STORE_READINESS 3.2).

## 8. Otomatik güncelleme

### 8.1 Mekanizma: uygulama içi zamanlayıcı (WorkManager değil)

**Gerekçe:**
- Yeni bağımlılık gerekmez.
- Bekleme modundaki TV'de Keystore şifre çözme ve ağ çalışmaz.
- Worker'ın yazdığı anlık görüntü ile ViewModel'in bellekteki `snapshots` haritası tutarsızlaşmaz.
- "Oynatma yokken" kuralı yalnız süreç içinde bilinebilir.
- Açılışta zaten bayatlık kontrolü var.

**Kapı** (`canAutoRefresh()`): ön planda (`MainActivity.onStart`/`onStop` → `model.foreground(Boolean)`) **ve** rota PLAYER, LOADING, SETUP veya RECOVERY değil **ve** `sourceTask == IDLE` **ve** `!refreshing` **ve** `!libraryEditing` **ve** doğrulanmış ağ var (`ConnectivityManager` + `NET_CAPABILITY_VALIDATED`; izin manifestte mevcut).

**Tetikleyiciler:**
- `restore()` başarıyla bittiğinde: sabit 6 saat bloğunun yerine `scheduleAuto(0)`.
- `foreground(true)`.
- Aralık değiştiğinde.
- `libraryEditing(false)`.
- PLAYER'dan çıktıktan 60 sn sonra (kısa kanal değişimleri tetiklemesin). Rota akışı `distinctUntilChanged` ile izlenir.
- Ayrıca `min(15 dk, sonraki vadeye kalan)` aralıklı tik.

**Çalışma:**
1. Vadesi gelen kataloglar `refreshSources(due, token, automatic = true)` ile sırayla yenilenir.
2. Ardından `loadJob?.join()`.
3. Sonra `guides.ensure(ids, onlyExisting = true)`: yalnız kayıtlı rehberi olan kaynaklar. Hiç açılmamış rehber arka planda indirilmez.

Böylece aynı sağlayıcıya aynı anda bir katalog ve bir EPG isteği gitmez.
- `automatic` döngüde her kaynaktan önce kapı yeniden denetlenir. Oynatma başlarsa sürmekte olan istek biter, sonrakiler başlamaz.
- Hata: kaynak başına bellekte 30 dk geri çekilme. Ağ yoksa deneme sayılmaz.
- Başarı sessizdir (`catalogNotice` yok). Hata: `CatalogNotice("Otomatik güncelleme yapılamadı. Kayıtlı içerikler kullanılıyor.", REFRESH, needsAttention = false)`. Bu, Ayarlar'daki "Kataloğu yenile" satırında görünür; kütüphaneye kırmızı bant düşmez. Kaynak durumu Kaynaklar ekranında görünür.

### 8.2 Aralık

- `enum class RefreshInterval(val hours: Int, val label: String) { OFF(0,"Kapalı"), H6(6,"6 saatte bir"), H12(12,"12 saatte bir"), H24(24,"Günde bir") }`
- `DeviceStore.catalogRefresh` / `guideRefresh`: anahtarlar `refresh.catalog` ve `refresh.guide`, varsayılan **H6** (bugünkü davranış değişmez). Genel ayar, kaynak başına değil.
- Saf `RefreshSchedule` (`data/RefreshSchedule.kt`): `dueSources(now, interval, updatedAt, lastAttempt)` ve `nextDelay(...)` (60 sn – 15 dk). Gelecek tarihli `updatedAt` (> şimdi + 5 dk) vadesi gelmiş sayılır.
- `GuideController` yeni parametre alır: `interval: () -> RefreshInterval`. Saf `GuideStaleness.isStale(info, now, zone, interval)` testlenir.
  - OFF: yalnız `info == null`; pencere denetimi de atlanır, yoksa her gün indirirdi.
  - Diğerleri: saat eşiği + pencere kapsamı.
- `guideStatusText` içindeki sabit 6 saatlik "eski olabilir" eşiği aralığa bağlanır; OFF'ta 24 saat.

## 9. Ayarlar (`SettingsScreen`; yeni `SettingsPage` değerleri)

**Kaynaklar bölümü: "Otomatik güncelleme"** → `REFRESH` sayfası.
- Satır açıklaması: "Liste: 6 saatte bir · Rehber: 6 saatte bir".
- Sayfa notu: "Güncelleme yalnız uygulama açıkken ve yayın izlenmiyorken yapılır. Kayıtlı içerikler bu sırada kullanılabilir."
- `SettingsLabel("Kanal ve kütüphane listesi")` + 4 onay satırı; `SettingsLabel("Yayın rehberi")` + 4 onay satırı. OFF notu: "Kapalıyken yalnız Kataloğu yenile ve Rehberi yenile ile güncellenir."

**İçerik bölümü** (mevcut iki satırın altına):
- **"Favori grupları"** → `FAVORITE_GROUPS`. Açıklama "{n} grup · Oluştur, adlandır, sırala veya sil."
  - İlk satır "Yeni grup oluştur".
  - Grup satırları: "Spor" / "12 içerik · 2 gizli · 1 kaynakta yok".
  - OK → eylem diyaloğu: "Yeniden adlandır", "Yukarı taşı", "Aşağı taşı", "Grubu sil" (varsayılanda yok), "Bulunamayanları temizle" (gerekirse), "Vazgeç".
- **"Gizlenen içerikler"** → `HIDDEN_ITEMS`. Açıklama "{n} içerik gizli." / "Gizlenen içerik yok."
  - Tembel liste. Satır: "{ad} · Canlı TV · {kaynak adı (çoklu kaynakta)}"; `checked` = görünür (Kapalı). OK → Açık. Satır sayfadan çıkılana kadar listede kalır, odak zıplamaz.
  - Kaynakta olmayanlar "Kaynakta artık yok" der; OK kaydı siler.
  - Başlıkta "Hepsini göster".
- **"Sıralamayı ve adları sıfırla"** → `RESET_CUSTOM` onayı. Not: "Kategori ve kanal sıraları ile verdiğin adlar sağlayıcıdaki hâline döner. Favori grupların ve gizlediğin içerikler korunur." Düğmeler "Vazgeç" (ilk odak), "Sıfırla".

`back()` yeni sayfalardan MAIN'e döner, `returnTo` odağı ilgili satıra verir (mevcut desen). Satırlarda beyaz halka, düğmelerde mint dolgu var; büyüme yok.

## 10. ViewModel API (yeni ve değişen)

```kotlin
// Favoriler
fun toggleFavorite(entry: MediaEntry)                      // R1; UI R2'de seçiciyi açar
fun setFavoriteMembership(entry: MediaEntry, groupId: String, member: Boolean)
fun createFavoriteGroup(name: String, addEntry: MediaEntry? = null): String?   // hata → state.libraryEditError
fun renameFavoriteGroup(id: String, name: String); fun deleteFavoriteGroup(id: String)
fun moveFavoriteGroup(id: String, toIndex: Int); fun reorderFavoriteGroup(id: String, orderedIds: List<String>)
fun purgeMissing(groupId: String)
// Sıra
fun saveCategoryOrder(kind: MediaKind, names: List<String>)
fun saveChannelOrder(category: String, orderedIds: List<String>, from: Int, to: Int)
fun moveChannelToTop(entry: MediaEntry); fun resetCustomOrderAndNames()
fun libraryEditing(active: Boolean)
// Ad ve gizleme
fun renameChannel(entry: MediaEntry, name: String?); fun renameCategory(kind: MediaKind, category: String, name: String?)
fun hideItem(entry: MediaEntry); fun showItem(key: String); fun showAllHidden()
// Bağlantı: SourceDraft.connection; connectDraft/editSource bunu taşır
// Güncelleme
fun setCatalogRefresh(value: RefreshInterval); fun setGuideRefresh(value: RefreshInterval); fun foreground(started: Boolean)
internal fun channelContext(): List<MediaEntry>
```

- Bütün yazımlar `store.updateCustomizations { … }` (kilitli) üzerinden gider; ardından hafif ya da tam yeniden yayın (§2).
- Doğrulama hataları `TvState.libraryEditError: String?` ile diyaloğa gelir; diyalog açılınca temizlenir.
- `QuickActions` imzası `QuickContext` alır:

```kotlin
data class QuickContext(val favorite: FavoriteAction /* Toggle(isFav) | Groups(inAny) */, val offerNewGroup: Boolean,
    val inGroup: String?, val canRename: Boolean, val canMove: Boolean, val canMoveToTop: Boolean, val canHide: Boolean)
```

- Satır sırası: favori, "Yeni gruba ekle…", İzle/Ayrıntılar, Yeniden adlandır, Taşı, Başa taşı, Gruptan çıkar, Gizle, Vazgeç. Yalnız geçerli olanlar görünür; tipik olarak en çok 7 satır.
- `SourceDialog(scrollBody = true)`: büyük yazıda (1,5) liste kayar, odaktaki satır görünür alana gelir.

## 11. Kenar durumlar ve hatalar

- **Aynı adlı kategori iki kaynakta:** Ad ve sıra birleşik kategoriye uygulanır (D068 ile aynı). Kanal sırası iki kaynağın kanallarını birlikte sıralar; ref'ler kaynak kimliği taşır.
- **Kaynak düzenlendi (M3U → Xtream):** Ref'ler çözülmez; alt (başlık) benzersizse yeniden bağlanır, değilse kayıt "kaynakta yok" sayılır.
- **Sağlayıcı iki kanalın adını takas ederse:** Alt yanlış bağlayabilir. Alt yalnız ref çözülemediğinde kullanılır (risk).
- **Yetişkin içerik:** Favori, ad ve gizleme hedefi olamaz (`FavoriteTargets.resolve` null döner; yetişkin içerik zaten hiç görünmez).
- **Yenileme taşıma sırasında:** Otomatik güncelleme ertelenir. Elle yenileme Ayarlar'dan yapılır, taşıma kipindeyken ulaşılamaz.
- **Bütün kaynaklar kaldırıldı:** `forgetSource()` → `library:v1` silinir. Yeni kaynak boş düzenle başlar.
- **Sınır doldu:** "Kişisel düzenlemeler için ayrılan alan doldu. Bazı favorileri veya özel sıraları temizle." Grup ve favori sınırı için ayrı metinler (§3.3).
- **Kayıt yazılamadı** (`commit` false, `saveSources`): mevcut "Kaynaklar cihazda saklanamadı…" yolu.
- **Bağlantı ayarı yanlış:** İçe aktarma mevcut `SourceFailure` metinleriyle başarısız olur (ör. "Kaynağa erişim reddedildi. Hesap bilgilerini kontrol et."). Önceki hesap ve kütüphane korunur (`connect` mevcut davranışı).
- **Yönlendirme başka hosta:** OkHttp özel başlıkları (Authorization hariç) taşır. Referer/Cookie sızma riski olarak kaydedildi.
- **Otomatik güncelleme sonrası liste değişti:** Mevcut `recoverRemoved` ve `LaunchedEffect(filtered)` odak korumaları geçerli.

## 12. Testler

**JVM (önce; `app/src/test/.../data`):**
1. `LibraryCustomizationsCodecTest`: gidiş-dönüş; bilinmeyen sürüm → hata; sınırlar; Türkçe benzersiz ad ("İSTANBUL" ve "istanbul"); `withoutSources` yalnız kaldırılan kaynağı budar; 512 KB sınırı.
2. `FavoriteMigrationTest`: iki kaynağın `f:` kümeleri → katalog sırasında varsayılan grup; çözülmeyenler belirli sırayla sonda; bölüm kimliği sırası korunarak diziye; anahtar varken yeniden çalışmaz.
3. `CustomIndexTest`: ref isabeti; ref kayıp + benzersiz alt → rewrite; belirsiz alt → çözülmez; alt kaynak kapsamlı; M3U token dönüşü (aynı başlık, yeni URL) → ad, gizleme, grup ve sıra korunur.
4. `CustomOrderTest`: önek uygulama (eksik/yinelenen anahtar); yeni öğeler sonda; yukarı/aşağı/başa; önek uzunluğu kuralı; sınır reddi.
5. `ChannelOrderTest`: yuva düzeni diğer kategorileri oynatmaz; kategori içinde numara artan; ayraçlar; özel sıra yokken aynı örnek.
6. `HiddenItemsTest`: kanal/film/dizi gizleme; dizinin `m3uEpisodes` bölümleri; hiçbir şey gizli değilken aynı örnek.
7. `LibraryViewsTest`: `favoriteOrder` birleşimi; grup görünümünde gizli ve eksik sayıları; tür bölümlerinde grup sanal kategorileri; `@group` kategorisinin yayında korunması (saf yardımcı).
8. `TitleIndexTest` (genişletme): özel ad ve sağlayıcı adıyla bulma. `EpgSourceTest`: eşleşme `displayName`'den etkilenmez.
9. `SourceConnectionTest`: doğrulama tablosu (§7.2); `toString` değer sızdırmaz; `SourceRegistryCodec` gidiş-dönüş; bağlantısız eski JSON okunur; varsayılan bağlantıda kodlama eski biçimle bayt bayt aynı (sabit fixture).
10. `RequestHeadersTest`: katalog başlıkları; rehberde host kapsamı (aynı host → hepsi, başka host → yalnız UA); oynatma önceliği; User-Agent haritada yok.
11. `SourceTest` (MockWebServer, genişletme): M3U ve Xtream isteklerinde özel UA/Referer/ek başlık; varsayılan "NEXTV/<sürüm> AndroidTV".
12. `EpgSourceTest` (MockWebServer): plan başlıkları; dış hosta yalnız UA.
13. `RefreshScheduleTest` ve `GuideStalenessTest`: aralıklar, OFF, gelecek tarih, 30 dk geri çekilme, gecikme sınırları.
14. `HomeLibraryTest`: favori rafı grup sırasıyla. `FavoriteTargetsTest`: sıralı `migrateList`.

**Enstrümantasyon (`app/src/androidTest`):**
- `LibraryEditingFlowTest`:
  - Kanal: basılı OK → Taşı → Aşağı ×2 → OK; sıra ve numara kalıcı; Geri iptal eder.
  - Kategori taşıma.
  - Kanal adlandırma: satırda, oynatıcı başlığında ve aramada iki ad.
  - Gizle → kaybolur → Ayarlar'dan geri açılır.
  - Semantik: "Taşınıyor", canlı bölge metni.
  - fontScale 1,5: hızlı menü kayar, odaktaki satır görünür.
- `FavoriteGroupsFlowTest`: tek grupta yıldız davranışı aynı; "Yeni gruba ekle…"; iki grupta ayrıntı, oynatıcı ve hızlı menü seçici açar; Favoriler sütununda gruplar; grup içinde taşıma; silme onayında ilk odak Vazgeç.
- `SourceConnectionFlowTest`: Kurulum → Bağlantı ayarları → UA → Kütüphaneyi aç; MockWebServer UA'yı görür; HLS fixture'ında oynatma isteği UA ve ek başlığı taşır; Bilgiler'de "özel".
- `MultiSourceFlowTest` (genişletme): kaynak kaldırınca grup üyeliği, ad ve gizleme budanır.
- `SourceRegistryStorageTest` (genişletme): bağlantılı şifreli kayıt yeniden açılışta korunur.
- Ayarlar → Otomatik güncelleme seçimi kalıcı (kısa akış). Zamanlayıcı mantığı JVM'de.
- Yeni bütün denetimlere `testTag` verilir (`quick-move`, `quick-rename`, `quick-hide`, `fav-group:<id>`, `move-status`, `connection-ua` …). Testler metne bağlı kalmaz; yerelleştirme de kolaylaşır.

## 13. Aşamalar ve efor (mühendis-günü)

1. **Bağlantı ayarları** (§7): 1,5
2. **Otomatik güncelleme** (§8 + Ayarlar REFRESH): 1,5
3. **Temel ve geçiş:** `LibraryCustomizations`, `CustomIndex`, `LibraryViews`, `liveChannels`, `channelContext`, `displayName` tesisatı, sanal kategori koruması; görünür UI yok, rehber sırası ve CH± düzelir: 2
4. **Gizleme ve yeniden adlandırma** (hızlı menü genişlemesi, diyaloglar, Gizlenen içerikler): 1,5
5. **Favori grupları** (seçici, sütun, Ayarlar sayfası, ana sayfa sırası): 2
6. **Taşıma kipi** (kategori, kanal, grup, grup öğesi; Başa taşı; sıfırlama sayfası): 2

Büyük yazı, TalkBack ve enstrümantasyon dengelemesi: 1. **Toplam yaklaşık 11,5.**

Belgeler (uygulama sırasında): DECISIONS'a yeni karar kaydı (öneri olarak işaretli), BACKLOG'a yeni BKL, SCREENS (taşıma kipi, Ayarlar sayfaları), NATIVE_UI (taşıma çipi ve hap), TIVIMATE_GAP satır durumları, STATUS/HANDOFF.

## 14. Yerelleştirmeye hazırlık (kullanıcının dil sorusuyla ilgili)

- Bütün yeni metinler Türkçe ve mevcut desende satır içi.
- Kural: yeni kullanıcı metni **composable'larda** kalır. Veri katmanında yalnız doğrulama `SourceFailure` metinleri bulunur (mevcut desen).
- Her denetimin `testTag`'i vardır. Ayrı bir yerelleştirme geçişi (`strings.xml`, `values-en`) bütün uygulamayı birlikte taşır.
- Ad karşılaştırmaları `Locale("tr")` ile yapılır; bu, cihaz dilinden bağımsızdır.
