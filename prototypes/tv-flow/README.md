# TV akış prototipi · 0.2

**14 Ekim güncellemesi:** [Google TV görsel revizyonu](review.html) ayrı bir inceleme girişidir. Aynı font/assetleri kullanır; bu dosyadaki eski tam akışları değiştirmez. [Kapsam ve beş ekran](../../docs/design/VISUAL_REVISION.md). Android0.13 mevcuttur, yeni görsel revizyon henüz native uygulanmadı; aşağıdaki “native yok” ifadesi13 Ekim tarihli kayıttır.

**13 Ekim 2026 · İncelemeye hazır.** F01–F06 yolları ve 17 ekran sözleşmesinin yerel karşılıkları gezilebilir. Gerçek katalog görselleri kullanılır; medya oynatma, kaynak bağlantısı ve EPG örnektir. Native Android uygulaması henüz yoktur.

[Prototipi aç](index.html). Klasörü birlikte tutarak bu dosyayı tarayıcıda açabilirsin; derleme ve internet gerekmez. Yerel sunucu için bu klasörde `python -m http.server 8765 --bind 127.0.0.1`, ardından `http://127.0.0.1:8765` kullanılabilir.

## Hızlı deneme

1. “Ana sayfayı dene” → gerçek kanal logoları, film afişleri ve dizi seçkisi.
2. Filmler → Inception → Oynat → ileri al → iki kez Escape → Devam et.
3. Diziler → Breaking Bad → sezon / bölüm seç → Deneme seçeneklerinden “Bölümü / filmi bitir” → sonraki bölüm veya iptal.
4. Rehber → mevcut programı aç; gelecekteki programdan canlı kanala geç.
5. Arama → Inception veya TRT → içeriğe git → Escape ile aynı sonuç ve sorguya dön.
6. Favori ekle / çıkar. Kilitli örnekte **1234** PIN'ini kullan; yanlış PIN ve iptali de dene.
7. Kuruluma dön → M3U veya Xtream → Örnek bilgiler → Kaynağı ekle. Başarılı, kısmi, boş ve hata durumlarını alttaki seçeneklerden değiştir.

Ok tuşları odak taşır, Enter seçer, Escape geri döner. Fare ve Tab da kullanılabilir. Form alanında Enter ekran klavyesini açar; fiziksel klavye desteklenir. Ses ve altyazı panelinde iki ayrı İngilizce altyazı seçeneği ve Kapalı durumu vardır. Panel açıkken Tab odağı dışarı kaçmaz.

Kısmi yüklemede kanallar kullanılabilir; film/dizi kütüphanesi tekrar deneme durumunda kalır. Kurtarmayı görmek için Deneme seçenekleri → Kurulum sonucu → Başarılı seçip Tekrar dene. “Ana sayfayı dene” her zaman başarılı katalog örneğini açar.

## Ekran eşlemesi

| Ekran | Çalışan yerel davranış |
| --- | --- |
| TV-001 | Karşılama ve kaynak yöntemi |
| TV-002 / TV-003 | M3U / Xtream formu, doğrulama, taslak koruma, ekran klavyesi |
| TV-004 | İptal edilen yükleme; başarılı, kısmi, boş ve hata sonucu; yeniden deneme |
| TV-010 | Ana sayfa, gerçek görseller, canlı erişimi, film/dizi ve oturum içi devam et |
| TV-020 | Kanal kategorisi, odak önizlemesi, favori, eksik rehber bilgisi ve PIN |
| TV-021 | Kanal/zaman ızgarası, dikey zaman çıpası, program paneli ve oynatıcıdan dönüş |
| TV-030 / TV-031 | Film filtresi, afiş, ayrıntı, tam açıklama, favori, devam et / baştan izle |
| TV-040 / TV-041 | Dizi, sezon, örnek bölümler, bölüm ilerlemesi ve aynı bölüme dönüş |
| TV-050 | Canlı oynatıcı benzetimi; kanal paneli, geçiş, ses/altyazı, hata ve katmanlı Back |
| TV-051 | Film/dizi oynatıcı benzetimi; duraklat, seek, ilerleme, sonraki bölüm ve bitiş |
| TV-060 | Kanal/film/dizi araması, tür filtresi, klavye, boş sonuç ve sorguyu koruma |
| TV-061 | Üç içerik türünde favori; boş listeye düşerken güvenli odak |
| TV-070 | Ayarlar paneli; hareket, örnek kaynak yenileme/ekleme ve PIN koruması |
| TV-071 | PIN paneli; yanlış/doğru giriş, iptal, kaynak konuma dönüş ve yeniden kilitleme |

TV-070/071 ayrı tam ekran yerine panel olarak uygulanır. Figma ekran node'ları henüz oluşturulmadı.

## Gerçek varlıklar ve veri sınırı

- Katalogdan seçilen **6 canlı kanal, 5 film, 4 dizi ve 21 örnek bölüm** görünür. TRT ve CNN Türk logoları; Inception, Interstellar, Dune (2021), The Grand Budapest Hotel ve Wallace & Gromit afişleri; dizi afişleri ve bölüm kareleri yereldir.
- İlk indirmede 47 görsel alındı. Dune Part Two etiketli `m1` kaydının afiş ve arka planı önceki filme ait göründüğünden bu içerik arayüzden çıkarıldı. 45 görsel kalan seçkiye aittir. Kayıt yeniden adlandırılarak gizlice düzeltilmedi.
- [Varlık notları](assets/catalog/README.md), [manifest](assets/catalog/manifest.json) ve [kalite notu](assets/catalog/quality-review.json) kaynak adresi, boyut, hash ve istisnayı içerir. Bu adreslerde IPTV hesabı veya oynatma URL'si yoktur.
- Açıklama, tür, yıl ve süre sağlayıcının verisidir; editoryal doğruluk onayı değildir. Eksik süreler yalnız prototip zamanlayıcısı için örneklenebilir; `durationVerified` alanına bak.
- En fazla iki sezon / sezon başına üç bölüm örneklenir. “Önizlemedeki bölümler tamamlandı” gerçek dizinin sona erdiği anlamına gelmez. Sonraki bölüm geçişi bu örnek liste üzerinde çalışır.
- PIN örnekleri (TRT Spor, Dune ve Dark) davranış denemesi içindir; sağlayıcının yaş veya yetişkin içerik sınıflaması değildir. **1234 üretim güvenliği sayılmaz.**
- EPG programları, saatler, import ilerlemesi ve ses/altyazı listeleri sentetiktir. Önceki medya incelemesindeki track sonuçlarının bu seçilmiş içeriklerin tamamında var olduğu iddia edilmez.

## Tasarım

[COLOR](../../docs/design/COLOR.md) içindeki Figma Dark renk rolleri ve paket içi **Roboto** kullanılır. Font `assets/fonts/Roboto.ttf`, SIL Open Font License 1.1 metni `assets/fonts/OFL.txt` içindedir. [Roboto kaynağı](https://github.com/google/fonts/tree/main/ofl/roboto).

[MOTION](../../docs/design/MOTION.md) temelinde kısa, kesilebilir odak/panel geçişleri bulunur. Mantıksal odak animasyonu beklemez. Ayardaki Hareketi azalt ve sistem `prefers-reduced-motion` tercihi desteklenir. Bunlar cihaz ölçümüyle kesinleşmiş süreler değildir.

Google TV ve mevcut Figma kitinin odak, kart ve panel rolleri referans alındı. Tarayıcı bileşenleri Figma instance'ları veya Compose bileşenleri değildir. Eski özgün SVG deneme görselleri yalnız temel görünüm kodunun yedeğidir; gerçek seçki `catalog-data.js` / `flows.js` ile yüklenir.

## Dosya sorumlulukları

| Dosya | Sorumluluk |
| --- | --- |
| index.html | Uygulama kabuğu, yerel betik sırası, CSP ve deneme araçları |
| styles.css / app.js | Temel kaynak akışı, TV geometrisi, odak/Back/panel altyapısı |
| flows.css / flows.js | Gerçek varlık görünümleri ve tüm katalog/EPG/arama/PIN akışları |
| catalog-data.js | Hesap ve medya URL'si taşımayan küçük katalog snapshot'ı |
| assets/catalog/ | Yerel görseller, kaynak manifesti ve veri kalite notu |
| assets/fonts/ | Roboto ve lisansı |

`flows.js`, `window.TVPrototype` üzerinden temel görünüm, panel, eylem ve odak kayıtlarını genişletir. Betik sırasını koru. Gerçek API eklemeden önce prototip verisi ile uygulama veri katmanını ayır; ham kaynakları bu klasöre koyma.

## Teknik sınır ve doğrulama

CSP ağ bağlantısını kapatır; form gönderilmez. Hesap veya ilerleme localStorage/cookie ile tutulmaz. Durum yalnız sayfanın belleğindedir. Yenileme sıfırlar; Kuruluma dön formu/kurulumu sıfırlar, favori/izleme geçmişi ve tercihler sayfa oturumunda kalır. Oynatıcı gerçek video veya ses çıkışı üretmez.

Hedef 16:9 TV'dir. 390 px dar görünüm ayrıca kontrol edildi; mobil ürün tasarımı olarak kabul edilmez. Gerçek TV mesafesi, kumanda tekrar hızı, performans, codec ve parça değişimi testleri bekliyor. QR kurulumu, kalıcı hesap, tam kaynak yönetimi ve üretim PIN güvenliği bu prototipte yoktur.

Proje kökünden `node tools/test_tv_full_flow.cjs` çalıştır. Node, Playwright ve Chromium gerekir; mevcut Chrome için `PLAYWRIGHT_CHANNEL=chrome`. `PLAYWRIGHT_MODULE` bağımlılık yolunu, `TV_SCREENSHOT_DIR` isteğe bağlı ekran görüntüsü klasörünü belirtir. Eski `test_tv_flow.cjs` aynı tam sete yönlendirir.

13 Ekim 2026: F01–F06, kanal panelindeki PIN, form sonuçları, kısmi katalog kurtarma, arama/favori dönüşü, film/dizi ilerlemesi, eksik görsel, azaltılmış hareket ve dar görünüm kontrolleri geçti. Harici ağ isteği ve tarayıcı hatası bulunmadı. Görsel kontrol: ana sayfa, rehber, ayrıntı, bölüm listesi, sonraki bölüm, PIN, arama ve oynatıcı. Sonuç kaydı [prototype-qa.json](../../docs/project/prototype-qa.json).
