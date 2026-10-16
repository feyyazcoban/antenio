# Gerçek kaynak yayın rehberi incelemesi

**14 Ekim 2026 · 0.13.0-pilot.** Kullanıcının önceden izin verdiği kaynaktan XMLTV ve canlı katalog sırayla alındı. İki HTTP isteği de başarılı; yayın, afiş veya logo açılmadı. İndirilen dosyalar Android emülatöründe uygulamanın mevcut okuyucu, yenileme ve veritabanı yolu üzerinden ayrıca doğrulandı. APK değişmedi. [Sayısal kanıt ve ölçümler](../project/provider-guide-qa.json).

## Kapsam ve sonuç

| Ölçüm | Sonuç |
| --- | --- |
| XMLTV gövdesi | 4.689.813 byte; HTTP 200 |
| Canlı katalog | 4.525 kayıt; 1.345.484 byte; HTTP 200 |
| XML kanal tanımları | 402 satır, 252 farklı kimlik; ayrıca yalnız program satırlarında bulunan 1 kimlik |
| XML program satırları | 10.648 |
| Geçersiz program | 31: 27 bitiş başlangıçtan önce, 4 bitiş başlangıca eşit |
| Geçerli program | 10.617; aynı kanal/başlangıç/bitiş/başlıklı 29 tekrar çıkarılınca 10.588 |
| Kaydedilip yeniden okunan | 253 kanal, 10.588 program; tamamı yeniden açıldı |
| Kaynaktaki saat ofseti | Bütün 21.296 başlangıç/bitiş alanında +03:00 |
| Gözlenen yayın dönemi | 13 Ekim 10:20 – 16 Ekim 08:00, İstanbul saati |
| Rehber dosyası ve yan dosyaları | 4.616.192 byte; bu örneğin ölçümüdür |

Rehberdeki kayıt sayısı, katalogdaki kanal sayısıyla aynı değildir. Kalite/bölge varyantları aynı EPG kimliğini paylaşabilir. 402 XML tanımını 402 farklı kanal gibi göstermeyin. Ayrı kanal tanımı bulunmayan bir kimlik de programlardan okunarak toplam 253 kanala ulaşılır. Kaynak yalnız yaklaşık üç günlük program gönderdi; uygulamanın yedi gelecek gün tutabilmesi kaynağın o günleri sağladığı anlamına gelmez.

## Kanal eşleşmesi

| 4.525 canlı katalog satırının sınıfı | Satır sayısı |
| --- | --- |
| Birebir EPG kimliği | 401 |
| Kimlik boşken tek ve tutarlı ad eşleşmesi | 1 |
| EPG kimliği dolu fakat XMLTV'de bulunamadı | 89 |
| Kimlik/ad eşleşmesi yok | 4.034 |

Toplam 402 katalog satırı, **252 farklı rehber kanalına** eşleşti. Katalogda EPG kimliği dolu 490 satır var. Eşleşmeyen satırlara benzer ad, HD/UHD eki veya tahmini kimlikle program atanmadı. Eşleşme kalitesi sağlayıcıya bağlıdır; bu örnek başka sağlayıcıların uyumluluğunu kanıtlamaz.

Karşılaştırma anı **14 Ekim 2026 10:34 İstanbul / 07:34 UTC** olarak sabitlendi. O anda 226 katalog satırında tek bir kesin program, 48 satırda birden fazla eşzamanlı program var. Bunlar sırasıyla 133 ve 26 farklı rehber kanalına karşılık gelir. 26 çakışan kanalın 18'inde farklı program başlıkları bulunur; yalnız sekizinde başlık aynı olsa da zamanlar farklıdır. Tekrarlanan 29 satırın temizlenmesi bu çakışmaları çözmez. Uygulama bu nedenle çakışan kayıtları kesin bir “şimdi” bilgisine dönüştürmez. 263 katalog satırında sonraki 12 saat içinde tek başlangıçlı sonraki program bulunur.

## Uygulama ve test yöntemi

- Gerçek HTTP gövdeleri yalnız özel çalışma alanında tutuldu. Android'e hesap veya sağlayıcı adresi yerine önceden indirilmiş metadata ve sentetik kaynak sahibi verildi.
- `XmltvParser`, `EpgRepository`, `CatalogParser.xtream` ve `EpgStore` üretim kodu kullanıldı. Yerel test sunucusundan üç ardışık yenileme, SQLite yeniden açılışı, kaynak sınırı ve silme kontrol edildi. Android testinin sağlayıcı isteği sıfırdır; host üzerindeki iki indirme ayrı kanıttır.
- Canlı kart/oynatıcı ile aynı **şimdi + 12 saat** sorgusu, 16 kayıtlık 283 grup halinde çalıştırıldı. Bu sorgularda ve o günün ızgara sorgularında satır kesilmesi yoktur. Bugün bir kanaldaki en yoğun çizelge 325 programdır.
- Tüm saklama dönemini tek sorguda isteyen bir kanalda 650 program vardır. Varsayılan 512 sınırı burada doğru biçimde `truncated` döndürür. Tam depolama denetimi açıkça 2.000 sınırıyla yapıldı; üretim sınırı artırılmadı. İlk denetim testi tüm dönemde kesilme olmayacağını varsayarak başarısız olmuştu; gerçek ekran pencereleriyle denetim ve ayrıca tam dönem kontrolü ayrıldı.
- Yanlış kaynak sahibi reddedildi. Testin rehber dosyası ve Android'e kopyalanan özel girdiler başarı/hata kapanışında temizlenir. Başka kaynakların kayıtları silinmez. Sonuç dosyası yalnız sayılar, süreler ve dosya özetleri içerir.

Süreler debug emülatörde tanı ölçümüdür. İnternet indirmesi, yerel tekrar oynatımı, yalın ayrıştırma ve SQL sorgusu ayrı ölçülür; birbirine eklenerek TV açılış süresi veya fiziksel p95 diye sunulmaz. Java heap son örneği tepe bellek, native/GPU bellek veya sızıntı testi değildir.

## Kuruluma etkisi ve açık işler

Xtream kurulumunda hesapla oluşturulan `xmltv.php` rehberi bu kaynakta vardır. 13 Ekim'deki iki kısa EPG API isteğinin hata vermesi bütün rehberin yokluğu anlamına gelmiyordu; eski gözlem korunur. Kısa EPG JSON desteği bu incelemede eklenmedi.

Önceki M3U incelemesinde başlıkta XMLTV adresi yoktu. Bu nedenle M3U ile kurulumda kaynak ayarındaki **Rehber adresi** alanı gerekir; uygulama liste URL'sinden hesap bilgilerini tahmin ederek rehber üretmez. Bu tur taze Xtream canlı katalog eşleşmesini doğrular; bütün M3U listesini tekrar indirme/kurma testi değildir.

Fiziksel 65U95A performansı, gerçek TV'de rehberden oynatmaya geçiş, düşük bellekli cihaz, başka sağlayıcılar ve kullanıcı incelemesi hâlâ açık. Native görsel/odak kanıtı önceki [0.13 QA kaydında](../project/native-guide-qa.json) bulunan sentetik programlı ekranlardır; bu incelemede yeni görsel veya Figma güncellemesi yoktur. BKL-011 bu yüzden review kalır.

## Güvenli tekrar

`PrivateGuideAuditTest` normal test çalışmasında atlanır. Yalnız ayrılmış emülatörde `privateGuideAudit=true` ve indirmenin dönemindeki açık bir ISO UTC `auditInstant` parametresiyle çalışır. `files/private-guide-audit/guide.xml` ve `live.json` önce uygulamanın özel alanına aktarılır; bu dosyalar test kaynağına veya asset paketine eklenmez. Test sınıfı fiziksel cihazı reddeder ve oynatıcı açmaz. Yeniden çalıştırırken yeni indirme zamanı, gövde özetleri ve sonuçlar ayrı kaydedilmelidir; tarihli sonuçlar güncel veri gibi yeniden etiketlenmez.
