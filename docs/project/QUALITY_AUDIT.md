# Ürün kalitesi ve tamamlanma denetimi

**Hedef:** “Mükemmel çalışacak ve görsel olarak da tatmin edecek” IPTV uygulaması. Bir APK'nin derlenmesi veya birkaç örneğin oynaması bu hedefi tamamlamaz. Bu kayıt PRD kapsamını daraltmaz; her satır gerçek uygulama ve ölçüm kanıtıyla kapatılır.

**Başlangıç denetimi · 13 Ekim 2026:** Önceki tur somut ilerleme: 0.1.0 APK, 14 JVM / 3 emülatör testi, gerçek sağlayıcıda üç sıralı oynatma. Mevcut kaynaklar ve raporlar yeniden okundu. Bu başlangıçtan sonra 0.2 fiziksel TV’ye kuruldu, kaynak bağlandı ve canlı oynatma gözlendi. Tam cihaz matrisi açık. Güncel 0.8 kanıtı native-lifecycle-qa.json içindedir; 0.7 bölüm sonu kanıtı native-episodes-qa.json içinde korunur; 0.6 kurtarma kanıtı native-playback-qa.json içinde korunur; 0.5 ayar kanıtı native-settings-qa.json içinde korunur; 0.4 arama kanıtı native-search-qa.json içinde korunur; 0.3 cache/açılış kanıtı native-polish-qa.json içinde korunur; 0.2 fiziksel kanıt native-home-qa.json içinde korunur.

## PRD kapsamı

| Gereksinim | Mevcut kanıt ve açık iş | Tamamlanma kanıtı |
| --- | --- | --- |
| SRC01 M3U | Ayrıştırıcı tvg-id/tvg-name ve header rehber adreslerini şifreli snapshot'ta korur; 0.13 özel XMLTV formu ve native entegrasyonu var | Geçerli/bozuk/büyük liste, tvg-id, XMLTV formu ve erişilebilir belirsiz tür |
| SRC02 Xtream | API ve gerçek örnekler çalıştı; farklı hata sınıfları daha ayrıntılı olmalı | Alan korunumu, auth/ağ/boş/kısmi katalog senaryoları |
| SRC03 Çoklu kaynak | 0.10 native yönetim/filtre, owner artwork temizliği ve bozuk registry recovery; 0.11 uzun ad/16 kaynak/üç font testli; 0.12 art index bakımı; kullanıcı turu açık | Çoklu kaynak ayrımı; izlenebilir yenileme; başarısızlıkta son çalışan katalog |
| LIVE01 Canlı katalog | 0.13 görünen kartlarda şimdi/sonra ve oynatıcıda program bağlamı var; bir sağlayıcıda 402 katalog satırının rehber eşleşmesi doğrulandı; fiziksel izleme geçişi açık | Rehberli ve rehbersiz kanalda doğru kart/oynatma |
| EPG01 ve kabul ölçütü EPG02 | 0.13 native ızgara/şimdi–sonra/ayrıntı, kalıcı saat dilimi ve şifreli XMLTV ayarı; sentetik DST/iptal/yenileme testli. Bir sağlayıcıdan indirilen XMLTV Android üretim yolunda doğrulandı; diğer sağlayıcılar, fiziksel rehber performansı ve kullanıcı turu açık | XMLTV/Xtream, kimlik, ofset/DST, şimdi/sonra/ızgara, önbellek/hata |
| LIB01 Kişisel kütüphane | 0.8 bölümden dizi favorisi ve eski kayıt dönüşümü doğrulandı; 0.2 ana sayfa/son kanallar ve 0.3 yerel cache doğrulandı | Devam/son kanal/favori, eksik kayıt ve yeniden açılış; en çok iki OK ile favori kanal |
| FIND01 Arama | Türkçe ortak/tür içi arama, Back, çoklu kaynak filtresi; 0.12 paylaşılan ön hazırlık ve 60bin kayıtlı tanı turu var; fiziksel UI p95 ölçümü açık | Türkçe eşleştirme, 10 bin kayıt, filtre/scroll/odak koruma ve erişim politikası |
| PLAY01 | Media3, TS/MP4 örnekleri ve gerçek track paneli çalıştı | HLS/VOD/TS matrisi, fiziksel decoder ve ilk kare ölçümü |
| PLAY02 | 0.6 sınırlı kurtarma/iptal/arka plan ve MP4/HLS sentetik hata akışları geçti; gerçek sağlayıcı/cihaz matrisi açık | Ağ kes/kurtar, auth/biçim hatasında dur, iptal, üst süre/deneme sınırı |
| Kabul ölçütü PLAY03 | Tek oynatıcı, Home'da önizleme yok; arka planda pause | Bağlantı sayısı ve Home/uyku/yeniden girişin cihaz ölçümü |
| UX01 | 0.8 sol menü 1.5/2.0 yazı ölçeği ve kaynak işlem/Geri yarışları geçti; 0.4 uzun başlık, silinen hedef/kategori, hızlı karşı yön ve 50 kategori; 0.5 Back/ayar odağı ve viewport daralması kontrolü var; tüm ekran/gerçek cihaz matrisi açık | Tüm ekranlarda 4 yön, hızlı tekrar, silinen hedef, liste sonu; gerçek kumanda |
| SAFE01 | Yalnız sağlayıcı yetişkin bayrağı filtresi var | PIN/kategori/kaynak, gizleme, arama/favori/geçmiş/direkt girişte ortak denetim |
| SYS01 | Azaltılmış odak ölçeği; 0.5 açıklamalı/kaydırılabilir Ayarlar ve 1.5 font turu var; 0.13 kalıcı rehber saat dilimi eklendi; tam erişilebilirlik denetimi değil | Sistem hareket tercihi, dil/saat dilimi, metin/kontrast, kaynak sağlığı |
| VOD01 | Film ayrıntısı/başlat/devam var | Eksik metadata/afişte kullanılabilirlik, odak ve gerçek cihaz seek |
| VOD02 | 0.7 otomatik geçiş/iptal/ayar ve belirsiz sıra, sezon sonu, hedef değişimi, arka plan sentetik akışları geçti; gerçek cihaz/kullanıcı turu bekler | Otomatik sonraki bölüm ayarı/iptal, sezon sonu, eksik/silinen bölüm |
| VOD03 | Yerel ilerleme var; 0.2 ana sayfa/geçmiş temizleme doğrulandı | Tam bölüm kimliği, %95 eşiği, ani kapanma toleransı, temizleme ve kalıcılık |

P1 QR, catch-up, EPG düzeltme, zengin metadata ve senkronizasyon; P2 kayıt/timeshift/çoklu ekran ilk yayın kapsamına sessizce eklenmez veya P0 eksiklerinin yerine sunulmaz. LG/Samsung/mobil fazları korunur; bu hedefin ilk yayın doğrulaması Android TV/Google TV üzerindedir.

## Görsel ve davranış kapıları

- Her P0 ekran: gerçek görsel, eksik görsel, yükleme, boş, hata ve kısıtlı içerik. Önce gerçek ekran görüntüsü, sonra gerekirse düzeltme.
- Renkler mevcut Figma rolleri; aktif metin hedefi ≥4.5:1, gerekli odak ≥3:1. Afiş üstü scrim en kötü arka planda ayrıca ölçülür.
- Hareket yeni tuşu bekletmez; azaltılmış hareket büyük ölçek/kaymayı kaldırır. Hareket rehberindeki gerçek cihaz ölçümleri bekler.
- 1080p/4K yerleşim ve gerçek izleme mesafesi. Kaynak afişini yanlış başlığa uydurmak veya sağlayıcıdaki dil etiketini gerçek track gibi göstermek yok.
- Figma kit envanteri/rol sözleşmesi korunur. Kota kalkınca ekran aktarımı ve bekleyen temizleme yapılır; kota nedeniyle yerel çalışma durmaz.

## Ölçüm kapıları

PRD öneri hedefleri: odak p95 ≤100 ms; hazır önbellekle açılış p95 ≤2 s; kontrollü HLS ilk kare p95 ≤4 s; 10 bin kayıt arama p95 ≤300 ms; iki saat oynatma/bellek; yeterli betada %99,5 çökmesiz oturum. En az beş kişilik keşif turu ve düşük bellekli cihaz + güncel Google TV + üretici TV matrisi gerekir. Henüz gerçekleşmiş sonuçlar olarak sunulamaz.

## Yakın çalışma sırası

0.12 tanı kanıtı:60bin kayıtlı debug/emülatör turunda restore2,38–2,63s, ilk sorgu823ms, sonraki23 sorgunun medyanı151ms/p95'i218ms. Bunlar aynı süreçte model isteğinden Compose görünürlüğüne, test senkron maliyetiyle ölçüldü; fiziksel input-to-pixel, süreç açılışı veya release p95 kabulü değildir. Hazır katalog açılışı ve ilk arama hâlâ iyileştirilmelidir.10bin geçersiz artwork referansı artımlı turla temizlendi, kullanılan tek afiş kaldı. [Yöntem](../architecture/PERFORMANCE.md).

1. Native ana sayfa, doğru bölümden devam, son kanallar, geçmiş temizleme ve geri dönüş odağı; gerçek afiş ekranını incele.
2. Kalıcı katalog ve kesintisiz yenileme; ağ bozulduğunda kullanılabilirlik. Kaynak değişimi/silme ve eski pilot verisi geçişini doğrula.
3. Türkçe ortak arama, çoklu kaynak, EPG ve PIN için PRD kabul ölçütlerini uygula; sahte sağlayıcıyla olumsuz durumları sınayıp izinli gerçek kaynakta örnekle.
4. Oynatıcı kurtarma/otomatik sonraki bölüm ve tüm odak durumları; ardından performans bütçeleri.
5. Kullanıcının 65U95A cihazı erişilebilir olunca paralel ürün işi icat etmeden gerçek cihaz ölçümlerini tamamla. Hazır APK'nin hangi sürümünün kurulu olduğunu kaydet.

**Hedef aktif:** Eksik P0 ve doğrulanmamış fiziksel kalite kapıları varken tamamlandı sayılmaz. Sonuçlar STATUS/HANDOFF ve sürüme özel QA kaydına işlenir.
