# antenio.app: gizlilik sayfası ve destek e-postası (GoDaddy ile)

**Durum (23 Ekim 2026, 11:20):** Site yayında: https://antenio.app, https://antenio.app/gizlilik, https://antenio.app/privacy (Netlify projesi `antenio`, herkese açık, Let's Encrypt sertifikası 21.01.2027'ye kadar, Netlify kendisi yeniler). GoDaddy DNS: A `@` → 75.2.60.5, CNAME `www` → antenio.app (Netlify www'yi ana adrese yönlendiriyor), MX ×2 ve SPF TXT yayında (GoDaddy sunucusunda ve 1.1.1.1'de doğrulandı). ImprovMX'te `support` takma adı kullanıcı tarafından kuruldu (23 Ekim; adres destek@ yerine support@antenio.app oldu). Kalan: deneme e-postası. Site tek klasördür (`gizlilik.html`, `privacy.html`, `_redirects`); güncelleme için klasörün zip'i Netlify Drop'a ya da projenin Deploys sayfasına bırakılır.

23 Ekim 2026 · Kullanıcı: "godaddy üzerinden devam edelim". Alan adı ve DNS GoDaddy'de kalır. Sayfalar ücretsiz Netlify'da, support@antenio.app ücretsiz ImprovMX ile kişisel e-postaya yönlenir. Hesap açmak, DNS kaydı değiştirmek ve yayımlamak kullanıcının işidir. LAUNCH_AUDIT E4, BKL-043.

**Neden bu yol:** GoDaddy'nin ücretsiz site planı alan adı bağlamaya izin vermiyor; bağlanan planlar aylık ücretli. GoDaddy'nin e-posta yönlendirmesi hesaba göre ücretsiz kredi ya da ücretli e-posta paketi istiyor. `.app` tarayıcıda HTTPS zorunlu kılar; Netlify sertifikayı (Let's Encrypt) kendisi alır.

## 0. Önce

- GoDaddy hesabında iki adımlı doğrulamayı ve antenio.app için otomatik yenilemeyi aç.
- Sayfalarda kişi adı yoktur (kullanıcı kararı): Google'ın kuralı, Play'deki geliştirici adının ("Antenio") ya da uygulama adının politikada geçmesini ister; iletişim support@antenio.app'tir. Metin uygulamanın 0.15.0'daki davranışına göre yazıldı ve tarihi 23 Ekim 2026'dır; yayından önce avukat kontrolü önerilir (BKL-043). Abonelik ya da hesap geldiğinde sayfa önceden güncellenmeli ve o zaman bir veri sorumlusu (şirket) adı gerekebilir.
- Play'de geliştirici adı "Antenio" seçilmeli. Kişisel hesap yasal adı Play'de gösterir; kullanıcı bunu kabul etti (D120).

## 1. Sayfaları Netlify'da yayımla

1. app.netlify.com adresinde ücretsiz hesap aç.
2. "Add new project" → "Deploy manually" ekranına `apps/web` klasörünü sürükle bırak. Site bir `*.netlify.app` adresiyle açılır.
3. Site ayarlarından adı `antenio` gibi bir şeye çevir (adres `antenio.netlify.app` olur; doluysa başka bir ad).
4. Domain management → Add a domain → `antenio.app`. Netlify alan adının başka bir yerde (GoDaddy) yönetildiğini söyler ve aşağıdaki kayıtları ister.
5. GoDaddy → antenio.app → DNS:
   - `@` için var olan A kayıtlarını (ör. "Parked") sil, yenisini ekle: **Tür A, Ad `@`, Değer `75.2.60.5`**.
   - `www` kaydını düzenle: **Tür CNAME, Ad `www`, Değer `antenio.netlify.app`** (kendi Netlify adresin).
   - GoDaddy'de "Forwarding" (yönlendirme) açıksa kapat.
6. Netlify → Domain management → HTTPS: "Verify DNS configuration", ardından sertifika kendiliğinden alınır. DNS yayılımı çoğunlukla bir saatten kısa, en çok 48 saat sürer.
7. Dene: `https://antenio.app/gizlilik` ve `https://antenio.app/privacy` kilit simgesiyle açılmalı (sonda `/` olursa aynı sayfaya yönlenir). Netlify Drop'a bırakılan site önce "yalnız ekip" görünür; "Make public" ile açılmadan Let's Encrypt sertifikası alınamaz.

Sayfa değişirse aynı ekranda klasörü yeniden bırakmak yeni sürümü yayımlar.

## 2. support@antenio.app yönlendirmesi (ImprovMX)

1. improvmx.com'da alan adına `antenio.app`, hedefe kişisel e-postanı yazıp ücretsiz hesap aç (ücretsiz plan: 1 alan adı, 25 takma ad).
2. Takma ad ekle: `support` → kişisel e-postan (kullanıcı `support` seçti). İstersen `*` (her şey) da eklenebilir.
3. GoDaddy → antenio.app → DNS:
   - Var olan bütün MX kayıtlarını sil.
   - **MX, Ad `@`, Öncelik 10, Değer `mx1.improvmx.com`**
   - **MX, Ad `@`, Öncelik 20, Değer `mx2.improvmx.com`**
   - **TXT, Ad `@`, Değer: ImprovMX panelinin gösterdiği SPF değeri** (genelde `v=spf1 include:spf.improvmx.com ~all`; paneldekini kopyala).
4. ImprovMX paneli "active" gösterince başka bir adresten support@antenio.app'e deneme e-postası at; spam klasörüne de bak.

Ücretsiz plan yalnız gelen postayı yönlendirir. support@antenio.app adına yanıt göndermek (SMTP) ücretli plan ya da başka bir e-posta hizmeti ister; Play için gelen posta yeterlidir.

## 3. Sonra

- Play Console: gizlilik politikası adresi `https://antenio.app/privacy` (İngilizce, Türkçeye bağlantı içerir), web sitesi `https://antenio.app`, iletişim e-postası `support@antenio.app`.
- Uygulama: Ayarlar → Uygulama hakkında'da gizlilik politikasının QR kodu ve adresi var (0.15.1; Türkçe arayüzde /gizlilik, İngilizcede /privacy).
- Kullanım koşulları ve telif bildirim sayfası aynı klasöre eklenebilir (BKL-043).
