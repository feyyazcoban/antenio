# IPTV Player

Bu klasör projenin tek ana çalışma alanıdır. Codex, Claude Code veya başka bir geliştirme aracında bu klasörü açın. Android Studio için `apps/android-tv` alt klasörünü açın.

İlk platform Android TV / Google TV; canlı TV, filmler ve diziler. M3U ve Xtream Codes kaynakları desteklenir. LG, Samsung ve mobil sonraki fazlardır. NEXTV kesin ürün adı değildir.

## Önce buradan başla

1. [AGENTS.md](AGENTS.md): bütün araçlar için ortak kurallar.
2. [STATUS](docs/project/STATUS.md): güncel durum ve açık sorunlar.
3. [HANDOFF](docs/project/HANDOFF.md): yapılan kontroller ve tam sonraki adım.
4. [WORKFLOW](docs/project/WORKFLOW.md): kurulum, derleme, cihaz ayrımı ve araç değiştirme.

Claude Code girişi [CLAUDE.md](CLAUDE.md) aynı sözleşmeye yönlendirir. Sohbet geçmişi yerine bu belgelerle devam edin.

## Güncel durum

Son önizleme **0.14.29**: Teslim artık release derlemesi (akıcılık), Canlı TV/Filmler/Diziler TiviMate mantığında kategori sütunuyla, menüde Google yuvarlak simgeleri. [Tasarım](docs/design/FLUID_TIVIMATE_29.md), [QA](docs/project/native-fluid-29-qa.json). Önceki: [sol menü ve Ara 0.14.28](docs/design/SIDE_NAV_SEARCH_28.md), [yazarken arama 0.14.27](docs/design/SEARCH_TYPING_27.md), [UI onarımı 0.14.26](docs/design/UI_REPAIR_26.md). Fiziksel TV 0.14.29 (16 Ekim gecesi kullanıcının izniyle, veri silinmeden kuruldu). APK/SHA-256 releases içinde. PIN yönetimi henüz kullanıcıya açık değil.

[Ortak erişim kararı](docs/architecture/ACCESS_DECISIONS.md) kaynakta hazır ve testli; uygulamaya henüz bağlanmadı. [PIN doğrulayıcı temeli](docs/architecture/PIN_CREDENTIAL.md) de testli; [deneme bütçesi mantığı](docs/architecture/PIN_ATTEMPTS.md) da testli. [Şifreli depo](docs/architecture/PROTECTION_STORE.md) da testli; Android saat adaptörü, doğrulama kontrolcüsü ve UI entegrasyonu sırada. Fiziksel performans/codec, Figma ve kullanıcı görsel kabulü açık. Ayrıntılar STATUS/HANDOFF'tadır.

[Son görsel değişiklikleri karşılaştır](design/reviews/LATEST.md).

## Klasör haritası

```text
TV/
  README.md                 Ana giriş
  AGENTS.md                 Araçtan bağımsız çalışma sözleşmesi
  CLAUDE.md                 Claude Code yönlendirmesi
  apps/android-tv/          Android uygulaması ve testler
  docs/product/             PRD ve ürün kapsamı
  docs/design/              Ekranlar, renk, hareket ve etkileşim
  docs/architecture/        Mimari sözleşmeler
  docs/research/            Rakip, kaynak ve medya araştırmaları
  docs/decisions/           Karar kayıtları
  docs/project/             Durum, devir, backlog ve QA
  design/                   Figma haritası, tokenlar ve inceleme görselleri
  prototypes/tv-flow/       Etkileşimli tasarım prototipi
  tools/                    Tekrar kullanılabilir kontrol araçları
  releases/                 Tarihli teslimler; Git dışında
  .private/                 Yerel/özel kayıtlar; Git dışında
```

Kökteki eski PRD PDF'si korunmuştur; güncel ürün kapsamının kaynağı aşağıdaki PRD.md'dir. Derleme çıktıları ve makine ayarları Git dışında tutulur. Önceki özel çalışma alanı ve SDK bu klasöre karıştırılmadı; yerel bağlantıları `.private/local-environment.json` içinde.

## Başvuru belgeleri

| Konu | Belge |
| --- | --- |
| Ürün kapsamı | [PRD](docs/product/PRD.md) |
| Görevler ve kararlar | [BACKLOG](docs/project/BACKLOG.md) · [DECISIONS](docs/decisions/DECISIONS.md) |
| Uygulama / derleme | [Android README](apps/android-tv/README.md) |
| Tasarım yönü | [Görsel revizyon](docs/design/VISUAL_REVISION.md) · [Native ekranlar](docs/design/NATIVE_VISUAL_REVISION.md) |
| Ekranlar ve Figma | [SCREENS](docs/design/SCREENS.md) · [FIGMA](docs/design/FIGMA.md) |
| Hareket ve renk | [MOTION](docs/design/MOTION.md) · [COLOR](docs/design/COLOR.md) |
| Kaynak/asset sözleşmesi | [SOURCE_GUIDELINES](docs/research/SOURCE_GUIDELINES.md) |
| Etkileşimli taslak | [Görsel inceleme](prototypes/tv-flow/review.html) · [Tam akış prototipi](prototypes/tv-flow/index.html) |
| Teslimler | [Arşiv açıklaması](releases/README.md) |
| Kalite ve TV kurulumu | [QUALITY_AUDIT](docs/project/QUALITY_AUDIT.md) · [TV_INSTALL](docs/project/TV_INSTALL.md) |

Prototip gerçek yayını açmaz; native uygulamanın tamamlandığına dair kanıt değildir. Kullanıcının kayıtlı kaynakları ve cihazları korunur. Dosyaları başka araca verirken `.private`, ham hesaplar ve yerel ayarlar paylaşılmaz.
