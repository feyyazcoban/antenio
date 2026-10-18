# NEXTV

Bu klasör projenin tek ana çalışma alanıdır. Codex, Claude Code veya başka bir geliştirme aracında bu klasörü açın. Android Studio için `apps/android-tv` alt klasörünü açın.

İlk platform Android TV / Google TV; canlı TV, filmler ve diziler. M3U ve Xtream Codes kaynakları desteklenir. LG, Samsung ve mobil sonraki fazlardır. Ürün adı NEXTV (D066).

## Önce buradan başla

1. [AGENTS.md](AGENTS.md): bütün araçlar için ortak kurallar.
2. [STATUS](docs/project/STATUS.md): güncel durum ve açık sorunlar.
3. [HANDOFF](docs/project/HANDOFF.md): yapılan kontroller ve tam sonraki adım.
4. [WORKFLOW](docs/project/WORKFLOW.md): kurulum, derleme, cihaz ayrımı ve araç değiştirme.

Claude Code girişi [CLAUDE.md](CLAUDE.md) aynı sözleşmeye yönlendirir. Sohbet geçmişi yerine bu belgelerle devam edin.

## Güncel durum

Son TV tesliminin sürümü, kurulum doğrulaması ve açık kontroller [STATUS](docs/project/STATUS.md) dosyasının en üst kaydındadır. APK/SHA-256 arşivi `releases` içindedir. TV teslimleri küçültülmüş release derlemesidir; kurulu kaynak ve tercihleri koruyarak güncellenir.

[Ortak erişim kararı](docs/architecture/ACCESS_DECISIONS.md), [PIN doğrulayıcı](docs/architecture/PIN_CREDENTIAL.md), [deneme bütçesi](docs/architecture/PIN_ATTEMPTS.md) ve [şifreli depo](docs/architecture/PROTECTION_STORE.md) native arayüze bağlıdır. İki dil, ebeveyn denetimi, kaynak/açılış koruması ve oynatıcı ayarları uygulandı. Kalan işler [gap adımları](docs/design/gap/STEPS.md) ve [BACKLOG](docs/project/BACKLOG.md) içinde; tam devralma noktası [HANDOFF](docs/project/HANDOFF.md). Fiziksel performans/codec, Figma ve kullanıcı görsel kabulü açık.

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
