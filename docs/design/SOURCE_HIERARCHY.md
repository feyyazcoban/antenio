# Kaynak kartlarında bilgi sırası

15 Ekim 2026 · 0.14.17 native önizleme. Figma eşlemesi ve kullanıcı görsel kabulü bekler.

Kaynak yönetimi kartlarında kaynak numarası ve bağlantı türü üstte, içerik sayısı aynı satırın sağındadır. Bu iki bilgi 11/16 sp kullanır. Kaynak adı sonraki satırda 18/24 sp Medium; son güncelleme ayrı yardımcı satırdır. Ad tek satırda ortadan kısaltılır: benzer başlangıçlı adların sonları da görülebilir. Tam ad Bilgiler içindeki kumandayla kaydırılabilen okuyucuda korunur. Çok uzun adların kartta tamamını göstermeye çalışarak düğmeleri aşağı itmeyiz.

Kart içindeki bir düğme odaktayken kartın 1 dp sınırı mint olur; odaksız sınır ortak ayırıcı rengini kullanır. Düğmenin kendi odak dolgu ve çizgisi korunur. Kart sınırı grubun bağlamını, düğme odağı yapılacak işlemi gösterir. Kart başlığına ek bir odak durağı eklenmedi.

Bilgiler → Yenile → Düzenle → Kaldır sırası, düzenlemeden dönülen düğme, kaldırma onayının ilk odağının Vazgeç olması ve sabit ekran başlığı korunur. Uzun hata bildiriminin tamamı Bilgiler ekranından okunabilir. Hesap adresi veya parola bu kartlarda gösterilmez.

İlgili kontrol: SourceLayoutFlowTest.managerKeepsHeaderAlignedAndAllRemoteActionsVisible, 16 uzun adlı kaynak boyunca başlık hizasını, bütün kartın ve dört eylemin görünürlüğünü gerçek D-pad hareketleriyle denetler. deepManagerRestoresEditAndRemovalCancelFocus düzenleme/iptal/bilgi dönüşünü doğrular. Normal, 1,5 ve 2,0 yazı boyutları ayrı çalıştırılır.
