package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import kotlin.test.Test
import kotlin.test.assertEquals

class MatchedRangesTest {

    @Test
    fun `typed text matches a name with a diacritic at its start`() {
        assertEquals(listOf(0..5), matchedRanges(name = "Karlštejn", text = "karlst"))
    }

    @Test
    fun `matching ignores case`() {
        assertEquals(listOf(0..5), matchedRanges(name = "Karlštejn", text = "KARLST"))
    }

    @Test
    fun `a name matched only in another language gets no ranges`() {
        assertEquals(emptyList(), matchedRanges(name = "Prague", text = "praha"))
    }

    @Test
    fun `each typed word matches the start of a word in the name`() {
        assertEquals(listOf(0..2, 8..11), matchedRanges(name = "Náměstí Míru", text = "nam miru"))
    }

    @Test
    fun `text inside a word does not match`() {
        assertEquals(emptyList(), matchedRanges(name = "Karlštejn", text = "stejn"))
    }

    @Test
    fun `a word after a bracket matches`() {
        assertEquals(listOf(11..14), matchedRanges(name = "Karlštejn (hrad)", text = "hrad"))
    }

    @Test
    fun `empty text matches nothing`() {
        assertEquals(emptyList(), matchedRanges(name = "Karlštejn", text = ""))
    }

    @Test
    fun `overlapping matches are merged into one range`() {
        assertEquals(listOf(0..4), matchedRanges(name = "ab-cd", text = "ab-c cd"))
    }
}
