# Список аудиофайлов

Все файлы кладутся в папку `app/src/main/assets/audio/` с подпапками ниже.
Формат: **MP3**, моно, 44.1 кГц, 64–128 кбит/с, без тишины в начале и конце (≤ 0.2 с).
Запись — голос живого чтеца (не синтез речи). Громкость выровнять по всем файлам.

Если файла нет, приложение не упадёт: покажет подсказку «Аудио ещё не добавлено».

Список создан скриптом `tools/audio_manifest.py`. Чтобы добавить слово — допишите его в скрипт и запустите заново.

## Сводка

| Папка | Файлов |
|---|---|
| `letters/` | 28 |
| `syllables/` | 195 |
| `madd/` | 84 |
| `words/` | 51 |
| `ui/` | 3 |
| **Всего** | **361** |

Аудио Корана (по аятам) сюда **не** входит — оно скачивается из интернета (EveryAyah.com) и кэшируется.
Примеры таджвида (модуль 8) тоже используют это аудио.

## 1. Названия букв — `letters/` (модуль 1)

| Файл | Текст | Что произнести |
|---|---|---|
| `letters/alif.mp3` | ا | название буквы: «алиф» |
| `letters/ba.mp3` | ب | название буквы: «ба» |
| `letters/ta.mp3` | ت | название буквы: «та» |
| `letters/tha.mp3` | ث | название буквы: «са» |
| `letters/jim.mp3` | ج | название буквы: «джим» |
| `letters/hha.mp3` | ح | название буквы: «ха» |
| `letters/kha.mp3` | خ | название буквы: «хо» |
| `letters/dal.mp3` | د | название буквы: «даль» |
| `letters/dhal.mp3` | ذ | название буквы: «заль» |
| `letters/ra.mp3` | ر | название буквы: «ра» |
| `letters/zay.mp3` | ز | название буквы: «зай» |
| `letters/sin.mp3` | س | название буквы: «син» |
| `letters/shin.mp3` | ش | название буквы: «шин» |
| `letters/sad.mp3` | ص | название буквы: «сод» |
| `letters/dad.mp3` | ض | название буквы: «дод» |
| `letters/tta.mp3` | ط | название буквы: «то» |
| `letters/zza.mp3` | ظ | название буквы: «зо» |
| `letters/ayn.mp3` | ع | название буквы: «айн» |
| `letters/ghayn.mp3` | غ | название буквы: «гойн» |
| `letters/fa.mp3` | ف | название буквы: «фа» |
| `letters/qaf.mp3` | ق | название буквы: «коф» |
| `letters/kaf.mp3` | ك | название буквы: «каф» |
| `letters/lam.mp3` | ل | название буквы: «лям» |
| `letters/mim.mp3` | م | название буквы: «мим» |
| `letters/nun.mp3` | ن | название буквы: «нун» |
| `letters/ha.mp3` | ه | название буквы: «ха» |
| `letters/waw.mp3` | و | название буквы: «вав» |
| `letters/ya.mp3` | ي | название буквы: «йа» |

## 2. Слоги с огласовками — `syllables/` (модуль 4)

| Файл | Текст | Что произнести |
|---|---|---|
| `syllables/alif_fatha.mp3` | أَ | слог «а» |
| `syllables/alif_kasra.mp3` | إِ | слог «и» |
| `syllables/alif_damma.mp3` | أُ | слог «у» |
| `syllables/ba_fatha.mp3` | بَ | слог «ба» |
| `syllables/ba_kasra.mp3` | بِ | слог «би» |
| `syllables/ba_damma.mp3` | بُ | слог «бу» |
| `syllables/ta_fatha.mp3` | تَ | слог «та» |
| `syllables/ta_kasra.mp3` | تِ | слог «ти» |
| `syllables/ta_damma.mp3` | تُ | слог «ту» |
| `syllables/tha_fatha.mp3` | ثَ | слог «с̱а» |
| `syllables/tha_kasra.mp3` | ثِ | слог «с̱и» |
| `syllables/tha_damma.mp3` | ثُ | слог «с̱у» |
| `syllables/jim_fatha.mp3` | جَ | слог «джа» |
| `syllables/jim_kasra.mp3` | جِ | слог «джи» |
| `syllables/jim_damma.mp3` | جُ | слог «джу» |
| `syllables/hha_fatha.mp3` | حَ | слог «ḥа» |
| `syllables/hha_kasra.mp3` | حِ | слог «ḥи» |
| `syllables/hha_damma.mp3` | حُ | слог «ḥу» |
| `syllables/kha_fatha.mp3` | خَ | слог «ха» |
| `syllables/kha_kasra.mp3` | خِ | слог «хи» |
| `syllables/kha_damma.mp3` | خُ | слог «ху» |
| `syllables/dal_fatha.mp3` | دَ | слог «да» |
| `syllables/dal_kasra.mp3` | دِ | слог «ди» |
| `syllables/dal_damma.mp3` | دُ | слог «ду» |
| `syllables/dhal_fatha.mp3` | ذَ | слог «з̱а» |
| `syllables/dhal_kasra.mp3` | ذِ | слог «з̱и» |
| `syllables/dhal_damma.mp3` | ذُ | слог «з̱у» |
| `syllables/ra_fatha.mp3` | رَ | слог «ра» |
| `syllables/ra_kasra.mp3` | رِ | слог «ри» |
| `syllables/ra_damma.mp3` | رُ | слог «ру» |
| `syllables/zay_fatha.mp3` | زَ | слог «за» |
| `syllables/zay_kasra.mp3` | زِ | слог «зи» |
| `syllables/zay_damma.mp3` | زُ | слог «зу» |
| `syllables/sin_fatha.mp3` | سَ | слог «са» |
| `syllables/sin_kasra.mp3` | سِ | слог «си» |
| `syllables/sin_damma.mp3` | سُ | слог «су» |
| `syllables/shin_fatha.mp3` | شَ | слог «ша» |
| `syllables/shin_kasra.mp3` | شِ | слог «ши» |
| `syllables/shin_damma.mp3` | شُ | слог «шу» |
| `syllables/sad_fatha.mp3` | صَ | слог «ṣа» |
| `syllables/sad_kasra.mp3` | صِ | слог «ṣи» |
| `syllables/sad_damma.mp3` | صُ | слог «ṣу» |
| `syllables/dad_fatha.mp3` | ضَ | слог «ḍа» |
| `syllables/dad_kasra.mp3` | ضِ | слог «ḍи» |
| `syllables/dad_damma.mp3` | ضُ | слог «ḍу» |
| `syllables/tta_fatha.mp3` | طَ | слог «ṭа» |
| `syllables/tta_kasra.mp3` | طِ | слог «ṭи» |
| `syllables/tta_damma.mp3` | طُ | слог «ṭу» |
| `syllables/zza_fatha.mp3` | ظَ | слог «ẓа» |
| `syllables/zza_kasra.mp3` | ظِ | слог «ẓи» |
| `syllables/zza_damma.mp3` | ظُ | слог «ẓу» |
| `syllables/ayn_fatha.mp3` | عَ | слог «ʿа» |
| `syllables/ayn_kasra.mp3` | عِ | слог «ʿи» |
| `syllables/ayn_damma.mp3` | عُ | слог «ʿу» |
| `syllables/ghayn_fatha.mp3` | غَ | слог «га» |
| `syllables/ghayn_kasra.mp3` | غِ | слог «ги» |
| `syllables/ghayn_damma.mp3` | غُ | слог «гу» |
| `syllables/fa_fatha.mp3` | فَ | слог «фа» |
| `syllables/fa_kasra.mp3` | فِ | слог «фи» |
| `syllables/fa_damma.mp3` | فُ | слог «фу» |
| `syllables/qaf_fatha.mp3` | قَ | слог «к̣а» |
| `syllables/qaf_kasra.mp3` | قِ | слог «к̣и» |
| `syllables/qaf_damma.mp3` | قُ | слог «к̣у» |
| `syllables/kaf_fatha.mp3` | كَ | слог «ка» |
| `syllables/kaf_kasra.mp3` | كِ | слог «ки» |
| `syllables/kaf_damma.mp3` | كُ | слог «ку» |
| `syllables/lam_fatha.mp3` | لَ | слог «ла» |
| `syllables/lam_kasra.mp3` | لِ | слог «ли» |
| `syllables/lam_damma.mp3` | لُ | слог «лу» |
| `syllables/mim_fatha.mp3` | مَ | слог «ма» |
| `syllables/mim_kasra.mp3` | مِ | слог «ми» |
| `syllables/mim_damma.mp3` | مُ | слог «му» |
| `syllables/nun_fatha.mp3` | نَ | слог «на» |
| `syllables/nun_kasra.mp3` | نِ | слог «ни» |
| `syllables/nun_damma.mp3` | نُ | слог «ну» |
| `syllables/ha_fatha.mp3` | هَ | слог «hа» |
| `syllables/ha_kasra.mp3` | هِ | слог «hи» |
| `syllables/ha_damma.mp3` | هُ | слог «hу» |
| `syllables/waw_fatha.mp3` | وَ | слог «ва» |
| `syllables/waw_kasra.mp3` | وِ | слог «ви» |
| `syllables/waw_damma.mp3` | وُ | слог «ву» |
| `syllables/ya_fatha.mp3` | يَ | слог «йа» |
| `syllables/ya_kasra.mp3` | يِ | слог «йи» |
| `syllables/ya_damma.mp3` | يُ | слог «йу» |
| `syllables/ba_sukun.mp3` | أَبْ | «аб» (согласный с сукуном) |
| `syllables/ta_sukun.mp3` | أَتْ | «ат» (согласный с сукуном) |
| `syllables/tha_sukun.mp3` | أَثْ | «ас̱» (согласный с сукуном) |
| `syllables/jim_sukun.mp3` | أَجْ | «адж» (согласный с сукуном) |
| `syllables/hha_sukun.mp3` | أَحْ | «аḥ» (согласный с сукуном) |
| `syllables/kha_sukun.mp3` | أَخْ | «ах» (согласный с сукуном) |
| `syllables/dal_sukun.mp3` | أَدْ | «ад» (согласный с сукуном) |
| `syllables/dhal_sukun.mp3` | أَذْ | «аз̱» (согласный с сукуном) |
| `syllables/ra_sukun.mp3` | أَرْ | «ар» (согласный с сукуном) |
| `syllables/zay_sukun.mp3` | أَزْ | «аз» (согласный с сукуном) |
| `syllables/sin_sukun.mp3` | أَسْ | «ас» (согласный с сукуном) |
| `syllables/shin_sukun.mp3` | أَشْ | «аш» (согласный с сукуном) |
| `syllables/sad_sukun.mp3` | أَصْ | «аṣ» (согласный с сукуном) |
| `syllables/dad_sukun.mp3` | أَضْ | «аḍ» (согласный с сукуном) |
| `syllables/tta_sukun.mp3` | أَطْ | «аṭ» (согласный с сукуном) |
| `syllables/zza_sukun.mp3` | أَظْ | «аẓ» (согласный с сукуном) |
| `syllables/ayn_sukun.mp3` | أَعْ | «аʿ» (согласный с сукуном) |
| `syllables/ghayn_sukun.mp3` | أَغْ | «аг» (согласный с сукуном) |
| `syllables/fa_sukun.mp3` | أَفْ | «аф» (согласный с сукуном) |
| `syllables/qaf_sukun.mp3` | أَقْ | «ак̣» (согласный с сукуном) |
| `syllables/kaf_sukun.mp3` | أَكْ | «ак» (согласный с сукуном) |
| `syllables/lam_sukun.mp3` | أَلْ | «ал» (согласный с сукуном) |
| `syllables/mim_sukun.mp3` | أَمْ | «ам» (согласный с сукуном) |
| `syllables/nun_sukun.mp3` | أَنْ | «ан» (согласный с сукуном) |
| `syllables/ha_sukun.mp3` | أَهْ | «аh» (согласный с сукуном) |
| `syllables/waw_sukun.mp3` | أَوْ | «ав» (согласный с сукуном) |
| `syllables/ya_sukun.mp3` | أَيْ | «ай» (согласный с сукуном) |
| `syllables/alif_fathatan.mp3` | أً | танвин «ан» |
| `syllables/alif_kasratan.mp3` | إٍ | танвин «ин» |
| `syllables/alif_dammatan.mp3` | أٌ | танвин «ун» |
| `syllables/ba_fathatan.mp3` | بًا | танвин «бан» |
| `syllables/ba_kasratan.mp3` | بٍ | танвин «бин» |
| `syllables/ba_dammatan.mp3` | بٌ | танвин «бун» |
| `syllables/ta_fathatan.mp3` | تًا | танвин «тан» |
| `syllables/ta_kasratan.mp3` | تٍ | танвин «тин» |
| `syllables/ta_dammatan.mp3` | تٌ | танвин «тун» |
| `syllables/tha_fathatan.mp3` | ثًا | танвин «с̱ан» |
| `syllables/tha_kasratan.mp3` | ثٍ | танвин «с̱ин» |
| `syllables/tha_dammatan.mp3` | ثٌ | танвин «с̱ун» |
| `syllables/jim_fathatan.mp3` | جًا | танвин «джан» |
| `syllables/jim_kasratan.mp3` | جٍ | танвин «джин» |
| `syllables/jim_dammatan.mp3` | جٌ | танвин «джун» |
| `syllables/hha_fathatan.mp3` | حًا | танвин «ḥан» |
| `syllables/hha_kasratan.mp3` | حٍ | танвин «ḥин» |
| `syllables/hha_dammatan.mp3` | حٌ | танвин «ḥун» |
| `syllables/kha_fathatan.mp3` | خًا | танвин «хан» |
| `syllables/kha_kasratan.mp3` | خٍ | танвин «хин» |
| `syllables/kha_dammatan.mp3` | خٌ | танвин «хун» |
| `syllables/dal_fathatan.mp3` | دًا | танвин «дан» |
| `syllables/dal_kasratan.mp3` | دٍ | танвин «дин» |
| `syllables/dal_dammatan.mp3` | دٌ | танвин «дун» |
| `syllables/dhal_fathatan.mp3` | ذًا | танвин «з̱ан» |
| `syllables/dhal_kasratan.mp3` | ذٍ | танвин «з̱ин» |
| `syllables/dhal_dammatan.mp3` | ذٌ | танвин «з̱ун» |
| `syllables/ra_fathatan.mp3` | رًا | танвин «ран» |
| `syllables/ra_kasratan.mp3` | رٍ | танвин «рин» |
| `syllables/ra_dammatan.mp3` | رٌ | танвин «рун» |
| `syllables/zay_fathatan.mp3` | زًا | танвин «зан» |
| `syllables/zay_kasratan.mp3` | زٍ | танвин «зин» |
| `syllables/zay_dammatan.mp3` | زٌ | танвин «зун» |
| `syllables/sin_fathatan.mp3` | سًا | танвин «сан» |
| `syllables/sin_kasratan.mp3` | سٍ | танвин «син» |
| `syllables/sin_dammatan.mp3` | سٌ | танвин «сун» |
| `syllables/shin_fathatan.mp3` | شًا | танвин «шан» |
| `syllables/shin_kasratan.mp3` | شٍ | танвин «шин» |
| `syllables/shin_dammatan.mp3` | شٌ | танвин «шун» |
| `syllables/sad_fathatan.mp3` | صًا | танвин «ṣан» |
| `syllables/sad_kasratan.mp3` | صٍ | танвин «ṣин» |
| `syllables/sad_dammatan.mp3` | صٌ | танвин «ṣун» |
| `syllables/dad_fathatan.mp3` | ضًا | танвин «ḍан» |
| `syllables/dad_kasratan.mp3` | ضٍ | танвин «ḍин» |
| `syllables/dad_dammatan.mp3` | ضٌ | танвин «ḍун» |
| `syllables/tta_fathatan.mp3` | طًا | танвин «ṭан» |
| `syllables/tta_kasratan.mp3` | طٍ | танвин «ṭин» |
| `syllables/tta_dammatan.mp3` | طٌ | танвин «ṭун» |
| `syllables/zza_fathatan.mp3` | ظًا | танвин «ẓан» |
| `syllables/zza_kasratan.mp3` | ظٍ | танвин «ẓин» |
| `syllables/zza_dammatan.mp3` | ظٌ | танвин «ẓун» |
| `syllables/ayn_fathatan.mp3` | عًا | танвин «ʿан» |
| `syllables/ayn_kasratan.mp3` | عٍ | танвин «ʿин» |
| `syllables/ayn_dammatan.mp3` | عٌ | танвин «ʿун» |
| `syllables/ghayn_fathatan.mp3` | غًا | танвин «ган» |
| `syllables/ghayn_kasratan.mp3` | غٍ | танвин «гин» |
| `syllables/ghayn_dammatan.mp3` | غٌ | танвин «гун» |
| `syllables/fa_fathatan.mp3` | فًا | танвин «фан» |
| `syllables/fa_kasratan.mp3` | فٍ | танвин «фин» |
| `syllables/fa_dammatan.mp3` | فٌ | танвин «фун» |
| `syllables/qaf_fathatan.mp3` | قًا | танвин «к̣ан» |
| `syllables/qaf_kasratan.mp3` | قٍ | танвин «к̣ин» |
| `syllables/qaf_dammatan.mp3` | قٌ | танвин «к̣ун» |
| `syllables/kaf_fathatan.mp3` | كًا | танвин «кан» |
| `syllables/kaf_kasratan.mp3` | كٍ | танвин «кин» |
| `syllables/kaf_dammatan.mp3` | كٌ | танвин «кун» |
| `syllables/lam_fathatan.mp3` | لًا | танвин «лан» |
| `syllables/lam_kasratan.mp3` | لٍ | танвин «лин» |
| `syllables/lam_dammatan.mp3` | لٌ | танвин «лун» |
| `syllables/mim_fathatan.mp3` | مًا | танвин «ман» |
| `syllables/mim_kasratan.mp3` | مٍ | танвин «мин» |
| `syllables/mim_dammatan.mp3` | مٌ | танвин «мун» |
| `syllables/nun_fathatan.mp3` | نًا | танвин «нан» |
| `syllables/nun_kasratan.mp3` | نٍ | танвин «нин» |
| `syllables/nun_dammatan.mp3` | نٌ | танвин «нун» |
| `syllables/ha_fathatan.mp3` | هًا | танвин «hан» |
| `syllables/ha_kasratan.mp3` | هٍ | танвин «hин» |
| `syllables/ha_dammatan.mp3` | هٌ | танвин «hун» |
| `syllables/waw_fathatan.mp3` | وًا | танвин «ван» |
| `syllables/waw_kasratan.mp3` | وٍ | танвин «вин» |
| `syllables/waw_dammatan.mp3` | وٌ | танвин «вун» |
| `syllables/ya_fathatan.mp3` | يًا | танвин «йан» |
| `syllables/ya_kasratan.mp3` | يٍ | танвин «йин» |
| `syllables/ya_dammatan.mp3` | يٌ | танвин «йун» |

## 3. Долгие слоги (мадд) — `madd/` (модуль 5)

| Файл | Текст | Что произнести |
|---|---|---|
| `madd/alif_madd_alif.mp3` | آ | долгий слог «аа» (2 счёта) |
| `madd/alif_madd_waw.mp3` | أُو | долгий слог «уу» (2 счёта) |
| `madd/alif_madd_ya.mp3` | إِي | долгий слог «ии» (2 счёта) |
| `madd/ba_madd_alif.mp3` | بَا | долгий слог «баа» (2 счёта) |
| `madd/ba_madd_waw.mp3` | بُو | долгий слог «буу» (2 счёта) |
| `madd/ba_madd_ya.mp3` | بِي | долгий слог «бии» (2 счёта) |
| `madd/ta_madd_alif.mp3` | تَا | долгий слог «таа» (2 счёта) |
| `madd/ta_madd_waw.mp3` | تُو | долгий слог «туу» (2 счёта) |
| `madd/ta_madd_ya.mp3` | تِي | долгий слог «тии» (2 счёта) |
| `madd/tha_madd_alif.mp3` | ثَا | долгий слог «с̱аа» (2 счёта) |
| `madd/tha_madd_waw.mp3` | ثُو | долгий слог «с̱уу» (2 счёта) |
| `madd/tha_madd_ya.mp3` | ثِي | долгий слог «с̱ии» (2 счёта) |
| `madd/jim_madd_alif.mp3` | جَا | долгий слог «джаа» (2 счёта) |
| `madd/jim_madd_waw.mp3` | جُو | долгий слог «джуу» (2 счёта) |
| `madd/jim_madd_ya.mp3` | جِي | долгий слог «джии» (2 счёта) |
| `madd/hha_madd_alif.mp3` | حَا | долгий слог «ḥаа» (2 счёта) |
| `madd/hha_madd_waw.mp3` | حُو | долгий слог «ḥуу» (2 счёта) |
| `madd/hha_madd_ya.mp3` | حِي | долгий слог «ḥии» (2 счёта) |
| `madd/kha_madd_alif.mp3` | خَا | долгий слог «хаа» (2 счёта) |
| `madd/kha_madd_waw.mp3` | خُو | долгий слог «хуу» (2 счёта) |
| `madd/kha_madd_ya.mp3` | خِي | долгий слог «хии» (2 счёта) |
| `madd/dal_madd_alif.mp3` | دَا | долгий слог «даа» (2 счёта) |
| `madd/dal_madd_waw.mp3` | دُو | долгий слог «дуу» (2 счёта) |
| `madd/dal_madd_ya.mp3` | دِي | долгий слог «дии» (2 счёта) |
| `madd/dhal_madd_alif.mp3` | ذَا | долгий слог «з̱аа» (2 счёта) |
| `madd/dhal_madd_waw.mp3` | ذُو | долгий слог «з̱уу» (2 счёта) |
| `madd/dhal_madd_ya.mp3` | ذِي | долгий слог «з̱ии» (2 счёта) |
| `madd/ra_madd_alif.mp3` | رَا | долгий слог «раа» (2 счёта) |
| `madd/ra_madd_waw.mp3` | رُو | долгий слог «руу» (2 счёта) |
| `madd/ra_madd_ya.mp3` | رِي | долгий слог «рии» (2 счёта) |
| `madd/zay_madd_alif.mp3` | زَا | долгий слог «заа» (2 счёта) |
| `madd/zay_madd_waw.mp3` | زُو | долгий слог «зуу» (2 счёта) |
| `madd/zay_madd_ya.mp3` | زِي | долгий слог «зии» (2 счёта) |
| `madd/sin_madd_alif.mp3` | سَا | долгий слог «саа» (2 счёта) |
| `madd/sin_madd_waw.mp3` | سُو | долгий слог «суу» (2 счёта) |
| `madd/sin_madd_ya.mp3` | سِي | долгий слог «сии» (2 счёта) |
| `madd/shin_madd_alif.mp3` | شَا | долгий слог «шаа» (2 счёта) |
| `madd/shin_madd_waw.mp3` | شُو | долгий слог «шуу» (2 счёта) |
| `madd/shin_madd_ya.mp3` | شِي | долгий слог «шии» (2 счёта) |
| `madd/sad_madd_alif.mp3` | صَا | долгий слог «ṣаа» (2 счёта) |
| `madd/sad_madd_waw.mp3` | صُو | долгий слог «ṣуу» (2 счёта) |
| `madd/sad_madd_ya.mp3` | صِي | долгий слог «ṣии» (2 счёта) |
| `madd/dad_madd_alif.mp3` | ضَا | долгий слог «ḍаа» (2 счёта) |
| `madd/dad_madd_waw.mp3` | ضُو | долгий слог «ḍуу» (2 счёта) |
| `madd/dad_madd_ya.mp3` | ضِي | долгий слог «ḍии» (2 счёта) |
| `madd/tta_madd_alif.mp3` | طَا | долгий слог «ṭаа» (2 счёта) |
| `madd/tta_madd_waw.mp3` | طُو | долгий слог «ṭуу» (2 счёта) |
| `madd/tta_madd_ya.mp3` | طِي | долгий слог «ṭии» (2 счёта) |
| `madd/zza_madd_alif.mp3` | ظَا | долгий слог «ẓаа» (2 счёта) |
| `madd/zza_madd_waw.mp3` | ظُو | долгий слог «ẓуу» (2 счёта) |
| `madd/zza_madd_ya.mp3` | ظِي | долгий слог «ẓии» (2 счёта) |
| `madd/ayn_madd_alif.mp3` | عَا | долгий слог «ʿаа» (2 счёта) |
| `madd/ayn_madd_waw.mp3` | عُو | долгий слог «ʿуу» (2 счёта) |
| `madd/ayn_madd_ya.mp3` | عِي | долгий слог «ʿии» (2 счёта) |
| `madd/ghayn_madd_alif.mp3` | غَا | долгий слог «гаа» (2 счёта) |
| `madd/ghayn_madd_waw.mp3` | غُو | долгий слог «гуу» (2 счёта) |
| `madd/ghayn_madd_ya.mp3` | غِي | долгий слог «гии» (2 счёта) |
| `madd/fa_madd_alif.mp3` | فَا | долгий слог «фаа» (2 счёта) |
| `madd/fa_madd_waw.mp3` | فُو | долгий слог «фуу» (2 счёта) |
| `madd/fa_madd_ya.mp3` | فِي | долгий слог «фии» (2 счёта) |
| `madd/qaf_madd_alif.mp3` | قَا | долгий слог «к̣аа» (2 счёта) |
| `madd/qaf_madd_waw.mp3` | قُو | долгий слог «к̣уу» (2 счёта) |
| `madd/qaf_madd_ya.mp3` | قِي | долгий слог «к̣ии» (2 счёта) |
| `madd/kaf_madd_alif.mp3` | كَا | долгий слог «каа» (2 счёта) |
| `madd/kaf_madd_waw.mp3` | كُو | долгий слог «куу» (2 счёта) |
| `madd/kaf_madd_ya.mp3` | كِي | долгий слог «кии» (2 счёта) |
| `madd/lam_madd_alif.mp3` | لَا | долгий слог «лаа» (2 счёта) |
| `madd/lam_madd_waw.mp3` | لُو | долгий слог «луу» (2 счёта) |
| `madd/lam_madd_ya.mp3` | لِي | долгий слог «лии» (2 счёта) |
| `madd/mim_madd_alif.mp3` | مَا | долгий слог «маа» (2 счёта) |
| `madd/mim_madd_waw.mp3` | مُو | долгий слог «муу» (2 счёта) |
| `madd/mim_madd_ya.mp3` | مِي | долгий слог «мии» (2 счёта) |
| `madd/nun_madd_alif.mp3` | نَا | долгий слог «наа» (2 счёта) |
| `madd/nun_madd_waw.mp3` | نُو | долгий слог «нуу» (2 счёта) |
| `madd/nun_madd_ya.mp3` | نِي | долгий слог «нии» (2 счёта) |
| `madd/ha_madd_alif.mp3` | هَا | долгий слог «hаа» (2 счёта) |
| `madd/ha_madd_waw.mp3` | هُو | долгий слог «hуу» (2 счёта) |
| `madd/ha_madd_ya.mp3` | هِي | долгий слог «hии» (2 счёта) |
| `madd/waw_madd_alif.mp3` | وَا | долгий слог «ваа» (2 счёта) |
| `madd/waw_madd_waw.mp3` | وُو | долгий слог «вуу» (2 счёта) |
| `madd/waw_madd_ya.mp3` | وِي | долгий слог «вии» (2 счёта) |
| `madd/ya_madd_alif.mp3` | يَا | долгий слог «йаа» (2 счёта) |
| `madd/ya_madd_waw.mp3` | يُو | долгий слог «йуу» (2 счёта) |
| `madd/ya_madd_ya.mp3` | يِي | долгий слог «йии» (2 счёта) |

## 4. Слова — `words/` (модули 4, 6, 7)

| Файл | Текст | Что произнести |
|---|---|---|
| `words/rabb.mp3` | رَبّ | «рабб» — Господь (модуль 4) |
| `words/umm.mp3` | أُمّ | «умм» — мать (модуль 4) |
| `words/jannah.mp3` | جَنَّة | «джанна» — рай (модуль 4) |
| `words/kitaban.mp3` | كِتَابًا | «китабан» — книгу (с танвином) (модуль 4) |
| `words/madrasa.mp3` | مَدْرَسَة | «мадраса» — школа (та марбута) (модуль 6) |
| `words/salat.mp3` | صَلَاة | «салят» — молитва (та марбута) (модуль 6) |
| `words/sama.mp3` | سَمَاء | «самаъ» — небо (хамза) (модуль 6) |
| `words/mumin.mp3` | مُؤْمِن | «муъмин» — верующий (хамза на вав) (модуль 6) |
| `words/suaal.mp3` | سُؤَال | «суъаль» — вопрос (хамза) (модуль 6) |
| `words/musa.mp3` | مُوسَى | «Муса» — Муса (алиф максура) (модуль 6) |
| `words/huda.mp3` | هُدًى | «худан» — руководство (алиф максура) (модуль 6) |
| `words/la.mp3` | لَا | «ля» — нет (лям-алиф) (модуль 6) |
| `words/illa.mp3` | إِلَّا | «илля» — кроме (лям-алиф) (модуль 6) |
| `words/kalla.mp3` | كَلَّا | «калля» — так нет же! (лям-алиф) (модуль 6) |
| `words/ash_shams.mp3` | الشَّمْس | «аш-шамс» — солнце (солнечная буква) (модуль 6) |
| `words/ar_rahman.mp3` | الرَّحْمَٰن | «ар-Рахман» — Милостивый (солнечная буква) (модуль 6) |
| `words/an_nur.mp3` | النُّور | «ан-нур» — свет (солнечная буква) (модуль 6) |
| `words/al_qamar.mp3` | الْقَمَر | «аль-камар» — луна (лунная буква) (модуль 6) |
| `words/al_kitab.mp3` | الْكِتَاب | «аль-китаб» — книга (лунная буква) (модуль 6) |
| `words/al_bayt.mp3` | الْبَيْت | «аль-байт» — дом (лунная буква) (модуль 6) |
| `words/bab.mp3` | بَاب | «баб» — дверь (модуль 7) |
| `words/kitab.mp3` | كِتَاب | «китаб» — книга (модуль 7) |
| `words/qalam.mp3` | قَلَم | «калам» — ручка (модуль 7) |
| `words/walad.mp3` | وَلَد | «валад» — мальчик (модуль 7) |
| `words/bint.mp3` | بِنْت | «бинт» — девочка (модуль 7) |
| `words/bayt.mp3` | بَيْت | «байт» — дом (модуль 7) |
| `words/nur.mp3` | نُور | «нур» — свет (модуль 7) |
| `words/shams.mp3` | شَمْس | «шамс» — солнце (модуль 7) |
| `words/qamar.mp3` | قَمَر | «камар» — луна (модуль 7) |
| `words/maa.mp3` | مَاء | «маъ» — вода (модуль 7) |
| `words/ab.mp3` | أَب | «аб» — отец (модуль 7) |
| `words/yawm.mp3` | يَوْم | «йаум» — день (модуль 7) |
| `words/samak.mp3` | سَمَك | «самак» — рыба (модуль 7) |
| `words/dars.mp3` | دَرْس | «дарс» — урок (модуль 7) |
| `words/kataba.mp3` | كَتَبَ | «катаба» — он написал (модуль 7) |
| `words/allah.mp3` | ٱللَّه | «Аллах» — Аллах (модуль 7) |
| `words/bismi.mp3` | بِسْمِ | «бисми» — во имя (модуль 7) |
| `words/rahim.mp3` | رَحِيم | «рахим» — Милосердный (модуль 7) |
| `words/hamd.mp3` | حَمْد | «хамд» — хвала (модуль 7) |
| `words/alamin.mp3` | عَالَمِين | «ʿалямин» — миры (модуль 7) |
| `words/malik.mp3` | مَالِك | «малик» — Владыка (модуль 7) |
| `words/din.mp3` | دِين | «дин» — религия, воздаяние (модуль 7) |
| `words/iyyaka.mp3` | إِيَّاكَ | «иййака» — Тебе (одному) (модуль 7) |
| `words/sirat.mp3` | صِرَاط | «сират» — путь (модуль 7) |
| `words/mustaqim.mp3` | مُسْتَقِيم | «мустаким» — прямой (модуль 7) |
| `words/qul.mp3` | قُلْ | «куль» — скажи (модуль 7) |
| `words/ahad.mp3` | أَحَد | «ахад» — Единый (модуль 7) |
| `words/samad.mp3` | صَمَد | «самад» — Самодостаточный (модуль 7) |
| `words/nas.mp3` | نَاس | «нас» — люди (модуль 7) |
| `words/falaq.mp3` | فَلَق | «фалак» — рассвет (модуль 7) |
| `words/kawthar.mp3` | كَوْثَر | «каусар» — Каусар (изобилие) (модуль 7) |

## 5. Звуки интерфейса — `ui/` (не голос, любые короткие сигналы)

| Файл | Текст | Что произнести |
|---|---|---|
| `ui/correct.mp3` |  | короткий приятный сигнал — правильный ответ |
| `ui/wrong.mp3` |  | мягкий низкий сигнал — неправильный ответ |
| `ui/complete.mp3` |  | мелодия завершения урока |

## Подсказки для записи

- Названия букв (`letters/`) — как учитель произносит название: «алиф», «ба», «та»…
- Слоги — коротко и чётко, одна огласовка: «ба», «би», «бу».
- Танвин с фатхой пишется с алифом на конце (بًا), читается «бан».
- Мадд — гласная тянется ровно в 2 раза дольше короткой.
- Для алифа вместо огласовки звучит хамза: أَ «а», إِ «и», أُ «у».
