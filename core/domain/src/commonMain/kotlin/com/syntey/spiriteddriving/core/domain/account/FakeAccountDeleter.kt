package com.syntey.spiriteddriving.core.domain.account

/**
 * [AccountDeleter] that counts the deletions it got. Throws [failWith] instead of deleting, when it's set.
 */
class FakeAccountDeleter : AccountDeleter {

    var deleteCount: Int = 0
        private set

    var failWith: Throwable? = null

    override suspend fun delete() {
        deleteCount++
        failWith?.let { throw it }
    }
}
