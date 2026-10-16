# İş listesi

Durumlar: done, review, ready, planned, blocked, in progress. Review çalışan taslağın kullanıcı incelemesini beklediğini; done somut çıktı/kontrolün tamamlandığını gösterir.

| ID | Öncelik | İş | Kabul ölçütü | Durum |
| --- | --- | --- | --- | --- |
| BKL-001 | P0 | PRD ve rakip araştırması | Üç içerik türü P0; kaynaklı karşılaştırma | done |
| BKL-002 | P0 | Ortak proje ve Figma çalışma alanı | AGENTS/CLAUDE, karar/devir kayıtları, ekran kimlikleri ve Figma ürün bölümleri | done |
| BKL-003 | P0 | Figma metin temizliği | Gerçek sayfa adları ve iki satır sonu düzeltilir; ekran görüntüsü kontrol edilir | blocked |
| BKL-004 | P0 | Kaynak ekleme ve ana gezinme | TV-001–004/010 yerel taslakta; ilk odak, OK/Back, hata, boş ve kısmi başarı kontrol edildi; kullanıcı incelemesi bekliyor | review |
| BKL-005 | P0 | Canlı TV ve rehber ekranları | TV-020/021/050 yerel taslak; örnek EPG/eksik bilgi, logo fallback ve PIN kontrolü; radyo, gerçek cihaz ve kullanıcı incelemesi bekliyor; bir sağlayıcının EPG metadata eşleşmesi doğrulandı | review |
| BKL-006 | P0 | Film ve dizi ekranları | TV-030/031/040/041/051 yerel taslak; sezon/bölüm, devam et, sonraki bölüm, bitiş ve eksik kapak | review |
| BKL-007 | P0 | Arama, favori ve ayarlar | TV-060/061/070/071 yerel taslak; filtre, PIN, oturum içi favori ve geri dönüş konumu | review |
| BKL-008 | P0 | Renk ve animasyon araştırması | MOTION/COLOR kaynakları, öneri/ölçüm ayrımı ve kontrol senaryoları | done |
| BKL-009 | P0 | Gerçek kaynak alan incelemesi | İki tam liste, API katalogları, varlık envanteri, örnek ses/altyazı | done |
| BKL-010 | P0 | Google TV medya denemesi | İlk pilot iFFALCON 65U95A; H.264/HEVC, stereo/5.1, dil ve altyazı değiştirme gerçek cihazda | planned |
| BKL-011 | P0 | EPG/XMLTV denemesi | Native ızgara/şimdi–sonra, kaynak ayarları, DST/kimlik, iptal ve kalıcılık testli; bir sağlayıcı metadata denetimi geçti; diğer sağlayıcı/gerçek cihaz ve kullanıcı doğrulaması açık | review |
| BKL-012 | P0 | Kumanda prototip testi | F01–F06, hızlı D-pad, Back ve odak koruma; en az 5 kişilik keşif turu | planned |
| BKL-013 | P0 | Büyük katalog denemesi | 0.14.10da60bin arama/yükleme tanısı ve63175kayıtlı süreç yeniden açılış/favori/geçmiş testi güncel; art index budama testli; fiziksel p95/afiş-bellek bütçesi açık | review |
| BKL-014 | P0 | Renk/motion cihaz doğrulaması | Gündüz/loş oda, düşük donanım ve hareketi azalt; ölçülmüş sonuç | planned |
| BKL-015 | P1 | LG/Samsung teknik keşfi | Desteklenen cihaz/model ve medya/girdi matrisi | planned |
| BKL-016 | P1 | Telefonla kolay kurulum ve senkronizasyon kapsamı | Kullanıcı ihtiyacı ve ONBOARDING akışı yazıldı; QR/web, veri yaşam döngüsü, maliyet ve kullanıcı testi bekliyor | ready |
| BKL-017 | P0 | Yerel tam akış prototipi | F01–F06; gerçek görseller, Roboto, kaynak/canlı/EPG/VOD/arama/favori/PIN; otomatik akış ve görsel kontrol | done |
| BKL-019 | P0 | Native ana sayfa ve geçmiş | Doğru bölüm devamı, kalıcılık, odak ve temizleme testleri geçti; görsel kullanıcı incelemesi bekliyor | review |
| BKL-020 | P0 | Native tasarım ve prototip uyumu | Google TV yönü Android 0.14 önizlemesinde; yedi ekranın üç fontta karşılığı ve ilgili odak kontrolleri hazır; 0.14.1 ayırıcı/eksik kapak, 0.14.2 yenileme/başlık dönüşü ve0.14.3oynatıcı/0.14.4ayarlar/0.14.5ses-altyazı paneli ve0.14.6geniş düzen/0.14.7detay-arama/0.14.8kurulum üç yazı ölçeğinde ilgili kontrollerden geçti; 0.14.9 yükleme/kurtarma ve boş filtre/favori/ana sayfa, menü iç payı üç fontta kontrol edildi;0.14.10boş tür/hata başlığı testli, bütün akış incelemesindeki rehber logo boyutu0.14.10tesliminde üç fontta düzeltildi; gerçek görsel eşleşmesi, kalan yüzeyler, Figma ve kullanıcı görsel onayı açık | in progress |
| BKL-021 | P0 | Kalıcı katalog ve kesintisiz yenileme | Ağsız yeniden açılış, başarısız yenilemede liste, bozuk cache ve kaynak ayrımı | done |
| BKL-022 | P0 | Splash ve özgün açılış sesi | Sistem splash uyumu, yaşam döngüsü, ses kapatma, sistem düzeyi ve görsel/ses önizleme; D062 ile NEXTV logo/splash paketi geldi, ses kullanıcının seçtiği telifsiz ses (kullanıcı beyanı); fiziksel TV kabulü açık | review |
| BKL-023 | P0 | Native Türkçe ortak arama ve odak | Kanal/film/dizi, tür ve kaynak bağlamı, arka plan indeks, geri dönüş; Back basamakları tamamlandı; 0.9 kaynak filtresi testli; p95 açık | review |
| BKL-024 | P0 | Native Ayarlar ve büyük yazı | Sabit başlık/kapatma, açıklamalı satırlar, Back/iptal odağı, tek yenileme; 1.5 font turu geçti, kullanıcı incelemesi bekler | review |
| BKL-025 | P0 | Sınırlı oynatma kurtarması | Süre/sayı sınırı, hata sınıfı, VOD devamı, iptal/Back/arka plan ve HLS sentetik akışları geçti; fiziksel/sağlayıcı matrisi açık | review |
| BKL-026 | P0 | Otomatik sonraki bölüm | Gerçek bitiş/10 s/iptal/kalıcı ayar, sezon sonu/belirsiz sıra/hedef değişimi/arka plan ve 1.5 font geçti; fiziksel ve kullanıcı turu açık | review |
| BKL-027 | P0 | Çoklu kaynak ve kaynak filtresi | Native yönetim/filtre, scoped artwork temizliği ve açık registry recovery testli; 16 kaynak/uzun ad/üç font ve index budama testli; gerçek cihaz/kullanıcı turu açık | review |
| BKL-028 | P0 | Native PIN ve gizleme | SAFE01: kaynak/kategori/kanal politikası; arama, favori, geçmiş ve doğrudan detay/oynatmada ortak erişim kontrolü; prototip kanıtından ayrı doğrulama | in progress |
| BKL-018 | P0 | İlk native Android TV pilotu | Kurulabilir APK; Xtream/M3U, canlı/film/dizi, gerçek parça seçimi; JVM, lint ve emülatör akış testleri; özel kaynakta sıralı üç oynatma | done |

BKL-004/005/006/007 yerel taslak olarak review durumundadır; kullanıcı doğrulaması ve Figma aktarımı bekler. BKL-017 tarayıcı prototipi, BKL-018 native pilot içindir. BKL-010 fiziksel 65U95A/codec testi, BKL-012 beş kişilik tur yapılmadığı için planned kalır. Native pilotta gerçek medya, kalıcı kaynak/favori/ilerleme vardır; bir sağlayıcıda gerçek EPG metadata doğrulandı; diğer sağlayıcı/cihaz, radyo örneği ve üretim PIN güvenliği açık. BKL-013 için 63.175 kayıt JVM ayrıştırması geçti; 0.3 emülatörde 63.175 kayıtla şifreli cache ve süreç yeniden açılışı gözlendi; p95/cihaz performans bütçesi henüz ölçülmedi.

BKL-003 bağımlılığı: kişisel hesabın Figma bağlantısı tekrar yazmaya izin vermeli. Hazır düzeltme scripti tools/figma_cleanup.js; çalıştırmadan önce hedef node metinleri kontrol edilir. Bu engel yerel doküman veya Android denemelerini durdurmaz.

0.4 ek kanıt: 32 JVM / 11 emülatör akışı; font ölçeği 1.3 ile 5 ek akış. Türkçe indeks 63.175 kayıtla doğruluk kontrolünden geçti. Bu sonuçlar BKL-012 kullanıcı turunu, BKL-013 UI p95 hedefini veya fiziksel cihaz kalite kapılarını tamamlamaz.

0.5 ek kanıt: 32 JVM / 15 emülatör akışı; yazı ölçeği 1.5 ile 9 ek akış. Ayarlar ve Arama Back katmanları, yenilemede daralan grid içinde zaten seçili başlığın görünürlüğü kontrol edildi. Tam erişilebilirlik/cihaz ölçümü değildir.

0.6 ek kanıt: 41 JVM / 26 emülatör akışı; 1.5 fontta 3 ek akış. PLAY02 sentetik sınama tamamlandı; gerçek cihaz/sağlayıcı ve performans kapıları açık.

0.7 ek kanıt: 58 JVM / 36 emülatör akışı; 1.5 fontta 5 ek akış. VOD02 otomatik geçiş yerel denemeleri geçti; gerçek cihaz ve kullanıcı incelemesi bekler.

0.14.38: 0.14.37 incelemesinin doğrulanan bulguları (ayraçlar sonda, sıralama önbellekte ve arka planda, ana sayfa rozeti, Geçmiş sırası, büyük yazı), Esc pencereye gerçek Geri olarak verilir, Ayarlar'da hızlı tuş, 0.14.36'dan beri düşen altı cihaz testi düzeltildi. [QA](native-sort-rating-38-qa.json). TV denemesi açık; BKL-020 in progress.

0.14.37: Filmler/Diziler'de Sırala (Varsayılan, Son eklenen, Ad, Yıl, Puan; bölüm başına saklanır) ve afişte sağlayıcı puanı rozeti; katalog kaydı v4. [QA](native-sort-rating-37-qa.json), D061. Emülatörde etiketle doğrulandı; TV denemesi açık; BKL-020 in progress.

0.14.31: Metin alanları klavyeyi odakta açmaz; OK açar, Geri kapatır. [Sözleşme](../design/KEYBOARD_ON_OK_31.md), [QA](native-keyboard-on-ok-31-qa.json). TV'de deneme açık; BKL-020 in progress.

0.14.30: Dizi ayrıntısında sezon başına 16:9 bölüm rafı, "Sezon N" ve "2 / 10" sayacı, ilk odak bölümde, sezonlar arası Yukarı/Aşağı; kütüphanede normal yazıda sabit başlık ve ızgara kipinde "9 / 80" konum sayacı. [Sözleşme](../design/TIVIMATE_SEASONS_30.md), [QA](native-tivimate-seasons-30-qa.json). TV görsel kabulü ve Figma açık; BKL-020 in progress.

0.14.29: Teslim release derlemesine geçti, odak büyümesi kaldırıldı, Canlı TV/Filmler/Diziler TiviMate mantığıyla kategori sütunu ve numaralı satır/bilgi paneliyle yeniden yapıldı. [Sözleşme](../design/FLUID_TIVIMATE_29.md), [QA](native-fluid-29-qa.json). TV görsel kabulü açık; BKL-020 in progress.

0.14.28: Gezinme sol menüye taşındı, Ara TCL düzenine geçti, ana sayfada Geri çıkış sorusu açar. [Sözleşme](../design/SIDE_NAV_SEARCH_28.md), [QA](native-side-nav-28-qa.json). Fiziksel TV görünümü, kullanıcı kabulü ve Figma açık; BKL-020 in progress.

0.14.27: Ara ve bölüm araması yazarken sonuçları klavyenin üstünde gösterir; bölüm aramasında yazmaya başlayınca oluşan çökme giderildi. [Sözleşme](../design/SEARCH_TYPING_27.md), [QA](native-search-typing-27-qa.json). Fiziksel TV görünümü ve kullanıcı kabulü açık; BKL-020 in progress.

0.14.26: UI denetimi 56 doğrulanmış bozukluk buldu; 46 native ve 8 prototip bulgusu düzeltildi, normal yazı 95/98 ve 2.0 yazı 31/34 (kalanlar 0.14.25 ile aynı ortam sınırı). BKL-020 kullanıcı/Figma/fiziksel kabul nedeniyle in progress kalır. [QA](native-ui-repair-26-qa.json).

0.8 ek kanıt: 68 JVM / 46 emülatör akışı; son Ayarlar pencere odağı düzeltmesinden sonra ilgili 19 akış tekrarlandı. 1.5 ve 2.0 fontta iki gezinme turu. BKL-020 menü metin taşması, BKL-007 bölüm→dizi favorisi ve BKL-021 kaynak kayıt/silme yarışları düzeltildi. Bunlar kullanıcı görsel onayı, tüm ekran erişilebilirliği ve fiziksel kapıları tamamlamaz. [0.8 QA](native-lifecycle-qa.json).

0.9 geliştirme ara kanıtı: 79 JVM / 24 emülatör akışı veri katmanı ve eski tek kaynaklı davranışı doğruladı. Çoklu kaynak ekranları henüz bağlı değildir; BKL-027 tamamlanmadı. [Ara QA](native-multisource-foundation-qa.json).

0.9 native entegrasyonu: 79 JVM /63 benzersiz emülatör akışının son sonucu geçti; tekrar ve düzeltme turları [QA](native-multisource-qa.json) içinde. Eski0.9 veri-temeli notu önceki aşamanın tarihli kaydıdır. BKL-027 görsel cache/recovery/kullanıcı kapsamı bitmeden done değildir.

0.10: kaynak sahipli görsel temizliği, yavaş gövde iptali ve bozuk registry/legacy kurtarması;79 JVM/44 benzersiz emülatör vakasının son sonucu başarılı. Büyük font recovery açıklamaları görünürlük testiyle düzeltildi. BKL-027 kullanıcı/çok kaynak turu tamamlanmadan review kalır. [QA](native-source-recovery-qa.json).

0.11: 16 kaynak, uzun/aynı adlar, tam ad/hata okuma ve sabit kaldırma onayı;79 JVM,tek temiz tur25 ilgili emülatör vakası ve dört yerleşim vakasının üç font tekrarı geçti. BKL-027 kullanıcı/performance kapıları nedeniyle review kalır. [QA](native-source-layout-qa.json).

0.12: paylaşılan arama ön hazırlığı ve art index bakımı;83 JVM/38 emülatör vakası.60bin kayıtlı debug tanı fiziksel p95 onayı değildir. BKL-013/027 review kalır. [QA](native-performance-qa.json).

0.13: native EPG/şimdi–sonra/ayrıntı, şifreli XMLTV override ve kalıcı saat dilimi;109 JVM/77 emülatör vakası,16 vakanın1.5/2.0 font tekrarı. BKL-011 gerçek sağlayıcı/cihaz ve kullanıcı kapıları nedeniyle review kalır. [QA](native-guide-qa.json).

14 Ekim gerçek rehber ek kontrolü: iki metadata HTTP200; Android üretim veri yolunda253kanal/10588program ve402katalog satırı eşleşmesi,1opt-in test başarılı. APK değişmedi. BKL-011 cihaz/kullanıcı/diğer sağlayıcı kapıları nedeniyle review; [kaynak incelemesi](../research/PROVIDER_EPG_AUDIT.md), [QA](provider-guide-qa.json).

0.14.11: BKL-019/020 için ana sayfa afiş ve devam kartları yenilendi; üç fontta raf testi ve normal fontta üç regresyon geçti. [Kapsam](../design/HOME_SHELVES.md). Kullanıcı görsel onayı beklediği için durumlar değişmedi.

0.14.12: BKL-019/020 uzun başlık/iki kaynak devam kartları üç fontta testli; eski APK üzerinde doğrulanan süre kesilmesi düzeltildi. [Sözleşme](../design/CONTINUE_CARDS.md). Kullanıcı görsel kabulü açık.

0.14.13: BKL-019/020 ana sayfa üst alanı üçfontta testli. [Sözleşme](../design/HOME_HERO.md). Büyük yazıda bazı menü etiketlerinin görsel yakınlığı takip edilmeli; kullanıcı onayı açık.

0.14.13 menü takip sonucu: Görsel yakınlık şüphesi karakter kutusu ve eski/yeni piksel ölçümleriyle doğrulanmadı. Üçfontta yeni kapsam6/6; üretim düzeni korunur. [Kanıt](../design/NAVIGATION_PAINT_AUDIT.md). Menü şüphesi açık hata değildir.

BKL-0130.14.13aşama tanısı: yaklaşık2sJSONçözümü ana maliyet; dizi satırı prototipi aynı60bin kayıtta medyan%48daha kısa çözüldü. Üretim biçimi ve APK değişmedi. [Rapor ve uygulama kapıları](CATALOG_STAGE_PROFILE.md).

BKL-0130.14.14: Kompaktv2kayıt üretime alındı; v1uyumluluk/limit/şifreli hesap ayrımı ve63175kayıtlı süreç yeniden açılışı testli. [QA](native-compact-cache-qa.json). İlk sorgu ve fiziksel p95 açık; durumreviewkalır.

BKL-013/023 ilk arama tanısı: doğrudan model yayını ilk sorguda 748–786 ms; sonraki sorgular hızlı. İndeks/await/find ayrımı sırada; üretim ve review durumu değişmedi. [QA](native-search-stage-qa.json).

BKL-013/023 · 0.14.15: Değişmeyen katalogda hazırlanan indeksin StateFlow kimliğiyle uyuşmaması düzeltildi. Eski kodda başarısız/yeni kodda geçen kimlik kontrolü ve 3 cihaz akışı; ilk sorgu toplam yaklaşık 450 ms. Önceki sonraki-sorgu model süreleri düzeltilmiştir. [QA](native-search-index-reuse-qa.json). Fiziksel p95 açık, review korunur.

BKL-020/023 · 0.14.16 arama afişleri/uyarlanan sütun/ortak odak görünürlüğü: üç font ve iki regresyon akışı geçti. [QA](native-search-density-qa.json). Genel görsel kabul ve fiziksel test açık.

BKL-020 · 0.14.17: Kaynak türü/sayı/ad/güncelleme hiyerarşisi ve grup odağı; final üç fontta 6 akış + 2.0 hata akışı geçti. [QA](native-source-hierarchy-qa.json). Genel görsel kabul açık.

BKL-020 · 0.14.18: Kütüphane eksik kapak/yıl hizası üç fontta doğrulandı; final büyük yazı ve D-pad dönüş testi geçti. [QA](native-library-captions-qa.json). Genel görsel kabul açık.

0.14.18 bütünlük kontrolü: normal/2.0 fontta gezinme ve yerel oynatıcı 4/4; [QA](native-cohesion-qa.json). BKL-020 kullanıcı/Figma kabulü bekler; SAFE01 native eksikliği BKL-028 olarak görünür hale getirildi.

0.14.18 oynatıcı yaşam döngüsü: normal oynayan/duraklatılmış bölümün arka plan konumu ve dönüş davranışı için yeni doğrudan Player kontrolü; sonraki sezon ve geri sayım iptaliyle 3/3. [QA](native-player-lifecycle-qa.json). BKL-010 fiziksel doğrulama yerine geçmez.

BKL-028 · İncelenen native giriş noktaları ve kimlik eksikleri [PIN/gizleme entegrasyon sözleşmesinde](../architecture/PARENTAL_ACCESS.md). Uygulama başlamadan kategori kimliği/bölüm ebeveyni ve PIN saklama kararları gerekir. Native özellik halen yok; planned durumu korunur.

BKL-028 · 0.14.19 kimlik temeli ve cache v3 hazır; 122 JVM/13 cihaz kontrolü. PIN ve ortak karar motoru henüz yok. [QA](native-content-identity-qa.json).

BKL-028 · Saf AccessPolicy karar bölümü ve 12 kapsam testi eklendi. [Sözleşme](../architecture/ACCESS_DECISIONS.md). Henüz UI/ViewModel entegrasyonu, PIN doğrulama/saklama yok; in progress korunur.

BKL-020 · 0.14.20: ortak kenar/üst menü payı, ana sayfa vitrini ve büyük yazılı başlık kaydırma düzeltildi. [Sözleşme](../design/VISUAL_POLISH_20.md). Kullanıcı görsel kabulü ve fiziksel TV kontrolü açık; in progress korunur.

BKL-020 · 0.14.21: parça seçimindeki dış odak çizgisi için güvenli kaydırma payı; eski sürümde başarısız/yeni sürümde geçen test ve 6 ilgili kontrol. [QA](native-track-focus-21-qa.json). Kullanıcı görsel kabulü açık.

BKL-020 · 0.14.22: iki satırlı bölüm başlığı, hizalı metadata ve fotoğraftan bağımsız metin yüzeyi; üç fontta detay/görünürlük + oynatıcıdan dönüş 4/4. [QA](native-episode-cards-22-qa.json). Kullanıcı kabulü açık.

BKL-020 · 0.14.22 karışık bölüm verisi: eksik kare/uzun-kısa başlık/Devam et aynı sırada üç fontta 3/3; [QA](native-episode-mixed-qa.json). Kullanıcı görsel kabulü açık.

BKL-020 · 15 Ekim: 0.14.22 fiziksel TV üzerine veri silmeden kuruldu, cihaz paket sürümü doğrulandı. [Görsel inceleme](../../design/reviews/LATEST.md) hazır. Bu kurulum BKL-010 medya testini veya kullanıcı görsel kabulünü tamamlamaz.

BKL-010/020 · 15 Ekim fiziksel 0.14.22 açılış isteği başarılı, süreç kaldı; TV ekranı Asleep olduğundan görünür katalog/medya kabulü yapılmadı. Bir şifreli katalog dosyası mevcut. [QA](physical-tv-22-opening-qa.json). Durumlar açık kalır.

BKL-028 · PIN doğrulayıcı temel hazır, dört yeni test; [QA](native-pin-credential-qa.json). UI/şifreli kalıcılık/deneme sınırı bağlı değil; in progress.

BKL-028 · PIN deneme rezervasyonu ve monoton zaman/reboot mantığı 8 testle hazır. [QA](native-pin-attempts-qa.json). Şifreli kalıcılık, Android adaptörü ve UI henüz bağlı değil.

BKL-028 · Şifreli atomik koruma deposu ve snapshot biçimi: 4 yeni JVM/4 Android kontrolü geçti. [QA](native-protection-store-qa.json). Saat/kontrolcü/UI/erişim girişleri henüz bağlı değil.

BKL-020 · 0.14.23: ana vitrin hiyerarşisi, hafif yardımcı kontroller, ana sayfa afiş başlığı/yıl hizası; normal/2.0 fontta ilgili akışlar geçti. [QA](native-visual-23-qa.json). Kullanıcı/Figma/fiziksel kabul açık; in progress korunur.

BKL-020 · 0.14.24: raf başlığı/kart birlikte hizalama ve ölçülü son raf alt payı; üç fontta gezinme/geri dönüş, büyük yazıda uzun kaynak/Devam et ve yenileme toplam 11/11. [QA](native-shelf-24-qa.json). Kullanıcı/Figma/fiziksel kabul açık.

BKL-020 · 0.14.24 hızlı yön değişimi: aynı kart kimliği/OK/Geri kontrolü 1.5 ve 2.0 fontta geçti; normal font testi askıya alınmış emülatörde canlı/bekliyor. [QA](native-shelf-reversal-qa.json). Tamamlandı sayılmaz.

Performans tanı altyapısı · Çevrimdışı kare çözümleyicisi ve 9 sentetik kontrol hazır; [QA](frame-diagnostics-tool-qa.json). Gerçek cihaz ölçümü ve NFR kabulü tamamlanmadı.

BKL-020 · 0.14.25: ortak seçim çizgisi/semantiği, kaynak alanı kontrastı ve sade arama kontrolleri. 149 JVM başarılı/1 atlanan; 135 farklı normal-font emülatör kontrolü ve 20 büyük yazı tekrarı başarılı. Host GPU koşusunun kesilen bölümü SwiftShader ile tamamlandı; [QA](native-polish-25-qa.json). Prototip 13 kontrol ve kare aracı 9 test geçti. Kullanıcı/Figma/fiziksel kabul açık, in progress korunur.
