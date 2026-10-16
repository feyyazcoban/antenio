# Yükleme ve kurtarma · 0.14.9 çalışma adayı

15 Ekim 2026. LoadingScreen, PilotApp içinden ayrıldı. 720dp azami genişlik, 24dp yüzey köşesi, 28sp işlem başlığı ve ayrı durum yüzeyi. Mevcut renkler, Roboto, Action ve LineIcon kullanılır. Yüzde bilinmediği için yüzde gösterilmez; durum semantiği Indeterminate ve Polite live region. Animasyon eklenmedi.

İptal yalnız IMPORTING sırasında görünür ve ilk odağı alır. SAVING, RESTORING, REMOVING ve RESETTING mevcut modele göre iptal düğmesi içermez. Silme metni bu kaynağa ait yerel kayıtları açıkça belirtir.

Kurtarma ekranı 72dp yerine ortak 32dp kenarı kullanır. Açıklama/eylemler 24dp köşeli, içeriğine göre yüksekliği değişen yüzeydedir; büyük metinde kayabilir. İlk odak yeniden dene; sıfırlama onayı ve kayıt koruma davranışı değişmedi.

## Kanıt

LoadingLayoutTest beş işlem durumunun başlık, durum, açıklama ve iptal sınırlarını kontrol eder; iptal callback ve belirsiz ilerleme semantiğini doğrular. SourceRecovery mevcut yeniden dene/iptal/odak/kayıtları koru/başarılı geri yükleme akışı ve SourceOperation iptal sonrası eski kaynağı koruma testi de normal,1.5,2.0 fontlarda geçti. 18 görüntü [inceleme klasöründe](../../design/reviews/loading); normal yükleme ve kurtarma, büyük yazı yükleme ve son kurtarma incelendi.

Sınırlama: Test durumları sentetiktir; gerçek sağlayıcı performansı, sınırsız uzun durum metni ve fiziksel TV doğrulaması değildir. Figma değişmedi. Boş ana sayfa/kütüphane/arama görsel kontrolü henüz tamamlanmadı; son dağıtım0.14.8, bu aday yayımlanmadı.
