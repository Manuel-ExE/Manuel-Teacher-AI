package com.manuel.tai.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.manuel.tai.R
import com.manuel.tai.data.LocalStore

class MaterialsActivity : AppCompatActivity() {
    private lateinit var list: LinearLayout
    private lateinit var search: TextInputEditText
    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) importText(uri) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materials)
        PDFBoxResourceLoader.init(applicationContext)
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        list = findViewById(R.id.materialsList)
        search = findViewById(R.id.materialSearch)
        findViewById<MaterialButton>(R.id.importMaterialButton).setOnClickListener { picker.launch("*/*") }
        findViewById<MaterialButton>(R.id.saveMaterialButton).setOnClickListener { saveMaterial() }
        search.setOnEditorActionListener { _, _, _ -> render(); true }
        render()
    }
    private fun importText(uri: Uri) {
        lifecycleScope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { input ->
                        val isPdf = contentResolver.getType(uri) == "application/pdf" || uri.toString().lowercase().contains(".pdf")
                        if (isPdf) PDDocument.load(input).use { PDFTextStripper().getText(it) }
                        else input.bufferedReader().use { it.readText() }
                    }.orEmpty()
                }
                if (text.isBlank()) { Toast.makeText(this@MaterialsActivity, R.string.material_text_required, Toast.LENGTH_SHORT).show(); return@launch }
                findViewById<TextInputEditText>(R.id.materialTitle).setText(uri.lastPathSegment ?: "Imported material")
                findViewById<TextInputEditText>(R.id.materialText).setText(text)
            } catch (e: Exception) {
                Toast.makeText(this@MaterialsActivity, getString(R.string.material_import_error, e.message ?: e.toString()), Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun saveMaterial() {
        val title = findViewById<TextInputEditText>(R.id.materialTitle).text?.toString()?.trim().orEmpty()
        val text = findViewById<TextInputEditText>(R.id.materialText).text?.toString()?.trim().orEmpty()
        if (title.isBlank() || text.isBlank()) { Toast.makeText(this, R.string.material_text_required, Toast.LENGTH_SHORT).show(); return }
        LocalStore.addMaterial(this, title, text)
        findViewById<TextInputEditText>(R.id.materialTitle).text?.clear()
        findViewById<TextInputEditText>(R.id.materialText).text?.clear()
        render()
    }
    private fun render() {
        list.removeAllViews()
        val query = search.text?.toString()?.trim()?.lowercase().orEmpty()
        LocalStore.materials(this).filter { query.isBlank() || it.title.lowercase().contains(query) || it.text.lowercase().contains(query) }.forEach { material ->
            val view = layoutInflater.inflate(R.layout.item_material, list, false)
            view.findViewById<TextView>(R.id.materialTitleText).text = material.title
            view.findViewById<TextView>(R.id.materialPreviewText).text = material.text.take(220)
            list.addView(view)
        }
    }
}
