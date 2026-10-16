# Arayüz kare süresi tanısı

15 Ekim 2026. `tools/analyze_framestats.py` kaydedilmiş tek bir `dumpsys gfxinfo ... framestats` çıktısını çevrimdışı inceler. Cihaza bağlanmaz, uygulamayı açmaz, test kaynağı yüklemez ve dosya silmez. Bu aracın testleri geçmiştir; henüz yeni bir gerçek cihaz ölçümü alınmış değildir.

## Dayanak ve ölçüm anlamı

[Android dumpsys belgesi](https://developer.android.com/tools/dumpsys) gfxinfo framestats çıktısını yakın geçmişteki kare zamanlarının tanısı için sunar ve Macrobenchmark’a yönlendirir. Bu çıktı bütün 60 saniyelik bir senaryoyu kendiliğinden kapsamaz.

[AOSP tarihli biçim açıklaması](https://android.googlesource.com/platform/frameworks/base/+/562ae3a/docs/html/training/testing/performance.jd) nanosecond zaman damgalarını, Flags=0 için FrameCompleted−IntendedVsync hesabını ve normal kare karşılaştırmasında sıfır olmayan bayrakları dışarıda tutmayı açıklar. Bu nedenle ilk kare/açılış bu rapordan çıkarılamaz; dışlanan sayılar ayrıca gösterilir. [AOSP CTS](https://android.googlesource.com/platform/cts/+/1dd5219231180f3f3d4a28ffd260d0e80d2e390d/hostsidetests/dumpsys/src/android/dumpsys/cts/GfxInfoDumpsysTest.java) aynı farkı toplam süre olarak denetler; CTS’nin doğrulama amacıyla bazı bayrakları kabul etmesi bizim normal-kare istatistiği seçimimizle aynı kapsam değildir.

Bu süre ekranın gerçekten sunduğu kare gecikmesi, video FPS’i, tuştan piksele gecikme veya FrameTimeline jank sınıflandırması olarak adlandırılmaz. Emülatör/debug sonuçları fiziksel release kabulünün yerini tutmaz.

## Kullanım

Önce hedef cihazı ve sürümünü doğrula; kullanıcı TV’sini otomatik test cihazı olarak kullanma. Gerçek bir kayıt alırken kaynak, font, çözünürlük, aktif yenileme hızı, build türü, APK özeti, senaryo ve toplama zamanını ayrı QA kaydına yaz. Ham çıktı `.private/` altında kalır; pencere adları özel bilgi içerebilir.

Tek bir mevcut kaydı incelemek için proje kökünden:

```powershell
python tools/analyze_framestats.py .private/frames.txt --output .private/frame-report.json
```

Eşik karşılaştırması ancak ölçüm koşulundan bilinen bir bütçe ile yapılır. Örneğin **yalnız 20 ms karşılaştırma bütçesi gerçekten seçildiyse**:

```powershell
python tools/analyze_framestats.py .private/frames.txt --frame-budget-ms 20 --output .private/frame-report-20ms.json
```

Varsayılan 60 Hz veya 16.67 ms varsayımı yoktur. Eşiğe eşit kare aşım sayılmaz; yalnız daha uzun süreler sayılır. Bu oran Android’in jank yüzdesi diye etiketlenmez. `--output` mevcut dosyanın üzerine yazmaz.

## Rapor sözleşmesi

- Sütunlar adlarıyla bulunur; eski büyük harf/alt çizgi ve yeni CamelCase başlıkları desteklenir. Bilinmeyen ek sütunlar süre hesabında kullanılmaz. Eksik/tekrarlı başlık, kapanmamış blok veya profil yokluğu hata verir.
- Her PROFILEDATA bloğu ayrı tutulur. Pencere kimlikleri rapora taşınmaz; birden fazla bloğun yüzdesi tek bir uygulama yüzdesine çevrilmez.
- Sıfır olmayan bayraklar, bozuk satır genişliği, geçersiz/sentinel/ters zamanlar ve birebir tekrar satırları ayrı sayılır. Pozitif ama çok yavaş kare sırf uzun diye atılmaz.
- p50/p95/p99 nearest-rank yöntemiyle hesaplanır. Küçük örnek sayısı istatistiksel güven veya performans kabulü oluşturmaz.
- Geçerli örnek yoksa süreler ve oran null, durum no_valid_samples ve CLI çıkışı 2 olur. Geçerli örnek varsa çıkış 0 yalnız çözümlemenin mümkün olduğunu gösterir; ürün kalite kararı değildir.
- Girdi özeti SHA-256 ile izlenir; ham metin, dosya yolu ve pencere başlıkları rapora kopyalanmaz. Girdi sınırı 64 MiB’dir.

## Doğrulama ve sonraki kullanım

```powershell
python -m unittest discover -s tools -p test_analyze_framestats.py -v
```

Dokuz sentetik test: eski/yeni/sırası değişmiş başlık, büyük tamsayı hassasiyeti, bilinen yüzdelikler, eşik sınırı, dışlamalar, boş veri, ayrı pencereler, kesilmiş biçim, yavaş kare, yanlış bütçe ve CLI üzerine yazmama davranışını kapsar. Bunlar uygulamadan ölçülmüş kareler değildir.

Emülatör askıdan çıktıktan ve bekleyen test normal sonuçlandıktan sonra kontrollü kısa raf gezinmesinin gerçek çıktısı alınmalı. Uzun senaryonun tamamı için ring-buffer kapsamına güvenilmemeli; [Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview) veya uygun trace üzerinden fiziksel cihaz kabulü ayrıca kurulmalıdır. [Genel performans sınırları](PERFORMANCE.md) geçerlidir.
