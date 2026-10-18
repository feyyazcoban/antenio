# IPTV Oynatıcı Ürün Gereksinimleri

**Sürüm:** 0.2 | **Tarih:** 13 Ekim 2026 | **Durum:** İlk sürüm içeriği kesinleşti; tasarım ve teknik ayrıntılar öneri

**Çalışma referansı:** NEXTV Figma dosyası | **İlk platform:** Android TV ve Google TV

## 1 Ürün yönü ve karar özeti

Kullanıcının M3U bağlantısını veya Xtream Codes hesabını ekleyerek canlı yayınlarını ve desteklenen film/dizi kütüphanesini televizyon kumandasıyla rahatça kullanabildiği bir IPTV oynatıcı geliştiriyoruz. İlk platform Google TV; sonraki platformlar LG webOS, Samsung Tizen, Android mobil ve iOS olacak.

Ürünün temel vaadi, kaynağı ekledikten sonra izlenecek içeriğe kısa ve anlaşılır bir yoldan ulaşmak. İlk tasarım kararlarını kurulum, içerik bulma, yayın rehberi ve oynatma akışları üzerinden vereceğiz. Görsel dili Google'ın resmî Android TV tasarım kiti ve TV Material bileşenleri üzerine kuracağız. [R09, R10]

**Önerilen ürün yaklaşımı:** TiviMate'in TV odaklı rehber ve kanal erişimini, IPTVX ve NexTv'nin düzenli kütüphane ve izlemeye devam etme yaklaşımıyla birleştirmek. Bu, özellik araştırmasından çıkan ürün önerisidir; rakiplerin performansına ilişkin ölçüm sonucu değildir.

### Kesinleşen kararlar

- Ürün bir IPTV oynatıcı olacak.
- İlk sürüm canlı TV, filmler ve dizileri birlikte içerecek. Kullanıcı kararı: 13 Ekim 2026.
- İlk platform Android TV ve Google TV olacak.
- M3U bağlantısı ve Xtream Codes hesabı desteklenecek.
- Arayüz Google'ın TV tasarım kitini temel alacak.
- LG, Samsung, Android mobil ve iOS sonraki fazlarda ele alınacak.
- İlk çalışma ürün kapsamı, rakip analizi ve ekran akışlarını netleştirecek.

### Henüz kesinleşmeyen kararlar

İlk yayında canlı TV, film ve dizi birlikte bulunacak. VOD, isteğe bağlı film ve dizi oynatımı anlamına gelir ve ilk sürümün zorunlu kapsamındadır. Ürün adı, hedef ülkeler, gelir modeli, ekip kapasitesi ve cihaz alt sınırı henüz karara bağlanmadı.

**İsim kararı:** Piyasada NexTv adlı bir IPTV oynatıcı bulunuyor. [R08] 17 Ekim 2026'da kullanıcı ürün adı olarak NEXTV'yi seçti (D066); mağaza yayını öncesi marka/ad çakışması kontrolü açık.

## 2 Kullanıcılar ve temel ihtiyaçlar

| Kullanıcı durumu | Yapmak istediği iş | Tasarım karşılığı |
| --- | --- | --- |
| İlk kez kaynak ekleyen kullanıcı | Hesabını veya bağlantısını hata yapmadan tanıtmak | İki açık giriş yöntemi, anlaşılır alanlar, düzeltilebilir hatalar |
| Her gün TV izleyen kullanıcı | Son izlediği veya favori kanalına dönmek | Ana ekranda son kanallar, favoriler ve tek seçimle oynatma |
| Yayınları karşılaştıran kullanıcı | Şimdi ne olduğunu ve sonra ne başlayacağını görmek | Kanal ve zaman eksenli EPG yani elektronik yayın rehberi |
| Film ve dizi izleyen kullanıcı | İçerik bulmak, doğru bölümü açmak, kaldığı yerden sürdürmek | Kütüphane, detay, sezon ve bölüm seçimi, izleme ilerlemesi |
| Ortak TV kullanan hane | Belirli kaynakları ve kategorileri sınırlamak | PIN koruması, gizleme ve arama sonuçlarında aynı erişim kuralı |
| Birden fazla cihaz kullanan kullanıcı | Favorilerini ve ilerlemesini taşımak | Sonraki fazda isteğe bağlı hesap ve senkronizasyon |

Bu kullanıcı grupları araştırma hipotezidir. Tasarım doğrulamasında en az beş katılımcıyla kaynak ekleme, kanal bulma, rehber kullanma, film açma, diziye devam etme ve hata sonrası devam etme görevleri denenmeli. Hedef ülke ve kullanıcı kitlesi belirlendiğinde örneklem genişletilmeli.

## 3 Rakiplerden öğrenilecekler

Karşılaştırma, 13 Ekim 2026 tarihinde erişilen üretici siteleri, resmî mağaza açıklamaları ve geliştirici belgelerine dayanır. Aşağıdaki uygulamalar farklı ürün problemlerini temsil eden referanslardır; liste bir pazar payı veya kalite sıralaması değildir. Özelliklerin bulunması, tüm cihazlarda aynı kaliteyle çalıştıkları anlamına gelmez. Gerçek cihaz deneyimi ayrıca sınanmalıdır.

### TiviMate

**Doğrulanan kapsam:** Android TV ve kumanda odaklı kullanım; M3U, Xtream Codes ve Stalker kaynakları; EPG, birden fazla liste, favoriler, arama, catch-up, kayıt, ebeveyn kontrolü ve çoklu ekran. Dokunmatik telefon/tablet kullanımına göre optimize edilmediğini belirtiyor. [R01]

**Alınacak fikir:** Canlı TV ve rehberi ürünün merkezine yerleştirmek; favori kanala erişimi kısa tutmak. **Doğrulama görevi:** Bir kanal açıldıktan sonra rehbere ve aynı kanala geri dönmek için gereken tuş sayısını ölçmek.

### Sparkle TV

**Doğrulanan kapsam:** M3U, Xtream Codes, XMLTV; kanal ve program rehberi; ses/altyazı seçimi; otomatik kare hızı; catch-up, film/dizi, zaman kaydırma, kayıt ve çoklu ekran. Bazı işlevler Plus katmanında sunuluyor. [R02]

**Alınacak fikir:** Canlı yayın, geçmiş yayın ve kayıt işlevlerini rehber bağlamında toplamak. **Dikkat:** Sağlayıcı arşivi, yerel zaman kaydırma ve kayıt farklı yeteneklerdir; tek bir düğmeyle aynı şeymiş gibi sunulmamalı.

### Televizo

**Doğrulanan kapsam:** Android telefon, tablet ve TV; çoklu liste, EPG, catch-up, Chromecast, sıralama, arama, favoriler, ebeveyn kontrolü ve ses/altyazı seçimi. [R03]

**Alınacak fikir:** Kaynak ekleme ve temel liste yönetimini anlaşılır tutmak; aynı veri modelinden TV ve mobil kullanım üretmek. **Dikkat:** TV ekranlarını mobil arayüzün büyütülmüş hali olarak tasarlamamak.

### OTT Navigator

**Doğrulanan kapsam:** Android TV/Google TV ve Android; farklı sağlayıcı kaynakları, M3U, Xtream kurulumu, kanal etiketleri, özel sıralama, EPG ayarları, yedekleme, çoklu görüntü ve gelişmiş oynatıcı seçenekleri. Resmî SSS 1.7.5.2 ile Premium kayıt desteğini; değişiklik kayıtları film/dizi için devam etme ve metadata özelliklerini belirtiyor. [R04, R05]

**Alınacak fikir:** Büyük ve düzensiz listeler için kategori yönetimi, filtreleme ve EPG düzeltme araçları. **Dikkat:** Gelişmiş ayarları temel kullanım akışından ayırmak; ilk kurulumda seçenek yükünü sınırlamak.

### IPTVX

**Doğrulanan kapsam:** M3U/Xtream, çoklu liste, film/dizi kütüphanesi, devam etme, EPG, catch-up, çoklu kanal, ebeveyn kontrolü ve desteklenen cihazlarda senkronizasyon. Telefonla TV eşleştirme ve iOS indirmeleri de belgeleniyor. Android/Android TV işlevleri sürüme göre değişebiliyor. [R06]

**Alınacak fikir:** İçeriği afiş, detay, sezon ve ilerleme bilgisiyle anlaşılır bir kütüphaneye dönüştürmek. **Dikkat:** Cihazlar arası veri senkronizasyonu ile mağazadan satın alınan abonelik haklarını ayrı tasarlamak. [R07]

### UHF

**Doğrulanan kapsam:** iPhone, iPad, Apple TV ve Mac; çoklu liste, EPG, catch-up, AirPlay/Chromecast, çoklu görüntü, ses/altyazı, ebeveyn kilidi, içerik düzenleme ve Trakt entegrasyonu. UHF Server üzerinden kayıt, Pro ile cihazlar arası senkronizasyon sunuluyor. [R16]

**Alınacak fikir:** Mobil ve Apple tarafında platforma uygun kullanım, düzenli kişisel kütüphane ve akış içinde erişilebilir oynatıcı kontrolleri. **Dikkat:** Döküm, HDR ve çoklu oynatma yeteneklerini cihaz ve kaynak koşullarından bağımsız vaat etmemek.

### SmartOne IPTV

**Doğrulanan kapsam:** Samsung Tizen, LG webOS ve başka TV platformları; M3U/Xtream, canlı yayın, film/dizi, favoriler, catch-up, ebeveyn kontrolü, kategori gizleme, TMDb bilgileri ve cihaz yönetimi. [R17]

**Alınacak fikir:** Farklı televizyon ekosistemleri için ortak ürün yapısı ve uzaktan kaynak yönetimi. **Dikkat:** Geniş platform listesi, her model yılında tüm özelliklerin eşit olduğu anlamına gelmez; model ve oynatma test matrisi gerekir.

### NexTv

**Doğrulanan kapsam:** Android/Android TV, iPhone/iPad ve masaüstü; çoklu liste, arama, EPG/catch-up, favori grupları, TMDb bilgileri ve izleme geçmişi/yer imi senkronizasyonu. Resmî sayfa kurulum için Xtream Codes ve 1-Stream bilgilerini belirtiyor. [R08]

**Alınacak fikir:** Kütüphanede aynı içeriğin dil ve kalite alternatiflerini anlaşılır biçimde toplamak. **Dikkat:** İsim ve marka ayrışması ayrıca çözülmeli. Genel M3U bağlantısı desteği bu kaynak üzerinden kesinleştirilmemeli.

## 4 Özellik karşılaştırması ve ürün sonuçları

**V:** Resmî kaynakta belirtiliyor. **D:** İncelenen belgede doğrulanmadı; özellik yok anlamına gelmez. **K:** Platform, paket veya cihaz koşuluna bağlı olduğu açıkça belirtiliyor. Bu tablo özellik varlığını karşılaştırır, kullanım kalitesini puanlamaz.

| Uygulama | Çoklu liste | EPG | Catch-up | Kayıt | Çoklu ekran | Cihazlar arası veri |
| --- | --- | --- | --- | --- | --- | --- |
| TiviMate [R01] | V | V | V | V | V | D |
| Sparkle [R02] | K | V | K | K | K | D |
| Televizo [R03] | V | V | V | D | D | D |
| OTT Navigator [R04 R05] | V | V | V | K | K | Yedek taşıma |
| IPTVX [R06 R07] | V | V | K | Sunucu ile | K | K |
| UHF [R16] | V | V | V | Sunucu ile | V | K |
| SmartOne [R17] | V | D | V | D | D | Cihaz yönetimi |
| NexTv [R08] | V | V | V | D | D | V |

Kayıt, çoklu ekran, catch-up ve senkronizasyon için sağlayıcı bağlantı limiti, depolama, işletim sistemi ve ücretli paket koşulları ayrıca incelenmelidir. SmartOne'ın cihaz yönetimi ve OTT Navigator'ın yedek taşıması, otomatik izleme ilerlemesi senkronizasyonuyla aynı işlev değildir.

### Ürünümüze aktarılacak güçlü yanlar

| İhtiyaç | Referans | Ürün kararı |
| --- | --- | --- |
| Kumandayla hızlı kanal erişimi | TiviMate | Canlı TV, favoriler ve son izlenen kanallar çekirdek kapsamda |
| Rehberden içeriğe geçiş | TiviMate ve Sparkle | EPG temel kapsamda; geçmiş yayın yeteneği ayrı kontrol edilir |
| Kolay kaynak kurulumu | Televizo ve IPTVX | M3U ve Xtream için ayrı formlar, tek doğrulama akışı |
| Düzenli film/dizi kütüphanesi | IPTVX ve NexTv | Film/dizi ilk sürümde; zengin metadata sonraki genişletme |
| Büyük listelerin yönetimi | OTT Navigator | Kaynak filtresi, gizleme, favori ve kategori düzeni |
| TV platformlarına yayılım | SmartOne | Ortak veri sözleşmesi, platforma özel oynatıcı ve odak davranışı |

**Fark yaratma hipotezi:** Kaynak eklemedeki hataları anlaşılır açıklamak, rehber eşleşmesini görünür kılmak ve kumandayla geri dönüşte kullanıcının yerini korumak. Bunların rakiplerde hiç bulunmadığı iddia edilmiyor; prototip testlerinde karşılaştırılacak ürün hedefleri olarak ele alınıyor.

## 5 Önceliklendirilmiş özellik seti

P0, canlı TV, film ve diziyi kapsayan ilk yayın için zorunlu özellikleri; P1, ilk yayın sonrasındaki genişletmeyi; P2 ise ileri özellikleri ifade eder. Ayrı ve isteğe bağlı bir film/dizi paketi yoktur. Ekip kapasitesi teslim sırasını ve takvimi belirler.

| Kod | Özellik | Öncelik | Teslim sınırı |
| --- | --- | --- | --- |
| SRC01 | M3U bağlantısı ekleme | P0 | URL, liste adı, isteğe bağlı XMLTV bağlantısı |
| SRC02 | Xtream Codes hesabı | P0 | Sunucu, kullanıcı adı, parola ve bağlantı doğrulama |
| SRC03 | Birden fazla kaynak | P0 | Ekleme, yenileme, düzenleme, silme ve kaynak filtresi |
| LIVE01 | Canlı TV kataloğu | P0 | Kategori, kanal listesi, logo, mevcut program |
| EPG01 | Yayın rehberi | P0 | Şimdi/sonra, zaman ızgarası, program ayrıntısı |
| LIB01 | Favoriler ve son izlenenler | P0 | Kanal/film/dizi favorileri; son kanallar ve hızlı erişim |
| FIND01 | Arama | P0 | Kanal, film ve dizi; tür ve kaynak filtreleri |
| PLAY01 | Oynatıcı | P0 | Oynatma, duruma uygun kontroller, ses ve altyazı; film/dizide 0,5×–2× oturumluk hız seçimi (D077) |
| PLAY02 | Bağlantı ve hata yönetimi | P0 | Sınırlı yeniden deneme, anlaşılır hata, yeniden başlatma |
| UX01 | Kumanda ve odak sistemi | P0 | Dört yön, OK, Back, odak geri yükleme |
| SAFE01 | PIN ve gizleme | P0 | Kategori/kaynak koruması, arama ve geçmişe aynı kurallar |
| SYS01 | Ayarlar ve erişilebilirlik | P0 | Dil, saat dilimi, metin/kontrast, kaynak durumu |
| VOD01 | Film kataloğu ve ayrıntı | P0 | Sağlayıcıdan gelen afiş, başlık, açıklama ve oynatma |
| VOD02 | Dizi sezon ve bölümleri | P0 | Bölüm seçimi, sonraki bölüm, otomatik geçiş ayarı |
| VOD03 | İzlemeye devam et | P0 | Cihaz üzerinde ilerleme, baştan başlatma, geçmişi temizleme |
| EXT01 | Telefonla kaynak ekleme | P1 | QR/kısa kod ile güvenli ve süreli eşleştirme |
| EXT02 | Catch-up | P1 | Sağlayıcı arşivine uygun geçmiş programları oynatma |
| EXT03 | EPG düzeltme | P1 | Elle eşleştirme, saat kayması düzeltmesi ve kaynak seçimi |
| EXT04 | Gelişmiş kütüphane | P1 | Metadata zenginleştirme, izleme listesi ve özel gruplar |
| EXT05 | Hesap ve senkronizasyon | P1 | Favori/ilerleme eşitleme, çakışma ve silme davranışı |
| ADV01 | Kayıt ve yerel timeshift | P2 | Depolama, zamanlama ve cihaz yaşam döngüsü doğrulaması |
| ADV02 | Çoklu ekran | P2 | Donanım ve sağlayıcı bağlantı sınırlarına göre |
| ADV03 | Mobil döküm ve indirme | P2 | Mobil fazda kaynak ve platform uygunluğuna göre |

**İlk kapsam dışında önerilenler:** İçerik aboneliği satışı, içerik barındırma, sosyal akış, sohbet, yapay zekâ öneri sistemi, spor skoru servisi, yerleşik VPN, Stalker/MAC portalı ve masaüstü istemcisi. Bunlar temel oynatma işinin teslimi için gerekli değil.

**İlk yayın kararı:** VOD01-03, tüm içerik türlerinde arama ve ana ekrandaki izlemeye devam et satırı P0 kapsamındadır. Zengin metadata, bulut senkronizasyonu, kayıt ve çoklu ekran sonraki fazlarda kalır.

## 6 Bilgi mimarisi ve ekran akışları

### Ana gezinme

Sol tarafta TV Material tabanlı modal gezinme çekmecesi önerilir. Dar durumda simgeler görünür; açıldığında etiketler görünür ve seçim OK ile yapılır. Ana hedefler **Ana Sayfa, Canlı TV, Rehber, Filmler, Diziler ve Favoriler**; arama üst bölümde, kaynaklar ve ayarlar alt bölümde yer alır. Filmler ve Diziler ayrı girişlerdir. Kaynağın sunmadığı tür için açıklayıcı boş durum gösterilir. Modal çekmecede odak değişimi sayfayı kendiliğinden değiştirmez. [R11]

### Ekran envanteri

| Ekran | Öncelikli içerik ve eylem | Zorunlu durumlar |
| --- | --- | --- |
| Karşılama | Kaynak ekle, yöntem seç | İlk kullanım, daha önce eklenmiş kaynak |
| M3U ve Xtream formları | Bilgileri gir, bağlantıyı doğrula | Alan hatası, kimlik doğrulama hatası, zaman aşımı |
| İçe aktarma | İlerleme, bulunan içerik sayısı | İptal, kısmi başarı, boş liste, yeniden deneme |
| Ana Sayfa | İzlemeye devam et, son kanallar, favoriler | Yeni kullanıcı, içerik yok, veri yenileniyor |
| Canlı TV | Kategori ve kanal listesi | Logo yok, kanal yok, kilitli içerik |
| Rehber | Kanal satırları ve program zamanları | EPG yok, kısmi veri, gün değişimi |
| Oynatıcı | Yayın, kanal/program bilgisi, kontroller | Yükleniyor, oynuyor, hata, ses/altyazı paneli |
| Arama | Kaynak ve içerik türüne göre sonuç | Sonuç yok, klavye açık, geri dönüş |
| Filmler ve Diziler | Ayrı kataloglar, kategori ve kaynak filtresi | Boş, eksik afiş, liste sonu |
| Favoriler | Kanal, film ve dizi filtreleri | Henüz favori yok, silinmiş içerik |
| Film ve dizi ayrıntısı | Oynat/devam et, sezon/bölüm | Eksik metadata, bölüm yok, kilitli |
| Kaynaklar ve ayarlar | Kaynak sağlığı, düzenleme, PIN | Yenileme başarısız, silme teyidi |

### Temel kullanıcı yolları

1. **İlk yayın:** Karşılama → M3U veya Xtream → Doğrulama → İçe aktarma → Ana Sayfa → Kanal → Oynatıcı.
2. **Günlük kullanım:** Ana Sayfa → Son kanallar veya Favoriler → Oynatıcı. Ana ekrandan favori kanala en fazla iki OK seçimi hedeflenir.
3. **Program keşfi:** Rehber → Program → Canlı yayını aç. Geçmiş program yalnızca EXT02 etkin ve kaynak uygun olduğunda oynatılabilir; ileri tarihli program ayrıntı gösterir.
4. **Film ve dizi:** Filmler, Diziler veya Arama → Ayrıntı → Oynat/devam et → Oynatıcı. Back aynı arama sorgusuna, seçili karta ve kaydırma konumuna döndürür.
5. **Hata çözümü:** Oynatma hatası → Tekrar dene veya Kaynağı kontrol et → Sorun çözülünce aynı içerik bağlamına dön.

## 7 Google TV tasarım kuralları

Resmî kaynak Google'ın Android TV tasarım kiti, tasarım kılavuzları ve Compose for TV bileşenleridir. Google TV ana ekranının birebir kopyası hedeflenmez. Kit; düğme, kart, liste, sekme ve gezinme gibi temel bileşenleri sağlar. IPTV'ye özgü EPG ızgarası, kaynak formları ve oynatıcı katmanları ayrıca tasarlanmalıdır. [R09, R10]

### Kumanda ve odak

- Her ekranın ilk odak hedefi tanımlanır. Kaydırılabilir bir bölgeye dönüşte önceki öğe korunur; öğe silinmişse en yakın geçerli öğeye geçilir.
- Yön tuşları uzamsal olarak beklenen komşuya gider. Ekranın kenarında odak kaybolmaz ve görünmeyen bir kontrole atlamaz.
- Seçili içerik ile o anda odaklanan içerik farklı durumlardır. Yalnızca renk değişimi yeterli kabul edilmez; belirgin çevre çizgisi, yüzey veya ölçek değişimi kullanılır.
- Uzun basış ek kısayol olabilir; zorunlu bir işlevin tek erişim yolu olamaz. Renkli ve sayısal kumanda tuşları zorunlu tutulmaz.
- Back önce açık paneli, sonra oynatıcı katmanını kapatır; ardından geldiği listeye döner. Ana gezinmeden devam eden Back uygulamadan çıkışa ulaşır; çıkışı engelleyen onay döngüsü kurulmaz. TV arayüzünde sürekli bir ekran içi geri düğmesi kullanılmaz. [R12]

### Görsel sistem

Koyu tema ilk öneridir. Yazı tipi ve boyutlar kitteki TV ölçeğinden alınır; uzak mesafeden okunabilirlik gerçek TV'de sınanır. Tasarım temeli 16:9 tuvaldir; 1080p ve 4K sunumları kontrol edilir. Geliştirmede piksel değerleri doğrudan kopyalanmaz, yoğunluktan bağımsız ölçülere çevrilir.

Kartlar için 16:9 önizleme, 1:1 kanal logosu ve 2:3 afiş seçenekleri kullanılabilir. Kart türü içeriğin işine göre seçilir; her listeye aynı afiş görünümü uygulanmaz. [R13]

### Canlı TV ve rehber

Kanal listesi ile zaman ızgarası ayrı yoğunluk gereksinimlerine sahiptir. Rehberde dikey eksen kanal, yatay eksen zamandır. Program uzunluğuna göre değişen hücrelerde odak hedefi ve zaman konumu korunur. EPG olmayan kanal oynatılabilir kalır ve “Program bilgisi yok” gösterir.

Odağa gelmek yayın başlatmaz. İlk sürümde hareketli kanal önizlemesi varsayılan olarak kapalıdır; böylece tek bağlantılı kaynaklarda ikinci akış açılmaz. Canlı yayın için durdurma ve geri sarma yalnızca kaynak yeteneği varsa sunulur.

### Bileşen teslim listesi

Navigation rail/drawer, TV button, içerik kartı, kanal satırı, kategori çipi, kaynak kartı, giriş alanı, EPG hücresi, ilerleme göstergesi, oynatıcı kontrolü ve alt panel hazırlanır. Etkileşimli her bileşen için varsayılan, odak, basılı, seçili ve devre dışı durumlar; ilgili ekranlarda yüklenme, boş, kısmi veri, hata ve kilitli durumlar tanımlanır.

## 8 İşlevsel kabul ölçütleri

Aşağıdaki ölçütler tasarım ve geliştirme için önerilen teslim sınırlarıdır. Kaynak veya cihazın sağlamadığı bir yetenek uygulamada varmış gibi gösterilmez.

### Kaynak ekleme ve yenileme

**SRC01:** Geçerli bir M3U URL'siyle kategori, kanal adı, logo ve varsa tvg-id bilgisi işlenir. UTF-8/Türkçe karakterler korunur. Hatalı satırlar raporlanır; tek hatalı satır bütün listeyi düşürmez. Liste dosyası ile tek bir HLS yayın adresi aynı kurulum girdisi olarak sessizce karıştırılmaz.

**SRC02:** Xtream için sunucu URL'si, kullanıcı adı ve parola ayrı alınır. Alanlar korunarak düzeltilebilir hata gösterilir. 401/403 gibi kimlik/erişim sorunları, ağ zaman aşımı ve boş katalog ayrıştırılır. Kaynağın sağladığı canlı, film ve dizi katalogları alınır. Bir tür boş veya hatalıysa diğer türler kullanılabilir kalır; kısmi başarı açıkça gösterilir.

**SRC03:** Yenileme sırasında mevcut katalog izlenebilir kalır. Başarısız yenileme son çalışan veriyi silmez. Kanal kimliği kaynakla birlikte tutulur; iki kaynaktaki aynı kanal adı birbirinin favorisini veya EPG eşleşmesini ezmez. Kaynak silme yalnızca o kaynağın yerel içerik ve sırlarını kaldırır.

### Rehber ve arama

**EPG01:** XMLTV ve desteklenen Xtream rehber verisi normalleştirilir. tvg-id öncelikli eşleştirme yapılır; belirsiz ad eşleşmesi kesinmiş gibi atanmaz. XMLTV'deki saat dilimi/ofset korunur ve kullanıcının seçtiği saat diliminde gösterilir. Yaz/kış saati geçişi test edilir.

**EPG02:** Boş rehber oynatmayı engellemez. Son güncelleme ve hata durumu kaynak ayrıntısında görülür. Önerilen önbellek hedefi, kaynak sağlıyorsa önceki bir gün ve sonraki yedi gündür; eksik günler açıkça boş görünür.

**FIND01:** Arama sonuçları kaynak ve tür bağlamını taşır. Türkçe büyük/küçük harf farkları sınanır. Sonuçtan geri dönüldüğünde sorgu, filtre, odak ve kaydırma korunur. Kilitli içerik için ad/afiş gösterimi seçilen ebeveyn politikasına uyar.

### Oynatma ve kullanıcı durumu

**PLAY01:** Asgari test kapsamı H.264/AAC ile HLS canlı ve VOD, MP4 VOD ve tanımlı MPEG-TS kaynaklarıdır. HEVC, HDR ve çok kanallı ses cihaz yeteneğine göre doğrulanır. Başarısız biçim için anlaşılır mesaj gösterilir. HLS/DASH ve örnek biçim desteği Media3 belgeleri ve gerçek cihaz testiyle kontrol edilir. [R14]

**PLAY02:** Ağ kopması, sunucu hatası ve desteklenmeyen medya ayrılır. Otomatik yeniden deneme en fazla üç girişim ve sınırlı toplam süreyle tasarlanır; kullanıcı iptal edebilir. Desteklenmeyen biçim ve kalıcı erişim hatalarında sonsuz yeniden deneme yapılmaz.

**PLAY03:** Kanal değişince önceki oturum kapatılır. Ana ekran önizlemesi ek bağlantı tüketmez. Uygulama arka plana geçtiğinde TV oynatması ve ses devamı için tek bir tanımlı davranış uygulanır; ilk sürüm önerisi oynatmayı durdurmaktır.

**VOD01-03:** Kaldığı yer kaynak/içerik/bölüm kimliğiyle kaydedilir. “Devam et” ve “Baştan başlat” ayrıdır. İlerleme hedefi her 15 saniyede ve normal çıkışta kayıttır; ani kapanma toleransı buna göre sınanır. Tamamlandı eşiği önerisi yüzde 95'tir ve ürün testinde doğrulanır. Otomatik sonraki bölüm kullanıcı tarafından kapatılabilir.



**VOD katalog kuralları:** Film ve dizi ayrımı güvenilir kaynak verisine dayanır. Başlıktan tahmin edilen sezon/bölüm bilgisi kesin veri gibi sunulmaz. Yapısı belirlenemeyen M3U girdileri erişilebilir bir belirsiz içerik durumunda kalır; sessizce kaybolmaz. Xtream için boş sezon, eksik bölüm ve farklı numaralandırma örnekleri test edilir. Afiş veya açıklama eksikliği oynatmayı engellemez.

**VOD oynatma kuralları:** Süre biliniyor ve akış destekliyorsa zaman çizgisinde arama yapılabilir. Kaydırma önizlemesi tek başına yeni oynatma oturumu başlatmaz. Sonraki bölüm yalnızca geçerli bir hedef varsa gösterilir; sezon/dizi sonunda var olmayan bölüm açılmaz. Otomatik geçiş öncesi iptal edilebilir geri sayım gösterilir. Tamamlanan içerik devam et satırından çıkarılır; geçmişten yeniden açılabilir. İlerlemesi bulunan içerik yenilemede silinmişse kullanıcı verisi başka bir başlığa taşınmaz.

**SAFE01:** PIN kilidi kanal, kaynak veya kategori seviyesinde uygulanır. Arama, favoriler, son izlenenler ve ayrıntıya doğrudan giriş aynı erişim denetiminden geçer. Gizlemek ile PIN ile korumak ayrı ayarlardır.

## 9 Kalite hedefleri ve ölçüm

Bu değerler rakip ölçümleri veya gerçekleşmiş sonuçlar değil, ilk mühendislik ve kullanılabilirlik hedefleridir. Test cihazları, ağ profili ve örnek kaynaklar sabitlenmeden yayın taahhüdüne dönüştürülmemeli.

| Ölçüt | Önerilen hedef | Ölçüm koşulu |
| --- | --- | --- |
| Kumanda tepkisi | p95 en fazla 100 ms | Tuş olayından odak görünümünün güncellenmesine |
| Sıcak katalog açılışı | p95 en fazla 2 sn | Yerel önbellek hazır; uygulama başlangıcından etkileşime |
| İlk görüntü | p95 en fazla 4 sn | Kontrollü 1080p HLS kaynağı, 25 Mbps bağlantı; seçimden ilk kareye |
| Arama yanıtı | p95 en fazla 300 ms | 10 bin kanallı yerel indeks; sorgudan sonuç görünümüne |
| İlk kurulum başarısı | En az 4/5 katılımcı | Geçerli örnek bilgilerle, yardım almadan; ilk prototip turu |
| Favori kanala erişim | En fazla iki OK seçimi | Ana ekran hazırken; yön tuşları ayrıca sayılır |
| Çökmesiz oturum | En az yüzde 99,5 | Beta için öneri; yeterli örneklem oluşmadan kabul edilmiş sayılmaz |
| Uzun oynatma | 2 saat kesintisiz test | Her temel cihaz/akış birleşiminde; kalıcı bellek artışı incelenir |

**Test profili:** En az bir düşük bellekli TV/dongle, bir güncel Google TV cihazı ve bir TV üreticisi cihazı seçilmeli. 1.000 ve 10.000 kanallı listeler; 50.000 öğeli film/dizi kataloğu; eksik logo, bozuk metadata ve kısmi EPG senaryoları hazırlanmalı. Cihaz modelleri ve minimum Android sürümü geliştirme başlangıcında sabitlenmeli.

**Olay ölçümü:** Kaynak ekleme başladı/başardı/başarısız, oynatma istendi/ilk kare/hata, arama yapıldı/sonuç açıldı ve rehberden kanal açıldı olayları önerilir. URL, kullanıcı adı, parola, kanal adı veya izlenen başlık analiz sistemine gönderilmez. Hata sınıfı, süre ve kimlik içermeyen cihaz yeteneği gibi alanlar yeterlidir. Ürün analitiği tercihi ve saklama süresi ayrıca belirlenir.

**Mahremiyet:** Kaynak sırları cihazda korumalı saklanır; loglar ve hata raporları URL içindeki sırları da maskeler. İlk sürümde uygulama hesabı zorunlu tutulmaz. İzleme geçmişini temizleme ve kaynak silme erişilebilir olmalıdır. QR eşleştirme eklendiğinde kod süreli, tek kullanımlık ve kullanıcı tarafından iptal edilebilir olmalıdır.

## 10 Platform stratejisi ve bağımlılıklar

Google TV için Kotlin, Compose for TV ve Media3/ExoPlayer önerilir. TV'ye özel Material bileşenleri kumanda ve odak davranışı için uygun başlangıçtır. Uygulamanın başka platformlara taşınması, bu arayüzün aynı kodla tüm TV'lerde çalışacağı anlamına gelmez. [R10, R14]

| Katman | Ortak tutulacak davranış | Platforma göre uyarlanacak bölüm |
| --- | --- | --- |
| Kaynak | M3U/Xtream sözleşmesi, hata türleri, kimlik kuralları | Ağ, kimlik bilgisi saklama ve yenileme altyapısı |
| Katalog | Kanal, kategori, film, dizi, bölüm ve EPG modeli | Depolama, indeksleme ve bellek sınırları |
| Kullanıcı verisi | Favori, ilerleme, gizleme ve sıralama anlamı | Yerel güvenli saklama; sonraki fazda senkronizasyon |
| Tasarım | Renk, yazı, boşluk, içerik hiyerarşisi ve durumlar | TV odağı, dokunma, klavye, ekran oranı |
| Oynatma | Başlat, durdur, seç, ara, hata ve yetenek sözleşmesi | Android Media3; Samsung AVPlay; LG medya katmanı; iOS oynatıcısı |

Samsung, AVPlay ile kendi oynatma API'sini sunar; LG webOS'un protokol ve medya yetenekleri kendi platform koşullarına tabidir. LG/Samsung fazının başında gerçek cihazda bir oynatma ve kumanda denemesi yapılmalı. Desteklenen model yılları bu sonuçla seçilmelidir. [R18, R19]

### Dış bağımlılıklar

- EPG kalitesi, yayın erişimi, arşiv süresi ve eşzamanlı bağlantı limiti kaynak sağlayıcıya bağlıdır.
- Catch-up geçmiş yayını sağlayıcıdan alır. Timeshift canlı yayının geri alınabilen bölümüdür. Kayıt ise yeni bir dosya/iş oluşturur. Üç yetenek ayrı değerlendirilir.
- İlk sürüm film/dizi kataloğu için sağlayıcı metadata'sı başlangıçtır. TMDb gibi zenginleştirme servisleri eklenirse eşleştirme hatası, kullanım koşulları, atıf, önbellek ve maliyet değerlendirilir.
- İlk sürüm doğrudan cihazdan kaynağa erişir. Bulut senkronizasyonu, QR eşleştirme ve lisans yönetimi seçilirse sunucu ihtiyacı ayrıca kapsamlandırılır.
- Uygulama içeriği kullanıcı kaynağından oynatır. Mağaza materyali ve demo akışları için kullanım hakkı olan test içerikleri hazırlanır.

## 11 Tasarım ve geliştirme sırası

Takvim, ekip büyüklüğü ve kapasite bilinmeden haftalara bölünmemelidir. Aşağıdaki sıra her adımın somut çıktısını ve tamamlanma koşulunu tanımlar.

| Adım | Çıktı | Tamamlanma koşulu |
| --- | --- | --- |
| 1 Kapsam kararı | Canlı TV + film + dizi P0; ülke/dil ve cihaz hedefi | İlk yayın içeriği ve sınırları yazılı olarak net |
| 2 Figma envanteri | Mevcut ekran ve bileşen listesi | Her ekran PRD gereksinimleriyle eşleştirilmiş |
| 3 Temel akışlar | Kurulum, canlı TV, EPG, film, dizi ve oynatıcı | Mutlu yol ile hata/boş durumları çizilmiş |
| 4 TV tasarım sistemi | Kit tabanlı bileşenler ve odak durumları | Kart, satır, düğme ve çekmece tekrar kullanılabilir |
| 5 Etkileşimli prototip | Kumanda mantığıyla dolaşılabilen ana akış | Odak tuzağı yok; Back ve listeye dönüş tanımlı |
| 6 Teknik deneme | Bir gerçek kaynakla kurulum ve oynatma | Temel cihazlarda katalog, EPG ve medya uyumlu |
| 7 Kapalı beta | Canlı TV, film ve dizi dahil bütün P0 işlevleri | Kritik hata yok; test matrisi ve hedefler incelenmiş |
| 8 Platform genişlemesi | LG/Samsung ve ardından Android/iOS istemcileri | Her platform için destek ve kalite kapısı ayrı geçilmiş |

### Figma dosyasını düzenleme önerisi

Mevcut NEXTV dosyasındaki Foundations, Components ve boş Design sayfası incelendi. Dosyada dokuz koleksiyonda 401 yerel değişken; Components sayfasında varyantlar dahil 554 bileşen/bileşen seti düğümü bulunuyor. Ürüne özel EPG, oynatıcı, klavye ve PIN bileşenleri mevcut. Bunlar yeniden üretilmeden önce değerlendirilir. [R15]

Starter planının üç sayfa sınırı nedeniyle mevcut **Foundations** ve **Components** sayfaları korunur. **03 Product & Screens** sayfasında ürün özeti, akışlar, Google TV ekranları, hareket denemeleri, teslim ve arşiv bölümleri oluşturuldu. Güncel Figma bağlantıları ve node kimlikleri proje içindeki design/figma/manifest.json dosyasında tutulur. MCP kullanım kotası nedeniyle iki metin düzeltmesi bekliyor; ayrıntısı docs/design/FIGMA.md içindedir.

Tasarım sırası kaynak ekleme, ana gezinme, canlı TV/rehber, film kataloğu/detayı, dizi/sezon/bölüm ve oynatıcıdır. Film/dizi akışları ilk tasarım turunda yer alır. Önce tüm ekranları görsel olarak bitirip odak davranışını sona bırakmak yerine, her ekranın davranışı bileşeniyle birlikte tanımlanır.

Her ekran için giriş noktası, ilk odak, OK sonucu, Back sonucu, yükleme, boş, hata ve erişim kısıtı yazılır. Böylece geliştirmeye teslim yalnızca bir ekran görüntüsünden ibaret kalmaz.

## 12 Açık kararlar ve başlıca riskler

| Karar | Önerilen başlangıç | Ne zaman netleşmeli |
| --- | --- | --- |
| Hedef pazar | Türkçe ve İngilizce aday diller | İçerik metinleri ve mağaza hazırlığından önce |
| Ürün adı | NEXTV çalışma referansı | Logo ve marka sistemi kesinleşmeden |
| Gelir modeli | Ücretsiz/ücretli paket ayrımı henüz yok | Ödeme ekranları tasarlanmadan |
| Kullanıcı hesabı | P0 yerel kullanım; hesap isteğe bağlı sonraki faz | Senkronizasyon ve lisans kararında |
| Telefonla kurulum | QR eşleştirme P1 | Kurulum testleri zorlanma gösterirse öne alınır |
| Minimum cihaz desteği | Gerçek cihaz testleriyle belirleme | Teknik deneme sonunda |
| HTTP kaynakları | Uyumluluk ihtiyacını test et; güvenlik etkisini görünür kıl | Kaynak erişim politikası belirlenirken |
| LG ve Samsung sırası | İki platformda kısa teknik deneme | Google TV çekirdeği doğrulandıktan sonra |

**Kaynak çeşitliliği:** Xtream ve M3U uygulamaları arasında alanlar ve davranışlar değişebilir. Tek bir sağlayıcıyla yapılan test yeterli değildir; farklı örnek veri kümeleri gerekir.

**Büyük kataloglar:** Tam katalog ve rehberi aynı anda belleğe almak TV'yi yavaşlatabilir. Sayfalama, yerel indeks ve arka planda kontrollü yenileme teknik denemede doğrulanmalıdır.

**Yanlış yetenek vaadi:** HDR, catch-up, kayıt veya çoklu ekran düğmesi, gerekli kaynak/cihaz koşulları sağlanmadan aktif gösterilmemeli. Tasarım durumu yetenek modelinden beslenmelidir.

**Kapsam büyümesi:** Kayıt, sunucu, senkronizasyon ve çoklu platformu aynı ilk sürüme almak çekirdek oynatma riskini artırır. Her genişletme önce bağımlılık ve kullanım değerine göre değerlendirilmelidir.

**Sıradaki adım:** Mevcut bileşenleri P0 ekran envanteriyle eşleştirip kaynak ekleme, canlı yayın açma, film izleme ve diziye devam etme akışlarını kumandayla doğrulamak. Animasyon ve renk kararları docs/design/MOTION.md ve docs/design/COLOR.md belgelerinde; açık araştırmalar docs/research/RESEARCH_PLAN.md içinde tutulur.

## 13 Kaynaklar

Erişim tarihi 13 Ekim 2026. Uygulama özellikleri üretici beyanlarıdır; mağaza, ülke, sürüm, cihaz ve paket koşulları değişebilir.

- **R01** [TiviMate resmî Google Play kaydı](https://play.google.com/store/apps/details?id=ar.tvplayer.tv&hl=en) — TV odağı, kaynaklar ve özellik listesi.
- **R02** [Sparkle TV resmî ürün sayfası](https://www.sparkleplayer.com/) — özellikler, kaynaklar ve Plus ayrımı.
- **R03** [Televizo resmî Google Play kaydı](https://play.google.com/store/apps/details?id=com.ottplay.ottplay&hl=en-US) — platformlar ve temel özellikler.
- **R04** [OTT Navigator resmî SSS](https://ottnav.github.io/faq.html) — kaynak kurulumu, platform ve yedekleme.
- **R05** [OTT Navigator değişiklik kayıtları](https://github.com/ottnav/ottnav.github.io/blob/main/changelog.md) — katalog ve gelişmiş ayarlar.
- **R06** [IPTVX özellik rehberi](https://iptvx.app/features/index.html) — kütüphane, rehber, eşleştirme ve koşullu özellikler.
- **R07** [IPTVX SSS](https://iptvx.app/faq/index.html) — senkronizasyon ve satın alma haklarının ayrımı.
- **R08** [NexTv resmî ürün sayfası](https://app.nextv.fr/) — platformlar, kütüphane ve senkronizasyon.
- **R09** [Android TV tasarım merkezi](https://developer.android.com/design/ui/tv) — resmî tasarım kiti ve bileşenler.
- **R10** [Compose for TV](https://developer.android.com/training/tv/playback/compose) — TV için arayüz yaklaşımı.
- **R11** [TV navigation drawer](https://developer.android.com/design/ui/tv/guides/components/navigation-drawer) — çekmece çeşitleri ve seçim davranışı.
- **R12** [TV üzerinde gezinme](https://developer.android.com/design/ui/tv/guides/foundations/navigation-on-tv) — kumanda, odak ve Back davranışı.
- **R13** [TV kartları](https://developer.android.com/design/ui/tv/guides/components/cards) — kart çeşitleri ve en boy oranları.
- **R14** [Media3 desteklenen biçimler](https://developer.android.com/media/media3/exoplayer/supported-formats) — akış, kapsayıcı ve kodek koşulları.
- **R15** [Mevcut NEXTV Figma dosyası](https://www.figma.com/design/KYAb9p9Aqu49vnXKZXXpcx/NEXTV) — kullanıcı tarafından sağlanan tasarım referansı.
- **R16** [UHF resmî ürün sayfası](https://getuhf.com/) — Apple platformları ve özellikler.
- **R17** [SmartOne IPTV resmî ürün sayfası](https://smartone-iptv.com/) — TV ekosistemleri ve özellikler.
- **R18** [Samsung AVPlay kullanımı](https://developer.samsung.com/smarttv/develop/guides/multimedia/media-playback/using-avplay.html) — Samsung oynatma katmanı.
- **R19** [LG webOS akış protokolleri](https://webostv.developer.lge.com/develop/specifications/streaming-protocol-drm) — webOS medya koşulları.
