# ManuelTAi

**MANUEL Teacher AI** is an Android-only, offline-first education assistant for teachers. The app is designed for low-end devices, small installation size, low memory usage, and minimal battery consumption.

## Current Android foundation

The repository contains a lightweight native Android application written in Kotlin with Jetpack Compose. The initial screen provides the ManuelTAi teacher workspace and establishes the package name `com.manuel.tai`.

The current MVP includes an Android dashboard, lesson planner form, question generator, teaching-materials PDF picker, local resource counters, offline status, model-tier selection, and battery-saver messaging. The local-AI engine is kept separate and will be connected as an Android-compatible module. It should run only when the teacher requests generation, remain stopped otherwise, and use a device-appropriate quantized model. The current lesson and question outputs are local demo drafts that will be replaced by the model runtime.

## Build locally

Install JDK 17 and the Android SDK, then run:

```bash
./gradlew assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Build with GitHub Actions

The workflow at `.github/workflows/android-apk.yml` runs on pushes and pull requests to `main`, as well as from the GitHub Actions **Run workflow** button. It builds both a debug APK and an unsigned release APK.

To download an APK, open the completed workflow run on GitHub and download either the `ManuelTAi-debug-apk` or `ManuelTAi-release-apk-unsigned` artifact.

The unsigned release APK is suitable for build verification but is not ready for Play Store distribution. Production distribution will require an Android signing keystore stored as protected GitHub Actions secrets.

## Battery and performance principles

The app will avoid background AI inference, continuous polling, unnecessary location services, automatic video or animation, and persistent network connections. AI generation will be user-triggered and executed only while the relevant screen is active. Local files and small SQLite-style data will be preferred over repeated network requests.
