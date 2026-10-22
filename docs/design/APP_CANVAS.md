# Uygulama ekran tuvali

23 Ekim 2026 · Kullanıcı: "bütün uygulamanın tasarım tuvalini bölüm bölüm çıkar. Önce mevcut ekranların TV görüntüleriyle başla."

Tuval: https://claude.ai/artifact/LvtaBnB1HKd2xWh3cu9MQT (özel; paylaşım sayfanın Share menüsünden yapılır). Figma dosyası değildir; [FIGMA](FIGMA.md) ve `design/figma/manifest.json` değişmedi. Kurulum taslağının ayrı tuvali [SETUP_FLOW](SETUP_FLOW.md) içinde kalır.

## Kurallar

- Görüntüler fiziksel TV'den (192.168.1.126) `adb exec-out screencap` ile alınır, 1920×1080; tuvalde 960×540 artboard.
- Artboard adı FIGMA düzeniyle: `TV-030 / Filmler / Focused`. Kimlik [SCREENS](SCREENS.md) numarasıdır; alt durumlar harfle ayrılır.
- Kimlik bilgisi, kimlikli adres ya da şifre içeren görüntü konmaz. Örnek adresler sentetiktir (`example.test`). Sağlayıcı kataloğu görünen görüntüler yalnız bu özel tuvalde durur, depoya konmaz.
- Görüntüler depoya eklenmez; tuvalin kendi dosyalarında durur.

## Bölümler

| # | Bölüm | Ekranlar | Durum |
|---|---|---|---|
| 1 | Kurulum | TV-001, 001a, 001b, 002a, 004b | 5 görüntü; TV-002, 003a–c, 002b, 004, 004a sırada |
| 2 | Ana sayfa | TV-010 | TV'de çalışan kaynak bekleniyor |
| 3 | Canlı TV ve rehber | TV-020, 021, 050, 052 | Kaynak bekleniyor |
| 4 | Filmler ve diziler | TV-030, 031, 040, 041, 051 | Kaynak bekleniyor |
| 5 | Arama ve favoriler | TV-060, 061 | Kaynak bekleniyor |
| 6 | Ayarlar, kaynaklar, PIN | TV-070, 071 | Kaynak bekleniyor |

Sıradaki iş: kullanıcının kaynağı TV'de çalışınca ve TV boştayken bölüm 2'den başlayarak görüntü alınır. Her bölüm bitince tuval ve bu tablo birlikte güncellenir. Yeni ya da değişecek ekranlar ancak görüntüler tamamlanınca ayrı taslak olarak çizilir.
