package com.syntey.spiriteddriving.feature.routes.model.ui

import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RenameDialogTest {

    @Test
    fun `a changed name that no other route has can be saved`() {
        assertTrue(dialog().canSave("Sunday loop"))
    }

    @Test
    fun `the name the route already has can't be saved`() {
        assertFalse(dialog().canSave("  Karlštejn Castle "))
    }

    @Test
    fun `a blank name can't be saved`() {
        assertFalse(dialog().canSave("   "))
    }

    @Test
    fun `a name of 40 characters can be saved and one of 41 can't`() {
        assertTrue(dialog().canSave("a".repeat(40)))
        assertFalse(dialog().canSave("a".repeat(41)))
    }

    @Test
    fun `a name another route has can't be saved whatever its case or spaces`() {
        assertTrue(dialog().isTaken(" křivoklát "))
        assertFalse(dialog().canSave(" křivoklát "))
    }

    @Test
    fun `changing only the case of the route's own name can be saved`() {
        assertTrue(dialog().canSave("karlštejn castle"))
    }

    private fun dialog() = RenameDialog(
        savedRouteId = "saved-1",
        currentName = "Karlštejn Castle",
        otherNames = persistentListOf("Křivoklát"),
    )
}
