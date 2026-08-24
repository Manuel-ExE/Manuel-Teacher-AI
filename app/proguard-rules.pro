# ManuelTAi release rules.
# minifyEnabled/shrinkResources are currently false (see app/build.gradle),
# so these rules aren't active yet — kept here for when that changes.

# MediaPipe LLM Inference (Gemma on-device) uses JNI + reflection internally.
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**
