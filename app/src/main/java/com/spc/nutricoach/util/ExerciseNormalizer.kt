package com.spc.nutricoach.util

import java.util.Locale

object ExerciseNormalizer {

    /**
     * Normaliza el nombre de un ejercicio convirtiéndolo a minúsculas y eliminando todos los espacios en blanco.
     * Ej. "Press banca" -> "pressbanca", "preSSbanca" -> "pressbanca"
     */
    fun normalize(name: String): String {
        return name.lowercase().replace("\\s+".toRegex(), "")
    }

    /**
     * Da formato correcto al nombre por primera vez antes de ser guardado, asegurando el espaciado y uso de mayúsculas estándar.
     * Ej. "press  banca" -> "Press banca"
     */
    fun formatFirstTime(name: String): String {
        val clean = name.trim().replace("\\s+".toRegex(), " ")
        if (clean.isEmpty()) return ""
        return clean.lowercase().replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
    }

    /**
     * Busca en el historial de nombres de ejercicios (nombre_snapshot) y asocia el nuevo nombre
     * con cualquier nombre existente (ignorando mayúsculas/minúsculas y espacios).
     * Si encuentra una coincidencia, devuelve el nombre exacto existente en el historial.
     * De lo contrario, devuelve el nombre formateado para primera vez.
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
     * Extrae una lista de nombres canónicos de ejercicios únicos a partir de una lista de nombres del historial.
     * Los agrupa por su forma normalizada y selecciona la mejor representación para cada grupo.
     */
    fun getCanonicalList(allNames: Collection<String>): List<String> {
        val grouped = allNames.groupBy { normalize(it) }
        return grouped.values.map { names ->
            // Busca el mejor nombre en el grupo:
            // 1. Primera opción: Tiene espacios y comienza con mayúscula
            val withSpacesAndUpper = names.filter { it.firstOrNull()?.isUpperCase() == true && it.contains(" ") }
            if (withSpacesAndUpper.isNotEmpty()) return@map withSpacesAndUpper.first()
            
            // 2. Segunda opción: Comienza con mayúscula
            val withUpper = names.filter { it.firstOrNull()?.isUpperCase() == true }
            if (withUpper.isNotEmpty()) return@map withUpper.first()
            
            // 3. De lo contrario: Da formato correcto al primer nombre de la lista
            formatFirstTime(names.firstOrNull() ?: "")
        }.filter { it.isNotEmpty() }.distinct().sorted()
    }
}
