package com.syntey.spiriteddriving.core.domain.savedroute

import com.syntey.spiriteddriving.core.domain.tools.Constants

/**
 * Whether one of these routes, other than [exceptId], is called [name]. Names are compared trimmed and ignoring case, so
 * "Karlštejn castle " is taken by "Karlštejn Castle".
 */
fun List<SavedRoute>.hasName(name: String, exceptId: String? = null): Boolean =
    filter { it.id != exceptId }.map { it.name }.containsName(name)

/** Whether any of these names is [name], compared trimmed and ignoring case. */
fun Collection<String>.containsName(name: String): Boolean {
    val key = name.nameKey()
    return any { it.nameKey() == key }
}

/** Whether [name] may be saved: not blank once trimmed, and at most the maximum length. */
fun isSavableName(name: String): Boolean {
    val trimmed = name.trim()
    return trimmed.isNotEmpty() && trimmed.length <= Constants.SavedRoutes.NAME_MAX_LENGTH
}

/**
 * The name offered when [name] is taken: "Karlštejn Castle from Smíchov" with the start's [startArea], or [name] without
 * one, followed by the first number that makes it free when that's taken too ("Karlštejn Castle from Smíchov 2"). It's cut
 * to [Constants.SavedRoutes.NAME_MAX_LENGTH], before the number, so the name dialog can save it as it is.
 */
fun List<SavedRoute>.freeNameFor(name: String, startArea: String?): String {
    val base = startArea?.let { "$name from $it" } ?: name
    val cut = base.fitting(suffix = "")
    if (!hasName(cut)) return cut
    return generateSequence(2) { it + 1 }.map { number -> "${base.fitting(suffix = " $number")} $number" }.first { !hasName(it) }
}

private fun String.fitting(suffix: String): String = take(Constants.SavedRoutes.NAME_MAX_LENGTH - suffix.length).trimEnd()

private fun String.nameKey(): String = trim().lowercase()
