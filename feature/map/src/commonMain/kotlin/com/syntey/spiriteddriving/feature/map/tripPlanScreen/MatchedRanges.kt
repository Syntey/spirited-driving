package com.syntey.spiriteddriving.feature.map.tripPlanScreen

/**
 * The parts of [name] that the typed [text] matches, for showing them bold. The typed text is split into words on whitespace.
 * A word of [name] that starts with one of them, counting from its first character, gives the matched part. Case and
 * diacritics don't count, so "karlst" matches "Karlštejn". Overlapping parts are merged.
 */
internal fun matchedRanges(name: String, text: String): List<IntRange> {
    val typedWords = text.split(WHITESPACE).filter { it.isNotEmpty() }.map(::foldText)
    if (typedWords.isEmpty()) return emptyList()
    // Folding keeps every character's position, so the ranges found in the folded name are the ones in the name
    val folded = foldText(name)
    val found = name.indices.filter { start -> start == 0 || !name[start - 1].isLetterOrDigit() }.mapNotNull { start ->
        val matchLength = typedWords.filter { folded.startsWith(it, startIndex = start) }.maxOfOrNull { it.length }
        matchLength?.let { start until start + it }
    }
    return merged(found)
}

/** Sorted by start, with overlapping ranges joined into one. */
private fun merged(ranges: List<IntRange>): List<IntRange> {
    val merged = mutableListOf<IntRange>()
    for (range in ranges.sortedBy { it.first }) {
        val last = merged.lastOrNull()
        if (last != null && range.first <= last.last) {
            merged[merged.lastIndex] = last.first..maxOf(last.last, range.last)
        } else {
            merged += range
        }
    }
    return merged
}

/** Lower case, with each diacritic folded to its base letter, one character to one character. */
private fun foldText(text: String): String = buildString(text.length) {
    for (char in text) append(FOLDED[char.lowercaseChar()] ?: char.lowercaseChar())
}

private val WHITESPACE = Regex("\\s+")

private val FOLDED: Map<Char, Char> = buildMap {
    fun fold(base: Char, variants: String) = variants.forEach { put(it, base) }
    fold('a', "áàâäãąå")
    fold('c', "čćç")
    fold('d', "ď")
    fold('e', "éěèêëę")
    fold('i', "íìîï")
    fold('l', "ł")
    fold('n', "ňñń")
    fold('o', "óöőôòõø")
    fold('r', "ř")
    fold('s', "šśß")
    fold('t', "ť")
    fold('u', "úůüűùû")
    fold('y', "ýÿ")
    fold('z', "žźż")
}
