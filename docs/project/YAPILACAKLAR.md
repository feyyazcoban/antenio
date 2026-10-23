# Senin yapacakların (Play yayınına kadar)

23 Ekim 2026 · Kullanıcı: "her yapmam gerekeni madde madde yaz email dahil". Sıra bağımlılığa göredir: bir adım öncekine dayanıyorsa sonra gelir. Ayrıntılı DNS adımları [WEB_SETUP](WEB_SETUP.md), açık maddelerin tamamı [LAUNCH_AUDIT](LAUNCH_AUDIT.md). Kod tarafında yapacaklarım en sonda.

## 1. Bu hafta: hesap güvenliği, e-posta ve site

1. **GoDaddy:** hesapta iki adımlı doğrulamayı aç; antenio.app için otomatik yenilemeyi aç; ikinci yıl yenileme fiyatına bak.
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
5. **TV:** Antenio'yu aç, kaynağını Xtream hesabı olarak yeniden ekle; logo, açılış ve afişte gördüğün sorunu yaz.

## 2. Marka ve hukuk

6. **Marka vekili:** TÜRKPATENT, EUIPO ve WIPO'da "Antenio" için sınıf 9 ve 42 araştırması; benzer kayıtlar ANTENNO ve anterio değerlendirilsin; temizse başvuru.
7. **Avukat:** gizlilik politikası (`apps/web`), kullanım koşulları ve abonelik metinleri kontrolü.
8. **Mali müşavir:** abonelik geliri, vergi kaydı ve Google'dan gelecek ödemeler.
9. **Logo:** tasarımcıdan Antenio logosu ve ikon (şu an geçici piksel yazı var); lisans ya da devir belgesini sakla.

## 3. Google Play hesabı (D120: kişisel hesap)

10. **Play Console hesabı:** play.google.com/console → kişisel hesap; tek seferlik kayıt ücreti; kimlik doğrulaması. Geliştirici adı "Antenio", iletişim e-postası support@antenio.app (önce 3. adım çalışmalı), web sitesi https://antenio.app (önce 4. adım).
11. **Ödeme profili:** satış için Google ödeme (satıcı) profili ve banka hesabı; tam adres Play'de görünür (kabul edildi).
12. **12 test kullanıcısı:** Google hesabı olan, mümkünse Android TV ya da Google TV cihazı olan en az 12 kişi bul; 14 gün kesintisiz testte kalacaklar.
13. **Yükleme anahtarı (upload key):** birlikte oluşturacağız; anahtar dosyasını ve şifresini iki ayrı güvenli yerde sakla (kaybolursa Play'den sıfırlama istenir).

## 4. Benimle verilecek kararlar

14. **Abonelik:** plan adı, dönem ve fiyat; hesabın ilk yayında olup olmayacağı (SUBSCRIPTION §1 ve §5).
15. **Mağaza görselleri:** 512×512 ikon, 1280×720 TV afişi, 1024×500 tanıtım görseli ve ekran görüntüleri; içerik sentetik olmalı.
16. **İnceleme için yasal test kaynağı:** Google incelemecisinin uygulamayı deneyebileceği, telif sorunu olmayan bir liste.

## 5. Play Console'da uygulama (hesap ve anahtar hazır olunca)

17. Uygulama oluştur: ad "Antenio IPTV Player", kategori, ücretsiz indirme.
18. Gizlilik politikası adresi https://antenio.app/privacy; veri güvenliği formu (veri toplanmıyor); içerik derecelendirmesi (IARC); hedef kitle (çocuklar değil); reklam yok; TV form faktörü.
19. Uygulama erişimi: 16. maddedeki test kaynağı.
20. Kapalı test kanalına ilk paketi (AAB) yükle, 12 kişiyi davet et, 14 gün bekle, sonra üretime erişim başvurusu.

## Benim yapacaklarım (kod)

- ~~Ayarlar → Uygulama hakkında'ya gizlilik bağlantısı~~ yapıldı (0.15.1: QR kodu ve adres).
- Yükleme anahtarıyla `bundleRelease` ve sürüm adından `-design-preview` ekinin kaldırılması.
- Abonelik ve 7 günlük deneme (BKL-048), lisans sayfası, HTTP kaynak uyarısı, mağaza görselleri için sentetik içerik.
