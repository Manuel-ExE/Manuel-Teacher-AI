package com.manuel.tai.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.manuel.tai.DraftGenerator
import com.manuel.tai.R
import com.manuel.tai.ai.LocalAiEngine
import com.manuel.tai.ai.ModelManager
import com.manuel.tai.databinding.ActivityLessonPlannerBinding
import com.manuel.tai.export.PdfExporter
import com.manuel.tai.data.LocalStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File

class LessonPlannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonPlannerBinding
    private var lastResult: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLessonPlannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        setupSpinners()
        refreshModelStatus()

        binding.generateButton.setOnClickListener { onGenerateClicked() }
        binding.saveButton.setOnClickListener { saveLesson() }
        binding.shareButton.setOnClickListener { shareLesson() }
        binding.exportButton.setOnClickListener { exportLesson() }
    }

    override fun onResume() {
        super.onResume()
        refreshModelStatus()
    }

    private fun setupSpinners() {
        binding.subjectSpinner.adapter = simpleAdapter(
            listOf("Mathematics", "English", "Biology", "Physics", "Chemistry", "Basic Science", "Social Studies", "Agricultural Science")
        )
        binding.classSpinner.adapter = simpleAdapter(
            listOf("Primary 1", "Primary 2", "Primary 3", "Primary 4", "Primary 5", "Primary 6", "JSS 1", "JSS 2", "JSS 3", "SSS 1", "SSS 2", "SSS 3")
        )
        binding.curriculumSpinner.adapter = simpleAdapter(
            listOf("Nigerian Curriculum", "Cambridge", "British Curriculum", "General / Unspecified")
        )
        binding.difficultySpinner.adapter = simpleAdapter(
            listOf("Standard", "Foundational", "Advanced")
        )
    }

    private fun simpleAdapter(items: List<String>): ArrayAdapter<String> =
        ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

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

        if (!ModelManager.isModelInstalled(this)) {
            generateOfflineDraft(topic)
            return
        }

        val prompt = buildPrompt(topic)
        setLoading(true)

        lifecycleScope.launch {
            try {
                if (!LocalAiEngine.isReady()) {
                    val modelPath = ModelManager.modelFile(this@LessonPlannerActivity).absolutePath
                    LocalAiEngine.initialize(this@LessonPlannerActivity, modelPath).getOrThrow()
                }
                val response = LocalAiEngine.generateResponse(prompt)
                lastResult = response
                binding.resultText.text = response
                binding.resultCard.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(
                    this@LessonPlannerActivity,
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
        val classLevel = binding.classSpinner.selectedItem?.toString().orEmpty()
        val duration = binding.durationInput.text?.toString()?.trim().takeUnless { it.isNullOrEmpty() } ?: "40"

        val localContext = LocalStore.retrieve(this, topic).joinToString("\n")
        val draft = DraftGenerator.lesson(subject, classLevel, topic, "$duration minutes") + if (localContext.isBlank()) "" else "\n\nLocal material context:\n$localContext"
        lastResult = draft
        binding.resultText.text = draft
        binding.resultCard.visibility = View.VISIBLE
        Toast.makeText(this, R.string.basic_draft_notice, Toast.LENGTH_LONG).show()
    }

    private fun buildPrompt(topic: String): String {
        val subject = binding.subjectSpinner.selectedItem?.toString().orEmpty()
        val classLevel = binding.classSpinner.selectedItem?.toString().orEmpty()
        val duration = binding.durationInput.text?.toString()?.trim().takeUnless { it.isNullOrEmpty() } ?: "40"
        val curriculum = binding.curriculumSpinner.selectedItem?.toString().orEmpty()
        val difficulty = binding.difficultySpinner.selectedItem?.toString().orEmpty()
        val localContext = LocalStore.retrieve(this, topic).joinToString("\n")

        return """
            You are helping a teacher plan a lesson. Write a complete, classroom-ready lesson plan.

            Subject: $subject
            Class: $classLevel
            Topic: $topic
            Duration: $duration minutes
            Curriculum: $curriculum
            Difficulty: $difficulty
            Local teaching-material excerpts:
            $localContext

            Structure the lesson plan with these sections, each clearly labeled:
            Learning Objectives
            Introduction
            Explanation
            Examples
            Classroom Activity
            Assessment
            Homework
        """.trimIndent()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.generateButton.isEnabled = !loading
    }

    private fun saveLesson() {
        val text = lastResult ?: return
        val topic = binding.topicInput.text?.toString()?.trim()?.ifEmpty { "lesson" } ?: "lesson"
        val safeName = topic.replace(Regex("[^A-Za-z0-9]+"), "_").take(40)
        val dir = File(filesDir, "lessons").apply { mkdirs() }
        val file = File(dir, "${safeName}_${System.currentTimeMillis()}.txt")
        file.writeText(text)
        Toast.makeText(this, R.string.lesson_saved, Toast.LENGTH_SHORT).show()
    }

    private fun exportLesson() {
        val text = lastResult ?: return
        val file = PdfExporter.write(this, "lesson_${System.currentTimeMillis()}", text)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, getString(R.string.export_pdf)))
    }

    private fun shareLesson() {
        val text = lastResult ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }
}
