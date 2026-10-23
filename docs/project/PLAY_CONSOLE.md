# Play Console: ilk kapalı test için cevaplar

23 Ekim 2026 · Kullanıcı: "Mağaza kaydı, görseller ve inceleme için test kaynağını hazırlayalım; kapalı teste yükleyelim." Hesap kişisel, geliştirici adı Antenio (D120). Aşağıdakiler öneridir; beyan kullanıcınındır ve "İncelemeye gönder"den önce kullanıcı onaylar. Metinler [STORE_LISTING_EN](../product/STORE_LISTING_EN.md) ve [STORE_LISTING_TR](../product/STORE_LISTING_TR.md); görseller `design/store` (`tools/make_store_graphics.py`), ekran görüntüleri `design/store/screenshots` (emülatör 5558, 1920×1080, inceleme kaynağıyla).

## Durum (23 Ekim 2026, 18:00)

Kullanıcı (16:20): "Dört beyanı onaylıyorum: hedef kitle 18+, veri toplanmıyor, sınırsız internet içeriği evet, varsayılan dil İngilizce ve Türkçe çeviri." Kullanıcı (17:40): "1 evet, 2 evet, 3 etiketleme" (IARC şartları, Android TV dağıtımı ve inceleme politikası, yapay zekâ öğe beyanı: etiketleme).

- Uygulama: Antenio IPTV Player, `app.antenio.tv`, en-US + tr-TR, uygulama, ücretsiz; kategori Video Oynatıcılar ve Düzenleyiciler; iletişim support@antenio.app, https://antenio.app (telefon yok). Oluştururken üç beyan kullanıcının onayıyla işaretlendi.
- Form faktörü: Android TV eklendi ve dağıtımı etkinleştirildi (TV kalite yönergeleriyle incelenir); Android XR kendiliğinden etkin.
- Mağaza kaydı kaydedildi ("incelenmek üzere göndermeye hazır"): metinler, ikon, tanıtım görseli, TV afişi, 7 TV ekran görüntüsü; Play en az 2 telefon/tablet görüntüsü istediği için telefon bölümüne de 4 TV görüntüsü kondu (uygulamanın telefon hâli yok). Yapay zekâ beyanı: etiketleme.
- Uygulama içeriği tamam: gizlilik politikası; reklam yok; uygulama erişimi (incelemeci talimatı, 500 karakter sınırı içinde); içerik derecelendirmesi; hedef kitle 18+; veri güvenliği (veri toplanmıyor/paylaşılmıyor); reklam kimliği yok; resmi kurum değil; finans yok; sağlık yok.
- İçerik derecelendirmesi (IARC, e-posta support@antenio.app, "Diğer tüm uygulama türleri"): paket içinde derecelendirmeye konu içerik yok; kullanıcılar arası paylaşım yok; uygulama dışarıdaki içeriği öne çıkarmıyor/tanıtmıyor (yalnız kullanıcının eklediği kaynağı oynatır; Netflix gibi bir katalog yok); yaş kısıtlı ürün tanıtımı yok; konum paylaşımı, dijital satın alma, para ödülü yok; web tarayıcısı ya da arama motoru değil; haber/eğitim ürünü değil. Sonuç: bütün bölgelerde "tüm yaşlar". Ankette "sınırsız internet" adlı bir soru yoktu; en yakını tarayıcı sorusuydu. **Abonelik gelince "dijital ürün satın alma" cevabı "evet" olarak yeniden gönderilmeli.**
- Kapalı test - Alpha (18:30): Kullanıcı: "Ülke olarak Türkiye seç." Ülke Türkiye; test kullanıcıları "Antenio testers" e-posta listesi (şimdilik yalnız hesap sahibi); geri bildirim support@antenio.app. Sürüm "109 (0.16.1)" önizlendi ve kaydedildi; tek uyarı yerel kod için hata ayıklama sembolü yok (engel değil). Yayın özetinde "16 değişikliği incelemeye gönder" hazır, **gönderilmedi** (kullanıcının son onayı bekleniyor).
- Test kullanıcıları: kullanıcı aile ve arkadaşlardan hesap toplayacak, eksik kalırsa test takası toplulukları (kullanıcı: "test takası topluluklarından yardım almak istiyorum"). Dikkat: manifest `android.software.leanback` zorunlu, telefonlar uygulamayı kuramaz; topluluk testçileri katılabilir ama çoğu kuramaz. Takas için kullanıcının da bir Android telefonla başkalarının uygulamalarını 14 gün test etmesi beklenir. Yurt dışından testçi gelirse kanalın ülkeleri genişletilmeli. Topluluklarda paylaşım için Google Grubu e-posta listesinden kolaydır (testçi kendisi katılır).

## Uygulama oluşturma

- Uygulama adı: Antenio IPTV Player · varsayılan dil: English (United States) · uygulama · ücretsiz.
- Paket: `apps/android-tv/app/build/outputs/bundle/release/app-release.aab` (0.16.1, 109; imza CN=Antenio). 0.16.0 yüklenmedi; kullanıcının bildirdiği kategori noktası hatası düzeltildi.
- Uygulama imzalama: Google'ın ürettiği anahtarla Play App Signing (varsayılan). Bizim anahtar yalnız yükleme anahtarıdır.

## Mağaza kaydı

- Kategori: Video Players & Editors. E-posta support@antenio.app, web sitesi https://antenio.app, telefon yok.
- Gizlilik politikası: https://antenio.app/privacy
- Görseller: `icon-512.png`, `feature-1024x500.png`; Android TV: `tv-banner-1280x720.png` ve `screenshots/01…07`. Telefon ekran görüntüsü yok: uygulama yalnız TV'de çalışır, Console telefon görüntüsü isterse TV görüntüleri kullanılmaz, kullanıcıya sorulur.
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
The list has 4 live channels with a test TV guide, 3 movies and 1 series: open movies by the Blender Foundation (CC BY) served from download.blender.org, and public HLS test streams from Akamai, Apple, Mux and Unified Streaming.
```

## Kapalı test

- Kanal: Kapalı test (Closed testing), test kullanıcıları e-posta listesiyle; en az 12 kişi, 14 gün kesintisiz (yeni kişisel hesap kuralı).
- Ülkeler: kullanıcı seçer (öneri: bütün ülkeler ya da test kullanıcılarının ülkeleri).
- Geri bildirim adresi: support@antenio.app
- Sürüm notu (en-US): "First test release: add a source with your phone or remote, live TV with guide, movies and series." · (tr-TR): "İlk test sürümü: kaynağı telefonla ya da kumandayla ekle, rehberli canlı TV, filmler ve diziler."

## İnceleme kaynağı

`apps/web/review` (`tools/make_review_source.py`): `demo.m3u`, 75 günlük `epg.xml`, kendi çizdiğimiz afiş ve logolar. Uygulama bu listeyi hiçbir yerde önermez (PLAY_STORE_READINESS §3.2); `_headers` arama motorlarına kapatır. Rehber 75 gün sonra biter: inceleme uzarsa betik yeniden çalıştırılır ve site yeniden yüklenir.
