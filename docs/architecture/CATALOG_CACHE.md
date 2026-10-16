# Katalog önbelleği sözleşmesi

## 0.14.19: sürüm 3 kimlik uzantısı

Yeni yazımlar version=3 kullanır; okuyucu 1/2/3 kabul eder. Grup bildirimi yine tek elemanlıdır; içerik satırı tam 22 elemanlıdır. Önceki 20 alan değişmez. Alan 20 categoryId (String, bilinmiyorsa boş), alan 21 parentLocalId (String veya null, kaynak içindeki dizi kimliği) olarak eklenir. V1/v2 okuyucu bu alanları boş/null bırakır; kategori veya ebeveyn uydurmaz. Sabit catalog-v2.jsonl ve catalog-v1.jsonl örnekleri güncel encoder’dan bağımsızdır. AES-GCM bağlamı, AtomicFile ve boyut sınırları değişmez. V3 kayıtlarını eski APK okuyamaz; aşağı sürüme dönüş desteklendiği iddia edilmez.

Xtream ayrıştırıcı category_id değerini ve bölümler için ait olduğu dizinin yerel kimliğini korur. M3U bölümleri mevcut grup anahtarını parentLocalId olarak taşır; sağlayıcı kategori kimliği uydurulmaz. İçerik id/sourceId/localId değerleri değişmez. Kategori adı değişimi, aynı adlı kategoriler ve kaynak ayrımı JVM testlidir. Bu alanlar PIN korumasını tek başına uygulamaz.

Aşağıdaki 0.14.14 açıklaması tarihli v2 sözleşmesidir.

15 Ekim 2026 · Android0.14.14 · D044.

Katalog halen özel uygulama alanında, hesap bağlamıyla doğrulanan AES-GCM şifreleme ve AtomicFile yazımıyla saklanır. Şifreleme bağlamındaki `catalog-v1` adı değişmedi; bu, açılmış JSON başlığındaki veri biçimi sürümünden bağımsızdır. Yeni yazımlar başlıkta sürüm2 kullanır; okuyucu sürüm1 ve2 kabul eder. Eski kayıtlar okumak için yeniden indirilmez. Sonraki başarılı kayıt yeni biçimde yazılır. Eski uygulama sürümlerinin yeni biçimi okuyabildiği iddia edilmez; geriye uygulama sürümü düşürme testi yoktur.

## Biçim

UTF-8 satırları gzip ile sıkıştırılır. İlk satır JSON nesnesidir: version,updatedAt,warnings,epgUrls. Başlık metaverileri korunur. Sürüm1 satırları eski group/entry nesneleri olarak okunur. Sürüm2 bir grubu `[group]` satırıyla bildirir; böylece boş bölüm grupları da korunur. İçerik satırı tam20elemanlı dizidir:

| İndeks | Alan |
| --- | --- |
| 0 | Bölüm grubu; ana katalogda null |
| 1–5 | id,providerId,kind,title,category |
| 6–9 | artwork,backdrop,description,year |
| 10–13 | durationSeconds,extension,uri,headers |
| 14–17 | season,episode,adult,episodeOrderKnown |
| 18–19 | epgId,epgName |

Hesapla kapsamlandırılmış sourceId/localId cache içine yazılmaz; yüklerken SourceLibrary tarafından eklenir. Alan sırası kalıcı sözleşmedir; değişiklik yeni sürüm gerektirir. Eksik/fazla alanlı satır, bilinmeyen sürüm, geçersiz tür veya null grup bildirimi reddedilir. JSON nesne anahtarlarının her kayıtta tekrarlanması kaldırılmıştır; görsel, başlık ve medya bilgileri silinmemiştir.

## Koruma ve uyumluluk

150bin satır ve160MiB açılmış veri sınırı,64MiB şifreli dosya sınırı korunur. V2satır sayısı/açılmış veri taşması testlidir. Gzip bütünlüğü, AtomicFile ve hesap bağlamlı şifreleme değişmedi. readEpgFeeds aynı doğrulanmış verinin yalnız başlığını açar ve iki sürümü tanır.

Sabit `catalog-v1.jsonl` fixture'ı güncel encoder'dan türetilmez. Eski rehber/bölüm sırası alanları olmayan katalogların okunması, boş grup ve bilinmeyen bölüm sırası davranışı bu örnekle kontrol edilir. Güncel biçim bütün alanlar/Unicode/adres başlıkları/EPG/boş grup ile eşitlik testinden geçer. Android testinde v1şifreli kayıt→v2yazım, rehber başlığı, bozuk şifreli veri ve başka hesabın kaydı kontrol edilir.

Önceki CompactCatalogExperimentTest tarihli denemedir; üretim encoder'ı artıkv2olduğundan karşılaştırması anlamını yitirmiştir ve açıklamalı Ignore ile kapalıdır. Güncel ölçüm CatalogStageProbeTest ve LargeCatalogProbeTest ile yapılır. [Teslim ve ölçümler](../project/native-compact-cache-qa.json).
