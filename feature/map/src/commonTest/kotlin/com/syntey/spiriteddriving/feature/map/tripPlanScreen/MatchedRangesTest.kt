package com.syntey.spiriteddriving.feature.map.tripPlanScreen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun `a name that starts with the typed text matches`() {
        assertTrue(placeMatches(name = "Karlštejn", area = null, text = "karlst"))
    }

    @Test
    fun `a word of the area matches`() {
        assertTrue(placeMatches(name = "Karlštejn", area = "Central Bohemian Region", text = "bohem"))
    }

    @Test
    fun `diacritics don't count when placing matches`() {
        assertTrue(placeMatches(name = "Křivoklát", area = null, text = "krivok"))
    }

    @Test
    fun `every typed word has to match`() {
        assertFalse(placeMatches(name = "Karlštejn", area = "Beroun", text = "karlst xyz"))
    }

    @Test
    fun `a match inside a word does not count`() {
        assertFalse(placeMatches(name = "Karlštejn", area = null, text = "stejn"))
    }

    @Test
    fun `blank text matches nothing`() {
        assertFalse(placeMatches(name = "Karlštejn", area = null, text = "  "))
    }
}
