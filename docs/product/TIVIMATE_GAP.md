# NEXTV ve TiviMate: rekabet farkı

18 Ekim 2026 · masa başı araştırma + kod okuması (0.14.42 ve çalışma ağacı). Öncelikler, sıralama ve fırsatlar **öneridir**, kullanıcı kararı değildir. TiviMate bilgilerinin bir kısmı yalnız üçüncü taraf kaynaklardan geliyor; bkz. "Doğrulanamayanlar".

## TiviMate bugün

- **Sürüm:** Son sürümler 5.3.2 (2 Haziran 2026) ve 5.3.3 (23 Haziran 2026, APKMirror). Resmî notlar yalnız "düzeltmeler" diyor.
- **Özellikler:** Resmî site catch-up, kayıt, çoklu ekran ve ebeveyn denetimini sayıyor. Mağaza açıklaması çoklu liste, zamanlanmış rehber güncellemesi, favoriler ve arama diyor. İçerik sağlamıyor; telefon ve tablet için optimize değil.
- **Premium:** Ayrı Companion uygulamasından satılıyor. Üçüncü taraflara göre yaklaşık 9,99 $/yıl ya da 33,99 $ ömür boyu, 5 cihaz (resmî olarak doğrulanamadı).
- **5.2.0 (Ağustos 2025):** Kullanıcılara göre AFR, çözünürlük geçişi, ses passthrough ve tünelli oynatma eklendi.
- **Kullanıcı görüşü:** Rehber düzeni ve özelleştirme övülüyor. Şikâyetler tamponlama, güvenilmez kayıt, timeshift ve cihazlar arası senkronizasyon eksikliği, Premium satın alma ve destek üzerine.

## 1. NEXTV'nin önde ya da eşit olduğu yerler

Film ve dizi sunumu TiviMate'in rehber merkezli yapısından daha modern:
- Sezon başına 16:9 bölüm rafları ve sayaç (D057).
- Kalan süreli "İzlemeye devam et" ve biten dizide sıradaki bölüm (D063, D065).
- İptal edilebilir geri sayımlı otomatik sonraki bölüm (BKL-026, `ui/EpisodeEndController.kt`).
- Sıralama ve sağlayıcı puanı (D061), geniş görselli vitrin (D067).

Başka güçlü yanlar:
- Birden çok kaynak ücretsiz; kaynak kimlikleri ayrı tutuluyor (BKL-027, 16 kaynak testli).
- Türkçe harf duyarlı arama (BKL-023) ve sadeleşmiş sağlayıcı adları (D065).
- Yetişkin kategorileri varsayılan olarak gizli; kategoriler tek tek açılıp kapanıyor (D068).
- Rehberde yaz saati geçişi ve saat dilimi seçimi; belirsiz eşleşme kanala atanmıyor (`data/EpgStore.kt`).
- Oynatma kurtarması sınırlı sayıda deniyor (BKL-025).
- Canlıda CH±, rakamla kanal ve rehber tuşu (D064, `ui/PlayerScreen.kt`).
- Kimlik bilgileri şifreli, analitik yok, veriler yedeğe gitmiyor.

Uyarı: bunlar kod ve emülatör düzeyinde. Fiziksel TV, görsel kabul ve p95 ölçümü açık (BKL-010/013).

## 2. Eksikler

| Özellik | TiviMate'te | NEXTV'de (kodda doğrulandı) | Öncelik | İş | Not |
|---|---|---|---|---|---|
| Ebeveyn PIN'i | Liste, rehber, grup ve kanal PIN'i | Doğrulayıcı, deneme sınırı ve şifreli depo testli; arayüze bağlı değil (BKL-028) | P0 | M | D068 gizlemesi kilit değil; Play hedef yaş beyanını etkiler |
| Oynatıcı üstünde kanal listesi, mini rehber, bilgi bandı | Oynatırken grup/kanal listesi | Yalnız CH±, rakam ve köşe notu | P0 | M | UX denetimi U1'in kalan kısmı |
| Son kanal | Açılışta son kanal ayarı | Yok; ana sayfada "Son izlediğin kanallar" rafı var | P0 | S | Önceki kanal tuşu TiviMate'te doğrulanamadı |
| EPG düzeltme: birden çok XMLTV, saat kaydırma, elle eşleme | Var (rehberlere göre) | Kaynak başına tek adres; M3U başlığından en çok 4 adres; önce tvg-id, yoksa ad; kaydırma ve elle eşleme yok (`data/EpgModels.kt`) | P0 | M | PRD EXT03 |
| Catch-up (sağlayıcı arşivi) | Var (Premium) | Yok; arşiv alanları ayrıştırılmıyor | P0 | M–L | Yalnız sağlayıcının sunduğu arşiv; nötr ad ("Geçmiş yayınlar") |
| Oynatıcı ayarları: AFR, en-boy/zoom, ses gecikmesi, arabellek, kod çözücü | AFR, passthrough, tünel (kullanıcı bildirimi) | Varsayılan ExoPlayer; ses/altyazı seçimi ve dil hafızası var | P0 (AFR, zoom), P1 (diğerleri) | M | Cihaza ve HDMI'ya bağlı; gerçek TV'de sınanmalı |
| Büyük liste performansı | Büyük listede yavaş yenileme şikâyeti | 63.175 kayıt JVM ve emülatörde test edildi; fiziksel p95 yok | P0 | M | Önce ölçüm |
| Liste ve EPG otomatik güncelleme | Zamanlanmış, aralık ayarlı | Açılışta ve 6 saatten eskiyse (`TvViewModel.kt`, `data/GuideController.kt`) | P1 | S–M | Arka plan işi tek bağlantı kuralını korumalı |
| Canlı duraklatma ve timeshift | Duraklat-sürdür; gerçek geri sarma en çok istenen eksik | Duraklat var; arabellekli geri sarma yok | P1 | L | Kayıttan ayrı yetenek (PRD §10) |
| Program hatırlatıcısı | Var | Yok | P1 | S–M | |
| EPG'de program araması | Var | Arama yalnız başlıklarda | P1 | M | |
| Favori grupları; kanal/grup sıralama, yeniden adlandırma, tek kanal gizleme | Gruplar, elle sıralama, kanal engelleme | Tek favori listesi; yalnız kategori gizleme (D068) | P1 | M | |
| User-Agent ve HTTP ayarları | Özel UA (rehberlere göre) | Sabit NEXTV UA ve M3U satırındaki UA/Referer | P1 | S | |
| Yedekleme ve geri yükleme | Var (Premium) | Yok; yedekleme bilinçli olarak kapalı | P1 | M | Kimlik bilgileri şifreli dışa aktarılmalı |
| Android TV ana ekranı (Watch Next/Play Next) | Doğrulanamadı | Yok | P1 | M | Fırsat |
| Kayıt (zamanlanmış, USB) | Var; güvenilirlik şikâyeti | Yok | P2 | L | Depolama ve ön plan hizmeti; "kendi kaynağın, kişisel kullanım" dili ([Play raporu 3.2](../project/PLAY_STORE_READINESS.md)) |
| Çoklu ekran | Var | Yok | P2 | L | Eşzamanlı bağlantı limiti; AGENTS paralel yayın kuralı |
| Resim içinde resim (PiP) | Var | Yok | P2 | S–M | Cihaz desteği değişken |
| Harici oynatıcı | Var | Yok | P2 | S | |
| Tema ve görünüm | Panel saydamlığı, zaman aşımı | Tek koyu tema, Hareketi azalt | P2 | S | |
| Stalker portalı | Var | Yok (PRD kapsam dışı) | P2 | M | |
| İş modeli | Ücretsiz + Premium (Companion), 5 cihaz | Karar yok (PRD §12) | P0 (karar) | — | Play Billing; ücretli sınırı kullanıcı belirler |

## 3. Farklılaşma fırsatları (öneri)

1. **Tek adımda satın alma:** uygulama içinde, Play Billing ile. TiviMate'te ayrı Companion uygulaması gerekiyor; aktivasyon ve destek şikâyetleri var.
2. **Hesapsız taşıma:** telefondan QR ile kurulum ve şifreli cihazdan cihaza aktarma (PRD EXT01/EXT05).
3. **Gerçek canlı timeshift:** 2026 dilek listelerinde en çok anılan eksik.
4. **Akıllı EPG:** rehber yalnız görünen kategoriler için indirilir; her kanalın eşleşme durumu görünür ve tek tuşla düzeltilir.
5. **VOD'u Google TV ana ekranına taşımak:** "Devam et" ve sıradaki bölüm Watch Next'te (D063, BKL-026).
6. **Kalite çeşitlerini birleştirmek:** HD/FHD/4K tek kanalda; sorun olunca yedek akış (D065'te açık).
7. **Şeffaf oynatma:** anlaşılır hata, isteğe bağlı codec/bit hızı bilgisi.
8. **Türkçe öncelikli deneyim:** arama, ad sadeleştirme, Türkçe boş ve hata durumları (D065, BKL-023).

Önerilmeyen: debrid ve içerik bulma entegrasyonları; telif ve Play riski taşır ([Play raporu 3.2](../project/PLAY_STORE_READINESS.md)).

## 4. Önerilen ilk 10 iş (öneri sırası)

1. PIN'i Ayarlar'a, kategori/kanal korumasına ve bütün erişim noktalarına bağlamak (BKL-028).
2. Oynatıcıya kanal listesi ve mini rehber, kanal geçişinde bilgi bandı, son kanal tuşu.
3. 65U95A'da codec, ses, altyazı ve büyük liste p95 ölçümü (BKL-010/013); sonraki oynatıcı işleri buna dayanır.
4. EPG düzeltme: birden çok XMLTV, kaynak başına saat kaydırma, elle eşleme, görünür eşleşme durumu (EXT03).
5. Catch-up: Xtream arşivi ve M3U catchup özniteliğiyle rehberden geçmiş program; yetenek yoksa düğme görünmez (EXT02).
6. Oynatıcı ayarları: AFR, en-boy/zoom, ses gecikmesi, arabellek.
7. Liste ve EPG için arka planda zamanlanmış güncelleme ve aralık ayarı.
8. Favori grupları; kanal/kategori sıralama, yeniden adlandırma, tek kanal gizleme.
9. Program hatırlatıcısı ve EPG'de program araması.
10. Watch Next/Play Next entegrasyonu.

Ön koşul (kullanıcı kararı): gelir modeli ve ücretsiz/ücretli sınırı.

## 5. Doğrulanamayanlar

- Resmî fiyat, 5 cihaz sınırı ve ücretsiz/Premium sınırı: yalnız üçüncü taraf ve satış ortaklığı sitelerinden. Google Play ve Companion sayfaları araçla okunamadı; tivimate.com yalnız başlık döndürdü.
- 5.2.0'daki AFR, passthrough ve tünel: yalnız kullanıcı bildirimi.
- Çoklu ekranda en çok kaç ekran olduğu, VOD'da devam ve sonraki bölüm, Watch Next, ağ sürücüsüne kayıt, özel UA menüsü, yeniden adlandırma, önceki kanal tuşu.
- EPG saat kaydırma ve elle atama: düşük güvenilirlikli rehberlerden.
- r/TiviMate doğrudan okunamadı; topluluk görüşleri TROYPOINT Insider ve inceleme sitelerinden (çoğu satış ortaklığı içeriği). Trustpilot profili sahiplenilmemiş bir alan adına ait.
- NEXTV tarafı kod okunarak doğrulandı, çalıştırılmadı; ExoPlayer passthrough davranışı gerçek TV'de görülmedi.

## 6. Kaynaklar

- https://tivimate.com/
- https://play.google.com/store/apps/details?id=ar.tvplayer.tv
- https://www.apkmirror.com/apk/ar-tvplayer/tivimate-iptv-player/
- https://troypoint.com/tivimate-iptv-player/
- https://troypointinsider.com/t/tivimate-feature-wish-list/153806
- https://troypointinsider.com/t/tivimate-pause-rewind-fast-forward/78857
- https://tuneline.app/blog/tivimate-cross-platform-alternatives-2026
- https://litiptv.com/blog/tivimate-review-guide
- https://uk.trustpilot.com/review/tivimate.org
- https://developer.android.com/training/tv/discovery/recommendations
