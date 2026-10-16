# Sol menü, TCL düzeninde Ara ve çıkış sorusu

15 Ekim 2026 · 0.14.28 tasarım önizlemesi · BKL-020.

Kullanıcı fiziksel TV'de iki uygulamanın ekranını gösterip "bizim stilde" uyarlanmasını istedi: TCL'nin arama ekranı (üstte geniş arama alanı, altında tek yatay sonuç rafı, klavye altta) ve NEXTV'nin sol menüsü (kapalıyken simge şeridi, odakta yazılı menü). Ayrıca ana sayfada Geri'ye basınca uygulamadan çıkmadan önce sorulmasını istedi. Örnek ekran görüntüleri kullanıcının TV'sinden alındı ve depoya konmadı; başka uygulamaların görseli, logosu veya renkleri kullanılmadı.

## Sol menü

- **Kapalı:** 72 dp simge şeridi. Üstte nane "›" logosu; ortada Ara, Ana Sayfa, Canlı TV, Filmler, Diziler, Favoriler (yıldız); en altta Ayarlar. Bulunduğun bölüm `Ink.Selected` daire içinde nane renkli simgeyle gösterilir.
- **Açık:** Menü odak alınca 208 dp'ye açılır (240 ms; kapanış 180 ms; Hareketi azalt'ta anında), yazılar ve "IPTV" görünür. Menü içeriğin üstüne açılır, içerik yeniden dizilmez (MOT04); görünür içerik `Ink.Background` %55'e kadar kararır, menü kenarı 40 dp'lik bir geçişle solar. Uzun bir etiket sığmazsa (eski Android'de 2.0 yazı) menü gerektiği kadar genişler (D029).
- **Odak:** Odaklanmak ekran değiştirmez, OK açar. Sol ok içerikteki ilk sütundan menüye girer; menüye her giriş (sol ok, menüye dönen Geri) bulunduğun bölüme düşer. Yukarı/aşağı öğeler arasında gezer, uçlarda durur; sol ok menüde bir şey yapmaz. Sağ ok içerikte en son odaklı öğeye döner; boş Rehber'de "Şimdi"ye gider.
- **Stil:** Mevcut gezinme dili korunur: odaktaki öğe açık hap (`Ink.Text` zemin, koyu yazı, 2 dp çerçeve), Roboto Medium 13 sp, `LineIcon` simgeleri. Simgeler iki durumda da aynı yerde kalır.
- **Kapsam:** Ana sayfa, Kütüphane, Ara, Rehber ve Ayrıntı menülü ekranlardır. İçerik 72 dp'den başlar ve kendi 24 dp kenarını korur. Ayrıntı ekranında arka plan görseli üst kenara kadar uzanır. Kurulum, Kaynaklar, Yükleme, Kurtarma ve Oynatıcı menüsüz kalır.

## Ara (TCL düzeni)

- **Alan:** En üstte geniş hap biçimli arama alanı; içinde büyüteç ve "Kanal, film veya dizi ara" ipucu. Yanında yazı varken Temizle; klavye kapalıyken kaynak seçimi.
- **Sonuçlar:** Izgara yerine türe göre yatay raflar ("Filmler · 8", "Canlı TV · 6"). Raflar alaka sırasını izler: en iyi eşleşmenin türü ilk raftır ve o eşleşmeyle başlar. Kartlar Ara'daki mevcut satır kartıdır (260 dp, büyük yazıda 380 dp; kenarda yalnız sonraki kartın görseli görünür).
- **Klavye açıkken:** Tür çipleri ve kaynak seçimi çekilir; ilk raf alanın hemen altında, TV klavyesinin üstünde kalır (normal yazıda ilk rafın alt kenarı yaklaşık 240 dp; klavye yaklaşık 308 dp'den başlar). Sonuç yoksa mesaj alanın altında görünür. Klavye Geri ile kapanınca tam düzen hemen döner.
- **Korunan davranış:** Türkçe arama, tür ve kaynak filtresi, Bitti ile sonuca geçiş, ayrıntıdan dönüşte aynı karta odak, yenilemede kaybolan karttan komşuya geçiş, Geri sırası sonuçlar → menüde Ara → Ana sayfa.

## Çıkış sorusu

Ana sayfada Geri "Uygulamadan çıkmak istiyor musunuz?" sorusunu açar. Seçenekler Vazgeç ve Çık; yanlışlıkla çıkmamak için odak Vazgeç'te başlar. Geri ve Vazgeç soruyu kapatır, Çık uygulamayı kapatır. Soru açılırken art arda gelen ikinci Geri uygulamadan sormadan çıkmaz.

## Açık kalanlar

- Rehber ızgarasında sol ok menüye ancak saklama penceresinin başında (dün 00:00) ulaşır; menüye hızlı yol Geri'dir ("Geri: menü" ipucu). Rehberin sol kenar davranışı ayrı bir tasarım kararı gerektirir.
- Boş aramada TCL'deki gibi bir öneri rafı yok; mevcut "Bir başlıkla başla" mesajı korunur.
- Fiziksel TV görünümü, kullanıcı görsel kabulü ve Figma açık.
