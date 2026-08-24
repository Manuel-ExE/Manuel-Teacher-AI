package com.manuel.tai

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.manuel.tai.ai.ModelManager
import com.manuel.tai.databinding.ActivityMainBinding
import com.manuel.tai.ui.LessonPlannerActivity
import com.manuel.tai.ui.QuestionGeneratorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var savedResources = 0

    private val materialPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            savedResources += 1
            getPreferences(Activity.MODE_PRIVATE).edit().putInt("resource_count", savedResources).apply()
            binding.resourceCountText.text = getString(R.string.saved_resources, savedResources)
            binding.statusText.text = getString(R.string.material_imported, uri.lastPathSegment ?: "document")
            Toast.makeText(this, R.string.material_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private val modelPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) importModel(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedResources = getPreferences(Activity.MODE_PRIVATE).getInt("resource_count", 0)
        binding.resourceCountText.text = getString(R.string.saved_resources, savedResources)

        binding.lessonButton.setOnClickListener {
            startActivity(Intent(this, LessonPlannerActivity::class.java))
        }

        binding.questionButton.setOnClickListener {
            startActivity(Intent(this, QuestionGeneratorActivity::class.java))
        }

        binding.materialButton.setOnClickListener {
            materialPicker.launch("application/pdf")
        }

        binding.settingsButton.setOnClickListener {
            binding.statusText.text = getString(R.string.settings_summary)
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
        refreshModelStatus()
    }

    private fun refreshModelStatus() {
        binding.modelStatusText.text = if (ModelManager.isModelInstalled(this)) {
            getString(R.string.model_ready, ModelManager.modelSizeMb(this))
        } else {
            getString(R.string.model_not_loaded)
        }
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
