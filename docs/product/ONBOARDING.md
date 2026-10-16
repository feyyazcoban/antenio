# Telefonda tamamlanan TV kurulumu

Kumandayla uzun bağlantı, kullanıcı adı ve parola yazmak ilk kurulumu zorlaştırıyor. Önerilen ana yol: TV'de QR kod, telefonda tek bağlantı veya hesap girişi, TV'de onay. Telefon uygulaması indirmek veya uygulamamıza hesap açmak gerekmemeli.

**Durum:** Kullanıcı 13 Ekim 2026'da kurulumu son kullanıcılar için kolaylaştırmamızı istedi. Bu akış ürün önerisidir; 0.2.0 pilotta uygulanmadı. PRD'deki P1 QR kurulumu ve BKL-016'yı ayrıntılandırır. Mevcut M3U/Xtream manuel giriş korunur. Üretimde uygulamanın TV mağazasından yüklenmesi ayrı adımdır; QR içerik kaynağını ekler.

## Kullanıcı akışı

1. TV'de **Telefonla ekle** seçilir. QR, kısa bir web adresi, okunaklı eşleştirme kodu ve **Kumandayla gir** seçeneği görünür.
2. Telefon QR'ı okutur ve HTTPS kurulum sayfası açılır. QR okunamıyorsa aynı sayfaya eşleştirme kodu yazılır. Ayrı mobil uygulama gerekmez.
3. Kullanıcı **Bağlantı yapıştır** veya **Sunucu ve hesap bilgileri** yolunu seçer. Yapıştırma yalnız kullanıcının eylemiyle yapılır; pano arka planda okunmaz.
4. Telefon, bağlanılacak TV'nin kısa kodunu ve kaynak adını gösterir. Kullanıcı **TV'ye gönder** der.
5. TV'de **Bu kaynağı ekle?** onayı, kaynak adı ve telefondakiyle eşleşen kod görünür. Parola ve kimlikli bağlantı TV'de gösterilmez.
6. TV sağlayıcıya kendisi bağlanır. İçe aktarma durumu ve tamamlanan canlı/film/dizi sayıları gösterilir. Kısmi katalogda başarılı bölümler açılabilir; başarısız bölüm yeniden denenir.
7. Ana sayfa açılır. Telefon **TV hazır** sonucu gösterir, geçici eşleştirme kapanır.

Başlangıç süre önerisi beş dakikadır; kullanıcı araştırmasıyla ayarlanır. Süre bitince **Yeni kod oluştur**, kullanıcı iptal edince **Kurulum iptal edildi** görünür. Yanlış parola veya erişilemeyen sağlayıcı, telefon formunu silmeden düzeltilebilir. İnternet/telefon yoksa manuel TV formu kullanılabilir.

## Tek bağlantıyla giriş

En yakın bağımsız iyileştirme, M3U adresini tek alanda alıp yapılandırmayı önermektir. Tanınan sağlayıcı bağlantılarında sunucu, kullanıcı ve parola alanları çıkarılabilir; desteklenmeyen veya belirsiz bağlantı M3U olarak kalır. Hesap API'si varsayılarak sessizce kaynak türü değiştirilmez. Kullanıcı kaynak tipini ve görünen kaynak adını doğrular.

Bağlantı yolu, port, sorgu parametreleri ve kodlanmış karakterler korunur. Parola kırpılmaz, küçük harfe çevrilmez veya iki kez URL çözülmez. Türkçe karakter, `@`, `:`, `/`, `+`, `%` ve `&` içeren girdiler birebir aktarılır. Elle hesap girişinde alanlar ayrı doğrulanır; hata mesajları parolayı veya tam adresi tekrar etmez.

Bu pilotta geliştirici tarafından `adb input text` ile yapılan girişte TV'nin klavye düzeni bazı karakterleri değiştirdi. Başarısız karşılaştırma gönderimi durdurdu. Bu durum kullanıcıya görünen klavyenin her girişte bozuk olduğu anlamına gelmez; ürünün telefon aktarımında metni klavye olaylarına dönüştürmemesi gerektiğini gösterir.

## Veri ve bağlantı tasarımı

TV ve telefonun dışarıya HTTPS ile bağlandığı geçici bir eşleştirme hizmeti önerilir. Böylece aynı Wi-Fi şartı, TV IP adresi ve geliştirici seçenekleri kullanıcı akışına girmez. Hizmetin alan adı, barındırma maliyeti ve işletimi henüz kurulmadı.

- QR, kısa ömürlü oturum bilgisi taşır; kullanıcı adı, parola veya IPTV adresi taşımaz. Elle girilen kod ile TV'nin özel erişim anahtarı farklıdır.
- Eşleştirme tek kullanımlı, süreli ve deneme sınırlandırmalı olur. Telefon ve TV'de aynı kısa kod gösterilir; TV onayı olmadan içe aktarma başlamaz. İptal, önceki QR ve bekleyen isteği geçersiz kılar.
- Hesap aktarımı için TV'ye bağlı şifreli zarf ve denetlenmiş şifreleme kütüphanesi değerlendirilir; özel kripto protokolü tasarlanmaz. Anahtar eşleştirmesi ve kötü niyetli aracı modeli doğrulanmadan uçtan uca şifreleme iddiası yapılmaz.
- Sağlayıcı adresine yalnız TV istek yapar. Eşleştirme hizmeti genel amaçlı bir URL indirme vekiline dönüşmez.
- Geçici yük teslimden veya zaman aşımından sonra silinir. Kaynak bilgileri TV'de mevcut Keystore deposuna yazılır; tarayıcı kalıcı depolamasına veya analitik/crash kayıtlarına alınmaz.
- Eşleştirme HTTPS koruması, sağlayıcının HTTP yayınını şifreli hale getirmez. Bu iki aktarım ayrı değerlendirilir.

Akıllı TV gibi yazı girişi sınırlı cihazlarda ikinci cihazla tamamlama modeli [IETF RFC 8628](https://www.rfc-editor.org/rfc/rfc8628) içinde tanımlanır. Burada yararlanılan kısım kullanıcı etkileşimi, süreli kod ve eşleştirme ilkeleridir; M3U/Xtream sağlayıcısının OAuth desteği olduğu varsayılmaz ve bu kaynak aktarımı OAuth uyumlu olarak sunulmaz.

## Kabul ölçütleri ve sıra

1. Önce tek bağlantıyı doğru ayrıştırma ve manuel girişte hatasız geri dönüş; sentetik özel karakter ve belirsiz bağlantı örnekleri.
2. Telefon web formu ile TV eşleştirme prototipi; QR ve elle kod yolları, yanlış TV, süre dolumu, iptal, tekrar gönderim ve ağ kesintisi.
3. Kalıcı sır bırakmayan aktarım ve hizmet doğrulaması; büyük katalog sırasında ilerleme ve yeniden deneme.
4. En az beş kişiyle kurulum denemesi: ilk denemede başarı, süre, kumandada yazılan karakter sayısı ve yardım ihtiyacı ölçülür. Öneri hedefi: telefon yolunda TV'de hiç hesap karakteri yazmamak.

İlgili ekranlar TV-001/002/003/004 ve kaynak ayarları TV-070; QR/telefon alt durumları [SCREENS](../design/SCREENS.md) altında tutulur. Yeni Figma ekranı oluşturulmadı; mevcut hesap/kota koşulları sürer.
