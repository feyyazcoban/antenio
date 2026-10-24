# Senin yapacakların (Play yayınına kadar)

23 Ekim 2026 · Kullanıcı: "her yapmam gerekeni madde madde yaz email dahil". Sıra bağımlılığa göredir: bir adım öncekine dayanıyorsa sonra gelir. Ayrıntılı DNS adımları [WEB_SETUP](WEB_SETUP.md), açık maddelerin tamamı [LAUNCH_AUDIT](LAUNCH_AUDIT.md). Kod tarafında yapacaklarım en sonda.

## 1. Bu hafta: hesap güvenliği, e-posta ve site

1. **GoDaddy (ertelendi):** Kullanıcı (23 Ekim): "şimdilik gerek yok". Bekleyen: hesapta iki adımlı doğrulama, antenio.app için otomatik yenileme, ikinci yıl yenileme fiyatı. Alan adı süresi dolmadan yenilenmeli; site, e-posta ve Play'deki adresler ona bağlı.
2. ~~**Kodu yedekle**~~ **Yapıldı (23 Ekim):** GitHub'daki özel depo `origin` olarak eklendi; main ve çalışma dalı gönderildi. Deponun özel (private) kaldığından emin ol.
3. ~~**support@antenio.app (ImprovMX)**~~ **Yapıldı (23 Ekim):** DNS kayıtları, `support` takma adı ve başka bir adresten deneme e-postası tamam; posta kişisel kutuna geliyor. Açık: ücretsiz plan yalnız gelen postayı yönlendirir; Gmail'den verilen yanıt kişisel adresten ve Gmail'deki görünen adla gider (gerçek ad görünebilir). support@antenio.app adına yanıt için ImprovMX ücretli planı (SMTP) ya da başka bir e-posta hizmeti gerekir; kullanıcılara yanıt vermeye başlamadan karar verilmeli.
   1. improvmx.com → alan adı `antenio.app`, hedef kişisel e-postan → ücretsiz hesap.
   2. Takma ad: `support` → kişisel e-postan.
   3. GoDaddy → antenio.app → DNS: eski MX kayıtlarını sil; MX `@` öncelik 10 `mx1.improvmx.com`; MX `@` öncelik 20 `mx2.improvmx.com`; TXT `@` ImprovMX'in verdiği SPF değeri.
   4. Panel "active" olunca başka bir adresten deneme e-postası at; spam klasörüne de bak.
4. ~~**antenio.app sitesi (Netlify)**~~ **Yapıldı (23 Ekim):** site https://antenio.app'te yayında, HTTPS açık; GoDaddy'de A, MX ve SPF kayıtları girildi. Aşağıdaki alt adımlar kayıt içindir.
   - **antenio.app sitesi (Netlify):**
   1. app.netlify.com → ücretsiz hesap → "Deploy manually" → `C:\Users\fyzcb\Desktop\TV\apps\web` klasörünü sürükle bırak.
   2. Site adını `antenio` gibi yap; Domain management → Add a domain → `antenio.app`.
   3. GoDaddy DNS: `@` için eski A kayıtlarını sil, A `@` → `75.2.60.5`; CNAME `www` → `<site-adın>.netlify.app`; yönlendirme (Forwarding) açıksa kapat.
   4. Netlify → HTTPS → doğrula; sertifika kendiliğinden gelir (çoğunlukla bir saat, en çok 48 saat).
   5. Dene: https://antenio.app/privacy/ ve https://antenio.app/gizlilik/ kilit simgesiyle açılmalı. Açılınca bana söyle; uygulamaya bağlantıyı eklerim.
5. ~~**TV:** kendi kaynağınla deneme~~ **Tamam (23 Ekim):** Kullanıcı: "burası okey"; sorun bildirilmedi.

## 2. Marka ve hukuk

6. **Marka (yayın için şart değil; karar senin):** Kullanıcı: "gerek var mı gerçekten". Ön taramada birebir "Antenio" kaydı yok; AB'de yazılım sınıfında benzer iki tescil var (ANTENNO, anterio). Tescil, adı başkasının almasına karşı korur; tescilsiz kullanımda benzer bir markanın sahibi itiraz ederse Play adı değiştirtebilir. Kişi olarak yapılan başvuruda ad ve adres marka sicilinde herkese açık görünür. Öneri: şimdilik ertele; gelir başlayıp şirket kurulursa şirket adına başvur.
7. ~~**Avukat**~~ Kullanıcı (23 Ekim): "bunları sordum sıkıntı yok gibi duruyor". Kullanım koşulları sayfası henüz yok; abonelik kodlanırken yazılacak.
8. **Mali müşavir (artık abonelik kurulmadan önce):** Kullanıcı (23 Ekim): "para kazandığımızda sorarım bunu". D121 ile üretime abonelikle çıkılacağı için Play'de ödeme profili ve banka hesabı abonelik ürünü oluşturulmadan önce gerekir; görüşme önümüzdeki 2–3 haftada olmalı. Dikkat: 20/B istisnası yalnız bu iş için açılan hesaba gelen paraya uygulanır; Google'dan ilk ödeme gelmeden önce sorulmalı ve ödeme profilindeki banka hesabı (11. madde) buna göre seçilmeli. Kullanıcı: "gelir üzerinden vergiyi google ödemiyor mu". Google Türkiye'deki alıcılardan KDV'yi tahsil edip öder; geliştiricinin kazancının gelir vergisi geliştiriciye aittir. GVK mükerrer 20/B (mobil uygulama geliştiriciliği istisnası): vergi dairesinden istisna belgesi, Türkiye'deki bir bankada yalnız bu iş için açılan hesap, banka gelen paradan %15 keser; yıllık sınırın altında beyanname ve defter yok; 2026 sınırı 5.300.000 TL (danışmanlık sitesi kaynağı, GİB'den doğrulanmalı). Müşavire sorulacak: Android TV uygulaması kapsamda mı, belge nasıl alınır, sınır aşılırsa ne olur.
9. **Logo:** tasarımcıdan Antenio logosu ve ikon (şu an geçici piksel yazı var); lisans ya da devir belgesini sakla.

## 3. Google Play hesabı (D120: kişisel hesap)

10. ~~**Play Console hesabı**~~ **Yapıldı (23 Ekim):** kişisel hesap, geliştirici adı Antenio; e-posta support@antenio.app, web sitesi https://antenio.app. Kullanıcı: "console onayını aldım doğrulama da tamam" (hesap onayı ve Android cihaz doğrulaması). Kayıt için adımlar: play.google.com/console → kişisel hesap; tek seferlik kayıt ücreti; kimlik doğrulaması. Geliştirici adı "Antenio", iletişim e-postası support@antenio.app (önce 3. adım çalışmalı), web sitesi https://antenio.app (önce 4. adım).
11. **Ödeme profili** (banka hesabı için 8. maddeye bak): satış için Google ödeme (satıcı) profili ve banka hesabı; tam adres Play'de görünür (kabul edildi).
12. ~~**12 test kullanıcısı**~~ **Tamam (23 Ekim gece):** kullanıcı: "12 kişiyi buldum"; Play Console'da "en az 12 test kullanıcısı kaydolsun" maddesi işaretli, grupta 13 üye. 14 günlük süre başladı; en erken üretim başvurusu 6 Kasım 2026 civarı. Sayı 12'nin altına düşmemeli (kimse gruptan/testten çıkmasın; 1–2 yedek iyi olur). Eski not: Google hesabı olan en az 12 kişi; 14 gün kesintisiz kapalı testte kayıtlı kalacaklar. Kullanıcı (23 Ekim): "kapalı test için 12 kişi bulamayacağım nasıl aşarız bunu". Seçenekler (öneri sırası): TV'si olmayan aile/arkadaş da Google hesabıyla katılıp sayılır; eksik kalanlar için test takası toplulukları; ücretli test hizmetleri (gri alan); kuruluş hesabı (şirket + D-U-N-S) bu şartı tümden kaldırır ama şirket kurmayı gerektirir.
13. ~~**Yükleme anahtarı (upload key)**~~ **Yapıldı (23 Ekim):** anahtarı kendi Terminal sekmende oluşturdun ve şifreyi gradle.properties'e yazdın. Not: dosya değişince uygulama içeriğini Claude'a gösterdi, şifre o oturumda Claude'un önüne geldi; hiçbir belgeye, commit'e ya da belleğe yazılmadı. Dosya `C:\Users\fyzcb\antenio-anahtar\antenio-upload.jks` olacak (takma ad `antenio-upload`, sertifikada yalnız "Antenio"). Şifreyi `C:\Users\fyzcb\.gradle\gradle.properties` dosyasındaki iki boş satıra kendin yaz. Anahtar dosyasını ve şifresini iki ayrı güvenli yerde sakla (kaybolursa Play'den sıfırlama istenir).

## 4. Benimle verilecek kararlar

14. **Abonelik (D121, 24 Ekim):** üretime abonelikle çıkılır; hesap yok, tek yıllık plan, ilk hafta kartsız ücretsiz, 8. gün abonelik. Ad ve fiyat (D122): "Antenio Premium", ABD'de yıllık 7,99 $; TL fiyatı ürün kurulurken birlikte konacak. Sıra: mali müşavir (8) → ödeme profili ve banka hesabı (11) → abonelikli sürüm kapalı teste → Play Console'da abonelik ürünü. Ayrıntı SUBSCRIPTION "24 Ekim güncellemesi".
15. ~~**Mağaza görselleri**~~ **Hazır (23 Ekim, akşam yenilendi):** `design/store`: 512×512 ikon, 1280×720 TV afişi, 1024×500 tanıtım görseli, 8 TV ekran görüntüsü (sentetik vitrinle) ve telefon bölümü için EN/TR başlıklı çerçeveler; incelemeye gönderildi. Metinler STORE_LISTING_EN ve STORE_LISTING_TR, form cevapları PLAY_CONSOLE.
16. ~~**İnceleme için yasal test kaynağı**~~ **Yayında (23 Ekim):** https://antenio.app/review/demo.m3u (Blender açık filmleri CC BY, herkese açık test yayınları, test rehberi); `apps/web/review`. Bozulan Akamai yayınının yerini Apple bipbop aldı.

## 5. Play Console'da uygulama (hesap ve anahtar hazır olunca)

17. ~~Uygulama oluştur~~ **Yapıldı (23 Ekim):** mağaza kaydı, beyanlar, Android TV ve kapalı test kuruldu; kapalı test yayında (ayrıntı PLAY_CONSOLE). 0.16.2 güncellemesi incelemede.
18. ~~Gizlilik, veri güvenliği, IARC, hedef kitle, reklam, TV form faktörü~~ **Yapıldı (23 Ekim).**
19. ~~Uygulama erişimi~~ **Yapıldı:** 16. maddedeki test kaynağı. Play'deki talimat metni hâlâ "Akamai" diyor; bir sonraki gönderimde düzeltilecek.
20. Kapalı test: ~~ilk paket~~, ~~12 kişi~~ tamam; 14 gün sürüyor. En erken üretim erişimi başvurusu 6 Kasım 2026 civarı.

## Benim yapacaklarım (kod)

- ~~Ayarlar → Uygulama hakkında'ya gizlilik bağlantısı~~ yapıldı (0.15.1: QR kodu ve adres).
- ~~Yükleme anahtarıyla `bundleRelease`~~ yapıldı: 0.16.0 (108), `apps/android-tv/app/build/outputs/bundle/release/app-release.aab`, imza CN=Antenio.
- ~~Mağaza görselleri için sentetik içerik~~ yapıldı (`tools/make_showcase_source.py`).
- ~~Lisans sayfası ve HTTP kaynak uyarısı~~ yapıldı (0.16.3, kullanıcı: "başla"); 24 Ekim gece kapalı teste yüklendi ve incelemeye gönderildi. Kalan: gizlilik politikasına HTTP cümlesi.
- Abonelik (BKL-048, D121): uygulama tarafı 0.17.0'da yazıldı, Play'e yüklenmedi. Kalan kod dışı işler: koşullar sayfası (antenio.app/terms, /kosullar), mağaza metni, IARC ve Data safety güncellemesi, gizlilik politikası; Play'de ürün ve lisans testi.
