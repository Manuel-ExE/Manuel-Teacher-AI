package com.manuel.tai.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.manuel.tai.DraftGenerator
import com.manuel.tai.R
import com.manuel.tai.ai.LocalAiEngine
import com.manuel.tai.ai.ModelManager
import com.manuel.tai.databinding.ActivityQuestionGeneratorBinding
import com.manuel.tai.export.PdfExporter
import androidx.core.content.FileProvider
import com.manuel.tai.model.QuestionParser
import java.io.File
import kotlinx.coroutines.launch

class QuestionGeneratorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionGeneratorBinding
    private lateinit var adapter: QuestionAdapter
    private var lastOutput = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        setupSpinners()
        setupRecyclerView()
        refreshModelStatus()

        binding.generateButton.setOnClickListener { onGenerateClicked() }
        binding.saveQuestionsButton.setOnClickListener { saveQuestions() }
        binding.exportQuestionsButton.setOnClickListener { exportQuestions() }
    }

    override fun onResume() {
        super.onResume()
        refreshModelStatus()
    }

    private fun setupSpinners() {
        binding.subjectSpinner.adapter = simpleAdapter(
            listOf("Mathematics", "English", "Biology", "Physics", "Chemistry", "Basic Science", "Social Studies")
        )
        binding.classSpinner.adapter = simpleAdapter(
            listOf("Primary 1", "Primary 2", "Primary 3", "Primary 4", "Primary 5", "Primary 6", "JSS 1", "JSS 2", "JSS 3", "SSS 1", "SSS 2", "SSS 3")
        )
        binding.typeSpinner.adapter = simpleAdapter(
            listOf("Multiple Choice", "Short Answer", "True/False")
        )
        binding.difficultySpinner.adapter = simpleAdapter(
            listOf("Mixed", "Easy", "Standard", "Advanced")
        )
    }

    private fun simpleAdapter(items: List<String>): ArrayAdapter<String> =
        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

    private fun setupRecyclerView() {
        adapter = QuestionAdapter(emptyList())
        binding.questionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.questionsRecyclerView.adapter = adapter
    }

    private fun refreshModelStatus() {
        val installed = ModelManager.isModelInstalled(this)
        binding.modelStatusText.text = if (installed) {
            getString(R.string.model_ready, ModelManager.modelSizeMb(this))
        } else {
            getString(R.string.model_not_loaded)
        }
    }

    private fun onGenerateClicked() {
        val topic = binding.topicInput.text?.toString()?.trim().orEmpty()
        if (topic.isEmpty()) {
            Toast.makeText(this, R.string.topic_required, Toast.LENGTH_SHORT).show()
            return
        }

        binding.rawFallbackText.visibility = View.GONE
        adapter.submitList(emptyList())

        if (!ModelManager.isModelInstalled(this)) {
            generateOfflineDraft(topic)
            return
        }

        val prompt = buildPrompt(topic)
        setLoading(true)

        lifecycleScope.launch {
            try {
                if (!LocalAiEngine.isReady()) {
                    val modelPath = ModelManager.modelFile(this@QuestionGeneratorActivity).absolutePath
                    LocalAiEngine.initialize(this@QuestionGeneratorActivity, modelPath).getOrThrow()
                }
                val response = LocalAiEngine.generateResponse(prompt)
                lastOutput = response
                val questions = QuestionParser.parse(response)
                if (questions.isEmpty()) {
                    binding.rawFallbackText.text = response
                    binding.rawFallbackText.visibility = View.VISIBLE
                } else {
                    adapter.submitList(questions)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@QuestionGeneratorActivity,
                    getString(R.string.generation_failed, e.message ?: e.toString()),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun generateOfflineDraft(topic: String) {
        val subject = binding.subjectSpinner.selectedItem?.toString().orEmpty()
        val count = binding.countInput.text?.toString()?.trim()?.toIntOrNull() ?: 10

        val draft = DraftGenerator.questions(subject, topic, count.toString())
        lastOutput = draft
        // The template output doesn't follow the "Q1:" format QuestionParser
        // expects, so show it as raw text rather than trying to parse it.
        binding.rawFallbackText.text = draft
        binding.rawFallbackText.visibility = View.VISIBLE
        Toast.makeText(this, R.string.basic_draft_notice, Toast.LENGTH_LONG).show()
    }

    private fun buildPrompt(topic: String): String {
        val subject = binding.subjectSpinner.selectedItem?.toString().orEmpty()
        val classLevel = binding.classSpinner.selectedItem?.toString().orEmpty()
        val count = binding.countInput.text?.toString()?.trim()?.toIntOrNull() ?: 10
        val type = binding.typeSpinner.selectedItem?.toString().orEmpty()
        val difficulty = binding.difficultySpinner.selectedItem?.toString().orEmpty()
        val includeAnswers = binding.includeAnswersCheck.isChecked
        val includeExplanations = binding.includeExplanationsCheck.isChecked

        val formatNote = buildString {
            append("Format each question EXACTLY like this, with no extra commentary:\n")
            append("Q1: <question text>\n")
            if (type == "Multiple Choice") {
                append("A) <option>\nB) <option>\nC) <option>\nD) <option>\n")
            }
            if (includeAnswers) append("ANSWER: <correct answer>\n")
            if (includeExplanations) append("EXPLANATION: <one sentence>\n")
            append("Then continue with Q2, Q3, and so on.")
        }

        return """
            You are generating exam-style questions for a teacher.

            Subject: $subject
            Class: $classLevel
            Topic: $topic
            Number of questions: $count
            Question type: $type
            Difficulty: $difficulty

            $formatNote
        """.trimIndent()
    }

    private fun saveQuestions() {
        if (lastOutput.isBlank()) return
        File(File(filesDir, "questions").apply { mkdirs() }, "questions_${System.currentTimeMillis()}.txt").writeText(lastOutput)
        Toast.makeText(this, R.string.result_saved, Toast.LENGTH_SHORT).show()
    }

    private fun exportQuestions() {
        if (lastOutput.isBlank()) return
        val file = PdfExporter.write(this, "questions_${System.currentTimeMillis()}", lastOutput)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, getString(R.string.export_pdf)))
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.generateButton.isEnabled = !loading
    }
}
