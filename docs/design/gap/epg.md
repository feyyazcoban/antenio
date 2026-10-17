# EPG düzeltme (çoklu XMLTV, saat kaydırma, elle eşleme, eşleşme durumu) + Ara'da program araması

18 Ekim 2026 · TiviMate gap tasarımı (`epg`), salt okunur tasarım ajanı; öneriler kullanıcı kararı değildir.

**Özet:** Not: Aktarılan kullanıcı isteği satış stratejisi, fiyat, yabancı dil davranışı ve farklı ülkelerden satın alma üzerineydi. Bu küme o isteği karşılamıyor. Yalnız iki kesişme noktası ele alındı: rehberdeki program adlarının hangi dilde seçileceği (bugün `XmltvParser(language = "tr")` sabit) ve EPG düzeltmenin ücretsiz mi ücretli mi olacağı. Fiyat ve ülkeler arası Play Billing ayrı bir tasarım gerektiriyor.
Tasarımın kendisi şöyle: kaynak başına en çok 4 rehber adresi (otomatik rehber + özel adresler) olacak, sıralama öncelik anlamına gelecek. Saat kaydırma rehber adresi başına tutulacak ve sorgu anında uygulanacak, yani yeniden indirme gerekmeyecek. Elle eşleme ve "Rehber yok" seçeneği şifresiz cihaz tercihinde (`guide:<sourceId>`) duracak. Rehber veritabanı şeması v2 olacak: `feeds` ve `titles` tabloları, aranabilir kanal adları, `programmes.title_id` ve dizini eklenecek. Eski rehberler bayat sayılıp bir kez yeniden indirilecek; bu arada eski veriler eski sorgu yoluyla gösterilmeye devam edecek. Bir adres alınamazsa diğerleri yayımlanacak, alınamayan adresin önceki verisi taşınacak.
Arayüz yeni bir Rehber ayarları sayfası (adresler, kaydırma ve önizleme, eşleşme özeti), Kanal eşleşmeleri listesi, rehber ızgarasında ve Canlı TV'de basılı OK ile açılan "Rehber eşlemesi" penceresi ve Ara'da "Programlar" satırı olarak eklenecek. Program araması bütün programları belleğe almıyor: önce ayrı başlıklar tablosunda LIKE ile eşleşen başlıklar bulunuyor, sonra yalnız o başlıkların yayınları dizinle getiriliyor.
Toplam efor yaklaşık 13 mühendis-günü, dört ayrı yayımlanabilir aşamada. Yeni bağımlılık yok.

**Tahmini iş:** 13 gün

**Aşamalar:**
1. 1. Çoklu rehber adresi ve öncelik (5 g): SourceConfig.guides ile şifreli kayıt codec'i, EpgPlan genişlemesi (4 sınırı ve skipped, sıra bağımsız binding), şema v2 (feeds/titles/aliases.folded/title_id ve dizin), eski rehberleri bayat sayıp yeniden indirme ve bu arada eski sorgu yolu, adres başına sonuç ve önceki veriyi taşıma, öncelikli eşleşme, Rehber ayarları sayfasının adresler bölümü (ekle/düzenle/sırala/kaldır/otomatiği geri ekle), JVM ve Android testleri, D072 ve EPG.md.
2. 2. Saat kaydırma (1,5 g): GuideTuning deposu (guide:<id>), sorgu anında kaydırma (indirme yok), rehber adresi penceresinde −30/+30/Sıfırla ve canlı önizleme, rehber satırlarının yenilenmesi, testler.
3. 3. Elle eşleme ve görünür eşleşme durumu (4 g): resolveMatch'e MANUAL türleri, aday araması, Rehber eşlemesi penceresi, rehber ızgarasında basılı OK ile Kanal seçenekleri, Canlı TV ve ana sayfa hızlı menüsü, Kanal eşleşmeleri sayfası (süzgeç ve arama), Rehber ayarlarında eşleşme özeti, yeni boş durum metinleri, büyük yazı ve TalkBack testleri.
4. 4. Ara'da program araması (2,5 g): titles üzerinde LIKE ve dizinli yayın sorgusu, ters eşleşme haritası, ProgrammeHit gruplaması, Programlar düğmesi ve satırı, ProgrammeCard, ortak program ayrıntısı ve Rehberde göster, odak dönüşü, testler.

**Kullanıcı kararları (varsayılanla):**
- Aynı kanal kimliği birden çok rehber adresinde varsa ne olsun? (Bugünkü kural ve test bunu 'belirsiz' sayıp atamıyor; bu kural kalırsa ikinci rehber neredeyse hiç işe yaramaz.) → varsayılan: Kullanıcının adres sırasına göre, o kanal için programı olan ilk rehber kullanılsın; hiçbirinde program yoksa kanalı tanımlayan ilk rehber. Belirsiz ad eşleşmesi yine hiçbir zaman atanmaz. EPG.md madde 3 ve ilgili test buna göre güncellenir.
- EPG düzeltme ve program araması ücretsiz sürümde mi olacak, ücretli pakette mi? (Gelir modeli PRD §12'de ve TIVIMATE_GAP'te açık; kullanıcının satış stratejisi sorusuyla bağlantılı.) → varsayılan: Ücretsiz inşa edilsin, ücret bayrağı eklenmesin. Rehberin güvenilirliği temel deneyimin parçası; ücretli sınır kararı verilince tek bir bayrakla kapatılabilecek biçimde ayrı tutuluyor.
- Uygulama yabancı dillerde yayımlanınca rehberdeki program adı, açıklama ve kategori hangi dilde seçilsin? (Bugün XmltvParser(language = "tr") sabit.) → varsayılan: Uygulama dili kararı verilene kadar 'tr' kalsın. Karar verilince tercih sırası: uygulama dili, cihaz dili, dil etiketi olmayan, herhangi biri. Şema değişmez; dil değişince rehber bir kez yeniden indirilir.

**Riskler:**
- Belgelenmiş iki rehber sözleşmesi değişiyor: aynı kimliğin birden çok rehberde birleştirilmemesi ve 'bir adres başarısızsa hiçbiri yayımlanmaz' kuralı. D072 ve EPG.md'ye yazılmadan uygulanmamalı; mevcut test multipleFeedsDoNotSilentlyMergeConflictingChannelIds yeniden yazılmalı.
- Güncellemeden sonra her kaynağın rehberi şema v2 için bir kez yeniden indirilir (sırayla, tek bağlantıyla). Yavaş bağlantıda ilk açılışta program araması bir süre 'Rehber güncelleniyor' der.
- Android SQLite'ta execSQL("ROLLBACK TO …") dış transaction'ı bitirir ve transaction içinde ATTACH yapılamaz. Başarısız adres temizliği DELETE ile, önceki verinin taşınması ayrı salt okunur bağlantı ve cursor ile yapılmalı.
- Dört büyük rehberde 96 MiB staging sayfa sınırına yaklaşılabilir. SQLITE_FULL artık rehber düzeyinde hata olarak ele alınıyor, ama kullanıcı bir adresi kaldırmak zorunda kalabilir. Gerçek boyut ölçülmedi.
- Elle eşleme ve kaydırma şifresiz SharedPreferences'ta duruyor (favorilerle aynı düzey; kimlik bilgisi yok, M3U kanal kimlikleri özet). Bu tercih belgeye yazılmalı. 2.000 eşleme yaklaşık 160 KB tercih dosyası demek.
- Basılı OK algılaması kumandanın tuş tekrarına ya da isLongPress bayrağına bağlı; bazı kumandalarda çalışmayabilir. TalkBack eylemi ve Canlı TV hızlı menüsü yedek yol olarak kalıyor. Fiziksel 65U95A'da denenmeli.
- Eşleşme raporu 4–10 bin kanal için yaklaşık 1 sn sürer (tahmin, ölçülmedi). Parçalı okuma kilidi kısa tutuyor, ama düşük bellekli cihazda süre uzayabilir.
- Sabit kaydırma, sağlayıcının yaz saati hatasını düzeltmez (Europe/Istanbul'da yaz saati yok, DE/UK rehberlerinde sorun çıkabilir). Bölge tabanlı yorumlama kapsam dışı.
- XmltvParser dili 'tr' sabit. Yabancı dilde yayımlanınca program adları yanlış dilde seçilebilir; bu kullanıcının 'yabancı dillerde nasıl davranacak' sorusuyla doğrudan ilgili.
- Bu çalışmayı başlatan kullanıcı mesajı (satış stratejisi, fiyat, yabancı dil, çok ülkeli satın alma) bu tasarımla karşılanmıyor. Fiyatlandırma, Play Billing ülke ve para birimi desteği ile uygulama yerelleştirmesi ayrı bir tasarım gerektiriyor.

**Dokunulacak dosyalar:**
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/Models.kt`: `SourceConfig.epgUrl` yerine `guides: List<GuideAddress>`; `GuideAddress` ve `legacyGuides`; `validated()` adres listesi kuralları
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/SourceRegistry.kt`: Codec'e eklenerek genişletilen `guides` alanı ve eski sürümler için `epgUrl` yazımı; okumada eski alana geri dönüş
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/DeviceStore.kt`: Eski tek kaynak biçiminde guides dönüşümü; `guideTuning(sourceId)` okuma/yazma (`guide:<id>`); `saveSources` ve `forgetSource` temizliği
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/EpgModels.kt`: `EpgPlan` (PlannedFeed, sıralı adreslerle binding, skipped; 4 aşılınca istisna yok); `EpgMatchKind` ve `EpgMatch` genişlemesi; `EpgSnapshotInfo.schema/feeds`
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/GuideMatching.kt`: YENİ: saf `resolveMatch`, `guideSearchSeed`, kaydırma yardımcıları, program arama sıralaması ve `ProgrammeHit` gruplaması
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/GuideTuning.kt`: YENİ: `GuideTuning`, `GuideMapping`, JSON codec ve sınırlar
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/EpgStore.kt`: Şema v2 (feeds, titles, aliases.display/folded, programmes.title_id, dizin); `feedDone/discardFeed/carry`; öncelik, ayar ve kaydırmalı `query/queryMany`; `candidates`; `searchProgrammes`; `previousFile`; eski şema okuma yolu
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/EpgRepository.kt`: Adres başına sonuç; başarısız adresin satırlarını silme ve önceki veriyi taşıma; hepsi başarısızsa eski davranış; SQLiteFull eşlemesi
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/XmltvParser.kt`: display-name ham metninin alias'a taşınması (sink sözleşmesi aynı); dil parametresinin dışarıdan verilebilmesi
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/GuideController.kt`: Sahibi binding'e göre koruma ve öncelik haritası; `accept` binding denetimi; `tuningChanged`; `matchReport` (parçalı, önbellekli); `candidates`; `searchProgrammes`; `offsetPreview`; eski şemayı bayat sayma
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/TvViewModel.kt`: `Route.GUIDE_SETUP` ve `GuideSetupState`; adres, kaydırma ve eşleme fonksiyonları (`saveGuideUrl` yerine `saveGuides`); `SourceDraft.guides`; program araması durumu ve işi; `guideAt`; `back()`
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/GuideRepair.kt`: YENİ: GuideSetupScreen (FEEDS/MATCHES), GuideAddressDialog, GuideFeedDialog ve OffsetStepper, GuideMatchesPage, GuideMappingDialog
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/GuideSettings.kt`: `GuideSourceDialog` kaldırılır; `guideStatusText` adres başına satırlarla genişler; zaman dilimi penceresi kalır
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SourcesScreen.kt`: Rehber ayarları düğmesi rotayı açar; `info-source:<id>` odak dönüşü
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/GuideScreen.kt`: Basılı OK ile Kanal seçenekleri; yeni eşleşme türleri için boş metinler; `ProgrammeDetailDialog` ortak bileşene çıkar; TalkBack onLongClick
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PilotApp.kt`: GUIDE_SETUP rota anahtarı; `QuickActions` guideMapping parametresi; karmaşık listesi olan kaynakta form notu
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/LibraryScreen.kt`: Hızlı menüde Rehber eşlemesi; MANUAL_NONE için 'Rehber kapalı'
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/HomeScreen.kt`: Canlı kart hızlı menüsünde Rehber eşlemesi
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SearchScreen.kt`: Programlar düğmesi ve satırı, ProgrammeCard, ayrıntı penceresi, yer tutucu ve boş metinler, program odak anahtarı
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/`: YENİ: GuideAddressTest, GuideTuningCodecTest, GuideMatchingTest, GuideOffsetTest, ProgrammeSearchTest; EpgSourceTest genişler
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/EpgStorageTest.kt`: Şema v2, göç, öncelik, kısmi hata ve taşıma, eşleme ve kaydırma, aday ve program araması; çakışan kimlik testi yeniden yazılır
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/GuideFlowTest.kt`: Rehber ayarları, kaydırma, basılı OK ile eşleme akışları; büyük yazı ve TalkBack; adres testi taşınır
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/SearchNavigationTest.kt`: Program satırı, ayrıntı, Rehberde göster, odak dönüşü
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/SourceRegistryStorageTest.kt`: Şifreli guides gidiş-dönüşü ve eski blob
- `docs/architecture/EPG.md`: Öncelik, kısmi yayın, şema v2, ayar deposu, program araması, eşleme sözleşmesi
- `docs/decisions/DECISIONS.md`: D072 (öneri; sahip onayı yazılana kadar öneri olarak işaretli)
- `docs/design/SCREENS.md`: Rehber ayarları, Kanal eşleşmeleri, Rehber eşlemesi, Ara Programlar ekran davranışları
- `docs/product/TIVIMATE_GAP.md`: İki satırın durumu

---

# EPG düzeltme ve Ara'da program araması — uygulama tasarımı

> Kapsam notu: Bu çalışmayı başlatan kullanıcı mesajı fiyatlandırma, yabancı dil davranışı ve çok ülkeli satın alma hakkındaydı. Bu belge yalnız TIVIMATE_GAP'teki "EPG düzeltme" ve "EPG'de program araması" satırlarını tasarlar. O istekle kesişen iki nokta §12'de karar olarak bırakıldı: program adının dili ve özelliğin ücretli olup olmadığı.

## 1. Bugünkü durum (kod okuması)

- `data/EpgModels.kt` `EpgPlan.create` şöyle çalışıyor: `SourceConfig.epgUrl` doluysa yalnız o adres kullanılır, Xtream'de `xmltvUrl()`, M3U'da `catalog.epgUrls` (başlıktan en çok 4 adres). 4'ten fazlası `SourceFailure` fırlatır. `binding` kaynak alanlarıyla adreslerin **sıralı** listesinin özetidir.
- `data/EpgStore.kt` şemasında `channels(feed,id)`, `aliases(name,feed,channel)`, `programmes(feed,channel,start,stop,title,description,category)` ve `snapshot` var. `feed` plan dizinidir. `match()` kimlik varsa `SELECT feed,id FROM channels WHERE id=? LIMIT 2` sorgusunu çalıştırır; iki rehberde aynı kimlik bulunursa sonuç `AMBIGUOUS` olur (test: `EpgStorageTest.multipleFeedsDoNotSilentlyMergeConflictingChannelIds`). Ad araması `epgNameKey` ile yapılır, belirsizlikte atama olmaz. Elle eşleme ve saat kaydırma yok.
- `data/EpgRepository.refresh` rehberleri sırayla tek bir `GuideWriter` iş parçacığında indirir; herhangi biri başarısız olursa hiçbiri yayımlanmaz. `XmltvParser` SAX ile akıştan okur (128 MiB, 1 M program, 20 bin kanal sınırı) ve `language = "tr"` sabittir.
- `data/GuideController` bir sahibi `old.source == source && binding eşit` ise korur. `accept` kaydedilmiş kaynağın tam eşitliğine bakar. Rehber 6 saatte bayat sayılır; hatadan sonra 5 dakika bekler.
- Arayüz: `ui/SourcesScreen` → Bilgiler → `GuideSourceDialog` (`ui/GuideSettings.kt`: tek "Adresi düzenle", `saveGuideUrl`). `ui/GuideScreen` boş hücrede `guideEmptyText` ile nedeni yazar. `ui/SearchScreen` yalnız `TitleIndex` başlıklarında arar.

## 2. Ürün kuralları

Korunanlar: belirsiz **ad** eşleşmesi hiçbir zaman atanmaz. Yanlış kimlik adla sessizce düzeltilmez (`MISSING_ID`). Saatler UTC anı olarak saklanır ve seçili bölgede gösterilir. Akıştan okuma ve indirmenin tek tek yapılması sürer. Ağ katmanından ham URL çıkmaz. Rehber indirmek yayın açmaz. Program araması da kaydırma önizlemesi de yayın oynatmaz.

Değişenler (DECISIONS'ta D072 olarak ve `docs/architecture/EPG.md`'de kayda geçirilmeli):
1. **Öncelik:** Aynı kimlik birden çok rehberde varsa, kullanıcının sırasında **o kanal için programı olan ilk** rehber seçilir. Hiçbirinde program yoksa kanalı tanımlayan ilk rehber seçilir. Ad eşleşmesinde de en üstteki rehberden başlanır: bir rehberde iki aday varsa sonuç `AMBIGUOUS` olur ve alttaki rehbere geçilmez. Sahibin onayına bağlı (§12-1).
2. **Kısmi yayın:** Bir adres alınamazsa diğerleri yayımlanır. Alınamayan adresin önceki verisi (adres değişmemişse) taşınır ve "Önceki veriler kullanılıyor" olarak gösterilir. Bütün adresler başarısızsa bugünkü gibi eski rehber olduğu gibi kalır.

## 3. Veri modeli

### 3.1 Rehber adresleri (`data/Models.kt`, şifreli kayıtta)
```kotlin
data class GuideAddress(val id: String, val url: String) {      // url == "" → otomatik rehber
    val automatic get() = url.isEmpty()
    override fun toString() = "GuideAddress(id=$id, automatic=$automatic, url=REDACTED)"
    companion object { const val AUTO_ID = "auto"; val Auto = GuideAddress(AUTO_ID, "") }
}
// SourceConfig: `epgUrl` kaldırılır → `val guides: List<GuideAddress> = listOf(GuideAddress.Auto)`
fun legacyGuides(epgUrl: String) = if (epgUrl.isBlank()) listOf(GuideAddress.Auto) else listOf(GuideAddress(UUID, epgUrl))
```
- `validated()` şunları denetler: en çok 4 kayıt, en çok bir otomatik kayıt, kimlikler tekil, her özel adres http(s) ve kullanıcı bilgisi içermiyor (bugünkü metin: "Rehber için geçerli bir http veya https adresi gir."), adresler tekil ("Bu adres zaten ekli."). Liste boş olabilir; bu, rehberin kapalı olduğu anlamına gelir.
- `SourceRegistryCodec`: `version` 1 olarak kalır, alan eklenerek genişletilir. Yazarken `"guides":[{"id","url"}]` ile birlikte eski sürümler için `"epgUrl"` de yazılır (listede otomatik kayıt yoksa ilk özel adres, varsa ""). Okurken `guides` varsa o kullanılır, yoksa `legacyGuides(epgUrl)`. `DeviceStore.loadSource/saveSource` (eski tek kaynak biçimi) aynı dönüşümü yapar.
- `SourceDraft`'a `guides: List<GuideAddress>?` eklenir. `editSource` iki durumu ayırır: liste `[Auto]` ya da tek özel adres ise bugünkü alan kullanılır, `guides = null`. Değilse alan gizlenir ve `guides` korunur. `connectDraft` alandan türetirken adres değişmemişse eski `id`'yi korur; kaydırma ve eşlemeler o kimliğe bağlı olduğu için bu önemli.

### 3.2 İstek planı (`EpgPlan`)
```kotlin
internal class PlannedFeed(val key: String, internal val url: String, val origin: String)  // origin = stableId(owner+"\u0000"+url)
class EpgPlan(val owner: String, val binding: String, internal val feeds: List<PlannedFeed>, val skipped: List<String>)
```
- Genişletme kaynak sırasıyla yapılır. `Auto` Xtream'de tek bir `xmltvUrl()` (anahtar `auto`), M3U'da `catalog.epgUrls` (anahtarlar `auto`, `auto#1`, `auto#2`…) olur. Tekrarlanan adresler atlanır. İlk 4 benzersiz adres alınır, fazlası `skipped` listesine girer; **istisna fırlatılmaz**.
- `binding = stableId([id,type,address,username,password] + urls.sorted())`. Sıra değişikliği binding'i değiştirmez, dolayısıyla yeniden indirme olmaz. Tek adresli kaynaklarda dosya adı bugünküyle aynı kalır.
- Öncelik sorgu anında kaynaktaki sıradan okunur: `priority: List<String>` (feed anahtarları). Listede olmayan rehberin satırları yok sayılır; böylece adres kaldırıldığı anda etkisi görünür.

### 3.3 Ayarlar: kaydırma ve elle eşleme (`data/GuideTuning.kt`, yeni)
`DeviceStore` (`device_state` tercihleri) içinde `guide:<sourceId>` anahtarında JSON olarak durur. Kimlik bilgisi içermez (favoriler `f:<id>` gibi); M3U kanal kimlikleri zaten özettir.
```json
{"v":1,"offsets":{"auto":60,"9f2c…":0},
 "map":{"live:123":{"f":"auto","c":"trt1.tr","n":"TRT 1 HD"},"m3u:ab12…":{"none":true}}}
```
```kotlin
data class GuideTuning(val offsets: Map<String, Int> = emptyMap(), val mappings: Map<String, GuideMapping> = emptyMap())
sealed interface GuideMapping { data object None : GuideMapping; data class Channel(val feed: String, val channel: String, val name: String) : GuideMapping }
```
- Anahtar `entry.localId ?: entry.id`. Kaydırma −720…+720 dakika, 30 dakikalık adımlarla. Kaynak başına en çok 2.000 eşleme ("En fazla 2.000 kanal elle eşlenebilir."). `n` en çok 80, `c` en çok 512 karakter. Bozuk JSON boş ayar olarak okunur; uygulama çökmez.
- Temizlik: `saveSources` kaldırılan kaynak için `guide:$id` anahtarını, `forgetSource()` ise `guide:` önekli bütün anahtarları siler. Bir rehber adresi kaldırılınca o adrese ait kaydırma ve eşlemeler, kayıt yazımı başarılı olduktan sonra budanır.

### 3.4 Rehber veritabanı, şema v2 (`EpgStore.Stage`)
```sql
PRAGMA user_version = 2;
CREATE TABLE feeds (feed INTEGER PRIMARY KEY, key TEXT NOT NULL UNIQUE, origin TEXT NOT NULL,
  state TEXT NOT NULL /* ok|carried|failed */, updated INTEGER NOT NULL, channels INTEGER NOT NULL,
  programmes INTEGER NOT NULL, invalid INTEGER NOT NULL, error TEXT /* yalnız sabit SourceFailure metni */);
CREATE TABLE channels (feed INTEGER NOT NULL, id TEXT NOT NULL, PRIMARY KEY(feed,id));
CREATE INDEX channel_id ON channels(id);
CREATE TABLE aliases (name TEXT NOT NULL, feed INTEGER NOT NULL, channel TEXT NOT NULL,
  display TEXT NOT NULL, folded TEXT NOT NULL, PRIMARY KEY(name,feed,channel));   -- folded = TitleIndex.normalize
CREATE TABLE titles (id INTEGER PRIMARY KEY, title TEXT NOT NULL UNIQUE, folded TEXT NOT NULL);
CREATE TABLE programmes (feed INTEGER NOT NULL, channel TEXT NOT NULL, start INTEGER NOT NULL, stop INTEGER NOT NULL,
  title_id INTEGER NOT NULL, description TEXT NOT NULL, category TEXT NOT NULL, PRIMARY KEY(feed,channel,start,stop,title_id));
CREATE TABLE snapshot (...bugünkü sütunlar...);
-- seal() içinde, eklemeler bittikten sonra:
CREATE INDEX programme_title ON programmes(title_id, start);
```
- Başlık metni bir kez saklanır. Yazıcı tarafında `HashMap<String, Long>` önbelleği tutulur (en çok 200 bin); dolarsa `INSERT OR IGNORE` + `SELECT id` kullanılır. Programlarda başlık metni yerine tamsayı kimlik durduğu için yeni dizinin boyutu dengelenir.
- `EpgSnapshotInfo`'ya `schema: Int` ve `feeds: List<EpgFeedInfo(key, state, updated, channels, programmes, invalid, error)>` eklenir.
- **Tuzak:** Android'de `execSQL("ROLLBACK TO …")` `STATEMENT_ABORT` olarak sınıflanır ve dış transaction'ı bitirir. Ayrıca transaction içinde `ATTACH` yapılamaz. Bu yüzden savepoint kullanılmaz; başarısız rehberin satırları `DELETE … WHERE feed=?` ile silinir.

### 3.5 Eşleşme modeli
`EpgMatchKind` = `ID, NAME, MANUAL, MANUAL_NONE, MANUAL_MISSING, MISSING_ID, AMBIGUOUS, NONE`.
`EpgMatch(kind, channel: EpgChannelRef?, feedKey: String? = null, name: String? = null)`.

Saf çözümleyici (`data/GuideMatching.kt`, JVM'de test edilebilir):
```kotlin
data class FeedHit(val feed: Int, val key: String, val channel: String, val hasProgrammes: Boolean)
fun resolveMatch(entry: MediaEntry, manual: GuideMapping?, priority: List<String>,
                 idHits: List<FeedHit>, nameHits: List<FeedHit>): EpgMatch
```
Sıra şöyle:
1. LIVE değilse `NONE`.
2. `manual == None` → `MANUAL_NONE`.
3. `manual is Channel` → o rehberde kanal varsa `MANUAL`, yoksa `MANUAL_MISSING`. Bu durumda otomatik eşlemeye düşülmez.
4. `epgId` doluysa, önceliğe göre sıralanmış `idHits` içinde programı olan ilk sonuç, yoksa ilk sonuç → `ID`. Sonuç yoksa `MISSING_ID`.
5. `epgId` boşsa her rehber için öncelik sırasıyla bakılır: o rehberde tek benzersiz kanal → `NAME`, birden fazla → `AMBIGUOUS` (durulur), hiç yoksa sonraki rehbere geçilir. Hiçbir rehberde yoksa `NONE`.

`EpgStore.match` yalnız ham sonuçları toplar: `SELECT c.feed, c.id, EXISTS(SELECT 1 FROM programmes p WHERE p.feed=c.feed AND p.channel=c.id) FROM channels c WHERE c.id=?` ve bugünkü alias sorgusu, `LIMIT` olmadan; bir rehberdeki adlar en çok 32 ile sınırlı.

### 3.6 Göç
- Kayıt: alan eklenerek genişletildiği için ayrı göç adımı yok. Eski `epgUrl` okunurken `guides` listesine çevrilir. Eski sürüm yeni kaydı yine okuyabilir.
- Rehber dosyaları: `user_version < 2` olan dosya **bayat** sayılır (`GuideController.stale`: `info.schema < EpgStore.SCHEMA`). İlk `ensure()` çağrısında sırayla yeniden indirilir ve atomik yayın aynı dosya adının üzerine yazar. Yeni dosya gelene kadar eski şema eski sorgu yoluyla gösterilmeye devam eder (`EpgLegacySchema`: bugünkü `query`/`match` SQL'i). Bu sırada dizin = plan sırası kabul edilir, eşleme ve kaydırma uygulanır, kanal araması alias'lar Kotlin'de katlanarak taranır. Program araması "Rehber güncelleniyor" der. İndirme başarısız olursa eski veri korunur.
- Birden çok başlık adresi olan eski M3U rehberlerinde binding formülü değiştiği için dosya bulunamaz ve yeniden indirme beklenir. Durum metni: "Kayıtlı rehber bulunmuyor."

## 4. Ağ ve ayrıştırma (`EpgRepository`, `EpgStore`)
- `refresh(plan, …)`: rehberler yine sırayla, tek `GuideWriter` iş parçacığında, `network` mutex'i altında indirilir. Paralel HTTP yoktur.
- Her rehber için akış şöyle:
  1. `download(url, window, stage.sink(i))` başarılıysa `stage.feedDone(i, report, "ok")`.
  2. İptal (`CancellationException`) dışındaki bir `SourceFailure` ya da `SQLiteFullException` olursa önce `stage.discardFeed(i)` çağrılır.
  3. Önceki yayımlanmış dosyada aynı `key` ve `origin` varsa `stage.carry(i, previousFile)` çalışır: önceki dosya ayrı bir salt okunur bağlantıyla açılır, satırlar cursor ile 500'lük parçalar halinde sink'e akıtılır, başlıklar yeniden kimliklenir, durum `carried` olur ve hata metni saklanır. Önceki veri yoksa durum `failed`.
- Hiçbir rehber `ok` değilse ilk hata fırlatılır ve eski rehber kalır. `seal()` dizini kurar ve feeds ile snapshot satırlarını yazar.
- `SQLiteFullException` için rehber düzeyinde metin: "Bu rehber cihazdaki rehber sınırını aşıyor." 96 MiB sayfa sınırı değişmez.
- `EpgStore.previousFile(owner)`: `guides/` içinde `"$owner-"` ile başlayan dosya (yayın diğerlerini sildiği için tektir).
- Ayrıştırıcıya tek değişiklik: `aliases.display/folded` için ham `display-name` korunur. Dil tercihi şimdilik `"tr"` kalır (§12-3).

## 5. GuideController ve ViewModel API
**GuideController** (`data/GuideController.kt`):
- `synchronize`: binding aynıysa ve plan hatası yoksa sahip **korunur**; yalnız `priorities[id]` güncellenir. Bu sayede ad değişikliği ya da sıra değişikliği indirmeyi iptal etmez. `accept = { currentPlan(savedSource, catalogs.readEpgFeeds(saved))?.binding == plan.binding }`.
- `tuningChanged(id)`: ayar önbelleğini siler ve `GuideState.revision++` yapar. Ekranlardaki `guideRows` ve kanal listeleri yeniden sorgular, indirme olmaz.
- `query(entries, window)` → `store.queryMany(plan, priority, tuning, entries, window)`. Kaydırma şöyle uygulanır: SQL penceresi `(from−off, until−off)`, dönen `start/end + off`.
- `suspend fun matchReport(sourceId, entries): GuideMatchReport` → `(kinds: Map<entryId, EpgMatch>, counts: Map<EpgMatchKind, Int>, reverse: Map<EpgChannelRef, List<entryId>>)`. 500'lük parçalarla `read()` çağrılır, böylece kilit uzun süre tutulmaz; parçalar arasında `ensureActive`. Önbellek anahtarı `(info.updated, tuningRevision, katalog örneği)`.
- `suspend fun candidates(sourceId, entry, query, limit = 50): List<EpgChannelCandidate(feedKey, channel, display, nowTitle?)>`.
- `suspend fun searchProgrammes(query, entriesBySource, now): ProgrammeSearchResult(hits, note)`.
- `suspend fun offsetPreview(sourceId, feedKey, minutes): EpgProgramme?`. Örnek kanal o kaynağın son izlenen canlı kanalıdır (o rehbere eşliyse), değilse rehberdeki programı olan ilk eşleşen kanal.

**TvState** eklemeleri: `guideSetup: GuideSetupState?(sourceId, page: FEEDS|MATCHES, returnRoute, returnFocus)`, `guideMatchFilter`, `searchProgrammesOnly: Boolean`, `programmeResults: List<ProgrammeHit>`, `programmeSearching`, `programmeSearchNote: String?`. Yeni rota `Route.GUIDE_SETUP`. `back()`: MATCHES → FEEDS, FEEDS → `returnRoute`; SOURCES'a dönerken `sourceManagerFocus = "info-source:<id>"` (SourcesScreen'in hedef filtresi bu anahtarı tanıyacak şekilde genişletilir).

**TvViewModel** fonksiyonları:
- `openGuideSetup(sourceId)`, `guideSetupPage(page)`, `guideMatchFilter(f)`
- `addGuideAddress(sourceId, url)`, `editGuideAddress(sourceId, id, url)`, `removeGuideAddress(sourceId, id)`, `moveGuideAddress(sourceId, id, delta)`, `restoreAutoGuide(sourceId)`: hepsi tek bir özel `saveGuides(sourceId, transform)` üzerinden geçer. Bu, `saveGuideUrl` mantığının genellenmiş hâlidir: `transactions.write` → `readRegistry` → `upsert` → `guides.synchronize`. Binding değiştiyse `refreshGuide(id)`. Hata `guideSaveError`'a yazılır.
- `guideOffset(sourceId, feedKey, minutes)`: 400 ms gecikmeli yazım ve `tuningChanged`. `suspend fun guideOffsetPreview(…)`.
- `mapGuideChannel(entry, target: Auto|None|Channel)`, `suspend fun guideCandidates(entry, query)`, `suspend fun guideMatch(entry): EpgMatch`, `suspend fun guideMatchReport(sourceId)`.
- `searchProgrammesOnly(on)`. `updateSearch()` içinden `updateProgrammeSearch()` çağrılır: 350 ms gecikme, ayrı `programmeGeneration`. `guideRevision` değişince sorgu doluysa yeniden çalışır.
- `guideAt(entryId, time)`: "Rehberde göster" için `route = GUIDE`, `guideChannelId`, `guideAnchor`.

## 6. Ekranlar ve kumanda akışları
Odak dili D064'e uyar: düğmeler (`Action`) mint dolgu alır; satır ve kart yüzeyleri `focusRing` ile beyaz 2 dp halka alır; ölçek büyümesi yoktur. Hareketi azalt açıkken `scrollToItem` kullanılır, yeni animasyon yoktur. Metin alanlarında klavye yalnız OK ile açılır, Geri önce klavyeyi kapatır (D058).

### 6.1 Rehber ayarları sayfası (`Route.GUIDE_SETUP`, FEEDS) — `ui/GuideRepair.kt`
Giriş: Kaynaklar → kart → **Bilgiler** → **Rehber ayarları** (mevcut `source-guide-settings` düğmesi artık bu sayfayı açar; `GuideSourceDialog` kaldırılır). Diğer giriş: rehberdeki Kanal seçenekleri → **Rehber ayarları**.

Yerleşim tek sütun ve kaydırılabilir; 960×540, büyük yazıda satırlar uzar.
- Başlık "Rehber ayarları"; alt satır "<kaynak adı> · Adresler üstten alta öncelik sırasıyla kullanılır."; sağda "Geri".
- Adres satırları (yüzey):
  - Birincil metin: "1 · Hesabın rehberi" (Xtream otomatik), "1 · Listenin rehberi (2 adres)" (M3U otomatik) ya da "2 · Özel rehber adresi". **Adres metni hiç gösterilmez.**
  - İkincil metin (birini gösterir): "10.588 program · 253 kanal · Saat: +1 sa", "İndiriliyor…", "Alınamadı: <hata> Önceki veriler kullanılıyor.", "Alınamadı: <hata>", "Henüz indirilmedi", "Listede rehber adresi yok", "Kullanılmıyor: en çok dört adres", "Kullanılmıyor: aynı adres üstte var".
- Düğmeler: "Rehber adresi ekle" (4 kayıtta devre dışı), "Hesabın rehberini geri ekle" / "Listenin rehberini geri ekle" (otomatik kayıt yoksa), "Rehberi yenile"/"Yenileniyor…", "İptal et".
- Durum metni: `guideStatusText`, adres başına satırlarla genişletilmiş hâli.
- Eşleşme özeti: "Kanal eşleşmesi: 402 / 4.525 kanal · 3 belirsiz · 89 kimliği rehberde yok · 2 elle". Hesaplanırken "Eşleşmeler hesaplanıyor…". Yanında **Kanal eşleşmeleri** düğmesi.
- İlk odak ilk adres satırındadır. Geri dönüş rotasına gider.

Kumanda akışı, ikinci XMLTV ekleme: Kaynaklar → kart → Sağ, **Bilgiler** → OK → Aşağı, **Rehber ayarları** → OK → Aşağı, **Rehber adresi ekle** → OK. "Rehber adresi ekle" penceresinde odak "XMLTV adresi" alanındadır (gizli alan, `secret`). OK klavyeyi açar → yaz → klavyenin İleri tuşu **Kaydet**'e götürür → OK. Pencere kapanır, yeni satır "İndiriliyor…" der ve odak o satıra gelir. Not metni: "http:// veya https:// ile başlayan XMLTV (.xml veya .xml.gz) adresi."

### 6.2 Rehber adresi penceresi (adres satırında OK)
Başlık "Hesabın rehberi" ya da "Rehber adresi 2". Önce odaklanan öğe kaydırma satırıdır:
- Etiket "Saat kaydırma".
- Değer metni (yalnız gösterim, `stateDescription`): "Kaydırma yok", "+1 sa", "−30 dk", "+2 sa 30 dk".
- Düğmeler **−30 dk**, **+30 dk**, **Sıfırla**. Büyük yazıda iki satıra kırılır (FlowRow).
- Yardım metni: "Programlar yayından erken görünüyorsa ileri, geç görünüyorsa geri al."
- Önizleme: "Şimdi (bu ayarla): <kanal> · <program> · 20:00–21:00" ya da "Önizleme için eşleşen kanal yok."

Diğer düğmeler: "Adresi düzenle" (yalnız özel adres), "Yukarı taşı", "Aşağı taşı" (uçlarda devre dışı; odak diğer düğmeye geçer), "Kaldır", "Kapat". Kaldır onay ister: "Rehber adresi kaldırılsın mı?" / "Bu adresin programları ve bu adrese yapılmış elle eşlemeler kaldırılır. Diğer adresler korunur." İlk odak **Vazgeç**'te, sonra **Kaldır**. Geri pencereyi kapatır ve odak satıra döner. Kaydırma her basışta önizlemeyi günceller ve indirme olmadan rehber satırlarını yeniler.

### 6.3 Kanal eşleşmeleri (`GUIDE_SETUP`, MATCHES)
- Başlık "Kanal eşleşmeleri · <kaynak>".
- Süzgeç düğmeleri "Sorunlu · N" (varsayılan: AMBIGUOUS, MISSING_ID, NONE, MANUAL_MISSING), "Elle · N" (MANUAL, MANUAL_NONE), "Eşleşen · N", "Tümü · N".
- "Kanal ara" alanı: catalog başlıkları `TitleIndex.normalize` ile süzülür.
- `LazyColumn` satırları yüzeydir: Canlı TV numarası (`catalogDisplayOrder`), logo, ad, durum metni ve "Rehber 2 · TRT 1 HD". OK eşleme penceresini açar.
- Boş süzgeç: "Bu süzgeçte kanal yok."
- Geri: klavye açıksa kapatır, değilse FEEDS sayfasına döner ve odak **Kanal eşleşmeleri** düğmesine gelir.

Durum metinleri: ID "Kimlikle eşleşti", NAME "Adla eşleşti", MANUAL "Elle eşlendi", MANUAL_NONE "Rehber kapalı", MANUAL_MISSING "Elle seçilen kanal rehberde yok", MISSING_ID "Kanal kimliği rehberde yok", AMBIGUOUS "Birden çok rehber kanalı uyuyor", NONE "Eşleşmedi".

### 6.4 Rehber eşlemesi penceresi (`GuideMappingDialog`, 600 dp)
- Başlık kanal adıdır (`displayTitle`; birden çok kaynak varsa kaynak adı da).
- Okunur satırlar: "Şu an: Kimlikle eşleşti · Hesabın rehberi · TRT 1 HD" ve kimlik varsa "Kanal kimliği: trt1.tr".
- "Rehberde kanal ara" alanı `guideSearchSeed(title)` ile önceden doldurulur: görünen ad, sonundaki HD/FHD/UHD/4K/SD/HEVC/H265 işaretleri atılmış hâliyle. Bu yalnız arama önerisidir; otomatik atama yapılmaz.
- Liste `GuideZoneDialog` gibi tam satırlara oturur:
  1. "Otomatik eşleşme" (elle eşleme yoksa onay işaretli).
  2. "Bu kanalda rehber gösterme".
  3. Adaylar: "TRT 1 HD" ve ikinci satırda "trt1.tr · Rehber 2 · Şimdi: Haberler" (şimdi bilgisi yalnız tek kesin program varsa).
- Seçim anında kaydedilir, pencere kapanır, odak açana döner ve satır yenilenir.
- Metinler: "Bu adla rehber kanalı bulunamadı. Daha kısa bir ad dene." / "İlk 50 sonuç gösteriliyor; aramayı daralt." / "Önce rehberi indir." (rehber yoksa yalnız ilk iki seçenek görünür).
- Geri: önce klavye, sonra pencere.

Aday sorgusu: kelime başına `folded LIKE '%w%' ESCAPE '\'` (AND ile bağlanır; `% _ \` kaçırılır) ve `channels.id LIKE`, `GROUP BY feed, channel LIMIT 51`. Sıralama: tam eşleşme > ön ek > içerir, ardından rehber önceliği, ardından ad.

### 6.5 Rehber ızgarası (`ui/GuideScreen.kt`)
- **Basılı OK** (≈500 ms): ızgaranın `onPreviewKeyEvent` içinde Enter KeyDown için `nativeKeyEvent.repeatCount == 1 || isLongPress` ise `held = true` yapılır ve "Kanal seçenekleri" penceresi açılır. Bunu izleyen KeyUp tüketilir, program açılmaz. Normal OK davranışı değişmez. TalkBack için ızgara düğümüne `onLongClick("Kanal seçenekleri")` eklenir.
- Kanal seçenekleri: **Canlı kanalı izle**, **Rehber eşlemesi**, **Rehber ayarları**, **Vazgeç**. Geri odağı aynı hücreye döndürür.
- `guideEmptyText` eklemeleri: MANUAL_NONE "Bu kanal için rehber kapalı", MANUAL_MISSING "Elle seçilen rehber kanalı bulunamadı". "Bilgi yok" koşuluna MANUAL de eklenir.
- Program ayrıntı penceresi Ara'da da kullanılmak üzere `ProgrammeDetailDialog` adlı ortak bir bileşene çıkarılır.

Akış: ızgarada kanal satırı → OK basılı tut → Aşağı, **Rehber eşlemesi** → OK → alan dolu, Aşağı → ilk aday → OK → pencere kapanır, satırda programlar görünür.

### 6.6 Canlı TV ve ana sayfa canlı kartları
`QuickActions(…, guideMapping: (() -> Unit)? = null)`: kaynağın rehberi yapılandırılmış bir LIVE kayıtta **Rehber eşlemesi** düğmesi görünür (`LibraryScreen` ChannelRow ve `HomeScreen` canlı kartları). MANUAL_NONE için ChannelRow'da "Program bilgisi yok" yerine "Rehber kapalı" yazar.

### 6.7 Kaynak formu (`ui/PilotApp.kt` SetupScreen)
Yeni kaynakta alan bugünkü gibidir: "XMLTV rehber adresi · isteğe bağlı" otomatik rehberin yerine geçer. Karmaşık listesi olan bir kaynak düzenlenirken alan yerine şu not görünür: "Bu kaynağın 3 rehber adresi var. Rehber ayarları'ndan düzenleyebilirsin."

### 6.8 Ara (`ui/SearchScreen.kt`)
- Yer tutucu: en az bir kaynakta rehber varsa "Kanal, film, dizi veya program ara".
- Tür düğmelerine **Programlar** eklenir (yalnız rehber varsa).
- "Tümü" seçiliyken "Programlar · N" satırı en sondadır; "Programlar" seçiliyken tek satırdır.
- `ProgrammeCard` (260 dp, büyük yazıda 380): kanal logosu 96×54, program başlığı (2 satır), mint meta satırı ("Şimdi · 20:00–21:00", "Bugün 21:00", "Yarın 09:30", "Cuma 21:00", "19 Eki 21:00" — rehber bölgesinde), kanal adı (ve "+2 kanal"), birden çok kaynak varsa kaynak adı. Odakta beyaz halka, ölçek yok. `contentDescription` "<başlık>, <kanal>, <zaman>".
- OK: program şu an yayındaysa `model.open(entry)`. Değilse `ProgrammeDetailDialog` açılır: **Canlı kanalı izle**, **Rehberde göster**, **Kapat**. Geri karta döner.
- `searchFocusedId` `p:<entryId>:<start>` biçimini de taşır; oynatıcıdan dönüşte hedef program satırında aranır.
- Metinler: Programlar seçiliyken sonuç yoksa "Rehberde bu adla yaklaşan program yok."; rehber yoksa "Program araması için kaynaklarına rehber adresi ekle."; eski şemada "Rehber güncelleniyor; program araması birazdan hazır."; aranırken "Kütüphanendeki başlıklar ve rehber kontrol ediliyor."

Akış: menü → Ara → OK (klavye) → "survivor" yaz → Ara tuşu → odak en iyi satıra gider → Aşağı, Programlar satırı → Sağ → OK → ayrıntı → **Rehberde göster** → rehber o kanal ve saatte açılır.

## 7. Program araması: sorgu planı ve bellek
1. Sorgu en az 2 katlanmış karakter olmalı. Kelimeler `TitleIndex.normalize` ile katlanır.
2. Kaynak başına, sırayla ve IO'da: `SELECT id, title, folded FROM titles WHERE folded LIKE ?1 ESCAPE '\' [AND …] LIMIT 400`. Bu tablo ayrı başlıkları tutar (≤ ~100 bin); taranan şey 1 M program değil.
3. Kotlin'de sıralanır (TitleIndex.find kademeleri) ve ilk 100 kimlik alınır. Ardından: `SELECT p.feed, p.channel, p.start, p.stop, p.title_id, p.description, p.category FROM programmes p WHERE p.title_id IN (…) AND p.start < :horizon−minOff AND (p.stop > :now−maxOff OR (p.stop=-1 AND p.start >= :now−maxOff)) ORDER BY p.start LIMIT 300`. `programme_title` dizinini kullanır. Sonra rehber başına kaydırma uygulanıp kesin süzme yapılır.
4. `(feed, channel)` → `matchReport.reverse` üzerinden görünür (gizli kategoriler hariç, `!adult`) katalog kanallarına çevrilir. Yalnız o kanalın kazanan rehberindeki yayınlar alınır. Bir yayın için tek sonuç üretilir; temsilci en küçük Canlı TV numaralı kanaldır, `others` sayısı tutulur.
5. Sıralama: başlık kademesi, sonra şu an yayında olanlar, sonra başlangıç saati, sonra kanal numarası. Toplam en çok 60 sonuç. Geçmiş programlar alınmaz (catch-up yok, EXT02). `searchSourceId` uygulanır.

Bellekte yalnız rapor (kanal başına bir enum ve ters harita) ve küçük sonuç listeleri tutulur.

## 8. Kenar durumlar ve metinler
| Durum | Davranış / metin |
|---|---|
| Aynı adres ikinci kez | "Bu adres zaten ekli." |
| 4 kayıt doluyken ekleme | Düğme devre dışı; doğrudan çağrıda "En fazla dört rehber adresi eklenebilir." |
| Otomatik genişleme 4'ü aşıyor | Fazla adresler satırda "Kullanılmıyor: en çok dört adres"; hata yok |
| Bir adres 404 veriyor, önceki verisi var | Durum `carried`: "Alınamadı: Yayın rehberi adresi bulunamadı. Önceki veriler kullanılıyor." |
| Bütün adresler başarısız | Bugünkü gibi eski rehber kalır, hata durum satırında |
| HTML ya da bozuk XML | "Yayın rehberi okunamadı. XMLTV biçimini kontrol et." (yalnız o adres) |
| Cihaz sınırı aşılıyor | "Bu rehber cihazdaki rehber sınırını aşıyor." (yalnız o adres) |
| Elle seçilen kanal yenilemeden sonra yok | MANUAL_MISSING; "Sorunlu" süzgecinde görünür |
| Bütün adresler kaldırıldı | `configured=false`; bugünkü rehbersiz ekran |
| M3U kanal kimliği değişti (URL tokenı) | Eşleme sahipsiz kalır, otomatik eşlemeye düşülür (favorilerdeki sınırla aynı); otomatik budama yok |
| Yaz saati | Kaydırma sabit bir süredir; `programmeTime` ofset geçişini gösterir. Sağlayıcının yaz saati hatası tek kaydırmayla düzelmez (yalnız belgede yazılır) |
| Kaydırma ±12 sa | Pencere uçlarında birkaç saat boş görünebilir; uydurma yapılmaz |
| Bitişi olmayan program | "Bitiş bilinmiyor"; aramada yalnız başlangıcı şimdiden sonraysa |
| Çakışan programlar | Aramada ayrı ayrı listelenir; `now()` kuralı değişmez |
| Rapor hesaplanırken kaynak kaldırıldı | Sahip denetimi ve iptal; sonuç yazılmaz |

## 9. Testler
**JVM (önce):**
- `GuideAddressTest`: legacy `epgUrl` → `guides`; codec iki alanı birden yazar ve okur; validated (4 kayıt, tek otomatik, http(s), kullanıcı bilgisi yok, tekil); `toString` adresleri gizler.
- `EpgSourceTest` (genişletme): otomatik ve özel adreslerin genişlemesi, 4 sınırı ve `skipped`; sıra değişince binding aynı kalır, adres değişince değişir; tek adresli kaynakta binding eskiyle aynı.
- `GuideTuningCodecTest`: gidiş-dönüş, sınırlar (2.000 eşleme, ±720 dk, 30 dk adımı), bozuk JSON → boş ayar.
- `GuideMatchingTest` (`resolveMatch`): None, Channel, Missing; programı olan rehberin önceliği; MISSING_ID'de ada düşülmemesi; aynı rehberde belirsiz ad alttaki rehbere geçmez; LIVE olmayan kayıt.
- `GuideOffsetTest`: pencere ve program kaydırma (null bitiş dahil); Europe/Berlin DST'de `programmeTime`.
- `ProgrammeSearchTest`: katlama (ı/İ, aksanlar), LIKE kaçırma, kademe sıralaması, varyantların tek sonuçta birleşmesi, ufuk ve 2 karakter sınırı; `guideSearchSeed`.

**Instrumentation:**
- `EpgStorageTest`:
  - Şema v2 ve `user_version`; başlık tekilleştirme ve dizin.
  - v1 dosyası okunur, bayat sayılır, yenileme yerine geçer.
  - Öncelik testi `multipleFeedsDoNotSilentlyMergeConflictingChannelIds`'in yerini alır; öncelik değişince indirme olmadan sonuç çevrilir.
  - Kısmi hata, taşıma ve hepsi başarısız senaryoları.
  - Eşleme ve kaydırma `queryMany`'de uygulanır.
  - Aday araması ve iki kaynakta program araması.
  - URL'lerin veritabanına yazılmadığı testi `origin` ve hata sütunlarını da kapsar.
- `SourceRegistryStorageTest`: şifreli `guides` gidiş-dönüşü; eski v1 blob.
- `GuideFlowTest`:
  - Rehber ayarlarında adres ekleme (MockWebServer'da ikinci rehber), sıra değiştirme, kaydırmanın gösterilen saati değiştirmesi.
  - Basılı OK → eşleme → satırda program; Geri basamakları.
  - fontScale 1.5 ekran görüntüsü; TalkBack etiketleri.
  - `guideOverrideIsEncryptedAndPersistsWithoutReloadingTheCatalog` yeni akışa taşınır.
- `SearchNavigationTest`: Programlar satırı; gelecek program → ayrıntı → Rehberde göster; oynatıcıdan dönüşte odak.

Test sabitleri `example.invalid` ve yerel sunucu kullanır; gerçek adres yok.

## 10. Dosyalar
Liste `files_touched` alanında. Yeni UI `ui/GuideRepair.kt`, yeni saf mantık `data/GuideMatching.kt` ve `data/GuideTuning.kt` dosyalarına gider.

## 11. Aşamalar
1. Çoklu adres ve öncelik + şema v2 + göç + kısmi yayın + Rehber ayarları sayfası (adresler) — 5 g
2. Saat kaydırma (ayar deposu, sorgu anında uygulama, önizleme) — 1,5 g
3. Elle eşleme ve görünür durum (çözümleyici, pencere, basılı OK, hızlı menü, eşleşme sayfası, özet) — 4 g
4. Ara'da program araması — 2,5 g

Her aşamada DECISIONS/EPG.md/SCREENS/STATUS/HANDOFF güncellenir. Toplam ≈ 13 mühendis-günü.

## 12. Sahip kararları
1. Aynı kimlik birden çok rehberde varsa → öneri: kullanıcı sırasında programı olan ilk rehber (EPG.md madde 3'ü değiştirir).
2. EPG düzeltme ücretsiz mi ücretli mi → öneri: ücretsiz inşa et, bayrak koyma (gelir modeli PRD §12'de açık).
3. Program adının dili → öneri: uygulama dili kararı verilene kadar `tr`; sonra uygulama dili → cihaz dili → dil etiketi olmayan → herhangi biri. Şema değişmez, yalnız `XmltvParser(language)` girdisi değişir.
