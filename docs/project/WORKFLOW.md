# Araçtan bağımsız çalışma rehberi

Bu klasör tek proje köküdür. Codex, Claude Code, Cursor veya başka bir araç aynı dosyaları kullanır; sohbet geçmişi proje durumu yerine geçmez.

## Yeni oturum

1. Proje kökünü aç; [AGENTS.md](../../AGENTS.md) ortak sözleşmesini oku.
2. [STATUS](STATUS.md) ve [HANDOFF](HANDOFF.md) dosyalarının en üstündeki güncel kaydı oku. Alt bölümler tarihçedir; eski bir “sonraki adım” güncel kaydı geçersiz kılmaz.
3. `git status --short` ile mevcut değişiklikleri incele. Henüz ilk commit yok; untracked dosyalar proje çalışmalarını içerir. Bunları gereksiz çıktı sayıp silme.
4. [BACKLOG](BACKLOG.md) ve ilgili ürün/tasarım sözleşmesinden tek iş seç. Öncelik yeni özellik değil görsel kalite.
5. İş sonunda değişiklikleri, gerçekten çalıştırılmış kontrolleri ve tam sonraki adımı STATUS/HANDOFF'a yaz. Başarısız testleri saklama veya tamamlandı sayma.

## Derleme ve kontrol

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

- Kullanıcı cihazı `emulator-5554`: gerçek kaynak ve 0.13 korunur; test/reset/izinsiz kurulum yapılmaz.
- Tasarım cihazı `emulator-5556`: önce bağlı cihaz ve AVD kimliği doğrulanır. Sadece ayrılmış cihazda hedefli test çalıştırılır.
- Fiziksel 65U95A: kullanıcının önceki güncelleme isteği kapsamında 16 Ekim'de kullanıcının isteğiyle 0.14.36 (release) kuruldu; güncel sürüm STATUS üzerinden doğrulanır. Bağlı veya açık olduğu varsayılmaz; test/reset yapılmaz, güncelleme kullanıcı isteğiyle gerçekleştirilir.
- Toplu `connectedAndroidTest` çalıştırma. Her kurulum/test komutunda doğrulanmış seri kimliğini açıkça belirt.

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
