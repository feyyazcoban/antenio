# Yazarken arama sonuçları

15 Ekim 2026 · 0.14.27 tasarım önizlemesi · BKL-020.

> 0.14.28'de Ara ekranı TCL düzenine ve sol menüye geçti ([SIDE_NAV_SEARCH_28](SIDE_NAV_SEARCH_28.md)); bu belgedeki yazma kipi kuralları korunur, bölüm araması değişmedi.

Kullanıcı fiziksel TV'de (TCL/iFFALCON 65U95A) "Search ederken Result u göremiyorum" bildirdi; ardından bölüm içi aramanın ("Bu bölümde ara") da aynı sorunu yaşadığını belirtti. TV klavyesi ekranın alt yaklaşık %43'ünü kaplar (1080 piksellik ekranda yaklaşık 617. pikselden aşağısı). Ara ekranında başlık, tür çipleri ve durum satırı; bölüm aramasında başlık satırı ve kategoriler alanın üstünde yer kapladığı için ilk sonuç satırı klavyenin altında kalıyordu.

## Davranış

- **Yazma kipi.** Arama alanı odaktayken klavye açıksa ekran yazma kipine geçer. Klavyesini pencere boşluğu olarak bildirmeyen TV klavyeleri için alanda yazı değiştiyse de yazma kipi sayılır; klavye kendini bir kez bildirdiyse Geri ile kapanması tam düzeni hemen geri getirir.
- **Ara ekranı.** "Ne izlemek istersin?" başlığı, kaynak filtresi, tür çipleri ve durum satırı yazarken çekilir; alan menünün hemen altına çıkar, sonuç sayısı alan etiketine eklenir ("Kanal, film veya dizi ara · 14 sonuç").
- **Bölüm araması.** Bölüm başlığı, eylemleri ve kategoriler yazarken çekilir; alan menünün altına çıkar, sonuç sayısı etikete eklenir ("Bu bölümde ara · 8 sonuç"). Sonuçlar Ara ekranındaki satır kartını kullanır: küçük afiş/logo, başlık, mint tür satırı ve çok kaynakta kaynak adı; normal yazıda 3, büyük yazıda 2 sütun.
- **Dönüş.** Klavye Geri ile kapanınca veya odak alandan ayrılınca (aşağı ok, Bitti) normal düzen ve normal kartlar geri gelir.

Yeni renk, yazı tipi veya bileşen eklenmedi; satır kartı Ara ekranındaki sonuç kartının diliyle aynıdır.

## Düzeltilen çökme

İlk sürümde bölüm aramasında yazmaya başlamak uygulamayı kapatıyordu (`ClassCastException`, PilotApp.kt). `WindowInsets.isImeVisible` bir composable okuyucudur; `&&` zincirinin sağında yalnız bazı geçişlerde çağrıldığı için sonraki `remember` yuvaları kayıyordu. Değer artık her geçişte koşulsuz okunur; aynı gizli hata Ara ekranında da giderildi.

## Doğrulama ve açık kalanlar

Ayrılmış emülatörde (nextv_design_api36) Canlı TV, Filmler ve Ara ekranlarında klavye açıkken ilk sonuç satırının klavye üst kenarının üstünde kaldığı, Geri ile tam düzenin döndüğü ve çökme olmadığı görüldü. Testler QA kaydında. Fiziksel TV'de görünüm ve kullanıcı görsel kabulü açık; Figma değişmedi.
