# TV renk kullanımı rehberi

**14 Ekim:** Daha yumuşak Google TV yönü için nötr zemin/sakin mint [adayları ve ölçümleri](VISUAL_REVISION.md) yerel taslakta denendi. Aşağıdaki palet tarihli Figma durumudur; Android0.14 tasarım önizlemesi aday renkleri kullanır, Figma güncellenmedi.

**Tarih:** 13 Ekim 2026 · **Sürüm:** 0.1 · **Durum:** Mevcut Figma renkleri ölçüldü; gerçek TV ve tamamlanmış ekran doğrulaması bekliyor.

Başlangıç önerisi, mevcut koyu yeşil zemin ve mint vurguyu koruyup renkleri görevlerine göre kullanmak. Marka rengi her yüzeyi doldurmaz; içerik, odak ve durumlar okunaklı bir hiyerarşi oluşturur. Buradaki sayısal kontrastlar kaynak tokenlarının ölçümüdür; tüm uygulamanın erişilebilirlik onayı değildir.

## 1 Araştırmadan çıkan ilkeler

| Kaynak | Bulgusu veya kuralı | Ürün kararı ve sınırı |
| --- | --- | --- |
| [Google TV color system](https://developer.android.com/design/ui/tv/guides/styles/color-system) | TV için koyu tema başlangıcı, Material renk rolleri ve içerikten türeyen renk seçeneklerini anlatır. | Koyu tema + anlamsal roller. Her afişte tüm uygulamanın rengini değiştirmek ilk sürümde önerilmiyor. |
| [Google Color on TV](https://developer.android.com/design/ui/tv/guides/foundations/color-on-tv) | Panel, görüntü modu ve ortam ışığı görünümü değiştirir; farklı cihazlarda okunabilirlik sınanmalıdır. | İlk referans Standard modu; gündüz ve loş oda, LCD ve OLED üzerinde test. Bilgisayar ekranı tek doğrulama değil. |
| [WCAG metin kontrastı](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html) | Normal metinde 4.5:1, tanımdaki büyük metinde 3:1; bazı istisnalar bulunur. | TV'de aktif metinler için 4.5:1 taban hedef; küçük/önemli metinde 7:1 tercih. 7:1 burada ürün hedefidir. |
| [WCAG metin dışı kontrast](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html) | Tanınması için gerekli kontrol/durum görsellerinde komşu renge karşı 3:1 gerekir; her dekoratif sınır için değildir. | Odak çizgisi ve gerekli oynatıcı ikonları en az 3:1 hedefler. Gerekli sınırda düşük kontrast dekoratif çizgi kullanılmaz. |
| [WCAG renk kullanımı](https://www.w3.org/WAI/WCAG22/Understanding/use-of-color.html) | Bilgi yalnız renkle taşınmamalıdır. | Hata metin/ikonla; seçim işaretle; oynayan kanal etiketle; kilit simgeyle anlatılır. |
| [Piepenbrock ve diğerleri, 2013](https://www.psychologie.hhu.de/fileadmin/redaktion/Oeffentliche_Medien/Fakultaeten/Mathematisch-Naturwissenschaftliche_Fakultaet/Psychologie/AAP/Publikationen/2013/Piepenbrock-2013-Positive_display_polarity_is_.pdf) | Görme keskinliği ve metin düzeltme görevlerinde açık zemin/koyu yazı avantajını genç ve yaşlı gruplarda araştırır. | Koyu temayı “bilimsel olarak her zaman daha okunaklı” diye sunmayız. Çalışma salon TV'si ve video arayüzü tercihini tek başına belirlemez. |

WCAG web standartları burada ölçülebilir tasarım dayanağıdır. Android dp büyüklüklerini CSS punto tanımlarına doğrudan eşitleyip metni “büyük” ilan etmeyin. Ürün hedefi aktif metinde en az 4.5:1'dir.

## 2 Mevcut paletin görevleri

Değerler [NEXTV Foundations](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV?node-id=160-3739) dosyasındaki **sys / Dark** modundan çözümlendi. İsimler collection/name gösterimidir; üretim kodunda HEX kopyalamak yerine role bağlanmalıdır.

| Rol | Mevcut değer | Kullanım |
| --- | --- | --- |
| sys/surface | #091610 | Ana sayfa ve katalog zemini |
| sys/surface-container | #1A211F | Kart dışı içerik grupları ve panel yüzeyi |
| sys/surface-container-high | #252B2A | Üst katman veya seçilebilir yardımcı yüzey |
| sys/on-surface | #D8E6DF | Ana başlık ve okunması gereken metin |
| sys/on-surface-variant | #B9CBC2 | İkincil açıklama; yalnız uygun zeminle |
| sys/primary | #1CECAB | Ana eylem, ilerleme ve kontrollü marka vurgusu |
| sys/on-primary | #023826 | Mint dolgu üzerindeki metin/ikon |
| sys/primary-container | #0D553E | Kalıcı seçim yüzeyi |
| sys/on-primary-container | #D1F7F0 | Seçim yüzeyi üzerindeki içerik |
| sys/outline | #84948C | Uygun yüzeyde gerekli sınır; bağlamda kontrol |
| sys/outline-variant | #3B4A43 | Dekoratif ayrım; tek odak işareti değil |
| sys/error | #FFB4AB | Hata vurgusu; metin/ikon eşlik eder |
| sys/error-container | #93000A | Hata paneli |
| sys/on-error-container | #FFDAD6 | Hata paneli metni |

Mint üzerinde beyaz yazı kullanmayın: #FFFFFF / #1CECAB **1.54:1**. Mevcut on-primary / primary eşleşmesi **8.54:1**. Bu, var olan tüm bileşenlerde hatalı eşleşme bulunduğu iddiası değildir; bileşen bazında ayrıca taranacak bir kullanım kuralıdır.

## 3 Kontrast sonuçları

Ölçüm, ham Figma alias zincirleri çözülerek sRGB bağıl parlaklık formülüyle yapıldı. Tabloda opak renkler kullanılıyor; değerler iki ondalık gösteriliyor. Alfa, görsel arka plan, gölge ve HDR etkisi ayrıca değerlendirilmelidir. Tam ölçüm kaydı design/tokens/contrast-audit.json içindedir.

| Ön plan / arka plan | Dark | Light | Yorum |
| --- | --- | --- | --- |
| on-surface / surface | 14.38 | 16.31 | Aktif metin taban hedefini karşılıyor |
| on-surface / surface-container | 12.73 | 14.67 | Panel metni için uygun başlangıç |
| on-surface-variant / surface-variant | 5.51 | 7.26 | İkincil metin için uygun başlangıç |
| on-primary / primary | 8.54 | 6.44 | Tema moduna bağlı eşleşmiş çift kullan |
| on-primary-container / primary-container | 7.66 | 7.28 | Seçili yüzey metni |
| on-error / error | 7.72 | 6.46 | Hata dolgusunda eşleşmiş çift |
| on-error-container / error-container | 7.24 | 7.24 | Hata mesajı |
| outline / surface | 5.82 | 4.26 | Gerekli sınır için 3:1 üzerinde |
| outline-variant / surface | 1.98 | 1.62 | Gerekli odak/sınır için yetersiz; dekorasyonla sınırlı |

Light moddaki primary #076C4C ve on-primary #FFFFFF'tir. Dark modun mint rengini Light primary yerine geçirip beyaz yazıyı korumayın.

## 4 Odak, seçim ve içerik durumu

- **Odak:** Kontrastlı çevre çizgisi ve gerekiyorsa kısa ölçek hareketi. Afişin kenarındaki renge karşı görünürlüğü güvenceye almak için sabit nötr ayırıcı yüzey kullanılabilir.
- **Seçili:** primary-container yüzeyi + işaret. Odak başka yere geçince seçim bilgisi kalır.
- **Oynuyor:** Kanal satırında “Oynatılıyor” etiketi veya ses simgesi. Yalnız yeşil noktayla anlatılmaz.
- **Canlı:** LIVE/CANLI metniyle rozet; hata kırmızısıyla aynı anlamı taşıdığı varsayılmaz.
- **Kilitli:** Kilit ikonu ve PIN eylemi. Soluk metin tek başına yeterli değil.
- **Hata:** error rolü + kısa açıklama + düzeltme eylemi. Her ağ gecikmesini kritik kırmızı ekran yapmayın.
- **Devre dışı:** Kullanılamayan eylem sebebi gerekiyorsa normal okunaklı yardımcı metinde gösterilir.

Marka minti otomatik “başarılı” anlamına gelmez. Başarı, uyarı ve bilgi renkleri için gelecekte ayrı anlamsal roller gerekiyorsa ad, eş renk ve kontrast birlikte tanımlanır; rastgele sarı/mavi eklenmez.

## 5 Video ve afiş üzerinde renk

Bir film afişi veya yayın karesi sabit arka plan değildir. Metin kontrastını yalnız token çiftinden çıkaramayız.

Oynatıcı kontrolleri için kararlı koyu panel veya yerel scrim kullanın. Hem beyaz/karlı sahne, hem karanlık sahne, hem parlak renkli yayın üzerinde ölçün. İki ardışık açık/koyu sahnede ikonun kaybolması test hatasıdır. Gölge tek başına bütün sahnelerde kontrast garantisi vermez.

Mevcut brand koleksiyonunda content/secondary, content/faint ve glass yüzeyleri **alfa** taşıyor. Beyaz RGB görmek opak beyaz oldukları anlamına gelmez. Örneğin content/faint yaklaşık %38, content/disabled yaklaşık %22 opaklıktadır. Nihai renk arka planla birleşimden sonra hesaplanır. Bu tokenlar tema bağımsız aktif metin rolü olarak kullanılmamalıdır.

Posterler kendi renklerini korur; marka rengine boyanmaz. Logo kutusunda logonun oranı korunur. Dinamik hero rengi ileride eklenirse metin ve odak rolleri yeniden kontrast kontrolünden geçer; EPG ve oynatıcı kontrol renkleri her içerikte değişmez.

## 6 Uygulama kuralları

1. sys renkleri eşlenmiş surface/on-surface, primary/on-primary çiftleriyle kullanılır.
2. Yeni renk değeri önce rol gerekçesi ve kullanım örneğiyle belgelenir.
3. Aynı bileşenin Default, Focused, Pressed, Selected, Disabled, Error ve Locked durumları tek tek kontrol edilir.
4. Opaklık uygulanmış metin için nihai kompozit renk ölçülür; yalnız HEX karşılaştırılmaz.
5. Önemli bilgi renk dışında metin, şekil veya ikonla korunur.
6. EPG zaman ızgarasında seçili, şu an yayınlanan ve odaklanan program ayrıdır.
7. Büyük parlak yüzey geçişleri ve odağa gelince tüm ekranı mintle doldurma kullanılmaz.
8. Renk körlüğü simülasyonu yardımcı kontroldür; kontrast ve kullanıcı testinin yerine geçmez.

## 7 Doğrulama matrisi

- Katalog, EPG, kaynak formu, hata, PIN, film/dizi ayrıntısı, oynatıcı.
- Normal, odak, seçili ve basılı durumlar.
- Standard görüntü modu referansı; gündüz/loş oda; 1080p/4K.
- LCD ve OLED; SDR UI ve HDR video üstü kontroller.
- Kaynak afişi/logosu boş, beyaz zeminli, siyah zeminli ve çok renkli.
- Artırılmış okunabilirlik ihtiyacı olan katılımcılarla gerçek mesafede kontrol.
- Alfa/scrim kombinasyonlarında en kötü arka plan örnekleri.

**Hazır sayılma:** aktif metin ≥4.5:1 hedefi, gerekli kontrol ve odak işaretleri ≥3:1, yalnız renkle taşınan durum yok; başarısız örnekler düzeltildi; kullanılan cihaz ve koşullar kayıtlı. Henüz bu son ekran/cihaz doğrulaması yapılmadı.
