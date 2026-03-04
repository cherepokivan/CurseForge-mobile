# CurseForge-mobile (Bedrock Addons)

Android-приложение (Kotlin + Jetpack Compose), которое получает список аддонов Minecraft Bedrock через прокси-бэкенд и позволяет скачать файл с последующим `open with` для импорта в Minecraft.

> Важно: API-ключ CurseForge **не хранится в приложении**. Приложение работает только с вашим backend proxy (например, Vercel), а ключ остается на сервере.

## Что реализовано

- Kotlin + Jetpack Compose UI в тёмной палитре CurseForge (графит + оранжевый акцент)
- MVVM: Repository -> ViewModel -> UI
- Retrofit + OkHttp + Kotlinx Serialization
- Поиск + постраничная загрузка
- Фильтр по версии Bedrock (поддержка точных форматов вроде `1.20.132`, `26.0.0.2`, `26.3.0`)
- Загрузка через `DownloadManager` в `Downloads`
- `BroadcastReceiver` на завершение загрузки и авто-открытие файла (`.mcpack` / `.mcaddon`) через `FileProvider`
- Обработка сетевых ошибок и отображение текста ошибки
- Мини-кэш в памяти по страницам
- Локальные избранные (Room)
- Экран настроек:
  - override BASE URL (для dev/debug)
  - toggle авто-открытия после загрузки
- Логи в буфере + кнопка `Send logs (preview)`
- Unit test для ViewModel

## API контракты бэкенда

Приложение ожидает:

1. `GET {BASE_URL}/api/search?q={query}&page={n}&version={bedrockVersion}` (version опционален)
2. `GET {BASE_URL}/api/file/{fileId}`
3. `GET {BASE_URL}/api/download?fileId={id}`

## Локальный запуск

1. Установите Android Studio (JDK 17).
2. Откройте проект.
3. Для локального build с вашим URL:
   ```bash
   ./gradlew -PbaseUrl=https://express-js-on-vercel-for-curseforge.vercel.app/ assembleDebug
   ```
4. Запустите `app` на устройстве/эмуляторе.

Если `-PbaseUrl` не передан, используется дефолт `https://express-js-on-vercel-for-curseforge.vercel.app/`.


### Если `gradlew` не стартует (403 при скачивании Gradle)

Если видите ошибку вида `CONNECT tunnel failed, response 403` при загрузке
`https://services.gradle.org/distributions/...`, это проблема сетевого proxy/firewall, а не `-PbaseUrl` вашего бэкенда.

Проверка:
```bash
curl -I -L https://services.gradle.org/distributions/gradle-8.10.2-bin.zip
```

Что делать:
- разрешить доступ к `services.gradle.org`/`downloads.gradle.org` в proxy;
- или запускать сборку в GitHub Actions (там Gradle скачивается штатно);
- или использовать окружение с открытым outbound HTTPS для Gradle wrapper.

## GitHub Actions

Workflow: `.github/workflows/build-apk.yml`

Сборка release APK использует публичный URL бэкенда напрямую:

- `https://express-js-on-vercel-for-curseforge.vercel.app/`

Workflow при пуше в `main` (или вручную через `workflow_dispatch`) делает:

- `./gradlew -PbaseUrl=https://express-js-on-vercel-for-curseforge.vercel.app/ test`
- `./gradlew -PbaseUrl=https://express-js-on-vercel-for-curseforge.vercel.app/ assembleRelease`
- складывает APK в артефакт
- создает GitHub Release и прикладывает APK

Для ручного запуска (`workflow_dispatch`) можно задать:
- `release_tag` — тег релиза (например `v1.0.1`)
- `release_name` — название релиза
- `release_notes` — текст исправлений (если пусто, включается авто-генерация GitHub notes)

### Опционально для подписанных релизов

Если захотите подписывать релизы в CI, добавьте секреты:
- `ANDROID_KEYSTORE`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

(В текущем примере workflow собирает release APK без кастомной подписи.)

## Отладка и смена backend URL

- Build-time URL задается через `-PbaseUrl=...` (по умолчанию уже выставлен публичный URL Vercel).
- В приложении на экране **Настройки** можно временно задать override URL для debug-сценариев.
- Для диагностики нажмите `Send logs (preview)` — появятся последние строки логов.

## Безопасность

- В репозитории нет приватных API-ключей.
- URL прокси является публичным и не требует хранения в GitHub Secrets.
- Настоящий CurseForge API ключ должен храниться только на вашем backend.


## Иконка приложения

Сейчас launcher-иконка подключена как adaptive icon (`@mipmap/ic_launcher` / `@mipmap/ic_launcher_round`).
Если хотите заменить на вашу картинку, просто передайте PNG — мы обновим foreground/background ресурсы в `res/drawable` и `mipmap-anydpi-v26`.
