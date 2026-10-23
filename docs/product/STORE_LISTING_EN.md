# Play mağaza metni: İngilizce

23 Ekim 2026 · Kullanıcı: "Mağaza kaydı, görseller ve inceleme için test kaynağını hazırlayalım; kapalı teste yükleyelim." İlk kapalı test (0.16.0) için hazırlandı; metni ana dili İngilizce biri gözden geçirmedi. Türkçesi [STORE_LISTING_TR](STORE_LISTING_TR.md), Console form cevapları [PLAY_CONSOLE](../project/PLAY_CONSOLE.md).

**Uyulan kurallar** (Play hazırlığı §3.2, [satış stratejisi](SALES_STRATEGY.md) §6):
- İçerik sağlanmadığı ilk cümlede söylenir.
- "Android TV" geçer (TV paketi koşulu).
- Kanal, maç, sağlayıcı ya da rakip adı, "free", "unlimited", "best" kullanılmaz.
- Başlıkta ve anahtar kelimelerde "Xtream Codes" yoktur; açıklamada "Xtream-compatible" geçer.
- Henüz olmayan özellik yazılmaz. 0.16.0'da abonelik yok; abonelik paragrafı aboneliği getiren sürümle birlikte eklenir (aşağıda hazır).
- Ekran görüntüleri sentetik veriyle ve açık lisanslı filmlerle çekildi; atıf açıklamanın sonunda.

## Title (en çok 30 karakter)

Antenio IPTV Player

## Short description (en çok 80 karakter)

Play your own M3U or Xtream-compatible source on your TV. No content included.

## Full description

Antenio doesn't provide any content. It plays the channels, movies and series of a source you already have the right to use: an M3U playlist link or an Xtream-compatible account from your provider.

Made for the remote
• Add your source from your phone: scan the QR code on the TV and paste your link
• Live TV with a TV guide (XMLTV), channel numbers and CH+/CH−
• Movie and series libraries with posters, seasons and episodes
• Continue watching and Favorites, across several sources
• Search across channels, movies and series
• The audio and subtitle tracks each stream offers

Private by design
• No sign-up needed, no tracking
• Your source details are stored encrypted on your TV
• Parental controls: a PIN locks categories, channels, titles and sources; adult categories are hidden by default

Languages: English and Turkish. Choose one in Settings → Dil / Language, or follow your TV's language.

You need an Android TV or Google TV device with Android 8.0 or later and your own source. Guide information depends on what your provider supports.

Antenio is not affiliated with any content provider. Use it only with content you are allowed to watch.

Screenshots show open movies by the Blender Foundation (Tears of Steel, Sintel, Elephants Dream; CC BY) and public test streams.

## Abonelik paragrafı (abonelik sürümüyle eklenecek)

Watching requires an Antenio Premium subscription. New subscribers get a 7-day free trial; after that the subscription costs [price per period] and renews automatically until you cancel it in Google Play.

## Açık sorular

- "Xtream-compatible" ifadesi kalsın mı, yoksa yalnız "M3U" mı denir? (Avukat önerisi "Xtream API uyumlu".)
- Abonelik paragrafı: fiyat, dönem ve plan adı açık ([SUBSCRIPTION](SUBSCRIPTION.md)).
- 22 Ekim 2026: "No sign-up needed, no tracking" ilk yayında doğru. Platformlar arası Premium için hesap gelirse metin ve Data safety aynı sürümde güncellenir ([SALES_CHANNELS](SALES_CHANNELS.md)).
