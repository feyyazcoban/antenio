# Google TV ekran ve akış sözleşmesi

**20 Ekim · TV-052 / D100:** Canlı görüntüde kısa OK bırakıldığında kanal panelini, basılı OK kontrolleri açar; bırakma yeni düğmeye tıklamaz. Panelde Sol kategori, Sağ mini rehber; kategori yalnız OK ile uygulanır. CH±/Page tuşları sayfalar, rakamlar 1,2 sn sonra satıra odaklanır ve yayın açmaz. Alt sütunda Geri kanallara; panelde Geri sağlıklı görüntüye, hata/yeniden bağlanmada kontrollere döner. 15 sn boşta kapanma; Hiçbir zaman, TalkBack, PIN ve ayrıntı penceresi korumaları. Ayrıntılı sözleşme [overlay](gap/overlay.md); S15c bilgi bandı/basılı zap/son kanal açılış arayüzü ayrı kalır.

**20 Ekim · TV-050 / D099:** Kanal numaraları yayınlanan görünür katalogdan hazırlanır; kategori/favori bağlamı zap boyunca korunur. Bekleyen rakam yokken 0 veya LAST_CHANNEL önceki kanalı kendi bağlamıyla ve güncel PIN kapısıyla seçer; 1 ardından 0 kanal10 olur. Guide oynayan kanalı açar. Son kanal yalnız gerçek oynama başlayınca kimlik/zaman olarak saklanır ve geçmiş temizliğinde unutulur. Panelin yeni kısa/basılı OK dili, kategori/mini rehber, bilgi bandı ve otomatik açılış arayüzü S15b–c kapsamındadır.

**19 Ekim · D098 / 0.14.68:** Splash gradient/logo çizimini siyaha yakın ilk karede hazırlar; ardından giriş saati ve ses başlar. İlk kare uzayabilir; toplam açılışın hızlandığı veya fiziksel akıcılığın kabul edildiği anlamına gelmez. Süre, kumandayla atlama, hareketi azalt ve içerik hazırlama davranışları korunur.

**19 Ekim · TV-010 / D097:** Vitrin karartması ekranın en solundan, kapalı menü ikonlarının arkasından başlar ve sağa doğru kesintisiz azalır. Sol kenar ve metin bölgesi biraz daha koyudur; D093'te onaylanan tam genişlik/oran korunur.

**19 Ekim · D095 / 0.14.65:** Home Hero ve raf kartlarında ilk odak kaydırması tam kart/başlık/pay alanını hedefler; geç görünürlük kontrolü aynı hedefe gider. İki farklı hedefin art arda hareket üretmesi giderilir. Kullanıcı19Ekim2026TVkaydırmasını onayladı; bu geri bildirim kapandı.

**19 Ekim · D094 / 0.14.64:** Canlı oynatıcıda Kanallar düğmesi soldan hızlı seçim çekmecesi açar. Mevcut kanal ilk odak; OK seçimden sonra listede kalır, oynayan kanalda OK veya Geri kapatır ve Kanallar'a döner. Aynı player/PIN kapısı; hata/yeniden bağlanmada liste kapanmaz. Ayrıntı DECISIONS D094; genel TV-052/S15 kabulü tamamlanmış değildir.

**18 Ekim · D093 / 0.14.63:** TV-010 vitrini ekran genişliği/yüksekliğine uzanır, oranlı üst hizalı görsel kapalı menü arkasına geçer; ilk raf aşağıdadır. TV-020/030/040 kategori ve menü kayarak açılır/kapanır (240/180 ms, azaltılmış harekette doğrudan). Ortak afiş başlığı 16/22 sp, afiş aralığı 12 dp, yıl gerçek başlığın 4 dp altında daha soluk; kategori adı 13/18 sp ve dil güvenli title case. PIN sayıları/geri sil ortalı, Cancel halkası kesilmez; ayarlar scrollbar gerçek listeyi izler ve sürüklenebilir. Player Seçenekler simgesi ayarlar çarkıdır. [Karar](../decisions/DECISIONS.md).

**18 Ekim · TV-050/051 / D092:** VOD tür etiketi başlığın üstünde, kaynak adı kaldırıldı; geçen süre sola, kalan süre sağa yaslı. Hata/yeniden bağlanma metadata yerine üstte ayrı kalıcı bildirim; retry/Back odağı korunur. [Oynatıcı](PLAYER_PRESENTATION.md).

**18 Ekim · TV-010 / D091:** Son izlenen kanallar kompakt yatay kart: solda logo, sağda kanal adı ve yakın kaynak satırı. 272 dp genişlik, en az 100 dp yükseklik, tek düz yüzey; kısa adda boş ikinci satır yok. D084 yerleşimini değiştirir. [Raf sözleşmesi](HOME_SHELVES.md).

**18 Ekim · D090:** Ortak İzle/Oynat eylemlerinin simgesi her zaman dolu, Material Symbols Rounded play_arrow FILL 1. D083'ün odakta dolu/odak dışında çizgi kuralının yerine geçer. 16 dp kutu, düğme ölçüleri, odak renk/çerçevesi ve favori yıldızının seçili doluluğu korunur.

**18 Ekim · TV-010 / D088:** Vitrin→ilk raf hareketi kart görünür olduğunda yarıda iptal edilmez; görsel ön yüklemesi odak değişiminden ayrıdır. [Raf geçişi](HOME_SHELF_FRAMING.md), [vitrin](HOME_HERO.md).

**18 Ekim · TV-050 / D087:** Oynatıcı simgeleri gerçek düğme merkezine hizalanır; odak etiketleri ortak satırda, zaman çubuğundan ve halkadan ayrı ölçülmüş alanda. [Oynatıcı sözleşmesi](PLAYER_PRESENTATION.md).

**18 Ekim · TV-010 / D086:** Dizi rafında yıl, başlığın gerçek son satırının 4 dp altında; tek satırlık ad için boş ikinci satır ayrılmaz. Büyük afiş/ad boyutları korunur. [Raf sözleşmesi](HOME_SHELVES.md).

**18 Ekim · TV-010 / D085:** “Dizi kütüphanen” rafında 160×240 dp afişler ve iki satırlık 16 sp Medium dizi adları. Ortak odak/görünürlük ve ayrıntıdan dönüş aynı. [Raf sözleşmesi](HOME_SHELVES.md).

**18 Ekim · TV-010 / D084:** Son izlenen kanallarda logo, iki satırlık ad ve kaynak adı 200 dp tek kart içinde; odak halkası bütün kartı sarar. OK/uzun OK, kilit ve odak dönüşü korunur. [Raf sözleşmesi](HOME_SHELVES.md).

**18 Ekim · D083:** Ortak İzle/Oynat eylemlerinin play simgesi odak geldiğinde dolu, ayrıldığında çizgili. Vitrin, ayrıntı, hızlı İzle ve bölüm sonu eylemleri tutarlı; favori gibi seçili durum simgelerinin doluluğu korunur.

**18 Ekim · TV-010 / D082:** Vitrinin YENİ EKLENEN / KÜTÜPHANENDEN üst etiketi 12 sp (önce 13); diğer metinler aynı. D081 iki satır açıklama, D080 420 dp yükseklik ve D079 tek İzle korunur.

**18 Ekim · TV-010 / D081:** Vitrin açıklaması normal ölçekte en fazla iki satır, taşan sonu üç nokta; büyük yazıdaki mevcut tek satır üst sınırı korunur. D080 yükseklik/D079 tek İzle/D078 kaydırma aynı.

**18 Ekim · TV-010 / D080:** Vitrin minimum yüksekliği 420 dp (önce 372); aşağıya 48 dp uzatıldı. Yatay ölçüler/yazı boyutları aynı, ilk raf başlığı ve kart önizlemesi altta kalır. D079 tek İzle ve D078 minimum kaydırma korunur.

**18 Ekim · TV-010 / D079:** Vitrinde yalnız İzle bulunur. İlk odak/menüden dönüş bu düğmeye; OK oynatır/devam eder, Sağ/Sol slaytı değiştirir, ilk slaytta Sol menüyü açar, tek slaytta Sağ odakta kalır. Ayrıntılar düğmesi kaldırıldı; ayrıntılar raf/kütüphane kartlarından açılır. Aşağı geçişte D078 korunur.

**18 Ekim · TV-010 / D078:** Vitrin varsa ilk raf, Aşağı ile odaklandığında yalnız başlık ve kartı görünür kılacak mesafe kadar kayar; ekranın üstüne zorla taşınmaz. Alt raflarda başlık hizalama korunur. [Raf sözleşmesi](HOME_SHELF_FRAMING.md).

**14 Ekim:** TV-010/020/030/031/040 için [görsel revizyon](VISUAL_REVISION.md) hazır. Aday üst gezinme: yatay sekme, aşağı içerik; kartta sağ/sol aynı satırda kalır. Android0.14 tasarım önizlemesinde üst gezinme uygulanmıştır. Aşağıdaki çekmece anlatımı0.13 ve ilk prototipin tarihli davranışıdır; [güncel native sözleşme](NATIVE_VISUAL_REVISION.md) önceliklidir.17 ekranın tamamı yeniden tasarlanmış sayılmaz.

**Kapsam:** Canlı TV + filmler + diziler. **Durum:** Ekran envanteri ve davranış sözleşmesi; F01–F06 yerel akışları gerçek görsellerle incelemeye hazır. Figma ekran çizimleri henüz yok.

Yerel karşılık: [TV prototipi](../../prototypes/tv-flow/README.md). Aşağıdaki 17 ekranın etkileşim karşılığı bulunur; TV-070/071 paneldir. Fiziksel ve ekran klavyesi desteklenir. Seçilmiş katalog başlıkları ve görseller gerçek; medya oynatma, import, EPG ve parça seçenekleri örnektir. Bu kapsam üretim uygulamasının veya tüm hata durumlarının tamamlandığı anlamına gelmez; ayrıntılı kapsam ve eşleme prototip README dosyasındadır.

ID'ler PRD, Figma ve geliştirme görevlerinde ortak kullanılır. İsimler değişse bile ID'ler değişmez. Akış özeti Figma 03 Product & Screens / 01 Flows bölümündedir.

## Ekran envanteri

| ID | Ekran | İlk odak | OK sonucu | Back sonucu |
| --- | --- | --- | --- | --- |
| TV-001 | Karşılama | Kaynak ekle | Yöntem seç | Uygulamadan normal çıkış |
| TV-002 | M3U formu | Liste adı / ilk boş alan | Doğrula → TV-004 | Alanları koruyarak TV-001 |
| TV-003 | Xtream formu | Sunucu / ilk boş alan | Doğrula → TV-004 | Alanları koruyarak TV-001 |
| TV-004 | İçe aktarma | İptal / tamamlanınca Devam | TV-010; kısmi sonuç açıklanır | İptal seçeneği |
| TV-010 | Ana Sayfa | Devam et varsa ilk içerik; yoksa son kanal | İçerik türüne göre detay veya oynatıcı | Gezinme; sonra normal çıkış |
| TV-020 | Canlı TV | Son seçili kanal / ilk geçerli kanal | TV-050 | Önce kategori; sonra gezinme |
| TV-021 | Rehber | Son kanal ve mevcut zaman hücresi | Şimdiki yayın TV-050; diğerleri ayrıntı | Önce program paneli; sonra gezinme |
| TV-030 | Filmler | Son odak / ilk film | TV-031 | Önce filtre; sonra gezinme |
| TV-031 | Film ayrıntısı | İlerleme varsa Devam et; yoksa Oynat | TV-051 | Aynı filtre, kart ve konum |
| TV-040 | Diziler | Son odak / ilk dizi | TV-041 | Önce filtre; sonra gezinme |
| TV-041 | Dizi sezon ve bölümleri | Devam edilecek bölüm / ilk bölüm | TV-051 | Önce sezon paneli; sonra aynı dizi kartı |
| TV-050 | Canlı oynatıcı | Kontroller açıldığında temel eylem | Kontrole göre; paneli aç/kapat | Önce panel, sonra kontroller, sonra geldiği ekran |
| TV-051 | Film ve dizi oynatıcı | Kontroller açıldığında oynat/duraklat | Kontrole göre; çubukta hedefi uygula veya oynat/duraklat | Panel → bekleyen hedefi iptal → kontroller → kaynak detay; ilerleme kaydet |
| TV-052 | Oynatıcı kanal paneli | Oynayan kanal / yoksa ilk satır; boşta Tüm kanallar | Başka kanala geç ve panelde kal; oynayan kanalda kapat; kilitliyse PIN | Mini rehber/kategori → kanallar → görüntü; hata varsa kontroller |
| TV-060 | Arama | Arama alanı / dönüşte son sonuç | Kanal oynar; film/dizi detayı açılır | Klavye → sonuç → gezinme |
| TV-061 | Favoriler | Son seçili tür ve öğe | İçerik türüne göre | Gezinme; tür filtresi korunur |
| TV-070 | Kaynaklar ve ayarlar | Son ayar / ilk kaynak | İlgili ayarı aç | Alt ayar → ayar listesi → gezinme |
| TV-071 | PIN | İlk rakam | Doğrula ve engellenmiş işleme dön | İptal; erişim açılmaz |

## Yön tuşları

**Oynatıcı · D072 / S14b:** Tek alt karartma üzerinde başlık/saat, meta, süre çubuğu ve kontrol satırı bulunur; üst tür hapı ve Kapat düğmesi kalkar. Film/dizide −10 · Oynat/Duraklat · +10, varsa sonraki bölüm, sağda Ses / Altyazı · Seçenekler · Favori. D077 hız menüsü korunur. Canlıda kanal −/+ geçici çipleri oynatma kümesinden ayrıdır; kanal listesi S15'te gelir. Normal canlı çubuğu program ilerlemesidir, sarma komutu değildir.

Satırdan Yukarı aranabilir çubuğa, Aşağı son düğmeye döner; geçersiz hedefte birincil düğme yedektir. Çubukta Sol/Sağ 10 sn, basılı tutmada 30 sn/1 dk hedef değiştirir; video sürer, 1 sn sonra tek seek yapılır. OK ve Aşağı hemen uygular; bekleyen hedefte Geri yalnız iptal eder. Medya oynat/duraklat önce hedefi uygular. Gizliyken sarma hemen uygulanır ve kısa çubuk/balon önizlemesi görünür. Panellerden Geri açan çipe döner. Büyük yazıda etiketler ölçülerek simgeye iner; yatay kaydırma yok. Ayrıntı ve cihaz kabul sınırları [tasarımda](gap/player-controls.md) ve güncel HANDOFF'tadır.

Gezinme çekmecesinde yukarı/aşağı hedefler arasında, sağ içerik bölgesine geçer. İçerikte sol, varsa aynı satırdaki önceki öğeye; ilk öğede kategori/gezinme alanına gider. Dikey geçişte yakın yatay konum korunur. Odak, görünmeyen veya devre dışı hedefe sıçramaz.

EPG'de yatay hareket zaman, dikey hareket kanal eksenidir. Farklı uzunluktaki programlar arasında bir zaman çıpası korunur. Kart odağı yalnız vurgu verir; video akışı başlatmaz.

## Kullanıcı yolları

- F01: TV-001 → TV-002 veya TV-003 → TV-004 → TV-010.
- F02: TV-010 → TV-020 veya TV-021 → TV-050.
- F03: TV-030 → TV-031 → TV-051 → Back ile TV-031 → aynı TV-030 kartı.
- F04: TV-040 → TV-041 → TV-051; ilerleme sezon/bölüm kimliğiyle tutulur.
- F05: TV-060 → türe göre hedef; Back sorgu, filtre ve konumu geri yükler.
- F06: Kısıtlı içerik → TV-071 → doğru PIN ile istenen hedef; iptal aynı kaynak ekrana döner.

## Ortak durumlar

Loading, Empty, Error, PartialData, MissingArtwork ve Locked. Formlarda invalid field ve credential error; oynatıcıda buffering, unsupported format, reconnecting, no audio alternatives, no subtitle tracks; dizide empty season ve final episode durumları eklenir.

Liste başlıkları, ayırıcı gibi görünen kanal adları ve kaynak kategori etiketleri güvenilir tip bilgisi sayılmaz. SOURCE_GUIDELINES içindeki gerçek veri sonuçları uygulanır.

## Ana sayfa satırları

İzlemeye devam et, son kanallar, favoriler; ardından kaynak verisine dayalı film ve dizi kategorileri. Veri yoksa boş satır başlıkları yığılmaz. “Yeni eklenen” sağlayıcının eklenme tarihidir, filmin çıkış tarihi değildir. “Popüler” ölçümü yoksa böyle etiketlenmez.

## Native 0.2 ve devam eden görsel uyum

TV-010 native ana sayfa, devam/son kanal/favori satırları ve doğru bölüme dönüş eklendi. TV-070 geçmiş temizleme onaylıdır, favorileri korur. Native görünüm kullanıcı tarafından prototipten farklı bulundu; hizalama, ölçüler, tipografi ve bileşen geometrisi prototiple karşılaştırılarak düzeltilmelidir. Tam görsel onay verilmedi.

TV-001–004 için [telefonla kurulum önerisi](../product/ONBOARDING.md): QR, kısa kod, süre dolumu/yenileme, telefon formu, TV onayı ve aktarım sonucu; henüz native uygulanmadı. Splash ve özgün açılış sesi istendi; ekran geçişi veya kaynak yenilemesinde tekrar oynatılmamalı. D089: giriş sırasında içerik oluşturulmaz; kaynaklar hazır ve logo hareketi bitmişken içerik opak kapak altında hazırlanır, sonra açılır. İlk yeni yön/OK/Enter/Back/Esc basışı yalnız atlar; tekrar/bırakması tüketilir, sonraki basışlar normaldir. Tek başına launcher tuş bırakması animasyonu kapatmaz. Hareketi azalt ve D069 sabit kapak korunur.

## Ses ve altyazı

TV-050 ve TV-051 aynı panel davranışını paylaşır. Ses ve altyazı ayrı seçim grupları; seçili parça işaretli; subtitle kapalı seçeneği görünür. Dil adı yoksa “Ses 1 / Ses 2” veya “Altyazı 1” kullanılır. DUAL, kategori ülkesi veya dosya adından kesin dil seçeneği üretilmez. Ayrıntılı veri kanıtı SOURCE_GUIDELINES içindedir.

## Teslim kartı

Her ekran tesliminde ID, Figma node, PRD gereksinimleri, durum, ilk odak, yön komşuları, OK, Back, boş/hata davranışı, MOTION ID'leri, COLOR rolleri ve yapılan kontroller yazılır. manifestte nodeId null ise çizim henüz yoktur; bölüm bağlantısı hazır ekran sayılmaz.

## Native 0.3

TV-010/030/031/040/041/050/051/070 geometri, tipografi ve odak [NATIVE_UI](NATIVE_UI.md) ile güncellendi. Şifreli katalog, arka plan yenileme, TV açılışı ve kapatılabilir özgün ses var. Yedi gerçek native ekran alındı; Figma aktarımı ve kullanıcı görsel onayı bekler.

## Native 0.4

TV-060 ortak arama eklendi: Tümü / Canlı TV / Filmler / Diziler, kaynak adı, Türkçe eşleştirme, boş sonuç, IME ile sonuçlara geçiş ve ayrıntıdan aynı sonuca dönüş. Kaynak filtresi çoklu kaynakla birlikte açıktır. Aramadan Back'in sonuç → sol gezinme basamağı henüz ayrı uygulanmadı; klavye kapandıktan sonraki Back ana sayfaya döner.

TV-010/020/030/040 odaklanan kartın başlığını da görünür tutar. TV-030/040 tür ve favori kapsamına göre kaydırma saklanır. Silinen kart ve kategoride güvenli odak kurtarma eklenir. TV-041 ilk bölüm satırı normal yazı ölçeğinde daha erken görünür. Ölçüler [NATIVE_UI](NATIVE_UI.md), gerçek test sonuçları sürüme özel proje QA kaydındadır; genel UX01 kapsamının tamamlandığı anlamına gelmez.

## Native 0.5

TV-060 için klavye → sonuç → Ara gezinme satırı → ana sayfa Back sırası tamamlandı; boş sonuçta olmayan basamak atlanır. Sorgu/tür korunur. Bu, 0.4 notundaki Back eksikliğinin yerine geçer.

TV-070 sağ panel, sabit başlık/kapatma, erişilebilir toggle durumu, kaydırma göstergesi ve ayrı uygulama bilgisi içerir. Silme/geçmiş onayında Geri önce ayar listesine döner; son satır/konum geri yüklenir. Kaynak yenilenirken tekrarlanan OK ek iş başlatmaz. TV-020/030/040'ta yenileme bildirimi viewport'u daralttığında zaten odaklı kartın alt bilgisi de görünür tutulur.

## Native 0.6 bağlantı kurtarması

TV-050/051 aynı panelde bekleme/bağlanma/hata durumlarını gösterir. Otomatik tekrar sırasında ilk odak İptal et, sonrasında aynı düğüm Tekrar dene. Geri bekleyen denemeyi iptal edip önceki ekrana döner; kullanıcı arka plana aldığında istek sürdürülmez. Manuel tekrar yeni bütçe açar, kaynak/medya değişiminde eski deneme iptal edilir. Süre ve hata sınıfları NATIVE_UI/D027 içinde tanımlıdır.

## Native 0.7 bölüm sonu

TV-051 bölüm sonunda TV-041'deki listeye bağlı son ekranı gösterir. Hedef varsa görsel/başlık/sezon-bölüm, varsa sıra belirsizliği ve 10 saniye geri sayım görünür. İlk odak Bu bölümde kal; OK veya Geri zamanlayıcıyı durdurup aynı oynatıcıda bırakır. Şimdi oynat geçerli hedefi baştan açar. Bölümlere dön geldiği ayrıntıya, yeni sezonda yeni bölüm odağına döner. Hedef yoksa geri sayım/Şimdi oynat yok; Baştan izle ve Bölümlere dön bulunur. Kaynağın son bölümünü dizinin kesin sonu gibi sunmaz.

TV-070 İzleme ve uygulama altında Otomatik sonraki bölüm anahtarı bulunur. Kapalıyken son ekran manuel kalır. Arka plana geçiş geri sayımı durdurur; geri dönüş otomatik devam ettirmez. TV-041 tamamlanan bölümden sonra başlıca oynat eylemini geçerli sıradaki bölüme yöneltir; son bölümde yeniden izlemeye izin verir.

## Native 0.8

TV-010/020/030/040/060 sol gezinmesi sistem yazı ölçeğinde etiketleri ölçerek genişler; satırlar büyür ve orta alan kayar. Ayarlar OK/Geri odağı korunur. TV-051 bölüm yıldızı diziyi favorilere ekler; TV-061'de dizi olarak görünür. Eski bölüm favorileri ilişki öğrenildiğinde taşınır.

TV-002/003/004 kaynak indirirken iptal/Geri önceki kaynağı korur. Kaydetme ve kaldırma ayrı yükleme durumlarıdır; kısa disk işlemi bitmeden Geri yeni kaynak formu açmaz. Silme hatası mevcut kullanılabilir ekrana uyarıyla döner. TV-070 kaldırma onayı önceki Vazgeç odağını korur; onaydan sonraki işlem bu kurala tabidir.

## Native 0.9 · TV-070 kaynak yönetimi

Kaynak yönetimi tam sayfa, Ayarlar sağ paneldir. Kaynak ekle başlangıç odağıdır; düzenlemeden ilgili Düzenle’ye, silmeden komşu Kaldır’a dönülür. Kaldırma onayı Vazgeç’le açılır. Seçici480dp/maksimum460dp; mevcut seçime kayar, Back değişiklik yapmadan kapanır. Ana sayfa/katalog/Favoriler seçimi kalıcı; arama kendi kaynak filtresini ve detay dönüşü odağını korur. Çoklu hesapta kaynak adı kart/ayrıntı/player bağlamında görünür. İlk indirme, boş liste ve kaynak hatası ayrı mesaj taşır. Native09 gerçek afiş ve1.5/2.0 ekranları QA’dadır; Figma’ya aktarım yapılmadı. Tüm ekran büyük font erişilebilirlik onayı değildir.

## TV-070 kaynak kurtarma · native 0.10

Kayıt açılamadığında `SourceRecoveryScreen` mevcut Roboto/Ink/Action bileşenlerini kullanır. Yeni kaynak formuna sessiz yönlendirme yoktur. Marka/başlık üstte, açıklama ve eylemler kaydırılabilir alandadır. İlk odak Yeniden dene; kayıtlar doğrulanana kadar kaybolmaz. Yalnız kaynak kaydı okunamıyorsa Kaynakları sıfırla… görünür; genel disk hatası otomatik hesap sıfırlama önermez.

Sıfırlama onayı mevcut SourceDialog ile: 480 dp genişlik, en çok 460 dp yükseklik, kaydırılabilir, ayrı odaklanabilir açıklama paragrafları ve sabit eylem bölümü. Vazgeç üzerindeyken Yukarı ilk açıklamaya gider; Aşağı her paragrafı ve düğmeleri izler. Vazgeç ilk odak, Geri ve OK ile iptal; dönüş sıfırlamayı açan düğmeye. Tüm kaynakları sıfırla eylemi aşağı yöndedir. Metin cihazdaki tüm hesap/listeler/favoriler/geçmişin silineceğini, giriş bilgilerinin gerekeceğini ve sağlayıcı aboneliğinin etkilenmediğini açıklar. Bekleme ekranında yeni işlem açılamaz. Sağlamlaşmış kayıt, eski onaydan korunur.

Bu ek native sözleşmedir; Figma'ya aktarım yapılmış sayılmaz. Kanıt ve font kapsamı native-source-recovery-qa.json dosyasındadır.

## TV-070 kaynak ayrıntısı ve uzun ad · native 0.11

Kaynak seçici 560 dp genişlikte ad ve sıra/tür satırını ayırır; adın başı/sonu görünür, aynı adların sıra eki korunur. Back Home/kütüphane veya Arama'daki açan filtreye döner, seçimi değiştirmez. Yönetimde Bilgiler düğmesi tam adı ve tam hata açıklamasını kumandayla okunabilen metin alanında açar. Başlangıç odağı metin; Kapat altta sabittir. Yukarı/Aşağı metnin sınırına kadar kaydırır, sınırda odak metinden çıkar. Back açan düğmeyi korur.

Kaldırma onayı Vazgeç ile açılır. Açıklama/tam ad kaydırılabilir; Vazgeç ve Kaynağı kaldır ekran içinde sabit kalır. Vazgeç'ten Yukarı metne erişilir. Yönetim satırındaki herhangi bir eyleme odaklanınca ad, durum ve düğmeler birlikte görünür alana alınır; uzun hata özeti Bilgiler üzerinden tam okunur. Native görüntü ve test kapsamı [0.11 QA](../project/native-source-layout-qa.json) içindedir; Figma aktarılmadı.


## Native 0.13 · TV-020/021/050 rehber

Rehber menüsü eklendi. Son kanal/şimdi ilk imleç; yukarı/aşağı kanal ve sabit zaman, sol/sağ program sınırı (boşlukta 30 dakika). Geçmiş/gelecek/eksik/çakışan veri ayrıntı açar; şimdi OK ile tek canlı oynatıcıya gider. Ayrıntı → aynı hücre → Rehber menüsü → Ana Sayfa Geri sırası. Oynatıcıdan dönüş kanal/zamanı korur. Kaynak filtresi ve eksik rehber oynatmaya engel olmaz.

TV-020 kartlarında şimdi/sonra, TV-050 program/saat/ilerleme gösterilir. Parlak görüntü üzerinde başlık koyu yüzeyle okunur. Büyük fontta kontroller yatay kaydırılabilir. TV-070 kaynak bilgisinden rehber durumu/yenileme/iptal ve ayrı adres düzenleme açılır; okuma ekranı klavyeyi çağırmaz. Saat dilimi seçimi anı korur, DST ofsetleri gerektiğinde açık gösterilir.

Roboto/Ink/Action bileşenleri korunur; ızgarada odak mint çizgi, seçim ayrı yüzeydir, satırlar büyütülmez. Font ≥1.5 ise pencere iki saat, kanal sütunu176dp/satır90dp; normalde üç saat/150dp/65dp. Uzun adlar kısaltılır, tam ad ve program açıklaması ayrıntıda kumandayla okunur; eylemler sabit kalır. [0.13 QA](../project/native-guide-qa.json) test ölçeklerini ve sınırları kaydeder. Ekran görüntülerindeki logolar gerçek, programlar sentetiktir. Figma çizimleri bu tur güncellenmedi.


## TV-051 · Oynatma hızı · 18 Ekim 2026 (D077)

Film ve dizide Seçenekler → Oynatma hızı: 0,5×, 0,75×, 1× (Normal), 1,25×, 1,5×, 1,75×, 2×. İlk odak geçerli hızdadır; beyaz odak halkası ve seçili işareti ayrıdır. Yukarı/Aşağı gezinir; OK uygular ve Seçenekler'deki hız satırına döner. Geri uygulamadan bir düzey çıkar; Seçenekler'den Geri kontrollerdeki Seçenekler düğmesine döner. Sonraki Geri kontrolleri gizler, sonraki oynatıcıdan çıkar. Pencere açılınca OSD kendiliğinden kapanmaz. Değerler RadioRow erişilebilirliğini, saydamlık ise oynatıcı panel tercihini kullanır. Uzun liste büyük yazıda dikey kayar; odaklı seçenek görünür kalır.

Ses perdesi korunur; değer yalnız mevcut oynatıcı oturumunda tutulur. Canlı TV'ye taşınmaz. Hata/toparlanma sırasında Seçenekler kapalıdır. Tünelli oynatma veya motorun komut desteği yoksa hız satırı kapalı, nedeni görünür, ilk odak Geri'dedir. Motordan farklı bir gerçek hız gelirse değer ona göre güncellenir ve uyumsuz ses çıkışı açıklanır. Oynarken bilinen süre üzerinden gösterilen bitiş saati gerçek hızla hesaplanır; duraklatmada/eksik sürede tahmin yoktur. Bu ek, D072/S14b kontrol yenilemesinin yerine geçmez.
