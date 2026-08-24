package com.manuel.tai.ai

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

/**
 * Manages the on-device .task model file.
 *
 * Model files (Gemma etc.) are several GB and gated behind a license
 * acceptance on Kaggle/Hugging Face, so they can't be bundled in the APK or
 * fetched from a hardcoded URL. Instead, the teacher downloads a .task file
 * once in their browser and imports it here via the system file picker —
 * after that, everything runs offline.
 */
object ModelManager {

    private const val MODEL_DIR_NAME = "models"
    private const val MODEL_FILE_NAME = "model.task"

    private fun modelDir(context: Context): File =
        File(context.filesDir, MODEL_DIR_NAME).apply { mkdirs() }

    fun modelFile(context: Context): File = File(modelDir(context), MODEL_FILE_NAME)

    fun isModelInstalled(context: Context): Boolean {
        val file = modelFile(context)
        return file.exists() && file.length() > 0
    }

    fun modelSizeMb(context: Context): Long = modelFile(context).length() / (1024 * 1024)

    /**
     * Copies the selected file into app-private storage. Runs on whatever
     * thread it's called from — callers should invoke this off the main
     * thread (it can take a while for multi-GB files).
     */
    fun importModel(context: Context, sourceUri: Uri): Result<Unit> {
        return try {
            val dest = modelFile(context)
            val tempDest = File(dest.parentFile, "$MODEL_FILE_NAME.tmp")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                tempDest.outputStream().use { output -> input.copyTo(output) }
            } ?: return Result.failure(IOException("Could not open the selected file"))

            if (dest.exists()) dest.delete()
            if (!tempDest.renameTo(dest)) {
                return Result.failure(IOException("Could not finish saving the model file"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteModel(context: Context) {
        modelFile(context).delete()
        LocalAiEngine.close()
    }
}
