package com.manuel.tai

import org.junit.Assert.assertTrue
import org.junit.Test

class DraftGeneratorExtendedTest {
    @Test
    fun worksheetIncludesInstructionsAndTopic() {
        val result = DraftGenerator.worksheet("Science", "Primary 5", "Plants", "Show your working", "chlorophyll")
        assertTrue(result.contains("WORKSHEET: Plants"))
        assertTrue(result.contains("Show your working"))
        assertTrue(result.contains("chlorophyll"))
    }

    @Test
    fun quizIncludesAnswerKeyAndCount() {
        val result = DraftGenerator.quiz("Maths", "JSS 1", "Fractions", "3", "Multiple Choice", "numerator")
        assertTrue(result.contains("QUIZ: Fractions"))
        assertTrue(result.contains("3. "))
        assertTrue(result.contains("Answer: B"))
    }

    @Test
    fun markingProvidesAConservativeTeacherAidResult() {
        val result = DraftGenerator.marking("What is photosynthesis?", "sunlight chlorophyll glucose", "Plants use sunlight and chlorophyll to make glucose")
        assertTrue(result.contains("MARKING RESULT"))
        assertTrue(result.contains("Strong response"))
        assertTrue(result.contains("final professional judgement"))
    }
}
