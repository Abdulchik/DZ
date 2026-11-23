# Алиф — арабский алфавит и чтение Корана

Android-приложение для новичков: от первых букв до чтения Корана.
Kotlin + Jetpack Compose (Material 3), MVVM, Room, DataStore, Media3.
Минимальная версия Android — 8.0.

## Этапы

| # | Этап | Статус |
|---|------|--------|
| 1 | Структура проекта, тема, навигация, список аудиофайлов | ✅ готово |
| 2 | Модуль алфавита с аудио | ⏳ |
| 3 | Формы и соединение букв | ⏳ |
| 4 | Огласовки и упражнения | ⏳ |
| 5 | Список сур и экран чтения | ⏳ |
| 6 | Аудио Корана | ⏳ |
| 7 | Избранное и заметки | ⏳ |
| 8 | Прогресс и напоминания | ⏳ |

## Как запустить в Android Studio

1. **Установите Android Studio** (бесплатно): https://developer.android.com/studio
   Берите последнюю версию (Ladybug или новее). При первом запуске согласитесь
   установить Android SDK — мастер всё сделает сам.
2. **Откройте проект.** File → Open → выберите папку **`AlifApp`** (именно её,
   а не корень репозитория) → OK.
3. **Дождитесь синхронизации Gradle.** Внизу будет полоска «Gradle sync…».
   В первый раз она скачивает библиотеки, это может занять 5–15 минут.
   Нужен интернет. Если Studio предложит обновить Gradle/AGP — можно согласиться.
4. **Подключите телефон** или создайте эмулятор:
   - Телефон: Настройки → О телефоне → 7 раз нажмите «Номер сборки» →
     появится «Для разработчиков» → включите «Отладка по USB» → подключите кабель
     и разрешите отладку на телефоне.
   - Эмулятор: Tools → Device Manager → «+» → выберите Pixel → образ Android 14 → Finish.
5. **Нажмите зелёную кнопку ▶ Run** (или Shift+F10). Приложение установится и откроется.

## Как собрать APK

**Быстрый способ (для себя и друзей):**
Build → Build App Bundle(s) / APK(s) → **Build APK(s)**.
Когда сборка закончится, справа внизу появится уведомление — нажмите **locate**.
Файл: `AlifApp/app/build/outputs/apk/debug/app-debug.apk`.
Этот файл можно отправить на телефон (Telegram, USB) и установить
(телефон попросит разрешить установку из неизвестных источников).

**Через терминал** (вкладка Terminal внизу Android Studio):
```
./gradlew assembleDebug      # отладочный APK
./gradlew assembleRelease    # сжатый release APK (app/build/outputs/apk/release/)
```
На Windows вместо `./gradlew` пишите `gradlew`.

> Release-сборка сейчас подписывается отладочным ключом — это удобно для тестов.
> Для Google Play создайте свой ключ: Build → Generate Signed App Bundle / APK.

## Структура проекта

```
AlifApp/
├── app/src/main/
│   ├── java/com/alif/app/
│   │   ├── AlifApplication.kt, AppContainer.kt   — запуск и зависимости
│   │   ├── MainActivity.kt
│   │   ├── data/settings/      — настройки (DataStore)
│   │   ├── data/learn/         — путь обучения, модули, уроки
│   │   ├── ui/theme/           — цвета, шрифты, формы, тёмная тема
│   │   ├── ui/components/      — общие элементы (арабский текст, карточки, вибрация)
│   │   ├── ui/navigation/      — вкладки и маршруты
│   │   ├── ui/learn | quran | favorites | profile/  — экраны
│   │   └── util/AppLanguage.kt — язык интерфейса
│   ├── res/font/               — Amiri Quran, Amiri, Inter
│   ├── res/values/strings.xml  — все тексты на русском
│   └── assets/audio/           — аудио букв, слогов, слов (ваши записи)
├── docs/AUDIO_FILES.md         — список всех нужных аудиофайлов
├── tools/audio_manifest.py     — генератор этого списка
└── licenses/                   — лицензии шрифтов
```

## Аудио букв

Синтез речи для арабского не используется — он искажает произношение.
Все записи лежат в `app/src/main/assets/audio/`. Полный список имён файлов
(361 шт.) — в [docs/AUDIO_FILES.md](docs/AUDIO_FILES.md).
Чтобы заменить запись, положите свой MP3 с тем же именем и пересоберите приложение.

## Как добавить таджикский язык

1. Скопируйте `app/src/main/res/values/strings.xml` в `app/src/main/res/values-tg/strings.xml`
   и переведите тексты (атрибуты `name` не трогайте).
2. В `app/build.gradle.kts`: `resourceConfigurations += listOf("ru", "tg")`.
3. В `res/xml/locales_config.xml` раскомментируйте `<locale android:name="tg" />`.
4. В `util/AppLanguage.kt` у `TAJIK` поставьте `available = true`.

Язык переключается в Профиль → Язык интерфейса.

## Источники и лицензии

- Текст Корана — [Tanzil Project](https://tanzil.net), Uthmani (CC BY 3.0, без изменений текста).
- Перевод смыслов — Э. Кулиев (через Tanzil, некоммерческое использование).
- Аудио аятов — [EveryAyah.com](https://everyayah.com).
- Шрифты — Amiri / Amiri Quran (Khaled Hosny), Inter (Rasmus Andersson), SIL OFL 1.1 — см. `licenses/`.

Эти же источники показаны в приложении: Профиль → О приложении.
