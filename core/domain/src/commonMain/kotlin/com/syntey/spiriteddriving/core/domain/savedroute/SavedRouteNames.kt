package com.syntey.spiriteddriving.core.domain.savedroute

/** The most characters a name the user types may have. */
const val SAVED_ROUTE_NAME_MAX_LENGTH: Int = 40

/**
 * Whether one of these routes, other than [exceptId], is called [name]. Names are compared trimmed and ignoring case, so
 * "Karlštejn castle " is taken by "Karlštejn Castle".
 */
fun List<SavedRoute>.hasName(name: String, exceptId: String? = null): Boolean {
    val key = name.nameKey()
    return any { it.id != exceptId && it.name.nameKey() == key }
}

/**
 * The name offered when [name] is taken: "Karlštejn Castle from Smíchov" with the start's [startArea], or [name] without
 * one, followed by the first number that makes it free when that's taken too ("Karlštejn Castle from Smíchov 2"). It's cut
 * to [SAVED_ROUTE_NAME_MAX_LENGTH], before the number, so the name dialog can save it as it is.
 */
fun List<SavedRoute>.freeNameFor(name: String, startArea: String?): String {
    val base = startArea?.let { "$name from $it" } ?: name
    val cut = base.fitting(suffix = "")
    if (!hasName(cut)) return cut
    return generateSequence(2) { it + 1 }.map { number -> "${base.fitting(suffix = " $number")} $number" }.first { !hasName(it) }
}

private fun String.fitting(suffix: String): String = take(SAVED_ROUTE_NAME_MAX_LENGTH - suffix.length).trimEnd()

private fun String.nameKey(): String = trim().lowercase()
