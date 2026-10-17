# Yedekleme ve geri yükleme + Android TV / Google TV ana ekranı (Watch Next / Play Next, isteğe bağlı uygulama kanalı)

18 Ekim 2026 · TiviMate gap tasarımı (`backup`), salt okunur tasarım ajanı; öneriler kullanıcı kararı değildir.

**Özet:** İki parçalı ve aşamalı bir tasarım. (1) Watch Next: yarım kalan filmler ve sırası kesin olan sıradaki bölümler androidx.tvprovider 1.1.0 ile ana ekrana yazılır. Derin bağlantı, İzlemeye devam et kartının bugün kullandığı TvViewModel.open(entry, record) yoluna gider. Afişler sağlayıcı URL'si yerine uygulama içi, salt okunur bir ContentProvider'dan verilir. Kullanıcı bir girişi ana ekrandan kaldırırsa uygulama onu yeniden eklemez. Ana ekrana yazmak bir ayarla kapatılabilir. (2) Yedekleme: bütün içerik izleyicinin TV'de belirlediği şifreyle şifrelenir (PBKDF2-HMAC-SHA256 ile AES-256-GCM; şifre ağdan hiç geçmez). Dosya TV'nin yerel ağda açtığı, kodla korunan geçici bir sayfadan, QR okutularak telefona iner ya da telefondan TV'ye yüklenir. Cihaz destekliyorsa USB/SAF yolu da sunulur; bulut yok. Geri yükleme hiçbir şeyi silmez, birleştirir; içerik korumasını (yetişkin filtresi, gizli kategoriler) hiçbir durumda gevşetmez. Kataloglar yedeğe girmez, kaynaklardan sırayla yeniden indirilir. Google'ın 19 Mayıs 2026 duyurusuna göre eski Watch Next API'sinin Google TV'deki "Continue watching" desteği 2027'nin ikinci yarısında bitiyor; Engage SDK'ya geçiş, Google kaydı ve hesap kimliği gerektirdiği için sahip kararına bırakıldı. Not: Aktarılan kullanıcı isteği satış stratejisi, fiyat, yabancı dil ve farklı ülkelerden satın alma hakkındaydı. Bu küme o soruları cevaplamıyor; yalnız kesiştiği noktalar (yedeğin ücretli olup olmayacağı, satın alma hakkının yedeğe girmemesi, dil davranışı) §6'da ve sahip kararlarında var.

**Tahmini iş:** 14 gün

**Aşamalar:**
1. 1. Watch Next (≈5,5 g): CONTINUE ve kesin sıralı NEXT girişleri, Ayarlar'da ana ekran anahtarı, nextv://watchnext derin bağlantısı (singleTask + onNewIntent, devam kartıyla aynı open(entry, record) yolu), kullanıcı kaldırma alıcısı, salt okunur afiş sağlayıcısı, JVM ve emülatör testleri
2. 2. Yedek çekirdeği ve yerel ağ/QR aktarımı (≈7 g): şifreli .nextv biçimi, birleştirme kuralları, tek commit'li geri yükleme ve kaynakların sırayla yeniden indirilmesi, Ayarlar'dan ve Kurulum'dan (RECOVERY sıfırlaması sonrası dahil) giriş, telefon sayfası
3. 3. USB/SAF yolu ve fiziksel tur (≈1,5 g): yalnız destekleyen cihazda USB'ye kaydet/USB'den seç; 65U95A ile Google TV ve Android TV emülatörlerinde Watch Next görünürlüğü, PBKDF2 süresi, misafir ağı davranışı
4. 4. (Sahip kararına bağlı) Favoriler uygulama kanalı ve Engage SDK değerlendirmesi; 14 güne dahil değil

**Kullanıcı kararları (varsayılanla):**
- 'Ana ekranda izlemeye devam et' varsayılan olarak açık mı olsun? Başlıklar paylaşılan TV'nin ana ekranında görünür. → varsayılan: Açık. Yetişkin, gizli kategori ve (BKL-028 sonrası) PIN'li içerik hiç yayımlanmaz; ayar tek tuşla kapatılır ve her şeyi siler.
- Android TV ana ekranına uygulama kanalı eklensin mi? → varsayılan: v1'de hayır. Sağlayıcı kataloğunu tanıtan kanal Play/telif algısı ve mahremiyet riski taşıyor. İstenirse sonra yalnız kullanıcının Favoriler kanalı, isteğe bağlı olarak (≈2 g).
- Eski Watch Next desteği Google TV'de 2027'nin ikinci yarısında bitiyor. Engage SDK'ya geçilsin mi? Google kaydı, hesap kimliği ve Google TV hizmetine veri gerektiriyor. → varsayılan: Şimdi kaydolma. Eski Watch Next ile çık; 2027'nin ilk çeyreğinde gelir modeli ve hesap kararıyla birlikte yeniden değerlendir.
- Yedekleme ücretsiz mi, ücretli paketin parçası mı olsun? TiviMate'te Premium. → varsayılan: Ücretsiz. Cihaz değişiminde ve Keystore kaybında kullanıcıyı koruyan bir güven özelliği. Satın alma hakkı yedeğe hiç girmez; Play hesabıyla her ülkede geri gelir.
- Telefonda açılan yedek sayfası hangi dillerde olsun? → varsayılan: v1'de Türkçe, metinler tek bir kopya nesnesinde. İngilizce, PRD §12 dil kararı verilince telefonun Accept-Language başlığına göre eklenir.

**Riskler:**
- Google'ın 19 Mayıs 2026 yazısına göre eski Watch Next API'sinin Google TV'deki 'continue watching 1.0' desteği 2027'nin ikinci yarısında bitiyor. Engage SDK kayıt, hesap kimliği ve yalnız yetişkin hesap verisi istiyor; hesapsız ürünle uyumu belirsiz.
- Google TV birden çok uygulamanın girişlerini birleştirip bazılarını göstermeyebilir. 65U95A'nın Google TV mi Android TV mi olduğu ve girişlerin görünüp görünmediği cihazda doğrulanmadı.
- Yerel ağ aktarımı misafir ağlarında ve istemci yalıtımlı Wi-Fi'de çalışmaz. Telefon tarayıcısı HTTP için 'güvenli değil' uyarısı gösterebilir; USB/SAF birçok TV'de yok.
- 600.000 turluk PBKDF2 düşük güçlü TV SoC'lerde birkaç saniye sürebilir; ölçülmedi. Tur sayısı başlıkta saklandığı için ayarlanabilir.
- Unutulan yedek şifresi kurtarılamaz (bilinçli tasarım). Kullanıcı desteğine 'yedeğim açılmıyor' talepleri gelebilir.
- launchMode=singleTask geri yığını ve çıkış sorusu davranışını etkileyebilir; mevcut enstrümantasyon akışları yeniden koşulmalı.
- Dışa açık afiş sağlayıcısı ve alıcı saldırı yüzeyini genişletir. Riski sınırlayanlar: tahmin edilemeyen jeton, salt okuma, yalnız kendi satırını silme.
- WRITE_EPG_DATA izni Play listesinde görünebilir; cihazda kendiliğinden verildiği doğrulanmalı.
- Geri yükleme katalogları içermez. Süresi dolmuş hesaplar veya ağ yoksa kaynaklar eklenir ama boş açılır; mesajla Kaynaklar'a yönlendirilir.
- Birleştirme silmeleri yaymaz; kullanıcı bunu eşitleme sanabilir. Metinler 'birleştirilir' der.
- İlerleme anahtarları tek yönlü özet olduğu için, farklı kimlikli bir kaynağa birleştirmede geçmişte olmayan Xtream bölümlerinin konumu taşınamaz. Aynı kimlikte (tam geri yükleme) kayıp yok.
- Ana ekranda sağlayıcı başlıkları ve afişleri görünür (paylaşılan ekranda mahremiyet). Sağlayıcı URL'leri ise TvProvider'a yazılmaz.

**Dokunulacak dosyalar:**
- `apps/android-tv/app/build.gradle.kts`: Bağımlılık ekle: androidx.tvprovider:tvprovider:1.1.0, io.nayuki:qrcodegen:1.8.0
- `apps/android-tv/app/src/main/AndroidManifest.xml`: WRITE_EPG_DATA izni; CREATE/OPEN_DOCUMENT için <queries>; MainActivity launchMode=singleTask ve nextv://watchnext VIEW filtresi (BROWSABLE yok); WatchNextDismissReceiver; ArtworkProvider (${applicationId}.watchnext, exported, salt okunur). allowBackup ve data_extraction_rules değişmez
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/DeviceStore.kt`: Yedek anlık görüntüsü (progressEntries, progressKeyFor, çözülmüş ilerleme), tek commit'li applyRestore(plan), ayarların dışa aktarılıp uygulanması; homeWatchNext, watchNext:v1, watchNextNext:v1, watchNextDismissed:v1 anahtarları
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/Models.kt`: SourceConfig.sameConnection() (TvViewModel'deki özel sameConnection buraya taşınır)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/backup/BackupCrypto.kt`: Yeni: Mac üzerinde PBKDF2-HMAC-SHA256, AES-256-GCM zarfı, AAD = başlık, bellek temizliği
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/backup/BackupCodec.kt`: Yeni: zarf ve şema 1 JSON kodlaması, doğrulama ve sınırlar
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/backup/BackupMerge.kt`: Yeni (saf): RestorePlan; kaynak eşleme, kişisel veri birleştirme, koruma gevşemez kuralı
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/backup/LocalTransferServer.kt`: Yeni: ServerSocket ile kodlu, süreli, tek bağlantılı HTTP sunucusu (GET sayfa/dosya, PUT dosya)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/backup/TransferPage.kt`: Yeni: tek dosya Türkçe telefon sayfası (CSP, dış kaynak yok) ve tek yerde toplanmış metin kopyası
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/backup/NetworkAddress.kt`: Yeni: Wi-Fi/Ethernet site-local IPv4 seçimi, VPN hariç
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/home/WatchNextPlanner.kt`: Yeni (saf): istenen girişler ve fark işlemleri
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/home/WatchNextStore.kt`: Yeni: jeton, program kimliği, NEXT ipucu ve kaldırılanlar mağazası (DeviceStore prefs)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/home/WatchNextPublisher.kt`: Yeni: TvProvider ekle/güncelle/sil/sorgula, uzlaştırma, destek tespiti, süreç çapında Mutex
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/home/WatchNextDismissReceiver.kt`: Yeni: WATCH_NEXT_PROGRAM_BROWSABLE_DISABLED alıcısı (goAsync)
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/data/home/ArtworkProvider.kt`: Yeni: filesDir/watchnext/<token>.jpg dosyalarını salt okunur sunan ContentProvider; afiş anlık görüntüsü ArtworkCache.loader ile alınır
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/TvViewModel.kt`: Route.BACKUP, SourceTask.RESTORING_BACKUP, BackupUi durumu ve yedek API'si; openFromHome ve bekleyen derin bağlantı; publishHome tetikleri (publishLibrary, clearHistory, removeSource, categoryVisible, hideAdultCategories, ayar); saveProgress'te NEXT ipucu; resetUnreadableSources'ta watchNext.clear; back() içinde backupBack; cancelImport'un indirme aşamasını kapsaması
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/MainActivity.kt`: onCreate'de intent işleme, onNewIntent, derin bağlantıda splash ve açılış sesinin atlanması
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/BackupScreen.kt`: Yeni: TV-072/073 dışa aktarma ve geri yükleme adımları, SAF başlatıcıları, ON_STOP'ta stopTransfer
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/QrCode.kt`: Yeni: qrcodegen modüllerini Canvas'ta çizen, semantikli QR bileşeni
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PilotApp.kt`: Route.BACKUP dalı; Kurulum'un sol sütununda 'Yedekten geri yükle' (kaynak yokken); SetupEntry internal olur
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/SettingsScreen.kt`: 'Yedekleme' bölümü (Yedek oluştur, Yedeği geri yükle); 'Ana ekranda izlemeye devam et' anahtarı; HISTORY ve ABOUT notlarının güncellenmesi
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt`: onDispose, ON_STOP ve STATE_ENDED'da save() sonrasında model.publishHome()
- `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/LoadingScreen.kt`: RESTORING_BACKUP için başlık ('Yedeğin geri yükleniyor') ve not
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/backup/`: Yeni JVM testleri: BackupCryptoTest, BackupCodecTest, BackupMergeTest, LocalTransferServerTest, ProgressResolutionTest
- `apps/android-tv/app/src/test/java/dev/iptvplayer/tv/data/home/`: Yeni JVM testleri: WatchNextPlannerTest, WatchNextStoreTest
- `apps/android-tv/app/src/androidTest/java/dev/iptvplayer/tv/`: Yeni: BackupStorageTest, BackupFlowTest, SetupRestoreEntryTest, WatchNextIntegrationTest, ArtworkProviderTest; singleTask regresyonu için mevcut akışlar yeniden koşulur
- `docs/decisions/DECISIONS.md, docs/product/PRD.md, docs/design/SCREENS.md, docs/product/TIVIMATE_GAP.md, docs/project/PLAY_STORE_READINESS.md, docs/project/STATUS.md, docs/project/HANDOFF.md, docs/project/BACKLOG.md`: Yeni karar kaydı, EXT05 notu, TV-072/073, gap satırlarının durumu, izin ve dışa açık bileşen notu, durum/devir/backlog

**Yeni bağımlılıklar:** androidx.tvprovider:tvprovider:1.1.0 (kararlı, 7 Mayıs 2025; resmî sürüm sayfasından doğrulandı), io.nayuki:qrcodegen:1.8.0 (MIT, geçişli bağımlılığı yok; Maven Central'da görünüyor, uygulama günü yeniden doğrulanmalı)

---

# Yedekleme/geri yükleme ve Android TV ana ekranı (Watch Next)

18 Ekim 2026 · salt okunur tasarım. İşaretli seçimler **öneridir**, kullanıcı kararı değildir. Bütün kod referansları `apps/android-tv/app/src/main/java/dev/iptvplayer/tv` altındadır.

## 0. Bugünkü durum (kodda doğrulandı)

- **Manifest:** `allowBackup="false"`, `fullBackupContent="false"`. `xml/data_extraction_rules.xml` hem cloud-backup hem device-transfer için bütün alanları (root, file, database, sharedpref, external) dışarıda bırakıyor. `MainActivity` exported; `launchMode` yok, MAIN/LEANBACK_LAUNCHER dışında intent-filter yok. İzinler yalnız INTERNET ve ACCESS_NETWORK_STATE.
- **`data/DeviceStore.kt`** (`device_state` prefs) şu anahtarları tutar:
  - Kaynaklar: `sources_v2` (Keystore anahtarı `iptv_source_v1` ile AES-GCM, AAD `source-registry-v1`, `SourceRegistryCodec`) ve eski `source`.
  - Favoriler: `f:<kaynak>` (yerel kimlik kümesi).
  - Geçmiş: `history:<kaynak>` (`WatchHistory`, en çok 50 kayıt, kimlik + konum).
  - İlerleme: `p2:<uzunluk>:<kaynak>:<sha256(kaynak:kimlik)>`. Anahtar tek yönlü özet olduğu için kimlik anahtardan geri çıkarılamaz.
  - Tercihler: `reduced`, `startupSound`, `guideZoneId`, `sort:MOVIE|SERIES`, `hiddenCategories`, `hideAdultCategories`, `autoNextEpisode`, `audioLanguage`, `subtitleLanguage`, `subtitlesOff`.
- **Anahtar dışa aktarılamaz.** Keystore anahtarı başka cihaza geçmez. Android Auto Backup açılsaydı geri gelen şifreli kayıt okunamaz, uygulama RECOVERY'ye düşerdi. Yedeklemenin bilinçli kapalı olmasının nedeni budur ve kapalı kalır.
- **Koruma kaydı:** `ProtectionStore` ayrı Keystore anahtarı kullanır. `PinCredential.encode()` için kural "never log or export" (PIN doğrulayıcısı yedeğe girmez). PIN, BKL-028 kapsamında henüz arayüze bağlı değil.
- **İlerleme ve geçmiş akışı:**
  - `PlayerScreen` her 5 sn'de (10 × 500 ms), ON_STOP'ta, onDispose'da ve STATE_ENDED'da `model.saveProgress()` çağırır.
  - `TvViewModel.saveProgress()` hem `p2:` kaydını hem geçmiş kaydını günceller. `recordViewed()` geçmişe ekler.
  - `homeLibrary()` devam rafını kurar: en çok 12 kayıt. Devam edilebilir (`PlaybackMark.resumable`) film/bölümler ile son bölümü bitirilmiş (`completed`, %95) diziler girer.
  - Devam kartının OK'i `model.open(entry, record)` çağırır (`ui/HomeScreen.kt`). `loadDetail()` kaydı izler: yarım bölümü oynatır; bitmiş bölümde `EpisodeSequence.next(...).automaticAllowed` ise sonrakini oynatır, değilse ayrıntıyı o sezonda açar.
- **Katalog ve rehber:** Kataloglar cihaz anahtarıyla şifreli ve kaynak yapılandırmasına bağlı (`CatalogStore.binding`). EPG SQLite'tır. İkisi de yeniden indirilebilir. `restore()` önbelleği olmayan kaynağı "bayat" sayar ve `refreshSources` ile sırayla indirir.

## 1. Bölüm A: Yedekleme ve geri yükleme

### 1.1 Aktarım yolları

| Yol | Artı | Eksi | Karar (öneri) |
|---|---|---|---|
| **Yerel ağ sayfası + QR** (TV geçici HTTP sunucusu açar, telefon tarayıcısıyla indirir/yükler) | Her TV'de çalışır, dosya seçici gerekmez, sunucu maliyeti yok. Yedek telefonda kalır; kullanıcı istediği yere (kendi Drive'ı, e-posta) taşır. | Aynı ağ şart; misafir ağı veya istemci yalıtımı bağlantıyı engeller. Tarayıcı "güvenli değil" uyarısı gösterebilir. | **Birincil** |
| **USB / SAF** (`ACTION_CREATE_DOCUMENT` / `ACTION_OPEN_DOCUMENT`) | Ağ gerektirmez. | Birçok TV'de DocumentsUI yok ya da D-pad ile zor; USB desteği değişken. | **İkincil**: yalnız `resolveActivity` sonuç döndürürse görünür |
| Bulut (Drive API, kendi sunucumuz) | Otomatik | OAuth istemcisi, hesap, sunucu ve KVKK yükü; PRD §10 sunucuyu ayrı kapsamlıyor | **Yok** |
| Android Auto Backup / cihaz aktarımı | Kendiliğinden | Keystore anahtarı taşınmaz, kayıt okunamaz | **Kapalı kalır** |

Aynı `.nextv` dosyası iki yolda da kullanılır. Eski TV'den yeni TV'ye geçiş: eski TV → telefon → yeni TV.

### 1.2 Yedeğe girenler

| Veri | Yedekte | Not |
|---|---|---|
| Kaynaklar (ad, tür, adres, kullanıcı, şifre, rehber adresi, **kaynak kimliği**) | Evet | Dosyanın tamamı izleyici şifresiyle şifreli |
| Favoriler `f:` | Evet | |
| Geçmiş `history:` | Evet (anahtar: "İzleme geçmişini ekle", varsayılan Açık) | |
| İlerleme `p2:` | Evet (aynı anahtarla) | Hem çözülebilen kimlikler hem ham anahtarlar (§1.3) |
| Tercihler | Evet | Geri yüklemede "koruma gevşemez" kuralı (§1.6) |
| Katalog, EPG, görsel önbelleği | Hayır | Kaynaktan sırayla yeniden indirilir |
| PIN doğrulayıcısı ve erişim kuralları | Hayır | `PinCredential` "never export"; yeni cihazda PIN yeniden belirlenir |
| Watch Next kayıtları, ana ekran ayarı | Hayır | Cihaza özgü |
| Satın alma hakkı (ileride) | **Asla** | Play Billing hakkı Google hesabıyla her ülkede geri gelir |

### 1.3 Dosya biçimi ve şifreleme (`data/backup/`)

**Zarf.** UTF-8 JSON, en çok 2 MiB, dosya adı `NEXTV-yedek-2026-10-18.nextv`. Kaynak adı ya da sağlayıcı adı dosya adına girmez.

```json
{"format":"nextv-backup","version":1,"createdAt":1760811600000,"app":"0.15.0",
 "kdf":{"alg":"PBKDF2-HMAC-SHA256","iterations":600000,"salt":"<b64 16 B>"},
 "cipher":{"alg":"AES-256-GCM","iv":"<b64 12 B>"},
 "payload":"<b64 şifreli metin + 16 B etiket>"}
```

**Şifreleme (`BackupCrypto`):**
- Anahtar türetme: PBKDF2-HMAC-SHA256, 600.000 tur, 256 bit.
  - Şifre önce NFC'ye normalize edilir (`java.text.Normalizer`), sonra UTF-8 baytlara çevrilir.
  - PBKDF2, `javax.crypto.Mac("HmacSHA256")` üzerinde elle yazılır (yaklaşık 20 satır). Sağlayıcıların `PBEKeySpec` karakter kodlaması farkı böylece ortadan kalkar; RFC 7914 §11 vektörleriyle test edilir.
- Tur sayısı başlıkta saklanır. Okurken 100.000–5.000.000 aralığı dışı reddedilir (DoS koruması).
- Şifreleme: AES-256-GCM, rastgele 12 B IV. AAD, `payload` dışındaki alanların kanonik baytlarıdır; başlık değiştirilirse çözme başarısız olur. Keystore değil `SecretKeySpec` kullanılır, çünkü anahtarın taşınabilir olması gerekir.
- Bellek temizliği: şifre `CharArray` olarak taşınır. Düz metin ve anahtar baytları iş bitince `fill(0)` ile silinir (`PinCredential` deseni).
- **Şifre ağdan hiç geçmez.** Hem oluşturmada hem açmada TV'de yazılır. Yerel sayfa yalnız zaten şifreli dosyayı taşır.

**İçerik (`BackupCodec`, şema 1):**

```json
{"schema":1,
 "sources":[{"id":"…","name":"Evim","type":"M3U","address":"…","username":"","password":"","epgUrl":""}],
 "personal":{"<kaynakId>":{"favorites":["yerelId"],"history":[WatchHistory kaydı],
   "progress":[{"id":"yerelId","position":0,"duration":0,"time":0}],
   "progressRaw":{"<sha256>":"<p2 değeri>"}}},
 "settings":{"reduced":false,"startupSound":true,"guideZoneId":"","sort":{"MOVIE":"DEFAULT","SERIES":"DEFAULT"},
   "hiddenCategories":[],"hideAdultCategories":true,"autoNextEpisode":true,"audioLanguage":null,
   "subtitleLanguage":null,"subtitlesOff":true}}
```

- **`progress`**, `p2:` anahtarlarının çözülebildiği kayıtlardır. Adaylar: kaynağın katalog anlık görüntüsündeki yerel kimlikler, `m3uEpisodes` kimlikleri ve geçmişteki kimlik/ebeveynler. Her aday için `stableId("$kaynak:$id")` hesaplanıp anahtarla eşleştirilir. 63 bin kayıtta `Dispatchers.Default` üzerinde sorun beklenmez; ölçülmeli.
- **`progressRaw`**, ham özet → değer eşlemesidir. Yalnız hedef kaynak kimliği yedektekiyle aynıysa yazılır; geçmişte olmayan Xtream bölümleri de böylece korunur. Kaynak başına en yeni 5.000 kayıtla sınırlıdır.
- **Okuma kuralları:**
  - Bilinmeyen alanlar yok sayılır.
  - `schema > 1` reddedilir.
  - Her kaynak `SourceConfig.validated()` ile denetlenir; geçersiz olanlar atlanır ve sayılır.
  - Bilinmeyen sıralama değeri `LibrarySort.DEFAULT` olur; geçersiz saat dilimi düşürülür.
  - `toString()` "REDACTED" döndürür.

### 1.4 Yerel ağ sunucusu (`data/backup/LocalTransferServer.kt`)

Elle yazılmış küçük bir `java.net.ServerSocket` sunucusudur; NanoHTTPD veya Ktor eklenmez.

- **Bağlanma adresi:** `ConnectivityManager` ağları arasında TRANSPORT_WIFI ya da TRANSPORT_ETHERNET olanın site-local IPv4 adresi. VPN açıksa VPN ağı seçilmez. Port geçicidir (0). Bulunamazsa hata gösterilir (§1.9).
- **Kod:** Crockford base32 ile 8 karakter (40 bit, SecureRandom), `K7QP-M2XD` biçiminde gösterilir. Yolda büyük/küçük harf ve tire önemsizdir; karşılaştırma sabit sürelidir.
- **Uçlar:**
  - `GET /<kod>`: tek dosya HTML sayfası.
  - `GET /<kod>/file` (dışa aktarma kipi): `application/octet-stream`, `Content-Disposition: attachment`.
  - `PUT /<kod>/file` (içe aktarma kipi): ham gövde, `Content-Length` zorunlu ve en çok 2 MiB → 201 / 411 / 413 / 400. Multipart yoktur; sayfa `fetch(PUT)` kullanır.
- **Sınırlar:**
  - Bağlantılar sırayla, birer birer işlenir. Soket zaman aşımı 10 sn, istek satırı + başlıklar en çok 8 KB.
  - Hatalı kod ya da bilinmeyen yol 5 kez olursa sunucu kapanır.
  - Oturum en çok 10 dakika açık kalır. Ekrandan çıkınca ya da Activity ON_STOP olunca kapanır. İçe aktarmada ilk başarılı yüklemeden sonra kapanır.
- **Başlıklar:** `Cache-Control: no-store`, `Referrer-Policy: no-referrer`, `X-Content-Type-Options: nosniff`, `Connection: close` ve CSP `default-src 'none'; style-src 'unsafe-inline'; script-src 'unsafe-inline'; connect-src 'self'`. Dış kaynak, analitik ya da CDN yoktur.
- **Sayfa metni (`TransferPage.kt`, `<html lang="tr">`):**
  - Dışa aktarma: başlık "NEXTV yedeği", gövde "Bu dosya şifrelidir. Açmak için TV'de belirlediğin şifre gerekir.", düğme "Yedeği indir".
  - İçe aktarma: başlık "NEXTV yedeğini TV'ye gönder", alan "Yedek dosyasını seç (.nextv)", düğme "TV'ye gönder".
  - Durumlar: "Gönderiliyor…", "Gönderildi. Şifreyi TV'de gir.", "Bu dosya bir NEXTV yedeği değil ya da çok büyük.", "Bağlantı kapandı. TV'de yeni bağlantı oluştur."
  - Sayfa hiçbir adres veya hesap bilgisi göstermez.
- **QR:** `io.nayuki:qrcodegen:1.8.0` (MIT, bağımlılığı yok), ECC M. `ui/QrCode.kt`, beyaz kutuda 4 modül sessiz alanla Canvas'ta çizer, yaklaşık 200 dp. Aynı bileşen ileride EXT01'de de kullanılabilir.

### 1.5 Akışlar (kumanda adım adım)

**Yedek oluşturma** (Ayarlar → Yedekleme → "Yedek oluştur"):
1. OK → `Route.BACKUP`, adım `EXPORT_FORM`. İlk odak "Yedek şifresi" alanında; klavye OK ile açılır (D058). Klavyedeki İleri "Şifreyi tekrar gir" alanına geçer.
2. Aşağı: "İzleme geçmişini ekle" (Açık), "Şifreyi göster" anahtarı (D065'teki "Bilgileri göster" deseni).
3. Aşağı: "Yedeği oluştur" (birincil, mint dolgu) ve "Vazgeç". OK → "Yedek şifreleniyor…" (Hareketi azalt'ta sabit yay). Bu sırada Geri işlemi bekler; şifreleme birkaç saniye sürer.
4. Adım `EXPORT_READY`: solda adımlar ve adres, sağda QR. Odak "Bitti" düğmesinde; yanında, varsa "USB'ye kaydet". Telefon indirme yaptıkça "Yedek telefona indirildi." notu görünür. Oturum kapanınca "Yeni bağlantı oluştur" belirir ve odağı alır.
5. Geri ya da "Bitti" → sunucu kapanır, şifreli bayt dizisi bellekten silinir. Ayarlar açılır, odak "Yedek oluştur" satırında olur.

**Geri yükleme** (Ayarlar → "Yedeği geri yükle", ya da Kurulum'da sol sütundaki "Yedekten geri yükle"; ikincisi `s.sources.isEmpty()` iken görünür ve RECOVERY sıfırlamasından sonra da ulaşılabilir):
1. OK → adım `IMPORT_WAIT`: QR ve adres. Odak "Vazgeç" düğmesinde; varsa "USB'den seç" de durur.
2. Telefon dosyayı yükleyince sunucu kapanır. Ekran kendiliğinden `IMPORT_UNLOCK` adımına geçer, odak "Yedek şifresi" alanındadır. OK ile klavye açılır, "Yedeği aç" ile "Yedek açılıyor…" başlar.
3. Adım `IMPORT_PREVIEW` şunları gösterir:
   - Oluşturulma tarihi ve sürüm
   - Kaynak listesi (ad ve tür etiketi; tür etiketi `SourcesScreen.label()` ile aynı, adres gösterilmez)
   - "42 favori · 18 izleme kaydı"
   - "Bu cihazdaki kaynaklar korunur; favoriler ve izleme konumları birleştirilir."
   - "Ayarları da al" anahtarı: cihazda kaynak yoksa Açık, varsa Kapalı.
   - Odak "Geri yükle" (birincil) düğmesinde; yanında "Vazgeç".
4. OK → `SourceTask.RESTORING_BACKUP` başlar. Önce tek commit atılır (§1.6; iptal edilemez). Ardından kaynaklar **sırayla** indirilir: "Kütüphane indiriliyor: Evim (1/3)". Bu aşamada Geri yalnız indirmeyi durdurur. Kalan kaynaklar sonraki açılışta `restore()` içindeki bayat kaynak kuralıyla indirilir.
5. Bitince Ana Sayfa açılır. Bildirim "Yedek geri yüklendi." ya da "Yedek geri yüklendi. 1 kaynak açılamadı; Kaynaklar'dan kontrol et." olur.

**Geri her zaman bir basamak çıkar:** PREVIEW → UNLOCK → WAIT → çağıran ekran (Ayarlar ya da Kurulum). Açık klavye önce kapanır. Geri `TvViewModel.back()` içinde `Route.BACKUP -> backupBack()` olarak ele alınır; ayrı bir BackHandler gerekmez.

### 1.6 Birleştirme kuralları (`data/backup/BackupMerge.kt`, saf)

**Kaynak eşleştirme.** Mevcut özel `sameConnection` (tür + adres + kullanıcı) `SourceConfig.sameConnection()` olarak taşınır ve iki yerde kullanılır.
- Eşleşen kaynakta cihazdaki kayıt (ad, şifre, kimlik) aynen kalır; yedek kimliği cihaz kimliğine eşlenir.
- Eşleşmeyen kaynak yedekteki kimliğiyle sona eklenir. Kimlik başka bir bağlantıda kullanılıyorsa yeni bir UUID verilir ve `progressRaw` düşer.
- `filterId` `null` olur (D060).

**Kişisel veri:**
- Favoriler: birleşim.
- Geçmiş: kimliğe göre birleşir, daha yeni `time` kazanır, en çok 50 kayıt (`WatchHistory.LIMIT`).
- İlerleme: kimlik başına daha yeni `time` kazanır. Hedef kimlik yedektekiyle aynıysa `progressRaw` de yazılır; değilse yalnız `progress` listesi `progressKey(cihazKimliği, yerelId)` ile yeniden anahtarlanır.

**Silme yayılmaz.** Bu bir eşitleme değildir (PRD §4: yedek taşıma senkronizasyon değildir). Metinler "birleştirilir" der, "eşitlenir" demez.

**Tercihler** yalnız "Ayarları da al" açıksa uygulanır. Uygulanırken koruma gevşemez:
- `hideAdultCategories = cihaz || yedek`
- `hiddenCategories = cihaz ∪ yedek`

BKL-028 bağlandıktan sonra cihazda PIN kuruluysa geri yükleme başlamadan PIN istenir.

**Atomiklik.**
- `DeviceStore.applyRestore(plan)` tek bir `SharedPreferences.Editor` ile tek `commit()` yapar: mühürlenmiş `sources_v2` + `f:` + `history:` + `p2:` + tercihler.
- Çağrı `SourceTransactions.shared.write {}` içinde, `artwork.synchronizeSources(...)` ile birlikte yapılır (`connect()` deseni).
- Commit başarısızsa cihazda hiçbir şey değişmemiş olur.
- Not: `connect()`'teki "önce anlık görüntü, sonra hesap" kuralı burada bilerek esnetilir. `restore()` ve `SourceLibrary.combine()` önbelleği olmayan kaynağı zaten tolere ediyor (`catalogs[source.id] ?: return@forEach`).

### 1.7 ViewModel API (`TvViewModel.kt`)

```kotlin
enum class Route { …, BACKUP }
enum class SourceTask { …, RESTORING_BACKUP }
enum class BackupStep { EXPORT_FORM, EXPORT_READY, IMPORT_WAIT, IMPORT_UNLOCK, IMPORT_PREVIEW }
data class TransferInfo(val url: String, val code: String, val expiresAt: Long, val downloads: Int = 0)
data class BackupSummary(val createdAt: Long, val appVersion: String, val sources: List<Pair<String, SourceType>>,
    val matched: Int, val added: Int, val skipped: Int, val favorites: Int, val history: Int)
data class BackupUi(val step: BackupStep, val returnTo: Route, val busy: String? = null, val error: String? = null,
    val transfer: TransferInfo? = null, val transferClosed: String? = null, val usbAvailable: Boolean = false,
    val summary: BackupSummary? = null)
// TvState += val backup: BackupUi? = null

fun openBackup(export: Boolean)                 // içe aktarmada sunucuyu da başlatır
fun createBackup(password: CharArray, repeat: CharArray, includeHistory: Boolean)
fun newTransfer()                               // "Yeni bağlantı oluştur"
fun writeBackup(uri: android.net.Uri)           // SAF ile kaydet
fun readBackup(uri: android.net.Uri)            // SAF ile aç
fun unlockBackup(password: CharArray)
fun applyBackup(applySettings: Boolean)
fun stopTransfer()                              // ekran ON_STOP / ayrılma
private fun backupBack()                        // back() içinden
```

- Şifreli dosya (`ByteArray`), çözülmüş `RestorePlan` ve sunucu nesnesi `TvState` içine girmez; özel alanlarda tutulur ve `onCleared()` içinde temizlenir.
- Metin alanı değerleri `BackupScreen` içinde yalnız `remember` ile tutulur, `rememberSaveable` kullanılmaz (Bundle'a şifre yazılmasın).
- `cancelImport()`, RESTORING_BACKUP'ın indirme aşamasını da durdurur.
- Kaynak işlemi sürerken (`sourceTask != IDLE`) `openBackup` hiçbir şey yapmaz.

### 1.8 Ekran (`ui/BackupScreen.kt`, yeni; TV-072 dışa aktarma / TV-073 geri yükleme)

**Yerleşim (960×540, normal yazı):**
- Kurulum ekranıyla aynı düzen: `TvLayout.Gutter` ve dikey 28 dp kenar.
- Sol sütunda başlık (28 sp), adım metinleri (`Note`), adres ve kod. Adres 20 sp, kod ayrıca "Kod: K7QP-M2XD" olarak yazılır.
- Sağda `Ink.Container` panel içinde 200 dp QR ya da form.
- Alt satırda eylem düğmeleri.

**Bileşenler:** Form alanları `EntryField` (`ui/Theme.kt`) kullanır. Kurulumdaki `SetupEntry` `internal` yapılıp yeniden kullanılır. Düğmeler `Action`, anahtarlar Ayarlar'daki `SettingRow` deseni.

**Odak dili (D064):** Düğmelerde mint dolgu, alanlarda ve satırlarda beyaz 2 dp halka, büyüme yok. QR odak almaz.

**Yazı boyu 1.5:** Sol sütun `verticalScroll` olur; odaklanan öğe `BringIntoViewRequester` ile görünür kalır. QR 200 dp sabit kalır, düğmeler alta sarar.

**TalkBack:**
- QR kutusunun açıklaması: "Telefonla açılacak bağlantının QR kodu".
- Adres satırı ayrı okunur: "Adres 192.168.1.23, port 41837, kod K 7 Q P, M 2 X D".
- Durum notları `liveRegion = Polite`.
- Kalan süre dakikada bir güncellenir ve canlı bölgeye yazılmaz.

**Hareketi azalt:** Animasyon yok; gösterge `LoadingArc` sabit kalır (U22).

### 1.9 Hatalar ve Türkçe metinler

| Durum | Metin |
|---|---|
| Şifre kısa | "Şifre en az 8 karakter olmalı." |
| Şifreler farklı | "Şifreler aynı değil." |
| Kaynak yok | "Yedeklenecek kaynak yok." (satır açıklaması, işlem yapmaz) |
| Ağ yok / uygun adres yok | "TV bir Wi-Fi ya da kablolu ağa bağlı değil. Bağlantıyı kontrol edip tekrar dene." |
| Ekrandaki yardım | "Telefonun TV ile aynı Wi-Fi ağında olmalı. Misafir ağlarında bağlantı engellenebilir. Tarayıcı ‘güvenli değil’ uyarısı gösterebilir; dosya zaten şifrelidir." |
| Süre doldu | "Bağlantının süresi doldu." + "Yeni bağlantı oluştur" |
| 5 hatalı deneme | "Çok fazla hatalı deneme yapıldı. Güvenliğin için bağlantı kapatıldı." |
| Dosya biçimi yanlış / büyük | "Bu dosya bir NEXTV yedeği değil." |
| Yanlış şifre ya da bozuk dosya | "Şifre yanlış ya da dosya bozuk. Tekrar dene." |
| Yeni şema | "Bu yedek NEXTV'nin daha yeni bir sürümüyle oluşturulmuş. Uygulamayı güncelleyip tekrar dene." |
| Geçersiz kaynak atlandı | "1 kaynak geçersiz olduğu için alınmadı." (önizlemede) |
| Commit başarısız | "Yedek cihaza yazılamadı. Boş alanı kontrol edip tekrar dene." |
| SAF yok ya da hata | "Bu TV dosya seçmeyi desteklemiyor. Telefonla aktarımı kullan." |
| Kısmi indirme | "Yedek geri yüklendi. 1 kaynak açılamadı; Kaynaklar'dan kontrol et." |
| İndirme durduruldu | "İndirme durduruldu. Kalan kaynaklar uygulama açılınca yenilenir." |

Ayarlar'daki yeni satırlar ("Yedekleme" bölümü, "Kaynaklar"ın altında):
- "Yedek oluştur": "Kaynakları, favorileri, izleme konumlarını ve ayarları şifreli bir dosyaya kaydeder."
- "Yedeği geri yükle": "Telefondaki veya USB'deki bir NEXTV yedeğini bu cihaza alır."

Uygulama hakkında'ya eklenecek not: "Yedekler senin belirlediğin şifreyle şifrelenir; şifre unutulursa açılamaz."

## 2. Bölüm B: Android TV / Google TV ana ekranı

### 2.1 Davranış kuralları (Google'ın Watch Next yönergeleri + uygulama kuralları)

**Neler yayımlanır:**
- Yalnız film (`TYPE_MOVIE`) ve dizi bölümü (`TYPE_TV_EPISODE`). Canlı kanal yayımlanmaz; Google yalnız "traditional movies and TV shows" diyor.
- **CONTINUE:** `PlaybackMark.resumable` ve `!completed` olan film ya da bölüm. Eşik D064'teki %2 / en çok 2 dk, bitme eşiği %95. Google'ın "%3 ya da 2 dk" kuralına yakın; uygulama içi devam rafıyla aynı kalsın diye uygulamanınkiler korunur.
- **NEXT:** Son bölümü bitmiş dizide sıradaki bölüm biliniyorsa ve `EpisodeSequence.next(...).automaticAllowed == true` ise yayımlanır. Sıra kesin değilse giriş kaldırılır; Google, izlenmemiş bölüm yoksa kaldırmayı istiyor.
- Dizi başına tek giriş; anahtar dizinin ContentRef'idir.
- En çok 10 giriş, en yeni etkileşime göre (Google TV listeleri birleştirir).

**Neler asla yayımlanmaz:**
- Sağlayıcının yetişkin işaretli içeriği.
- Gizli kategoriler (D068); `state.allCatalog` zaten süzülmüş katalogdur.
- BKL-028 bağlandıktan sonra `AccessPolicy.decide(...) != ALLOW` olan her şey. Başlık bile ana ekranda görünmemeli.

**Mevsim/bölüm numarası** yalnız `episodeOrderKnown` ise yazılır; başlıktan tahmin kesin sunulmaz (D057, PRD VOD kuralları).

### 2.2 Program alanları (`WatchNextProgram.Builder`)

| Alan | Değer |
|---|---|
| type / watchNextType | MOVIE·EPISODE / CONTINUE·NEXT |
| title | `displayTitle(entry.title).name` (D065). Bölümde dizinin adı; `episodeTitle` bölüm adı |
| season/episodeNumber | Sayısal dize, yalnız sıra kesinse |
| description | Sağlayıcı açıklaması varsa en çok 200 karakter; yoksa boş (yer tutucu yok) |
| lastPlaybackPositionMillis / durationMillis | `WatchRecord` konum/süre; Int'e sığacak şekilde sınırlanır |
| lastEngagementTimeUtcMillis | `WatchRecord.updatedAt` |
| posterArtUri + aspect | `content://<applicationId>.watchnext/poster/<token>.jpg`. Bölümde 16:9 kare (`ASPECT_RATIO_16_9`), filmde backdrop varsa 16:9, yoksa afiş (`ASPECT_RATIO_MOVIE_POSTER`) |
| intentUri | `Intent(ACTION_VIEW, "nextv://watchnext/<token>").setClass(MainActivity).toUri(URI_INTENT_SCHEME)` |
| internalProviderId | `<token>`: 16 karakter Crockford base32, rastgele; içerik kimliği değildir |

### 2.3 Yayınlama ve fark alma

**Tetikler** (1 sn birleştirme, `Dispatchers.IO`, süreç çapında `Mutex`; `WatchNextPublisher.lock`):
- Oynatıcıdan çıkış: `PlayerScreen` onDispose ve ON_STOP içinde `save()` sonrasında `model.publishHome()`.
- STATE_ENDED.
- `publishLibrary()` sonu (açılış, yenileme, kaynak ekleme/kaldırma, kategori görünürlüğü).
- `clearHistory`, ayar değişikliği, geri yükleme.

Her 5 sn'lik `saveProgress`'te sağlayıcıya yazılmaz. Google'ın "çıkışta ya da 5 dk duraklamada, en çok 5 sn gecikme" kuralı karşılanır.

**Planlayıcı** (`data/home/WatchNextPlanner.kt`, saf): girdi `state.allCatalog`, `state.history`, NEXT ipuçları ve izin fonksiyonu; çıktı istenen `WatchNextItem` listesi. `diff(wanted, published, dismissed)` ekleme/güncelleme/silme işlemlerini üretir. Yalnız değişen girişe dokunulur (Google: "Only update the item the user has interacted with").

**NEXT ipucu.** `saveProgress` bölüm %95'i geçtiğinde `nextEpisodeChoice()` sonucunu (`state.episodes` o anda yüklü) kaydeder: sonraki bölümün yerel kimliği, adı, sezon/bölüm ve karesi. Böylece açılıştaki uzlaştırma bölümleri yeniden indirmeden NEXT girişini kurabilir.

**Uzlaştırma.**
- `contentResolver.query(WatchNextPrograms.CONTENT_URI, WatchNextProgram.PROJECTION, …)` ile kendi programları okunur (`WatchNextProgram.fromCursor`).
- `isBrowsable == false` olanlar kullanıcı tarafından kaldırılmıştır: silinir ve `dismissed`'e eklenir.
- Mağazada olup sağlayıcıda olmayanlar mağazadan düşer; hâlâ isteniyorsa yeniden eklenir.

**Sağlayıcı yoksa.** `packageManager.resolveContentProvider(TvContractCompat.AUTHORITY, 0) == null` ya da SecurityException / IllegalArgumentException gelirse özellik o oturumda kapanır ve ayar satırı gizlenir. Çökme yoktur.

### 2.4 Derin bağlantı (kumanda akışı)

1. Ana ekranda karta OK basılır. Başlatıcı `nextv://watchnext/<token>` ile MainActivity'yi açar.
   - **Soğuk açılış:** `onCreate` intent'i okur ve `model.openFromHome(uri)` çağırır. Splash ve açılış sesi atlanır (öneri): `opening = savedInstanceState == null && !watchNext && claimOpening()`. Kaynaklar açılırken D069 kapağı görünür.
   - **Sıcak açılış:** `android:launchMode="singleTask"` ve `onNewIntent()`. Bu olmadan NEW_TASK mevcut görevi yalnız öne getirir ve intent kaybolur.
2. `openFromHome` jetonu `[0-9A-HJKMNP-TV-Z]{16}` ile doğrular, `WatchNextStore` içinde arar ve bekleyen iş olarak saklar.
   - Bekleyen iş, `restore()` başarı yolunda (`publishLibrary` sonrası) ya da `sourceTask == IDLE` iken hemen işlenir.
   - SETUP veya RECOVERY'de yok sayılır.
   - BACKUP akışı açıksa sunucu kapatılır ve plan silinir. Geri yükleme commit/indirme sürüyorsa iş IDLE'a kadar bekler.
3. İşleme:
   - `home()` çağrılır, ardından `open(entry, record)`. `entry` film ya da dizidir (`state.allCatalog`, ContentRef ile); `record`, `state.history` içinden ilgili kayıttır.
   - Sonuç devam kartıyla birebir aynıdır: film kaldığı yerden; yarım bölüm o bölümden; NEXT'te sıra kesinse sonraki bölüm oynar, değilse ayrıntı sonraki bölümün sezonunda açılır.
   - Önceki oynatma `PlayerScreen` onDispose'da kaydedilir ve bırakılır; yeni oturum `LaunchedEffect(media)` eskisini durdurduktan sonra başlar. Tek oturum kuralı korunur.
4. Geri: Oynatıcı → Ayrıntı → Ana Sayfa → (ana sayfa menüsünden) çıkış sorusu. Her basış bir basamak.
5. Hatalar:
   - Bilinmeyen jeton (ya da başka bir uygulamanın uydurduğu intent): Ana Sayfa açılır, "Bu kısayol artık geçerli değil." yazar; aynı jetonlu program varsa silinir.
   - İçerik katalogda yok: "Bu içerik artık kaynağında bulunamıyor." yazar, program silinir.
   - PIN gerekiyorsa (BKL-028 sonrası) önce PIN istenir. Normalde böyle bir içerik zaten yayımlanmaz.

### 2.5 Kaldırma, kapatma, temizleme

- **Kullanıcı ana ekrandan kaldırırsa:** `WatchNextDismissReceiver` (exported; `android.media.tv.action.WATCH_NEXT_PROGRAM_BROWSABLE_DISABLED`, `TvContractCompat.EXTRA_WATCH_NEXT_PROGRAM_ID`) `goAsync` ile çalışır. Satırı siler ve `dismissed[anahtar] = engagedAt` yazar. Aynı başlık, ancak daha yeni bir izleme etkileşimi olursa yeniden eklenir; Google "should NOT reinsert it" diyor. Mağazada olmayan program kimliği yok sayılır.
- **"Ana ekranda izlemeye devam et" kapatılırsa:** bütün programlar, afiş dosyaları ve mağaza silinir; `dismissed` korunur.
- **Geçmişi temizle, kaynak kaldır, kategori gizle:** uzlaştırma ilgili girişleri siler. HISTORY sayfası notuna eklenir: "Ana ekrandaki ‘İzlemeye devam et’ önerileri de kaldırılacak."
- **`resetUnreadableSources()`:** işlem içinde, prefs silinmeden önce `watchNext.clear()` çağrılır, çünkü program kimlikleri prefs'tedir.
- **Kaldırma (uninstall):** TvProvider paket satırlarını kendisi temizler (beklenen; cihazda doğrulanmalı).

### 2.6 Afiş vekili (`data/home/ArtworkProvider.kt`)

Sağlayıcı görsel URL'leri sağlayıcı alan adını, hatta imzalı jetonları içerebilir. Bunları sistemin TvProvider veritabanına yazmak, şifreli tutulan katalogdaki bilgiyi başlatıcıya açar. Bu yüzden:

- **Yayımlarken:** afiş mevcut Coil yükleyicisiyle (`ArtworkCache.loader`, `SourceArtwork(sourceId, url)`) en çok 400 px yüksekliğe ölçeklenerek alınır ve JPEG 85 olarak `filesDir/watchnext/<token>.jpg` dosyasına `AtomicFile` ile yazılır. Bu dosya `data_extraction_rules` gereği yedeğe gitmez.
- **`ArtworkProvider`:** `exported=true`, okuma izni yok (başlatıcının okuması gerekir). Yalnız `openFile(uri,"r")` desteklenir. Jeton biçimi ve mağaza üyeliği denetlenir. Yazma kipi, query, insert, update ve delete `UnsupportedOperationException` atar; `getType` `image/jpeg` döner. Süreç soğukken de çalışır, ağa çıkmaz.
- **Görsel yoksa** poster alanı boş bırakılır.

### 2.7 Gizlilik ayarı

Ayarlar → "İzleme ve uygulama" bölümüne, "İzleme geçmişini temizle"nin üstüne bir satır eklenir:
- Ad: "Ana ekranda izlemeye devam et"
- Açıklama: "Yarım kalan filmler ve sıradaki bölümler TV'nin ana ekranındaki devam satırında görünür. Kapatınca oradan kaldırılır."
- Semantik `Role.Switch`, durum "Açık/Kapalı".
- Anahtar `homeWatchNext`; varsayılan sahip kararı (öneri: Açık).
- Ayar, TvProvider olmayan cihazda gizlenir.

### 2.8 Google TV / Android TV farkları (doğrulanmalı)

- **Android TV ana ekranı (eski başlatıcı):** "Play Next" satırı. Kullanıcı kaldırabilir; kaldırınca BROWSABLE_DISABLED yayınlanır.
- **Google TV:** Watch Next, For You'daki "Continue watching" satırında listeleri birleştirir ve hepsini göstermeyebilir. Google'ın 19 Mayıs 2026 yazısı: eski Watch Next API'sinin "continue watching 1.0" desteği **2027'nin ikinci yarısında bitiyor**. Halefi Engage SDK (`com.google.android.engage:engage-tv:1.1.0`) şunları gerektiriyor:
  - Google'a ilgi bildirimi ve kayıt
  - Hesap kimliği içeren `AccountProfile`
  - Yalnız yetişkin hesaplar için veri
  - En çok 10 varlık ve her seferinde bütün listeyi değiştiren yayın
  - En az 200 px yüksekliğinde 16:9 görseller
  - Varsayılan kapalı cihazlar arası eşitleme ve bunun için zorunlu bir ayar

  Verinin Google TV hizmetine gitmesi, hesapsız ve analitiksiz ürün ilkesiyle çelişebilir. **Sahip kararı.**
- **65U95A:** Bu TV'nin Google TV mi Android TV mi olduğu ve girişlerin gerçekten görünüp görünmediği cihazda denenmeli. Ayrıca Android TV ve Google TV emülatör görüntülerinde denenmeli.
- **Operatör başlatıcıları:** Watch Next'i yok sayabilir. Özellik sessizce etkisiz kalır.

### 2.9 Uygulama kanalı (isteğe bağlı, öneri: v1'de yok)

Yeni eklenen filmlerden oluşan bir kanal, sağlayıcı kataloğunu ana ekranda uygulama tanıtıyormuş gibi gösterir. Bu, Play raporu §3.2'deki "içeriği benimseme, teşvik" riskidir; ayrıca paylaşılan ekranda mahremiyet sorunu çıkar. Google TV'nin uygulama kanallarını gösterip göstermediği de doğrulanamadı.

Sahip isterse yalnız kullanıcının seçtiği **Favoriler** kanalı yapılır: varsayılan kanal, isteğe bağlı ayar, `INITIALIZE_PROGRAMS` alıcısı, `PreviewChannelHelper`. Ek iş yaklaşık 2 gün.

### 2.10 Sınıflar ve VM API

```kotlin
// data/home/WatchNextStore.kt — DeviceStore anahtarları:
//   homeWatchNext: Boolean
//   watchNext:v1          JSON [{token, programId, key, type, wnType, engagedAt, position, title,
//                                episodeTitle, season, episode, poster:Boolean}]
//   watchNextNext:v1      { seriesKey → {episodeId, title, season, episode, completedId, at} }
//   watchNextDismissed:v1 { key → engagedAt } (en çok 200)
data class WatchNextItem(val key: String, val sourceId: String, val localId: String, val parentLocalId: String?,
    val type: Int, val watchNextType: Int, val title: String, val episodeTitle: String?, val season: Int?, val episode: Int?,
    val positionMs: Long, val durationMs: Long, val engagedAt: Long, val artwork: String?, val wide: Boolean, val description: String)
object WatchNextPlanner {
    fun items(catalog: Catalog, history: List<WatchRecord>, next: Map<String, NextHint>,
        allow: (MediaEntry) -> Boolean, limit: Int = 10): List<WatchNextItem>
    fun diff(wanted: List<WatchNextItem>, published: List<Published>, dismissed: Map<String, Long>): List<WatchNextOp>
}
class WatchNextPublisher(context: Context, store: DeviceStore, artwork: ArtworkCache) {
    val supported: Boolean
    suspend fun sync(items: List<WatchNextItem>, enabled: Boolean)
    suspend fun clear()
    suspend fun dismissed(programId: Long)
    companion object { val lock = Mutex() }
}
// TvViewModel
//   TvState += homeWatchNext: Boolean, homeWatchNextSupported: Boolean
fun homeWatchNext()                  // ayarı değiştirir
fun publishHome()                    // birleştirilmiş tetik
fun openFromHome(uri: android.net.Uri?)
```

## 3. Bağımlılık, izin ve manifest

- `app/build.gradle.kts`:
  - `implementation("androidx.tvprovider:tvprovider:1.1.0")`: kararlı, 7 Mayıs 2025; bugün resmî sürüm sayfasından doğrulandı.
  - `implementation("io.nayuki:qrcodegen:1.8.0")`: MIT; Maven Central'da 1.8.0 görünüyor. Uygulama günü yeniden doğrulanmalı.
  - ProGuard için ek kural beklenmez; release'te denenmeli.
- Manifest:

```xml
<uses-permission android:name="com.android.providers.tv.permission.WRITE_EPG_DATA" />
<queries>
  <intent><action android:name="android.intent.action.CREATE_DOCUMENT" /><data android:mimeType="*/*" /></intent>
  <intent><action android:name="android.intent.action.OPEN_DOCUMENT" /><data android:mimeType="*/*" /></intent>
</queries>
<activity android:name=".MainActivity" android:exported="true" android:launchMode="singleTask">
  <!-- mevcut MAIN/LEANBACK_LAUNCHER filtresi kalır -->
  <intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:scheme="nextv" android:host="watchnext" />
  </intent-filter>
</activity>
<receiver android:name=".data.home.WatchNextDismissReceiver" android:exported="true">
  <intent-filter><action android:name="android.media.tv.action.WATCH_NEXT_PROGRAM_BROWSABLE_DISABLED" /></intent-filter>
</receiver>
<provider android:name=".data.home.ArtworkProvider" android:authorities="${applicationId}.watchnext"
  android:exported="true" />
```

- **İzin notları.**
  - WRITE_EPG_DATA'yı resmî kanal sayfası ve Engage istiyor. Çalışma zamanı izin penceresi beklenmez; cihazda `dumpsys package` ile verildiği doğrulanmalı. Play listesinde görünebilir.
  - Yeni tehlikeli izin yok. Sunucu soketi için ek izin gerekmez (INTERNET mevcut).
  - Filtrede BROWSABLE yok: web sayfaları uygulamayı açamaz.
  - `allowBackup` ve `data_extraction_rules` değişmez.

## 4. Kimlik bilgisi ve güvenlik özeti

- Kaynak şifreleri yedekte yalnız izleyici şifresinden türetilen anahtarla şifreli durur. Şifre ağdan geçmez.
- Yerel sayfada yalnız şifreli dosya taşınır. Sayfa adres veya hesap göstermez; dış kaynak ve analitik yoktur.
- Sunucu 40 bitlik kodla, 5 deneme sınırıyla ve 10 dakikalık ömürle çalışır; ON_STOP'ta kapanır.
- Dışa açılan bileşenler: salt okunur afiş sağlayıcısı (tahmin edilemeyen jetonla) ve yalnız kendi satırını silebilen alıcı. Derin bağlantı içerik kimliği taşımaz; jeton yalnız cihaz içi bir kayda karşılık gelir.
- Gerçek kaynak URL'si veya kimlik bilgisi testlere girmez; sentetik `FixtureSources` ve MockWebServer kullanılır.

## 5. Kenar durumları (ek)

- Eski tek kaynaklı kayıt: `loadSources()` taşıma yolu geçerlidir. Dışa aktarma `transactions.read` içinde yapılır.
- Keystore kaybı: RECOVERY'den sıfırlama → Kurulum → "Yedekten geri yükle". Kimlikler aynı olduğu için `progressRaw` dahil tam geri dönüş olur.
- Geri yükleme sırasında başka bir ViewModel'in yazması: `SourceTransactions.shared` sıraya alır; commit öncesi registry yeniden okunup plan ona göre yeniden kurulur (`readRegistry()`).
- PBKDF2 süresi: TV SoC'de birkaç saniye beklenir. 65U95A'da 5 sn'yi aşarsa tur sayısı başlıkta düşürülebilir (alt sınır 310.000; öneri).
- Unutulan yedek şifresi kurtarılamaz; metinlerde açıkça söylenir.
- Watch Next süresi Int'e sığmayan değerlerde sınırlanır. Aynı başlık iki kaynakta ayrı anahtarlarla tutulur (ContentRef).

## 6. Satış, dil ve ülke ile ilişkisi (kullanıcının sorusuyla kesişen kısım)

- **Yabancı dilde davranış:**
  - Watch Next'e yalnız sağlayıcı verisi (başlık, açıklama, sayılar) yazılır. "Continue watching / İzlemeye devam et" satır adını ve süre biçimini başlatıcı, sistem dilinde kendisi çizer. Bu yüzden İngilizce ya da başka dildeki bir TV'de ek iş gerekmez.
  - Yedek dosyası dilden bağımsızdır (JSON).
  - Telefon sayfası v1'de Türkçe. Metinler `TransferPage` içinde tek bir kopya nesnesinde toplanır; ileride `Accept-Language`'a göre İngilizce eklenebilir (PRD §12: Türkçe ve İngilizce aday diller).
  - TV arayüzü Türkçe kalır. Uygulamada bugün `strings.xml` yok, metinler Kotlin'de sabit. Bu küme yeni metinleri ekran başına tek bir yerde toplar; yerelleştirme kararı ayrı bir iştir.
- **Farklı ülkelerden satın alma:**
  - Satın alma hakkı yedeğe hiçbir zaman girmez. Play Billing, satın almayı Google hesabıyla her ülkede geri getirir; yedek yalnız ayar ve kaynak taşır.
  - Watch Next ve yerel ağ aktarımı ülkeye bağlı değildir. Engage'in ülke ve uygunluk koşulları kayıt sırasında öğrenilir.
- **Gelir modeli:** TiviMate'te yedekleme Premium'da (TIVIMATE_GAP). Yedeğin ücretsiz mi ücretli mi olacağı, sahip kararları arasında PRD §12 gelir modeli kararına bağlandı.

## 7. Testler

**JVM (`app/src/test`):**
- `data/backup/BackupCryptoTest`: RFC 7914 §11 PBKDF2-HMAC-SHA256 vektörleri; gidiş-dönüş; yanlış şifre; değiştirilmiş başlık (AAD) ve şifreli metin; tur sınırları; 2 MiB sınırı; Türkçe şifrede NFC/NFD eşitliği; düz metnin sıfırlandığı.
- `BackupCodecTest`: bilinmeyen alanlar; `schema 2` reddi; geçersiz kaynağın atlanıp sayılması; geçersiz sıralama ve saat dilimi; `toString` maskesi.
- `BackupMergeTest`:
  - boş cihazda kimlikler korunur, `progressRaw` yazılır;
  - `sameConnection` eşlemesi ve cihaz kaydının korunması;
  - favori birleşimi; geçmişte yeni olanın kazanması ve 50 sınırı; ilerlemede yeni olanın kazanması;
  - farklı kimlikte yalnız çözülmüş ilerlemenin yeniden anahtarlanması;
  - kimlik çakışmasında yeni UUID;
  - koruma gevşemez (`hideAdult`, gizli kategori birleşimi); `applySettings=false`;
  - `filterId` `null`.
- `LocalTransferServerTest` (loopback'te gerçek soket, OkHttp istemcisi): kod büyük/küçük harf ve tire; 5 hatada kapanma; 411 / 413 / 400 / 404; 8 KB başlık sınırı; ilk yüklemeden sonra kapanma; süre dolumu; güvenlik başlıkları.
- `data/home/WatchNextPlannerTest`:
  - yarım film → CONTINUE; bitmiş film → yok;
  - bitmiş bölüm + kesin sıra → NEXT ve sonraki bölüm alanları; belirsiz sıra → yok;
  - canlı, yetişkin ve gizli kategori → yok;
  - dizi başına tek giriş; en yeni 10;
  - kaldırılmış giriş yeni etkileşim olmadan geri gelmez;
  - kapalı ayar → hepsi silinir; fark yalnız değişeni günceller;
  - sezon/bölüm yalnız `episodeOrderKnown` iken; başlıkta `displayTitle`.
- `WatchNextStoreTest`: kodlama, 200 sınırı, bozuk JSON'da boş liste.
- `ProgressResolutionTest`: katalog, M3U bölümleri ve geçmiş kimliklerinden `p2:` özetlerinin çözülmesi.

**Enstrümantasyon (`app/src/androidTest`):**
- `BackupStorageTest`: `applyRestore` tek commit; sonra `loadSources()` okunur. Aynı RFC vektörleri cihazda da geçer. İki kaynakla dışa aktar → temizle → içe aktar gidiş-dönüşü.
- `BackupFlowTest` (Compose):
  - Ayarlar → Yedek oluştur → alanlar OK ile klavye → QR ekranı. İlk odak "Bitti"; Geri, "Yedek oluştur" satırına döner.
  - İçe aktarma: test, `state.backup.transfer.url` adresine fikstür yedeği PUT eder → şifre → önizleme → Geri yükle → MockWebServer kataloğu → Ana Sayfa'da favoriler. İndirme sırasında Geri.
  - fontScale 1.5'te QR ve metin kırpılmaz; QR ve adres TalkBack semantiği.
- `SetupRestoreEntryTest`: kaynak yokken "Yedekten geri yükle" görünür, odaklanabilir, tag `setup-restore`.
- `WatchNextIntegrationTest` (TvProvider olan TV emülatörü):
  - fikstür filmi eşik ötesine oynat, oynatıcıdan çık → paketin tek CONTINUE satırı, başlık/konum doğru;
  - ayar kapanınca satır silinir;
  - `nextv://watchnext/<token>` → PLAYER, konum kayıtlı yerde; Geri → DETAIL → HOME;
  - uydurma jeton → HOME, çökme yok;
  - BROWSABLE_DISABLED yayını → satır silinir, aynı konum tekrar yayımlanınca geri gelmez;
  - sıcak açılışta `onNewIntent` yeni içeriği açar ve önceki oynatıcı durur.
- `ArtworkProviderTest`: bilinmeyen jeton → `FileNotFoundException`; "w" kipi reddedilir.
- Regresyon: `singleTask` nedeniyle `PilotFlowTest`, `SourceRecoveryFlowTest`, `EpisodeAutoplayFlowTest` ve çıkış sorusu yeniden koşulmalı.

## 8. Aşamalar ve efor (mühendis-günü)

1. **Watch Next (≈5,5 g):** planlayıcı ve mağaza 1; yayıncı, alıcı ve afiş vekili 1,5; derin bağlantı, `singleTask` ve VM 1; ayar ve tetikler 0,5; testler 1,5. Tek başına teslim edilebilir.
2. **Yedek çekirdeği + yerel ağ/QR (≈7 g):** kripto, kod çözücü ve birleştirme 1,5; DeviceStore dışa aktarma/uygulama 0,5; sunucu, sayfa ve QR 2; BackupScreen, VM akışları ve geri yükleme sonrası indirme 2; testler 1.
3. **USB/SAF ve fiziksel tur (≈1,5 g):** SAF yolu 0,5; 65U95A ve Google TV/Android TV emülatörlerinde Watch Next görünürlüğü, PBKDF2 süresi, misafir ağı denemesi 1.
4. **(Sahip kararına bağlı)** Favoriler kanalı yaklaşık 2 g; Engage değerlendirmesi ayrı.

Toplam ≈14 g (L).

## 9. Belgeler (uygulama sırasında)

- DECISIONS: yeni D kaydı; numara birleştirme sırasında verilir.
- PRD: EXT05 notu ("yedek ≠ eşitleme"). SCREENS: TV-072/073.
- TIVIMATE_GAP: iki satırın durumu.
- PLAY_STORE_READINESS: yeni izin, dışa açık sağlayıcı ve alıcı; Data safety değişmiyor, çünkü veri geliştiriciye gitmiyor.
- STATUS / HANDOFF / BACKLOG: yeni BKL kaydı.