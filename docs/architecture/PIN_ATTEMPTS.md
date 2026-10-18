# Kalıcı PIN deneme bütçesinin mantığı

18 Ekim 2026 · SAFE01/BKL-028 · S08–S12 ile depo, kontrolcü ve UI bağlı (D073/D074).

`PinAttempts` doğrulama öncesi rezervasyon modelidir. Çağıran tek bir kilit altında son durumu okumalı, `reserve` sonucundaki `next` durumunu atomik/şifreli kaydetmeli, ancak yazma başarılı ve `mayVerify=true` ise PIN karşılaştırmasına başlamalıdır. Sonuçtaki boolean tek başına erişim izni değildir. Yazılamayan kayıt, iki eşzamanlı çağrı, eski politika revizyonu ve başarılı PIN sonrası yetki kontrolü bu sınıfın dışında repository/kontrolcü sorumluluğundadır.

## Uygulama tercihleri

İlk dört rezervasyon beklemesizdir. Beşinci de karşılaştırılabilir ancak yanlış/iptal/çökme halinde sonraki denemeden önce 30 saniye beklenir. Sonraki rezervasyonlar 60, 120, 240 ve en fazla 300 saniye bekleme oluşturur. Sayaç 9'da doyar. Doğru PIN sonrası sıfırlama ayrıca kalıcı yazılır; o yazı başarısızsa izin verilmemelidir. Bu eşikler ürünün uygulama tercihidir; bilimsel optimum veya bir standardın zorunlu sayıları diye sunulmaz.

Başarısız sonuç beklenmeden rezervasyon kalıcılaştırılır. Böylece uygulamayı karşılaştırma sırasında kapatmak ücretsiz deneme üretmez. Bunun bedeli, iptal veya teknik kesintinin de deneme tüketmesidir; UI bunu açıklamalı ve aynı doğrulamayı otomatik yeniden göndermemelidir.

## Saat ve yeniden başlatma

Girdi yalnız güvenilir boot kimliği ve monoton elapsed zamanıdır; kullanıcı tarafından değiştirilen duvar saati kullanılmaz. Aynı boot içinde yeniden açılan süreç kayıttaki son tarihi kullanır. Beklerken tekrar sorgu son tarihi uzatmaz.

Boot değişmişse veya monoton zaman geriye gitmişse, mevcut sayacın tam bekleme süresi yeni zamana taşınır. Boot kimliği değişikliği yalnız uptime küçülmesinden çıkarılmaz: yeni boot'un uptime değeri daha büyük olabilir. Reboot, kilidi kaldırmaz. Cihaz uzun süre kapalı kalsa bile bu muhafazakâr tercih yeniden bekleme isteyebilir. AndroidPinClock BOOT_COUNT ve elapsedRealtime okur. D073 ile S08'de uygulanan yedek: BOOT_COUNT yoksa 0 kullanılır ve yalnız uptime gerilemesi reboot'u gösterir. Yeni boot daha uzun uptime ile görülürse kalan bekleme atlanabilir; güvenilir boot kimliği garantisi bu cihazlarda yoktur. Bu, eski D048 sözleşmesinden belgelenmiş bir sapmadır; fiziksel cihaz kontrolü açık.

Negatif değerler, geçersiz sayaç, imkânsız büyük bekleme ve toplama taşmasına yol açan saat girdileri reddedilir. Hatalı kayıt `clear` çağrısına dönüşmemelidir. `clear` yalnız ilk kurulum veya başarılı ve güncel kimlik doğrulama sonrası kullanılabilir.

## Kontrol kapsamı

Sekiz JVM testi eşik/sınır anını, aynı boot süreç dönüşünü, büyük uptime'lı reboot'u, monoton rollback'i, yarıda kalan rezervasyonu, artan/sınırlı gecikmeyi, temiz başlangıcı ve bozuk/taşan değerleri kapsar. Bunlar gerçek diske yazmayı, Android boot adaptörünü, sürecin gerçekten öldürülmesini veya eşzamanlı denemeleri kanıtlamaz. Bu kontroller şifreli depo ve kontrolcü entegrasyonunda ayrıca yapılmalıdır.

S08–S12 güncellemesi: ParentalGuard Mutex altında rezervasyon → kalıcı yazma → arka planda KDF → kalıcı sayaç temizliği sırasını uygular. waitMs kalan süreyi verir. AndroidPinClockTest ve ParentalGuardTest bu yolları kapsar; cihaz testlerinin güncel hedefi yalnız 5558 ve tools/run-device-tests.sh'dir. Tarihî sekiz test, güncel toplam test sayısı değildir.
