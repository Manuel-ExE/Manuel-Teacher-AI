package com.manuel.tai.model

data class QuestionItem(
    val number: Int,
    val prompt: String,
    val options: List<String> = emptyList(),
    val answer: String? = null,
    val explanation: String? = null
)
