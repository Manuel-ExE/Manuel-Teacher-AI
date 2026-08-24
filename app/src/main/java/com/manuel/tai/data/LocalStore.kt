package com.manuel.tai.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

object LocalStore {
    data class Student(val id: Long, val name: String, val className: String, val notes: String)
    data class Material(val id: Long, val title: String, val text: String)

    private const val PREFS = "manueltai_local_store"
    private const val STUDENTS = "students"
    private const val MATERIALS = "materials"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun students(context: Context): List<Student> {
        val array = JSONArray(prefs(context).getString(STUDENTS, "[]"))
        return (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.let {
                Student(it.optLong("id"), it.optString("name"), it.optString("className"), it.optString("notes"))
            }
        }.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    fun addStudent(context: Context, name: String, className: String, notes: String) {
        val array = JSONArray(prefs(context).getString(STUDENTS, "[]"))
        array.put(JSONObject().apply {
            put("id", System.currentTimeMillis())
            put("name", name)
            put("className", className)
            put("notes", notes)
        })
        prefs(context).edit().putString(STUDENTS, array.toString()).apply()
    }

    fun materials(context: Context): List<Material> {
        val array = JSONArray(prefs(context).getString(MATERIALS, "[]"))
        return (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.let {
                Material(it.optLong("id"), it.optString("title"), it.optString("text"))
            }
        }.sortedByDescending { it.id }
    }

    fun addMaterial(context: Context, title: String, text: String) {
        val array = JSONArray(prefs(context).getString(MATERIALS, "[]"))
        array.put(JSONObject().apply {
            put("id", System.currentTimeMillis())
            put("title", title)
            put("text", text)
        })
        prefs(context).edit().putString(MATERIALS, array.toString()).apply()
    }

    /** Returns the most relevant local excerpts using simple term-frequency scoring. */
    fun retrieve(context: Context, query: String, limit: Int = 3): List<String> {
        val terms = query.lowercase(Locale.getDefault())
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length > 2 }
            .toSet()
        return materials(context).flatMap { material ->
            material.text.split(Regex("(?<=[.!?])\\s+|\\n{2,}"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { excerpt ->
                    val lower = excerpt.lowercase(Locale.getDefault())
                    val score = terms.count { lower.contains(it) }
                    Triple(score, material.title, excerpt)
                }
        }.filter { it.first > 0 }
            .sortedByDescending { it.first }
            .take(limit)
            .map { "${it.second}: ${it.third}" }
    }
}
