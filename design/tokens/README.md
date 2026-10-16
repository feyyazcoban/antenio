# Tasarım değişkenleri

figma-variables.snapshot.json, 13 Ekim 2026 tarihinde kişisel hesaptan okunan NEXTV dosyasının **401 değişken ve 9 koleksiyonluk tarihli kaydıdır**. Figma ile otomatik eşitlenmez ve üretim koduna hazır DTCG paketi değildir.

- collectionId, variable id ve mode id kaynak izlenebilirliği için korunur.
- VARIABLE_ALIAS zincirleri korunur; Light/Dark modları karıştırılmamalıdır.
- Renkler sRGB 0-1 bileşenleri ve varsa alfa taşır.
- FLOAT alanlarının birimi adından/sözleşmesinden anlaşılmalıdır; hepsi dp değildir. Örneğin süre ms, scale birimsizdir.
- valuesByMode içindeki uzun ondalıklar ham kayıt hassasiyetidir; belgede kısa biçim gösterilebilir.
- layout/rows/gutter değeri 492. Anlamı ve tüketicileri incelenmeden normal boşluk tokenı gibi kullanılmaz.
- Hareket ve renk rehberlerindeki öneriler henüz mevcut Figma token değerlerini değiştirmedi.

Figma değiştiğinde snapshot yeniden alınır, fark incelenir ve tarih güncellenir. Yeni üretim token dönüşümü önce ayrı bir görev olarak tanımlanmalıdır.
