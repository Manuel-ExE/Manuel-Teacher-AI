package com.manuel.tai

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.manuel.tai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var savedResources = 0

    private val materialPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            savedResources += 1
            binding.resourceCountText.text = getString(R.string.saved_resources, savedResources)
            binding.statusText.text = getString(R.string.material_imported, uri.lastPathSegment ?: "document")
            Toast.makeText(this, R.string.material_saved, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedResources = getPreferences(Activity.MODE_PRIVATE).getInt("resource_count", 0)
        binding.resourceCountText.text = getString(R.string.saved_resources, savedResources)

        binding.lessonButton.setOnClickListener {
            binding.statusText.text = getString(R.string.lesson_draft_ready)
            savedResources += 1
            persistResourceCount()
        }

        binding.questionButton.setOnClickListener {
            binding.statusText.text = getString(R.string.question_draft_ready)
            savedResources += 1
            persistResourceCount()
        }

        binding.materialButton.setOnClickListener {
            materialPicker.launch("application/pdf")
        }

        binding.settingsButton.setOnClickListener {
            binding.statusText.text = getString(R.string.settings_summary)
        }
    }

    private fun persistResourceCount() {
        getPreferences(Activity.MODE_PRIVATE)
            .edit()
            .putInt("resource_count", savedResources)
            .apply()
        binding.resourceCountText.text = getString(R.string.saved_resources, savedResources)
    }
}
