package com.example.util

import java.text.Normalizer
import java.util.Locale

object StringUtils {
    /**
     * Strips diacritical marks (accents) and converts to lowercase.
     * E.g. "Câble USB-C" -> "cable usb-c"
     */
    fun normalizeForSearch(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase(Locale.ROOT).trim()
    }

    /**
     * Returns true if candidate contains query, ignoring case and accents.
     */
    fun matchesFuzzy(candidate: String?, query: String?): Boolean {
        if (query.isNullOrBlank()) return true
        if (candidate.isNullOrBlank()) return false
        val normalizedCandidate = normalizeForSearch(candidate)
        val normalizedQuery = normalizeForSearch(query)
        return normalizedCandidate.contains(normalizedQuery)
    }
}
