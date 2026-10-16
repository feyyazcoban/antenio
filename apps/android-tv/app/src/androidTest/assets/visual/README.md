# Yerel native görsel kontrol verisi

Mevcut `prototypes/tv-flow/assets/catalog` görsellerinin test kopyalarıdır. Kaynak/provenans, SHA-256 ve ilk kalite kontrolü prototipin `manifest.json` ve `quality-review.json` dosyalarındadır. Afişlerin hakları ilgili sahiplerindedir; bu kopyalar pilot tasarım kontrolü içindir. Ana uygulama APK'sına girmezler.

`catalog.json` aynı prototipin arındırılmış başlık/açıklama verisinden üretilmiş sentetik Xtream yanıtlarıdır. `@BASE@` yalnız test sırasında yerel MockWebServer adresiyle değiştirilir. Gerçek sağlayıcı adresi/hesabı/medya akışı yoktur. `m1` bilinen yanlış Dune eşleşmesi nedeniyle alınmadı.

`VisualPreviewTest` Android ekranlarından Home, katalog, film, dizi, oynatıcı, dil paneli ve ayarlar PNG'lerini uygulamanın özel `files/visual-preview` dizinine yazar. Oynatıcıda sentetik `playback-fixture.mp4` kullanılır; film veya dizinin gerçek videosu gibi sunulmaz. Test kaynağı ve geçmişi sonunda silinir. Yalnız ayrılmış emülatörde çalıştırın.
