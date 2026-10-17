# UI/UX denetimi · 0.14.40

17 Ekim 2026 · Kod `ac94c85` (0.14.40, kod 54). Bu belge bir **öneri listesidir**; hiçbir madde kullanıcı onayı almış sayılmaz. Bazı öneriler kullanıcı kararlarına (D056, D059, D060, D063) dokunur; bunlar ayrıca işaretlenmiştir.

## Uygulama durumu · 17 Ekim 2026

Kullanıcının devam isteğiyle ve iki ek isteğiyle (kütüphane bandı ve afiş/yazı boyu; oynatıcıda alttan gradient) şu maddeler uygulandı, karar [D064](../decisions/DECISIONS.md): **U1, U2, U3, U4, U5, U7, U10, U12, U16, U17, U20, U21, U23, U24, U26** ve U8'in ayrıntı kısmı. U5, istek üzerine bilgi panelinin tamamen kaldırılması ve 4/5 sütun büyük afişle çözüldü. Gerçek "Evim" kaynağıyla 5556'da görülenler: Geri katmanları, menüden yeniden girişte odak, büyük afişler, gradient oynatıcı, CH−/Yukarı/rakamla kanal ve köşe notu, Sağ +10 sn ve İleri sarma +30 sn, kısa izlemenin devam rafına girmemesi, "8x8" düzeltmesi ve "Bölüm N" kartları.

İkinci tur (D065, D066): **U6, U8 (basılı OK menüsü), U9, U11 (NEXTV adı ve logo), U13, U14, U18, U22, U25 (adlar; kalite çeşitlerini birleştirme hariç)**. U15, 4/5 sütunla afiş boyu neredeyse sabit kaldığı için kapandı sayıldı. Gerçek Evim ile 5556'da görülenler: menüde NEXTV logosu, sadeleşmiş kanal/film adları ve "YERLI 2025 · TR" kategorileri, kanal satırında kategori, "1 sa 18 dk kaldı", basılı OK menüsü, Uygulama hakkında ve kurulumda logo, "Bilgileri göster".

Açık kalanlar: U19 (ana sayfa rafları, kullanıcı kararı), kalite çeşitlerini tek kanalda toplama (ürün kararı), rehbersiz kaynakta rehber boş durumu cihazda görülmedi (5556'daki sentetik Demo kaynağının rehberi var). TalkBack, büyük yazı ve fiziksel TV denenmedi.

## Yöntem ve sınırlar

- `ui/` altındaki bütün ekranlar (Home, Library, Detail, Search, Player, Guide, Settings, Sources, Setup, Loading, Recovery, EpisodeEnd) satır satır okundu; Back/odak yolları `TvViewModel` ile birlikte izlendi.
- Depodaki kayıtlı ekran görüntüleri (0.14.28–0.14.31) karşılaştırma için kullanıldı.
- Ink paletinin kontrastı WCAG bağıl parlaklık formülüyle hesaplandı.
- **Gerçek veriyle doğrulama:** Tasarım emülatörüne (`emulator-5556`, `nextv_design_api36`, 1920×1080, 320 dpi, yazı 1,0) imzası doğrulanmış 0.14.40 release APK'sı `install -r` ile, veri korunarak kuruldu. Kayıtlı kaynaklar: kullanıcının gerçek sağlayıcısının M3U listesi "Evim" (4.531 kanal, 14.626 film, 2.172 dizi; rehber adresi yok) ve sentetik "Demo" (yalnız vitrin slaytları ondan geliyor). Gerçek kaynaktan sırayla tek bir canlı yayın ve tek bir film açıldı. Fiziksel TV yalnız okunarak kontrol edildi (uykudaydı, yayın yoktu); ona dokunulmadı.
- Ekran görüntüleri gerçek sağlayıcı verisi içerdiği için depoya konmadı. TalkBack, fiziksel TV, 4K ve büyük yazı bu turda denenmedi; okunabilirlik yorumları izleme mesafesinde ölçülmüş değildir.

Önem: **Yüksek** = günlük kullanımı belirgin yavaşlatır ya da yanıltır; **Orta** = fark edilir sürtünme; **Düşük** = cila. "✓ Gerçek veride görüldü" işareti, bulgunun emülatörde gerçek kaynakla tekrar edildiğini gösterir.

## Güçlü yanlar (korunmalı)

- Odak geri yükleme çoğu yerde özenli: ayrıntıdan dönüşte aynı kart (Diziler'de gerçek veriyle görüldü), sezon raflarının hatırlanan bölümü.
- Türkçe arama gerçek veride çalışıyor: "asirlik" yazınca "Asırlık Gece (2026)" bulundu.
- Oynatıcıda Geri önce kontrolleri gizliyor, ikinci Geri çıkıyor; tur boyunca çökme kaydı yok.
- Boş, hata ve yükleme durumları Türkçe, açıklamalı ve her birinde bir sonraki eylem var.
- Metin kontrastı yüksek: Text/Background 15,8:1, Muted/Background 9,6:1, Mint/Container 9,5:1.

## Yüksek öncelik

### U1 · Canlı yayında kumanda kısayolları yok · ✓ Gerçek veride görüldü
- **Kanıt:** [PlayerScreen.kt](../../apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/PlayerScreen.kt) 220–236: kontroller gizliyken yön tuşları ve OK yalnız kontrolleri açar. Kaynak kodunda CH+/CH−, rakam, Guide, Info ya da son kanal tuşu için işleyici yok. Gerçek yayında CH+, ardından "2", "5" ve Yukarı gönderildi: kanal değişmedi, Yukarı yalnız kontrolleri açtı.
- **Etki:** Kanal değiştirmek için kontrolleri açıp "‹ Kanal / Kanal ›" düğmesine gitmek gerekiyor. 4.531 kanallık listede numaralar gösteriliyor ama girilemiyor.
- **Öneri:** Kontroller gizliyken Yukarı/Aşağı ile kanal ±1, CH+/CH− her durumda. Rakamlarla 1,5 sn biriken numara girişi ve köşede numara rozeti. Geçişte 3–4 sn'lik bilgi bandı (logo, numara, şimdi/sonra). OK ile kanal listesi ya da mini rehber katmanı. Kanal sırası bugünkü gibi gelinen kategoriyi izlemeli (`changeChannel`).

### U2 · Film ve dizide sarma çok yavaş · ✓ Gerçek veride görüldü
- **Kanıt:** PlayerScreen.kt 311–312: yalnız "−10 sn / +10 sn" düğmeleri var. Gerçek 1:20:36'lık filmde, kontroller gizliyken Sağ yalnız kontrolleri açtı; iki kez İleri sarma tuşu konumu yalnız geçen süre kadar ilerletti (0:15 → 0:17).
- **Etki:** 40. dakikaya gitmek yüzlerce basış ister.
- **Öneri:** Gizliyken Sol/Sağ ile −10/+30 sn, basılı tutunca hızlanan adım. FF/RW tuşları. Odaklanabilir zaman çubuğu ve hedef zaman etiketi; OK ile atlama.

### U3 · Odak dili tutarsız, beyaz iki anlam taşıyor · ✓ Gerçek veride görüldü
- **Kanıt:** Dinlenen birincil düğme beyaz dolgu ([Theme.kt](../../apps/android-tv/app/src/main/java/dev/iptvplayer/tv/ui/Theme.kt) 103). Odaklı menü ve kategori öğesi de beyaz dolgu (HomeScreen.kt 176, LibraryScreen.kt 371). Odaklı düğme ise mint (Theme.kt 104). Kartta beyaz halka, rehber hücresinde mint çerçeve (GuideScreen.kt 235–236).
- **Gerçek veride:** Film ayrıntısında beyaz "Devam et" ile odaklı mint "Baştan başlat" yan yana; oynatıcıda beyaz "Duraklat" ile odaklı "‹ Kanal"; dizi ayrıntısında odak bölüm kartındayken beyaz "Oynat" odaklı gibi görünüyor.
- **Öneri:** Tek kural: **odak = açık dolgu + koyu yazı + 2 dp halka**, her yerde. Dinlenen birincil düğme beyaz olmamalı; mint ikonlu koyu yüzey ya da mint ton kullanılabilir. Kart ve rehber hücresi aynı halkayı paylaşmalı. D056'nın "büyüme yok, renk ve çerçeve" kuralıyla uyumludur.

### U4 · Kütüphanede tek Geri ana sayfaya atıyor · ✓ Gerçek veride görüldü
- **Kanıt:** LibraryScreen'de BackHandler yok. Geri, PilotApp.kt 82–83'ten `model.back()`'e gidiyor; [TvViewModel.kt](../../apps/android-tv/app/src/main/java/dev/iptvplayer/tv/TvViewModel.kt) 690'da LIBRARY → `home()`. Gerçek veride Canlı TV'de 26. kanaldayken tek Geri doğrudan Ana Sayfa'ya götürdü; Filmler'de de aynı.
- **Etki:** SCREENS TV-020/030/040 "önce kategori/filtre, sonra gezinme" diyor. Ana Sayfa (içerik → menü, D060), Ara (sonuç → menü → ana sayfa) ve Rehber (ızgara → menü) katmanlı; kütüphane değil. Yanlışlıkla basılan Geri bütün konumu kaybettiriyor.
- **Öneri:** Izgara/satır → seçili kategori → menüde bölüm → Ana Sayfa. Bölüm araması açıksa önce o kapanmalı.

### U23 · Bölüme yeniden girişte liste ve odak uyuşmuyor · Yalnız gerçek veride görüldü
- **Kanıt:** Canlı TV'den çıkıp menüden yeniden girince liste eski kaydırma konumunda (21.–26. kanal) açıldı, bilgi paneli ise 1. kanalı gösterdi. Kategori sütunundan Sağ ok kanala değil, başlıktaki **Rehber** düğmesine gitti. Filmler'de aynı yol **Sırala** düğmesine gitti; OK sıralama penceresini açtı.
- **Neden (koddan):** `rememberLazyGridState` kaydedilen durumdan eski kaydırmayı geri yüklüyor (PilotApp.kt 120–121, `SaveableStateProvider`), `showKind` ise `focusedId`'yi sıfırlıyor (TvViewModel.kt 450–453). Hatırlanan ilk kart ekranda olmadığı için `focusContent()` başarısız oluyor (LibraryScreen.kt 174–175, 207), Sağ olayı varsayılan odak aramasına düşüyor. Bilgi paneli `focusedEntry ?: filtered.first()` gösteriyor (211–212). 15 kayıtlık fikstürde liste kaymadığı için testlerde görünmüyor.
- **Öneri:** Menüden girişte ya kaydırmayı başa alın ya da odak belleğini kaydırmayla birlikte saklayın. `focusContent()` başarısız olunca ilk görünen karta düşün; bilgi panelini de odaktaki ya da ilk görünen kayda bağlayın. 5.000 kayıtlık bir enstrümantasyon fikstürüyle test edin.

### U5 · Kütüphane ızgarasında görünen içerik az, üstte afişsiz başlıklar kalıyor · ✓ Gerçek veride görüldü
- **Kanıt:** Bilgi paneli 176 dp (LibraryScreen.kt 477), üstüne başlık satırı geliyor. 14.626 filmlik listede 7 sütun kipinde tek tam afiş satırı görünüyor; üst kenarda önceki satırın yalnız başlıkları ve 7 kez "Evim" duruyor. M3U'da açıklama olmadığından panelde başlık ve kategori dışında büyük boş bir alan kalıyor. Izgarada D053'teki kenar solması yok.
- **Öneri:** Paneli yaklaşık 120 dp'ye indirin, açıklama yoksa daha da küçültün. Izgaranın üst kenarına `scrollEdgeMask` ekleyin. Kaydırmayı tam afiş satırına yuvarlayın.

## Orta öncelik

### U24 · Kısa bir oynatma "İzlemeye devam et"e giriyor, film rafını siliyor · Gerçek veride görüldü
- **Kanıt:** `PlaybackMark.resumeMs` her konumu devam noktası sayıyor (Models.kt 128–130); alt eşik yok. Gerçek bir film 19 sn açıldı: Ana Sayfa'da "İzlemeye devam et · 0:19" kartı belirdi ve D063 kuralıyla "Film kütüphanen" rafı kayboldu. Ayrıntı ekranı "Devam et" ve "Baştan başlat" göstermeye başladı.
- **Öneri:** Yaklaşık 2 dakikanın (ya da sürenin %3'ünün) altındaki oynatmayı devam kaydı saymayın. Rafın gidip gelmesi kullanıcıya rastgele görünüyor; D063'teki raf değişimi bu eşikten sonra tetiklenmeli.

### U25 · Sağlayıcı adları temizlenmeden gösteriliyor · Gerçek veride görüldü
- **Kanıt:** Kanallarda "TR: " öneki ve "SD / HD / HD+ / (FEED) / 4K (Minimum 150mbps)" ekleri; filmlerde "(2024) DE", "- 2026", "(2026) TR". Kategoriler büyük harf ve önekli: "TR/FILM ~ YERLI 2026", "DE/SERIE ~ NETFLIX". 196 dp sütunda kesiliyor ("DE/FILM ~ DOKUMEN…", "TR/DIZI ~ COK GUZEL …"). Aynı kanalın 4–5 kalite çeşidi art arda diziliyor; ana sayfadaki başlangıç rafı bunlarla doluyor. Yıl başlıkta olduğu hâlde yıl satırı boş kalıyor ve kart altında boşluk açıyor.
- **Öneri:** Veriyi değiştirmeden **görüntü katmanında** sadeleştirin: ülke/dil önekini küçük rozete, yılı yıl satırına, kalite ekini rozete taşıyın. Kategori öneklerini ("TR/FILM ~") sütunda grup başlığı yapın. Aynı kanalın kalite çeşitlerini tek kanal altında toplayıp oynatırken kalite seçtirin (bu bir ürün kararıdır).

### U26 · Başlıktaki "8x8" sezon/bölüm sanılıyor · Gerçek veride görüldü
- **Kanıt:** `M3uParser.episodeTag` (CatalogParser.kt 105) `(\d{1,3})x(\d{1,4})` kalıbını kelime sınırı olmadan arıyor. "8x8 (2023)" dizisi ayrıntıda "1 sezon" yazarken rafta "Sezon 8", kartta "E8. 8x8 (2023)" olarak göründü. PRD'ye göre başlıktan tahmin edilen sezon/bölüm kesin veri gibi sunulmamalı.
- **Öneri:** Kalıbı kelime sınırına bağlayın; dizi adının kendisiyle eşleşen ya da tek kayıtlık sonuçları kesin saymayın; çıkarım olduğunu UI'da belirtin.

### U6 · Yazılar izleme mesafesi için küçük
- **Kanıt:** UI kodunda 44 yerde 11 sp ve altı var (24 × 10 sp, 3 × 8–9 sp). Örnekler: kart yılı ve kaynak adı 10 sp (PilotApp.kt 287–295), "YENİ EKLENEN" 10 sp (HomeScreen.kt 402), rehberde kaynak adı ve saat ofseti 8 sp (GuideScreen.kt 216, 164), program saati 9 sp (247), oynatıcıda "Sonra" 10 sp (PlayerScreen.kt 286). tv-material3 tip ölçeğinin en küçük kademesi Label Small 11 sp'dir; 960×540 dp tuvalde 10 sp, 1080p'de yaklaşık 20 px eder.
- **Öneri:** Yardımcı metinde alt sınır 12 sp, gövdede 14 sp. Elle yazılmış `fontSize` değerlerini tek bir tip ölçeği nesnesinde toplayın. Fiziksel TV'de 3 m'den kontrol edin (BKL-014).

### U7 · Vitrin slider'ı odak üzerindeyken de kayıyor · ✓ Gerçek veride görüldü
- **Kanıt:** HomeScreen.kt 361–369: zamanlayıcı yalnız "hareketi azalt" ve sistem animasyonlarına bakıyor. Odak "Ayrıntılar"dayken slaytlar yaklaşık 8 sn'de bir değişti (ölçüm: 4 → 5 → 6 → 1). İkinci ve sonraki slaytlarda Sol, menüyü açmak yerine önceki slayta gidiyor (420–421); denemede menüye gitmek için basılan Sol slaytı değiştirdi ve sonraki Aşağı + OK beklenmedik bir kanalı açtı.
- **Öneri:** "Ayrıntılar" odaktayken zamanlayıcıyı durdurun ya da belirgin uzatın. TalkBack açıkken otomatik geçişi kapatın. Geçişten sonraki yaklaşık 500 ms'de gelen OK'yi önceki slayda bağlayın. Slaytı Sol/Sağ yerine yalnız Sağ ile ilerletmek ya da Sol'u her zaman menüye ayırmak düşünülebilir. *(D063 "tek eylem Ayrıntılar" kararını değiştirmez.)*

### U8 · Favoriye eklemek zahmetli, durum etiketi belirsiz
- **Kanıt:** Kartlarda bağlam menüsü yok. Bir kanalı favorilere eklemek için oynatmak, kontrolleri açmak ve yıldıza kadar dört kez Sağ'a basmak gerekiyor. Ayrıntıda düğme "Favorilerde" diyor (durum, eylem değil) ve yıldız dolmuyor (DetailScreen.kt 145; oynatıcıda `iconFilled` kullanılıyor, burada kullanılmıyor).
- **Öneri:** Kartta uzun OK ya da Menü tuşu ile küçük bağlam menüsü: Favorilere ekle/çıkar, Rehberde göster, Ayrıntılar. Ayrıntıda dolu yıldız ve "Favorilerden çıkar" etiketi.

### U9 · Rehber seyrek, zor bulunuyor, rehbersiz kaynakta boş ızgara gösteriyor · ✓ Gerçek veride görüldü
- **Kanıt:** Normal yazıda satır 65 dp (GuideScreen.kt 107); aynı anda 4 kanal görünüyor. Başlık, 5 düğmelik satır, saat başlığı ve alt bilgi paneli ekranın yarısını alıyor. Rehber sol menüde yok; yalnız Canlı TV başlığındaki düğmeden açılıyor. Gerçek kaynakta rehber adresi olmadığı için 4.531 satırın her biri "Rehber adresi eklenmemiş" diyor.
- **Öneri:** Rehber adresi yoksa ızgara yerine tek bir boş durum ve "Rehber adresi ekle" eylemi gösterin. Satırı 48–52 dp'ye indirip 6–7 kanal gösterin. "Saat dilimi" ve "Kaynaklar"ı Ayarlar'a taşıyın. Guide tuşunu ve oynatıcıdaki OK'yi rehbere bağlayın; kanal numarasını gösterin.

### U10 · Oynatıcı paneli videonun yarısını kapatıyor · ✓ Gerçek veride görüldü
- **Kanıt:** Gerçek yayında panel 1080 px'in yaklaşık 595–1027 aralığında. Başlık 28 sp. Altta "Geri: kontrolleri gizle · Yeniden Geri: kütüphaneye dön" ipucu var; oynatıcı Ana Sayfa'dan açıldığında ikinci Geri kütüphaneye değil Ana Sayfa'ya döndüğü için metin yanlış yönlendiriyor (PlayerScreen.kt 331–333).
- **Öneri:** Alt bandı ekranın yaklaşık %30'una indirin, başlığı 22 sp tek satır yapın. Geri standart davrandığı için ipucu satırını kaldırın ya da "geldiğin ekrana dön" deyin. Köşeye bir duvar saati ekleyin.

### U11 · Marka adı ekrandan ekrana değişiyor *(D007 açık, kullanıcı kararı)* · ✓ Gerçek veride görüldü
- **Kanıt:** Splash, ikon ve banner NEXTV (D062). Uygulama içinde "› IPTV" (HomeScreen.kt 143–146), "IPTV PLAYER" (PilotApp.kt 173, LoadingScreen, SourceRecoveryScreen), "IPTV Player · Pilot" (SettingsScreen.kt 170). Menünün başındaki mint "›" logo gibi değil, "aç" ipucu gibi okunuyor.
- **Öneri:** Ad kesinleşene kadar tek bir çalışma adı kullanın. Menü başında amblemi gösterin.

### U12 · "Hareketi azalt" açıklaması eskimiş · ✓ Gerçek veride görüldü
- **Kanıt:** SettingsScreen.kt 133: "Kartların odak büyümesini kapatır." Odak büyümesi D056'da tamamen kaldırıldı; ayar artık vitrin geçişini, splash'i, menü ve kaydırma animasyonlarını etkiliyor.
- **Öneri:** "Vitrin geçişlerini, açılış animasyonunu ve menü kaymasını kapatır."

### U13 · Kurulumda adres görülmeden yazılıyor
- **Kanıt:** PilotApp.kt 209 ve 217: M3U bağlantısı ve XMLTV adresi gizli alan; şifrede de göster/gizle yok. *(Bu turda form denenmedi.)*
- **Etki:** Kumandayla uzun bir URL'yi görmeden yazmak hataya açık; hata mesajı geldiğinde kullanıcı neyi yanlış yazdığını göremiyor.
- **Öneri:** Varsayılanı gizli tutan geçici bir "Göster" düğmesi; ya da adreste yalnız kimlik kısmını maskeleyip sunucuyu gösterin. En büyük kazanç telefonla kurulum (BKL-016).

## Düşük öncelik

- **U14 · "Program bilgisi yok" her satırda tekrar ediyor.** ✓ Gerçek veride 4.531 kanalın hepsinde aynı soluk metin var ve sağdaki sütun tamamen bununla dolu (LibraryScreen.kt 431, PilotApp.kt 292). Bilgi paneli de odaktaki satırı aynen tekrarlıyor. Kaynakta rehber yoksa sütunu ve paneli küçültün, başlıkta bir kez "Bu kaynakta rehber yok · Ekle" deyin.
- **U15 · Izgaraya girerken 5 → 7 sütun yeniden dizilim.** ✓ Gerçek veride görüldü (LibraryScreen.kt 145–146): odaklı afiş yer ve boy değiştiriyor. D056'nın TiviMate tercihi; kumanda testinde (BKL-012) gözlenmeli.
- **U16 · Bölüm adında tekrar ve boş kart alanı.** ✓ Gerçek veride her bölüm "E1. Asırlık Gece (2026) S01 E01" biçiminde; asıl bilgi zaten "E1.". Süre olmadığı için kartların alt yarısı boş. Görüntüde dizi adını, yılı ve SxxEyy önekini temizleyin; veri yoksa boş satır ayırmayın.
- **U17 · Ayrıntıda ekranda Geri düğmesi, tür etiketi tekrarı ve yer tutucu metin.** ✓ Geri düğmesi kumandanın Geri'sini tekrarlıyor; "FİLM KÜTÜPHANENDEN" ile sağ üstteki "FİLM" aynı bilgi (DetailScreen.kt 125, 163–168). M3U'da neredeyse her başlıkta "Bu içerik için açıklama sağlanmamış." yazıyor (133); veri yoksa satırı gizleyin.
- **U18 · Devam bilgisi konum gösteriyor, kalan süreyi değil.** ✓ Kart "0:19" diyor (HomeScreen.kt 496), ayrıntı yalnız "Devam et". "1 sa 20 dk kaldı" ve "Devam et · 0:19" daha işe yarar.
- **U19 · Ana sayfada keşif sınırlı.** ✓ Raflar devam / son kanal / favori / canlı / film / dizi. Gerçek kaynakta başlangıç film rafı sağlayıcı sırasıyla Almanca ("DE") başlıklarla açılıyor. Geçmiş oluşunca "Film kütüphanen" kalkıyor ama "Dizi kütüphanen" kalıyor (HomeScreen.kt 203–205, D063). Cihaz dilindeki kategorileri öne almak, "Yeni eklenen filmler/diziler" ve en dolu 2–3 kategori rafı düşünülebilir *(kullanıcı kararı)*.
- **U20 · Metin dışı kontrast.** Dinlenen çerçeveli düğmenin kenarı Divider/Background 2,24:1 (Theme.kt 105), metin dışı öğe için 3:1'in altında. Odaklı kartta dolgu değişimi Container → High 1,30:1, yani odağı yalnız 2 dp halka taşıyor; beyaz afişte halka kaybolabilir. Kenarda Outline'ı kısık kullanın (tam opaklıkta 7,8:1), halkayı 3 dp yapın ya da koyu dış kontur ekleyin.
- **U21 · Menü açıkken içerik karartılmıyor.** ✓ İçerik sağa kayıp tam parlaklıkta ve kesik duruyor. Çizim katmanında hafif karartma MOT04'ü bozmaz.
- **U22 · Oynatıcıda yükleme yalnız metin.** "Yayın açılıyor…" (PlayerScreen.kt 245–249) için küçük bir gösterge ekleyin. *(Gerçek yayınlar hızlı açıldığı için bu turda görülmedi.)*

## Önerilen sıra

1. **Hızlı düzeltmeler (küçük, ayrı commit'ler):** U12, U10'daki ipucu metni, U17, U8'in ayrıntı kısmı, U20, U21, U18, U5'in kenar maskesi, U14.
2. **Bir sonraki sürüm:** U23 yeniden giriş odağı, U4 Geri katmanları, U24 devam eşiği, U26 sezon çıkarımı, U3 odak dili, U1 canlı kısayollar, U2 sarma, U7 slider.
3. **Tasarım turu gerektirenler:** U25 ad sadeleştirme ve kalite çeşitleri, U9 rehber, U10 oynatıcı paneli, U6 tip ölçeği, U19 keşif, U13 + BKL-016, U11 (kullanıcı kararı).

## Doğrulama önerisi

Testlerdeki 15 kayıtlık fikstür U23 ve U25 gibi büyük liste sorunlarını göstermiyor; binlerce kayıtlı sentetik bir fikstür eklenmeli. BKL-012 kumanda testine şu görevler eklenebilir: "kanal 47'ye geç", "filmde 40. dakikaya git", "bu kanalı favorilere ekle", "Filmler'de 3. satırdayken Geri'ye bas, sonra aynı yere dön", "vitrindeki filmin ayrıntısını aç". Her görev için basış sayısı ve hata sayılır. U6 ve U20 fiziksel TV'de, gündüz ve loş ışıkta 3 m'den kontrol edilir (BKL-014).
