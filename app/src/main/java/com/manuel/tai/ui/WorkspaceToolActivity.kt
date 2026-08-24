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
import com.manuel.tai.data.LocalStore
import com.manuel.tai.export.PdfExporter
import androidx.core.content.FileProvider
import com.manuel.tai.databinding.ActivityToolBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class WorkspaceToolActivity : AppCompatActivity() {
    private lateinit var binding: ActivityToolBinding
    private var mode = MODE_WORKSHEET
    private var lastResult = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_WORKSHEET
        binding.toolbar.setNavigationOnClickListener { finish() }
        configureForm()
        binding.generateButton.setOnClickListener { generate() }
        binding.saveButton.setOnClickListener { saveResult() }
        binding.shareButton.setOnClickListener { shareResult() }
        binding.exportButton.setOnClickListener { exportPdf() }
    }

    private fun configureForm() {
        val subjects = listOf("Mathematics", "English", "Biology", "Physics", "Chemistry", "Basic Science", "Social Studies")
        val classes = listOf("Primary 1", "Primary 2", "Primary 3", "Primary 4", "Primary 5", "Primary 6", "JSS 1", "JSS 2", "JSS 3", "SSS 1", "SSS 2", "SSS 3")
        binding.inputOneLabel.text = getString(R.string.label_subject)
        binding.inputTwoLabel.text = getString(R.string.label_class)
        binding.inputOne.setText(subjects.first())
        binding.inputTwo.setText(classes.first())
        binding.inputThreeLabel.text = getString(R.string.label_topic)
        binding.inputThree.hint = getString(R.string.hint_topic)
        binding.inputFourLabel.text = when (mode) {
            MODE_MARKING -> getString(R.string.label_mark_scheme)
            MODE_QUIZ -> getString(R.string.label_question_type)
            else -> getString(R.string.label_instructions)
        }
        binding.inputFour.hint = when (mode) {
            MODE_MARKING -> getString(R.string.hint_mark_scheme)
            MODE_QUIZ -> getString(R.string.hint_question_type)
            else -> getString(R.string.hint_instructions)
        }
        binding.inputFour.visibility = View.VISIBLE
        binding.inputFiveLabel.text = when (mode) {
            MODE_MARKING -> getString(R.string.label_student_answer)
            MODE_QUIZ -> getString(R.string.label_question_count)
            else -> getString(R.string.label_question_count)
        }
        binding.inputFive.hint = when (mode) {
            MODE_MARKING -> getString(R.string.hint_student_answer)
            MODE_QUIZ -> getString(R.string.hint_question_count)
            else -> getString(R.string.hint_question_count)
        }
        binding.studentNameCheck.visibility = if (mode == MODE_WORKSHEET) View.VISIBLE else View.GONE
        binding.answerKeyCheck.visibility = if (mode == MODE_WORKSHEET) View.VISIBLE else View.GONE
        binding.inputFour.setText(if (mode == MODE_QUIZ) "Multiple Choice" else "")
        binding.inputFive.setText(if (mode == MODE_QUIZ || mode == MODE_WORKSHEET) "5" else "")
        binding.inputOne.inputType = android.text.InputType.TYPE_CLASS_TEXT
        binding.inputTwo.inputType = android.text.InputType.TYPE_CLASS_TEXT
        binding.titleText.text = when (mode) {
            MODE_MARKING -> getString(R.string.marking_assistant_title)
            MODE_QUIZ -> getString(R.string.quiz_creator_title)
            else -> getString(R.string.worksheet_creator_title)
        }
        binding.generateButton.text = getString(R.string.generate)
    }

    private fun generate() {
        val one = binding.inputOne.text?.toString()?.trim().orEmpty()
        val two = binding.inputTwo.text?.toString()?.trim().orEmpty()
        val three = binding.inputThree.text?.toString()?.trim().orEmpty()
        val four = binding.inputFour.text?.toString()?.trim().orEmpty()
        val five = binding.inputFive.text?.toString()?.trim().orEmpty()
        if (three.isBlank() && mode != MODE_MARKING) {
            Toast.makeText(this, R.string.topic_required, Toast.LENGTH_SHORT).show()
            return
        }
        if (mode == MODE_MARKING && (three.isBlank() || five.isBlank())) {
            Toast.makeText(this, R.string.marking_inputs_required, Toast.LENGTH_SHORT).show()
            return
        }
        val query = listOf(three, four, five).filter { it.isNotBlank() }.joinToString(" ")
        val context = LocalStore.retrieve(this, query).joinToString("\n")
        val prompt = when (mode) {
            MODE_MARKING -> "Mark the student answer using the question and mark scheme. Question: $three\nMark scheme: $four\nStudent answer: $five\nLocal context: $context"
            MODE_QUIZ -> "Create a $four quiz on $three for $two. Generate $five questions. Use this local material if relevant: $context"
            else -> "Create a worksheet on $three for $two in $one. Instructions: $four. Use this local material if relevant: $context"
        }
        setLoading(true)
        lifecycleScope.launch {
            try {
                lastResult = if (ModelManager.isModelInstalled(this@WorkspaceToolActivity)) {
                    if (!LocalAiEngine.isReady()) LocalAiEngine.initialize(this@WorkspaceToolActivity, ModelManager.modelFile(this@WorkspaceToolActivity).absolutePath).getOrThrow()
                    withContext(Dispatchers.Default) { LocalAiEngine.generateResponse(prompt) }
                } else {
                    when (mode) {
                        MODE_MARKING -> DraftGenerator.marking(three, four, five)
                        MODE_QUIZ -> DraftGenerator.quiz(one, two, three, five, four.ifBlank { "Multiple Choice" }, context)
                        else -> DraftGenerator.worksheet(one, two, three, four, context, five, binding.studentNameCheck.isChecked, binding.answerKeyCheck.isChecked)
                    }
                }
                binding.resultText.text = lastResult
                binding.resultCard.visibility = View.VISIBLE
                if (!ModelManager.isModelInstalled(this@WorkspaceToolActivity)) Toast.makeText(this@WorkspaceToolActivity, R.string.basic_draft_notice, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@WorkspaceToolActivity, getString(R.string.generation_failed, e.message ?: e.toString()), Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.generateButton.isEnabled = !loading
    }

    private fun saveResult() {
        if (lastResult.isBlank()) return
        val dir = File(filesDir, "generated").apply { mkdirs() }
        File(dir, "${mode}_${System.currentTimeMillis()}.txt").writeText(lastResult)
        Toast.makeText(this, R.string.result_saved, Toast.LENGTH_SHORT).show()
    }

    private fun exportPdf() {
        if (lastResult.isBlank()) return
        val file = PdfExporter.write(this, "${mode}_${System.currentTimeMillis()}", lastResult)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Export PDF"))
    }

    private fun shareResult() {
        if (lastResult.isBlank()) return
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, lastResult)
        }, getString(R.string.share)))
    }

    companion object {
        const val EXTRA_MODE = "tool_mode"
        const val MODE_WORKSHEET = "worksheet"
        const val MODE_QUIZ = "quiz"
        const val MODE_MARKING = "marking"
    }
}
