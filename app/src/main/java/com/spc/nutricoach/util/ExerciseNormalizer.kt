package com.spc.nutricoach.util

import java.util.Locale

object ExerciseNormalizer {

    /**
     * Normalizes an exercise name by converting it to lowercase and removing all whitespace.
     * E.g. "Press banca" -> "pressbanca", "preSSbanca" -> "pressbanca"
     */
    fun normalize(name: String): String {
        return name.lowercase().replace("\\s+".toRegex(), "")
    }

    /**
     * Nicely formats a name for the first time it is saved, ensuring standard capitalization and spacing.
     * E.g. "press  banca" -> "Press banca"
     */
    fun formatFirstTime(name: String): String {
        val clean = name.trim().replace("\\s+".toRegex(), " ")
        if (clean.isEmpty()) return ""
        return clean.lowercase().replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
    }

    /**
     * Looks through a history of exercise names (nombre_snapshot) and matches the new name
     * with any existing name (ignoring case and spaces).
     * If a match is found, returns the exact existing name from history.
     * Otherwise, returns the first-time formatted name.
     */
    fun getCanonicalName(newName: String, existingNames: Collection<String>): String {
        val normalizedNew = normalize(newName)
        val match = existingNames.firstOrNull { normalize(it) == normalizedNew }
        if (match != null) {
            return match
        }
        return formatFirstTime(newName)
    }

    /**
     * Extracts a list of unique canonical exercise names from a list of raw names in the history.
     * Groups them by their normalized form and selects the best representation for each group.
     */
    fun getCanonicalList(allNames: Collection<String>): List<String> {
        val grouped = allNames.groupBy { normalize(it) }
        return grouped.values.map { names ->
            // Find the best name in the group:
            // 1. First choice: Has spaces and starts with uppercase letter
            val withSpacesAndUpper = names.filter { it.firstOrNull()?.isUpperCase() == true && it.contains(" ") }
            if (withSpacesAndUpper.isNotEmpty()) return@map withSpacesAndUpper.first()
            
            // 2. Second choice: Starts with uppercase letter
            val withUpper = names.filter { it.firstOrNull()?.isUpperCase() == true }
            if (withUpper.isNotEmpty()) return@map withUpper.first()
            
            // 3. Otherwise: Format the first name nicely
            formatFirstTime(names.firstOrNull() ?: "")
        }.filter { it.isNotEmpty() }.distinct().sorted()
    }
}
