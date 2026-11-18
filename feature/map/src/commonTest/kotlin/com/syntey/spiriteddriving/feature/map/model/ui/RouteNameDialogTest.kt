package com.syntey.spiriteddriving.feature.map.model.ui

import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RouteNameDialogTest {

    private val dialog = RouteNameDialog(
        routeId = "route-1",
        takenName = "Karlštejn Castle",
        suggestion = "Karlštejn Castle from Smíchov",
        takenNames = persistentListOf("Karlštejn Castle"),
    )

    @Test
    fun `a free name can be saved`() {
        assertTrue(dialog.canSave("Sunday loop"))
    }

    @Test
    fun `a blank name can't be saved`() {
        assertFalse(dialog.canSave("   "))
    }

    @Test
    fun `a name longer than the maximum can't be saved`() {
        assertFalse(dialog.canSave("a".repeat(41)))
    }

    @Test
    fun `a name of exactly the maximum can be saved`() {
        assertTrue(dialog.canSave("a".repeat(40)))
    }

    @Test
    fun `a name in use can't be saved whatever its case and spaces`() {
        assertTrue(dialog.isTaken("  karlštejn castle "))
        assertFalse(dialog.canSave("  karlštejn castle "))
    }

    @Test
    fun `the suggestion of a free name can be saved`() {
        assertTrue(dialog.canSave(dialog.suggestion))
    }
}
