package com.afnan.stopme.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateChallengeTextUseCaseTest {

    private lateinit var useCase: ValidateChallengeTextUseCase

    private val target = "Every minute I spend scrolling is a minute I will never reclaim. " +
        "The content disappearing from my screen in seconds demanded my full attention, " +
        "yet I will recall none of it tomorrow. I am trading the finite hours of my life " +
        "for a habit that gives nothing back."

    @Before
    fun setUp() {
        useCase = ValidateChallengeTextUseCase()
    }

    @Test
    fun `exact match returns Success`() {
        val result = useCase(target, target)
        assertTrue(result is ValidateChallengeTextUseCase.ValidationResult.Success)
    }

    @Test
    fun `checkRealtime exact match returns isFullyMatched true`() {
        val check = useCase.checkRealtime(target, target)
        assertTrue(check.isFullyMatched)
        assertNull(check.errorMessage)
        assertEquals(check.totalWords, check.matchedWords)
    }

    @Test
    fun `checkRealtime in-progress word without error`() {
        // Typing "Every min" (middle of the second word "minute")
        val check = useCase.checkRealtime(target, "Every min")
        assertNull(check.errorMessage)
        assertFalse(check.isFullyMatched)
        assertEquals(1, check.matchedWords) // "Every" is matched, "min" is in progress
    }

    @Test
    fun `checkRealtime word mismatch flags exact word error`() {
        // Typing "Every hour" instead of "Every minute"
        val check = useCase.checkRealtime(target, "Every hour")
        assertNotNull(check.errorMessage)
        assertTrue(check.errorMessage!!.contains("Word #2 mismatch"))
        assertTrue(check.errorMessage!!.contains("minute"))
        assertTrue(check.errorMessage!!.contains("hour"))
        assertFalse(check.isFullyMatched)
    }

    @Test
    fun `checkRealtime too many words flags error`() {
        val longText = (target + " extra words are typed here and there").trim()
        val check = useCase.checkRealtime(target, longText)
        assertNotNull(check.errorMessage)
        assertTrue(check.errorMessage!!.contains("Too many extra words"))
        assertFalse(check.isFullyMatched)
    }

    @Test
    fun `checkRealtime empty input returns 0 matches without error`() {
        val check = useCase.checkRealtime(target, "")
        assertNull(check.errorMessage)
        assertFalse(check.isFullyMatched)
        assertEquals(0, check.matchedWords)
    }
}
