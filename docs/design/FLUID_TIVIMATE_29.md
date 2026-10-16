# Akıcılık ve TiviMate yapısı

16 Ekim 2026 · 0.14.29 tasarım önizlemesi · BKL-020.

Kullanıcı 0.14.28'i TV'de denerken her hareketin takıldığını, TiviMate'teki geçişlerin anında hissettirdiğini söyledi; TiviMate'in Canlı TV, Filmler, Diziler ve ayrıntı ekranlarını gösterip kategori mantığının ve listelemenin aynısını istedi; sol menüde Google'ın yuvarlak simgelerini, seçili olanın dolu halini ve menü açılınca içeriğin menü kadar sağa kaymasını istedi. Gece boyunca kendi kararımla ilerlememi, sabah akıcı bir sürüm görmek istediğini söyledi.

## Takılmanın nedenleri (ölçüm)

TV'de (MT5896, 2,4 GB, 1080p arayüz) 0.14.28 açıldıktan sonra `dumpsys gfxinfo`: 369 karenin %81,6'sı gecikmeli; 185 karede yavaş UI iş parçacığı, 303 karede yüksek giriş gecikmesi. Nedenler:

1. **Hata ayıklama derlemesi.** Bugüne kadar bütün teslim APK'ları `debug` türündeydi (DEBUGGABLE). Bu türde ART kodu tam derlemez, kütüphanelerin baseline profilleri uygulanmaz; Compose belirgin biçimde yavaştır. Teslim artık `release` türü: R8 ile küçültülmüş (18 MB → 3 MB), hata ayıklama kapalı, aynı imza anahtarıyla (TV'deki kurulumun üstüne veri silinmeden kurulur). Testler `debug` ile çalışmaya devam eder.
2. **Odakta büyüme animasyonu.** tv-material kart/düğme odak büyütmesi (1,025) her öğe için bileşim aşamasında 300 ms açılıp 500 ms kapanır; basılı tutulan kumandada aynı anda onlarca kart yeniden oluşturulur ([akıcılık rehberi](ANIMATION_SMOOTHNESS.md)). Büyüme kaldırıldı; odak renk ve 2 dp çerçeveyle anında görünür (TiviMate gibi).
3. **Her odak hareketinde tüm uygulama durumu.** Ana sayfada her hareket `homeFocusKey`'i uygulama durumuna yazıp ekranın tamamını yeniden oluşturuyordu. Odak belleği gözlemlenmeyen bir alana taşındı; kütüphanede de kart odağı yalnız bilgi panelini yeniden çizer.

Sol menü hareketi 240/180 ms'den 160/120 ms'ye (FastOutSlowIn) indi; içerik menü açıldıkça çizim katmanında sağa kayar (yeniden dizilmez, MOT04 korunur).

## Sol menü simgeleri

Simgeler Google Material Symbols Rounded'dan (Apache 2.0) alındı: search, home, live_tv, movie, video_library, star, settings. Bulunduğun bölüm dolu (FILL 1) ve nane renkli, diğerleri çizgi. Kaynak ve lisans notu `ui/MenuSymbols.kt` içinde.

## Canlı TV, Filmler, Diziler (TiviMate mantığı)

- **Kategori sütunu** menünün sağında: Canlı TV'de Favoriler, Tüm kanallar; Filmler ve Dizilerde Favoriler, Geçmiş, Tüm filmler/diziler; ardından sağlayıcının kategorileri **sağlayıcının sırasıyla** (Xtream kategori listesi kataloğa kaydedilir; eski kayıtlar ve M3U ilk görünüş sırasını kullanır). Bir kategoride kısa süre durmak onu gösterir, OK hemen gösterir (0.14.36'dan beri yalnız OK gösterir; menüden girişte odak kategori sütunundadır, D060).
- **Canlı TV:** numaralı satırlar (numara "Tüm kanallar" sırası, kategori değişse de aynı), logo, ad, şimdiki program ve süre çubuğu, sıradaki program. Üstte odaktaki kanalın bilgi paneli. TiviMate'teki görüntü önizlemesi eklenmedi: ikinci bir oynatıcı TV işlemcisinde akıcılığı bozar.
- **Filmler/Diziler:** üstte odaktaki başlığın bilgisi (ad, yıl, tür, kategori, konu, görsel), altında 5 sütunlu afiş ızgarası. Odak afişlere geçince kategori sütunu çekilir ve ızgara 7 sütuna çıkar (TiviMate gibi); ilk sütundaki afişten sol ok sütunu seçili kategoride geri getirir. Bu geçiş bir kez yeniden dizer, her odak hareketinde değil.
- **Odak:** içerikten sol ok seçili kategoriye, oradan sol ok menüdeki bölüme; menüden sağ ok seçili kategoriye, oradan sağ ok son odaklı karta/satıra döner.
- Bölüm içi arama, kaynak seçimi, Rehber, boş durumlar ve yenilemede kaybolan karttan komşuya geçiş korunur.

## Açık kalanlar (öneri)

- TiviMate'teki konum sayacı ("1 / 2176") ve dizi ayrıntısının sezon rafları 0.14.30'da uygulandı ([TIVIMATE_SEASONS_30](TIVIMATE_SEASONS_30.md)); sıralama düğmesi ve puan rozeti açık.
- Fiziksel TV'de yeni kare ölçümü, kullanıcı görsel kabulü ve Figma açık.
