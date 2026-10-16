# Kurulum yerleşimi · 0.14.8

15 Ekim 2026. Ortak 32dp dış kenar korunur. Sol tarafta kısa karşılama; sağ tarafta 24dp köşeli form yüzeyi. Başlık ve ana eylem sabit, alanlar arada kayar. Kaynak türleri yatay kayan satırdadır. Büyük yazıda tanıtım metni kısalır; giriş değerlerinin font ölçeği korunur.

Mevcut EntryField, Action, renkler ve CardVisibility kullanılır. Kuruluma özel SetupEntry yalnız alan ve etiketin birlikte görünür alana taşınmasını sağlar. Kaynak işleme ve kayıt modeli değişmedi. Hata mesajı ana eylemin üstündedir.

## Doğrulama

SetupLayoutTest normal, 1.5 ve 2.0 yazı ölçeklerinde geçti: beş Xtream alanına aşağı tuşuyla erişim, ana eyleme geçiş, M3U seçimi, boş adres doğrulaması, hata ve ana düğmenin görünürlüğü. Klavye açıkken yön tuşları klavyeyi yönetir; test alanlar arası gezinmeden önce sistem klavyesini kapatır. Bu test klavyenin tüm etkileşimlerini doğrulamaz.

İlk görüntüde son alanın alt çerçevesi kesiliyordu. Alan+etiket ortak görünürlük yardımcısına bağlandı, test ekran yerine form alanının sınırını kontrol eder. Düzeltilmiş normal ve 2.0 son alan görüntüleri incelendi. İlk testin klavyede kalan odağı uygulama hatası olarak raporlanmaz; başarısız log özel çalışma alanında saklandı.

12 görüntü: [normal kurulum](../../design/reviews/setup/font-1.0/xtream.png), [büyük yazıda son alan](../../design/reviews/setup/font-2.0/last-field.png). [QA](../project/native-setup-layout-qa.json).

Yükleme, boş ve kurtarma ekranlarının görsel incelemesi sonraki iş. Figma eşlemesi ve fiziksel TV görsel kabulü bekliyor.
