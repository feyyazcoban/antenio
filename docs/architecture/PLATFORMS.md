# Platform ve veri sınırları

## Fazlar

1. Android TV / Google TV: canlı TV, film ve dizi.
2. LG webOS ve Samsung Tizen: önce gerçek cihazda medya ve kumanda denemesi; sıra henüz kararlaştırılmadı.
3. Android mobil ve iOS: dokunma ve mobil yaşam döngüsüyle uyarlama.

TV arayüzünü tek kod tabanıyla bütün platformlarda çalıştırmak henüz bir karar değildir. Ürün sözleşmeleri ortak, oynatıcı ve giriş davranışları platforma özgüdür. Kotlin/Compose for TV/Media3 ilk Android pilotunda uygulandı; gerekçe PRD bölüm 10'da, sabitlenmiş sürümler [uygulama README](../../apps/android-tv/README.md) içindedir.

## İlk fiziksel test cihazı

Kullanıcı 13 Ekim 2026'da **iFFALCON 65U95A** modelini bildirdi. Bu, ilk pilot cihazdır; uygulamanın minimum donanım sınırı veya bütün Google TV cihazları için uyumluluk kanıtı değildir.

Üreticinin [Türkiye ürün sayfası](https://www.iffalcon.com/tr/tr/tvs/u95a) 65U95A modelini 4K QD-Mini LED U95A ailesinde listeliyor; 144 Hz doğal panel yenilemesi belirtiliyor. [Türkiye İngilizce ürün sayfası](https://www.iffalcon.com/tr/en/tvs/u95a) Google TV kullanımını belirtiyor. Kontrol tarihi: 13 Ekim 2026.

Kurulu Android sürümü, API seviyesi, firmware, uygulamaya sunulan ekran modları ve decoder yetenekleri cihaz üzerinde henüz okunmadı. Panelin 144 Hz olması uygulamanın 144 fps çalıştığı anlamına gelmez; gerçek çizim/oynatma hızı ölçülmeli.

İlk APK denemesi: kumandayla kaynak ekleme → canlı kanal / film / dizi bölümü → mevcut ses ve altyazı parçalarını seçme → ayrıntı veya listeye geri dönme. Aynı cihazda kanal geçişi, VOD ilerlemesi, büyük katalog gezinmesi ve gündüz/loş odada okunabilirlik ölçülmeli. Sonuçlar ölçülene kadar BKL-010/012/014 tamamlandı sayılmaz.

## Ortak veri kavramları

Source, Channel, Category, Programme, Movie, Series, Season, Episode, PlaybackProgress, Favorite ve MediaCapability.

Her içerik kimliği kaynak kimliğiyle birlikte tutulur. Ad veya afiş URL'si birincil kimlik değildir. Kaynak yenilenince favori ve ilerleme başka içeriğe bağlanmamalıdır. Türü belirsiz girdiler tahmin olarak işaretlenir.

## Yetenekler

Oynatıcı arayüzü, kaynağın ve cihazın birlikte desteklediği yetenekleri gösterir: seekable, live, catchup, audioTracks, subtitleTracks, durationKnown. Bir katalog alanı akışın gerçekten oynatılabildiğini kanıtlamaz.

Ses ve altyazı seçimleri parça kimliğinden çok dil/rol tercihiyle hatırlanır; her yeni akışta mevcut parçalara yeniden eşlenir. Eşleşme yoksa güvenli varsayılana dönülür. Kaynak dil etiketi vermiyorsa arayüz dil tahminini gerçekmiş gibi göstermez.

## Veri yaşam döngüsü

Kaynak sırları güvenli cihaz deposunda; katalog ve kullanıcı ilerlemesi ayrı mantıksal alanlarda tutulur. Tam kaynak URL'si analitik veya hata kaydına yazılmaz. VOD meta verisi ve büyük afişler ihtiyaç oldukça alınır; UI ana iş parçacığında tüm liste parse edilmez.

İlk sürüm için uygulama hesabı zorunlu değil. Bulut senkronizasyonu, QR eşleştirme ve lisans servisi ayrı sonraki faz kararlarıdır.

D111 (22 Ekim 2026, kullanıcı gereksinimi): Bir platformda alınan Premium, NEXTV hesabıyla giriş yapılan diğer platformlarda yeniden ödeme olmadan açılmalı. Bu, bir NEXTV hesabı ve sunucu tarafında bir hak kaydı gerektirir. Önerilen modelde (karar değil) hesap isteğe bağlıdır ve kaynak verisi (adres, kimlik bilgisi, liste, favori, geçmiş) sunucuya gitmez. Hesap ve senkronizasyon ayrı işlerdir. Ayrıntı: [SALES_CHANNELS](../product/SALES_CHANNELS.md).
