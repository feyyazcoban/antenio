# Play Console: ilk kapalı test için cevaplar

23 Ekim 2026 · Kullanıcı: "Mağaza kaydı, görseller ve inceleme için test kaynağını hazırlayalım; kapalı teste yükleyelim." Hesap kişisel, geliştirici adı Antenio (D120). Aşağıdakiler öneridir; beyan kullanıcınındır ve "İncelemeye gönder"den önce kullanıcı onaylar. Metinler [STORE_LISTING_EN](../product/STORE_LISTING_EN.md) ve [STORE_LISTING_TR](../product/STORE_LISTING_TR.md); görseller `design/store` (`tools/make_store_graphics.py`), ekran görüntüleri `design/store/screenshots` (emülatör 5558, 1920×1080, inceleme kaynağıyla).

## Durum (23 Ekim 2026, 17:00)

Kullanıcı (16:20): "Dört beyanı onaylıyorum: hedef kitle 18+, veri toplanmıyor, sınırsız internet içeriği evet, varsayılan dil İngilizce ve Türkçe çeviri."

- Uygulama oluşturuldu: Antenio IPTV Player, `app.antenio.tv`, en-US, uygulama, ücretsiz. Kullanıcı: "İşaretle" (Geliştirici Program Politikaları, Play Uygulama İmzalama Hizmet Şartları, ABD ihracat yasaları beyanları kullanıcının onayıyla işaretlendi).
- Form faktörü: Android TV eklendi (Android XR kendiliğinden etkin). Bekleyen: "Android TV'ye dağıtımı etkinleştir ve inceleme politikasını kabul et" (kullanıcı kabulü).
- Mağaza kaydı taslakta: en-US ve tr-TR metinleri, ikon, tanıtım görseli, TV afişi ve 7 TV ekran görüntüsü (sıra yüklemenin bitişine göre oldu; ilki filmler ekranı). Bekleyen: kaydın son adımındaki "yapay zekayla üretilmiş öğe beyanı" (kullanıcı kararı; görseller kodla çizildi ve gerçek ekran görüntüleri).
- Uygulama içeriği kaydedildi: gizlilik politikası, reklam yok, uygulama erişimi (incelemeci talimatı, "Review source (no login)"), hedef kitle 18+, veri güvenliği (veri toplanmıyor/paylaşılmıyor), reklam kimliği yok, resmi kurum değil, finans özelliği yok, sağlık özelliği yok. Bekleyen: içerik derecelendirmesi (IARC kullanım şartları kabulü kullanıcıda; e-posta support@antenio.app, kategori "Diğer tüm uygulama türleri").
- Kapalı test - Alpha: taslak sürüm "109 (0.16.1)", AAB ve en-US/tr-TR sürüm notları. Bekleyen: ülkeler, test kullanıcıları (12 e-posta), önizleme ve incelemeye gönderme.

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
