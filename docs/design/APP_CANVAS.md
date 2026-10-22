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
| 1 | Kurulum | TV-001 (iki hâl), 001a, 001b, 002, 002a, 003a, 004b | 8 görüntü; TV-003b–c, 002b, 004, 004a alınmadı |
| 2 | Ana sayfa | TV-010 vitrin, son izlenen kanallar, film rafı, yan menü | 4 görüntü |
| 3 | Canlı TV ve rehber | TV-020 (iki hâl), 021, 050 (iki hâl), 052 | 6 görüntü |
| 4 | Filmler ve diziler | TV-030, 031, 051, 040, 041 | 5 görüntü |
| 5 | Arama ve favoriler | TV-060 (boş ve sonuçlar), 061 boş | 3 görüntü |
| 6 | Ayarlar ve kaynaklar | TV-070 ayarlar, kaynaklar, kaynak yönetimi | 3 görüntü; TV-071 PIN alınmadı (PIN tanımlı değil) |

23 Ekim gecesi (02:30–02:50) kullanıcının izniyle TV uyandırıldı; hoparlör sesi 0'a çekildi ve sessizde çalışıldı, iş bitince TV uykuya alındı ve ses düzeyi eski haline (15, sessiz) döndürüldü. Kaynak Xtream olarak ekliydi; içerik ekranlarında sağlayıcının kataloğu görünür, hesap bilgisi görünmez.

Görülen sorun: tür ekranı (TV-002) M3U odaktayken de "Adım 1 / 5" yazıyor; M3U yolu 3 adımdır.

Sıradaki iş: kalan kurulum adımları için kayıt dışı bir deneme kaynağıyla (hesap bilgisi görüntüye girmeden) çekim; PIN tanımlıyken TV-071. Ardından değişecek ekranlar ayrı taslak olarak çizilir.
