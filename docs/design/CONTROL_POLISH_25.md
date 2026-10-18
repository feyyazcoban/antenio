# Seçim, form ve arama düzeni

**18 Ekim 2026 · 0.14.53 / D083:** Action play simgesi odakta dolu üçgene geçer; odak dışındayken çizgili. iconFilled ile belirtilen durum simgeleri (favori yıldızı) odaktan bağımsız kalır. Odak rengi/halkası ve eylem davranışı aynı.

15 Ekim 2026 · 0.14.25 tasarım önizlemesi · BKL-020.

Mevcut Google TV yönü, Roboto, anlamsal renkler ve Compose for TV bileşenleri kullanılır. Bu tur TV-002/003 kaynak formu, TV-060 arama ve ortak seçim düğmelerine odaklanır.

- Seçili düğme dolgusuna 12 dp uzunluğunda, 2 dp kalınlığında alt çizgi eklendi. Odak başka yere taşındığında seçim işareti kalır; odaktaki seçili düğmede çizgi koyu içerik rengini kullanır. Çizim mevcut iç boşlukta kalır, düğme genişliğini değiştirmez. Normal eylemler seçim durumu taşımaz; seçim bildiren düğmeler `selected` erişilebilirlik bilgisini de yayınlar.
- Kaynak formunun alanları panelden ayrılması için mevcut `Ink.Background` yüzeyini kullanır. Odaksız alan çerçevesi `Ink.Outline` renginin %70 opaklığıdır; odakta mevcut 2 dp açık çerçeve korunur. Arama ve diğer alanlarda kendi mevcut zeminleri kalır.
- Arama türleri katalogdaki kompakt, çevre çizgili yardımcı düğmelerle aynı sunumu kullanır. Temizle eylemi çevre çizgili hale getirildi. Sonuç kartlarının boyutu, sütun kuralı ve sorgu davranışı değişmedi.

0.14.26 ile çizgi metin kutusunun 4 dp altına iner ve odak dışında mint olur; tam genişlik liste satırları ✓ kullanır ([UI_REPAIR_26](UI_REPAIR_26.md)).

İlk odak, OK ve Back yolları aynı: kurulumda Xtream yöntemi, aramada giriş/geri dönüşte son sonuç; yön tuşu yalnız odağı taşır, OK seçimi uygular. Doğrulama hatası, boş sonuç ve kaynak eksikliği mevcut açıklamalarını kullanır. Yeni PIN veya kısıtlı içerik akışı eklenmedi.

## Renk hesabı

sRGB birleşimi üzerinden hesaplanan nominal değerlerdir; fiziksel panel ölçümü veya tüm ekran erişilebilirlik onayı değildir.

| Eşleşme | Önce | Yeni |
| --- | --- | --- |
| Kaynak alanı çerçevesi / dış panel | 2,48:1 | 3,56:1 |
| Kaynak alanı çerçevesi / alan içi | 2,48:1 | 4,45:1 |
| Arama alanı çerçevesi / alan içi | 2,48:1 | 3,84:1 |

## Kanıt ve sürüm dayanağı

[Önce/sonra incelemesi](../../design/reviews/LATEST.md) ve [QA kaydı](../project/native-polish-25-qa.json) gerçekleşen kontrolleri ayrı kaydeder. Figma değişmedi; kullanıcı görsel kabulü ve fiziksel cihaz koşulları açık.

15 Ekimde [TV Material 1.1.0](https://developer.android.com/jetpack/androidx/releases/tv), [AGP 9.0.1](https://developer.android.com/build/releases/agp-9-0-0-release-notes) ve [Compose BOM eşlemesi](https://developer.android.com/develop/ui/compose/bom/bom-mapping) resmî sayfalardan kontrol edildi. Bağımlılık yükseltmesi yapılmadı; projede sabitlenen setle derlendi.

Host GPU ile geniş cihaz turu 72 başarılı kontrolden sonra emülatörün kapanmasıyla kesildi. Son günlükte ColorBuffer hataları vardı; kesin kök neden kanıtlanmadı. Aynı AVD veri silmeden, [resmî belgede açıklanan SwiftShader modu](https://developer.android.com/studio/run/emulator-acceleration) ile yeniden açıldı; kalan 62 kontrol geçti. Bu yazılımsal çizim sonucu fiziksel TV performans ölçümü değildir.
