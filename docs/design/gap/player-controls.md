# Oynatıcı kontrolleri: önerilen tasarım (S14b)

18 Ekim 2026 · tasarım iş akışı (üç yön, üç yargıç, birleşim). Kullanıcı önerilen tasarımı ve varsayılan kararları aynı gün "Evet" ile onayladı (D072). Maketler: [proposal.html](../../../design/reviews/player-controls-2026-10-18/proposal.html), [current.html](../../../design/reviews/player-controls-2026-10-18/current.html).

# S14b · Oynatıcı kontrolleri ve sarma çubuğu (Faz D)

**D072 ile onaylandı.** Aşağıdaki ölçüler 960 × 540 dp HTML taslağından gelir; native uygulama ve test sonuçlarının güncel kaydı STATUS/HANDOFF'tadır. Fiziksel TV ve Figma kabulü ayrıca izlenir. Maketler bu belgenin başında bağlıdır.

**Taban:** Dengeli'nin iskeleti. Başlık sol altta, tek alt scrim; player-panel / player-title / player-controls geometri testleri geçerli kalır.
**Eklenenler:**
- Sinematik'ten: oynatma kümesi, dolgulu çipler, sarma çubuğu ve pencereli canlı görseli.
- Bilgi bantlı'dan: Canlıya dön eşiği, PlaybackClock, tek bileşenli bilgi bandı ve Material Symbols.

**Karar:** D072, D064'ün oynatıcı maddesinin (24 sp başlık, 14 sp durum, sağda ipucu, üst etiket) yerine geçer. D077 oturumluk hız seçimi yeni Seçenekler çipinde korunur.

## 1. Yerleşim (1,0 yazı)

**Scrim**
- Yalnız alttadır. Üst karartma ve üst sol tür hapı kalkar.
- Alt %60'lık kutuda `verticalGradient(0f to Transparent, .367f to Black(.62f), .667f to Black(.86f), 1f to Black(.94f))` kullanılır. Bunlar Dengeli'nin tam ekran durakları (.40 / .62 / .80 / 1) ve parlak karede ölçülmüştür.
- ≥ 1,3 yazıda kutu %70 olur, duraklar .343 / .657.
- Bulanıklık yok.

**Blok** (`player-panel`)
- Konum: sol 24, sağ 24, alt 24; genişlik 912. Halka payı yok: halka denetimin 5 dp dışına taşar (x 19), ekranda kırpılmaz.
- Satırlar yukarıdan aşağı:

| Satır | Yükseklik | Üst aralık | y (1,0) |
|---|---|---|---|
| Başlık satırı | 34 | — | 330–364 |
| Meta satırı | 20 | 4 | 368–388 |
| Çubuk satırı | max(28, 26 sp) | 12 | 400–428 |
| Odak etiketi bölgesi | 24 × yazı ölçeği | — | 428–452 |
| Kontrol satırı | 64 | — | 452–516 |

Blok film ve canlıda aynı yükseklikte, 210 dp. Bugün filmde ≈ 145 dp ve üstte ayrı bir hap var, canlıda ≈ 196 dp.

**Başlık satırı**
- Film / dizi: başlık 28/34 sp Medium Text. 1,0'da tek satır, ≥ 1,3'te en çok 2 satır, sonda "…".
- Sağda saat: 20/26 sp Regular Muted, tabular. Başlıkla en az 24 dp boşluk bırakır. Saat yalnız kontroller ve bilgi bandı açıkken görünür; kontroller kapalıyken o köşe köşe notuna aittir.
- Dizi: başlıkta dizi adı yazar (bölümün dizi kaydından). Bulunamazsa `displayTitle(entry.title).name` tek başına başlıkta kalır.
- Canlı: soldan sağa
  1. Numara: 24 sp Regular Muted, tabular, en az 28 dp, sağa yaslı.
  2. 12 dp aralıkla logo kutusu: 56 × 34, köşe 6, Container. `Artwork` logo kipi kullanılır; logo yoksa harf yedeği 13 sp Bold Muted.
  3. Kanal adı: 28 sp.
  4. CANLI rozeti.
  5. Saat.

**Meta satırı** (15/20 sp Muted)
- Film: `[FİLM] 2023 · 1 sa 48 dk · Bitiş 22:20`.
- Dizi: `[DİZİ] S2 B5 · Yol Ayrımı · 52 dk · Bitiş 21:28`. Bölüm kısmı Text renginde. S/B yalnız `episodeOrderKnown` ise yazılır; bölüm adı ayrışmazsa yazılmaz.
- Canlı, pencere yok: "Akşam Bülteni" (16 sp Medium Text) ve "· 18 dk kaldı". Sağa yaslı "Sonra · 20:00 Gündem Özel" (14 sp).
- Canlı, pencereli: "Akşam Bülteni · 19:00–20:00" ve sağda "Sonra". Meta satırı her zaman çubuğun göstermediği zaman bilgisini taşır.
- Birden çok kaynak varsa kaynak adı en sona gelir: Muted, en çok 200 dp, "…". ≥ 1,3'te gizlenir.
- Etiket: 12/16 sp Medium Text, harf aralığı ,1 em, 1 dp Outline %60 kenar, köşe 4, iç boşluk 1/7. Metni mevcut `player_kicker_movie/series`.
- "Bitiş" = şimdi + kalan süre, dakikaya yuvarlanır. Yalnız süre biliniyorsa yazılır.
- Durum metinleri satırın yerine geçer (etiketten sonra):
  - LoadingArc 18 dp ve "Yayın açılıyor…"
  - Yeniden bağlanma mesajı
  - Hata: Error renginde, en çok 2 satır
  - "Bitti"
  - `player-current-programme` test etiketi canlı program kısmında kalır.

**Çubuk satırı:** `[geçen] 16 [iz] 16 [bitiş]`
- Geçen: 20/26 sp Medium Text, tabular.
- Bitiş: kalan süre "−1:05:44", 20/26 Regular Muted, sağa yaslı.
- Etiketlerin en küçük genişliği o biçimin en geniş metninden ölçülür ("0:00:00", ≥ 1 sa için; aksi hâlde "00:00"). Böylece iz hiç zıplamaz. İz 1,0'da ≈ 718 dp.
- Canlı, pencere yok: etiketler "19:00" / "20:00" (Muted). İz program ilerlemesidir, odak almaz, tutamaç yoktur. Rehber yoksa satırın yeri ayrılır ama boş kalır.
- Canlı, pencereli: solda pencere başının saati ("19:12"; duvar saati bilinmiyorsa "−30:00"), sağda canlı kenar etiketi.

**Kontrol satırı** (`player-controls`, 64 dp, kaydırma yok): `[küme] 24 [tuş ipucu] 24 … [çipler, sağa yaslı]`
- Küme ile çipler arası en az 32 dp.
- Küme aralığı 12, çip aralığı 8.

**Satır içerikleri**

| Durum | Küme (soldan) | Çipler (sağa yaslı) |
|---|---|---|
| Film | −10 · Duraklat/Oynat · +10 | Ses / Altyazı · Seçenekler (S16) · ★ |
| Dizi | aynı + "Sonraki bölüm" hapı (biliniyorsa) | aynı |
| Aranamayan VOD | yalnız birincil (−10/+10 çizilmez) | aynı |
| Canlı, S15 öncesi | Duraklat | Kanal − · Kanal + (geçici, yukarı/aşağı ok) · Ses / Altyazı · ★ |
| Canlı, S15 sonrası, pencere yok | Duraklat | Kanallar · Önceki kanal · Ses / Altyazı · Seçenekler · ★ |
| Canlı, pencereli (S17) | −10 · Duraklat · +10 (+ "Canlıya dön") | aynı |
| Açılıyor | Duraklat; ±10 pasif | normal |
| Yeniden bağlanıyor | birincil "İptal et" (dur simgesi) | Ses / Seçenekler pasif |
| Hata | birincil "Tekrar dene" (yenile simgesi) | Ses / Seçenekler pasif, Kanallar açık |
| Bitti (film) | birincil "Baştan izle" (yeniden oynat simgesi); dizide EpisodeEnd kartı devralır | normal |

- Kapat yoktur.
- Kanal düğmesi hiçbir durumda kümeye girmez; aynı yuva hiç anlam değiştirmez.
- "Önceki kanal" yoksa pasif ama görünür (%38, D-pad atlar). Sağa yaslı satırda sonradan belirip odaktaki çipi kaydırmaması için böyle.

**Etiket düşürme**
- Küme her zaman tam ölçülür. Çipler önce etiketli ölçülür; sığmazlarsa sırayla yalnız simgeye inerler: Önceki kanal → Ses / Altyazı → Kanallar (Kanal −/+ zaten simge).
- ★ ve Seçenekler her zaman yalnız simgedir.
- "Sonraki bölüm" ve "Canlıya dön" yalnız 2,0'da 52 dp daireye iner.
- Ölçülen örnek: pencereli ve geride canlıda (1,0) küme x 378'de biter. Etiketli çipler 406'da başlardı (aralık < 32), bu yüzden "Önceki kanal" düşer ve çipler 510'da başlar.
- En kötü durum 2,0'da: küme 256 + çipler 5 × 56 + 32 = 600 dp < 912. `horizontalScroll` hiçbir ölçekte gerekmez.

## 2. Bileşenler

**Oynatma dairesi**
- Boyut: birincil 64 dp (simge 32); −10 / +10 52 dp (simge 28).
- Dinlenme: zemin `Ink.High` %72. Simge Text; birincilde Mint (D064: dinlenen birincil = koyu zemin, mint içerik).
- Odak: Mint dolgu, OnMint simge, 2 dp Text halka 3 dp dışarıda.
- Pasif: %38, D-pad girmez (Action'daki `canFocus = enabled || focused` kuralı).

**Hap** (Sonraki bölüm, Canlıya dön)
- 52 dp yükseklik, köşe 26, iç boşluk 16 / 20, simge 22, aralık 8, etiket 15 sp Medium. Renkler dairedeki gibi.
- Yalnız koşulu varsa çizilir; kümenin sonunda olduğu için başka bir şey kaymaz.

**Çip**
- Yükseklik max(44 dp, satır + 16), tam yuvarlak, iç boşluk 12 / 16, simge 20, aralık 8, etiket 15/20 sp Medium. Yalnız simgeli çip kare: 44 × 44.
- Dinlenme High %72, çerçeve yok (tek düğme ailesi).
- Seçili (★ favoride): Selected zemin, dolu Mint yıldız. Odakta yine Mint / OnMint.

**Odak etiketi**
- Odaktaki denetimin kendisi çizer, 6 dp üstünde. Kümede sol kenara, çiplerde sağ kenara yaslı; bu yüzden hiçbir zaman ekrandan taşmaz.
- Zemin Background %94, köşe 8, iç boşluk 4 / 10, 13/18 sp.
- İçerik:
  - Yalnız simgeli denetimlerde ad (Medium Text) ve varsa durum (Muted): "10 sn geri", "Seçenekler", "Favorilere ekle", "Kanal + · 13 Belgesel Plus".
  - Etiketli çiplerde durum (Regular): "Türkçe · Altyazı kapalı", "Haber · 86 kanal", "7 · Kent TV", "B6 · Dönüş · 48 dk".
  - Birincil Oynat/Duraklat ve Canlıya dön'de etiket yok. Birincil "Tekrar dene", "İptal et" ya da "Baştan izle" olduğunda adını gösterir.
- Anında çıkar, animasyon yok. TalkBack'e kapalıdır.

**Tuş ipucu**
- Kümeden 24 dp sonra başlar; 13/18 sp Muted. Tuş kapakları 18 dp yükseklikte, 1 dp Outline %60 kenarlı, köşe 4, içinde 12 sp Medium Text ya da 8 dp üçgen.
- Parçalardan oluşur. Sığmayan parça sondan düşer; hiçbir parça sığmazsa ipucu çizilmez. ≥ 1,3 yazıda gizlenir.
- İçerikler:
  - VOD: "Gizliyken ◀ ▶ sar · basılı tut: daha hızlı"
  - Çubuk odakta: "◀ ▶ 10 sn · basılı: 30 sn, 1 dk · OK git · Geri vazgeç"
  - Canlı (S15 sonrası): "Gizliyken ▲▼ kanal · OK kanallar · 0 önceki"
  - Canlı (S15 öncesi): "Gizliyken ▲▼ kanal · Rakam: kanal no"
  - Yeniden bağlanırken: "Geri: yeniden bağlanmayı durdur"

**Sarma çubuğu** (`player-scrub`)
- İz:
  - Dinlenmede 4 dp, köşe 2. Zemin Text %24, arabellek %42, oynanan %92, tutamaç 12 dp Text (VOD ve pencereli canlı).
  - Odakta 6 dp, oynanan kısım Mint, tutamaç 18 dp Mint ve 2 dp Text halka 3 dp dışarıda. Değişim anındadır.
- Bekleyen hedef:
  - Başlangıç noktasında 2 × 16 dp Text çentik.
  - Başlangıç ile hedef arası Mint %45.
  - Tutamaç hedefte.
- Pencereli canlı: oynayan nokta ile canlı kenar arası kesikli çizilir (Text %55, 5 dp dolu / 4 dp boş).
- Canlı kenar etiketi: nokta ve "CANLI" 14 sp Medium, harf aralığı ,08 em.
  - Kenardayken: Text, dolu nokta.
  - Gerideyken: Muted, içi boş nokta (1,5 dp).

**Zaman balonu** (`player-scrub-bubble`)
- Zemin Background %94, köşe 10, iç boşluk 6 / 12, 6 dp ok. Alt kenarı iz merkezinin 22 dp üstündedir; x 24–936 içine kıstırılır.
- İçerik: hedef 20/26 sp Medium Text ve 10 dp aralıkla fark 14 sp Medium Mint ("+10:00"). Pencereli canlıda "19:30:12 · 12 dk geride", kenarda "Canlı".
- Ne zaman görünür: çubuk odaktayken (fark yok) ya da bekleyen hedef varken (odak başka yerde olsa da).
- Meta satırını geçici olarak örtebilir; bu kabul edildi.

**CANLI rozeti**
- 24 dp, tam yuvarlak, zemin Background %72, iç boşluk 8 / 10.
- Kenarda: beyaz 8 dp nokta ve "CANLI" 12 sp Medium, harf aralığı ,08 em.
- Gerideyken: içi boş Muted nokta ve "4 dk 12 sn geride · 19:37" 13 sp tabular.

## 3. Tipografi

| Öğe | sp / satır | Kalınlık · renk |
|---|---|---|
| Başlık | 28 / 34 | Medium · Text |
| Saat | 20 / 26 | Regular · Muted, tabular |
| Canlı numara | 24 / 34 | Regular · Muted, tabular |
| Meta / canlı program / sonraki | 15 / 20 · 16 · 14 | Muted · Medium Text · Muted |
| Meta etiketi · rozet | 12 / 16 · 12 | Medium |
| Çubuk etiketleri | 20 / 26 | geçen Medium Text, kalan Regular Muted, tabular |
| Hap · çip | 15 / 20 | Medium |
| Zaman balonu | 20 / 26 + 14 | Medium Text + Medium Mint |
| Odak etiketi · ipucu · tuş kapağı | 13 / 18 · 13 / 18 · 12 | Medium / Regular · Muted · Medium |

12 sp'nin altında metin yok (D065).

## 4. Renk

Yeni token yok.

| Token | Kullanıldığı yer |
|---|---|
| Mint | odak dolgusu, odaktaki iz ve tutamaç, dinlenen birincil simge, balondaki fark, favori yıldızı |
| OnMint | odaktaki içerik |
| High %72 | dinlenen daire, hap, çip |
| Selected | favori zemini |
| Text | başlık, simge, halka, oynanan iz %92, çentik, CANLI noktası |
| Muted | meta, kalan süre, saat, ipucu |
| Background %94 / %72 | balonlar / rozet |
| Error | yalnız hata metni |

- Marka minti #1CECAB kullanılmaz.
- Dinlenen ilerleme beyazdır; mint videonun üstünde yalnız odağı gösterir.

## 5. Odak ve tuşlar

**İlk odak**
- Kontroller açılınca birincil düğme (Duraklat / Oynat / Tekrar dene / İptal et / Baştan izle).
  - VOD'da gizliyken OK açar.
  - Canlıda S15 sonrasında basılı OK, Menü ya da ikinci Bilgi açar (overlay.md).
- Panel (Tracks / Options / ChannelList) çipten açıldıysa, kapanınca odak o çipe döner.
- Canlıya dön'den sonra hap kaybolur ve odak birincile geçer.
- Sonraki bölüm PIN isterse odak hapa döner (bugünkü gibi).

**Odak grafiği**
- Satır soldan sağa gider: küme → ipucu atlanır → çipler. Uçta durur, başa sarmaz.
- Satırdan Yukarı: çubuk odaklanabiliyorsa çubuğa (`focusProperties { up = scrub }`), değilse bir şey olmaz.
- Çubuktan Aşağı: bekleyen hedefi uygular ve gelinen düğmeye döner (`focusRestorer`, yedek birincil). Çubuktan Yukarı bir şey yapmaz.
- Çubuk `focusProperties { canFocus = clock.seekable || focused }`. Pencere küçülüp çubuk aranamaz olsa da odak kaybolmaz.

**Çubuk odaktayken** (kendi `onPreviewKeyEvent`'i)
- Sol / Sağ (KeyDown, tekrarlar dahil): hedef `PlayerKeyRouter.seekStep(repeatCount)` kadar kayar: 10 sn, 10. tekrardan sonra 30 sn, 30. tekrardan sonra 60 sn. Sınırlar VOD'da 0…süre, pencereli canlıda pencere başı…canlı kenar.
- Video oynamaya devam eder; tek bir `seekTo` yapılır.
- OK (KeyUp): hedef varsa hemen gider; yoksa oynat / duraklat.
- Kendiliğinden gitme: son adımdan `SCRUB_COMMIT_MS = 1_000` sonra. Odak çubuktan çıkınca da gider.
- Geri: bekleyen hedeften vazgeçer, odak çubukta kalır. Sonraki Geri kontrolleri gizler.

**Kontroller açıkken medya tuşları**
- İleri / Geri sarma, odak nerede olursa olsun bekleyen hedefi +30 / −10 sn kaydırır ve balonu gösterir. Pencereli canlıda kenarda durur.
- Oynat / Duraklat tuşu bekleyen hedefi önce uygular, sonra oynatmayı değiştirir.
- CH± kanal değiştirir; rakamlar ve Rehber tuşu bugünkü gibi çalışır.

**Kontroller gizliyken** (bugünkü kurallar değişmez)
- VOD: Sol / Sağ anında 10 / 30 / 60 sn sarar, OK kontrolleri açar, FF / RW +30 / −10 sn.
- Yeni: köşe notu yerine sarma önizlemesi. Yalnız çubuk satırı ve balon ("+30 sn") alt 24 dp'de 2,5 sn görünür. Scrim 150 dp, 0 → .6. Odak almaz.
- Canlı: Yukarı / Aşağı ve CH± kanal değiştirir, rakamlar kanal seçer.
- S15: kısa OK listeyi açar, basılı OK kontrolleri açar, 0 önceki kanalı açar.
- S17: pencere varsa Sol / Sağ pencere içinde sarar.

**Basılı OK koruması (S15):** Basılı OK kontrolleri açıp odağı Duraklat'a verdiğinde aynı basışın kalan tekrarları ve KeyUp'ı yutulur. Yoksa tv-material Button KeyUp'ta tıklar ve yayın yanlışlıkla duraklar.

**Geri sırası** (her basışta bir katman)
1. EpisodeEnd
2. Tracks / Options / ChannelList (alt sütun önce)
3. Yeniden bağlanma: durdur ve çık
4. Bekleyen hedef: vazgeç
5. Controls: gizle (odak tuvale)
6. Bilgi bandı: gizle (S15)
7. Çık

**Otomatik gizleme**
- 8 sn (S14'te OSD süresi).
- Bekleyen hedef varken, TalkBack açıkken, yeniden bağlanırken ve hatada gizlenmez.

## 6. TalkBack

**Blok:** `isTraversalGroup`. Odak etiketi ve ipucu `clearAndSetSemantics {}` alır.

**Başlık satırı** (tek açıklama, canlı bölge değil)
- "Uzak Şehirler, film, 2023, 1 saat 48 dakika. Saat 21:14, bitiş 22:20"
- "12, Haber 24, canlı. Saat 19:42"; gerideyken "…, canlının 4 dakika 12 saniye gerisinde"

**Meta (canlı):** "Akşam Bülteni, 18 dakika kaldı. Sonra 20:00 Gündem Özel". Odak almayan program çubuğu buna katılır.

**Çubuk**
- contentDescription "Oynatma konumu" (canlıda "Yayın konumu").
- `progressBarRangeInfo` ve stateDescription "42 dakika 16 saniye, toplam 1 saat 48 dakika".
- `setProgress` hemen sarar. Özel eylemler: "10 saniye geri" / "10 saniye ileri".
- Hedef her tekrarda değil, uygulanınca bir kez kibar duyurulur: "52 dakika 16 saniyeye gidildi".

**Düğmeler**
- Birincil: Duraklat / Oynat / Tekrar dene / İptal et / Baştan izle (mevcut metinler).
- "10 saniye geri", "10 saniye ileri".
- "Sonraki bölüm, 6. bölüm, Dönüş".
- "Canlıya dön, 4 dakika 12 saniye ileri"; dönünce bir kez "Canlı yayına dönüldü".
- "Ses ve altyazı" + durum "Türkçe, altyazı kapalı".
- "Seçenekler".
- Favori: mevcut metinler + "Favorilerde" / "Favorilerde değil".
- "Kanal listesi" + durum "Haber, 86 kanal".
- "Önceki kanal, 7 Kent TV" (yoksa "Önceki kanal yok").
- S15 öncesi: "Kanal artı, 13 Belgesel Plus" / "Kanal eksi, 11 Ekonomi TV".

## 7. Yazı ölçeği

| | 1,0 | 1,3 | 1,5 | 2,0 |
|---|---|---|---|---|
| Başlık | 28 sp, 1 satır | 2 satır | 42 sp, 2 satır | 56 sp, 2 satır |
| Meta | 15 sp | "Sonra" 2. satıra | 22,5 sp | 30 sp, en çok 2 satır; "Sonra" ve kaynak gizli |
| Çubuk etiketleri | 20 sp (76 / 88 dp) | | 30 sp (114 / 132) | 40 sp |
| İz | ≈ 718 dp | | ≈ 637 | ≈ 556 |
| Odak etiketi bölgesi | 24 | 31 | 36 | 48 |
| Çip | 44 | 44 | 47 | 56 |
| İpucu, kaynak adı | var | gizli | gizli | gizli |
| Blok üstü (film) | y 330 | ≈ 300 | ≈ 280 (tek satır başlık) | ≈ 120–200 |
| Scrim | normal | büyük | büyük | büyük |

- Daire, hap yüksekliği, simge, halka ve iz kalınlığı dp'de sabittir.
- 2,0'da bütün çip etiketleri simgeye, haplar daireye iner. Satır yine sığar.

## 8. Hareket

- Kontroller: tek `graphicsLayer` alfasıyla giriş 120 ms, çıkış 100 ms (MOT08). Kayma ve ölçek yok.
- Odak, iz kalınlığı, tutamaç, balonlar ve hap belirmesi anında (D056).
- Konum 500 ms'lik tikle güncellenir; ara animasyon yok.
- Bilgi bandı ↔ kontroller ve sarma önizlemesi: 120 ms çapraz geçiş.
- Panel açılınca kontroller 100 ms'de söner.
- Hareketi azalt açıkken her şey anında.

## 9. PlayerLayer / PlayerKeyRouter

**Yeni katman yok.** Sarma Controls'un alt durumudur.

```kotlin
internal data class KeyContext(val mode: PlaybackMode, val layer: PlayerLayer, val recovering: Boolean = false,
    val failed: Boolean = false, val playing: Boolean = false, val repeat: Int = 0,
    val scrubbing: Boolean = false,   // çubukta bekleyen hedef var
    val liveWindow: Boolean = false)  // S17: aranabilir sağlayıcı penceresi
// Yeni komutlar
data class ScrubNudge(val deltaMs: Long) : PlayerCommand            // hedef yoksa konumdan başlatır
data class CommitScrub(val thenToggle: Boolean) : PlayerCommand
data object CancelScrub : PlayerCommand
data object BackToLive : PlayerCommand                               // S17
```

**`route` değişiklikleri**
- `PlayPause && scrubbing → CommitScrub(true)`. Mevcut PlayPause dalından önce gelir.
- `healthy && layer == Controls && (FF || RW) && (!live || liveWindow) → ScrubNudge(+30_000 / −10_000)`.
- Gizliyken FF / RW → `Seek` (bugünkü gibi).
- S17: `live && liveWindow && hidden && healthy && (Left || Right) → Seek(seekStep)`.
- Controls'ta Sol / Sağ / OK `null` döner (bugünkü gibi); çubuk kendisi işler.

**`back`:** EpisodeEnd → panel → recovering → `scrubbing → CancelScrub` → `Controls && !failed → HideControls` → (S15 bant) → Exit.

## 10. S14–S17 ve mevcut işlerle bağlantı

**Sıra:** S14 → **S14b** → S15 → S16 → S17 (PLAN §3-3: `PlayerScreen.kt` adımları sırayla yürür).
- S14b, S14'ten önce yürürse `ControlsAutoHide` yerine bugünkü 8 sn döngüsüne `scrubbing` koşulu eklenir.

**S14:** `ControlsAutoHide.shouldHide(..., scrubbing)` girdisi eklenir. Panel saydamlığı balonlara uygulanmaz (küçük, okunurluk öncelikli, %94 sabit).

**S15**
- Kanallar ve Önceki kanal çipleri gelir; geçici Kanal −/+ çipleri kalkar.
- Bilgi bandı (TV-053) ayrı bir `ChannelBand` değil, `PlayerInfoBlock(kind = Zap)` olur: başlık + meta + program çubuğu, alt 24, scrim 0 → .82, 4 sn. Genişlik 912 dp (sahip kararı 10). `ChannelBandModel`, `PlayerInfoModel`'e katılır.
- Kanallar çipinden açılan panelde Geri, Controls'a döner ve odak çipe gelir (Tracks gibi). Kısa OK ile açılan panel tuvale kapanır. overlay.md §3.3 bu ayrımla güncellenir.
- Menü ve Bilgi tuşları ile basılı OK koruması da bu adımda gelir.

**S16:** Seçenekler çipi (tune simgesi) Options katmanını açar: 440 dp, TracksDialog kabuğu. Panel kapanınca odak çipe döner. Yayın bilgisi panelde, OSD'de değil.

**S17**
- Saf `LiveWindow` modeli `Timeline.Window`'dan okur: `isDynamic`, `windowStartTimeMs`, `durationMs`, `currentLiveOffset`, `liveConfiguration.targetOffsetMs`.
- Geride sayılma: ofset − hedef ≥ 10 sn ise "Canlıya dön" belirir, < 3 sn ise kalkar. OK → `seekToDefaultPosition()`.
- Çubuk pencere ölçeğindedir; EPG eşlemesi gerekmez. Duvar saati yoksa (`TIME_UNSET`) göreli gösterilir.
- S56 aynı rozeti kullanır ("Duraklatıldı · 1 dk 05 sn geride").

**Değişmeyenler:** Sonraki bölüm kartı (480 dp, sağ alt; kart açıkken kontroller çizilmez), Tracks paneli (440 dp), köşe notu (rakam, kanal adı, "kanal yok") ve kontroller gizliyken ortadaki "Yayın açılıyor…" hapı.

## 11. Compose uygulama notları

**PlaybackClock** (yeni)
- `@Stable` bir sınıf. Alanları:
  - `mutableLongStateOf`: position, duration, buffered, windowStart, liveEdge.
  - `mutableStateOf`: seekable, liveWindow.
- 500 ms döngüsü position ve buffered'ı yazar. Listener (`onTimelineChanged`, `onAvailableCommandsChanged`) duration, seekable ve liveWindow'u yazar.
- Okuma yalnız şu yerlerde yapılır: zaman metinleri, `ScrubBar`'ın `drawBehind`'ı ve dakikaya yuvarlanan "Bitiş" (`derivedStateOf`).
- Bugün `position` / `duration` PlayerScreen düzeyinde (satır 106–107) ve 500 ms'de bütün ekranı yeniden oluşturuyor (D056).
- `player.isCurrentMediaItemSeekable` composition içinde okunuyor (satır 390–391) ve yalnız bu yeniden oluşturma sayesinde tazeleniyor. Taşımadan sonra ayrı bir durum olmalıdır.

**Bekleyen sarma**
- Saf `ScrubRules` (`nudge`, `commit`, `cancel`, `COMMIT_MS`) ve hedefin tutulduğu bir `ScrubHolder`.
- Hedef yalnız çizimde ve balonun `offset { }`'inde okunur.
- Zamanlayıcı tuş işleyicisinden başlatılan, her adımda iptal edilen bir `Job`'dur. PlayerScreen düzeyinde `LaunchedEffect` değildir; basılı tutmada ekran yeniden oluşmaz.

**Düğmeler**
- Theme.kt'de `Action`'ın renk / çerçeve / ölçek tanımı ortak bir `FilledControl` nesnesine taşınır: rest rengi parametre, odakta Mint / OnMint, `Border(BorderStroke(TvLayout.Focus, Ink.Text), inset = TvLayout.FocusInset)`, `focusedScale = 1f`, pasif %38.
- Bunun yanına `Modifier.enterWhenEnabled(enabled, focused)` eklenir.
- `Action` ve yeni `TransportButton` / `PlayerHap` / `PlayerChip` bunu kullanır; D064 tek kaynakta kalır.
- Odak etiketi Button'ın dışında, aynı `Box`'ta kardeş olarak çizilir, çünkü tv-material Surface içeriği şekle kırpar. Sıfır boyutlu bir `layout {}` ile yerleşir; odak durumu denetimin kendi `onFocusChanged`'ındadır. Satır düzeyinde odak durumu tutulmaz.

**Simgeler:** Yeni `ui/PlayerSymbols.kt`, MenuSymbols ile aynı yöntem: Material Symbols Rounded, Apache 2.0, 960 birimlik yol verisi. Gereken simgeler:
- pause, play_arrow, replay_10, forward_10, skip_next
- subtitles, tune, star (boş ve dolu), format_list_bulleted, history
- keyboard_arrow_up / down, last_page (Canlıya dön)
- refresh, stop, replay

HTML'deki simgeler yer tutucudur.

**`ControlRow`:** Özel `Layout`: küme | ipucu | çipler. Etiket planı saf `labelPlan(widths, max)` ile hesaplanır. Ölçüm `rememberTextMeasurer` ile (mod, fontScale, dil, metinler) anahtarında bir kez yapılır; odak değişince yeniden ölçülmez. İpucu parçaları da aynı ölçümle düşer.

**`PlayerInfoBlock(kind = Full | Zap | Peek)`:** Saf `PlayerInfoModel.from(entry, series, schedule, now, zone, clock)` üstüne kurulur. İçerik: başlık, etiket, meta parçaları, canlı kimlik, şimdi / sonra, kaynak, bitiş.

**Kaldırılanlar:** üst hap, üst scrim, `horizontalScroll` ve `controlVisibility` `BringIntoViewSpec`, Kapat, ‹ Kanal / Kanal ›.

**Altyazı:** Kontroller ya da bant görünürken `subtitleView.setBottomPaddingFraction((ekranYüksekliği − blokÜstü + 12 dp) / ekranYüksekliği)`. Blok üstü `onPlaced` ile ölçülür; 1,0'da ≈ ,41. Gizliyken varsayılan.

**Test etiketleri**
- Korunanlar: `player-panel` (blok), `player-title`, `player-controls` (satır), `player-favorite`, `player-current-programme`, `player-note`.
- Yeniler: `player-primary`, `player-seek-back`, `player-seek-forward`, `player-scrub`, `player-scrub-bubble`, `player-next-episode`, `player-tracks`, `player-focus-label`, `player-hint`, `player-clock`, `player-live-badge`, `player-peek`, S15'te `player-channels` / `player-previous-channel`, S16'da `player-options`, S17'de `player-back-to-live`.

**Metinler** (NoHardcodedUiTextTest)
- "10 saniye geri / ileri", "10 sn geri / ileri", "Bitiş %1$s", "%1$s kaldı", "%1$s geride · %2$s", "CANLI"
- "Oynatma konumu", "Yayın konumu", "%1$s, toplam %2$s", "%1$s konumuna gidildi"
- "Kanal −", "Kanal +", "Kanal artı, %1$s", "Kanal eksi, %1$s"
- İpucu parçaları
- S15–S17'nin kendi metinleri: Kanallar, Kanal listesi, Önceki kanal, Önceki kanal yok, Seçenekler, Canlıya dön, Canlı yayına dönüldü
- Kullanımdan kalkanlar: `player_channel_previous/next`, `player_keys_vod/live`, `player_live`, oynatıcıdaki `action_close`

## 12. Dokunulacak dosyalar

- `ui/PlayerScreen.kt`
  - Satır 314–413 yerine `PlayerInfoBlock` + `ControlRow` gelir.
  - PlaybackClock'a geçilir.
  - Sarma durumu ve sarma önizlemesi eklenir (sarmada köşe notunun yerine).
  - Altyazı boşluğu eklenir.
  - Seekable / liveWindow durumu listener'dan beslenir.
- `ui/PlayerControls.kt` (yeni): `PlayerInfoBlock`, `TitleRow`, `MetaLine`, `ScrubBar`, `TimeBubble`, `TransportButton`, `PlayerHap`, `PlayerChip`, `FocusLabel`, `KeyHint`, `ControlRow`, `LiveBadge`.
- `ui/PlaybackClock.kt`, `ui/ScrubRules.kt`, `ui/ControlRowPlan.kt`, `ui/PlayerInfoModel.kt` (yeni, saf).
- `ui/PlayerSymbols.kt` (yeni).
- `ui/PlayerKeys.kt`: KeyContext, komutlar, `route`, `back`.
- `ui/Theme.kt`: `FilledControl`, `enterWhenEnabled`; Action bunlara geçer.
- `res/values/strings.xml`.
- Belgeler: DECISIONS (yeni kayıt, onaydan sonra), SCREENS (oynatıcı), NATIVE_UI, PLAYER_PRESENTATION, STATUS / HANDOFF / BACKLOG.
- Figma çerçevesi ve manifest kaydı açık.

## 13. Testler

**JVM**
- `ScrubRulesTest`: seekStep hızlanması (tekrar 0 / 9 / 10 / 29 / 30), 0 / süre ve pencere sınırına kıstırma, hedef yokken nudge'ın konumdan başlaması, vazgeçme, tek commit.
- `ControlRowPlanTest`:
  - film / dizi / canlı 1,0'da sığar;
  - pencereli canlıda önce Önceki kanal düşer;
  - düşme sırası Önceki kanal → Ses / Altyazı → Kanallar;
  - 2,0'da hepsi simge ve haplar daire;
  - ★ her zaman simge;
  - ipucu parçalarının sayısı.
- `PlayerInfoModelTest`: meta parçaları, Bitiş yuvarlama, dizi adı ayrışmayınca tam başlık, geride metni.
- `PlayerKeyRouterTest` ekleri:
  - scrubbing iken PlayPause → `CommitScrub(true)`;
  - Controls'ta FF / RW → `ScrubNudge`, gizliyken → `Seek`;
  - Geri sırasında `CancelScrub`, `HideControls`'tan önce;
  - recovering yine önce çıkar;
  - S17'de pencereli gizli Sol / Sağ → `Seek`.

**Cihaz:** yeni `PlayerControlsLayoutTest`, 1,0 / 1,5 / 2,0 yazıda.
- Blok gutter içinde.
- Her odaklanabilir denetimin halkası (+5 dp) ekranda.
- Küme ile çipler arası ≥ 32; kaydırma yok.
- ★ ve −10'un odak etiketi ekranda.
- Sıra: başlık < çubuk < satır.
- İlk odak birincilde. Yukarı → çubuk, Aşağı → son düğme.
- Çubukta Sağ × 3, sonra 1 sn → konum +30 sn ±1 sn ve tek konum sıçraması (`onPositionDiscontinuity` sayısı 1).
- Bekleyen hedefte Geri konumu değiştirmez; ikinci Geri gizler, üçüncüsü çıkar.

**Güncellenecek testler**
- `GuideFlowTest:341–345`: Kapat yerine Geri (gizler) → Geri (LIBRARY).
- `VisualPreviewTest:492, 676, 683`: `click("Kapat")` yerine iki Geri.
- `VisualPreviewTest:513–528`:
  - Ses / Altyazı'ya Duraklat'tan artık 2 Sağ ile gelinir.
  - "kaydırma ile panele girer" döngüsünün yerine halkanın ekranda kalması kontrol edilir.
  - Son öğe ★ olur; çıkış Geri ile.
- `PilotFlowTest:99`: `click("Kanal ›")` yerine "Kanal artı" açıklaması (S15'te Kanallar ya da CH tuşu).
- `PilotFlowTest:110, 205`: `click("+10 sn")` yerine `onNodeWithContentDescription("10 saniye ileri")`.
- Geçerli kalanlar: `PlaybackRecoveryFlowTest:105–112`, `GuideFlowTest:167, 335–339`, `EpisodeAutoplayFlowTest:237`, `ProviderSmokeTest:65`, VisualPreview'deki "Ses / Altyazı" tıklamaları.

**Elle**
- 3 m'den beyaz ve koyu karede okunurluk.
- "10" glifi ve odak etiketi.
- Fiziksel TV'de kare süresi.
- Kendiliğinden gitme süresinin ayarı.
- TalkBack turu.

## 14. Efor

**Toplam ≈ 4 mühendis-günü.** Faz D 11 → 15 g.

| Aşama | İçerik | g |
|---|---|---|
| K1 | VOD: PlaybackClock, PlayerSymbols, FilledControl, küme / çip / odak etiketi / ipucu / ControlRow, ScrubBar + ScrubRules, router, PlayerInfoModel, scrim ve üst hapın kaldırılması, altyazı boşluğu, JVM testleri | 2,5 |
| K2 | Canlı: numara / logo / rozet, program çubuğu, geçici Kanal −/+, seekable durumu | 0,75 |
| K3 | Sarma önizlemesi, 1,5 / 2,0 dengeleme, TalkBack turu, test güncellemeleri, belgeler | 0,75 |

S15, S16 ve S17 kendi çiplerini ve görsellerini kendi tahminleri içinde ekler (her biri ≈ 0,25 g, yer zaten ayrıldı).

## 15. Sahip kararları

Cevap gelmezse varsayılan uygulanır.

| # | Karar | Varsayılan (öneri) | Alternatif |
|---|---|---|---|
| 1 | "Kapat" | Kalkar; Geri iki adımda çıkar | Çiplerin sonunda × simgesi |
| 2 | ‹ Kanal / Kanal › | Kümeye girmez. S15'e kadar geçici "Kanal −/+" çipleri, S15'te Kanallar + Önceki kanal | S15'ten sonra da "Kanal −/+" kalır (canlı satır 7 durak, ipucu sığmaz) |
| 3 | Çubuğun sağ etiketi | Kalan süre; toplam ve Bitiş meta satırında | Toplam süre |
| 4 | Kendiliğinden gitme | 1 000 ms, OK hemen gider | 700 ms ya da her adımda hemen sarma |
| 5 | Gizliyken Sol / Sağ | Anında (bugünkü gibi) + sarma önizlemesi | Çubukla aynı bekleyen hedef (tek seekTo, tek sarma dili) |
| 6 | CANLI noktası | Beyaz (Text) | Error #FFB4AB |
| 7 | Saat ve Bitiş | İkisi de var | İkisi de yok |
| 8 | Önceki kanal yokken | Pasif ama görünür | Gizli |
| 9 | Odak etiketi | Simgede ad, etiketli çipte durum | Yalnız simgeli denetimlerde |
| 10 | Bilgi bandı genişliği | 912 dp (kontrollerle aynı) | 640 dp (overlay.md) |

## 16. Riskler

1. Blok 210 dp; bugünkü film bloğundan uzun. Resmin üstü temiz kalsa da gömülü altyazı ve haber bantlarını daha geniş örter. Önlemler: 8 sn'de gizlenme ve ölçülen altyazı boşluğu.
2. Simgelerin tanınması (Seçenekler, ±10'daki "10" ≈ 9 dp) odak etiketine dayanıyor. Fiziksel TV'de kabul gerekir.
3. Kendiliğinden gitme süresi cihazda ayarlanmalı. Çubuktaki ertelemeli sarma ile gizlikenki anında sarma iki ayrı dil oluşturur (karar 5).
4. Etiket planı ve ipucu ölçümü yeni karmaşıklık getiriyor. Mockup genişlikleri sistem yazı tipiyle alındı (±%5); cihazdaki Roboto ile yeniden ölçülmeli. Pencereli canlı satırı sınıra yakın.
5. %72 saydam dolgu parlak karede: kontrast fiziksel TV'de ölçülmeli. Scrim parlak karede hesaplandı, cihazda ölçülmedi.
6. Pencere yayın sırasında değişebilir (S17). Çubuk odaktayken aranamaz olursa odak korunur, sonra birincile gider.
7. Basılı OK koruması uygulanmazsa canlıda yanlışlıkla duraklatma olur (S15).
8. S15 öncesi geçici Kanal −/+ çipleri ve S15'teki Geri ayrımı, overlay.md ile eşitlenmeli.
9. Compose uygulaması ve emülatör otomatik kontrolleri 0.14.47 çalışmasında; nihai koşu/teslim sonucu STATUS/HANDOFF'ta. Fiziksel TV okunurluk/kare süresi/kumanda/TalkBack, Figma ve sahibin görsel kabulü açık.
10. Bağımsız kod incelemesindeki P3 (18 Ekim 2026) düzeltildi: çubuk odaktayken §5'teki OK/Geri ipucu seçilir; çubuktan ayrılınca genel VOD ipucu geri gelir. Büyük yazıda ipucunu gizleme ve ölçerek parça düşürme korunur.
