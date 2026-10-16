# PIN doğrulayıcı temeli

15 Ekim 2026 · SAFE01/BKL-028 · Henüz uygulamaya bağlı değil.

`PinCredential` yalnız PIN karşılaştırma ilkelidir. Kaynak/kategori/kanal erişimini açmaz, izin üretmez, kayıt dosyası yazmaz. SettingsScreen içindeki PIN yok açıklaması korunur.

## Biçim ve seçim

V1 kayıt tam 52 bayttır: ASCII PIN + sürüm baytı 1 (4 bayt), SecureRandom ile üretilen 16 bayt tuz, 32 bayt PBKDF2-HMAC-SHA256 çıktısı. V1 iş yükü sabit 600.000 iterasyondur; dış kayıttan sınırsız iş yükü parametresi kabul edilmez. Parametre değişimi yeni kayıt sürümü/migrasyon gerektirir. Bilinmeyen sürüm, eksik/fazla bayt veya bozuk başlık hata verir; çağıran bunu koruma kapalı diye yorumlamamalıdır.

Android bu algoritmayı API26'dan itibaren sağlıyor; projenin minSdk26 sınırıyla uyumludur. Yeni kripto bağımlılığı eklemeden platform sağlayıcısını kullanmak uygulama tercihidir. [Android SecretKeyFactory](https://developer.android.com/reference/javax/crypto/SecretKeyFactory).

OWASP genel olarak Argon2id, yoksa scrypt önerir; PBKDF2-HMAC-SHA256 için 600.000+ iterasyonu FIPS bağlamında belirtir. Buradaki PBKDF2 seçimi Android/JCA kullanılabilirliğiyle ilgili bir ödünleşimdir; uygulamanın FIPS gereksinimi veya sertifikası olduğu anlamına gelmez. [OWASP Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

## Çağıranın sorumluluğu

- `create` ve `matches` verilen CharArray'i finally içinde sıfırlar. PBEKeySpec kopyası da temizlenir; geçici karşılaştırma çıktısı silinir. Yönetilen bellek/JCA içindeki bütün kopyaların silindiği garanti edilmez. UI kendi metin durumunu ayrıca temizlemeli; PIN String/log/analytics içine taşınmamalıdır.
- İlkel 4–8 ASCII rakam kabul eder; boşluk/Unicode rakam normalize edilmez. Bu bir UI uzunluğu kararı değildir; oluşturma ekranının tek ve tutarlı uzunluğu entegrasyonda seçilecek.
- KDF ana thread dışında, tek doğrulama işi olarak çalıştırılmalı. Gerçek TV gecikmesi henüz ölçülmedi; UI yükleme ve iptal davranışı gereklidir.
- `encode` hassas doğrulayıcıdır. Diskte kimliği doğrulanmış, Keystore anahtarlı şifreleme içinde saklanmalıdır. Dışarı verilen kopyayı kullanan katman temizlemelidir. `decode` savunmacı kopya alır; yapı geçerli olması kaydın özgünlüğünü kanıtlamaz.
- Karşılaştırma MessageDigest.isEqual kullanır. PIN biçim kontrolü sabit süre iddiası taşımaz. Kripto sağlayıcı hatası erişim iznine dönüştürülmemelidir.
- Kısa PIN düşük entropilidir. Tek başına hash çevrimdışı tahmin saldırısını ortadan kaldırmaz. Kalıcı yanlış deneme sınırı, atomik PIN/politika durumu, bozuk kayıt ve unutulan PIN davranışı çözülmeden özellik açılmamalıdır.

## Açık entegrasyon kapıları

Keystore ile şifreli/atomik depo; reboot/saat değişimini ele alan kalıcı gecikme; doğru PIN sonrası güncel hedef/politika revizyonu doğrulaması; tüm arama/favori/geçmiş/oynatma girişleri; PIN oluşturma/değiştirme/iptal akışı. Cihaz sahibi uygulama verilerini silebilir; bu yerel kontrol root veya uygulama kaldırmaya karşı koruma vaat etmez.

Testler sentetik girdiyle bağımsız Python hashlib referansını, yanlış PIN'i, giriş temizliğini, rastgele tuzu, savunmacı kopyaları ve bozuk kayıtları kapsar. Android sağlayıcı davranışı, cihaz gecikmesi ve kalıcı güvenlik henüz bu testlerin kapsamı değildir.
