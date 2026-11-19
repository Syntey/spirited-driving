package com.syntey.spiriteddriving.core.domain.work

/**
 * Runs work that has to finish after the screen that started it closes, such as a delete whose Undo was still open.
 * It lives as long as the app. What fails is logged, not thrown.
 */
interface AppWork {

    fun launch(block: suspend () -> Unit)
}
