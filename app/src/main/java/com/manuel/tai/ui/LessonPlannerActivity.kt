package com.manuel.tai.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.manuel.tai.R
import com.manuel.tai.ai.LocalAiEngine
import com.manuel.tai.ai.ModelManager
import com.manuel.tai.databinding.ActivityLessonPlannerBinding
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
        binding.generateButton.isEnabled = installed
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

    private fun buildPrompt(topic: String): String {
        val subject = binding.subjectSpinner.selectedItem?.toString().orEmpty()
        val classLevel = binding.classSpinner.selectedItem?.toString().orEmpty()
        val duration = binding.durationInput.text?.toString()?.trim().takeUnless { it.isNullOrEmpty() } ?: "40"
        val curriculum = binding.curriculumSpinner.selectedItem?.toString().orEmpty()
        val difficulty = binding.difficultySpinner.selectedItem?.toString().orEmpty()

        return """
            You are helping a teacher plan a lesson. Write a complete, classroom-ready lesson plan.

            Subject: $subject
            Class: $classLevel
            Topic: $topic
            Duration: $duration minutes
            Curriculum: $curriculum
            Difficulty: $difficulty

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
        binding.generateButton.isEnabled = !loading && ModelManager.isModelInstalled(this)
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

    private fun shareLesson() {
        val text = lastResult ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }
}
