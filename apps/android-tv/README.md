# Android TV

**Son tasarım önizlemesi 0.14.29.** Teslimler `assembleRelease` ile (R8, hata ayıklama kapalı); testler `debug` ile. Canlı TV/Filmler/Diziler TiviMate mantığında ([FLUID_TIVIMATE_29](../../docs/design/FLUID_TIVIMATE_29.md), [QA](../../docs/project/native-fluid-29-qa.json)). Önceki: [SIDE_NAV_SEARCH_28](../../docs/design/SIDE_NAV_SEARCH_28.md). [Görsel inceleme](../../design/reviews/LATEST.md). Güncel cihaz ve test durumu için [devir notu](../../docs/project/HANDOFF.md) ve [çalışma rehberi](../../docs/project/WORKFLOW.md) esas alınır.

**0.14.1 görsel iyileştirme:** [Katalog, afiş ve eksik görsel sunumu](../../docs/design/CATALOG_PRESENTATION.md). Ayrı test cihazında doğrulandı; kullanıcı emülatöründeki kaynaklar korunuyor.

Google TV görsel revizyonu çalışma koduna uygulanmıştır. [Görseller ve kapsam](../../docs/design/NATIVE_VISUAL_REVISION.md). Bu sürüm mağaza/üretim onayı değildir. Testler ayrılmış `emulator-5556` üzerinde yürütülür. Kullanıcı emülatörü korunur; fiziksel TV'nin son bilinen sürümü 0.14.22'dir.

Kotlin + Compose for TV + Media3 ile ilk kurulabilir uygulama. Paket kimliği `app.antenio.tv` (D119), görünen ad **Antenio** (D115). 0.15.0 öncesi kimlik `dev.iptvplayer.tv.pilot` idi; o kurulum ayrı bir uygulamadır. [TV kurulum rehberi](../../docs/project/TV_INSTALL.md).

## Kapsam

- Xtream Codes veya M3U; birden fazla kayıtlı kaynak ve ortak kütüphane, gerçek ağ istekleri, iptal/yenileme ve kısmi katalog uyarıları.
- Canlı/film/dizi, kategori ve başlık araması, kaynaktan logo/afiş/kareler ve eksik görsel karşılığı.
- Film ayrıntısı, sezon/bölüm seçimi, kalıcı favoriler ve izleme konumu.
- Tek Media3 oynatıcı; kanal geçişi, VOD ±10 saniye, devam/baştan, elle/otomatik sonraki bölüm, gerçek ses/altyazı seçimi.
- Kumanda odağı, katalog kartına dönüş, azaltılmış odak büyümesi, Roboto ve Google TV görsel revizyonunun anlamsal renk rolleri; Figma henüz eşitlenmedi.
- Android Keystore + AES-GCM ile kaynak saklama; yedekleme ve cihaz aktarımı kapalı. APK'de gerçek hesap veya hazır abonelik yok.

## Pilot sınırı

PRD'nin tamamlanmış üretim sürümü değildir. XMLTV rehber ve şimdi/sonra var; bir sağlayıcının gerçek XMLTV metadata’sı Android veri yolunda doğrulandı; diğer sağlayıcılar ve fiziksel TV doğrulaması bekliyor ([inceleme](../../docs/research/PROVIDER_EPG_AUDIT.md)). Catch-up, PIN, çoklu profil, QR kurulum ve bulut eşitleme yok. `is_adult=1` kayıtlar gizlenir; bu tam yaş sınıflaması veya PIN koruması değildir.

M3U türleri `/movie/`, `/series/`, dosya uzantısı ve SxxExx/1x01 başlıklarından çıkarılır; belirsiz kayıt canlıya düşer. tvg-id/tvg-name ve header XMLTV adresleri korunur; özel rehber adresi şifreli kaynak ayarında saklanır. Xtream türleri API bölümlerinden gelir. Yalnız gömülü altyazılar seçilir; dış altyazı indirme yok. Kaynak ve katalog Keystore/AES-GCM ile kalıcıdır. Katalog gzip/NDJSON ve AtomicFile ile saklanır; taze cache ağ isteği yapmadan açılır, altı saatten eski liste arka planda yenilenir. Hata veya kısmi yanıtta son liste korunur.

HTTP kaynakta ağ taşıması şifrelenmez; cihazda şifreli saklama bunu değiştirmez. Kullanıcının HTTP kaynakları için cleartext açıktır; HTTPS sertifika doğrulaması korunur. Ham istek/Media3 URL logları uygulama tarafından tutulmaz; hata mesajları sabittir. Afiş önbelleği uygulama özel alanında, 80 MB disk ve %12 bellek bütçesindedir.

İlk fiziksel cihaz iFFALCON 65U95A; 0.2.0 kablosuz kuruldu, hesap eklendi ve canlı oynatma gözlendi. Tam cihaz/codec matrisi bekliyor. APK min API 26, hedef API 36: nihai minimum cihaz kararı değil. Emülatör, gerçek cihazın HDR/HEVC/AC-3/5.1 veya performansını doğrulamaz.

## Derleme

Android Studio'da bu klasörü aç. SDK Platform 36 ve Build Tools 36.0.0 gerekli. SDK yolunu `local.properties` veya `ANDROID_HOME` ile ver; makineye özel dosyayı paylaşma. JDK en az 17; doğrulanan ortam Android Studio JBR 25 ve Gradle 9.2.1.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
.\gradlew.bat assembleDebugAndroidTest
# Önce yalnız bu iş için ayrılmış test emülatörünün seri kimliğini doğrula.
# Kullanıcı cihazlarını seçen toplu connectedDebugAndroidTest komutunu çalıştırma.
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Test APK’sini yükleme ve instrumentation komutları yalnız açıkça seçilmiş ayrılmış test cihazında çalıştırılmalı: testler **pilot uygulamasının** kaynağını/geçmişini sıfırlar. APK debug imzalı; mağaza sürümü değildir. `.gradle`, `.kotlin`, `build`, `local.properties`, imza ve gerçek hesap dosyaları Git'e/pakete girmez.

0.13 rehber, kalıcı saat dilimi, kaynak yenileme/iptal ve SQLite iş parçacığı sözleşmesi: [EPG](../../docs/architecture/EPG.md). [Güncel test kanıtı](../../docs/project/native-guide-qa.json).

## Kod haritası

| Dosya | Sorumluluk |
| --- | --- |
| `data/SourceRegistry.kt` / `data/SourceLibrary.kt` | Şifreli hesap listesi sözleşmesi, ContentRef ve ortak kütüphane |
| `ui/SourcesScreen.kt` | Kaynak yönetimi, seçim ve kaldırma onayı |
| `data/Models.kt` | Kaynak/medya, URL oluşturma, güvenli hata |
| `data/TitleIndex.kt` | Ortak Türkçe başlık indeksi, tür filtresi ve sıralama |
| `ui/SearchScreen.kt` | Ortak arama, boş/yükleme, IME ve odak dönüşü |
| `ui/SettingsScreen.kt` | Sabit başlıklı sağ panel, ayar/onay/okuma odağı |
| `ui/CardVisibility.kt` | Kart resmi ve başlığını birlikte görünür tutma |
| `data/CatalogParser.kt` | M3U ve Xtream ayrıştırma |
| `data/SourceRepository.kt` | Ağ erişimi, iptal ve kısmi başarı |
| `data/DeviceStore.kt` | Şifreli kaynak, favori, konum ve dil |
| `data/SourceTransactions.kt` | Süreç genelinde kaynak/cache okuma, kayıt ve silme sırası |
| `data/FavoriteTargets.kt` | Bölümden dizi favorisi ve eski kayıt dönüşümü |
| `TvViewModel.kt` | Ekran ve işlem durumu |
| `ui/Theme.kt` | Roboto, renk rolleri, TV düğme/alanları |
| `ui/PilotApp.kt` | Kaynak, katalog, ayrıntı, ayarlar |
| `data/EpisodeSequence.kt` | Bölüm sırası, otomatik/manuel uygunluk ve erişilebilir hedef |
| `ui/EpisodeEndController.kt` / `ui/EpisodeEndDialog.kt` | İptal edilebilir bitiş sayacı ve TV kartı |
| `ui/PlaybackRecovery.kt` | Tek tekrar sahibi, süre/sayı sınırı, hata sınıfı ve yaşam döngüsü |
| `ui/PlayerScreen.kt` | Tek oynatıcı, yaşam döngüsü, track seçimi |

## Testler

0.9’da63 benzersiz emülatör akışının son sonucu geçti; tekrar turları ve sınırları [QA](../../docs/project/native-multisource-qa.json) içindedir. MultiSourceFlowTest iki yerel sunucuyla aynı ID/favori/ilerleme, filtre/detay dönüşü, düzenleme/silme, M3U bölüm haritası, kısmi/başarısız yenileme ve duplicate bağlantıyı sınar. Kaynaklar ayrı şifreli cache’lerle yeniden açılır. 0.10 bu iki maddeyi uygular: owner referanslı Coil temizliği/gerçek ağ iptali ve bozuk kayıtta açık onaylı sıfırlama. [0.10 QA](../../docs/project/native-source-recovery-qa.json):79 JVM,44 farklı emülatör vakasının son sonucu geçti;1.5/2.0 recovery açıklamaları kumandayla okunabilir. Bu bütün ürünün fiziksel/erişilebilirlik onayı değildir.

79 JVM testi (11 çoklu kaynak/registry testi; 6 favori hedefi ve 4 kaynak işlemi dahil; 9 bölüm sırası, 8 bölüm sonu sayacı, 9 kurtarma politikası, 8 Türkçe başlık indeksi, 7 Home/geçmiş ve 3 katalog snapshot testi dahil): URL kodlama, adres doğrulama, M3U alanları/HTTP başlıkları, dizi gruplama, yinelenen kayıt, HLS ayrımı, Xtream eksik veri, bölüm uzantısı/süresi, konum, kısmi katalog, hata gizliliği, iptal ve 63.175 sentetik kayıt.

`PilotFlowTest`: cihazda MockWebServer ve üretilmiş MP4 ile canlı geçişi, iki ses dili/altyazı, film seek/devam, sezon/bölüm/sonraki bölüm, favori, Back/odak, şifreli kaynak, boş M3U ve kaldırma. TV yüzeyleri dokunma odaklı değildir; testler erişilebilirlik eylemleri ve gerçek Back tuşu kullanır. Fiziksel D-pad tekrar hızı ayrıca ölçülür.

`playback-fixture.mp4` sentetik renk örüntüsü, 440/880 Hz ton ve test altyazısıdır. Yalnız test APK'sına girer. Roboto OFL lisansı ana APK'da `assets/licenses` altında.

`ProviderSmokeTest` varsayılan atlanır. İzinli özel test, yalnız test cihazının uygulama özel `files/provider-test.json` dosyasından `base`, `username`, `password`, `samples: [{kind, id}]` okur. Dosyayı proje ağacına koyma. `-e privateProvider true` ile çalışır; içerikleri sırayla açar ve sonunda hesabı siler. `provider-result.json` sadece sayı/durum içerir. Katalog ekran görüntüsü hesap alanı içermez.

Çalıştırılmış sonuçlar [0.8 QA kaydı](../../docs/project/native-lifecycle-qa.json) içindedir; [0.1 kaydı](../../docs/project/native-qa.json) tarihsel kanıttır. Ana sayfa, doğru bölüm devamı, son kanallar ve geçmiş temizleme eklendi. Eski 0.1 ilerlemeleri Home geçmişine taşınmadı. `DeviceProvisioningTest` geliştirme için açık onaylı kurulum yardımcısıdır; ana APK’de bulunmaz, mevcut kaynağın üstüne yazmaz ve kullanıcı verisini temizlemez. Genel test paketi kullanıcı TV’sinde çalıştırılmamalıdır.

## Sürüm dayanakları

13 Ekim 2026'da resmî belgeler ve Maven erişimi kontrol edildi. Uyumlu set sabitlendi: AGP 9.0.1, Gradle 9.2.1, yerleşik Kotlin/Compose eklentisi 2.2.10, Compose BOM 2026.05.01, TV Material 1.1.0, Media3 1.11.1, Coil 3.3.0, OkHttp 4.12.0. Daha yeni sürüm uyarıları tek başına hata değildir.

- [AGP 9.0 / Kotlin](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Compose BOM](https://developer.android.com/develop/ui/compose/bom/bom-mapping) ve [TV Material](https://developer.android.com/jetpack/androidx/releases/tv)
- [Media3](https://developer.android.com/media/media3/exoplayer/hello-world) / [track seçimi](https://developer.android.com/media/media3/exoplayer/track-selection)
- [Yedekleme ve cihaz aktarımı](https://developer.android.com/identity/data/autobackup)

Figma'ya bu turda yazılmadı. Native tema mevcut rol değerlerini kullanır; ekranlar mevcut Figma instance'larının birebir aktarımı olarak sunulmaz.

## 0.3 görünüm ve açılış

[Native sözleşme](../../docs/design/NATIVE_UI.md): ortak geometri, gerçek görsel kontrolü, TV başlangıç penceresi, 220 ms geçiş ve özgün 1,12 saniye ses. Altı PilotFlowTest ve bir VisualPreviewTest emülatör akışı; ayrıca opt-in CatalogColdLaunchTest büyük disk cache denemesi. Açılış sesi ve hareket ayarı kaynak kaldırmada korunur. Telefonla kurulum henüz yok.

## 0.4 arama ve kumanda

Dört SearchNavigationTest, altı PilotFlowTest ve bir VisualPreviewTest: toplam 11 akış. Ek olarak font ölçeği 1.3 ile dört arama/gezinme ve bir görsel tur geçti; ayar sonunda geri alındı. Türkçe global ve bölüm içi arama arka planda aynı indeksi paylaşır. Sonuç türü/kaynak bağlamı, query/filtre/odak dönüşü, eksik afiş, uzun başlık, silinen kart/kategori ve 50 kategorilik yatay satır kontrol edilir. Native görseller teslim klasöründeki `IPTV_Player_Native_04_*.png` dosyalarıdır. TV hâlâ 0.2.0; bu testler ayrılmış emülatörde yapılır.

## 0.5 ayarlar ve geri dönüş

Sağ Ayarlar panelinde sabit başlık/kapatma, açıklamalı satırlar ve kaydırma göstergesi; alt sayfalardan aynı satıra dönüş. Yenilemede çift OK tek istek olarak kalır. Arama Back: klavye → sonuç → Ara → ana sayfa. Grid daralınca seçili afiş başlığı görünür tutulur. Sekiz SearchNavigationTest, altı PilotFlowTest ve bir VisualPreviewTest: 15 akış. Yazı ölçeği 1.5 ile 9 ek akış; sistem ayarı geri alındı. Native05 on ekran teslim klasöründedir.

## 0.6 bağlantı kurtarması

1/3 saniye bekleyerek iki otomatik deneme, ilk hatadan 20 saniye kurtarma sınırı; açma/buffer 30 saniye, HTTP bağlanma/okuma 8 saniye. İç Media3 yükleme tekrarları/fallback kapalı. VOD konumu korunur; canlı tekrar güncel pencereye katılır. Auth/limit/sertifika/biçim hatası otomatik tekrarlanmaz. İptal/Back/arka plan/medya değişimi bekleyen işi durdurur. Kullanıcı tekrarı veya 10 saniye kesintisiz oynatma bütçeyi yeniler.

11 PlaybackRecoveryFlowTest, 6 PilotFlowTest, 8 SearchNavigationTest, 1 VisualPreviewTest: 26 akış. Ayrıca 1.5 font ile iki kurtarma ve bir görsel tur geçti. Dokuz yeni sanal saat testi toplam JVM sayısını 41'e çıkarır. recovery-fixture.ts, mevcut sentetik MP4'ün dört saniyelik video/ses MPEG-TS dönüşümüdür; yalnız test APK'sına girer. HLS denemesi tek segmentli yerel VOD'dur, tam canlı/ABR matrisi değildir. Süreler yavaş gerçek sağlayıcılarda ayrıca doğrulanır.

## 0.7 bölüm sonu

Gerçek medya sonunda 10 saniye iptal edilebilir otomatik geçiş; ilk odak Bu bölümde kal. Ayarlar'da kapatılır ve cihaz tercihi kalır. Sonraki bölüm baştan açılır; yeni sezon/bölüm odağı geri yüklenir. Eksik/tekrarlanan/numarasız sıra manuel kalır, M3U çıkarımı belirtilir. Eski cache sıra kanıtı gelene kadar manuel çalışır. Arka plan, iptal ve hedef değişikliği sayacı keser; kendiliğinden yeniden başlatmaz. Son kayıt dizinin kesin finali gibi gösterilmez.

10 EpisodeAutoplayFlowTest ile önceki 26 akış: toplam 36; ayrıca 1.5 font ile beş akış. 58 JVM. Dört saniyelik episode-end-fixture.mp4 yalnız test paketindedir. Native07 on beş ekran teslim klasöründe; gerçek TV hâlâ 0.2.0.

## 0.8 gezinme, favori ve kaynak yaşam döngüsü

Sol menü gerçek Roboto etiketi/yazı ölçeğine göre genişler, satırları büyür ve orta bölüm kayar. Bölüm favorisi görünür ana diziye çözülür; eski bölüm ID'leri bilinen ilişkiyle taşınır. Player yıldızında anlamlı erişilebilir ad/durum vardır.

SourceTransactions süreç genelinde okuma/kayıt/silme sıralar. Başlamış disk yazısı iptal sırasında tamamlanır; yeni okuyucu bekler. TvViewModel ayrı işlem evreleriyle geç yanıtı ve silme sırasında ilerleme dirilmesini önler. Kayıt/kaldırma aşamasında İptal sunulmaz. Çok dosyalı power-loss atomicity iddiası yoktur.

68 JVM; SourceOperationFlowTest7, PilotFlowTest8, SearchNavigationTest9, PlaybackRecoveryFlowTest11, EpisodeAutoplayFlowTest10, VisualPreviewTest1 = 46 emülatör akışı. Son Settings pencere odağı düzeltmesinden sonra etkilenen Pilot8/Search9/AutoplaySetting1/Visual1 toplam19 akış yeniden geçti; ayrı yeni testler değildir. İki ek gezinme turu 1.5/2.0 yazı ölçeği; sonra 1.0'a dönüş. 17 Native08 görüntü. Disk yeniden yükleme testleri ViewModelStore'u temizler; Activity recreate tek başına bunu yapmaz. Bu tur yeni tam süreç öldürme testi değildir. Fiziksel TV 0.2.0'da kalır.

## 0.11 kaynak adı ve kumanda

16 kaynakla uzun/aynı adlar ayrı sıra ve tür satırlarıyla gösterilir. Tam ad ve hata Bilgiler ekranında kumandayla okunur. Kaldırma onayında Vazgeç ilk odak; metin kayar, düğmeler sabittir. Odaklı kaynak kartı adı ve eylemleriyle görünür kalır. 79 JVM, tek turda25 emülatör vakası; dört kaynak yerleşim vakası1.0/1.5/2.0 fontta ayrıca geçti. [0.11 QA](../../docs/project/native-source-layout-qa.json). Fiziksel cihaz ve tüm ekran büyük font matrisi açık.

## 0.12 arama hazırlığı ve önbellek bakımı

Arama indeksi katalog yayımlanınca arka planda hazırlanır; hızlı sorgu değişimi aynı işi tekrar oluşturmaz. Geçersiz artwork referansları küçük arka plan turlarıyla temizlenir; disk/bellek/aktif iş ve ortak sahiplik korunur.83 JVM/38 emülatör vakası.60bin içerikli tanı turu performans darboğazlarını kaydeder; fiziksel p95 kabulü değildir. [Yöntem](../../docs/architecture/PERFORMANCE.md) · [0.12 QA](../../docs/project/native-performance-qa.json).
