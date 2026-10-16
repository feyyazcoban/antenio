# Katalog aşama profili ve biçim denemesi

15 Ekim 2026 · Üretim0.14.13 değişmedi. İki tanısal test, yalnız ayrılmış API36 emülatöründe geçti. Gerçek sağlayıcı ve kullanıcı hesabı kullanılmadı.

## 60 bin kayıt aşama tanısı

| İşlem | Üç örnek aralığı, ms | Medyan, ms |
| --- | --- | --- |
| JSON/gzip yazımı | 2080–2168 | 2168 |
| JSON/gzip çözümü | 1901–1998 | 1905 |
| Şifreli kayıt (yazım dahil) | 2075–2205 | 2191 |
| Doğrulanmış başlık okuma | 41–54 | 44 |
| Şifreli okuma ve tam çözüm | 1975–2001 | 1988 |
| Kaynak kimliği birleştirme | 63–193 | 76 |
| Ana sayfa listelerini hazırlama | 55–245 | 64 |
| Arama indeksi hazırlama | 393–527 | 403 |
| Dar sorgu | 25–95 | 35 |
| Geniş sorgu | 33–38 | 36 |

Kayıt çözümü ölçülen işler arasında belirgin maliyettir. Şifreli okuma tam çözümü de içerir; JSON çözümüyle toplanmaz. Başlık okuma da tam şifreli veriyi doğrular ancak bütün kayıtları oluşturmaz. Örnekler ardışık olduğundan JIT, bellek tahsisi ve önbellek etkileri taşınır. İlk örnek soğuk açılış değildir. Bu ölçümler önceki911msilk UI sorgusunun doğrudan açıklaması değildir: indeks önceden hazırlanır ve UI senkronizasyonu ayrı maliyettir.

## Üretim dışı dizi satırı denemesi

CompactCatalogExperimentTest, aynı60bin kaydın her alanını eşitlikle doğrulayarak nesne anahtarları yerine sıralı JSON dizi alanlarını denedi. Null kapak/adres, Unicode, satır sonu, tırnak, başlıklar, sezon/bölüm, yetişkin işareti, bölüm sırası ve EPG alanları çeşitlendirildi. Üç geçişte karşılaştırma sırası dönüşümlü değişti. Seri bölümler katalog girdisi olarak vardır; episode-group kapsayıcı biçimi bu prototipte yoktur.

Nesne çözümü medyanı **2222ms**, dizi çözümü **1152ms**: bu denemede yaklaşık **%48 daha az süre**. Sıkıştırılmış boyut1493571bayttan1241057bayta indi. Bunlar gerçek uygulama açılış hızlanması olarak sunulmaz. Daha zengin ikinci veri seti nedeniyle ilk tablonun kayıt çözümüyle doğrudan karşılaştırılmaz.

## Sonraki uygulama ve kabul ölçütleri

Sürüm numaralı kompakt biçim üretime alınmadan önce: mevcutv1kayıtlar okunmalı; bütün alanlar, boş bölüm grupları, uyarılar, EPG adresleri ve zaman damgası korunmalı; bozuk/bilinmeyen sürüm ve kesilmiş veri reddedilmeli;150bin kayıt/160MiB açılmış veri/64MiB şifreli dosya sınırları, hesap ayrımı, kimlik doğrulama ve atomik yazım değişmemeli. readEpgFeeds yeni başlığı da anlamalı. Mevcut katalog/EPG/bölüm sırası birim testleri, cihazda şifreli cache ve süreç yeniden açılışı tekrar doğrulanmalı. Aynı tam aşama tanısı ve UI büyük-katalog testiyle gerçek fark ölçülmeli.

Prototipin koruma ve uyumluluk kapsamı üretim için yeterli değildir; yalnız test kaynaklarında bulunur. Bu tur üretim değişikliği, yeni APK, Figma veya TV kurulumu yok. [Ham sonuçlar ve kapsam](native-catalog-stage-qa.json).
