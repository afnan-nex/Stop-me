package com.afnan.stopme.domain.usecase

import javax.inject.Inject

data class ChallengeWordCheckResult(
    val matchedWords: Int,
    val totalWords: Int,
    val errorMessage: String? = null,
    val isFullyMatched: Boolean = false
)

/**
 * Validates the user's challenge text against the expected paragraph word-by-word in real time.
 *
 * Rules:
 *  - Strips surrounding punctuation from words for comparison.
 *  - Checks completed words and flags errors (e.g. "Word #X mismatch: expected "...", got "..."" or "Too many extra words").
 *  - Checks in-progress words (prefix match) so as not to flag mistakes before a word is finished typing.
 *  - Flags isFullyMatched = true only when all words match exactly.
 */
class ValidateChallengeTextUseCase @Inject constructor() {

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    operator fun invoke(expected: String, userInput: String): ValidationResult {
        val check = checkRealtime(expected, userInput)
        return if (check.isFullyMatched) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(check.errorMessage ?: "Please type the complete text.")
        }
    }

    /**
     * Real-time word-by-word checker for live UI feedback.
     */
    fun checkRealtime(expected: String, userInput: String): ChallengeWordCheckResult {
        val targetWords = cleanTokens(expected)
        val totalWords = targetWords.size
        if (totalWords == 0) {
            return ChallengeWordCheckResult(0, 0, isFullyMatched = true)
        }

        val trimmedInput = userInput.trimStart()
        if (trimmedInput.isEmpty()) {
            return ChallengeWordCheckResult(
                matchedWords = 0,
                totalWords = totalWords,
                errorMessage = null,
                isFullyMatched = false
            )
        }

        // Determine whether user is currently typing a word (no trailing space)
        val isInProgressWord = !userInput.endsWith(" ") && !userInput.endsWith("\n")
        val inputTokens = cleanTokens(userInput)

        if (inputTokens.size > targetWords.size) {
            return ChallengeWordCheckResult(
                matchedWords = targetWords.size,
                totalWords = totalWords,
                errorMessage = "Too many extra words.",
                isFullyMatched = false
            )
        }

        var matchedCount = 0

        for (i in inputTokens.indices) {
            val inputWord = inputTokens[i]
            val expectedWord = targetWords[i]
            val isLastWord = (i == inputTokens.lastIndex)

            if (isLastWord && isInProgressWord) {
                // In-progress word -> check prefix match
                if (!expectedWord.startsWith(inputWord, ignoreCase = true)) {
                    return ChallengeWordCheckResult(
                        matchedWords = matchedCount,
                        totalWords = totalWords,
                        errorMessage = "Word #${i + 1} mismatch: expected \"$expectedWord\", got \"$inputWord\"",
                        isFullyMatched = false
                    )
                }
                // If it starts with, it's valid so far, but not yet a fully completed word match (unless exactly equal)
                if (inputWord.equals(expectedWord, ignoreCase = true)) {
                    matchedCount++
                }
            } else {
                // Completed word -> must match exactly
                if (!inputWord.equals(expectedWord, ignoreCase = true)) {
                    return ChallengeWordCheckResult(
                        matchedWords = matchedCount,
                        totalWords = totalWords,
                        errorMessage = "Word #${i + 1} mismatch: expected \"$expectedWord\", got \"$inputWord\"",
                        isFullyMatched = false
                    )
                }
                matchedCount++
            }
        }

        val isFullyMatched = (matchedCount == totalWords && inputTokens.size == totalWords && !isInProgressWord || (matchedCount == totalWords && inputTokens.size == totalWords && inputTokens.last().equals(targetWords.last(), ignoreCase = true)))

        return ChallengeWordCheckResult(
            matchedWords = matchedCount,
            totalWords = totalWords,
            errorMessage = null,
            isFullyMatched = isFullyMatched
        )
    }

    private fun cleanTokens(text: String): List<String> {
        return text
            .trim()
            .split(Regex("\\s+"))
            .map { it.replace(Regex("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$"), "") } // Strip leading/trailing punctuation
            .filter { it.isNotEmpty() }
    }
}
