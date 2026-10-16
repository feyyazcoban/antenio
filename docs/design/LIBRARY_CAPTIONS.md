# Kütüphanede eksik kapak ve yıl hizası

15 Ekim 2026 · 0.14.18 native önizleme.

Film ve dizi kütüphanesi, görsel adresi olmayan veya görsel yüklemesi başarısız olan kartlarda ortak film simgesini gösterir. Başlık yalnız afişin altında yer alır; aynı başlığın afişin içinde üç satıra bölünüp tekrar görünmesi kaldırılmıştır. Afişin 2:3 oranı ve kartın odak davranışı değişmez. Ortak Artwork bileşeninin diğer kullanımları bu değişiklikten etkilenmez.

Alt başlık iki satırlık alanını korur. Sonrasındaki yıl satırı, yıl yokken de bir satır yüksekliğindedir. Böylece aynı satırdaki kaynak etiketleri ve sonraki raf aralıkları eksik metadata nedeniyle yukarı kaymaz. Bilinmeyen yıl uydurulmaz ve yer tutucu tarih gösterilmez.

VisualPreviewTest.providerDividersRemainReachableAndPosterOnlyContentKeepsItsShape, yılı olan ve olmayan iki kartın yıl alanlarının üst kenarını ve yüksekliğini karşılaştırır. Kapaksız kartı odaklayıp görsel ile başlığın birlikte görünür kalmasını denetler. Mevcut test ayrıca ayırıcı kaydın erişilebilir kalmasını, aramadan bulunmasını ve detaydan geri dönüşü kontrol eder.

Bu kontrol teknik olarak başarılı ama yanlış filme ait kapağı algılamaz; bütün katalog sıralarının veya fiziksel TV'nin görsel kabulü değildir. Normal, 1,5 ve 2,0 yazı ölçeklerinde görüntü ve sonuçlar QA kaydındadır.
