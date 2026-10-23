# Satış kanalı ve platformlar arası Premium

22 Ekim 2026 · Gereksinim: [D111](../decisions/DECISIONS.md) (kullanıcı). Hukuk ve vergi tavsiyesi değildir; mali müşavir ve avukat görüşünün yerini tutmaz.

**Karar durumu (D112, 22 Ekim 2026):** Kullanıcı üç maddeyi kabul etti:
1. satış yalnız mağazalardan yapılır, kendi site satış yapmaz;
2. Antenio hesabı isteğe bağlıdır;
3. hesap, Premium satışını açan Android sürümüyle aynı sürümde gelir.

Bu belgedeki diğer model, sağlayıcı, sayı ve sıralar **öneridir, karar değildir**. Uygulama planı: [BKL-047 planı](../design/gap/account.md).

**D113 (22 Ekim 2026):** Gelir modeli artık tek seferlik Premium değil. Model şöyle: 7 gün ücretsiz deneme, sonra Google Play aboneliği. Bu belgede "tek seferlik", "lifetime" ve "non-consumable" geçen yerler tarihçedir.

Şunlar geçerliliğini koruyor:
- mağaza, web satışı ve Apple kuralları,
- hesabın platformlar arası taşıma rolü.

D113 ile ilk ücretli sürüm ilk Play yayını oldu. Bu yüzden D112-3 hesabı ilk yayına çeker. Bu açık bir sorudur, bkz. [SUBSCRIPTION](SUBSCRIPTION.md) §1.

Kaynaklar 22.10.2026'da açıldı. Karar verdiren 13 iddia ayrıca bağımsız doğrulayıcılarla birincil kaynaktan yeniden kontrol edildi; düzeltilmiş hâlleri aşağıdadır. **[3. taraf]** işareti haber, forum ya da satıcı dışı siteden gelen, resmî kaynakla doğrulanmamış bilgidir.

Bu belge [SALES_STRATEGY](SALES_STRATEGY.md)'nin hesapsız ve sunucusuz varsayımlarının yerine geçer. Fiyat, dil, pazar ve vergi bölümleri öneri olarak sürer (§5). Yayın engelleri [LAUNCH_AUDIT](../project/LAUNCH_AUDIT.md) içindedir.

## 1. Kısa cevap

- **Satışı mağazalar yapsın.** Kendi siteniz şimdilik satış değil hesap merkezi olsun. Üç gerekçe var:
  - **Ödeme yolu yok.** Türkiye'den web satışı için kullanılabilir bir ödeme yolu bulunamadı. Stripe ve PayPal Türkiye'deki satıcıya kapalı. Satışı kendi adına yapıp vergiyi üstlenen sağlayıcıların (Merchant of Record, MoR) üçü de, Paddle, Lemon Squeezy ve FastSpring, "IPTV"yi yazılı olarak satılamayacak iş türleri arasında sayıyor (§2.3).
  - **Uygulama web satışından söz edemez.** Play'de satış yapan uygulama, Türkiye'deki kullanıcıya web satışına bağlantı veremez, onu öneremez. Android TV için tek istisna, işletme kaydı isteyen AEA programları (§2.1). Web satışı ancak uygulama dışındaki tanıtımla bulunur.
  - **Yükü mağaza taşır.** Vergiyi ve ödeme güvenliğini mağaza üstlenir; Türkiye'de KDV'yi Google ve Apple öder. Play'de ilk 48 saatten sonraki iade talepleri size gelir ([SALES_STRATEGY](SALES_STRATEGY.md) [17]).
- **İkisini birden yapmanın yolu tek Antenio hesabıdır.**
  - Satın alma hangi kanaldan yapılırsa yapılsın hesaba yazılır. Hesapla giriş yapılan her platformda Premium açılır.
  - Web satışı sonra eklenirse o alım da aynı hesaba "web" kaynağıyla yazılır.
  - Uygulamalar web alımını yalnız girişle tanır, web satışından hiç söz etmez.
- **Apple şartı karşılanabilir.**
  - Apple 3.1.3(b), başka platformda ya da sitede alınan özelliği açmaya izin veriyor. Şartı, aynı Premium'un Apple uygulamasında da uygulama içi satın alma (IAP) olarak satılması.
  - Play'de alan kullanıcı Apple TV'de Antenio hesabıyla giriş yapınca yeniden ödemez. Apple'da alan da Android TV'de yeniden ödemez.
- **Bunun bedeli:**
  - isteğe bağlı bir Antenio hesabı,
  - TV'de QR ve kodla giriş,
  - sunucu tarafında bir hak kaydı.

  "Antenio hesabı yok, sunucu yok" vaadi Premium için biter.
- **Zamanlama önerisi:** Hesap, Premium satışıyla aynı Android sürümünde gelsin. Böylece her alıcı satın aldığı anda hesabını bağlayabilir ve "Apple'a geçtim, satın alımımı taşıyamıyorum" vakası büyük ölçüde önlenir. "Sonra" deyip Android cihazı kalmayan alıcı yine destek gerektirir (§3.8).

## 2. Kurallar (doğrulanmış, 22.10.2026)

### 2.1 Google Play

- **Başka yerde alınanı açmak:**
  - Ödemeler politikasında bunu yasaklayan bir hüküm yok [G1][G2].
  - SSS, hiç satış yapmayan "yalnız tüketim" uygulamalarını açıkça onaylıyor: kullanıcı giriş yapar ve başka yerde ödediğine erişir [G2].
  - Hem Play'de satan hem de dışarıdaki alımı tanıyan karma uygulama için açık bir cümle yok. Google'ın geliştirici belgesi sunucu entegrasyonunun gerekçeleri arasında "platformlar arası hakları" sayıyor [G3]. TiviMate gibi olası emsaller var (§2.2). Risk düşük görünüyor, ama yazılı bir onay yok.
- **Yönlendirme yasağı:**
  - Play'de satış yapan uygulama, Türkiye'deki kullanıcıya uygulamada ve mağaza sayfasında web satışına bağlantı, düğme, web görünümü ya da "sitemizden alın" gibi teşvik veremez.
  - Yasak hesap oluşturma ve kayıt akışlarını da kapsıyor.
  - "Bağlantısız bilgi verme" izni yalnız hiç satış yapmayan uygulamaya tanınıyor [G1][G2].
- **Fiyat eşitliği** şart değil. Uygulama dışında, örneğin e-postayla, başka bir fiyat duyurulabilir [G2]. Türkiye'de bunun için ticari ileti onayı ve İYS kaydı gerekir (6563 sayılı Kanun; avukata).
- **Alternatif programlar:**
  - ABD'nin dış bağlantı ve alternatif faturalama programları ile Birleşik Krallık, Avustralya ve Japonya'nın "billing choice" programı yalnız telefon ve tablet içindir. Android TV bunlara giremez [G4][G5][G6].
  - AEA programları (external offers ve alternatif faturalama) Android TV dahil bütün form faktörlerini kapsıyor. Ancak işletme kaydı istiyor [G7][G8].
  - **Türkiye hiçbir programda yok.**
  - Google, yeni düzeni dünyanın geri kalanına 30.09.2027'ye kadar yayacağını duyurdu [G9]. Ancak bugünkü billing choice programı yalnız telefon ve tableti kapsıyor; yayılımın Android TV'yi kapsayacağı belli değil.
  - Rekabet Kurulu, Play faturalandırma zorunluluğu ve yönlendirme yasağı hakkında 07.08.2025'te soruşturma açtı (karar no. 25-29/680-M); sonuç bulunamadı [G10]. Kural lehte değişebilir.
- **Ücret:** Ücreti alıcının bulunduğu yer belirler.
  - Türkiye'deki alıcıda eski düzen geçerli: %15 kademesine kayıtla ilk 1 M$ için %15, üstü %30.
  - ABD, Birleşik Krallık, AEA, Avustralya ve Japonya'daki alıcıda 2026 düzeni geçerli: ilk 1 M$ için %10 hizmet bedeli, Play faturalandırma kullanılırsa üstüne %5 [G11][G12].
  - 2026 düzeninin TV'ye nasıl uygulandığı birincil kaynakta açık değil.
- **Hesap silme:**
  - Uygulamada hesap açılabiliyorsa ya da uygulama kullanıcıyı dışarıdaki bir hesap oluşturma akışına (ör. QR ile telefonda kayıt) yönlendiriyorsa silme yolu zorunlu.
  - **Android TV'de web üzerinden silme bağlantısı yeterli**; bu bağlantı Data safety formuna girilir.
  - Telefon ve tablet sürümünde uygulama içi yol da gerekir [G13].
- **Data safety** [G14]:
  - Hesap sunucuda tutuluyorsa e-posta ve kullanıcı kimliği beyan edilir.
  - Satın alma geçmişi, uygulama, sunucu ya da bir SDK onu topluyorsa beyan edilir.
  - SDK'ların cihaz dışına gönderdiği veriler de beyan edilir.
- **Sürümler:**
  - Play Billing Library 31.08.2026'dan beri en az 8 olmalı. PBL 8, 31.08.2027'den sonra kabul edilmeyecek; yeni entegrasyon 9 ile başlamalı (son sürüm 9.1.0) [G15].
  - TV için hedef API alt sınırı 34; uygulama 36'yı hedefliyor [G16].
- **Tek seferlik ürün** [G17][G18][G19]:
  - Satın alma PURCHASED olduktan sonra 3 gün içinde onaylanmazsa otomatik iade edilir. Bekleyen (PENDING) ödeme desteklenmeli.
  - `obfuscatedAccountId` en çok 64 karakterdir ve açık kişisel veri içeremez.
  - Sunucu doğrulaması için güncel uç nokta `purchases.productsv2.getproductpurchasev2`.
  - Voided Purchases API yalnız son 30 günü gösterir.
  - Tek seferlik ürün bildirimleri (RTDN), Console'da "tüm bildirimler" seçilince gelir.
  - Console'dan iade yapılırken "revoke" seçilmezse alım iptal listesinde görünmez.
  - Ters ibraz incelemesi (PendingRefundReviewNotification) 24 saat içinde yanıt bekler.

### 2.2 Apple (Review Guidelines, 16.10.2026)

- **3.1.3(b) Multiplatform:**
  - Başka platformda ya da sitede alınan özelliği açmak serbest. Şartı, aynı öğenin uygulamada IAP olarak da satılması.
  - Uygulama içinden başka ödeme yoluna yönlendirme yasak; ABD vitrini istisna [A1].
- **3.1.1:**
  - Kilit IAP ile açılır. Lisans anahtarı, QR kod gibi kendi mekanizmalarla kilit açmak yasak. Bu yüzden TV ekranında "aktivasyon kodu" değil "hesap girişi" denir.
  - Geri yüklenebilir IAP için bir geri yükleme (Restore) mekanizması olmalı [A1].
- **3.1.3(a) Reader:** Dergi, gazete, kitap, ses, müzik ve video içeriği içindir. Antenio içerik değil oynatıcı işlevi sattığı için büyük olasılıkla bu kapsama girmez. Bu Apple'ın sözü değil, bizim yorumumuz.
- **Dış bağlantı:**
  - ABD vitrininde ayrı izin gerekmeden serbest. Dava Yüksek Mahkeme'de sürüyor; bağlantılı alımlara makul bir komisyon ilk derece mahkemesinde belirlenebilir [A2][A3, 3. taraf].
  - AB ve bazı ülkelerde ayrı izin (entitlement) ile mümkün.
  - **Türkiye vitrininde yasak** [A4].
- **4.8:** Birincil hesap için Google girişi sunulursa, gizliliği koruyan eşdeğer bir giriş de zorunlu. Sign in with Apple bu şartı karşılar [A1].
- **5.1.1(v):**
  - Hesaba bağlı önemli bir özellik yoksa uygulama girişsiz de çalışmalı.
  - Hesap açılabiliyorsa silme uygulama içinden başlatılmalı. Silme web'de tamamlanabilir, ama uygulama doğrudan o sayfaya bağlanmalı [A1][A5].
- **Universal Purchase:**
  - iOS ve tvOS tek kayıt ve aynı bundle ID ile satılır.
  - Ayrı açılmış kayıtlar sonradan birleştirilemez. Karar ilk kayıttan önce verilmeli [A6].
- **Family Sharing:** Ürün bazında açılır; bir kez açılınca geliştirici kapatamaz. Açılırsa aile üyesinin hakkı hesap üzerinden Android'e de taşınır. Öneri: kapalı [A7].
- **Sunucu:**
  - App Store Server API ve Notifications V2 kullanılır; V1 kullanımdan kalktı.
  - İlgili bildirimler `ONE_TIME_CHARGE`, `REFUND`, `REFUND_REVERSED` ve `REVOKE`.
  - `appAccountToken`, geliştiricinin ürettiği UUID'dir. Set App Account Token ile sonradan bağlanabilir [A8].
- **Ücret ve vergi:**
  - Geliştirici programı yıllık 99 USD. TL tutarı kayıt ekranında görünür; forumlarda 1.029 TL yazıyor [3. taraf].
  - Small Business Program %15. Başvuru gerekir. İndirim, başvurunun onaylandığı Apple mali ayının bitiminden 15 gün sonra başlar ve geriye işlemez [A9].
  - Türkiye satışlarında Apple Distribution International komisyoncu olarak işlem yapar ve KDV'yi Apple öder.
  - Dijital hizmet vergisi 2026'da %5'tir ve gelirden düşülür [A10][A11].
- **IPTV emsali:**
  - IPTVX içeriksiz oynatıcı olarak App Store'da IAP ile satılıyor [C1]. UHF, Purple Playlist Player ve iPlayTV için de ilk araştırmada aynısı görüldü; ikinci kontrol yapılmadı [C5][C6][C7].
  - IPTVX'in kendi hesabı var, ama Apple'da alınan Android'i, Google Play'de alınan Apple'ı açmıyor. Play aboneliği ise yandan yüklenmiş Fire TV sürümünde aynı hesapla çalışıyor [C1].
  - TiviMate Premium'u tivimate.com hesabına bağlı; fatura Google Play ya da Huawei AppGallery'den, Play olmayan cihazlar Companion uygulamasıyla açılıyor [C4].
  - Apple ile Google Play arasında satın alma taşıyan, doğrulanmış bir IPTV oynatıcı emsali bulunamadı.

### 2.3 Web satışı: Türkiye'deki satıcı

| Sağlayıcı | Türkiye'deki satıcı | IPTV politikası | Sonuç |
|---|---|---|---|
| Stripe | Desteklenen 51 ülke arasında yok [W1] | — | Kullanılamaz; ancak yurt dışında şirketle |
| PayPal | Haziran 2016'dan beri Türkiye'de kapalı [W2] | — | Kullanılamaz |
| Paddle (MoR, %5 + 50¢) | Desteklenmeyen ülkeler arasında yok, ama açıkça "destekleniyor" da denmiyor | "IPTV" yasaklı (13.04.2026) [W3] | Ret riski yüksek |
| Lemon Squeezy (MoR) | Banka ödemesi var; kullanıcılarını Stripe Managed Payments'a taşıyor, orada Türkiye yok [W4][W5] | "IPTV services" yasaklı [W6] | Ret riski yüksek, geleceği belirsiz |
| FastSpring (MoR) | Kısıtlı ülkeler arasında yok | "IPTV", satış yapamayan "Restricted Business Types" listesinde [W7] | Ret riski yüksek |
| iyzico | Sanal POS için şahıs şirketi yeterli; döviz TL'ye çevrilir [W8] | Politika bulunamadı | MoR değil: KDV, AB OSS ve e-Arşiv sizde |
| PayTR, Param, Sipay | Doğrulanmadı | Doğrulanmadı | — |

- **e-Arşiv:** 2026'da fatura tutarına bakılmaksızın düzenlenir. Basit usul ve işletme hesabı esasındaki mükelleflerde 3.000 TL eşiği 2026 sonuna kadar geçerli, 01.01.2027'de kalkar. İnternetten satışta brüt hasılat 500 bin TL'yi geçerse e-Fatura ve e-Arşiv zorunlu [W9].
- **AB'deki tüketiciye satış:** AB dışında yerleşik satıcı non-Union OSS ile beyan verir [W10].
- **RevenueCat Billing:** Stripe hesabı istiyor, bu yüzden Türkiye'den doğrudan kullanılamaz. Web Purchase Links Paddle ile de çalışıyor, ama Paddle'ın IPTV yasağı geçerli [R1][R2].
- **Kart ağları:** Telif ihlali içeren işlemleri yasaklıyor [3. taraf]. Bir oynatıcının bu sınıfa sokulma riski ödeme kuruluşunun takdirinde.
- **Rakiplerin web modeli:** Smart IPTV ve Net IPTV web'de cihaz başına MAC aktivasyonu satıyor [C2][C3]. SET IPTV için yalnız üçüncü taraf kaynak var [3. taraf]. Bu model Apple 3.1.1 ile çelişiyor. "Aktivasyon kodu" dili de [Play hazırlığı](../project/PLAY_STORE_READINESS.md) §3.2'de korsan algısı riski olarak işaretli.

### 2.4 LG ve Samsung

- **LG:**
  - LG kendi uygulama içi ödeme hizmetini artık sunmuyor. Paymentwall'u öneriyor; başka bir sağlayıcı için ayrı sözleşme isteyebilir [L1].
  - Seller Lounge'da "webOS Pay" seçeneği görüldüğü yazılmış, ama resmî bir belgesi yok [L2, 3. taraf].
  - Bireysel satıcı hesabı mümkün.
- **Samsung:**
  - Samsung Checkout'ta Türkiye "Agent" modeliyle listeleniyor; bu terimin anlamı tanımlanmamış [L3].
  - Bireysel hesap Public Seller olarak başlar ve yalnız ABD'de yayınlayabilir.
  - Türkiye'de yayın için Partner Seller olmak, bunun için de çevrimdışı bir sözleşme gerekiyor. Bireysel geliştiriciye verilip verilmediği yazmıyor [L4].
- **RevenueCat:** webOS ve Tizen desteği bulunamadı [R3].

## 3. Önerilen model

### 3.1 İlkeler

1. **Mağaza alımı kendi mağazasında her zaman açılır.** Kaynak mağazanın kendi kaydıdır: Play'de satın alma sorgusu, Apple'da StoreKit. Antenio sunucusu ya da hak servisi kapansa bile bu değişmez.
2. **Antenio hesabı yalnız platformlar arası taşımayı ekler.** Ücretsiz kullanım ve aynı mağazada geri yükleme için hesap gerekmez.
3. **Uygulamada satış yönlendirmesi yok.** Fiyat karşılaştırması, web satışı ya da "başka yerden al" çağrısı hiçbir zaman olmaz. Tek cümle yeter: "Premium başka bir cihazında varsa hesabınla giriş yap." Bu cümle ilk sürümden önce politika açısından gözden geçirilir.
4. **Sunucuya kaynak verisi gitmez.** IPTV adresi, kullanıcı adı ve parola, liste, favori ve izleme geçmişi cihazda kalır.

### 3.2 Platformlar

| Platform | Dağıtım | Satış | Başka yerde alınan Premium | Aşama |
|---|---|---|---|---|
| Google Play: Android TV / Google TV | Ücretsiz ilk yayın, hesapsız | Play faturalandırma, tek bir tüketilmeyen ürün | Hesapla giriş yapınca açılır | F0, F2 |
| Google Play: telefon / tablet | Aynı paket olabilir. Ama manifestte `android.software.leanback` zorunlu (`AndroidManifest.xml:5`); telefon için bu değişmeli ve dokunmatik arayüz gerekir | Aynı ürün | Aynı; ayrıca uygulama içi silme yolu | F4 |
| Apple: tvOS + iOS/iPadOS | Tek kayıt, Universal Purchase | Aynı Premium StoreKit IAP olarak; Restore; Small Business Program | Hesapla giriş, Sign in with Apple | F3 |
| Kendi web sitesi | Önce gizlilik, koşullar, destek ve telif bildirimi. Sonra hesap merkezi: giriş onayı, cihazlar, silme | Yok (W koşullu) | — | F0, F2 |
| LG webOS, Samsung Tizen | Yalnız girişli uygulama; önce mağazalara yazılı soru | Yok. Premium Play, Apple ya da W'den gelir | Hesapla giriş | F5 |
| Fire TV | SALES_STRATEGY §4'teki koşullu plan | Amazon IAP | Hesaba bağlanabilir | Sonra |

### 3.3 Hesap

**Kademeler:**
- **Ücretsiz:** hesap yok.
- **Mağazadan Premium:** hesap gerekmez; aynı mağazada geri yüklenir.
- **Antenio hesabı:** Premium'u başka bir ekosisteme taşımak, mağaza satışı olmayan platformlar (LG, Samsung) ve cihaz yönetimi için.

**Satın alma akışı:**
- Satın almanın önünde hesap engeli olmaz.
- Satın almadan sonra bir kez şu öneri çıkar: "Premium'u diğer cihazlarında da kullanmak için hesabına bağla: Şimdi / Sonra." Fiyattan ya da başka bir kanaldan söz etmez.

**Giriş yöntemleri:**
- Google ve Sign in with Apple. Apple platformlarında Google girişi sunulursa, 4.8 gereği gizliliği koruyan eşdeğer bir giriş de olmalı; Sign in with Apple bu şartı karşılar.
- Antenio parola saklamaz.
- E-postaya tek kullanımlık kod ikinci adımda eklenebilir (karar sizin).

**TV'de giriş:**
- RFC 8628 cihaz akışı kullanılır. TV bir QR (`verification_uri_complete`) ve kısa bir kod gösterir; kullanıcı telefonda onaylar, TV bu sırada sunucuyu sorgular.
- Neden bu akış: Uygulamanın minSdk'sı 26. Android TV'de Credential Manager ise Android 14 ve 2026W30 Play hizmetleri istiyor; passkey de TV'de oluşturulamıyor [I1]. Cihaz akışı tvOS, webOS ve Tizen'de de aynı çalışır.
- tvOS'ta ek olarak yerel Sign in with Apple düğmesi bulunur.

**Kimlik sağlayıcı:**
- Auth0 cihaz akışını hazır destekliyor. Ayarları: uygulama türü Native, token uç noktası kimlik doğrulaması "None", akış uygulama başına açılır [I2]. Bu akışta Auth0'ın geliştirici anahtarlarıyla çalışan sosyal bağlantılar ve hesap bağlama kısıtlı. Google ve Apple için kendi istemci anahtarlarınız gerekir; hesap birleştirme prototipte denenmeli.
- Keycloak da destekliyor ve kendiniz barındırırsınız [I3].
- Firebase, Supabase ve Cognito'da yerleşik destek yok; özel eşleştirme yazmak gerekir [I4][I5].
- Clerk'te destek var, ama beta durumu belirsiz [I6].
- Auth0'ın fiyatı, IPTV oynatıcıyı kabul edip etmediği ve onay sayfasında cihaz adı gösterip gösterememesi sorulmalı.

**Bağlama kuralları:**
- Bir mağaza alımı tek bir hesaba bağlanır. Başka hesaba taşıma destek üzerinden yapılır.
- Apple'ın gizli e-postası ile Google hesabı iki ayrı hesap oluşturabilir. Bunun için hesap merkezinde "giriş yöntemi ekle" seçeneği olur.

**Sunucuda tutulanlar:**
- e-posta (Apple'ın gizli e-postası olabilir) ve giriş sağlayıcısındaki kimlik,
- rastgele hesap UUID'si,
- satın alma referansları: Play `purchaseToken` ve `orderId`, Apple `originalTransactionId`,
- cihaz listesi: rastgele kimlik, ad, platform, son görülme.

**Silme:**
- Android TV'de web üzerinden silme bağlantısı; telefonda ve Apple'da uygulama içinden.
- Silme yalnız platformlar arası bağı kaldırır; mağaza alımı kendi mağazasında açık kalır (ilke 1).
- Apple'ın rehberi silme sırasında Sign in with Apple belirteçlerinin iptal edilmesini istiyor görünüyor; bu doğrulanmalı.

### 3.4 Hak altyapısı

| | A. Yönetilen: RevenueCat (öneri) | B. Kendi küçük servisiniz |
|---|---|---|
| İş | Play ve App Store doğrulaması, onay, bekleyen ödeme, RTDN ve Notifications V2 onda. App User ID olarak hesap UUID'si kullanılır [R4]. | Bağlama uç noktası (Play token için `productsv2`, Apple için imzalı işlem doğrulaması), iptaller (RTDN ve Apple REFUND/REVOKE), hak uç noktası, silme. Abonelik olmadığından yenileme mantığı yok. |
| Ücret | Aylık takip edilen gelir 2.500 USD'nin altındaysa ücretsiz. Eşiğe ulaşılan ayda o ayın toplam gelirinin %1'i [R5]. Adapty 5.000 USD'ye kadar ücretsiz, sonra %1; Qonversion 7.000 USD'ye kadar ücretsiz, sonra %0,8. | Barındırma |
| Android | Tek seferlik ürün için SDK 7.11.0 veya üstü ve panelde "non-consumable" işareti; yoksa alım tüketilir [R6] | PBL 9 doğrudan |
| Risk | IPTV oynatıcıyı kabul ettiği yazılı sorulmalı. Veri yurt dışına aktarılır (KVKK). webOS/Tizen SDK'sı yok. | Bekleyen ödeme, 3 günlük onay ve 24 saatlik ters ibraz gibi kenar durumlar sizde |

**Öneri:**
- A ile başlanır.
- Hesap ile satın alma arasındaki eşleme, RevenueCat bildirimlerinden kendi tablonuza da yazılır. Bu tablo tedarikçi değişikliğini ve LG/Samsung için hak uç noktasını kolaylaştırır.
- **Onay tek bir yoldan yapılır** (RevenueCat). İki ayrı onay yolu yarış koşulu yaratır.

**Geri yükleme:**
- Hesapsız alıcının geri yüklemesi RevenueCat ayarına değil ilke 1'e dayanır.
- RevenueCat'te alımın asıl hesapta kalması seçilirse, hesaba bağlı bir alım hesapsız bir cihazdaki geri yüklemeyle başka kimliğe kaymaz.
- Uygulamanın Play kaydını doğrudan mı, RevenueCat'in yerel önbelleğiyle mi okuyacağı prototipte seçilir. Sandbox'ta iki senaryo denenir:
  - hesaba bağlı alım + yeni bir TV'de girişsiz geri yükleme,
  - hesap silme sonrası aynı mağazada geri yükleme.

**İstemci:**
- `data/Features.kt` içindeki `Entitlements` tek kapı olarak kalır. `AllFree` yerine şu kural gelir: mağaza kaydı **ya da** hesaptan gelen imzalı hak.
- Hak önbelleği `DeviceStore` içinde ayrı bir anahtarla ve ayrı bir `DeviceKeys` girdisiyle tutulur. Gerekçe:
  - "PIN'i unuttum → Her şeyi sıfırla" (`startOver()`, `DeviceStore.kt:144`) tercihleri temizliyor.
  - `resetUnreadableSources()` (`:151`) kaynak anahtarını siliyor.
  - Bu ikisi Premium'u silmemeli.

**Ad:** Kod "Plus" (`GateResult.NeedsPlus`), belgeler "Premium" diyor. Ücretli paketin adı kararıyla tek ad seçilir (uygulama adı Antenio, D115).

### 3.5 Cihaz sınırı ve kötüye kullanım

Bu bölümdeki sayılar öneridir.
- Hesap üzerinden açılan Premium en çok 5 kayıtlı cihazda çalışır. Mağazanın kendi geri yüklemesi bu sayıya dahil değildir.
- Cihaz TV'den ya da hesap merkezinden çıkarılabilir.
- Çevrimdışı hak 30 gün geçerlidir. Saat geri alınarak aşılabilir; bu kabul edilen bir risktir, DRM'e yatırım yapılmaz.
- Sınır ilk satıştan önce kullanım koşullarına yazılır. Sonradan konan sınır tepki çeker.
- **Bölgesel fiyat:** Türkiye fiyatı düşük. Hesap sayesinde Türkiye Play'inden alınan Premium Apple'da da açılacağı için arbitraj etkisi büyür. Bu, [SALES_STRATEGY §3](SALES_STRATEGY.md)'teki TL bandı tartışmasına eklenmeli.

### 3.6 İade ve hak geri alma

- **Play:**
  - Hak yalnız PURCHASED durumunda verilir; onay 3 gün içinde yapılır.
  - Hak geri alma bilgisi RTDN'de `VoidedPurchaseNotification` ile gelir; bu bildirim iki Console seçeneğinde de gelir. `ONE_TIME_PRODUCT_CANCELED` bekleyen bir alımın iptalidir, hak geri almaz. "Tüm bildirimler" seçeneği, alımı hesaba bağlamakta kullanılan `ONE_TIME_PRODUCT_PURCHASED` için gerekir.
  - Console'dan iade yaparken "revoke" seçilir.
  - Ters ibrazda 24 saat içinde yanıt gerekir. Tek kişilik destek için bir uyarı ve yedek plan yazılmalı.
- **Apple:** `REFUND` ve `REVOKE` hakkı kapatır, `REFUND_REVERSED` yeniden açar.
- **Hesapla açılan cihazlar** bir sonraki yenilemede ya da çevrimdışı süre dolunca kapanır.
- **İade metni** Apple'da Apple sürecine, Play'de ilk 48 saatte Google'a, sonra destek adresinize yönlendirir.

### 3.7 Gizlilik, Data safety ve KVKK

- **İlk ücretsiz yayın** (gerçekte doğru olduğu sürece):
  - "Hesap gerekmez. Reklam, analitik ve takip yok. Kaynak bilgileri ve katalog cihazda şifreli."
  - "Hiçbir zaman sunucu yok" gibi geleceğe uzanan bir vaat verilmez; henüz olmayan hesap özelliği de vaat edilmez.
  - Favori ve izleme geçmişinin şifreli olduğu yazılmaz, çünkü değiller ([LAUNCH_AUDIT](../project/LAUNCH_AUDIT.md) E4).
- **Premium ve hesap sürümü:**
  - Data safety'ye e-posta, kullanıcı kimliği ve satın alma geçmişi eklenir. RevenueCat ve kimlik sağlayıcının topladıkları (cihaz modeli, IP vb.) onların rehberinden çıkarılır.
  - Gizlilik politikası ikinci sürümüne geçer.
  - Kullanım koşulları şunları yazar: lisansın kapsamı, cihaz sınırı, platformlar arası erişimin süresi ("hizmet sürdükçe" gibi; avukata).
- **KVKK:**
  - aydınlatma metni,
  - yurt dışına aktarım: RevenueCat (ABD), kimlik sağlayıcı, barındırma (AB de KVKK açısından yurt dışıdır),
  - standart sözleşme ve Kurum'a bildirim,
  - veri işleme sözleşmeleri ve saklama süreleri.
- **GDPR:** AB'deki kullanıcılar için 27. madde temsilcisi gerekip gerekmediği avukata sorulur.
- **Mağaza metni:** [STORE_LISTING_EN](STORE_LISTING_EN.md)'deki hesap ve sunucu cümlesi buna göre güncellendi.

### 3.8 Aşamalar

| Aşama | İçerik |
|---|---|
| **F0, şimdi** | Yayın engelleri ([LAUNCH_AUDIT](../project/LAUNCH_AUDIT.md)). Ücretsiz/Premium sınırı ilk ücretsiz yayından önce yazılır; ilk günden ücretsiz verilen özellik sonra ücretliye alınmaz. Gizlilik metninin ilk sürümü. Ücretli paketin adı (Premium mi Plus mı), marka, alan adı ve statik site. |
| **F1, Premium özellikleri geliştirilirken** | Yazılı sorular: RevenueCat ve Auth0'a IPTV oynatıcı kabulü; LG Seller Lounge; Samsung Partner. Mali müşavir: 20/B, şahıs şirketi, giderler (20/B'de gider düşülmez). Avukat: koşullar, KVKK. Play'de %15 kademesine kayıt ve RTDN. Prototip: cihaz akışı ve RevenueCat sandbox ile geri yükleme senaryoları. |
| **F2, Play'de Premium ve hesap, aynı sürümde (D112)** | RevenueCat üzerinden PBL, tek ürün, Ayarlar > Hesap ve Premium, QR ile giriş, web silme sayfası, güncel Data safety ve gizlilik metni, kademeli yayın. |
| **F3, Apple** | Ayrı uygulama: Swift/SwiftUI mı, paylaşılan kod mu ayrı bir karar ve büyük bir iş. Small Business Program başvurusu ilk satıştan haftalar önce. IAP, Restore, Sign in with Apple, uygulama içi silme ve Notifications V2. İnceleme notunda 3.1.3(b) açıklaması, demo hesap ve yasal bir test kaynağı. **Kabul testleri:** Play'de al → Apple TV'de aç; Apple'da al → Android TV'de aç; iade iki tarafta da kapatır; hesap silme yalnız bağı kaldırır. |
| **F4, Android telefon ve tablet** | Manifest ve dokunmatik arayüz. Telefon uygulaması, LG ve Samsung kullanıcıları için satın alma köprüsü olur. |
| **F5, LG, sonra Samsung** | Yazılı yanıtlardan sonra; uygulamalar yalnız girişli. |
| **W, web satışı (koşullu)** | Ödeme kuruluşunun yazılı onayı; şahıs şirketi; e-Arşiv ve OSS; mesafeli satış ve cayma metni. Uygulamaların açtığı sayfalardan satışa giden bir yol olmamalı: ayrı alt alan adı, mağaza sayfasındaki "website" alanı satışa götürmemeli. |

**F2'yi hesapsız açma seçeneği (D112 ile seçilmedi; kayıt için duruyor):** Gelir daha erken başlar. Bedeli şu: hesap gelmeden Premium alan kullanıcı, alımı Apple'a taşımak için önce bir Android cihazdan aynı Google hesabıyla giriş yapmak zorunda kalır. Android cihazı kalmamış kullanıcı yalnız destek üzerinden, Google sipariş numarasıyla bağlanabilir. Bu yolda sahipliği doğrulamak zayıftır; ekran görüntüsüyle dolandırıcılık yapılabilir.

### 3.9 Riskler

- **Google'ın karma model için yazılı bir onayı yok.** Risk düşük görünüyor; politika değişiklikleri izlenmeli.
- **Tedarikçiler IPTV oynatıcıyı kabul etmeyebilir** (RevenueCat, Auth0). Reddederlerse B seçeneğine geçilir.
- **Türkiye kuralları lehte değişebilir:** Rekabet Kurulu soruşturması ve Google'ın 2027 yayılımı. Yayılımın Android TV'yi kapsayacağı ise belli değil.
- **Apple incelemesi:** Yönergelerde IPTV'ye özel madde yok; içeriksiz oynatıcıya nasıl bakılacağı incelemeye bağlı. İncelemede yasal bir test kaynağı verilmeli.
- **Tek kişilik destek yükü:** 24 saatlik ters ibraz yanıtı, hesap kurtarma, cihaz sıfırlama ve hesap birleştirme.
- **Marka:** NexTv çakışması hesap altyapısının adını da etkiler: Google OAuth onay ekranı, Apple Services ID, alan adı. Ad kararı hesap altyapısından önce verilmeli.
- **Net gelir:** 20/B'de gider düşülmez. RevenueCat, Auth0, barındırma ve Apple ücreti net geliri azaltır.

### 3.10 Sahibin kararları

1. ~~Satış kanalı: yalnız mağaza mı?~~ **D112: yalnız mağaza.** Web satışı ancak yeni bir kararla açılır (W koşulları §3.8).
2. ~~Hesap Premium satışıyla aynı sürümde mi, isteğe bağlı mı?~~ **D112: aynı sürümde, isteğe bağlı.**
3. Giriş yöntemleri: Google ve Apple (öneri); e-posta kodu da olsun mu?
4. Hak altyapısı: RevenueCat (öneri) mi, kendi servis mi? Kimlik sağlayıcı: Auth0 (öneri) mi, Keycloak mı? Veri bölgesi neresi?
5. Cihaz sınırı ve çevrimdışı süre.
6. Ücretli paketin adı (Premium mi Plus mı). SALES_STRATEGY ile gap planı arasındaki fiyat ve ücretsiz sınır farkları.
7. Yasal yapı: bireysel ve 20/B mi, şahıs şirketi mi? Web satışı ve AEA programları işletme ister.
8. Apple'ın zamanı, Universal Purchase ve Family Sharing (öneri: kapalı).
9. LG ve Samsung modeli.
10. Platformlar arası erişimin kullanım koşullarında yazılacak süresi.

## 4. Doğrulanamayanlar

- Google'ın karma model için yazılı onayı. "Premium başka bir cihazında varsa giriş yap" cümlesinin politikaya uygunluğu. 2026 Google ücret düzeninin Android TV'ye uygulanışı.
- RevenueCat ve Auth0'ın IPTV oynatıcıyı kabul edip etmediği. RevenueCat bildirim ve REST API'sinin plan kapsamı. Auth0'ın fiyatı ve onay sayfasında cihaz adı.
- Paddle'ın Türkiye'deki satıcıyı açıkça kabul etmesi. PayTR, Param ve Sipay koşulları. Paymentwall'un IPTV tutumu.
- LG "webOS Pay". Samsung Partner'ın bireysel geliştiriciye açık olup olmadığı ve "Agent" modelinin vergi anlamı.
- Apple: Sign in with Apple belirteç iptali şartı, ABD'deki bağlantı komisyonunun sonucu, Rekabet Kurumu'nun Apple soruşturmasının sonucu [3. taraf].
- Dijital hizmet vergisinin 2027'de %2,5 olacağı [3. taraf].

## 5. Önceki önerilerle ilişki

- **[SALES_STRATEGY](SALES_STRATEGY.md):** Şu ifadeler bu belgeyle güncellenir:
  - "sunucusuz geri yükleme": yalnız aynı mağaza içinde geçerli kalır (ilke 1); başka platforma taşıma hesap ve sunucu ister,
  - "sunucusuz yapıda cihaz sınırı konamaz",
  - "lisans anahtarı 'sunucu yok' ilkesini bozar",
  - "Antenio hesabı yok, sunucumuz yok",
  - "TV için uygun program görünmüyor". AEA programları Android TV'yi kapsıyor, ama işletme kaydı istiyor.

  Fiyat, dil, pazar ve vergi bölümleri öneri olarak sürer.
- **[gap PLAN](../design/gap/PLAN.md) ve [sales_draft](../design/gap/sales_draft.md) (S35–S37):** "Hak cihazda, sunucu yok" varsayımı da geçersizdir; iki belgenin başına not düşüldü. S35–S37 hesap ve hak işleriyle genişler.
- **TiviMate:** Premium artık tivimate.com hesabına bağlı; Play'de doğrudan TV uygulamasında da satılıyor. "Google hesabı başına 5 cihaz" bilgisi birincil kaynakta bulunamadı [3. taraf] [C4].

## 6. Kaynaklar

Google Play (22.10.2026'da açıldı):
- G1 Ödemeler politikası: https://support.google.com/googleplay/android-developer/answer/9858738
- G2 Ödemeler SSS: https://support.google.com/googleplay/android-developer/answer/10281818
- G3 Play Billing genel bakış: https://developer.android.com/google/play/billing
- G4 ABD değişiklikleri: https://support.google.com/googleplay/android-developer/answer/15582165
- G5 ABD dış içerik bağlantıları: https://support.google.com/googleplay/android-developer/answer/16470497
- G6 Billing choice: https://support.google.com/googleplay/android-developer/answer/17161464
- G7 AEA external offers: https://support.google.com/googleplay/android-developer/answer/14372887
- G8 AEA alternatif faturalama ve user choice billing: https://support.google.com/googleplay/android-developer/answer/12348241 · https://support.google.com/googleplay/android-developer/answer/13821247
- G9 Google duyurusu (04.03.2026): https://android-developers.googleblog.com/2026/03/a-new-era-for-choice-and-openness.html
- G10 Rekabet Kurumu: https://www.rekabet.gov.tr/tr/Guncel/alphabet-inc-google-ireland-limited-goog-31d6cac01f7ff01193e20050568549fa
- G11 Hizmet bedelleri: https://support.google.com/googleplay/android-developer/answer/112622
- G12 2026 bedel düzeni: https://support.google.com/googleplay/android-developer/answer/16954621 · https://android-developers.googleblog.com/2026/06/play-expanded-billing.html
- G13 Hesap silme: https://support.google.com/googleplay/android-developer/answer/13327111
- G14 Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- G15 PBL takvimi (20.10.2026): https://developer.android.com/google/play/billing/deprecation-faq
- G16 Hedef API: https://support.google.com/googleplay/android-developer/answer/11926878
- G17 Entegrasyon: https://developer.android.com/google/play/billing/integrate
- G18 Tek seferlik ürün yaşam döngüsü: https://developer.android.com/google/play/billing/lifecycle/one-time
- G19 RTDN ve iptaller: https://developer.android.com/google/play/billing/rtdn-reference · https://developers.google.com/android-publisher/voided-purchases

Apple:
- A1 App Review Guidelines (16.10.2026): https://developer.apple.com/app-store/review/guidelines/
- A2 Yüksek Mahkeme dosyası 25-1311: https://www.supremecourt.gov/docket/docketfiles/html/public/25-1311.html
- A3 [3. taraf] https://www.courthousenews.com/apples-fight-over-commissions-for-linked-out-app-store-purchases-continues-in-district-court/
- A4 External Purchase Link izinleri: https://developer.apple.com/support/storekit-external-entitlement/
- A5 Hesap silme: https://developer.apple.com/support/offering-account-deletion-in-your-app/
- A6 Universal Purchase: https://developer.apple.com/support/universal-purchase/
- A7 Family Sharing: https://developer.apple.com/help/app-store-connect/configure-in-app-purchase-settings/turn-on-family-sharing-for-in-app-purchases/
- A8 Server API ve bildirimler: https://developer.apple.com/documentation/appstoreserverapi · https://developer.apple.com/documentation/appstoreservernotifications
- A9 Small Business Program: https://developer.apple.com/app-store/small-business-program/
- A10 Program sözleşmesi, Schedule 2: https://developer.apple.com/support/terms/apple-developer-program-license-agreement/
- A11 Dijital hizmet vergisi güncellemesi (29.01.2026): https://developer.apple.com/news/?id=gvnljl3f

Web ödemesi ve vergi:
- W1 https://stripe.com/global
- W2 https://www.paypal.com/tr/home
- W3 https://www.paddle.com/help/start/intro-to-paddle/what-am-i-not-allowed-to-sell-on-paddle
- W4 https://docs.lemonsqueezy.com/help/getting-started/supported-countries
- W5 https://www.lemonsqueezy.com/blog/2026-update · https://docs.stripe.com/payments/managed-payments/eligibility
- W6 https://docs.lemonsqueezy.com/help/getting-started/prohibited-products
- W7 https://fastspring.com/terms-use/
- W8 https://www.iyzico.com/en/business/signup · https://iyzico.com/en/general-conditions
- W9 VUK 509 güncel metin (GİB): https://cdn.gib.gov.tr/api/gibportal-file/file/getFile?objectKey=MEVZUAT_TEBLIGLER%2FUNIVERSAL%2F2026%2FMEVZUAT_TEBLIGLER_2026_VukTeb509_Guncel.pdf
- W10 AB OSS: https://vat-one-stop-shop.ec.europa.eu/one-stop-shop_en

Hak altyapısı ve giriş:
- R1 https://www.revenuecat.com/docs/web/web-billing/web-sdk
- R2 https://www.revenuecat.com/docs/web/web-billing/web-purchase-links · https://www.revenuecat.com/docs/web/integrations/paddle
- R3 https://www.revenuecat.com/docs/getting-started/installation/ios
- R4 https://www.revenuecat.com/docs/customers/identifying-customers
- R5 https://www.revenuecat.com/pricing/ · https://adapty.io/pricing/ · https://qonversion.io/pricing
- R6 https://www.revenuecat.com/docs/platform-resources/non-subscriptions
- I1 https://developer.android.com/identity/form-factors · https://developers.google.com/identity/protocols/oauth2/limited-input-device
- I2 https://auth0.com/docs/get-started/authentication-and-authorization-flow/device-authorization-flow
- I3 https://www.keycloak.org/securing-apps/specifications
- I4 https://firebase.google.com/docs/auth/web/google-signin · https://supabase.com/docs/guides/auth/oauth-server/oauth-flows
- I5 https://docs.aws.amazon.com/cognito/latest/developerguide/federation-endpoints-oauth-grants.html
- I6 https://clerk.com/docs/guides/configure/auth-strategies/oauth/device-authorization-grant

LG, Samsung ve rakipler:
- L1 https://webostv.developer.lge.com/develop/guides/in-app-purchase
- L2 [3. taraf] https://forum.webostv.developer.lge.com/t/in-app-purchase/28344
- L3 https://developer.samsung.com/smarttv/develop/guides/samsung-checkout.html
- L4 https://developer.samsung.com/tv-seller-office/guides/membership/becoming-partner.html
- C1 IPTVX SSS (18.10.2026): https://iptvx.app/faq/index.html
- C2 Smart IPTV: https://siptv.app/activation/
- C3 Net IPTV: https://www.netiptv.eu/payment
- C4 TiviMate: https://play.google.com/store/apps/details?id=ar.tvplayer.tv · https://play.google.com/store/apps/details?id=ar.tvplayer.companion
- C5 UHF (ikinci kontrol yok): https://apps.apple.com/us/app/uhf-love-your-iptv/id6443751726
- C6 Purple Playlist Player (ikinci kontrol yok): https://apps.apple.com/app/id1547219704
- C7 iPlayTV (ikinci kontrol yok): https://apps.apple.com/us/app/iplaytv-iptv-m3u-player/id1072226801
