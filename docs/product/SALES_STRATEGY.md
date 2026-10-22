# NEXTV satış stratejisi, fiyat, diller ve uluslararası satış

18 Ekim 2026 · Masa başı araştırma ve kod okuması (0.14.43, `95ddba7`). Buradaki her öneri **öneridir**, karar değildir. Vergi ve hukuk kısmı mali müşavir ya da avukat görüşünün yerini tutmaz. **[3. taraf]** işareti bilginin satış ortaklığı, forum, haber ya da fırsat sitesinden geldiğini gösterir; bu bilgiler resmî kaynakla doğrulanmadı. Köşeli parantezdeki sayılar §10'daki kaynaklardır.

**22 Ekim 2026 · D113:** Kullanıcı gelir modelini seçti: 7 gün ücretsiz deneme, sonra Google Play aboneliği. Bu belgenin "ücretsiz + tek seferlik Premium" önerisi, ücretsiz/Premium sınırı ve tek seferlik fiyat tablosu tarihçedir. Güncel plan [SUBSCRIPTION](SUBSCRIPTION.md) içindedir. Dil, pazar, vergi ve Play mekaniği notları bilgi olarak sürer.

**22 Ekim 2026 güncellemesi:** Kullanıcı, platformlar arası Premium gereksinimini bildirdi (D111). Bu belgedeki şu bölümler güncellenir:
- "sunucusuz geri yükleme" yalnız aynı mağaza içinde geçerli kalır; başka platforma taşıma hesap ve sunucu ister,
- "sunucusuz yapıda cihaz sınırı konamaz",
- "NEXTV hesabı yok, sunucumuz yok" mesajı,
- "Kendi web mağazası neden şimdi değil" altındaki "sunucu yok" gerekçesi,
- "TV için uygun program görünmüyor" ifadesi. AEA programları Android TV'yi de kapsıyor, ama işletme kaydı istiyor.

Güncel öneri [SALES_CHANNELS](SALES_CHANNELS.md) içindedir. Fiyat, dil, pazar ve vergi bölümleri öneri olarak sürer. İki olgu eskidi:
- TiviMate Premium'u artık kendi hesabına bağlı; "5 cihaz" bilgisi birincil kaynakta bulunamadı.
- §1'deki "`strings.xml` yok, uygulama her cihazda Türkçe açılıyor" bilgisi geçerli değil; uygulama İngilizce varsayılan ve Türkçe kaynaklarla, dil seçiciyle geliyor ([LAUNCH_AUDIT](../project/LAUNCH_AUDIT.md) §1).

## 1. Özet

**Önerilen model (öneri):**
- NEXTV Play'de ücretsiz yayımlanır.
- Canlı TV, rehber, filmler ve diziler, arama, PIN ve kanal gizleme ücretsizdir.
- Güç kullanıcılarına "NEXTV Premium" adlı **tek seferlik lisans** (tüketilmeyen ürün) uygulama içinden Play Billing ile satılır.
- Satın alma Google hesabına bağlıdır. O hesaptaki bütün TV'lerde sunucusuz geri yüklenir.
- Lansman fiyatı 12,99 $ ve 249,99 TL'dir. 8 hafta sonra 19,99 $ ve 399,99 TL olur.
- Diğer ülkeler üç basamakta elle fiyatlanır.
- Yıllık abonelik sonraya kalır.
- Uygulama sistem dilini izler. Desteklenmeyen bir dilde İngilizceye düşer.

- **Neden tek seferlik (öneri):** Sunucu, yenileme takibi, TL'de yıllık zam ve 6502 otomatik yenileme sorusu gerekmez. Rakiplerde tek seferlik seçenek yaygın: Sparkle [27], TiviMate [3. taraf, 30].
- **Google'ın payı:**
  - ABD, İngiltere ve AEA'da 30.06.2026'dan beri ilk 1 M$ için %10 + %5 faturalama bedeli alınıyor. Tek seferlik ürünler de buna dahil.
  - Avustralya ve Japonya 30.09.2026'da bu düzene geçti. Kore 31.12.2026'da geçecek [2].
  - Türkiye dahil kalan ülkelerde eski düzen 30.09.2027'ye kadar sürüyor. Bu düzende ilk 1 M$ için %15 oranı ancak **%15 kademesine kayıtla** (hesap grubu ve şartlar) geçerli. Kayıtsız geliştiricinin oranı sayfada yazmıyor [1][3].
- **Vergi:**
  - Satış vergisini AB, İngiltere, Norveç, Türkiye ve ABD'nin listelenen eyaletlerinde Google topluyor.
  - Japonya, Hindistan, S. Arabistan, BAE, Meksika ve Kanada'da da Google topluyor, ama yalnız yurt dışındaki geliştirici için.
  - Brezilya için sayfa net değil [8].
  - Ödeme aylık yapılıyor ve TRY olarak geliyor [4][5].
- **Sınır ilk günden çizilmeli (öneri):** Ücretsiz bir özelliği sonradan ücretliye almak en sert tepkiyi getirir.
- **Satışın zamanı (öneri):** Bugün Premium'a konabilecek hazır tek özellik çoklu kaynak. Satış catch-up, gelişmiş EPG araçları ve çoklu kaynak hazır olunca açılır.
- **PIN, ebeveyn denetimi ve kanal/kategori gizleme hiçbir zaman ücretli olmaz (öneri).**
- **Yerelleştirme ön koşul (öneri):** `strings.xml` yok, ~550 Türkçe sabit metin var. Uygulama bugün her cihazda Türkçe açılıyor. İlk dil dalgası (TR, EN, DE) ~4–6 hafta sürer.
- **Önce yayın engelleri:** Ad/marka çakışması, imza, gizlilik politikası, PIN (PLAY_STORE_READINESS) ve çok dilli yetişkin filtresi.
- **Kanallar (öneri):** İkinci kanal koşullu olarak Fire OS. Kendi web mağazası yok.

**Sahibin vermesi gereken 5 karar:**
1. **Model:** Ücretsiz + tek seferlik Premium ile başlamak, aboneliği sonraya bırakmak.
2. **Sınır:** Ücretsiz kaynak sayısı (öneri: 2) ve hangi EPG araçlarının ücretli olacağı.
3. **Fiyat ve lansman:** Geçiş fiyat artışıyla mı (12,99 → 19,99 $ ve 249,99 → 399,99 TL), indirim teklifiyle mi yapılacak? Üç basamaklı fiyat düzeni.
4. **Satış kapsamı:** Satın alma bütün ülkelere açık, pazarlama dar mı olsun (öneri), yoksa satış yalnız seçili ülkelerde mi? Vergisi geliştiricide kalan ülkelerde Premium kapalı mı kalsın?
5. **Hesap ve vergi yapısı:** Bireysel hesap ve 20/B mi, şirket ve kuruluş hesabı (D-U-N-S) mı? Karar ilk satıştan önce verilmeli, çünkü hesap türü ve ödeme profilinin ülkesi sonradan değişmiyor [6][7].

## 2. İş modeli seçenekleri

| Seçenek | Artı | Eksi | İade ve kötüye kullanım | Play/TV uyumu |
|---|---|---|---|---|
| **(a)** Ücretsiz + tek seferlik Premium | Sunucusuz; tek ürün; rakiplerde yaygın | Tekrarlayan gelir yok; süresiz yükümlülük (Plex ömür boyu fiyatını 749,99 $'a çıkardı [28]); yerleşik deneme yok | Google'dan 48 saat, sonrası geliştiriciye [17]; AEA'da "Hizmet" beyanında 14 gün [25] | İyi |
| **(b)** Ücretsiz + yıllık abonelik | Tekrarlayan gelir; 3 gün–3 yıl deneme hazır [15] | Yenileme, ödeme sorunu süresi, zam bildirimleri; TL'de her yıl zam; 6502 sorusu | İptal kolay | İyi, destek yükü fazla |
| **(c)** İkisi birden | İki kitleyi yakalar | (a) ve (b)'nin yükü birlikte | — | İyi |
| **(d)** Ücretli uygulama | Basit; Aile Kitaplığı'nda paylaşılır [16] | Kaynağı görmeden ödeme; ücretsiz yayımlanan uygulama sonradan ücretliye çevrilemiyor [9] | 48 saat | Zayıf |
| **(e)** Süreli deneme, sonra kilit | Hızlı dönüşüm | Sunucusuz süre takibi yeniden kurulumla sıfırlanır; sürpriz ödeme duvarı en çok öfke yaratan şey (Smarters yorumları [26]) | Yüksek | Orta |

**Öneri:**
- (a) ile başlamak, (c)'ye hazır olmak.
- Yıllık plan (7,99 $) üç koşul sağlanınca eklenir:
  - catch-up ve timeshift Premium'da,
  - 3–6 aylık satış verisi var,
  - avukat 6502'yi değerlendirdi.
- Tek seferlik/yıllık fiyat oranı 2,5 olur. Sparkle'da bu oran 2,63 [27].
- Ücretsiz katman kalıcı olduğu için ayrı deneme gerekmez.
- Değiştirilmiş APK kaçağı kabul edilir. DRM'e yatırım önerilmez.

**Ücretsiz ve Premium (öneri; özellikler TIVIMATE_GAP'ten):**

| Ücretsiz, her zaman | Premium |
|---|---|
| M3U ve Xtream, **2 kaynak** (kapalı testte fazlası olanların kaynakları korunur) | Çoklu kaynak (16 kaynak testli) |
| Canlı TV, CH±, rakamla kanal, son kanal, oynatıcı üstü kanal listesi | Catch-up (EXT02), sağlayıcı destekliyorsa |
| Rehber: sağlayıcının M3U başlığından gelen rehberler (bugün 4'e kadar birleşiyor, `CatalogParser.kt` 136–143), otomatik eşleme, saat dilimi ve kaydırma | Elle eklenen XMLTV, elle eşleme, EPG'de program araması |
| Filmler ve diziler: vitrin, devam et, otomatik sonraki bölüm | Canlı timeshift, sağlayıcı destekliyorsa |
| Arama, tek favori listesi, **tek kanal ve kategori gizleme/kilitleme** | Favori grupları, sıralama, yeniden adlandırma |
| Ses/altyazı seçimi, en-boy/zoom | AFR, arabellek ve kod çözücü ayarı, harici oynatıcı, PiP |
| Açılışta ve 6 saatte bir yenileme | Zamanlanmış güncelleme, program hatırlatıcısı |
| **PIN, ebeveyn denetimi, yetişkin filtresi** | Şifreli yedek ve cihaz aktarma (EXT05), temalar |
| Bütün diller, Watch Next | Sonra: kayıt, çoklu ekran (P2, L) |

**Koruma neden ücretli olmaz:**
- Play, katalog uygulamasındaki yetişkin içeriğe ancak reşit olmayanların erişimi kısıtlanırsa izin veriyor. Bu kısıtı ödemeye bağlamak savunmayı zayıflatır (READINESS §3.5).
- Yetişkin filtresi bugün bazı yabancı kelimeleri kaçırıyor (§5). Filtrenin kaçırdığı bir kanalı gizlemek ücretli olursa, ebeveyn denetimi dolaylı yoldan ücretli hale gelir.
- Sparkle ebeveyn denetimini ve gizlemeyi Plus sürümünde satıyor [27]. Bunları ücretsiz vermek fark yaratır.

## 3. Fiyat

**Çıpalar:**
- **Sparkle:** Sitesinde tek seferlik 20,99 $, yıllık 7,99 $ ve aylık 1,49 $ seçenekleri var. Site "lifetime" değil "one-time" diyor [27].
- **Plex:** 01.07.2026'dan beri ömür boyu plan 749,99 $ (önceden 249,99 $). Mevcut sahipler etkilenmedi. 249,99 $'a 5 yıllık plan eklendi [28].
- **Play'deki rakip fiyatları:** İlk taslakta Play'den şu uygulama içi fiyatlar not edilmişti: TiviMate en çok 33,99 $ ve 599 TL, iMPlayer 34,99 $, Sparkle 19,99 $ ve 699,99 TL, Smarters 214,99 TL. İkinci kontrolde bu fiyatlar doğrulanamadı [26].

**Lansman mekanizması (öneri):**
- 12,99 $ ilk 8 hafta **normal fiyat** olarak girilir. Üstü çizili fiyat, indirim yüzdesi ya da "önceki fiyat" gösterilmez.
- 8 hafta sonra fiyat 19,99 $'a çıkar. Geçiş tarihi önceden duyurulur.
- Düşük başlangıcın gerekçesi: marka yeni, DVR ve çoklu ekran yok.
- Play'in tek seferlik ürün indirim teklifi, uygulamaya tam fiyatı ve indirim yüzdesini döndürür [11]. Hiç uygulanmamış 19,99 $'ı eski fiyat gibi göstermek risklidir. Bu yüzden indirim teklifi ancak 19,99 $ fiilen uygulandıktan sonra kullanılır.
- Türkiye'de 01.08.2026'dan beri indirimli satışta eski fiyat olarak, indirimden önceki son 10 günün en düşük fiyatı gösteriliyor. Hizmetlerde bir önceki fiyat esas alınıyor [41, haber; Resmî Gazete künyesi doğrulanmadı].
- AB Omnibus'un 30 gün kuralının uygulama içi ürünlere uygulanıp uygulanmadığı doğrulanmadı.

**Önerilen fiyatlar (öneri):**

| Bölge (para birimi) | Basamak (ABD = %100) | Tek seferlik | Lansman | Yıllık (2. aşama) |
|---|---|---|---|---|
| ABD (USD, vergi hariç) | A | 19,99 | 12,99 | 7,99 |
| Kanada (CAD) | A | 26,99 | 17,99 | 10,99 |
| Euro bölgesi (EUR, KDV dahil) | A | 19,99 | 12,99 | 7,99 |
| İngiltere (GBP, KDV dahil) | A | 17,99 | 11,99 | 6,99 |
| S. Arabistan / BAE (SAR/AED) | A−, ~%75 | 54,99 | 36,99 | 22,99 |
| Polonya (PLN) | B+, ~%75 | 54,99 | 36,99 | 21,99 |
| Romanya, Çekya, Macaristan | B, %55–65 | Console kuru + yuvarlama | | |
| Brezilya (BRL) | B, ~%55 | 59,90 | 39,90 | 24,90 |
| Meksika (MXN) | B, ~%55 | 199 | 129 | 79 |
| **Türkiye (TRY, KDV dahil)** | C, ~%40 | **399,99** | **249,99** | 149,99 |
| Hindistan, Mısır, Endonezya | C | Önerdiğimiz taban 2,99–3,99 $ (Play sınırı değil) | | |

- **Kur:** TCMB'nin 16.10.2026 döviz satış kuru 1 $ = 49,45 TL [31]. Diğer kurlar varsayımdır.
- **Play sınırları:** Play'in bölgesel alt/üst fiyat sınırları ayrı bir tabloda ve doğrulanamadı [9].
- **Polonya:** 2023'te TiviMate'in fiyatı 164,99 zł olarak görüldü [30, 3. taraf]. Bu yüzden Polonya fiyatı satın alma gücü oranının üstünde tutuldu.
- **Türkiye'de net gelir:** 249,99 TL'den %20 KDV, Google'ın %15'i (kademe kaydıyla) ve 20/B kesintisi düşülünce ~150 TL (~3,0 $) kalır.
- **TL fiyatının oranı:** ABD fiyatının KDV dahil ~%40'ı, KDV hariç ~%34'ü.

**Play mekaniği [9]:**
- Varsayılan fiyat USD olarak girilir. Play bunu o günün kuruyla çevirir, bazı ülkelerde vergiyi ekler ve yuvarlar.
- Kur değişince fiyat kendiliğinden güncellenmez. TL fiyatı 3–6 ayda bir elle kontrol edilmeli (öneri).
- Fiyat şablonları 27.10.2025'te kaldırıldı.
- Fiyat deneyleriyle [10] sonra 12,99 $ ve 14,99 $ karşılaştırılabilir. Deneylerin TV'de ve tek seferlik üründe çalıştığı doğrulanmadı.

**Bölgeler arası fiyat farkı:**
- Play ülkesi ilk ayardan ve her değişiklikten 90 gün sonra değiştirilebiliyor. Bunun için o ülkede bulunmak ve oradan bir ödeme yöntemi gerekiyor [18].
- Almanya'dan Türk hesabıyla TiviMate satın alındığı paylaşılmış [29, 3. taraf]. Yani ucuz ülkeden alım olur.
- TL fiyatını rakiplerin TL bandında tutmak bu etkiyi azaltır (öneri: kabul edilsin).

**Sunuş (öneri):**
- Ürün "tek seferlik lisans" olarak sunulur ve kapsamı yazılır.
- "Ömür boyu" ya da "sonradan eklenen her şey dahil" gibi süresiz taahhüt içeren ifadeler kullanılmaz. Metin avukata gösterilir.
- Başlıkta ve ikonda fiyat ya da promosyon olmaz [20].

**Cihaz:**
- Satın alma, aynı hesaptaki bütün TV ve telefonlarda `queryPurchasesAsync` ile geri gelir [13].
- Sunucusuz yapıda cihaz sınırı konamaz. Sunuş "hesabındaki bütün TV'lerde" olur. Karşılaştırma için TiviMate 5 cihaza izin veriyor [3. taraf, 30].
- Aile Kitaplığı uygulama içi satın almayı paylaşmaz [16].

## 4. Uluslararası satın alma

1. **Hesap:** Bireysel hesap para kazanmaya başlayınca tam adres Play'de herkese açık oluyor. Bazı bölgelerde telefon da istenebiliyor. Kuruluş hesabı D-U-N-S istiyor [7].
2. **Satıcı profili:**
   - Türkiye destekleniyor. Ödeme para birimi TRY, USD/EUR seçeneği yok [4].
   - Banka hesabı aynı ülkede olmalı [6]. 20/B seçilirse istisnaya özel hesap gerekiyor.
   - Satış ile ödeme arasında 15–45 gün var ve gelir TRY'ye çevriliyor. Bu bir kur riski.
3. **%15 kademe:** İlk satıştan önce hesap grubu kurulur ve şartlar kabul edilir [3]. Yeni düzende bazı oranlar Apps Experience Program katılımına bağlı görünüyor; sayfadaki tablo belirsiz [2].
4. **Ürün:**
   - Tek bir tüketilmeyen ürün. USD varsayılan, bölge fiyatları elle girilir. Ürün adı ve açıklaması dil başına girilir.
   - AEA için ürünün "Dijital içerik" mi "Hizmet" mi olduğu beyan edilir. Dijital içerikte cayma hakkı yok, hizmette 14 gün iade var. Emin olunamazsa Hizmet öneriliyor [25].
   - Aynı sayfada indirimli KDV (video) ve ABD "streaming ürünü" seçenekleri de var. Bunlar içerik sağlamayan bir oynatıcıya uymayabilir. Müşavire sorulmadan seçilmez.
5. **Kapsam (öneri):**
   - Uygulama bütün ülkelere dağıtılır.
   - Vergisi geliştiricide kalan ülkelerde (Kolombiya, Peru, Tanzanya, Zimbabve ve İsviçre) Premium, müşavir onayına kadar kapalı kalır [8].
   - İsviçre'de zorunlu KDV mükellefiyeti dünya genelinde 100.000 CHF ciroda başlıyor. ESTV sayfasında elektronik hizmete özel bir kural yok [40].
   - Premium'un kapalı olduğu ülkede ödeme ekranı `ITEM_UNAVAILABLE` hatasını ve boş ürün listesini düzgün göstermeli [11].
   - Rusya'da Play faturalandırması 10.03.2022'den beri durdu. Belarus'ta ücretli uygulama alınamıyor, ama ücretsiz uygulamadaki uygulama içi satın alma engellenmiyor [21].
6. **Ödeme:** Bir ayın satışı ertesi ayın ~15'inde, 1 $ karşılığı eşikle TRY olarak gelir [5]. Alıcı, Google'ın kendi ülkesindeki ödeme yöntemleriyle öder.

**Fire TV (öneri: Play'den 3–6 ay sonra, yalnız Fire OS cihazlarda ve Türkiye'ye elektronik ödeme doğrulanırsa):**
- Amazon Appstore, Amazon dışı Android cihazlarda 20.08.2025'te kapandı. Fire OS'ta sürüyor [32].
- Vega OS React Native kullanıyor. İlk Vega cihazı Fire TV Stick 4K Select (Eylül 2025). Bu cihazda yalnız Appstore uygulamaları kuruluyor; Android uygulaması yeniden yazılmadan çalışmıyor [33].
- Sonraki Stick'lerin de Vega'ya geçtiği ve Fire OS desteğinin sürdüğü bilgisi yalnız 3. taraf kaynaklarda [33].
- Amazon'un engellediği uygulamalar korsan yayın sunanlar (BeeTV, Cinema HD gibi), bütün oynatıcılar değil [35, 3. taraf].
- Ayrı bir Amazon IAP entegrasyonu gerekir.
- Amazon'un ödeme sayfası elektronik ödeme yapılan ülkeleri listelemiyor. Uygun olmayan ülkelerde ödeme pazar başına çekle ve 100 $/€/£ eşiğiyle yapılıyor.
- 80/20 gelir paylaşımı yalnız Small Business Accelerator kapsamında (yıllık 1 M$ altı) [34].
- Fire OS'ta Türkçe arayüz dili yok [36].

**Kendi web mağazası neden şimdi değil:**
- Lisans anahtarı sunucu ister ve "sunucu yok" ilkesini bozar.
- Vergi, tüketici hukuku ve geri ödeme bize kalır.
- Play, uygun ülkelerde program kaydıyla alternatif faturalama ve uygulama dışı teklife izin veriyor [19]. ABD dış bağlantı programı ise mobil ve tablet için tanımlı [22]. TV için uygun bir program görünmüyor; sunucusuz yapıda buna zaten gerek yok.
- Paddle ve Lemon Squeezy'nin Türkiye desteği doğrulanmadı.

## 5. Diller

| Dalga | Diller | Neden (öneri) |
|---|---|---|
| 1, lansmanla | TR, EN (yedek), DE | Türkiye, Almanya, Avusturya; EN İngiltere, İrlanda, Kuzey Amerika ve İskandinavya'yı karşılar |
| 2, +1–3 ay | FR, NL, PL, ES, PT-BR, IT | Rakiplerin ortak listesi; Fire TV'de var. Dil açmak pazarlama demek değil |
| 3 | AR (RTL), SV/NB, RO/CS/EL/HU, ID/TH/VI, HE; RU/UK düşük | AR için RTL testi ve anadil kontrolü |

**Davranış (öneri):**
- Sistem dili desteklenen bir dilse uygulama o dilde açılır, değilse İngilizce (`values/` EN, `values-tr/` TR).
- **Üç ayrı eksen:**
  - Arayüz dili cihazdan ya da uygulamadan gelir.
  - Mağaza içeriğini Play ülkesi belirler [18]. Para biriminin de buna bağlı olduğu görünüyor, ama sayfa bunu açıkça yazmıyor.
  - Vergi alıcının ülkesine göredir.
  - Örnek: Almanya'daki Türk kullanıcı Türkçe arayüzde EUR fiyat görür.
  - Play ödeme penceresinin hangi dili izlediği TV'de test edilmeli.
- **Dil seçici:**
  - Yeri Ayarlar > Görünüm ve ses > "Dil / Language" olur. Seçenekler "Sistem dili" ve her dilin kendi adı.
  - Android 13 ve üstünde `LocaleManager` kullanılır.
  - Uygulamada `minSdk = 26` ve `MainActivity : ComponentActivity` var. Android 12 ve altında AppCompat yolu AppCompat 1.6+ ve `AppCompatActivity` istiyor. `generateLocaleConfig` (AGP 8.1+) yalnız 13 ve üstünde işe yarıyor [23]. Bu yüzden etkinlik sınıfı değişmeli ya da bağlam elle sarılmalı.
  - Google'ın belgesi TV'den söz etmiyor ve Android TV'de uygulama dili ekranı doğrulanamadı. Uygulama içi seçici bu yüzden şart.
- Kanal adları ve EPG çevrilmez. Arayüz dili, ses ve altyazı tercihinden ayrıdır.
- Fiyat yalnız `getFormattedPrice()` ile gösterilir.
- Yarım çeviriyle yayın yapılmaz. `MissingTranslation` CI'da hata sayılır.
- 48 saatten sonraki iade talepleri geliştiriciye geliyor [17]. Bunun için EN ve DE destek metni, iade politikası sayfası ve hazır yanıtlar hazırlanır.

**RTL:**
- `supportsRtl` açık. Compose'un start/end kullanımı düzenin çoğunu kendiliğinden aynalar.
- Elle düzeltilecekler: 13 D-pad işleyicisi, 5 çizim noktası ve geri oku.
- RTL kodu 1. dalgada hazırlanır, Arapça 3. dalgada yayımlanır (+1–1,5 hafta).

**Kod denetimi:**
- ~550 Türkçe sabit, 442 farklı metin, ~1.755 kelime var. `R.string` hiç kullanılmıyor.
- `"Tümü"` ve `"Diğer"` anahtar olarak kullanılıyor.
- 5 yerde sabit `Locale("tr")`, ~20 çoğul noktası var.
- 205 test bulucusu metne bağlı.
- İki hata bulundu:
  - "CSI: Vegas" gibi başlıklar ülke öneki sanılıp kırpılıyor.
  - Yetişkin filtresi "adultos", "erwachsene" gibi kelimeleri tanımıyor.

**Geçiş sırası:**
1. Anahtarları ayırma (3–4 gün)
2. Biçim katmanı (2)
3. Türkçe varsayılanla kaynaklara taşıma (5–7)
4. Testleri dilden bağımsızlaştırma (2–3)
5. AppCompat ya da bağlam sarma (1–2)
6. Dil seçici (2)
7. Varsayılanı EN yapma (2–3)
8. Sahte dil kontrolü (2–3)
9. Çeviri (dil başına ~0,5)

Toplam ~21–28 iş günü.

**Mağaza:**
- Varsayılan mağaza dili EN-US olabilir.
- Her dil için mağaza metni ve sentetik verili TV ekran görüntüleri gerekir.
- Play'in ücretsiz makine çevirisi mağaza metnini ve uygulama içi ürün adlarını kapsıyor. Kapsanan diller: AR, FR, DE, ID, IT, JA, PT-BR, ES.
- Gemini çevirisi ücretsiz, ama `strings.xml` istiyor ve seçilen dildeki mevcut çevirinin üzerine yazıyor.
- İnsan çevirisi kelime başına 0,07 $'dan başlıyor [24].
- Taslak makineyle çıkarılır, kontrolü anadil sahibi yapar (dil başına 1–2 saat).

## 6. Pazarlar ve konumlandırma

**Lansman sırası (öneri; gerekçe diaspora, arayüz dili ve yasal kaynakların bulunabilirliği):**

| # | Pazar | Gerekçe |
|---|---|---|
| 1 | Türkiye | Ev pazarı, kapalı test kitlesi |
| 2 | Almanya, Avusturya | En büyük Türk diasporası; DE; FRITZ!Box kablo TV listesi, HDHomeRun |
| 3 | Hollanda, Belçika | Diaspora; EN, sonra NL |
| 4 | İngiltere, İrlanda | EN; HDHomeRun, Jellyfin/Plex Live TV |
| 5 | Kanada, ABD | EN; HDHomeRun, Jellyfin/Plex Live TV |
| 6 | İskandinavya | EN yeterli |
| 7 | Fransa | ~700 bin diaspora; FR 2. dalgada |

- İsviçre, vergi kararı verilene kadar pazarlanmaz.
- Şimdilik pazarlama yapılmayacak ülkeler: İtalya, İspanya, MENA, Latin Amerika, Hindistan. Buralarda satın alma açık kalır.
- Talep, "m3u player", "hdhomerun", "xmltv" gibi nötr terimlerle yayından önce Google Trends'te karşılaştırılmalı. Bu karşılaştırma yapılmadı.

**Mesaj (öneri):**
- **Ana mesaj:**
  - Kendi yasal kaynağın için sade bir TV oynatıcısı; filmler ve diziler için tasarlandı.
  - NEXTV hesabı yok, sunucumuz yok, izleme verisi yok. Bu ifade Veri güvenliği formuyla birebir uyumlu olmalı.
  - Tek uygulama, tek satın alma, aktivasyon kodu yok.
- **Açıklamanın başında:** NEXTV içerik sağlamaz.
- **Ölçülü dil:** "Sınırsız" değil "çoklu kaynak" denir. Catch-up ve timeshift "sağlayıcınız destekliyorsa" diye sunulur.
- **Kullanılmayacaklar:** rakip adı, "en iyi", "ücretsiz kanal", maç ya da kanal örneği, logolar, başlıkta "Xtream Codes" (READINESS §3.2).

**Kanallar (öneri):**
- Mağaza optimizasyonu (IPTV player, M3U, EPG, Android TV).
- Yasal kaynak rehberleri (FRITZ!Box, Init7, HDHomeRun, Jellyfin).
- Topluluklar: r/AndroidTV, r/htpc, r/selfhosted, XDA, Technopat.
- Kullanılmayacaklar: satıcı ortaklığı, kupon, beyaz etiket ve Telegram.
- Telif şikâyetlerine karşı bildirim süreci baştan hazırlanır.

## 7. Uygulama içi teknik iş listesi (öneri sırası)

| # | İş | Boy |
|---|---|---|
| 0 | Ön koşullar: ad/marka, upload anahtarı, AAB, paket adı, gizlilik politikası, PIN (BKL-028), çok dilli yetişkin filtresi, başlık öneki hatası | READINESS + S |
| 1 | Karar kaydı: model, sınır, fiyat, lansman mekanizması | S (sahip) |
| 2 | Tek merkezli yetki ve yetenek modeli | S |
| 3 | Mağaza soyutlaması (Play şimdi, Amazon koşullu) | S |
| 4 | Play Billing Library (31.08.2026'dan beri 8+ zorunlu, uzatma 01.11.2026'ya kadar; son sürüm 9.1.0, 18.06.2026 [12]): ürün bilgisi, satın alma, bekleyen ödeme, 3 gün içinde onay [13], `ITEM_UNAVAILABLE` dahil hatalar | M |
| 5 | Yetki önbelleği: `DeviceStore` AES/GCM; açılışta ve öne gelişte sorgu; iadede kapanır; ağsız çalışır | S–M |
| 6 | TV ödeme ekranı ve Ayarlar > Premium: D-pad, Geri, yükleme/hata/bekleyen/ürün yok; kilit rozeti; oynatmayı kesmez | M |
| 7 | Satın alımı geri yükle | S |
| 8 | Test: lisans test kullanıcıları, kapalı test, test kartları, iade, iki TV, ödeme yöntemi tanımlı olmayan kullanıcı [14] | S + sürekli |
| 9 | Console (ilk satıştan önce): %15 kademe kaydı, ürün, dil başına ad, bölge fiyatları, ülke kısıtları, AEA beyanı | S (sahip) |
| 10 | Yerelleştirme 1–4. adımlar | L |
| 11 | AppCompat/bağlam, dil seçici, `generateLocaleConfig`, varsayılan EN, DE | M |
| 12 | Mağaza varlıkları; EN/DE destek ve iade metinleri | S–M |
| 13 | RTL hazırlığı | M |
| 14 | Fire OS ve Amazon IAP (koşullu) | M–L |

2–8. işler için tahmin 6–9 iş günü (ölçülmedi).

**Ölçüm ve takvim (öneri):**
- Uygulamada analitik yok. Dönüşüm Play Console satış raporları, mağaza girişi deneyleri ve fiyat deneyleriyle izlenir [10].
- Bireysel hesapta 12 test kullanıcısıyla 14 günlük kapalı test gerekiyor (READINESS). Bu test satış takviminin parçasıdır.
- Satın alma akışı bu dönemde lisans test kullanıcılarıyla denenir.

## 8. Vergi ve hukuk kontrol listesi

**Mali müşavir:**
- **20/B:**
  - Kapsam: mobil cihaz uygulamaları ve elektronik uygulama satış platformları. Yalnız TV'de çalışan bir uygulamanın kapsama girip girmediği belirsiz; **özelge** gerekli.
  - 2026 sınırı 5.300.000 TL.
  - Banka hasılattan %15 kesiyor. Bu oranı Cumhurbaşkanı değiştirebilir.
  - İstisna belgesi ve özel hesap şart.
  - Sınır aşılırsa o yılın kazancının tamamı beyan edilir. Banka kesintiye devam eder ve kesinti beyannamedeki vergiden düşülür [37].
- **20/B olmazsa:** Şahıs şirketi mi, limited şirket mi?
- **Hizmet ihracı:** 30.01.2026 tarihli özelgeye göre, Türkiye'de de kullanılan bir uygulamaya KDV istisnası ve kazanç indirimi uygulanmıyor [38].
- **Diğer:** Vergisi geliştiricide kalan ülkeler, İsviçre dahil [8][40]. AEA indirimli KDV ve ABD streaming seçenekleri [25].

**Avukat:**
- Lansman fiyatı ve indirimler: Türkiye'deki 10 gün kuralı [41] ve AB Omnibus.
- AEA Dijital içerik/Hizmet beyanı. Anında ifa edilen dijital içerikte cayma hakkı istisnası ve onay metni.
- "Tek seferlik lisans" kapsam metni.
- AB DSA tacir beyanı ve herkese açık adres. Play'deki beyan ekranına dair Google kaynağı bulunamadı. Benzer örnek olarak Apple, AB'de adres, telefon ve e-postayı yayımlıyor [42].
- Abonelik eklenirse otomatik yenileme:
  - READINESS 6502 m.52/3'e işaret ediyor, ama fıkra numarası resmî metinden doğrulanamadı.
  - Bir hukuk bürosu, onaysız otomatik uzatmanın yapılamayacağını yazıyor [39].
- Kullanım koşulları ve gizlilik politikasına satın alma bölümü eklenmesi.
- NexTv marka çakışmasının ücretli satıştan önce çözülmesi (öneri).

**Yaptırım ortamı (bir risk; pazar seçme gerekçesi değil):**
- Türkiye'de 2026'da kaçak yayın izleyen 1.000+ kullanıcının kimliği tespit edildi.
- İngiltere'de FACT uyarıları, ABD'de ACE celpleri, Fransa'da kullanıcı cezaları var [43].
- Mesaj bu yüzden yasal kaynak üzerine kurulur.

## 9. Açık sorular ve doğrulanamayanlar

- Rakiplerin Play'deki uygulama içi fiyatları; TiviMate'in deneme süresi ve 5 cihaz sınırı. TIVIMATE_GAP'teki "Premium yalnız Companion'dan" bilgisi eskimiş.
- Play'in bölgesel fiyat sınırları; yeni düzendeki program şartı; kayıtsız geliştiricinin oranı.
- Billing Lab ve fiyat deneylerinin TV'de çalışması; Android TV'de uygulama dili ekranı; ödeme penceresinin dili.
- Amazon'un Türkiye'ye elektronik ödeme yapıp yapmadığı; Vega OS'un cihaz kapsamı; Paddle ve Lemon Squeezy'nin Türkiye desteği.
- İndirim yönetmeliğinin Resmî Gazete künyesi; Omnibus; Play'in DSA ekranı; İsviçre'de elektronik hizmet kuralı; 20/B kapsamı.
- Kurlar, efor ve çeviri maliyeti tahmindir.

## 10. Kaynaklar

Google Play ve Android (18.10.2026'da erişildi):
1. Hizmet bedelleri: https://support.google.com/googleplay/android-developer/answer/112622
2. Yeni bedeller (ABD, İngiltere, AEA, Avustralya, Japonya, Kore): https://support.google.com/googleplay/android-developer/answer/16954621 · https://android-developers.googleblog.com/2026/06/play-expanded-billing.html
3. %15 kademe ve hesap grubu: https://support.google.com/googleplay/android-developer/answer/10632485
4. Konumlar ve para birimi: https://support.google.com/googleplay/android-developer/answer/9306917
5. Ödeme zamanı: https://support.google.com/googleplay/android-developer/answer/137997
6. Ödeme profili: https://support.google.com/googleplay/android-developer/answer/7161426
7. Gerekli bilgiler ve adres: https://support.google.com/googleplay/android-developer/answer/13628312
8. Vergi: https://support.google.com/googleplay/android-developer/answer/138000
9. Fiyat ayarları: https://support.google.com/googleplay/android-developer/answer/6334373
10. Fiyat deneyleri (05/2023): https://android-developers.googleblog.com/2023/05/price-experiments-in-play-console.html
11. Tek seferlik ürün teklifleri: https://developer.android.com/google/play/billing/one-time-product-multi-purchase-options-offers
12. Billing Library: https://developer.android.com/google/play/billing/deprecation-faq · https://developer.android.com/google/play/billing/release-notes
13. Entegrasyon: https://developer.android.com/google/play/billing/integrate
14. Test: https://developer.android.com/google/play/billing/test
15. Abonelikler: https://support.google.com/googleplay/android-developer/answer/12154973
16. Aile Kitaplığı: https://support.google.com/googleplay/answer/7007852
17. İade: https://support.google.com/googleplay/answer/15574908
18. Play ülkesi: https://support.google.com/googleplay/answer/7431675
19. Ödemeler politikası: https://support.google.com/googleplay/android-developer/answer/9858738
20. Metadata politikası: https://support.google.com/googleplay/android-developer/answer/9898842
21. Rusya ve Belarus: https://support.google.com/googleplay/android-developer/answer/11950272
22. ABD dış bağlantı programı: https://support.google.com/googleplay/android-developer/answer/16470497
23. Uygulama dilleri: https://developer.android.com/guide/topics/resources/app-languages
24. Çeviri: https://support.google.com/googleplay/android-developer/answer/9844778
25. AEA Dijital içerik/Hizmet, KDV ve streaming seçenekleri: https://support.google.com/googleplay/android-developer/answer/10463498

Rakipler ve piyasa:

26. Play sayfaları (uygulama içi fiyatlar ikinci kontrolde görünmedi): https://play.google.com/store/apps/details?id=ar.tvplayer.tv · https://play.google.com/store/apps/details?id=se.hedekonsult.sparkle · https://play.google.com/store/apps/details?id=com.myiptvonline.implayer · https://play.google.com/store/apps/details?id=com.smarterspro.smartersprotv
27. Sparkle (tarih yok): https://sparkleplayer.com/
28. Plex (19.05.2026 duyuru): https://www.plex.tv/blog/new-lifetime-plex-pass-pricing/ · https://9to5mac.com/2026/05/19/plex-increasing-lifetime-plex-pass-cost-to-whopping-750/
29. [3. taraf] https://www.mydealz.de/deals/tivimate-premium-fur-599-tr-ca-1160eur-uber-google-playstore-turkei-2725588
30. [3. taraf] https://www.pepper.pl/promocje/tivimate-premium-maks-5-urzadzen-licencja-dozywotnia-756653
31. TCMB kur bülteni (16.10.2026, 2026/196): https://www.tcmb.gov.tr/kurlar/today.xml

Amazon:

32. https://developer.amazon.com/apps-and-games/blogs/2025/02/upcoming-changes-to-amazon-appstore-for-android-devices-and-coins-program
33. https://developer.amazon.com/apps-and-games/blogs/2026/07/guide-to-building-for-fire-tv-on-vega-os (13.07.2026) · https://9to5google.com/2025/09/30/amazon-fire-tv-android-vega-os-switch/ · [3. taraf] https://www.aftvnews.com/tag/vega-os/page/2/
34. https://developer.amazon.com/docs/reports-promo/payments-understand.html
35. [3. taraf] https://www.aftvnews.com/?p=45650 · https://www.cordbusters.co.uk/amazon-blocking-dodgy-iptv-all-fire-tv/
36. https://developer.amazon.com/docs/app-submission/distribute-your-app-internationally.html

Vergi ve hukuk:

37. https://fatiharas.com/gvk-madde-mukerrer-20-b-sosyal-icerik-ureticiligi-istisnasi · https://www.forbes.com.tr/ekonomi/2026-yili-gelir-vergisi-tarifesi-ve-istisna-tutarlari-belirlendi
38. https://www.muhasebenews.com/app-store-ve-google-play-gelirlerinin-vergilendirilmesi-2026-mobil-uygulama-abonelik-ve-uygulama-ici-satin-alma-gelirlerinde-kdv-ve-kurumlar-vergisi/
39. https://www.erdem-erdem.av.tr/bilgi-bankasi/abonelik-sozlesmelerinin-sona-erdirilmesi
40. ESTV: https://www.estv.admin.ch/de/mwst-steuerpflicht-auslaendische-unternehmen
41. [haber] https://www.alomaliye.com/2026/09/02/indirimli-satislarda-yeni-donem/
42. Apple DSA (benzer örnek): https://developer.apple.com/help/app-store-connect/manage-compliance-information/manage-european-union-digital-services-act-compliance-information

Yaptırım:

43. https://www.gzt.com/ekonomi/hapis-cezasi-verilebilir-kacak-mac-yayini-izleyenlere-kotu-haber-artik-kullanicilar-da-takibe-aliniyor-4234773 · https://www.broadbandtvnews.com/2025/12/27/fact-warns-illegal-streamers-following-joint-investigation-with-police/ · https://torrentfreak.com/ace-subpoenas-target-iptv-services-piracy-apps-and-streaming-portals-240616/ · https://selectra.info/telecom/actualites/marche/iptv-pirate-amende-abonnes-ligue-1

Depo içi:
- `C:\Users\fyzcb\Desktop\TV\.claude\worktrees\app-ui-ux-audit-a78e98\docs\project\PLAY_STORE_READINESS.md`
- `C:\Users\fyzcb\Desktop\TV\.claude\worktrees\app-ui-ux-audit-a78e98\docs\product\TIVIMATE_GAP.md`
- `C:\Users\fyzcb\Desktop\TV\.claude\worktrees\app-ui-ux-audit-a78e98\docs\product\PRD.md` §12
- `C:\Users\fyzcb\Desktop\TV\.claude\worktrees\app-ui-ux-audit-a78e98\apps\android-tv\app\src\main\java\dev\iptvplayer\tv\` (`data\CatalogParser.kt`, `data\DeviceStore.kt`, `MainActivity.kt`, `ui\`)
- `C:\Users\fyzcb\Desktop\TV\.claude\worktrees\app-ui-ux-audit-a78e98\apps\android-tv\app\build.gradle.kts`
