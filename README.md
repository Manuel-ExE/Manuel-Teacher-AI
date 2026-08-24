# ManuelTAi

**MANUEL Teacher AI** is an Android-only, offline-first teaching workspace for teachers. The project is intentionally structured as a conventional Android Gradle project so it can be built reliably by Android Studio, GitHub Actions, or the included Gradle wrapper.

## Project structure

```text
Manuel-Teacher-AI/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/manuel/tai/MainActivity.kt
│       └── res/
│           ├── drawable/ic_launcher.xml
│           ├── layout/activity_main.xml
│           └── values/
├── .github/workflows/android-apk.yml
├── build.gradle
├── gradle.properties
├── gradlew
├── gradle/wrapper/
└── settings.gradle
```

The app uses Kotlin 1.9.22, Android Gradle Plugin 8.7.3, Gradle 8.10.2, Java 17, view binding, Android XML resources, and a small set of AndroidX and Material dependencies. The package name is `com.manuel.tai`, and the Android label is `ManuelTAi`.

## Current MVP

The current screen provides the ManuelTAi dashboard, truthful prototype status, Lesson Planner and Question Generator forms, deterministic offline draft generation, Teaching Materials PDF selection with persisted URI references, saved-item persistence, local resource counting, battery-saver settings, and adaptive launcher icons. The current drafts are deterministic offline templates; a Gemma/Qwen on-device model runtime will be connected as a separate next step.

## Build locally

Install JDK 17 and the Android SDK platform and build tools for API 34. The included wrapper downloads Gradle 8.10.2 from the standard Gradle distribution service. From the repository root, run:

```bash
chmod +x gradlew
./gradlew --no-daemon clean lintDebug assembleDebug
```

The installable testing APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Build with GitHub Actions

The workflow at `.github/workflows/android-apk.yml` runs on pushes and pull requests to `main`, and can also be started manually from **Actions → Build Android APK → Run workflow**. It installs Android API 34 and runs `clean`, `lintDebug`, `assembleDebug`, and `assembleRelease`. A preflight step removes stale Kotlin-DSL and duplicate legacy theme files, which prevents duplicate-resource failures when older copies of the project remain in the repository.

The workflow runs unit tests and debug/release lint, builds both APKs, verifies their signatures, and uploads `ManuelTAi-debug-apk` and `ManuelTAi-release-apk`. The release artifact uses an ephemeral CI test certificate so it can be installed directly for testing; replace this with a protected production keystore before Play Store distribution. When updating an existing checkout, replace the whole project folder or remove old files such as `app/src/main/res/values/styles.xml`; leaving both `styles.xml` and `themes.xml` with the same `Theme.ManuelTAi` declaration causes a resource merge error.

## Performance and privacy principles

ManuelTAi does not run continuous AI inference, background polling, location services, or a permanent network connection. AI generation will be user-triggered, local document storage will be preferred, and the eventual model manager will choose a smaller quantized model on low-memory Android devices. Internet access will remain optional for updates, model downloads, backups, or explicitly enabled synchronization.
