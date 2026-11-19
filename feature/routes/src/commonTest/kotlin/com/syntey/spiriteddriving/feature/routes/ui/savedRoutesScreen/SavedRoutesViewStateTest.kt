package com.syntey.spiriteddriving.feature.routes.ui.savedRoutesScreen

import com.syntey.spiriteddriving.core.domain.savedroute.FakeSavedRoutesSource
import com.syntey.spiriteddriving.core.domain.savedroute.SavedRoute
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SavedRoutesViewStateTest {

    @Test
    fun `a route being deleted is left out of the rows until its message ends`() {
        val state = SavedRoutesViewState(
            savedRoutes = persistentListOf(route("a"), route("b")),
            deletingIds = persistentSetOf("a"),
        )

        assertEquals(listOf("b"), state.routes.map { it.id })
    }

    @Test
    fun `a route being deleted still counts as kept and the list doesn't turn empty`() {
        val state = SavedRoutesViewState(savedRoutes = persistentListOf(route("a")), deletingIds = persistentSetOf("a"))

        assertFalse(state.isEmpty)
    }

    @Test
    fun `nothing kept is empty`() {
        assertTrue(SavedRoutesViewState(savedRoutes = null).isEmpty)
    }

    private fun route(id: String): SavedRoute = FakeSavedRoutesSource.Recorded.copy(id = id)
}
