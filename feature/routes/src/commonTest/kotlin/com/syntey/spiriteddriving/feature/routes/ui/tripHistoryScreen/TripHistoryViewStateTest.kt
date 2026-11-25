package com.syntey.spiriteddriving.feature.routes.ui.tripHistoryScreen

import com.syntey.spiriteddriving.core.domain.drive.FakeTripHistorySource
import com.syntey.spiriteddriving.core.domain.drive.PastDrivePage
import kotlinx.collections.immutable.persistentSetOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TripHistoryViewStateTest {

    @Test
    fun `nothing kept is empty and has no more`() {
        val state = TripHistoryViewState(page = null)

        assertTrue(state.isEmpty)
        assertFalse(state.hasMore)
        assertTrue(state.sections.isEmpty())
    }

    @Test
    fun `a page without drives is empty`() {
        assertTrue(TripHistoryViewState(page = PastDrivePage(drives = emptyList(), nextCursor = null)).isEmpty)
    }

    @Test
    fun `a page with a next cursor has more`() {
        assertTrue(TripHistoryViewState(page = PastDrivePage(drives = listOf(drive("a")), nextCursor = "1")).hasMore)
    }

    @Test
    fun `the last page has no more`() {
        assertFalse(TripHistoryViewState(page = PastDrivePage(drives = listOf(drive("a")), nextCursor = null)).hasMore)
    }

    @Test
    fun `the drives come as sections of their rows, in the order kept`() {
        val state = TripHistoryViewState(
            page = PastDrivePage(drives = listOf(drive("a"), drive("b")), nextCursor = null),
            now = FakeTripHistorySource.STARTED_AT,
        )

        assertEquals(listOf(listOf("a", "b")), state.sections.map { section -> section.rows.map { it.id } })
    }

    @Test
    fun `a drive being deleted is left out of the sections until its delete is sent`() {
        val state = TripHistoryViewState(
            page = PastDrivePage(drives = listOf(drive("a"), drive("b")), nextCursor = null),
            now = FakeTripHistorySource.STARTED_AT,
            deletingIds = persistentSetOf("a"),
        )

        assertEquals(listOf(listOf("b")), state.sections.map { section -> section.rows.map { it.id } })
    }

    @Test
    fun `a drive being deleted still counts as kept and the list doesn't turn empty`() {
        val state = TripHistoryViewState(
            page = PastDrivePage(drives = listOf(drive("a")), nextCursor = null),
            deletingIds = persistentSetOf("a"),
        )

        assertFalse(state.isEmpty)
    }

    @Test
    fun `the delete confirmation is shown only while a drive is picked for it`() {
        assertTrue(TripHistoryViewState(deleteConfirmationId = "a").isDeleteConfirmationShown)
        assertFalse(TripHistoryViewState(deleteConfirmationId = null).isDeleteConfirmationShown)
    }

    @Test
    fun `clear is offered once the drives are loaded`() {
        val state = TripHistoryViewState(
            page = PastDrivePage(drives = listOf(drive("a")), nextCursor = null),
            status = TripHistoryStatus.Ready,
        )

        assertTrue(state.canClear)
    }

    @Test
    fun `clear is hidden while the history is empty`() {
        val state = TripHistoryViewState(
            page = PastDrivePage(drives = emptyList(), nextCursor = null),
            status = TripHistoryStatus.Ready,
        )

        assertFalse(state.canClear)
    }

    @Test
    fun `clear is hidden while the history is still loading`() {
        val state = TripHistoryViewState(page = PastDrivePage(drives = listOf(drive("a")), nextCursor = null))

        assertFalse(state.canClear)
    }

    private fun drive(id: String) = FakeTripHistorySource.Recorded.copy(id = id)
}
