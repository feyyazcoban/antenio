# TiviMate yapısı: Canlı TV, Filmler, Diziler (referans ve öneri)

> 0.14.29'da kategori sütunu, sağlayıcı sırası, numaralı canlı satırları, bilgi panelleri ve ızgara kipi uygulandı ([FLUID_TIVIMATE_29](FLUID_TIVIMATE_29.md)); 0.14.30'da dizi ayrıntısının sezon rafları ve ızgaradaki konum sayacı ([TIVIMATE_SEASONS_30](TIVIMATE_SEASONS_30.md)). Sıralama, puan rozeti ve canlı önizleme açık.

16 Ekim 2026 · Kullanıcı TV'de TiviMate'i açıp Canlı TV, Filmler, Diziler, odaklı ızgara ve dizi ayrıntısı ekranlarını gösterdi; kategori sistemi ve listelemenin aynı mantıkla kurulmasını istedi. Ekran görüntüleri gerçek sağlayıcı içeriği gösterdiği için depoya alınmadı; burada yalnız yapı ve mantık var. TiviMate'in logosu, adı veya renkleri kullanılmayacak; uygulama bizim Ink, Roboto ve odak dilimizle yapılacak. Aşağıdakiler henüz uygulanmamış **öneridir**.

## Ortak kategori mantığı

- Kategori sütunu, sol menünün sağında dikey bir listedir. En üstte sanal kategoriler, altında sağlayıcının kategorileri **sağlayıcının kendi sırasıyla** (alfabetik değil) gelir.
  - Canlı TV: Favoriler, Tüm kanallar, sonra sağlayıcı kategorileri (ör. TURK SPOR, TURK ULUSAL…).
  - Filmler: Listem (favoriler), Geçmiş, Tüm filmler, sonra sağlayıcı kategorileri.
  - Diziler: Listem, Geçmiş, Tüm diziler, sonra sağlayıcı kategorileri.
- Seçili kategori dolu vurgu ile gösterilir; kategoride gezinmek listeyi hemen değiştirir.
- Bizde bugün kategoriler alfabetik sıralanıyor (`categories ... .sorted()`) ve Favoriler ayrı bir menü bölümü. Sağlayıcı sırası için Xtream `get_*_categories` sırası ve M3U'da `group-title` ilk görünüş sırası saklanmalı.

## Canlı TV

- Sağ üstte seçili kanalın küçük önizlemesi ve şimdiki program (başlık, saat aralığı, ilerleme).
- Altında tarih/saat başlığı ve zaman cetveli; her satırda **kanal numarası**, logo, ad ve aynı satırda programlar (rehber satırı).
- Numara "Tüm kanallar" sırasındaki yerdir (Xtream `num` veya oynatma listesi sırası); kategori seçilince numaralar değişmez.
- Bizdeki Rehber ekranı bu satır yapısının çoğuna zaten sahip; Canlı TV, rehber satırları + kategori sütunu + önizleme olarak birleştirilebilir.

## Filmler ve Diziler

- Üstte odaktaki başlığın bilgisi: ad, yıl · süre/sezon · türler, puan rozeti, oyuncular, yönetmen, konu; arkada başlığın görseli.
- Altında seçili kategorinin adı ve afiş ızgarası (kategori sütunu açıkken 5, odak ızgaraya geçince 7 sütun); afişte puan rozeti, altında ad.
- Odak ızgaraya geçince menü ve kategori sütunu gizlenir, ızgara tam genişliğe çıkar; başlıkta "1 / 2176" gibi konum sayacı, sağ üstte arama ve sıralama.

## Ayrıntı (dizi)

- Aynı üst bilgi bölümü; altında "Sezon 1" başlığı, "1 / 10" sayacı ve 16:9 bölüm rafı ("E1. …" başlıkları). Odak doğrudan bölümlere gelir.

## Hız

Kullanıcı TiviMate geçişlerinin anında hissettirdiğini belirtti. 0.14.29'da sürüm derlemesine geçildi ve menü hareketi kısaltıldı; yeni ekranlar da kısa, düzen yeniden kurmayan geçişlerle yapılmalı ([akıcılık rehberi](ANIMATION_SMOOTHNESS.md)).
