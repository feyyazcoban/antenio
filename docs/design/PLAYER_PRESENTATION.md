# Oynatıcı paneli

## 0.14.47 · D072 kontrol bloğu

18 Ekim 2026 · S14b, önceki köşeli paneli tek alt karartma ve ortak bilgi bloğuyla değiştirir. Ayrıntılı sözleşme [player-controls](gap/player-controls.md); aşağıdaki 0.14.3 görselleri tarihçedir.

- Başlık 28 sp, saat 20 sp; tür, süre/bölüm ve oynarken gerçek hıza göre bitiş zamanı alt satırdadır. Canlıda kanal numarası, logo/harf karşılığı, beyaz CANLI noktası ve varsa şimdi/sonra bilgisi kullanılır.
- Sol küme 52 / 64 / 52 dp geri sar–oynat/duraklat–ileri sar; sağda Ses / Altyazı, film/dizide Seçenekler ve favori vardır. Canlıdaki geçici Kanal −/+ düğmeleri sağ kümededir. Sonraki bölüm yalnız mevcut bölüm verisi izin verirse görünür.
- Çubukta Sol/Sağ bekleyen hedefi 10 sn, basılı tutuşta 30 sn/1 dk değiştirir; 1 sn sonra tek sarma uygulanır. OK hemen uygular; Aşağı uygulayıp önceki düğmeye döner. Geri önce hedefi iptal eder, sonra kontrolleri gizler, sonra oynatıcıdan çıkar. Kontroller gizliyken sarma hemen uygulanır ve 2,5 sn süre önizlemesi gösterilir.
- Kalan süre çubuğun sağındadır. Odak etiketi düğmenin yanında çizilir; D064 halkası ve dolgu kullanılır, odakta büyüme ve yatay kaydırma yoktur. Büyük yazıda etiketler simgeye iner; başlık iki satıra çıkabilir. Hareketi azalt, kontrol geçişini anlık yapar.
- Altyazı boşluğu gerçek blok yüksekliğinden ölçülür. Konum/saat ayrı durum nesnesindedir; her yarım saniyede bütün kontrol katmanı yeniden kurulmaz. Erişilebilir çubuk ilerleme aralığı, süre açıklaması, ±10 sn eylemleri ve uygulanan hedef için tek duyuru sağlar.
- D077 hız seçimi korunur; kaynak adresi gösterilmez ve sarma yeni bir oynatma oturumu açmaz. S15 kanal paneli/bilgi bandı ve S16'nın ek seçenekleri bu adımın dışındadır.

Güncel test ve teslim sonuçları [STATUS](../project/STATUS.md) / [HANDOFF](../project/HANDOFF.md) içindedir. Fiziksel kumanda, 3 m okunurluk, ses ve TalkBack kabulü ayrıca yapılmalıdır; Figma değiştirilmedi.

## Önceki: 0.14.3

0.14.3 tasarım önizlemesi, mevcut oynatma davranışını ortak görsel sisteme taşır. Fiziksel TV kurulumu veya Figma eşlemesi değildir.

## Yerleşim

- İçerik türü ve alt kontrol paneli ortak 58 dp ekran kenarına hizalanır.
- İçerik başlığı, zaman/program bilgisi ve kumanda eylemleri tek yüzeyde toplanır. Ayrı başlık etiketi kaldırılır.
- Panel 20 dp köşe, yatay 24 dp / dikey 20 dp iç boşluk ve mevcut koyu renk rollerini kullanır.
- İlerleme çizgisi 4 dp yüksekliğinde, yuvarlatılmış uçlarla gösterilir. Yeni bir seek etkileşimi eklenmez.
- Kullanıcının yazı ölçeği korunur. Eylemler büyük yazıda yatay kayar; odaktaki eylemin tamamı görünür olmalıdır.
- Canlı program, bağlantı hatası, yeniden deneme, bölüm sonu, ses/altyazı ve iki aşamalı Geri davranışları mevcut akışlarını korur.

## İnceleme

Görseller ayrı test emülatöründen alınır. Görüntüdeki renk çubukları yerel test videosudur; gerçek sağlayıcı yayını veya film karesi değildir. Başlık ve görseller sentetik test kataloğuna bağlıdır.

| Ölçek | Panel | Ses/altyazı |
| --- | --- | --- |
| Normal | [Görüntü](../../design/reviews/player-panel/font-1.0/player-panel.png) | [Görüntü](../../design/reviews/player-panel/font-1.0/player-tracks.png) |
| 1,5 | [Görüntü](../../design/reviews/player-panel/font-1.5/player-panel.png) | [Görüntü](../../design/reviews/player-panel/font-1.5/player-tracks.png) |
| 2,0 | [Görüntü](../../design/reviews/player-panel/font-2.0/player-panel.png) | [Görüntü](../../design/reviews/player-panel/font-2.0/player-tracks.png) |

Geometri ve genişletilmiş kumanda turu normal/1,5/2,0 yazıda geçti. Eylem sırasının iki kenarında 12 dp güvenli alan, odak çerçevesinin kırpılmasını önler. Ek testlerde uzun iki satırlı başlık, canlı şimdi/sonra bilgisi, otomatik yeniden bağlanma, kalıcı503hatası ve elle yeniden deneme üç yazı ölçeğinde doğrulandı. Bu kanıt fiziksel cihaz/codec matrisi veya bütün sağlayıcı hatalarının onayı değildir. Güncel sonuç ve sonraki iş için [HANDOFF](../project/HANDOFF.md).

[2,0 yazıda son düğme](../../design/reviews/player-panel/font-2.0/player-last-action.png). Yerleşim kontrolünün ilk hali yalnız düğme metnini ölçüyordu; görsel incelemede kesik çerçeve görüldü, kaydırma ve ölçüm güvenli boşlukla düzeltildi. [Android BringIntoViewSpec](https://developer.android.com/reference/kotlin/androidx/compose/foundation/gestures/BringIntoViewSpec) kullanıldı.

## Zorlu durumlar

| Ölçek | Uzun başlık / hata | Canlı program bilgisi |
| --- | --- | --- |
| 1.0 | [Görüntü](../../design/reviews/player-panel/font-1.0/long-failed.png) | [Görüntü](../../design/reviews/player-panel/font-1.0/guide-player-1_0.png) |
| 1.5 | [Görüntü](../../design/reviews/player-panel/font-1.5/long-failed.png) | [Görüntü](../../design/reviews/player-panel/font-1.5/guide-player-1_5.png) |
| 2.0 | [Görüntü](../../design/reviews/player-panel/font-2.0/long-failed.png) | [Görüntü](../../design/reviews/player-panel/font-2.0/guide-player-2_0.png) |

Aynı klasörlerde `long-recovering`, `long-ready` ve `guide-player-controls` görüntüleri bulunur. [Doğrulama kaydı](../project/native-player-presentation-qa.json).
