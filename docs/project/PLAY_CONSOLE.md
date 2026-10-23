# Play Console: ilk kapalı test için cevaplar

23 Ekim 2026 · Kullanıcı: "Mağaza kaydı, görseller ve inceleme için test kaynağını hazırlayalım; kapalı teste yükleyelim." Hesap kişisel, geliştirici adı Antenio (D120). Aşağıdakiler öneridir; beyan kullanıcınındır ve "İncelemeye gönder"den önce kullanıcı onaylar. Metinler [STORE_LISTING_EN](../product/STORE_LISTING_EN.md) ve [STORE_LISTING_TR](../product/STORE_LISTING_TR.md); görseller `design/store` (`tools/make_store_graphics.py`), ekran görüntüleri `design/store/screenshots` (emülatör 5558, 1920×1080, inceleme kaynağıyla).

## Durum (23 Ekim 2026, 18:00)

Kullanıcı (16:20): "Dört beyanı onaylıyorum: hedef kitle 18+, veri toplanmıyor, sınırsız internet içeriği evet, varsayılan dil İngilizce ve Türkçe çeviri." Kullanıcı (17:40): "1 evet, 2 evet, 3 etiketleme" (IARC şartları, Android TV dağıtımı ve inceleme politikası, yapay zekâ öğe beyanı: etiketleme).

- Uygulama: Antenio IPTV Player, `app.antenio.tv`, en-US + tr-TR, uygulama, ücretsiz; kategori Video Oynatıcılar ve Düzenleyiciler; iletişim support@antenio.app, https://antenio.app (telefon yok). Oluştururken üç beyan kullanıcının onayıyla işaretlendi.
- Form faktörü: Android TV eklendi ve dağıtımı etkinleştirildi (TV kalite yönergeleriyle incelenir); Android XR kendiliğinden etkin.
- Mağaza kaydı kaydedildi ("incelenmek üzere göndermeye hazır"): metinler, ikon, tanıtım görseli, TV afişi, 7 TV ekran görüntüsü; Play en az 2 telefon/tablet görüntüsü istediği için telefon bölümüne de 4 TV görüntüsü kondu (uygulamanın telefon hâli yok). Yapay zekâ beyanı: etiketleme.
- Uygulama içeriği tamam: gizlilik politikası; reklam yok; uygulama erişimi (incelemeci talimatı, 500 karakter sınırı içinde); içerik derecelendirmesi; hedef kitle 18+; veri güvenliği (veri toplanmıyor/paylaşılmıyor); reklam kimliği yok; resmi kurum değil; finans yok; sağlık yok.
- İçerik derecelendirmesi (IARC, e-posta support@antenio.app, "Diğer tüm uygulama türleri"): paket içinde derecelendirmeye konu içerik yok; kullanıcılar arası paylaşım yok; uygulama dışarıdaki içeriği öne çıkarmıyor/tanıtmıyor (yalnız kullanıcının eklediği kaynağı oynatır; Netflix gibi bir katalog yok); yaş kısıtlı ürün tanıtımı yok; konum paylaşımı, dijital satın alma, para ödülü yok; web tarayıcısı ya da arama motoru değil; haber/eğitim ürünü değil. Sonuç: bütün bölgelerde "tüm yaşlar". Ankette "sınırsız internet" adlı bir soru yoktu; en yakını tarayıcı sorusuydu. **Abonelik gelince "dijital ürün satın alma" cevabı "evet" olarak yeniden gönderilmeli.**
- Kapalı test - Alpha (18:30): Kullanıcı: "Ülke olarak Türkiye seç." Ülke Türkiye; test kullanıcıları "Antenio testers" e-posta listesi (şimdilik yalnız hesap sahibi); geri bildirim support@antenio.app. Sürüm "109 (0.16.1)" önizlendi ve kaydedildi; tek uyarı yerel kod için hata ayıklama sembolü yok (engel değil). Kullanıcı (18:45): "Kapalı testin ülkelerini bütün ülkeler yap ve 16 değişikliği incelemeye gönder." Ülkeler: 177 ülke + dünyanın geri kalanı. **17 değişiklik incelemeye gönderildi (23 Ekim 2026, 18:50)**; Google'a göre inceleme genellikle 7 gün içinde biter. Aynı akşam kapalı test sürümü yayına girdi (kanal "Etkin", 178 ülke/bölge). Katılma bağlantıları: web https://play.google.com/apps/testing/app.antenio.tv, Android https://play.google.com/store/apps/details?id=app.antenio.tv. Testçi önce "Antenio testers" listesinde olmalı (adresler yalnız Play Console'da tutulur, depoya yazılmaz), sonra bağlantıyı aynı Google hesabıyla açıp teste katılmalı. Kontrol paneli: "an itibarıyla 0 test kullanıcısı kayıtlı"; listede 2 adres var. Kullanıcı: "Evet" (Google Grubu). Aynı akşam "Antenio Testers" grubu (antenio-testers@googlegroups.com, herkes katılabilir, üyeleri/iletileri yalnız yönetici görür) kullanıcının hesabıyla açıldı (CAPTCHA'yı kullanıcı geçti) ve kapalı testin test kullanıcıları e-posta listesinden bu gruba çevrilip gönderildi (Play'de ikisi birlikte kullanılamıyor). Oturumsuz bakışta grup sayfasında sahibin adı görünmüyor. Mesaj ve gönderi metinleri: [TESTERS](TESTERS.md).
- Test kullanıcıları: kullanıcı aile ve arkadaşlardan hesap toplayacak, eksik kalırsa test takası toplulukları (kullanıcı: "test takası topluluklarından yardım almak istiyorum"). Dikkat: manifest `android.software.leanback` zorunlu, telefonlar uygulamayı kuramaz; topluluk testçileri katılabilir ama çoğu kuramaz. Takas için kullanıcının da bir Android telefonla başkalarının uygulamalarını 14 gün test etmesi beklenir. Yurt dışından testçi gelirse kanalın ülkeleri genişletilmeli. Topluluklarda paylaşım için Google Grubu e-posta listesinden kolaydır (testçi kendisi katılır).

## Kapalı test sayacı

23 Ekim 2026 gece: kontrol panelinde "Kapalı test sürümü yayınlayın" ve "En az 12 test kullanıcısının kapalı testinize kaydolmasını sağlayın" işaretli; kalan "En az 12 test kullanıcısıyla en az 14 gün boyunca kapalı test yapın". Grup: 13 üye. En erken üretim başvurusu 6 Kasım 2026 civarı. Başvuruda Google testle ilgili sorular soruyor (soruları kontrol panelinden önizlenebilir); test sırasında testçi geri bildirimine dayanan bir güncelleme yayımlamak cevapları güçlendirir.

## Uygulama oluşturma

- Uygulama adı: Antenio IPTV Player · varsayılan dil: English (United States) · uygulama · ücretsiz.
- Paket: `apps/android-tv/app/build/outputs/bundle/release/app-release.aab` (0.16.1, 109; imza CN=Antenio). 0.16.0 yüklenmedi; kullanıcının bildirdiği kategori noktası hatası düzeltildi.
- İlk güncelleme "110 (0.16.2)": Kullanıcı: "İlk açılışta boş kalan afiş hatasını düzelt; testte güncelleme olarak yayınlayalım." Kapalı test - Alpha'da 2. sürüm olarak oluşturuldu; sürüm notu en-US "Posters and channel logos that failed to load on the first screen now load again on their own.", tr-TR "İlk ekranda yüklenemeyen afişler ve kanal logoları artık kendiliğinden yeniden yükleniyor." Tek uyarı yine hata ayıklama sembolü (engel değil); desteklenen cihaz sayısı değişmedi (TV 3.047). **23 Ekim 2026 gece incelemeye gönderildi** (Yayın özeti "İncelenmekte olan değişiklikler").
- Uygulama imzalama: Google'ın ürettiği anahtarla Play App Signing (varsayılan). Bizim anahtar yalnız yükleme anahtarıdır.

## Mağaza kaydı

- Kategori: Video Players & Editors. E-posta support@antenio.app, web sitesi https://antenio.app, telefon yok.
- Gizlilik politikası: https://antenio.app/privacy
- Görseller (23 Ekim akşamı yenilendi; kullanıcı: "Sadelik ve genel görünüm çok basit göründü", "İkisi birden"): `icon-512.png`, yeni `feature-1024x500.png` (afiş yelpazesi); Android TV: `tv-banner-1280x720.png` ve `screenshots/01…08` (sentetik vitrinle, değiştirilmemiş); telefon: `frames/en/*` (başlıklı), tr-TR kaydında `frames/tr/*`. Kullanıcı: "gönder". 4 değişiklik (en-US telefon, en-US Android TV, en-US tanıtım görseli, tr-TR telefon) 23 Ekim gece incelemeye gönderildi. Yapay zekâ beyanı yine "etiketleme" (görseller kodla çizildi). Telefon ekran görüntüsü yok: uygulama yalnız TV'de çalışır, Console telefon görüntüsü isterse TV görüntüleri kullanılmaz, kullanıcıya sorulur.
- Çeviri: Türkçe (tr-TR), aynı görseller.

## Uygulama içeriği (App content)

| Form | Önerilen cevap | Not |
|---|---|---|
| Gizlilik politikası | https://antenio.app/privacy | |
| Reklamlar | Reklam yok | |
| Uygulama erişimi | Bazı işlevler için talimat gerekir (aşağıdaki metin) | Giriş yok; kaynak eklenmeden kütüphane boş |
| İçerik derecelendirmesi | Kategori: diğer uygulama türleri; şiddet, cinsellik, dil, madde, kumar yok; kullanıcılar arası etkileşim yok; konum paylaşımı yok; dijital satın alma yok (0.16.0); **sınırsız internet içeriği: evet** | Uygulama kullanıcının eklediği her yayını oynatır; dürüst cevap evet, derece yükselebilir |
| Hedef kitle | Yalnız 18 ve üstü | Kaynaklarda yetişkin içerik olabilir; çocuklara yönelik değil |
| Veri güvenliği | Veri toplanmıyor ve paylaşılmıyor | Gri alan: kaynak bilgileri kullanıcının seçtiği sunucuya gider, geliştiriciye gelmez (PLAY_STORE_READINESS §2.2); politika bunu anlatır |
| Haber uygulaması | Hayır | |
| Devlet uygulaması | Hayır | |
| Finansal özellikler | Yok | |
| Sağlık | Yok | |
| Form faktörü | Android TV eklenir, TV inceleme koşulları kabul edilir | Kabul kullanıcının |

## Uygulama erişimi metni (incelemeci için)

```
Antenio is a player and needs a source; no account or login. Please use this open-licensed review list:
1. On the first screen choose "Add with the remote", then "M3U link".
2. Press OK, type https://antenio.app/review/demo.m3u and press Next.
3. Keep the name and press Connect, then "Open library".
The list has 4 live channels with a test TV guide, 3 movies and 1 series: open movies by the Blender Foundation (CC BY) served from download.blender.org, and public HLS test streams from Apple, Mux and Unified Streaming.
```

Not: Akamai canlı test yayını 23 Ekim'de bozuldu (alt listeler 404); listede yerini Apple'ın bipbop 16:9 örneği aldı ve site yeniden yüklendi. Play Console'daki talimat metni hâlâ "Akamai" adını anıyor; bir sonraki gönderimde düzeltilebilir.
```
```

## Kapalı test

- Kanal: Kapalı test (Closed testing), test kullanıcıları e-posta listesiyle; en az 12 kişi, 14 gün kesintisiz (yeni kişisel hesap kuralı).
- Ülkeler: kullanıcı seçer (öneri: bütün ülkeler ya da test kullanıcılarının ülkeleri).
- Geri bildirim adresi: support@antenio.app
- Sürüm notu (en-US): "First test release: add a source with your phone or remote, live TV with guide, movies and series." · (tr-TR): "İlk test sürümü: kaynağı telefonla ya da kumandayla ekle, rehberli canlı TV, filmler ve diziler."

## İnceleme kaynağı

`apps/web/review` (`tools/make_review_source.py`): `demo.m3u`, 75 günlük `epg.xml`, kendi çizdiğimiz afiş ve logolar. Uygulama bu listeyi hiçbir yerde önermez (PLAY_STORE_READINESS §3.2); `_headers` arama motorlarına kapatır. Rehber 75 gün sonra biter: inceleme uzarsa betik yeniden çalıştırılır ve site yeniden yüklenir.
