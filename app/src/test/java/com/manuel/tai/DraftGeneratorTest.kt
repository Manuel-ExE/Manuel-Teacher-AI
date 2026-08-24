package com.manuel.tai

import org.junit.Assert.assertTrue
import org.junit.Test

class DraftGeneratorTest {
    @Test
    fun lessonDraftIncludesTeacherInputs() {
        val result = DraftGenerator.lesson("Science", "Grade 5", "Plants", "40 minutes")
        assertTrue(result.contains("Science"))
        assertTrue(result.contains("Grade 5"))
        assertTrue(result.contains("Plants"))
        assertTrue(result.contains("40 minutes"))
    }

    @Test
    fun questionDraftIncludesSubjectAndTopic() {
        val result = DraftGenerator.questions("Mathematics", "Fractions", "5")
        assertTrue(result.contains("Mathematics"))
        assertTrue(result.contains("Fractions"))
        assertTrue(result.contains("Question count: 5"))
    }
}
