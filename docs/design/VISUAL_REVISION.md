# Google TV görsel revizyonu

**14 Ekim 2026 · İnceleme taslağı.** Kullanıcı yeni özelliklerden önce görsel kaliteyi istedi; yumuşak yüzeyler, yuvarlak kartlar ve ferah Google TV düzenini seçti. [Etkileşimli taslak](../../prototypes/tv-flow/review.html) · [Ölçüler ve renk adayları](../../design/tokens/google-tv-soft.candidate.json) · [Kontrol kaydı](../project/visual-revision-qa.json).

Mevcut yerel prototipin görsel revizyonu: aynı Roboto, temizlenmiş katalog ve onaylı assetler kullanılır. Figma kitinin doğrudan instance'ları değildir. Figma kota nedeniyle güncellenmedi; Android0.13 bu görünümü kullanmıyor; [0.14 tasarım önizlemesi](NATIVE_VISUAL_REVISION.md) ayrı test cihazında uygulanmıştır. Eski tam akış prototipi korunur.

## Referans ekranlar

| Ekran | Görüntü | Tasarım karşılığı |
| --- | --- | --- |
| TV-010 | [Ana sayfa](../../design/reviews/google-tv-soft/home.png) | Üst gezinme, sınırlı büyük kapak, ilk rafta dört tam kart |
| TV-020 | [Canlı TV](../../design/reviews/google-tv-soft/live.png) | Kategori çipleri; oranı korunan logolar; ayrı rehber erişimi |
| TV-030 | [Filmler](../../design/reviews/google-tv-soft/movies.png) | Beş sütun,2:3 afiş, iki satır başlık, ortak bilgi tabanı |
| TV-040 | [Diziler](../../design/reviews/google-tv-soft/series.png) | Filmle aynı kart ve filtre geometrisi |
| TV-031 | [Film ayrıntısı](../../design/reviews/google-tv-soft/detail.png) | Korunan metin alanı; oynatma ve favori hiyerarşisi |

Arama, boş favoriler, dizi bölümleri, rehber bilgisi yok paneli ve hareket ayarı da dolaşılabilir. Oynat yalnız inceleme çubuğunda bilgi verir; yayın açmaz. Favori oturumda tutulur. Kaynak yönetimi, tam program çizelgesi, oynatıcı, PIN ve kurulumun yeni görsel karşılıkları tamamlanmadı. Kaynak CTA'sı bu sınırı inceleme çubuğunda açıklar.

## Yerleşim

Temel **960×540 dp**; görüntüler1920×1080. Yatay58 dp, üst/alt27 dp güvenli boşluk hedefi. Google rehberi960×540 temeli, kenar güvenliği ve eksenlere göre gezinmeyi anlatır; aşağıdaki ölçüler proje adayıdır. [Android TV layouts](https://developer.android.com/design/ui/tv/guides/styles/layouts)

- Üstte Ana sayfa, Canlı TV, Filmler, Diziler, Favoriler; sağda arama/ayarlar. Odak sekmeyi açmaz, OK açar. Seçili sekme küçük alt işaretini korur.
- İçerik844 dp. Hero228 dp yüksek/20 dp köşe. İlk rafta196×110,25 dp görsel, altta başlık;20 dp yatay aralık. Sonraki raflarda32 dp dikey boşluk, ilk ekranda yarım bölüm başlığını önler.
- Afiş152,8×229,2 dp/12 dp köşe. İki satır etiket alanı; metadata aynı tabanda. Kırpma oranı korunur.
- Canlı kart268×126 dp/14 dp köşe. Logo130×40 dp sınırında `contain`; kanal adı ayrıca yazılır. Program bilgisi uydurulmaz.
- Roboto: hero39 sp, sayfa31, bölüm17, gezinme13, kart12, açıklama13, metadata11. Bunlar normal yazı adayları; büyük fontta geometri uyarlanmalı.
- Nötr koyu zemin ve tonlu yüzey; mint sınırlı vurgu/eylem odağı. Renkli kapaklar kendi renklerini taşır.

Odak2 dp açık çizgi+3 dp ayrım; afiş1,025 ölçek/160 ms. Canlı kartta ölçek yok. Azaltılmış harekette geçiş/ölçek kapalı. Bu proje reçetesidir; evrensel optimum veya ölçülmüş native performans değildir. [Android TV focus system](https://developer.android.com/design/ui/tv/guides/styles/focus-system)

## Renk adayları

| Rol | Aday | Opak çift kontrastı |
| --- | --- | --- |
| Zemin / ana metin | #15191E / #F0F3F1 |15,80:1|
| Kart / ana metin | #252C33 / #F0F3F1 |12,64:1|
| Yüksek yüzey / ikincil metin | #343E47 / #B7C1C7 |5,95:1|
| Mint / üzerindeki metin | #A8DFCB / #152B22 |10,06:1|
| Seçili yüzey / metin | #35423F / #F0F3F1 |9,39:1|

sRGB opak çift hesabı; bütün ekranın erişilebilirlik/HDR onayı değildir. Kapak üstü metinde ayrıca koyu scrim var. Gerçek görsel, uzun ad, TV modu ve büyük font native karşılığında yeniden ölçülmeli. Tarihli Figma token snapshot'ı değiştirilmedi.

## Veri ve kapsam

Beş film/dört dizi/altı kanal mevcut temizlenmiş örnekten gelir; sayılar tam hesap kataloğunu temsil etmez. `m1` başlık/görsel uyumsuzluğu nedeniyle dışarıda; Dune2021 doğru `m5` kaydıdır. Interstellar'ın Almanca özeti için kısa Türkçe sunum metni, m3/m4/m5 için editoryal kısa özet kullanıldı; kaynak metadata değişmedi. Otomatik çeviri özelliği değildir.

Popülerlik, güncel EPG, izleme geçmişi, puan, ses/altyazı desteği uydurulmadı. Eski prototipin örnek kilit bayrağı bu görsel kapsamda PIN güvenliği iddiasına dönüştürülmez. Gerçek katalogdaki INFO/ayırıcı satırları henüz temizlenmedi; seçilmiş örneklerin düzgün olması bu sorunu çözmez.

## Doğrulama ve native aktarım

Beş normal ekran incelendi; D-pad, satır sınırı, kategori, favori/aynı karta dönüş, hata/boş/kapaksız/yükleniyor, hareket ayarı denendi. Film alt boşluğu12 dp artırıldı; sağ/sol üst menü sıçraması ve430×700 panel kırpılması giderildi. [QA ve sınırlar](../project/visual-revision-qa.json).

**Android aktarım kapsamı:** `Theme.kt`/`DesignSystem.kt` rolleri, ortak üst gezinme ve kartlar; ardından `HomeScreen.kt`, `PilotApp.kt` kütüphane, arama, rehber ve ayrıntı yerleşimleri. Kaynak filtresi, devam et/son kanal/favoriler ve rehber erişimi korunmalı. Sabit Interstellar seçimi üretime kopyalanmamalı. Sahiplik, kalıcılık, tek yayın ve erişim kuralları korunmalı.

Kullanıcının `emulator-5554` cihazına dokunmadan ayrı AVD/serial ile aynı içerik/odak/ölçekte native görsel karşılaştırma yapılmalı.1,0/1,5/2,0 font, uzun ad, eksik kapak, çok kaynak, kumanda dönüşleri; kullanıcı görsel onayı, Figma eşlemesi, fiziksel TV ve akıcılık ölçümü açık.
