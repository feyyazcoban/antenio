# Üyelik planı: 7 gün ücretsiz deneme

22 Ekim 2026 · **Karar (D113, senin):** Yeni kullanıcı 7 gün ücretsiz dener, sonra Google Play aboneliğiyle ücretli üye olur. Bu belgede D111, D112 ve D113 dışındaki her şey **öneridir**. Gün sayıları tahmindir, ölçülmedi. Hukuk ya da vergi tavsiyesi değildir.

Kaynaklar 22.10.2026'da açıldı ve ayrıca bir kez daha kontrol edildi. "Çıkarım" diye işaretli yerler resmî bir cümleye değil, kuralların okunmasına dayanır.

## 24 Ekim 2026 güncellemesi (D121): bu belgenin değişen yerleri

**Karar (D121, senin):** Üretime abonelikle çıkılır. İlk yayında Antenio hesabı yok (B yolu). Tek yıllık plan. İlk hafta kartsız ve abonelik istenmeden ücretsiz izlenir; 8. günden itibaren izlemek için abonelik gerekir. Aşağıdaki eski metinde Google Play'in kart isteyen 7 günlük deneme teklifi geçen her yer (§2 adım 4–7 ve 10, §3 adım 6'daki teklif, §8 "Fiyat metni", teklif `trial-7d`) bu kararla geçersizdir.

Yeni akış (D121 dışındaki ayrıntılar öneridir):

1. Kullanıcı kurar, listesini ekler, gezer. Ödeme sorulmaz.
2. **Ücretsiz hafta ilk oynatmada başlar** (öneri): kullanıcı listesini bir hafta sonra eklese de 7 gün izler. Süre dolana kadar Ayarlar → Üyeliğim'de ve oynatıcı açılırken kısa bir "N gün kaldı" notu görünür (öneri).
3. **8. gün ve sonrasında "İzle":** üyelik ekranı çıkar. Ekranda yıllık fiyat (Google'dan okunur, vergiler dahil), her yıl kendiliğinden yenilendiği, iptal yolu, koşulların bağlantısı ve görünür bir "Kapat" bulunur. "Abone ol" Google Play'in ödeme penceresini açar; ücret o an alınır.
4. **İzleme dışında her şey açık kalır** (öneri, eski §2 madde 11).
5. **Haftanın sayılması:** Başlangıç zamanı yalnız bu cihazda, uygulamanın özel depolamasında ve sıfırlamalardan (kaynak kurtarma, "Her şeyi sıfırla") etkilenmeyen bir anahtarda (`DeviceKeys.membership`) tutulur. Sunucu olmadığı için uygulamayı silip yeniden kurmak haftayı yeniden başlatabilir; kaynağın yeniden eklenmesi gerektiği için caydırıcıdır. Bu risk ilk yayında kabul edilir, hesap ve sunucu gelince kapanır (öneri). Cihaz saati geri alınırsa haftanın uzamaması için son görülen zaman da saklanır (öneri).
6. **Google incelemesi:** İnceleme ekibi ilk hafta her şeyi ücretsiz gördüğü için eski Risk 6 (ödeme duvarının arkasına erişim) büyük ölçüde çözülür; talimata ücretsiz haftadan söz eden bir cümle eklenir.
7. **Hukuk açısından:** Deneme sonunda kendiliğinden ücrete dönüşme olmadığı için eski Risk 3'teki "denemeden ücretliye dönüş" sorusu düşer; otomatik yıllık yenileme, iade ve iptal soruları (§7) sürer.

Fiyat (D122, senin): ABD'de yıllık 7,99 $; TL fiyatı ürün kurulurken Play'in çevirisine bakılıp birlikte konur.

Play Console (öneri adlar): abonelik `antenio_premium`, temel plan `yearly` (otomatik yenilenen), teklif yok. Önce ödeme kütüphanesi içeren bir sürümün kapalı teste yüklenmesi ve ödeme profilinin (banka hesabı) kurulması gerekir; ödeme profili mali müşavir görüşmesinden sonra.

Kod tahmini yine yaklaşık 3 hafta (ölçülmedi): Play deneme teklifi metni yerine uygulamanın kendi haftası gelir; sayaç ve "N gün kaldı" notu yaklaşık 1 gün ekler, teklif ve uygunluk işleri düşer.

**Kod durumu (0.17.0, 24 Ekim):** Uygulama tarafı yazıldı: `data/Membership.kt` (hafta, saat geri alınması, Play durumu, 3 günlük güven süresi), `data/PlayBilling.kt` (Play Billing Library 9.1.0, onaylama cihazda), `TvViewModel.startPlayback` içindeki tek kapı, `ui/MembershipScreen.kt` (üyelik ekranı) ve Ayarlar → Üyelik. Gerçek satın alma denenmedi: Play Console'da `antenio_premium` / `yearly` yok. Kalanlar: ödeme profili, ürün, lisans test kullanıcılarıyla gerçek deneme, koşullar sayfası (ekrandaki QR `antenio.app/terms` ve `antenio.app/kosullar`'a gider; sayfalar yok), mağaza metni, IARC "dijital satın alma" = evet, Data safety (kütüphane Google'ın datatransport paketini ve Play services'ı getiriyor; konum izni eklenmiyor, beyan etkisi kontrol edilecek), gizlilik politikası.

## Kısaca

- Uygulama Play'den indirilir. Kullanıcı kendi IPTV listesini ekler ve kanallarını görür.
- İzlemek için 7 günlük ücretsiz denemeyi başlatır. Deneme Google Play üzerinden açılır ve kart ister.
- 8. gün Google ücreti kendiliğinden çeker. Üyelik her dönem kendiliğinden yenilenir; kullanıcı istediğinde iptal eder.
- Para ilk günden gelmeye başlar. Yeni özellik yazmayı beklemeye gerek yoktur.
- **Bunun için ilk yayından önce üç iş şart:** mali müşavir görüşmesi, avukatın kısa metin kontrolü ve ödeme sistemini yazmak (yaklaşık 3 hafta, ölçülmedi).

## 1. Önce bir soru: Antenio hesabı ilk yayında olsun mu?

Daha önce şunu kabul ettin: "Premium ile aynı sürümde hesap" (D112-3). O karar şunu da söylüyor: "Hesapsız Premium satışı yapılmaz." D113 ile ilk ücretli sürüm artık ilk Play yayını oldu. Bu yüzden D112-3 değişmezse hesap sistemi ilk yayına girer.

| | A: D112-3 kalsın | B: Önce yalnız abonelik (öneri) |
|---|---|---|
| İlk yayında | Abonelik + Antenio hesabı (QR ile giriş, küçük sunucu) | Yalnız abonelik |
| Ödeme sistemi için kod | Yaklaşık 3 hafta, artı hesap planındaki iş ([account.md](../design/gap/account.md), yaklaşık 31,5 iş günü; bir kısmı örtüşüyor, net ek süre ölçülmedi) | Yaklaşık 3 hafta |
| Yeni kararlar | Satın alma takip servisi, giriş servisi, cihaz sınırı, veri yeri | Yok |
| Bedeli | Yayın daha geç | Hesap Apple sürümünden önce gelir. O zamana kadar abone olanlar, üyeliklerini Apple'a taşımak için bir kez Android TV'lerinden hesaba giriş yapar. Gizlilik beyanı da o zaman güncellenir. |

Android'de üyelik zaten Google hesabına bağlıdır. Uygulama silinip yeniden kurulunca geri gelir. Aynı Google hesabıyla kullanılan ikinci bir Android TV'de de gelmesi beklenir (testte doğrulanacak). Hesap yalnız üyeliği başka platformlara (Apple, ileride başka yerler) taşımak için gerekir (D111). **B'yi seçersen D112-3 değişir; bunun için açık onayın gerekir.**

## 2. Müşteri ne yaşar? (B yolu, öneri)

1. **Kurar.** Antenio'yu Google Play'den kurar. Mağaza sayfası açıkça söyler: Antenio kanal içermez, kendi listeni eklersin, izlemek için üyelik gerekir (7 gün ücretsiz, sonra fiyat, otomatik yenilenir).
2. **Listesini ekler.** Bugünkü gibi kendi IPTV listesini ekler. Ödeme sorulmaz.
3. **Gezer.** Kanallarını, filmlerini ve rehberi görür; listesinin çalıştığını anlar.
4. **İlk kez "İzle"ye basar.** Üyelik ekranı çıkar. Kullanıcı ayrıca bir şeye basmadan, tek ekranda şunları görür:
   - 7 gün ücretsiz deneme ve sonra ödeyeceği fiyat (vergiler dahil, dönem başına);
   - denemenin 7. günün sonunda kendiliğinden ücretliye döneceği ve her dönem yenileneceği;
   - nasıl iptal edeceği;
   - satıcı bilgisi ve koşulların bağlantısı;
   - görünür bir "Kapat" düğmesi.
5. **"Denemeyi başlat"a basar.** Google Play'in kendi ödeme penceresi açılır. Google geçerli bir ödeme yöntemi (kart ya da Play'in kabul ettiği başka bir yöntem) ister. Kartta geçici bir bloke görülebilir; Google sonra kaldırır. O gün para çekilmez.
6. **1–7. gün:** Her şey açıktır.
7. **8. gün:** Google ücreti kendiliğinden çeker. Üyelik dönem boyunca sürer ve dönem sonunda kendiliğinden yenilenir. Google'ın yardım sayfasına göre deneme bitmeden kullanıcıya bir hatırlatma e-postası gider; planı buna güvenerek kurmuyoruz.
8. **İptal:** Kullanıcı Google Play > Abonelikler'den iptal eder. Google, TV'den iptal için telefon ya da bilgisayar gerektiğini söylüyor. Bu yüzden uygulamada `g.co/play/subscriptions` adresini ve telefonla okutulacak bir QR kod gösteririz (öneri).
   - Denemede iptal ederse 7. günün sonuna kadar izler, hiç ödemez.
   - Ücretli dönemde iptal ederse Google'ın kuralına göre dönem sonuna kadar izler, iade almaz. Türk hukuku kalan ücretin iadesini isteyebilir (Risk 3).
   - Uygulamayı silmek aboneliği iptal etmez.
9. **Ödeme sorunu:** Kart reddedilirse Google bir süre yeniden dener; bu sırada izleme sürer. Düzelmezse izleme durur ve uygulama "Google Play'de ödeme yöntemini güncelle" der. Bu bekleme en çok 60 gün sürer (Google'ın varsayılanı); sonra üyelik biter.
10. **İkinci deneme yok:** Bu uygulamada daha önce abone olmuş biri ikinci kez deneme alamaz. Google bunu Google hesabına göre izler; uygulamayı silip kurmak süreyi sıfırlamaz (çıkarım, testte doğrulanacak).
11. **Üyeliksiz açık kalanlar (öneri):** liste ekleme ve silme, listeleri gezme, rehber, arama, Ayarlar, PIN kilidi ve "PIN'i unuttum", Hakkında ve gizlilik, üyelik sayfası. Kapalı olan tek şey izlemek.

## 3. Senin yapacakların

Sırayla. Her biri için adımları ben yazarım; tıklamak ve imzalamak sana kalır.

1. **Mali müşavirle görüş (yayından önce).** Türkiye'de Play'deki satıcı geliştiricinin kendisidir. Kanun, sözleşmeden önce satıcının adını, vergi ya da MERSİS numarasını (şirketlerin merkezi sicil numarası), adresini ve telefonunu göstermeni istiyor. Bu yüzden şahıs olarak mı, şirketle mi satacağını ve vergiyi yayından önce netleştirmelisin.
2. **Avukatla kısa bir görüşme yap.** Sorular hazır (§7). Avukat ayrıca üyelik ekranı metnini, koşullar metnini ve mağaza metnindeki "ücretsiz" ifadesini okur.
3. **Google Play geliştirici hesabını aç.** 25 $ ve kimlik doğrulama. Ödeme profiline banka hesabını ve vergi bilgilerini gir (gereken formlar Play Console'da kontrol edilecek). Bilmen gereken iki şey var:
   - Kişisel hesapla satış yaparsan Google tam adresini Play'de herkese gösterir.
   - Kişisel hesapta yayından önce 12 kişiyle 14 gün kapalı test şartı var. Kapalı test, kişisel hesapta yayından önce istenen bu test turudur.

   Kişisel mi kurum mu, mali müşavirle birlikte seç.
4. **Uygulamanın kalıcı adını ve imza anahtarını hazırla** ([LAUNCH_AUDIT](../project/LAUNCH_AUDIT.md) E1, E3).
   - Paket adı, uygulamanın Play'deki kalıcı kimliğidir; ilk yüklemeden sonra değişmez.
   - İmza anahtarı, uygulamanın sana ait olduğunu kanıtlayan dijital anahtardır. Kaybedilmemeli.
5. **Ad, dönem ve fiyatı seç** (§5, karar 2).
   - Google'ın payı bugün Türkiye'deki abonelikte %15. ABD, Avrupa ve İngiltere'de de toplam %15.
   - Türkiye'nin de girdiği anlaşılan "diğer ülkeler" grubunda yeni ücret yapısı 30.09.2027'de başlıyor; ayrıntısı henüz açıklanmadı.
6. **Play Console'da aboneliği kur.** Play Console, Google'ın geliştirici panelidir. Kurulum sırası:
   - **Abonelik:** ör. "Antenio Premium".
   - **Temel plan:** fiyat ve dönem, ör. yıllık.
   - **Teklif:** temel plana eklenen koşul; burada 7 gün ücretsiz deneme.
   - **Uygunluk:** kimin deneme alabileceği. Öneri: "yeni müşteri", yani daha önce abone olmamış kişi. Kimin deneme alacağına Google karar verir.
7. **Gizlilik politikası ve koşullar metni (Türkçe ve İngilizce).**
   - **Gizlilik politikası:** bugün yok, mağaza için şart.
   - **Koşullar metni:** fiyat, deneme, otomatik yenileme, iptal yolu, iade, satıcı bilgileri, desteklenen cihazlar, hakem heyeti ve arabulucu bilgisi.
   - **Yayın yeri:** ikisi de bir web sayfasında durur (öneri; alan adı ve basit bir sayfa gerekir). Mağaza sayfası ve üyelik ekranı onlara bağlanır.
8. **Destek ve iade süreci.** Mağazada görünen bir destek e-postası olmalı. İade talepleri Play'de ilk 48 saatten sonra sana gelir; Play Console'dan tam ya da kısmi iade yapabilirsin.
9. **Play Console beyanları.** Bunları doldurursun:
   - **Data safety:** Google'a verilen veri beyanı.
   - **İçerik derecelendirme anketi:** dijital satın alma sorusuyla birlikte; sorunun tam metni Console'da kontrol edilecek.
   - **Hedef yaş** ve **TV seçeneği.**
   - **İnceleme erişimi:** Google'ın inceleme ekibine ödeme ekranının arkasını ücretsiz görme yolunu gösteren talimat. Bunun için yasal bir deneme listesi de gerekir (Risk 6).
10. **Test et.**
    - Play Console > Ayarlar > Lisans testi bölümüne kendini ve test edecekleri ekle. Lisans test kullanıcısı, kartından para çekilmeden satın almayı deneyebilen kişidir.
    - Testte süreler kısalır: deneme 3 dakika, bir yıl 30 dakika.
    - Kendi TV'nde test kartlarıyla dene: deneme → ücretli, denemede iptal, reddedilen kart, "Aboneliği yönet" QR'ının telefonda açılması.
    - Kapalı testteki 12 kişinin Android TV ya da Google TV cihazı gerekebilir.

## 4. Benim (kod) yapacaklarım

B yolu için. Günler **tahmin, ölçülmedi**. Ekran ve kapı işleri, §5'teki 3. ve 4. kararlarda önerilen seçeneğe göre yazıldı.

| İş | Gün |
|---|---|
| Kodun en güncel hâlinden başlamak, değişen belgeleri birleştirmek | 0,5 |
| Google Play ödeme kütüphanesini eklemek | 1,5 |
| Üyeliği Google'dan okumak, cihazda saklamak, satın almayı onaylamak (Google'a "aldım" demek; 3 gün içinde yapılmazsa Google parayı iade eder) | 2 |
| "İzle"ye basıldığında üyeliği kontrol eden tek nokta; üyelik bitince oynatıcıdan çıkmak | 1,5 |
| Üyelik ekranı: kumandayla kolay, Türkçe ve İngilizce, fiyatı Google'dan okur, QR kod | 3 |
| Ayarlar'da "Üyeliğim": durum, "Aboneliği yönet" (QR), "Yeniden kontrol et", ödeme sorunu mesajları | 1,5 |
| Otomatik testler (sahte ödeme sistemiyle) | 2 |
| Gerçek Play testi ve düzeltmeler | 2 |
| Kayıtlar, mağaza metni, Data safety notu | 0,5 |
| **Toplam** | **Yaklaşık 14,5 iş günü (yaklaşık 3 hafta)** |

A yolu seçilirse buna hesap işi eklenir ([account.md](../design/gap/account.md)).

**Sonraya kalan iş:** Fiyat artışında mevcut abonelere uygulama içinde en az 30 gün önce haber verilmesi gerekiyor. Bu ilk zamdan önce bir uygulama güncellemesiyle eklenir.

## 5. Senden beklenen kararlar

1. **Hesap ilk yayında mı?** (§1) **Öneri: B,** önce yalnız abonelik.
2. **Ad, dönem ve fiyat.** **Öneri:** Tek plan, yıllık, adı "Antenio Premium".
   - Fiyat için yalnız örnek: eski önerilerde yıllık 149,99 TL ve 7,99 $ vardı ([SALES_STRATEGY](SALES_STRATEGY.md) §3). O rakam tek seferlik modelin yanına düşünülmüştü; yalnız abonelikte aynı anlamı taşımaz.
   - Aylık plan da eklenirse, yıllık plan ekranda aylık fiyatıyla en öne çıkarılmamalı. Google bunu yanıltıcı sayıyor.
3. **Üyelik ekranı ne zaman çıksın?** İlk açılışta, liste eklenince ya da ilk "İzle"de. **Öneri:** İlk "İzle"de; kullanıcı önce kendi listesinin çalıştığını görür.
4. **Üyeliksiz ne açık kalsın?** **Öneri:** İzlemek dışında her şey (§2, madde 11).
5. **Hangi ülkelerde satılsın?** **Öneri:** Satış bütün ülkelerde açık, tanıtım Türkiye ve Avrupa'da. Avrupa'da ek kurallar var (Risk 3); avukata sorulur.
6. **Test edenlere ne olacak?** **Öneri:** Test boyunca lisans test kullanıcısı olsunlar, para ödemesinler. Yayında herkes gibi 7 gün denesinler. Testteki satın almanın yayında deneme hakkını etkileyip etkilemediği testte kontrol edilecek.

## 6. Riskler

1. **Kendi sunucumuz yok (B yolunda).** Sunucu, internette sürekli çalışan kendi hizmetimiz demek. B yolunda üyelik yalnız TV'de kontrol edilir. Google, satın almanın güvenli bir sunucuda doğrulanmasını kesinlikle öneriyor. Değiştirilmiş bir uygulamayla üyelik atlatılabilir (çıkarım). Öneri: ilk yayında bu riski kabul etmek; hesap gelince sunucu doğrulaması eklemek.
2. **Yanlış beklenti.** Kullanıcı Antenio'nun kanal sattığını sanabilir. Rakip oynatıcıların yorumlarında "bu sadece bir oynatıcı" uyarıları var; iade şikâyetleri çoğunlukla içerik satan IPTV servislerine yönelik [3. taraf]. Mağaza metni ve üyelik ekranı bunu açıkça söylemeli.
3. **Tüketici hukuku belirsiz.** İki kritik soru var: otomatik yenileme kanunda nasıl sayılır, iptalde kalan ücret iade edilmeli mi? Google'ın kuralı "iade yok", ama Play Console'dan kısmi iade yapılabilir. Kanunun ilgili maddelerine aykırılıkta her işlem için ayrı idari para cezası var (taban 2.200 TL, her yıl güncellenir). Avrupa'da deneme sonrası ücret açıkça söylenmezse kullanıcı ücretliye dönüşte yeniden cayma hakkı kazanır.
4. **TV'den iptal edilemiyor.** Google iptal için telefon ya da bilgisayar istiyor. Kanun ise iptalin, aboneliği başlatmaktan zor olmamasını istiyor. QR kod ve kısa adres yardım eder; yeterli olup olmadığını avukata soruyoruz.
5. **TL'de zam.** Fiyat artınca mevcut aboneler eski fiyatta kalır. Onları yeni fiyata geçirmenin kuralları var: aynı plan ve ülke için yılda en çok bir kez, en çok %50 (ya da günde 17 ABD senti, hangisi büyükse), en az 30 gün önce uygulama içinde haber. Bu kurallara uymayan zamda, kabul etmeyenin aboneliği biter.
6. **Google incelemesinde ret.** İnceleme ekibi ödeme ekranının arkasını ücretsiz görebilmeli. Bugün onlara verecek yasal bir deneme listemiz yok ve ücretsiz erişim yolu henüz seçilmedi. Çözülmezse uygulama reddedilebilir.

## 7. Avukata sorulacaklar

1. Her dönem kendiliğinden yenilenen abonelik kanunda "belirli süreli" mi, "belirsiz süreli" mi sayılır? Belirli süreliyse otomatik uzatma için tüketicinin onayı gerekir.
2. Kanun iptalde kalan ücretin 15 gün içinde iadesini istiyor; Google "dönem sonuna kadar, iade yok" diyor. Nasıl uzlaşır? Kısmi iade yeterli mi?
3. 14 günlük cayma hakkı hangi istisnayla kalkar ve kullanıcıya nasıl söylenir?
4. Ön bilgilendirme, kullanıcı onayı ve sözleşme nüshası Google'ın akışında nasıl verilir ve ispatlanır? Google'ın makbuz e-postası yeterli mi?
5. Google Play kanunda "aracı hizmet sağlayıcı" sayılır mı?
6. Şahıs olarak satışta numara, adres ve telefon yükümlülükleri neler?
7. TV'den iptal edilemiyor; QR ve kısa adres yeterli mi?
8. "7 gün ücretsiz" ifadesi şartlarla birlikte gösterilince haksız ticari uygulama sayılmaz mı? Düğmede "Denemeyi başlat" demek yeterli mi?
9. Avrupa'da satış için deneme sonrası bilgilendirme ve 2026'dan itibaren zorunlu olan cayma düğmesi bize nasıl uygulanır?

## 8. Geliştirici notları

Satır numaraları bu dalın tabanından (`ae5afb4`) alındı. `main` artık `c25a66a`'da (0.14.83) ve aynı dosyaların çoğunu değiştirdi; satırlar kayabilir, özellikle `PlayerScreen.kt`'de, adla ara. `<tv>/` = `apps/android-tv/app/src/main/java/dev/iptvplayer/tv/`.

**Başlangıç:** Kod işi `main`'den başlar. Bu daldaki belgeler `main`'e birleşirken DECISIONS sonu (D109–D110 `main`'de, D111–D113 burada), STATUS ve HANDOFF üst girişleri çakışır. İkisini de koruyun, numara sırası korunur.

**Kütüphane:**
- PBL 8 ve üstü 31.08.2026'dan beri zorunlu; ek süre talebi için son gün 01.11.2026.
- Belgelerdeki örnek sürüm 9.1.0. 9.0.0 targetSdk 35, 8.1.0 minSdk 23 istiyor; bizde 36 ve 26.
- Eklemeden önce güncel sürüm release notes'tan doğrulanır (`apps/android-tv/app/build.gradle.kts`).
- `enableAutoServiceReconnection()` kullanılır. `queryPurchaseHistory` ve `querySkuDetailsAsync` 8.0'da kalktı.
- B yolunda RevenueCat gerekmez; doğrudan PBL yeterli. A yolunda [account.md](../design/gap/account.md) RevenueCat önerir. O plan tek seferlik ürüne göre yazıldı; abonelik için güncellenmeli.

**Yapı:** `BillingClient` bir arayüzün arkasında durur; gerçek ve test için sahte uygulaması olur. Tek örneği `TvApplication`'da (`MainActivity.kt`) tutulur ve ViewModel'e `ProtectionStore` gibi verilir.

**Kapı** (karar 3: ilk "İzle"):
- `GateSnapshot`'a (`data/AccessGate.kt`) `subscribed` alanı eklenir; `gateSnapshot()` (`TvViewModel.kt:1075-1083`) bunu doldurur.
- Yeni sonuç `GateResult.NeedsSubscription(target)`. `answer()` içinde `appLocked`/null → `Deny` kuralından sonra, PIN'den önce gelir. Böylece kimse PIN yazdıktan sonra ödeme ekranına çarpmaz.
- `request()` bu sonuçta üyelik ekranını açar. Ekranların Boolean API'si değişmez.
- `playNextEpisode` sonucu UNAVAILABLE'a çevrilir; otomatik oynatma üyelik ekranı açmaz.
- `request()`'i atlayan yollar ayrıca denetlenir: `startAutomatically`, `lockLifted`, `appStopped`, `zapTarget`, `retryDetail`, `publishLibrary` detay kontrolü.
- `NeedsPlus`, `Feature`, `AllFree` (`data/Features.kt`) ve `PageGuard.Plus` özellik başına Plus önerisine dayanıyor; D113 o öneriyi geçersiz kıldı. Kaldırılmaları öneridir.

**Ekran:**
- Modal için `AppOverlay` (`ui/PilotApp.kt`), `rememberOverlayOpener`, `AppOverlayHost` ve `refocusAfterDialog` kullanılır. Tam ekran istenirse `ui/SourceRecoveryScreen.kt` deseni izlenir ve yeni route `BackHandler` istisna listesine eklenir.
- Politika şartları: bütün koşullar kaydırmadan görünür (1,5 ve 2,0 yazı boyutunda da), "Kapat" görünür, fiyat yerelleştirilmiştir. Aylık plan da varsa yıllık plan aylık fiyatıyla en öne çıkarılmaz.
- Ayar sayfaları (`PageGuard`, `PageGate`) üyelik istemez. Metinler `values` ve `values-tr`'de durur.

**Fiyat metni:**
- `ProductDetails` her satın almadan önce taze alınır, önbelleğe konmaz. `getSubscriptionOfferDetails()` yalnız kullanıcının uygun olduğu teklifleri döndürür.
- Deneme teklifi dönmezse ekran "7 gün ücretsiz" demez, temel planı gösterir.
- Metin iki aşamadan kurulur: deneme aşaması ve `recurrenceMode=INFINITE` olan temel aşama (`billingPeriod` P1Y).
- `launchBillingFlow`'a `offerToken` verilir; 8.0'dan beri `USER_INELIGIBLE` dönebilir.

**Durumlar:**

| Durum | İzleme | Uygulama |
|---|---|---|
| Aktif, ödemesiz ilk 24 saat, ek süre | Var | Normal |
| İptal edilmiş, dönem sürüyor (`isAutoRenewing()=false`) | Var | "Üyeliğim": yenilenmeyecek |
| PENDING | Yok | "Ödeme bekleniyor"; onaylanmaz |
| Askı ya da duraklatılmış (`includeSuspendedSubscriptions(true)`, `isSuspended()`; çıkarım, testte doğrula) | Yok | Ödeme sorunu ve yönet bağlantısı; deneme teklifi gösterilmez |
| Süresi dolmuş ya da geri alınmış (sorguda dönmez; çıkarım, testte doğrula) | Yok | Normal üyelik ekranı |

PURCHASED olunca `acknowledgePurchase()` çağrılır; onaylanmamışlar her açılışta yeniden denenir. Yenilemeler onay istemez.

**Önbellek:**
- `data/DeviceKeys.kt`'ye `KeyScope.DEVICE`, `reset = false`, `BackupClass.NONE` olan bir anahtar eklenir ve `all` listesine konur.
- Bu anahtar `forgetSource`, `startOver` ve `resetUnreadableSources` sonrasında da kalır. `iptv_source_v1` alias'ıyla şifrelenmez, çünkü o alias kaynak kurtarmada silinir.
- Saklanan: durum ve son başarılı kontrol zamanı.
- Yenileme yerleri: `restore`, `appStarted`, satın alma dinleyicisi.
- Play'e ulaşılamazsa son olumlu durum kısa süre kabul edilir (öneri: 3 gün). Play "abonelik yok" derse kapı hemen kapanır.
- Yedekleme kapalı olduğundan önbellek yeniden kurulumda gider; `queryPurchasesAsync` durumu geri getirir.

**Yönet bağlantısı:** `https://play.google.com/store/account/subscriptions?sku=<productId>&package=<paket>`. TV'deki Play Store'da açıldığı belgelenmemiş; ayrıca `g.co/play/subscriptions` için QR gösterilir. Adres sabit olduğu için QR kütüphanesiz, gömülü bir görsel olabilir (öneri).

**Play Console (öneri adlar):** Abonelik `nextv_premium`, otomatik yenilenen temel plan `yearly`, teklif `trial-7d`. Teklif 7 gün ücretsiz denemedir; uygunluk "yeni müşteri". Ek süre ve hesap askısı varsayılan kalır: toplam 30–60 gün; askı 01.12.2025'ten beri 60 gün eksi ek süre. Duraklatma varsayılan olarak açık (öneri: kapatılması değerlendirilsin).

**Data safety:** Uygulama ödeme verisine hiç erişmediği için Play Billing'in aldığı ödeme verisi beyan edilmez. Satın alma bilgisi cihazdan çıkmazsa "Purchase history" beyanı gerekmiyor gibi görünüyor (çıkarım, resmî teyit değil). Analitik, sunucu doğrulaması ya da hesap gelince form güncellenir.

**Testler:**
- **JVM:** `AccessGateTest` (NeedsSubscription sırası, kilit arkasında Deny), `DeviceKeysTest` (`reset=false`), yeni ve saf bir "durum → erişim" testi.
- **Cihaz:** `PaywallFlowTest` (sahte billing, `FixtureSources.kt`); `bash tools/run-device-tests.sh PaywallFlowTest`, yalnız emulator-5558.
- **Gerçek Play:** dahili test ve lisans test kullanıcılarıyla. Test kartları: onaylayan, reddeden, yavaş (PENDING), chargeback. Senaryolar: deneme → ücretli, denemede iptal, reddedilen kart → ek süre → askı, yeniden kurulum, ikinci deneme yok. Test aboneliği en çok 6 kez yenilenir; deneme bu sayıya girmez.
- **Sıra ve süreç riskleri (doğrulanacak):** Play Console'da abonelik oluşturmadan önce ödeme kütüphanesi içeren bir sürüm yüklenmiş olmalı. Kapalı testten sonra üretime erişim başvurusu ve inceleme süresi var.

## Kaynaklar

- Abonelik, deneme, uygunluk, zam: https://support.google.com/googleplay/android-developer/answer/12154973
- Abonelik davranışı, yönet bağlantısı: https://developer.android.com/google/play/billing/subscriptions
- Entegrasyon, onaylama, PBL takvimi: https://developer.android.com/google/play/billing/integrate
- Abonelik durumları: https://developer.android.com/google/play/billing/lifecycle/subscriptions
- Güvenlik: https://developer.android.com/google/play/billing/security
- Sürüm notları: https://developer.android.com/google/play/billing/release-notes
- Test: https://developer.android.com/google/play/billing/test · https://support.google.com/googleplay/android-developer/answer/6062777
- Fiyat değişikliği: https://developer.android.com/google/play/billing/price-changes · https://support.google.com/googleplay/android-developer/answer/13542779 · https://support.google.com/googleplay/android-developer/answer/140504
- Ek süre ve askı: https://developers.google.com/android-publisher/api-ref/rest/v3/monetization.subscriptions · https://support.google.com/googleplay/android-developer/answer/16631229
- Abonelik politikası: https://support.google.com/googleplay/android-developer/answer/9900533
- İnceleme erişimi: https://support.google.com/googleplay/android-developer/answer/15748846
- Adres görünürlüğü: https://support.google.com/googleplay/android-developer/answer/13628312
- Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Komisyon: https://support.google.com/googleplay/android-developer/answer/112622 · https://support.google.com/googleplay/android-developer/answer/16954621
- İade ve iptal: https://support.google.com/googleplay/android-developer/answer/2741495 · https://support.google.com/googleplay/answer/7018481
- Deneme e-postası: https://support.google.com/googleplay/answer/2476088
- TV'de iptal: https://support.google.com/androidtv/answer/9461288
- Satıcı: https://support.google.com/googleplay/android-developer/answer/7645364 · https://play.google/developer-distribution-agreement.html
- TKHK 6502: https://www.mevzuat.gov.tr/MevzuatMetin/1.5.6502.pdf
- Mesafeli Sözleşmeler Yönetmeliği: https://www.mevzuat.gov.tr/MevzuatMetin/yonetmelik/7.5.20237.pdf
- Abonelik Sözleşmeleri Yönetmeliği (Bakanlık kopyası, 2015; güncel birleşik metin teyit edilmedi): https://sirnak.ticaret.gov.tr/data/5eddf7f713b876697046a145/Abonelik%20S%C3%B6zle%C5%9Fmeleri%20Y%C3%B6netmeli%C4%9Fi.pdf
- Haksız ticari uygulamalar ("ücretsiz"): https://www.mevzuat.gov.tr/MevzuatMetin/yonetmelik/7.5.20435.pdf
- AB, Sofatutor kararı (C-565/22): https://curia.europa.eu/jcms/upload/docs/application/pdf/2023-10/cp230154en.pdf
- Kullanıcı yorumları [3. taraf]: https://play.google.com/store/apps/details?id=ar.tvplayer.tv · https://www.trustpilot.com/review/streamqtv.com
