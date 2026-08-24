# ManuelTAi

MANUEL Teacher AI is an Android-only teaching workspace for teachers. It is designed to work offline, avoid background processing, and keep model inference user-triggered to reduce battery use.

## Current project

The repository is a conventional Android Gradle project using Kotlin, XML layouts, view binding, AndroidX, Material Components, and an optional MediaPipe Tasks GenAI integration. The app package is `com.manuel.tai` and the app label is `ManuelTAi`.

The current workspace includes AI Teacher Assistant, Lesson Planner, Question Generator, Worksheet Creator, Quiz Creator, Marking Assistant, Student Records, and a Materials Library. Student records and saved materials are stored as small JSON documents in app-private SharedPreferences; no account, network, database server, or background worker is required. Materials can be pasted or imported as text or PDF, extracted locally, searched locally, and ranked by keyword overlap. The top matching excerpts are added to assistant, lesson, worksheet, and quiz prompts as transparent local context, providing a lightweight retrieval-augmented generation (RAG) path. Generated lessons, questions, worksheets, quizzes, and marking reports can be saved as text, shared, and exported as local PDFs through Android’s secure FileProvider. Lesson, question, assistant, worksheet, quiz, and marking generation use the local MediaPipe model once a compatible `.task` model has been imported. Without a model, every tool still works with deterministic offline drafts (`DraftGenerator`) so the app is useful immediately and upgrades in place once a model is imported.

## Structure

```text
Manuel-Teacher-AI/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/manuel/tai/
│       │   │   ├── data/LocalStore.kt
│       │   │   ├── export/PdfExporter.kt
│       │   │   └── ui/AssistantActivity.kt, WorkspaceToolActivity.kt, StudentRecordsActivity.kt, MaterialsActivity.kt
│       │   └── res/
│       └── test/java/com/manuel/tai/
├── .github/workflows/android-apk.yml
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
└── settings.gradle
```

There must be only one active Gradle configuration and one `Theme.ManuelTAi` declaration. Do not restore `build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`, or `app/src/main/res/values/styles.xml` into the repository.

## Build locally

Use JDK 17 and an Android SDK with API 34 and Build Tools 34.0.0 installed:

```bash
chmod +x gradlew
./gradlew --no-daemon clean testDebugUnitTest lintDebug lintRelease assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. A local release build is unsigned unless signing properties are provided; use the GitHub Actions workflow for the installable test-signed release artifact.

## GitHub Actions

The workflow in `.github/workflows/android-apk.yml` runs unit tests, debug and release lint, builds debug and release APKs, verifies both signatures with `apksigner`, and uploads `ManuelTAi-debug-apk` and `ManuelTAi-release-apk`.

The workflow creates an ephemeral test keystore for the release APK. This is intentionally suitable only for testing and sideloading. Before public distribution, replace it with a protected production keystore stored in GitHub Actions secrets. Never use the test certificate for a Google Play release.

After downloading an artifact, extract the artifact ZIP and install the contained APK. Do not install an old `app-release-unsigned.apk` file.

## Local AI model

The model is not bundled in the APK because compatible on-device LLM files are large. Import a compatible `.task` model from the app’s model picker. The MediaPipe LLM Inference API is optimized for higher-end Android devices; verify memory, heat, storage, and battery impact on the target phone before deployment. The project currently uses MediaPipe Tasks GenAI 0.10.27.
