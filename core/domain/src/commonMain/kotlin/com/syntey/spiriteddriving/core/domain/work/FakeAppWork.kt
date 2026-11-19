package com.syntey.spiriteddriving.core.domain.work

/**
 * [AppWork] that holds what it's given until [runAll], so a test can check that nothing runs before then. Like the
 * real one, a block that fails doesn't stop the others.
 */
class FakeAppWork : AppWork {

    val pending: List<suspend () -> Unit>
        field = mutableListOf<suspend () -> Unit>()

    override fun launch(block: suspend () -> Unit) {
        pending += block
    }

    suspend fun runAll() {
        val blocks = pending.toList()
        pending.clear()
        blocks.forEach { block -> runCatching { block() } }
    }
}
