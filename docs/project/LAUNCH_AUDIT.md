# Yayına hazırlık denetimi (0.14.77)

22 Ekim 2026 · Kod `ae5afb4` (91 / 0.14.77-design-preview), belgeler ve resmî mağaza kaynakları. Ana checkout'taki iş ayrıca not edildi (§2, §3, §6); aşağıda ".82" diye geçer.
- Denetim sırasında commitlenmemiş bu iş, sonra `main`'e `c25a66a` (0.14.83) olarak commitlendi.
- E1'deki debug imzaya düşme ve E4'teki yanlış gizlilik metni o commit'te değişmeden duruyor. İkisi de `main`'de düzeltilmeli.
- Tablo 0.14.83'e karşı yeniden koşulmadı.

**D113 (22 Ekim 2026):** Gelir modeli 7 gün ücretsiz deneme ve Google Play aboneliği oldu. İlk Play yayını artık ücretli. Bu yüzden ödeme sistemi, mali müşavir ve hukuk metinleri ilk yayından önce gerekir (E9, §5). Bu belge hukuki tavsiye değildir.

Gerekçeler ve kaynaklar [Play hazırlığı](PLAY_STORE_READINESS.md) (0.14.42) içindedir; bu belge onun güncel durum tablosudur, gerekçeyi tekrar etmez. Satış kanalı ve platformlar arası hesap önerisi ayrı belgededir: [SALES_CHANNELS](../product/SALES_CHANNELS.md).

## 1. Özet

- **Teknik temel hazır.**
  - Hedef API 36, minSdk 26. Android TV için istenen hedef API'nin üstünde.
  - Leanback, banner, dokunmatik zorunlu değil. Yalnız INTERNET ve ACCESS_NETWORK_STATE izinleri.
  - Yedekleme ve cihaz aktarımı kapalı. R8 ve kaynak küçültme açık. Tek `.so` 16 KB hizalı.
  - Kodda `Log` çağrısı yok, XML dış varlıkları kapalı, kimlik bilgisi loglanmıyor.
  - İki dil var: İngilizce varsayılan, Türkçe. Ayarlar'da "Dil / Language" seçici var.
  - PIN kuruluysa yetişkin filtresini kapatmayı ve gizli kategori açmayı koruyor (PIN isteğe bağlı, R7).
  - Çok dilli yetişkin kelimeleri ve "CSI:" önek hatası düzeltilmiş.
- **Play'e yüklemeyi engelleyen 8 iş vardı; D113 ile dokuzuncusu eklendi:** abonelik ve 7 günlük deneme (E9). Kod tarafı imza, AAB, gizlilik bağlantısı ve görsellerdir. Paket adı, marka, Console beyanları ve kapalı test sizin kararınız ya da hesabınızla ilgilidir.
- **Kalite kapıları açık** (§4). Fiziksel TV kabulü, 5 kişilik kumanda turu, p95 ölçümleri ve Figma.
- **Yeni gereksinim gizlilik vaadini değiştiriyor.** Platformlar arası hesap isteği (D111) gelince "NEXTV hesabı yok, sunucu yok" vaadi Premium için geçersiz olur. Mağaza metni, gizlilik politikası ve Data safety iki aşamalı yazılmalı (§5).
- **Acil (.82 commit'inden önce):** ana checkout'taki .82 gizlilik metni kodla çelişiyor. Commit edilmeden düzeltilmeli (§2, E4). Yayın sırası §7'dedir.

## 2. Engeller: Play'e yükleme

| # | Konu | Durum (0.14.77) | Kanıt | Kim |
|---|---|---|---|---|
| E1 | Üretim imzası | Açık | `apps/android-tv/app/build.gradle.kts:33` release debug anahtarıyla imzalanıyor. .82'de ortam değişkeninden imza eklendi, ama anahtar yoksa **sessizce debug anahtarına düşüyor**. | Siz (anahtar), kod |
| E2 | AAB | Açık | Yalnız `assembleRelease` kullanılıyor (`tools/run-device-tests.sh:70`). `bundle {}` bloğu hazır, `releases/` içinde AAB yok. | Kod |
| E3 | Kalıcı paket adı | Karar gerekiyor | `dev.iptvplayer.tv.pilot` ilk yüklemeden sonra değişmez. AGENTS ve D066 paket kimliğini kurulu veriyi korumak için sabit tuttu. Play'de imza değişeceği için bu gerekçe düşer ve yeni ad ek veri kaybı getirmez ([READINESS §2.1](PLAY_STORE_READINESS.md)). Ama adın değişmesi DECISIONS'a yeni bir kayıt ve AGENTS güncellemesi ister. | Siz |
| E4 | Gizlilik politikası URL'si ve uygulama içi bağlantı | Açık; .82 metni yanlış | About'ta bağlantı yok (`ui/settings/SettingsPages.kt:244-256`). .82'deki "Privacy and data safety" notu kaynak, katalog, favori, geçmiş ve kilitlerin "AES-256-GCM ile şifreli" olduğunu söylüyor. Kodda favori, geçmiş ve ilerleme düz SharedPreferences'ta (`data/DeviceStore.kt:156-187`). Kodda hiçbir yerde `setKeySize` yok (`DeviceStore.kt:27-30`, `ProtectionStore.kt:98-99`); Android Keystore varsayılanı kullanılıyor, "256" kodla kanıtlanmıyor. | Siz (metin), kod |
| E5 | Console beyanları | Açık | Data safety, IARC içerik derecelendirmesi, hedef yaş ve TV form faktörü beyanları yapılmadı. App access için yasal bir test kaynağı yok. | Siz |
| E6 | Mağaza görselleri | Açık | 1280×720 banner, 512×512 ikon ve 1024×500 tanıtım görseli yok. Git'te 567 adet 1920×1080 PNG QA çekimi var (`design/reviews`); ana checkout'taki `releases/` ile 821. Mağaza için seçilmedi, sentetik oldukları doğrulanmadı. | Siz, kod (sentetik çekim) |
| E7 | Geliştirici hesabı ve kapalı test | Başlamadı | Bireysel hesapta 12 test kullanıcısı 14 gün. TV'de çalıştığı için test kullanıcısının Android TV/Google TV cihazı gerekebilir. | Siz |
| E8 | Ad ve marka | D114: NEXTV adı değişecek; yeni ad açık | Play'de "NexTv IPTV player", iOS'ta "NexTv" var ([READINESS §3.1](PLAY_STORE_READINESS.md)). Yeni ad seçilince marka vekili araştırması, alan adı, logo ve kalıcı paket adı birlikte belirlenir. | Siz, marka vekili |
| E9 | Abonelik ve 7 günlük deneme (D113) | Yok | Ödeme kütüphanesi, üyelik ekranı, "Üyeliğim", Play Console'da abonelik, mali müşavir, koşullar ve avukat kontrolü, inceleme erişimi ([SUBSCRIPTION](../product/SUBSCRIPTION.md)). Kod yaklaşık 14,5 iş günü (ölçülmedi). | Siz, kod |

**E1 öneri:** Release görevinde anahtar yoksa derleme hata versin. Debug imzalı tasarım önizlemesi ayrı bir derleme türüyle sürsün. Böylece yanlışlıkla debug imzalı bir paket Play'e gitmez.

**E4 öneri:** Gizlilik metni ve Data safety kodla birebir olmalı. İki seçenek var:
- Metni düzeltmek ve anahtar boyunu metinden çıkarmak: "Kaynak, katalog önbelleği ve PIN şifreli; favori ve izleme geçmişi yalnız bu cihazda, uygulamanın özel alanında."
- Ya da bu verileri de şifrelemek; "256" yazılacaksa `setKeySize(256)` ve anahtar geçişi eklemek.

## 3. Riskler: yüklemeyi engellemez, sonra iş açar

| # | Konu | Kanıt | Öneri |
|---|---|---|---|
| R1 | Açılış sesinin lisans belgesi yok | D062, BKL-022. .82'deki izlenmeyen `assets/licenses/SPLASH-CHIME-LICENSE.txt` projenin kendi beyanı; hak sahibinden alınmış bir lisans değil | Belge ya da yeni ses |
| R2 | Açık kaynak lisans ekranı yok | `assets/licenses/Roboto-OFL.txt` APK'da ama gösterilmiyor. .82 notu yalnız özet. | Tam metinli lisans sayfası (Apache 2.0, NOTICE, OFL) |
| R3 | Bütün uygulamada şifresiz HTTP açık; HTTP kaynakta uyarı yok | `AndroidManifest.xml:19`, `data/Models.kt:26-34` | Kaynak eklerken kısa uyarı; politikada açıklama |
| R4 | Arayüzde "Xtream Codes" geçiyor | `res/values/strings.xml:197`, `ui/PilotApp.kt:261`, `ui/SourcesScreen.kt:213` | "Xtream API uyumlu" (karar sizin) |
| R5 | Sürüm adı akış User-Agent'ına gidiyor | `data/SourceHttp.kt:16` → `NEXTV/0.14.77-design-preview` | Üretimde `-design-preview` eki kalkmalı |
| R6 | Çökme raporu yok | Yalnız Android vitals | Her sürümde R8 mapping dosyası Console'a yüklenmeli |
| R7 | PIN isteğe bağlı | PIN yoksa yetişkin filtresi PIN'siz kapanıyor (`ui/settings/ParentalPages.kt:224`) | İlk kurulumda PIN önerisi ([READINESS §3.5](PLAY_STORE_READINESS.md)); karar sizin |
| R8 | IPTV uygulamalarının toplu kaldırılması | Mart 2026'da 36 uygulamanın Play ve iOS'tan kaldırıldığı haberi [3. taraf]; Perfect Player örneği | Sentetik görseller, nötr dil, telif bildirim adresi |
| R9 | Bilinen önek kırpması: "IT: Welcome to Derry" | `data/CatalogPresentation.kt` | Düşük |

## 4. Kalite kapıları: yayın kalitesi

- **Fiziksel TV kabulü açık:** .72 vitrin, .74 logo, .75 Devam → Son kanallar, .76 uzun OK, .77 Home odak. Splash ses eşzamanı ve hız/codec de bekliyor ([STATUS](STATUS.md)).
- **P0 işler review/planned:** BKL-010 medya/codec, BKL-012 beş kişilik kumanda turu, BKL-013 büyük katalog p95, BKL-014 renk/hareket, BKL-019/020/022–028.
- **Ölçüm kapılarının hiçbiri sağlanmadı** ([QUALITY_AUDIT](QUALITY_AUDIT.md)): odak p95 ≤ 100 ms, açılış p95 ≤ 2 s, ilk kare ≤ 4 s, arama ≤ 300 ms, 2 saat bellek, %99,5 çökmesiz oturum, cihaz matrisi.
- **Yazı boyutu matrisi** (1,5 / 2,0) "yayına hazırla" kabul turunda koşulur ([WORKFLOW](WORKFLOW.md)). Bu denetimde koşulmadı. Aday sürüm imzalı AAB olarak hazır olunca koşulur.
- **Düşük bellekli TV denemesi** (TV-ME) yapılmadı.

## 5. Yeni gereksinimin yayına etkisi

> **D113 güncellemesi:** Aşağıdaki maddeler "önce ücretsiz yayın, Premium sonra" varsayımıyla yazıldı. D113 ile ilk yayın ücretli. Gizlilik ve Data safety maddeleri sürer. Hesap, [SUBSCRIPTION](../product/SUBSCRIPTION.md) §1'deki A/B kararına göre ilk yayına girer (A) ya da Apple sürümünden önce gelir (B).

D111 platformlar arası hesap ve satın alma gereksinimidir. Ayrıntı [SALES_CHANNELS](../product/SALES_CHANNELS.md) içinde. İlk Play yayınına etkisi:

- **Ücretsiz ilk yayın hesapsız kalabilir.** Bugün geliştiriciye veri gitmiyor; "veri toplanmıyor" beyanı ilk yayın için doğru kalır. Ama "hiçbir zaman hesap ya da sunucu yok" gibi geleceğe uzanan bir vaat verilmez.
- **Premium satışı açılmadan önce** (öneri: hesap aynı sürümde gelir):
  - gizlilik politikası, kullanım koşulları ve Data safety formu e-posta, kullanıcı kimliği ve satın alma geçmişiyle güncellenir;
  - hesap silme eklenir: Android TV'de web üzerinden silme bağlantısı yeterli; telefon ve Apple sürümlerinde uygulama içi yol da gerekir;
  - KVKK aydınlatma metni ve yurt dışına aktarım (RevenueCat, kimlik sağlayıcı, barındırma) hazırlanır.
- **Mağaza metni** ([STORE_LISTING_EN](../product/STORE_LISTING_EN.md)) "No NEXTV account, no NEXTV server" diyordu. Taslak, ilk yayında doğru olan ve geleceği bağlamayan bir cümleye çevrildi.
- **Paket adı ve imza** hesap sisteminden bağımsız. Ama ilk yüklemeden önce kesinleşmeli (E1, E3).

## 6. Belge tutarsızlıkları

- **BACKLOG eksikleri:** İmza/AAB, paket adı, gizlilik ve Console beyanları, mağaza görselleri, marka ve kapalı test için kayıt yoktu. BKL-042–046 olarak eklendi; platformlar arası hesap işi BKL-047.
- **SALES_STRATEGY dil bilgisi eskidi:** "`strings.xml` yok, her cihazda Türkçe" diyor; uygulama artık İngilizce varsayılan ve Türkçe kaynaklarla geliyor. Belgenin başına not düşüldü.
- **Eskimiş ad satırları:** [DECISIONS](../decisions/DECISIONS.md) "Açık kararlar" satırı ve [PRD](../product/PRD.md) §1/§12 ürün adını hâlâ açık gösteriyor. D066 ile kapandı.
- **QUALITY_AUDIT eski:** 13 Ekim tarihli. SAFE01 satırı D073/D074'ten önceki durumu anlatıyor.
- **İki satış öneri belgesi çelişiyor:**

  | Konu | [SALES_STRATEGY](../product/SALES_STRATEGY.md) | [gap PLAN](../design/gap/PLAN.md) / sales_draft |
  |---|---|---|
  | Ücretli paketin adı | NEXTV Premium | NEXTV Plus |
  | Fiyat | 12,99 → 19,99 $ | 14,99 $ |
  | Ücretsiz kaynak | 2 kaynak | Çoklu kaynak ücretsiz |
  | Almanca | İlk dalgada | İkinci dalgada |

  İkisi de öneri. Kod da "Plus" diyor (`GateResult.NeedsPlus`). "Hesap yok, sunucu yok" varsayımları D111 ile geçersizleşti. Satış kararından önce uzlaştırılmalı.
- **Ana checkout ile numara çakışması:** Ana checkout'ta commitlenmemiş D109–D110 (vitrin İncele, S15c) var. Bu dal platformlar arası gereksinimi D111 olarak kaydetti; birleşmede sıra korunur.

## 7. Önerilen sıra

| # | İş | Kim | BKL |
|---|---|---|---|
| 1 | Marka araştırması; gerekirse yeni ad. Paket adı bu karardan sonra seçilir. | Marka vekili, siz | BKL-045 |
| 1b | D113 ile ücretsiz/Premium sınırı kalktı. Yerine: hesap A/B kararı, plan adı, dönem, fiyat, ödeme ekranının yeri ([SUBSCRIPTION](../product/SUBSCRIPTION.md) §5); mali müşavir ve avukat görüşmesi | Siz | BKL-048 |
| 1c | Abonelik ve 7 günlük deneme kodu, Play Console'da abonelik, gerçek Play testi | Kod, siz | BKL-048 |
| 2 | .82 gizlilik metnini kodla eşitlemek (E4) | Kod | BKL-043 |
| 3 | Upload anahtarı, Play App Signing seçimi, release'te anahtar zorunluluğu, `bundleRelease` | Siz, kod | BKL-042 |
| 4 | Gizlilik politikası ve kullanım koşulları (TR/EN), telif bildirim e-postası, uygulama içi bağlantılar, lisans sayfası | Siz (taslak), avukat, kod | BKL-043 |
| 5 | Sentetik içerikle mağaza görselleri ve banner; yasal test kaynağı | Siz, kod | BKL-044 |
| 6 | Geliştirici hesabı, 12 kişi × 14 gün kapalı test | Siz | BKL-046 |
| 7 | Fiziksel TV kabul turu, p95 ölçümleri, yazı boyutu matrisi, düşük bellekli cihaz | Siz, kod | BKL-010/012/013/014 |
| 8 | Satış kanalı ve hesap modeli D112 ile kararlaştı. Hak altyapısı ve kimlik sağlayıcı kararı ile [BKL-047 planı](../design/gap/account.md) (Premium satışından önce) | Siz, kod | BKL-047, BKL-038 |
