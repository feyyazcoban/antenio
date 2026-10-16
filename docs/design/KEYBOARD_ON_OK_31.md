# Klavye yalnız OK ile açılır, Esc Geri'dir

16 Ekim 2026 · 0.14.31, düzeltme 0.14.32 · karar D058.

Kullanıcı isteği: metin alanları klavyeyi kendiliğinden açmasın; klavye, kullanıcı alana tıklayınca (kumandada OK) açılsın. 0.14.31'i denerken klavyenin hâlâ kendiliğinden açıldığını ve emülatörde Geri için bastığı Esc'nin çalışmadığını bildirdi; ikisi 0.14.32'de düzeltildi.

## Davranış

- Bir metin alanına odak gelmesi klavyeyi açmaz. Bu, ekranın kendisinin odak verdiği alanlar için de geçerli: Ara'nın arama alanı, "Bu bölümde ara" sonrası bölüm araması, Kaynak ekle/düzenle formu, rehber adresi ve saat dilimi araması.
- Odaktaki alanda **OK** klavyeyi açar. Kumandanın OK'i Enter olarak gelirse de açar; bilgisayar klavyesinin Enter'ı alanın kendi eylemini yapar (İleri/Ara). Dokunmatik/fare tıklaması denenmedi; TV'de yok.
- **Geri** klavyeyi kapatır, odak alanda kalır; bir kez daha Geri ekranın kendi Geri davranışını yapar.
- Klavyedeki "İleri" sonraki alana geçer, klavye açık kalır (yazmaya devam etmek için). "Ara" eylemi sonuçlara geçer.
- Odaktaki alandan Yukarı/Aşağı alanı bırakır. Bilgisayar klavyesiyle (emülatör, USB klavye) alana doğrudan yazılabilir.
- Yazma kipi (D054) klavye açılınca başlar; fiziksel klavyeyle doğrudan yazmak da başlatır.
- **Esc** (bilgisayar klavyesi) Geri gibi çalışır: açık klavyeyi kapatır, değilse ekranın Geri davranışını yapar (ana sayfada çıkış sorusu, soruda Vazgeç). Önceden Compose Esc'yi "odak grubundan çık" olarak kullanıyordu, Geri hiç gelmiyordu.

## Uygulama

- 0.14.31'deki yöntem (eski `BasicTextField(String)` API'sinin odakta istediği klavyeyi aynı karede gizlemek) bazı geçişlerde tutmadı. Kullanıcının denediği kaynak formunda klavye isteği gizleme isteğinden yaklaşık 0,3 sn sonra geldi (sistem günlüğü, `ImeTracker`).
- 0.14.32'de iki alan bileşeni (`EntryField`, `SearchField`) yeni `TextFieldState` API'sine taşındı: `KeyboardOptions(showKeyboardOnFocus = false)` bu API'de çalışır ve odakta klavye istenmez. Parola alanları `BasicSecureTextField` (gizli kip) kullanır. Dışarıdan gelen değer (Temizle, yeniden yükleme) alana yazılır; kullanıcının kendi yazısının geç yankıları yazıyı geri almaz (`rememberFieldInput`).
- Compose'un klavye denetleyicisi yeni alanın girişine ulaşmadığı için OK klavyeyi platformun giriş yöneticisinden açar, Ara/İleri eylemi de oradan kapatır (`FieldKeyboard`). Klavye açıkken Geri bu yeni alanda ekranın Geri işleyicisine kadar ulaşıp odağı alandan götürüyordu; alanın tuş işleyicisi artık o durumda yalnız klavyeyi kapatır.
- Yeni API metni bir kare sonra bildirir; kullanıcının her düzenlemesi, eski alanlardaki gibi anında bildirilsin diye bir giriş dönüşümüyle (`InputTransformation`) raporlanır.
- Esc, uygulamanın kökünde tuş önizlemesiyle yakalanır ve geri dağıtıcısına (bütün `BackHandler`'lar) iletilir; diyaloglar Esc'yi kendileri kapatır. Activity'nin tuş dağıtımını ezmek lint'te kısıtlı API hatası verdiği için kullanılmadı.
- Ortak kod `ui/Theme.kt` (`fieldKeyboardOptions`, `fieldKey`, `FieldKeyboard`, `rememberFieldInput`) ve `ui/PilotApp.kt` (`escapeAsBack`).

## Açık

- Fiziksel TV'nin klavyesiyle (TCL) deneme ve kullanıcı kabulü.
- Oynatıcıda odak yerel video görünümündeyken Esc denenmedi.
