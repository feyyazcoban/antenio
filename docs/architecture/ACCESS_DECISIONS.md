# Ortak erişim kararı

15 Ekim 2026 · SAFE01 uygulama temeli. `AccessPolicy.kt` JVM'de test edilen saf karar bölümüdür; TvViewModel veya UI henüz bunu çağırmaz. PIN doğrulaması, kalıcı politika ve izin oturumu uygulanmadığından mevcut APK'de PIN koruması yoktur.

## Girdi ve kapsam

- `AccessScope.Source`: kaynak kimliği.
- `AccessScope.Content`: mevcut `ContentRef(sourceId, localId)`; başlık veya URL kullanılmaz.
- `AccessScope.Category`: kaynak + içerik türü + tipli kategori anahtarı. `ProviderId` ile `M3uName` farklı tiplerdir; aynı metin birbirine eşit sayılmaz. Bölüm kategorisi bağımsız tanımlanmaz, doğrulanmış dizi bağlamından alınır.
- `AccessRule.hidden` ve `pinRequired` bağımsızdır. İkisi de false olan kural üst kapsam kilidini kaldırmaz. Kural listesi karar bölümüne alınırken kopyalanır, etkin kurallar kaynak bazında gruplanır.
- `revision` negatif olamaz. Henüz eski bekleyen işlemleri iptal eden bir oturum kontrolcüsü yoktur; alanın varlığı bu denetimi sağlamaz.

## Sonuçlar

| Karar | Normal yüzeyde görünür | Doğrudan açılabilir |
| --- | --- | --- |
| ALLOW | Evet | Evet |
| REQUIRE_PIN | Evet | Hayır |
| HIDDEN | Hayır | Hayır |
| UNRESOLVED | Hayır | Hayır |
| DENIED | Hayır | Hayır |

PIN sonucunu çağıran kod kendi başına ALLOW'a çevirmemeli; doğrulama ve hedefe bağlı izin oturumu ayrı kapıdır. Gizleme, bilinen kapsamların birleşiminde üstün gelir. Eksik kaynak, tutarsız scoped kimlik veya adult işareti DENIED üretir.

Xtream kategori kuralı varken eski kaydın categoryId'si bilinmiyorsa UNRESOLVED döner. Kaynak tipi değişince eski tipte kategori kuralı sessizce etkisizleşmez, ilgili türde UNRESOLVED döner. M3U kategori adları tam metin eşleşir; büyük/küçük harf veya Türkçe karakter dönüşümüyle farklı gruplar birleştirilmez. Ad değişimini otomatik takip ettiği iddia edilmez; kural yönetimi bu sınırlamayı ele almalıdır.

## Bölüm bağlamı ve çağıranın sorumluluğu

`SeriesContext`, tek bir yetkili repository/cache sonucundan gelen dizi ve bölüm listesini taşımalıdır. Karar bölümü kaynak/kind/scoped kimlik, bölüm üyeliği ve mevcut parentLocalId eşleşmesini kontrol eder. Eski null parentLocalId ancak doğrulanmış üyelik bağlamıyla çözülür. Aynı kaynakta etkin kural varken eksik/yanlış ebeveyn UNRESOLVED olur. Başka kaynağın kuralı korumasız eski bölümü etkilemez. Geçerli ebeveyn adult ise hiçbir kural olmasa da reddedilir.

Bu bölüm veritabanı otoritesi değildir. Çağıran güncel SourceRegistry'den kaynağı, güncel katalogdan hedefi ve güvenilir bölüm listesini çözmelidir. Sadece eski bir UI nesnesinin kimliğinin biçimsel olarak doğru olması, güncel olduğunu kanıtlamaz. Sahte üyelik listesini çağıranın üretmesine karşı bu sınıf tek başına koruma sağlamaz. Ekran entegrasyonunda canonical lookup zorunludur.

## Doğrulama ve kalanlar

AccessPolicyTest; kaynak ayrımı, aynı adlı kategori/farklı ID, tür ayrımı, ad değişimi, M3U ayrı anahtar tipi, eski eksik kategori, kaynak tipi değişimi, dizi mirası, yanlış ebeveyn, eski üyelik, adult reddi, kural sırası ve dışarıdan değiştirilen listeyi kapsar.

Bu testler PIN tahminine direnç, gizli verinin UI/arama/rehberden çıkarılması, asenkron eski hedefin iptali veya kalıcı saklama kanıtı değildir. Sıradaki işler: PIN/politika saklama ve kurtarma tasarımı; ardından canonical hedef çözümü, görünür katalog projeksiyonu ve PARENTAL_ACCESS tablosundaki bütün giriş kapıları. SettingsScreen'deki “PIN yönetimi henüz yok” açıklaması korunur.
