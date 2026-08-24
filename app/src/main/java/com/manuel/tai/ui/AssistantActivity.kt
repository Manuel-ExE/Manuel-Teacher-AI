package com.manuel.tai.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.manuel.tai.DraftGenerator
import com.manuel.tai.R
import com.manuel.tai.ai.LocalAiEngine
import com.manuel.tai.ai.ModelManager
import com.manuel.tai.data.LocalStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssistantActivity : AppCompatActivity() {
    private lateinit var promptInput: TextInputEditText
    private lateinit var conversation: TextView
    private lateinit var progress: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistant)
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        promptInput = findViewById(R.id.promptInput)
        conversation = findViewById(R.id.conversationText)
        progress = findViewById(R.id.progressBar)
        findViewById<MaterialButton>(R.id.askButton).setOnClickListener { ask() }
    }
    private fun ask() {
        val question = promptInput.text?.toString()?.trim().orEmpty()
        if (question.isBlank()) { Toast.makeText(this, R.string.assistant_prompt_required, Toast.LENGTH_SHORT).show(); return }
        promptInput.text?.clear()
        val context = LocalStore.retrieve(this, question).joinToString("\n")
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val answer = if (ModelManager.isModelInstalled(this@AssistantActivity)) {
                    if (!LocalAiEngine.isReady()) LocalAiEngine.initialize(this@AssistantActivity, ModelManager.modelFile(this@AssistantActivity).absolutePath).getOrThrow()
                    withContext(Dispatchers.Default) { LocalAiEngine.generateResponse("You are an offline teacher assistant. Answer clearly and safely. Use only this local context when relevant:\n$context\n\nTeacher request: $question") }
                } else DraftGenerator.assistant(question, context)
                conversation.append("\nTeacher: $question\nAssistant: $answer\n")
            } catch (e: Exception) {
                Toast.makeText(this@AssistantActivity, getString(R.string.generation_failed, e.message ?: e.toString()), Toast.LENGTH_LONG).show()
            } finally { progress.visibility = View.GONE }
        }
    }
}
