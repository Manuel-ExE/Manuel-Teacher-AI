package com.manuel.tai

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.manuel.tai.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val preferences by lazy { getSharedPreferences(PREFERENCES, Activity.MODE_PRIVATE) }

    private val materialPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Some document providers grant temporary access only; the URI is still recorded.
        }
        saveItem("material", uri.lastPathSegment ?: "Teaching material", uri.toString())
        binding.statusText.text = getString(R.string.material_imported, uri.lastPathSegment ?: "document")
        Toast.makeText(this, R.string.material_saved, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        refreshResourceCount()
        refreshSettingsStatus()

        binding.lessonButton.setOnClickListener { showLessonPlanner() }
        binding.questionButton.setOnClickListener { showQuestionGenerator() }
        binding.materialButton.setOnClickListener { materialPicker.launch(arrayOf("application/pdf")) }
        binding.settingsButton.setOnClickListener { showSettings() }
    }

    private fun showLessonPlanner() {
        val form = formContainer()
        val subject = field(getString(R.string.subject_hint))
        val classLevel = field(getString(R.string.class_hint))
        val topic = field(getString(R.string.topic_hint))
        val duration = field(getString(R.string.duration_hint))
        form.addViews(subject, classLevel, topic, duration)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.lesson_planner_title)
            .setView(form)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.generate_save) { _, _ ->
                val subjectText = subject.text.toString().trim().ifBlank { "General Studies" }
                val classText = classLevel.text.toString().trim().ifBlank { "Class" }
                val topicText = topic.text.toString().trim().ifBlank { "Today’s topic" }
                val durationText = duration.text.toString().trim().ifBlank { "40 minutes" }
                val draft = DraftGenerator.lesson(subjectText, classText, topicText, durationText)
                saveItem("lesson", "$subjectText: $topicText", draft)
                binding.statusText.text = getString(R.string.lesson_saved, topicText)
            }
            .show()
    }

    private fun showQuestionGenerator() {
        val form = formContainer()
        val subject = field(getString(R.string.subject_hint))
        val topic = field(getString(R.string.topic_hint))
        val count = field(getString(R.string.question_count_hint))
        form.addViews(subject, topic, count)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.question_generator_title)
            .setView(form)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.generate_save) { _, _ ->
                val subjectText = subject.text.toString().trim().ifBlank { "General Studies" }
                val topicText = topic.text.toString().trim().ifBlank { "Today’s topic" }
                val countText = count.text.toString().trim().ifBlank { "5" }
                val draft = DraftGenerator.questions(subjectText, topicText, countText)
                saveItem("questions", "$subjectText: $topicText", draft)
                binding.statusText.text = getString(R.string.questions_saved, topicText)
            }
            .show()
    }

    private fun showSettings() {
        val batterySaver = !preferences.getBoolean(KEY_BATTERY_SAVER, true)
        preferences.edit().putBoolean(KEY_BATTERY_SAVER, batterySaver).apply()
        refreshSettingsStatus()
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
    }

    private fun refreshSettingsStatus() {
        val enabled = preferences.getBoolean(KEY_BATTERY_SAVER, true)
        binding.settingsButton.text = if (enabled) getString(R.string.settings_battery_on) else getString(R.string.settings_battery_off)
        binding.statusText.text = if (enabled) getString(R.string.battery_mode_on) else getString(R.string.battery_mode_off)
    }

    private fun saveItem(type: String, title: String, content: String) {
        val items = try {
            JSONArray(preferences.getString(KEY_ITEMS, "[]"))
        } catch (_: Exception) {
            JSONArray()
        }
        items.put(JSONObject().apply {
            put("type", type)
            put("title", title)
            put("content", content)
            put("createdAt", System.currentTimeMillis())
        })
        preferences.edit().putString(KEY_ITEMS, items.toString()).apply()
        refreshResourceCount()
    }

    private fun refreshResourceCount() {
        val count = try { JSONArray(preferences.getString(KEY_ITEMS, "[]")).length() } catch (_: Exception) { 0 }
        binding.resourceCountText.text = getString(R.string.saved_resources, count)
    }

    private fun formContainer() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(48, 8, 48, 0)
    }

    private fun field(hint: String) = EditText(this).apply {
        this.hint = hint
        setSingleLine(true)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun LinearLayout.addViews(vararg views: EditText) = views.forEach { addView(it) }

    companion object {
        private const val PREFERENCES = "manuel_tai"
        private const val KEY_ITEMS = "saved_items"
        private const val KEY_BATTERY_SAVER = "battery_saver"
    }
}
