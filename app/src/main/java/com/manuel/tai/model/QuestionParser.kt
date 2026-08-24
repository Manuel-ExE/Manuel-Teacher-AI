package com.manuel.tai.model

/**
 * Parses the local model's plain-text question output into [QuestionItem]s.
 *
 * Expects a format like:
 *   Q1: What is 3/4 + 1/4?
 *   A) 1/2
 *   B) 1
 *   C) 3/8
 *   D) 4/8
 *   ANSWER: B
 *   EXPLANATION: Add the numerators since denominators match.
 *
 * The prompt sent to the model (see QuestionGeneratorActivity) asks for
 * exactly this format. If the model doesn't follow it closely, parsing may
 * return fewer items than requested — callers should fall back to showing
 * the raw text when the result is empty.
 */
object QuestionParser {

    private val questionStart = Regex("""^Q\s*(\d+)\s*[:.)]\s*(.*)$""", RegexOption.IGNORE_CASE)
    private val optionLine = Regex("""^([A-D])\s*[).]\s*(.*)$""", RegexOption.IGNORE_CASE)
    private val answerLine = Regex("""^ANSWER\s*[:.]\s*(.*)$""", RegexOption.IGNORE_CASE)
    private val explanationLine = Regex("""^EXPLANATION\s*[:.]\s*(.*)$""", RegexOption.IGNORE_CASE)

    fun parse(raw: String): List<QuestionItem> {
        val items = mutableListOf<QuestionItem>()

        var number = 0
        var promptText: StringBuilder? = null
        var options = mutableListOf<String>()
        var answer: String? = null
        var explanation: String? = null

        fun flush() {
            val text = promptText?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                items.add(QuestionItem(number, text, options.toList(), answer, explanation))
            }
            promptText = null
            options = mutableListOf()
            answer = null
            explanation = null
        }

        raw.lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            val qMatch = questionStart.find(line)
            val oMatch = optionLine.find(line)
            val aMatch = answerLine.find(line)
            val eMatch = explanationLine.find(line)

            when {
                qMatch != null -> {
                    flush()
                    number = qMatch.groupValues[1].toIntOrNull() ?: (items.size + 1)
                    promptText = StringBuilder(qMatch.groupValues[2])
                }
                oMatch != null -> {
                    options.add("${oMatch.groupValues[1].uppercase()}) ${oMatch.groupValues[2]}")
                }
                aMatch != null -> {
                    answer = aMatch.groupValues[1].trim()
                }
                eMatch != null -> {
                    explanation = eMatch.groupValues[1].trim()
                }
                promptText != null -> {
                    // Continuation of a question that wrapped onto multiple lines.
                    promptText?.append(' ')?.append(line)
                }
            }
        }
        flush()

        return items
    }
}
