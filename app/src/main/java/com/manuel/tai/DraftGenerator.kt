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

    fun worksheet(subject: String, classLevel: String, topic: String, task: String, context: String, countText: String = "4", includeStudentName: Boolean = true, includeAnswerKey: Boolean = true): String = buildString {
        val count = countText.toIntOrNull()?.coerceIn(1, 30) ?: 4
        appendLine("WORKSHEET: $topic")
        appendLine("Subject: $subject | Class: $classLevel")
        if (includeStudentName) appendLine("Student name: ______________________________")
        appendLine()
        appendLine("Instructions")
        appendLine(task.ifBlank { "Answer every question and show your reasoning." })
        appendLine()
        appendLine("Questions")
        appendLine("1. State the main idea of $topic in your own words.")
        appendLine("2. Give one example of $topic from class or everyday life.")
        appendLine("3. Apply $topic to a new situation and explain your answer.")
        appendLine("4. What common mistake should a learner avoid when working with $topic?")
        if (count > 4) repeat(count - 4) { index -> appendLine("${index + 5}. Give another example or application of $topic.") }
        appendLine()
        appendLine("Teacher reference")
        if (includeAnswerKey) appendLine("Answer key: accept accurate vocabulary, a clear explanation, and a relevant example.")
        else appendLine("Answer key: teacher-only key omitted from this copy.")
        if (context.isNotBlank()) appendLine("\nLocal material context:\n$context")
    }

    fun quiz(subject: String, classLevel: String, topic: String, countText: String, type: String, context: String): String = buildString {
        val count = countText.toIntOrNull()?.coerceIn(1, 20) ?: 5
        appendLine("QUIZ: $topic")
        appendLine("Subject: $subject | Class: $classLevel | Type: $type")
        appendLine()
        repeat(count) { index ->
            val n = index + 1
            val prompt = when (type) {
                "True/False" -> "True or False: $topic includes an idea that can be explained with evidence."
                "Short Answer" -> "Explain one important idea about $topic."
                else -> "Which statement best describes an important idea about $topic?\nA) It is unrelated to evidence\nB) It can be explained and applied\nC) It has no examples\nD) It cannot be discussed"
            }
            appendLine("$n. $prompt")
            appendLine("Answer: ${if (type == "Multiple Choice") "B" else "A relevant, accurate explanation"}")
            appendLine()
        }
        if (context.isNotBlank()) appendLine("Local material context:\n$context")
    }

    fun assistant(question: String, context: String): String = buildString {
        appendLine("Offline Teacher Assistant")
        appendLine()
        appendLine("You asked: $question")
        appendLine()
        if (context.isBlank()) {
            appendLine("I can help you plan a lesson, create questions, prepare a worksheet, or draft feedback. Add teaching text to Materials Library if you want answers grounded in your school documents.")
        } else {
            appendLine("Relevant local material")
            appendLine(context)
            appendLine()
            appendLine("Suggested next step")
            appendLine("Use the relevant excerpt above to shape a lesson objective, classroom activity, assessment, or follow-up question. The teacher remains responsible for checking accuracy and suitability.")
        }
    }

    fun marking(question: String, markScheme: String, studentAnswer: String): String = buildString {
        val expected = markScheme.trim().split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length > 3 }.map { it.lowercase() }.toSet()
        val answer = studentAnswer.lowercase()
        val matched = expected.count { answer.contains(it) }
        val score = if (expected.isEmpty()) 0 else ((matched.toDouble() / expected.size) * 100).toInt()
        val judgement = when {
            score >= 80 -> "Strong response"
            score >= 50 -> "Partially correct"
            else -> "Needs support"
        }
        appendLine("MARKING RESULT")
        appendLine("Judgement: $judgement")
        appendLine("Indicative coverage: $score% ($matched of ${expected.size} key terms found)")
        appendLine()
        appendLine("Feedback")
        appendLine(if (score >= 80) "The answer addresses the main points. Encourage the learner to add evidence or an example where useful." else "Review the key ideas in the mark scheme, then ask the learner to add a clearer explanation and supporting example.")
        appendLine()
        appendLine("Question: $question")
        appendLine("Mark scheme: $markScheme")
        appendLine("Student answer: $studentAnswer")
        appendLine()
        appendLine("Important: this is an offline drafting aid. The teacher makes the final professional judgement.")
    }
}
