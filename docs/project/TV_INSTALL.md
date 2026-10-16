# 65U95A · İlk APK kurulumu

**15 Ekim açılış kontrolü:** Yeni sürümün süreci açıldı ve aynı PID çalışır kaldı; TV ekranı uyuduğu için görünür katalog doğrulanamadı. Bir şifreli katalog dosyası mevcut. Ekran uyandırılmadı, yayın başlatılmadı. [QA](physical-tv-22-opening-qa.json).

**15 Ekim 2026:** Fiziksel TV, mevcut kurulum üzerine 0.14.22/code36 sürümüne güncellendi. Kurulum Success ve paket sürümü cihazdan doğrulandı. Veri temizleme yapılmadı; uygulama/katalog açılışı ve medya testi bu tur yapılmadı. Aşağıdaki kurulum notları tarihçedir.

**Güncel kurulum · 14 Ekim 2026:** Kullanıcı isteğiyle fiziksel TV 0.14.1-design-preview/code15 sürümüne veri silmeden güncellendi. İmza ve SHA-256 doğrulandı; uygulama açıldı, kayıtlı hesapla katalog yenilemesi başladı. Aşağıdaki eski sürüm açıklamaları ilk kurulum tarihçesidir. Rehber artık mevcut; PIN/QR hâlâ açık kapsamdır.

Uygulama **IPTV Player · Pilot**, yeni sürüm **0.12.0**. Dosya: teslim klasöründeki `IPTV_Player_TV_Pilot_0.12.0.apk`. Hesap APK'ye eklenmedi. Mevcut TV'de 0.2.0 ve kullanıcının kayıtlı hesabı var; güncelleme yeniden hesap girişi istememeli. 0.12 henüz TV'ye kurulmadı.

## USB bellekle

1. APK'yi bilgisayardan USB belleğe kopyala; belleği TV'ye tak.
2. TV'deki dosya yöneticisinden APK'yi aç. Yoksa TV Play Store'undan APK açabilen bir dosya yöneticisi gerekir.
3. TV sorarsa kullandığın dosya yöneticisine **Bilinmeyen uygulama yükleme** iznini ver; kurulumu tamamla.
4. Uygulamalar bölümünden **IPTV Player · Pilot**'u aç.

Menü isimleri TV yazılımına göre değişebilir; bu TV Android 12/API31 olarak okundu. USB bellek yöntemi geliştirici seçenekleri gerektirmez.

## Bilgisayardan test kurulumu

Geliştirme sırasında ADB ile güncelleme/ölçüm yapılabilir. TV ve bilgisayar aynı yerel ağda olmalı. Ayarlar → Sistem → Hakkında altında **Android TV OS derlemesi / Derleme numarası** satırına yedi kez basmak geliştirici seçeneklerini açar; menü yolu yazılıma göre değişebilir. [Android geliştirici seçenekleri](https://developer.android.com/studio/debug/dev-options).

**Kablosuz hata ayıklama** varsa TV'nin gösterdiği eşleştirme adresi/port kullanılır; eşleştirme ve bağlantı portu farklı olabilir. Yalnız USB/ağ hata ayıklama varsa yöntem o menüye göre belirlenir; 5555 portunun her TV'de açık olduğu varsayılmaz. [ADB bağlantı ve kurulum rehberi](https://developer.android.com/tools/adb).

Bağlantı kurulduktan sonra geliştirici tarafında:

```text
adb devices
adb -s TV_BAGLANTI_KIMLIGI install -r IPTV_Player_TV_Pilot_0.12.0.apk
adb -s TV_BAGLANTI_KIMLIGI shell am start -n dev.iptvplayer.tv.pilot/dev.iptvplayer.tv.MainActivity
```

Bağlantı kimliği `adb devices` çıktısından alınır; TV'deki bilgisayar erişim istemini kullanıcı onaylar. 14 Ekim 2026: Fiziksel 65U95A kablosuz bağlandı, 0.2.0 kuruldu ve kullanıcının hesabı eklendi. Kurulum için yeniden USB gerekmedi. Kullanıcı TV’sinde veri sıfırlayan testler çalıştırılmamalıdır.

## İlk deneme

1. Kaynağı ekle; canlı/film/dizi sayıları ve kapakları kontrol et.
2. Canlı kanal ve önceki/sonraki kanalı dene. İlk Geri kontrolleri gizler, ikinci Geri katalog kartına döner.
3. Film aç, 10 saniye ilerlet, geri dön ve **Devam et** ile aç.
4. Dizi sezon/bölümünü seç, sonraki bölüme geç.
5. **Ses / Altyazı** panelinde bulunan dilleri değiştir; olmayan seçenek üretilmez.
6. Kapat/aç; kaynak, favori ve ilerlemenin kaldığını kontrol et.

Sorunda içerik türünü, kısa hata metnini ve TV Android sürümünü kaydet. Kimlikli yayın adresini veya parolayı ekran görüntüsüne koyma. HDR/HEVC/5.1 ve kumanda akıcılığı fiziksel testte ayrıca ölçülecek.

EPG, PIN ve QR kurulum henüz yok. Otomatik sonraki bölüm 0.7 ile eklendi; Ayarlar’dan kapatılabilir. [Pilot kapsamı](../../apps/android-tv/README.md).
