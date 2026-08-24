package com.manuel.tai.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.manuel.tai.R
import com.manuel.tai.data.LocalStore

class StudentRecordsActivity : AppCompatActivity() {
    private lateinit var list: LinearLayout
    private lateinit var search: TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_records)
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        search = findViewById(R.id.searchInput)
        list = findViewById(R.id.recordsList)
        findViewById<MaterialButton>(R.id.addStudentButton).setOnClickListener { addStudent() }
        search.setOnEditorActionListener { _, _, _ -> render(); true }
        render()
    }
    private fun addStudent() {
        val name = findViewById<TextInputEditText>(R.id.nameInput).text?.toString()?.trim().orEmpty()
        val className = findViewById<TextInputEditText>(R.id.classInput).text?.toString()?.trim().orEmpty()
        val notes = findViewById<TextInputEditText>(R.id.notesInput).text?.toString()?.trim().orEmpty()
        if (name.isBlank() || className.isBlank()) { Toast.makeText(this, R.string.student_required, Toast.LENGTH_SHORT).show(); return }
        LocalStore.addStudent(this, name, className, notes)
        findViewById<TextInputEditText>(R.id.nameInput).text?.clear()
        findViewById<TextInputEditText>(R.id.classInput).text?.clear()
        findViewById<TextInputEditText>(R.id.notesInput).text?.clear()
        render()
    }
    private fun render() {
        list.removeAllViews()
        val query = search.text?.toString()?.trim()?.lowercase().orEmpty()
        LocalStore.students(this).filter { query.isBlank() || listOf(it.name, it.className, it.notes).any { value -> value.lowercase().contains(query) } }.forEach { student ->
            val card = layoutInflater.inflate(R.layout.item_student_record, list, false)
            card.findViewById<TextView>(R.id.studentName).text = student.name
            card.findViewById<TextView>(R.id.studentClass).text = student.className
            card.findViewById<TextView>(R.id.studentNotes).apply { text = student.notes; visibility = if (student.notes.isBlank()) View.GONE else View.VISIBLE }
            list.addView(card)
        }
    }
}
