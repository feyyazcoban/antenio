# Araştırma planı

Görsel araştırmalar MOTION ve COLOR belgelerinde; rakip özellikleri PRD bölüm 3-4'te. Aşağıdaki araştırmalar ürünün güvenilirliğini ve geliştirme sınırlarını netleştirir. Durumlar yapılmamış işi tamamlanmış göstermez.

| ID | Öncelik | Cevaplanacak soru | Yöntem ve çıktı | Durum |
| --- | --- | --- | --- | --- |
| RCH01 | P0 | M3U/Xtream hangi alanları gerçekten sağlıyor? | Kullanıcının iki listesi ve aynı sunucu API'si; alan dolulukları, varlıklar, dil ve altyazı kanıtı | İlk kaynak incelemesi tamamlandı; SOURCE_GUIDELINES |
| RCH02 | P0 | Kumandayla kullanıcı yolunu kaybediyor mu? | En az 5 kişiyle kaynak ekleme, EPG, film, dizi ve Back görevleri; hata ve tuş sayısı | Planlandı |
| RCH03 | P0 | Kaynak bozulduğunda oynatıcı ne yapmalı? | Tek oturumla kanal değiştirme, ağ kopması, HLS/TS/VOD ve seek; yetenek matrisi | Planlandı |
| RCH04 | P0 | Büyük katalog düşük bellekli TV'de kullanılabilir mi? | 1k/10k kanal, 50k VOD sentetik veri; arama, önbellek, afiş ve yenileme ölçümü | Planlandı |
| RCH05 | P0 | EPG ve içerik eşleştirmesi ne kadar güvenilir? | tvg-id, zaman dilimi, aynı adlı içerik, sezon eksikleri; normalleştirme sözleşmesi | Planlandı |
| RCH06 | P0 | Altyazı ve ses tercihi akış değişince korunuyor mu? | Etiketli/etiketsiz, çok dilli, forced/SDH örnekleri; seçim ve geri dönüş kuralları | Planlandı |
| RCH07 | P0 | Renk ve hareket salonda okunaklı ve rahat mı? | Standart TV modu, gündüz/loş ortam, düşük donanım, hareketi azalt; ölçüm raporu | Rehber hazır; cihaz testi bekliyor |
| RCH08 | P1 | LG ve Samsung hangi farkları gerektiriyor? | Gerçek webOS/Tizen cihazında akış, kumanda, depolama, model yılı denemesi | Planlandı |
| RCH09 | P1 | Uzun hesap girişi kullanıcıyı zorluyor mu? | TV klavyesi ile QR eşleştirme prototip karşılaştırması; değer/maliyet kararı | Planlandı |
| RCH10 | P1 | Kullanıcı neye ödeme yapar? | Pilot görüşmeleri, mağaza konumlandırması ve paket hipotezleri | Planlandı |
| RCH11 | P1 | Kaynak sırları ve geçmiş nasıl taşınmalı? | Yerel güvenli saklama, silme, isteğe bağlı hesap ve senkronizasyon çatışmaları | Planlandı |

## Önce yapılacak üç iş

1. Gerçek kaynak alanlarından küçük, kimliksiz bir örnek veri sözleşmesi çıkarmak.
2. TV kumandası için kaynak ekleme, canlı kanal açma, film ve diziye devam etme yollarını prototiplemek.
3. Oynatıcıyla aynı anda katalog kaydırırken performansı temel cihazda ölçmek.

Tek bir sağlayıcının sonucu tüm M3U veya Xtream ekosistemine genellenmez. Test içeriğinin erişim yetkisi ve medya koşulları her kaynakta ayrı doğrulanır.

## Araştırma kaydı biçimi

Soru, tarih, kaynak/sürüm, yöntem, örneklem, gözlem, yorum, ürün kararı, belirsizlik ve sonraki test. Ürün beyanı, doğrudan ölçüm ve tasarım önerisi ayrı alanlardır.
