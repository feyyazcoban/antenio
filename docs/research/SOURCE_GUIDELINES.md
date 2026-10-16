# IPTV kaynak, görsel varlık ve dil rehberi

**Tarih:** 13 Ekim 2026 · **Kaynak:** Kullanıcının sağladığı aynı hesaba ait iki liste ve aynı sunucunun Xtream arayüzü. **Durum:** Tam liste/katalog alan incelemesi; görsel ve medya doğrulaması örneklemle sınırlı.

İlk tercih **Xtream Codes**, yalnız URL ile kurulumda **M3U Plus** olmalı. Xtream dizi-sezon-bölüm yapısını ve zengin ayrıntıları sağlar; M3U Plus düz listeyi kategori ve görsellerle zenginleştirir. Düz M3U oynatma için yedek seçenek olarak desteklenir. Bu sonuç incelenen sağlayıcıya aittir.

Ham URL'ler, kimlik bilgileri ve medya erişim adresleri bu belgede bulunmaz. Sayılar aynı gün ardışık isteklerden alınmıştır; sağlayıcı kataloğu değişebilir. Sayımın “alan dolu” demesi görselin çalıştığı, metadata'nın doğru olduğu veya içeriğin oynatılabildiği anlamına gelmez.

## 1 İnceleme kapsamı ve kanıt

- İki M3U dosyasının tüm 63.175 girdisi parse edilip yayın URL'leri sıralı karşılaştırıldı.
- Xtream canlı, film, dizi ve kategori listelerinin tamamı incelendi.
- Üç film ayrıntısı ve üç dizi ayrıntısı örneği okundu; bütün eserlerin ayrıntı uçları ayrı ayrı çağrılmadı.
- Liste/katalog alanlarından 47.388 benzersiz HTTP(S) görsel adresi envantere alındı; tüm adresler tek tek indirilmedi.
- Logo, poster, dizi kapağı ve backdrop gruplarından 20 görsel için erişim/çözümleme denendi; 19 başarılı, bir ağ/erişim hatası. Başarı oranı kataloğun tamamına genellenmez.
- Altı yayın dosyasında ardışık, kısa ffprobe incelemesi yapıldı: beşinde parça bilgisi alındı, ilk film denemesinde alınamadı. Aynı anda bir yayın açıldı; uzun süreli izleme veya gerçek Android oynatma testi yapılmadı.
- EPG için iki kısa rehber sorgusu HTTP hatası verdi. Bu, bütün kaynağın EPG'siz olduğunu kanıtlamaz.

Kanıt dosyaları:
- [Kimliksiz ölçüm kaydı](data/source-audit.json)
- [47.388 satırlık görsel envanteri](data/asset-inventory.csv)
- [201 kategori kaydı](data/category-inventory.csv)

Görsel envanteri adres yerine SHA-256 anahtar, host, şema, kullanım rolü ve tekrar sayısı taşır. İndirme manifesti değildir. Ham adres eşlemesi yalnız özel inceleme alanında kalır.

## 2 M3U Plus ve düz M3U karşılaştırması

| Alan | M3U Plus | Düz M3U | Ürün karşılığı |
| --- | --- | --- | --- |
| Toplam girdi | 63.175 | 63.175 | Aynı sıralı yayın URL'leri; bu örnekte yalnız metadata farklı |
| Benzersiz yayın adresi | 63.175 | 63.175 | URL tekrarı bulunmadı; aynı içerik alternatifleri yine olabilir |
| tvg-name dolu | 63.171 | 0 | EXTINF görünen adı fallback |
| group-title dolu | 63.135 | 0 | 40 kategorisiz kayıt için “Diğer” |
| tvg-logo dolu | 56.604 | 0 | 6 değer geçerli HTTP(S) adresi değil; boş/kırık durum şart |
| tvg-id dolu | 490 | 0 | EPG eşleşmesini tüm kanallarda varsayma |
| Kategori grupları | 201 dolu grup + boş grup | Grup yok | Filtre ve kategori gezinmesi Plus ile mümkün |
| XMLTV veya ek parça etiketi | İncelenen dosyada yok | Yok | Altyazı/ses listesi veya EPG URL'si bu başlıktan türetilemedi |

Yayın yollarında 14.603 movie ve 44.047 series girdisi var. Kalan 4.525 adres çıplak kanal yolu biçiminde; canlı API sayısıyla eşleşiyor. URL yolu genel M3U standardında tür garantisi değildir; bu sağlayıcıya özgü ipucu olarak saklanmalıdır.

`output=ts` parametresi bütün film ve bölümlerin MPEG-TS olduğu anlamına gelmiyor. Gerçek örneklerde MKV ve MP4 konteynerleri görüldü; yayın adresindeki biçim ve oynatıcı keşfi korunmalıdır.

## 3 Xtream kataloğu

| Katalog | Sayı | Anlamı |
| --- | --- | --- |
| Canlı listesi | 4.525 | 4.484 live + 41 radio_streams |
| Film listesi | 14.609 | M3U film yollarından 6 fazla; ardışık snapshot/kapsam farkı incelenmeli |
| Dizi listesi | 2.173 | Dizi başlıkları; 44.047 M3U bölüm girdisiyle aynı birim değil |
| Canlı kategorisi | 58 | 40 canlı kaydında category_id boş |
| Film kategorisi | 105 | Kategoriler tür, ülke, sağlayıcı ve yılı birlikte kullanabiliyor |
| Dizi kategorisi | 38 | Yerel/ülke/platform ve DUAL etiketleri bir arada |

Örnek kategori adları `TR/DIZI ~ NETFLIX (DUAL)`, `TR/FILM ~ YABANCI 2017-2000 (DUAL)` ve `DE/SERIE ~ ALL` gibi bileşik adlar. Ham adı koruyun; isteğe bağlı temiz başlık ve etiketler ayrı alanlar olsun. Kategori adı resmî yayıncı ilişkisi, dil veya kullanım hakkı kanıtı değildir.

Canlı listesinde 51, film listesinde 40 ad ayırıcı satıra benziyor. Yıldızlı isim gördüğünüz için kaydı kalıcı olarak silmeyin; bazıları bilgi videosu olabilir. Android 0.14.1 bunları otomatik rafta öne çıkarmaz, tam kütüphanede sona taşır; arama/favori/geçmiş erişimi korunur. Ayrı kullanıcı gizleme tercihi henüz eklenmedi. [Sunum kuralları ve gerçek snapshot denetimi](../design/CATALOG_PRESENTATION.md).

## 4 Kullanılabilir bütün varlık rolleri

| Varlık / bilgi | Kaynak alanı | Ekranda kullanım | Eksik veya hatalı veri davranışı |
| --- | --- | --- | --- |
| Kanal logosu | tvg-logo / stream_icon | Kanal listesi, EPG sabit sütunu, banner | Sabit kutu + kanal baş harfi veya nötr TV ikonu |
| Film posteri | stream_icon; ayrıntıda movie_image / cover_big | TV-030 ve TV-031 | Nötr 2:3 kart, okunaklı başlık; yanlış eser afişi otomatik bağlanmaz |
| Dizi kapağı | cover; ayrıntı info.cover | TV-040/041 | Dizi başlığı bulunan nötr poster |
| Geniş arka plan | backdrop_path dizisi | Detay hero alanı | Düz yüzey; dikey posteri bulanıklaştırma zorunlu değil |
| Bölüm görseli | episode.info.movie_image, sağlanıyorsa | Sezon/bölüm listesi | Dizi kapağı, ardından nötr 16:9 yer tutucu |
| Sezon görseli | seasons içindeki mevcut görsel alanları | Sezon seçici | Dizi kapağı + “Sezon N”; alan varlığı her seride doğrulanır |
| Fragman | trailer / youtube_trailer | Ayrıntıda isteğe bağlı eylem | Geçerli ID/URL yoksa eylem gizlenir; otomatik oynatma yok |
| Başlık / özgün başlık | name / o_name / title | Liste, detay, arama | Ham ad korunur; boşsa tür + yerel sıra etiketi |
| Açıklama | plot / description | Ayrıntı, daha fazla oku | Yer kaplayan sahte açıklama eklenmez |
| Oyuncu / yönetmen | cast / actors / director | Metin listesi | İncelenen katalog oyuncu portre seti sağlamıyor; fotoğraf icat edilmez |
| Tür / tarih / süre | genre, releaseDate/release_date/releasedate, duration_secs | Detay kısa bilgileri | Tarih ve süre parse edilemiyorsa gösterme |
| Puan | rating / rating_5based | Ayrıntı veya küçük etiket | 0 veya bilinmeyen değer “0/10” olarak sunulmaz; ölçek doğrulanır |
| Kaynak kimliği | stream_id / series_id / episode.id | Favori, ilerleme, cache eşlemesi | sourceId + type + nativeId birleşimi kullanılır |
| TMDb kimliği | tmdb / tmdb_id | İleride doğrulanmış metadata eşleşmesi | Sıfır, hatalı tür veya yanlış eser eşleşmesi reddedilir |
| İçerik türü | stream_type | Canlı/radyo/film ayrımı | Eksikse adapter sonucu ve güven derecesi saklanır |
| Kısıt bilgisi | is_adult, kategori ipuçları | PIN/gizleme | Alanlar çelişebilir; kategori filtresi ve kullanıcı denetimi korunur |
| EPG kimliği | tvg-id / epg_channel_id | Yayın rehberi eşleştirme | İsim benzerliği otomatik kesin eşleşme sayılmaz |
| Arşiv yeteneği | tv_archive / tv_archive_duration | Catch-up uygunluğu | İncelenen canlı kayıtlarda etkin değer 0; düğme sunulmaz |
| Teknik medya bilgisi | container_extension; bazı detail.info.audio/video | Oynatıcı hazırlığı | Son karar gerçek akış ve cihazdan alınır |

Kanal “HD/4K” yazısı, film kategorisi veya poster üzerindeki metin çözünürlük kanıtı değildir. Örnekte 3840×1608 HEVC görüldü; 2160 satır varmış gibi teknik bilgi yazılmamalı.

## 5 Görsel doluluk ve kalite

| Tür | Dolu alan | Geçerli HTTP(S) biçimi | Eksik alan |
| --- | --- | --- | --- |
| Canlı logosu | 1.282 / 4.525 (%28,33) | 1.280 | 3.243 |
| Film posteri | 11.708 / 14.609 (%80,14) | 11.704 | 2.901 |
| Dizi kapağı | 2.153 / 2.173 (%99,08) | 2.152 | 20 |
| Dizi backdrop | 2.063 / 2.173 | Dizi başına birden fazla URL olabilir | 110 |

Toplam 77.220 görsel referansı, 47.388 benzersiz HTTP(S) URL. M3U Plus ve API birlikte sayıldığı için aynı görsel birden fazla kez geçiyor. 75.326 referans HTTPS, 1.894 HTTP. En sık host image.tmdb.org; kanal logolarında raw.githubusercontent.com ve diğer hostlar da var. URL hostu, resmi API entegrasyonu veya yeniden dağıtım izni anlamına gelmez.

20 örnekte:
- Logolar değişken oranlı şeffaf PNG: 680×350, 512×443, 512×289, 512×301 gibi.
- Film/dizi posterleri çoğunlukla 600×900 JPEG; bir örnek 1175×1763.
- Backdrop örnekleri 1280×720 veya 1279×720.
- Bir logo isteği ağ/erişim hatası verdi; adresin kalıcı olarak bozuk olduğu sonucuna varılmadı.

**Yerleşim kuralları:** Logo için contain ve boşluk, poster için 2:3 kutu, backdrop/bölüm için 16:9 kutu. Poster üzerindeki başlığı kesen otomatik kırpma yerine uygun oranı koruyun. Kanal logosunu daire veya kareye zorlayarak kesmeyin. Yer tutucu ve son görsel aynı ölçüde olmalı; yüklenince odak/satır yer değiştirmemeli.

**Yükleme kuralları:** Görünür ve yakın sıradaki görseller alınır; 47 bin görsel topluca indirilmez. URL rolü + istenen boyut ile cache anahtarı, bellek/disk bütçesi, sınırlı eşzamanlı istek ve hata sonrası gecikmeli tekrar kullanılır. HTTP(S) dışındaki şemalar asset olarak çalıştırılmaz. MIME ve decode sonucu doğrulanır; bozuk yanıt sonsuz yeniden denenmez.

Kaynak URL'sini rastgele daha büyük boyut parametresine çevirmeyin. Boyut varyantı yalnız ilgili sağlayıcının belgelenmiş sözleşmesiyle kullanılmalı. Sunucu-proxy eklenirse host, özel ağ, yönlendirme ve boyut denetimleri ayrıca tasarlanmalı.

## 6 Ses ve altyazı bulguları

M3U dosyalarında ayrı altyazı adresi veya tüm ses parçalarını listeleyen alan bulunmadı. API detail içindeki audio/video alanları bazı bölümlerde mevcut; tüm alternatif parçalara ilişkin güvenilir bir katalog değil. Oynatıcı hazırlanınca parça listesi alınmalı.

| Örnek | Doğrulanan medya | Ses | Altyazı |
| --- | --- | --- | --- |
| Bir canlı kanal | MPEG-TS, H.264, 1280×720 | AAC stereo, tur | İnceleme penceresinde parça görülmedi |
| İlk film denemesi | Parça bilgisi alınamadı | Belirsiz | Belirsiz |
| Bir dizi bölümü | MP4, H.264, 1920×1080 | AAC stereo, tur | Parça görülmedi |
| DUAL kategori, MKV film | HEVC, 3840×1608 | tur AC-3 5.1 + eng AAC 5.1 | İki ayrı eng SubRip parçası |
| DUAL kategori, MP4 film | H.264, 1920×1080 | Yalnız tur AAC stereo | Parça görülmedi |
| DUAL kategori, başka bölüm | H.264, 1920×1080 | Yalnız ger AAC stereo | Parça görülmedi |

**Sonuç:** DUAL etiketi iki ses parçası garantisi değil. Kategori ülkesinden konuşulan dili çıkarmak da güvenilir değil. Parça etiketi ger olan örnekte içerik dinlenmedi; rapor etiket bilgisini aktarır.

MKV örneğinde İngilizce ses default=1, Türkçe ses default=0. İki İngilizce altyazıdan yalnız biri default=1; ikisinde de forced ve hearing_impaired bayrağı 0. Bu iki altyazının anlam farkı doğrulanmadı; aynı dil diye birleştirilmeyecek ve “SDH” diye etiketlenmeyecek.

## 7 Ses ve altyazı arayüzü sözleşmesi

**Ses paneli**
- Gerçek parçalardan “Türkçe · 5.1”, “İngilizce · 5.1” gibi okunaklı seçenekler üretin.
- tur → Türkçe, eng → İngilizce, ger/deu → Almanca gibi dil kodlarını normalize edin. und/boş etikette “Ses 1”, “Ses 2” kullanın.
- Kanal sayısı veya format bilinmiyorsa uydurmayın. Codec bilgisi gerektiğinde teknik ayrıntı veya uyumsuzluk açıklamasında yer alır.
- Cihazın desteklemediği parça seçilebilir gibi görünmesin; varsa desteklenen alternatifi önerin.
- Seçili işaret ve kumanda odağı ayrı olsun. Değişim mevcut konumu korumalı; yeni ayrı oynatıcı açılmamalı.

**Altyazı paneli**
- “Kapalı” her zaman anlaşılır bir seçenek; mevcut parçalar dil ve varsa rol etiketiyle listelenir.
- Örnekteki iki isimsiz İngilizce parça “İngilizce 1 / İngilizce 2” olarak ayrılabilir.
- Forced, SDH veya işitme desteği yalnız metadata/format güvenilir biçimde bildiriyorsa gösterilir.
- Dil tercihi bulunamazsa sessizce rastgele başka dil açılmaz; mevcut seçenekler erişilebilir kalır.
- Harici SRT/WebVTT dosyası ekleme ayrı bir gelecek özelliktir; bu listelerde doğrulanmadı.
- Parça görünmemesi görüntüye gömülü altyazı bulunmadığını kanıtlamaz. Görüntüye gömülü yazı bu menüden kapatılamaz.

**Tercihi hatırlama**
İlk öneri: kullanıcı ses tercihi → desteklenen eşleşen dil → akış varsayılanı. Altyazıda Kapalı / Otomatik / tercih edilen dil ayrımı tanımlanır. Kullanıcının açık Kapalı seçimi yeni bölümde zorla değiştirilmez. Önceki dosyanın sayısal parça index'i sonraki dosyaya taşınmaz.

Media3, medya hazırlanırken ve parça seçimi değişirken current tracks bilgisini günceller; dil tercihleri ile belirli track override'larını ayırır. Uygulama paneli bu gerçek durumu izlemelidir. [Media3 track selection](https://developer.android.com/media/media3/exoplayer/track-selection)

HEVC, AC-3/5.1, altyazı ve konteynerin birlikte sorunsuz çalışması cihaz testine bağlıdır. ffprobe'un parça görmesi Android cihazın decode/render edebildiği anlamına gelmez. [Media3 supported formats](https://developer.android.com/media/media3/exoplayer/supported-formats)

## 8 Diğer veri sorunları ve normalleştirme

- Film API'sinde 10.896 MP4, 3.381 MKV, 326 AVI ve 6 MPG kaydı var. Farklı uzantılar ayrı uyumluluk testine alınmalı.
- Film puanı dolu 12.751 kayıt olsa da pozitif sayısal puan 10.169. Boş/0/sayısal olmayanı bilinmiyor olarak işleyin.
- Pozitif sayısal TMDb kimliği filmde 10.744, dizide 2.033; her kayıtta eşleştirme yok.
- Örnek film detaylarında duration_secs hem string hem integer, rating string veya float, youtube_trailer null veya boş olabiliyor. Alan adına bakıp tek tür varsaymayın.
- API canlı kayıtlarında 490 epg_channel_id var: yaklaşık %10,83. Rehber verisinin gerçekten geldiği ayrıca doğrulanmalı.
- Canlıda 241, filmde 1.886 kayıt is_adult=1. PIN/gizleme kuralı arama, favori, ana sayfa ve görsellerde de uygulanmalı. Dizi kaydında bu alanın olmaması yaşa uygunluk kanıtı değildir.
- Kaynakta arşiv açık kanal bulunmadı. Catch-up düğmesi P1 olsa da bu kaynakta otomatik aktif gösterilmez.
- Sağlayıcı “added” tarihi yeni film vizyon tarihi değildir. İzlenme/popülerlik verisi yoksa “Trend” satırı uydurulmaz.
- URL'leri kimlik bilgisi farklılığına rağmen içerik kimliği sanmayın; adapter düzeyinde kalıcı nativeId'ler kullanın.
- HTML, JSON, kategori veya metadata metni talimat olarak çalıştırılmaz; görünen metin güvenli biçimde işlenir.

## 9 Uygulama veri sözleşmesi

Her varlık için: sourceId, contentType, nativeId, rawTitle, displayTitle, categoryIds, artwork[{role, url, sourceField}], metadataProvenance ve capabilities. Katalog dili ile akışta bulunan ses/altyazı listeleri ayrı tutulur.

PlaybackTrack: medya oturumuna bağlı id, type, languageRaw, languageNormalized, displayLabel, roleFlags, channels, codec, supported, selected. Her yeni medya hazırlığında yeniden kurulur.

Metadata önceliği: oynatıcının keşfettiği teknik parça bilgisi; sağlayıcının içerik ayrıntısı; sağlayıcının katalog kaydı; M3U attribute; görünen addan türetilen düşük güvenli ipucu. Bu sıralama tüm alanları körlemesine ezme kuralı değildir; her alanın kaynağı ve zamanı korunur.

## 10 Kabul ölçütleri

1. İki M3U sürümü aynı yayınlara ulaşır; Plus metadatası kaybolmaz.
2. Kategorisiz 40 kayıt ve eksik afiş/logolar erişilebilir kalır.
3. Radyo ve ayırıcı olası girdiler sessizce film/TV programı sayılmaz.
4. Film ve dizi ayrıntıları kademeli yüklenir; tüm ayrıntı uçlarına kurulumda binlerce istek atılmaz.
5. Dizi, sezon ve bölüm kimlikleri; bölüm sırası ve ilerleme doğru kalır.
6. Logo kutusunda contain, poster ve backdrop'ta doğru oran; hatada sabit yer tutucu.
7. DUAL etiketi ile parça listesi çelişince oynatıcı verisi geçerli olur.
8. Türkçe/İngilizce ses, iki İngilizce altyazı, altyazı kapalı, etiketsiz dil ve desteklenmeyen parça test edilir.
9. EPG hatası kanal oynatmayı engellemez; tvg-id yokken sahte rehber atanmaz.
10. Tek bağlantılı hesapta katalog gezinmesi veya afiş odağı ikinci yayın oturumu açmaz.

**Kalan doğrulamalar:** Gerçek Google TV'de ses/altyazı değiştirerek oynatma, altyazı senkronu, 5.1 ses çıkışı, HEVC performansı, AVI/MPG örnekleri, fiziksel TV’de EPG/oynatma geçişi, eksik veya yanlış poster eşleşmesi ve başka sağlayıcılarla uyumluluk.

## 11 Yeni EPG kanıtı · 14 Ekim 2026

Önceki günün alan incelemesi ve kısa EPG API hata sonuçları korunur. Ayrı tarihte tam XMLTV uç noktası HTTP 200 verdi. 402 kanal tanımı 252 farklı kimliğe indi; ayrıca yalnız program satırlarında bir kimlik bulunuyor. 10.648 programın 31’i geçersiz, 29’u tekrar: 10.588 program Android üretim okuyucu/SQLite yolunda saklanıp yeniden açıldı. 4.525 taze canlı katalog satırında 401 kimlik ve 1 ad eşleşmesi var; bunlar 252 farklı rehber kanalını temsil eder. 89 dolu EPG kimliği rehberde yok; 4.034 satır eşleşmiyor. Tahmini ad/kalite ekiyle düzeltme yapılmadı.

07:34 UTC sabit anında 48 katalog satırı / 26 farklı rehber kanalında çakışan program var; uygulama bunları kesin “şimdi” bilgisi saymaz. Bu tur iki metadata indirmesi, sıfır medya yayınıdır; fiziksel TV veya başka sağlayıcı onayı değildir. Saatler, yöntem, performans ve M3U özel rehber ayarı için [gerçek EPG incelemesi](PROVIDER_EPG_AUDIT.md), [sayısal kanıt](../project/provider-guide-qa.json).
