# Kurulum akışı yenilemesi (TV-001–004): tasarım taslağı

22 Ekim 2026 · Kullanıcı mevcut kaynak ekleme ekranını "modernitemize yakışmıyor" buldu ve yenileme istedi. Kullanıcı 23 Ekim 2026'da taslağı onayladı ve önerilen kararlarla kodlandı (D117, 0.14.85); aşağıdaki "Uygulamadaki farklar" bölümü taslaktan ayrılan noktaları sayar. Mevcut davranış sözleşmesi [SCREENS](SCREENS.md) TV-001–004, telefonla kurulum düşüncesi [ONBOARDING](../product/ONBOARDING.md), D116 (alanlar açık metin) ve D058 (klavye yalnız OK ile açılır) geçerlidir.

Görsel taslak (9 ekran, özel tuval): https://claude.ai/artifact/SqgnRvfmSRHhDCPZnDBXYV. Taslak Antenio adını (D115) ve uygulamanın `Ink` renklerini kullanır; QR görseli temsilidir, sayılar örnektir. Figma'ya aktarılmadı.

## Kısaca (teknik olmayan)

Bugünkü ekran: solda slogan, sağda tek kartta tür seçimi, ad, adres, kullanıcı adı ve şifre. Kumandayla uzun adres yazmak zor; klavye açılınca yazılan alan ekranın altında kalıyor; her şey tek yoğun kartta.

Yeni akış:
1. **Karşılama:** iki büyük seçenek, "Telefonla ekle" (öne çıkan) ve "Kumandayla ekle".
2. **Telefonla ekle:** TV'de QR kod, kısa adres ve 6 haneli kod. Telefonda açılan sayfaya bağlantı yapıştırılır ya da sunucu/kullanıcı/şifre yazılır, "TV'ye gönder" denir. TV'de "Bu kaynağı ekle?" onayı ve aynı kod. Sunucu gerekmez; ikisi aynı ev ağında konuşur.
3. **Kumandayla ekle:** her ekranda tek soru, büyük yazı: tür → bağlantı (M3U) ya da sunucu, kullanıcı adı, şifre (Xtream) → ad (isteğe bağlı, varsayılan dolu). Klavye açıkken alan hep görünür.
4. **Bağlanıyor:** ilerleme ve ardından sonuç: "128 kanal · 2.400 film · 310 dizi bulundu". Kısmi sonuçta hangi bölümün geldiği yazılır.
5. **Hazır:** "Kütüphaneyi aç" ile ana sayfa.

Hata, zaman aşımı ve iptal her adımda tek bir sade cümleyle ve "Tekrar dene / Geri" ile çözülür.

## Ekranlar

Kimlikler mevcut TV-001–004 korunur; alt durumlar harfle ayrılır (öneri). Her ekranda sol üstte küçük logo, sağ altta adım göstergesi ("2/4") bulunur. Tam ekran, kart yok; içerik solda metin, sağda eylem alanı olarak ayrılır. Yazı boyutları mevcut `Heading`/`Note` ölçüleri; renkler `Ink` tokenları (Background 15191E, Container 252C33, Text F0F3F1, Muted B7C1C7, Mint A8DFCB).

| ID | Ekran | İçerik | İlk odak | OK | Back |
|---|---|---|---|---|---|
| TV-001 | Karşılama | Sol: logo, "Televizyonun. Senin kütüphanen.", bir satır gizlilik notu. Sağ: iki büyük kart: **Telefonla ekle** (açıklama: "QR'ı okut, bağlantıyı telefondan yapıştır") ve **Kumandayla ekle**. | Telefonla ekle | Seçilen yola geçer | Uygulamadan çıkış onayı (mevcut) |
| TV-001a | Telefonla ekle | Sol: 3 adımlık yönerge ("1. QR'ı okut 2. Bağlantıyı yapıştır 3. TV'de onayla"), altında "Kumandayla ekle" bağlantısı. Sağ: büyük QR (en az 280 dp), altında kısa adres (`http://192.168.1.126:8765`) ve 6 haneli kod (büyük, aralıklı). | Vazgeç | Vazgeç → TV-001 | TV-001 |
| TV-001b | TV'de onay | "Bu kaynağı ekle?" başlığı; kaynak adı ve türü; aynı 6 haneli kod; şifre ve kimlikli adres **gösterilmez**. Düğmeler: **Ekle** / Vazgeç. | Ekle | TV-004 | Vazgeç → TV-001a (yeni kod) |
| TV-002 | Kumandayla: tür | "Ne ekleyeceksin?" İki kart: **M3U bağlantısı** / **Xtream hesabı** (etiket "Xtream Codes" yerine; READINESS §3.2). | Son kullanılan tür, yoksa M3U | TV-002a / TV-003a | TV-001 |
| TV-002a | M3U bağlantısı | Tek alan, tam genişlik, açık metin. Üstte kısa ipucu: "Sağlayıcının verdiği liste bağlantısı. Telefondan da yapıştırabilirsin." | Alan | Klavye açılır (D058); klavyede Bitti → TV-002b | TV-002, metin korunur |
| TV-002b | Ad (isteğe bağlı) | Tek alan, varsayılan "Evim". Altında "XMLTV rehber adresi ekle" isteğe bağlı bağlantısı (TV-002c). | **Bağlan** düğmesi (alan ikinci odak) | TV-004 | TV-002a |
| TV-003a–c | Xtream: sunucu → kullanıcı adı → şifre | Her biri tek alan; şifre açık metin (D116), ekranda "Gizle" anahtarı. | Alan | Klavyede Bitti → sonraki adım; şifreden sonra TV-002b (ad) | Önceki adım, metin korunur |
| TV-004 | Bağlanıyor | Sol: kaynak adı ve "Bağlanıyor…"; ilerleme halkası (`LoadingArc`) ve aşama metni ("Liste alınıyor", "Kanallar hazırlanıyor"). Sağ: boş; tamamlanınca sonuç kartı. | İptal | — | İptal onayı |
| TV-004a | Hazır | "Hazır!" + sonuç: "128 kanal · 2.400 film · 310 dizi". Kısmi sonuçta eksik bölüm bir satırla açıklanır ("Diziler alınamadı, sonra tekrar denenecek"). Düğme: **Kütüphaneyi aç**; ikinci: "Bir kaynak daha ekle". | Kütüphaneyi aç | TV-010 | Kütüphaneyi aç ile aynı |
| TV-004b | Hata | Tek cümle hata ("Sunucuya ulaşılamadı. Adresi ve ağ bağlantısını kontrol et."), teknik kod küçük ve soluk. Düğmeler: **Tekrar dene** / Bilgileri düzelt / Vazgeç. | Tekrar dene | Yeniden bağlanır | Bilgileri düzelt |

**Durumlar (her ekran için):**
- **Yükleme:** TV-004 halkası; D069 hareketi azalt ile durağan.
- **Boş:** alan boşken Bitti/Bağlan devre dışı değil, basılınca alanın altında kısa uyarı ("Bağlantı gerekli") ve odak alanda kalır.
- **Hata:** TV-004b; ağ yok, 401/403 ("Kullanıcı adı ya da şifre yanlış"), boş katalog, zaman aşımı ayrı cümlelerle (mevcut `FailureReason` eşlemesi).
- **Telefon yolu:** kodun süresi 5 dk; dolunca TV-001a'da "Kodun süresi doldu" ve **Yeni kod**. Telefon ağa erişemezse TV-001a altındaki "Kumandayla ekle" her zaman görünür. Aynı ağda değilse telefon sayfası açılmaz; yönergede "Telefon ve TV aynı Wi-Fi'da olmalı" yazar.
- **Erişim kısıtı:** kaynak eklemek PIN gerektiriyorsa (D074 kaynak yönetimi kilidi) akış başlamadan önce mevcut PIN ekranı gelir.

**Klavye kuralı:** Tek alanlı ekranlarda alan ekranın üst üçte birinde durur; klavye açılınca alan görünür kalır (IME ekleme/`imePadding` ve `BringIntoViewRequester`). Mevcut çok alanlı kartta bu sağlanamıyordu.

**Metin kuralları:** Ham hata kodu ve belirteç gösterilmez. "Xtream Codes" etiketi kullanılmaz. Kimlik bilgisi ekran görüntüsüne girmez; sentetik örnekler kullanılır. Metinler `values` ve `values-tr`'de.

**TalkBack:** QR "Telefonla kurulum için QR kodu" diye okunur; kod rakam rakam okunur; sonuç ve hata polite live region.

## Telefonla ekleme: nasıl çalışır (öneri)

- TV, "Telefonla ekle" seçilince yerel bir HTTP sunucusu açar (ev ağı, rastgele port, yalnız bu ekran açıkken; `appStopped`'da kapanır). PLAN S43'teki yerel aktarım sunucusuyla aynı bileşen kullanılır.
- QR içeriği: `http://<TV-IP>:<port>/#<oturum>`; kısa adres ve kod ekranda ayrıca yazılır. Kod 6 hane, 5 dakika geçerli, tek kullanımlık, deneme sınırlı.
- Telefon sayfası (TV'den sunulur; TR/EN, `Accept-Language`): "Bağlantı yapıştır" ya da "Sunucu, kullanıcı adı, şifre" seçimi; kaynak adı; **TV'ye gönder**. Sayfa, TV'nin kodunu gösterir. Pano arka planda okunmaz; yapıştırma kullanıcının eylemidir.
- Gönderim TV'ye POST ile gider; TV, TV-001b'de onay ister. Onaydan sonra sağlayıcıya yalnız TV bağlanır. Şifre TV ekranında görünmez, telefon sayfasında tutulmaz.
- Sınırlar: aynı Wi-Fi şart; ev ağında HTTP (dışarı çıkmaz, gizlilik politikasında yazılır); TV IP'si değişirse QR her açılışta yeniden üretilir. Bulut eşleştirme (ONBOARDING'in HTTPS servisi) ileride hesap sunucusu gelince eklenebilir; bu taslak onu gerektirmez.
- Güvenlik: QR'da kimlik bilgisi yok; aynı ağdaki başka biri sayfayı açsa bile TV onayı olmadan kaynak eklenemez ve ekranda kod eşleşmesi aranır.

## Kodlama planı (öneri, tahmin ölçülmedi)

| # | İş | Dosyalar | Boy |
|---|---|---|---|
| 1 | Karşılama ve tek alanlı adım ekranları (TV-001, 002, 002a–b, 003a–c), adım göstergesi, klavye görünürlüğü | `ui/SetupFlow.kt` (yeni), `ui/PilotApp.kt` (Setup yerine), `strings.xml` ×2 | 2 g |
| 2 | Bağlanıyor / Hazır / Hata ekranları, kısmi sonuç metni | `ui/SetupFlow.kt`, `TvViewModel.kt` (mevcut `connectDraft` ve içe aktarma durumu) | 1 g |
| 3 | Yerel sunucu, oturum kodu, telefon sayfası (TR/EN), QR üretimi | `data/setup/LocalSetupServer.kt`, `assets/setup/index.html`, `ui/QrCode.kt`, `build.gradle.kts` (qrcodegen; sürüm uygulama günü doğrulanır) | 2 g |
| 4 | TV-001a/001b ekranları ve akış bağlama, zaman aşımı, iptal | `ui/SetupFlow.kt`, `TvViewModel.kt` | 1 g |
| 5 | Testler: JVM (oturum kodu, süre, tek kullanım; sunucu loopback), cihaz (adım akışı, Back/odak, hata, QR ekranı, onay), görsel kontrol | `src/test`, `src/androidTest` | 1,5 g |
| 6 | SCREENS, MOTION, ONBOARDING, STATUS/HANDOFF, DECISIONS | belgeler | 0,5 g |

Toplam yaklaşık 8 iş günü. Mevcut Ayarlar → Kaynaklar → "Kaynak ekle" aynı akışı kullanır (TV-070'ten açılır, Back ayarlara döner).

## Kullanıcının kararları

1. Taslak onaylanıyor mu; "Telefonla ekle" ilk seçenek mi olsun?
2. Telefon yolu ilk sürümde mi (8 g), yoksa önce kumanda akışı (1, 2, 5, 6: yaklaşık 5 g) sonra telefon mu?
3. Şifre alanı açık metin (D116) mi kalsın, yoksa Xtream şifresi için varsayılan gizli + "Göster" mi?
4. Sonuç ekranında "Bir kaynak daha ekle" olsun mu?

## Uygulamadaki farklar (D117)

- Karar 1–4 önerildiği gibi uygulandı: Telefonla ekle ilk seçenek, telefon yolu ilk sürümde, şifre açık metin ve "Şifreyi gizle", sonuçta "Bir kaynak daha ekle".
- Sunucu `data/PhoneSetupServer.kt`, eşleştirme ve form `data/PhoneSetup.kt` içindedir; sayfa dili `Accept-Language` yerine tarayıcının dilinden seçilir.
- Bağlanıyor ekranı (TV-004) mevcut yükleme kartıdır; "Hazır" (TV-004a) ana sayfanın ya da Kaynaklar'ın üstünde bir pencere olarak açılır.
- Adım göstergesi yalnız kumanda yolundadır. Kayıtlı kaynağı düzenleme eski tek form ekranında kalır.
- TV-001b'de sunucunun yalnız alan adı gösterilir; bağlantının kendisi ve şifre gösterilmez.
