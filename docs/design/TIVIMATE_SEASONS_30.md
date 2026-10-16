# Sezon rafları ve konum sayacı

16 Ekim 2026 · 0.14.30 tasarım önizlemesi · BKL-020 · karar D057.

0.14.29'da açık kalan iki TiviMate parçası ([referans](TIVIMATE_REFERENCE.md), [0.14.29](FLUID_TIVIMATE_29.md)): dizi ayrıntısının sezon başına tek yatay bölüm rafı ve ızgaradaki konum sayacı. TiviMate'in adı, logosu ve renkleri kullanılmadı; Ink renkleri, Roboto ve mevcut odak dili (renk + 2 dp çerçeve, büyüme yok, D056) korunur.

## Dizi ayrıntısı

- Üstte mevcut bilgi bölümü (başlık, yıl · sezon sayısı, açıklama, Oynat/Devam et, Favori, Açıklama) değişmedi.
- Altında her sezon için bir raf: "Sezon N" ve hemen sağında, kütüphane başlığındaki gibi "2 / 10" konum sayacı (arka plan görselinin parlak sağ kenarında değil, koyu tarafta okunur), altında tek satır 16:9 bölüm kartları (196 dp, ana sayfa yatay kartıyla aynı genişlik, 20 dp aralık; yan menünün yanında dört kart). Kartta "E1." ve sağlayıcının bölüm adı (iki satır), altında süre ve "Devam et"/"İzlendi"; yarım kalan bölümde görselin altında ilerleme çizgisi. Eski "Bölümler" başlığı ve sezon düğmeleri kaldırıldı.
- **İlk odak bölümde:** oynatıcıdan dönüşte oynatılan bölüm, değilse Oynat'ın başlatacağı bölüm (yarım kalan, bitmişse sıradaki, hiç izlenmemişse ilk). Hedef ilk sezonda değilse ekran o sezonun rafında açılır; ilk sezonda bilgi bölümü yerinde kalır. Bölüm yoksa ya da yükleme hatasında odak eskisi gibi Tekrar dene/Geri'dedir.
- **Yukarı/aşağı sezonlar arasında:** her raf son odaklanan bölümünü hatırlar; Aşağı/Yukarı bir rafa o bölümle girer (hiç girilmemiş rafta ilk bölüm). Sayaç bu hatırlanan sırayı gösterir. İlk raftan Yukarı eylem düğmelerine, oradan Geri'ye çıkar; bilgi bölümü sığıyorsa bütünüyle görünür.
- Kart odaklanınca kart ve rafın başlığı birlikte görünür kılınır, ama ana sayfadaki gibi raf ekranın üstüne sabitlenmez (`frameShelf = false`): ilk raf bilgi bölümünün altında kalır.

## Kütüphane ızgarası

- Normal yazıda başlık satırı (kategori adı, sayı, kaynak filtresi, Bu bölümde ara, canlıda Rehber) artık ızgaranın üstünde sabit: afişler altından kayar, başlık ve sayaç görünür kalır (TiviMate gibi). Büyük yazıda (1,5 ve üstü) başlık eskisi gibi kartlarla kayar; sabit başlık odaklı kartın etiketlerine yer bırakmazdı.
- Odak afiş ızgarasındayken başlıktaki "80 içerik" yerine "9 / 80": odaktaki afişin kategorideki sırası. Kategori sütununa ya da başlık düğmelerine geçince yine "N içerik". Canlı satırlarda sayaç yok (ızgara kipi değil).
- Yenileme odaktaki kartın yerini değiştirirse sayaç ve sol kenar hesabı yeni sırayı alır.

## Akıcılık

- Sayaçlar (`PositionNote`) konumu yalnız kendi içinde okur: kartlar arasında gezinmek yalnız o metni yeniden oluşturur; ekran, bilgi paneli dışındaki başlık ve raflar yeniden oluşturulmaz. Rakamlar sabit genişlikli (tnum), basamak sayısı değişmedikçe başlığın yerleşimi yeniden hesaplanmaz.
- Raf kaydırma konumları, hatırlanan bölüm ve kart odak istekleri Compose durumunun dışında tutulur (kütüphanedeki odak belleği gibi); yalnız sayaç değeri gözlenir.
- Yeni animasyon eklenmedi; kaydırma platformun kesilebilir varsayılanı, Hareketi azalt'ta da aynı yol.

## Açık

- Fiziksel TV'de görünüm, kare ölçümü ve kullanıcı kabulü; Figma.
- TiviMate'teki sıralama düğmesi ve puan rozeti uygulanmadı; dizi ayrıntısında sağlayıcı bölüm adından dizi adı/SxxEyy temizliği yapılmadı.
