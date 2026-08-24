package com.manuel.tai.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thin wrapper around MediaPipe's on-device LLM Inference API.
 *
 * The model runs entirely on the device once loaded — no network calls are
 * made by this class. Callers must load a model (via [initialize]) before
 * calling [generateResponse]; check [isReady] first to avoid the exception.
 */
object LocalAiEngine {

    private var llmInference: LlmInference? = null
    private var loadedModelPath: String? = null

    fun isReady(): Boolean = llmInference != null

    /**
     * Loads a .task model file from local storage. Safe to call again with a
     * new path (e.g. after importing a different/updated model) — the
     * previous session is closed first.
     */
    @Synchronized
    fun initialize(context: Context, modelPath: String): Result<Unit> {
        if (loadedModelPath == modelPath && llmInference != null) {
            return Result.success(Unit)
        }
        return try {
            close()
            val options = LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(1024)
                .setMaxTopK(40)
                .setTemperature(0.7f)
                .setRandomSeed(0)
                .build()
            llmInference = LlmInference.createFromOptions(context.applicationContext, options)
            loadedModelPath = modelPath
            Result.success(Unit)
        } catch (e: Exception) {
            llmInference = null
            loadedModelPath = null
            Result.failure(e)
        }
    }

    /**
     * Runs inference off the main thread. Throws [IllegalStateException] if
     * no model has been loaded yet — check [isReady] first.
     */
    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.Default) {
        val engine = llmInference
            ?: throw IllegalStateException("No local AI model loaded. Import a .task model file first.")
        engine.generateResponse(prompt)
    }

    @Synchronized
    fun close() {
        llmInference?.close()
        llmInference = null
        loadedModelPath = null
    }
}
