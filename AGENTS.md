# Ortak çalışma talimatları

Bu dosya Codex ve Claude Code için aynı çalışma sözleşmesidir. CLAUDE.md buraya yönlendirir; ayrı bir kural seti oluşturmayın. Kullanıcının güncel talimatlarını ve ortamın üst düzey kurallarını izleyin.

## İşe başlarken

1. README.md, docs/project/STATUS.md, docs/project/HANDOFF.md ve docs/project/WORKFLOW.md dosyalarını okuyun. Güncel kayıt en üsttedir; alttaki eski sonraki adımlar tarihçedir.
2. İlgili işi docs/project/BACKLOG.md içinde bulun; kapsam ve bağımlılıklarını kontrol edin.
3. docs/product/PRD.md ile docs/decisions/DECISIONS.md dosyalarını referans alın.
4. Tasarım işinde docs/design/FIGMA.md, SCREENS.md, MOTION.md ve COLOR.md dosyalarını okuyun.
5. Mevcut değişiklikleri inceleyin. Başka aracın veya kullanıcının işini geri almayın.

## Kalıcı ürün kararları

- İlk sürüm Android TV / Google TV; canlı TV + filmler + diziler birlikte.
- M3U bağlantısı ve Xtream Codes hesabı desteklenir.
- TV UI kitini ve mevcut Figma bileşenlerini temel alın.
- LG webOS, Samsung Tizen, Android mobil ve iOS sonraki fazlardır.
- Ürün adı Antenio'dur (D115, kullanıcı kararı 22 Ekim 2026; D066 ve D114'ün yerine geçer). Uygulama 0.15.0'dan beri Antenio adıyla derlenir (BKL-045); logo, tasarımcı logosu gelene kadar NEXTV piksel ızgarasında çizilmiş geçici "ANTENIO" yazısıdır. Kalıcı paket adı `app.antenio.tv`'dir (D119, kullanıcı kararı 23 Ekim 2026); 0.15.0'dan beri bu kimlikle derlenir; eski `dev.iptvplayer.tv.pilot` kurulumu ayrı bir uygulamadır ve verisi taşınmaz. Marka vekili araştırması yayından önce şarttır.
- Takvim, ücretlendirme ve minimum cihaz desteği onaylanmış sayılmaz.

## Tek kaynak kuralı

- Ürün kapsamı: docs/product/PRD.md.
- Ekran kimliği ve davranışı: docs/design/SCREENS.md.
- Görsel sistem: Figma ve docs/design altındaki sözleşmeler.
- Figma node bağlantıları: design/figma/manifest.json.
- Güncel iş durumu: STATUS.md; bir sonraki devralma: HANDOFF.md.
- Snapshot JSON dosyası Figma'nın tarihli kaydıdır, canlı senkronizasyon veya üretim token paketi değildir.
- Bir karar değişirse karar kaydına nedenini yazın; eski kararı sessizce yeniden yorumlamayın.

## Uygulama ve tasarım disiplini

- Ekran tasarlamadan önce mevcut bileşeni arayın. İsim benzerliğini kalite veya davranış kanıtı saymayın.
- Odak, seçili ve basılı durumlarını ayırın; OK, Back ve ilk odağı tanımlayın.
- Her P0 ekran için yükleme, boş, hata, eksik veri ve erişim kısıtı durumlarını ele alın.
- Kod aşamasında sürümleri o günkü resmî kaynaklardan doğrulayın; çalıştırılmamış komutları başarılı göstermeyin.
- Dosya yollarında platforma özgü kullanıcı adları kullanmayın.
- Gereksiz paket, örnek uygulama ve ikinci tasarım sistemi eklemeyin.
- Değişiklikle ilgili anlamlı kontrolleri yapın; sadece doküman değişikliği için uygulama testi icat etmeyin.

## Hassas veri

Gerçek IPTV URL'si, kullanıcı adı, parola, erişim tokenı ve kimlik içeren medya adreslerini commit etmeyin; Figma'ya, ekran görüntüsüne veya analitik kaydına koymayın. Ham inceleme verisi .private/ ya da proje dışı geçici alanda kalır. Paylaşılacak örnekleri sentetik kimliklerle üretin. Kaynak testi tek oynatma oturumu üzerinden yapılır; eşzamanlı bağlantı limiti bilinmiyorsa paralel yayın açılmaz.

Geliştiricinin gerçek adı ve soyadı hiçbir belgeye, koda, sayfaya, mağaza metnine, tasarıma ya da commit mesajına yazılmaz (kullanıcı kararı, 23 Ekim 2026). Kamuya dönük yerlerde "Antenio" ve destek@antenio.app kullanılır. Google Play kişisel hesabı yasal adı gösterdiği için hesap türü kararı LAUNCH_AUDIT E7'dedir.

## Birlikte çalışma ve devir

Aynı dosyayı iki araç aynı anda değiştirmesin. Kullanıcı gerçekten eşzamanlı çalışma isterse iş sınırlarını ve ayrı checkout/branch kullanımını yazılı belirleyin. Sırayla çalışırken yeni sohbet açmak kararları sıfırlamaz.

Her anlamlı iş sonunda:
- STATUS: tamamlanan iş, mevcut aşama, engel.
- BACKLOG: yalnızca kabul ölçütü sağlanan işi done yapın.
- HANDOFF: değişen dosyalar/node kimlikleri, yapılan kontroller, kalan risk ve tam sonraki adım.
- Figma değiştiyse manifest; yeni ürün/teknik karar varsa DECISIONS.
- Kullanıcıya kısa sonuç ve varsa doğrulanamayan noktayı bildirin.

Bu dosya tek başına Figma yetkisi veya otomatik senkronizasyon sağlamaz. Her araç kendi bağlı hesabını doğrular. Bu projede kullanıcının seçtiği hesap kişisel fyzcbn hesabıdır.
