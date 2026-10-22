# Figma çalışma düzeni

**15 Ekim güncel kontrol:** Kişisel fyzcbn hesabı whoami ile doğrulandı;204:32screenshot çağrısı Starter MCP araç kotası nedeniyle reddedildi. Dosyada değişiklik yapılmadı.

**14 Ekim:** Kişisel hesapta tasarım bağlamı ve screenshot çağrıları Starter MCP kotasına takıldı. [Google TV görsel revizyonu](VISUAL_REVISION.md) yalnız yerelde; beş görüntü yeni Figma node'u değildir. Manifest değiştirilmedi.

[Dosyayı aç](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV?node-id=204-3)

Kullanıcının seçtiği kişisel fyzcbn hesabı kullanıldı. Dosya üç mevcut sayfadan oluşuyor. Starter planının üç sayfa sınırı nedeniyle yeni sayfalar yerine Design sayfasında altı bölüm oluşturuldu.

## Gerçek kaydedilmiş durum

| Sayfa | Node | İçerik |
| --- | --- | --- |
| Foundations | 160:3739 | Mevcut sekiz temel sistem panosu; içerik korundu |
| Components | 0:1 | Mevcut TV kiti ve NEXTV ürün bileşenleri; içerik korundu |
| 03 · Product & Screens | 87:3626 | Ürün, akışlar, ekran çalışma alanı, Motion Lab, Handoff ve Archive |

**Sınır:** Ürün sayfası yazımı başarılı oldu. Foundations ve Components sayfalarına rehber panosu ekleme/isim düzenleme çağrıları MCP kullanım kotası nedeniyle uygulanmadı. Tarayıcıdan yeni ürün panosu görüldü; ortak belgelerde bu iki sayfanın gerçek mevcut adları kullanılır. Ürün panosundaki hedef sayfa adları ve iki metindeki düz satır sonu kaçışları, erişim açıldığında tools/figma_cleanup.js ile düzeltilmeli. Figma içinde uygulama ekranları veya çalışan animasyon prototipi henüz yoktur; yerel tarayıcı prototipi aşağıda bağlıdır.

**23 Ekim:** Uygulamanın TV görüntülerinden oluşan ekran tuvali Figma dışında tutulur: [APP_CANVAS](APP_CANVAS.md). Manifest değişmedi.

## Yeni bölümler

| Bölüm | Bağlantı | Projedeki karşılığı |
| --- | --- | --- |
| 00 Project | [204:2](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV?node-id=204-2) | README, PRD, STATUS |
| 01 Flows | [204:16](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV?node-id=204-16) | SCREENS |
| 02 Google TV Screens | [204:32](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV?node-id=204-32) | Ekran çizimleri için ayrılmış alan |
| 03 Motion Lab | [204:48](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV?node-id=204-48) | MOTION, COLOR ve karşılaştırmalı prototipler |
| 04 Handoff | [204:62](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV?node-id=204-62) | AGENTS, HANDOFF |
| 99 Archive | [204:76](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV?node-id=204-76) | Yerine yenisi gelen çalışmalar; şimdilik kural panosu |

## Mevcut sistem envanteri

**Yerel prototip notu:** [F01–F06 akışları](../../prototypes/tv-flow/README.md) gerçek katalog görselleriyle tarayıcıda oluşturuldu; bu çalışma Figma dosyasını değiştirmedi. Figma ekran node'ları henüz yok. Renkler ve Roboto fontu snapshot'taki rollerle eşleşir; tarayıcı bileşenleri mevcut kitin doğrudan instance'ları değildir.

9 koleksiyon, 401 yerel değişken: ref 184, sys 49, type 48, elevation 32, layout 9, size 39, brand 20, motion 9, state 11. sys koleksiyonunda Light ve Dark modları var.

Components sayfasında 554 COMPONENT/COMPONENT_SET düğümü bulunuyor; bu sayı varyantları da içerir, 554 ayrı tasarım türü anlamına gelmez. 45 component set bulunuyor. Ürüne özel bölümde 116 component ve 15 component set var.

| Mevcut alan | Node | İşe yarayacağı ekran |
| --- | --- | --- |
| TV kit bölümü | 55:6424 | Ortak düğme, kart, kontrol ve gezinme |
| NEXTV Components | 155:2627 | Ürüne özel bütün parçalar |
| Focus Ring / Badge / Tooltip | 157:2635 / 157:2643 / 157:2656 | Ortak odak ve işaretler |
| State Block | 157:2714 | Boş/hata/yükleme durumları |
| Player Transport | 158:2643 | TV-050/051 |
| Player Menus | 159:2649 | Ses, altyazı ve ayar panelleri |
| EPG Program Guide | 160:2661 | TV-021 |
| On-screen Keyboard | 160:3526 | TV-002/003/060 |
| PIN Parental Control | 180:2831 | TV-071 |
| EPG Program Detail | 180:2836 | TV-021 |
| Live Channel Banner | 180:2841 | TV-050 |

Varlığı doğrulanan bileşenlerin bütün durumları ve gerçek cihaz davranışı henüz denetlenmedi. İmplementasyondan önce variant, token bağı ve kullanılabilirlik kontrolü yapılmalı.

## Adlandırma ve teslim

Ekran: `TV-030 / Movies / Default`. Durumlar aynı ekran ID'sini kullanır: Focused, Loading, Empty, Error, Locked. İş akışı Draft → Review → Ready → Implemented.

Bileşenleri ekran sayfasında yeniden master olarak oluşturmayın. Mevcut library instance kullanın; yoksa Components alanında ekleyin. Ekran kimliği sabit, başlık değişebilir.

Figma, uygulama davranışının tamamını tek başına açıklamaz. Her ekranda ilk odak, OK, Back, yön komşuları ve eksik veri sözleşmesi SCREENS ile birlikte tutulur. Sayfa/node değiştiğinde design/figma/manifest.json aynı işte güncellenir.

Renk ve motion önerileri mevcut tokenların üstüne otomatik yazılmadı. Snapshot tarihli bir kayıttır. Kaynakların gerçek erişim bilgileri hiçbir tasarım panosuna eklenmez.
