package com.manuel.tai

object DraftGenerator {
    fun lesson(subject: String, classLevel: String, topic: String, duration: String): String = buildString {
        appendLine("Lesson plan")
        appendLine("Subject: $subject")
        appendLine("Class: $classLevel")
        appendLine("Topic: $topic")
        appendLine("Duration: $duration")
        appendLine()
        appendLine("Learning objective")
        appendLine("Learners will explain the main idea of $topic and apply it to one practical example.")
        appendLine()
        appendLine("Lesson sequence")
        appendLine("1. Starter: ask learners what they already know about $topic.")
        appendLine("2. Explain the key idea using a familiar classroom example.")
        appendLine("3. Pair activity: learners solve one short $subject task.")
        appendLine("4. Review: invite two learners to share their reasoning.")
        appendLine("5. Exit ticket: write one thing learned and one question remaining.")
        appendLine()
        appendLine("Assessment")
        appendLine("Check participation, accuracy of the example, and the exit-ticket response.")
    }

    fun questions(subject: String, topic: String, requestedCount: String): String = buildString {
        val count = requestedCount.toIntOrNull()?.coerceIn(1, 20) ?: 5
        appendLine("Question set")
        appendLine("Subject: $subject")
        appendLine("Topic: $topic")
        appendLine("Question count: $count")
        appendLine()
        val prompts = listOf(
            "Define $topic in your own words.",
            "Give one real-world application of $topic.",
            "Compare two important ideas connected to $topic.",
            "Explain how you would check whether an answer about $topic is correct.",
            "Write one question you would ask to investigate $topic further."
        )
        repeat(count) { index ->
            val prompt = if (index < prompts.size) prompts[index] else "Describe one new example, evidence source, or application related to $topic."
            appendLine("${index + 1}. $prompt")
            appendLine("Answer guide: accept a relevant response with a clear explanation.")
            appendLine()
        }
    }
}
