# Bölüm kartlarının okunabilirliği

15 Ekim 2026 · Android 0.14.22 · TV-041 / BKL-020.

Önceki tek satırlı başlıklarda, özellikle 2.0 yazı ölçeğinde dizi adı ve S01E01 gibi sağlayıcı öneki bölüm adını görünmez bırakıyordu. Sağlayıcı başlığını tahmini kurallarla silmek yerine iki satıra yer açıldı. Başlık 12/17 sp, iki sabit satır; sezon/bölüm ve varsa Devam et bilgisi 10/14 sp, iki sabit satır kullanır. Çok uzun başlıklar yine üç noktayla kısalabilir; sınırsız metin gösterildiği iddia edilmez.

Kartın metin kısmı ortak Container yüzeyinde, odakta High yüzeyindedir. Böylece arka plan fotoğrafı metin kontrastını değiştirmez. Metin yatay iç payı 10 dp, alt pay 10 dp; farklı uzunluktaki adlar bilgi satırını kaydırmaz. Görsel 2:1 ve mevcut üç sütun korunur. Eksik kapak içinde başlık tekrar edilmez; simge ve kart altındaki başlık kalır.

Mevcut CardVisibility kartın tamamını görünür alana taşır. Daha uzun kart nedeniyle üst açıklama/eylemlerin yukarı kayması normaldir; odaktaki bölüm, fotoğrafı ve metni birlikte görünür kalmalıdır. Kimlikler, oynatma ve sezon seçimi değişmez.

[Büyük yazı görünümü](../../design/reviews/episodes-0.14.22/font-2.0/series-episodes.png). QA kaydı docs/project/native-episode-cards-22-qa.json içindedir. Figma ve fiziksel TV onayı değildir.

## Eksik veri kontrolü

Aynı sırada çözülemeyen kare, uzun ve kısa başlık ile 45 saniyeden devam edilecek bölüm normal/1.5/2.0 yazıda kontrol edildi. Metadata üst hizası 1 dp tolerans içinde, tüm seçili kartlar ekranda, placeholder başlığı tekrarlamıyor. [Görsel](../../design/reviews/episodes-0.14.22/font-2.0/series-mixed-episodes.png) · [QA](../project/native-episode-mixed-qa.json). Bu ek kontrol üretim kodunu değiştirmedi.
