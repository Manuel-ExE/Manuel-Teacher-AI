package com.manuel.tai

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import com.manuel.tai.ai.ModelManager
import com.manuel.tai.data.LocalStore
import com.manuel.tai.databinding.ActivityMainBinding
import com.manuel.tai.ui.LessonPlannerActivity
import com.manuel.tai.ui.MaterialsActivity
import com.manuel.tai.ui.QuestionGeneratorActivity
import com.manuel.tai.ui.SettingsActivity
import com.manuel.tai.ui.StudentRecordsActivity
import com.manuel.tai.ui.WorkspaceToolActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var savedResources = 0

    private val modelPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) importModel(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedResources = LocalStore.materials(this).size
        binding.resourceCountText.text = getString(R.string.saved_resources, savedResources)

        binding.lessonButton.setOnClickListener {
            startActivity(Intent(this, LessonPlannerActivity::class.java))
        }

        binding.questionButton.setOnClickListener {
            startActivity(Intent(this, QuestionGeneratorActivity::class.java))
        }

        binding.materialButton.setOnClickListener {
            startActivity(Intent(this, MaterialsActivity::class.java))
        }

        binding.worksheetButton.setOnClickListener { openWorkspaceTool(WorkspaceToolActivity.MODE_WORKSHEET) }
        binding.quizButton.setOnClickListener { openWorkspaceTool(WorkspaceToolActivity.MODE_QUIZ) }
        binding.markingButton.setOnClickListener { openWorkspaceTool(WorkspaceToolActivity.MODE_MARKING) }
        binding.recordsButton.setOnClickListener { startActivity(Intent(this, StudentRecordsActivity::class.java)) }
        binding.assistantButton.setOnClickListener { startActivity(Intent(this, com.manuel.tai.ui.AssistantActivity::class.java)) }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.importModelButton.setOnClickListener {
            // .task model files usually have no registered MIME type, so we
            // accept any file and let the copy step fail gracefully if it's
            // not a real model file.
            modelPicker.launch("*/*")
        }

        refreshModelStatus()
    }

    override fun onResume() {
        super.onResume()
        savedResources = LocalStore.materials(this).size
        binding.resourceCountText.text = getString(R.string.saved_resources, savedResources)
        refreshModelStatus()
    }

    private data class StatusStyle(
        val label: String,
        val dotColorRes: Int,
        val pillColorRes: Int,
        val textColorRes: Int
    )

    private fun refreshModelStatus() {
        val installed = ModelManager.isModelInstalled(this)

        binding.modelStatusText.text = if (installed) {
            getString(R.string.model_ready, ModelManager.modelSizeMb(this))
        } else {
            getString(R.string.model_not_loaded)
        }

        val style = if (installed) {
            StatusStyle(
                label = getString(R.string.ai_status_ready),
                dotColorRes = R.color.status_ready,
                pillColorRes = R.color.status_ready_soft,
                textColorRes = R.color.status_ready
            )
        } else {
            StatusStyle(
                label = getString(R.string.ai_status_template),
                dotColorRes = R.color.status_template,
                pillColorRes = R.color.status_template_soft,
                textColorRes = R.color.status_template
            )
        }

        binding.aiStatusBadge.text = style.label
        binding.aiStatusBadge.setTextColor(ContextCompat.getColor(this, style.textColorRes))
        DrawableCompat.setTint(
            binding.aiStatusBadge.background.mutate(),
            ContextCompat.getColor(this, style.pillColorRes)
        )
        DrawableCompat.setTint(
            binding.aiStatusDot.background.mutate(),
            ContextCompat.getColor(this, style.dotColorRes)
        )
    }

    private fun openWorkspaceTool(mode: String) {
        startActivity(Intent(this, WorkspaceToolActivity::class.java).putExtra(WorkspaceToolActivity.EXTRA_MODE, mode))
    }

    private fun importModel(uri: android.net.Uri) {
        binding.modelStatusText.text = getString(R.string.model_importing)
        binding.importModelButton.isEnabled = false

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                ModelManager.importModel(this@MainActivity, uri)
            }
            binding.importModelButton.isEnabled = true
            result.onSuccess {
                refreshModelStatus()
                Toast.makeText(this@MainActivity, "Model imported", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                binding.modelStatusText.text = getString(R.string.model_import_failed, e.message ?: e.toString())
            }
        }
    }
}
