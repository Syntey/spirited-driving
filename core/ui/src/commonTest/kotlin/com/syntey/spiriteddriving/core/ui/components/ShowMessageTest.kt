package com.syntey.spiriteddriving.core.ui.components

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShowMessageTest {

    @Test
    fun `message without action is gone after four seconds`() = runTest {
        val hostState = SnackbarHostState()
        val result = async { hostState.showMessage("Saved") }
        runCurrent()
        assertNotNull(hostState.currentSnackbarData)

        advanceTimeBy(4_000)
        runCurrent()

        assertNull(hostState.currentSnackbarData)
        assertFalse(result.await())
    }

    @Test
    fun `message with action is still shown at four point nine seconds`() = runTest {
        val hostState = SnackbarHostState()
        async { hostState.showMessage("Route deleted", "Undo") }
        runCurrent()

        advanceTimeBy(4_900)
        runCurrent()

        assertNotNull(hostState.currentSnackbarData)
    }

    @Test
    fun `message with action is gone after five seconds`() = runTest {
        val hostState = SnackbarHostState()
        val result = async { hostState.showMessage("Route deleted", "Undo") }
        runCurrent()

        advanceTimeBy(5_000)
        runCurrent()

        assertNull(hostState.currentSnackbarData)
        assertFalse(result.await())
    }

    @Test
    fun `tapping the action returns true`() = runTest {
        val hostState = SnackbarHostState()
        val result = async { hostState.showMessage("Route deleted", "Undo") }
        runCurrent()

        hostState.currentSnackbarData?.performAction()
        runCurrent()

        assertTrue(result.await())
    }
}
