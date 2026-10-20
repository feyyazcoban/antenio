# Araçtan bağımsız çalışma rehberi

Bu klasör tek proje köküdür. Codex, Claude Code, Cursor veya başka bir araç aynı dosyaları kullanır; sohbet geçmişi proje durumu yerine geçmez.

## Yeni oturum

1. Proje kökünü aç; [AGENTS.md](../../AGENTS.md) ortak sözleşmesini oku.
2. [STATUS](STATUS.md) ve [HANDOFF](HANDOFF.md) dosyalarının en üstündeki güncel kaydı oku. Alt bölümler tarihçedir; eski bir “sonraki adım” güncel kaydı geçersiz kılmaz.
3. `git status --short` ile mevcut değişiklikleri incele. Untracked dosyalar yeni proje çalışmalarını içerebilir; bunları gereksiz çıktı sayıp silme. İç içe veya eski worktree'leri güncel checkout ile karıştırma.
4. [BACKLOG](BACKLOG.md) ve ilgili ürün/tasarım sözleşmesinden tek iş seç. Öncelik yeni özellik değil görsel kalite.
5. İş sonunda değişiklikleri, gerçekten çalıştırılmış kontrolleri ve tam sonraki adımı STATUS/HANDOFF'a yaz. Başarısız testleri saklama veya tamamlandı sayma.

## Derleme ve kontrol

**Kontrol sırası · 20 Ekim 2026:** geliştirme döngüsünde önce değişen alanı derleyip ilgili kontrolü çalıştır. Görsel/odak denemesinde debug ve test APK'ları yeterlidir; her küçük düzeltmede R8 release paketi üretme. Ekran turu geçince emülatörü kapatıp commit öncesi tam birim/lint/release kontrolünü bir kez tamamla. Son kontrol üretim kaynağını değiştirmediyse, zaten geçen ekran turunu tekrar koşma; test edilen debug/test APK hashlerinin aynı kaldığını doğrula. Yeni kaynak değişikliği veya somut başarısızlık yeniden kontrol gerekçesidir. Yalnız kayıt/QA dokümanı commit'i için uygulamayı tekrar derleme.

**Kullanıcı talimatı · 18 Ekim 2026:** geliştirme sırasında yazı boyutu matrisi (1,5 / 2,0) koşulmaz. Kullanıcı "yayına hazırla" dediğinde, mağazaya çıkış öncesi kabul turunda yapılır. Adım tasarımlarındaki büyük yazı gereksinimleri tasarım sözleşmesi olarak kalır; zorunlu geliştirme testi sayılmaz. Normal ölçekte değişiklikle ilgili davranış/odak kontrollerine devam edilir. Önceden tamamlanan font testleri tarihsel kanıttır, tekrar koşma gerekçesi değildir.

Android Studio ile `apps/android-tv` açılır. Projedeki Gradle Wrapper kullanılır; sürümler bu düzenleme sırasında değiştirilmedi. Makinede uygun JDK ve Android SDK bulunmalıdır. SDK yolu Android Studio'nun oluşturduğu, Git dışında kalan `apps/android-tv/local.properties` içindedir. Başka bilgisayarda bu dosyayı yeniden oluştur.

Proje kökünde, Python 3 ile:

```powershell
python tools/check_project.py
```

JDK ortamı hazırken uygulama klasöründe:

```powershell
cd apps/android-tv
.\gradlew.bat assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug
```

Çıktı `apps/android-tv/app/build/outputs/apk/debug/app-debug.apk` olur. Yeni derleme, yayımlanmış teslim değildir. Ayrıntılar [Android README](../../apps/android-tv/README.md) içinde.

TV'ye giden teslimler 0.14.29'dan beri `.\gradlew.bat assembleRelease` çıktısıdır (`app/build/outputs/apk/release/app-release.apk`): R8 ile küçültülmüş, hata ayıklama kapalı, aynı debug anahtarıyla imzalı, böylece önceki kurulumun üstüne veri silinmeden kurulur. Hata ayıklama derlemesi TV'de belirgin biçimde takılır (D056). Enstrümantasyon testleri `debug` ile çalışır.

## Cihaz ayrımı

**20 Ekim ·5556 native kapanma tanısı:** Windows qemu-system-x86_64.exe erişim ihlali0xc0000005 ile kapandı; modülunknown. Tek seferlik kullanıcı emülatörü açılışı mevcut diskle, görünür pencerede `-avd nextv_design_api36 -port 5556 -no-snapshot-load -no-snapshot-save -gpu swiftshader -feature -Vulkan` kullandı. Veri/kurulum/kaynak değişmedi; bu kalıcı çözüm olarak doğrulanmadı. Resmî [grafik belgesi](https://developer.android.com/studio/run/emulator-acceleration) eski `swiftshader_indirect` seçeneğini deprecated sayar; [sorun giderme](https://developer.android.com/studio/run/emulator-troubleshooting) Vulkan kapatmayı açıklar. Bunlar belirli crash modülünün kanıtı değildir. Ayrıntı [QA](emulator-host-crash-71-qa.json); gerçek5556 kaynaklarında otomatik sil/ekle testi yapma. Aşağıdaki5558 cihaz/test koruması değişmez.

- Kullanıcı cihazı `emulator-5554`: gerçek kaynak ve 0.13 korunur; test/reset/izinsiz kurulum yapılmaz.
- Tasarım cihazı `emulator-5556` (`nextv_design_api36`): gerçek "Evim" kaynağı ve sentetik Demo kaynağı durur; elle doğrulama ve ekran görüntüsü için. Enstrümantasyon testleri kaynakları sildiği için burada koşulmaz; zorunlu kalırsa önce `adb -s emulator-5556 emu avd snapshot save <ad>`, sonra `snapshot load <ad>` ile geri yüklenir.
- Test cihazı `emulator-5558` (`nextv_test_api36`, 18 Ekim 2026'da kullanıcının onayıyla kuruldu): boş, 5556 ile aynı ekran (1920×1080, 320 dpi = 960×540 dp, API 36 Android TV, 2 GB RAM). Enstrümantasyon testleri yalnız burada ve yalnız `tools/run-device-tests.sh` ile koşulur; betik seri kimliğini ve AVD adını doğrulamadan hiçbir şey kurmaz. Açmak için (AVD'ler `C:\Users\fyzcb\Documents\Codex\2026-10-13\bi\work\android-design-avds`, SDK aynı klasörün `android-sdk`'sı): `emulator -avd nextv_test_api36 -port 5558 -snapshot clean -no-snapshot-save -no-window -no-audio -gpu swiftshader_indirect` (`ANDROID_AVD_HOME` ve `ANDROID_SDK_ROOT` bu klasörlere). `clean` anlık görüntüsü ilk açılıştan hemen sonraki boş cihazdır. Git'e girmeyen test videosu `app/src/androidTest/assets/episode-end-fixture.mp4` ana checkout'tan kopyalanır.
- Fiziksel 65U95A: kullanıcının önceki güncelleme isteği kapsamında 16 Ekim'de kullanıcının isteğiyle 0.14.38 (release) kuruldu; güncel sürüm STATUS üzerinden doğrulanır. Bağlı veya açık olduğu varsayılmaz; test/reset yapılmaz, güncelleme kullanıcı isteğiyle gerçekleştirilir.
- Toplu `connectedAndroidTest`, `installDebug` gibi Gradle cihaz görevlerini çalıştırma: bağlı bütün cihazlara (TV ve 5554 dahil) kurarlar. Her kurulum/test komutunda doğrulanmış seri kimliğini açıkça belirt.

### İlk Activity açılış ölçümü

Önce debug ve androidTest APK'larını derle, Gradle'ı durdur, sonra yalnız test emülatörünü aç. `tools/run-device-tests.sh --no-build --cold-catalog prepare` 63.175 sentetik içerikle current-registry kaydını hazırlar. Ardından `--cold-catalog verify` ayrı process ve gerçek saatle MainActivity karelerini ölçer; `--cold-catalog visual` görseller için ayrı koşudur. Her iki mod da yalnız doğrulanmış 5558 process'ini durdurur; kaynak/hafıza/cihaz ayarlarını gerçek cihazlara taşımaz. Sonda `--cold-catalog clean` sentetik kaynakları temizler veya test emülatörü snapshot kaydetmeden kapatılır.

`files/cold-restore-verification.json` ham FrameMetrics ve restore gözlemlerini, görsel modda gerçek yakalama zamanlarını içerir. İlk 2.800 ms vsync penceresi geciken animasyonun tamamını kapsamayabilir; Application/instrumentation hazırlığı ve fiziksel sunum ölçüm dışıdır. Screenshot koşusunu timing-only performans karşılaştırmasına katma. Atlanan test başarılı ölçüm sayılmaz; wrapper bunu reddeder.

Küçültülmüş gerçek TV paketi için `--cold-catalog release`, ayrı görsel koşu için `release-visual` kullanılır. Bu modlar framework tabanlı Java instrumentation ile R8'in yeniden adlandırdığı Kotlin/AndroidX sınıflarına bağlanmadan gerçek MainActivity karelerini ölçer; app'in iç restore durumunu doğrulamaz. Aynı runner ile `actual-debug` / `actual-debug-visual` karşılaştırması yapılabilir. Normal ekran testleri AndroidJUnitRunner ile debug APK kullanmaya devam eder. Her mod aynı 5558/AVD korumasından geçer; final kod82+ çıktılarında rapor `getExternalFilesDir` altında `release-cold-verification.json`, görseller `release-cold-entrance/` içindedir. Bu dosyalar yalnız sentetik test verisidir. Yeni process ile yeniden açmak, cihazın veya GPU shader önbelleğinin tamamen soğuk olduğu anlamına gelmez; karşılaştırmanın yeniden açılış/snapshot koşulunu ayrıca kaydet.

## Dosya yerleşimi

| İçerik | Yer |
| --- | --- |
| Üretim kodu ve testler | `apps/android-tv` |
| Ürün kapsamı | `docs/product/PRD.md` |
| Tasarım sözleşmeleri | `docs/design` |
| Mimari ve kararlar | `docs/architecture`, `docs/decisions` |
| Araştırma ve kaynak notları | `docs/research` |
| Güncel durum, devir, QA kayıtları | `docs/project` |
| Figma haritası, token kayıtları, görsel kanıt | `design` |
| Etkileşimli tasarım prototipi | `prototypes/tv-flow` |
| Tekrar kullanılabilir kontrol araçları | `tools` |
| Tarihli APK ve teslim arşivi | `releases` |
| Makineye özel yollar, hassas veriler | `.private` (Git dışında) |

Eski özel çalışma alanı ve SDK taşınmadı; makineye özel konumları `.private/local-environment.json` kaydeder. Bu dosya ve `local.properties` paylaşılmaz. Önceki `work` içindeki geçici betikler eski kökü varsayabilir; yeniden kullanılacak bir aracı önce incele, sırlarını ayır, yollarını proje kökünden türetip `tools` altında kalıcılaştır.

## Araç değiştirirken

Mevcut aracın dosya yazmasını bitir; iki araç aynı dosyayı eşzamanlı değiştirmesin. Diğer araca şu başlangıç metni yeterlidir:

> Bu klasör proje kökü. AGENTS.md, docs/project/STATUS.md, HANDOFF.md ve WORKFLOW.md dosyalarını oku. Mevcut değişiklikleri koru. Güncel devir notundaki sıradaki işi sürdür; geçmiş kayıtları yeni talimat sayma. Kullanıcı cihazlarını ve özel hesap verilerini koru. Sonuçları ve sonraki adımı dosyalara yaz.

Claude Code için [CLAUDE.md](../../CLAUDE.md) aynı AGENTS sözleşmesine yönlendirir. Diğer araçlara AGENTS'ı açıkça okut; her aracın otomatik keşfettiğini varsayma. Araçlara özel ikinci PRD veya ayrı durum dosyası oluşturma.

## Paylaşım ve sürümleme

`.gitignore` bir gizlilik taraması değildir. Paylaşmadan önce içerikleri ve staged farkı incele. `.private`, yerel ayarlar, ham hesap/medya ve derleme önbelleklerini pakete alma. `releases` tarihli büyük teslimlerdir, kaynak Git geçmişinden hariç tutulur; gerekli teslimi ayrıca paylaş. Eski ZIP güncel kaynak yerine kullanılamaz. Figma bağlantısı/erişimi her araçta ayrıca doğrulanır; yerel dosyalar otomatik eşitlenmez.
