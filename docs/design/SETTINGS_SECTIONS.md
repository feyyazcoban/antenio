# Ayarlar bölüm başlığı

0.14.4 tasarım önizlemesinde ana ayarlar listesinin bölüm adı sabit başlık alanındadır. Kumandada odaktaki satırın grubunu gösterir: Görünüm ve ses, Kaynaklar veya İzleme ve uygulama. Bu etiket listedeki tüm görünür satırların aynı gruba ait olduğu anlamına gelmez; odak değişince güncellenir. Gruplar arası boşluk ve mevcut tek liste korunur.

Kaydırma içindeki üç bölüm etiketi kaldırıldı; böylece bölüm adı kesilmez. Başlık erişilebilirlikte heading olarak işaretlidir. Aynı ayarlar ve sıra korunur; yeni menü, filtre veya sayfa eklenmez. Kapat/Geri, alt sayfadan eski satıra odak dönüşü ve kaydırma ipuçları korunur.

## Doğrulama

Normal,1,5ve2,0 yazıda dört ilgili akış geçti: dokuz satır boyunca aşağı/yukarı tur, sabit etiket konumu ve doğru grup, Hakkında'dan geri dönüş, silme iptali, yenileme hatası ve önceki katalog/ayar geri bildirimi. Odaktaki satır bütünüyle viewport içinde ölçüldü. Diğer odaksız satırların kaydırma kenarında kırpılması normal liste davranışıdır; bütün liste aynı anda görünür iddiası yoktur. Fiziksel TV ve Figma onayı değildir.

| Yazı ölçeği | İlk ayarlar | Kaynaklar odağı | Son ayarlar | Hata |
| --- | --- | --- | --- | --- |
| 1.0 | [Aç](../../design/reviews/settings-sections/font-1.0/section-appearance.png) | [Aç](../../design/reviews/settings-sections/font-1.0/section-sources.png) | [Aç](../../design/reviews/settings-sections/font-1.0/section-playback.png) | [Aç](../../design/reviews/settings-sections/font-1.0/settings-warning.png) |
| 1.5 | [Aç](../../design/reviews/settings-sections/font-1.5/section-appearance.png) | [Aç](../../design/reviews/settings-sections/font-1.5/section-sources.png) | [Aç](../../design/reviews/settings-sections/font-1.5/section-playback.png) | [Aç](../../design/reviews/settings-sections/font-1.5/settings-warning.png) |
| 2.0 | [Aç](../../design/reviews/settings-sections/font-2.0/section-appearance.png) | [Aç](../../design/reviews/settings-sections/font-2.0/section-sources.png) | [Aç](../../design/reviews/settings-sections/font-2.0/section-playback.png) | [Aç](../../design/reviews/settings-sections/font-2.0/settings-warning.png) |

[QA](../project/native-settings-sections-qa.json). Görseller yalnız yerel sentetik test hesabıyla alındı; gerçek sağlayıcı hesabı kullanılmadı.
